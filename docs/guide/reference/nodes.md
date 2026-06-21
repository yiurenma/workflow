# 节点类型参考

共 **6 种**节点类型（后端线格式 `action.type`），在画布上由 3 个编辑器表单覆盖。每个节点 = 一个步骤，挂一条[规则](rules-jsonpath.md)（命中才执行）。

| `action.type` | 家族 | 编辑器表单 | 用途 |
|---|---|---|---|
| `CONSUMER` | HTTP 调用 | HttpCall | 调外部接口做丰富化；失败计入错误 |
| `CONSUMERWITHOUTERROR` | HTTP 调用 | HttpCall | 同上，但失败**不**中断/不计错误（尽力而为） |
| `IFELSE` | 逻辑/分支 | Logic | 二元条件分支（true/false） |
| `MESSAGE` | 逻辑/下发 | Logic | 下发阶段步骤（如通知） |
| `FUNCTION_V2` | 函数 | Function | 函数式步骤（provider + 逻辑） |
| `FUNCTION_V3` | 函数 | Function | 函数式步骤（新版） |

> 旧版 FUNCTION v1 不支持。

## 动作字段（`action.*`）
节点的"语法"由动作字段表达（来源：`WorkflowType`）：

| 字段 | 适用 | 说明 |
|---|---|---|
| `type` | 全部 | 上表 6 种之一（必填） |
| `provider` | 全部 | 展示/引用用的 provider 标识，不参与逻辑 |
| `remark` | 全部 | 自由文本备注 |
| `retryErrorCodes` | HTTP 类 | 逗号分隔的错误码，命中触发重试 |
| `elseLogic` | IFELSE | 规则不匹配时的回退逻辑（JSON/脚本，常 base64 编码） |
| `httpRequestMethod` | HTTP 类 | `GET`/`POST`/… |
| `httpRequestUrlWithQueryParameter` | HTTP 类 | 含查询参数的完整 URL |
| `internalHttpRequestUrlWithQueryParameter` | HTTP 类 | 内部服务端点 URL |
| `httpRequestHeaders` | HTTP 类 | 请求头（JSON/字符串） |
| `httpRequestBody` | HTTP 类 | 请求体模板 |
| `trackingNumberSchemaInHttpResponse` | HTTP 类 | 从响应提取（如跟踪号）的 JSONPath/schema |

## 各家族详解

### HTTP 调用（CONSUMER / CONSUMERWITHOUTERROR）
丰富化主力：按规则命中后发 HTTP 请求，把响应（按 `trackingNumberSchemaInHttpResponse` 等）合并进运行负载。
- **CONSUMER**：失败按错误处理（可配 `retryErrorCodes`）。
- **CONSUMERWITHOUTERROR**：失败不阻断后续，用于"可选补全"。
- 表单字段：描述、provider、remark、方法、外部 URL、内部 URL、请求头、请求体、响应提取。

示例：
```json
{ "type": "CONSUMER", "provider": "CustomerService",
  "httpRequestMethod": "GET",
  "httpRequestUrlWithQueryParameter": "https://example.com/customers?id={{customerId}}",
  "trackingNumberSchemaInHttpResponse": "$.profile.contact" }
```

### 逻辑（IFELSE / MESSAGE）
- **IFELSE**：规则命中走 true 分支；用 `elseLogic` 表达不命中时的回退。连线用 `IFELSE_<id>_true` / `IFELSE_<id>_false` 表达分支端点。
- **MESSAGE**：下发阶段步骤（通知类）。表单字段：描述、provider（如 `SYSTEM`）、remark。

示例：
```json
{ "type": "IFELSE", "elseLogic": "eyJsb3ciOiB0cnVlfQ==" }
```

### 函数（FUNCTION_V2 / FUNCTION_V3）
函数式步骤，表单字段：provider、remark、逻辑（logic）。用于平台内置的函数型处理。

## 连线与分支
普通连线 `{"id","source","target"}`；IFELSE 分支端点用虚拟 ID `IFELSE_<nodeId>_true|false`，无需在 `pluginList` 里单列条目（校验已对此放宽）。详见[工作流 JSON](workflow-json.md)。

## 下一步
→ [规则语言（JSONPath）](rules-jsonpath.md)
