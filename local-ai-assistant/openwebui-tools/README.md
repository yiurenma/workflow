# Open WebUI 工具

本目录有两个工具，都「全自动」、由模型自己按需调用，你不用点任何开关：

| 文件 | 作用 | 谁调用 |
|---|---|---|
| `web_search.py` | 联网搜索：本地答不了才搜，**Google → Serper → DuckDuckGo 自动兜底** | 两个模型都挂 |
| `validate_workflow.py` | 校验工作流 JSON 并**自动修复**常见错误（非法节点类型、JSONPath 缺 `$`、悬空边） | 写码模型 `workflow-helper-code` 挂 |

> 安装方式相同：Open WebUI → **Workspace → Tools → ＋** → 粘贴对应文件内容 → Save →
> 在 **Workspace → Models** 里把工具勾给对应模型，并把 **Function Calling 设为 `Native`**。

---

## 一、全自动联网搜索（Google 优先）—— `web_search.py`

装上后「小流」会**自己决定**要不要联网——本地答得了就本地答，答不了才搜；
搜索内部 **Google → Serper → DuckDuckGo 自动兜底**。**你不需要点任何开关，全自动。**

> 这是「智能化」路线，取代了「手动点亮 Web Search 开关」。两者二选一，推荐用这个。

## 安装（一次性，约 5 分钟）

1. **拿 Google 钥匙**（同主 README 的「联网搜索」一节）：
   - Google Cloud 启用 *Custom Search API* → 建一个 **API Key**
   - https://programmablesearchengine.google.com 建搜索引擎（选「搜索整个网络」）→ 复制 **Engine ID (cx)**

2. **导入工具**：Open WebUI → 左下 **Workspace → Tools → ＋**，
   把本目录 `web_search.py` 的全部内容**粘贴进去 → Save**。

3. **填钥匙（Valves）**：在该工具的 ⚙️ 设置里填 `GOOGLE_PSE_API_KEY` 和 `GOOGLE_PSE_ENGINE_ID`。
   （可选再填 `SERPER_API_KEY` 作备选；`ENABLE_DUCKDUCKGO` 默认开，作最后免费兜底。）

4. **挂到模型并开启原生工具调用**：
   - Workspace → **Models** → 选 `workflow-helper` → 把这个工具**勾上（启用）**。
   - 同一页高级参数里把 **Function Calling 设为 `Native`**（qwen2.5 支持原生工具调用，效果最好）。

5. **确保 VPN 开着**：联网搜索是运行时访问 Google，主机 VPN/代理要开（全局/TUN 模式最省心；
   用 pip 起 Open WebUI 时直接复用主机网络，最顺）。

## 完成后是什么体验

你只管正常提问：
- 平台/本地知识能答 → 它直接答，**不联网**（省时省额度）。
- 本地答不了（如「xxx 库最新版怎么用」「今天的某新闻」）→ 它**自动**调用搜索，
  Google 优先、失败自动兜底，然后综合作答并**附来源链接**。

全程不用你点开关、不用你选引擎。

## 排查

- **它不主动搜**：确认第 4 步工具已勾选、Function Calling = Native；模型用的是 qwen2.5 这类支持工具调用的。
- **搜索报错/超时**：多半是 VPN 没开或代理没覆盖到 Open WebUI 进程；或 Google 钥匙没填对/超了免费额度（此时会自动走 DuckDuckGo）。
- **想换条数/兜底策略**：改工具 Valves 里的 `MAX_RESULTS`、`ENABLE_DUCKDUCKGO` 即可，无需改代码。

---

## 二、工作流 JSON 校验器 —— `validate_workflow.py`

让「写码模型」生成工作流 JSON 后**自我检查、自我修正**，大幅提高导入成功率。

### 安装
1. Open WebUI → **Workspace → Tools → ＋** → 粘贴 `validate_workflow.py` → Save。
2. **Workspace → Models → `workflow-helper-code`** → 勾上本工具；Function Calling = **Native**。
3. 无需任何 Valves/钥匙，纯本地逻辑、不联网。

### 完成后是什么体验
你说「帮我生成一个支付通知工作流」→ 写码模型生成 JSON → **自动调用 `validate_workflow`** →
若有错（非法 `action.type`、规则键缺 `$`、悬空边…）→ 按返回的 errors 自我修正 → 再交付**合法** JSON。

### 它查什么（对齐画布 Import 规则）
- **错误(阻断)**：JSON 解析失败、缺 `pluginList`、`action.type` 不在 6 种合法类型内、节点 id 重复、
  规则键不是单个 JSONPath（须以 `$` 开头、无逗号/分号）。
- **自动修复**：规则键缺 `$`（如 `customerId` → `$.customerId`）、剥离 ``` 代码围栏。
- **警告(建议)**：边的 `source/target` 不在节点里且非 `IFELSE_<id>_true/false` 虚拟端点、HTTP 节点缺 method。

> 同一套逻辑也有命令行版 `tools/validate_workflow.py`，可单独 `python validate_workflow.py x.json` 用。

