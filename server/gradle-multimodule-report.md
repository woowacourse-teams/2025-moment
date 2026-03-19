# Moment 프로젝트 Gradle 멀티모듈 의존성 관리 보고서

## 1. 전체 모듈 구조

```
server/ (루트 프로젝트: moment)
├── build.gradle          ← 루트: 공통 설정 + 플러그인 선언
├── settings.gradle       ← 모듈 등록
├── common/
│   └── build.gradle      ← 공유 라이브러리 모듈
├── api/
│   └── build.gradle      ← 사용자 API 애플리케이션 모듈
└── admin/
    └── build.gradle      ← 관리자 애플리케이션 모듈
```

**의존 방향:**
```
api ──→ common
admin ──→ common
```

`api`와 `admin`은 서로 의존하지 않고, 오직 `common`만 참조한다.

---

## 2. settings.gradle — 모듈 등록

```groovy
rootProject.name = 'moment'
include 'common', 'api', 'admin'
```

| 항목 | 설명 |
|------|------|
| `rootProject.name` | 루트 프로젝트 이름. `project(':common')` 등으로 참조할 때의 기준점 |
| `include` | Gradle에게 이 디렉토리들이 서브모듈임을 알림. 각 디렉토리에 `build.gradle`이 있어야 함 |

---

## 3. 루트 build.gradle — 공통 설정의 중앙 관리

### 3.1 플러그인 선언 (`apply false`)

```groovy
plugins {
    id 'org.springframework.boot' version '3.5.3' apply false
    id 'io.spring.dependency-management' version '1.1.7' apply false
}
```

| 키워드 | 의미 |
|--------|------|
| `apply false` | 플러그인을 **다운로드만** 하고 루트 프로젝트에는 **적용하지 않음** |

**왜 `apply false`인가?**
- 루트 프로젝트는 코드가 없는 순수 설정 프로젝트
- `spring-boot` 플러그인을 루트에 적용하면 bootJar 태스크가 루트에 생기는데, 빌드할 소스가 없어서 오류 발생
- 버전은 **한 곳에서만** 선언하고, 서브모듈에서 버전 없이 `id 'org.springframework.boot'`로 적용

### 3.2 subprojects 블록 — 모든 서브모듈에 공통 적용

```groovy
subprojects {
    apply plugin: 'java'
    apply plugin: 'io.spring.dependency-management'
    ...
}
```

`subprojects { }` 안의 모든 설정은 `common`, `api`, `admin` **세 모듈 모두**에 자동 적용된다.

#### 공통 플러그인

| 플러그인 | 역할 |
|----------|------|
| `java` | Java 컴파일, 테스트, JAR 생성 등 기본 태스크 제공 |
| `io.spring.dependency-management` | Spring Boot BOM을 통한 의존성 버전 자동 관리 |

#### Java 21 설정

```groovy
java {
    toolchain { languageVersion = JavaLanguageVersion.of(21) }
}
```

- 모든 모듈이 **Java 21**로 컴파일됨을 보장
- `toolchain`은 로컬에 해당 JDK가 없으면 자동 다운로드까지 지원

#### 컴파일러 옵션

```groovy
tasks.withType(JavaCompile).configureEach {
    options.compilerArgs.add('-parameters')
}
```

- `-parameters`: 컴파일 시 **메서드 파라미터 이름을 바이트코드에 보존**
- Spring의 `@PathVariable`, `@RequestParam` 등에서 `name` 속성 생략 가능
- Jackson의 `@JsonCreator` record 역직렬화에도 필요

#### Lombok 설정

```groovy
configurations {
    compileOnly { extendsFrom annotationProcessor }
}
```

- `compileOnly`가 `annotationProcessor` 설정을 **상속**하도록 구성
- Lombok처럼 컴파일 타임에만 필요한 라이브러리가 annotation processing과 compileOnly 양쪽에서 일관되게 동작

#### BOM (Bill of Materials) 임포트

```groovy
dependencyManagement {
    imports {
        mavenBom org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES
    }
}
```

**핵심 개념: BOM이란?**
- Spring Boot가 관리하는 **수백 개 라이브러리의 호환 버전 목록**
- BOM을 임포트하면 `spring-boot-starter-web`, `jackson`, `hibernate` 등의 **버전을 명시하지 않아도** Spring Boot 3.5.3과 호환되는 버전이 자동 적용됨

