# Notification Domain (PREFIX: NTF)

> Last Updated: 2026-02-12
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
    - Domain: `PushNotificationService`
    - Entity: `PushNotification`
    - Repository: `PushNotificationRepository` (`existsByUserAndDeviceEndpoint`, `deleteByDeviceEndpoint`)
- **Business Rules**: Firebase 디바이스 토큰 등록, 동일 사용자+동일 토큰 중복 등록 방지, 다른 사용자가 동일 디바이스 토큰 등록 시 기존 등록 삭제 (디바이스 1대 = 1사용자)
- **Dependencies**: 없음 (외부: Firebase)
- **Tests**: `PushNotificationServiceTest` (4개: 저장/중복방지/타사용자 동일토큰/삭제), `PushNotificationRepositoryTest` (7개: 저장/조회/없음/존재여부/디바이스토큰삭제/사용자+토큰삭제), `PushNotificationControllerTest` (E2E)

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

- `NotificationFacadeService`: DeepLinkGenerator로 딥링크 생성 → 알림 저장 → SSE 전송 → Push 전송 (메인 진입점)
- `NotificationEventHandler`: 7개 이벤트 handler (6개 활성, 1개 미발행) - 모든 알림의 트리거
- `@Async` + `@TransactionalEventListener(AFTER_COMMIT)` 패턴
- `SourceData`: JSON 기반 알림 원본 데이터 값 객체 (NotificationPayload 대체)
- `DeepLinkGenerator`: NotificationType + SourceData 기반 클라이언트 딥링크 생성 (순수 도메인 로직)
- `SourceDataConverter`: JPA @Converter로 SourceData ↔ JSON 자동 변환

### 차단 사용자 알림 필터링

`NotificationEventHandler`의 4개 이벤트 핸들러에 차단 확인 로직 추가:
- `handleCommentCreateEvent`: 댓글 작성자 ↔ 모멘트 소유자 차단 시 알림 스킵
- `handleMomentLikeEvent`: 좋아요 누른 사용자 ↔ 모멘트 소유자 차단 시 알림 스킵
- `handleCommentLikeEvent`: 좋아요 누른 사용자 ↔ 댓글 소유자 차단 시 알림 스킵
- `handleGroupCommentCreateEvent`: 댓글 작성자 ↔ 모멘트 소유자 차단 시 알림 스킵

자기 자신에게 알림 방지(self-notification skip) 로직도 함께 추가.

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

> **Note**: `EchoCreateEvent` record는 멀티모듈 전환 시 삭제됨 (dead code 제거).

## 관련 엔티티

- `Notification` (@Entity: "notifications") - NotificationType enum, SourceData(JSON), link(VARCHAR 512)
- `PushNotification` (@Entity: "push_notifications")

## 관련 테스트 클래스 (20개)

- Domain: `NotificationTest`, `NotificationTypeTest`, `DeepLinkGeneratorTest`, `SourceDataTest`, `PushNotificationMessageTest`
- Infrastructure: `EmittersTest`, `ExpoPushNotificationSenderTest`, `NotificationRepositoryTest`, `PushNotificationRepositoryTest`, `SourceDataConverterTest`
- Service: `NotificationApplicationServiceTest`, `NotificationEventHandlerTest`, `NotificationFacadeServiceTest`, `NotificationServiceTest`, `PushNotificationServiceTest`, `SseNotificationServiceTest`
- DTO: `NotificationResponseTest`, `NotificationSseResponseTest`
- E2E: `NotificationControllerTest`, `PushNotificationControllerTest`

## DB 마이그레이션

- V22: `V22__create_pushNotification__mysql.sql`
- V33: `V33__alter_notifications_for_groups.sql`
- V36: `V36__alter_notifications_remove_legacy_add_source_and_link.sql` - target_type, target_id, group_id 컬럼 삭제 → source_data(JSON), link(VARCHAR 512) 추가
- V37: `V37__add_notification_indexes.sql` - (user_id, is_read, notification_type) 복합 인덱스 추가
- V39: `V39__add_push_notification_indexes.sql` - push_notifications 테이블 인덱스 추가: (device_endpoint, deleted_at), (user_id, device_endpoint, deleted_at)
