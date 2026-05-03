<#
.SYNOPSIS
  直接生成 21 张纯色 + 文字的占位 JPG 到最终路径
  （frontend/public/templates/<code>/<file>.jpg），让模板卡片先有图可显示。

.DESCRIPTION
  每组用不同色调，中心写模板名 + zone 标题，底部小字"占位图·待替换"。
  稍后用 AI 生成的真实图片覆盖这些占位即可（place-template-assets.ps1 -Force）。

.PARAMETER Force
  覆盖已有占位/真实图。默认跳过已存在的。
#>

[CmdletBinding()]
param(
  [switch]$Force
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot '..')

# 每张图：编号 / 相对路径 / 尺寸 / 背景色（十六进制）/ 主文字 / 副文字
$items = @(
  # 组 1 - 红色长征（暖红系 #8B2C1D）
  @{ Id='01'; Path='frontend/public/templates/red_journey_long_march/cover.jpg';         W=400;  H=240;  Bg='#8B2C1D'; Fg='#FFF5E1'; Title='红色记忆长征展';       Sub='综合封面' }
  @{ Id='02'; Path='frontend/public/templates/red_journey_long_march/ruijin.jpg';        W=1920; H=1080; Bg='#5B1E18'; Fg='#FFE4B5'; Title='瑞金·星火';             Sub='红色记忆长征展 · 展区 1 / 5' }
  @{ Id='03'; Path='frontend/public/templates/red_journey_long_march/snow_mountain.jpg'; W=1920; H=1080; Bg='#8F6A47'; Fg='#FFF9E6'; Title='雪山草地';               Sub='红色记忆长征展 · 展区 2 / 5' }
  @{ Id='04'; Path='frontend/public/templates/red_journey_long_march/luding_bridge.jpg'; W=1920; H=1080; Bg='#4A3528'; Fg='#F5D7A1'; Title='飞夺泸定桥';             Sub='红色记忆长征展 · 展区 3 / 5' }
  @{ Id='05'; Path='frontend/public/templates/red_journey_long_march/yan_an.jpg';        W=1920; H=1080; Bg='#A0522D'; Fg='#FFF8DC'; Title='延安会师';               Sub='红色记忆长征展 · 展区 4 / 5' }
  @{ Id='06'; Path='frontend/public/templates/red_journey_long_march/exit.jpg';          W=1920; H=1080; Bg='#B8391F'; Fg='#FFFAF0'; Title='胜利升旗';               Sub='红色记忆长征展 · 展区 5 / 5' }

  # 组 2 - 博物馆青铜（深褐 #3C2A1A）
  @{ Id='07'; Path='frontend/public/templates/museum_bronze_hall/cover.jpg';              W=400;  H=240;  Bg='#3C2A1A'; Fg='#C9A961'; Title='博物馆青铜文物展';     Sub='综合封面' }
  @{ Id='08'; Path='frontend/public/templates/museum_bronze_hall/entrance.jpg';           W=1920; H=1080; Bg='#2A1F14'; Fg='#D4B678'; Title='博物馆大厅';             Sub='博物馆青铜文物展 · 展区 1 / 3' }
  @{ Id='09'; Path='frontend/public/templates/museum_bronze_hall/hall.jpg';               W=1920; H=1080; Bg='#4A3424'; Fg='#E0BF86'; Title='青铜展厅';               Sub='博物馆青铜文物展 · 展区 2 / 3' }
  @{ Id='10'; Path='frontend/public/templates/museum_bronze_hall/comments.jpg';           W=1920; H=1080; Bg='#5A4434'; Fg='#EAD4A0'; Title='参观留言廊';             Sub='博物馆青铜文物展 · 展区 3 / 3' }

  # 组 3 - 研学时间轴（米白嫩绿 #E8E1C9 / #7FA088）
  @{ Id='11'; Path='frontend/public/templates/timeline_learning/cover.jpg';               W=400;  H=240;  Bg='#E8E1C9'; Fg='#3D5A3F'; Title='研学成果时间轴展';   Sub='综合封面' }
  @{ Id='12'; Path='frontend/public/templates/timeline_learning/topic.jpg';               W=1920; H=1080; Bg='#7FA088'; Fg='#FFFFFF'; Title='立题';                     Sub='研学成果时间轴展 · 展区 1 / 4' }
  @{ Id='13'; Path='frontend/public/templates/timeline_learning/research.jpg';            W=1920; H=1080; Bg='#A8B99A'; Fg='#2B3B2B'; Title='调研';                     Sub='研学成果时间轴展 · 展区 2 / 4' }
  @{ Id='14'; Path='frontend/public/templates/timeline_learning/result.jpg';              W=1920; H=1080; Bg='#D9C7A7'; Fg='#4A3820'; Title='成果';                     Sub='研学成果时间轴展 · 展区 3 / 4' }
  @{ Id='15'; Path='frontend/public/templates/timeline_learning/defense.jpg';             W=1920; H=1080; Bg='#BCB89A'; Fg='#3A3528'; Title='答辩';                     Sub='研学成果时间轴展 · 展区 4 / 4' }

  # 组 4 - 地图文化（羊皮纸黄 #D4B57F）
  @{ Id='16'; Path='frontend/public/templates/map_culture_tour/cover.jpg';                W=400;  H=240;  Bg='#D4B57F'; Fg='#6B3410'; Title='地图探索式文化展';   Sub='综合封面' }
  @{ Id='17'; Path='frontend/public/templates/map_culture_tour/ancient.jpg';              W=1920; H=1080; Bg='#A64B2A'; Fg='#FCECD0'; Title='古建文化';               Sub='地图探索式文化展 · 展区 1 / 3' }
  @{ Id='18'; Path='frontend/public/templates/map_culture_tour/silk_road.jpg';            W=1920; H=1080; Bg='#C7924F'; Fg='#3B2817'; Title='丝绸之路';               Sub='地图探索式文化展 · 展区 2 / 3' }
  @{ Id='19'; Path='frontend/public/templates/map_culture_tour/dunhuang.jpg';             W=1920; H=1080; Bg='#9E6D3B'; Fg='#FFE4B5'; Title='敦煌壁画';               Sub='地图探索式文化展 · 展区 3 / 3' }

  # 组 5 - 简约白板（纯白 #FAFAFA）
  @{ Id='20'; Path='frontend/public/templates/minimal_whiteboard/cover.jpg';              W=400;  H=240;  Bg='#FAFAFA'; Fg='#9E9E9E'; Title='简约白板展';             Sub='综合封面' }
  @{ Id='21'; Path='frontend/public/templates/minimal_whiteboard/canvas.jpg';             W=1920; H=1080; Bg='#F5F3EF'; Fg='#8A847A'; Title='自由创作画布';           Sub='简约白板展 · 展区 1 / 1' }
)

