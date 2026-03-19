# Phase 6: 배포 파이프라인 수정

> Created: 2026-02-11
> Status: PLANNED
> 전제 조건: Phase 5 완료 (멀티모듈 빌드/테스트 검증 통과)

## 목적

api와 admin이 각각 독립적으로 빌드/배포되도록 Dockerfile, docker-compose, CI/CD 파이프라인을 수정한다.

---

## 6-1. Dockerfile 분리

### 삭제: `server/Dockerfile`

### 생성 1: `server/api/Dockerfile`

```dockerfile
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser

COPY build/libs/*.jar app.jar

USER appuser

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=${SPRING_PROFILES_ACTIVE}"]
```

### 생성 2: `server/admin/Dockerfile`

```dockerfile
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser

COPY build/libs/*.jar app.jar

USER appuser

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=${SPRING_PROFILES_ACTIVE}"]
```

### 보안 개선

기존 Dockerfile은 root 사용자로 실행. 새 버전은 `appuser` 비특권 사용자로 실행.

### JAR 경로 변경

| 변경 전 | 변경 후 (api) | 변경 후 (admin) |
|---------|--------------|-----------------|
| `build/libs/*.jar` | `api/build/libs/api-*.jar` | `admin/build/libs/admin-*.jar` |

> Dockerfile의 `COPY build/libs/*.jar`는 Docker build context 기준이므로 context가 `./api`면 `api/build/libs/`를 가리킴.

---

## 6-2. docker-compose.yml 수정

### 환경 변수 파일 분리

```bash
# 현재: .env (모든 설정 통합)
# 변경: .env.common + .env.api + .env.admin

# .env.common — 공통 DB 설정
# MYSQL_DATABASE, MYSQL_USER, MYSQL_PASSWORD, MYSQL_ROOT_PASSWORD
# DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD
# FLYWAY_DB_USER, FLYWAY_DB_PASSWORD

# .env.api — api 전용
# JWT_ACCESS_SECRET_KEY, JWT_REFRESH_SECRET_KEY
# ACCESS_TOKEN_EXPIRATION_TIME, REFRESH_TOKEN_EXPIRATION_TIME
# GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET
# APPLE_CLIENT_IDS
# S3_BUCKET_NAME, S3_DEV_BUCKET_PATH, S3_PROD_BUCKET_PATH
# S3_DEV_BUCKET_OPTIMIZED_PATH, S3_PROD_BUCKET_OPTIMIZED_PATH
# CLOUDFRONT_DOMAIN
# GOOGLE_EMAIL_ACCOUNT, GOOGLE_EMAIL_PASSWORD

# .env.admin — admin 전용
# ADMIN_INITIAL_EMAIL, ADMIN_INITIAL_PASSWORD, ADMIN_INITIAL_NAME
# ADMIN_SESSION_TIMEOUT
```

### 변경 후: `server/docker-compose.yml`

```yaml
services:
  mysql:
    container_name: moment-dev-mysql
    image: mysql:8.0
    restart: always
    env_file:
      - .env.common
    environment:
      MYSQL_DATABASE: ${MYSQL_DATABASE}
      MYSQL_USER: ${MYSQL_USER}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-p${MYSQL_ROOT_PASSWORD}"]
      interval: 5s
      timeout: 3s
      retries: 10
      start_period: 30s

  api:
    container_name: moment-api-server
    build:
      context: ./api
    depends_on:
      mysql:
        condition: service_healthy
    ports:
      - "8080:8080"
    env_file:
      - .env.common
      - .env.api
    environment:
      SPRING_PROFILES_ACTIVE: dev
    volumes:
      - ./logs/api:/app/logs

  admin-server:
    container_name: moment-admin-server
    build:
      context: ./admin
    depends_on:
      mysql:
        condition: service_healthy
    ports:
      - "8081:8081"
    env_file:
      - .env.common
      - .env.admin
    environment:
      SPRING_PROFILES_ACTIVE: dev
    volumes:
      - ./logs/admin:/app/logs

volumes:
  mysql_data:
```

### 주요 변경점

| 항목 | 변경 전 | 변경 후 |
|------|---------|---------|
| 서비스 수 | 2 (mysql, app) | 3 (mysql, api, admin-server) |
| api 포트 | 8080 | 8080 (유지) |
| admin 포트 | — | 8081 |
| 환경 변수 | 단일 `.env` | `.env.common` + `.env.api` + `.env.admin` |
| Docker context | `.` (서버 루트) | `./api` 또는 `./admin` |

---

## 6-3. GitHub Actions 워크플로우 수정

### 수정: `.github/workflows/prod-server-ci.yml`

주요 변경:

