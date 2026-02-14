# Moment - 당신의 이야기가 공감받는 순간

삶의 순간을 기록하고, 칭찬과 위로를 주고받는 감성 소셜 플랫폼
<img width="2064" height="2752" alt="Simulator Screenshot - iPad Pro 13-inch (M5) - 2026-02-01 at 05 17 17" src="https://github.com/user-attachments/assets/ec3e3553-04fa-449f-9660-0bfaae9423a3" />

<br>

## 프로젝트 구조

```
2025-moment/
├── server/        # Backend API (Spring Boot)
├── client/        # Web Frontend (React)
├── admin/         # Admin Panel (React + Vite)
├── app/           # Mobile App (React Native + Expo)
└── .github/       # CI/CD Workflows
```

<br>

## 기술 스택

### Backend (`server/`)

| 분류 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 3.5 |
| Database | MySQL 8.0 |
| ORM | Spring Data JPA |
| Migration | Flyway |
| Auth | JWT + Google OAuth + Apple Sign-in |
| Storage | AWS S3 |
| Push | Firebase Cloud Messaging |
| Docs | SpringDoc OpenAPI (Swagger) |
| Monitoring | Prometheus + Micrometer, Logstash |
| Cache | Caffeine |
| Build | Gradle |
| Container | Docker (Eclipse Temurin 21 JRE) |

### Web Frontend (`client/`)

| 분류 | 기술 |
|------|------|
| Core | React 19, TypeScript 5.8 |
| Bundler | Webpack 5 |
| Server State | TanStack React Query 5 |
| Styling | Emotion (CSS-in-JS) |
| HTTP | Axios |
| Error Tracking | Sentry |
| Testing | Jest, Testing Library, Cypress, MSW |
| Component Docs | Storybook 9 |
| Package Manager | pnpm 9 |

### Admin Panel (`admin/`)

| 분류 | 기술 |
|------|------|
| Core | React 18, TypeScript 5.7 |
| Bundler | Vite 6 |
| Server State | TanStack React Query 5 |
| Styling | Emotion |
| HTTP | Axios |
| Testing | Vitest, Cypress |

### Mobile App (`app/`)

| 분류 | 기술 |
|------|------|
| Core | React Native 0.81, Expo 54 |
| Routing | Expo Router 6 |
| Navigation | React Navigation 7 |
| Auth | Google Sign-in, Apple Authentication |
| Push | Expo Notifications |
| Animation | React Native Reanimated |

<br>

## 아키텍처

### Backend - 도메인 기반 모듈러 모놀리스

```
server/src/main/java/moment/
├── auth/             # 인증 (JWT, OAuth)
├── moment/           # 모멘트 (핵심 도메인)
├── comment/          # 에코 (댓글)
├── group/            # 그룹 관리
├── like/             # 좋아요
├── notification/     # 알림 (SSE + FCM)
├── report/           # 신고
├── storage/          # 파일 저장 (S3)
├── user/             # 사용자
├── admin/            # 관리자
└── global/           # 공통 인프라
```

각 모듈은 `domain → service → infrastructure → presentation` 계층으로 구성

### Frontend - Feature-Based Architecture

```
client/src/
├── app/              # 라우트, API 설정, QueryClient
├── features/         # 기능 모듈 (auth, moment, comment, group ...)
├── pages/            # 페이지 컴포넌트
├── shared/           # 공통 (design-system, hooks, store, styles)
└── widgets/          # 재사용 위젯
```

<br>

## 시작하기

### 사전 요구사항

- **Node.js** 20.x
- **pnpm** 9
- **Java** 21
- **Docker** (MySQL 실행용)

### Backend

```bash
cd server

# MySQL 실행
docker-compose up -d moment-dev-mysql

# 서버 실행
./gradlew bootRun
```

### Web Frontend

```bash
cd client
pnpm install
pnpm dev
```

### Admin Panel

```bash
cd admin
pnpm install
pnpm dev
```

### Mobile App

```bash
cd app
pnpm install
pnpm start
```

<br>

## 주요 스크립트

### Client

```bash
pnpm dev              # 개발 서버
pnpm build            # 프로덕션 빌드
pnpm test             # Jest 테스트
pnpm cypress:open     # E2E 테스트
pnpm lint             # ESLint
pnpm format           # Prettier
pnpm storybook        # Storybook (port 6006)
```

### Server

```bash
./gradlew bootRun     # 개발 서버
./gradlew test        # 전체 테스트
./gradlew fastTest    # 빠른 테스트 (E2E 제외)
./gradlew e2eTest     # E2E 테스트
```

<br>

## CI/CD

GitHub Actions 기반, 경로별 트리거로 독립 배포

| 대상 | CI | CD |
|------|----|----|
| Client | lint + type check + Jest + Cypress | S3 배포 (dev/prod) |
| Server | Fast Test + E2E + Docker Build | Docker Hub → 서버 배포 |
| Admin | TypeScript + Vitest + Cypress | 빌드 후 배포 |

<br>

## Git 컨벤션

- **브랜치**: `feat/#이슈번호`, `fix/#이슈번호`
- **커밋**: 한국어, Conventional Commits (`feat:`, `fix:`, `refactor:`, `chore:`, `docs:`, `style:`, `test:`)
- **원칙**: 책임 단위로 원자적 커밋 분리
