package moment.auth.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import moment.config.TestTags;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag(TestTags.UNIT)
public class EmailVerificationTest {

    @Test
    void 쿨타임_내에_재요청시_isCoolTime이_true를_반환한다() {
        // given
        long coolDownSeconds = 60;
        EmailVerification verification = new EmailVerification("test-value", LocalDateTime.now(), 300);

        // when & then
        assertTrue(verification.isCoolTime(coolDownSeconds));
    }

    @Test
    void 쿨타임이_지난_후_재요청시_isCoolTime이_false를_반환한다() throws InterruptedException {
        // given
        long coolDownSeconds = 1;
        EmailVerification verification = new EmailVerification("test-value", LocalDateTime.now(), 300);

        Thread.sleep(1000);

        // when & then
        assertFalse(verification.isCoolTime(coolDownSeconds));
    }

    @Test
    void 제공된_코드와_내부_value가_같으면_hasSameCode가_true를_반환한다() {
        // given
        String code = "123456";
        EmailVerification verification = new EmailVerification(code, LocalDateTime.now(), 300);

        // when & then
        assertTrue(verification.hasSameCode(code));
    }

    @Test
    void 제공된_코드와_내부_value가_다르면_hasSameCode가_false를_반환한다() {
        // given
        EmailVerification verification = new EmailVerification("123456", LocalDateTime.now(), 300);

        // when & then
        assertFalse(verification.hasSameCode("654321"));
    }

    @Test
    void 만료_시간이_지나면_isExpired가_true를_반환한다() throws InterruptedException {
        // given
        EmailVerification verification = new EmailVerification("test-value", LocalDateTime.now(), 1);

        Thread.sleep(1000);

        // when & then
        assertTrue(verification.isExpired());
    }

    @Test
    void 만료_시간이_지나지_않으면_isExpired가_false를_반환한다() {
        // given
        EmailVerification verification = new EmailVerification("test-value", LocalDateTime.now(), 300);

        // when & then
        assertFalse(verification.isExpired());
    }
}
