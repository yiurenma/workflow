"""
title: Validate Workflow JSON (Workflow Studio)
author: workflow local-ai-assistant
version: 0.1.0
required_open_webui_version: 0.4.0
description: 校验 Workflow Studio 的工作流 JSON 并自动修复常见错误。写码模型生成 JSON 后自动调用，做「生成→校验→修正」闭环。
"""

# 这是一个 Open WebUI「工具(Tool)」。安装方法同 openwebui-tools/README.md。
# 写码模型(workflow-helper-code)生成工作流 JSON 后，会自动调用 validate_workflow，
# 拿到 errors 后自我修正，再交付——无需手动操作。
# 逻辑与 tools/validate_workflow.py 保持一致（此处自包含，便于直接粘贴进 Open WebUI）。

import json
import re

from pydantic import BaseModel

LEGAL_TYPES = {
    "CONSUMER", "CONSUMERWITHOUTERROR", "IFELSE",
    "MESSAGE", "FUNCTION_V2", "FUNCTION_V3",
}
HTTP_TYPES = {"CONSUMER", "CONSUMERWITHOUTERROR"}
UI_ENUM_HINTS = {"HTTP_CALL", "LOGIC", "FUNCTION", "HTTP", "BRANCH", "NOTIFY"}
IFELSE_VIRTUAL_RE = re.compile(r"^IFELSE_.+_(true|false)$")


def _strip_fences(text):
    t = text.strip()
    if t.startswith("```"):
        t = re.sub(r"^```[a-zA-Z0-9]*\s*", "", t)
        t = re.sub(r"\s*```$", "", t)
    return t.strip()


def _is_single_jsonpath(key):
    if not isinstance(key, str):
        return False
    k = key.strip()
    return bool(k) and k.startswith("$") and "," not in k and ";" not in k


def _try_fix_key(key):
    if not isinstance(key, str):
        return key, None
    k = key.strip()
    if not k or k.startswith("$"):
        return key, None
    if any(c in k for c in [",", ";", " "]) or any(op in k for op in ["<", ">", "="]):
        return key, None
    newk = "$." + k.lstrip(".")
    return newk, f"规则键补全 JSONPath 前缀: {key!r} -> {newk!r}"


def _validate(workflow):
    errors, warnings, fixes = [], [], []
    if isinstance(workflow, str):
        try:
            workflow = json.loads(_strip_fences(workflow))
        except json.JSONDecodeError as e:
            return {"ok": False, "errors": [f"JSON 解析失败: {e}"], "warnings": [],
                    "fixes": [], "workflow": None}
    if not isinstance(workflow, dict):
        return {"ok": False, "errors": ["顶层必须是对象 { pluginList, uiMapList }"],
                "warnings": [], "fixes": [], "workflow": workflow}

    plugin_list = workflow.get("pluginList")
    if not isinstance(plugin_list, list):
        errors.append("缺少 pluginList 数组（节点列表）")
        plugin_list = []
    ui_map_list = workflow.get("uiMapList") or []

    seen, node_ids = set(), set()
    for i, node in enumerate(plugin_list):
        where = f"pluginList[{i}]"
        if not isinstance(node, dict):
            errors.append(f"{where}: 节点必须是对象")
            continue
        nid = node.get("id")
        if nid is None:
            errors.append(f"{where}: 缺少节点 id")
        else:
            node_ids.add(str(nid))
            if nid in seen:
                errors.append(f"{where}: 节点 id 重复 -> {nid}")
            seen.add(nid)
        action = node.get("action")
        if not isinstance(action, dict):
            errors.append(f"{where}: 缺少 action 对象")
        else:
            atype = action.get("type")
            if atype not in LEGAL_TYPES:
                hint = "（UI 枚举名，导入不接受）" if isinstance(atype, str) and atype.upper() in UI_ENUM_HINTS else ""
                errors.append(f"{where}: action.type 非法 -> {atype!r}{hint}；只接受 {sorted(LEGAL_TYPES)}")
            if atype in HTTP_TYPES and not action.get("httpRequestMethod"):
                warnings.append(f"{where}: {atype} 缺 httpRequestMethod")
        rules = node.get("ruleList")
        if isinstance(rules, list):
            for j, rule in enumerate(rules):
                rwhere = f"{where}.ruleList[{j}]"
                if not isinstance(rule, dict) or "key" not in rule:
                    errors.append(f"{rwhere}: 规则需为 {{ key, remark }}")
                    continue
                key = rule["key"]
                if not _is_single_jsonpath(key):
                    newk, note = _try_fix_key(key)
                    if note:
                        rule["key"] = newk
                        fixes.append(f"{rwhere}: {note}")
                    else:
                        errors.append(f"{rwhere}: 规则键必须是单个 JSONPath（以 $ 开头）-> {key!r}")
        elif rules is not None:
            errors.append(f"{where}.ruleList: 必须是数组")

    for i, edge in enumerate(ui_map_list):
        where = f"uiMapList[{i}]"
        if not isinstance(edge, dict):
            errors.append(f"{where}: 连线必须是对象")
            continue
        for end in ("source", "target"):
            val = edge.get(end)
            if val is None:
                warnings.append(f"{where}: 缺少 {end}")
            elif str(val) not in node_ids and not IFELSE_VIRTUAL_RE.match(str(val)):
                warnings.append(f"{where}.{end} -> {str(val)!r} 不在节点列表中（且非 IFELSE 虚拟端点）")

    return {"ok": not errors, "errors": errors, "warnings": warnings, "fixes": fixes, "workflow": workflow}


class Tools:
    class Valves(BaseModel):
        pass

    def __init__(self):
        self.valves = self.Valves()

    def validate_workflow(self, workflow_json: str) -> str:
        """
        校验 Workflow Studio 的工作流 JSON（pluginList + uiMapList），并自动修复常见错误。
        生成工作流 JSON 后**务必**调用本工具；若返回 errors，请按提示修正后重新校验，直到通过再交付。

        :param workflow_json: 待校验的工作流 JSON 字符串（可含 ``` 代码围栏，会自动剥离）
        :return: 校验报告（ok / errors / warnings / fixes）+ 修复后的工作流 JSON
        """
        rep = _validate(workflow_json)
        out = {
            "ok": rep["ok"],
            "errors": rep["errors"],
            "warnings": rep["warnings"],
            "fixes": rep["fixes"],
            "fixed_workflow": rep["workflow"],
        }
        return json.dumps(out, ensure_ascii=False, indent=2)