function ConvertTo-Color([string]$Hex) {
  $hex = $Hex.TrimStart('#')
  $r = [Convert]::ToInt32($hex.Substring(0,2), 16)
  $g = [Convert]::ToInt32($hex.Substring(2,2), 16)
  $b = [Convert]::ToInt32($hex.Substring(4,2), 16)
  return [System.Drawing.Color]::FromArgb(255, $r, $g, $b)
}

function Render-Placeholder {
  param(
    [string]$Path, [int]$W, [int]$H,
    [string]$Bg, [string]$Fg,
    [string]$Title, [string]$Sub
  )

  $bgColor = ConvertTo-Color $Bg
  $fgColor = ConvertTo-Color $Fg

  $bmp = New-Object System.Drawing.Bitmap($W, $H)
  $gfx = [System.Drawing.Graphics]::FromImage($bmp)
  $gfx.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
  $gfx.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::AntiAliasGridFit
  $gfx.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic

  $bgBrush = New-Object System.Drawing.SolidBrush($bgColor)
  $gfx.FillRectangle($bgBrush, 0, 0, $W, $H)

  # 一条装饰分隔线（半透明前景色）
  $lineColor = [System.Drawing.Color]::FromArgb(64, $fgColor.R, $fgColor.G, $fgColor.B)
  $pen = New-Object System.Drawing.Pen($lineColor, [int][math]::Max(1, $W / 600))
  $gfx.DrawLine($pen, [int]($W * 0.12), [int]($H * 0.62), [int]($W * 0.88), [int]($H * 0.62))

  # 主标题字号按图宽缩放
  $titleSize = if ($W -ge 1000) { 96 } elseif ($W -ge 600) { 64 } else { 32 }
  $subSize   = if ($W -ge 1000) { 36 } elseif ($W -ge 600) { 24 } else { 14 }
  $tinySize  = if ($W -ge 1000) { 24 } elseif ($W -ge 600) { 16 } else { 11 }

  $fontTitle = New-Object System.Drawing.Font('Microsoft YaHei', $titleSize, [System.Drawing.FontStyle]::Bold, [System.Drawing.GraphicsUnit]::Pixel)
  $fontSub   = New-Object System.Drawing.Font('Microsoft YaHei', $subSize, [System.Drawing.FontStyle]::Regular, [System.Drawing.GraphicsUnit]::Pixel)
  $fontTiny  = New-Object System.Drawing.Font('Microsoft YaHei', $tinySize, [System.Drawing.FontStyle]::Regular, [System.Drawing.GraphicsUnit]::Pixel)

  $fgBrush = New-Object System.Drawing.SolidBrush($fgColor)
  $tinyBrush = New-Object System.Drawing.SolidBrush(([System.Drawing.Color]::FromArgb(180, $fgColor.R, $fgColor.G, $fgColor.B)))

  $sf = New-Object System.Drawing.StringFormat
  $sf.Alignment = [System.Drawing.StringAlignment]::Center
  $sf.LineAlignment = [System.Drawing.StringAlignment]::Center

  # 主标题：画面 0.35 处
  $titleRect = New-Object System.Drawing.RectangleF(0, [single]($H * 0.20), [single]$W, [single]($H * 0.30))
  $gfx.DrawString($Title, $fontTitle, $fgBrush, $titleRect, $sf)

  # 副标题：画面 0.70 处
  $subRect = New-Object System.Drawing.RectangleF(0, [single]($H * 0.66), [single]$W, [single]($H * 0.14))
  $gfx.DrawString($Sub, $fontSub, $fgBrush, $subRect, $sf)

  # 角标：底部
  $tinyRect = New-Object System.Drawing.RectangleF(0, [single]($H * 0.88), [single]$W, [single]($H * 0.08))
  $gfx.DrawString('占位图 · 待 AI 生成真实素材替换', $fontTiny, $tinyBrush, $tinyRect, $sf)

  # JPG 质量 80
  $encoderInfo = [System.Drawing.Imaging.ImageCodecInfo]::GetImageEncoders() | Where-Object { $_.MimeType -eq 'image/jpeg' }
  $encoderParams = New-Object System.Drawing.Imaging.EncoderParameters(1)
  $encoderParams.Param[0] = New-Object System.Drawing.Imaging.EncoderParameter([System.Drawing.Imaging.Encoder]::Quality, [long]80)

  $bmp.Save($Path, $encoderInfo, $encoderParams)

  $gfx.Dispose()
  $bmp.Dispose()
  $pen.Dispose()
  $bgBrush.Dispose()
  $fgBrush.Dispose()
  $tinyBrush.Dispose()
  $fontTitle.Dispose()
  $fontSub.Dispose()
  $fontTiny.Dispose()
}

