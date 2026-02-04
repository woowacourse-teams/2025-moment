# Apple 로그인 이메일 개선 Spec

## 문제

Apple 로그인 시 `sub` 클레임을 이용해 `{sub}@apple.user` 형태의 이메일을 생성하는데, Apple `sub`이 ~44자로 매우 길어 UI가 깨지는 문제.

## 해결 방향

1. Apple Identity Token에 `email` 클레임이 있으면 → 해당 이메일을 그대로 사용
2. `email`이 없으면 → 짧은 임의 이메일 생성: `apple_{8자hash}@apple.app`
3. 사용자 조회를 `socialId`(= Apple `sub`) 기반으로 변경
4. **기존 사용자 이메일은 변경하지 않음** → 다음 로그인 시 Apple이 이메일을 제공하면 업데이트

## 핵심 설계

### 왜 `socialId` 필드가 필요한가?

현재 사용자 식별이 `email + providerType`으로 이루어지는데, 이메일을 변경하면 기존 사용자를 찾을 수 없다. Apple은 `sub`을 항상 제공하지만 `email`은 최초 인증 시에만 제공할 수 있으므로, `sub`을 별도 필드에 저장하고 이를 조회 키로 사용해야 한다.

### DB 제약조건 현황

- `users` 테이블: `UNIQUE (email, provider_type)` (V2 마이그레이션에서 설정)
- JPA 엔티티의 `@Column(unique = true)`는 실제 DDL에 반영되지 않음 (Flyway 관리)

---

## 수정 대상 파일

### 1. Flyway 마이그레이션 (신규)

**MySQL**: `server/src/main/resources/db/migration/mysql/V36__add_social_id_to_users.sql`

```sql
ALTER TABLE users ADD COLUMN social_id VARCHAR(255) DEFAULT NULL;

-- 기존 Apple 사용자: email에서 sub 추출하여 social_id 채움
UPDATE users
SET social_id = SUBSTRING_INDEX(email, '@apple.user', 1)
WHERE provider_type = 'APPLE' AND email LIKE '%@apple.user';

-- 기존 사용자 이메일은 변경하지 않음 (다음 로그인 시 업데이트)

CREATE INDEX idx_users_social_id_provider ON users (social_id, provider_type);
```

**H2**: `server/src/test/resources/db/migration/h2/V36__add_social_id_to_users__h2.sql`

```sql
ALTER TABLE users ADD COLUMN social_id VARCHAR(255) DEFAULT NULL;
CREATE INDEX IF NOT EXISTS idx_users_social_id_provider ON users (social_id, provider_type);
```

### 2. `AppleUserInfo` (DTO 수정)

**파일**: `server/src/main/java/moment/auth/dto/apple/AppleUserInfo.java`

- `email` 필드 추가 (nullable)
- `toAppleEmail()` 제거 → `resolveDisplayEmail()` 추가
  - email이 있으면 그대로 반환
  - 없으면 `apple_{sub의 MD5 해시 앞 8자}@apple.app` 생성

### 3. `AppleAuthClient` (인프라 수정)

**파일**: `server/src/main/java/moment/auth/infrastructure/AppleAuthClient.java`

- `verifyAndGetUserInfo()` 64번째 줄 수정
- `claims.get("email", String.class)` 추출하여 `AppleUserInfo(sub, email)` 생성

### 4. `User` (엔티티 수정)

**파일**: `server/src/main/java/moment/user/domain/User.java`

- `socialId` 필드 추가: `@Column(name = "social_id") private String socialId;`
- `socialId`를 받는 5인자 생성자 추가
- 기존 4인자 생성자 유지 (EMAIL 가입용)
- `email` 필드의 `@Column(unique = true)` → `@Column()` 수정
- `updateEmail(String newEmail)` 메서드 추가

### 5. `UserRepository` (리포지토리 수정)

**파일**: `server/src/main/java/moment/user/infrastructure/UserRepository.java`

- `findBySocialIdAndProviderType(String socialId, ProviderType providerType)` 추가

### 6. `AppleAuthService` (서비스 핵심 로직 수정)

**파일**: `server/src/main/java/moment/auth/application/AppleAuthService.java`

변경된 `loginOrSignUp()` 흐름:
1. Identity Token에서 `sub` + `email` 추출
2. `findBySocialIdAndProviderType(sub, APPLE)`로 기존 사용자 조회
3. 기존 사용자 발견:
   - Apple이 이메일을 제공했고, 현재 이메일이 `@apple.user`로 끝나면 → 이메일 업데이트
   - 토큰 발급
4. 신규 사용자:
   - `resolveDisplayEmail()`로 이메일 결정
   - `socialId=sub`으로 생성 후 토큰 발급

---

## 테스트 계획 (TDD)

### AppleUserInfo 테스트
- `email이_있으면_해당_이메일을_반환한다`
- `email이_null이면_짧은_이메일을_생성한다`
- `email이_빈_문자열이면_짧은_이메일을_생성한다`
- `생성된_이메일은_유효한_이메일_형식이다`
- `동일한_sub에_대해_항상_같은_이메일을_생성한다`

### AppleAuthClient 테스트
- `토큰에_email_클레임이_있으면_email을_포함한_사용자_정보를_반환한다`
- `토큰에_email_클레임이_없으면_email이_null인_사용자_정보를_반환한다`

### AppleAuthService 테스트
- `기존_사용자면_socialId로_조회하여_토큰을_발급한다`
- `신규_사용자에_Apple_이메일이_있으면_해당_이메일로_생성한다`
- `신규_사용자에_Apple_이메일이_없으면_짧은_이메일로_생성한다`
- `신규_사용자_생성_시_socialId가_sub으로_설정된다`
- `동일_sub로_재로그인하면_같은_사용자로_인식한다`
- `기존_사용자의_이메일이_apple_user이고_Apple이_실제_이메일을_제공하면_업데이트한다`

### User 엔티티 테스트
- `socialId를_포함하여_User를_생성한다`
- `socialId가_null이어도_User를_생성할_수_있다`

---

## 구현 순서

| 단계 | 유형 | 내용 |
|------|------|------|
| 1 | Tidy | User 엔티티 `@Column(unique=true)` → `@Column()` 수정 |
| 2 | Red | AppleUserInfo 새 테스트 작성 |
| 3 | Green | AppleUserInfo 구현 (email 필드 + resolveDisplayEmail) |
| 4 | Red | User 엔티티 socialId 테스트 작성 |
| 5 | Green | User 엔티티 socialId 필드 + 생성자 + updateEmail 추가 |
| 6 | Red | AppleAuthClient email 추출 테스트 |
| 7 | Green | AppleAuthClient 수정 |
| 8 | Green | UserRepository 메서드 추가 |
| 9 | Red | AppleAuthService 새 로직 테스트 |
| 10 | Green | AppleAuthService 수정 |
| 11 | Green | Flyway 마이그레이션 V36 생성 (MySQL + H2) |
| 12 | Verify | `./gradlew fastTest` 전체 테스트 통과 확인 |

---

## 검증 방법

1. `./gradlew fastTest` - 전체 단위 + 통합 테스트 통과
2. `./gradlew e2eTest` - E2E 테스트 통과
3. H2 환경에서 Flyway 마이그레이션 정상 적용 확인