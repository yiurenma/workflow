# 本地 Workflow AI 助手「小流」— 搭建手册

一套**硬件到货后照着敲就能跑**的本地 AI 助手套件，专门服务用 **Workflow Studio** 平台的人。
它跑在你那台 RTX 5060 Ti 16GB 的主机上，通过**局域网网页**和**微信**对外服务。

> 🎯 **George 专属：[`QUICKSTART-snailnow.md`](QUICKSTART-snailnow.md)** — 按你的实际情况定制（snailnow.com + 双模型 + Cloudflare + 已备钥匙）。
> 🚀 **通用版：[`QUICKSTART.md`](QUICKSTART.md) — 装机当天一页纸命令清单**（建模型→网页→公网网关，一路串好）。
> 📦 **还在等快递？先看 [`PREP.md`](PREP.md) — 到货前能提前做的事**（切域名 NS、申请钥匙/账号，省到货当天的时间）。
> 🧭 **想看"为什么这么设计"？看 [`DECISIONS.md`](DECISIONS.md) — 决策记录**（整条讨论的来龙去脉 + 每个决定的理由）。

> 关键认知：你**不是在"训练"模型**。16GB 单卡只够做推理，也不需要训练。
> 正确做法 = 跑一个现成开源模型（Ollama）+ 把本平台文档作为知识注入 + 用 Modelfile 写性格。
> 本平台全部用户文档只有 ~1700 词，能**直接塞进系统提示词**，初期连向量库/RAG 都不用，又快又准。

## 架构一眼看懂

```
[微信手机端] ──扫码──┐
                     ├─→ chatgpt-on-wechat ──┐
[局域网浏览器/旧Mac]─→ Open WebUI(:3000) ────┼─→ Ollama(:11434, OpenAI兼容)
                                              │     └─ 模型 workflow-helper
[阶段三 工具]─────────────────────────────────┘        (基座 + 性格 + workflow知识)
                                                          │
                                          (阶段三) ──→ operation-api / online-api
```

- **Ollama**：本地推理引擎，提供 OpenAI 兼容接口，局域网共享。
- **workflow-helper**：你的专属模型（`Modelfile` = 基座 + 性格 + 知识库）。换电脑只要复制配置就能继承。
- **Open WebUI**：局域网网页聊天，旧 Mac / 手机浏览器直接访问。
- **chatgpt-on-wechat**：微信桥接，配置即用。

## 目录里有什么

| 文件 | 作用 |
|---|---|
| `system-prompt.md` | 性格「灵魂剧本」+ 防幻觉护栏（**随意改这个**） |
| `Modelfile` | 模型模板（基座 + 占位符）。换基座模型改这里的 `FROM` |
| `build-knowledge.ps1` / `.sh` | 把 `docs/98-product-docs/` 拼成知识库并注入模板，生成最终 Modelfile |
| `docker-compose.yml` | 一条命令起 Open WebUI（网页） |
| `wechat/` | 微信接入说明 + 配置示例（阶段二） |
| `tools/` | 查记录 / 触发 / 生成工作流脚本（阶段三） |

---

## 阶段一：能答疑的本地助手（到货当天）

### 前置：软件清单（全新机器要装的全部东西）

这是一台干净的 Windows 11，下面是**从零到能跑**所需的所有软件。按阶段标了「何时需要」，
阶段一只需前 4 项即可上线。

| # | 软件 | 何时需要 | 说明 / 下载 |
|---|---|---|---|
| 1 | **NVIDIA 显卡驱动** | 阶段一（必需） | nvidia.cn 或 GeForce Experience，装最新版 |
| 2 | **Ollama** | 阶段一（必需） | https://ollama.com/download —— 本地推理引擎 |
| 3 | **Git** | 阶段一（必需） | https://git-scm.com —— 拉本仓库；自带 Git-Bash 还能跑 `.sh` |
| 4 | **网页界面**（二选一） | 阶段一（必需） | A. **Docker Desktop**（需开 WSL2）跑 `docker compose up -d`；<br>B. 嫌 Docker 重，可改用 **Python + `pip install open-webui`**（见下方「不想装 Docker」） |
| 5 | **Python 3.10+** | 阶段二、三 | https://python.org —— 微信桥接、tools 脚本、（可选）Open WebUI 都要它。装时勾选 *Add to PATH* |
| 6 | **chatgpt-on-wechat**（企业微信渠道） | 阶段二 | `git clone` + `pip install`，见 `wechat/README-wechat.md` |
| 7 | **内网穿透**（frp + 小 VPS，或 cpolar/花生壳） | 阶段二 | 企业微信回调需公网可达；见 `wechat/frpc.example.ini` |

