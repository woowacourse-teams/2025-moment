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
class DeepLinkGeneratorTest {

    @Test
    void 개인_모멘트_댓글_알림의_딥링크를_생성한다() {
        SourceData sourceData = SourceData.of(Map.of("momentId", 42L));

        String link = DeepLinkGenerator.generate(
            NotificationType.NEW_COMMENT_ON_MOMENT, sourceData);

        assertThat(link).isEqualTo("/moments/42");
    }

    @Test
    void 그룹_모멘트_댓글_알림의_딥링크를_생성한다() {
        SourceData sourceData = SourceData.of(Map.of("momentId", 42L, "groupId", 3L));

        String link = DeepLinkGenerator.generate(
            NotificationType.NEW_COMMENT_ON_MOMENT, sourceData);

        assertThat(link).isEqualTo("/groups/3/moments/42");
    }

    @Test
    void 그룹_가입_신청_알림의_딥링크를_생성한다() {
        SourceData sourceData = SourceData.of(Map.of("groupId", 3L));

        String link = DeepLinkGenerator.generate(
            NotificationType.GROUP_JOIN_REQUEST, sourceData);

        assertThat(link).isEqualTo("/groups/3");
    }

    @Test
    void 그룹_가입_승인_알림의_딥링크를_생성한다() {
        SourceData sourceData = SourceData.of(Map.of("groupId", 3L));

        String link = DeepLinkGenerator.generate(
            NotificationType.GROUP_JOIN_APPROVED, sourceData);

        assertThat(link).isEqualTo("/groups/3");
    }

    @Test
    void 그룹_강퇴_알림의_딥링크는_null이다() {
        SourceData sourceData = SourceData.of(Map.of("groupId", 3L));

        String link = DeepLinkGenerator.generate(
            NotificationType.GROUP_KICKED, sourceData);

        assertThat(link).isNull();
    }

    @Test
    void 모멘트_좋아요_알림의_딥링크를_생성한다() {
        SourceData sourceData = SourceData.of(Map.of("momentId", 42L));

        String link = DeepLinkGenerator.generate(
            NotificationType.MOMENT_LIKED, sourceData);

        assertThat(link).isEqualTo("/moments/42");
    }

    @Test
    void 코멘트_좋아요_알림의_딥링크를_생성한다() {
        SourceData sourceData = SourceData.of(Map.of("commentId", 15L));

        String link = DeepLinkGenerator.generate(
            NotificationType.COMMENT_LIKED, sourceData);

        assertThat(link).isEqualTo("/comments/15");
    }
}
