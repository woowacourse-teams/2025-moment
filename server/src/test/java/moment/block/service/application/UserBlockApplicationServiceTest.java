package moment.block.service.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import moment.block.domain.UserBlock;
import moment.block.dto.response.UserBlockListResponse;
import moment.block.dto.response.UserBlockResponse;
import moment.block.service.block.UserBlockService;
import moment.fixture.UserBlockFixture;
import moment.fixture.UserFixture;
import moment.user.domain.User;
import moment.user.service.user.UserService;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class UserBlockApplicationServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private UserBlockService userBlockService;

    @InjectMocks
    private UserBlockApplicationService userBlockApplicationService;

    @Test
    void 사용자를_차단하고_응답을_반환한다() {
        User blocker = UserFixture.createUserWithId(1L);
        User blockedUser = UserFixture.createUserWithId(2L);
        UserBlock userBlock = UserBlockFixture.createUserBlockWithId(1L, blocker, blockedUser);

        given(userService.getUserBy(1L)).willReturn(blocker);
        given(userService.getUserBy(2L)).willReturn(blockedUser);
        given(userBlockService.block(blocker, blockedUser)).willReturn(userBlock);

        UserBlockResponse result = userBlockApplicationService.blockUser(1L, 2L);

        assertThat(result.blockedUserId()).isEqualTo(2L);
    }

    @Test
    void 차단을_해제한다() {
        User blocker = UserFixture.createUserWithId(1L);
        User blockedUser = UserFixture.createUserWithId(2L);

        given(userService.getUserBy(1L)).willReturn(blocker);
        given(userService.getUserBy(2L)).willReturn(blockedUser);

        userBlockApplicationService.unblockUser(1L, 2L);

        verify(userBlockService).unblock(blocker, blockedUser);
    }

    @Test
    void 차단된_사용자_ID_목록을_반환한다() {
        given(userBlockService.getBlockedUserIds(1L)).willReturn(List.of(2L, 3L));

        List<Long> result = userBlockApplicationService.getBlockedUserIds(1L);

        assertThat(result).containsExactly(2L, 3L);
    }

    @Test
    void 차단_목록을_반환한다() {
        User blocker = UserFixture.createUserWithId(1L);
        User blockedUser = UserFixture.createUserWithId(2L);
        UserBlock userBlock = UserBlockFixture.createUserBlockWithId(1L, blocker, blockedUser);

        given(userService.getUserBy(1L)).willReturn(blocker);
        given(userBlockService.getBlockedUsers(blocker)).willReturn(List.of(userBlock));

        List<UserBlockListResponse> result = userBlockApplicationService.getBlockedUsers(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).blockedUserId()).isEqualTo(2L);
    }
}
