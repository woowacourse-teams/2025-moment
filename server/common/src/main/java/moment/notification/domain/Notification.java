package moment.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
import lombok.ToString;
import moment.global.domain.BaseEntity;
import moment.notification.infrastructure.SourceDataConverter;
import moment.user.domain.User;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity(name = "notifications")
@SQLDelete(sql = "UPDATE notifications SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    @Column(name = "source_data", columnDefinition = "JSON")
    @Convert(converter = SourceDataConverter.class)
    private SourceData sourceData;

    @Column(name = "link", length = 512)
    private String link;

    private boolean isRead;

    private LocalDateTime deletedAt;

    public Notification(User user,
                        NotificationType notificationType,
                        SourceData sourceData,
                        String link) {
        this.user = user;
        this.notificationType = notificationType;
        this.sourceData = sourceData;
        this.link = link;
        this.isRead = false;
    }

    public void markAsRead() {
        isRead = true;
    }
}
