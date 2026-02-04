# 알림 기능 전면 리팩토링 - 현황 파악 및 분석 계획

**Status**: DRAFT
**Created**: 2026-02-03
**Complexity**: HIGH

---

## 1. 요구사항 재진술

Moment 프로젝트의 알림(Notification) 시스템을 전면 리팩토링하기 위해, 현재 코드가 어떻게 분포되어 있고 어떤 문제점이 있는지 체계적으로 파악한다.

---

## 2. 현재 알림 시스템 전체 지도

### 2.1 모듈 구성 (24개 소스 파일 + 15개 테스트)

```
notification/
├── domain/                         # 6 files
│   ├── Notification.java           # 알림 엔티티 (soft delete)
│   ├── NotificationType.java       # 7개 알림 유형 enum (+1 미사용)
│   ├── PushNotification.java       # 디바이스 토큰 엔티티
│   ├── PushNotificationCommand.java
│   ├── PushNotificationMessage.java # 6개 푸시 메시지 enum
│   └── PushNotificationSender.java  # 푸시 발송 인터페이스
├── dto/                            # 4 files
│   ├── request/  (DeviceEndpointRequest, NotificationReadRequest)
│   └── response/ (NotificationResponse, NotificationSseResponse)
├── infrastructure/                 # 4 files
│   ├── Emitters.java               # SSE ConcurrentHashMap 관리
│   ├── FcmPushNotificationSender.java
│   ├── NotificationRepository.java
│   └── PushNotificationRepository.java
├── presentation/                   # 2 files
│   ├── NotificationController.java  # 4 endpoints (SSE 구독 + CRUD)
│   └── PushNotificationController.java # 2 endpoints
└── service/                        # 8 files
    ├── application/ (NotificationApplicationService, PushNotificationApplicationService)
    ├── eventHandler/ (NotificationEventHandler - 7개 핸들러)
    ├── facade/ (NotificationFacadeService)
    ├── notification/ (NotificationService, PushNotificationService, SseNotificationService)
    └── EmailNotificationService.java  # 전체 주석 처리 (미사용)
```

### 2.2 이벤트 발행-소비 매핑 (4개 모듈 → 8개 이벤트)

| 이벤트 | 발행 모듈/클래스 | 핸들러 | NotificationType | 상태 |
|--------|-----------------|--------|-----------------|------|
| `CommentCreateEvent` | comment / `CommentCreateFacadeService` | ✅ 있음 | `NEW_COMMENT_ON_MOMENT` | **활성** |
| `EchoCreateEvent` | comment / **발행처 없음** | ❌ 없음 | `NEW_REPLY_ON_COMMENT` | **Dead Code** |
| `GroupCommentCreateEvent` | comment / **발행처 없음** | ✅ 있음 | `NEW_COMMENT_ON_MOMENT` | **미완성** (핸들러만 존재) |
| `GroupJoinRequestEvent` | group / `GroupMemberApplicationService` | ✅ 있음 | `GROUP_JOIN_REQUEST` | **활성** |
| `GroupJoinApprovedEvent` | group / `GroupMemberApplicationService` | ✅ 있음 | `GROUP_JOIN_APPROVED` | **활성** |
| `GroupKickedEvent` | group / `GroupMemberApplicationService` | ✅ 있음 | `GROUP_KICKED` | **활성** |
| `MomentLikeEvent` | like / `MomentLikeService` | ✅ 있음 | `MOMENT_LIKED` | **활성** |
| `CommentLikeEvent` | like / `CommentLikeService` | ✅ 있음 | `COMMENT_LIKED` | **활성** |

### 2.3 알림 전달 채널

| 채널 | 구현체 | 상태 |
|------|--------|------|
| DB 저장 | `NotificationRepository` | 활성 |
| SSE (실시간) | `SseNotificationService` + `Emitters` | 활성 (10분 타임아웃, 25초 하트비트) |
| Firebase Push | `FcmPushNotificationSender` | 활성 (Optional 구성) |
| Email 다이제스트 | `EmailNotificationService` | **비활성** (전체 주석 처리) |

