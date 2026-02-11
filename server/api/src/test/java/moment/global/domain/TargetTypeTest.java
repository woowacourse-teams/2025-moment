package moment.global.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TargetType 테스트")
class TargetTypeTest {

    @Test
    @DisplayName("기존 타겟 타입이 존재한다")
    void TargetType_기존_타입_존재() {
        assertThat(TargetType.values()).contains(
            TargetType.COMMENT,
            TargetType.MOMENT
        );
    }

    @Test
    @DisplayName("그룹 관련 타겟 타입이 존재한다")
    void TargetType_그룹_관련_타입_존재() {
        assertThat(TargetType.values()).contains(
            TargetType.GROUP,
            TargetType.GROUP_MEMBER
        );
    }
}
