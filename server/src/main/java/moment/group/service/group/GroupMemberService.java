package moment.group.service.group;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.group.domain.Group;
import moment.group.domain.GroupMember;
import moment.group.domain.MemberStatus;
import moment.group.infrastructure.GroupMemberRepository;
import moment.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupMemberService {

    private final GroupMemberRepository memberRepository;

    @Transactional
    public GroupMember createOwner(Group group, User user, String nickname) {
        validateNicknameNotUsed(group.getId(), nickname);
        GroupMember owner = GroupMember.createOwner(group, user, nickname);
        return memberRepository.save(owner);
    }

    @Transactional
    public GroupMember joinOrRestore(Group group, User user, String nickname) {
        validateNicknameNotUsed(group.getId(), nickname);

        Optional<GroupMember> existing = memberRepository
            .findByGroupIdAndUserIdIncludeDeleted(group.getId(), user.getId());

        if (existing.isPresent()) {
            GroupMember member = existing.get();
            if (!member.isDeleted()) {
                throw new MomentException(ErrorCode.ALREADY_GROUP_MEMBER);
            }
            member.restore(nickname);
            return member;
        }

        GroupMember member = GroupMember.createPendingMember(group, user, nickname);
        return memberRepository.save(member);
    }

    @Transactional
    public void approve(Long memberId) {
        GroupMember member = getById(memberId);
        if (!member.isPending()) {
            throw new MomentException(ErrorCode.MEMBER_NOT_PENDING);
        }
        member.approve();
    }

    @Transactional
    public void reject(Long memberId) {
        GroupMember member = getById(memberId);
        if (!member.isPending()) {
            throw new MomentException(ErrorCode.MEMBER_NOT_PENDING);
        }
        memberRepository.delete(member);
    }

    @Transactional
    public void kick(Long memberId) {
        GroupMember member = getById(memberId);
        if (member.isOwner()) {
            throw new MomentException(ErrorCode.CANNOT_KICK_OWNER);
        }
        member.kick();
        memberRepository.delete(member);
    }

    @Transactional
    public void leave(Long groupId, Long userId) {
        GroupMember member = getByGroupAndUser(groupId, userId);
        if (member.isOwner()) {
            throw new MomentException(ErrorCode.OWNER_CANNOT_LEAVE);
        }
        memberRepository.delete(member);
    }

    @Transactional
    public void transferOwnership(Long groupId, Long currentOwnerId, Long newOwnerMemberId) {
        GroupMember currentOwner = getByGroupAndUser(groupId, currentOwnerId);
        GroupMember newOwner = getById(newOwnerMemberId);

        if (!currentOwner.isOwner()) {
            throw new MomentException(ErrorCode.NOT_GROUP_OWNER);
        }
        if (!newOwner.isApproved()) {
            throw new MomentException(ErrorCode.MEMBER_NOT_APPROVED);
        }

        currentOwner.demoteToMember();
        newOwner.transferOwnership();
    }

    @Transactional
    public void updateNickname(Long memberId, String nickname) {
        GroupMember member = getById(memberId);
        validateNicknameNotUsed(member.getGroup().getId(), nickname);
        member.updateNickname(nickname);
    }

    public GroupMember getById(Long id) {
        return memberRepository.findById(id)
            .orElseThrow(() -> new MomentException(ErrorCode.MEMBER_NOT_FOUND));
    }

    public GroupMember getByGroupAndUser(Long groupId, Long userId) {
        return memberRepository.findByGroupIdAndUserId(groupId, userId)
            .orElseThrow(() -> new MomentException(ErrorCode.NOT_GROUP_MEMBER));
    }

    public List<GroupMember> getApprovedMembers(Long groupId) {
        return memberRepository.findByGroupIdAndStatus(groupId, MemberStatus.APPROVED);
    }

    public List<GroupMember> getPendingMembers(Long groupId) {
        return memberRepository.findByGroupIdAndStatus(groupId, MemberStatus.PENDING);
    }

    public List<GroupMember> getMyGroups(Long userId) {
        return memberRepository.findApprovedMembershipsByUserId(userId);
    }

    public long countApprovedMembers(Long groupId) {
        return memberRepository.countByGroupIdAndStatus(groupId, MemberStatus.APPROVED);
    }

    private void validateNicknameNotUsed(Long groupId, String nickname) {
        if (memberRepository.existsByGroupIdAndNicknameAndDeletedAtIsNull(groupId, nickname)) {
            throw new MomentException(ErrorCode.NICKNAME_ALREADY_USED);
        }
    }
}
