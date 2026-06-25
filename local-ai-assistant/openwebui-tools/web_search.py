"""
title: Web Search (Google-first, auto-fallback)
author: workflow local-ai-assistant
version: 0.1.0
required_open_webui_version: 0.4.0
description: 本地答不了时，模型自动调用本工具联网搜索；内部按 Google → Serper → DuckDuckGo 顺序自动兜底。
"""

# 这是一个 Open WebUI「工具(Tool)」。安装方法见同目录 README.md。
# 装好后，支持 function-calling 的模型（如 qwen2.5）会在「本地答不出」时自动调用，
# 你无需手动点任何开关。Google 优先、失败自动换备选，全在本文件里完成。

import json
import re
import urllib.parse
import urllib.request

from pydantic import BaseModel, Field


def _get_json(url, headers=None, timeout=15):
    req = urllib.request.Request(url, headers=headers or {})
    with urllib.request.urlopen(req, timeout=timeout) as resp:
        return json.loads(resp.read().decode("utf-8"))


def _post_json(url, payload, headers=None, timeout=15):
    h = {"Content-Type": "application/json"}
    if headers:
        h.update(headers)
    req = urllib.request.Request(
        url, data=json.dumps(payload).encode("utf-8"), headers=h, method="POST"
    )
    with urllib.request.urlopen(req, timeout=timeout) as resp:
        return json.loads(resp.read().decode("utf-8"))


class Tools:
    class Valves(BaseModel):
        # 在 Open WebUI 的工具设置(Valves)里填这些，无需改代码
        GOOGLE_PSE_API_KEY: str = Field(default="", description="Google Custom Search API Key（首选）")
        GOOGLE_PSE_ENGINE_ID: str = Field(default="", description="Google 可编程搜索引擎 ID (cx)")
        SERPER_API_KEY: str = Field(default="", description="serper.dev API Key（备选，也是 Google 结果，可留空）")
        ENABLE_DUCKDUCKGO: bool = Field(default=True, description="最后兜底用 DuckDuckGo（免 key）")
        MAX_RESULTS: int = Field(default=5, description="返回结果条数")

    def __init__(self):
        self.valves = self.Valves()

    def web_search(self, query: str) -> str:
        """
        联网搜索。仅当本地知识/对话无法回答，或问题需要实时/最新信息时才调用本工具。
        会优先使用 Google，失败时自动切换到备选搜索源。

        :param query: 要搜索的关键词或问题
        :return: 若干条结果（标题、摘要、链接）的文本，供你综合作答并注明来源链接
        """
        n = max(1, int(self.valves.MAX_RESULTS))
        errors = []

        # 1) Google PSE —— 首选
        if self.valves.GOOGLE_PSE_API_KEY and self.valves.GOOGLE_PSE_ENGINE_ID:
            try:
                params = urllib.parse.urlencode({
                    "key": self.valves.GOOGLE_PSE_API_KEY,
                    "cx": self.valves.GOOGLE_PSE_ENGINE_ID,
                    "q": query,
                    "num": min(n, 10),
                })
                data = _get_json("https://www.googleapis.com/customsearch/v1?" + params)
                items = data.get("items", [])
                if items:
                    return self._format("Google", [
                        (it.get("title", ""), it.get("snippet", ""), it.get("link", ""))
                        for it in items[:n]
                    ])
                errors.append("Google: 无结果")
            except Exception as e:  # noqa: BLE001
                errors.append(f"Google 失败: {e}")

        # 2) Serper —— 备选（同样是 Google 结果）
        if self.valves.SERPER_API_KEY:
            try:
                data = _post_json(
                    "https://google.serper.dev/search",
                    {"q": query, "num": n},
                    headers={"X-API-KEY": self.valves.SERPER_API_KEY},
                )
                organic = data.get("organic", [])
                if organic:
                    return self._format("Google(Serper)", [
                        (it.get("title", ""), it.get("snippet", ""), it.get("link", ""))
                        for it in organic[:n]
                    ])
                errors.append("Serper: 无结果")
            except Exception as e:  # noqa: BLE001
                errors.append(f"Serper 失败: {e}")

        # 3) DuckDuckGo —— 最后兜底（免 key）
        if self.valves.ENABLE_DUCKDUCKGO:
            try:
                results = self._duckduckgo(query, n)
                if results:
                    return self._format("DuckDuckGo", results)
                errors.append("DuckDuckGo: 无结果")
            except Exception as e:  # noqa: BLE001
                errors.append(f"DuckDuckGo 失败: {e}")

        return ("联网搜索未取得结果：" + "; ".join(errors or ["未配置任何搜索源"]) +
                "。请在本工具的 Valves 里至少填好 Google PSE 的 API Key 与 Engine ID，"
                "并确认主机的 VPN/代理已开启可访问外网。")

    def _duckduckgo(self, query, n):
        # 优先用 duckduckgo_search 库；没有则退回 HTML 端点解析
        try:
            from duckduckgo_search import DDGS  # Open WebUI 镜像通常自带
            out = []
            with DDGS() as ddgs:
                for r in ddgs.text(query, max_results=n):
                    out.append((r.get("title", ""), r.get("body", ""), r.get("href", "")))
            return out
        except Exception:  # noqa: BLE001
            req = urllib.request.Request(
                "https://html.duckduckgo.com/html/?" + urllib.parse.urlencode({"q": query}),
                headers={"User-Agent": "Mozilla/5.0"},
            )
            with urllib.request.urlopen(req, timeout=15) as resp:
                html = resp.read().decode("utf-8", "replace")
            pairs = re.findall(r'result__a[^>]*href="([^"]+)"[^>]*>(.*?)</a>', html)
            return [(re.sub("<[^>]+>", "", t), "", h) for h, t in pairs[:n]]

    def _format(self, source, results):
        lines = [f"[来源引擎: {source}]"]
        for i, (title, snippet, url) in enumerate(results, 1):
            lines.append(f"{i}. {title}\n   {snippet}\n   {url}")
        return "\n".join(lines)
