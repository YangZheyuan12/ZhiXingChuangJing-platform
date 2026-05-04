# 启动 MinIO 作为后端 app.storage.mode=minio 的对象存储
#
# 用法：
#   1. 确保本机装了 Docker Desktop 并已启动
#   2. PowerShell 执行：.\scripts\start-minio.ps1
#   3. 浏览器打开 http://127.0.0.1:9001 验证（账号 minioadmin / minioadmin）
#   4. 把后端环境变量 STORAGE_MODE 设为 minio（或直接改 application.yml 的 app.storage.mode）
#
# 参数与 backend/src/main/resources/application.yml 中的默认值保持一致。

param(
    [string]$ContainerName = "zxcyj-minio",
    [int]$ApiPort = 9000,
    [int]$ConsolePort = 9001,
    [string]$RootUser = "minioadmin",
    [string]$RootPassword = "minioadmin",
    [string]$DataVolume = "zxcyj-minio-data"
)

$ErrorActionPreference = "Stop"

function Require-Docker {
    try {
        docker version --format "{{.Server.Version}}" | Out-Null
    } catch {
        Write-Host "未检测到 Docker，请先安装 Docker Desktop 并启动。" -ForegroundColor Red
        Write-Host "下载地址：https://www.docker.com/products/docker-desktop/" -ForegroundColor Yellow
        exit 1
    }
}

function Stop-ExistingContainer {
    $existing = docker ps -a --filter "name=^$ContainerName$" --format "{{.Names}}" 2>$null
    if ($existing -eq $ContainerName) {
        Write-Host "发现已存在的容器 $ContainerName，正在停止并移除..." -ForegroundColor Yellow
        docker rm -f $ContainerName | Out-Null
    }
}

function Start-MinIO {
    Write-Host "拉取并启动 MinIO 容器 $ContainerName ..." -ForegroundColor Cyan
    docker run -d `
        --name $ContainerName `
        --restart unless-stopped `
        -p "${ApiPort}:9000" `
        -p "${ConsolePort}:9001" `
        -e "MINIO_ROOT_USER=$RootUser" `
        -e "MINIO_ROOT_PASSWORD=$RootPassword" `
        -v "${DataVolume}:/data" `
        minio/minio server /data --console-address ":9001" | Out-Null
}

function Show-Summary {
    Write-Host ""
    Write-Host "✅ MinIO 已启动" -ForegroundColor Green
    Write-Host "   API    : http://127.0.0.1:$ApiPort" -ForegroundColor Green
    Write-Host "   Console: http://127.0.0.1:$ConsolePort (账号 $RootUser / $RootPassword)" -ForegroundColor Green
    Write-Host ""
    Write-Host "👉 切换后端为 MinIO 模式：" -ForegroundColor Cyan
    Write-Host "   方式 A：application.yml 改 app.storage.mode: minio" -ForegroundColor Gray
    Write-Host "   方式 B：设置环境变量 `$env:STORAGE_MODE='minio' 后重启后端" -ForegroundColor Gray
    Write-Host ""
    Write-Host "📌 默认仍使用本地文件存储（app.storage.mode=local），素材落在 backend/storage/" -ForegroundColor Cyan
}

Require-Docker
Stop-ExistingContainer
Start-MinIO
Show-Summary
