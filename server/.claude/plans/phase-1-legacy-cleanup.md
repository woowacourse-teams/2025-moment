# Phase 1: 레거시 코드 정리 (Day 1-2)

## 개요
- **목표**: Echo, Tag, Reward, Policy, WriteType, Level 시스템 제거
- **원칙**: 역순 의존성 제거 (Presentation → Facade → Application → Domain → Entity → Repository)
- **검증**: 각 Step 후 `./gradlew compileJava` 실행

---

## Step 1: Presentation Layer 정리

### 1.1 Controller 수정

#### `moment/presentation/MomentController.java`
```
- writeType 파라미터 제거 (있다면)
- Tag 관련 파라미터 제거
```

#### `comment/presentation/EchoController.java` (있다면)
```
- 파일 전체 삭제
```

### 1.2 TDD 테스트 케이스
```java
// 테스트: MomentController에서 writeType 없이 모멘트 생성 가능
@Test
void 모멘트_생성_writeType_없이_성공() {
    // Given: writeType 필드 없는 요청
    // When: POST /api/v1/moments
    // Then: 201 Created
}
```

### 1.3 검증
```bash
./gradlew compileJava
```

---

## Step 2: Facade 서비스에서 Reward 호출 제거

### 2.1 대상 파일

#### `moment/service/facade/MomentCreateFacadeService.java`
```java
// 제거 대상
- rewardApplicationService.rewardForMoment() 호출 (약 Line 24)
- rewardApplicationService.useReward() 호출 (약 Line 31)
- RewardApplicationService 필드 및 import
```

#### `comment/service/facade/CommentCreateFacadeService.java`
```java
// 제거 대상
- rewardApplicationService.rewardForComment() 호출 (약 Line 34)
- RewardApplicationService 필드 및 import
```

#### `comment/service/facade/EchoCreateFacadeService.java`
```java
// 파일 전체 삭제 (echo 제거)
```

### 2.2 TDD 테스트 케이스
```java
// 테스트: MomentCreateFacadeService가 reward 없이 동작
@Test
void 모멘트_생성시_보상_처리_없음() {
    // Given: 사용자
    // When: createMoment 호출
    // Then: 모멘트 생성 성공, Reward 관련 호출 없음
}

// 테스트: CommentCreateFacadeService가 reward 없이 동작
@Test
void 댓글_생성시_보상_처리_없음() {
    // Given: 모멘트와 사용자
    // When: createComment 호출
    // Then: 댓글 생성 성공, Reward 관련 호출 없음
}
```

### 2.3 검증
```bash
./gradlew compileJava
```

---

## Step 3: Application 서비스에서 Echo/Tag/Policy 의존성 제거

### 3.1 대상 파일

#### `moment/service/application/MomentApplicationService.java`
```java
// Tag 관련 제거
- Line 64: tagService.getOrCreate() 제거
- Line 66: momentTagService.createAll() 제거
- Line 129: momentTagService.getMomentTagsByMoment() 제거
- Line 185: momentTagService.getMomentIdsByTags() 제거
- Line 202: momentTagService.deleteBy() 제거
- TagService, MomentTagService 필드 및 import 제거

// Policy 관련 제거
- createBasicMoment()의 OnceADayPolicy 검사 제거
- createExtraMoment()의 PointDeductionPolicy 검사 제거
- OnceADayPolicy, PointDeductionPolicy 필드 및 import 제거

// WriteType 관련 제거
- WriteType.BASIC, WriteType.EXTRA 사용 코드 제거
- WriteType import 제거
```

#### `comment/service/application/CommentApplicationService.java`
```java
// Echo 관련 제거
- Line 41, 55, 141: echoService.getEchosOfComments() 제거
- Line 96: echoService.deleteBy() 제거
- Line 190: echoService.saveIfNotExisted() 제거
- Line 196-198: getEchosBy() 메서드 전체 제거
- EchoService 필드 및 import 제거
```

