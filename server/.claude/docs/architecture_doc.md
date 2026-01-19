# Moment - Technical Specification

> 감성 익명 SNS 서비스 기술 명세서

## 1. Executive Summary

### 1.1 서비스 개요

**Moment**는 사용자들이 하루의 일상을 익명으로 공유하고, 서로의 순간에 공감하며 정서적 지지를 주고받는 감성 SNS 서비스입니다.

### 1.2 핵심 기능

| 기능                | 설명                                 |
|-------------------|------------------------------------|
| **모멘트 작성**        | 하루 1회 기본 작성 + 별(포인트) 소모로 추가 작성     |
| **댓글 (Comment)**  | 다른 사용자의 모멘트에 익명 댓글 작성              |
| **에코 (Reaction)** | 댓글에 이모지형 공감 반응(중복 불가)              |
| **태그/필터링**        | 모멘트 태그 부여, 댓글 가능 모멘트 조회 시 태그 필터    |
| **이미지 첨부**        | 모멘트/댓글 이미지 업로드 (S3 presigned URL)  |
| **레벨/보상**         | 활동에 따른 별/경험치 및 15단계 레벨             |
| **알림**            | SSE + Firebase Push(FCM)           |
| **관리자 기능**        | Thymeleaf 기반 어드민 페이지 (유저/계정/세션 관리) |

### 1.3 기술 스택 요약

| 구분           | 기술                                                                             |
|--------------|--------------------------------------------------------------------------------|
| **Backend**  | Java 21, Spring Boot 3.5.3, JPA/Hibernate, Spring Session JDBC                 |
| **Frontend** | React 19, TypeScript 5.8, React Router, TanStack React Query, Emotion, Webpack |
| **Database** | MySQL 8.0, Flyway (마이그레이션)                                                     |
| **인증**       | JWT (Access/Refresh Token), Google OAuth 2.0                                   |
| **알림**       | Server-Sent Events (SSE), Firebase Cloud Messaging                             |
| **저장소**      | AWS S3 + CloudFront CDN                                                        |
| **모니터링**     | Micrometer + Prometheus, AWS CloudWatch                                        |
| **배포**       | Docker, Docker Compose                                                         |

---

## 2. Architecture Overview

### 2.1 시스템 아키텍처

```
┌─────────────────────────────────────────────────────────────────┐
│                        Client Layer                             │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  │
│  │   React Web     │  │  Mobile (PWA)   │  │  Admin Page     │  │
│  │  (TypeScript)   │  │                 │  │  (Thymeleaf)    │  │
│  └────────┬────────┘  └────────┬────────┘  └────────┬────────┘  │
└───────────┼─────────────────────┼─────────────────────┼─────────┘
            │                     │                     │
            ▼                     ▼                     ▼
┌─────────────────────────────────────────────────────────────────┐
│        Ingress / Reverse Proxy (optional, external layer)       │
└─────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Spring Boot Application                      │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                   Presentation Layer                      │   │
│  │  REST Controllers │ SSE Emitters │ Thymeleaf Controllers │   │
│  └──────────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                     Service Layer                         │   │
│  │  Facade Services │ Application Services │ Domain Services │   │
│  └──────────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                     Domain Layer                          │   │
│  │  Entities │ Value Objects │ Policies │ Domain Events     │   │
│  └──────────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                  Infrastructure Layer                     │   │
│  │  JPA Repositories │ External Adapters │ Event Handlers   │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
            │              │              │              │
            ▼              ▼              ▼              ▼
    ┌───────────┐  ┌───────────┐  ┌───────────┐  ┌───────────┐
    │  MySQL    │  │  AWS S3   │  │  Firebase │  │ CloudWatch│
    │  8.0      │  │  + CDN    │  │   FCM     │  │ Metrics   │
    └───────────┘  └───────────┘  └───────────┘  └───────────┘
```

### 2.2 레이어 아키텍처 (Clean Architecture)

각 도메인 모듈은 다음과 같은 계층 구조를 따릅니다:

```
{domain}/
├── domain/                 # 핵심 비즈니스 로직
│   ├── Entity.java        # JPA 엔티티
│   ├── ValueObject.java   # 값 객체
│   ├── Policy.java        # 비즈니스 정책
│   └── DomainEvent.java   # 도메인 이벤트
│
├── infrastructure/         # 외부 시스템 연동
│   ├── Repository.java    # JPA Repository
│   └── ExternalAdapter.java
│
├── service/                # 비즈니스 서비스
│   ├── facade/            # 여러 서비스 조율
│   ├── application/       # 애플리케이션 서비스
│   ├── eventHandler/      # 비동기 이벤트 처리
│   └── {domain}/          # 도메인 서비스
│
├── presentation/           # API 컨트롤러
│   └── Controller.java
│
└── dto/                    # 데이터 전송 객체
    ├── request/
    └── response/
```

### 2.3 모듈 구조

```
server/src/main/java/moment/
├── auth/           # 인증 & JWT 토큰 관리
├── admin/          # 관리자 기능
├── user/           # 사용자 관리 & 레벨 시스템
├── moment/         # 핵심 모멘트 도메인
├── comment/        # 댓글/에코 시스템
├── notification/   # SSE + Firebase Push 알림
├── reward/         # 보상 & 포인트 시스템
├── report/         # 콘텐츠 신고 시스템
├── storage/        # AWS S3 파일 저장소
└── global/         # 공유 인프라
    ├── config/     # Spring 설정
    ├── exception/  # 예외 처리
    ├── logging/    # 로깅
    ├── domain/     # BaseEntity
    └── dto/        # 공통 DTO
```

