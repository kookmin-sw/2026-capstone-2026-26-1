# 운영 논힙 재측정 및 CloudWatch SDK 개선안 (2026-08-05)

## 배경

`2026-08-04-운영-힙논힙-실측.md`에서 논힙(Metaspace)이 OOM의 실질적 원인임을 확인한 뒤,
`WebClient`→`RestClient` 전환(webflux 제거)으로 로드 클래스를 줄이는 PR을 머지했다
(`refactor: WebClient를 RestClient로 전환하고 spring-boot-starter-webflux 제거`).
`release-server` 배포(운영 반영) 직전, 배포 전 기준선을 남기고 배포 효과를 사후 비교할 수
있도록 운영 환경을 재측정했다.

이 재측정 과정에서 예상 밖의 사실이 드러났다: 로컬에서는
`software.amazon.awssdk.*`/`io.grpc.*` 클래스가 0개였는데, 이는 로컬 `.env.local`의
`CLOUDWATCH_METRICS_ENABLED` 기본값이 `false`였기 때문이었다. `.github/workflows/deploy.yml`을
보면 운영은 이 값이 **`true`로 하드코딩**돼 있어(`CloudWatchMetricsConfig`가
`@ConditionalOnProperty`로 게이트하는 그 값), 운영에서만 AWS SDK CloudWatch 비동기
클라이언트가 실제로 로드된다. 이 문서는 그 실측치와, "CloudWatch를 계속 쓴다는 전제 하에"
이 SDK의 메타스페이스 기여를 줄일 수 있는 방법을 정리한다.

## 측정 대상 및 방법

- 인스턴스: `i-09cf2646d48142d46`(`2026-08-04-운영-힙논힙-실측.md`와 동일)
- 측정 시점 기준 실행 중이던 컨테이너: `release-server` 최신 배포분(PR #73~#75 반영,
  이번 webflux 제거 PR은 **아직 미배포** — 배포 전 기준선)
- **측정 방법 갱신**: 기존 문서는 대화형 `jcmd`/JMX 부착 방식을 썼으나, 이번에는
  `aws ssm send-command`(`AWS-RunShellScript` 문서)로 비대화형 실행했다. 방식 자체
  (임시 `eclipse-temurin:21-jdk` 컨테이너를 `--pid=container:<대상>`으로 붙여 `jcmd`
  실행)는 동일하나, 대화형 세션(`aws ssm start-session`) 대신 `send-command` +
  `get-command-invocation` 폴링을 쓰면 에이전트가 스크립트로 완전히 자동화할 수 있다.
  AWS 자격증명은 `terraform-admin` 프로파일 사용(`aws-account-migration-profile` 메모리
  참고 — 계정 이관으로 기본 프로파일이 아닌 별도 프로파일 필요).
  ```bash
  aws ssm send-command --profile terraform-admin --region ap-northeast-2 \
    --instance-ids i-09cf2646d48142d46 --document-name AWS-RunShellScript \
    --parameters 'commands=["CID=$(docker ps --filter name=myapp --format \"{{.ID}}\" | head -1); docker run --rm --pid=container:$CID --user root eclipse-temurin:21-jdk jcmd 1 VM.metaspace"]'
  ```
- 로드된 클래스 상세 집계는 `jcmd 1 VM.classes`로 전체 클래스를 덤프한 뒤 패키지별 `grep -c`로
  집계(로컬 실험 `2026-08-05-webflux-제거-클래스로딩-비교.md`와 동일한 방법론).

## 측정 결과

### 논힙 총량 (`jcmd VM.metaspace`)

| 영역 | committed | used |
|---|---:|---:|
| Non-Class(Metaspace) | 98.67 MB | 98.28 MB |
| Class space | 16.63 MB | 16.36 MB |
| **논힙 합계** | **115.31 MB** | **114.63 MB** |

`MaxMetaspaceSize: unlimited`는 8/4 실측과 동일하게 여전히 무제한이다.

### 로드된 클래스 (총 26,387개, 클래스로더 487개, CDS 공유 1,415개)

| 패키지 | 로드된 클래스 수 | 비고 |
|---|---:|---|
| `software.amazon.awssdk.*` | **1,946개** | CloudWatch 비동기 클라이언트 — 로컬에선 0개였던 것과 대비, 운영 최대 기여자 |
| `io.netty.*` | 1,126개 | AWS SDK(`netty-nio-client`) + Lettuce + webflux(아직 미제거) 합산 |
| `io.lettuce.*` | 549개 | Redis(RefreshToken), 이번 PR과 무관 |
| `reactor.core.*` | 391개 | Lettuce 내부 이벤트 분배 |
| `com.google.firebase.*` | 41개 | FCM, REST 기반이라 gRPC 미사용 |
| `reactor.netty.*` | 91개 | **webflux 전용 — 이번 PR 배포 시 0개로 사라질 예정** |
| `org.h2.*` | 3개 | **`testRuntimeOnly` 스코프 수정으로 이번 PR 배포 시 0개로 사라질 예정** |
| `org.apache.http.*` | **0개** | `cloudwatch` SDK가 끌고 오는 `apache-client`의 실제 사용 흔적 — 아래 개선안 1번 근거 |
| `io.grpc.*` | 0개 | firebase-admin이 REST 기반이라 gRPC 전송 계층 미사용 |

