# Démarre le panel Admin (sans Docker)
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
$Jar = "$Root\api\build\libs\AdminApi-1.0.0.jar"
$EnvFile = "$Root\.env"

if (-not (Test-Path $Jar)) {
    Write-Host "JAR introuvable. Lancez d'abord : .\scripts\deploy-local.ps1" -ForegroundColor Red
    exit 1
}

if (Test-Path $EnvFile) {
    Write-Host "Chargement de .env..." -ForegroundColor Gray
    Get-Content $EnvFile | ForEach-Object {
        if ($_ -match '^\s*([^#][^=]+)=(.*)$') {
            $name = $matches[1].Trim()
            $value = $matches[2].Trim()
            Set-Item -Path "env:$name" -Value $value
        }
    }
} else {
    Write-Host "Fichier .env absent — valeurs par défaut." -ForegroundColor Yellow
    Write-Host "Copiez .env.example vers .env pour configurer." -ForegroundColor Yellow
}

$port = if ($env:SERVER_PORT) { $env:SERVER_PORT } else { "8080" }
Write-Host ""
Write-Host "Panel Admin : http://localhost:$port" -ForegroundColor Green
Write-Host "Login par défaut : admin / admin123" -ForegroundColor Gray
Write-Host "Ctrl+C pour arrêter" -ForegroundColor Gray
Write-Host ""

Push-Location "$Root\api"
java -jar "build\libs\AdminApi-1.0.0.jar" --spring.profiles.active=prod
