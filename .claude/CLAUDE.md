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

## 공용 Git & GitHub 규칙

`android/`, `backend/` 모두에 적용되는 공용 규칙은 이 디렉터리(`.claude/rules/`)에 둔다.
모듈 전용 규칙(예: backend의 RDS 접속 절차)은 각 모듈의 `.claude/rules/`에 남아 있다.

- 커밋 메시지 형식·브랜치 명명: [git-commit.md](rules/git-commit.md)
- 이슈 작성 구조: [github-issue.md](rules/github-issue.md)
- PR 작성 구조: [github-pr.md](rules/github-pr.md)

### 모듈별 라벨

이슈/PR을 만들 때 작업 대상 모듈에 따라 라벨을 다르게 붙인다.

| 작업 대상    | 라벨      |
|----------|---------|
| `backend/` | `💻BE`  |
| `android/` | `📱AND` |

## GitHub 템플릿

`.github/ISSUE_TEMPLATE/이슈-템플릿.md`, `.github/pull_request_template.md`가
`android/`, `backend/` 공용 템플릿이다. GitHub은 저장소 루트의 `.github/`만 인식하므로,
모듈별로 별도 템플릿을 두지 않는다.
