# 실행 가이드: 커밋 전략 + 빌드 안정성

> 이 문서는 Phase 1~8을 실제로 진행할 때의 커밋 전략과 주의사항을 정리한다.

---

## 커밋 전략

### 커밋 1: Phase 1 (독립, 빌드 안전)

```
feat: SourceData, SourceDataConverter, DeepLinkGenerator 생성

- SourceData 값 객체 (getLong 헬퍼 포함)
- SourceDataConverter JPA JSON 변환기
- DeepLinkGenerator NotificationType + SourceData 기반 딥링크 생성
- 단위 테스트 추가
```

**검증**: `./gradlew fastTest` ✅ (기존 코드 미수정)

---

### 커밋 2: Phase 2~7 일괄 (빌드 깨짐 방지)

```
refactor: 알림 시스템 TargetType 제거, source_data + link 기반으로 전환

- Notification 엔티티: targetType, targetId, groupId 삭제 → sourceData, link 추가
- NotificationCommand: TargetType 제거, SourceData 추가
- NotificationRepository: TargetType 쿼리 → NotificationType 기반 조회
- NotificationService/ApplicationService: 새 시그니처 적용
- NotificationFacadeService: DeepLinkGenerator 기반 딥링크 생성
- NotificationEventHandler: 7개 핸들러 SourceData 적용
- PushNotificationCommand: link 필드 추가
- ExpoPushNotificationSender: data에 link 포함
- NotificationResponse/SseResponse: targetType/targetId/groupId 제거, link 추가
- NotificationPayload 삭제
- MyGroupMomentPageFacadeService/MyGroupCommentPageFacadeService 호출부 변경
- Flyway V36 마이그레이션 (기존 데이터 삭제 + 스키마 변경)
```

**이유**: Phase 2에서 Notification 생성자가 변경되면 Phase 3~7까지 완료하지 않으면 컴파일 에러.
단일 커밋으로 원자적 변경 보장.

**검증**: `./gradlew fastTest` ❌ (테스트가 아직 구 API 사용)

---

### 커밋 3: Phase 8 (테스트 갱신)

```
test: 알림 리팩토링에 따른 테스트 코드 전면 갱신

- NotificationPayloadTest 삭제
- NotificationTest: 새 생성자 기반 테스트
- NotificationResponseTest: link 필드 검증
- NotificationRepositoryTest: NotificationType 기반 쿼리 테스트
- NotificationServiceTest: 새 save/조회 메서드 테스트
- NotificationApplicationServiceTest: getUnreadMomentIds/CommentIds 테스트
- NotificationFacadeServiceTest: SourceData 기반 알림 생성 테스트
- NotificationEventHandlerTest: 새 NotificationCommand 검증
- ExpoPushNotificationSenderTest: data.link 포함 검증
```

**검증**: `./gradlew fastTest` ✅

---

## 대안: 커밋 2+3 합치기

Phase 2~8을 하나의 커밋으로 합치는 것도 가능. 장점:
- 모든 커밋에서 빌드 성공 보장
- 단점: 커밋이 매우 커짐

---

## 주의사항

### H2 마이그레이션 차이

| 구문 | MySQL | H2 |
|------|-------|-----|
| JSON 타입 | `JSON` | `TEXT` |
| DROP INDEX | `ALTER TABLE t DROP INDEX idx` | `DROP INDEX IF EXISTS idx` |
| DELETE | `DELETE FROM notifications` | 동일 |

### SourceData의 Jackson 역직렬화 주의

Jackson이 JSON의 정수를 `Integer`로 역직렬화하므로,
`SourceData.getLong("momentId")`에서 `Integer → Long` 변환이 필요.
Phase 1의 `getLong` 구현에서 `Number.longValue()` 처리 포함.

### record의 equals 비교 (테스트 주의)

`SourceData`는 record이므로 `equals()` 비교 시 내부 `Map`의 내용까지 비교됨.
테스트에서 `Map.of("groupId", 1L)`과 핸들러의 `Map.of("groupId", event.groupId())`가
같은 Long 값이어야 verify 통과.

이벤트 메서드가 `long` (primitive)을 반환하면 `Map.of("groupId", event.groupId())`는
auto-boxing으로 `Long` 객체가 됨. `Map.of("groupId", 1L)`과 동일하므로 문제없음.

### TargetType enum은 삭제하지 않음

`TargetType`은 `moment.global.domain` 패키지에 있으며, notification 외의 도메인에서도 사용 가능.
notification 도메인 내에서만 참조를 제거한다.

---

## 파일 변경 요약 (최종)

### 신규 생성 (5개)
| 파일 | Phase |
|------|-------|
| `notification/domain/SourceData.java` | 1 |
| `notification/domain/DeepLinkGenerator.java` | 1 |
| `notification/infrastructure/SourceDataConverter.java` | 1 |
| `db/migration/mysql/V36__alter_notifications_remove_legacy_add_source_and_link.sql` | 2 |
| `db/migration/h2/V36__alter_notifications_remove_legacy_add_source_and_link__h2.sql` | 2 |

### 수정 (14개 프로덕션)
| 파일 | Phase |
|------|-------|
| `notification/domain/Notification.java` | 2 |
| `notification/domain/NotificationCommand.java` | 3 |
| `notification/domain/PushNotificationCommand.java` | 5 |
| `notification/infrastructure/NotificationRepository.java` | 3 |
| `notification/infrastructure/expo/ExpoPushNotificationSender.java` | 5 |
| `notification/service/notification/NotificationService.java` | 3 |
| `notification/service/application/NotificationApplicationService.java` | 3 |
| `notification/service/application/PushNotificationApplicationService.java` | 5 |
| `notification/service/facade/NotificationFacadeService.java` | 3 |
| `notification/service/eventHandler/NotificationEventHandler.java` | 4 |
| `notification/dto/response/NotificationResponse.java` | 6 |
| `notification/dto/response/NotificationSseResponse.java` | 6 |
| `moment/service/facade/MyGroupMomentPageFacadeService.java` | 7 |
| `comment/service/facade/MyGroupCommentPageFacadeService.java` | 7 |

### 삭제 (2개)
| 파일 | Phase |
|------|-------|
| `notification/domain/NotificationPayload.java` | 6 |
| `notification/domain/NotificationPayloadTest.java` | 8 |

### 테스트 신규/수정 (10개)
| 파일 | Phase | 유형 |
|------|-------|------|
| `notification/domain/SourceDataTest.java` | 1 | 신규 |
| `notification/infrastructure/SourceDataConverterTest.java` | 1 | 신규 |
| `notification/domain/DeepLinkGeneratorTest.java` | 1 | 신규 |
| `notification/domain/NotificationTest.java` | 8 | 수정 |
| `notification/dto/response/NotificationResponseTest.java` | 8 | 수정 |
| `notification/infrastructure/NotificationRepositoryTest.java` | 8 | 수정 |
| `notification/service/notification/NotificationServiceTest.java` | 8 | 수정 |
| `notification/service/application/NotificationApplicationServiceTest.java` | 8 | 수정 |
| `notification/service/facade/NotificationFacadeServiceTest.java` | 8 | 수정 |
| `notification/service/eventHandler/NotificationEventHandlerTest.java` | 8 | 수정 |
| `notification/infrastructure/expo/ExpoPushNotificationSenderTest.java` | 8 | 수정 |