> Windows 上**不需要**额外装 bash —— 直接用 PowerShell 跑 `build-knowledge.ps1`。
> `.sh` 是给你在旧 Mac 上改东西时用的。

> 💡 **硬件建议：内存补到 32GB 双通道。** 单条 16GB 跑 Windows + Docker/Open WebUI + 浏览器偏紧，
> 而且单条是单通道、带宽减半。再加**一条同型号** 16GB DDR5-6000（约 300-400 元）凑 32GB 双通道，
> 是这台机器性价比最高的升级。暂不加也能用 —— 优先选下面的「pip 起 Open WebUI」免 Docker 路线省内存。

**拿到代码：**
```powershell
git clone <本仓库地址>
cd workflow\local-ai-assistant
# 或只拷 local-ai-assistant\ + docs\98-product-docs\ 两个目录，保持相对位置（脚本靠相对路径找文档）
```

### 三步起飞（双模型：答疑 + 写码）
```powershell
# 1) 拉两个基座（答疑用通用版，写码用 Coder 版；显存吃紧可各换 7B）
ollama pull qwen2.5:14b-instruct
ollama pull qwen2.5-coder:14b-instruct

# 2) 生成知识库 + 两份 Modelfile，并创建两个专属模型
cd local-ai-assistant
./build-knowledge.ps1                      # macOS/Linux 用 ./build-knowledge.sh
ollama create workflow-helper      -f knowledge/Modelfile.chat.generated
ollama create workflow-helper-code -f knowledge/Modelfile.code.generated

# 3) 先命令行验证一下
ollama run workflow-helper "调用 workflow API 必须带哪个请求头？少了会怎样？"
#   ✅ 期望答：必须带 X-Request-Correlation-Id，缺失返回 400 / 440000
```

> **自动选模型（推荐）：** 装上 [`openwebui-functions/model_router.py`](openwebui-functions/README.md)，
> 模型下拉里只留一个 **「小流（自动）」**——写代码/生成工作流自动走写码模型，其余走答疑模型，**你不用手动切**。
> **双语：** 中文问中文答、English ask → English answer（节点名/错误码等专有名词保持原文）。

### 起网页（局域网）
```powershell
docker compose up -d
```
- 主机浏览器开 `http://localhost:3000`，首位注册者即管理员。
- **旧 Mac / 手机**在同一局域网开 `http://<主机IP>:3000`（用 `ipconfig` 查主机 IP）。
- 在 Open WebUI 右上角模型选 **「小流（自动）」**（装了路由器后）或 `workflow-helper` 即可开聊。
- 旧 MacBook 2015 就这样变成「躺床上白嫖 5060Ti 算力」的遥控器。

> 防火墙：Windows 首次会弹窗，允许专用网络访问 3000 端口即可让局域网设备连上。

#### 不想装 Docker？用 pip 起 Open WebUI
国内拉 `ghcr.io` 镜像常常很慢，如果你已装了 Python（阶段二/三也要用），可以跳过 Docker：
```powershell
pip install open-webui
$env:OLLAMA_BASE_URL="http://127.0.0.1:11434"
open-webui serve --host 0.0.0.0 --port 3000
```
效果一样，局域网照样开 `http://<主机IP>:3000`。

---

## 网络与代理（VPN）：什么时候要外网、怎么让它走你的 VPN

**先记住一句话：机器人「跑起来」基本不用外网——本地模型推理完全离线。**
外网只在两类时刻需要：

| 场景 | 是否要外网 | 走哪条网络 |
|---|---|---|
| `ollama pull` 拉模型 | 要（装机一次性） | 海外/加速，VPN 可大幅提速 |
| 拉 Open WebUI 的 Docker 镜像 | 要（装机一次性） | `ghcr.io` 国内常被墙，**最需要 VPN** |
| `pip install` 装 Python 包 | 要 | 可用国内镜像（清华源），不一定要 VPN |
| **日常答疑聊天（阶段一/二）** | **不要** | 纯本地，断网也能用 |
| 微信收发消息 | 不要（微信本身国内可用） | 国内直连 |
| 阶段三 查记录/触发（DigitalOcean 上的 UAT API） | 视情况 | 若访问慢/不通，走 VPN |
| （可选）让机器人调海外大模型 API 兜底 | 要 | 走 VPN |

