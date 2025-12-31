package moment.admin.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import moment.global.domain.BaseEntity;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity(name = "admins")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE admins SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Admin extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 100)
    private String name;

    private LocalDateTime deletedAt;

    public Admin(String email, String name, String password) {
        validateEmail(email);
        validateName(name);
        this.email = email;
        this.name = name;
        this.password = password;
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new MomentException(ErrorCode.ADMIN_INVALID_INFO);
        }
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new MomentException(ErrorCode.ADMIN_INVALID_INFO);
        }
    }
}
