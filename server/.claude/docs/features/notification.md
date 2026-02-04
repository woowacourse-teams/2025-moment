# Notification Domain (PREFIX: NTF)

> Last Updated: 2026-02-04
> Features: 6

## 기능 목록

### NTF-001: SSE 구독

- **Status**: DONE
- **API**: `GET /api/v2/notifications/subscribe`
- **Key Classes**:
    - Controller: `NotificationController`
    - Domain: `SseNotificationService`
    - Infrastructure: `Emitters`
- **Business Rules**: Server-Sent Events로 실시간 알림 스트리밍, 연결 유지
- **Dependencies**: 없음
- **Tests**: `SseNotificationServiceTest`, `EmittersTest`, `NotificationControllerTest` (E2E)

### NTF-002: 알림 목록 조회

- **Status**: DONE
- **API**: `GET /api/v2/notifications`
- **Key Classes**:
    - Controller: `NotificationController`
    - Application: `NotificationApplicationService`
    - DTO: `NotificationResponse`
- **Business Rules**: 커서 기반 페이지네이션
- **Dependencies**: 없음
- **Tests**: `NotificationApplicationServiceTest`, `NotificationResponseTest`, `NotificationControllerTest` (E2E)

### NTF-003: 단건 읽음 처리

- **Status**: DONE
- **API**: `PATCH /api/v2/notifications/{id}/read`
- **Key Classes**:
    - Controller: `NotificationController`
    - Application: `NotificationApplicationService`
- **Business Rules**: 알림 소유자만 읽음 처리 가능
- **Dependencies**: 없음
- **Tests**: `NotificationServiceTest`, `NotificationControllerTest` (E2E)

### NTF-004: 전체 읽음 처리

- **Status**: DONE
- **API**: `PATCH /api/v2/notifications/read-all`
- **Key Classes**:
    - Controller: `NotificationController`
    - Application: `NotificationApplicationService`
    - DTO: `NotificationReadRequest`
- **Business Rules**: `NotificationReadRequest` body로 대상 지정, 현재 사용자의 모든 미읽은 알림을 읽음 처리
- **Dependencies**: 없음
- **Tests**: `NotificationServiceTest`, `NotificationControllerTest` (E2E)

### NTF-005: 디바이스 등록

- **Status**: DONE
- **API**: `POST /api/v2/push-notifications`
- **Key Classes**:
    - Controller: `PushNotificationController`
    - Application: `PushNotificationApplicationService`
    - Entity: `PushNotification`
- **Business Rules**: Firebase 디바이스 토큰 등록
- **Dependencies**: 없음 (외부: Firebase)
- **Tests**: `PushNotificationServiceTest`, `PushNotificationRepositoryTest`, `PushNotificationControllerTest` (E2E)

### NTF-006: 디바이스 해제

- **Status**: DONE
- **API**: `DELETE /api/v2/push-notifications`
- **Key Classes**:
    - Controller: `PushNotificationController`
    - Domain: `PushNotificationService`
- **Business Rules**: Firebase 디바이스 토큰 삭제
- **Dependencies**: 없음
- **Tests**: `PushNotificationServiceTest`, `PushNotificationControllerTest` (E2E)

## 핵심 아키텍처

- `NotificationFacadeService`: SSE + Push + DB 조율 (메인 진입점)
- `NotificationEventHandler`: 7개 이벤트 handler (6개 활성, 1개 미발행) - 모든 알림의 트리거
- `@Async` + `@TransactionalEventListener(AFTER_COMMIT)` 패턴

### 이벤트 구독 (NotificationEventHandler)

| Event | 발행 도메인 | 상태 |
|-------|------------|------|
| `CommentCreateEvent` | comment | ✅ 활성 |
| `GroupCommentCreateEvent` | comment | ⚠️ handler 존재, 미발행 |
| `GroupJoinRequestEvent` | group | ✅ 활성 |
| `GroupJoinApprovedEvent` | group | ✅ 활성 |
| `GroupKickedEvent` | group | ✅ 활성 |
| `MomentLikeEvent` | like | ✅ 활성 |
| `CommentLikeEvent` | like | ✅ 활성 |

> **Note**: `EchoCreateEvent` record는 존재하나 발행/구독 모두 없는 dead code로, 테이블에서 제외.

## 관련 엔티티

- `Notification` (@Entity: "notifications") - NotificationType, TargetType enums
- `PushNotification` (@Entity: "push_notifications")

## 관련 테스트 클래스 (15개)

- Domain: `NotificationTest`, `NotificationTypeTest`
- Infrastructure: `EmittersTest`, `FcmPushNotificationSenderTest`, `NotificationRepositoryTest`, `PushNotificationRepositoryTest`
- Service: `NotificationApplicationServiceTest`, `NotificationEventHandlerTest`, `NotificationFacadeServiceTest`, `NotificationServiceTest`, `PushNotificationServiceTest`, `SseNotificationServiceTest`
- DTO: `NotificationResponseTest`
- E2E: `NotificationControllerTest`, `PushNotificationControllerTest`

## DB 마이그레이션

- V22: `V22__create_pushNotification__mysql.sql`
- V33: `V33__alter_notifications_for_groups.sql`