---

## 3. Core Domains & Features

### 3.1 User 도메인

#### 엔티티 구조

```java
User {
    Long(PK)
    String(provider_type와 복합 unique)
    String(BCrypt)
    String(1 - 15자, unique)
    p
```

- 고유 제약: `(email, provider_type)` 복합 유니크 + `nickname` 유니크

#### 레벨 시스템 (15단계)

| 레벨 | 등급명                 | 필요 경험치      |
|----|---------------------|-------------|
| 1  | ASTEROID_WHITE      | 0-4         |
| 2  | ASTEROID_YELLOW     | 5-9         |
| 3  | ASTEROID_SKY        | 10-19       |
| 4  | METEOR_WHITE        | 20-49       |
| 5  | METEOR_YELLOW       | 50-99       |
| 6  | METEOR_SKY          | 100-199     |
| 7  | COMET_WHITE         | 200-349     |
| 8  | COMET_YELLOW        | 350-699     |
| 9  | COMET_SKY           | 700-1199    |
| 10 | ROCKY_PLANET_WHITE  | 1200-1999   |
| 11 | ROCKY_PLANET_YELLOW | 2000-3999   |
| 12 | ROCKY_PLANET_SKY    | 4000-7999   |
| 13 | GAS_GIANT_WHITE     | 8000-15999  |
| 14 | GAS_GIANT_YELLOW    | 16000-31999 |
| 15 | GAS_GIANT_SKY       | 32000+      |

#### API 엔드포인트

| Method | Endpoint                              | 설명        |
|--------|---------------------------------------|-----------|
| POST   | `/api/v1/users/signup`                | 회원가입      |
| GET    | `/api/v1/users/me`                    | 프로필 조회    |
| POST   | `/api/v1/users/signup/nickname/check` | 닉네임 중복 확인 |
| GET    | `/api/v1/users/signup/nickname`       | 랜덤 닉네임 생성 |

---

### 3.2 Auth 도메인

#### 인증 방식: JWT

```
Access Token (환경 변수 기반)
├── subject: userId
├── claim: email
├── algorithm: HS256
└── expiration: ACCESS_TOKEN_EXPIRATION_TIME (ms)

Refresh Token (환경 변수 기반)
├── subject: userId
├── claim: email
├── algorithm: HS256
└── expiration: REFRESH_TOKEN_EXPIRATION_TIME (ms)
```

#### 토큰 저장 방식

```
ResponseCookie 설정:
├── sameSite: "none"      # CORS 허용
├── secure: true          # HTTPS only
├── httpOnly: true        # XSS 방지
├── path: "/"
├── maxAge: accessToken 30분, refreshToken 7일
└── (JWT 만료는 환경 변수 기반)
```

Refresh Token은 `refresh_tokens` 테이블에 영속화됩니다.

#### API 엔드포인트

| Method | Endpoint                            | 설명              |
|--------|-------------------------------------|-----------------|
| POST   | `/api/v1/auth/login`                | 로그인             |
| POST   | `/api/v1/auth/logout`               | 로그아웃            |
| GET    | `/api/v1/auth/login/google`         | Google OAuth 시작 |
| GET    | `/api/v1/auth/callback/google`      | Google OAuth 콜백 |
| GET    | `/api/v1/auth/login/check`          | 로그인 상태 확인       |
| POST   | `/api/v1/auth/refresh`              | 토큰 재발급          |
| POST   | `/api/v1/auth/email`                | 이메일 인증 코드 전송    |
| POST   | `/api/v1/auth/email/verify`         | 이메일 인증 확인       |
| POST   | `/api/v1/auth/email/password`       | 비밀번호 재설정 링크 전송  |
| POST   | `/api/v1/auth/email/password/reset` | 비밀번호 재설정        |

---

### 3.3 Moment 도메인

#### 엔티티 구조

```java
Moment {
    Long(PK)
    String(1 - 200자)
    boolean

```

#### 연관 엔티티

```java
MomentImage {
    Long(PK)
    Moment(FK)
    String
            String
    deletedAt:

```

`tagNames`는 필수(최소 1개)이며, `imageUrl`/`imageName`은 선택입니다.

#### 작성 정책

| 타입    | 정책                   | 설명            |
|-------|----------------------|---------------|
| BASIC | OnceADayPolicy       | 하루 1회 제한 (무료) |
| EXTRA | PointDeductionPolicy | 포인트 소모로 추가 작성 |

#### API 엔드포인트

| Method | Endpoint                         | 설명                                            |
|--------|----------------------------------|-----------------------------------------------|
| POST   | `/api/v1/moments`                | 기본 모멘트 등록                                     |
| POST   | `/api/v1/moments/extra`          | 추가 모멘트 등록 (포인트 소모)                            |
| GET    | `/api/v1/moments/me`             | 내 모멘트 조회 (cursor: `nextCursor`, `limit`)      |
| GET    | `/api/v1/moments/me/unread`      | 알림 미확인 모멘트 조회 (cursor: `nextCursor`, `limit`) |
| GET    | `/api/v1/moments/writable/basic` | 기본 모멘트 작성 가능 여부                               |
| GET    | `/api/v1/moments/writable/extra` | 추가 모멘트 작성 가능 여부                               |
| GET    | `/api/v1/moments/commentable`    | 댓글 작성 가능 모멘트 조회 (`tagName` 필터)                |

