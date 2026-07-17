# .env.local의 값을 .claude/settings.local.json의 env 필드로 병합한다.
# 클로드 코드가 실행하는 Bash 명령에 환경변수가 자동 주입되도록 하기 위함.
# 병합 규칙: .env.local이 single source of truth로 우선한다(같은 키는 .env.local 값이 이김).
#   기존 settings.env에만 있고 .env.local에는 없는 키는 그대로 보존한다.
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
$mergedEnv = [ordered]@{}
if (Test-Path $settingsFile) {
    $existing = Get-Content -LiteralPath $settingsFile -Raw | ConvertFrom-Json
    # env 이외의 최상위 설정(permissions 등)은 그대로 보존한다.
    foreach ($prop in $existing.PSObject.Properties) {
        if ($prop.Name -ne "env") {
            $settings[$prop.Name] = $prop.Value
        }
    }
    # 기존 settings.env를 base로 깔아, .env.local에 없는 수동 추가 키를 보존한다.
    if ($existing.PSObject.Properties.Name -contains "env") {
        foreach ($prop in $existing.env.PSObject.Properties) {
            $mergedEnv[$prop.Name] = $prop.Value
        }
    }
}
# .env.local 값을 마지막에 덮어써, .env.local을 single source of truth로 우선 적용한다.
# (기존에는 순서가 반대라 settings의 옛 값이 이겨 .env.local 변경이 반영되지 않던 버그가 있었다.)
foreach ($key in $envValues.Keys) {
    $mergedEnv[$key] = $envValues[$key]
}
$settings["env"] = $mergedEnv

$json = $settings | ConvertTo-Json -Depth 10
[System.IO.File]::WriteAllText($settingsFile, $json, [System.Text.UTF8Encoding]::new($false))

Write-Output ("병합된 env 키: " + ($mergedEnv.Keys -join ", "))
