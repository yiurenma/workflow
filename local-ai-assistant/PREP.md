# 到货前 · 提前准备清单（等快递这几天就能做）

下面这些**大多不需要那台新机器**，现在就能做。带 ⏳ 的**正好需要等待时间**（DNS 生效、账号审核），
越早做越好；到货当天能省一大截。勾掉 `[x]` 跟踪进度。

## ⏳ 现在就做（因为要等生效，最该提前）
- [ ] **域名 NS 切到 Cloudflare**（你的域名在 GoDaddy）：Cloudflare 注册 → Add a site → 拿 2 个 nameserver →
      GoDaddy 域名后台改 NS → 等 Active。新域名通常几分钟~1 小时。详见 `gateway/README.md`。
- [ ] **Google PSE 钥匙**（联网搜索用）：拿 `API Key` + 搜索引擎 `cx`。步骤见 [`KEYS.md`](KEYS.md)。
- [ ] **企业微信**（微信机器人用）：work.weixin.qq.com 建免费企业 → 自建应用 → 记下 `CorpID / AgentId / Secret`。
      详见 `wechat/README-wechat.md`。
- [ ] **Clerk 账号**（以后网关鉴权用）：注册 → 建应用 → 记下 issuer（形如 `https://xxx.clerk.accounts.dev`）。
- [ ] （可选）**serper.dev** 注册拿 key（Google 搜索兜底）。
- [ ] （走企业微信/frp 才需要）**买一台香港/海外小 VPS**（¥10–30/月），装好 `frps` + `Caddy`（自动 HTTPS）。

## 到货即省时（先备好，插上电就能用）
- [ ] **Windows Xray 客户端**：准备 **v2rayN / Nekoray** 的节点/订阅（手机 v2Box 帮不到 Windows 主机）。
      默认本地代理端口 HTTP `10809` / SOCKS `10808`。
- [ ] **子域名规划**：`ai.你的域名`（AI 网关）、`app.你的域名`（给 workflow UI 绑 Vercel）、可选 `chat.你的域名`。
- [ ] **预生成 `GATEWAY_API_KEY`**：一串很长的随机字符串，先存密码管理器里。
- [ ] **通读** [`QUICKSTART.md`](QUICKSTART.md) + `gateway/README.md` + `wechat/README-wechat.md`，心里有数。
- [ ] **想好性格**（默认是「专业英式管家·小流」）：要改就先想好一句话风格，到货改 `system-prompt.md` 即可。

## 线下/硬件
- [ ] **加购第二条 16GB DDR5-6000**（同型号），到货一起装，凑 **32G 双通道**。
- [ ] **约上门装机理线师傅**（闲鱼/美团，¥100–200），交代："原封显卡插大主板、KVM 把笔记本和新主机接到同一显示器、理线"。
- [ ] 收货时**核对配件**（显卡、电源、主板、内存、固态、机箱、KVM 切换器都到齐）。

---

> 做完上面 ⏳ 那几项，到货当天基本就是"敲命令 + 填已经拿到的钥匙"，半天内能把本地 AI + 局域网网页 + 公网网关全跑通。
> 不确定哪步随时问我。
