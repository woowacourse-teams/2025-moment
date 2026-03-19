package moment.group.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MemberRoleTest {

    @Test
    void MemberRole_OWNER_MEMBER_존재() {
        assertThat(MemberRole.values()).containsExactly(MemberRole.OWNER, MemberRole.MEMBER);
    }
}
