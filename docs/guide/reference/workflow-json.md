# 工作流 JSON Schema

工作流可作为 JSON 导入/导出——这是它的"源码格式"。画布的 **Import** 会校验后应用，**Export / Save** 产出此结构。

## 顶层结构
```json
{
  "pluginList": [ /* 节点 */ ],
  "uiMapList":  [ /* 连线 */ ]
}
```

## 节点（`pluginList[]`）
```json
{
  "id": 1,
  "description": "Fetch customer profile",
  "linkingIdOfRuleListAndAction": "rule-1",
  "ruleList": [
    { "key": "$.customerId", "remark": "run only when a customerId is present" }
  ],
  "action": {
    "type": "CONSUMER",
    "provider": "CustomerService",
    "httpRequestMethod": "GET",
    "httpRequestUrlWithQueryParameter": "https://example.com/customers?id={{customerId}}",
    "trackingNumberSchemaInHttpResponse": "$.profile.contact"
  }
}
```

| 字段 | 说明 |
|---|---|
| `id` | 节点 ID（唯一） |
| `description` | 画布上显示的节点标签 |
| `linkingIdOfRuleListAndAction` | 规则与动作的关联 ID |
| `ruleList[]` | 规则数组，每条 `{ key, remark }`（`key` 为单个 JSONPath） |
| `action` | 动作对象，字段见[节点参考](nodes.md) |

## 连线（`uiMapList[]`）
```json
{ "id": "e1", "source": "1", "target": "2" }
```
- IFELSE 分支用虚拟端点 ID：`IFELSE_<nodeId>_true` / `IFELSE_<nodeId>_false`，例：
```json
{ "id": "e2", "source": "2", "target": "IFELSE_2_true" }
```

## 导入校验规则（与画布 Import 一致）
导入时会校验，**通过**才允许应用到画布：
1. **必填字段**齐全（结构合法的 JSON）。
2. **插件类型合法**：仅接受 6 种线格式类型 `CONSUMER` / `CONSUMERWITHOUTERROR` / `IFELSE` / `MESSAGE` / `FUNCTION_V2` / `FUNCTION_V3`（**不**接受 UI 枚举名如 `HTTP_CALL`、`LOGIC`）。
3. **节点 ID 唯一**。
4. **规则键为单个 JSONPath**（见[规则参考](rules-jsonpath.md)）。
5. **已放宽**：`uiMapList` 的边引用**不强制**其 `source`/`target` 必须存在于 `pluginList`（允许字符串 ID、外部格式、IFELSE 分支虚拟 ID、部分拓扑）。
- 导入会**剥离 markdown 代码围栏**（```），并显示预览摘要（节点数、警告）。画布非空时确认替换。

> Import 用于"结构化 JSON 验证后替换画布"；自然语言生成请用 **Generate**（二者分离）。

## 可直接用的示例
[`../examples/payment-notify.workflow.json`](../examples/payment-notify.workflow.json) —— CONSUMER → IFELSE → MESSAGE，已实测可创建/导入。

## 下一步
→ [调用已发布 API](api-call.md)
