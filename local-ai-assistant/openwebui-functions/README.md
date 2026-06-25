# Open WebUI 函数：小流（自动）模型路由

装上这个「Pipe 函数」后，模型下拉里只会多出一个 **「小流（自动）」**。
选它，机器人会**自己判断**该用哪个底层模型——你不用手动切换：

- 写代码 / 生成工作流 / JSON / JSONPath / 调试 → **写码模型**（`workflow-helper-code`）
- 其余日常问答 → **答疑模型**（`workflow-helper`）

## 安装（一次性）

1. 先确保两个底层模型已建好（见上层 README 的「双模型」一节）：
   `workflow-helper`（答疑）、`workflow-helper-code`（写码）。
2. Open WebUI → **Workspace → Functions → ＋**，粘贴 `model_router.py` 全部内容 → **Save → 启用**。
3. （可选）点该函数的 ⚙️ 设置（Valves）：
   - `CHAT_MODEL` / `CODE_MODEL`：两个底层模型 id（默认已对）。
   - `CODE_KEYWORDS`：命中即走写码模型的关键词（中英双语，可加你常用的词）。
   - `DEBUG`：开了会在回答前显示「路由 → 某模型」，方便确认。
4. 在聊天里把模型选成 **「小流（自动）」** 即可。

## 工作原理与注意

- 路由判定用**关键词启发式**（命中即走写码模型），零额外延迟；不命中走答疑模型。
- 路由器**委托**给底层模型，使该模型自带的工具（`web_search`、写码模型的 `validate_workflow`）照常触发。
- ⚠️ **版本差异**：委托用的是 Open WebUI 内部 API，随版本可能变动。
  若你的版本导入失败，路由器会**自动退化为直连 Ollama**（仍能路由，但工具不触发）。
  真机联调时按实际版本把委托段调通即可；最简退路是**不装本路由器、直接用单一 `workflow-helper-code` 全包**。

## 自测

- 问「什么是幂等」→ 应走 `workflow-helper`（开 DEBUG 可见）。
- 问「生成一个含 IFELSE 的工作流 JSON」→ 应走 `workflow-helper-code`，并随后自动调用 `validate_workflow`。
