# Moment Domain (PREFIX: MOM)

> Last Updated: 2026-02-03
> Features: 4

## 기능 목록

### MOM-001: 기본 모멘트 생성

- **Status**: DONE
- **API**: `POST /api/v2/moments`
- **Key Classes**:
    - Controller: `MomentController`
    - Facade: `MomentCreateFacadeService`
    - Application: `MomentApplicationService`
    - Domain: `MomentService`
    - Entity: `Moment`
    - DTO: `MomentCreateRequest`, `MomentCreateResponse`
- **Business Rules**: 하루 1회 작성 가능 (`OnceADayPolicy`), WriteType: `BASIC`, 내용 1~200자
- **Dependencies**: user (UserService)
- **Tests**: `MomentTest`, `MomentServiceTest`, `MomentApplicationServiceTest`, `MomentControllerTest` (E2E)
- **Error Codes**: M-001 (내용 비어있음), M-004 (글자수 초과)

### MOM-002: 추가 모멘트 생성

- **Status**: DONE
- **API**: `POST /api/v2/moments/extra`
- **Key Classes**:
    - Controller: `MomentController`
    - Facade: `MomentCreateFacadeService`
    - Application: `MomentApplicationService`
- **Business Rules**: 포인트 소모 (`PointDeductionPolicy`), WriteType: `EXTRA`
- **Dependencies**: user (UserService), reward (포인트)
- **Tests**: `MomentApplicationServiceTest`, `MomentControllerTest` (E2E)

### MOM-003: 기본 작성 가능 여부 확인

- **Status**: DONE
- **API**: `GET /api/v2/moments/writable/basic`
- **Key Classes**:
    - Controller: `MomentController`
    - Application: `MomentApplicationService`
- **Business Rules**: 오늘 기본 모멘트 작성 여부 boolean 반환
- **Dependencies**: user (UserService)
- **Tests**: `MomentApplicationServiceTest`, `MomentControllerTest` (E2E)

### MOM-004: 추가 작성 가능 여부 확인

- **Status**: DONE
- **API**: `GET /api/v2/moments/writable/extra`
- **Key Classes**:
    - Controller: `MomentController`
    - Application: `MomentApplicationService`
- **Business Rules**: 추가 모멘트 작성 가능 여부 (포인트 충분 여부) boolean 반환
- **Dependencies**: user (UserService)
- **Tests**: `MomentApplicationServiceTest`, `MomentControllerTest` (E2E)

## 관련 에러 코드

- M-001: 내용 비어있음 (400)
- M-002: 존재하지 않는 모멘트 (404)
- M-004: 글자수 초과 (400)
- M-005: 유효하지 않은 페이지 사이즈 (400)

## 관련 엔티티

- `Moment` (@Entity: "moments") - implements Cursorable
- `MomentImage` (@Entity: "moment_images")

## 관련 테스트 클래스 (9개)

- `MomentTest`, `MomentGroupContextTest`
- `MomentRepositoryTest`, `MomentImageRepositoryTest`
- `MomentApplicationServiceTest`, `MomentServiceTest`, `MomentImageServiceTest`
- `MyGroupMomentPageFacadeServiceTest`
- `MomentControllerTest` (E2E)

## DB 마이그레이션

- V1: 초기 스키마
- V3, V5: `V3/V5__alter_moments__mysql.sql`
- V13~V15: 이미지 테이블
- V21: `V21__create_moments_index__mysql.sql`
- V30: `V30__alter_moments_for_groups.sql`
