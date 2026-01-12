package moment.admin.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import moment.admin.domain.Admin;
import moment.admin.domain.AdminRole;
import moment.admin.infrastructure.AdminRepository;
import moment.common.DatabaseCleaner;
import moment.config.TestTags;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@Tag(TestTags.INTEGRATION)
@ActiveProfiles("test")
@SpringBootTest
@Transactional
@DisplayNameGeneration(ReplaceUnderscores.class)
class AdminServiceTest {

    @Autowired
    private AdminService adminService;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void setUp() {
        databaseCleaner.clean();
    }

    @AfterEach
    void tearDown() {
        databaseCleaner.clean();
    }

    @Test
    void 정상적으로_관리자_인증에_성공한다() {
        // given
        String rawPassword = "password123!@#";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        Admin admin = new Admin("admin@test.com", "Admin", encodedPassword, AdminRole.ADMIN);
        adminRepository.save(admin);

        // when
        Admin result = adminService.authenticateAdmin("admin@test.com", rawPassword);

        // then
        assertAll(
                () -> assertThat(result.getEmail()).isEqualTo("admin@test.com"),
                () -> assertThat(result.getName()).isEqualTo("Admin"),
                () -> assertThat(result.getRole()).isEqualTo(AdminRole.ADMIN)
        );
    }

