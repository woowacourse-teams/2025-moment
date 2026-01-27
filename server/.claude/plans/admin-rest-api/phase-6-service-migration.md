# Phase 6: 서비스 레이어 마이그레이션

## 목표
기존 서비스에서 사용하는 `MomentException(ErrorCode.A_XXX)`를 `AdminException(AdminErrorCode.XXX)`로 변경하여 Admin 모듈 독립성 확보

---

## 작업 대상 파일

| 파일 | 변경 내용 |
|------|----------|
| AdminService.java | MomentException → AdminException |
| AdminUserService.java | MomentException → AdminException |
| AdminSessionService.java | MomentException → AdminException |
| AdminManagementApplicationService.java | MomentException → AdminException |
| ErrorCode.java | Admin 관련 코드 (A-001 ~ A-009) 제거 |

---

## 작업 목록

### 6.1 AdminService.java 수정

**파일**: `server/src/main/java/moment/admin/service/admin/AdminService.java`

**변경 사항**:
```java
// 기존 import 제거
- import moment.global.exception.ErrorCode;
- import moment.global.exception.MomentException;

// 새 import 추가
+ import moment.admin.global.exception.AdminErrorCode;
+ import moment.admin.global.exception.AdminException;

// 예외 던지는 부분 변경
- throw new MomentException(ErrorCode.ADMIN_LOGIN_FAILED);
+ throw new AdminException(AdminErrorCode.LOGIN_FAILED);

- throw new MomentException(ErrorCode.ADMIN_NOT_FOUND);
+ throw new AdminException(AdminErrorCode.NOT_FOUND);

- throw new MomentException(ErrorCode.ADMIN_DUPLICATE_EMAIL);
+ throw new AdminException(AdminErrorCode.DUPLICATE_EMAIL);
```

**ErrorCode 매핑**:
| 기존 (ErrorCode) | 신규 (AdminErrorCode) |
|-----------------|---------------------|
| ADMIN_LOGIN_FAILED | LOGIN_FAILED |
| ADMIN_NOT_FOUND | NOT_FOUND |
| ADMIN_UNAUTHORIZED | UNAUTHORIZED |
| ADMIN_DUPLICATE_EMAIL | DUPLICATE_EMAIL |
| ADMIN_INVALID_INPUT | INVALID_INPUT |
| ADMIN_CANNOT_BLOCK_SELF | CANNOT_BLOCK_SELF |
| ADMIN_CANNOT_BLOCK_LAST_SUPER_ADMIN | CANNOT_BLOCK_LAST_SUPER_ADMIN |
| ADMIN_SESSION_NOT_FOUND | SESSION_NOT_FOUND |
| ADMIN_SESSION_EXPIRED | SESSION_EXPIRED |

---

### 6.2 AdminUserService.java 수정

**파일**: `server/src/main/java/moment/admin/service/user/AdminUserService.java`

**변경 사항**:
```java
// 기존 import 제거
- import moment.global.exception.ErrorCode;
- import moment.global.exception.MomentException;

// 새 import 추가
+ import moment.admin.global.exception.AdminErrorCode;
+ import moment.admin.global.exception.AdminException;

// 사용자를 찾지 못할 때
- throw new MomentException(ErrorCode.USER_NOT_FOUND);
+ throw new AdminException(AdminErrorCode.NOT_FOUND);
// 또는 User 관련 에러 코드가 필요하면 AdminErrorCode에 추가
```

**참고**: User 도메인 관련 에러는 별도 코드 추가 검토
```java
// AdminErrorCode에 추가 (필요 시)
USER_NOT_FOUND("A-010", "존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND),
```

---

### 6.3 AdminSessionService.java 수정

**파일**: `server/src/main/java/moment/admin/service/session/AdminSessionService.java`

**변경 사항**:
```java
// 기존 import 제거
- import moment.global.exception.ErrorCode;
- import moment.global.exception.MomentException;

// 새 import 추가
+ import moment.admin.global.exception.AdminErrorCode;
+ import moment.admin.global.exception.AdminException;

// 세션을 찾지 못할 때
- throw new MomentException(ErrorCode.ADMIN_SESSION_NOT_FOUND);
+ throw new AdminException(AdminErrorCode.SESSION_NOT_FOUND);
```

---

### 6.4 AdminManagementApplicationService.java 수정

**파일**: `server/src/main/java/moment/admin/service/application/AdminManagementApplicationService.java`

