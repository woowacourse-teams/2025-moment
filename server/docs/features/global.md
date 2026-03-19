# Global Domain (PREFIX: GLB)

> Last Updated: 2026-02-03
> Features: 7

## 기능 목록

### GLB-001: BaseEntity (감사 필드)

- **Status**: DONE
- **API**: N/A (인프라)
- **Key Classes**:
    - Entity: `BaseEntity`
- **Business Rules**: `@CreatedDate` 자동 감사, 모든 엔티티가 상속
- **Notes**: `@EntityListeners(AuditingEntityListener.class)`, `@MappedSuperclass`

### GLB-002: ErrorCode + MomentException

- **Status**: DONE
- **API**: N/A (인프라)
- **Key Classes**:
    - Exception: `ErrorCode`, `MomentException`, `GlobalExceptionHandler`
- **Business Rules**: 모든 비즈니스 예외는 `MomentException(ErrorCode)` 사용
- **Notes**: ErrorCode 형식 `{PREFIX}-{NUMBER}`, HttpStatus 매핑

### GLB-003: Cursor 기반 페이지네이션

- **Status**: DONE
- **API**: N/A (인프라)
- **Key Classes**:
    - Infrastructure: `Cursor`, `PageSize`, `Cursorable`
- **Business Rules**: 커서 형식 `{createdAt}_{id}`, size+1 fetch로 다음 페이지 감지
- **Notes**: User API 전용, Admin API는 오프셋 기반

### GLB-004: Soft Delete 패턴

- **Status**: DONE
- **API**: N/A (인프라)
- **Key Classes**:
    - Annotation: `@SQLDelete`, `@SQLRestriction`
- **Business Rules**: `deleted_at IS NULL` 조건 자동 적용, Hard Delete 금지
- **Notes**: 모든 엔티티에 적용 필수

### GLB-005: Logstash 구조화 로깅

- **Status**: DONE
- **API**: N/A (인프라)
- **Key Classes**:
    - Infrastructure: `ControllerLogAspect`
- **Business Rules**: AOP 기반 컨트롤러 요청/응답 로깅, Logstash JSON 포맷
- **Notes**: `@Slf4j` + `kv()` 패턴 사용

### GLB-006: SuccessResponse / ErrorResponse

- **Status**: DONE
- **API**: N/A (인프라)
- **Key Classes**:
    - DTO: `SuccessResponse`, `ErrorResponse`
- **Business Rules**: 모든 API 응답은 `SuccessResponse.of(HttpStatus, data)` 래핑
- **Notes**: Admin API는 별도 `AdminSuccessResponse` / `AdminErrorResponse` 사용

### GLB-007: Health Check

- **Status**: DONE
- **API**: `GET /health`
- **Key Classes**:
    - Controller: `HealthCheckController`
- **Business Rules**: 서버 상태 확인용 엔드포인트
- **Tests**: 없음

## 관련 테스트 클래스 (1개)

- `TargetTypeTest`

## DB 마이그레이션

- 없음 (글로벌 인프라 모듈)
