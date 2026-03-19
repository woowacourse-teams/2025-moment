# Phase 0: 선행 작업 (ArchUnit 도입 전 필수)

> 기반 문서: `docs/arch/archunit-implementation-plan.md`
> 상태: 구현 대기
> 목표: ArchUnit 규칙에 예외 없이 통과할 수 있도록 코드베이스 표준화

---

## 개요

ArchUnit 도입 전 반드시 완료해야 하는 4가지 선행 작업:

| Step | 작업 | 변경 파일 수 | 위험도 |
|------|------|------------|--------|
| 0-1 | like 모듈 패키지 표준화 | ~12개 | 중 (import 변경 다수) |
| 0-2 | group/invite 서비스 위치 표준화 | ~6개 | 낮음 |
| 0-3 | Gradle ArchUnit 의존성 추가 | 1개 | 낮음 |
| 0-4 | TestTags에 ARCHITECTURE 태그 추가 | 1개 | 낮음 |

**완료 기준**: `./gradlew test` 전체 통과

---

## Step 0-1: like 모듈 패키지 구조 표준화

### 배경

프로젝트의 서비스 패키지 컨벤션은 `service/{domain}/`이지만, like 모듈만 서비스가 `service/` 직접 하위에 위치.

### 현재 구조

```
api/src/main/java/moment/like/
├── dto/
│   ├── event/
│   │   ├── CommentLikeEvent.java
│   │   └── MomentLikeEvent.java
│   └── response/
│       └── LikeToggleResponse.java
└── service/
    ├── MomentLikeService.java     ← 비표준 위치
    └── CommentLikeService.java    ← 비표준 위치

api/src/test/java/moment/like/
├── domain/
│   ├── MomentLikeTest.java
│   └── CommentLikeTest.java
└── service/
    ├── MomentLikeServiceTest.java  ← 비표준 위치
    └── CommentLikeServiceTest.java ← 비표준 위치
```

### 변경 후 구조

```
api/src/main/java/moment/like/
├── dto/                            (변경 없음)
└── service/
    └── like/                       ← 새 패키지
        ├── MomentLikeService.java
        └── CommentLikeService.java

api/src/test/java/moment/like/
├── domain/                         (변경 없음)
└── service/
    └── like/                       ← 새 패키지
        ├── MomentLikeServiceTest.java
        └── CommentLikeServiceTest.java
```

### 작업 순서

#### 1단계: 패키지 생성 및 파일 이동 (main)

```bash
# 패키지 디렉토리 생성
mkdir -p api/src/main/java/moment/like/service/like

# 서비스 파일 이동
git mv api/src/main/java/moment/like/service/MomentLikeService.java \
       api/src/main/java/moment/like/service/like/MomentLikeService.java
git mv api/src/main/java/moment/like/service/CommentLikeService.java \
       api/src/main/java/moment/like/service/like/CommentLikeService.java
```

#### 2단계: 이동한 파일의 package 선언 수정

**`api/src/main/java/moment/like/service/like/MomentLikeService.java`**:
```java
// 변경 전
package moment.like.service;

// 변경 후
package moment.like.service.like;
```

**`api/src/main/java/moment/like/service/like/CommentLikeService.java`**:
```java
// 변경 전
package moment.like.service;

// 변경 후
package moment.like.service.like;
```

#### 3단계: 의존 클래스의 import 경로 변경

아래 8개 파일의 import 변경 필요:

| # | 파일 경로 | 변경 import |
|---|---------|------------|
| 1 | `api/src/main/java/moment/moment/service/application/MomentApplicationService.java` | `moment.like.service.MomentLikeService` → `moment.like.service.like.MomentLikeService` |
| 2 | `api/src/main/java/moment/moment/service/facade/MyGroupMomentPageFacadeService.java` | 두 서비스 모두 변경 |
| 3 | `api/src/main/java/moment/comment/service/facade/MyGroupCommentPageFacadeService.java` | 두 서비스 모두 변경 |
| 4 | `api/src/main/java/moment/comment/service/application/CommentApplicationService.java` | `CommentLikeService` 변경 |
| 5 | `api/src/main/java/moment/group/presentation/GroupMomentController.java` | `MomentLikeService` 변경 |
| 6 | `api/src/main/java/moment/group/presentation/GroupCommentController.java` | `CommentLikeService` 변경 |

