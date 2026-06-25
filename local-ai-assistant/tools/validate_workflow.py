#!/usr/bin/env python3
"""工作流 JSON 校验器 + 自动修复。

按 docs/98-product-docs/reference/{workflow-json,nodes,rules-jsonpath}.md 的规则，
校验「自然语言生成器」产出的工作流 JSON，并对常见幻觉做自动修复。

- 错误(errors)：会让画布 Import 失败的硬问题（阻断）。
- 警告(warnings)：Import 已放宽、但很可能是 bug 的问题（建议改）。
- 修复(fixes)：自动改好的项（如 JSONPath 缺 `$`、剥离 ``` 代码围栏）。

可作为库用：
    from validate_workflow import validate
    report = validate(workflow_or_str)   # -> {ok, errors, warnings, fixes, workflow}

也可作为命令行用：
    python validate_workflow.py path.json            # 打印报告，有错误则退出码 1
    python validate_workflow.py - < input.json       # 从 stdin 读
    python validate_workflow.py path.json --fix      # 把修复后的 JSON 打到 stdout
"""
import json
import re
import sys

# 后端线格式的 6 种合法 action.type（见 reference/nodes.md）
LEGAL_TYPES = {
    "CONSUMER", "CONSUMERWITHOUTERROR", "IFELSE",
    "MESSAGE", "FUNCTION_V2", "FUNCTION_V3",
}
HTTP_TYPES = {"CONSUMER", "CONSUMERWITHOUTERROR"}
# UI 枚举名（导入不接受，常被模型误用）
UI_ENUM_HINTS = {"HTTP_CALL", "LOGIC", "FUNCTION", "HTTP", "BRANCH", "NOTIFY"}
IFELSE_VIRTUAL_RE = re.compile(r"^IFELSE_.+_(true|false)$")


def _strip_code_fences(text):
    """剥离 ```json ... ``` 之类的 markdown 代码围栏。"""
    t = text.strip()
    if t.startswith("```"):
        t = re.sub(r"^```[a-zA-Z0-9]*\s*", "", t)
        t = re.sub(r"\s*```$", "", t)
    return t.strip()


def _is_single_jsonpath(key):
    """规则键的硬性约束：单个 JSONPath，以 $ 开头，不含逗号/分号，非自由文本。"""
    if not isinstance(key, str):
        return False
    k = key.strip()
    if not k or not k.startswith("$"):
        return False
    if "," in k or ";" in k:
        return False
    return True


def _try_fix_key(key):
    """对缺 `$` 但形似路径的键，自动补 `$.` 前缀。返回 (新键, 修复说明 or None)。"""
    if not isinstance(key, str):
        return key, None
    k = key.strip()
    if not k or k.startswith("$"):
        return key, None
    # 含逗号/分号/空格/比较运算符的，多半是自由文本或多路径，无法安全自动修复
    if any(c in k for c in [",", ";", " "]) or any(op in k for op in ["<", ">", "="]):
        return key, None
    newk = "$." + k.lstrip(".")
    return newk, f"规则键补全 JSONPath 前缀: {key!r} -> {newk!r}"


