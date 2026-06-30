# Incrémente la version d'un plugin Paper (source : <plugin>/gradle.properties)
# Usage :
#   .\scripts\bump-version.ps1 -Plugin admin -Part patch
#   .\scripts\bump-version.ps1 -Plugin slime-capture -Part minor
#   .\scripts\bump-version.ps1 -Plugin mon-plugin -Part major -PluginDir "..\mon-plugin"

param(
    [Parameter(Mandatory = $true)]
    [string]$Plugin,

    [Parameter(Mandatory = $true)]
    [ValidateSet("patch", "minor", "major")]
    [string]$Part,

    [string]$PluginDir = ""
)

$ErrorActionPreference = "Stop"
$root = Split-Path $PSScriptRoot -Parent

$known = @{
    "admin"         = "plugin"
    "adminplugin"   = "plugin"
    "slime-capture" = "slime-capture"
    "slimecapture"  = "slime-capture"
}

$key = $Plugin.ToLowerInvariant()
if ($PluginDir -eq "") {
    if (-not $known.ContainsKey($key)) {
        Write-Error "Plugin inconnu : $Plugin. Utilisez -PluginDir ou un alias : admin, slime-capture"
    }
    $PluginDir = Join-Path $root $known[$key]
} else {
    $PluginDir = Resolve-Path $PluginDir
}

$propsFile = Join-Path $PluginDir "gradle.properties"
if (-not (Test-Path $propsFile)) {
    Write-Error "Fichier introuvable : $propsFile`nCréez gradle.properties avec : version=1.0.0"
}

$lines = Get-Content $propsFile
$versionLine = $lines | Where-Object { $_ -match '^\s*version\s*=' } | Select-Object -First 1
if (-not $versionLine) {
    Write-Error "Aucune ligne version= dans $propsFile"
}

$current = ($versionLine -split '=', 2)[1].Trim()
if ($current -notmatch '^(\d+)\.(\d+)\.(\d+)(.*)$') {
    Write-Error "Version non semver : $current (attendu : X.Y.Z)"
}

$major = [int]$Matches[1]
$minor = [int]$Matches[2]
$patch = [int]$Matches[3]
$suffix = $Matches[4]

switch ($Part) {
    "major" { $major++; $minor = 0; $patch = 0 }
    "minor" { $minor++; $patch = 0 }
    "patch" { $patch++ }
}

$newVersion = "$major.$minor.$patch$suffix"
$newLines = $lines | ForEach-Object {
    if ($_ -match '^\s*version\s*=') { "version=$newVersion" } else { $_ }
}
Set-Content -Path $propsFile -Value $newLines -Encoding UTF8

Write-Host "[$PluginDir] $current -> $newVersion ($Part)"
Write-Host "Recompilez : cd `"$PluginDir`" ; .\gradlew.bat build"
