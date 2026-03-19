package moment.storage.service.storage;

import static org.assertj.core.api.Assertions.assertThat;

import moment.config.TestTags;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@Tag(TestTags.UNIT)
@ActiveProfiles("test")
@DisplayNameGeneration(ReplaceUnderscores.class)
class PhotoUrlResolverTest {

    private PhotoUrlResolver photoUrlResolver;

    @BeforeEach
    void setUp() {
        photoUrlResolver = new PhotoUrlResolver("/original/", "/resized/");
    }

    @Test
    void 원본_이미지_URL을_성공적으로_변환한다() {
        // given
        String originalUrl = "https://example.com/images/original/my-photo.jpg";

        // when
        String resolvedUrl = photoUrlResolver.resolve(originalUrl);

        // then
        assertThat(resolvedUrl).isEqualTo("https://example.com/images/resized/my-photo.jpg");
    }

    @Test
    void URL에_원본_경로_세그먼트가_없으면_경로를_변경하지_않는다() {
        // given
        String originalUrl = "https://example.com/images/other/my-photo.jpg";

        // when
        String resolvedUrl = photoUrlResolver.resolve(originalUrl);

        // then
        // Only extension should be removed
        assertThat(resolvedUrl).isEqualTo("https://example.com/images/other/my-photo.jpg");
    }

    @Test
    @DisplayName("URL에 확장자가 없으면 확장자를 제거하지 않는다")
    void URL에_확장자가_없으면_확장자를_제거하지_않는다() {
        // given
        String originalUrl = "https://example.com/images/original/my-photo";

        // when
        String resolvedUrl = photoUrlResolver.resolve(originalUrl);

        // then
        // Only path should be changed
        assertThat(resolvedUrl).isEqualTo("https://example.com/images/resized/my-photo");
    }

    @Test
    void null_URL이_주어지면_null을_반환한다() {
        // given
        String originalUrl = null;

        // when
        String resolvedUrl = photoUrlResolver.resolve(originalUrl);

        // then
        assertThat(resolvedUrl).isNull();
    }

    @Test
    void 빈_URL_문자열이_주어지면_빈_문자열을_반환한다() {
        // given
        String originalUrl = "";

        // when
        String resolvedUrl = photoUrlResolver.resolve(originalUrl);

        // then
        assertThat(resolvedUrl).isEmpty();
    }
}
