#!/usr/bin/env bash
# 把 docs/98-product-docs 的用户文档拼成一份知识库，并注入 Modelfile 模板，
# 生成可直接 ollama create 的 knowledge/Modelfile.generated。
# 用法：  ./build-knowledge.sh        （在 macOS / Linux / Git-Bash 上跑）
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
DOCS="$REPO_ROOT/docs/98-product-docs"
OUT_DIR="$SCRIPT_DIR/knowledge"
KB="$OUT_DIR/knowledge.md"
MODELFILE_TPL="$SCRIPT_DIR/Modelfile"
SYS_PROMPT="$SCRIPT_DIR/system-prompt.md"
MODELFILE_OUT="$OUT_DIR/Modelfile.generated"

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

# 把 system-prompt.md 与 knowledge.md 注入模板（占位符各占一整行）
awk -v sp="$SYS_PROMPT" -v kb="$KB" '
  /^\{\{SYSTEM_PROMPT\}\}$/ { while ((getline line < sp) > 0) print line; close(sp); next }
  /^\{\{KNOWLEDGE\}\}$/     { while ((getline line < kb) > 0) print line; close(kb); next }
  { print }
' "$MODELFILE_TPL" > "$MODELFILE_OUT"

echo "✅ 已合并 $count 个文档 -> $KB"
echo "✅ 已生成 -> $MODELFILE_OUT"
echo ""
echo "下一步："
echo "  ollama create workflow-helper -f \"$MODELFILE_OUT\""
echo "  ollama run workflow-helper \"调用 workflow API 必须带哪个请求头？少了会怎样？\""
