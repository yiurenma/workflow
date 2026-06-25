# 企业微信回调 · VPS(frps + Caddy)搭建

> 🟡 **多数情况不需要本方案。优先用 [Cloudflare Tunnel](#) 代替**（家里主机 cloudflared 加一条
> `wx.你的域名 → 127.0.0.1:9898`，零额外机器、自带 HTTPS）。
> ⚠️ 尤其**别用你的 VPN 服务器**来跑这个：若该 VPS 已装 Xray/V2Ray 等占用 443，Caddy 会和它抢端口、
> 还可能影响 VPN。本文件保留作"有独立空闲公网 VPS"时的参考。

企业微信「接收消息」回调要求**公网 HTTPS + 标准端口**,而家里主机在 CGNAT 后面。
用一台有公网 IP 的 VPS（你已有 DigitalOcean droplet）跑 **frps + Caddy**,把家里 cow 的回调口
（`9898` `/wxcomapp`）暴露成 `https://wx.你的域名/wxcomapp`。

```
[企业微信云] --回调--> https://wx.snailnow.com/wxcomapp (VPS:443 Caddy 自动HTTPS)
                          └ reverse_proxy 127.0.0.1:9898 (frps 暴露口)
                              └ frp 隧道 ──> 家里主机 127.0.0.1:9898 (cow wechatcom_app)
```

> 💡 其实也可以**不用 VPS**：你给 AI 网关装的 Cloudflare Tunnel 顺带加一条
> `wx.snailnow.com → http://127.0.0.1:9898` 就行（cloudflared 跑在家里主机）。
> 既然你已有 VPS、更可控/国内更稳，下面是 VPS 方案。二选一即可。

---

## 步骤 A：DNS（在 Cloudflare）
加一条 A 记录：`wx` → `<droplet 公网IP>`，**Proxy 状态设「DNS only」灰云**
（让 Caddy 自己签 Let's Encrypt 证书，别走橙云代理）。

## 步骤 B：VPS 上装 frps（frp 服务端）
SSH 进 droplet（Ubuntu）：
```bash
mkdir -p /opt/frp && cd /opt/frp
# 版本号去 github.com/fatedier/frp/releases 看最新；架构按你的 droplet（多为 amd64）
curl -LO https://github.com/fatedier/frp/releases/download/v0.61.0/frp_0.61.0_linux_amd64.tar.gz
tar xf frp_0.61.0_linux_amd64.tar.gz --strip-components=1
cat > /opt/frp/frps.toml <<'EOF'
bindPort = 7000
auth.method = "token"
auth.token = "换成一串长随机密码-家里frpc要一致"
EOF
cat > /etc/systemd/system/frps.service <<'EOF'
[Unit]
Description=frps
After=network.target
[Service]
Restart=always
ExecStart=/opt/frp/frps -c /opt/frp/frps.toml
[Install]
WantedBy=multi-user.target
EOF
systemctl enable --now frps
systemctl status frps --no-pager
```

## 步骤 C：防火墙
开放 22 / 80 / 443 / 7000；**不要**对外开 9898（只让本机 Caddy 访问，更安全）：
```bash
ufw allow OpenSSH && ufw allow 80 && ufw allow 443 && ufw allow 7000 && ufw --force enable
```
（如果你用的是 DigitalOcean 云防火墙，就在面板上放行这几个端口。）

## 步骤 D：装 Caddy（自动 HTTPS）
```bash
apt install -y debian-keyring debian-archive-keyring apt-transport-https curl
curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/gpg.key' | gpg --dearmor -o /usr/share/keyrings/caddy-stable-archive-keyring.gpg
curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/debian.deb.txt' | tee /etc/apt/sources.list.d/caddy-stable.list
apt update && apt install -y caddy

cat > /etc/caddy/Caddyfile <<'EOF'
wx.snailnow.com {
    reverse_proxy 127.0.0.1:9898
}
EOF
systemctl reload caddy
```
Caddy 会自动向 Let's Encrypt 申请证书（需 DNS 已生效 + 80/443 开放 + 灰云）。
此刻访问 `https://wx.snailnow.com` 会 502（正常——家里 frpc 还没连上）。

## 步骤 E：家里主机（到货后）跑 frpc
见 [`frpc.example.ini`](frpc.example.ini)：`server_addr=<droplet IP>`、`server_port=7000`、
`token` 与 frps 一致，隧道 `local_port=9898 / remote_port=9898`。启动后 502 变成正常响应。

## 步骤 F：回填企业微信
企业微信后台 → 自建应用 → **接收消息 → 设置API接收**：
- URL：`https://wx.snailnow.com/wxcomapp`
- Token / EncodingAESKey：和 `wechat/config.json` 里一致
- ⚠️ 先确保 cow + frpc 在家里跑起来了，**再**点保存（保存会立即回调验证）。

## 自测
- `curl https://wx.snailnow.com`（cow 未起时 502 正常，证明 Caddy + 证书 OK）。
- frps 日志：`journalctl -u frps -f`；家里 frpc 连上后能看到 proxy 上线。
