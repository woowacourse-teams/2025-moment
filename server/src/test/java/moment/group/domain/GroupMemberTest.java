package moment.group.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class GroupMemberTest {

    @Test
    void GroupMember_Owner_생성_성공() {
        // Given
        Group group = createGroup();
        User user = createUser(1L);

        // When
        GroupMember member = GroupMember.createOwner(group, user, "닉네임");

        // Then
        assertThat(member.getRole()).isEqualTo(MemberRole.OWNER);
        assertThat(member.getStatus()).isEqualTo(MemberStatus.APPROVED);
        assertThat(member.isOwner()).isTrue();
        assertThat(member.isApproved()).isTrue();
    }

    @Test
    void GroupMember_Pending_생성_성공() {
        // Given
        Group group = createGroup();
        User user = createUser(1L);

        // When
        GroupMember member = GroupMember.createPendingMember(group, user, "닉네임");

        // Then
        assertThat(member.getRole()).isEqualTo(MemberRole.MEMBER);
        assertThat(member.getStatus()).isEqualTo(MemberStatus.PENDING);
        assertThat(member.isPending()).isTrue();
    }

    @Test
    void GroupMember_승인_성공() {
        // Given
        GroupMember member = createPendingMember();

        // When
        member.approve();

        // Then
        assertThat(member.getStatus()).isEqualTo(MemberStatus.APPROVED);
        assertThat(member.isApproved()).isTrue();
    }

    @Test
    void GroupMember_강퇴_성공() {
        // Given
        GroupMember member = createApprovedMember();

        // When
        member.kick();

        // Then
        assertThat(member.getStatus()).isEqualTo(MemberStatus.KICKED);
    }

    @Test
    void GroupMember_복구_성공() {
        // Given
        GroupMember member = createDeletedMember();

        // When
        member.restore("새닉네임");

        // Then
        assertThat(member.getDeletedAt()).isNull();
        assertThat(member.getNickname()).isEqualTo("새닉네임");
        assertThat(member.getStatus()).isEqualTo(MemberStatus.PENDING);
    }

    @Test
    void GroupMember_소유권_이전_성공() {
        // Given
        GroupMember owner = createOwnerMember();
        GroupMember member = createApprovedMember();

        // When
        owner.demoteToMember();
        member.transferOwnership();

        // Then
        assertThat(owner.getRole()).isEqualTo(MemberRole.MEMBER);
        assertThat(member.getRole()).isEqualTo(MemberRole.OWNER);
    }

    @Test
    void GroupMember_닉네임_수정_성공() {
        // Given
        GroupMember member = createApprovedMember();

        // When
        member.updateNickname("변경된닉네임");

        // Then
        assertThat(member.getNickname()).isEqualTo("변경된닉네임");
    }

    private User createUser(Long id) {
        User user = new User("test" + id + "@example.com", "password", "닉네임" + id, ProviderType.EMAIL);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Group createGroup() {
        User owner = createUser(1L);
        Group group = new Group("테스트 그룹", "설명", owner);
        ReflectionTestUtils.setField(group, "id", 1L);
        return group;
    }

    private GroupMember createPendingMember() {
        return GroupMember.createPendingMember(createGroup(), createUser(2L), "펜딩멤버");
    }

    private GroupMember createApprovedMember() {
        GroupMember member = GroupMember.createPendingMember(createGroup(), createUser(2L), "승인멤버");
        member.approve();
        return member;
    }

    private GroupMember createOwnerMember() {
        return GroupMember.createOwner(createGroup(), createUser(1L), "오너");
    }

    private GroupMember createDeletedMember() {
        GroupMember member = createApprovedMember();
        ReflectionTestUtils.setField(member, "deletedAt", LocalDateTime.now());
        return member;
    }
}