### 3.2 TDD 테스트 케이스
```java
// 테스트: MomentApplicationService가 Tag 없이 동작
@Test
void 모멘트_생성시_태그_처리_없음() {
    // Given: 사용자
    // When: createMoment 호출
    // Then: 모멘트 생성 성공, Tag 관련 호출 없음
}

// 테스트: MomentApplicationService가 Policy 없이 동작
@Test
void 모멘트_생성시_정책_검사_없음() {
    // Given: 오늘 이미 모멘트를 작성한 사용자
    // When: createMoment 호출
    // Then: 모멘트 생성 성공 (OnceADay 제한 없음)
}

// 테스트: CommentApplicationService가 Echo 없이 동작
@Test
void 댓글_조회시_에코_정보_없음() {
    // Given: 댓글이 있는 모멘트
    // When: getComments 호출
    // Then: 댓글 목록 반환, Echo 정보 없음
}
```

### 3.3 검증
```bash
./gradlew compileJava
```

---

## Step 4: Event Handler에서 Echo 이벤트 제거

### 4.1 대상 파일

#### `notification/service/eventHandler/NotificationEventHandler.java`
```java
// 제거 대상
- Line 33-41: handleEchoCreateEvent() 메서드 전체 제거
- EchoCreateEvent import 제거
```

### 4.2 TDD 테스트 케이스
```java
// 테스트: EchoCreateEvent 핸들러가 없어도 알림 시스템 정상 동작
@Test
void 댓글_생성시_알림_정상_발송() {
    // Given: 모멘트 작성자와 다른 사용자
    // When: 댓글 생성 이벤트 발행
    // Then: NEW_COMMENT 알림 발송
}
```

### 4.3 검증
```bash
./gradlew compileJava
```

---

## Step 5: Domain 서비스에서 Reward 의존성 제거

### 5.1 대상 파일

#### `user/service/user/UserService.java`
```java
// 제거 대상 (약 Line 89-93)
- 닉네임 변경 시 포인트 차감 로직 제거
- Reason enum import 제거
- 관련 validation 로직 제거
```

### 5.2 TDD 테스트 케이스
```java
// 테스트: 닉네임 변경 시 포인트 차감 없음
@Test
void 닉네임_변경시_포인트_차감_없음() {
    // Given: 사용자
    // When: changeNickname 호출
    // Then: 닉네임 변경 성공, 포인트 차감 없음
}
```

### 5.3 검증
```bash
./gradlew compileJava
```

---

## Step 6: Entity에서 star/level/writeType 필드 제거

### 6.1 대상 파일

#### `user/domain/User.java`
```java
// 필드 제거
- private int availableStar;
- private int expStar;
- private Level level;

// 메서드 제거
- addStarAndUpdateLevel()
- canNotUseStars()
- 관련 getter/setter

// Import 제거
- Level enum import
```

#### `moment/domain/Moment.java`
```java
// 필드 제거
- private WriteType writeType;
- private boolean isMatched;  (있다면)

// Import 제거
- WriteType enum import
```

### 6.2 TDD 테스트 케이스
```java
// 테스트: User 생성 시 star/level 필드 없음
@Test
void User_생성시_star_level_필드_없음() {
    // Given/When: User 생성
    // Then: star, level 필드 없이 생성 성공
}

// 테스트: Moment 생성 시 writeType 필드 없음
@Test
void Moment_생성시_writeType_필드_없음() {
    // Given/When: Moment 생성
    // Then: writeType 없이 생성 성공
}
```

### 6.3 검증
```bash
./gradlew compileJava
```

---

## Step 7: DTO 정리

### 7.1 대상 파일

#### `user/dto/response/MyPageProfileResponse.java`
```java
// 필드 제거
- availableStar
- expStar
- level
- from() 메서드에서 해당 필드 매핑 제거
```

