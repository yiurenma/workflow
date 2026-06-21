# Demo 演示脚本

用于录屏 / 路演 / 直播。总时长约 **5–6 分钟**。主线 = 示例工作流 `payment-notify`。

## 准备
- 打开 https://workflow-ui-gamma.vercel.app
- 备好示例 JSON：[`../guide/examples/payment-notify.workflow.json`](../guide/examples/payment-notify.workflow.json)
- 备好一个终端（演示 curl）

## 脚本

### 0:00 — 钩子（15s）
> "做集成时，收到一条消息要补全数据、按条件下发、还要幂等和留痕——通常得写一个后端。今天我不写一行后端，直接做出这个 API。"

### 0:15 — 建应用（30s）
- `/workflows` → **＋ New application** → 命名 `DEMO_PAY`。
- 旁白："建一个应用，它就是我这个 API 的标识。"

### 0:45 — 导入工作流（45s）
- 画布工具栏 **Import** → 粘贴示例 JSON → 预览摘要 → 应用。
- 旁白："三个节点：调外部接口补全联系方式、按金额分支、下发通知。连线就是顺序，节点上的 JSONPath 就是条件。"

### 1:30 — 讲规则（30s）
- 点开 CONSUMER 节点 → 指 `$.customerId`；IFELSE → `$.amount`；MESSAGE → `$.profile.contact`。
- 旁白："命中规则才执行这步，不命中就跳过。"

### 2:00 — 试跑（45s）
- **Run / Test** → 输入 `{ "customerId": "C-1001", "amount": 8800 }` → 看每步命中。
- 旁白："发布前先试跑，确认逻辑对。"

### 2:45 — AI Explain / Generate（45s，可选亮点）
- 点 **Explain** → 展示 AI 用自然语言解释这个工作流。
- （可选）**Generate** → 输入一句话生成一个新工作流骨架。
- 旁白："AI 能解释，也能从一句话生成。"

### 3:30 — 发布（30s）
- **Deploy** → 填表单 → 完成。
- 旁白："一键发布，这个 API 就上线了。"

### 4:00 — 调用（60s，高潮）
- 终端运行：
```bash
curl -X POST "https://workflow-online-api-nr3e4.ondigitalocean.app/api/workflow?applicationName=DEMO_PAY&confirmationNumber=PAY-0001" \
  -H "Content-Type: application/json" \
  -H "X-Request-Correlation-Id: 11111111-1111-1111-1111-111111111111" \
  -d '{ "customerId": "C-1001", "amount": 8800 }'
```
- 旁白："200，调用成功。再用同一个关联 ID 发一次——"（重发）"被识别为重复，幂等生效，这是平台白送的。"

### 5:00 — 看记录 + 收尾（45s）
- `/records` → 按 `DEMO_PAY` 筛选 → 下钻每步与通道状态。
- 旁白："执行结果、每步状态、重试链都在运行记录里可查。
> 从建到发布到调用，我没写一行后端。这就是 Serverless Easy API Maker。"

## 备选/应急
- 网络受限时，用本地 H2 后端（见 `../local-verification-report.md`）把 base URL 换成 `localhost:8081` 演示同样流程。
- 强调诚实点："当前结果在运行记录看；同步返回结果在路线图上"，避免现场被问倒。