### 2.4 외부 모듈의 알림 소비

| 소비 클래스 | 모듈 | 호출하는 notification 메서드 |
|------------|------|---------------------------|
| `MyGroupMomentPageFacadeService` | moment | `getUnreadNotifications()`, `getNotificationsByTargetIdsAndTargetType()` |
| `MyGroupCommentPageFacadeService` | comment | 동일 |
| `MomentNotificationResponse` | moment | 알림 상태 DTO |
| `CommentNotificationResponse` | comment | 알림 상태 DTO |

### 2.5 API 엔드포인트 (6개)

| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/v2/notifications/subscribe` | SSE 구독 |
| GET | `/api/v2/notifications` | 알림 목록 (read 필터) |
| PATCH | `/api/v2/notifications/{id}/read` | 단건 읽음 |
| PATCH | `/api/v2/notifications/read-all` | 다건 읽음 |
| POST | `/api/v2/push-notifications` | 디바이스 등록 |
| DELETE | `/api/v2/push-notifications` | 디바이스 삭제 |

---

## 3. 발견된 문제점 (심각도별)

### HIGH - 버그 또는 심각한 설계 문제

| # | 문제 | 파일 | 상세 |
|---|------|------|------|
| H1 | Self-notification 방지 불일치 | `NotificationEventHandler.java` | 7개 핸들러 중 1개(`handleGroupCommentCreateEvent`)만 self-notification 방지 로직 있음. Like 이벤트는 발행처에서 방지하지만, Comment는 미방지 |
| H2 | `GroupCommentCreateEvent` 미발행 | 발행처 없음 | 핸들러와 이벤트 클래스 존재하나 실제 publish 하는 코드 없음 (미완성 기능) |
| H3 | `EchoCreateEvent` / `NEW_REPLY_ON_COMMENT` Dead Code | `EchoCreateEvent.java`, `NotificationType.java` | 이벤트 정의됨, 타입 정의됨, 그러나 발행처도 핸들러도 없음 |
| H4 | `NotificationService` 중복 save 메서드 | `NotificationService.java` | `saveNotificationWithNewTransaction()`과 `saveNotificationWithGroupId()`가 거의 동일 (groupId null 여부만 다름) |
| H5 | `Boolean` vs `boolean` 타입 혼용 | `NotificationRepository.java`, `NotificationService.java` | 쿼리 파라미터에서 nullable `Boolean` 사용 → NPE 위험 |
| H6 | `PushNotificationApplicationService.sendToDeviceEndpoint()` 트랜잭션 누락 | `PushNotificationApplicationService.java` | 클래스 레벨 `readOnly=true` 상속, 별도 `@Transactional` 없음 |

### MEDIUM - 설계 개선 필요

| # | 문제 | 파일 | 상세 |
|---|------|------|------|
| M1 | 이벤트 필드 구조 불일치 | 8개 이벤트 클래스 | `CommentCreateEvent`는 commenter 정보 없음, `GroupCommentCreateEvent`는 풍부. 일관성 없음 |
| M2 | FacadeService 파라미터 과다 | `NotificationFacadeService.java` | `createNotificationAndSendSseAndPush()` 6개 파라미터 |
| M3 | `NotificationType`-`PushNotificationMessage` 비대칭 | 두 enum | 8개 타입 vs 6개 메시지. 매핑 보장 안 됨 (컴파일 타임 검증 불가) |
| M4 | `Notification.checkNotification()` 메서드명 | `Notification.java` | 의미 불명확 → `markAsRead()`가 적절 |
| M5 | `SseNotificationService` 얇은 위임 | `SseNotificationService.java` | 비즈니스 로직 없이 `Emitters`에 위임만. 레이어 존재 의미 불분명 |
| M6 | Notification 생성자 검증 부재 | `Notification.java` | user/notificationType/targetType null 허용 |
| M7 | ApplicationService 래퍼 메서드 | `NotificationApplicationService.java` | `getUnreadNotifications()`이 `getUnreadTargetIdsBy()`를 그대로 호출 |
| M8 | SSE 단일 서버 한계 | `Emitters.java` | `ConcurrentHashMap` 기반 → 다중 서버 환경에서 동작 불가 |

### LOW - 코드 품질

| # | 문제 | 상세 |
|---|------|------|
| L1 | `handleCommentCreateEvent()`에만 로깅 누락 | 다른 6개 핸들러는 `log.info()` 있음 |
| L2 | `PushNotificationMessage` 하드코딩 title `"[moment]"` | 브랜딩 변경 시 전체 수정 필요 |
| L3 | `Emitters` 로깅 레벨 불일치 | send 실패는 `error`, 연결 끊김은 `info` |
| L4 | `EmailNotificationService` 전체 주석 처리 | 삭제 또는 활성화 결정 필요 |

---

## 4. 파악 항목 체크리스트

리팩토링 전 반드시 확인해야 할 8개 영역:

### Phase 1: 도메인 모델 (핵심 구조 이해)
- [ ] `Notification.java` - 엔티티 필드, 연관관계, 생성자 검증
- [ ] `NotificationType.java` - 8개 타입 중 실제 사용되는 것 확인
- [ ] `PushNotificationMessage.java` - NotificationType과의 매핑 관계
- [ ] `TargetType.java` - 4개 값(COMMENT, MOMENT, GROUP, GROUP_MEMBER) 사용처
- [ ] `PushNotification.java` - 사용자당 다중 디바이스 관계 확인

### Phase 2: 이벤트 흐름 (모듈 간 통신)
- [ ] 8개 이벤트 클래스의 필드 구조 비교 → 통일 방안
- [ ] 각 이벤트 발행 조건 확인 (self-action 제외 로직 위치)
- [ ] `EchoCreateEvent`, `GroupCommentCreateEvent` 미발행 이유 확인 → 삭제/완성 결정
- [ ] `NotificationEventHandler` 7개 핸들러 에러 처리 방식

### Phase 3: 외부 의존성 (영향 범위 확정)
- [ ] `MyGroupMomentPageFacadeService`의 notification 직접 의존
- [ ] `MyGroupCommentPageFacadeService`의 notification 직접 의존
- [ ] `MomentNotificationResponse` / `CommentNotificationResponse` 로직 검증
- [ ] 모듈 간 순환 의존 여부 (notification → user, moment/comment → notification)

### Phase 4: 서비스 레이어 (내부 책임 분배)
- [ ] Facade → Application → Domain Service 각 레이어 책임 명확한지
- [ ] `NotificationService`의 두 save 메서드 통합 가능 여부
- [ ] `NotificationApplicationService`의 불필요 래퍼 메서드 정리

### Phase 5: SSE 구현 (실시간 전달)
- [ ] `Emitters` 동시성 처리 적절성
- [ ] 타임아웃(10분), 하트비트(25초) 설정 근거
- [ ] 사용자당 1개 연결 제한의 한계
- [ ] 다중 서버 환경 대응 방안

### Phase 6: Push 알림 (Firebase)
- [ ] `FcmPushNotificationSender` 비동기 발송 + 실패 처리
- [ ] 디바이스 토큰 자동 정리 로직
- [ ] `FcmConfig` Optional 처리 (로컬 개발 환경)

### Phase 7: 테스트 커버리지
- [ ] 15개 테스트 파일 커버리지 수준
- [ ] 이벤트 핸들러 비동기 테스트 방식
- [ ] SSE 동시성/타임아웃 테스트

### Phase 8: 레거시/Dead Code
- [ ] `EmailNotificationService` (주석 처리) → 삭제 or 활성화
- [ ] `EchoCreateEvent` → 삭제 or 완성
- [ ] `GroupCommentCreateEvent` → 발행 로직 추가 or 삭제
- [ ] `NEW_REPLY_ON_COMMENT` NotificationType → 삭제 or 완성

---

## 5. 잠재적 리팩토링 방향 (사전 식별)

| # | 방향 | 현재 | 개선안 |
|---|------|------|--------|
| 1 | 이벤트 구조 통일 | 필드 제각각 | 공통 base 인터페이스 또는 표준 필드셋 정의 |
| 2 | 파라미터 객체 도입 | 6개 파라미터 직접 전달 | Command/Request 객체로 묶기 |
| 3 | NotificationType-Message 매핑 | 별개 enum, 런타임 매핑 | 하나의 enum에 통합 또는 컴파일타임 매핑 보장 |
| 4 | EventHandler 분리 또는 패턴화 | 단일 클래스 7개 메서드 | 도메인별 핸들러 분리 or 템플릿 메서드 |
| 5 | self-notification 방지 일원화 | 발행처/핸들러에 분산 | 한 곳에서 일관되게 처리 |
| 6 | 서비스 레이어 정리 | Facade→Application→Domain 3단계 | 불필요 위임 제거, 책임 재정의 |
| 7 | Dead Code 정리 | 4개 미사용 코드 | 삭제 또는 완성 결정 |
| 8 | SSE 스케일링 | in-memory ConcurrentHashMap | Redis Pub/Sub 등 외부 브로커 도입 검토 |

---

## 6. 리스크

| 수준 | 리스크 | 설명 |
|------|--------|------|
| HIGH | 외부 모듈 영향 | notification 리팩토링이 moment, comment, group, like 4개 모듈에 영향 |
| HIGH | 이벤트 흐름 누락 | 런타임에만 확인 가능한 경로 놓칠 수 있음 |
| MEDIUM | 테스트 결합도 | 15개 테스트가 현재 구조에 강하게 결합 가능 |
| MEDIUM | SSE 안정성 | 현재 구현 한계 미파악 상태에서 변경 시 퇴보 가능 |
| LOW | DB 마이그레이션 | 스키마 변경 시 Flyway 복잡도 증가 |

---

## 7. 권장 분석 순서

```
Phase 1 (도메인 모델) → 핵심 구조 이해
    ↓