**왜 루트에서 `apply false` 후 여기서 BOM만 임포트하는가?**
- `spring-boot` 플러그인을 직접 적용하면 `bootJar` 태스크가 생김
- `common` 모듈은 실행 가능 JAR이 아닌 **라이브러리 JAR**이므로 `bootJar`가 불필요
- BOM만 별도로 임포트하면 **버전 관리 혜택만** 받고 불필요한 태스크는 생기지 않음

#### 공통 의존성

```groovy
dependencies {
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}
```

| 스코프 | 의존성 | 설명 |
|--------|--------|------|
| `compileOnly` | Lombok | 컴파일 시에만 필요, JAR에 포함 안 됨 |
| `annotationProcessor` | Lombok | 어노테이션 프로세서로 `@Getter`, `@RequiredArgsConstructor` 등 코드 생성 |
| `testImplementation` | spring-boot-starter-test | JUnit5, Mockito, AssertJ 등 테스트 프레임워크 일괄 제공 |
| `testRuntimeOnly` | junit-platform-launcher | JUnit5 테스트 실행 엔진. 컴파일에는 불필요하고 실행 시에만 필요 |

---

## 4. common/build.gradle — 공유 라이브러리 모듈

### 4.1 플러그인

```groovy
plugins {
    id 'java-library'
    id 'java-test-fixtures'
}
```

| 플러그인 | 역할 |
|----------|------|
| `java-library` | `api`/`implementation` 의존성 구분 활성화 (핵심!) |
| `java-test-fixtures` | 테스트 픽스처를 별도 소스셋으로 제공. 다른 모듈에서 `testFixtures(project(':common'))`로 참조 가능 |

### 4.2 `java-library` 플러그인과 `api` vs `implementation`

이것이 멀티모듈 의존성 관리의 **가장 핵심적인 개념**이다.

```groovy
dependencies {
    api 'org.springframework.boot:spring-boot-starter-data-jpa'       // 전이됨
    api 'org.springframework.boot:spring-boot-starter-validation'     // 전이됨
    api 'org.springframework.boot:spring-boot-starter-web'            // 전이됨
    runtimeOnly 'com.mysql:mysql-connector-j'                         // 전이 안 됨
}
```

#### `api` vs `implementation` 차이

```
                    api 스코프                    implementation 스코프
                ┌─────────────┐               ┌─────────────┐
  common에서     │ JPA Entity  │               │ 내부 유틸     │
  선언           │ Validation  │               │ 구현 세부사항  │
                └──────┬──────┘               └──────┬──────┘
                       │                             │
                 ✅ 전이됨                        ❌ 전이 안 됨
                       │                             │
                ┌──────▼──────┐               ┌──────▼──────┐
  api/admin     │ 직접 사용 가능 │               │ 보이지 않음    │
  모듈에서       │ import 가능   │               │ 컴파일 에러    │
                └─────────────┘               └─────────────┘
```

| 스코프 | 전이적(transitive) | 사용 기준 |
|--------|-------------------|----------|
| `api` | **O** — 이 모듈을 의존하는 모듈에도 **노출됨** | common의 공개 API에 등장하는 타입 (Entity 어노테이션, Validation 등) |
| `implementation` | **X** — 이 모듈 내부에서만 사용 | 외부에 노출할 필요 없는 내부 구현 |
| `runtimeOnly` | **X** — 실행 시에만 필요 | JDBC 드라이버처럼 코드에서 직접 참조하지 않는 것 |

#### common이 `api`로 선언한 의존성이 전이되는 흐름

```
common (api: spring-data-jpa, validation, web, springdoc, logstash, flyway-mysql, spring-security-crypto, aop)
   │
   ├──→ api 모듈: implementation project(':common')
   │    → JPA, Validation, Web 등 모두 자동으로 사용 가능 (api 전이)
   │
   └──→ admin 모듈: implementation project(':common')
        → 동일하게 JPA, Validation, Web 등 자동 사용 가능
```

**이 설계의 장점:**
- `api`와 `admin`에서 JPA, Validation 등을 **중복 선언할 필요가 없음**
- 버전을 common 한 곳에서만 관리

### 4.3 JAR 설정

```groovy
jar { enabled = true }
```

- common은 **라이브러리 모듈**이므로 일반 JAR을 생성
- `spring-boot` 플러그인을 적용하지 않았으므로 `bootJar`는 존재하지 않음

