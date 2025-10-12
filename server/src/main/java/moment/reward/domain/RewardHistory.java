package moment.reward.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import moment.global.domain.BaseEntity;
import moment.user.domain.User;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity(name = "reward_history")
@SQLDelete(sql = "UPDATE reward_history SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class RewardHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    private Integer amount;

    @Enumerated(EnumType.STRING)
    private Reason reason;

    private Long contentId;

    private LocalDateTime deletedAt;

    public RewardHistory(User user, Reason reason, Long contentId) {
        validate(user, reason, contentId);
        this.user = user;
        this.amount = reason.getPointTo();
        this.reason = reason;
        this.contentId = contentId;
    }

    private void validate(User user, Reason reason, Long contentId) {
        validateUser(user);
        validateAmount(reason.getPointTo());
        validateReason(reason);
        validateContentId(contentId);
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("user가 null이 되어서는 안 됩니다.");
        }
    }

    private void validateAmount(Integer amount) {
        if (amount == 0) {
            throw new IllegalArgumentException("amount가 0이 되어서는 안 됩니다.");
        }
    }

    private void validateReason(Reason reason) {
        if (reason == null) {
            throw new IllegalArgumentException("reason이 null이 되어서는 안 됩니다.");
        }
    }

    private void validateContentId(Long contentId) {
        if (contentId == null) {
            throw new IllegalArgumentException("contentId는 null이 되어서는 안 됩니다.");
        }
    }
}
