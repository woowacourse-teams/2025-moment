package moment.notification.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import moment.config.TestTags;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag(TestTags.UNIT)
@DisplayNameGeneration(ReplaceUnderscores.class)
class SourceDataTest {

    @Test
    void of로_SourceData를_생성한다() {
        Map<String, Object> data = Map.of("momentId", 42L);

        SourceData sourceData = SourceData.of(data);

        assertThat(sourceData.data()).isEqualTo(data);
    }

    @Test
    void empty로_빈_SourceData를_생성한다() {
        SourceData sourceData = SourceData.empty();

        assertThat(sourceData.data()).isEmpty();
    }

    @Test
    void get으로_값을_조회한다() {
        SourceData sourceData = SourceData.of(Map.of("momentId", 42L));

        assertThat(sourceData.get("momentId")).isEqualTo(42L);
    }

    @Test
    void getLong으로_Long_타입_값을_조회한다() {
        SourceData sourceData = SourceData.of(Map.of("momentId", 42L));

        assertThat(sourceData.getLong("momentId")).isEqualTo(42L);
    }

    @Test
    void getLong으로_Integer_타입_값을_Long으로_변환한다() {
        SourceData sourceData = SourceData.of(Map.of("momentId", 42));

        assertThat(sourceData.getLong("momentId")).isEqualTo(42L);
    }

    @Test
    void getLong으로_존재하지_않는_키를_조회하면_null을_반환한다() {
        SourceData sourceData = SourceData.of(Map.of("momentId", 42L));

        assertThat(sourceData.getLong("nonExistent")).isNull();
    }

    @Test
    void getLong으로_String_타입_숫자를_Long으로_변환한다() {
        SourceData sourceData = SourceData.of(Map.of("momentId", "42"));

        assertThat(sourceData.getLong("momentId")).isEqualTo(42L);
    }
}
