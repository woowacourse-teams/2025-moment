package moment.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import moment.global.domain.BaseEntity;
import moment.user.domain.User;

@Entity(name = "refresh_tokens")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString
public class RefreshToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String tokenValue;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @Column(nullable = false)
    private LocalDateTime issuedAt;

    @Column(nullable = false)
    private LocalDateTime expiredAt;

    public RefreshToken(String tokenValue, User user, Date issuedAt, Date expiredAt) {
        this.tokenValue = tokenValue;
        this.user = user;
        this.issuedAt = issuedAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        this.expiredAt = expiredAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public boolean isExpired(LocalDateTime now) {
        return now.isAfter(expiredAt);
    }

    public void renew(String newRefreshToken, Date issuedAt, Date expiredAt) {
        this.tokenValue = newRefreshToken;
        this.issuedAt = issuedAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        this.expiredAt = expiredAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
