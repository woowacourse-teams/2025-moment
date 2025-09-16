package moment.report.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import moment.global.domain.TargetType;
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

    @Transactional
    public Report createReport(TargetType targetType,
                               User reporter,
                               Long targetId,
                               String reason) {

        Report report = new Report(
                reporter,
                targetType,
                targetId,
                ReportReason.valueOf(reason)
        );

        // 모멘트나 코멘트
        return reportRepository.save(report);
    }

    @Transactional(readOnly = true)
    public long countReportsByTarget(TargetType targetType, Long targetId) {
        return reportRepository.countByTargetTypeAndTargetId(targetType, targetId);
    }

    public List<Report> findMomentReportByUser(User user) {
        return reportRepository.findAllByUserAndTargetType(user, TargetType.MOMENT);
    }
}
