package moment.admin.global.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(ReplaceUnderscores.class)
class UserAgentParserTest {

    @Test
    void Chrome_브라우저를_정상적으로_파싱한다() {
        // given
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

        // when
        UserAgentParser.ParsedUserAgent result = UserAgentParser.parse(userAgent);

        // then
        assertThat(result.browser()).isEqualTo("Chrome");
        assertThat(result.os()).isEqualTo("Windows");
        assertThat(result.deviceType()).isEqualTo("Desktop");
    }

    @Test
    void Firefox_브라우저를_정상적으로_파싱한다() {
        // given
        String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:121.0) Gecko/20100101 Firefox/121.0";

        // when
        UserAgentParser.ParsedUserAgent result = UserAgentParser.parse(userAgent);

        // then
        assertThat(result.browser()).isEqualTo("Firefox");
        assertThat(result.os()).isEqualTo("Mac OS");
        assertThat(result.deviceType()).isEqualTo("Desktop");
    }

    @Test
    void Safari_브라우저를_정상적으로_파싱한다() {
        // given
        String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2 Safari/605.1.15";

        // when
        UserAgentParser.ParsedUserAgent result = UserAgentParser.parse(userAgent);

        // then
        assertThat(result.browser()).isEqualTo("Safari");
        assertThat(result.os()).isEqualTo("Mac OS");
        assertThat(result.deviceType()).isEqualTo("Desktop");
    }

    @Test
    void Edge_브라우저를_정상적으로_파싱한다() {
        // given
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0";

        // when
        UserAgentParser.ParsedUserAgent result = UserAgentParser.parse(userAgent);

        // then
        assertThat(result.browser()).isEqualTo("Edge");
        assertThat(result.os()).isEqualTo("Windows");
        assertThat(result.deviceType()).isEqualTo("Desktop");
    }

    @Test
    void iPhone_모바일_기기를_정상적으로_파싱한다() {
        // given
        String userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2 Mobile/15E148 Safari/604.1";

        // when
        UserAgentParser.ParsedUserAgent result = UserAgentParser.parse(userAgent);

        // then
        assertThat(result.browser()).isEqualTo("Safari");
        assertThat(result.os()).isEqualTo("iOS");
        assertThat(result.deviceType()).isEqualTo("Mobile");
    }

    @Test
    void Android_모바일_기기를_정상적으로_파싱한다() {
        // given
        String userAgent = "Mozilla/5.0 (Linux; Android 14; SM-S911B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36";

        // when
        UserAgentParser.ParsedUserAgent result = UserAgentParser.parse(userAgent);

        // then
        assertThat(result.browser()).isEqualTo("Chrome");
        assertThat(result.os()).isEqualTo("Android");
        assertThat(result.deviceType()).isEqualTo("Mobile");
    }

    @Test
    void iPad_태블릿_기기를_정상적으로_파싱한다() {
        // given
        String userAgent = "Mozilla/5.0 (iPad; CPU OS 17_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2 Mobile/15E148 Safari/604.1";

        // when
        UserAgentParser.ParsedUserAgent result = UserAgentParser.parse(userAgent);

        // then
        assertThat(result.browser()).isEqualTo("Safari");
        assertThat(result.os()).isEqualTo("iOS");
        assertThat(result.deviceType()).isEqualTo("Tablet");
    }

    @Test
    void Linux_운영체제를_정상적으로_파싱한다() {
        // given
        String userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

        // when
        UserAgentParser.ParsedUserAgent result = UserAgentParser.parse(userAgent);

        // then
        assertThat(result.browser()).isEqualTo("Chrome");
        assertThat(result.os()).isEqualTo("Linux");
        assertThat(result.deviceType()).isEqualTo("Desktop");
    }

    @Test
    void 빈_UserAgent_문자열을_처리한다() {
        // given
        String userAgent = "";

        // when
        UserAgentParser.ParsedUserAgent result = UserAgentParser.parse(userAgent);

        // then
        assertThat(result.browser()).isEqualTo("Unknown");
        assertThat(result.os()).isEqualTo("Unknown");
        assertThat(result.deviceType()).isEqualTo("Unknown");
    }

    @Test
    void null_UserAgent를_처리한다() {
        // when
        UserAgentParser.ParsedUserAgent result = UserAgentParser.parse(null);

        // then
        assertThat(result.browser()).isEqualTo("Unknown");
        assertThat(result.os()).isEqualTo("Unknown");
        assertThat(result.deviceType()).isEqualTo("Unknown");
    }

    @Test
    void 알_수_없는_UserAgent를_처리한다() {
        // given
        String userAgent = "SomeCustomBot/1.0";

        // when
        UserAgentParser.ParsedUserAgent result = UserAgentParser.parse(userAgent);

        // then
        assertThat(result.browser()).isEqualTo("Unknown");
        assertThat(result.os()).isEqualTo("Unknown");
        assertThat(result.deviceType()).isEqualTo("Unknown");
    }
}
