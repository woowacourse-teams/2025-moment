# 멀티 모듈 구조 분석 보고서

작성일: 2026-02-11  
대상 프로젝트: `server`  
분석 기준: 실제 코드/빌드 스크립트/배포 스크립트

## 1. 결론 요약

현재 프로젝트는 **코드/빌드 레벨에서는 멀티 모듈(common, api, admin) 구조가 적용**되어 있습니다.  
다만 **배포 파이프라인(Dockerfile, docker-compose, CI/CD)은 아직 단일 app 기준**이 남아 있습니다.

## 2. 현재 멀티 모듈 구조

### 2.1 모듈 선언

- 루트 프로젝트 이름: `moment`
- 포함 모듈: `common`, `api`, `admin`

근거:
- `settings.gradle:1`
- `settings.gradle:2`

```text
root
├─ common  (java-library + java-test-fixtures)
├─ api     (spring-boot executable)
└─ admin   (spring-boot executable)
```

### 2.2 모듈 의존 방향

```text
api   ─┐
       ├──> common
admin ─┘
```

- `api`는 `project(':common')`을 의존
- `admin`도 `project(':common')`을 의존
- `common`은 내부 프로젝트 의존 없음

근거:
- `api/build.gradle:8`
- `admin/build.gradle:8`

## 3. 모듈별 역할과 경계

### 3.1 `common` 모듈

역할:
- 엔티티/리포지토리/공통 설정/공통 인프라
- Flyway MySQL 마이그레이션 리소스
- 공유 테스트 픽스처(testFixtures)

핵심 설정:
- `java-library`, `java-test-fixtures` 플러그인 사용
- `jar` 활성화
- 공통 API 의존성(JPA, Validation, AOP, Flyway, Security Crypto 등)

근거:
- `common/build.gradle:2`
- `common/build.gradle:3`
- `common/build.gradle:6`
- `common/build.gradle:8`

패키지 예시:
- `common/src/main/java/moment/admin`
- `common/src/main/java/moment/auth`
- `common/src/main/java/moment/group`
- `common/src/main/java/moment/user`

### 3.2 `api` 모듈

역할:
- 사용자 API 실행 모듈
- REST 컨트롤러/서비스/인증/JWT/S3/메일/알림 등

핵심 설정:
- Spring Boot 실행 모듈
- `jar` 비활성화 + `bootJar` 중심
- `fastTest`, `e2eTest` 태스크 분리

근거:
- `api/build.gradle:2`
- `api/build.gradle:5`
- `api/build.gradle:28`
- `api/build.gradle:31`
- `api/src/main/java/moment/ApiApplication.java:10`

### 3.3 `admin` 모듈

역할:
- 관리자 API 실행 모듈
- 관리자 전용 컨트롤러/서비스/세션 기반 인증

핵심 설정:
- Spring Boot 실행 모듈
- `jar` 비활성화 + `bootJar` 중심
- `fastTest`, `e2eTest` 태스크 분리

근거:
- `admin/build.gradle:2`
- `admin/build.gradle:5`
- `admin/build.gradle:22`
- `admin/build.gradle:25`
- `admin/src/main/java/moment/AdminApplication.java:8`

## 4. 공통/실행 설정 분리 상태

### 4.1 루트 공통 빌드 규칙

루트 `build.gradle`의 `subprojects` 블록에서 공통 규칙을 강제합니다.

- Java toolchain 21
- Lombok, JUnit platform, BOM import
- 모든 서브모듈의 공통 컴파일/테스트 정책

근거:
- `build.gradle:6`
- `build.gradle:14`
- `build.gradle:27`
- `build.gradle:33`
- `build.gradle:40`

### 4.2 애플리케이션 설정(YAML) 분리

`api`:
- Flyway 활성화
- OAuth/JWT/S3/CloudWatch 등 API 전용 설정

근거:
- `api/src/main/resources/application-dev.yml:23`
- `api/src/main/resources/application-prod.yml:31`

`admin`:
- 포트 8081
- Flyway 비활성화(운영)
- Spring Session JDBC 중심

근거:
- `admin/src/main/resources/application-dev.yml:21`
- `admin/src/main/resources/application-dev.yml:32`
- `admin/src/main/resources/application-prod.yml:27`
- `admin/src/main/resources/application-prod.yml:44`

### 4.3 테스트 환경 분리

- `api` 테스트: H2 + Flyway(h2 경로)
- `admin` 테스트: H2 + Flyway(h2 경로) + session schema

근거:
- `api/src/test/resources/application-test.yml:22`
- `api/src/test/resources/application-test.yml:25`
- `admin/src/test/resources/application-test.yml:21`
- `admin/src/test/resources/application-test.yml:24`

## 5. 멀티 모듈 검증 포인트(실측)

### 5.1 Gradle 프로젝트 인식

`./gradlew -q projects` 결과:

- `Project ':admin'`
- `Project ':api'`
- `Project ':common'`

### 5.2 루트 테스트 태스크 호출 방식