#### `moment/dto/request/MomentCreateRequest.java`
```java
// 필드 제거
- tagNames (List<String>)
- 관련 validation 제거
```

#### `moment/dto/response/MomentCreateResponse.java`
```java
// 필드 제거
- tags (List<String>)
- writeType
```

#### `comment/dto/tobe/CommentComposition.java`
```java
// 필드 제거
- echo 관련 필드
```

#### `moment/dto/response/MyMomentCommentResponse.java`
```java
// 필드 제거
- echo 관련 필드
```

### 7.2 TDD 테스트 케이스
```java
// 테스트: MyPageProfileResponse에 star/level 없음
@Test
void MyPageProfileResponse_star_level_없음() {
    // Given: User
    // When: MyPageProfileResponse.from(user)
    // Then: star, level 필드 없이 생성
}

// 테스트: MomentCreateRequest에 tagNames 없음
@Test
void MomentCreateRequest_태그_없음() {
    // Given: content만 있는 요청
    // When: MomentCreateRequest 생성
    // Then: 유효한 요청
}
```

### 7.3 검증
```bash
./gradlew compileJava
```

---

## Step 8: Repository 정리

### 8.1 대상 파일

#### `moment/infrastructure/MomentRepository.java`
```java
// 제거 대상
- writeType 관련 쿼리 메서드
- findByMomenterAndWriteType()
- countByMomenterAndWriteTypeAndCreatedAtBetween()
- 등
```

### 8.2 검증
```bash
./gradlew compileJava
```

---

## Step 9: Admin 정리

### 9.1 대상 파일

#### `admin/service/user/AdminUserService.java`
```java
// 제거 대상
- star/level 조회/수정 관련 로직
```

#### `resources/templates/admin/users/list.html`
```html
<!-- 제거 대상 -->
- star/level 표시 컬럼
```

### 9.2 검증
```bash
./gradlew compileJava
```

---

## Step 10: ErrorCode 정리

### 10.1 대상 파일

#### `global/exception/ErrorCode.java`
```java
// 삭제 대상 코드
ECHO_NOT_FOUND,
ECHO_ALREADY_EXISTS,
// ... ECHO_* 전체

TAG_NOT_FOUND,
TAG_ALREADY_EXISTS,
// ... TAG_* 전체

INSUFFICIENT_STAR,
STAR_NOT_ENOUGH,
// ... STAR_* 전체

LEVEL_*,  // 레벨 관련 전체

ALREADY_CREATED_TODAY,  // OnceADayPolicy
```

### 10.2 검증
```bash
./gradlew compileJava
```

---

## Step 11: 파일 삭제

### 11.1 Echo 시스템 (5개)
```bash
rm src/main/java/moment/comment/domain/Echo.java
rm src/main/java/moment/comment/service/comment/EchoService.java
rm src/main/java/moment/comment/service/facade/EchoCreateFacadeService.java
rm src/main/java/moment/comment/infrastructure/EchoRepository.java
rm src/main/java/moment/comment/dto/EchoCreateEvent.java
```

### 11.2 Tag 시스템 (6개)
```bash
rm src/main/java/moment/moment/domain/Tag.java
rm src/main/java/moment/moment/domain/MomentTag.java
rm src/main/java/moment/moment/service/moment/TagService.java
rm src/main/java/moment/moment/service/moment/MomentTagService.java
rm src/main/java/moment/moment/infrastructure/TagRepository.java
rm src/main/java/moment/moment/infrastructure/MomentTagRepository.java
```

### 11.3 Reward 시스템 (디렉토리 전체)
```bash
rm -rf src/main/java/moment/reward/
```

### 11.4 Policy (3-4개)
```bash
rm src/main/java/moment/moment/domain/OnceADayPolicy.java
rm src/main/java/moment/moment/domain/PointDeductionPolicy.java
rm src/main/java/moment/moment/domain/BasicMomentCreatePolicy.java
rm src/main/java/moment/moment/domain/ExtraMomentCreatePolicy.java
# (인터페이스 포함, 파일 존재 확인 후 삭제)
```

