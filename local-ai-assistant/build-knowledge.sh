#!/usr/bin/env bash
# 把 docs/98-product-docs 的用户文档拼成一份知识库，并注入 Modelfile 模板，
# 生成两份可直接 ollama create 的文件：
#     knowledge/Modelfile.chat.generated  （答疑，基座 qwen2.5:14b-instruct）
#     knowledge/Modelfile.code.generated  （写码，基座 qwen2.5-coder:14b-instruct）
# 用法：  ./build-knowledge.sh        （在 macOS / Linux / Git-Bash 上跑）
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
DOCS="$REPO_ROOT/docs/98-product-docs"
OUT_DIR="$SCRIPT_DIR/knowledge"
KB="$OUT_DIR/knowledge.md"
MODELFILE_TPL="$SCRIPT_DIR/Modelfile"
SYS_PROMPT="$SCRIPT_DIR/system-prompt.md"

# 两个基座（换模型只改这里）
FROM_CHAT="qwen2.5:14b-instruct"
FROM_CODE="qwen2.5-coder:14b-instruct"
# 写码模型的角色补充（注入 SYSTEM；答疑模型为空）
ROLE_NOTE_CODE="【写码模式 / Coding mode】生成工作流 JSON 后，必须先调用 validate_workflow 工具校验；有错先按返回的 errors 自我修正，再交付合法 JSON。"

mkdir -p "$OUT_DIR"

# 按顺序拼接的源文件（相对 docs/98-product-docs/）
FILES=(
  "01-product-manual.md"
  "02-getting-started.md"
  "03-concepts.md"
  "reference/nodes.md"
  "reference/api-call.md"
  "reference/error-codes.md"
  "reference/rules-jsonpath.md"
  "reference/workflow-json.md"
  "CHANGELOG.md"
)

echo "# Workflow Studio 平台知识库（由 build-knowledge 自动生成，请勿手改）" > "$KB"
echo "" >> "$KB"

count=0
for f in "${FILES[@]}"; do
  if [[ -f "$DOCS/$f" ]]; then
    {
      echo ""
      echo "<!-- ===== 来源: docs/98-product-docs/$f ===== -->"
      echo ""
      cat "$DOCS/$f"
      echo ""
    } >> "$KB"
    count=$((count + 1))
  else
    echo "WARN: 缺少源文件 $DOCS/$f" >&2
  fi
done

# 把模板里的占位符替换好，生成一份 Modelfile。
# 参数：$1=基座模型  $2=ROLE_NOTE 字符串  $3=输出路径
gen_modelfile() {
  local from_model="$1" role_note="$2" out="$3"
  awk -v from_model="$from_model" -v role_note="$role_note" -v sp="$SYS_PROMPT" -v kb="$KB" '
    { gsub(/\{\{FROM_MODEL\}\}/, from_model) }
    /^\{\{SYSTEM_PROMPT\}\}$/ { while ((getline line < sp) > 0) print line; close(sp); next }
    /^\{\{ROLE_NOTE\}\}$/     { if (role_note != "") print role_note; next }
    /^\{\{KNOWLEDGE\}\}$/     { while ((getline line < kb) > 0) print line; close(kb); next }
    { print }
  ' "$MODELFILE_TPL" > "$out"
}

gen_modelfile "$FROM_CHAT" ""                "$OUT_DIR/Modelfile.chat.generated"
gen_modelfile "$FROM_CODE" "$ROLE_NOTE_CODE" "$OUT_DIR/Modelfile.code.generated"

echo "✅ 已合并 $count 个文档 -> $KB"
echo "✅ 已生成 -> $OUT_DIR/Modelfile.chat.generated  (基座 $FROM_CHAT)"
echo "✅ 已生成 -> $OUT_DIR/Modelfile.code.generated  (基座 $FROM_CODE)"
echo ""
echo "下一步："
echo "  ollama create workflow-helper      -f \"$OUT_DIR/Modelfile.chat.generated\""
echo "  ollama create workflow-helper-code -f \"$OUT_DIR/Modelfile.code.generated\""
echo "  ollama run workflow-helper \"调用 workflow API 必须带哪个请求头？少了会怎样？\""
