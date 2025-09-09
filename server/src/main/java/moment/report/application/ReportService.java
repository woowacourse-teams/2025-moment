package moment.report.application;

import lombok.RequiredArgsConstructor;
import moment.global.domain.TargetType;
import moment.moment.domain.Moment;
import moment.moment.dto.request.MomentReportCreateRequest;
import moment.report.domain.Report;
import moment.report.domain.ReportReason;
import moment.report.infrastructure.ReportRepository;
import moment.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;

    public Report createReport(TargetType targetType, User reporter, Moment moment, MomentReportCreateRequest request) {

        Report report = new Report(
                reporter,
                targetType,
                moment.getId(),
                ReportReason.valueOf(request.reason()
                )
        );
        return reportRepository.save(report);
    }
}
