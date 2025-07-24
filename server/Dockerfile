# 1단계: 빌드용 (Gradle 포함)
FROM --platform=linux/arm64 gradle:8.5-jdk21 AS builder

WORKDIR /app

COPY . .
RUN gradle build -x test --no-daemon

# 2단계: 실행용 (최소한의 JDK 포함)
FROM --platform=linux/arm64 eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
