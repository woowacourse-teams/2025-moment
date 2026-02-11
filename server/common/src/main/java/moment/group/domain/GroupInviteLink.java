package moment.group.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import moment.global.domain.BaseEntity;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "group_invite_links")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE group_invite_links SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class GroupInviteLink extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(nullable = false, unique = true, length = 36)
    private String code;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public GroupInviteLink(Group group, int validDays) {
        this.group = group;
        this.code = UUID.randomUUID().toString();
        this.expiredAt = LocalDateTime.now().plusDays(validDays);
        this.isActive = true;
    }

    public boolean isValid() {
        return isActive && LocalDateTime.now().isBefore(expiredAt);
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }

    public void extendExpiration(int days) {
        this.expiredAt = LocalDateTime.now().plusDays(days);
    }
}
