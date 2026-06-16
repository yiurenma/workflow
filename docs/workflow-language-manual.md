# Workflow DSL 语言说明书

> 本说明书根据 `workflow-ui`、`workflow-operation-api`、`workflow-online-api` 的公开仓库 README 与关键实现文件整理。当前运行环境无法通过 `git clone` 下载 submodule（GitHub HTTPS 被代理 403），所以本文把该项目实际持久化在数据库中的“工作流配置语言”命名为 **Workflow DSL**，用于说明它的核心概念、语法结构与执行语义。

## 1. 语言定位

Workflow DSL 不是传统文本编程语言，而是一种以数据库记录、JSON 模板、JsonPath 规则、Base64 编码片段和 HTTP/function 配置组成的轻量级工作流语言。它的目标是让用户通过 UI 或 API 定义：

- 哪个应用使用哪条工作流；
- 工作流包含哪些步骤；
- 每个步骤在什么条件下执行；
- 每个步骤执行 HTTP 调用、条件分支、函数调用或消息派发；
- 执行过程如何写入 `WORKFLOW_RECORD`，并形成可追踪的运行记录。

从系统边界看：

- `workflow-ui` 是工作流编辑 UI，使用 React、TypeScript、Vite、Ant Design 和 TanStack Router。
- `workflow-operation-api` 是控制面服务，负责定义和管理 workflows、rules、types、entity settings 等配置。
- `workflow-online-api` 是运行面服务，只暴露 `POST /api/workflow` 作为工作流执行入口。

## 2. 运行模型总览

一次工作流执行从 HTTP 请求开始：

```text
Client
  → POST /api/workflow
  → WorkflowOnlineController
  → WORKFLOW_RECORD 初始记录
  → WorkflowDispatchService
  → enrichment steps
  → dispatch steps
  → WORKFLOW_RECORD 子记录 / 状态 / tracking number
```

运行时主要分为三层：

1. **入口层**：接收 JSON 或 XML 请求，按 `applicationName` 找到唯一启用的实体配置。
2. **编排层**：按 `logicOrder` 读取步骤，每个步骤通过 `linkingId` 绑定一组 rule/type。
3. **执行层**：先执行 enrichment 类步骤，再执行 dispatch 类步骤；每个步骤先判断规则，规则全部命中才执行动作。

## 3. 核心对象

### 3.1 Entity Setting：应用级入口配置

`WORKFLOW_ENTITY_SETTING` 表示某个应用的一条工作流入口配置。它把外部请求里的 `applicationName` 映射到具体工作流定义。

主要字段：

| 字段 | 含义 |
|---|---|
| `applicationName` | 应用名，运行入口用它查找工作流 |
| `retry` | 是否启用重试 |
| `tracking` | 是否启用 tracking |
| `trackingServiceProviderActionId` | 主 workflow type |
| `trackingServiceProviderActionId2` | 备用 workflow type |
| `enabled` | 是否启用 |
| `retryProperties` | 重试配置，JSON 或文本 |
| `asyncMode` | 是否异步执行 enrichment 和 dispatch |
| `workflow` | Base64 编码的 UI 工作流图信息 |
| `description` | 人类可读描述 |

### 3.2 Entity-Link Mapping：步骤顺序

`WORKFLOW_ENTITY_AND_LINKING_ID_MAPPING` 用于把一个 entity setting 连接到多个逻辑步骤。

| 字段 | 含义 |
|---|---|
| `workflowEntitySetting` | 所属应用配置 |
| `linkingId` | 步骤 ID；后续 rule/type mapping 也用它关联 |
| `logicOrder` | 步骤顺序；数值小的先执行 |
| `remark` | 备注 |

### 3.3 Rule：条件表达式

`WORKFLOW_RULE` 是条件规则表。它的 `rule_key` 字段是一个 **Jayway JsonPath 表达式**。运行时会把整个 runtime payload 序列化为 JSON，然后用 JsonPath 读取；如果读取结果非空，则认为规则命中。

示例：

```jsonpath
$.ingressBody[?(@.channel == 'SMS')]
```

```jsonpath
$.contactProfile[?(@.customerId)]
```

### 3.4 Type：动作定义

`WORKFLOW_TYPE` 描述步骤真正要执行什么动作。

主要字段：

