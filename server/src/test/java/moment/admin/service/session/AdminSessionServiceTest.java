package moment.admin.service.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import moment.admin.domain.Admin;
import moment.admin.domain.AdminRole;
import moment.admin.domain.AdminSession;
import moment.admin.dto.response.AdminSessionDetailResponse;
import moment.admin.infrastructure.AdminRepository;
import moment.admin.infrastructure.AdminSessionRepository;
import moment.admin.global.exception.AdminErrorCode;
import moment.admin.global.exception.AdminException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class AdminSessionServiceTest {

    @Mock
    private AdminSessionRepository adminSessionRepository;

    @Mock
    private AdminRepository adminRepository;

    @InjectMocks
    private AdminSessionService adminSessionService;

    private Admin testAdmin;
    private AdminSession testSession;

    @BeforeEach
    void setUp() {
        testAdmin = createTestAdmin(1L, "test@example.com", "테스트관리자", AdminRole.ADMIN);
        testSession = createTestSession(1L, 1L, "session-123", "127.0.0.1",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
    }

    @Nested
    class getSessionDetail_메서드는 {

        @Test
        void 세션_ID로_상세_정보를_조회한다() {
            // given
            Long sessionId = 1L;
            when(adminSessionRepository.findById(sessionId)).thenReturn(Optional.of(testSession));
            when(adminRepository.findById(testSession.getAdminId())).thenReturn(Optional.of(testAdmin));

            // when
            AdminSessionDetailResponse result = adminSessionService.getSessionDetail(sessionId);

            // then
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.adminName()).isEqualTo("테스트관리자");
            assertThat(result.adminEmail()).isEqualTo("test@example.com");
            assertThat(result.ipAddress()).isEqualTo("127.0.0.1");
            assertThat(result.browser()).isEqualTo("Chrome");
            assertThat(result.os()).isEqualTo("Windows");
            assertThat(result.deviceType()).isEqualTo("Desktop");
            assertThat(result.isActive()).isTrue();
        }

        @Test
        void 존재하지_않는_세션이면_예외를_던진다() {
            // given
            Long sessionId = 999L;
            when(adminSessionRepository.findById(sessionId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adminSessionService.getSessionDetail(sessionId))
                    .isInstanceOf(AdminException.class)
                    .hasFieldOrPropertyWithValue("errorCode", AdminErrorCode.SESSION_NOT_FOUND);
        }

        @Test
        void 세션에_연결된_관리자가_없으면_예외를_던진다() {
            // given
            Long sessionId = 1L;
            when(adminSessionRepository.findById(sessionId)).thenReturn(Optional.of(testSession));
            when(adminRepository.findById(testSession.getAdminId())).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adminSessionService.getSessionDetail(sessionId))
                    .isInstanceOf(AdminException.class)
                    .hasFieldOrPropertyWithValue("errorCode", AdminErrorCode.NOT_FOUND);
        }
    }

    private Admin createTestAdmin(Long id, String email, String name, AdminRole role) {
        try {
            Admin admin = new Admin(email, name, "hashedPassword", role);
            java.lang.reflect.Field idField = Admin.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(admin, id);
            return admin;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private AdminSession createTestSession(Long id, Long adminId, String sessionId, String ipAddress, String userAgent) {
        try {
            AdminSession session = new AdminSession(adminId, sessionId, ipAddress, userAgent);
            java.lang.reflect.Field idField = AdminSession.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(session, id);
            return session;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