### 4.4 Test Fixtures

```groovy
testFixturesImplementation 'org.springframework.boot:spring-boot-starter-data-jpa'
testFixturesImplementation 'org.springframework.boot:spring-boot-starter-test'
testFixturesCompileOnly 'org.projectlombok:lombok'
testFixturesAnnotationProcessor 'org.projectlombok:lombok'
```

**Test Fixtures란?**
- `src/testFixtures/java/`에 위치하는 **테스트 전용 공유 코드** (예: `UserFixture`, `MomentFixture`)
- 프로덕션 JAR에는 포함되지 않음
- 다른 모듈에서 `testFixtures(project(':common'))`로 참조하여 테스트 데이터를 재사용

```
common/
├── src/main/java/          ← 프로덕션 코드 (Entity, Service 등)
├── src/test/java/          ← common 자체 테스트
└── src/testFixtures/java/  ← UserFixture 등 (api, admin에서 재사용)
```

---

## 5. api/build.gradle — 사용자 API 애플리케이션

### 5.1 플러그인

```groovy
plugins {
    id 'org.springframework.boot'
}
```

- 루트에서 `apply false`로 선언한 플러그인을 **버전 없이** 적용
- `bootJar` 태스크가 활성화되어 **실행 가능한 Fat JAR** 생성

### 5.2 JAR 설정

```groovy
jar { enabled = false }
```

- 일반 `jar` 태스크를 비활성화
- `bootJar`만 생성 (Spring Boot 실행 가능 JAR)
- 이 모듈은 **배포 단위**이지 다른 모듈의 라이브러리가 아님

### 5.3 모듈 간 의존성

```groovy
implementation project(':common')
testImplementation testFixtures(project(':common'))
```

| 선언 | 설명 |
|------|------|
| `implementation project(':common')` | common 모듈의 프로덕션 코드를 사용. common이 `api`로 선언한 의존성들도 전이됨 |
| `testFixtures(project(':common'))` | common의 테스트 픽스처(`UserFixture` 등)를 테스트에서 사용 |

### 5.4 API 전용 의존성

```groovy
// JWT 인증
implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.6'
runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.6'

// 이메일 발송
implementation 'org.springframework.boot:spring-boot-starter-mail'

// AWS S3 파일 스토리지
implementation 'io.awspring.cloud:spring-cloud-aws-starter-s3:3.4.0'
implementation 'io.awspring.cloud:spring-cloud-aws-starter-metrics:3.4.0'

// 캐싱
implementation 'com.github.ben-manes.caffeine:caffeine'

// SQL 로깅
implementation 'com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.9.0'

// 모니터링
implementation 'io.micrometer:micrometer-registry-prometheus'

// 테스트
testRuntimeOnly 'com.h2database:h2'
testImplementation 'io.rest-assured:rest-assured'
testImplementation 'com.launchdarkly:okhttp-eventsource:2.7.1'  // SSE 테스트
```

**JJWT의 `api` / `impl` / `jackson` 분리 패턴:**
- `jjwt-api`: 인터페이스만 포함 → `implementation` (코드에서 직접 참조)
- `jjwt-impl`, `jjwt-jackson`: 구현체 → `runtimeOnly` (리플렉션으로 로드, 코드에서 직접 참조 안 함)

### 5.5 커스텀 테스트 태스크

```groovy
tasks.register('fastTest', Test) {
    useJUnitPlatform { excludeTags 'e2e' }
}
tasks.register('e2eTest', Test) {
    useJUnitPlatform { includeTags 'e2e' }
}
```

| 태스크 | 실행 범위 | 용도 |
|--------|----------|------|
| `./gradlew :api:test` | 전체 테스트 | CI에서 사용 |
| `./gradlew :api:fastTest` | e2e 제외한 단위/통합 테스트 | 개발 중 빠른 피드백 |
| `./gradlew :api:e2eTest` | e2e 테스트만 | 배포 전 전체 흐름 검증 |

---

## 6. admin/build.gradle — 관리자 애플리케이션

api 모듈과 구조가 동일하되, Admin 전용 의존성만 다르다.

```groovy
// 세션 기반 인증 (JWT 대신)
implementation 'org.springframework.session:spring-session-jdbc'

// 서버사이드 렌더링 (REST API 대신)
implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
implementation 'nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:3.3.0'
```

