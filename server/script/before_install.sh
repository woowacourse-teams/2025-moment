#!/bin/bash
# scripts/before_install.sh

# /home/ubuntu/moment 디렉토리의 .env 파일을 삭제하여
# ApplicationStart 단계에서 새로 생성하도록 합니다.
sudo rm -f /home/ubuntu/moment/.env
