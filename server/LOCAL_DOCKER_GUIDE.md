# 로컬 Docker 멀티모듈 배포 가이드

API(8080)와 Admin(8081)을 각각 독립 컨테이너로 실행하는 절차입니다.

---

## 사전 준비

Docker Desktop이 실행 중인지 확인합니다.

```bash
docker --version
docker compose version
```

둘 다 버전이 출력되면 준비 완료입니다.

---

## Step 1: 프로젝트 디렉토리로 이동

```bash
cd /Users/kwonkeonhyeong/Desktop/2025-moment/server
```

---

## Step 2: `.env.local` 파일 생성

프로젝트 루트(`server/`)에 `.env.local` 파일을 생성합니다. 이 파일은 `.gitignore`에 의해 자동 무시됩니다.

```bash
touch .env.local
```

아래 내용을 `.env.local`에 붙여넣으세요. **그대로 복사해서 사용해도 로컬 실행에는 문제없습니다.**

```properties
# === MySQL ===
MYSQL_DATABASE=moment
MYSQL_USER=moment_user
MYSQL_PASSWORD=moment_password
MYSQL_ROOT_PASSWORD=root_password

# === DB Connection (DB_HOST=mysql → docker-compose 서비스명) ===
DB_HOST=mysql
DB_PORT=3306
DB_NAME=moment
DB_USER=moment_user
DB_PASSWORD=moment_password

# === Flyway (API만 사용, root 권한 필요) ===
FLYWAY_DB_USER=root
FLYWAY_DB_PASSWORD=root_password

# === JWT (32자 이상 필수) ===
JWT_ACCESS_SECRET_KEY=local-test-access-key-must-be-at-least-32-characters-long
JWT_REFRESH_SECRET_KEY=local-test-refresh-key-must-be-at-least-32-characters-long
ACCESS_TOKEN_EXPIRATION_TIME=1800000
REFRESH_TOKEN_EXPIRATION_TIME=604800000

# === Google OAuth (로컬에서는 더미값) ===
GOOGLE_CLIENT_ID=dummy-google-client-id
GOOGLE_CLIENT_SECRET=dummy-google-client-secret

# === Google Email (로컬에서는 더미값) ===
GOOGLE_EMAIL_ACCOUNT=dummy@gmail.com
GOOGLE_EMAIL_PASSWORD=dummy-password

# === S3 (로컬에서는 더미값) ===
S3_BUCKET_NAME=local-test-bucket
S3_DEV_BUCKET_PATH=local/images
S3_DEV_BUCKET_OPTIMIZED_PATH=local/optimized-images
CLOUDFRONT_DOMAIN=https://local-cloudfront.example.com

# === AWS (더미값 - auto-config 실패 방지) ===
AWS_ACCESS_KEY_ID=local-dummy-access-key
AWS_SECRET_ACCESS_KEY=local-dummy-secret-key
AWS_REGION=ap-northeast-2

# === Admin ===
ADMIN_INITIAL_EMAIL=admin@moment.local
ADMIN_INITIAL_PASSWORD=LocalAdmin123!
ADMIN_INITIAL_NAME=LocalAdmin
ADMIN_SESSION_TIMEOUT=3600
```

---

## Step 3: 포트 충돌 확인

로컬에 MySQL이 이미 실행 중이면 3306 포트가 충돌합니다.

```bash
lsof -i :3306
lsof -i :8080
lsof -i :8081
```

출력이 있으면 해당 프로세스를 먼저 종료해주세요.

- 로컬 MySQL 종료: `brew services stop mysql` 또는 `sudo systemctl stop mysql`
- 기존 Docker 컨테이너 종료: `docker compose down`
- **이전에 Docker MySQL을 실행한 적 있다면** 반드시 볼륨까지 삭제: `docker compose down -v`
  > MySQL은 최초 볼륨 생성 시에만 `MYSQL_ROOT_PASSWORD`를 적용합니다.
  > 이전 실행에서 다른 비밀번호로 생성된 볼륨이 남아있으면 `Access denied` 에러가 발생합니다.

---

## Step 4: 빌드 및 실행

```bash
./deploy.sh
```

이 스크립트가 하는 일:
1. `.env.local` 파일 존재 여부 확인
2. `docker compose --env-file .env.local up --build -d` 실행

**첫 빌드는 5~10분 소요됩니다.** (Gradle 의존성 다운로드 + Docker 이미지 빌드)

---

## Step 5: 기동 상태 확인

### 5-1. 컨테이너 상태 확인

```bash
docker compose ps
```

3개 컨테이너가 모두 `Up` 또는 `healthy`로 표시되어야 합니다:

| NAME | PORTS | 기대 상태 |
|------|-------|----------|
| moment-dev-mysql | 3306 | healthy |
| moment-api-server | 8080 | healthy |
| moment-admin-server | 8081 | running |

### 5-2. 실시간 로그 확인

```bash
# 전체 로그
docker compose logs -f

# API 로그만
docker compose logs -f api

# Admin 로그만
docker compose logs -f admin
```

기동 순서: **MySQL → API (Flyway 마이그레이션) → Admin**

Admin은 API의 헬스체크(`/health`)가 통과된 후에 시작됩니다.

### 5-3. API 헬스체크

```bash
curl http://localhost:8080/health
```

`OK`가 반환되면 API 서버가 정상입니다.

### 5-4. Admin 접근 확인

```bash
curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8081/api/admin/auth/login
```

`400`이 반환되면 정상입니다. (body 없이 POST했으므로 validation 에러)

### 5-5. Swagger UI

브라우저에서 열어보세요:

```
http://localhost:8080/swagger-ui/index.html
```

---

## Step 6: 종료

```bash
# 컨테이너만 종료 (MySQL 데이터 유지)
docker compose down

# 컨테이너 + MySQL 데이터 모두 삭제 (완전 초기화)
docker compose down -v
```

---

## 트러블슈팅

### "ERROR: .env.local 파일이 없습니다"

Step 2를 수행하지 않았습니다. `.env.local` 파일을 생성하세요.

### API 컨테이너가 계속 재시작

```bash
docker compose logs api
```

로그에서 원인을 확인하세요. 흔한 원인:
- MySQL이 아직 준비되지 않음 → 자동으로 재시도하므로 잠시 대기
- 환경변수 누락 → `.env.local` 내용 확인

### Admin이 시작하지 않음

Admin은 API 헬스체크가 통과된 후 시작됩니다. API가 정상 기동될 때까지 기다리세요.

```bash
docker compose logs -f api
```

`Started MomentApplication` 메시지가 보이면 Admin도 곧 시작됩니다.

### 포트 충돌 (Address already in use)

```bash
# 충돌 프로세스 확인
lsof -i :3306
lsof -i :8080
lsof -i :8081

# 기존 Docker 컨테이너가 남아있는 경우
docker compose down
```

### 이미지 재빌드 (코드 변경 후)

```bash
# 전체 재빌드
./deploy.sh

# 특정 서비스만 재빌드
docker compose up --build -d api
docker compose up --build -d admin
```

### 완전 초기화 (캐시 포함)

```bash
docker compose down -v
docker builder prune -f
./deploy.sh
```