| api 모듈 | admin 모듈 | 이유 |
|----------|-----------|------|
| JWT (jjwt) | Session (spring-session-jdbc) | 인증 방식 차이 |
| REST API only | Thymeleaf SSR | 관리자 페이지는 서버사이드 렌더링 |
| S3, Firebase, Mail | 없음 | 관리자에게는 불필요 |

---

## 7. 의존성 스코프 총정리

```
┌─────────────────────────────────────────────────────────────┐
│                    Gradle 의존성 스코프                       │
├──────────────────────┬──────────────────────────────────────┤
│ 스코프                │ 설명                                 │
├──────────────────────┼──────────────────────────────────────┤
│ api                  │ 컴파일 + 런타임 + 전이적 노출           │
│                      │ (java-library 플러그인 전용)           │
├──────────────────────┼──────────────────────────────────────┤
│ implementation       │ 컴파일 + 런타임 (전이 안 됨)           │
│                      │ 가장 일반적인 스코프                    │
├──────────────────────┼──────────────────────────────────────┤
│ compileOnly          │ 컴파일에만 사용, JAR에 미포함           │
│                      │ (Lombok 등)                          │
├──────────────────────┼──────────────────────────────────────┤
│ runtimeOnly          │ 실행 시에만 필요, 컴파일에 불필요        │
│                      │ (JDBC 드라이버, JJWT 구현체)           │
├──────────────────────┼──────────────────────────────────────┤
│ annotationProcessor  │ 컴파일 시 코드 생성용                   │
│                      │ (Lombok)                             │
├──────────────────────┼──────────────────────────────────────┤
│ testImplementation   │ 테스트 컴파일 + 테스트 런타임            │
│                      │ (JUnit, RestAssured)                 │
├──────────────────────┼──────────────────────────────────────┤
│ testRuntimeOnly      │ 테스트 실행 시에만 필요                 │
│                      │ (H2, JUnit Launcher)                 │
├──────────────────────┼──────────────────────────────────────┤
│ testFixtures-        │ 테스트 픽스처 전용 스코프               │
│ Implementation       │ (Fixture 클래스에서 사용하는 의존성)     │
└──────────────────────┴──────────────────────────────────────┘
```

---

## 8. 전체 의존성 흐름도

```
settings.gradle
  └─ include 'common', 'api', 'admin'

루트 build.gradle (subprojects 블록)
  ├─ Java 21 toolchain
  ├─ Spring Boot BOM (버전 자동 관리)
  ├─ Lombok (compileOnly + annotationProcessor)
  └─ spring-boot-starter-test

common/build.gradle
  ├─ java-library 플러그인 → api 스코프 활성화
  ├─ java-test-fixtures → 테스트 픽스처 공유
  ├─ api: JPA, Validation, Web, AOP, Security-Crypto, Springdoc, Logstash, Flyway
  ├─ runtimeOnly: MySQL 드라이버
  └─ testFixtures: JPA, Test, Lombok

api/build.gradle                          admin/build.gradle
  ├─ spring-boot 플러그인 (bootJar)          ├─ spring-boot 플러그인 (bootJar)
  ├─ implementation: common                  ├─ implementation: common
  │   └─ common의 api 의존성 전이              │   └─ common의 api 의존성 전이
  ├─ testFixtures: common 픽스처              ├─ testFixtures: common 픽스처
  ├─ JWT, Mail, S3, Caffeine, Prometheus     ├─ Session-JDBC, Thymeleaf
  └─ H2(test), RestAssured, SSE client       └─ H2(test), RestAssured
```

---

## 9. 설계 판단 요약

| 결정 | 이유 |
|------|------|
| 루트에서 `apply false` | 루트는 코드 없는 설정 프로젝트. bootJar 태스크 방지 |
| common에 `java-library` | `api` 스코프로 JPA 등을 전이시켜 중복 선언 제거 |
| common에 `java-test-fixtures` | `UserFixture` 등을 api/admin에서 재사용 |
| common에 `spring-boot` 미적용 | 라이브러리 모듈이므로 bootJar 불필요. BOM만 subprojects에서 임포트 |
| api/admin에서 `jar { enabled = false }` | 실행 가능 bootJar만 생성. 일반 JAR은 불필요 |
| BOM을 subprojects에서 임포트 | 모든 모듈에서 Spring Boot 호환 버전 자동 관리 |
