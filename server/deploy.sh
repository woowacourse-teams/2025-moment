#!/bin/bash
set -euo pipefail

if [ ! -f .env.local ]; then
    echo "ERROR: .env.local 파일이 없습니다."
    echo ""
    echo ".env.local 파일을 생성하고 다음 환경변수를 설정하세요:"
    echo "  MYSQL_DATABASE, MYSQL_USER, MYSQL_PASSWORD, MYSQL_ROOT_PASSWORD"
    echo "  DB_HOST=mysql, DB_PORT=3306, DB_NAME, DB_USER, DB_PASSWORD"
    echo "  FLYWAY_DB_USER, FLYWAY_DB_PASSWORD"
    echo "  JWT_ACCESS_SECRET_KEY, JWT_REFRESH_SECRET_KEY"
    echo "  ACCESS_TOKEN_EXPIRATION_TIME, REFRESH_TOKEN_EXPIRATION_TIME"
    echo "  GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET"
    echo "  GOOGLE_EMAIL_ACCOUNT, GOOGLE_EMAIL_PASSWORD"
    echo "  S3_BUCKET_NAME, S3_DEV_BUCKET_PATH, S3_DEV_BUCKET_OPTIMIZED_PATH, CLOUDFRONT_DOMAIN"
    echo "  AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY, AWS_REGION"
    echo "  ADMIN_INITIAL_EMAIL, ADMIN_INITIAL_PASSWORD, ADMIN_INITIAL_NAME, ADMIN_SESSION_TIMEOUT"
    exit 1
fi

echo ">>> Docker Compose로 전체 서비스를 빌드하고 실행합니다..."
docker compose --env-file .env.local up --build -d

echo ""
echo "=== 서비스 목록 ==="
echo "  API:   http://localhost:8080 (health: http://localhost:8080/health)"
echo "  Admin: http://localhost:8081"
echo "  MySQL: localhost:3306"
echo ""
echo ">>> 'docker compose logs -f' 로 로그를 확인하세요."
echo ">>> 'docker compose down' 으로 종료. '-v' 추가 시 데이터 삭제."
