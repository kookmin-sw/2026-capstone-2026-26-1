# 저장소 가이드라인

## 목적

이 파일은 `backend/`에서 작업하는 기여자와 코딩 에이전트를 위한 로컬 작업 규칙을 정의합니다.

## 프로젝트 개요

"길벗"은 위치 데이터를 수집해 사용자에게는 기억 보조를, 보호자에게는 안심 확인을 제공하는 서비스입니다.
이동 경로 기록/조회, 15분 이상 체류한 장소 자동 저장, 외출·귀가 시각 기록, 개인 메모, 보호자의 실시간 위치 확인과 요약 통계 조회가 핵심 기능입니다.
Android 앱(API 24+)과 이 Spring Boot 백엔드로 구성되며 Kakao/Naver/Google 지도 API를 연동합니다.

## 기술 스택

- Spring Boot 3.2.5 / Java 21 / Gradle
- Spring Security + JWT(jjwt 0.12.5) + Kakao OAuth 인증
- Spring Data JPA + MySQL(운영), H2(테스트)
- Redis: RefreshToken 저장소(`auth/service/RefreshTokenService`)
- springdoc-openapi(Swagger UI, 기본 포트 8080), Docker 컨테이너 배포

## 빌드 & 테스트 명령

- 빌드: `./gradlew build`
- 전체 테스트: `./gradlew test`
- 단일 테스트: `./gradlew test --tests "backend.capstone.<패키지>.<클래스명>"`
- 로컬 인프라(MySQL/Redis)는 `docker compose -f docker-compose.local.yml up -d`로 컨테이너를 띄운 뒤
  네이티브 `bootRun`으로 앱을 실행하는 하이브리드 방식이다(컨테이너는 MySQL 3307, Redis 6380 포트로
  네이티브 서비스와 공존). DB를 초기화하려면 `docker compose -f docker-compose.local.yml down -v`.
  `.env.local`의 `DB_URL`/`REDIS_HOST`/`REDIS_PORT`가 이 포트를 가리켜야 한다(예시는 `.env.example`).
- 로컬 `bootRun`이 환경변수 누락으로 실패하면 `.claude/scripts/sync-env-to-settings.ps1`을 실행해 `.env.local` 값을 `.claude/settings.local.json`의 `env` 필드로 동기화한다(`.env.local`이 바뀐 뒤에도 동일하게 재실행).
- Checkstyle/Spotless 등 빌드 강제 플러그인은 없음— 코드 스타일은 `config/codestyle/GoogleStyle_java`를 IntelliJ에 수동
  import해서 적용합니다.

## RDS 접속 (데이터 분석용)

운영 RDS는 퍼블릭 액세스가 차단되어 있습니다. Claude Code가 데이터 조회가 필요하면 SSH 키/DB
고정 비밀번호가 아니라 **SSM Session Manager 포트 포워딩 + RDS IAM 데이터베이스 인증**으로
접속합니다. 상세 절차와 알려진 함정은 `.claude/rules/rds-access.md` 참고.

알려진 함정: SSM 문서명은 `AWS-StartPortForwardingSessionToRemoteHost`(비슷한 이름과 혼동
주의) / 로컬에 기존 MySQL 서비스가 3306을 점유할 수 있으니 포트 충돌 여부 확인 후 필요시
다른 로컬 포트 사용 / mysql 클라이언트로 IAM 토큰 인증 시 `--enable-cleartext-plugin` 필수.

## Git & GitHub Workflow

커밋 타입은 다음을 사용합니다: feat, fix, docs, style, design, test, refactor, build,
ci, perf, chore, rename, remove.

`android/`와 공용으로 쓰는 규칙이라 저장소 루트 `.claude/rules/`에 있습니다.

- 커밋 메시지 형식과 브랜치 명명 규칙: `../../.claude/rules/git-commit.md` 참고.
- 이슈 작성 구조: `../../.claude/rules/github-issue.md` 참고.
- PR 작성 구조: `../../.claude/rules/github-pr.md` 참고. backend 작업 이슈/PR 라벨은 `💻BE`.

