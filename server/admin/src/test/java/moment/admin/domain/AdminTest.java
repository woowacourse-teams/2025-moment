package moment.admin.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(ReplaceUnderscores.class)
class AdminTest {

    @Test
    void 정상적으로_기본_역할_ADMIN으로_관리자를_생성한다() {
        // given
        String email = "admin@test.com";
        String name = "Admin";
        String password = "password123";

        // when
        Admin admin = new Admin(email, name, password);

        // then
        assertThat(admin.getEmail()).isEqualTo(email);
        assertThat(admin.getName()).isEqualTo(name);
        assertThat(admin.getPassword()).isEqualTo(password);
        assertThat(admin.getRole()).isEqualTo(AdminRole.ADMIN);
    }

    @Test
    void 정상적으로_SUPER_ADMIN_역할로_관리자를_생성한다() {
        // given
        String email = "superadmin@test.com";
        String name = "SuperAdmin";
        String password = "password123";
        AdminRole role = AdminRole.SUPER_ADMIN;

        // when
        Admin admin = new Admin(email, name, password, role);

        // then
        assertThat(admin.getEmail()).isEqualTo(email);
        assertThat(admin.getName()).isEqualTo(name);
        assertThat(admin.getPassword()).isEqualTo(password);
        assertThat(admin.getRole()).isEqualTo(AdminRole.SUPER_ADMIN);
    }

    @Test
    void 이메일이_null이면_예외를_던진다() {
        // given
        String email = null;
        String name = "Admin";
        String password = "password123";

        // when & then
        assertThatThrownBy(() -> new Admin(email, name, password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 email 형식입니다.");
    }

    @Test
    void 이메일이_빈_문자열이면_예외를_던진다() {
        // given
        String email = "";
        String name = "Admin";
        String password = "password123";

        // when & then
        assertThatThrownBy(() -> new Admin(email, name, password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 email 형식입니다.");
    }

    @Test
    void 이메일이_공백만_있으면_예외를_던진다() {
        // given
        String email = "   ";
        String name = "Admin";
        String password = "password123";

        // when & then
        assertThatThrownBy(() -> new Admin(email, name, password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 email 형식입니다.");
    }

    @Test
    void 이메일_형식이_잘못되면_예외를_던진다() {
        // given
        String invalidEmail = "invalid-email";
        String name = "Admin";
        String password = "password123";

        // when & then
        assertThatThrownBy(() -> new Admin(invalidEmail, name, password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 email 형식입니다.");
    }

    @Test
    void 이메일에_at_기호가_없으면_예외를_던진다() {
        // given
        String invalidEmail = "adminemail.com";
        String name = "Admin";
        String password = "password123";

        // when & then
        assertThatThrownBy(() -> new Admin(invalidEmail, name, password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 email 형식입니다.");
    }

    @Test
    void 이메일에_도메인이_없으면_예외를_던진다() {
        // given
        String invalidEmail = "admin@";
        String name = "Admin";
        String password = "password123";

        // when & then
        assertThatThrownBy(() -> new Admin(invalidEmail, name, password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 email 형식입니다.");
    }

    @Test
    void 비밀번호가_null이면_예외를_던진다() {
        // given
        String email = "admin@test.com";
        String name = "Admin";
        String password = null;

        // when & then
        assertThatThrownBy(() -> new Admin(email, name, password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 password 형식입니다.");
    }

    @Test
    void 비밀번호가_빈_문자열이면_예외를_던진다() {
        // given
        String email = "admin@test.com";
        String name = "Admin";
        String password = "";

        // when & then
        assertThatThrownBy(() -> new Admin(email, name, password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 password 형식입니다.");
    }

    @Test
    void 비밀번호가_공백만_있으면_예외를_던진다() {
        // given
        String email = "admin@test.com";
        String name = "Admin";
        String password = "   ";

        // when & then
        assertThatThrownBy(() -> new Admin(email, name, password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 password 형식입니다.");
    }

    @Test
    void 이름이_null이면_예외를_던진다() {
        // given
        String email = "admin@test.com";
        String name = null;
        String password = "password123";

        // when & then
        assertThatThrownBy(() -> new Admin(email, name, password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 name 형식입니다.");
    }

    @Test
    void 이름이_빈_문자열이면_예외를_던진다() {
        // given
        String email = "admin@test.com";
        String name = "";
        String password = "password123";

        // when & then
        assertThatThrownBy(() -> new Admin(email, name, password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 name 형식입니다.");
    }

    @Test
    void 이름이_공백만_있으면_예외를_던진다() {
        // given
        String email = "admin@test.com";
        String name = "   ";
        String password = "password123";

        // when & then
        assertThatThrownBy(() -> new Admin(email, name, password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 name 형식입니다.");
    }

    @Test
    void 이름이_1글자면_예외를_던진다() {
        // given
        String email = "admin@test.com";
        String name = "A";
        String password = "password123";

        // when & then
        assertThatThrownBy(() -> new Admin(email, name, password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 name 형식입니다.");
    }

    @Test
    void 이름이_16글자면_예외를_던진다() {
        // given
        String email = "admin@test.com";
        String name = "1234567890123456"; // 16자
        String password = "password123";

        // when & then
        assertThatThrownBy(() -> new Admin(email, name, password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 name 형식입니다.");
    }

    @Test
    void 이름이_2글자면_정상적으로_생성된다() {
        // given
        String email = "admin@test.com";
        String name = "김철";
        String password = "password123";

        // when & then
        assertDoesNotThrow(() -> new Admin(email, name, password));
    }

    @Test
    void 이름이_15글자면_정상적으로_생성된다() {
        // given
        String email = "admin@test.com";
        String name = "123456789012345"; // 15자
        String password = "password123";

        // when & then
        assertDoesNotThrow(() -> new Admin(email, name, password));
    }

    @Test
    void SUPER_ADMIN_역할이면_isSuperAdmin이_true를_반환한다() {
        // given
        Admin admin = new Admin("admin@test.com", "Admin", "password123", AdminRole.SUPER_ADMIN);

        // when
        boolean result = admin.isSuperAdmin();

        // then
        assertThat(result).isTrue();
    }

    @Test
    void ADMIN_역할이면_isSuperAdmin이_false를_반환한다() {
        // given
        Admin admin = new Admin("admin@test.com", "Admin", "password123", AdminRole.ADMIN);

        // when
        boolean result = admin.isSuperAdmin();

        // then
        assertThat(result).isFalse();
    }

    @Test
    void SUPER_ADMIN_역할이면_canRegisterAdmin이_true를_반환한다() {
        // given
        Admin admin = new Admin("admin@test.com", "Admin", "password123", AdminRole.SUPER_ADMIN);

        // when
        boolean result = admin.canRegisterAdmin();

        // then
        assertThat(result).isTrue();
    }

    @Test
    void ADMIN_역할이면_canRegisterAdmin이_false를_반환한다() {
        // given
        Admin admin = new Admin("admin@test.com", "Admin", "password123", AdminRole.ADMIN);

        // when
        boolean result = admin.canRegisterAdmin();

        // then
        assertThat(result).isFalse();
    }
}
