# User Domain (PREFIX: USER)

> Last Updated: 2026-02-03
> Features: 7

## 기능 목록

### USER-001: 이메일 회원가입

- **Status**: DONE
- **API**: `POST /api/v2/users/signup`
- **Key Classes**:
    - Controller: `UserController`
    - Domain: `UserService`
    - Entity: `User`
    - DTO: `UserCreateRequest`, `UserCreateResponse`
- **Business Rules**: 이메일 중복 검사, 비밀번호 암호화(BCrypt), 닉네임 중복 검사
- **Dependencies**: 없음
- **Tests**: `UserTest`, `UserServiceTest`, `UserControllerTest` (E2E)
- **Error Codes**: U-001 (이미 가입된 사용자), U-003 (닉네임 중복)

### USER-002: 현재 사용자 조회

- **Status**: DONE
- **API**: `GET /api/v2/users/me`
- **Key Classes**:
    - Controller: `UserController`
    - Domain: `UserService`
    - DTO: `UserProfileResponse`
- **Business Rules**: JWT 인증 후 현재 사용자 정보 반환
- **Dependencies**: auth (JWT)
- **Tests**: `UserControllerTest` (E2E)
- **Error Codes**: U-009 (존재하지 않는 사용자)

### USER-003: 닉네임 중복 확인

- **Status**: DONE
- **API**: `POST /api/v2/users/signup/nickname/check`
- **Key Classes**:
    - Controller: `UserController`
    - Domain: `UserService`
- **Business Rules**: 닉네임 중복 여부 boolean 반환
- **Dependencies**: 없음
- **Tests**: `UserServiceTest`, `UserControllerTest` (E2E)

### USER-004: 랜덤 닉네임 생성

- **Status**: DONE
- **API**: `GET /api/v2/users/signup/nickname`
- **Key Classes**:
    - Controller: `UserController`
    - Application: `NicknameGenerateApplicationService`
- **Business Rules**: 중복되지 않는 랜덤 닉네임 생성
- **Dependencies**: 없음
- **Tests**: `MomentRandomNicknameGeneratorTest`, `NicknameGenerateApplicationServiceTest`
- **Error Codes**: U-010 (닉네임 생성 불가)

### USER-005: 마이페이지 프로필 조회

- **Status**: DONE
- **API**: `GET /api/v2/me/profile`
- **Key Classes**:
    - Controller: `MyPageController`
    - Facade: `MyPageFacadeService`
- **Business Rules**: 사용자 프로필 + 통계 정보 반환
- **Dependencies**: moment, comment
- **Tests**: `MyPageControllerTest` (E2E)

### USER-006: 닉네임 변경

- **Status**: DONE
- **API**: `POST /api/v2/me/nickname`
- **Key Classes**:
    - Controller: `MyPageController`
    - Domain: `UserService`
- **Business Rules**: 닉네임 중복 검사 후 변경
- **Dependencies**: 없음
- **Tests**: `UserServiceTest`, `MyPageControllerTest` (E2E)
- **Error Codes**: U-003 (닉네임 중복), U-004 ~ U-006 (유효성 검증)

### USER-007: 비밀번호 변경

- **Status**: DONE
- **API**: `POST /api/v2/me/password`
- **Key Classes**:
    - Controller: `MyPageController`
    - Domain: `UserService`
- **Business Rules**: 현재 비밀번호 확인 후 변경, 소셜 로그인 계정은 변경 불가
- **Dependencies**: 없음
- **Tests**: `UserServiceTest`, `MyPageControllerTest` (E2E)
- **Error Codes**: U-007 (비밀번호 불일치), U-012 (비밀번호 동일), U-013 (소셜 로그인 비밀번호 변경 불가)

## 관련 에러 코드

- U-001: 이미 가입된 사용자 (409)
- U-002: 로그인 실패 (400)
- U-003: 닉네임 중복 (409)
- U-004 ~ U-006: 유효성 검증 (400)
- U-007: 비밀번호 불일치 (400)
- U-008: 권한 없음 (401)
- U-009: 존재하지 않는 사용자 (404)
- U-010: 닉네임 생성 불가 (409)
- U-012: 비밀번호 동일 (400)
- U-013: 소셜 로그인 비밀번호 변경 불가 (400)

## 관련 엔티티

- `User` (@Entity: "users") - fields: id, email, password, nickname, providerType, deletedAt

## 관련 테스트 클래스 (7개)

- `UserTest`, `MomentRandomNicknameGeneratorTest`
- `UserRepositoryTest`
- `NicknameGenerateApplicationServiceTest`, `UserServiceTest`
- `MyPageControllerTest` (E2E), `UserControllerTest` (E2E)

## DB 마이그레이션

- V1: 초기 스키마
- V2: `V2__alter_users__mysql.sql`
- V6: `V6__alter_users__mysql.sql`
