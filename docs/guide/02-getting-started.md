# 入门指南 — 5 分钟做出第一个 API

跟着这一篇，你会从零做出一个**可调用的 API**：收到一条带 `customerId` / `amount` 的消息 → 调外部接口补全联系方式 → 高额时走分支 → 下发通知，并在运行记录里看到结果。

全程以示例 [`examples/payment-notify.workflow.json`](examples/payment-notify.workflow.json) 为主线。

> **环境**：界面在 https://workflow-ui-gamma.vercel.app （UAT）。后端调用地址见[调用参考](reference/api-call.md)。

---

## 第 0 步：心智模型（30 秒）
- **应用 = 你的 API**。给它起个名字（如 `DEMO_PAY`），它就是你这个 API 的标识。
- **画布 = 你的逻辑**。节点是步骤，连线是顺序。
- **规则 = 条件**。每个节点挂一个 JSONPath，命中才执行。
- **发布 = 让它能被调用**。

## 第 1 步：建应用
1. 打开 `/workflows`，点 **＋ New application**（移动端用右下角 FAB）。
2. 命名 `DEMO_PAY`，确定后进入画布。

## 第 2 步：在画布上摆三个节点
从左侧节点面板拖入并连线：

```
[CONSUMER 补全客户资料]  →  [IFELSE 高额分支]  →(true)→  [MESSAGE 下发通知]
```

- **CONSUMER（HTTP 丰富化）**：调外部接口，把返回合并进运行负载。
  - 方法 `GET`，URL `https://example.com/customers?id={{customerId}}`
  - 从响应提取：`$.profile.contact`
- **IFELSE（条件分支）**：高额走 true 分支。
- **MESSAGE（下发）**：把通知发给补全到的联系方式。

> 不想手摆？用画布工具栏的 **Import**，粘贴 [`examples/payment-notify.workflow.json`](examples/payment-notify.workflow.json) 一键导入即可。

## 第 3 步：配规则（JSONPath）
给每个节点配"命中才执行"的规则：

| 节点 | 规则 key | 含义 |
|---|---|---|
| CONSUMER | `$.customerId` | 有 customerId 才补全 |
| IFELSE | `$.amount` | 有金额才分支 |
| MESSAGE | `$.profile.contact` | 补全到联系方式才通知 |

规则语法见[规则参考](reference/rules-jsonpath.md)。

## 第 4 步：试跑
点工具栏 **Run / Test**，填入示例输入：

```json
{ "customerId": "C-1001", "amount": 8800, "currency": "AUD" }
```

观察每步是否按规则命中执行。试跑通过后再发布。

## 第 5 步：发布（Deploy）→ 你的 API 就上线了
点应用操作区的 **Deploy**，填表单（目标 URL / 应用名 / 服务账号 / 环境），平台按序完成发布。发布后，这个工作流即可通过统一入口被调用。

## 第 6 步：调用你的 API
用 `curl` 调用（必需头 `X-Request-Correlation-Id` 用于幂等；`confirmationNumber` 是业务关联键）：

```bash
curl -X POST \
  "https://workflow-online-api-nr3e4.ondigitalocean.app/api/workflow?applicationName=DEMO_PAY&confirmationNumber=PAY-20260621-0001" \
  -H "Content-Type: application/json" \
  -H "X-Request-Correlation-Id: 11111111-1111-1111-1111-111111111111" \
  -d '{ "customerId": "C-1001", "amount": 8800, "currency": "AUD" }'
```

- **成功**：返回 `200`（空体）。⚠️ 当前结果**不在响应体里**，去运行记录看（见下）。
- **未知应用**：`400` + `M0001`。
- **缺 `X-Request-Correlation-Id`**：`400`。
- **同一关联 ID 重发**：被识别为重复，`400` + `M0002`（幂等）。

完整契约见[调用参考](reference/api-call.md)。

## 第 7 步：看运行记录
打开 `/records`，按 `applicationName=DEMO_PAY` 或 `confirmationNumber` 筛选，下钻看每步结果、通道状态、父子/重试链。**这是你今天拿到"执行结果"的地方。**

---

## 你刚刚做了什么
你没写后端，就发布了一个 API：它会补全数据、按条件分支、下发通知，并且自带幂等、重试、可追溯记录。

## 接下来
- 想复用？把这个 API 当积木——在别的工作流里用一个 **CONSUMER** 节点调它的 URL 即可。
- 查字段/语法 → [节点参考](reference/nodes.md) · [工作流 JSON](reference/workflow-json.md)
- 自然语言生成工作流 → 工具栏 **Generate**；读懂一个工作流 → **Explain**
