# 决策记录 / 对话整理 —— 本地 Workflow AI 助手「小流」

> 这份文档把"从想法到落地"的整条讨论浓缩成可长期参考的决策记录。
> 每条决策都附**理由**，方便日后回看或换人接手。配套上手文档：
> [`README.md`](README.md) · [`QUICKSTART.md`](QUICKSTART.md) · [`PREP.md`](PREP.md) · [`KEYS.md`](KEYS.md)

## 1. 背景与目标
用一台自购主机（RTX 5060 Ti 16GB / i5-14600KF / Z790）搭一个**本地 AI 助手「小流」**，
专门服务 **Workflow Studio** 平台的用户，通过**局域网网页 + 微信 + 公网接口**对外提供。
平台 = Serverless Easy API Maker：画布编排"消息丰富化 + JSONPath 条件 + 多通道下发"→ 发布成可调用 API。

## 2. 两个关键认知（决定了整体路线）
- **不是"训练"模型。** 16GB 单卡只够**推理**。正确做法 = 跑现成开源模型（Ollama）+ 把平台文档作为知识注入 + Modelfile 写性格。"大脑/灵魂分离"，换机器只需复制 Modelfile。
- **知识库极小**（平台文档 ~1700 词）→ 直接塞进 **system prompt**，初期**不需要向量库/RAG**，准确又快。

## 3. 硬件评估
- GPU 5060Ti 16G / 1200W 电源 / 14600KF / Z790 / 雅浚散热 / 致态 1TB —— 都合适，地基好。
- **唯一短板：内存单条 16GB（单通道）。** 建议再加一条同型号 16GB → **32G 双通道**，是性价比最高的升级。
- 14600**KF** 无核显（靠独显出画面，对"当服务器远程用"无影响）。

## 4. 架构
```
                         ┌─ Open WebUI (:3000, 局域网网页, 给你自己)
个人微信 ─ WeCom 官方API ─┤
workflow UI 用户 ─ 公网网关(:8800) ─┼─→ Ollama (:11434, 仅 localhost, OpenAI 兼容)
                         └           └─ 自定义模型: workflow-helper(答疑) / workflow-helper-code(写码)
联网搜索/校验/查记录 = 模型自动调用的"工具"
```

## 5. 决策点（决定 + 理由）
| 决策 | 选了什么 | 理由 |
|---|---|---|
| 模型路线 | Ollama + 知识注入，不训练 | 16G 只够推理；文档小，注入即可 |
| 模型数量 | **双模型 + 自动路由** | 答疑用 `qwen2.5:14b`、写码用 `qwen2.5-coder:14b`；路由器对外只露「小流（自动）」，用户不用手动切 |
| 为何 Coder | 写码/生成工作流用 **coder** 版 | 代码/JSON 能力明显强于通用版；16G 上限 ~14B，32B 爆显存 |
| 工作流生成质量 | 加 **JSON 校验闭环** | 合法率关键在"接地 + 校验"，不在模型大小；生成→校验→自修 |
| 联网搜索 | **全自动**，模型自己决定 | 本地优先、答不了才联网；引擎 Google→Serper→DuckDuckGo 自动兜底；不用手动开关 |
| 语言 | **中英双语**，按用户语言回复 | 专有名词（节点名/错误码/请求头）保持原文 |
| 性格 | **默认**（专业英式管家） | 可随时改 `system-prompt.md` 重新构建 |
| 微信 | **企业微信(WeCom)官方机器人** | 弃个人号协议（封号风险）；WeCom 官方 API = **零封号**；代价是回调需内网穿透 |
| 公网访问 | **网关 + Cloudflare Tunnel + Clerk** | 绝不裸奔 Ollama；网关鉴权（先 API Key→后 Clerk JWT）；Tunnel 解决无公网 IP(CGNAT) |
| 域名 | 新域名 GoDaddy → **NS 切 Cloudflare** | 新域名无旧记录，最省事；Cloudflare Tunnel 要求 zone 托管在 CF |
| VPN | 主机用 **v2rayN/Nekoray**（同 Xray 节点） | 手机 v2Box 帮不到 Windows 主机；端口 HTTP 10809；仅下载/联网搜索时需要 |
| 备案 | **不需要** | Cloudflare Tunnel/Vercel 走境外边缘，非国内落地服务器 |

## 6. 时间预期
- **本地 AI + 局域网网页**：到货当天半天（主要等模型下载，~18GB）。提速：先用 7B、Open WebUI 用 `pip` 跳过 Docker 镜像、全程挂 VPN。
- **公网网关 + 域名**：当天能通（纯动手 ~40 分钟 + 等 NS 生效）。
- **分天做的**：企业微信（要 VPS + 内网穿透）、Clerk 接入、workflow UI 嵌聊天框——各自独立，1–3 小时。

## 7. 套件文件地图（`local-ai-assistant/`）
| 路径 | 作用 |
|---|---|
| `README.md` | 总部署手册 |
| `QUICKSTART.md` | 装机当天一页纸命令清单 |
| `PREP.md` | 到货前提前准备清单（切 NS、办钥匙） |
| `KEYS.md` | Google PSE 密钥申请步骤 |
| `system-prompt.md` | 小流性格 + 护栏 + 双语（灵魂剧本） |
| `Modelfile` | 模型模板（占位符 FROM/ROLE_NOTE/SYSTEM/KNOWLEDGE） |
| `build-knowledge.{sh,ps1}` | 拼知识库 + 生成 chat/code 两份 Modelfile |
| `docker-compose.yml` | 起 Open WebUI（局域网网页） |
| `openwebui-functions/model_router.py` | 自动选模型的 Pipe（「小流（自动）」） |
| `openwebui-tools/web_search.py` | 全自动 Google 联网搜索（自动兜底） |
| `openwebui-tools/validate_workflow.py` | 工作流 JSON 校验工具（生成→校验→自修） |
| `gateway/` | 公网 AI 网关（FastAPI + Clerk/API Key 鉴权）+ Cloudflare Tunnel 示例 |
| `wechat/` | 企业微信接入（config + 内网穿透 frpc 示例） |
| `tools/` | 阶段三：查记录 / 触发 / 生成 / 校验（命令行） |

## 8. 待办 / 下一步
- **到货后配置**：填 Google/企业微信/Clerk 凭据、建模型、起网页、起网关、建 Cloudflare 隧道。
- **仍需做**：① 企业微信联调（VPS + 穿透 + 回调验证）；② workflow-ui 接 Clerk 后把网关 `AUTH_MODE` 切 `clerk`；
  ③ **在 workflow UI 里嵌聊天框**调网关（前端开发，需 workflow-ui 仓库可检出——本会话里它是空指针/独立仓库，权限受限）。
- **硬件**：补第二条 16GB 内存。

## 9. 备注
- 本套件是**新增的内部工具目录**，对 Workflow Studio 平台代码**零侵入**；分支 `claude/local-ai-desktop-setup-g4ontl`。
- 工作流相关事实（节点名/错误码/调用契约）均以 `docs/98-product-docs/` 为准，模型护栏要求逐字引用、不杜撰。