```yaml
name: Prod Server CI  # → "Prod API CI"로 이름 변경 고려

on:
  pull_request:
    types: [ opened, synchronize, reopened ]
    branches: [ main ]
    paths:
      - 'server/api/**'
      - 'server/common/**'   # common 변경도 api CI 트리거
  push:
    branches: [ main ]
    paths:
      - 'server/api/**'
      - 'server/common/**'

jobs:
  test:
    # ...
    steps:
      # ... (JDK, cache 등 동일)
      - name: Run all tests
        run: ./gradlew :api:fastTest :api:e2eTest
        working-directory: ./server

  build-and-push:
    # ...
    steps:
      # ... (JDK, cache 등 동일)
      - name: build with Gradle
        run: ./gradlew :api:clean :api:build -x test
        working-directory: ./server

      - name: Build and push Docker image
        uses: docker/build-push-action@v5
        with:
          context: ./server/api          # 변경
          file: ./server/api/Dockerfile   # 변경
          push: true
          platforms: linux/arm64
          tags: |
            ${{ secrets.DOCKERHUB_PROD_USERNAME }}/moment-prod-images:latest
            ${{ secrets.DOCKERHUB_PROD_USERNAME }}/moment-prod-images:${{ github.sha }}
```

### 수정: `.github/workflows/prod-server-cd.yml`

```yaml
# deploy 단계에서 컨테이너 변경
script: |
  # 컨테이너명 변경
  if [ "$(sudo docker ps -a -q -f name=moment-api-server)" ]; then
    sudo docker stop moment-api-server
    sudo docker rm -f moment-api-server
  fi

  # docker compose 서비스명 변경
  sudo -E docker compose up --no-deps -d api
```

### 신규: `.github/workflows/backend-admin-ci.yml`

```yaml
name: Backend Admin CI

on:
  pull_request:
    types: [ opened, synchronize, reopened ]
    branches: [ main ]
    paths:
      - 'server/admin/**'
      - 'server/common/**'
  push:
    branches: [ main ]
    paths:
      - 'server/admin/**'
      - 'server/common/**'

jobs:
  test:
    if: github.event_name == 'pull_request'
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: ./server
    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Cache Gradle dependencies
        uses: actions/cache@v3
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
          restore-keys: ${{ runner.os }}-gradle-

      - name: Setup Gradle
        uses: gradle/gradle-build-action@v2
        with:
          gradle-home-cache-cleanup: true

      - name: Run admin tests
        run: ./gradlew :admin:fastTest :admin:e2eTest

  build-and-push:
    if: github.event_name == 'push'
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: ./server
    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Free disk space
        run: |
          docker system prune -af
          docker builder prune -af
          sudo rm -rf /usr/local/lib/android
          df -h

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Cache Gradle dependencies
        uses: actions/cache@v3
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
          restore-keys: ${{ runner.os }}-gradle-

      - name: Setup Gradle
        uses: gradle/gradle-build-action@v2
        with:
          gradle-home-cache-cleanup: true

      - name: Build admin with Gradle
        run: ./gradlew :admin:clean :admin:build -x test

      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v3

      - name: Login to Docker Hub
        uses: docker/login-action@v3
        with:
          username: ${{ secrets.DOCKERHUB_PROD_USERNAME }}
          password: ${{ secrets.DOCKERHUB_PROD_TOKEN }}

      - name: Build and push admin Docker image
        uses: docker/build-push-action@v5
        with:
          context: ./server/admin
          file: ./server/admin/Dockerfile
          push: true
          platforms: linux/arm64
          tags: |
            ${{ secrets.DOCKERHUB_PROD_USERNAME }}/moment-admin-server:latest
            ${{ secrets.DOCKERHUB_PROD_USERNAME }}/moment-admin-server:${{ github.sha }}

      - name: Save image tag
        run: echo ${{ github.sha }} > ./admin-image-tag.txt

      - name: Upload artifact
        uses: actions/upload-artifact@v4
        with:
          name: admin-build-artifact
          path: server/admin-image-tag.txt
```

### 신규: `.github/workflows/backend-admin-cd.yml`

```yaml
name: Backend Admin CD

on:
  workflow_run:
    workflows: [ "Backend Admin CI" ]
    types: [ completed ]
    branches: [ main ]

jobs:
  deploy:
    if: >
      github.event.workflow_run.conclusion == 'success' &&
      github.event.workflow_run.event == 'push'
    runs-on: ubuntu-latest
    permissions:
      actions: read
    steps:
      - name: Download artifact
        uses: actions/download-artifact@v4
        with:
          name: admin-build-artifact
          run-id: ${{ github.event.workflow_run.id }}
          github-token: ${{ secrets.GITHUB_TOKEN }}

      - name: Extract deploy tag
        run: |
          echo "DEPLOY_TAG=$(cat admin-image-tag.txt)" >> $GITHUB_ENV

      - name: Deploy admin to EC2
        uses: appleboy/ssh-action@master
        with:
          host: ${{ secrets.PROD_EC2_HOST }}
          username: ${{ secrets.PROD_EC2_USERNAME }}
          key: ${{ secrets.PROD_EC2_SSH_KEY }}
          script: |
            echo ${{ secrets.DOCKERHUB_PROD_TOKEN }} | sudo docker login -u ${{ secrets.DOCKERHUB_PROD_USERNAME }} --password-stdin

            if [ "$(sudo docker ps -a -q -f name=moment-admin-server)" ]; then
              sudo docker stop moment-admin-server
              sudo docker rm -f moment-admin-server
            fi

            sudo docker pull ${{ secrets.DOCKERHUB_PROD_USERNAME }}/moment-admin-server:${{ env.DEPLOY_TAG }}

            cd /home/ubuntu/moment
            export ADMIN_IMAGE_TAG=${{ env.DEPLOY_TAG }}
            sudo -E docker compose up --no-deps -d admin-server

            sudo docker image prune -f
```

