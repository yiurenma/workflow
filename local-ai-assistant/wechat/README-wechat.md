# 微信接入（阶段二）—— 企业微信(WeCom)官方机器人

让你在**个人微信**里直接和本地的「小流」对话，但**不碰个人号协议、零封号风险**。
做法：建一个**企业微信自建应用**（官方 API），把消息转发给本机 Ollama 的 `workflow-helper`。
你的个人微信与企业微信消息互通，体验≈"在微信里跟小流聊天"。

> 为什么不用个人号扫码？个人号「网页/iPad 协议」机器人有**封号风险**，大号被封代价大。
> 企业微信是官方接口，**不会因此封你大号**——这是稳妥的正路。

## 一次性准备：3 个 ID/密钥 + 1 个回调

### A. 建企业 + 拿凭据（在 work.weixin.qq.com）
1. 注册一个企业微信（**一人也能建**，免费）。
2. **我的企业 → 企业信息**：记下 **企业ID（CorpID）**。
3. **应用管理 → 应用 → 自建 → 创建应用**（名字如「小流」，可见范围选你自己）：
   - 进应用，记下 **AgentId** 和 **Secret**。
4. 应用里 **接收消息 → 设置API接收**：
   - **URL**：你的公网回调地址（见下方「内网穿透」），形如 `https://你的域名/wxcomapp`
   - **Token**：自己起一串
   - **EncodingAESKey**：点「随机获取」（43 位）
   - 这三项同时填进 `config.json`（`wechatcomapp_token` / `wechatcomapp_aes_key`）。
   - ⚠️ 点「保存」时企业微信会**立刻回调你的 URL 做验证**，所以要**先把机器人和穿透都跑起来**，再点保存。

### B. 让个人微信能收发
**我的企业 → 微信插件**：用个人微信扫码关注/绑定。绑定后，企业应用发的消息会到你个人微信，你也能回。

## 内网穿透（家用网络必需）

企业微信要把你的消息**回调**到你家主机，而联通家宽多是 CGNAT（无公网 IP），所以需要内网穿透，
把本机端口（cow 的 `wechatcom_app` 默认 **9898**，路径 `/wxcomapp`）暴露成**公网 HTTPS**。

- **frp（推荐，稳）**：自备一台有公网 IP 的小 VPS（¥10–30/月）跑 `frps`，家里跑 `frpc`。
  示例见 [`frpc.example.ini`](frpc.example.ini)。需在 VPS 上配 HTTPS（如 Caddy/Nginx 自动证书）。
- **cpolar / 花生壳（零成本起步）**：SaaS 穿透，注册即用、自带 HTTPS 域名。
  缺点：免费版域名会变，**域名一变就得回企业微信后台改回调 URL**。

> 验证回调通不通：穿透起来后，浏览器访问 `https://你的域名/wxcomapp` 应能连上 cow 的服务（返回非 404 的响应）。

## 启动顺序（重要）

1. 确认本地模型就绪：`ollama run workflow-helper "你好"`。
2. 装 cow 并配置：
   ```bash
   git clone https://github.com/zhayujie/chatgpt-on-wechat
   cd chatgpt-on-wechat
   pip install -r requirements.txt
   # 把本目录 config.example.json 复制成 cow 目录下的 config.json，填好 A 步的 5 个值
   ```
3. **先**启动穿透（frpc / cpolar），**再** `python app.py` 起 cow（监听 9898）。
4. **最后**回企业微信后台点「保存」接收消息设置 → 验证通过即接通。
5. **验收**：个人微信给应用发「什么是 IFELSE 节点？」→ 小流按知识库、用定制语气回复。

> 模型名小坑：cow 按 `model` 名挑「机器人类型」。若某版本不认 `workflow-helper` 报错，
> 把 `model` 临时设为 `gpt-3.5-turbo`（请求照样发到 `open_ai_api_base` 指的 Ollama），
> 或按 cow 文档把 `bot_type` 指向 OpenAI 兼容后端。

## 想接「小流（自动）」路由模型？

把 `open_ai_api_base` 指向 **Open WebUI** 的 OpenAI 兼容地址（`http://127.0.0.1:3000/api`，
并在 Open WebUI 里生成 API Key 填 `open_ai_api_key`），`model` 用路由模型名，
即可让微信也走「自动选模型 + 工具」那套。直连 Ollama 更简单，先用直连亦可。
