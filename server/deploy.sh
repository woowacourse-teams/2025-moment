#!/bin/bash

set -euo pipefail

echo "▶ Docker Compose를 사용하여 컨테이너를 빌드하고 실행합니다..."
docker compose up --build -d

echo ""
echo "✅ Docker Compose 실행 완료!"
echo "컨테이너가 백그라운드에서 실행 중입니다. 'docker ps' 명령어로 확인하세요."
echo "로그를 확인하려면 'docker compose logs -f' 명령어를 사용하세요."
