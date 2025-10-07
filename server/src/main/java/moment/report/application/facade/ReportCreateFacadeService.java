package moment.report.application.facade;

import lombok.RequiredArgsConstructor;
import moment.comment.service.application.CommentApplicationService;
import moment.global.domain.TargetType;
import moment.moment.service.application.MomentApplicationService;
import moment.report.application.application.ReportApplicationService;
import moment.report.dto.ReportCreateRequest;
import moment.report.dto.ReportCreateResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportCreateFacadeService {

    private final ReportApplicationService reportApplicationService;
    private final MomentApplicationService momentApplicationService;
    private final CommentApplicationService commentApplicationService;

    public ReportCreateResponse createReport(
            Long contentId,
            Long userId,
            ReportCreateRequest request,
            TargetType targetType
    ) {
        Long reportId = reportApplicationService.createReport(contentId, userId, request, targetType);
        Long reportCount = reportApplicationService.countReport(targetType, contentId);
        
        if (targetType == TargetType.MOMENT) {
            momentApplicationService.deleteByReport(contentId, reportCount);
        }
        if (targetType == TargetType.COMMENT) {
            commentApplicationService.deleteByReport(contentId, reportCount);
        }
        return new ReportCreateResponse(reportId);
    }
}
