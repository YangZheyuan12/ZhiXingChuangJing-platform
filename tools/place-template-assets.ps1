<#
.SYNOPSIS
  把 template-assets-inbox/ 里按编号 01-21 命名的图片，
  按 docs/templates/prompt-list.md 的映射表搬运/重命名到
  frontend/public/templates/<template_code>/<file>.jpg。

.DESCRIPTION
  使用方式：
    1. 按编号（01.jpg .. 21.jpg）把 AI 生成的图放进仓库根的 template-assets-inbox/
    2. 在仓库根执行：
         pwsh tools/place-template-assets.ps1
       或带 -DryRun 参数只打印映射不实际搬运：
         pwsh tools/place-template-assets.ps1 -DryRun

.PARAMETER DryRun
  只打印映射，不做实际搬运。用于预检。

.PARAMETER Copy
  用 Copy-Item 代替 Move-Item，保留 inbox 原文件。默认是 Move。

.PARAMETER InboxDir
  自定义收件箱目录。默认 ./template-assets-inbox。

.PARAMETER Force
  覆盖已有目标文件。默认跳过已存在的。
#>

[CmdletBinding()]
param(
  [switch]$DryRun,
  [switch]$Copy,
  [switch]$Force,
  [string]$InboxDir = (Join-Path $PSScriptRoot '..' 'template-assets-inbox')
)

$ErrorActionPreference = 'Stop'

# 编号 → 目标路径（相对仓库根）
$mapping = @(
  @{ Id = '01'; Target = 'frontend/public/templates/red_journey_long_march/cover.jpg';          Desc = '长征 · 综合封面' }
  @{ Id = '02'; Target = 'frontend/public/templates/red_journey_long_march/ruijin.jpg';         Desc = '长征 · 瑞金纪念碑' }
  @{ Id = '03'; Target = 'frontend/public/templates/red_journey_long_march/snow_mountain.jpg';  Desc = '长征 · 雪山草地' }
  @{ Id = '04'; Target = 'frontend/public/templates/red_journey_long_march/luding_bridge.jpg';  Desc = '长征 · 泸定桥' }
  @{ Id = '05'; Target = 'frontend/public/templates/red_journey_long_march/yan_an.jpg';         Desc = '长征 · 延安窑洞' }
  @{ Id = '06'; Target = 'frontend/public/templates/red_journey_long_march/exit.jpg';           Desc = '长征 · 升旗日出' }
  @{ Id = '07'; Target = 'frontend/public/templates/museum_bronze_hall/cover.jpg';              Desc = '文博 · 综合封面' }
  @{ Id = '08'; Target = 'frontend/public/templates/museum_bronze_hall/entrance.jpg';           Desc = '文博 · 博物馆大厅' }
  @{ Id = '09'; Target = 'frontend/public/templates/museum_bronze_hall/hall.jpg';               Desc = '文博 · 青铜展厅' }
  @{ Id = '10'; Target = 'frontend/public/templates/museum_bronze_hall/comments.jpg';           Desc = '文博 · 留言廊' }
  @{ Id = '11'; Target = 'frontend/public/templates/timeline_learning/cover.jpg';               Desc = '研学 · 综合封面' }
  @{ Id = '12'; Target = 'frontend/public/templates/timeline_learning/topic.jpg';               Desc = '研学 · 立题教室' }
  @{ Id = '13'; Target = 'frontend/public/templates/timeline_learning/research.jpg';            Desc = '研学 · 户外调研' }
  @{ Id = '14'; Target = 'frontend/public/templates/timeline_learning/result.jpg';              Desc = '研学 · 成果展示桌' }
  @{ Id = '15'; Target = 'frontend/public/templates/timeline_learning/defense.jpg';             Desc = '研学 · 阶梯教室' }
  @{ Id = '16'; Target = 'frontend/public/templates/map_culture_tour/cover.jpg';                Desc = '地图 · 综合封面' }
  @{ Id = '17'; Target = 'frontend/public/templates/map_culture_tour/ancient.jpg';              Desc = '地图 · 中式古建' }
  @{ Id = '18'; Target = 'frontend/public/templates/map_culture_tour/silk_road.jpg';            Desc = '地图 · 丝绸之路沙漠' }
  @{ Id = '19'; Target = 'frontend/public/templates/map_culture_tour/dunhuang.jpg';             Desc = '地图 · 敦煌洞窟' }
  @{ Id = '20'; Target = 'frontend/public/templates/minimal_whiteboard/cover.jpg';              Desc = '白板 · 极简封面' }
  @{ Id = '21'; Target = 'frontend/public/templates/minimal_whiteboard/canvas.jpg';             Desc = '白板 · 自由画布' }
)

