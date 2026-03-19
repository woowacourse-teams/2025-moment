package moment.group.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MemberStatusTest {

    @Test
    void MemberStatus_PENDING_APPROVED_KICKED_존재() {
        assertThat(MemberStatus.values()).containsExactly(
            MemberStatus.PENDING, MemberStatus.APPROVED, MemberStatus.KICKED
        );
    }
}
