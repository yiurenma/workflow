#!/usr/bin/env python3
"""自然语言 -> 工作流 JSON 骨架（调本地 Ollama）。

复用前端已有的生成提示词（workflow-ui/src/constants/prompts.ts 里的
WORKFLOW_GENERATOR_SYSTEM_PROMPT）；若该 submodule 未检出，则用内置精简提示词。

⚠️ 已知幻觉点（生成结果务必人工核对）：
  - 非法 plugin id（编造的名字）
  - 与插件类型不匹配的 action kind
  - JSONPath 规则键缺 `$` 或用逗号分隔
  - 规则类型用错（字符串 vs 数字）
  - 边(edge)指向不存在的节点

用法:
    python generate_workflow.py "收到支付消息，金额>5000 走短信，否则邮件"

环境变量:
    OLLAMA_HOST   默认 http://127.0.0.1:11434
    OLLAMA_MODEL  默认 workflow-helper
"""
import json
import os
import re
import sys
import urllib.request

FALLBACK_SYSTEM_PROMPT = (
    "你是 Workflow Studio 的工作流生成器。把用户的自然语言需求转成平台的工作流 JSON 骨架"
    "（pluginList + uiMapList）。只用平台支持的 6 种节点："
    "CONSUMER / CONSUMERWITHOUTERROR / IFELSE / MESSAGE / FUNCTION_V2 / FUNCTION_V3。"
    "规则用单个 JSONPath（以 $ 开头）。只输出 JSON，不要解释。"
)


def load_ui_system_prompt():
    """尝试从 workflow-ui 的 prompts.ts 读取官方系统提示词。"""
    here = os.path.dirname(os.path.abspath(__file__))
    repo_root = os.path.abspath(os.path.join(here, "..", ".."))
    prompts_ts = os.path.join(repo_root, "workflow-ui", "src", "constants", "prompts.ts")
    if not os.path.isfile(prompts_ts):
        return None
    try:
        text = open(prompts_ts, encoding="utf-8").read()
    except OSError:
        return None
    # 抓取 WORKFLOW_GENERATOR_SYSTEM_PROMPT = `...` 的模板字符串
    m = re.search(r"WORKFLOW_GENERATOR_SYSTEM_PROMPT\s*=\s*`(.*?)`", text, re.S)
    return m.group(1).strip() if m else None


def generate(prompt, system_prompt):
    host = os.environ.get("OLLAMA_HOST", "http://127.0.0.1:11434")
    model = os.environ.get("OLLAMA_MODEL", "workflow-helper")
    payload = {
        "model": model,
        "stream": False,
        "options": {"temperature": 0.2},
        "messages": [
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": prompt},
        ],
    }
    req = urllib.request.Request(
        f"{host.rstrip('/')}/api/chat",
        data=json.dumps(payload).encode("utf-8"),
        headers={"Content-Type": "application/json"},
        method="POST",
    )
    with urllib.request.urlopen(req, timeout=120) as resp:
        data = json.loads(resp.read().decode("utf-8"))
    return data.get("message", {}).get("content", "")


def main():
    if len(sys.argv) < 2:
        print("用法: python generate_workflow.py \"用自然语言描述你的工作流\"", file=sys.stderr)
        sys.exit(2)
    user_prompt = " ".join(sys.argv[1:])
    system_prompt = load_ui_system_prompt() or FALLBACK_SYSTEM_PROMPT
    if system_prompt is FALLBACK_SYSTEM_PROMPT:
        print("提示：未找到 workflow-ui/src/constants/prompts.ts（submodule 未检出），"
              "已用内置精简提示词。", file=sys.stderr)
    try:
        out = generate(user_prompt, system_prompt)
    except Exception as e:  # noqa: BLE001
        print(f"生成失败（Ollama 是否在运行？）: {e}", file=sys.stderr)
        sys.exit(1)
    print(out)
    print("\n⚠️ 这是骨架，导入前请人工核对 plugin id / action kind / JSONPath(以$开头) / 边引用。",
          file=sys.stderr)


if __name__ == "__main__":
    main()
