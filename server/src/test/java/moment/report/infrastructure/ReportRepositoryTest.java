package moment.report.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import moment.global.domain.TargetType;
import moment.moment.domain.Moment;
import moment.moment.domain.WriteType;
import moment.moment.infrastructure.MomentRepository;
import moment.report.domain.Report;
import moment.report.domain.ReportReason;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
class ReportRepositoryTest {

    @Autowired
    ReportRepository reportRepository;

    @Autowired
    MomentRepository momentRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    void 모멘트의_신고_갯수를_센다() {
        // given
        TargetType targetType = TargetType.MOMENT;
        User user = new User("eee@gmail.com", "1234!", "아마", ProviderType.EMAIL);
        User saveMomenter = userRepository.save(user);

        Moment moment = new Moment("내용", saveMomenter, WriteType.BASIC);
        Moment savedMoment = momentRepository.save(moment);

        User reporter1 = new User("ddd@gmail.com", "1234!", "드라고", ProviderType.EMAIL);
        User reporter2 = new User("hhh@gmail.com", "1234!", "히포", ProviderType.EMAIL);
        User savedReporter1 = userRepository.save(reporter1);
        User savedReporter2 = userRepository.save(reporter2);

        Report report1 = new Report(savedReporter1, targetType, savedMoment.getId(), ReportReason.ABUSE_OR_HARASSMENT);
        Report report2 = new Report(savedReporter2, targetType, savedMoment.getId(), ReportReason.ABUSE_OR_HARASSMENT);

        reportRepository.save(report1);
        reportRepository.save(report2);

        // when
        long result = reportRepository.countByTargetTypeAndTargetId(targetType, savedMoment.getId());

        // then
        assertThat(result).isEqualTo(2L);
    }
}