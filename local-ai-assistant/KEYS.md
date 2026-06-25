# 密钥申请指南（联网搜索用 / 可选）

联网搜索（`openwebui-tools/web_search.py`）默认首选 **Google 官方可编程搜索（PSE）**。
需要两样东西：**API Key** + **搜索引擎 ID(cx)**。**纯答疑不联网时可以先不配。**

## 一、Google API Key（在 Google Cloud 申请）

1. 打开 https://console.cloud.google.com ，用 Google 账号登录。
2. 顶部项目下拉 → **新建项目**（随便起名，如 `xiaoliu-search`）。
3. 左侧 **APIs & Services → Library（库）** → 搜 **"Custom Search API"** → **Enable（启用）**。
4. **APIs & Services → Credentials（凭据）→ Create Credentials → API key** → 复制（形如 `AIza...`）。
5. （建议）点该 key → **限制为只用 Custom Search API**，更安全。

## 二、搜索引擎 ID / cx（在可编程搜索申请）

1. 打开 https://programmablesearchengine.google.com → **Add / 添加**。
2. 起个名字；"要搜什么"处把 **"搜索整个网络（Search the entire web）"** 打开。
3. 创建后进入该引擎 → 概览/基本信息 → 复制 **Search engine ID**（即 cx）。

## 三、填到哪里

Open WebUI → Workspace → Tools → `web_search` 工具的 ⚙️（Valves）：
- `GOOGLE_PSE_API_KEY` ← 第一步的 API Key
- `GOOGLE_PSE_ENGINE_ID` ← 第二步的 cx

## 额度与注意

- **免费 100 次/天**；超出约 $5 / 千次（最高 1 万次/天）。个人完全够用。
- 联网搜索是**运行时访问 googleapis.com**，需开 VPN（见主 README「网络与代理」）。
- 隐私：开了搜索，提问会发给 Google。模型只在本地答不出时才自动联网。

## 可选：兜底搜索源

- **Serper**（serper.dev）：注册送约 2500 次免费额度，拿一个 key 填 `SERPER_API_KEY`；结果同样来自 Google。
- **DuckDuckGo**：免 key，`ENABLE_DUCKDUCKGO` 默认开，作最后兜底。
- 三者都不配也能跑（纯本地），只是没有联网搜索能力。