| 字段 | 含义 |
|---|---|
| `provider` | provider 标识，主要用于展示或记录 |
| `type` | 动作类型，例如 `CONSUMER`、`IFELSE`、`FUNCTION_V2`、`MESSAGE` |
| `remark` | 说明 |
| `elseLogic` | 条件分支或函数配置，通常是 Base64 编码 JSON |
| `httpRequestMethod` | HTTP 方法 |
| `httpRequestUrlWithQueryParameter` | 外部 HTTP URL |
| `internalHttpRequestUrlWithQueryParameter` | 内部 HTTP URL |
| `httpRequestHeaders` | HTTP headers 模板 |
| `httpRequestBody` | HTTP body 模板 |
| `trackingNumberSchemaInHttpResponse` | 从响应中提取 tracking number 或构造子 payload 的模板，通常 Base64 编码 |

### 3.5 Rule-Type Mapping：条件与动作绑定

`WORKFLOW_RULE_AND_TYPE_MAPPING` 把 `WorkflowRule` 和 `WorkflowType` 绑定到同一个 `linkingId`。

一个 `linkingId` 可以绑定多条规则。执行语义是：

```text
同一个 linkingId 下的所有 rule 全部命中 → 执行对应 type
任一 rule 不命中 → 跳过该步骤
```

### 3.6 Record：运行记录

`WORKFLOW_RECORD` 是运行时记录表。它保存：

- 应用名；
- correlation id；
- confirmation number；
- workflow linking id；
- tracking number；
- 加密后的 runtime payload；
- provider 响应；
- overall status；
- origin workflow record 等。

## 4. 程序结构

Workflow DSL 的“程序”可以理解成以下结构：

```yaml
application: <applicationName>
settings:
  asyncMode: true|false
steps:
  - order: 10
    linkingId: enrich-customer
    rules:
      - <JsonPath expression>
    type:
      kind: CONSUMER
      http:
        method: POST
        url: ...
        headers: ...
        body: ...
      outputTemplate: <Base64 JSON template>

  - order: 20
    linkingId: choose-channel
    rules:
      - <JsonPath expression>
    type:
      kind: IFELSE
      elseLogic: <Base64 JSON template>

  - order: 30
    linkingId: send-message
    rules:
      - <JsonPath expression>
    type:
      kind: MESSAGE
      http:
        method: POST
        url: ...
        body: ...
      trackingNumberSchemaInHttpResponse: <Base64 template>
```

注意：这不是系统实际存储格式，而是为了说明语言结构抽象出来的等价表示。实际系统把这些信息拆在多张表里。

## 5. 语法元素

### 5.1 标识符

Workflow DSL 中最重要的标识符是：

| 标识符 | 作用 |
|---|---|
| `applicationName` | 工作流入口名 |
| `linkingId` | 步骤标识；连接 step、rules、type |
| `logicOrder` | 步骤顺序 |
| `provider` | 动作提供方 |
| `type` | 动作类型 |
| `requestCorrelationId` | 请求幂等/重复检测 ID |
| `confirmationNumber` | 业务查询键或确认号 |

### 5.2 条件表达式：JsonPath

规则使用 Jayway JsonPath。判断方式不是返回布尔值，而是：

```text
JsonPath 读取结果为空       → false
JsonPath 读取结果非空       → true
同一 linkingId 多条规则全部 true → 执行动作
```

示例：

```jsonpath
$.ingressBody[?(@.amount > 1000)]
```

```jsonpath
$.workflowEntitySetting[?(@.tracking == true)]
```

```jsonpath
$.channelKind[?(@ == 'SMS')]
```

### 5.3 模板表达式

系统大量使用变量替换：

```text
${...}
```

具体起止符来自 `AppConstant.VARIABLE_BEGIN_STRING` 和 `AppConstant.VARIABLE_END_STRING`。模板可以出现在：

- HTTP URL；
- HTTP headers；
- HTTP body；
- `elseLogic`；
- `trackingNumberSchemaInHttpResponse`；
- function input/output 参数。

模板执行时会用当前 `WorkflowRuntimePayload` 或 HTTP 响应对象替换变量。

### 5.4 Base64 编码片段

以下字段经常保存 Base64 编码后的 JSON/template：

- `elseLogic`
- `trackingNumberSchemaInHttpResponse`

执行时先 Base64 decode，再变量替换，再按 JSON 或字符串解析。

## 6. 类型系统 / 动作类型

动作类型保存在 `WORKFLOW_TYPE.type` 中。当前代码支持：

