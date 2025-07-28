package moment.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import moment.global.domain.BaseEntity;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;

@Entity(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString
public class User extends BaseEntity {

    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;

    public User(String email, String password, String nickname) {
        validate(email, password, nickname);
        this.email = email;
        this.password = password;
        this.nickname = nickname;
    }

    private void validate(String email, String password, String nickname) {
        validateEmail(email);
        validatePassword(password);
        validateNickname(nickname);
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email이 null이거나 빈 값이어서는 안 됩니다.");
        }
        // TODO : 도메인 검증은 전부 서버 예외일까? 클라이언트 예외는 DTO 검증 해서 거르고 클라이언트한테 반환할 것이니까?
        if (!email.matches(EMAIL_REGEX)) {
            throw new MomentException(ErrorCode.EMAIL_INVALID);
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("password가 null이거나 빈 값이어서는 안 됩니다.");
        }
    }

    private void validateNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new IllegalArgumentException("nickname이 null이거나 빈 값이어서는 안 됩니다.");
        }
    }

    public boolean checkPassword(String loginPassword) {
        return password.equals(loginPassword);
    }
}