**변경 사항**:
```java
// 기존 import 제거
- import moment.global.exception.ErrorCode;
- import moment.global.exception.MomentException;

// 새 import 추가
+ import moment.admin.global.exception.AdminErrorCode;
+ import moment.admin.global.exception.AdminException;

// 자기 자신 차단 시도
- throw new MomentException(ErrorCode.ADMIN_CANNOT_BLOCK_SELF);
+ throw new AdminException(AdminErrorCode.CANNOT_BLOCK_SELF);

// 마지막 SUPER_ADMIN 차단 시도
- throw new MomentException(ErrorCode.ADMIN_CANNOT_BLOCK_LAST_SUPER_ADMIN);
+ throw new AdminException(AdminErrorCode.CANNOT_BLOCK_LAST_SUPER_ADMIN);
```

---

### 6.5 AdminSessionManager.java 수정

**파일**: `server/src/main/java/moment/admin/global/util/AdminSessionManager.java`

**변경 사항**:
```java
// 기존 import 제거
- import moment.global.exception.ErrorCode;
- import moment.global.exception.MomentException;

// 새 import 추가
+ import moment.admin.global.exception.AdminErrorCode;
+ import moment.admin.global.exception.AdminException;

// 권한 없음
- throw new MomentException(ErrorCode.ADMIN_UNAUTHORIZED);
+ throw new AdminException(AdminErrorCode.UNAUTHORIZED);
```

---

### 6.6 ErrorCode.java에서 Admin 코드 제거

**파일**: `server/src/main/java/moment/global/exception/ErrorCode.java`

**제거할 항목**:
```java
// Admin 관련 에러 코드 제거
- ADMIN_LOGIN_FAILED("A-001", "관리자 로그인에 실패했습니다.", HttpStatus.UNAUTHORIZED),
- ADMIN_NOT_FOUND("A-002", "존재하지 않는 관리자입니다.", HttpStatus.NOT_FOUND),
- ADMIN_UNAUTHORIZED("A-003", "관리자 권한이 없습니다.", HttpStatus.FORBIDDEN),
- ADMIN_DUPLICATE_EMAIL("A-004", "이미 등록된 관리자 이메일입니다.", HttpStatus.CONFLICT),
- ADMIN_INVALID_INPUT("A-005", "유효하지 않은 입력 정보입니다.", HttpStatus.BAD_REQUEST),
- ADMIN_CANNOT_BLOCK_SELF("A-006", "자기 자신을 차단할 수 없습니다.", HttpStatus.BAD_REQUEST),
- ADMIN_CANNOT_BLOCK_LAST_SUPER_ADMIN("A-007", "마지막 SUPER_ADMIN은 차단할 수 없습니다.", HttpStatus.BAD_REQUEST),
- ADMIN_SESSION_NOT_FOUND("A-008", "세션을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
- ADMIN_SESSION_EXPIRED("A-009", "세션이 만료되었습니다. 다시 로그인해 주세요.", HttpStatus.UNAUTHORIZED),
```

---

### 6.7 AdminExceptionHandler.java 수정 (SSR용)

**파일**: `server/src/main/java/moment/admin/global/exception/AdminExceptionHandler.java`

기존 SSR용 예외 핸들러도 `AdminException`을 처리하도록 수정:

```java
// 기존 MomentException 처리 부분 수정
@ExceptionHandler(AdminException.class)
public String handleAdminException(AdminException e, Model model) {
    AdminErrorCode errorCode = e.getErrorCode();

    // 인증 관련 에러는 로그인 페이지로 리다이렉트
    if (errorCode == AdminErrorCode.UNAUTHORIZED ||
        errorCode == AdminErrorCode.NOT_FOUND ||
        errorCode == AdminErrorCode.LOGIN_FAILED ||
        errorCode == AdminErrorCode.SESSION_EXPIRED) {
        return "redirect:/admin/login?error=" + errorCode.getMessage();
    }

    model.addAttribute("errorCode", errorCode.getCode());
    model.addAttribute("errorMessage", errorCode.getMessage());
    model.addAttribute("statusCode", errorCode.getStatus().value());
    return "admin/error/error";
}
```

---

## 검증 방법

1. 빌드 확인
```bash
./gradlew build
```

2. 기존 테스트 실행
```bash
./gradlew test
```

3. Admin 기능 통합 테스트
- SSR 로그인/로그아웃 동작 확인
- REST API 로그인/로그아웃 동작 확인
- 에러 발생 시 적절한 응답 확인

---

## 완료 조건

- [ ] AdminService.java 수정
- [ ] AdminUserService.java 수정
- [ ] AdminSessionService.java 수정
- [ ] AdminManagementApplicationService.java 수정
- [ ] AdminSessionManager.java 수정
- [ ] ErrorCode.java에서 Admin 코드 제거
- [ ] AdminExceptionHandler.java 수정 (SSR용)
- [ ] 빌드 성공
- [ ] 기존 테스트 통과
- [ ] SSR Admin 기능 정상 동작
- [ ] REST API Admin 기능 정상 동작