### 8/4 대비 총 클래스 수 변화

25,096개(8/4) → 26,387개(8/5, 오늘) — 배포된 PR(#73~#75)이 CloudWatch 파이프라인을
고쳐 실제로 작동시키면서 오히려 늘어난 것으로 추정된다(아래 "핵심 발견" 참고). 반면 논힙
사용량 자체는 124.6MiB(8/4) → 114.63MB(오늘)로 **줄었다** — 8/4~8/5 사이 반영된
`54d27eb5`(GC/스레드 지표 태그 축소), `c10cbaea`(Gauge 필터 버그 수정), `eb543151`(미사용
지표 제거) 등이 클래스 수보다 태그 카디널리티(같은 클래스가 반복 생성하는 오브젝트/문자열
비용)에 더 크게 작용한 것으로 보인다 — 클래스 수와 메타스페이스 사용량이 반드시 같은
방향으로 움직이지 않는다는 사례다.

## 핵심 발견 — PR #74가 CloudWatch를 "고장난 채 등록"에서 "실제로 작동"으로 바꿨다

`c10cbaea`(PR #74, "CloudWatch 커스텀 Gauge가 자체 필터에 막혀 등록 안 되던 문제 수정")
이전에는 `CLOUDWATCH_METRICS_ENABLED=true`라도 `CloudWatchAsyncClient` 빈은 만들어지되
실제 지표 push는 필터에 막혀 있었을 가능성이 있다. 이 버그가 고쳐지면서 이 클라이언트가
매 스텝(1분)마다 실제로 `PutMetricData` 호출을 보내는 상태가 됐다 — 즉 이번에 측정한
1,946개는 "죽은 채 남아있는 잔재"가 아니라 **의도대로 작동 중인 기능의 실제 비용**이다.

## CloudWatch 관련 개선안 (CloudWatch 사용을 유지한다는 전제)

`CloudWatchMetricsConfig.java`가 `CloudWatchAsyncClient.create()`로 커스터마이징 없이
기본값만 쓰고 있어 개선 여지가 있다.

### 1. `apache-client` 제외 (즉시 적용 가능, 리스크 없음)

`software.amazon.awssdk:cloudwatch`는 동기용 `apache-client`와 비동기용
`netty-nio-client`를 동시에 끌고 오는데, 이 앱은 `CloudWatchAsyncClient`(비동기)만
쓴다. 위 표에서 확인했듯 `org.apache.http.*`는 **실측 0개** — 애초에 로드되지 않는
죽은 의존성이다.

```gradle
implementation('io.micrometer:micrometer-registry-cloudwatch2') {
    exclude group: 'software.amazon.awssdk', module: 'apache-client'
}
```

이미 로드가 안 되고 있었으므로 논힙 절감 효과 자체는 미미하지만, jar 용량이 줄고 불필요한
의존성이 제거된다.

### 2. `netty-nio-client` → `aws-crt-client` 전환 (효과 큼, 검증 필요)

AWS 공식 블로그(Sources 참고)에 따르면 CRT(AWS Common Runtime) 기반 클라이언트는 C로
작성된 네이티브 바인딩으로 I/O를 처리해 Netty의 이벤트루프·코덱·파이프라인 Java 클래스
스택 자체가 필요 없다. AWS는 "CRT로 전환 시 netty-nio-client는 패키지 크기 절감을 위해
제거를 권장"한다고 명시한다.

```java
CloudWatchAsyncClient.builder()
    .httpClientBuilder(AwsCrtAsyncHttpClient.builder())
    .build();
```

**트레이드오프**: CRT는 플랫폼별 JNI 네이티브 바이너리(`software.amazon.awssdk.crt:aws-crt`)를
번들해야 한다. 지금은 EC2 t3.micro 단일 아키텍처(linux-x86_64)라 문제없지만, 향후 멀티
아키텍처(예: ARM Graviton) 전환 시 검토가 필요하다. 로컬에서 실제로 CloudWatch까지
전송되는지 확인 후 적용을 권장한다(변경 자체가 지표 파이프라인을 건드리므로).

### 3. AWS SDK 코어/프로토콜 계층은 축소 불가 — 구조적 하한선

`sdk-core`, `protocol-core`, `aws-query-protocol`, `regions`, `checksums`,
`http-auth*` 등은 HTTP 클라이언트 선택과 무관하게 공식 SDK로 API 호출 1건을 하려 해도
필요한 필수 계층이다. 1,946개 중 상당수가 여기서 나오며, 이 계층을 없애려면 SDK를
전부 버리고 `PutMetricData` REST API를 SigV4 서명까지 직접 구현해 기존 `RestClient`로
호출하는 방법뿐이다 — firebase-admin REST 대체 검토와 동일한 이유로 리스크가 커서
이번 스코프에서는 권장하지 않는다.

### 4. (직교적 옵션) AppCDS 확장

클래스 수 자체가 아니라 로드 방식을 최적화하는 보완책. 현재 26,387개 중 CDS 공유는
1,415개(5%)뿐이다. 앱 자체 클래스와 AWS SDK 클래스까지 포함하는 커스텀 AppCDS 아카이브를
만들면(`-XX:ArchiveClassesAtExit` 등) 공유 비중이 늘어 클래스당 메타스페이스 오버헤드와
부팅 시간을 줄일 수 있다. CloudWatch 기능 자체는 그대로 유지하면서 전반적으로 효과를 보는
방법이라 1~2번과 병행 가능하다.

## 배포 후 검증 (webflux 제거 PR 실제 반영 결과)

위 측정 직후 `release-server`에 `develop`을 병합해 push, `.github/workflows/deploy.yml`
(ECR 빌드 → EC2 배포)이 성공적으로 실행됐고 새 컨테이너(`b394e27f34c3`, 이미지
`capstone:latest`)가 정상 기동한 것을 확인했다. 기동 직후(부팅 후 수 분 이내) **같은
운영 컨테이너를 배포 전/후로 직접 비교** 측정했다.

| 항목 | 배포 전 | 배포 후 | 변화 |
|---|---:|---:|---:|
| 로드된 클래스 총합 | 26,387개 | 25,531개 | **-856개 (-3.2%)** |
| Metaspace 실사용량(Non-Class+Class) | 114.63 MB | 109.94 MB | **-4.69 MB (-4.1%)** |
| `reactor.netty.*`(webflux 전용) | 91개 | 0개 | **-91개 (완전 제거)** |
| `org.h2.*` | 3개 | 0개 | **-3개 (완전 제거)** |
| `io.netty.*` | 1,126개 | 914개 | -212개 |
| `reactor.core.*` | 391개 | 333개 | -58개 |
| `io.lettuce.*` | 549개 | 528개 | -21개(측정 변동 범위) |
| `software.amazon.awssdk.*` | 1,946개 | 1,944개 | 사실상 변화 없음(예상대로 이번 PR과 무관) |

`reactor.netty`(webflux 전용)와 `org.h2`는 정확히 예상대로 완전히 사라졌다. 다만 총
감소분(-856개)이 직접 추적한 패키지들의 합(-212-91-58-3=-364개)보다 훨씬 크다 —
`WebClientAutoConfiguration`이 함께 물고 있던 리액티브 코덱
(`org.springframework.http.codec.*`, `org.springframework.web.reactive.*` 등) 같은
주변부 클래스까지 같이 걷힌 것으로 보인다. 로컬 idle 실험(`../local/2026-08-05-webflux-제거-클래스로딩-비교.md`,
-219개)보다 운영 실측(-856개)이 훨씬 크게 나온 것은, 그 실험에서 남겼던 "실제 트래픽
하에서는 절감폭이 더 클 수 있다"는 예상과 일치한다.

`software.amazon.awssdk.*`는 예상대로 이번 PR과 무관하게 변화가 없다 — CloudWatch
관련 개선안(위 1~4번)은 아직 하나도 적용되지 않은 상태이며, 별도로 진행해야 줄어드는
부분이다.

## 남은 공백

- 위 CloudWatch 개선안은 아직 코드에 적용하지 않았다 — 우선순위와 적용 여부는 별도 논의 후 진행.
- 개선안 적용 시 실제 절감폭은 추정치이며, 로컬/운영 재측정으로 검증이 필요하다.
- `netty-nio-client`가 실제로 몇 개의 `io.netty.*` 클래스를 책임지는지(Lettuce·webflux
  몫과 겹치지 않게) 정확히 분리 측정하지 못했다 — 패키지명 기반 집계의 한계로, 더 정밀한
  분리는 클래스별 소속 JAR 확인이 필요하다.

## 참고

- `2026-08-04-운영-힙논힙-실측.md` — 최초 논힙 원인 규명, 대화형 jcmd/JMX 부착 방법론
- `../local/2026-08-05-webflux-제거-클래스로딩-비교.md` — webflux 제거 전/후 로컬 비교 실험
- [Introducing AWS Common Runtime HTTP Client in the AWS SDK for Java 2.x](https://aws.amazon.com/blogs/developer/introducing-aws-common-runtime-http-client-in-the-aws-sdk-for-java-2-x/)
- [AWS CRT 기반 HTTP 클라이언트 구성](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/http-configuration-crt.html)