---

### 3.4 Comment 도메인

#### 엔티티 구조

```java
Comment {
    Long(PK)
    String(1 - 200자)
    commenter
```

#### 연관 엔티티

```java
CommentImage {
    Long(PK)
    Comment(FK)
    imageUrl:

```

`imageUrl`/`imageName`은 선택입니다.

#### 이벤트 기반 처리

```java
// 댓글 생성 시 이벤트 발행
CommentCreateEvent →NotificationEventHandler
→ 1.
DB 알림
저장
→ 2.
SSE 실시간
전송
→ 3.
Firebase Push
전송
```

#### API 엔드포인트

| Method | Endpoint                     | 설명                                           |
|--------|------------------------------|----------------------------------------------|
| POST   | `/api/v1/comments`           | 댓글 등록                                        |
| GET    | `/api/v1/comments/me`        | 내 댓글 조회 (cursor: `nextCursor`, `limit`)      |
| GET    | `/api/v1/comments/me/unread` | 알림 미확인 댓글 조회 (cursor: `nextCursor`, `limit`) |

---

### 3.5 Echo 도메인

#### 엔티티 구조

```java
Echo {
    Long(PK)
    String
    User(FK)

```

`echoTypes`는 1~6개 선택 가능하며, (user, comment, echoType) 조합은 중복 생성되지 않습니다.

#### 이벤트 처리

```java
// 에코 생성 시 이벤트 발행
EchoCreateEvent →NotificationEventHandler
→ 1.
DB 알림
저장
→ 2.
SSE 실시간

전송(Push 전송 없음)
```

#### API 엔드포인트

| Method | Endpoint                    | 설명        |
|--------|-----------------------------|-----------|
| POST   | `/api/v1/echos`             | 에코 등록     |
| GET    | `/api/v1/echos/{commentId}` | 댓글별 에코 조회 |

---

### 3.6 Notification 도메인

#### 알림 채널

| 채널   | 기술                       | 용도           |
|------|--------------------------|--------------|
| SSE  | Server-Sent Events       | 웹 실시간 알림     |
| Push | Firebase Cloud Messaging | 모바일/백그라운드 알림 |

#### 이벤트 처리 흐름

```
도메인 이벤트 발행
    ↓
@TransactionalEventListener (AFTER_COMMIT)
    ↓
@Async 비동기 처리
    ↓
┌──────────────────────────────────────────────────────────┐
│  1. Notification 엔티티 저장                             │
│  2. SseEmitter로 실시간 전송                             │
│  3. Firebase Push 전송 (댓글 생성 이벤트에 한해)         │
└──────────────────────────────────────────────────────────┘
```

#### API 엔드포인트

| Method | Endpoint                          | 설명                   |
|--------|-----------------------------------|----------------------|
| GET    | `/api/v1/notifications/subscribe` | SSE 구독               |
| GET    | `/api/v1/notifications`           | 알림 목록 조회 (`read=true |false`) |
| PATCH  | `/api/v1/notifications/{id}/read` | 특정 알림 읽음 처리          |
| PATCH  | `/api/v1/notifications/read-all`  | 전체 알림 읽음 처리          |

---

### 3.7 Reward 도메인

#### 엔티티 구조

```java
RewardHistory {
    Long(PK)
    User(FK)
    I
```

#### 포인트 규칙

| 행동        | Reason                | 점수   |
|-----------|-----------------------|------|
| 모멘트 작성    | MOMENT_CREATION       | +5   |
| 댓글 작성     | COMMENT_CREATION      | +2   |
| 에코 받음     | ECHO_RECEIVED         | +3   |
| 추가 모멘트 작성 | MOMENT_ADDITIONAL_USE | -10  |
| 닉네임 변경    | NICKNAME_CHANGE       | -100 |

`exp_star`는 보상 획득(양수) 시에만 증가하며, 레벨은 `exp_star` 기준으로 갱신됩니다.

---

### 3.8 Report 도메인

#### 신고 사유

| 코드                            | 설명        |
|-------------------------------|-----------|
| SPAM_OR_ADVERTISEMENT         | 스팸 또는 광고  |
| SEXUAL_CONTENT                | 음란물       |
| HATE_SPEECH_OR_DISCRIMINATION | 혐오 발언/차별  |
| ABUSE_OR_HARASSMENT           | 욕설/괴롭힘    |
| VIOLENT_OR_DANGEROUS_CONTENT  | 폭력/위험 콘텐츠 |
| PRIVACY_VIOLATION             | 개인정보 침해   |
| ILLEGAL_INFORMATION           | 불법 정보     |

#### API 엔드포인트

| Method | Endpoint                        | 설명     |
|--------|---------------------------------|--------|
| POST   | `/api/v1/moments/{id}/reports`  | 모멘트 신고 |
| POST   | `/api/v1/comments/{id}/reports` | 댓글 신고  |

