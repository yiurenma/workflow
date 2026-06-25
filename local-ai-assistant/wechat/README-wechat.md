# 微信接入（阶段二）

让你在手机微信里直接和本地的「小流」对话。本地主机用开源机器人
[chatgpt-on-wechat](https://github.com/zhayujie/chatgpt-on-wechat)（简称 cow）扫码登录一个微信号，
所有消息转发给本机 Ollama 的 OpenAI 兼容接口（`workflow-helper` 模型）。**不写代码，改配置即可。**

## ⚠️ 防封号 / 防误触（先读）

- 微信对「网页/iPad 协议」机器人有**封号风险**。**强烈建议用一个小号扫码**，不要用主力大号。
- 必须设**白名单**（`nick_name_white_list`），只回复你自己，避免误回别人、避免群里刷屏。
- 别高频压测；像正常人一样用。

## 步骤

1. **确认本地模型已就绪**（阶段一已完成）：
   ```bash
   ollama run workflow-helper "你好"
   ```
   Ollama 默认在 `http://127.0.0.1:11434`，已兼容 OpenAI 接口（`/v1`）。

2. **装 cow**：
   ```bash
   git clone https://github.com/zhayujie/chatgpt-on-wechat
   cd chatgpt-on-wechat
   pip install -r requirements.txt
   ```

3. **配置**：把本目录的 `config.example.json` 复制成 cow 目录下的 `config.json`，
   按需修改：
   - `model`: `workflow-helper`（就是你 `ollama create` 出来的模型名）
   - `open_ai_api_base`: `http://127.0.0.1:11434/v1`
   - `open_ai_api_key`: 随便填（Ollama 不校验），示例用 `ollama`
   - `nick_name_white_list`: 填**你自己的微信昵称**

   > 注意：cow 会根据 `model` 名选择「机器人类型」。若某些版本不认识自定义模型名而报错，
   > 把 `model` 临时设为 `gpt-3.5-turbo` 这种它认识的名字（请求照样会发到上面的
   > `open_ai_api_base`，即你的 Ollama），或参考 cow 文档把 `bot_type` 指向 OpenAI 兼容后端。

4. **启动并扫码**：
   ```bash
   python app.py
   ```
   终端会出二维码，用**要当机器人的那个微信号**扫码登录。

5. **验收**：用你（主人）的微信给机器人号发：「什么是 IFELSE 节点？」
   → 应当用小流的语气、按知识库内容回复。

## 备选：连 Open WebUI 而不是直连 Ollama

如果你想让微信和网页共享同一套「记忆/账号」，可把 `open_ai_api_base` 指向 Open WebUI 的
OpenAI 兼容地址（`http://127.0.0.1:3000/api`，并在 Open WebUI 里生成一个 API Key 填到
`open_ai_api_key`）。直连 Ollama 更简单，先用直连即可。
