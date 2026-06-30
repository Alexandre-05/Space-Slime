# Déploiement local sans Docker — panel sur http://localhost:8080
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot

Write-Host "==> Build frontend React..." -ForegroundColor Cyan
Push-Location "$Root\web"
npm install
npm run build
Pop-Location

Write-Host "==> Copie du frontend dans l'API..." -ForegroundColor Cyan
$staticDir = "$Root\api\src\main\resources\static"
if (Test-Path $staticDir) { Remove-Item -Recurse -Force $staticDir }
Copy-Item -Recurse "$Root\web\dist" $staticDir

Write-Host "==> Build API Spring Boot..." -ForegroundColor Cyan
Push-Location "$Root\api"
.\gradlew.bat bootJar --no-daemon
Pop-Location

Write-Host ""
Write-Host "Terminé ! Lancez le panel avec :" -ForegroundColor Green
Write-Host "  cd api" -ForegroundColor Yellow
Write-Host "  java -jar build\libs\AdminApi-1.0.0.jar --spring.profiles.active=prod" -ForegroundColor Yellow
Write-Host ""
Write-Host "Assurez-vous que MariaDB est accessible (local ou Minestrator)." -ForegroundColor Gray
Write-Host "Puis lancez : .\scripts\start-panel.ps1" -ForegroundColor Green
