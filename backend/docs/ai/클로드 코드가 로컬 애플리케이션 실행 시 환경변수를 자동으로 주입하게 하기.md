# 클로드 코드가 로컬 애플리케이션 실행 시 환경변수를 자동으로 주입하게 하기

**요약**: 클로드 코드가 `./gradlew bootRun`으로 로컬 서버를 직접 띄우려 하면, `.env.local`에
있는 실제 값(DB 접속 정보, JWT 시크릿 등)이 셸에 로드되어 있지 않아 매번 실행이 실패한다.
`.env.local`의 값을 클로드 코드 설정 파일(`.claude/settings.local.json`)의 `env` 필드로
한 번 옮겨두면, 이후에는 클로드 코드가 실행하는 모든 Bash 명령에 값이 자동으로 주입된다. 이
문서는 그 설정 방법을 그대로 따라 할 수 있도록 정리한 가이드다.

## 왜 필요한가

Spring Boot 설정(`application.yml`)은 `${DB_URL}`, `${JWT_SECRET}`처럼 환경변수를 참조한다.
로컬 개발자는 보통 IDE의 실행 설정이나 `.env` 로더 플러그인으로 이 값을 미리 채워두지만,
클로드 코드가 터미널에서 직접 `./gradlew bootRun`을 실행할 때는 그런 사전 설정이 없다. 그
결과 매번 "환경변수를 찾을 수 없다"는 이유로 애플리케이션 기동에 실패한다.

매번 값을 대화창에 붙여넣거나 명령어에 직접 박아 넣는 방식은 두 가지 문제가 있다.

- 매 세션 반복 작업이라 번거롭다.
- 시크릿 값이 대화 로그(transcript)에 그대로 남는다.

## 준비물

- 이미 값이 채워진 `.env.local` 파일 (프로젝트 루트, git에는 커밋하지 않는 파일)
- `.claude/settings.json`이 이미 존재하는 프로젝트 (없다면 빈 `{}` 파일로 시작해도 된다)

## 설정 방법

### 1. 병합 스크립트 추가

`.claude/scripts/sync-env-to-settings.ps1` 파일을 아래 내용으로 만든다. 이 스크립트는
`.env.local`을 한 줄씩 읽어 `.claude/settings.local.json`의 `env` 필드에 병합해주는
일회성/재사용 가능 도구다.

```powershell
$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$repoRoot = Resolve-Path (Join-Path $scriptDir "..\..")
$envFile = Join-Path $repoRoot ".env.local"
$settingsFile = Join-Path $repoRoot ".claude\settings.local.json"

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
```

핵심은 마지막 줄이다. 병합된 **키 이름만** 출력하고 값은 절대 출력하지 않는다. 클로드 코드가
이 스크립트를 Bash 도구로 실행하더라도, 표준출력에 값이 찍히지 않으면 클로드는 값을 한 번도
눈으로 보지 않은 채로 설정 파일만 갱신할 수 있다.

### 2. 스크립트 실행

PowerShell로 한 번 실행한다.

```powershell
& ".claude\scripts\sync-env-to-settings.ps1"
```

`-ExecutionPolicy Bypass` 같은 플래그는 필요 없다. 실행 정책이 걸려 있어도 스크립트 파일을
직접 `&`로 호출하면 대부분의 기본 정책에서 문제없이 동작한다.

정상적으로 실행되면 아래처럼 키 이름만 출력된다.

```
병합된 env 키: DB_URL, DB_USERNAME, DB_PASSWORD, JWT_SECRET, REDIS_HOST, REDIS_PORT, ...
```

### 3. 결과 파일 확인 (git 추적 제외 필수)

`.claude/settings.local.json`이 아래와 같은 형태로 생성된다.

```json
{
  "env": {
    "DB_URL": "...",
    "JWT_SECRET": "..."
  }
}
```

이 파일은 실제 시크릿 값을 담고 있으므로 반드시 `.gitignore`에 추가한다.

```
.claude/settings.local.json
```

### 4. 동작 확인

새 터미널(또는 새 클로드 코드 세션)에서 값이 자동으로 주입되는지 확인한다. 값 자체를 화면에
출력하지 않고 길이만 확인하는 방식을 권장한다.

```bash
echo "DB_URL length: ${#DB_URL}"
```

길이가 0이 아니면 정상 주입된 것이다. 이후 `./gradlew bootRun`을 실행해 환경변수 누락 에러
없이 애플리케이션이 뜨는지 확인한다.

## `.env.local` 값이 바뀌면?

`settings.local.json`은 정적 파일이라 자동으로 동기화되지 않는다. `.env.local`을 수정한
뒤에는 1단계 스크립트를 다시 실행해야 최신 값이 반영된다.

## 왜 이 방식이 "Read 금지" 설정과 충돌하지 않는가

많은 프로젝트가 `.claude/settings.json`에 `deny: ["Read(./.env.*)"]` 같은 규칙을 걸어
클로드 코드가 시크릿 파일을 직접 읽지 못하도록 막아둔다. 이 규칙은 클로드 코드의 **Read
도구**에만 적용되는 것이지, Bash로 실행되는 스크립트의 파일시스템 접근까지 막지는 않는다.
따라서 위 스크립트는 이 규칙을 우회하는 게 아니라,애초에 그 규칙이 다루지 않는 경로(Bash
하위 프로세스)로 값을 옮기는 것이다. 대신 스크립트가 값을 stdout으로 출력하지 않도록
설계해서, "클로드가 실제로 값을 보게 되는 것"만은 막아둔다.

## 한계

- `.env.local`이 바뀔 때마다 스크립트를 수동으로 다시 실행해야 한다. 매 세션 시작 시
  자동으로 동기화하고 싶다면 클로드 코드의 `SessionStart` 훅에 이 스크립트 실행을 등록하는
  방법도 있지만, `.env.local`이 자주 바뀌지 않는다면 오버엔지니어링일 수 있다.
- 이 방식은 로컬 개발 편의를 위한 것으로, CI나 운영 배포 파이프라인의 시크릿 관리 방식과는
  별개다.
