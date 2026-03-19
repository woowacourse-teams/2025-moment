package moment.admin.service.group;

import java.util.List;
import lombok.RequiredArgsConstructor;
import moment.admin.dto.request.AdminGroupUpdateRequest;
import moment.admin.dto.response.AdminGroupInviteLinkResponse;
import moment.admin.global.exception.AdminErrorCode;
import moment.admin.global.exception.AdminException;
import moment.comment.infrastructure.CommentRepository;
import moment.group.domain.Group;
import moment.group.domain.GroupInviteLink;
import moment.group.infrastructure.GroupInviteLinkRepository;
import moment.group.infrastructure.GroupMemberRepository;
import moment.group.infrastructure.GroupRepository;
import moment.moment.infrastructure.MomentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminGroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final MomentRepository momentRepository;
    private final CommentRepository commentRepository;
    private final GroupInviteLinkRepository groupInviteLinkRepository;

    @Value("${app.base-url:https://moment.com}")
    private String baseUrl;

    @Transactional
    public void updateGroup(Long groupId, AdminGroupUpdateRequest request) {
        Group group = groupRepository.findByIdIncludingDeleted(groupId)
            .orElseThrow(() -> new AdminException(AdminErrorCode.GROUP_NOT_FOUND));

        if (group.getDeletedAt() != null) {
            throw new AdminException(AdminErrorCode.GROUP_ALREADY_DELETED);
        }

        group.updateInfo(request.name(), request.description());
    }

    @Transactional
    public void deleteGroup(Long groupId) {
        Group group = groupRepository.findByIdIncludingDeleted(groupId)
            .orElseThrow(() -> new AdminException(AdminErrorCode.GROUP_NOT_FOUND));

        if (group.getDeletedAt() != null) {
            throw new AdminException(AdminErrorCode.GROUP_ALREADY_DELETED);
        }

        // 1. 코멘트 전체 soft delete (모멘트 ID 기반)
        List<Long> momentIds = momentRepository.findAllIdsByGroupId(groupId);
        if (!momentIds.isEmpty()) {
            commentRepository.softDeleteByMomentIds(momentIds);
        }

        // 2. 모멘트 전체 soft delete
        momentRepository.softDeleteByGroupId(groupId);

        // 3. 멤버 전체 soft delete
        groupMemberRepository.softDeleteByGroupId(groupId);

        // 4. 그룹 soft delete
        groupRepository.delete(group);
    }

    @Transactional
    public void restoreGroup(Long groupId) {
        Group group = groupRepository.findByIdIncludingDeleted(groupId)
            .orElseThrow(() -> new AdminException(AdminErrorCode.GROUP_NOT_FOUND));

        if (group.getDeletedAt() == null) {
            throw new AdminException(AdminErrorCode.GROUP_NOT_DELETED);
        }

        // 1. 그룹 복원
        group.restore();

        // 2. 멤버 전체 복원
        groupMemberRepository.restoreByGroupId(groupId);

        // 3. 모멘트 전체 복원
        momentRepository.restoreByGroupId(groupId);

        // 4. 코멘트 전체 복원 (모멘트 ID 기반)
        List<Long> momentIds = momentRepository.findAllIdsByGroupId(groupId);
        if (!momentIds.isEmpty()) {
            commentRepository.restoreByMomentIds(momentIds);
        }
    }

    public AdminGroupInviteLinkResponse getInviteLink(Long groupId) {
        groupRepository.findByIdIncludingDeleted(groupId)
            .orElseThrow(() -> new AdminException(AdminErrorCode.GROUP_NOT_FOUND));

        GroupInviteLink inviteLink = groupInviteLinkRepository
            .findFirstByGroupIdOrderByCreatedAtDesc(groupId)
            .orElse(null);

        if (inviteLink == null) {
            return null;
        }

        return AdminGroupInviteLinkResponse.from(inviteLink, baseUrl);
    }
}