**일괄 변경 패턴**:
```
moment.like.service.MomentLikeService  → moment.like.service.like.MomentLikeService
moment.like.service.CommentLikeService → moment.like.service.like.CommentLikeService
```

#### 4단계: 테스트 파일 이동

```bash
# 테스트 패키지 디렉토리 생성
mkdir -p api/src/test/java/moment/like/service/like

# 테스트 파일 이동
git mv api/src/test/java/moment/like/service/MomentLikeServiceTest.java \
       api/src/test/java/moment/like/service/like/MomentLikeServiceTest.java
git mv api/src/test/java/moment/like/service/CommentLikeServiceTest.java \
       api/src/test/java/moment/like/service/like/CommentLikeServiceTest.java
```

#### 5단계: 테스트 파일의 package 선언 및 import 수정

**각 테스트 파일**:
```java
// 변경 전
package moment.like.service;

// 변경 후
package moment.like.service.like;
```

#### 6단계: 검증

```bash
cd server
./gradlew compileJava        # 컴파일 확인
./gradlew fastTest           # 테스트 통과 확인
```

### 주의사항

- IDE 리팩토링 도구 사용 시 자동 import 변경 확인 필수
- 엔티티(`MomentLike`, `CommentLike`)는 `common` 모듈에 있으므로 변경 불필요
- 리포지토리(`MomentLikeRepository`, `CommentLikeRepository`)도 `common` 모듈이므로 변경 불필요

---

## Step 0-2: group/invite 서비스 위치 표준화

### 배경

`InviteLinkService`가 `service/invite/` 패키지에 단독으로 존재. group 도메인의 하위 서비스이므로 `service/group/`으로 통합.

### 현재 구조

```
api/src/main/java/moment/group/service/
├── group/
│   ├── GroupService.java
│   └── GroupMemberService.java
├── invite/
│   └── InviteLinkService.java     ← 이동 대상
└── application/
    ├── GroupApplicationService.java
    └── GroupMemberApplicationService.java

api/src/test/java/moment/group/service/
├── group/
│   ├── GroupServiceTest.java
│   └── GroupMemberServiceTest.java
├── invite/
│   └── InviteLinkServiceTest.java  ← 이동 대상
└── application/
    ├── GroupApplicationServiceTest.java
    └── GroupMemberApplicationServiceTest.java
```

### 변경 후 구조

```
api/src/main/java/moment/group/service/
├── group/
│   ├── GroupService.java
│   ├── GroupMemberService.java
│   └── InviteLinkService.java     ← 이동 완료
└── application/
    ├── GroupApplicationService.java
    └── GroupMemberApplicationService.java

api/src/test/java/moment/group/service/
├── group/
│   ├── GroupServiceTest.java
│   ├── GroupMemberServiceTest.java
│   └── InviteLinkServiceTest.java  ← 이동 완료
└── application/
    (변경 없음)
```

### 작업 순서

#### 1단계: 파일 이동

```bash
# main 소스
git mv api/src/main/java/moment/group/service/invite/InviteLinkService.java \
       api/src/main/java/moment/group/service/group/InviteLinkService.java

# test 소스
git mv api/src/test/java/moment/group/service/invite/InviteLinkServiceTest.java \
       api/src/test/java/moment/group/service/group/InviteLinkServiceTest.java
```

#### 2단계: package 선언 수정

**`InviteLinkService.java`**:
```java
// 변경 전
package moment.group.service.invite;

// 변경 후
package moment.group.service.group;
```

**`InviteLinkServiceTest.java`**:
```java
// 변경 전
package moment.group.service.invite;

// 변경 후
package moment.group.service.group;
```

#### 3단계: 의존 클래스의 import 경로 변경

4개 파일 변경 필요:

| # | 파일 경로 | 변경 내용 |
|---|---------|---------|
| 1 | `api/src/main/java/moment/group/service/application/GroupMemberApplicationService.java` | `moment.group.service.invite.InviteLinkService` → `moment.group.service.group.InviteLinkService` |
| 2 | `api/src/main/java/moment/group/service/application/GroupApplicationService.java` | 동일 |
| 3 | `api/src/test/java/moment/group/service/application/GroupMemberApplicationServiceTest.java` | 동일 |
| 4 | `api/src/test/java/moment/group/service/application/GroupApplicationServiceTest.java` | 동일 |

#### 4단계: 빈 패키지 삭제

```bash
# invite 패키지가 비어있으면 자동 삭제됨 (git은 빈 디렉토리 추적 안 함)
# IDE에서 수동 삭제 필요할 수 있음
rmdir api/src/main/java/moment/group/service/invite/
rmdir api/src/test/java/moment/group/service/invite/
```

#### 5단계: 검증

```bash
./gradlew compileJava
./gradlew fastTest
```

---

## Step 0-3: Gradle ArchUnit 의존성 추가

### 변경 파일

**`build.gradle` (루트)**

### 변경 내용

```groovy
// 변경 전
dependencies {
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}

// 변경 후
dependencies {
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'com.tngtech.archunit:archunit-junit5:1.3.0'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}
```

### 검증

```bash
./gradlew dependencies --configuration testCompileClasspath | grep archunit
# 출력: com.tngtech.archunit:archunit-junit5:1.3.0
```

### 참고

- ArchUnit 1.3.0은 JUnit 5 네이티브 지원
- `archunit-junit5`가 `archunit` core를 전이적으로 포함
- `subprojects` 블록에 추가하므로 api, admin, common 모듈 모두에 적용

---

## Step 0-4: TestTags에 ARCHITECTURE 태그 추가

### 변경 파일

**`common/src/testFixtures/java/moment/config/TestTags.java`**

### 변경 내용

```java
// 변경 전
package moment.config;

public class TestTags {
    public static final String UNIT = "unit";
    public static final String INTEGRATION = "integration";
    public static final String E2E = "e2e";

    private TestTags() {
    }
}

// 변경 후
package moment.config;

public class TestTags {
    public static final String UNIT = "unit";
    public static final String INTEGRATION = "integration";
    public static final String E2E = "e2e";
    public static final String ARCHITECTURE = "architecture";

    private TestTags() {
    }
}
```

### 동작 원리

- `fastTest`는 `excludeTags 'e2e'`이므로 `ARCHITECTURE` 태그가 붙은 테스트는 **자동 포함**
- `e2eTest`는 `includeTags 'e2e'`이므로 ArchUnit 테스트는 **자동 제외**
- 별도 Gradle task 추가 불필요

### 검증

```bash
./gradlew compileTestJava    # testFixtures 컴파일 확인
```

---

## Phase 0 전체 검증 체크리스트

```bash
# 1. 전체 컴파일 확인
./gradlew compileJava compileTestJava

# 2. 빠른 테스트 실행
./gradlew fastTest

# 3. 전체 테스트 실행 (e2e 포함)
./gradlew test

# 4. ArchUnit 의존성 확인
./gradlew dependencies --configuration testCompileClasspath | grep archunit
```

### 완료 판정 기준

- [ ] like 모듈: `service/like/MomentLikeService.java` 위치 확인
- [ ] like 모듈: `service/like/CommentLikeService.java` 위치 확인
- [ ] group 모듈: `service/group/InviteLinkService.java` 위치 확인
- [ ] group 모듈: `service/invite/` 패키지 삭제 확인
- [ ] ArchUnit 의존성: `archunit-junit5:1.3.0` 추가 확인
- [ ] TestTags: `ARCHITECTURE` 상수 추가 확인
- [ ] `./gradlew test` 전체 통과

### 커밋 전략

구조적 변경이므로 Tidy First 원칙에 따라 **동작 변경 없이** 커밋:

```
커밋 1: refactor: like 모듈 패키지 구조를 service/like/로 표준화
커밋 2: refactor: group/invite 서비스를 service/group/으로 이동
커밋 3: chore: ArchUnit 의존성 및 ARCHITECTURE 테스트 태그 추가
```