# 仓库根目录（脚本所在目录的上级）
$repoRoot = Resolve-Path (Join-Path $PSScriptRoot '..')
Set-Location $repoRoot

$inbox = Resolve-Path -Path $InboxDir -ErrorAction SilentlyContinue
if (-not $inbox) {
  Write-Host "✗ 收件箱目录不存在：$InboxDir" -ForegroundColor Red
  Write-Host "   请先创建并把 AI 生成的图按 01.jpg..21.jpg 放进去。" -ForegroundColor Yellow
  exit 1
}

$action = if ($Copy) { 'Copy' } else { 'Move' }
$mode = if ($DryRun) { '[DRY-RUN]' } else { "[$action]" }

Write-Host ""
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "  模板素材落地器 $mode" -ForegroundColor Cyan
Write-Host "  Inbox : $inbox" -ForegroundColor DarkGray
Write-Host "  Repo  : $repoRoot" -ForegroundColor DarkGray
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""

$found   = 0
$missing = 0
$skipped = 0
$done    = 0

foreach ($item in $mapping) {
  $id = $item.Id
  $targetRel = $item.Target
  $desc = $item.Desc

  # 尝试匹配多种扩展名（AI 工具可能出 .jpg/.jpeg/.png/.webp）
  $candidates = @(
    (Join-Path $inbox "$id.jpg")
    (Join-Path $inbox "$id.jpeg")
    (Join-Path $inbox "$id.png")
    (Join-Path $inbox "$id.webp")
  )
  $source = $candidates | Where-Object { Test-Path $_ } | Select-Object -First 1

  if (-not $source) {
    Write-Host ("  [{0}] ⚠ 缺：{1}" -f $id, $desc) -ForegroundColor DarkYellow
    $missing++
    continue
  }

  $found++
  $sourceLeaf = Split-Path $source -Leaf
  $targetAbs = Join-Path $repoRoot $targetRel

  if ((Test-Path $targetAbs) -and (-not $Force)) {
    Write-Host ("  [{0}] ⏭ 跳过（目标已存在）{1} → {2}" -f $id, $sourceLeaf, $targetRel) -ForegroundColor DarkGray
    $skipped++
    continue
  }

  # 确保目标目录存在
  $targetDir = Split-Path $targetAbs -Parent
  if (-not $DryRun -and -not (Test-Path $targetDir)) {
    New-Item -ItemType Directory -Path $targetDir -Force | Out-Null
  }

  Write-Host ("  [{0}] ✓ {1,-30} → {2}" -f $id, $sourceLeaf, $targetRel) -ForegroundColor Green

  if (-not $DryRun) {
    if ($Copy) {
      Copy-Item -Path $source -Destination $targetAbs -Force:$Force
    } else {
      Move-Item -Path $source -Destination $targetAbs -Force:$Force
    }
  }
  $done++
}

Write-Host ""
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ("  汇总：共 21 张 | 已找到 {0} 张 | 处理 {1} 张 | 跳过 {2} 张 | 缺失 {3} 张" -f $found, $done, $skipped, $missing) -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""

if ($missing -gt 0) {
  Write-Host "💡 缺失的编号可以稍后再补齐，再跑一次本脚本即可（幂等）。" -ForegroundColor Yellow
}
if ($DryRun) {
  Write-Host "💡 当前是 DryRun，实际未搬运。去掉 -DryRun 再跑一次正式执行。" -ForegroundColor Yellow
}

exit 0
