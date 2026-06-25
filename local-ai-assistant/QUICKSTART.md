# 装机当天 · 一页纸命令清单

硬件到货、装好 Windows 11 后，照这个顺序敲。命令为主，细节看各自的 README。
带 ⭐ 的是当天必做；其余可日后再补。占位符（`你的域名`、`<KEY>` 等）换成你自己的。

> 📦 **到货前先过一遍 [`PREP.md`](PREP.md)**：把域名 NS、Google/企业微信/Clerk 钥匙提前办好，当天直接填。

---

## 0. 装软件（一次性）⭐
- NVIDIA 驱动（nvidia.cn）
- [Ollama](https://ollama.com/download)、[Git](https://git-scm.com)、[Python 3.10+](https://python.org)（勾 Add to PATH）
- 网页二选一：Docker Desktop（需 WSL2）**或** 后面用 `pip install open-webui`
- 公网用：`winget install --id Cloudflare.cloudflared`

```powershell
git clone <本仓库地址>
cd workflow\local-ai-assistant
```

## 1. 本地 AI 跑起来（双模型）⭐
```powershell
ollama pull qwen2.5:14b-instruct
ollama pull qwen2.5-coder:14b-instruct
./build-knowledge.ps1
ollama create workflow-helper      -f knowledge/Modelfile.chat.generated
ollama create workflow-helper-code -f knowledge/Modelfile.code.generated
ollama run workflow-helper "调用 workflow API 必须带哪个请求头？少了会怎样？"
#   ✅ X-Request-Correlation-Id，缺失 400 / 440000
```

## 2. 局域网网页 ⭐
```powershell
docker compose up -d           # 或：pip install open-webui; open-webui serve --host 0.0.0.0 --port 3000
```
- 主机/旧 Mac/手机浏览器开 `http://<主机IP>:3000`（`ipconfig` 查 IP），首位注册者=管理员。
- 建好账号后**关掉开放注册**（Open WebUI 设置里）。

## 3. 装「智能化」三件套（按需）
- 路由器（自动选模型，下拉只留「小流（自动）」）：`openwebui-functions/model_router.py` → Workspace→Functions 粘贴启用。
- 联网搜索（Google 优先）：`openwebui-tools/web_search.py` → Workspace→Tools 粘贴，填 Google 钥匙（见 [`KEYS.md`](KEYS.md)）。
- 工作流校验：`openwebui-tools/validate_workflow.py` → 挂给写码模型。Function Calling 设 **Native**。

## 4. 公网网关 + 你的域名（让 workflow UI 用户能用）⭐你要的"外网访问"
**(a) 先本机起网关，用 API Key 跑通**
```powershell
cd gateway
pip install -r requirements.txt
copy .env.example .env          # 编辑 .env：AUTH_MODE=apikey，GATEWAY_API_KEY=<一串长随机>，ALLOWED_ORIGINS=workflow UI 地址
uvicorn app:app --host 127.0.0.1 --port 8800
#   自测：curl http://127.0.0.1:8800/healthz
```
**(b) 新域名托管到 Cloudflare（GoDaddy 改 NS）**
1. Cloudflare 注册 → Add a site → 你的域名 → Free。
2. 拿到 2 个 nameserver → GoDaddy：域名 → Nameservers → Change → "I'll use my own nameservers" → 粘贴 → 保存。
3. 等 Cloudflare 显示 **Active**（新域名通常很快）。

**(c) 建隧道，把网关暴露成 `https://ai.你的域名`**
```powershell
cloudflared tunnel login
cloudflared tunnel create workflow-ai
cloudflared tunnel route dns workflow-ai ai.你的域名
#   按 gateway/cloudflared.example.yml 写好 ~/.cloudflared/config.yml（service: http://127.0.0.1:8800）
cloudflared tunnel run workflow-ai      # 可 cloudflared service install 设成开机自启
```
验证：`curl https://ai.你的域名/healthz` 返回 `{"ok":true}`。  
🔒 只暴露网关；Ollama 永远只监听 127.0.0.1；`ALLOWED_ORIGINS` 收紧；建议再加 Cloudflare Access 登录。

## 5. 以后再做
- **企业微信机器人**（零封号）：`wechat/README-wechat.md` + 内网穿透 `wechat/frpc.example.ini`。
- **Clerk 鉴权**：网关 `.env` 改 `AUTH_MODE=clerk` + `CLERK_ISSUER`（见 `gateway/README.md`）。
- **workflow UI 里嵌聊天框**：前端带 Clerk token 调 `https://ai.你的域名/v1/chat/completions`。
- **给 workflow UI 绑自定义域名**（Vercel 加 `workflow.你的域名`，国内更稳）。
- **内存补到 32GB 双通道**。
