# 调用已发布 API（给调用方）

发布后，工作流通过**在线执行入口**被调用。本页是给"调用方"的契约。

## 端点
```
POST /api/workflow
```
| 环境 | Base URL |
|---|---|
| UAT | `https://workflow-online-api-nr3e4.ondigitalocean.app` |
| 本地（H2 联调） | `http://localhost:8081` |

## 查询参数
| 参数 | 必需 | 说明 |
|---|---|---|
| `applicationName` | ✅ | 目标应用名（= 你要调的那个 API），如 `DEMO_PAY` |
| `confirmationNumber` | ✅ | 业务关联/确认值，随运行记录保存，供查询与幂等上下文。例 `PAY-20260621-0001` |
| `channelKind` | ❌ | 通道提示，如 `SMS` / `Email` |
| `isSelfRequest` | ❌ | 自请求标记（默认 `false`），用于特定重试/联名场景 |

## 必需请求头
| 头 | 必需 | 说明 |
|---|---|---|
| `Content-Type` | ✅ | `application/json` 或 `application/xml` |
| `X-Request-Correlation-Id` | ✅ | 请求关联 ID，用于**幂等/重复检测**。同应用同值只处理一次。 |

> ⚠️ 常见坑：少了 `X-Request-Correlation-Id` 会直接 `400`（`440000`：Required request header 'X-Request-Correlation-Id' ... not present）。

## 请求体
任意业务 JSON（或 XML）。会作为入站负载（`ingressBody`）进入运行。
```json
{ "customerId": "C-1001", "amount": 8800, "currency": "AUD" }
```

## 响应
| 情况 | 状态 | 说明 |
|---|---|---|
| 成功 | `200` | **空体**。执行结果请到[运行记录](#查看结果)看。 |
| 缺必需头 | `400` | `440000` |
| 未知应用 | `400` | `M0001`（No or more than one entity setting...） |
| 重复关联 ID | `400` | `M0002`（幂等：Duplicate per request correlation ID） |
| 重复自请求重试 | `400` | `M0004` |
| 系统错误 | `500` | 内部错误 |

完整错误码见[错误码表](error-codes.md)。

## 完整示例
```bash
curl -X POST \
  "https://workflow-online-api-nr3e4.ondigitalocean.app/api/workflow?applicationName=DEMO_PAY&confirmationNumber=PAY-20260621-0001" \
  -H "Content-Type: application/json" \
  -H "X-Request-Correlation-Id: 11111111-1111-1111-1111-111111111111" \
  -d '{ "customerId": "C-1001", "amount": 8800, "currency": "AUD" }'
```

## 同步 / 异步
按目标应用的配置：同步=处理完才返回；异步=立即返回、后台处理。无论哪种，结果都落在运行记录。

## 查看结果
界面 `/records`，按 `applicationName` 或 `confirmationNumber` 筛选，下钻看每步状态、通道状态、父子/重试链。
管理侧也可查询：`GET /api/workflow/records?applicationName=DEMO_PAY`（operation-api）。

## 复用：从另一个工作流调它
在另一个工作流里放一个 **CONSUMER** 节点，URL 指向本端点（带上 `applicationName` 等参数），即完成"API 组合"。

## 路线图提示
- "由工作流塑造同步响应体"、"每个 API 自定义路径/方法"、"入站鉴权/限流" 在路线图上（见 [`../../TODO-doc-gaps.md`](../../TODO-doc-gaps.md)）。
