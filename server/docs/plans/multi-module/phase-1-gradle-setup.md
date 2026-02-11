# Phase 1: Gradle 멀티모듈 구조 설정

> Created: 2026-02-11
> Status: PLANNED
> 예상 작업 파일 수: ~5개
> 검증: `./gradlew build` (src/ 코드가 아직 남아있으므로 기존 빌드 유지)

## 목적

Gradle 서브모듈(common, api, admin) 디렉토리 구조와 빌드 설정을 생성한다.
이 Phase에서는 **코드 이동 없이** 빌드 인프라만 구성한다.

---

## 1-1. settings.gradle 수정

### 현재

```groovy
rootProject.name = 'moment'
```

### 변경 후

```groovy
rootProject.name = 'moment'
include 'common', 'api', 'admin'
```

---

## 1-2. 루트 build.gradle 재구성

### 현재

단일 모듈 구성. 모든 의존성이 루트에 직접 선언.

### 변경 후

기존 의존성을 `subprojects` 블록으로 이동. 루트 프로젝트는 소스 코드 없음.

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.5.3' apply false
    id 'io.spring.dependency-management' version '1.1.7' apply false
}

subprojects {
    apply plugin: 'java'
    apply plugin: 'io.spring.dependency-management'

    group = 'com.moment'
    version = '0.0.1-SNAPSHOT'

    java {
        toolchain { languageVersion = JavaLanguageVersion.of(21) }
    }

    configurations {
        compileOnly { extendsFrom annotationProcessor }
    }

    repositories { mavenCentral() }

    dependencyManagement {
        imports {
            mavenBom org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES
        }
    }

    dependencies {
        compileOnly 'org.projectlombok:lombok'
        annotationProcessor 'org.projectlombok:lombok'
        testImplementation 'org.springframework.boot:spring-boot-starter-test'
        testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
    }

    tasks.named('test') { useJUnitPlatform() }
}
```

### 주의사항

- `org.sonarqube` 플러그인은 루트에 유지 가능 (CI에서만 사용)
- 기존 `src/` 빌드: 루트 프로젝트에 `subprojects` 적용 후, 기존 `src/` 코드는 루트 프로젝트의 소스셋에서 제외됨.
  Phase 2~4 완료 전까지 루트에서 직접 빌드는 불가.

---

## 1-3. common/build.gradle 생성

```groovy
plugins {
    id 'java-library'
    id 'java-test-fixtures'
}

// bootJar 태스크 없음 (java-library). plain jar만 생성
jar { enabled = true }

dependencies {
    api 'org.springframework.boot:spring-boot-starter-data-jpa'
    api 'org.springframework.boot:spring-boot-starter-validation'
    api 'org.springframework.boot:spring-boot-starter-aop'
    api 'org.springframework.security:spring-security-crypto'
    api 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.8'
    api 'net.logstash.logback:logstash-logback-encoder:7.4'
    api 'org.flywaydb:flyway-mysql'

    // NOTE: ErrorCode가 HttpStatus에 의존하여 starter-web이 필요.
    // 장기적으로 ErrorCode에서 HttpStatus를 int status code로 대체하여 제거 예정.
    api 'org.springframework.boot:spring-boot-starter-web'

    runtimeOnly 'com.mysql:mysql-connector-j'

    testFixturesImplementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    testFixturesImplementation 'org.springframework.boot:spring-boot-starter-test'
    testFixturesCompileOnly 'org.projectlombok:lombok'
    testFixturesAnnotationProcessor 'org.projectlombok:lombok'
}
```

### 의존성 설명

| 의존성 | 이유 |
|--------|------|
| `starter-data-jpa` | 엔티티, 레포지토리 |
| `starter-validation` | Jakarta Validation 어노테이션 |
| `starter-aop` | AOP 로깅 (ControllerLogAspect 등) |
| `spring-security-crypto` | PasswordEncoder (AppConfig) |
| `springdoc` | @Schema 어노테이션 (DTO에서 사용) |
| `logstash-logback-encoder` | 구조화 로깅 |
| `flyway-mysql` | Flyway 마이그레이션 |
| `starter-web` | ErrorCode의 HttpStatus 의존 |

---

## 1-4. api/build.gradle 생성

```groovy
plugins {
    id 'org.springframework.boot'
}

// plain jar 생성 비활성화 (Dockerfile *.jar 와일드카드 대응)
jar { enabled = false }

dependencies {
    implementation project(':common')
    testImplementation testFixtures(project(':common'))

    // API 전용 의존성
    implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
    implementation 'org.springframework.boot:spring-boot-starter-mail'
    implementation 'io.awspring.cloud:spring-cloud-aws-starter-s3:3.4.0'
    implementation 'io.awspring.cloud:spring-cloud-aws-starter-metrics:3.4.0'
    implementation 'com.github.ben-manes.caffeine:caffeine'
    implementation 'com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.9.0'
    implementation 'io.micrometer:micrometer-registry-prometheus'

    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.6'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.6'
    testRuntimeOnly 'com.h2database:h2'

    testImplementation 'io.rest-assured:rest-assured'
    testImplementation 'com.launchdarkly:okhttp-eventsource:2.7.1'
}