    @Test
    void 존재하지_않는_이메일로_인증_시_ADMIN_LOGIN_FAILED_예외를_던진다() {
        // when & then
        assertThatThrownBy(() -> adminService.authenticateAdmin("nonexistent@test.com", "password"))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ADMIN_LOGIN_FAILED);
    }

    @Test
    void 잘못된_비밀번호로_인증_시_ADMIN_LOGIN_FAILED_예외를_던진다() {
        // given
        String correctPassword = "password123!@#";
        String encodedPassword = passwordEncoder.encode(correctPassword);
        Admin admin = new Admin("admin@test.com", "Admin", encodedPassword, AdminRole.ADMIN);
        adminRepository.save(admin);

        // when & then
        assertThatThrownBy(() -> adminService.authenticateAdmin("admin@test.com", "wrongPassword"))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ADMIN_LOGIN_FAILED);
    }

    @Test
    void ID로_관리자를_정상적으로_조회한다() {
        // given
        String encodedPassword = passwordEncoder.encode("password123!@#");
        Admin admin = new Admin("admin@test.com", "Admin", encodedPassword, AdminRole.ADMIN);
        Admin savedAdmin = adminRepository.save(admin);

        // when
        Admin result = adminService.getAdminById(savedAdmin.getId());

        // then
        assertAll(
                () -> assertThat(result.getId()).isEqualTo(savedAdmin.getId()),
                () -> assertThat(result.getEmail()).isEqualTo("admin@test.com"),
                () -> assertThat(result.getName()).isEqualTo("Admin")
        );
    }

    @Test
    void 존재하지_않는_ID로_조회_시_ADMIN_NOT_FOUND_예외를_던진다() {
        // when & then
        assertThatThrownBy(() -> adminService.getAdminById(999L))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ADMIN_NOT_FOUND);
    }

    @Test
    void 정상적으로_기본_역할_ADMIN으로_관리자를_생성한다() {
        // given
        String email = "newadmin@test.com";
        String name = "NewAdmin";
        String password = "password123!@#";

        // when
        Admin result = adminService.createAdmin(email, name, password);

        // then
        assertAll(
                () -> assertThat(result.getId()).isNotNull(),
                () -> assertThat(result.getEmail()).isEqualTo(email),
                () -> assertThat(result.getName()).isEqualTo(name),
                () -> assertThat(result.getRole()).isEqualTo(AdminRole.ADMIN),
                () -> assertThat(passwordEncoder.matches(password, result.getPassword())).isTrue()
        );

        // 데이터베이스에 실제로 저장되었는지 확인
        Admin savedAdmin = adminRepository.findByEmail(email).orElseThrow();
        assertThat(savedAdmin.getEmail()).isEqualTo(email);
    }

    @Test
    void 정상적으로_SUPER_ADMIN_역할로_관리자를_생성한다() {
        // given
        String email = "superadmin@test.com";
        String name = "SuperAdmin";
        String password = "password123!@#";

        // when
        Admin result = adminService.createAdmin(email, name, password, AdminRole.SUPER_ADMIN);

        // then
        assertAll(
                () -> assertThat(result.getId()).isNotNull(),
                () -> assertThat(result.getEmail()).isEqualTo(email),
                () -> assertThat(result.getName()).isEqualTo(name),
                () -> assertThat(result.getRole()).isEqualTo(AdminRole.SUPER_ADMIN),
                () -> assertThat(passwordEncoder.matches(password, result.getPassword())).isTrue()
        );

        // 데이터베이스에 실제로 저장되었는지 확인
        Admin savedAdmin = adminRepository.findByEmail(email).orElseThrow();
        assertThat(savedAdmin.getRole()).isEqualTo(AdminRole.SUPER_ADMIN);
    }

    @Test
    void 중복된_이메일로_관리자_생성_시_ADMIN_EMAIL_CONFLICT_예외를_던진다() {
        // given
        String duplicateEmail = "duplicate@test.com";
        adminService.createAdmin(duplicateEmail, "Admin1", "password123!@#");

        // when & then
        assertThatThrownBy(() -> adminService.createAdmin(duplicateEmail, "Admin2", "password456!@#"))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ADMIN_EMAIL_CONFLICT);

        // 데이터베이스에 한 개만 존재하는지 확인
        long count = adminRepository.findAll().stream()
                .filter(admin -> admin.getEmail().equals(duplicateEmail))
                .count();
        assertThat(count).isEqualTo(1);
    }

    @Test
    void 비밀번호가_정상적으로_해싱되어_저장된다() {
        // given
        String email = "admin@test.com";
        String name = "Admin";
        String rawPassword = "password123!@#";

        // when
        Admin result = adminService.createAdmin(email, name, rawPassword);

        // then
        assertAll(
                () -> assertThat(result.getPassword()).isNotEqualTo(rawPassword),
                () -> assertThat(result.getPassword()).startsWith("$2a$"),
                () -> assertThat(passwordEncoder.matches(rawPassword, result.getPassword())).isTrue()
        );
    }

    @Test
    void 이메일_존재_여부를_정확히_반환한다_존재하는_경우() {
        // given
        String email = "existing@test.com";
        adminService.createAdmin(email, "Admin", "password123!@#");

        // when
        boolean result = adminService.existsByEmail(email);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 이메일_존재_여부를_정확히_반환한다_존재하지_않는_경우() {
        // when
        boolean result = adminService.existsByEmail("nonexistent@test.com");

        // then
        assertThat(result).isFalse();
    }

    @Test
    void SUPER_ADMIN은_관리자_등록_권한_검증을_통과한다() {
        // given
        Admin superAdmin = adminService.createAdmin(
                "super@test.com",
                "SuperAdmin",
                "password123!@#",
                AdminRole.SUPER_ADMIN
        );

        // when & then
        assertDoesNotThrow(() -> adminService.validateAdminRegistrationPermission(superAdmin.getId()));
    }

    @Test
    void 일반_ADMIN은_관리자_등록_권한_검증에서_ADMIN_UNAUTHORIZED_예외를_던진다() {
        // given
        Admin normalAdmin = adminService.createAdmin(
                "admin@test.com",
                "Admin",
                "password123!@#",
                AdminRole.ADMIN
        );

        // when & then
        assertThatThrownBy(() -> adminService.validateAdminRegistrationPermission(normalAdmin.getId()))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ADMIN_UNAUTHORIZED);
    }

    @Test
    void 존재하지_않는_관리자_ID로_권한_검증_시_ADMIN_NOT_FOUND_예외를_던진다() {
        // when & then
        assertThatThrownBy(() -> adminService.validateAdminRegistrationPermission(999L))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ADMIN_NOT_FOUND);
    }

    @Test
    void 동일한_이메일로_동시에_여러_관리자_생성_시도_시_한_명만_생성된다() {
        // given
        String email = "concurrent@test.com";
        String password = "password123!@#";

        // when
        adminService.createAdmin(email, "Admin1", password);

        // then
        assertThatThrownBy(() -> adminService.createAdmin(email, "Admin2", password))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ADMIN_EMAIL_CONFLICT);

        // 최종적으로 한 명만 존재
        long count = adminRepository.findAll().stream()
                .filter(admin -> admin.getEmail().equals(email))
                .count();
        assertThat(count).isEqualTo(1);
    }
}
