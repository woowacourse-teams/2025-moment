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
import moment.admin.global.exception.AdminErrorCode;
import moment.admin.global.exception.AdminException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
                .isInstanceOf(AdminException.class)
                .hasFieldOrPropertyWithValue("errorCode", AdminErrorCode.LOGIN_FAILED);
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
                .isInstanceOf(AdminException.class)
                .hasFieldOrPropertyWithValue("errorCode", AdminErrorCode.LOGIN_FAILED);
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
                .isInstanceOf(AdminException.class)
                .hasFieldOrPropertyWithValue("errorCode", AdminErrorCode.NOT_FOUND);
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
                .isInstanceOf(AdminException.class)
                .hasFieldOrPropertyWithValue("errorCode", AdminErrorCode.DUPLICATE_EMAIL);

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
                .isInstanceOf(AdminException.class)
                .hasFieldOrPropertyWithValue("errorCode", AdminErrorCode.UNAUTHORIZED);
    }

    @Test
    void 존재하지_않는_관리자_ID로_권한_검증_시_ADMIN_NOT_FOUND_예외를_던진다() {
        // when & then
        assertThatThrownBy(() -> adminService.validateAdminRegistrationPermission(999L))
                .isInstanceOf(AdminException.class)
                .hasFieldOrPropertyWithValue("errorCode", AdminErrorCode.NOT_FOUND);
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
                .isInstanceOf(AdminException.class)
                .hasFieldOrPropertyWithValue("errorCode", AdminErrorCode.DUPLICATE_EMAIL);

        // 최종적으로 한 명만 존재
        long count = adminRepository.findAll().stream()
                .filter(admin -> admin.getEmail().equals(email))
                .count();
        assertThat(count).isEqualTo(1);
    }

    // ===== validateNotSelfBlock 테스트 =====

    @Test
    void 자기_자신을_차단하려고_하면_ADMIN_CANNOT_BLOCK_SELF_예외를_던진다() {
        // given
        Long adminId = 1L;

        // when & then
        assertThatThrownBy(() -> adminService.validateNotSelfBlock(adminId, adminId))
                .isInstanceOf(AdminException.class)
                .hasFieldOrPropertyWithValue("errorCode", AdminErrorCode.CANNOT_BLOCK_SELF);
    }

    @Test
    void 다른_관리자를_차단하려고_하면_예외가_발생하지_않는다() {
        // given
        Long currentAdminId = 1L;
        Long targetAdminId = 2L;

        // when & then
        assertDoesNotThrow(() -> adminService.validateNotSelfBlock(currentAdminId, targetAdminId));
    }

    // ===== validateNotLastSuperAdmin 테스트 =====

    @Test
    void 마지막_SUPER_ADMIN을_차단하려고_하면_ADMIN_LAST_SUPER_ADMIN_DELETE_예외를_던진다() {
        // given - 단 하나의 SUPER_ADMIN만 존재
        Admin superAdmin = adminService.createAdmin(
                "super@test.com",
                "SuperAdmin",
                "password123!@#",
                AdminRole.SUPER_ADMIN
        );

        // when & then
        assertThatThrownBy(() -> adminService.validateNotLastSuperAdmin(superAdmin.getId()))
                .isInstanceOf(AdminException.class)
                .hasFieldOrPropertyWithValue("errorCode", AdminErrorCode.CANNOT_BLOCK_LAST_SUPER_ADMIN);
    }

    @Test
    void 두_명_이상의_SUPER_ADMIN이_있을_때_차단하면_예외가_발생하지_않는다() {
        // given - 두 명의 SUPER_ADMIN 존재
        Admin superAdmin1 = adminService.createAdmin(
                "super1@test.com",
                "SuperAdmin1",
                "password123!@#",
                AdminRole.SUPER_ADMIN
        );
        adminService.createAdmin(
                "super2@test.com",
                "SuperAdmin2",
                "password123!@#",
                AdminRole.SUPER_ADMIN
        );

        // when & then - 첫 번째 SUPER_ADMIN 차단 시도
        assertDoesNotThrow(() -> adminService.validateNotLastSuperAdmin(superAdmin1.getId()));
    }

    @Test
    void 일반_ADMIN을_차단할_때는_마지막_SUPER_ADMIN_검증이_통과한다() {
        // given - 하나의 SUPER_ADMIN과 일반 ADMIN
        adminService.createAdmin(
                "super@test.com",
                "SuperAdmin",
                "password123!@#",
                AdminRole.SUPER_ADMIN
        );
        Admin normalAdmin = adminService.createAdmin(
                "admin@test.com",
                "Admin",
                "password123!@#",
                AdminRole.ADMIN
        );

        // when & then - 일반 ADMIN 차단 시도
        assertDoesNotThrow(() -> adminService.validateNotLastSuperAdmin(normalAdmin.getId()));
    }

    // ===== blockAdmin 테스트 =====

    @Test
    void 관리자_차단시_마지막_SUPER_ADMIN은_차단할_수_없다() {
        // given - 단 하나의 SUPER_ADMIN만 존재
        Admin superAdmin = adminService.createAdmin(
                "super@test.com",
                "SuperAdmin",
                "password123!@#",
                AdminRole.SUPER_ADMIN
        );

        // when & then
        assertThatThrownBy(() -> adminService.blockAdmin(superAdmin.getId()))
                .isInstanceOf(AdminException.class)
                .hasFieldOrPropertyWithValue("errorCode", AdminErrorCode.CANNOT_BLOCK_LAST_SUPER_ADMIN);
    }

    @Test
    void 일반_ADMIN_차단은_정상적으로_동작한다() {
        // given
        adminService.createAdmin(
                "super@test.com",
                "SuperAdmin",
                "password123!@#",
                AdminRole.SUPER_ADMIN
        );
        Admin targetAdmin = adminService.createAdmin(
                "admin@test.com",
                "Admin",
                "password123!@#",
                AdminRole.ADMIN
        );

        // when
        adminService.blockAdmin(targetAdmin.getId());

        // then - 차단된 관리자는 일반 조회에서 제외됨 (Soft Delete)
        // @SQLRestriction으로 인해 findById로는 조회되지 않음
        assertThat(adminRepository.findById(targetAdmin.getId())).isEmpty();
    }

    // ===== getAllAdmins 테스트 =====

    @Test
    void getAllAdmins_차단된_관리자도_포함하여_반환한다() {
        // given - 활성 관리자와 차단된 관리자 생성
        Admin activeAdmin = adminService.createAdmin(
                "active@test.com", "ActiveAdmin", "password123!@#", AdminRole.ADMIN);
        Admin blockedAdmin = adminService.createAdmin(
                "blocked@test.com", "BlockedAdmin", "password123!@#", AdminRole.ADMIN);
        adminService.blockAdmin(blockedAdmin.getId());

        // when
        Page<Admin> result = adminService.getAllAdmins(PageRequest.of(0, 20));

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        // 차단된 관리자가 포함되어 있는지 확인
        assertThat(result.getContent())
                .extracting(Admin::getEmail)
                .containsExactlyInAnyOrder("active@test.com", "blocked@test.com");
    }
}
