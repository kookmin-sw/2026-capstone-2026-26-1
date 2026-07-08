# 저장소 가이드라인

## 목적

이 파일은 `android/`에서 작업하는 기여자와 코딩 에이전트를 위한 로컬 작업 규칙을 정의합니다.

## 프로젝트 개요

"길벗"은 위치 데이터를 수집해 사용자에게는 기억 보조를, 보호자에게는 안심 확인을 제공하는 서비스입니다.
이동 경로 기록/조회, 15분 이상 체류한 장소 자동 저장, 외출·귀가 시각 기록, 개인 메모, 보호자의 실시간 위치 확인과 요약 통계 조회가 핵심 기능입니다.
이 앱(API 24+)은 `backend/`의 Spring Boot 백엔드와 통신하며 Kakao/Google 지도 API를 연동합니다.

## 기술 스택

- Kotlin + Jetpack Compose(`kotlin.compose` 플러그인), KSP
- minSdk 24 / compileSdk·targetSdk 36, JVM 17
- Kakao OAuth(`kakao.nativeAppKey`) 로그인, Google Maps API 연동
- 시크릿(`kakao.nativeAppKey`, `app.baseUrl`, `google.mapsApiKey`)은 `local.properties`로 관리(커밋 금지)

## 코드/UI 규칙

상세 네이밍(Route/Screen/ViewModel/UiState/Effect), 문자열 규칙(하드코딩 금지, `stringResource`,
UI 카피 한국어 우선), UI State 패턴(`AsyncUiState<T>`), 기능 폴더 구조는
[DEVELOPMENT_GUIDELINES.md](../DEVELOPMENT_GUIDELINES.md)를 참고합니다.

라우트/장소/카메라/권한/daynote 저장 정책 등 앱 사이드 정책의 단일 소스는
[docs/global-policy.md](../docs/global-policy.md)입니다.

## Git & GitHub Workflow

`backend/`와 공용으로 쓰는 규칙이라 저장소 루트 `.claude/rules/`에 있습니다.

- 커밋 메시지 형식과 브랜치 명명 규칙: [../../.claude/rules/git-commit.md](../../.claude/rules/git-commit.md) 참고.
- 이슈 작성 구조: [../../.claude/rules/github-issue.md](../../.claude/rules/github-issue.md) 참고.
- PR 작성 구조: [../../.claude/rules/github-pr.md](../../.claude/rules/github-pr.md) 참고. android 작업 이슈/PR 라벨은 `📱AND`.

## 작업 스타일

- 편집하기 전에 관련 코드와 컨텍스트를 조사합니다.
- 새로운 추상화를 도입하기 전에 기존 패턴(Route/Screen/ViewModel/UiState)을 재사용합니다.
- 커밋 정책(사용자가 명시적으로 지시하기 전까지 절대 커밋/푸시하지 않음)은
  [../../.claude/CLAUDE.md](../../.claude/CLAUDE.md)의 "커밋 정책" 참고.