---

### 3.9 Admin 도메인

#### 관리자 역할

| 역할          | 권한             |
|-------------|----------------|
| SUPER_ADMIN | 모든 권한 + 관리자 등록 |
| ADMIN       | 기본 관리 권한       |

#### 세션 관리

```java
AdminSession {
    Long(PK)
    Long(FK)
    String(unique)
    loginTime:

```

#### Admin 페이지 (Thymeleaf SSR)

| URL                                      | 기능             |
|------------------------------------------|----------------|
| `/admin/login`                           | 로그인 페이지        |
| `/admin/logout`                          | 로그아웃           |
| `/admin/users`                           | 사용자 목록/관리      |
| `/admin/users/{id}/edit`                 | 사용자 정보 수정      |
| `/admin/users/{id}/delete`               | 사용자 삭제(소프트 삭제) |
| `/admin/sessions`                        | 관리자 세션 목록      |
| `/admin/sessions/{sessionId}/invalidate` | 세션 강제 종료       |
| `/admin/accounts`                        | 관리자 계정 목록      |
| `/admin/accounts/new`                    | 관리자 계정 생성      |
| `/admin/accounts/{id}/block`             | 관리자 계정 차단      |
| `/admin/accounts/{id}/unblock`           | 관리자 계정 차단 해제   |
| `/admin/error/forbidden`                 | 권한 오류 페이지      |

---

### 3.10 MyPage 도메인

#### API 엔드포인트

| Method | Endpoint                    | 설명                               |
|--------|-----------------------------|----------------------------------|
| GET    | `/api/v1/me/profile`        | 마이페이지 프로필                        |
| GET    | `/api/v1/me/reward/history` | 보상 기록 조회 (`pageNum`, `pageSize`) |
| POST   | `/api/v1/me/nickname`       | 닉네임 변경 (별 소모)                    |
| POST   | `/api/v1/me/password`       | 비밀번호 변경                          |

---

## 4. Data Model

### 4.1 주요 테이블 관계도

```
users ─1:N─ moments ─1:N─ comments ─1:N─ echos
  │            │              │
  │            │              └─1:1─ comment_images
  │            └─1:1─ moment_images
  │
  ├─1:N─ reward_history
  ├─1:N─ notifications
  └─1:N─ push_notifications

moments ─N:M─ tags (via moment_tags)

admins ─1:N─ admin_sessions
```

### 4.2 주요 테이블 스키마

#### users

```sql
CREATE TABLE users
(
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    email          VARCHAR(255) NOT NULL,
    password       VARCHAR(255) NOT NULL,
    nickname       VARCHAR(255) NOT NULL UNIQUE,
    provider_type  VARCHAR(20)  NOT NULL,
    available_star INT          NOT NULL DEFAULT 0,
    exp_star       INT          NOT NULL DEFAULT 0,
    level          VARCHAR(100) NOT NULL,
    created_at     TIMESTAMP    NOT NULL,
    deleted_at     TIMESTAMP NULL,
    UNIQUE KEY uq_email_provider (email, provider_type)
);
```

#### moments

```sql
CREATE TABLE moments
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    momenter_id BIGINT       NOT NULL,
    content     VARCHAR(200) NOT NULL,
    is_matched  BOOLEAN     DEFAULT FALSE,
    write_type  VARCHAR(50) DEFAULT 'BASIC',
    created_at  TIMESTAMP    NOT NULL,
    deleted_at  TIMESTAMP NULL,
    FOREIGN KEY (momenter_id) REFERENCES users (id),
    INDEX       moments_created_at_id (created_at, momenter_id)
);
```

#### comments

```sql
CREATE TABLE comments
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    commenter_id BIGINT       NOT NULL,
    moment_id    BIGINT       NOT NULL,
    content      VARCHAR(200) NOT NULL,
    created_at   TIMESTAMP    NOT NULL,
    deleted_at   TIMESTAMP NULL,
    FOREIGN KEY (commenter_id) REFERENCES users (id),
    INDEX        idx_comments_commenter_created_id (commenter_id, created_at DESC, id DESC),
    INDEX        idx_commenter_id (commenter_id)
);
```

#### echos

```sql
CREATE TABLE echos
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id    BIGINT       NOT NULL,
    comment_id BIGINT       NOT NULL,
    type       VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    deleted_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (comment_id) REFERENCES comments (id),
    UNIQUE KEY uq_echo (user_id, comment_id, type)
);
```

#### tags / moment_tags

```sql
CREATE TABLE tags
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    name       VARCHAR(30) NOT NULL UNIQUE,
    created_at TIMESTAMP   NOT NULL,
    deleted_at TIMESTAMP NULL
);

CREATE TABLE moment_tags
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    moment_id  BIGINT    NOT NULL,
    tag_id     BIGINT    NOT NULL,
    created_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP NULL,
    FOREIGN KEY (moment_id) REFERENCES moments (id),
    FOREIGN KEY (tag_id) REFERENCES tags (id)
);
```

#### 기타 주요 테이블

