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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Entity(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString
public class User extends BaseEntity {

    private static final Pattern EMAIL_REGEX = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern NICKNAME_REGEX = Pattern.compile("^[a-zA-Z0-9가-힣]{2,6}$");
    private static final int DEFAULT_POINT = 0;

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

    @Column(nullable = false)
    private ProviderType providerType;
  
    private Integer currentPoint = DEFAULT_POINT;

    public User(String email, String password, String nickname, ProviderType providerType) {
        validate(email, password, nickname);
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.providerType = providerType;
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

        Matcher matcher = EMAIL_REGEX.matcher(email);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("유효하지 않은 이메일 형식입니다.");
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

        Matcher matcher = NICKNAME_REGEX.matcher(nickname);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("유효하지 않은 닉네임 형식입니다.");
        }
    }

    public boolean checkPassword(String loginPassword) {
        return password.equals(loginPassword);
    }

    public void addPoint(int commentCreationPoint) {
        currentPoint += commentCreationPoint;
    }
}
