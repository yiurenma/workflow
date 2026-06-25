# 工具脚本（阶段三：能动手的助手）

让助手不只是答疑，还能**查运行记录 / 触发工作流 / 生成工作流骨架**。
纯标准库 + `requests`（可选），无重依赖。建议在阶段一、二稳定后再用。

| 脚本 | 作用 | 风险 |
|---|---|---|
| `query_records.py` | 查某次调用的运行记录，排查失败原因 | **只读，安全** |
| `trigger_workflow.py` | 触发已发布工作流（POST /api/workflow） | **写操作**：默认白名单 + 二次确认 |
| `generate_workflow.py` | 自然语言→工作流 JSON 骨架（调本地 Ollama） | 生成结果需人工核对 |

## 环境变量

```bash
export WORKFLOW_OPERATION_API="https://workflow-operation-api-n9sbp.ondigitalocean.app"
export WORKFLOW_ONLINE_API="https://workflow-online-api-nr3e4.ondigitalocean.app"
export WORKFLOW_ALLOWED_APPS="DEMO_PAY"     # 触发白名单，逗号分隔；强烈建议设置
export OLLAMA_HOST="http://127.0.0.1:11434"
export OLLAMA_MODEL="workflow-helper"
```

## 示例

```bash
# 查记录
python query_records.py --app DEMO_PAY --status FAILED --size 5

# 触发（会要求输入 yes 确认）
python trigger_workflow.py --app DEMO_PAY --confirmation PAY-20260625-0001 \
    --body '{"customerId":"C-1001","amount":8800,"currency":"AUD"}'

# 生成骨架
python generate_workflow.py "收到支付消息，金额>5000 走短信，否则邮件"
```

## 接进聊天（让助手自己调用）

这些脚本既能当命令行用，也能注册成 [Open WebUI Tools](https://docs.openwebui.com/features/plugin/tools/)
或挂到支持 function-calling 的客户端，让「小流」在对话里自动调用。
`qwen2.5` 系列支持 function-calling。接入时务必保留 `trigger_workflow.py` 的白名单 + 确认护栏。

> `generate_workflow.py` 会优先读取 `workflow-ui/src/constants/prompts.ts` 的官方生成提示词；
> 若 submodule 未检出，请先在仓库根目录 `git submodule update --init workflow-ui`，否则用内置精简提示词。
