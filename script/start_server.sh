#!/bin/bash
cd /home/ubuntu/moment

# Docker Hub 로그인 (DOCKER_HUB... 변수들은 CodeDeploy가 주입)
echo "> Login to Docker Hub"
echo "${DOCKER_HUB_TOKEN}" | sudo docker login --username "${DOCKER_HUB_USERNAME}" --password-stdin

# =========================================================================
# .env 파일 생성 로직이 완전히 사라졌습니다.
# CodeDeploy Agent가 이 스크립트를 실행하는 쉘에 이미 환경변수를
# 모두 주입해줬기 때문에 추가 작업이 필요 없습니다.
# =========================================================================

# docker-compose up 실행
echo "> Run docker-compose"
sudo docker-compose pull

# ✨ 핵심: -E 옵션으로 CodeDeploy가 주입해준 환경변수들을
#          root 권한의 docker-compose 프로세스에 그대로 전달합니다.
sudo -E docker-compose up -d --force-recreate

echo "> Deployment complete"