#!/bin/bash
set -e

# 애플리케이션 루트 디렉터리로 이동
cd /home/ubuntu/moment

# 1. (핵심) EC2 부팅 시 생성된 .env 파일을 읽어 모든 변수를 현재 쉘 환경으로 로드합니다.
echo ">> .env 파일에서 환경 변수를 로드합니다..."
if [ -f .env ]; then
  # source 명령어는 파일 안의 KEY=VALUE 를 현재 쉘의 환경변수로 설정해줍니다.
  source .env
else
  echo ">> ERROR: .env 파일이 존재하지 않습니다. ASG User Data 스크립트를 확인하세요."
  exit 1 # .env 파일이 없으면 배포를 즉시 실패시킴
fi

# 2. image_tag.txt를 읽어 전체 이미지 URI를 생성합니다.
#    DOCKERHUB_USERNAME은 방금 .env 파일에서 로드되었으므로 여기서 사용 가능합니다.
IMAGE_TAG=$(cat image_tag.txt)
export IMAGE_URI="${DOCKERHUB_USERNAME}/moment:${IMAGE_TAG}"
echo "IMAGE_URI=${IMAGE_URI}" >> .env

# 3. Docker Hub에 로그인합니다.
#    DOCKERHUB_TOKEN도 .env 파일에서 로드되었습니다.
echo ">> Docker Hub에 로그인합니다..."
echo "${DOCKERHUB_TOKEN}" | sudo docker login --username "${DOCKERHUB_USERNAME}" --password-stdin

# 4. docker-compose.yml이 .env 파일을 참조하여 컨테이너를 실행합니다.
echo ">> 최신 이미지를 PULL합니다: ${IMAGE_URI}"
sudo docker-compose pull app

echo ">> Docker 컨테이너를 시작합니다..."
sudo docker-compose up -d --force-recreate

echo ">> 배포 스크립트 완료"
