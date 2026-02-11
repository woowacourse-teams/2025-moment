package moment.block.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import moment.block.domain.UserBlock;
import moment.config.TestTags;
import moment.fixture.UserFixture;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@Tag(TestTags.INTEGRATION)
@ActiveProfiles("test")
@DataJpaTest
@DisplayNameGeneration(ReplaceUnderscores.class)
class UserBlockRepositoryTest {

    @Autowired
    private UserBlockRepository userBlockRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private User userA;
    private User userB;
    private User userC;

    @BeforeEach
    void setUp() {
        userA = userRepository.save(UserFixture.createUser());
        userB = userRepository.save(UserFixture.createUser());
        userC = userRepository.save(UserFixture.createUser());
    }

    @Nested
    class existsBidirectionalBlock {

        @Test
        void 양방향_차단이_존재하면_true를_반환한다_정방향() {
            // given
            userBlockRepository.save(new UserBlock(userA, userB));

            // when
            boolean result = userBlockRepository.existsBidirectionalBlock(userA.getId(), userB.getId());

            // then
            assertThat(result).isTrue();
        }

        @Test
        void 양방향_차단이_존재하면_true를_반환한다_역방향() {
            // given
            userBlockRepository.save(new UserBlock(userA, userB));

            // when
            boolean result = userBlockRepository.existsBidirectionalBlock(userB.getId(), userA.getId());

            // then
            assertThat(result).isTrue();
        }

        @Test
        void 차단이_존재하지_않으면_false를_반환한다() {
            // when
            boolean result = userBlockRepository.existsBidirectionalBlock(userA.getId(), userB.getId());

            // then
            assertThat(result).isFalse();
        }

        @Test
        void soft_delete된_차단은_존재하지_않는_것으로_판단한다() {
            // given
            UserBlock block = userBlockRepository.save(new UserBlock(userA, userB));
            userBlockRepository.delete(block);
            entityManager.flush();
            entityManager.clear();

            // when
            boolean result = userBlockRepository.existsBidirectionalBlock(userA.getId(), userB.getId());

            // then
            assertThat(result).isFalse();
        }

        @Test
        void 양방향_모두_차단된_경우에도_true를_반환한다() {
            // given
            userBlockRepository.save(new UserBlock(userA, userB));
            userBlockRepository.save(new UserBlock(userB, userA));

            // when
            boolean result = userBlockRepository.existsBidirectionalBlock(userA.getId(), userB.getId());

            // then
            assertThat(result).isTrue();
        }
    }

    @Nested
    class findBlockedUserIds {

        @Test
        void 양방향_차단된_사용자_ID_목록을_반환한다() {
            // given
            userBlockRepository.save(new UserBlock(userA, userB));
            userBlockRepository.save(new UserBlock(userC, userA));

            // when
            List<Long> blockedUserIds = userBlockRepository.findBlockedUserIds(userA.getId());

            // then
            assertThat(blockedUserIds).containsExactlyInAnyOrder(userB.getId(), userC.getId());
        }

        @Test
        void 차단이_없으면_빈_목록을_반환한다() {
            // when
            List<Long> blockedUserIds = userBlockRepository.findBlockedUserIds(userA.getId());

            // then
            assertThat(blockedUserIds).isEmpty();
        }
    }

    @Nested
    class findByBlockerAndBlockedUserIncludeDeleted {

        @Test
        void 삭제된_차단을_포함하여_조회한다() {
            // given
            UserBlock block = userBlockRepository.save(new UserBlock(userA, userB));
            userBlockRepository.delete(block);
            entityManager.flush();
            entityManager.clear();

            // when
            Optional<UserBlock> result = userBlockRepository.findByBlockerAndBlockedUserIncludeDeleted(
                    userA.getId(), userB.getId());

            // then
            assertThat(result).isPresent();
        }
    }

    @Nested
    class findByBlockerAndBlockedUser {

        @Test
        void 삭제된_차단은_일반_조회에서_제외된다() {
            // given
            UserBlock block = userBlockRepository.save(new UserBlock(userA, userB));
            userBlockRepository.delete(block);
            entityManager.flush();
            entityManager.clear();

            // when
            Optional<UserBlock> result = userBlockRepository.findByBlockerAndBlockedUser(userA, userB);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class findAllByBlockerWithBlockedUser {

        @Test
        void 차단_목록을_차단된_사용자와_함께_조회한다() {
            // given
            userBlockRepository.save(new UserBlock(userA, userB));
            userBlockRepository.save(new UserBlock(userA, userC));
            entityManager.flush();
            entityManager.clear();

            // when
            List<UserBlock> result = userBlockRepository.findAllByBlockerWithBlockedUser(userA);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).allSatisfy(block -> {
                assertThat(block.getBlocker().getId()).isEqualTo(userA.getId());
                assertThat(block.getBlockedUser()).isNotNull();
            });
        }
    }

    @Nested
    class existsByBlockerAndBlockedUser {

        @Test
        void 차단_존재_여부를_확인한다() {
            // given
            userBlockRepository.save(new UserBlock(userA, userB));

            // when & then
            assertThat(userBlockRepository.existsByBlockerAndBlockedUser(userA, userB)).isTrue();
            assertThat(userBlockRepository.existsByBlockerAndBlockedUser(userB, userA)).isFalse();
        }
    }
}
