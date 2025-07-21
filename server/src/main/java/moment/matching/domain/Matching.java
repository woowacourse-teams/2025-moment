package moment.matching.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import moment.global.domain.BaseEntity;
import moment.moment.domain.Moment;
import moment.user.domain.User;

@Entity(name = "matchings")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString
public class Matching extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "moment_id")
    private Moment moment;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "commenter_id")
    private User commenter;

    public Matching(Moment moment, User commenter) {
        this.moment = moment;
        this.commenter = commenter;
    }

    public long getCommenterId() {
        return commenter.getId();
    }
}
