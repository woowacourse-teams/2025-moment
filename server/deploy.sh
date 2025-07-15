#!/bin/bash

set -euo pipefail

echo "▶ Spring Boot 애플리케이션 빌드를 시작합니다..."

if [ -f "./gradlew" ]; then
    chmod +x ./gradlew
    echo "  - Gradle Wrapper 실행 권한을 부여했습니다."
    ./gradlew build -x test # 테스트 제외 후 빌드 시 개발 환경에서 빠르게 실행 가능
    echo "  - Gradle 빌드가 완료되었습니다. (테스트 제외)"
else
    echo "오류: ./gradlew 또는 ./mvnw 파일을 찾을 수 없습니다."
    exit 1
fi

echo "✅ 빌드 성공!"
echo ""

echo "▶ Docker Compose를 사용하여 컨테이너를 빌드하고 실행합니다..."
docker compose up --build -d

echo ""
echo "✅ Docker Compose 실행 완료!"
echo "컨테이너가 백그라운드에서 실행 중입니다. 'docker ps' 명령어로 확인하세요."
echo "로그를 확인하려면 'docker compose logs -f' 명령어를 사용하세요."