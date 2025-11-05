package moment.report.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import moment.config.TestTags;
import moment.fixture.UserFixture;
import moment.global.domain.TargetType;
import moment.report.domain.Report;
import moment.report.domain.ReportReason;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@Tag(TestTags.INTEGRATION)
@ActiveProfiles("test")
@DataJpaTest
class ReportRepositoryTest {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = userRepository.save(UserFixture.createUser());
    }

    @Test
    @DisplayName("유저 ID와 대상 타입으로 신고된 대상의 ID 목록을 조회한다")
    void findAllTargetIdByUserIdAndTargetType() {
        // given
        Report report1 = reportRepository.save(
                new Report(user, TargetType.MOMENT, 1L, ReportReason.SPAM_OR_ADVERTISEMENT));
        Report report2 = reportRepository.save(
                new Report(user, TargetType.MOMENT, 2L, ReportReason.SPAM_OR_ADVERTISEMENT));

        reportRepository.save(new Report(user, TargetType.COMMENT, 3L, ReportReason.SPAM_OR_ADVERTISEMENT));

        User otherUser = userRepository.save(UserFixture.createUser());
        reportRepository.save(new Report(otherUser, TargetType.MOMENT, 4L, ReportReason.SPAM_OR_ADVERTISEMENT));

        // when
        List<Long> targetIds = reportRepository.findAllTargetIdByUserIdAndTargetType(user.getId(), TargetType.MOMENT);

        // then
        assertThat(targetIds).hasSize(2).containsExactlyInAnyOrder(report1.getTargetId(), report2.getTargetId());
    }
}
