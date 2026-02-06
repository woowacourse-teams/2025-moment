# Moment 알림 시스템 - 프론트엔드 연동 가이드

## 전체 아키텍처

알림은 **두 가지 채널**로 전달됩니다:

```
1. SSE (Server-Sent Events) → 앱이 포그라운드일 때 실시간 알림
2. Expo Push Notification   → 앱이 백그라운드/종료 상태일 때 푸시 알림
```

---

## 1. SSE (실시간 알림) 연동

### 1-1. SSE 구독 (연결)

```
GET /api/v2/notifications/subscribe
Content-Type: text/event-stream
Cookie: accessToken=<JWT 토큰>
```

- 연결 성공 시 서버가 즉시 `connect` 이벤트를 보냄
- **연결 유지 시간**: 10분 (600,000ms). 이후 자동 만료되므로 재연결 필요
- **하트비트**: 서버가 25초마다 `heartbeat` 이벤트를 전송하여 연결 유지
- **다중 세션 지원**: 같은 유저가 여러 기기/탭에서 동시 구독 가능 (모든 세션에 알림 전달)

### 1-2. SSE 이벤트 종류

| 이벤트 name | 설명 | 수신 시점 |
|---|---|---|
| `connect` | 연결 성공 확인 | 구독 직후 |
| `heartbeat` | 연결 유지용 | 25초마다 |
| `notification` | **실제 알림 데이터** | 알림 발생 시 |

### 1-3. `notification` 이벤트 데이터 구조 (JSON)

```json
{
  "notificationId": 42,
  "notificationType": "NEW_COMMENT_ON_MOMENT",
  "targetType": "MOMENT",
  "targetId": 15,
  "groupId": null,
  "message": "내 모멘트에 새로운 코멘트가 달렸습니다.",
  "isRead": false,
  "link": "/moments/15"
}
```

### 1-4. 프론트엔드 SSE 구현 예시

```typescript
// EventSource 연결
const eventSource = new EventSource(
  `${BASE_URL}/api/v2/notifications/subscribe`,
  { withCredentials: true } // 쿠키 전송을 위해 필수
);

// 연결 성공
eventSource.addEventListener('connect', (event) => {
  console.log('SSE 연결 성공:', event.data);
});

// 알림 수신
eventSource.addEventListener('notification', (event) => {
  const notification = JSON.parse(event.data);
  // notification.notificationType 에 따라 UI 처리
  // notification.link 를 이용해 딥링크 네비게이션
});

// 하트비트 (무시해도 됨, 연결 유지 용도)
eventSource.addEventListener('heartbeat', () => {
  // no-op
});

// 에러/재연결
eventSource.onerror = (error) => {
  // 자동 재연결 로직 구현
  // EventSource는 기본적으로 자동 재연결하지만,
  // 10분 타임아웃 후에는 새로 연결해야 함
};
```

> **주의**: Expo/React Native에서 EventSource를 사용하려면 `react-native-sse` 같은 폴리필이 필요할 수 있습니다.

---

## 2. Expo Push Notification 연동

### 2-1. 디바이스 토큰 등록

앱 시작 시 (또는 로그인 후) Expo Push Token을 서버에 등록합니다.

```
POST /api/v2/push-notifications
Cookie: accessToken=<JWT 토큰>
Content-Type: application/json

{
  "deviceEndpoint": "ExponentPushToken[xxxxxxxxxxxxxxxxxxxxxx]"
}
```

- `Notifications.getExpoPushTokenAsync()`로 토큰 획득 후 서버에 전송
- 동일 유저 + 동일 토큰 중복 등록 시 서버에서 무시 (멱등성 보장)

### 2-2. 디바이스 토큰 삭제

로그아웃 시 호출하여 푸시 알림 수신을 중단합니다.

```
DELETE /api/v2/push-notifications
Cookie: accessToken=<JWT 토큰>
Content-Type: application/json

{
  "deviceEndpoint": "ExponentPushToken[xxxxxxxxxxxxxxxxxxxxxx]"
}
```

### 2-3. 푸시 메시지 형식

서버에서 Expo Push API로 전송하는 메시지:

| 알림 종류 | title | body |
|---|---|---|
| 코멘트 | `Moment` | `당신의 모멘트에 누군가 코멘트를 달았어요:)` |
| 그룹 가입 신청 | `Moment` | `누군가 그룹 가입을 신청했어요` |
| 그룹 가입 승인 | `Moment` | `그룹 가입이 승인되었어요` |
| 그룹 강퇴 | `Moment` | `그룹에서 강퇴되었어요` |
| 모멘트 좋아요 | `Moment` | `누군가 당신의 모멘트를 좋아해요` |
| 코멘트 좋아요 | `Moment` | `누군가 당신의 코멘트를 좋아해요` |

---

## 3. 알림 목록 조회 API

### 3-1. 알림 목록 가져오기

```
GET /api/v2/notifications?read=false
Cookie: accessToken=<JWT 토큰>
```

- `read=false`: 읽지 않은 알림만 (기본값)
- `read=true`: 읽은 알림만