def validate(workflow, autofix=True):
    """校验工作流 JSON。workflow 可为 dict 或 JSON 字符串（自动剥围栏）。"""
    errors, warnings, fixes = [], [], []

    if isinstance(workflow, str):
        raw = _strip_code_fences(workflow)
        try:
            workflow = json.loads(raw)
        except json.JSONDecodeError as e:
            return {"ok": False, "errors": [f"JSON 解析失败: {e}"],
                    "warnings": [], "fixes": [], "workflow": None}

    if not isinstance(workflow, dict):
        return {"ok": False, "errors": ["顶层必须是对象 { pluginList, uiMapList }"],
                "warnings": [], "fixes": [], "workflow": workflow}

    plugin_list = workflow.get("pluginList")
    if not isinstance(plugin_list, list):
        errors.append("缺少 pluginList 数组（节点列表）")
        plugin_list = []
    ui_map_list = workflow.get("uiMapList")
    if ui_map_list is None:
        ui_map_list = []
    elif not isinstance(ui_map_list, list):
        errors.append("uiMapList 必须是数组（连线列表）")
        ui_map_list = []

    # --- 节点校验 ---
    seen_ids = set()
    node_ids = set()
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
            if nid in seen_ids:
                errors.append(f"{where}: 节点 id 重复 -> {nid}")
            seen_ids.add(nid)

        action = node.get("action")
        if not isinstance(action, dict):
            errors.append(f"{where}: 缺少 action 对象")
        else:
            atype = action.get("type")
            if atype not in LEGAL_TYPES:
                hint = ""
                if isinstance(atype, str) and atype.upper() in UI_ENUM_HINTS:
                    hint = "（这是 UI 枚举名，导入不接受；请改用后端线格式类型）"
                errors.append(
                    f"{where}: action.type 非法 -> {atype!r}{hint}；"
                    f"只接受 {sorted(LEGAL_TYPES)}"
                )
            if atype in HTTP_TYPES and not action.get("httpRequestMethod"):
                warnings.append(f"{where}: {atype} 缺 httpRequestMethod（HTTP 类节点通常需要）")
            if atype in HTTP_TYPES and not action.get("httpRequestUrlWithQueryParameter"):
                warnings.append(f"{where}: {atype} 缺 httpRequestUrlWithQueryParameter")

        # 规则校验 + 自动修复
        rules = node.get("ruleList")
        if rules is not None:
            if not isinstance(rules, list):
                errors.append(f"{where}.ruleList: 必须是数组")
            else:
                for j, rule in enumerate(rules):
                    rwhere = f"{where}.ruleList[{j}]"
                    if not isinstance(rule, dict) or "key" not in rule:
                        errors.append(f"{rwhere}: 规则需为 {{ key, remark }}")
                        continue
                    key = rule["key"]
                    if not _is_single_jsonpath(key):
                        newk, note = (_try_fix_key(key) if autofix else (key, None))
                        if note:
                            rule["key"] = newk
                            fixes.append(f"{rwhere}: {note}")
                        else:
                            errors.append(
                                f"{rwhere}: 规则键必须是单个 JSONPath（以 $ 开头、"
                                f"无逗号/分号、非自由文本）-> {key!r}"
                            )

    # --- 连线校验（Import 已放宽，故记为 warning）---
    for i, edge in enumerate(ui_map_list):
        where = f"uiMapList[{i}]"
        if not isinstance(edge, dict):
            errors.append(f"{where}: 连线必须是对象")
            continue
        for end in ("source", "target"):
            val = edge.get(end)
            if val is None:
                warnings.append(f"{where}: 缺少 {end}")
                continue
            sval = str(val)
            if sval not in node_ids and not IFELSE_VIRTUAL_RE.match(sval):
                warnings.append(
                    f"{where}.{end} -> {sval!r} 不在节点列表中（且非 IFELSE_<id>_true/false 虚拟端点）"
                )

    return {
        "ok": len(errors) == 0,
        "errors": errors,
        "warnings": warnings,
        "fixes": fixes,
        "workflow": workflow,
    }


def _format_report(rep):
    lines = []
    lines.append("✅ 校验通过" if rep["ok"] else "❌ 校验未通过")
    for tag, items in (("ERROR", rep["errors"]), ("WARN", rep["warnings"]), ("FIX", rep["fixes"])):
        for it in items:
            lines.append(f"  [{tag}] {it}")
    return "\n".join(lines)


def main():
    args = [a for a in sys.argv[1:]]
    fix_out = "--fix" in args
    args = [a for a in args if a != "--fix"]
    if not args:
        print("用法: python validate_workflow.py <path.json | -> [--fix]", file=sys.stderr)
        sys.exit(2)
    src = args[0]
    text = sys.stdin.read() if src == "-" else open(src, encoding="utf-8").read()

    rep = validate(text)
    if fix_out:
        print(json.dumps(rep["workflow"], ensure_ascii=False, indent=2))
        print(_format_report(rep), file=sys.stderr)
    else:
        print(_format_report(rep))
    sys.exit(0 if rep["ok"] else 1)


if __name__ == "__main__":
    main()
