package moment.admin.service.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import moment.config.TestTags;
import moment.fixture.UserFixture;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import moment.admin.dto.request.AdminUserUpdateRequest;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@Tag(TestTags.INTEGRATION)
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
@DisplayNameGeneration(ReplaceUnderscores.class)
class AdminUserServiceTest {

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void 오프셋_기반_페이징으로_사용자_목록을_조회한다() {
        List<User> users = UserFixture.createUsersByAmount(10);
        users.forEach(userRepository::save);

        Page<User> page = adminUserService.getAllUsers(0, 5);

        assertAll(
            () -> assertThat(page.getContent()).hasSize(5),
            () -> assertThat(page.getTotalElements()).isEqualTo(10),
            () -> assertThat(page.getTotalPages()).isEqualTo(2),
            () -> assertThat(page.isFirst()).isTrue()
        );
    }

    @Test
    void 사용자_정보를_수정한다() {
        User user = UserFixture.createUser();
        User savedUser = userRepository.save(user);
        AdminUserUpdateRequest request = new AdminUserUpdateRequest(
            "새닉네임", 100, 200
        );

        adminUserService.updateUser(savedUser.getId(), request);

        User updatedUser = userRepository.findById(savedUser.getId()).get();
        assertAll(
            () -> assertThat(updatedUser.getNickname()).isEqualTo("새닉네임"),
            () -> assertThat(updatedUser.getAvailableStar()).isEqualTo(100),
            () -> assertThat(updatedUser.getExpStar()).isEqualTo(200)
        );
    }

    @Test
    void 사용자를_차단한다_Soft_Delete() {
        User user = UserFixture.createUser();
        User savedUser = userRepository.save(user);

        adminUserService.deleteUser(savedUser.getId());

        assertThat(userRepository.findById(savedUser.getId())).isEmpty();
    }
}
