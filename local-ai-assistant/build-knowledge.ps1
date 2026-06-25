# 把 docs/98-product-docs 的用户文档拼成一份知识库，并注入 Modelfile 模板，
# 生成两份可直接 ollama create 的文件：
#     knowledge/Modelfile.chat.generated  （答疑，基座 qwen2.5:14b-instruct）
#     knowledge/Modelfile.code.generated  （写码，基座 qwen2.5-coder:14b-instruct）
# 用法（Windows PowerShell，在本目录下）：  ./build-knowledge.ps1
$ErrorActionPreference = 'Stop'

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RepoRoot  = Split-Path -Parent $ScriptDir
$Docs      = Join-Path $RepoRoot 'docs/98-product-docs'
$OutDir    = Join-Path $ScriptDir 'knowledge'
$Kb        = Join-Path $OutDir 'knowledge.md'
$Tpl       = Join-Path $ScriptDir 'Modelfile'
$SysPrompt = Join-Path $ScriptDir 'system-prompt.md'

# 两个基座（换模型只改这里）
$FromChat = 'qwen2.5:14b-instruct'
$FromCode = 'qwen2.5-coder:14b-instruct'
# 写码模型的角色补充（注入 SYSTEM；答疑模型为空）
$RoleNoteCode = '【写码模式 / Coding mode】生成工作流 JSON 后，必须先调用 validate_workflow 工具校验；有错先按返回的 errors 自我修正，再交付合法 JSON。'

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

$kbText = Get-Content -LiteralPath $Kb -Raw
$spText = Get-Content -LiteralPath $SysPrompt -Raw

# 把模板里的占位符替换好，生成一份 Modelfile。
function New-Modelfile([string]$FromModel, [string]$RoleNote, [string]$OutPath) {
  $result = New-Object System.Text.StringBuilder
  foreach ($line in Get-Content -LiteralPath $Tpl) {
    $line = $line -replace '\{\{FROM_MODEL\}\}', $FromModel
    if ($line -eq '{{SYSTEM_PROMPT}}')   { [void]$result.AppendLine($spText) }
    elseif ($line -eq '{{ROLE_NOTE}}')   { if ($RoleNote -ne '') { [void]$result.AppendLine($RoleNote) } }
    elseif ($line -eq '{{KNOWLEDGE}}')   { [void]$result.AppendLine($kbText) }
    else                                 { [void]$result.AppendLine($line) }
  }
  [System.IO.File]::WriteAllText($OutPath, $result.ToString(), (New-Object System.Text.UTF8Encoding($false)))
}

$OutChat = Join-Path $OutDir 'Modelfile.chat.generated'
$OutCode = Join-Path $OutDir 'Modelfile.code.generated'
New-Modelfile $FromChat ''            $OutChat
New-Modelfile $FromCode $RoleNoteCode $OutCode

Write-Host "✅ 已合并 $count 个文档 -> $Kb"
Write-Host "✅ 已生成 -> $OutChat  (基座 $FromChat)"
Write-Host "✅ 已生成 -> $OutCode  (基座 $FromCode)"
Write-Host ""
Write-Host "下一步："
Write-Host "  ollama create workflow-helper      -f `"$OutChat`""
Write-Host "  ollama create workflow-helper-code -f `"$OutCode`""
Write-Host "  ollama run workflow-helper `"调用 workflow API 必须带哪个请求头？少了会怎样？`""
