package moment.notification.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import moment.config.TestTags;
import moment.notification.domain.SourceData;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag(TestTags.UNIT)
@DisplayNameGeneration(ReplaceUnderscores.class)
class SourceDataConverterTest {

    private final SourceDataConverter converter = new SourceDataConverter();

    @Test
    void SourceData를_JSON_문자열로_변환한다() {
        SourceData sourceData = SourceData.of(Map.of("momentId", 42));

        String json = converter.convertToDatabaseColumn(sourceData);

        assertThat(json).contains("\"momentId\"");
        assertThat(json).contains("42");
    }

    @Test
    void JSON_문자열을_SourceData로_변환한다() {
        String json = "{\"momentId\":42}";

        SourceData sourceData = converter.convertToEntityAttribute(json);

        assertThat(sourceData.getLong("momentId")).isEqualTo(42L);
    }

    @Test
    void null_SourceData를_변환하면_null을_반환한다() {
        String result = converter.convertToDatabaseColumn(null);

        assertThat(result).isNull();
    }

    @Test
    void null_JSON을_변환하면_empty_SourceData를_반환한다() {
        SourceData result = converter.convertToEntityAttribute(null);

        assertThat(result.data()).isEmpty();
    }

    @Test
    void 빈_문자열_JSON을_변환하면_empty_SourceData를_반환한다() {
        SourceData result = converter.convertToEntityAttribute("");

        assertThat(result.data()).isEmpty();
    }

    @Test
    void 복합_키_JSON을_SourceData로_변환한다() {
        String json = "{\"momentId\":42,\"groupId\":3}";

        SourceData sourceData = converter.convertToEntityAttribute(json);

        assertThat(sourceData.getLong("momentId")).isEqualTo(42L);
        assertThat(sourceData.getLong("groupId")).isEqualTo(3L);
    }
}
