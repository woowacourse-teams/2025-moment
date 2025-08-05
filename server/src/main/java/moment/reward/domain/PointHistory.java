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
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import moment.global.domain.BaseEntity;
import moment.user.domain.User;

@Entity(name = "point_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class PointHistory extends BaseEntity {

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

    public PointHistory(User user, Integer amount, Reason reason) {
        validate(user, amount, reason);
        this.user = user;
        this.amount = amount;
        this.reason = reason;
    }

    private void validate(User user, Integer amount, Reason reason) {
        validateUser(user);
        validateAmount(amount);
        validateReason(reason);
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("user가 null이 되어서는 안 됩니다.");
        }
    }

    private void validateAmount(Integer amount) {
        if (amount == null || amount == 0) {
            throw new IllegalArgumentException("amount가 null이나 0이 되어서는 안 됩니다.");
        }
    }

    private void validateReason(Reason reason) {
        if (reason == null) {
            throw new IllegalArgumentException("reason이 null이 되어서는 안 됩니다.");
        }
    }
}
