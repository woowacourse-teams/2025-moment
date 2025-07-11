# 1. 베이스 이미지 선택 (Java 17 실행 환경)
FROM openjdk:21

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. 빌드된 JAR 파일을 컨테이너로 복사
COPY build/libs/*.jar app.jar

# 4. 컨테이너 실행 시 구동될 명령어
ENTRYPOINT ["java", "-jar", "app.jar"]