| 类型 | 含义 |
|---|---|
| `CONSUMER` | 调用 HTTP consumer，并把响应映射成子 runtime payload |
| `CONSUMERWITHOUTERROR` | 类似 `CONSUMER`，但异常会被吞掉，后续步骤继续执行 |
| `IFELSE` | 不调用外部服务，直接用 `elseLogic` 模板生成子 payload |
| `FUNCTION` | 旧版 Java 反射函数调用，只读兼容历史记录，不建议新增 |
| `FUNCTION_V2` | Java 反射函数调用，把函数返回值包到 `reference` 再映射输出 |
| `FUNCTION_V3` | 当前与 `FUNCTION_V2` 调用方式相同，预留未来差异化 |
| `MESSAGE` | 派发/dispatch 类型；代码里枚举名是 `DISPATCH`，但持久化值是 `MESSAGE` |
| `TRACKING` | tracking 类型；枚举存在，当前主要作为类型值使用 |

## 7. 执行语义

### 7.1 入口校验

执行入口是：

```http
POST /api/workflow?confirmationNumber=...&applicationName=...
X-Request-Correlation-Id: <uuid>
Content-Type: application/json 或 application/xml
```

入口语义：

1. 如果 body 是 XML，先转换成 JSON。
2. 使用 `requestCorrelationId + applicationName` 做重复请求检测。
3. 根据 `applicationName` 查 `WORKFLOW_ENTITY_SETTING`。
4. 必须且只能找到一条配置，否则报错。
5. 创建 `WORKFLOW_RECORD`，初始状态为 `INITIATION`。
6. 构造 `WorkflowRuntimePayload`，加密后写入 `workflowTransactionDetails`。
7. 根据 `asyncMode` 选择同步或异步执行。

### 7.2 步骤分组

系统先读取 entity setting 下的所有 `WorkflowEntityAndLinkingIdMapping`，再按 `linkingId` 查对应 rule/type bindings。

步骤分成两类：

1. **Enrichment 类**：`CONSUMER`、`CONSUMERWITHOUTERROR`、`IFELSE`、`FUNCTION`、`FUNCTION_V2`、`FUNCTION_V3`
2. **Dispatch 类**：`MESSAGE`

执行顺序是：

```text
全部 enrichment steps → 保存 GI 状态 → 全部 dispatch steps
```

### 7.3 规则匹配

一个 step 可以有多条 rule：

```text
for rule in rules:
  result = JsonPath.read(runtimePayload, rule.key)
  if result is empty:
    skip step
if all rules matched:
  execute type
```

### 7.4 Enrichment 执行

Enrichment 的结果是一个新的 JSON 片段，通常会合并或影响 runtime payload。

- `CONSUMER`：发 HTTP 请求，使用响应和 `trackingNumberSchemaInHttpResponse` 模板生成 JSON。
- `CONSUMERWITHOUTERROR`：同上，但失败不阻断整个 pipeline。
- `IFELSE`：直接执行 `elseLogic` 模板，适合写条件分支或默认值。
- `FUNCTION*`：通过 Java reflection 调用指定类和方法，然后把返回值映射成 JSON。

### 7.5 Dispatch 执行

`MESSAGE` 类型用于真正对外派发，例如短信、邮件、推送或其他 provider。

执行时会：

1. 生成新的 workflow instance id；
2. 记录 provider；
3. 调用 HTTP；
4. 用 `trackingNumberSchemaInHttpResponse` 从响应中提取 tracking number；
5. 如果 tracking number 非空，状态为 `SM_SUCCESS`；否则为 `SM_FAIL`；
6. 生成子 `WORKFLOW_RECORD`；
7. 删除或替换父记录。

## 8. 状态语义

常见运行状态包括：

| 状态 | 含义 |
|---|---|
| `INITIATION` | 入口记录已创建 |
| `GI_SUCCESS` | gather/enrichment 成功 |
| `GI_FAIL` | gather/enrichment 失败 |
| `SM_SUCCESS` | dispatch/message 成功 |
| `SM_FAIL` | dispatch/message 失败 |

## 9. 示例：短信派发工作流

下面用抽象 YAML 表示一个简单工作流：

