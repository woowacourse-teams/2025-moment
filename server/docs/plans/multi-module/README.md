# 멀티모듈 마이그레이션 — 상세 구현 계획

> Created: 2026-02-11
> Branch: refactor/#1087
> 원본 계획: [multi-module-migration.md](./multi-module-migration.md)
> 리뷰 결과: [multi-module-migration-review.md](./multi-module-migration-review.md)

## 개요

단일 모듈 모놀리스(`server/src/`)를 **common, api, admin** 3개 Gradle 서브모듈로 분리하는 상세 구현 계획.

## Phase별 문서

| Phase | 파일 | 내용 | 예상 파일 수 |
|-------|------|------|-------------|
| [Phase 0](./phase-0-decoupling.md) | 사전 작업 | 모놀리스 내 결합도 해소 (WebConfig 분리, SuccessResponse 교체, Fixture 분리) | ~10 |
| [Phase 1](./phase-1-gradle-setup.md) | Gradle 설정 | settings.gradle, build.gradle 재구성, 서브모듈 디렉토리 생성 | ~5 |
| [Phase 2](./phase-2-common-module.md) | common 이동 | 엔티티, 레포지토리, 공유 인프라, Flyway, 테스트 픽스쳐 | ~110+ |
| [Phase 3](./phase-3-admin-module.md) | admin 이동 | admin 서비스, 컨트롤러, DTO, 설정, 테스트 | ~80+ |
| [Phase 4](./phase-4-api-module.md) | api 이동 | api 서비스, 컨트롤러, DTO, 이벤트, 설정, 테스트 | ~190+ |
| [Phase 5](./phase-5-cleanup.md) | 정리/검증 | 빌드 검증, src/ 삭제, 컴포넌트 스캔 확인, Flyway 확인 | 삭제 |
| [Phase 6](./phase-6-deployment.md) | 배포 | Dockerfile, docker-compose, CI/CD, 배포 스크립트 | ~10 |

## 실행 전략

### Phase별 atomic 단위

```
Phase 0  →  Phase 1  →  [Phase 2 + 3 + 4]  →  Phase 5  →  Phase 6
(결합해소)   (Gradle)     (atomic: 코드 이동)    (검증/삭제)   (배포)
```

- Phase 0, 1: 각각 독립 커밋 가능 (기존 빌드 유지)
- Phase 2~4: 하나의 atomic 작업 (중간 빌드 불가)
- Phase 5: 빌드 검증 후 src/ 삭제
- Phase 6: 배포 인프라 변경

### 롤백

각 Phase 완료 시 git tag 생성:

```bash
git tag phase-0-complete
git tag phase-1-complete
git tag phase-2-4-complete   # Phase 2~4 합본
git tag phase-5-complete
git tag phase-6-complete
```

## 코드베이스 분석 결과 (계획 작성 시 발견)

### Phase 0 관련

| 발견 사항 | 상태 |
|-----------|------|
| V35 H2 마이그레이션 | `src/main/resources/db/migration/h2/`에 이미 존재. 위치만 정리 필요 |
| AdminFixture dead code | 다른 테스트에서 미사용 (자기 참조만). 엔티티 메서드는 유지, DTO 메서드만 분리 |
| AdminGroupApiController | 18개 메서드에서 `SuccessResponse` 사용 → `AdminSuccessResponse`로 교체 |
| WebConfig | `AuthService` + `AdminAuthInterceptor` 혼재, CORS `allowedOriginPatterns("*")` 보안 취약 |
| SwaggerConfig | API + Admin 태그 혼재. Bean 이름 분리로 충돌 방지 |
| UserFixture DTO 메서드 | 4개 (createUserCreateRequest 등) → UserRequestFixture로 분리 |
| AdminFixture DTO 메서드 | 3개 (createAdminCreateRequest 등) → AdminRequestFixture로 분리 |

### 구조 분석

| 항목 | 개수 |
|------|------|
| 도메인 모듈 | 11 (admin, auth, block, comment, group, like, moment, notification, report, storage, user) |
| Java 소스 파일 (main) | ~180+ |
| Java 테스트 파일 | ~110+ |
| Flyway MySQL 마이그레이션 | V1~V38 (약 36개) |
| Flyway H2 마이그레이션 | V1~V38 (약 36개) |
| 이벤트 DTO | 7개 (CommentCreateEvent, GroupCommentCreateEvent, GroupJoinRequestEvent, GroupJoinApprovedEvent, GroupKickedEvent, MomentLikeEvent, CommentLikeEvent) |
| GitHub Actions 워크플로우 | 11개 (server CI/CD 2개 수정, admin CI/CD 2개 신규) |

### 주의사항

1. **EmailVerificationRepository 부재**: 마이그레이션 계획에 언급되었으나 실제 코드에 없음. skip.
2. **static 리소스 없음**: `src/main/resources/static/admin/css/` 디렉토리 비어있음. 이동 불필요.
3. **Notification infrastructure 결정**: Expo push 관련 클래스를 common에 유지 (추후 api로 분리 가능).
4. **OpenAPI Bean 충돌**: Phase 0에서 SwaggerConfig 분리 시 Bean 이름(`openAPI`, `adminOpenAPI`)으로 구분.