tasks.register('fastTest', Test) {
    useJUnitPlatform { excludeTags 'e2e' }
}
tasks.register('e2eTest', Test) {
    useJUnitPlatform { includeTags 'e2e' }
}
```

### 의존성 설명

| 의존성 | 이유 |
|--------|------|
| `jjwt-*` | JWT 토큰 생성/검증 |
| `starter-mail` | 이메일 발송 |
| `spring-cloud-aws-*` | S3 파일 업로드 + CloudWatch 메트릭 |
| `caffeine` | Apple 키 캐시 |
| `p6spy` | SQL 쿼리 로깅 |
| `micrometer-registry-prometheus` | Prometheus 메트릭 |
| `rest-assured` | E2E API 테스트 |
| `okhttp-eventsource` | SSE 테스트 |

---

## 1-5. admin/build.gradle 생성

```groovy
plugins {
    id 'org.springframework.boot'
}

// plain jar 생성 비활성화 (Dockerfile *.jar 와일드카드 대응)
jar { enabled = false }

dependencies {
    implementation project(':common')
    testImplementation testFixtures(project(':common'))

    // Admin 전용 의존성
    implementation 'org.springframework.session:spring-session-jdbc'
    implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
    implementation 'nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:3.3.0'
    implementation 'com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.9.0'

    testRuntimeOnly 'com.h2database:h2'

    testImplementation 'io.rest-assured:rest-assured'
}

tasks.register('fastTest', Test) {
    useJUnitPlatform { excludeTags 'e2e' }
}
tasks.register('e2eTest', Test) {
    useJUnitPlatform { includeTags 'e2e' }
}
```

---

## 1-6. 디렉토리 생성

```bash
# common
mkdir -p common/src/main/java/moment
mkdir -p common/src/main/resources
mkdir -p common/src/testFixtures/java/moment
mkdir -p common/src/testFixtures/resources
mkdir -p common/src/test

# api
mkdir -p api/src/main/java/moment
mkdir -p api/src/main/resources
mkdir -p api/src/test/java/moment
mkdir -p api/src/test/resources

# admin
mkdir -p admin/src/main/java/moment
mkdir -p admin/src/main/resources
mkdir -p admin/src/test/java/moment
mkdir -p admin/src/test/resources
```

### 결과 구조

```
server/
├── common/
│   ├── build.gradle
│   └── src/
│       ├── main/java/moment/
│       ├── main/resources/
│       ├── testFixtures/java/moment/
│       ├── testFixtures/resources/
│       └── test/
├── api/
│   ├── build.gradle
│   └── src/
│       ├── main/java/moment/
│       ├── main/resources/
│       ├── test/java/moment/
│       └── test/resources/
├── admin/
│   ├── build.gradle
│   └── src/
│       ├── main/java/moment/
│       ├── main/resources/
│       ├── test/java/moment/
│       └── test/resources/
├── src/            ← 기존 코드 (Phase 5에서 삭제)
├── build.gradle    ← 수정됨
└── settings.gradle ← 수정됨
```

---

## 1-7. testFixtures classpath 검증

common 모듈 단독 빌드하여 testFixtures jar 구조 확인.

```bash
./gradlew :common:testFixturesJar
jar tf common/build/libs/common-*-test-fixtures.jar | grep "db/migration/h2"
```

Phase 2에서 H2 마이그레이션 파일을 `common/src/testFixtures/resources/db/migration/h2/`로 이동한 후 이 검증 재실행.

문제 발생 시 대안: `common/src/test/resources/db/migration/h2/`로 이동.

---

## 1-8. Phase 1 완료 검증

```bash
# 서브모듈 구조 확인 (비어있으므로 빌드만 확인)
./gradlew :common:build
./gradlew :api:build
./gradlew :admin:build

git add -A && git commit -m "refactor: Phase 1 - Gradle 멀티모듈 구조 설정"
git tag phase-1-complete
```

### Phase 1 변경 파일 체크리스트

| 파일 | 변경 유형 |
|------|-----------|
| `settings.gradle` | 수정 (include 추가) |
| `build.gradle` | 전면 재구성 (subprojects 블록) |
| `common/build.gradle` | 새 파일 |
| `api/build.gradle` | 새 파일 |
| `admin/build.gradle` | 새 파일 |
| `common/src/...` | 빈 디렉토리 |
| `api/src/...` | 빈 디렉토리 |
| `admin/src/...` | 빈 디렉토리 |
