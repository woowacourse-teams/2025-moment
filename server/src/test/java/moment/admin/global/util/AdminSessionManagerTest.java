package moment.admin.global.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;
import moment.admin.domain.AdminRole;
import moment.admin.infrastructure.AdminRepository;
import moment.admin.infrastructure.AdminSessionRepository;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

@DisplayNameGeneration(ReplaceUnderscores.class)
class AdminSessionManagerTest {

    private static final String ADMIN_SESSION_KEY = "ADMIN_ID";
    private static final String ADMIN_ROLE_KEY = "ADMIN_ROLE";

    private AdminSessionManager sessionManager;
    private MockHttpSession session;

    @BeforeEach
    void setUp() throws Exception {
        AdminSessionRepository adminSessionRepository = mock(AdminSessionRepository.class);
        AdminRepository adminRepository = mock(AdminRepository.class);
        sessionManager = new AdminSessionManager(adminSessionRepository, adminRepository);

        Field sessionTimeoutField = AdminSessionManager.class.getDeclaredField("sessionTimeout");
        sessionTimeoutField.setAccessible(true);
        sessionTimeoutField.set(sessionManager, 3600);

        session = new MockHttpSession();
    }

    @Test
    void 세션에_관리자_ID와_역할을_정상적으로_저장하고_타임아웃을_설정한다() {
        // given
        Long adminId = 1L;
        AdminRole role = AdminRole.SUPER_ADMIN;

        // when
        sessionManager.setAuth(session, adminId, role);

        // then
        assertThat(session.getAttribute(ADMIN_SESSION_KEY)).isEqualTo(1L);
        assertThat(session.getAttribute(ADMIN_ROLE_KEY)).isEqualTo(AdminRole.SUPER_ADMIN);
        assertThat(session.getMaxInactiveInterval()).isEqualTo(3600);
    }

    @Test
    void 유효한_세션이면_예외를_던지지_않는다() {
        // given
        session.setAttribute(ADMIN_SESSION_KEY, 1L);
        session.setAttribute(ADMIN_ROLE_KEY, AdminRole.ADMIN);

        // when & then
        assertDoesNotThrow(() -> sessionManager.validateAuthorized(session));
    }

    @Test
    void 세션이_null이면_ADMIN_UNAUTHORIZED_예외를_던진다() {
        // when & then
        assertThatThrownBy(() -> sessionManager.validateAuthorized(null))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ADMIN_UNAUTHORIZED);
    }

    @Test
    void 세션_검증_시_관리자_ID가_없으면_예외를_던진다() {
        // given
        session.setAttribute(ADMIN_ROLE_KEY, AdminRole.ADMIN);

        // when & then
        assertThatThrownBy(() -> sessionManager.validateAuthorized(session))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ADMIN_UNAUTHORIZED);
    }

    @Test
    void 세션_검증_시_관리자_역할이_없으면_예외를_던진다() {
        // given
        session.setAttribute(ADMIN_SESSION_KEY, 1L);

        // when & then
        assertThatThrownBy(() -> sessionManager.validateAuthorized(session))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ADMIN_UNAUTHORIZED);
    }

    @Test
    void 세션에서_관리자_ID를_정상적으로_반환한다() {
        // given
        session.setAttribute(ADMIN_SESSION_KEY, 123L);

        // when
        Long adminId = sessionManager.getId(session);

        // then
        assertThat(adminId).isEqualTo(123L);
    }

    @Test
    void ID_조회_시_세션에_값이_없으면_예외를_던진다() {
        // when & then
        assertThatThrownBy(() -> sessionManager.getId(session))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ADMIN_UNAUTHORIZED);
    }

    @Test
    void 세션에서_관리자_역할을_정상적으로_반환한다() {
        // given
        session.setAttribute(ADMIN_ROLE_KEY, AdminRole.SUPER_ADMIN);

        // when
        AdminRole role = sessionManager.getRole(session);

        // then
        assertThat(role).isEqualTo(AdminRole.SUPER_ADMIN);
    }

    @Test
    void 역할_조회_시_세션에_값이_없으면_예외를_던진다() {
        // when & then
        assertThatThrownBy(() -> sessionManager.getRole(session))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ADMIN_UNAUTHORIZED);
    }

    @Test
    void SUPER_ADMIN_역할이면_true를_반환한다() {
        // given
        session.setAttribute(ADMIN_ROLE_KEY, AdminRole.SUPER_ADMIN);

        // when
        boolean result = sessionManager.isSuperAdmin(session);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 일반_ADMIN_역할이면_false를_반환한다() {
        // given
        session.setAttribute(ADMIN_ROLE_KEY, AdminRole.ADMIN);

        // when
        boolean result = sessionManager.isSuperAdmin(session);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void 관리자_관리_권한은_SUPER_ADMIN만_true를_반환한다() {
        // given
        session.setAttribute(ADMIN_ROLE_KEY, AdminRole.SUPER_ADMIN);

        // when
        boolean result = sessionManager.canManageAdmins(session);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 관리자_관리_권한은_일반_ADMIN이면_false를_반환한다() {
        // given
        session.setAttribute(ADMIN_ROLE_KEY, AdminRole.ADMIN);

        // when
        boolean result = sessionManager.canManageAdmins(session);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void 세션을_무효화한다() {
        // given
        session.setAttribute(ADMIN_SESSION_KEY, 1L);

        // when
        sessionManager.invalidate(session);

        // then
        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    void null_세션을_안전하게_처리한다() {
        // when & then
        assertDoesNotThrow(() -> sessionManager.invalidate(null));
    }
}
