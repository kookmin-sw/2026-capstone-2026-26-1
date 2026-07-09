# 모노레포 가이드라인 (루트)

## 구성

이 저장소는 "길벗" 서비스의 `android/`(Kotlin/Compose 앱)와 `backend/`(Spring Boot API)를
함께 담은 모노레포다. 각 모듈은 자체 `.claude/CLAUDE.md`를 갖고 있어(`android/.claude/CLAUDE.md`,
`backend/.claude/CLAUDE.md`) 해당 디렉터리 아래에서 작업할 때 자동으로 함께 적용된다.

## Claude Code 세션 실행 관행

Claude Code 세션은 **항상 저장소 루트**(`2026-capstone-26/`)에서 실행한다. Claude Code가
`backend/.claude/`, `android/.claude/`를 하위 디렉터리 스코프로 자동 인식하므로, 모듈별로
세션을 나눌 필요가 없다 — `backend/` 파일을 다룰 때는 `backend/.claude/CLAUDE.md`와
`backend/.claude/rules/`, `backend/.claude/skills/`가, `android/` 파일을 다룰 때는
`android/.claude/CLAUDE.md`가 자동으로 함께 적용된다. 두 모듈을 넘나드는 작업(예: API 스펙
변경이 앱에 미치는 영향 확인)도 한 세션에서 처리할 수 있다.

## 커밋 정책 (필수)

**사용자가 명시적으로 커밋하라고 지시하기 전까지는 절대 `git commit`(및 `git push`)을 실행하지
않는다.** 코드/문서/설정 변경을 완료했더라도, 그 자체가 커밋 승인을 의미하지 않는다. Plan
Mode에서 계획에 커밋 단계가 포함되어 사용자가 그 계획을 승인한 경우는 예외적으로 해당
커밋들에 한해 명시적 지시로 간주하되, 그 외의 모든 상황에서는 변경 후 반드시 커밋 여부를
먼저 물어보거나 사용자의 명령을 기다린다. `android/`, `backend/` 모두 동일하게 적용된다.

## 공용 Git & GitHub 규칙

`android/`, `backend/` 모두에 적용되는 공용 규칙은 이 디렉터리(`.claude/rules/`)에 둔다.
모듈 전용 규칙(예: backend의 RDS 접속 절차)은 각 모듈의 `.claude/rules/`에 남아 있다.

- 커밋 메시지 형식·브랜치 명명: [git-commit.md](rules/git-commit.md)
- 이슈 작성 구조: [github-issue.md](rules/github-issue.md)
- PR 작성 구조: [github-pr.md](rules/github-pr.md)

### 모듈별 라벨

이슈/PR을 만들 때 작업 대상 모듈에 따라 라벨을 다르게 붙인다.

| 작업 대상      | 라벨      |
|------------|---------|
| `backend/` | `💻BE`  |
| `android/` | `📱AND` |

## GitHub 템플릿

`.github/ISSUE_TEMPLATE/이슈-템플릿.md`, `.github/pull_request_template.md`가
`android/`, `backend/` 공용 템플릿이다. GitHub은 저장소 루트의 `.github/`만 인식하므로,
모듈별로 별도 템플릿을 두지 않는다.

## 포트폴리오 관점 제안

- 이 저장소는 개발자 취업을 위한 포트폴리오로 사용됩니다.
- 작업 중 단순 구현을 넘어 포트폴리오에서 어필 가능한 개선점(테스트/CI 강화, 코드 품질 자동화, 아키텍처 개선, 성능 개선 사항, 트러플 슈팅, 기술적 의사결정 포인트 등)
  을 발견하면 코드를 임의로 고치지 말고 최종 응답에서 제안합니다.