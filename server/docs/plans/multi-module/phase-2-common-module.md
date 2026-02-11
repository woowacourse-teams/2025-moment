# Phase 2: common 모듈 코드 이동

> Created: 2026-02-11
> Status: PLANNED
> 전략: "복사 후 삭제" — Phase 2~4를 atomic 작업으로 진행
> Phase 2~4 사이에는 전체 빌드가 불가능. 모든 이동 완료 후 Phase 5에서 검증.

## 목적

엔티티, 레포지토리, 공유 인프라, 테스트 픽스쳐를 common 모듈로 이동한다.

---

## 2-1. 공유 글로벌 인프라 이동

### 대상: `src/main/java/moment/global/` → `common/src/main/java/moment/global/`

| 원본 파일 | 내용 |
|-----------|------|
| `global/domain/BaseEntity.java` | 모든 엔티티의 부모 (`@MappedSuperclass`, `@EntityListeners`) |
| `global/domain/TargetType.java` | 알림 타겟 타입 Enum |
| `global/page/Cursor.java` | 커서 페이지네이션 |
| `global/page/Cursorable.java` | 커서 인터페이스 |
| `global/page/PageSize.java` | 페이지 크기 래퍼 |
| `global/exception/ErrorCode.java` | 공통 에러 코드 Enum |
| `global/exception/MomentException.java` | 공통 예외 클래스 |
| `global/config/AppConfig.java` | PasswordEncoder, Clock 빈 |
| `global/logging/ApiLogFilter.java` | API 로그 필터 |
| `global/logging/ControllerLogAspect.java` | 컨트롤러 AOP 로깅 |
| `global/logging/NoLogging.java` | 로깅 제외 어노테이션 |
| `global/logging/RepositoryLogAspect.java` | 레포지토리 AOP 로깅 |
| `global/logging/ServiceLogAspect.java` | 서비스 AOP 로깅 |

### 명령어

```bash
# 디렉토리 구조 생성
mkdir -p common/src/main/java/moment/global/{domain,page,exception,config,logging}

# 복사
cp src/main/java/moment/global/domain/BaseEntity.java common/src/main/java/moment/global/domain/
cp src/main/java/moment/global/domain/TargetType.java common/src/main/java/moment/global/domain/
cp src/main/java/moment/global/page/Cursor.java common/src/main/java/moment/global/page/
cp src/main/java/moment/global/page/Cursorable.java common/src/main/java/moment/global/page/
cp src/main/java/moment/global/page/PageSize.java common/src/main/java/moment/global/page/
cp src/main/java/moment/global/exception/ErrorCode.java common/src/main/java/moment/global/exception/
cp src/main/java/moment/global/exception/MomentException.java common/src/main/java/moment/global/exception/
cp src/main/java/moment/global/config/AppConfig.java common/src/main/java/moment/global/config/
cp src/main/java/moment/global/logging/*.java common/src/main/java/moment/global/logging/
```

---

## 2-2. @EnableJpaAuditing 공통 설정 생성

### 생성: `common/src/main/java/moment/global/config/JpaAuditingConfig.java`

```java
package moment.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
```

### 이유

`@EnableJpaAuditing`이 `MomentApplication.java`에 있으면, api와 admin 각각의 Application 클래스에 중복 선언해야 한다.
common의 `@Configuration` 클래스에 두면 api/admin 모두에서 자동 스캔되어 하나만 관리하면 된다.

### 후속 작업 (Phase 4/5에서 처리)

- `MomentApplication.java`에서 `@EnableJpaAuditing` 제거
- `ApiApplication.java`, `AdminApplication.java`에 `@EnableJpaAuditing` 추가하지 않음

---

## 2-3. 도메인 엔티티 + 레포지토리 이동

### 각 도메인별 이동 대상

#### user

