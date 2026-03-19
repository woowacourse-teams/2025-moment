package moment.admin.service.group;

import java.util.List;
import lombok.RequiredArgsConstructor;
import moment.admin.global.exception.AdminErrorCode;
import moment.admin.global.exception.AdminException;
import moment.comment.infrastructure.CommentRepository;
import moment.group.domain.Group;
import moment.group.domain.GroupMember;
import moment.group.infrastructure.GroupMemberRepository;
import moment.group.infrastructure.GroupRepository;
import moment.moment.infrastructure.MomentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminGroupMemberService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final MomentRepository momentRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public void approveMember(Long groupId, Long memberId) {
        Group group = findGroupOrThrow(groupId);
        GroupMember member = findMemberOrThrow(groupId, memberId);

        if (member.isApproved()) {
            throw new AdminException(AdminErrorCode.ALREADY_APPROVED);
        }
        if (!member.isPending()) {
            throw new AdminException(AdminErrorCode.MEMBER_NOT_PENDING);
        }

        member.approve();
    }

    @Transactional
    public void rejectMember(Long groupId, Long memberId) {
        Group group = findGroupOrThrow(groupId);
        GroupMember member = findMemberOrThrow(groupId, memberId);

        if (!member.isPending()) {
            throw new AdminException(AdminErrorCode.MEMBER_NOT_PENDING);
        }

        groupMemberRepository.delete(member);
    }

    @Transactional
    public void kickMember(Long groupId, Long memberId) {
        Group group = findGroupOrThrow(groupId);
        GroupMember member = findMemberOrThrow(groupId, memberId);

        if (member.isOwner()) {
            throw new AdminException(AdminErrorCode.CANNOT_KICK_OWNER);
        }
        if (!member.isApproved()) {
            throw new AdminException(AdminErrorCode.MEMBER_NOT_APPROVED);
        }

        member.kick();

        // 해당 멤버의 모멘트에 달린 코멘트 삭제
        List<Long> momentIds = momentRepository.findAllIdsByMemberId(memberId);
        if (!momentIds.isEmpty()) {
            commentRepository.softDeleteByMomentIds(momentIds);
        }

        // 해당 멤버의 모멘트 삭제
        momentRepository.softDeleteByMemberId(memberId);

        // 해당 멤버의 코멘트 삭제
        commentRepository.softDeleteByMemberId(memberId);

        // 멤버 Soft Delete
        groupMemberRepository.delete(member);
    }

    @Transactional
    public void transferOwnership(Long groupId, Long newOwnerMemberId) {
        Group group = findGroupOrThrow(groupId);
        GroupMember newOwner = findMemberOrThrow(groupId, newOwnerMemberId);

        if (!newOwner.isApproved()) {
            throw new AdminException(AdminErrorCode.MEMBER_NOT_APPROVED);
        }
        if (newOwner.isOwner()) {
            throw new AdminException(AdminErrorCode.ALREADY_OWNER);
        }

        GroupMember currentOwner = groupMemberRepository.findOwnerByGroupId(groupId)
            .orElseThrow(() -> new AdminException(AdminErrorCode.MEMBER_NOT_FOUND));

        currentOwner.demoteToMember();
        newOwner.transferOwnership();
    }

    private Group findGroupOrThrow(Long groupId) {
        return groupRepository.findByIdIncludingDeleted(groupId)
            .orElseThrow(() -> new AdminException(AdminErrorCode.GROUP_NOT_FOUND));
    }

    private GroupMember findMemberOrThrow(Long groupId, Long memberId) {
        return groupMemberRepository.findByGroupIdAndMemberId(groupId, memberId)
            .orElseThrow(() -> new AdminException(AdminErrorCode.MEMBER_NOT_FOUND));
    }
}
