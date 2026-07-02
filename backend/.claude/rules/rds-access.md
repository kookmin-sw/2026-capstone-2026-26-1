# RDS 접속 런북 (SSM + IAM 인증)

운영 RDS(MySQL)는 퍼블릭 액세스가 차단되어 있다. 장애 대응/원인 파악을 위해 접속할 때는
SSH 키나 DB 고정 비밀번호를 쓰지 않고, **AWS SSM Session Manager 포트 포워딩 + RDS IAM
데이터베이스 인증**을 사용한다. 설계 배경과 대안 비교는 `docs/tech/claude-code-access-rds.md`
참고.

이 문서는 git에 커밋되어 공개될 수 있으므로, 실제 계정 ID/인스턴스 ID/엔드포인트 등 구체적인
값은 적지 않는다. 아래 명령어의 `<EC2-인스턴스ID>`, `<RDS엔드포인트>`, `<리전>`, `<스키마명>`,
`<로컬포트>` 자리에 들어갈 실제 값은 **`rds-access.local.md`(gitignore 처리됨, 이 문서와 같은
디렉터리)** 에 있다. 그 파일이 없다면 사용자에게 물어보거나 이 절차를 처음 만들 때와 같은
방식(AWS 콘솔 확인)으로 알아낸 뒤 새로 만든다.

## 1. 사전 설정 체크리스트 (최초 1회, AWS 콘솔/관리자 권한 필요)

- **배스천 EC2**: `AmazonSSMManagedInstanceCore` 정책이 포함된 IAM 인스턴스 프로파일이
  연결되어 있는지 확인. SSM Agent는 최신 AL2/AL2023/Ubuntu AMI라면 기본 설치돼 있음.
- **로컬 IAM 사용자 권한**: 아래 액션이 필요하다. 리소스 범위는 특정 EC2 인스턴스 ARN·SSM
  문서 ARN·DB 유저 ARN으로 좁히는 것을 권장한다(최소 권한 원칙).
  - `ssm:StartSession`, `ssm:TerminateSession`, `ssm:ResumeSession`
  - `ssm:GetDocument`, `ssm:DescribeDocument` (없으면 `StartSession`이
    `InvalidDocument: Document ... does not exist`라는 오해하기 쉬운 에러로 실패한다 — 실제
    원인은 권한 부족인 경우가 많다)
  - `rds-db:connect`
- **RDS**: IAM 데이터베이스 인증 활성화
  (`aws rds modify-db-instance --enable-iam-database-authentication --apply-immediately`,
  엔진에 따라 재부팅 필요할 수 있음. 운영 중인 DB이므로 트래픽이 적은 시간대에 적용 권장).
- **RDS 읽기 전용 DB 계정**: 마스터 계정으로 1회 접속해 실행.
  ```sql
  CREATE USER 'readonly_iam'@'%' IDENTIFIED WITH AWSAuthenticationPlugin AS 'RDS';
  GRANT SELECT ON <스키마명>.* TO 'readonly_iam'@'%';
  FLUSH PRIVILEGES;
  ```
- **로컬 도구**: `session-manager-plugin` 설치(winget 또는 AWS 공식 배포본), Docker Desktop
  실행 중 (mysql CLI가 로컬에 없으면 `mysql:8` 이미지를 사용).

## 2. 접속 절차

### 2.1 로컬 포트 사용 가능 여부 확인 (건너뛰지 말 것)

```bash
netstat -ano | grep ":3306 "
```

무언가 이미 3306을 점유하고 있다면(로컬 개발용 MySQL 서비스 등), 해당 서비스를 건드리지 말고
아래 SSM 포트 포워딩에서 로컬 포트를 다른 값(예: 13306)으로 바꾼다. 포트 점유 프로세스는
`tasklist //FI "PID eq <netstat로 확인한 PID>"`로 어떤 프로세스인지 확인할 수 있다.

### 2.2 SSM 포트 포워딩 시작 (백그라운드)

```bash
aws ssm start-session \
  --target <EC2-인스턴스ID> \
  --document-name AWS-StartPortForwardingSessionToRemoteHost \
  --parameters '{"host":["<RDS엔드포인트>"],"portNumber":["3306"],"localPortNumber":["<로컬포트>"]}' \
  --region <리전>
```

문서명은 `AWS-StartPortForwardingSessionToRemoteHost`이다 (`AWS-StartPortForwardingToRemoteHost`
아님 — "Session"이 중간에 들어간다. 비슷한 이름의 다른 문서(`AWS-StartPortForwardingSession`,
`AWS-StartSSHSession`)가 실제로 존재하므로 헷갈리기 쉽다).

### 2.3 IAM 인증 토큰 발급 (15분 유효, 매번 새로 발급)

```bash
TOKEN=$(aws rds generate-db-auth-token \
  --hostname <RDS엔드포인트> --port 3306 \
  --username readonly_iam --region <리전>)
```

토큰 값은 화면에 출력하지 않는다(변수에 담아 바로 다음 명령에 사용).

### 2.4 Docker mysql 클라이언트로 접속

```bash
docker run --rm mysql:8 mysql -h host.docker.internal -P <로컬포트> \
  -u readonly_iam --password="$TOKEN" \
  --ssl-mode=REQUIRED --enable-cleartext-plugin \
  <스키마명> -e "<조회 SQL>"
```

- `--ssl-mode=REQUIRED`: RDS IAM 인증은 SSL 연결을 강제한다.
- `--enable-cleartext-plugin`: IAM 토큰은 `mysql_clear_password` 플러그인으로 전달되는데,
  MySQL 공식 클라이언트가 이 플러그인을 기본 비활성화해두므로 명시적으로 켜야 한다. 없으면
  `ERROR 2059: Authentication plugin 'mysql_clear_password' cannot be loaded`가 발생한다.

## 3. 정리

- SSM 세션은 작업 종료 후 Ctrl+C 또는 세션 종료로 닫는다.
- IAM 토큰은 15분 후 자동 만료되므로 별도 폐기 작업이 필요 없다.

## 4. 참고

- 설계 배경(왜 SSH+고정 비밀번호 대신 이 방식을 선택했는가): `docs/tech/claude-code-access-rds.md`
- 실제 EC2 인스턴스 ID/RDS 엔드포인트/리전/스키마명: `rds-access.local.md` (같은 디렉터리,
  git에 커밋되지 않음)
- 로컬 IAM 사용자의 `ssm:StartSession`/`rds-db:connect` 권한이 `Resource: "*"`처럼 넓게
  열려 있다면, 특정 EC2 인스턴스·SSM 문서·DB 유저 ARN으로 스코프를 좁히는 것을 다음 개선
  과제로 남겨둔다.