```bash
mkdir -p common/src/main/java/moment/user/{domain,infrastructure}
cp src/main/java/moment/user/domain/User.java common/src/main/java/moment/user/domain/
cp src/main/java/moment/user/domain/ProviderType.java common/src/main/java/moment/user/domain/
cp src/main/java/moment/user/domain/NicknameGenerator.java common/src/main/java/moment/user/domain/
cp src/main/java/moment/user/domain/MomentRandomNicknameGenerator.java common/src/main/java/moment/user/domain/
cp src/main/java/moment/user/domain/AlphanumericNicknameGenerator.java common/src/main/java/moment/user/domain/
cp src/main/java/moment/user/infrastructure/UserRepository.java common/src/main/java/moment/user/infrastructure/
```

#### moment

```bash
mkdir -p common/src/main/java/moment/moment/{domain,infrastructure}
cp src/main/java/moment/moment/domain/Moment.java common/src/main/java/moment/moment/domain/
cp src/main/java/moment/moment/domain/MomentCreationStatus.java common/src/main/java/moment/moment/domain/
cp src/main/java/moment/moment/domain/MomentImage.java common/src/main/java/moment/moment/domain/
cp src/main/java/moment/moment/infrastructure/MomentRepository.java common/src/main/java/moment/moment/infrastructure/
cp src/main/java/moment/moment/infrastructure/MomentImageRepository.java common/src/main/java/moment/moment/infrastructure/
```

#### comment

```bash
mkdir -p common/src/main/java/moment/comment/{domain,infrastructure}
cp src/main/java/moment/comment/domain/Comment.java common/src/main/java/moment/comment/domain/
cp src/main/java/moment/comment/domain/CommentCreationStatus.java common/src/main/java/moment/comment/domain/
cp src/main/java/moment/comment/domain/CommentImage.java common/src/main/java/moment/comment/domain/
cp src/main/java/moment/comment/infrastructure/CommentRepository.java common/src/main/java/moment/comment/infrastructure/
cp src/main/java/moment/comment/infrastructure/CommentImageRepository.java common/src/main/java/moment/comment/infrastructure/
```

#### group

```bash
mkdir -p common/src/main/java/moment/group/{domain,infrastructure}
cp src/main/java/moment/group/domain/Group.java common/src/main/java/moment/group/domain/
cp src/main/java/moment/group/domain/GroupMember.java common/src/main/java/moment/group/domain/
cp src/main/java/moment/group/domain/GroupInviteLink.java common/src/main/java/moment/group/domain/
cp src/main/java/moment/group/domain/MemberRole.java common/src/main/java/moment/group/domain/
cp src/main/java/moment/group/domain/MemberStatus.java common/src/main/java/moment/group/domain/
cp src/main/java/moment/group/infrastructure/GroupRepository.java common/src/main/java/moment/group/infrastructure/
cp src/main/java/moment/group/infrastructure/GroupMemberRepository.java common/src/main/java/moment/group/infrastructure/
cp src/main/java/moment/group/infrastructure/GroupInviteLinkRepository.java common/src/main/java/moment/group/infrastructure/
```

#### like

```bash
mkdir -p common/src/main/java/moment/like/{domain,infrastructure}
cp src/main/java/moment/like/domain/MomentLike.java common/src/main/java/moment/like/domain/
cp src/main/java/moment/like/domain/CommentLike.java common/src/main/java/moment/like/domain/
cp src/main/java/moment/like/infrastructure/MomentLikeRepository.java common/src/main/java/moment/like/infrastructure/
cp src/main/java/moment/like/infrastructure/CommentLikeRepository.java common/src/main/java/moment/like/infrastructure/
```

#### notification

```bash
mkdir -p common/src/main/java/moment/notification/{domain,infrastructure,infrastructure/expo}
cp src/main/java/moment/notification/domain/*.java common/src/main/java/moment/notification/domain/
cp src/main/java/moment/notification/infrastructure/NotificationRepository.java common/src/main/java/moment/notification/infrastructure/
cp src/main/java/moment/notification/infrastructure/PushNotificationRepository.java common/src/main/java/moment/notification/infrastructure/
cp src/main/java/moment/notification/infrastructure/SourceDataConverter.java common/src/main/java/moment/notification/infrastructure/
cp src/main/java/moment/notification/infrastructure/Emitters.java common/src/main/java/moment/notification/infrastructure/
cp src/main/java/moment/notification/infrastructure/expo/*.java common/src/main/java/moment/notification/infrastructure/expo/
```

