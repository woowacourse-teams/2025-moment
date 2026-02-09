package moment.block.service.block;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import moment.block.domain.UserBlock;
import moment.block.infrastructure.UserBlockRepository;
import moment.fixture.UserBlockFixture;
import moment.fixture.UserFixture;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.user.domain.User;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class UserBlockServiceTest {

    @Mock
    private UserBlockRepository userBlockRepository;

    @InjectMocks
    private UserBlockService userBlockService;

    @Test
    void 사용자를_차단한다() {
        User blocker = UserFixture.createUserWithId(1L);
        User blockedUser = UserFixture.createUserWithId(2L);
        UserBlock expected = UserBlockFixture.createUserBlockWithId(1L, blocker, blockedUser);

        given(userBlockRepository.existsByBlockerAndBlockedUser(blocker, blockedUser)).willReturn(false);
        given(userBlockRepository.findByBlockerAndBlockedUserIncludeDeleted(1L, 2L)).willReturn(Optional.empty());
        given(userBlockRepository.save(any(UserBlock.class))).willReturn(expected);

        UserBlock result = userBlockService.block(blocker, blockedUser);

        assertThat(result.getBlocker()).isEqualTo(blocker);
        assertThat(result.getBlockedUser()).isEqualTo(blockedUser);
    }

    @Test
    void 자기_자신을_차단하면_예외가_발생한다() {
        User user = UserFixture.createUserWithId(1L);

        assertThatThrownBy(() -> userBlockService.block(user, user))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BLOCK_SELF);
    }

    @Test
    void 이미_차단된_사용자를_다시_차단하면_예외가_발생한다() {
        User blocker = UserFixture.createUserWithId(1L);
        User blockedUser = UserFixture.createUserWithId(2L);

        given(userBlockRepository.existsByBlockerAndBlockedUser(blocker, blockedUser)).willReturn(true);

        assertThatThrownBy(() -> userBlockService.block(blocker, blockedUser))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BLOCK_ALREADY_EXISTS);
    }

    @Test
    void soft_delete된_차단을_재차단하면_restore한다() {
        User blocker = UserFixture.createUserWithId(1L);
        User blockedUser = UserFixture.createUserWithId(2L);
        UserBlock deletedBlock = UserBlockFixture.createUserBlockWithId(1L, blocker, blockedUser);

        given(userBlockRepository.existsByBlockerAndBlockedUser(blocker, blockedUser)).willReturn(false);
        given(userBlockRepository.findByBlockerAndBlockedUserIncludeDeleted(1L, 2L)).willReturn(Optional.of(deletedBlock));

        UserBlock result = userBlockService.block(blocker, blockedUser);

        assertThat(result).isEqualTo(deletedBlock);
        assertThat(result.isDeleted()).isFalse();
    }

    @Test
    void 차단을_해제한다() {
        User blocker = UserFixture.createUserWithId(1L);
        User blockedUser = UserFixture.createUserWithId(2L);
        UserBlock userBlock = UserBlockFixture.createUserBlockWithId(1L, blocker, blockedUser);

        given(userBlockRepository.findByBlockerAndBlockedUser(blocker, blockedUser)).willReturn(Optional.of(userBlock));

        userBlockService.unblock(blocker, blockedUser);

        verify(userBlockRepository).delete(userBlock);
    }

    @Test
    void 존재하지_않는_차단을_해제하면_예외가_발생한다() {
        User blocker = UserFixture.createUserWithId(1L);
        User blockedUser = UserFixture.createUserWithId(2L);

        given(userBlockRepository.findByBlockerAndBlockedUser(blocker, blockedUser)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userBlockService.unblock(blocker, blockedUser))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BLOCK_NOT_FOUND);
    }

    @Test
    void 양방향_차단된_사용자_ID_목록을_반환한다() {
        given(userBlockRepository.findBlockedUserIds(1L)).willReturn(List.of(2L, 3L));

        List<Long> result = userBlockService.getBlockedUserIds(1L);

        assertThat(result).containsExactly(2L, 3L);
    }

    @Test
    void 차단_목록이_비어있으면_빈_리스트를_반환한다() {
        given(userBlockRepository.findBlockedUserIds(1L)).willReturn(Collections.emptyList());

        List<Long> result = userBlockService.getBlockedUserIds(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void 양방향_차단_여부를_확인한다_차단된_경우() {
        given(userBlockRepository.existsBidirectionalBlock(1L, 2L)).willReturn(true);

        assertThat(userBlockService.isBlocked(1L, 2L)).isTrue();
    }

    @Test
    void 양방향_차단_여부를_확인한다_차단되지_않은_경우() {
        given(userBlockRepository.existsBidirectionalBlock(1L, 2L)).willReturn(false);

        assertThat(userBlockService.isBlocked(1L, 2L)).isFalse();
    }

    @Test
    void 내가_차단한_사용자_목록을_반환한다() {
        User blocker = UserFixture.createUserWithId(1L);
        User blockedUser1 = UserFixture.createUserWithId(2L);
        User blockedUser2 = UserFixture.createUserWithId(3L);
        List<UserBlock> blocks = List.of(
                UserBlockFixture.createUserBlockWithId(1L, blocker, blockedUser1),
                UserBlockFixture.createUserBlockWithId(2L, blocker, blockedUser2)
        );

        given(userBlockRepository.findAllByBlockerWithBlockedUser(blocker)).willReturn(blocks);

        List<UserBlock> result = userBlockService.getBlockedUsers(blocker);

        assertThat(result).hasSize(2);
    }
}
