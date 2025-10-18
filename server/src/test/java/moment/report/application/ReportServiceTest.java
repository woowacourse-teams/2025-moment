package moment.report.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import moment.global.domain.TargetType;
import moment.moment.domain.Moment;
import moment.moment.domain.WriteType;
import moment.moment.dto.request.MomentReportCreateRequest;
import moment.report.application.report.ReportService;
import moment.report.domain.Report;
import moment.report.domain.ReportReason;
import moment.report.infrastructure.ReportRepository;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @InjectMocks
    ReportService reportService;

    @Mock
    ReportRepository reportRepository;

    @Test
    void 신고를_생성한다() {
        // given
        TargetType targetType = TargetType.MOMENT;
        User user = new User("drago@mail.com", "1234", "drago", ProviderType.EMAIL);
        User momenter = new User("ama@mail.com", "1234", "ama", ProviderType.EMAIL);
        Moment moment = new Moment("잘자요", momenter, WriteType.BASIC);
        ReflectionTestUtils.setField(moment, "id", 1L);

        MomentReportCreateRequest request = new MomentReportCreateRequest("SEXUAL_CONTENT");
        Report report = new Report(user, targetType, 1L, ReportReason.SEXUAL_CONTENT);
        given(reportRepository.save(any(Report.class))).willReturn(report);

        // when
        Report result = reportService.createReport(targetType, user, moment.getId(), request.reason());

        // then
        assertThat(result).isEqualTo(report);
        then(reportRepository).should(times(1)).save(any(Report.class));
    }
}
