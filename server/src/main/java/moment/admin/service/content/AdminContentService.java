package moment.admin.service.content;

import java.util.List;
import lombok.RequiredArgsConstructor;
import moment.admin.domain.AdminGroupLog;
import moment.admin.domain.AdminGroupLogType;
import moment.admin.dto.response.AdminCommentListResponse;
import moment.admin.dto.response.AdminCommentResponse;
import moment.admin.dto.response.AdminMomentListResponse;
import moment.admin.dto.response.AdminMomentResponse;
import moment.admin.infrastructure.AdminGroupLogRepository;
import moment.comment.domain.Comment;
import moment.comment.infrastructure.CommentRepository;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.group.domain.Group;
import moment.group.infrastructure.GroupRepository;
import moment.moment.domain.Moment;
import moment.moment.infrastructure.MomentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminContentService {

    private final GroupRepository groupRepository;
    private final MomentRepository momentRepository;
    private final CommentRepository commentRepository;
    private final AdminGroupLogRepository adminGroupLogRepository;

    public AdminMomentListResponse getMoments(Long groupId, int page, int size) {
        Group group = findGroupOrThrow(groupId);

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Moment> momentPage = momentRepository.findByGroupId(groupId, pageable);

        List<AdminMomentResponse> content = momentPage.getContent().stream()
            .map(moment -> {
                int commentCount = (int) commentRepository.countByMomentId(moment.getId());
                return AdminMomentResponse.from(moment, commentCount);
            })
            .toList();

        return new AdminMomentListResponse(
            content,
            momentPage.getNumber(),
            momentPage.getSize(),
            momentPage.getTotalElements(),
            momentPage.getTotalPages()
        );
    }

    @Transactional
    public void deleteMoment(Long groupId, Long momentId, Long adminId, String adminEmail) {
        Group group = findGroupOrThrow(groupId);
        Moment moment = findMomentOrThrow(groupId, momentId);

        if (moment.getDeletedAt() != null) {
            throw new MomentException(ErrorCode.ADMIN_MOMENT_ALREADY_DELETED);
        }

        // 해당 모멘트의 코멘트 전체 soft delete
        commentRepository.softDeleteByMomentId(momentId);

        // 모멘트 soft delete
        momentRepository.delete(moment);

        // 로그 기록
        adminGroupLogRepository.save(AdminGroupLog.builder()
            .adminId(adminId)
            .adminEmail(adminEmail)
            .type(AdminGroupLogType.MOMENT_DELETE)
            .groupId(groupId)
            .targetId(momentId)
            .description("모멘트 삭제")
            .build());
    }

    public AdminCommentListResponse getComments(Long groupId, Long momentId, int page, int size) {
        Group group = findGroupOrThrow(groupId);
        Moment moment = findMomentOrThrow(groupId, momentId);

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<Comment> commentPage = commentRepository.findByMomentId(momentId, pageable);

        List<AdminCommentResponse> content = commentPage.getContent().stream()
            .map(AdminCommentResponse::from)
            .toList();

        return new AdminCommentListResponse(
            content,
            commentPage.getNumber(),
            commentPage.getSize(),
            commentPage.getTotalElements(),
            commentPage.getTotalPages()
        );
    }

    @Transactional
    public void deleteComment(Long groupId, Long commentId, Long adminId, String adminEmail) {
        Group group = findGroupOrThrow(groupId);
        Comment comment = findCommentOrThrow(groupId, commentId);

        if (comment.getDeletedAt() != null) {
            throw new MomentException(ErrorCode.ADMIN_COMMENT_ALREADY_DELETED);
        }

        // 코멘트 soft delete
        commentRepository.delete(comment);

        // 로그 기록
        adminGroupLogRepository.save(AdminGroupLog.builder()
            .adminId(adminId)
            .adminEmail(adminEmail)
            .type(AdminGroupLogType.COMMENT_DELETE)
            .groupId(groupId)
            .targetId(commentId)
            .description("코멘트 삭제")
            .build());
    }

    private Group findGroupOrThrow(Long groupId) {
        return groupRepository.findByIdIncludingDeleted(groupId)
            .orElseThrow(() -> new MomentException(ErrorCode.GROUP_NOT_FOUND));
    }

    private Moment findMomentOrThrow(Long groupId, Long momentId) {
        return momentRepository.findByIdAndGroupId(momentId, groupId)
            .orElseThrow(() -> new MomentException(ErrorCode.ADMIN_MOMENT_NOT_FOUND));
    }

    private Comment findCommentOrThrow(Long groupId, Long commentId) {
        return commentRepository.findByIdAndGroupId(commentId, groupId)
            .orElseThrow(() -> new MomentException(ErrorCode.ADMIN_COMMENT_NOT_FOUND));
    }
}
