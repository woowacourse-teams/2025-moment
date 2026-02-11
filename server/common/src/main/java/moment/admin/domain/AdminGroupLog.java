package moment.admin.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import moment.global.domain.BaseEntity;

@Entity
@Table(name = "admin_group_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminGroupLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long adminId;

    @Column(nullable = false)
    private String adminEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdminGroupLogType type;

    @Column(nullable = false)
    private Long groupId;

    private Long targetId;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String beforeValue;

    @Column(columnDefinition = "TEXT")
    private String afterValue;

    @Builder
    public AdminGroupLog(Long adminId, String adminEmail, AdminGroupLogType type,
                         Long groupId, Long targetId, String description,
                         String beforeValue, String afterValue) {
        this.adminId = adminId;
        this.adminEmail = adminEmail;
        this.type = type;
        this.groupId = groupId;
        this.targetId = targetId;
        this.description = description;
        this.beforeValue = beforeValue;
        this.afterValue = afterValue;
    }
}
