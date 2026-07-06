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

# JVM 메모리/기동 옵션 (t2.micro OOM 대응)
# - MaxRAMPercentage=50: 컨테이너 mem_limit의 50%를 최대 힙으로 (나머지는 논힙/버퍼 여유)
# - ExitOnOutOfMemoryError: 힙 고갈 시 매달리지 않고 즉시 종료 → 깔끔한 재기동
# - TieredStopAtLevel=1: C2 JIT 생략으로 기동 CPU 버스트/코드캐시 절감
ENTRYPOINT ["java", \
  "-XX:MaxRAMPercentage=50.0", \
  "-XX:InitialRAMPercentage=30.0", \
  "-XX:+ExitOnOutOfMemoryError", \
  "-XX:TieredStopAtLevel=1", \
  "-jar", "app.jar"]