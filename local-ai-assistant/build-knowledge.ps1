# 把 docs/98-product-docs 的用户文档拼成一份知识库，并注入 Modelfile 模板，
# 生成可直接 ollama create 的 knowledge/Modelfile.generated。
# 用法（Windows PowerShell，在本目录下）：  ./build-knowledge.ps1
$ErrorActionPreference = 'Stop'

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RepoRoot  = Split-Path -Parent $ScriptDir
$Docs      = Join-Path $RepoRoot 'docs/98-product-docs'
$OutDir    = Join-Path $ScriptDir 'knowledge'
$Kb        = Join-Path $OutDir 'knowledge.md'
$Tpl       = Join-Path $ScriptDir 'Modelfile'
$SysPrompt = Join-Path $ScriptDir 'system-prompt.md'
$Out       = Join-Path $OutDir 'Modelfile.generated'

New-Item -ItemType Directory -Force -Path $OutDir | Out-Null

# 按顺序拼接的源文件（相对 docs/98-product-docs/）
$Files = @(
  '01-product-manual.md',
  '02-getting-started.md',
  '03-concepts.md',
  'reference/nodes.md',
  'reference/api-call.md',
  'reference/error-codes.md',
  'reference/rules-jsonpath.md',
  'reference/workflow-json.md',
  'CHANGELOG.md'
)

$sb = New-Object System.Text.StringBuilder
[void]$sb.AppendLine('# Workflow Studio 平台知识库（由 build-knowledge 自动生成，请勿手改）')
[void]$sb.AppendLine('')

$count = 0
foreach ($f in $Files) {
  $p = Join-Path $Docs $f
  if (Test-Path -LiteralPath $p) {
    [void]$sb.AppendLine('')
    [void]$sb.AppendLine("<!-- ===== 来源: docs/98-product-docs/$f ===== -->")
    [void]$sb.AppendLine('')
    [void]$sb.AppendLine((Get-Content -LiteralPath $p -Raw))
    [void]$sb.AppendLine('')
    $count++
  } else {
    Write-Warning "缺少源文件 $p"
  }
}
# 用 UTF-8 (无 BOM) 写出，避免 Ollama 读到 BOM
[System.IO.File]::WriteAllText($Kb, $sb.ToString(), (New-Object System.Text.UTF8Encoding($false)))

# 把 system-prompt.md 与 knowledge.md 注入模板（占位符各占一整行）
$kbText  = Get-Content -LiteralPath $Kb -Raw
$spText  = Get-Content -LiteralPath $SysPrompt -Raw
$result = New-Object System.Text.StringBuilder
foreach ($line in Get-Content -LiteralPath $Tpl) {
  if ($line -eq '{{SYSTEM_PROMPT}}')   { [void]$result.AppendLine($spText) }
  elseif ($line -eq '{{KNOWLEDGE}}')   { [void]$result.AppendLine($kbText) }
  else                                 { [void]$result.AppendLine($line) }
}
[System.IO.File]::WriteAllText($Out, $result.ToString(), (New-Object System.Text.UTF8Encoding($false)))

Write-Host "✅ 已合并 $count 个文档 -> $Kb"
Write-Host "✅ 已生成 -> $Out"
Write-Host ""
Write-Host "下一步："
Write-Host "  ollama create workflow-helper -f `"$Out`""
Write-Host "  ollama run workflow-helper `"调用 workflow API 必须带哪个请求头？少了会怎样？`""
