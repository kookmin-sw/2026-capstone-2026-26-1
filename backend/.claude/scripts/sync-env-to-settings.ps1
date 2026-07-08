# .env.local의 값을 .claude/settings.local.json의 env 필드로 병합한다.
# 클로드 코드가 실행하는 Bash 명령에 환경변수가 자동 주입되도록 하기 위함.
# 값은 절대 표준출력에 출력하지 않고, 병합된 키 이름만 출력한다.

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$backendRoot = Resolve-Path (Join-Path $scriptDir "..\..")
$monorepoRoot = Resolve-Path (Join-Path $scriptDir "..\..\..")
$envFile = Join-Path $backendRoot ".env.local"
$settingsFile = Join-Path $monorepoRoot ".claude\settings.local.json"

if (-not (Test-Path $envFile)) {
    throw "$envFile 을(를) 찾을 수 없습니다."
}

$envValues = [ordered]@{}
foreach ($line in Get-Content -LiteralPath $envFile) {
    $trimmed = $line.Trim()
    if ($trimmed -eq "" -or $trimmed.StartsWith("#")) {
        continue
    }
    $idx = $trimmed.IndexOf("=")
    if ($idx -lt 1) {
        continue
    }
    $key = $trimmed.Substring(0, $idx).Trim()
    $value = $trimmed.Substring($idx + 1).Trim()
    $envValues[$key] = $value
}

$settings = [ordered]@{}
if (Test-Path $settingsFile) {
    $existing = Get-Content -LiteralPath $settingsFile -Raw | ConvertFrom-Json
    foreach ($prop in $existing.PSObject.Properties) {
        if ($prop.Name -ne "env") {
            $settings[$prop.Name] = $prop.Value
        }
    }
    if ($existing.PSObject.Properties.Name -contains "env") {
        foreach ($prop in $existing.env.PSObject.Properties) {
            $envValues[$prop.Name] = $prop.Value
        }
    }
}
$settings["env"] = $envValues

$json = $settings | ConvertTo-Json -Depth 10
[System.IO.File]::WriteAllText($settingsFile, $json, [System.Text.UTF8Encoding]::new($false))

Write-Output ("병합된 env 키: " + ($envValues.Keys -join ", "))
