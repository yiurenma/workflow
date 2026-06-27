# 到货前 · 提前准备清单（等快递这几天就能做）

下面这些**大多不需要那台新机器**，现在就能做。带 ⏳ 的**正好需要等待时间**（DNS 生效、账号审核），
越早做越好；到货当天能省一大截。勾掉 `[x]` 跟踪进度。

## ⏳ 现在就做（因为要等生效，最该提前）
- [x] **域名 NS 切到 Cloudflare**（snailnow.com）—— ✅ **已 Active**，解析由 Cloudflare 接管。
      > ⚠️ 换 NS 在域名的**「Nameservers」独立设置**里（GoDaddy：域名 → Nameservers → Change → "I'll use my own
      > nameservers" → 用 Cloudflare 的两个替换 `ns49/ns50.domaincontrol.com`），**不是**在「DNS 记录」表里加/删 NS 记录。
- [x] **Google PSE 钥匙** —— ✅ 已拿到 `API Key` + 搜索引擎 `cx`，存密码管理器（**key 勿入库**）。
- [x] **企业微信凭据** —— ✅ 已拿到 `CorpID / AgentId / Secret`，存好。
      （「接收消息」回调到货后用 Cloudflare 配，见下方 VPS 那条）
- [x] **Clerk 账号** —— ✅ 已拿到 `issuer / Publishable key / Secret key`，存好。
- [x] （可选）**serper.dev** —— ✅ 已拿 key（Google 搜索兜底）。
- [x] ~~买 VPS 跑 frps+Caddy~~ —— **不需要了**。原计划用 frp+VPS 中转微信回调，但你的 DigitalOcean droplet
      **是你的 Xray VPN 机**（443 已被 Xray 占用，不适合再跑 Caddy）。**微信回调改用 Cloudflare Tunnel**：
      到货后在家里主机上 `cloudflared` 加一条 `wx.snailnow.com → http://127.0.0.1:9898` 即可——零额外机器、不碰 VPN。

## 到货即省时（先备好，插上电就能用）
- [ ] **Windows Xray 客户端**：准备 **v2rayN / Nekoray** 的节点/订阅（手机 v2Box 帮不到 Windows 主机）。
      默认本地代理端口 HTTP `10809` / SOCKS `10808`。
- [x] **workflow 域名 / 子域名规划**：✅ `workflow.snailnow.com` 已绑 Vercel 上线（workflow UI）。
      `ai.`（网关）/ `wx.`（微信）到货建 Cloudflare 隧道时自动配；可选 `chat.`。
- [ ] **预生成 `GATEWAY_API_KEY`**：一串很长的随机字符串，先存密码管理器里。
- [ ] **通读** [`QUICKSTART.md`](QUICKSTART.md) + `gateway/README.md` + `wechat/README-wechat.md`，心里有数。
- [ ] **想好性格**（默认是「专业英式管家·小流」）：要改就先想好一句话风格，到货改 `system-prompt.md` 即可。

## 线下/硬件
- [ ] ~~加购第二条 16GB 内存~~ —— **暂缓**。DDR5 现涨价（单条约 ¥1200），性价比差。
      本用法**模型在显存里、网页用 pip 免 Docker**，16G 单条够起步；价格回落或真遇内存吃紧再补凑 32G 双通道。
- [ ] **约上门装机理线师傅**（闲鱼/美团，¥100–200），交代："原封显卡插大主板、KVM 把笔记本和新主机接到同一显示器、理线"。
- [ ] 收货时**核对配件**（显卡、电源、主板、内存、固态、机箱、KVM 切换器都到齐）。

---

## 到货后 · 装机进度（实时）
- [x] 新机到货、装机理线、配件齐
- [x] VPN（v2rayN + Xray 节点）联通（坑：VPS 上误开的 ufw 挡了订阅端口 2096 → `ufw disable` 解决）
- [x] 蓝牙 / AirPods Max 修好（根因：WiFi/蓝牙**天线没装** → 拧上后端正常）
- [x] 基础软件：**Python 3.12.10 / Git 2.54 / Ollama 0.30.11 / cloudflared**（注意：Python 用 3.12，3.14 太新装不上 open-webui）
- [x] `git clone` 套件代码到 `…\workspace\workflow`
- [x] 拉两个模型（坑：`\x00`＝缓存损坏，清 `.ollama\models` 重拉；GPU 已确认在跑 `llama-server.exe`）
- [x] `build-knowledge` + `ollama create` 两个模型成功（坑：PS5.1 中文编码 → 用 Git Bash 跑 `.sh`，仓库 ps1 已加 BOM）
- [x] **起 Open WebUI（pip，:3000）跑起来、网页能对话** ✅（坑：HF 嵌入模型龟速 → 设 `HF_ENDPOINT=https://hf-mirror.com` 提速）
- [ ] 装三件套（路由器 / 联网搜索 / 工作流校验）+ 关开放注册
- [ ] 网关 + Cloudflare 隧道（`ai.snailnow.com`）
- [ ] 企业微信（`wx.snailnow.com`，同隧道）

> ✅ **到货前**那批「现在就做」已全部完成（域名 Active、Google/企业微信/Clerk/Serper 钥匙到手、`workflow.snailnow.com` 上线）。
> 微信改走 Cloudflare Tunnel，不再需要 VPS。当前正按 [`QUICKSTART-snailnow.md`](QUICKSTART-snailnow.md) 装机，进度见上。