### 推荐做法：装机时开「全局/TUN 模式」最省事
你的 VPN 客户端（Clash / V2rayN 等）开**全局模式**（或 TUN 模式），
然后再执行 `ollama pull` 和 `docker compose up -d`，所有下载自动走 VPN。装完即可关掉，日常聊天不需要它。

### 给单个工具单独配代理（不想全局时）
假设你的本地代理是 `http://127.0.0.1:7890`（**端口换成你客户端实际的**）：

> 用 **Xray** 的话：手机端 v2Box 帮不到 Windows 主机；在主机上用 **v2rayN / Nekoray** 导入同一节点，
> 默认本地端口是 **HTTP `10809`** / SOCKS `10808`（把下面的 `7890` 换成 `10809`），或直接开它的系统代理/TUN 全局模式。

- **Ollama 拉模型**：先设环境变量再拉（Ollama 认 `HTTPS_PROXY`）。设完要从托盘**退出并重启 Ollama**：
  ```powershell
  setx HTTPS_PROXY "http://127.0.0.1:7890"
  setx HTTP_PROXY  "http://127.0.0.1:7890"
  ```
- **Docker 拉镜像**：Docker Desktop → Settings → Resources → **Proxies** → 填 `http://127.0.0.1:7890`。
  若填了不生效，在代理客户端打开「允许局域网连接(Allow LAN)」，并把地址换成主机局域网 IP。
- **tools 脚本（阶段三）**：脚本用的 `urllib` 自动读 `HTTPS_PROXY`，所以同一个环境变量就够；
  只想让脚本走代理时，临时设：
  ```powershell
  $env:HTTPS_PROXY="http://127.0.0.1:7890"; python tools\query_records.py --app DEMO_PAY
  ```
- **微信桥接**：连的是**本地** Ollama，**不要**给它配外网代理，否则可能影响微信登录。

> 安全提示：本地模型 + 局域网使用时，机器人不会把你的聊天内容发到外网。
> 只有当你（阶段三可选）接海外大模型 API 兜底时，内容才会出境——按需选择。

---

## 让它能联网搜索（Google）—— 不知道就上网查

**策略：本地优先，联网兜底——而且全自动。** 本地答得了就本地答，答不了它**自己**联网搜，
搜索内部 **Google 优先、失败自动换备选**。你**不用点任何开关、不用选引擎**。

> 🚀 **推荐：全自动联网搜索（function-calling 工具）。**
> 见 [`openwebui-tools/README.md`](openwebui-tools/README.md)：导入 `openwebui-tools/web_search.py`、
> 填 Google 钥匙、把工具挂到 `workflow-helper` 模型并开启 **Function Calling = Native**。
> 之后模型会在「本地答不出」时**自动**调用它（Google→Serper→DuckDuckGo 自动兜底），全程零手动。
> 这就是你要的「一切智能化」。下面那套「手动开关」是不想装工具时的简易替代，二选一即可。

---

### 简易替代：内置 Web Search 手动开关（不想装工具时）
用 Open WebUI 内置的 **Web Search**，接 **Google 可编程搜索（PSE）**。需要时手动点亮开关。

### 一次性准备（拿两把钥匙）—— 详细图文步骤见 [`KEYS.md`](KEYS.md)
1. **Google API Key**：到 Google Cloud Console 启用 **Custom Search API**，创建一个 API 密钥。
2. **搜索引擎 ID (cx)**：到 https://programmablesearchengine.google.com 新建搜索引擎，
   选「搜索整个网络」，复制它的 **Search engine ID**。
   （PSE 免费额度约 100 次/天，个人够用；超了才计费。）

### 在 Open WebUI 里打开
管理员进 **Admin → Settings → Web Search**：
- 打开 Web Search 开关
- Engine 选 **Google PSE**
- 填上面的 **API Key** 和 **Engine ID**