## 아키텍처

`backend.capstone` 하위 도메인 기반 패키징입니다.

- `domain/{user, care, mobility, bookmarkplace, region}`: 도메인별
  controller/service/repository/entity/dto
- `auth/`: JWT 발급·검증(jwt), 인증 필터(filter), Kakao OAuth 처리(service), 인증 예외(entrypoint/exception)
- `integration/kakao`: 카카오 로컬 API 등 외부 연동
- `global/`: 공통 설정(config), 공통 예외 처리(exception), 공통 엔티티(entity), 유틸(util)

계층 흐름은 Controller → Service → Repository(JPA)이며, Entity↔DTO 변환은 Mapper가 담당합니다.
인증은 Stateless 세션 + `JwtAuthenticationFilter`로 처리하고 AccessToken(1일)/RefreshToken(30일, Redis 저장)을
사용하며, 인증 실패는 `ExAuthenticationEntryPoint`가 처리합니다.
예외는 `global/exception`의 `GlobalExceptionHandler`가 ErrorCode/ErrorResponse 레코드 패턴으로 일괄 처리하므로, 새 예외를
추가할 때 이 패턴을 재사용합니다.

## Java 스타일

- 기본 기준선은 Google Java Style이며, `config/codestyle/GoogleStyle_java` 기준 최대 줄 길이 100자, 들여쓰기 2칸(연속 들여쓰기
  4칸)입니다.
- 합리적으로 가능한 경우 메서드 매개변수를 한 줄에 유지합니다.
- 각 매개변수를 별도의 줄에 배치하는 방식으로 메서드 선언을 포맷하지 않습니다.
- 짧은 생성자 호출이나 예외 throw는 한 줄로 유지합니다.
- 정말 길 때만 줄바꿈하며, 이때 이항 연산자는 다음 줄 시작에 둡니다.
- 줄바꿈이 필요한 경우, 주변 파일의 스타일을 우선적으로 따릅니다.
- 제어문(if/for/while 등)은 항상 중괄호를 사용합니다.

금지:

```java
public void example(
    TypeA a,
    TypeB b,
    TypeC c
) {
}
```

권장:

```java
public void example(TypeA a, TypeB b, TypeC c) {
}
```

## 작업 스타일

- 편집하기 전에 관련 코드와 컨텍스트를 조사합니다.
- 새로운 추상화를 도입하기 전에 기존 도메인 패턴(Controller/Service/Repository/Mapper)을 재사용합니다.
- 데이터 조사를 위해 인터넷 검색 및 기타 리서치를 적극 활용합니다.
- 업무 태도에서 낙관주의보다 객관성을 우선시합니다.
- 익숙하지 않은 개념을 발견하면 정의·기능·목적·예시·예상 효과를 조사해 작업에 종합적으로 활용합니다.
- 예외 메시지, 로그, 테스트 메서드 이름은 한국어로 작성합니다.

## 금지 변경 사항

- 명시적인 요청 없는 대규모 구조적 변경
- 기존 어노테이션을 절대 삭제하지 않습니다.
- 명령이 없으면 테스트 코드를 건드리지 않습니다.
- 커밋 정책(사용자가 명시적으로 지시하기 전까지 절대 커밋/푸시하지 않음)은
  `../../.claude/CLAUDE.md`의 "커밋 정책" 참고.

## 포트폴리오 관점 제안

- 이 저장소는 백엔드 개발자 포트폴리오로 사용됩니다.
- 작업 중 단순 구현을 넘어 포트폴리오에서 어필 가능한 개선점(테스트/CI 강화, 코드 품질 자동화, 아키텍처 개선, 성능 개선 사항, 트러플 슈팅, 기술적 의사결정 포인트 등)
  을 발견하면 코드를 임의로 고치지 말고 최종 응답에서 제안합니다.

## 응답 기대사항

최종 응답에는 다음을 포함해야 합니다:

- 주요 작업 내용 요약
- 주요 구현 세부사항
- 남은 위험 요소 및 개선 사항
- 이후에 해야 할 일
