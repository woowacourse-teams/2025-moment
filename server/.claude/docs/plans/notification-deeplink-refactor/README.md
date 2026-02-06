# 알림 시스템 딥링크 리팩토링 — 구현 계획

> Created: 2026-02-06
> Spec: `.claude/docs/specs/notification-deeplink-refactor.md`
> Branch: refactor/#1068

## 개요

기존 `target_type` + `target_id` + `group_id` 기반 알림 구조를 `source_data` (JSON) + `link` 기반으로 전면 교체.
기존 알림 데이터 전량 삭제, 하위 호환 없음.

## Phase 구성

| Phase | 파일 | 설명 | 의존성 |
|-------|------|------|--------|
| 1 | `phase-1-domain-foundation.md` | SourceData, SourceDataConverter, DeepLinkGenerator 신규 생성 | 없음 |
| 2 | `phase-2-entity-migration.md` | Notification 엔티티 변경 + Flyway 마이그레이션 | Phase 1 |
| 3 | `phase-3-service-layer.md` | NotificationCommand, Service, Facade, Repository 변경 | Phase 2 |
| 4 | `phase-4-event-handler.md` | NotificationEventHandler 전체 핸들러 SourceData 적용 | Phase 3 |
| 5 | `phase-5-push-deeplink.md` | Push 알림에 link 추가 | Phase 3 |
| 6 | `phase-6-api-response.md` | NotificationResponse, NotificationSseResponse, NotificationPayload 변경 | Phase 3 |
| 7 | `phase-7-external-dependencies.md` | MyGroupMomentPageFacadeService, MyGroupCommentPageFacadeService 호출부 변경 | Phase 3 |
| 8 | `phase-8-test-update.md` | 기존 테스트 갱신 (17개 테스트 파일) | Phase 1~7 전체 |

## TDD 진행 순서

각 Phase 내부에서 TDD 사이클 (Red → Green → Refactor) 을 따름.
**Phase 1**부터 시작하여 순서대로 진행. Phase 간 의존성이 있으므로 순차 실행.

## 영향 받는 파일 전체 목록

### 신규 생성 (4개)
- `notification/domain/SourceData.java`
- `notification/domain/DeepLinkGenerator.java`
- `notification/infrastructure/SourceDataConverter.java`
- `db/migration/mysql/V36__alter_notifications_remove_legacy_add_source_and_link.sql`
- `db/migration/h2/V36__alter_notifications_remove_legacy_add_source_and_link__h2.sql` (테스트)

### 수정 (16개 프로덕션 + 17개 테스트)

**프로덕션 코드**:
1. `notification/domain/Notification.java`
2. `notification/domain/NotificationCommand.java`
3. `notification/domain/NotificationPayload.java` → 삭제
4. `notification/domain/PushNotificationCommand.java`
5. `notification/infrastructure/NotificationRepository.java`
6. `notification/infrastructure/expo/ExpoPushNotificationSender.java`
7. `notification/service/facade/NotificationFacadeService.java`
8. `notification/service/application/NotificationApplicationService.java`
9. `notification/service/application/PushNotificationApplicationService.java`
10. `notification/service/notification/NotificationService.java`
11. `notification/service/eventHandler/NotificationEventHandler.java`
12. `notification/dto/response/NotificationResponse.java`
13. `notification/dto/response/NotificationSseResponse.java`
14. `moment/service/facade/MyGroupMomentPageFacadeService.java`
15. `comment/service/facade/MyGroupCommentPageFacadeService.java`

**테스트 코드**:
1. `notification/domain/NotificationTest.java`
2. `notification/domain/NotificationPayloadTest.java` → 삭제 또는 DeepLinkGeneratorTest로 대체
3. `notification/domain/PushNotificationMessageTest.java` (변경 없음)
4. `notification/dto/response/NotificationResponseTest.java`
5. `notification/infrastructure/NotificationRepositoryTest.java`
6. `notification/infrastructure/expo/ExpoPushNotificationSenderTest.java`
7. `notification/service/facade/NotificationFacadeServiceTest.java`
8. `notification/service/application/NotificationApplicationServiceTest.java`
9. `notification/service/eventHandler/NotificationEventHandlerTest.java`
10. `notification/service/notification/NotificationServiceTest.java`
