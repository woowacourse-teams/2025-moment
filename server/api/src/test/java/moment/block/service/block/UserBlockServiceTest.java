package moment.block.service.block;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import java.util.List;
import moment.block.domain.UserBlock;
import moment.block.infrastructure.UserBlockRepository;
import moment.config.TestTags;
import moment.fixture.UserFixture;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@Tag(TestTags.INTEGRATION)
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayNameGeneration(ReplaceUnderscores.class)
class UserBlockServiceTest {

    @Autowired
    private UserBlockService userBlockService;

    @Autowired
    private UserBlockRepository userBlockRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private User blocker;
    private User blockedUser;

    @BeforeEach
    void setUp() {
        blocker = userRepository.save(UserFixture.createUser());
        blockedUser = userRepository.save(UserFixture.createUser());
    }

    @Test
    void 사용자를_차단한다() {
        // when
        UserBlock result = userBlockService.block(blocker, blockedUser);

        // then
        assertThat(result.getBlocker().getId()).isEqualTo(blocker.getId());
        assertThat(result.getBlockedUser().getId()).isEqualTo(blockedUser.getId());
    }

    @Test
    void 자기_자신을_차단하면_예외가_발생한다() {
        assertThatThrownBy(() -> userBlockService.block(blocker, blocker))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BLOCK_SELF);
    }

    @Test
    void 이미_차단된_사용자를_다시_차단하면_예외가_발생한다() {
        // given
        userBlockService.block(blocker, blockedUser);

        // when & then
        assertThatThrownBy(() -> userBlockService.block(blocker, blockedUser))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BLOCK_ALREADY_EXISTS);
    }

    @Test
    void soft_delete된_차단을_재차단하면_restore한다() {
        // given
        userBlockService.block(blocker, blockedUser);
        userBlockService.unblock(blocker, blockedUser);
        entityManager.flush();
        entityManager.clear();

        // when
        UserBlock result = userBlockService.block(blocker, blockedUser);

        // then
        assertThat(result.isDeleted()).isFalse();
        assertThat(result.getBlocker().getId()).isEqualTo(blocker.getId());
        assertThat(result.getBlockedUser().getId()).isEqualTo(blockedUser.getId());
    }

    @Test
    void 차단을_해제한다() {
        // given
        userBlockService.block(blocker, blockedUser);

        // when
        userBlockService.unblock(blocker, blockedUser);
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(userBlockRepository.findByBlockerAndBlockedUser(blocker, blockedUser)).isEmpty();
    }

    @Test
    void 존재하지_않는_차단을_해제하면_예외가_발생한다() {
        assertThatThrownBy(() -> userBlockService.unblock(blocker, blockedUser))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BLOCK_NOT_FOUND);
    }

    @Test
    void 양방향_차단된_사용자_ID_목록을_반환한다() {
        // given
        User anotherUser = userRepository.save(UserFixture.createUser());
        userBlockService.block(blocker, blockedUser);
        userBlockService.block(anotherUser, blocker);

        // when
        List<Long> result = userBlockService.getBlockedUserIds(blocker.getId());

        // then
        assertThat(result).containsExactlyInAnyOrder(blockedUser.getId(), anotherUser.getId());
    }

    @Test
    void 차단_목록이_비어있으면_빈_리스트를_반환한다() {
        // when
        List<Long> result = userBlockService.getBlockedUserIds(blocker.getId());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 양방향_차단_여부를_확인한다_차단된_경우() {
        // given
        userBlockService.block(blocker, blockedUser);

        // when & then
        assertThat(userBlockService.isBlocked(blocker.getId(), blockedUser.getId())).isTrue();
    }

    @Test
    void 양방향_차단_여부를_확인한다_차단되지_않은_경우() {
        assertThat(userBlockService.isBlocked(blocker.getId(), blockedUser.getId())).isFalse();
    }

    @Test
    void 내가_차단한_사용자_목록을_반환한다() {
        // given
        User anotherUser = userRepository.save(UserFixture.createUser());
        userBlockService.block(blocker, blockedUser);
        userBlockService.block(blocker, anotherUser);

        // when
        List<UserBlock> result = userBlockService.getBlockedUsers(blocker);

        // then
        assertThat(result).hasSize(2);
    }
}