- `moment_images`, `comment_images`: 이미지 URL/원본명 저장, 소프트 삭제 포함
- `reward_history`: 별/경험치 변동 이력
- `refresh_tokens`: 리프레시 토큰 저장
- `notifications`: 알림 저장 (type/target/is_read)
- `push_notifications`: 디바이스 토큰 저장
- `reports`: 신고 내역 저장
- `admins`, `admin_sessions`, `SPRING_SESSION*`: 관리자 및 세션 관리

#### admin_sessions

```sql
CREATE TABLE admin_sessions
(
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_id         BIGINT       NOT NULL,
    session_id       VARCHAR(512) NOT NULL UNIQUE,
    login_time       DATETIME     NOT NULL,
    last_access_time DATETIME     NOT NULL,
    ip_address       VARCHAR(50)  NOT NULL,
    user_agent       VARCHAR(512) NOT NULL,
    logout_time      DATETIME NULL,
    deleted_at       DATETIME NULL,
    FOREIGN KEY (admin_id) REFERENCES admins (id),
    INDEX            idx_admin_id (admin_id),
    INDEX            idx_session_id (session_id),
    INDEX            idx_last_access_time (last_access_time),
    INDEX            idx_deleted_at (deleted_at)
);
```

### 4.3 Soft Delete 패턴

모든 주요 엔티티에 Soft Delete 적용:

```java

@SQLDelete(sql = "UPDATE table_name SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Entity extends BaseEntity {
    private LocalDateTime deletedAt;
}
```

### 4.4 마이그레이션 히스토리

| 버전      | 설명                                                         |
|---------|------------------------------------------------------------|
| V1      | 초기 스키마 (users, moments, comments, notifications, emojis 등) |
| V2      | users: provider_type/포인트/레벨 추가, 복합 유니크 적용                  |
| V3-V5   | moments/comments 내용 길이 및 컬럼 수정                             |
| V6      | point_history → reward_history, exp_star 추가                |
| V8      | emojis → echos 테이블명 변경                                     |
| V9      | refresh_tokens 테이블 생성                                      |
| V10     | Soft Delete 컬럼 추가 (users, moments, comments, echos 등)      |
| V11     | reward_history 인덱스 조정                                      |
| V12     | tags, moment_tags 테이블 생성                                   |
| V13-V15 | 이미지 테이블 생성 및 제약/컬럼 수정                                      |
| V16     | reports 테이블 생성                                             |
| V17     | matchings 테이블 제거                                           |
| V18-V21 | comments/moments 인덱스 추가                                    |
| V22     | push_notifications 테이블 생성                                  |
| V23     | admins 테이블 생성                                              |
| V24     | admin_sessions + SPRING_SESSION 테이블                        |

---

## 5. Authentication & Authorization

### 5.1 JWT 인증 흐름

```
┌──────────┐                    ┌──────────┐                    ┌──────────┐
│  Client  │                    │  Server  │                    │    DB    │
└────┬─────┘                    └────┬─────┘                    └────┬─────┘
     │                               │                               │
     │  POST /api/v1/auth/login       │                               │
     │  {email, password}            │                               │
     │──────────────────────────────>│                               │
     │                               │  verify credentials           │
     │                               │──────────────────────────────>│
     │                               │<──────────────────────────────│
     │                               │                               │
     │  Set-Cookie: accessToken      │                               │
     │  Set-Cookie: refreshToken     │                               │
     │<──────────────────────────────│                               │
     │                               │                               │
     │  GET /api/v1/users/me         │                               │
     │  Cookie: accessToken          │                               │
     │──────────────────────────────>│                               │
     │                               │  validate JWT                 │
     │                               │  extract Authentication       │
     │                               │                               │
     │  200 OK {user data}           │                               │
     │<──────────────────────────────│                               │
```

### 5.2 Google OAuth 흐름

```
1. GET /api/v1/auth/login/google
   → Redirect to Google 인증 페이지

2. 사용자 Google 로그인 & 권한 승인

3. GET /api/v1/auth/callback/google?code=xxx
   → Google에서 authorization code 전달
   → Server가 code로 access_token 교환
   → 사용자 정보 조회 및 JWT 발급
   → Redirect to 클라이언트 + Cookie 설정
```

### 5.3 Admin 세션 관리

```
┌─────────────────────────────────────────────────────────────┐
│                  Spring Session JDBC                        │
│                                                             │
│  SPRING_SESSION 테이블                                      │
│  ├── SESSION_ID (unique)                                   │
│  ├── CREATION_TIME                                         │
│  ├── LAST_ACCESS_TIME                                      │
│  ├── MAX_INACTIVE_INTERVAL                                 │
│  └── EXPIRY_TIME                                           │
│                                                             │
│  + AdminSession 테이블 (추가 추적 정보)                     │
│  ├── ip_address                                            │
│  ├── user_agent                                            │
│  ├── login_time                                            │
│  └── logout_time                                           │
└─────────────────────────────────────────────────────────────┘
```

---

## 6. External Integrations

### 6.1 AWS S3 (파일 저장)

```yaml
s3:
  bucket-name: ${S3_BUCKET_NAME}
  bucket-path: ${S3_DEV_BUCKET_PATH}        # prod: S3_PROD_BUCKET_PATH
  optimized-bucket-path: ${S3_DEV_BUCKET_OPTIMIZED_PATH}  # prod: S3_PROD_BUCKET_OPTIMIZED_PATH
  cloudfront-domain: ${CLOUDFRONT_DOMAIN}
```