### 11.5 WriteType, Level
```bash
rm src/main/java/moment/moment/domain/WriteType.java
rm src/main/java/moment/user/domain/Level.java
```

### 11.6 검증
```bash
./gradlew compileJava
```

---

## Step 12: 테스트 정리

### 12.1 삭제 대상 테스트 파일
```bash
# Echo 관련 테스트
rm src/test/java/moment/comment/**/Echo*Test.java

# Tag 관련 테스트
rm src/test/java/moment/moment/**/Tag*Test.java
rm src/test/java/moment/moment/**/MomentTag*Test.java

# Reward 관련 테스트
rm -rf src/test/java/moment/reward/

# Policy 관련 테스트
rm src/test/java/moment/moment/**/*Policy*Test.java

# Level 관련 테스트
rm src/test/java/moment/user/**/Level*Test.java
```

### 12.2 수정 대상 테스트 파일
```java
// Moment 생성 테스트: writeType, tag 관련 assertion 제거
// Comment 생성 테스트: echo 관련 assertion 제거
// User 테스트: star/level 관련 assertion 제거
// Admin 테스트: star/level 관련 테스트 제거
```

### 12.3 검증
```bash
./gradlew fastTest
./gradlew test
```

---

## Step 13: DB 마이그레이션

### 13.1 MySQL 마이그레이션

#### `src/main/resources/db/migration/mysql/V25__soft_delete_legacy_data.sql`
```sql
-- 기존 콘텐츠 soft delete
UPDATE moments SET deleted_at = NOW() WHERE deleted_at IS NULL;
UPDATE comments SET deleted_at = NOW() WHERE deleted_at IS NULL;
UPDATE echos SET deleted_at = NOW() WHERE deleted_at IS NULL;
UPDATE moment_tags SET deleted_at = NOW() WHERE deleted_at IS NULL;
UPDATE tags SET deleted_at = NOW() WHERE deleted_at IS NULL;
UPDATE moment_images SET deleted_at = NOW() WHERE deleted_at IS NULL;
UPDATE comment_images SET deleted_at = NOW() WHERE deleted_at IS NULL;
UPDATE notifications SET deleted_at = NOW() WHERE deleted_at IS NULL;
UPDATE reward_history SET deleted_at = NOW() WHERE deleted_at IS NULL;

-- User의 star/level 초기화 (컬럼 삭제 전)
UPDATE users SET available_star = 0, exp_star = 0, level = 'ASTEROID_WHITE';
```

#### `src/main/resources/db/migration/mysql/V26__remove_legacy_columns.sql`
```sql
-- User 테이블에서 star/level 컬럼 제거
ALTER TABLE users DROP COLUMN available_star;
ALTER TABLE users DROP COLUMN exp_star;
ALTER TABLE users DROP COLUMN level;

-- Moment 테이블에서 write_type 제거
ALTER TABLE moments DROP COLUMN write_type;
ALTER TABLE moments DROP COLUMN is_matched;
```

### 13.2 H2 마이그레이션 (테스트용)

#### `src/test/resources/db/migration/h2/V25__soft_delete_legacy_data.sql`
```sql
-- MySQL과 동일 (H2 문법 호환)
UPDATE moments SET deleted_at = CURRENT_TIMESTAMP WHERE deleted_at IS NULL;
UPDATE comments SET deleted_at = CURRENT_TIMESTAMP WHERE deleted_at IS NULL;
UPDATE echos SET deleted_at = CURRENT_TIMESTAMP WHERE deleted_at IS NULL;
UPDATE moment_tags SET deleted_at = CURRENT_TIMESTAMP WHERE deleted_at IS NULL;
UPDATE tags SET deleted_at = CURRENT_TIMESTAMP WHERE deleted_at IS NULL;
UPDATE moment_images SET deleted_at = CURRENT_TIMESTAMP WHERE deleted_at IS NULL;
UPDATE comment_images SET deleted_at = CURRENT_TIMESTAMP WHERE deleted_at IS NULL;
UPDATE notifications SET deleted_at = CURRENT_TIMESTAMP WHERE deleted_at IS NULL;
UPDATE reward_history SET deleted_at = CURRENT_TIMESTAMP WHERE deleted_at IS NULL;

UPDATE users SET available_star = 0, exp_star = 0, level = 'ASTEROID_WHITE';
```

