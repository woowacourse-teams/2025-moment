# Phase 1: 도메인 기반 클래스 신규 생성

> 의존성: 없음 (독립 실행 가능)
> 예상 파일: 3개 신규 + 3개 테스트

## 목표

notification 도메인에 필요한 새로운 값 객체와 유틸리티 클래스를 생성한다.
기존 코드를 수정하지 않으며, 신규 클래스만 추가한다.

---

## Task 1.1: SourceData 값 객체 생성

### 파일
- 신규: `src/main/java/moment/notification/domain/SourceData.java`
- 테스트: `src/test/java/moment/notification/domain/SourceDataTest.java`

### 구현 내용

```java
package moment.notification.domain;

import java.util.Map;

public record SourceData(Map<String, Object> data) {

    public static SourceData of(Map<String, Object> data) {
        return new SourceData(data);
    }

    public static SourceData empty() {
        return new SourceData(Map.of());
    }

    public Object get(String key) {
        return data.get(key);
    }

    public Long getLong(String key) {
        Object value = data.get(key);
        if (value == null) return null;
        if (value instanceof Long l) return l;
        if (value instanceof Number n) return n.longValue();
        return Long.valueOf(value.toString());
    }
}
```

### TDD 테스트 목록

```java
@Tag(TestTags.UNIT)
@DisplayNameGeneration(ReplaceUnderscores.class)
class SourceDataTest {

    @Test
    void of로_SourceData를_생성한다()

    @Test
    void empty로_빈_SourceData를_생성한다()

    @Test
    void get으로_값을_조회한다()

    @Test
    void getLong으로_Long_타입_값을_조회한다()

    @Test
    void getLong으로_Integer_타입_값을_Long으로_변환한다()
    // Jackson 역직렬화 시 Integer로 들어오는 케이스

    @Test
    void getLong으로_존재하지_않는_키를_조회하면_null을_반환한다()

    @Test
    void getLong으로_String_타입_숫자를_Long으로_변환한다()
}
```

---

## Task 1.2: SourceDataConverter JPA 변환기 생성

### 파일
- 신규: `src/main/java/moment/notification/infrastructure/SourceDataConverter.java`
- 테스트: `src/test/java/moment/notification/infrastructure/SourceDataConverterTest.java`

### 구현 내용

```java
package moment.notification.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Map;
import moment.notification.domain.SourceData;

@Converter
public class SourceDataConverter implements AttributeConverter<SourceData, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(SourceData sourceData) {
        if (sourceData == null || sourceData.data() == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(sourceData.data());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("source_data JSON 변환 실패", e);
        }
    }

    @Override
    public SourceData convertToEntityAttribute(String json) {
        if (json == null || json.isBlank()) {
            return SourceData.empty();
        }
        try {
            Map<String, Object> data = objectMapper.readValue(json,
                    new TypeReference<Map<String, Object>>() {});
            return SourceData.of(data);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("source_data JSON 파싱 실패", e);
        }
    }
}
```

### TDD 테스트 목록

```java
@Tag(TestTags.UNIT)
@DisplayNameGeneration(ReplaceUnderscores.class)
class SourceDataConverterTest {

    @Test
    void SourceData를_JSON_문자열로_변환한다()
    // {"momentId": 42} 형태 확인

    @Test
    void JSON_문자열을_SourceData로_변환한다()

    @Test
    void null_SourceData를_변환하면_null을_반환한다()

    @Test
    void null_JSON을_변환하면_empty_SourceData를_반환한다()

    @Test
    void 빈_문자열_JSON을_변환하면_empty_SourceData를_반환한다()

    @Test
    void 복합_키_JSON을_SourceData로_변환한다()
    // {"momentId": 42, "groupId": 3}
}
```

---

## Task 1.3: DeepLinkGenerator 딥링크 생성 클래스

### 파일
- 신규: `src/main/java/moment/notification/domain/DeepLinkGenerator.java`
- 테스트: `src/test/java/moment/notification/domain/DeepLinkGeneratorTest.java`

### 구현 내용

```java
package moment.notification.domain;

public class DeepLinkGenerator {

    public static String generate(NotificationType notificationType, SourceData sourceData) {
        return switch (notificationType) {
            case NEW_COMMENT_ON_MOMENT -> {
                Long groupId = sourceData.getLong("groupId");
                Long momentId = sourceData.getLong("momentId");
                yield (groupId != null)
                    ? "/groups/" + groupId + "/moments/" + momentId
                    : "/moments/" + momentId;
            }
            case GROUP_JOIN_REQUEST, GROUP_JOIN_APPROVED ->
                "/groups/" + sourceData.getLong("groupId");
            case GROUP_KICKED -> null;
            case MOMENT_LIKED ->
                "/moments/" + sourceData.getLong("momentId");
            case COMMENT_LIKED ->
                "/comments/" + sourceData.getLong("commentId");
        };
    }
}
```

### TDD 테스트 목록

```java
@Tag(TestTags.UNIT)
@DisplayNameGeneration(ReplaceUnderscores.class)
class DeepLinkGeneratorTest {

    @Test
    void 개인_모멘트_댓글_알림의_딥링크를_생성한다()
    // NEW_COMMENT_ON_MOMENT + {momentId: 42} → "/moments/42"

    @Test
    void 그룹_모멘트_댓글_알림의_딥링크를_생성한다()
    // NEW_COMMENT_ON_MOMENT + {momentId: 42, groupId: 3} → "/groups/3/moments/42"

    @Test
    void 그룹_가입_신청_알림의_딥링크를_생성한다()
    // GROUP_JOIN_REQUEST + {groupId: 3} → "/groups/3"

    @Test
    void 그룹_가입_승인_알림의_딥링크를_생성한다()
    // GROUP_JOIN_APPROVED + {groupId: 3} → "/groups/3"

    @Test
    void 그룹_강퇴_알림의_딥링크는_null이다()
    // GROUP_KICKED + {groupId: 3} → null

    @Test
    void 모멘트_좋아요_알림의_딥링크를_생성한다()
    // MOMENT_LIKED + {momentId: 42} → "/moments/42"

    @Test
    void 코멘트_좋아요_알림의_딥링크를_생성한다()
    // COMMENT_LIKED + {commentId: 15} → "/comments/15"
}
```

---

## 완료 조건

- [ ] `SourceData`, `SourceDataConverter`, `DeepLinkGenerator` 생성
- [ ] 모든 단위 테스트 통과
- [ ] 기존 코드 수정 없음 (신규 파일만 추가)
- [ ] `./gradlew fastTest` 전체 통과
