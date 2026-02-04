# Phase 6: SSE 다중 세션 지원

---

## Step 6.1: Emitters 다중 연결 지원

### 현재 코드 (Emitters.java)
```java
private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
```
-> 사용자당 1개 연결만 지원. 새 연결이 기존 연결을 덮어씀.

### 변경 후
```java
private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

public SseEmitter add(Long userId, SseEmitter emitter) {
    emitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

    emitter.onError(e -> removeEmitter(userId, emitter));
    emitter.onTimeout(() -> removeEmitter(userId, emitter));
    emitter.onCompletion(() -> removeEmitter(userId, emitter));

    return emitter;
}

private void removeEmitter(Long userId, SseEmitter emitter) {
    List<SseEmitter> userEmitters = emitters.get(userId);
    if (userEmitters != null) {
        userEmitters.remove(emitter);
        if (userEmitters.isEmpty()) {
            emitters.remove(userId);
        }
    }
}

public void sendToClient(Long userId, String eventName, Object data) {
    List<SseEmitter> userEmitters = emitters.get(userId);
    if (userEmitters != null) {
        List<SseEmitter> deadEmitters = new ArrayList<>();
        for (SseEmitter emitter : userEmitters) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (IOException e) {
                log.error("Failed to send SSE to user {}", userId, e);
                deadEmitters.add(emitter);
            }
        }
        userEmitters.removeAll(deadEmitters);
        if (userEmitters.isEmpty()) emitters.remove(userId);
    }
}

public void sendHeartbeat() {
    emitters.forEach((userId, userEmitters) -> {
        List<SseEmitter> deadEmitters = new ArrayList<>();
        for (SseEmitter emitter : userEmitters) {
            try {
                emitter.send(SseEmitter.event().name("heartbeat")
                    .comment("keeping connection alive").data("heartbeat"));
            } catch (IOException e) {
                log.info("User {} connection lost.", userId);
                deadEmitters.add(emitter);
            }
        }
        userEmitters.removeAll(deadEmitters);
        if (userEmitters.isEmpty()) emitters.remove(userId);
    });
}
```

### TDD 테스트 목록 -- `EmittersTest.java` (기존 3개 수정 + 신규 추가)
```
1. 사용자에게_SseEmitter를_추가한다
2. 같은_사용자에게_여러_SseEmitter를_추가할_수_있다
3. 특정_emitter_에러_시_해당_emitter만_제거한다
4. 사용자의_모든_emitter에_메시지를_전송한다
5. 전송_실패한_emitter는_자동_제거된다
6. 모든_사용자의_emitter에_하트비트를_전송한다
7. 마지막_emitter_제거_시_사용자_엔트리를_삭제한다
```

---

# 검증 계획

```bash
# Phase별 검증
./gradlew fastTest           # 각 Phase 완료 후
./gradlew e2eTest            # Phase 2 완료 후 (Push 엔드포인트 변경)

# 전체 검증
./gradlew build              # 최종 빌드 + 전체 테스트
```

### 수동 검증
- [ ] SSE 구독 후 알림 발생 -> 실시간 수신 확인
- [ ] 다중 탭 SSE 구독 -> 모든 탭에서 수신 확인
- [ ] Expo Push 토큰 등록 -> 푸시 발송 -> 디바이스 수신 확인
- [ ] Expo API 일시 장애 -> SEND_FAILED -> 스케줄러 재시도 -> 최종 발송 확인
- [ ] maxRetries 초과 -> DEAD 전환 -> 7일 후 정리 확인
- [ ] 유효하지 않은 토큰 -> 영수증 DeviceNotRegistered -> 토큰 자동 삭제 확인
- [ ] self-notification 방지 (자기 글에 자기가 댓글) 확인