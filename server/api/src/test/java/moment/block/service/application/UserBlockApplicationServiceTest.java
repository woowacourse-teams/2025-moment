package moment.block.service.application;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.util.List;
import moment.block.domain.UserBlock;
import moment.block.dto.response.UserBlockListResponse;
import moment.block.dto.response.UserBlockResponse;
import moment.block.infrastructure.UserBlockRepository;
import moment.config.TestTags;
import moment.fixture.UserFixture;
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
class UserBlockApplicationServiceTest {

    @Autowired
    private UserBlockApplicationService userBlockApplicationService;

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
    void 사용자를_차단하고_응답을_반환한다() {
        // when
        UserBlockResponse result = userBlockApplicationService.blockUser(blocker.getId(), blockedUser.getId());

        // then
        assertThat(result.blockedUserId()).isEqualTo(blockedUser.getId());
    }

    @Test
    void 차단을_해제한다() {
        // given
        userBlockRepository.save(new UserBlock(blocker, blockedUser));
        entityManager.flush();
        entityManager.clear();

        // when
        userBlockApplicationService.unblockUser(blocker.getId(), blockedUser.getId());
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(userBlockRepository.findByBlockerAndBlockedUser(blocker, blockedUser)).isEmpty();
    }

    @Test
    void 차단된_사용자_ID_목록을_반환한다() {
        // given
        User anotherUser = userRepository.save(UserFixture.createUser());
        userBlockRepository.save(new UserBlock(blocker, blockedUser));
        userBlockRepository.save(new UserBlock(anotherUser, blocker));

        // when
        List<Long> result = userBlockApplicationService.getBlockedUserIds(blocker.getId());

        // then
        assertThat(result).containsExactlyInAnyOrder(blockedUser.getId(), anotherUser.getId());
    }

    @Test
    void 차단_목록을_반환한다() {
        // given
        userBlockRepository.save(new UserBlock(blocker, blockedUser));

        // when
        List<UserBlockListResponse> result = userBlockApplicationService.getBlockedUsers(blocker.getId());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).blockedUserId()).isEqualTo(blockedUser.getId());
    }
}
