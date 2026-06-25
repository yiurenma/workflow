# AI 网关 —— 让 workflow UI 的用户也能用你的本地 AI

给本地 Ollama 套一层「**鉴权 + 公网 HTTPS 接口**」，让 workflow UI 里的聊天框能安全地调到它。
**只暴露这个网关，不暴露 Ollama / Open WebUI。**

```
[workflow UI 用户(登录 Clerk)] --Bearer <Clerk JWT>--> [Cloudflare Tunnel(公网HTTPS)]
                                                              --> [网关 :8800] --验证--> [Ollama :11434 仅本地]
```

## 为什么这么设计
- **Ollama 没有鉴权**，直接暴露 = 全网蹭你显卡。网关是唯一入口，先验证再转发。
- 鉴权**两段式**：先用共享 **API Key** 跑通；以后填 Clerk issuer，自动切到**校验 Clerk JWT**，对接你的权限体系。
- **Cloudflare Tunnel** 解决"家里没有公网 IP（联通 CGNAT）"，还自带 HTTPS。

## 启动（本机）
```bash
cd local-ai-assistant/gateway
pip install -r requirements.txt
cp .env.example .env          # 按下面说明填好
uvicorn app:app --host 127.0.0.1 --port 8800
# 自测：curl -s http://127.0.0.1:8800/healthz
```

## 第一步：先用 API Key 跑通（最简）
`.env` 里：
```
AUTH_MODE=apikey
GATEWAY_API_KEY=<一串很长的随机字符串>
ALLOWED_ORIGINS=https://workflow-ui-gamma.vercel.app
```
调用（workflow UI 聊天框里这样发请求）：
```bash
curl -X POST https://ai.你的域名/v1/chat/completions \
  -H "Authorization: Bearer <你的 GATEWAY_API_KEY>" \
  -H "Content-Type: application/json" \
  -d '{"model":"workflow-helper","messages":[{"role":"user","content":"什么是 IFELSE 节点"}]}'
```

## 第二步：暴露到公网（Cloudflare Tunnel）
见 [`cloudflared.example.yml`](cloudflared.example.yml)。装 `cloudflared` → 建隧道 → 路由
`ai.你的域名` → `http://127.0.0.1:8800` → `cloudflared tunnel run`。Windows 可装成开机服务。

> 备选：用我们已有的 `../wechat/frpc.example.ini`（frp + VPS）也能暴露；或 cpolar/花生壳。
> 但 Cloudflare Tunnel 对"网页 + 自带 HTTPS + 免公网 IP"最省心。

## 第三步：换成 Clerk 鉴权（接你的权限体系）
当 workflow UI 接好 Clerk 后，前端用 `getToken()` 拿登录用户的 JWT，放进 `Authorization: Bearer`。
网关 `.env` 改：
```
AUTH_MODE=clerk          # 或 both（过渡期同时接受 API Key 和 Clerk）
CLERK_ISSUER=https://your-app.clerk.accounts.dev   # 或生产 https://clerk.你的域名
CLERK_AUTHORIZED_PARTIES=https://workflow-ui-gamma.vercel.app
```
网关用 Clerk 的 JWKS **本地验签**（无网络往返），校验签名 + `exp` + 授权方(azp)。
想再细化权限（谁能用/配额），可在 `app.py` 的 `_authorize` 返回的 claims 上加判断（如读 `claims` 里的角色）。

## 安全清单（务必）
- 只暴露网关 `:8800`；**Ollama 保持 `127.0.0.1:11434`**，不要监听 0.0.0.0。
- `ALLOWED_ORIGINS` 收紧到 workflow UI 的真实源，别留 `*`。
- 生产别用 `AUTH_MODE=none`。
- 再加一层 **Cloudflare Access** 登录最稳（鉴权代码即便出错也有兜底）。
- 限流/防滥用：可在 Cloudflare 侧加 Rate Limiting 规则，或在网关前置反代加限速。

## 它和 Open WebUI 的关系
- **Open WebUI**（:3000，局域网）= 给**你自己**的管理/聊天台。
- **本网关**（:8800，公网）= 给 **workflow UI 用户**的受控 API 入口。
- 两者都连同一个本地 Ollama，互不影响。
