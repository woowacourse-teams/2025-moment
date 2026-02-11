# Phase 5: 정리 및 검증

> Created: 2026-02-11
> Status: PLANNED
> 전제 조건: Phase 2~4 모든 코드 이동 완료

## 목적

모든 모듈의 빌드/테스트를 검증하고, 검증 통과 후 기존 `src/` 디렉토리를 삭제한다.

---

## 5-1. 1차 빌드 검증 (src/ 삭제 전)

### 전체 빌드 + 테스트

```bash
./gradlew clean build
```

### 예상되는 이슈와 해결 방법

#### 이슈 1: 중복 클래스 (src/와 모듈에 동일 클래스)

Phase 2~4에서 "복사"만 했으므로 `src/`와 모듈에 동일 파일이 존재.
루트 `build.gradle`이 `subprojects`로 변경되었으므로 루트의 `src/`는 빌드 대상이 아님.
→ **자동 해결**됨 (루트에 source set 없음)

만약 빌드 오류 발생 시:
```groovy
// 루트 build.gradle에 추가 (임시)
sourceSets.main.java.srcDirs = []
sourceSets.test.java.srcDirs = []
```

#### 이슈 2: import 누락

모듈 간 의존성 경계에서 발생. 예: api 모듈에서 common에 없는 클래스 참조.
→ 해당 클래스를 올바른 모듈로 이동

#### 이슈 3: Spring Session 의존성

admin에서만 `spring-session-jdbc` 사용. api에서 `spring.session.*` 설정이 남아있으면 오류.
→ api의 yml에서 session 설정 완전 제거 확인

#### 이슈 4: @EnableJpaAuditing 중복

`MomentApplication.java`에 남아있는 `@EnableJpaAuditing`과 common의 `JpaAuditingConfig` 충돌 가능.
→ Phase 5-1에서 `src/` 삭제 시 자동 해결. 또는 먼저 제거.

### 모듈별 빌드

```bash
./gradlew :common:build         # common 단독 빌드
./gradlew :api:test             # api 테스트
./gradlew :admin:test           # admin 테스트
```

### bootJar 생성 확인

```bash
./gradlew :api:bootJar          # api/build/libs/api-*.jar 생성
./gradlew :admin:bootJar        # admin/build/libs/admin-*.jar 생성
```

---

## 5-2. 기존 src/ 디렉토리 삭제

### 전제 조건 확인 스크립트

```bash
#!/bin/bash
set -e

echo "=== Phase 5 사전 검증 ==="

echo "1. 전체 빌드..."
./gradlew clean build

echo "2. api bootJar..."
./gradlew :api:bootJar

echo "3. admin bootJar..."
./gradlew :admin:bootJar

echo "=== 모든 검증 통과! src/ 삭제 가능 ==="
```

### 삭제 실행

```bash
# 안전을 위해 먼저 git에 커밋
git add -A
git commit -m "refactor: Phase 2~4 완료 - 모든 코드 모듈로 복사"

# src/ 삭제
rm -rf src/

# MomentApplication.java 삭제 확인
# (이미 src/ 하위이므로 함께 삭제됨)
```

### 삭제 후 즉시 빌드 확인

```bash
./gradlew clean build
```

---

## 5-3. 컴포넌트 스캔 검증

### 원리

| Application | 패키지 | 스캔 대상 |
|-------------|--------|-----------|
| `ApiApplication` | `moment` | `moment.*` → common + api 클래스만 classpath에 존재 |
| `AdminApplication` | `moment` | `moment.*` → common + admin 클래스만 classpath에 존재 |

Gradle 모듈 경계가 classpath를 자동 분리:
- `:api`는 `implementation project(':common')`만 의존 → admin 클래스 접근 불가
- `:admin`은 `implementation project(':common')`만 의존 → api 클래스 접근 불가

### 검증 방법

```bash
# api에서 admin 클래스 참조 없는지 확인
grep -r "import moment.admin" api/src/ --include="*.java" | head -20
# 결과 0건이어야 함

# admin에서 api 전용 클래스 참조 없는지 확인
grep -r "import moment.auth.infrastructure.JwtTokenManager" admin/src/ --include="*.java" | head -20
grep -r "import moment.storage" admin/src/ --include="*.java" | head -20
# 결과 0건이어야 함
```

---

## 5-4. Flyway 설정 검증

### 설정 매트릭스

| 모듈 | 환경 | `flyway.enabled` | `locations` |
|------|------|-------------------|-------------|
| api | dev/prod (MySQL) | `true` | `classpath:db/migration/mysql` |
| api | test (H2) | `true` | `classpath:db/migration/h2` |
| admin | dev/prod (MySQL) | `false` | - |
| admin | test (H2) | `true` | `classpath:db/migration/h2` |

### 검증

- api prod: 마이그레이션 실행 → 스키마 생성/업데이트
- admin prod: 마이그레이션 미실행 → api가 이미 생성한 스키마 사용
- 양쪽 test: H2 마이그레이션 실행 → common의 testFixtures/resources에서 마이그레이션 로드

### Classpath 확인

마이그레이션 파일이 common에 있으므로 양쪽 모듈의 classpath에서 접근 가능:
- common의 `src/main/resources/db/migration/mysql/` → api/admin runtime classpath
- common의 `src/testFixtures/resources/db/migration/h2/` → api/admin test classpath (via `testFixtures(project(':common'))`)

---

## 5-5. 최종 빌드 및 테스트 검증

```bash
echo "=== 최종 검증 시작 ==="

# 1. 클린 빌드
./gradlew clean build

# 2. 모듈별 테스트
./gradlew :common:build
./gradlew :api:test
./gradlew :admin:test

# 3. 실행 JAR 생성
./gradlew :api:bootJar
./gradlew :admin:bootJar

# 4. JAR 파일 존재 확인
ls -la api/build/libs/api-*.jar
ls -la admin/build/libs/admin-*.jar

# 5. JAR 실행 가능 확인 (5초 후 종료)
timeout 10 java -jar api/build/libs/api-*-SNAPSHOT.jar --spring.profiles.active=test || true
timeout 10 java -jar admin/build/libs/admin-*-SNAPSHOT.jar --spring.profiles.active=test || true

echo "=== 최종 검증 완료 ==="
```

---

## 5-6. Phase 5 커밋 및 태그

```bash
git add -A
git commit -m "refactor: Phase 5 - src/ 삭제 및 멀티모듈 검증 완료"
git tag phase-5-complete
```

---

## Phase 5 체크리스트

| 항목 | 확인 |
|------|------|
| `./gradlew clean build` 통과 | [ ] |
| `./gradlew :common:build` 통과 | [ ] |
| `./gradlew :api:test` 전체 통과 | [ ] |
| `./gradlew :admin:test` 전체 통과 | [ ] |
| `./gradlew :api:bootJar` 성공 | [ ] |
| `./gradlew :admin:bootJar` 성공 | [ ] |
| `src/` 디렉토리 삭제 완료 | [ ] |
| api에서 admin 클래스 참조 없음 | [ ] |
| admin에서 api 전용 클래스 참조 없음 | [ ] |
| Flyway 설정 모듈별 확인 | [ ] |