```yaml
application: AU_PAY_TO
settings:
  asyncMode: true

steps:
  - order: 10
    linkingId: load-contact-profile
    rules:
      - $.ingressBody[?(@.customerId)]
    type:
      kind: CONSUMER
      provider: CUSTOMER_PROFILE
      httpRequestMethod: GET
      httpRequestUrlWithQueryParameter: https://profile.example.com/customers/${ingressBody.customerId}
      trackingNumberSchemaInHttpResponse: base64('{"contactProfile": ${reference}}')

  - order: 20
    linkingId: sms-body
    rules:
      - $.contactProfile[?(@.mobileNumber)]
    type:
      kind: IFELSE
      elseLogic: base64('{"message": "Your confirmation is ${requestSearchKey}"}')

  - order: 30
    linkingId: send-sms
    rules:
      - $.message[?(@)]
      - $.contactProfile[?(@.mobileNumber)]
    type:
      kind: MESSAGE
      provider: SMS_PROVIDER
      httpRequestMethod: POST
      httpRequestUrlWithQueryParameter: https://sms.example.com/send
      httpRequestBody: '{"to":"${contactProfile.mobileNumber}","text":"${message}"}'
      trackingNumberSchemaInHttpResponse: base64('${reference.trackingId}')
```

等价含义：

1. 只有请求体里有 `customerId` 才查询客户资料。
2. 只有客户资料里有手机号才构造短信内容。
3. 只有短信内容和手机号都存在才发送短信。
4. provider 返回 tracking id 后记录成功。

## 10. 常见写法

### 10.1 无异常 enrichment

如果一个 enrichment 是可选增强，不应该因为它失败而阻断工作流，使用：

```text
CONSUMERWITHOUTERROR
```

典型场景：

- 获取非关键画像；
- 获取营销标签；
- 获取可选偏好设置。

### 10.2 纯条件分支

如果不需要外部调用，只想根据已有 payload 构造新字段，使用：

```text
IFELSE
```

例如根据 channelKind 生成不同消息模板。

### 10.3 Java 函数扩展

当模板能力不够时，可以使用：

```text
FUNCTION_V2
FUNCTION_V3
```

函数配置一般放在 Base64 编码的 `elseLogic` 中，内容包含：

- className；
- methodName；
- inputParameterList；
- outputParameter。

注意：这类步骤依赖后端 classpath 中存在对应 Java 类和方法。

## 11. 最佳实践

1. **每个 applicationName 只保留一条有效配置**：入口要求按 applicationName 只能查到一条 setting。
2. **linkingId 要稳定**：它是步骤和 rule/type 绑定的核心键。
3. **logicOrder 留间隔**：建议使用 10、20、30，便于中间插入新步骤。
4. **rule 尽量小而明确**：同一 linkingId 下多条 rule 是 AND 关系。
5. **可选外部调用使用 CONSUMERWITHOUTERROR**：避免非核心系统故障阻塞主流程。
6. **dispatch 步骤只做最终派发**：enrichment 和 dispatch 分层，便于排查。
7. **模板字段统一 Base64 管理**：避免 JSON 转义混乱。
8. **新功能优先使用 FUNCTION_V2/V3，不再新增 FUNCTION**：`FUNCTION` 是历史兼容类型。
9. **trackingNumberSchemaInHttpResponse 要返回稳定字符串**：它直接影响 `SM_SUCCESS` / `SM_FAIL` 判断。
10. **使用 correlation id 保证幂等**：同一 applicationName 下重复 request id 会被拒绝。

## 12. 调试指南

### 12.1 规则未命中

检查：

- JsonPath 是否以 `$` 开头；
- runtime payload 中字段路径是否真实存在；
- 过滤表达式是否返回数组；
- 同一 linkingId 下是否有某条 rule 未命中。

### 12.2 enrichment 失败

检查：

- HTTP URL / headers / body 模板是否替换成功；
- provider 是否返回预期 JSON；
- `trackingNumberSchemaInHttpResponse` decode 后是否是合法模板；
- 是否应该改用 `CONSUMERWITHOUTERROR`。

### 12.3 dispatch 失败

检查：

- `type` 是否存成 `MESSAGE`；
- provider 是否不是特殊值 `SYSTEM`；
- tracking number 模板是否能从响应中取到非空值；
- `workflowResponseFromProvider` 中是否记录异常。

## 13. 语言速查表

| 概念 | 存储位置 | 作用 |
|---|---|---|
| Program | `WORKFLOW_ENTITY_SETTING` + mappings | 一条应用工作流 |
| Step | `WORKFLOW_ENTITY_AND_LINKING_ID_MAPPING` | 一个有序步骤 |
| Condition | `WORKFLOW_RULE.rule_key` | JsonPath 条件 |
| Action | `WORKFLOW_TYPE` | HTTP / IFELSE / function / dispatch |
| Binding | `WORKFLOW_RULE_AND_TYPE_MAPPING` | 把条件和动作绑定到 step |
| Runtime | `WorkflowRuntimePayload` | 执行上下文 |
| Record | `WORKFLOW_RECORD` | 运行日志和状态 |

