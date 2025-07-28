package moment.moment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import lombok.ToString;
import moment.global.domain.BaseEntity;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.user.domain.User;

@Entity(name = "moments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString
public class Moment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 100)
    private String content;

    @Column(nullable = false)
    private boolean isMatched;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "momenter_id")
    private User momenter;

    public Moment(String content, User momenter) {
        validate(content, momenter);
        this.content = content;
        this.isMatched = false;
        this.momenter = momenter;
    }

    public Moment(String content, boolean isMatched, User momenter) {
        validate(content, momenter);
        this.content = content;
        this.isMatched = isMatched;
        this.momenter = momenter;
    }
  
    private void validate(String content, User momenter) {
        validateContent(content);
        validateUser(momenter);
    }

    private void validateContent(String content) {
        if (content == null || content.isEmpty()) {
            throw new IllegalArgumentException("moment의 content는 null이거나 빈 값이어서는 안 됩니다.");
        }
    }

    private void validateUser(User momenter) {
        if (momenter == null) {
            throw new IllegalArgumentException("momenter가 null이 되어서는 안 됩니다.");
        }
    }

    public void matchComplete() {
        isMatched = true;
    }

    public boolean alreadyMatched() {
        return isMatched;
    }

    public Long getMomenterId() {
        return momenter.getId();
    }
}
