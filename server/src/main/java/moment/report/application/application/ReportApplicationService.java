package moment.report.application.application;

import lombok.RequiredArgsConstructor;
import moment.global.domain.TargetType;
import moment.report.application.report.ReportService;
import moment.report.domain.Report;
import moment.report.dto.ReportCreateRequest;
import moment.user.domain.User;
import moment.user.service.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportApplicationService {

    private final UserService userService;
    private final ReportService reportService;

    @Transactional
    public Long createReport(
            Long contentId,
            Long userId,
            ReportCreateRequest request,
            TargetType targetType
    ) {
        User user = userService.getUserBy(userId);

        Report report = reportService.createReport(TargetType.MOMENT, user, contentId, request.reason());

        return report.getId();
    }

    public Long countReport(TargetType targetType, Long contentId) {
        return reportService.countReportsBy(targetType, contentId);
    }
}
