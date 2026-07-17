# ---- build stage ----
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# 1) Gradle Wrapper + 설정 파일만 먼저 복사 (의존성 캐시 최적화)
COPY gradlew .
COPY gradlew.bat .
COPY gradle/ gradle/
COPY build.gradle settings.gradle ./

# gradlew 실행권한
RUN chmod +x gradlew

# (선택) 의존성만 먼저 받아 캐시 (실패해도 괜찮게 || true)
RUN ./gradlew dependencies --no-daemon || true

# 2) 소스 전체 복사 후 빌드
COPY src/ src/
# 프로젝트에 필요한 리소스가 src 말고 더 있으면 아래처럼 통째로 복사로 바꿔도 됨
# COPY . .

# Spring Boot jar 생성 (테스트는 빼고 빠르게)
RUN ./gradlew clean bootJar -x test --no-daemon

# ---- run stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# 빌드 결과 jar 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# (선택) Spring Boot 기본 포트
EXPOSE 8080

# JVM 기동 옵션
#
# 힙 상한은 명시하지 않고 Java 21의 컨테이너 인식 기본 ergonomics에 맡긴다. UseContainerSupport가
# 기본 활성이라 JVM이 compose의 mem_limit(cgroup 한도)을 자동으로 읽어 그 25%를 힙 상한으로 잡는다.
# 과거 MaxRAMPercentage=50.0을 걸었으나 실측 결과 역효과여서 제거했다 — 640m 기준 힙 상한을
# 320MB로 잡아 기본값(25% = 160MB)의 2배로 오히려 힙 천장을 키우고 RSS를 부풀렸다. 힙 실사용량은
# idle 기준 약 68MB(로컬 NMT 실측)라 기본 25%로도 2.6배 여유이고, 메모리 위험은 늘 상한이 없는
# 논힙(Metaspace 등 약 197MB)에서 왔지 힙에서 온 적이 없다. InitialRAMPercentage=30.0도 초기 힙을
# 미리 커밋시켜 RSS를 앞당겨 부풀리므로 함께 제거했다.
# 산출 근거: docs/tech/infra/EC2 응답 불능 문제 해결하기.md
#
# - ExitOnOutOfMemoryError: 힙 고갈 시 매달리지 않고 즉시 종료 → 깔끔한 재기동
# - TieredStopAtLevel=1: C2 JIT 생략으로 기동 CPU 버스트/코드캐시 절감
ENTRYPOINT ["java", \
  "-XX:+ExitOnOutOfMemoryError", \
  "-XX:TieredStopAtLevel=1", \
  "-jar", "app.jar"]