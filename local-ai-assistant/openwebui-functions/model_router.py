"""
title: 小流（自动）Model Router
author: workflow local-ai-assistant
version: 0.1.0
required_open_webui_version: 0.5.0
description: 一个 Open WebUI「Pipe 函数」。对外只暴露一个模型「小流（自动）」，按提问内容自动选用答疑模型或写码模型，用户无需手动切换。
"""

# 安装：Open WebUI -> Workspace -> Functions -> ＋ -> 粘贴本文件 -> Save -> 启用。
# 之后模型下拉里会出现「小流（自动）」。选它即可，路由器自动判断该用哪个底层模型：
#   - 写代码 / 生成工作流 / JSON / JSONPath / 调试 类 -> CODE_MODEL（写码模型）
#   - 其余                                          -> CHAT_MODEL（答疑模型）
# 路由器委托给底层模型（保留其自带工具 web_search / validate_workflow）。
#
# ⚠️ Open WebUI 内部委托 API 随版本变化。若你的版本导入失败，会自动退化为直连 Ollama
#    （此时仍能路由、但底层模型的「工具」不触发）。真机联调时按实际版本调通这一段即可；
#    最简退路是不装本路由器、直接用单一 qwen2.5-coder:14b 全包。

import json
import re
import urllib.request

from pydantic import BaseModel, Field

# 触发「写码模型」的关键词（中英双语）。命中任一即路由到 CODE_MODEL。
DEFAULT_CODE_KEYWORDS = ",".join([
    # 中文
    "代码", "写个", "写一个函数", "函数", "脚本", "正则", "报错", "调试", "bug", "异常",
    "工作流", "生成工作流", "节点", "规则", "jsonpath", "导入", "导出", "schema", "json",
    "重构", "实现", "算法", "sql", "接口", "curl",
    # English
    "code", "function", "script", "regex", "debug", "stack trace", "exception",
    "workflow", "generate", "node", "rule", "import", "export",
    "refactor", "implement", "algorithm", "api", "endpoint", "snippet",
])


class Pipe:
    class Valves(BaseModel):
        CHAT_MODEL: str = Field(default="workflow-helper", description="答疑底层模型 id")
        CODE_MODEL: str = Field(default="workflow-helper-code", description="写码底层模型 id")
        CODE_KEYWORDS: str = Field(default=DEFAULT_CODE_KEYWORDS, description="命中即走写码模型的关键词（逗号分隔，中英）")
        OLLAMA_BASE_URL: str = Field(default="http://host.docker.internal:11434", description="退化直连时用的 Ollama 地址")
        DEBUG: bool = Field(default=False, description="在回答前打印选中的模型（排查路由用）")

    def __init__(self):
        self.valves = self.Valves()

    def pipes(self):
        # 对外只暴露一个入口
        return [{"id": "auto", "name": "小流（自动）"}]

    # ---- 路由判定 ----
    def _last_user_text(self, body):
        for msg in reversed(body.get("messages", [])):
            if msg.get("role") == "user":
                c = msg.get("content", "")
                if isinstance(c, list):  # 多模态：拼接文本片段
                    c = " ".join(p.get("text", "") for p in c if isinstance(p, dict))
                return c or ""
        return ""

    def _route(self, body):
        text = self._last_user_text(body).lower()
        kws = [k.strip().lower() for k in self.valves.CODE_KEYWORDS.split(",") if k.strip()]
        is_code = "```" in text or any(k in text for k in kws)
        return self.valves.CODE_MODEL if is_code else self.valves.CHAT_MODEL

    async def pipe(self, body: dict, __user__=None, __request__=None, __event_emitter__=None, **kwargs):
        target = self._route(body)
        new_body = {**body, "model": target}

        if self.valves.DEBUG and __event_emitter__:
            await __event_emitter__({
                "type": "status",
                "data": {"description": f"路由 -> {target}", "done": True},
            })

        # 首选：委托给 Open WebUI 内部生成（保留底层模型的工具/记忆/过滤器）
        try:
            try:
                from open_webui.utils.chat import generate_chat_completion
            except Exception:  # noqa: BLE001
                from open_webui.main import generate_chat_completion  # 旧版路径
            user = __user__
            try:
                from open_webui.models.users import Users
                if isinstance(__user__, dict) and __user__.get("id"):
                    user = Users.get_user_by_id(__user__["id"])
            except Exception:  # noqa: BLE001
                pass
            return await generate_chat_completion(__request__, new_body, user)
        except Exception as e:  # noqa: BLE001
            # 退化：直连 Ollama 的 OpenAI 兼容端点（工具不触发，但保证可用）
            return self._fallback_direct(new_body, note=str(e))

    def _fallback_direct(self, body, note=""):
        url = self.valves.OLLAMA_BASE_URL.rstrip("/") + "/v1/chat/completions"
        payload = {"model": body["model"], "messages": body.get("messages", []), "stream": False}
        req = urllib.request.Request(
            url, data=json.dumps(payload).encode("utf-8"),
            headers={"Content-Type": "application/json"}, method="POST",
        )
        with urllib.request.urlopen(req, timeout=120) as resp:
            data = json.loads(resp.read().decode("utf-8"))
        return data["choices"][0]["message"]["content"]
