#!/bin/bash
# scripts/stop_server.sh

cd /home/ubuntu/moment
# 실행 중인 컨테이너가 있으면 docker-compose down으로 모두 내림
if [ "$(sudo docker-compose ps -q)" ]; then
    sudo docker-compose down
fi