package moment.admin.dto.response;

import java.time.LocalDateTime;
import moment.admin.domain.AdminGroupLog;

public record AdminGroupLogResponse(
    Long id,
    Long adminId,
    String adminEmail,
    String type,
    Long groupId,
    Long targetId,
    String description,
    String beforeValue,
    String afterValue,
    LocalDateTime createdAt
) {
    public static AdminGroupLogResponse from(AdminGroupLog log) {
        return new AdminGroupLogResponse(
            log.getId(),
            log.getAdminId(),
            log.getAdminEmail(),
            log.getType().name(),
            log.getGroupId(),
            log.getTargetId(),
            log.getDescription(),
            log.getBeforeValue(),
            log.getAfterValue(),
            log.getCreatedAt()
        );
    }
}
