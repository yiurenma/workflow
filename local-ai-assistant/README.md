# 本地 Workflow AI 助手「小流」— 搭建手册

一套**硬件到货后照着敲就能跑**的本地 AI 助手套件，专门服务用 **Workflow Studio** 平台的人。
它跑在你那台 RTX 5060 Ti 16GB 的主机上，通过**局域网网页**和**微信**对外服务。

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

### 前置（硬件到货后）
1. 装好 Windows 11，到 nvidia.cn 装最新显卡驱动。
2. 装 [Ollama](https://ollama.com/download)（Windows 版，一路下一步）。
3. 装 [Docker Desktop](https://www.docker.com/products/docker-desktop/)（用于网页界面）。
4. 把本仓库 clone 到主机（或只拷 `local-ai-assistant/` + `docs/98-product-docs/` 两个目录，
   保持相对位置不变 —— 构建脚本靠相对路径找文档）。

### 三步起飞
```powershell
# 1) 拉模型（首选 14B；显存吃紧可换 qwen2.5:7b-instruct）
ollama pull qwen2.5:14b-instruct

# 2) 生成知识库 + 最终 Modelfile，并创建专属模型
cd local-ai-assistant
./build-knowledge.ps1                      # macOS/Linux 用 ./build-knowledge.sh
ollama create workflow-helper -f knowledge/Modelfile.generated

# 3) 先命令行验证一下
ollama run workflow-helper "调用 workflow API 必须带哪个请求头？少了会怎样？"
#   ✅ 期望答：必须带 X-Request-Correlation-Id，缺失返回 400 / 440000
```

### 起网页（局域网）
```powershell
docker compose up -d
```
- 主机浏览器开 `http://localhost:3000`，首位注册者即管理员。
- **旧 Mac / 手机**在同一局域网开 `http://<主机IP>:3000`（用 `ipconfig` 查主机 IP）。
- 在 Open WebUI 右上角模型选 `workflow-helper` 即可开聊。
- 旧 MacBook 2015 就这样变成「躺床上白嫖 5060Ti 算力」的遥控器。

> 防火墙：Windows 首次会弹窗，允许专用网络访问 3000 端口即可让局域网设备连上。

---

## 阶段二：接入微信
见 [`wechat/README-wechat.md`](wechat/README-wechat.md)。要点：**用小号扫码 + 主人白名单**，防封防误触。

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