#### `src/test/resources/db/migration/h2/V26__remove_legacy_columns.sql`
```sql
ALTER TABLE users DROP COLUMN available_star;
ALTER TABLE users DROP COLUMN exp_star;
ALTER TABLE users DROP COLUMN level;

ALTER TABLE moments DROP COLUMN write_type;
ALTER TABLE moments DROP COLUMN is_matched;
```

### 13.3 검증
```bash
./gradlew test  # Flyway 마이그레이션 포함 테스트
```

---

## 최종 검증

```bash
# 1. 컴파일 확인
./gradlew compileJava

# 2. 단위 테스트
./gradlew fastTest

# 3. 전체 테스트 (마이그레이션 포함)
./gradlew test

# 4. 빌드
./gradlew build
```

---

## 체크리스트

### 코드 제거 완료
- [ ] Echo 시스템 (5개 파일)
- [ ] Tag 시스템 (6개 파일)
- [ ] Reward 시스템 (전체 디렉토리)
- [ ] Policy 파일 (3-4개)
- [ ] WriteType enum
- [ ] Level enum

### 의존성 제거 완료
- [ ] MomentCreateFacadeService → Reward 제거
- [ ] CommentCreateFacadeService → Reward 제거
- [ ] MomentApplicationService → Tag, Policy 제거
- [ ] CommentApplicationService → Echo 제거
- [ ] NotificationEventHandler → EchoCreateEvent 제거
- [ ] UserService → Reward 제거
- [ ] User 엔티티 → star/level 제거
- [ ] Moment 엔티티 → writeType 제거

### DTO 정리 완료
- [ ] MyPageProfileResponse → star/level 제거
- [ ] MomentCreateRequest → tagNames 제거
- [ ] MomentCreateResponse → tags, writeType 제거
- [ ] CommentComposition → echo 제거

### ErrorCode 정리 완료
- [ ] ECHO_* 코드 삭제
- [ ] TAG_* 코드 삭제
- [ ] STAR_*, LEVEL_* 코드 삭제
- [ ] ALREADY_CREATED_TODAY 삭제

### 테스트 정리 완료
- [ ] Echo 관련 테스트 삭제
- [ ] Tag 관련 테스트 삭제
- [ ] Reward 관련 테스트 삭제
- [ ] Policy 관련 테스트 삭제
- [ ] Level 관련 테스트 삭제

### DB 마이그레이션 완료
- [ ] V25: 레거시 데이터 soft delete (MySQL)
- [ ] V25: 레거시 데이터 soft delete (H2)
- [ ] V26: 레거시 컬럼 제거 (MySQL)
- [ ] V26: 레거시 컬럼 제거 (H2)

### 최종 검증
- [ ] `./gradlew compileJava` 성공
- [ ] `./gradlew fastTest` 성공
- [ ] `./gradlew test` 성공
- [ ] `./gradlew build` 성공

---

## 롤백 전략

### 코드 롤백
```bash
git checkout HEAD~1 -- .
```

### DB 롤백 (soft delete만 사용했으므로)
```sql
-- 데이터 복구
UPDATE moments SET deleted_at = NULL WHERE deleted_at IS NOT NULL;
UPDATE comments SET deleted_at = NULL WHERE deleted_at IS NOT NULL;
-- ... 기타 테이블

-- 참고: V26 (컬럼 삭제)은 롤백 불가, 새 마이그레이션 필요
```
