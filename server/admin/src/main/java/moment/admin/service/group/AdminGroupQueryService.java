package moment.admin.service.group;

import lombok.RequiredArgsConstructor;
import moment.admin.domain.GroupStatusFilter;
import moment.admin.dto.response.AdminGroupDetailResponse;
import moment.admin.dto.response.AdminGroupListResponse;
import moment.admin.dto.response.AdminGroupMemberListResponse;
import moment.admin.dto.response.AdminGroupMemberResponse;
import moment.admin.dto.response.AdminGroupOwnerInfo;
import moment.admin.dto.response.AdminGroupStatsResponse;
import moment.admin.dto.response.AdminGroupSummary;
import moment.admin.dto.response.AdminInviteLinkInfo;
import moment.admin.global.exception.AdminErrorCode;
import moment.admin.global.exception.AdminException;
import moment.group.domain.Group;
import moment.group.domain.GroupInviteLink;
import moment.group.domain.GroupMember;
import moment.group.domain.MemberStatus;
import moment.group.infrastructure.GroupInviteLinkRepository;
import moment.group.infrastructure.GroupMemberRepository;
import moment.group.infrastructure.GroupRepository;
import moment.moment.infrastructure.MomentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminGroupQueryService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final MomentRepository momentRepository;
    private final GroupInviteLinkRepository groupInviteLinkRepository;

    public AdminGroupStatsResponse getGroupStats() {
        long totalGroups = groupRepository.countAllIncludingDeleted();
        long activeGroups = groupRepository.countActiveGroups();
        long deletedGroups = groupRepository.countDeletedGroups();
        long totalMembers = groupMemberRepository.countTotalApprovedMembers();
        long totalMoments = momentRepository.count();
        long todayCreatedGroups = groupRepository.countTodayCreatedGroups();

        return new AdminGroupStatsResponse(
            totalGroups,
            activeGroups,
            deletedGroups,
            totalMembers,
            totalMoments,
            todayCreatedGroups
        );
    }

    public AdminGroupListResponse getGroupList(int page, int size, String keyword, GroupStatusFilter status) {
        // Native query에 이미 ORDER BY created_at DESC가 포함되어 있으므로 Sort 제외
        Pageable pageable = PageRequest.of(page, size);

        Page<Group> groupPage = findGroupsByFilter(keyword, status, pageable);

        Page<AdminGroupSummary> summaryPage = groupPage.map(group -> {
            GroupMember owner = groupMemberRepository.findOwnerByGroupId(group.getId()).orElse(null);
            int memberCount = (int) groupMemberRepository.countByGroupIdAndStatus(group.getId(), MemberStatus.APPROVED);
            int momentCount = 0; // TODO: 실제 모멘트 수 조회 구현

            return new AdminGroupSummary(
                group.getId(),
                group.getName(),
                group.getDescription(),
                memberCount,
                momentCount,
                owner != null ? AdminGroupOwnerInfo.from(owner) : null,
                group.getCreatedAt(),
                group.getDeletedAt(),
                group.getDeletedAt() != null
            );
        });

        return AdminGroupListResponse.from(summaryPage);
    }

    private Page<Group> findGroupsByFilter(String keyword, GroupStatusFilter status, Pageable pageable) {
        boolean hasKeyword = keyword != null && !keyword.isBlank();

        if (hasKeyword) {
            return switch (status) {
                case ACTIVE -> groupRepository.findByNameContainingAndActive(keyword, pageable);
                case DELETED -> groupRepository.findByNameContainingAndDeleted(keyword, pageable);
                case ALL -> groupRepository.findByNameContainingIncludingDeleted(keyword, pageable);
            };
        } else {
            return switch (status) {
                case ACTIVE -> groupRepository.findActiveGroups(pageable);
                case DELETED -> groupRepository.findDeletedGroups(pageable);
                case ALL -> groupRepository.findAllIncludingDeleted(pageable);
            };
        }
    }

    public AdminGroupDetailResponse getGroupDetail(Long groupId) {
        Group group = groupRepository.findByIdIncludingDeleted(groupId)
            .orElseThrow(() -> new AdminException(AdminErrorCode.GROUP_NOT_FOUND));

        GroupMember owner = groupMemberRepository.findOwnerByGroupId(groupId).orElse(null);
        int memberCount = (int) groupMemberRepository.countByGroupIdAndStatus(groupId, MemberStatus.APPROVED);
        int pendingMemberCount = (int) groupMemberRepository.countByGroupIdAndStatus(groupId, MemberStatus.PENDING);
        int momentCount = 0; // TODO: 그룹별 모멘트 수 조회 구현
        int commentCount = 0; // TODO: 그룹별 댓글 수 조회 구현

        GroupInviteLink inviteLink = groupInviteLinkRepository.findFirstByGroupIdOrderByCreatedAtDesc(groupId)
            .orElse(null);

        return new AdminGroupDetailResponse(
            group.getId(),
            group.getName(),
            group.getDescription(),
            memberCount,
            pendingMemberCount,
            momentCount,
            commentCount,
            owner != null ? AdminGroupOwnerInfo.from(owner) : null,
            AdminInviteLinkInfo.from(inviteLink),
            group.getCreatedAt(),
            group.getDeletedAt(),
            group.getDeletedAt() != null
        );
    }

    public AdminGroupMemberListResponse getApprovedMembers(Long groupId, int page, int size) {
        validateGroupExists(groupId);

        Pageable pageable = PageRequest.of(page, size);
        Page<GroupMember> memberPage = groupMemberRepository.findApprovedMembersByGroupId(groupId, pageable);
        Page<AdminGroupMemberResponse> responsePage = memberPage.map(AdminGroupMemberResponse::from);

        return AdminGroupMemberListResponse.from(responsePage);
    }

    public AdminGroupMemberListResponse getPendingMembers(Long groupId, int page, int size) {
        validateGroupExists(groupId);

        Pageable pageable = PageRequest.of(page, size);
        Page<GroupMember> memberPage = groupMemberRepository.findPendingMembersByGroupId(groupId, pageable);
        Page<AdminGroupMemberResponse> responsePage = memberPage.map(AdminGroupMemberResponse::from);

        return AdminGroupMemberListResponse.from(responsePage);
    }

    private void validateGroupExists(Long groupId) {
        groupRepository.findByIdIncludingDeleted(groupId)
            .orElseThrow(() -> new AdminException(AdminErrorCode.GROUP_NOT_FOUND));
    }
}