**응답**:
```json
{
  "status": 200,
  "data": [
    {
      "id": 42,
      "notificationType": "NEW_COMMENT_ON_MOMENT",
      "targetType": "MOMENT",
      "targetId": 15,
      "groupId": null,
      "message": "내 모멘트에 새로운 코멘트가 달렸습니다.",
      "isRead": false
    }
  ]
}
```

### 3-2. 단건 읽음 처리

```
PATCH /api/v2/notifications/{id}/read
```

### 3-3. 다건 읽음 처리

```
PATCH /api/v2/notifications/read-all
Content-Type: application/json

{
  "notificationIds": [1, 2, 3]
}
```

---

## 4. 알림 타입 전체 정리

| `notificationType` | `targetType` | `targetId` 의미 | `groupId` | `link` 패턴 | 설명 |
|---|---|---|---|---|---|
| `NEW_COMMENT_ON_MOMENT` | `MOMENT` | 모멘트 ID | `null` 또는 그룹 ID | `/moments/{targetId}` | 내 모멘트에 코멘트 |
| `GROUP_JOIN_REQUEST` | `GROUP` | 그룹 ID | 그룹 ID | `/groups/{groupId}` | 그룹 가입 신청 (방장에게) |
| `GROUP_JOIN_APPROVED` | `GROUP` | 그룹 ID | 그룹 ID | `/groups/{groupId}` | 그룹 가입 승인됨 |
| `GROUP_KICKED` | `GROUP` | 그룹 ID | 그룹 ID | `/groups/{groupId}` | 그룹에서 강퇴됨 |
| `MOMENT_LIKED` | `MOMENT` | 모멘트 ID | `null` | `/moments/{targetId}` | 모멘트에 좋아요 |
| `COMMENT_LIKED` | `COMMENT` | 코멘트 ID | `null` | `/comments/{targetId}` | 코멘트에 좋아요 |

### `targetType` enum 값

| 값 | 의미 |
|---|---|
| `MOMENT` | 모멘트 |
| `COMMENT` | 코멘트 |
| `GROUP` | 그룹 |
| `GROUP_MEMBER` | 그룹 멤버 |

---

## 5. Self-notification 방지

**본인이 수행한 액션에 대해서는 알림이 발생하지 않습니다.**

- 내 모멘트에 내가 코멘트 → 알림 없음
- 내 모멘트에 내가 좋아요 → 알림 없음
- 내 코멘트에 내가 좋아요 → 알림 없음

이는 서버에서 이벤트 발행 시점에 필터링됩니다.

---

## 6. 딥링크 (`link`) 처리

SSE 알림의 `link` 필드를 활용하여 알림 탭 시 해당 화면으로 네비게이션합니다.

| `link` 패턴 | 이동 화면 |
|---|---|
| `/moments/{id}` | 모멘트 상세 |
| `/comments/{id}` | 코멘트가 포함된 화면 |
| `/groups/{id}` | 그룹 상세 |

> `link`는 SSE 응답(`NotificationSseResponse`)에만 포함됩니다. 알림 목록 조회 API(`NotificationResponse`)에는 `link`가 없으므로, 프론트에서 `targetType` + `targetId` + `groupId` 조합으로 직접 네비게이션 경로를 구성해야 합니다.

---

## 7. 권장 구현 흐름

```
[앱 시작 / 로그인]
  ├─ Expo Push Token 획득 → POST /api/v2/push-notifications (토큰 등록)
  ├─ SSE 구독 → GET /api/v2/notifications/subscribe
  └─ 미읽은 알림 로드 → GET /api/v2/notifications?read=false

[포그라운드 상태]
  └─ SSE 'notification' 이벤트 수신 → 인앱 알림 UI 표시

[백그라운드/종료 상태]
  └─ Expo Push 수신 → 시스템 알림 표시 → 탭 시 앱 진입 + 딥링크

[알림 탭]
  ├─ PATCH /api/v2/notifications/{id}/read (읽음 처리)
  └─ link 또는 targetType+targetId로 화면 이동

[알림 목록 화면]
  ├─ GET /api/v2/notifications?read=false (미읽은)
  ├─ GET /api/v2/notifications?read=true  (읽은)
  └─ PATCH /api/v2/notifications/read-all (일괄 읽음)

[로그아웃]
  ├─ SSE 연결 종료 (eventSource.close())
  └─ DELETE /api/v2/push-notifications (토큰 삭제)
```

---

## 8. 주의사항

1. **SSE 재연결**: SSE 연결은 10분 후 만료됩니다. `onerror` 또는 `onclose` 시 자동 재연결 로직을 반드시 구현하세요.
2. **SSE 응답 헤더**: 서버가 `X-Accel-Buffering: no`를 설정하므로 Nginx 프록시 환경에서도 버퍼링 없이 즉시 전달됩니다.
3. **인증**: 모든 API는 `accessToken` 쿠키 기반 JWT 인증이 필요합니다.
4. **Expo Push Token**: 앱 시작마다 토큰을 등록해도 서버에서 중복 체크하므로 안전합니다.
5. **알림 목록 API에는 `link`가 없음**: SSE 실시간 알림에만 `link`가 포함됩니다. 목록에서는 `targetType`/`targetId`/`groupId`로 직접 경로를 구성하세요.