Phase 2 (이벤트 흐름) → 모듈 간 통신 전수 파악
    ↓
Phase 3 (외부 의존성) → 리팩토링 영향 범위 확정
    ↓
Phase 4 (서비스 레이어) → 내부 책임 재설계 방향 도출
    ↓
Phase 5-6 (SSE/Push) → 전달 채널별 세부 개선점
    ↓
Phase 8 (레거시) → Dead Code 처리 결정
    ↓
Phase 7 (테스트) → 리팩토링 안전망 확인
```

---

## 8. 주요 파일 경로 (빠른 참조)

**Notification 모듈 핵심:**
- `src/main/java/moment/notification/domain/Notification.java`
- `src/main/java/moment/notification/domain/NotificationType.java`
- `src/main/java/moment/notification/service/eventHandler/NotificationEventHandler.java`
- `src/main/java/moment/notification/service/facade/NotificationFacadeService.java`
- `src/main/java/moment/notification/service/application/NotificationApplicationService.java`
- `src/main/java/moment/notification/service/notification/NotificationService.java`
- `src/main/java/moment/notification/service/notification/SseNotificationService.java`
- `src/main/java/moment/notification/infrastructure/Emitters.java`

**이벤트 발행처:**
- `src/main/java/moment/comment/service/facade/CommentCreateFacadeService.java`
- `src/main/java/moment/group/service/application/GroupMemberApplicationService.java`
- `src/main/java/moment/like/service/MomentLikeService.java`
- `src/main/java/moment/like/service/CommentLikeService.java`

**외부 소비처:**
- `src/main/java/moment/moment/service/facade/MyGroupMomentPageFacadeService.java`
- `src/main/java/moment/comment/service/facade/MyGroupCommentPageFacadeService.java`

**Dead Code 후보:**
- `src/main/java/moment/notification/service/EmailNotificationService.java`
- `src/main/java/moment/comment/dto/EchoCreateEvent.java`
- `src/main/java/moment/comment/dto/event/GroupCommentCreateEvent.java`