> **참고**: notification의 infrastructure에는 Expo push 관련 클래스가 있다.
> Expo push는 api에서만 사용하지만, `PushNotificationSender` 인터페이스가 domain에 있으므로
> 구현체(`ExpoPushNotificationSender` 등)도 common에 두거나, 인터페이스만 common에 두고 구현체는 api에 둘 수 있다.
> **결정**: 현재 단계에서는 전체를 common에 이동. 추후 api로 분리 가능.

#### report

```bash
mkdir -p common/src/main/java/moment/report/{domain,infrastructure}
cp src/main/java/moment/report/domain/Report.java common/src/main/java/moment/report/domain/
cp src/main/java/moment/report/domain/ReportReason.java common/src/main/java/moment/report/domain/
cp src/main/java/moment/report/infrastructure/ReportRepository.java common/src/main/java/moment/report/infrastructure/
```

#### block

```bash
mkdir -p common/src/main/java/moment/block/{domain,infrastructure}
cp src/main/java/moment/block/domain/UserBlock.java common/src/main/java/moment/block/domain/
cp src/main/java/moment/block/infrastructure/UserBlockRepository.java common/src/main/java/moment/block/infrastructure/
```

#### auth (레포지토리만 — JPA 엔티티 관련)

```bash
mkdir -p common/src/main/java/moment/auth/{domain,infrastructure}
cp src/main/java/moment/auth/domain/EmailVerification.java common/src/main/java/moment/auth/domain/
cp src/main/java/moment/auth/domain/RefreshToken.java common/src/main/java/moment/auth/domain/
cp src/main/java/moment/auth/domain/Tokens.java common/src/main/java/moment/auth/domain/
cp src/main/java/moment/auth/infrastructure/RefreshTokenRepository.java common/src/main/java/moment/auth/infrastructure/
```

> **주의**: `EmailVerificationRepository`는 코드에 존재하지 않음.
> `EmailVerification`은 `AuthEmailService`에서 직접 관리.
> `JwtTokenManager`, `AppleAuthClient`, `GoogleAuthClient`는 **api 전용** → Phase 4에서 api로 이동.

#### admin (엔티티 + 레포지토리만)

```bash
mkdir -p common/src/main/java/moment/admin/{domain,infrastructure}
cp src/main/java/moment/admin/domain/Admin.java common/src/main/java/moment/admin/domain/
cp src/main/java/moment/admin/domain/AdminGroupLog.java common/src/main/java/moment/admin/domain/
cp src/main/java/moment/admin/domain/AdminGroupLogType.java common/src/main/java/moment/admin/domain/
cp src/main/java/moment/admin/domain/AdminRole.java common/src/main/java/moment/admin/domain/
cp src/main/java/moment/admin/domain/AdminSession.java common/src/main/java/moment/admin/domain/
cp src/main/java/moment/admin/domain/GroupStatusFilter.java common/src/main/java/moment/admin/domain/
cp src/main/java/moment/admin/infrastructure/AdminRepository.java common/src/main/java/moment/admin/infrastructure/
cp src/main/java/moment/admin/infrastructure/AdminSessionRepository.java common/src/main/java/moment/admin/infrastructure/
cp src/main/java/moment/admin/infrastructure/AdminGroupLogRepository.java common/src/main/java/moment/admin/infrastructure/
```

---

## 2-4. Flyway 마이그레이션 이동

### MySQL 마이그레이션

```bash
mkdir -p common/src/main/resources/db/migration/mysql
cp src/main/resources/db/migration/mysql/V*.sql common/src/main/resources/db/migration/mysql/
```

**총 파일 수**: V1 ~ V38 (약 36개 파일)

### V35 H2 파일 정리

현재 `src/main/resources/db/migration/h2/V35__create_admin_group_logs__h2.sql`이 main/resources에 있음.
이 파일은 Phase 2-5에서 testFixtures/resources로 함께 이동.

---

## 2-5. 테스트 픽스쳐 이동

### 대상: `src/test/java/moment/` → `common/src/testFixtures/java/moment/`