之后在聊天框底部把 **Web Search** 开关点亮，提问时它就会先 Google、抓取网页、再综合回答（带来源链接）。
**用法配合「本地优先」：开关平时关着，本地答不上来了再点亮它去查。**

### 引擎优先级：Google 首选，不行再切别的
Open WebUI 一次只用**一个**配置好的搜索引擎，所以把 **Google PSE 设为默认（首选）**。
当 Google 不可用时（超免费额度 / 被限流 / 网络不通），到 Admin → Settings → Web Search 把 Engine
临时切到备选，建议顺序：

1. **Google PSE**（首选，官方结果）
2. **DuckDuckGo**（免 key、最省事的兜底）
3. **Serper / Tavily**（第三方，结果同样来自 Google，注册拿 key 即用）
4. **SearXNG**（自建，进阶）

> 想要「Google 不行自动换下一个」这种**自动链式兜底**，Open WebUI 原生不支持，需要进阶做法：
> 给模型挂一个会 function-calling 的搜索工具（qwen2.5 支持），由它按顺序尝试。需要的话我再单独给你配。

### ⚠️ 联网搜索 = 运行时要外网（走你的 VPN）
和本地答疑不同，搜索要实时访问 Google，所以**搜索时 VPN 必须开着**，且 Open WebUI 要能走到 VPN：
- **用 pip 起 Open WebUI（这种场景推荐）**：它是宿主机进程，直接复用你电脑的 VPN（全局/TUN 模式最省心）。
- **用 Docker 起**：要给容器单独配代理 —— 见 `docker-compose.yml` 里注释好的 `HTTP_PROXY/HTTPS_PROXY`
  和 Web Search 环境变量，按提示填（也可不填环境变量，纯在上面的 Admin 界面里配）。

> 隐私提示：开了联网搜索，你的提问会发给 Google。**只在需要时点亮那个开关**，平时纯本地不出网。

> 不想配 Google？Open WebUI 也支持 **DuckDuckGo**（免 key）、**SearXNG**（自建）、
> **Serper / Tavily**（第三方，结果同样来自 Google，注册即用）。你点名要 Google，PSE 是官方正路。

---

## 阶段二：接入微信 —— 企业微信(WeCom)官方机器人
见 [`wechat/README-wechat.md`](wechat/README-wechat.md)。要点：**用企业微信自建应用（官方 API，零封号风险）**，
个人微信即可收发；接收消息的回调需**内网穿透**（见 `wechat/frpc.example.ini`）。

## 让 workflow UI 的用户也能用（公网受控接入）
见 [`gateway/README.md`](gateway/README.md)。给本地 Ollama 套一层 **鉴权 + 公网 HTTPS 接口**：
- **只暴露网关，不暴露 Ollama/Open WebUI**；workflow UI 聊天框带令牌来调。
- 鉴权两段式：先用**共享 API Key** 跑通 → 以后换成校验 **Clerk JWT**，对接你的权限体系。
- 用 **Cloudflare Tunnel** 暴露（无需公网 IP、自带 HTTPS）。

## 阶段三：让它能动手（查记录 / 触发 / 生成工作流）
见 [`tools/README.md`](tools/README.md)。这是你的「最终目的」，建议阶段一二稳定后再开。
触发类写操作默认有**白名单 + 二次确认**护栏。

---

## 常见问题

- **改了性格不生效？** 改完 `system-prompt.md` 要重新 `build-knowledge` + `ollama create`（覆盖同名模型即可）。
- **平台文档更新了？** 重跑 `build-knowledge` 再 `ollama create`，知识库即随文档更新。
- **回答慢 / 显存不够？** 把 `Modelfile` 的 `FROM` 换成 `qwen2.5:7b-instruct`，或把 `num_ctx` 调到 4096。
- **知识库以后变很大？** 再上 RAG：用 Open WebUI 内置「知识库(Knowledge)」上传文档 + `bge-m3` embedding，
  无需自建向量库。当前 1700 词远没到这一步。
- **存储会爆吗？** 纯文字聊天聊 10 年也超不过 1GB，1TB 固态绰绰有余。

## 升级模型时如何继承性格与记忆
AI 的「大脑（基座模型）」和「灵魂（性格+知识）」是分离的：
保留 `system-prompt.md` + `Modelfile`，在新机器上重跑「三步起飞」即可完美继承。