Write-Host ""
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "  模板占位图生成器" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""

$generated = 0
$skipped = 0

foreach ($item in $items) {
  $abs = Join-Path $repoRoot $item.Path

  if ((Test-Path $abs) -and -not $Force) {
    Write-Host ("  [{0}] 跳过（已存在）{1}" -f $item.Id, $item.Path) -ForegroundColor DarkGray
    $skipped++
    continue
  }

  $dir = Split-Path $abs -Parent
  if (-not (Test-Path $dir)) {
    New-Item -ItemType Directory -Path $dir -Force | Out-Null
  }

  Render-Placeholder -Path $abs -W $item.W -H $item.H -Bg $item.Bg -Fg $item.Fg -Title $item.Title -Sub $item.Sub

  Write-Host ("  [{0}] 生成 {1,4}x{2,-4} -> {3}" -f $item.Id, $item.W, $item.H, $item.Path) -ForegroundColor Green
  $generated++
}

Write-Host ""
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ("  汇总：共 21 张 | 新生成 {0} 张 | 跳过 {1} 张" -f $generated, $skipped) -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""
Write-Host "💡 后续用真实 AI 图片替换时：把图放入 template-assets-inbox/ 后执行：" -ForegroundColor Yellow
Write-Host "     pwsh tools/place-template-assets.ps1 -Force" -ForegroundColor Yellow
Write-Host ""

exit 0