| 용도      | 경로                                 |
|---------|------------------------------------|
| 원본 이미지  | `{bucket-path}/` (UUID + 파일명으로 저장) |
| 최적화 이미지 | `{optimized-bucket-path}/`         |
| CDN 배포  | CloudFront를 통한 캐싱 및 배포             |

### 6.2 Firebase Cloud Messaging

```yaml
fcm:
  service-account-json: ${FCM_CREDENTIALS}
```

| 기능      | 설명                                |
|---------|-----------------------------------|
| 디바이스 등록 | push_notifications 테이블에 FCM 토큰 저장 |
| Push 전송 | Firebase Admin SDK를 통한 알림 발송      |

### 6.3 Google Services

| 서비스        | 용도               |
|------------|------------------|
| OAuth 2.0  | 소셜 로그인           |
| Gmail SMTP | 이메일 인증, 비밀번호 재설정 |

### 6.4 AWS CloudWatch (모니터링)

```yaml
management:
  metrics:
    export:
      cloudwatch:
        enabled: true
        step: 1m
        namespace: Moment/${profile}
```

| 메트릭           | 설명          |
|---------------|-------------|
| jvm.memory.*  | JVM 메모리 사용량 |
| jvm.gc.*      | GC 통계       |
| hikaricp.*    | DB 커넥션 풀 상태 |
| http.server.* | HTTP 요청 통계  |
| process.cpu.* | CPU 사용률     |

---

## 7. Infrastructure

### 7.1 환경 설정

#### Development 환경

```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
  mvc:
    async:
      request-timeout: -1          # SSE 연결을 위한 비동기 타임아웃 비활성화
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false            # LazyInitializationException 방지
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration/mysql
    user: ${FLYWAY_DB_USER}
    password: ${FLYWAY_DB_PASSWORD}
    url: jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}
  session:
    store-type: jdbc
    jdbc:
      initialize-schema: never
      table-name: SPRING_SESSION
  sql:
    init:
      mode: always
      data-locations: classpath:sql/test-users.sql
```

#### Production 환경

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 20
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
  sql:
    init:
      mode: never

server:
  tomcat:
    threads:
      max: 17
      min-spare: 17
    accept-count: 100
    mbeanregistry:
      enabled: true              # Tomcat 메트릭 수집

management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics
  endpoint:
    health:
      show-details: always
```

### 7.2 Docker 구성

#### Dockerfile

```dockerfile
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=${SPRING_PROFILES_ACTIVE}"]
```

#### Docker Compose

```yaml
services:
  mysql:
    container_name: moment-dev-mysql
    image: mysql:8.0
    restart: always
    env_file:
      - .env
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
      test: [ "CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-p${MYSQL_ROOT_PASSWORD}" ]

  app:
    container_name: moment-app-server
    build: ../../../docs
    image: moment:local
    depends_on:
      mysql:
        condition: service_healthy
    ports:
      - "8080:8080"
    env_file:
      - .env
    environment:
      SPRING_PROFILES_ACTIVE: dev
    volumes:
      - ./logs:/app/logs
      - ./firebase-credentials.json:/app/firebase-credentials.json

volumes:
  mysql_data:
```

### 7.3 환경 변수

| 카테고리                  | 변수                                                              | 설명                      |
|-----------------------|-----------------------------------------------------------------|-------------------------|
| **Database (App)**    | DB_HOST, DB_PORT, DB_NAME                                       | MySQL 연결 정보             |
|                       | DB_USER, DB_PASSWORD                                            | DB 인증 정보                |
|                       | FLYWAY_DB_USER, FLYWAY_DB_PASSWORD                              | Flyway 마이그레이션 계정        |
| **Database (Docker)** | MYSQL_DATABASE, MYSQL_USER, MYSQL_PASSWORD, MYSQL_ROOT_PASSWORD | docker-compose MySQL 설정 |
| **Security**          | JWT_ACCESS_SECRET_KEY                                           | Access Token 서명 키       |
|                       | JWT_REFRESH_SECRET_KEY                                          | Refresh Token 서명 키      |
|                       | ACCESS_TOKEN_EXPIRATION_TIME                                    | Access Token 만료 시간      |
|                       | REFRESH_TOKEN_EXPIRATION_TIME                                   | Refresh Token 만료 시간     |
| **OAuth**             | GOOGLE_CLIENT_ID                                                | Google OAuth 클라이언트 ID   |
|                       | GOOGLE_CLIENT_SECRET                                            | Google OAuth 시크릿        |
| **Email**             | GOOGLE_EMAIL_ACCOUNT                                            | SMTP 계정                 |
|                       | GOOGLE_EMAIL_PASSWORD                                           | SMTP 앱 비밀번호             |
| **AWS**               | S3_BUCKET_NAME                                                  | S3 버킷명                  |
|                       | S3_DEV_BUCKET_PATH, S3_PROD_BUCKET_PATH                         | 환경별 원본 이미지 경로           |
|                       | S3_DEV_BUCKET_OPTIMIZED_PATH, S3_PROD_BUCKET_OPTIMIZED_PATH     | 환경별 최적화 이미지 경로          |
|                       | CLOUDFRONT_DOMAIN                                               | CDN 도메인                 |
| **Firebase**          | FCM_CREDENTIALS                                                 | Firebase 서비스 계정 JSON 경로 |
| **Admin**             | ADMIN_INITIAL_EMAIL                                             | 초기 관리자 이메일              |
|                       | ADMIN_INITIAL_PASSWORD                                          | 초기 관리자 비밀번호             |
|                       | ADMIN_INITIAL_NAME                                              | 초기 관리자 이름               |
|                       | ADMIN_SESSION_TIMEOUT                                           | 관리자 세션 타임아웃             |
|                       | ADMIN_SESSION_COOKIE_NAME                                       | 관리자 세션 쿠키명              |

---

## 8. Testing Strategy

### 8.1 테스트 구조

```
src/test/
├── java/moment/
│   ├── admin/           # 관리자 기능 테스트
│   ├── auth/            # 인증 테스트
│   ├── comment/         # 댓글 테스트
│   ├── moment/          # 모멘트 테스트
│   ├── notification/    # 알림 테스트
│   ├── user/            # 사용자 테스트
│   ├── common/          # DatabaseCleaner
│   ├── config/          # 테스트 설정
│   ├── fixture/         # 테스트 데이터 팩토리
│   └── support/         # 테스트 헬퍼
│
└── resources/
    ├── application-test.yml
    └── db/migration/h2/  # H2 전용 마이그레이션
