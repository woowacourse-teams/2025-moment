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
import org.springframework.data.domain.Sort;
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
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public AdminGroupDetailResponse getGroupDetail(Long groupId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public AdminGroupMemberListResponse getApprovedMembers(Long groupId, int page, int size) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public AdminGroupMemberListResponse getPendingMembers(Long groupId, int page, int size) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
