package moment.moment.service;

import lombok.RequiredArgsConstructor;
import moment.global.domain.TargetType;
import moment.moment.domain.Moment;
import moment.moment.dto.request.MomentReportCreateRequest;
import moment.moment.dto.response.MomentReportCreateResponse;
import moment.moment.infrastructure.MomentRepository;
import moment.report.application.ReportService;
import moment.report.domain.Report;
import moment.user.application.UserQueryService;
import moment.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MomentService {

    private static final int MOMENT_DELETE_THRESHOLD = 3;

    private final MomentRepository momentRepository;
    private final MomentTagService momentTagService;
    private final UserQueryService userQueryService;
    private final MomentImageService momentImageService;
    private final ReportService reportService;
    private final MomentQueryService momentQueryService;

    @Transactional
    public MomentReportCreateResponse reportMoment(Long momentId, Long reporterId, MomentReportCreateRequest request) {
        User user = userQueryService.getUserById(reporterId);
        Moment moment = momentQueryService.getMomentWithMomenterById(momentId);

        Report report = reportService.createReport(TargetType.MOMENT, user, moment.getId(), request.reason());

        long reportCount = reportService.countReportsByTarget(TargetType.MOMENT, moment.getId());

        if (reportCount >= MOMENT_DELETE_THRESHOLD) {
            momentImageService.deleteByMoment(moment);
            momentTagService.deleteByMoment(moment);
            momentRepository.delete(moment);
        }

        return MomentReportCreateResponse.from(report);
    }
}