---

## 6-4. 배포 스크립트 수정

### `server/deploy.sh`

```bash
#!/bin/bash
set -euo pipefail

echo "▶ Docker Compose를 사용하여 전체 서비스를 빌드하고 실행합니다..."
docker compose up --build -d

echo ""
echo "✅ Docker Compose 실행 완료!"
echo "컨테이너:"
echo "  - moment-api-server (port 8080)"
echo "  - moment-admin-server (port 8081)"
echo "  - moment-dev-mysql (port 3306)"
echo ""
echo "'docker ps' 또는 'docker compose logs -f' 로 확인하세요."
```

### `server/scripts/start_server.sh`

```bash
#!/bin/bash
set -euo pipefail
command -v docker >/dev/null 2>&1 || { echo >&2 "Docker가 설치되지 않았습니다."; exit 1; }

cd /home/ubuntu/moment

echo ">> .env 파일들에서 환경 변수를 로드합니다..."
for env_file in .env.common .env.api .env.admin; do
  if [ -f "$env_file" ]; then
    source "$env_file"
  else
    echo ">> WARNING: $env_file 파일이 없습니다."
  fi
done

# API 이미지
API_IMAGE_TAG=$(cat api-image-tag.txt 2>/dev/null || echo "latest")
export API_IMAGE_URI="${DOCKERHUB_USERNAME}/moment-prod-images:${API_IMAGE_TAG}"

# Admin 이미지
ADMIN_IMAGE_TAG=$(cat admin-image-tag.txt 2>/dev/null || echo "latest")
export ADMIN_IMAGE_URI="${DOCKERHUB_USERNAME}/moment-admin-server:${ADMIN_IMAGE_TAG}"

echo ">> Docker Hub에 로그인합니다..."
echo "${DOCKERHUB_TOKEN}" | sudo docker login --username "${DOCKERHUB_USERNAME}" --password-stdin

echo ">> 최신 이미지를 PULL합니다..."
sudo docker compose pull api admin-server

echo ">> Docker 컨테이너를 시작합니다..."
sudo docker compose up -d --force-recreate

echo ">> 배포 스크립트 완료"
```

---

## 6-5. 배포 전략 요약

| 항목 | 결정 |
|------|------|
| Docker Hub | 같은 계정, 이미지명 분리 (`moment-prod-images`, `moment-admin-server`) |
| EC2 배치 | 같은 EC2에 2개 컨테이너 (추후 분리 가능) |
| CI 트리거 | common 변경 시 **양쪽 CI 모두 트리거** |
| 개별 배포 | `docker compose up --no-deps -d {service}` |
| 포트 | api=8080, admin=8081 |

---

## 6-6. 배포 검증

### 로컬 Docker 빌드 테스트

```bash
# Gradle 빌드
./gradlew :api:build -x test
./gradlew :admin:build -x test

# Docker 이미지 빌드
docker build -t moment-api ./api
docker build -t moment-admin ./admin

# Docker Compose 시작
docker compose up -d

# 헬스 체크
curl http://localhost:8080/health
curl http://localhost:8081/api/admin/auth/login  # 401 반환 예상 (미로그인)

# 정리
docker compose down
```

### 커밋 및 태그

```bash
git add -A
git commit -m "ci: Phase 6 - 배포 파이프라인 멀티모듈 대응"
git tag phase-6-complete
```

---

## Phase 6 체크리스트

| 항목 | 확인 |
|------|------|
| 기존 `Dockerfile` 삭제 | [ ] |
| `api/Dockerfile` 생성 (non-root) | [ ] |
| `admin/Dockerfile` 생성 (non-root) | [ ] |
| `docker-compose.yml` 3개 서비스 | [ ] |
| `.env.common`, `.env.api`, `.env.admin` 분리 | [ ] |
| `prod-server-ci.yml` paths 수정 | [ ] |
| `prod-server-cd.yml` 컨테이너명 수정 | [ ] |
| `backend-admin-ci.yml` 신규 생성 | [ ] |
| `backend-admin-cd.yml` 신규 생성 | [ ] |
| `deploy.sh` 수정 | [ ] |
| `scripts/start_server.sh` 수정 | [ ] |
| 로컬 Docker 빌드 테스트 통과 | [ ] |
| api 헬스 체크 성공 | [ ] |
| admin 접근 확인 | [ ] |
