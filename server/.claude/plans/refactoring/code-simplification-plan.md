# 코드 단순화 및 개선 계획

> **상태: 완료** (2026-01-23)

## 개요
Git에서 수정된 Group 및 Notification 관련 코드를 분석하여 코드 품질 개선 기회를 식별했습니다.

---

## 분석 결과 요약

### 현재 코드의 강점
- Java Record 기반 DTO 설계 (불변성)
- 일관된 Swagger 문서화
- 적절한 검증 어노테이션 (@Valid, @NotBlank, @Size)
- 컨트롤러 계층 분리 양호

### 개선이 필요한 영역

| 문제 | 위치 | 심각도 | 설명 |
|------|------|--------|------|
| 메서드 증식 | NotificationFacadeService | 중간 | 4개의 유사한 메서드가 중복 로직 포함 |
| 대형 컨트롤러 | GroupMemberController | 낮음 | 260줄, 9개 작업 (복잡도 임계치) |
| 파라미터 확장 | NotificationFacadeService | 중간 | 메서드당 4-6개 파라미터 (유지보수 어려움) |

---

## 개선 계획

### 1. NotificationFacadeService 통합 (우선순위: 높음)

**현재 상태:**
```java
// 4개의 유사한 메서드
createNotificationAndSendSse(userId, targetId, type, targetType)
createNotificationAndSendSseAndSendToDeviceEndpoint(..., message)
createNotificationWithGroupIdAndSendSse(..., groupId)
createNotificationWithGroupIdAndSendSseAndSendPush(..., groupId, message)
```

**개선안:** Optional 파라미터를 활용한 2개 메서드로 통합
```java
// 기본 메서드 (groupId nullable)
createNotificationAndSendSse(userId, targetId, type, targetType, Long groupId)

// Push 포함 메서드 (groupId nullable)
createNotificationAndSendSseAndPush(userId, targetId, type, targetType, Long groupId, String message)
```

**수정 파일:**
- `server/src/main/java/moment/notification/service/facade/NotificationFacadeService.java`
- 해당 서비스를 호출하는 모든 클라이언트 코드

---

### 2. GroupMemberController 분리 (우선순위: 중간)

**현재 상태:**
- 260줄, 9개 엔드포인트
- 멤버 관리 + 승인 워크플로우 + 소유권 이전이 혼재

**개선안:** 승인 워크플로우 분리
```
GroupMemberController (멤버 관리)
├── GET /members - 멤버 목록
├── GET /pending - 대기 목록
├── PUT /profile - 프로필 수정
└── DELETE /leave - 그룹 탈퇴

GroupMemberApprovalController (승인 워크플로우) [신규]
├── POST /approve - 가입 승인
├── POST /reject - 가입 거절
├── DELETE /kick - 멤버 강퇴
└── POST /transfer-ownership - 소유권 이전
```

**수정 파일:**
- `server/src/main/java/moment/group/presentation/GroupMemberController.java`
- `server/src/main/java/moment/group/presentation/GroupMemberApprovalController.java` (신규)

---

## 실행 순서

1. **Phase 1: NotificationFacadeService 리팩토링**
   - 4개 메서드를 2개로 통합
   - 호출부 수정
   - 테스트 실행으로 검증

2. **Phase 2: GroupMemberController 분리**
   - 승인 관련 엔드포인트를 GroupMemberApprovalController로 분리
   - API 경로 유지 (하위 호환성)
   - 테스트 실행으로 검증

---

## 검증 방법

```bash
# 테스트 실행
cd server && ./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests "NotificationFacadeServiceTest"
./gradlew test --tests "GroupMemberControllerTest"
```

---

## 수정 대상 파일 목록

### 필수 수정
- `server/src/main/java/moment/notification/service/facade/NotificationFacadeService.java`

### 선택적 수정
- `server/src/main/java/moment/group/presentation/GroupMemberController.java`
- `server/src/main/java/moment/group/presentation/GroupMemberApprovalController.java` (신규 생성)

### 영향받는 파일 (호출부)
- NotificationFacadeService를 호출하는 이벤트 핸들러 및 서비스