`./gradlew -m fastTest e2eTest` 결과에서 아래 태스크가 모두 선택됨:

- `:admin:fastTest`, `:admin:e2eTest`
- `:api:fastTest`, `:api:e2eTest`

즉, 루트에서 이름 기반으로 모듈 테스트를 함께 실행하는 구조입니다.

### 5.3 testFixtures 공유

- `api`, `admin` 모두 `testImplementation testFixtures(project(':common'))` 사용
- `common`에 공유 fixture 존재 (`moment/fixture/*`)

근거:
- `api/build.gradle:9`
- `admin/build.gradle:9`
- `common/src/testFixtures/java/moment/fixture`

## 6. 현재 구조에서 반드시 확인할 부분 (단계별 가이드)

아래 순서대로 보면 멀티 모듈 구조를 빠르게 이해할 수 있습니다.

### Step 1. 모듈 선언 확인

확인 파일:
- `settings.gradle`

체크:
- `include 'common', 'api', 'admin'` 존재 여부

### Step 2. 공통 빌드 정책 확인

확인 파일:
- `build.gradle`

체크:
- `subprojects { ... }` 내부 공통 정책
- Java 21/toolchain, BOM, test 설정 통일 여부

### Step 3. 모듈별 플러그인/의존성 확인

확인 파일:
- `common/build.gradle`
- `api/build.gradle`
- `admin/build.gradle`

체크:
- `common`: `java-library`, `java-test-fixtures`
- `api/admin`: `org.springframework.boot`
- `api/admin -> common` 의존 방향
- 모듈별 전용 라이브러리 분리 여부

### Step 4. 실행 엔트리포인트 확인

확인 파일:
- `api/src/main/java/moment/ApiApplication.java`
- `admin/src/main/java/moment/AdminApplication.java`

체크:
- 각각 독립 실행 가능한 Spring Boot 메인 클래스 존재

### Step 5. 실제 코드 경계 확인

확인 경로:
- `common/src/main/java/moment/*`
- `api/src/main/java/moment/*`
- `admin/src/main/java/moment/admin/*`

체크:
- `common`에 domain/repository/공통 config 존재
- `api`에 사용자 API 로직 집중
- `admin`에 관리자 API 로직 집중

### Step 6. 설정 파일 분리 확인

확인 파일:
- `api/src/main/resources/application-dev.yml`
- `api/src/main/resources/application-prod.yml`
- `admin/src/main/resources/application-dev.yml`
- `admin/src/main/resources/application-prod.yml`

체크:
- 포트/세션/Flyway/Swagger 설정이 모듈 목적에 맞게 분리되었는지

### Step 7. DB 마이그레이션 위치 확인

확인 경로:
- `common/src/main/resources/db/migration/mysql`
- `common/src/testFixtures/resources/db/migration/h2`

체크:
- 운영(MySQL)과 테스트(H2) 마이그레이션이 common에 집약되어 있는지

### Step 8. 테스트 공유자원 확인

확인 경로:
- `common/src/testFixtures/java/moment/fixture`
- `api/src/test/java/*`
- `admin/src/test/java/*`

체크:
- 양쪽 모듈 테스트가 공통 fixture를 import해서 사용하는지

### Step 9. 빌드/테스트 실행 방식 확인

권장 명령:

```bash
./gradlew -q projects
./gradlew -m fastTest e2eTest
./gradlew :common:testFixturesJar
```

체크:
- 프로젝트 인식, 태스크 선택, fixtures jar 생성이 정상인지

### Step 10. 배포 구조 정합성 확인(중요)

확인 파일:
- `Dockerfile`
- `docker-compose.yml`
- `../.github/workflows/prod-server-ci.yml`
- `../.github/workflows/prod-server-cd.yml`
- `buildspec.yml`
- `scripts/start_server.sh`

현재 상태:
- 배포는 아직 `app` 단일 컨테이너 기준
- 루트 Dockerfile이 `build/libs/*.jar`만 복사

근거:
- `Dockerfile:5`
- `docker-compose.yml:24`
- `../.github/workflows/prod-server-ci.yml:105`
- `../.github/workflows/prod-server-cd.yml:56`
- `buildspec.yml:31`
- `scripts/start_server.sh:32`

## 7. 현재 구조의 해석

1. 코드 구조 관점: 멀티 모듈 적용 완료 상태
2. 테스트 구조 관점: `common` testFixtures 재사용 구조 정착
3. 운영/배포 관점: 단일 app 배포 잔재 존재
4. 문서 관점: `docs/plans/multi-module/phase-6-deployment.md`는 아직 `PLANNED` 상태

근거:
- `docs/plans/multi-module/phase-6-deployment.md:4`

## 8. 다음 확인 우선순위 (권장)

1. 배포 기준을 `api`/`admin` 분리로 실제 적용할지 결정
2. 적용 시 Dockerfile/compose/CI/CD를 모듈별로 분리
3. 분리 전후 `./gradlew :api:build :admin:build` 및 배포 smoke test 수행
