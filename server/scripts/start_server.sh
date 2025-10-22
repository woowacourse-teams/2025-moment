#!/bin/bash

# 1. 스크립트 실행 옵션 강화
set -euo pipefail

# 필수 명령어 존재 여부 확인
command -v docker >/dev/null 2>&1 || { echo >&2 "Docker가 설치되지 않았거나 PATH에 없습니다. 스크립트를 중단합니다."; exit 1; }

# 애플리케이션 루트 디렉터리로 이동
cd /home/ubuntu/moment

# 2. .env 파일에서 환경 변수 로드
echo ">> .env 파일에서 환경 변수를 로드합니다..."
if [ -f .env ]; then
  source .env
else
  echo ">> ERROR: .env 파일이 존재하지 않습니다. ASG User Data 스크립트를 확인하세요."
  exit 1
fi

# 3. 이미지 URI 생성
IMAGE_TAG=$(cat image_tag.txt)
export IMAGE_URI="${DOCKERHUB_USERNAME}/moment-prod-images:${IMAGE_TAG}"
echo "IMAGE_URI=${IMAGE_URI}" >> .env

# 4. Docker Hub 로그인
echo ">> Docker Hub에 로그인합니다..."
echo "${DOCKERHUB_TOKEN}" | sudo docker login --username "${DOCKERHUB_USERNAME}" --password-stdin

# 5. 최신 이미지를 PULL하고 컨테이너 재시작 (docker compose)
echo ">> 최신 이미지를 PULL합니다: ${IMAGE_URI}"
sudo docker compose pull app

echo ">> Docker 컨테이너를 시작합니다..."
sudo docker compose up -d --force-recreate

echo ">> 배포 스크립트 완료"