```bash
# 디렉토리 생성
mkdir -p common/src/testFixtures/java/moment/{fixture,common,config,support}

# 엔티티 픽스쳐 (Phase 0에서 DTO 메서드 분리 완료된 상태)
cp src/test/java/moment/fixture/UserFixture.java common/src/testFixtures/java/moment/fixture/
cp src/test/java/moment/fixture/AdminFixture.java common/src/testFixtures/java/moment/fixture/
cp src/test/java/moment/fixture/MomentFixture.java common/src/testFixtures/java/moment/fixture/
cp src/test/java/moment/fixture/CommentFixture.java common/src/testFixtures/java/moment/fixture/
cp src/test/java/moment/fixture/GroupFixture.java common/src/testFixtures/java/moment/fixture/
cp src/test/java/moment/fixture/GroupMemberFixture.java common/src/testFixtures/java/moment/fixture/
cp src/test/java/moment/fixture/GroupInviteLinkFixture.java common/src/testFixtures/java/moment/fixture/
cp src/test/java/moment/fixture/MomentLikeFixture.java common/src/testFixtures/java/moment/fixture/
cp src/test/java/moment/fixture/CommentLikeFixture.java common/src/testFixtures/java/moment/fixture/
cp src/test/java/moment/fixture/UserBlockFixture.java common/src/testFixtures/java/moment/fixture/

# 공통 테스트 유틸
cp src/test/java/moment/common/DatabaseCleaner.java common/src/testFixtures/java/moment/common/
cp src/test/java/moment/config/TestTags.java common/src/testFixtures/java/moment/config/
cp src/test/java/moment/support/MomentCreatedAtHelper.java common/src/testFixtures/java/moment/support/
cp src/test/java/moment/support/CommentCreatedAtHelper.java common/src/testFixtures/java/moment/support/
```

### H2 테스트 마이그레이션 이동

```bash
mkdir -p common/src/testFixtures/resources/db/migration/h2

# test/resources에서 복사
cp src/test/resources/db/migration/h2/V*.sql common/src/testFixtures/resources/db/migration/h2/

# main/resources에 있는 V35 H2 파일도 복사
cp src/main/resources/db/migration/h2/V35__create_admin_group_logs__h2.sql common/src/testFixtures/resources/db/migration/h2/
```

**총 파일 수**: V1 ~ V38 H2 마이그레이션 약 36개

---

## 2-6. Phase 2 이동 파일 요약

### common/src/main/java/moment/ (약 62개 파일)

| 카테고리 | 패키지 | 파일 수 |
|----------|--------|---------|
| 글로벌 인프라 | global/{domain,page,exception,config,logging} | 13 |
| JPA Auditing | global/config/JpaAuditingConfig.java | 1 (신규) |
| user 도메인 | user/{domain,infrastructure} | 6 |
| moment 도메인 | moment/{domain,infrastructure} | 5 |
| comment 도메인 | comment/{domain,infrastructure} | 5 |
| group 도메인 | group/{domain,infrastructure} | 8 |
| like 도메인 | like/{domain,infrastructure} | 4 |
| notification 도메인 | notification/{domain,infrastructure} | ~13 |
| report 도메인 | report/{domain,infrastructure} | 3 |
| block 도메인 | block/{domain,infrastructure} | 2 |
| auth 도메인 | auth/{domain,infrastructure} | 4 |
| admin 도메인 | admin/{domain,infrastructure} | 9 |

### common/src/main/resources/

| 카테고리 | 파일 수 |
|----------|---------|
| db/migration/mysql/ | ~36 |

### common/src/testFixtures/java/moment/ (약 14개 파일)

| 카테고리 | 파일 수 |
|----------|---------|
| fixture/ | 10 |
| common/ | 1 |
| config/ | 1 |
| support/ | 2 |

### common/src/testFixtures/resources/

| 카테고리 | 파일 수 |
|----------|---------|
| db/migration/h2/ | ~36 |

---

## 참고: 원본 삭제는 Phase 5에서 수행

Phase 2에서는 **복사만** 한다. `src/` 원본은 Phase 2~4 모두 완료 후 Phase 5에서 일괄 삭제.