```

### 8.2 테스트 유형

| 유형      | 도구                        | 용도            |
|---------|---------------------------|---------------|
| 단위 테스트  | JUnit 5                   | 개별 컴포넌트 테스트   |
| 통합 테스트  | Spring Test, REST Assured | API 엔드포인트 테스트 |
| E2E 테스트 | @Tag("e2e")               | 전체 흐름 테스트     |
| SSE 테스트 | okhttp-eventsource        | 실시간 알림 테스트    |

### 8.3 테스트 실행

```bash
# 빠른 테스트 (E2E 제외)
./gradlew fastTest

# 전체 테스트
./gradlew test

# E2E 테스트만
./gradlew e2eTest
```

### 8.4 테스트 환경

```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:database-test;MODE=MySQL;DATABASE_TO_LOWER=TRUE
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration/h2
  sql:
    init:
      mode: never
```

---

## 9. Frontend (Client)

### 9.1 기술 스택

| 카테고리       | 기술                           | 버전        |
|------------|------------------------------|-----------|
| 프레임워크      | React                        | 19.1.0    |
| 언어         | TypeScript                   | 5.8.3     |
| 라우팅        | React Router                 | 7.6.3     |
| 서버 상태      | TanStack React Query         | 5.83.0    |
| HTTP 클라이언트 | Axios                        | 1.10.0    |
| 스타일링       | Emotion                      | latest    |
| 푸시         | Firebase Web SDK             | 12.3.0    |
| 번들러        | Webpack                      | 5.100.1   |
| 테스트        | Jest + React Testing Library | latest    |
| E2E        | Cypress                      | 15.3.0    |
| 에러 추적      | Sentry                       | 9.43.0    |
| 분석         | Google Analytics 4           | react-ga4 |

### 9.2 디렉토리 구조

```
client/src/
├── app/                # 앱 초기화, 라우트 설정
├── features/           # 기능별 도메인 모듈
│   ├── auth/          # 인증
│   ├── moment/        # 모멘트
│   ├── comment/       # 댓글
│   ├── echo/          # 에코
│   ├── notification/  # 알림
│   ├── my/            # 마이페이지
│   └── complaint/     # 신고
├── pages/             # 페이지 컴포넌트
├── widgets/           # 재사용 UI 컴포넌트
├── shared/            # 공유 유틸리티
│   ├── api/          # API 통신
│   ├── hooks/        # Custom Hooks
│   ├── types/        # TypeScript 타입
│   └── utils/        # 유틸리티 함수
└── index.tsx          # 진입점
```

---

## 10. Appendix

### 10.1 API 전체 목록

#### 인증 API

| Method | Endpoint                            | 인증 | 설명           |
|--------|-------------------------------------|----|--------------|
| POST   | `/api/v1/auth/login`                | X  | 로그인          |
| POST   | `/api/v1/auth/logout`               | O  | 로그아웃         |
| GET    | `/api/v1/auth/login/google`         | X  | Google OAuth |
| GET    | `/api/v1/auth/callback/google`      | X  | OAuth 콜백     |
| GET    | `/api/v1/auth/login/check`          | X  | 로그인 확인       |
| POST   | `/api/v1/auth/refresh`              | X  | 토큰 재발급       |
| POST   | `/api/v1/auth/email`                | X  | 이메일 인증 발송    |
| POST   | `/api/v1/auth/email/verify`         | X  | 이메일 인증 확인    |
| POST   | `/api/v1/auth/email/password`       | X  | 비밀번호 재설정 링크  |
| POST   | `/api/v1/auth/email/password/reset` | X  | 비밀번호 재설정     |

#### 사용자 API

| Method | Endpoint                              | 인증 | 설명        |
|--------|---------------------------------------|----|-----------|
| POST   | `/api/v1/users/signup`                | X  | 회원가입      |
| GET    | `/api/v1/users/me`                    | O  | 프로필 조회    |
| POST   | `/api/v1/users/signup/nickname/check` | X  | 닉네임 중복 확인 |
| GET    | `/api/v1/users/signup/nickname`       | X  | 랜덤 닉네임 생성 |

#### 모멘트 API

| Method | Endpoint                         | 인증 | 설명                                         |
|--------|----------------------------------|----|--------------------------------------------|
| POST   | `/api/v1/moments`                | O  | 기본 모멘트 등록                                  |
| POST   | `/api/v1/moments/extra`          | O  | 추가 모멘트 등록                                  |
| GET    | `/api/v1/moments/me`             | O  | 내 모멘트 조회 (cursor: `nextCursor`, `limit`)   |
| GET    | `/api/v1/moments/me/unread`      | O  | 미확인 모멘트 조회 (cursor: `nextCursor`, `limit`) |
| GET    | `/api/v1/moments/writable/basic` | O  | 기본 작성 가능 여부                                |
| GET    | `/api/v1/moments/writable/extra` | O  | 추가 작성 가능 여부                                |
| GET    | `/api/v1/moments/commentable`    | O  | 댓글 가능 모멘트 조회 (`tagName` 필터)                |

#### 댓글 API

| Method | Endpoint                     | 인증 | 설명                                        |
|--------|------------------------------|----|-------------------------------------------|
| POST   | `/api/v1/comments`           | O  | 댓글 등록                                     |
| GET    | `/api/v1/comments/me`        | O  | 내 댓글 조회 (cursor: `nextCursor`, `limit`)   |
| GET    | `/api/v1/comments/me/unread` | O  | 미확인 댓글 조회 (cursor: `nextCursor`, `limit`) |

#### 에코 API

| Method | Endpoint                    | 인증 | 설명        |
|--------|-----------------------------|----|-----------|
| POST   | `/api/v1/echos`             | O  | 에코 등록     |
| GET    | `/api/v1/echos/{commentId}` | O  | 댓글별 에코 조회 |

#### 알림 API

| Method | Endpoint                          | 인증 | 설명                   |
|--------|-----------------------------------|----|----------------------|
| GET    | `/api/v1/notifications/subscribe` | O  | SSE 구독               |
| GET    | `/api/v1/notifications`           | O  | 알림 목록 조회 (`read=true |false`) |
| PATCH  | `/api/v1/notifications/{id}/read` | O  | 알림 읽음 처리             |
| PATCH  | `/api/v1/notifications/read-all`  | O  | 전체 읽음 처리             |

#### 푸시 알림 API

| Method | Endpoint                     | 인증 | 설명         |
|--------|------------------------------|----|------------|
| POST   | `/api/v1/push-notifications` | O  | 디바이스 토큰 등록 |
| DELETE | `/api/v1/push-notifications` | O  | 디바이스 토큰 삭제 |

#### 신고 API

| Method | Endpoint                        | 인증 | 설명     |
|--------|---------------------------------|----|--------|
| POST   | `/api/v1/moments/{id}/reports`  | O  | 모멘트 신고 |
| POST   | `/api/v1/comments/{id}/reports` | O  | 댓글 신고  |

#### 스토리지 API

| Method | Endpoint                     | 인증 | 설명            |
|--------|------------------------------|----|---------------|
| POST   | `/api/v1/storage/upload-url` | O  | S3 업로드 URL 발급 |

#### 마이페이지 API

| Method | Endpoint                    | 인증 | 설명                               |
|--------|-----------------------------|----|----------------------------------|
| GET    | `/api/v1/me/profile`        | O  | 마이페이지 프로필                        |
| GET    | `/api/v1/me/reward/history` | O  | 보상 기록 조회 (`pageNum`, `pageSize`) |
| POST   | `/api/v1/me/nickname`       | O  | 닉네임 변경                           |
| POST   | `/api/v1/me/password`       | O  | 비밀번호 변경                          |

#### Health Check

| Method | Endpoint  | 인증 | 설명   |
|--------|-----------|----|------|
| GET    | `/health` | X  | 헬스체크 |

### 10.2 주요 의존성

```gradle
// Core
implementation 'org.springframework.boot:spring-boot-starter-web'
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
implementation 'org.springframework.boot:spring-boot-starter-validation'
implementation 'org.springframework.boot:spring-boot-starter-aop'

// Security
implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
implementation 'org.springframework.security:spring-security-crypto'

// Database
implementation 'org.springframework.session:spring-session-jdbc'
implementation 'org.flywaydb:flyway-mysql'
runtimeOnly 'com.mysql:mysql-connector-j'

// Email
implementation 'org.springframework.boot:spring-boot-starter-mail'

// AWS
implementation 'io.awspring.cloud:spring-cloud-aws-starter-s3:3.4.0'
implementation 'io.awspring.cloud:spring-cloud-aws-starter-metrics:3.4.0'

// Firebase
implementation 'com.google.firebase:firebase-admin:9.5.0'

// Monitoring
implementation 'io.micrometer:micrometer-registry-prometheus'
implementation 'net.logstash.logback:logstash-logback-encoder:7.4'

// SQL logging
implementation 'com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.9.0'

// API Documentation
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.8'

// Admin UI
implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
implementation 'nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:3.3.0'
```

---

*Last Updated: 2026-01*
