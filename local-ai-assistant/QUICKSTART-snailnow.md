# 到货当天 · 专属命令清单（snailnow.com）

这是按你的实际情况定制的版本：域名 **snailnow.com**、**双模型 + 自动路由**、网页用 **pip**（跳过 Docker）、
公网用 **Cloudflare Tunnel**（网关 + 微信回调都走它，**不用 VPS**）。钥匙你已备好，到时从密码管理器里取。

> 子域名规划：`ai.snailnow.com`（AI 网关）· `wx.snailnow.com`（微信回调）· `workflow.snailnow.com`（以后给 workflow UI 绑 Vercel）。

---

## 0. 装软件 + 联网 ⭐
- NVIDIA 驱动（nvidia.cn）、[Ollama](https://ollama.com/download)、[Git](https://git-scm.com)、[Python 3.10+](https://python.org)（勾 Add to PATH）
- 公网用：`winget install --id Cloudflare.cloudflared`
- **VPN**：主机装 **v2rayN**（导入你 Xray 节点），开**系统代理/TUN 全局**，或设环境变量走它的 HTTP 端口 `10809`：
  ```powershell
  setx HTTPS_PROXY "http://127.0.0.1:10809"; setx HTTP_PROXY "http://127.0.0.1:10809"
  ```
  （拉模型/镜像时用；设完重启 Ollama。日常聊天不需要。）
```powershell
git clone <本仓库地址>
cd workflow\local-ai-assistant
```

## 1. 双模型 ⭐
```powershell
ollama pull qwen2.5:14b-instruct
ollama pull qwen2.5-coder:14b-instruct
./build-knowledge.ps1
ollama create workflow-helper      -f knowledge/Modelfile.chat.generated
ollama create workflow-helper-code -f knowledge/Modelfile.code.generated
ollama run workflow-helper "调用 workflow API 必须带哪个请求头？少了会怎样？"
#   ✅ X-Request-Correlation-Id，缺失 400 / 440000
```
> 想先快点跑通：把上面两个 14b 换成 `qwen2.5-coder:7b` 一个先玩，14b 后台慢慢拉。

## 2. 局域网网页（pip，免 Docker）⭐
```powershell
pip install open-webui
$env:OLLAMA_BASE_URL="http://127.0.0.1:11434"
open-webui serve --host 0.0.0.0 --port 3000
```
- 浏览器开 `http://<主机IP>:3000`，首位注册=管理员；**建好账号后到设置里关掉开放注册**。
- 旧 Mac / 手机同局域网开 `http://<主机IP>:3000`。

## 3. 智能三件套（在 Open WebUI 里）
- **路由器**：Workspace → Functions → 粘贴 `openwebui-functions/model_router.py` → 启用（下拉只留「小流（自动）」）。
- **联网搜索**：Workspace → Tools → 粘贴 `openwebui-tools/web_search.py` → Valves 填你的
  `GOOGLE_PSE_API_KEY`(AIza…) 和 `GOOGLE_PSE_ENGINE_ID`(`e4ef2b961ab0e4676`)；可选填 `SERPER_API_KEY`。
- **工作流校验**：Workspace → Tools → 粘贴 `openwebui-tools/validate_workflow.py` → 挂给 `workflow-helper-code`。
- 两个模型都把 **Function Calling 设 `Native`**。

## 4. 公网网关 + Cloudflare 隧道（ai.snailnow.com）⭐
**(a) 起网关，先用 API Key**
```powershell
cd gateway
pip install -r requirements.txt
copy .env.example .env
#   编辑 .env：AUTH_MODE=apikey；GATEWAY_API_KEY=<你预生成的长随机串>；
#             ALLOWED_ORIGINS=https://workflow.snailnow.com,https://workflow-ui-gamma.vercel.app
uvicorn app:app --host 127.0.0.1 --port 8800
#   自测：curl http://127.0.0.1:8800/healthz
```
**(b) 建一个隧道，绑两个子域名（网关 + 微信回调）**
```powershell
cloudflared tunnel login
cloudflared tunnel create workflow-ai
cloudflared tunnel route dns workflow-ai ai.snailnow.com
cloudflared tunnel route dns workflow-ai wx.snailnow.com
```
> ⚠️ 先去 Cloudflare DNS **删掉旧的 `wx.snailnow.com` A 记录**（指向那台 VPN 的 134.122.115.32），
> 否则上面 route 会冲突。删了再 route，cloudflared 会自动建对的 CNAME。

`~/.cloudflared/config.yml`：
```yaml
tunnel: <上一步的 TUNNEL_ID>
credentials-file: C:\Users\你\.cloudflared\<TUNNEL_ID>.json
ingress:
  - hostname: ai.snailnow.com
    service: http://127.0.0.1:8800     # AI 网关
  - hostname: wx.snailnow.com
    service: http://127.0.0.1:9898     # 微信回调（cow）
  - service: http_status:404
```
```powershell
cloudflared tunnel run workflow-ai     # 可 cloudflared service install 设开机自启
#   验证：curl https://ai.snailnow.com/healthz  → {"ok":true}
```
🔒 只暴露网关；Ollama 保持 127.0.0.1；建议再加 Cloudflare Access 给 ai.snailnow.com 加登录。

## 5. 微信（企业微信，走同一条隧道，无需 VPS）
```powershell
git clone https://github.com/zhayujie/chatgpt-on-wechat
cd chatgpt-on-wechat && pip install -r requirements.txt
#   把 ../local-ai-assistant/wechat/config.example.json 复制成 config.json，填：
#   wechatcom_corp_id / wechatcomapp_agent_id / wechatcomapp_secret / wechatcomapp_token / wechatcomapp_aes_key
python app.py        # 监听 9898（已被上面隧道转发到 wx.snailnow.com）
```
- 企业微信后台 → 自建应用 → 接收消息 → **URL = `https://wx.snailnow.com/wxcomapp`**，Token/AESKey 与 config 一致 →
  **先确保 cow + 隧道在跑，再点保存**（保存会立即回调验证）。
- 「我的企业 → 微信插件」用个人微信扫码绑定，即可在微信里跟小流聊。

## 6. 以后
- **Clerk 鉴权**：workflow-ui 接好 Clerk 后，网关 `.env` 改 `AUTH_MODE=clerk` + `CLERK_ISSUER=<你的 issuer>` +
  `CLERK_AUTHORIZED_PARTIES=https://workflow.snailnow.com`。
- **workflow UI 嵌聊天框**：前端带 Clerk token 调 `https://ai.snailnow.com/v1/chat/completions`。
- **给 workflow UI 绑域名**：Vercel 加 `workflow.snailnow.com`（国内更稳）。
- **内存补到 32G 双通道**。
