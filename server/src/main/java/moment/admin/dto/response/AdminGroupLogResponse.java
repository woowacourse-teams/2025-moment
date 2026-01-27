package moment.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import moment.admin.domain.AdminGroupLog;

@Schema(description = "Admin 로그 정보")
public record AdminGroupLogResponse(
    @Schema(description = "로그 ID", example = "1")
    Long id,

    @Schema(description = "관리자 ID", example = "1")
    Long adminId,

    @Schema(description = "관리자 이메일", example = "admin@example.com")
    String adminEmail,

    @Schema(description = "로그 유형", example = "GROUP_UPDATE")
    String type,

    @Schema(description = "대상 그룹 ID", example = "1")
    Long groupId,

    @Schema(description = "대상 ID (멤버, 모멘트 등)", example = "10")
    Long targetId,

    @Schema(description = "작업 설명", example = "그룹 정보 수정")
    String description,

    @Schema(description = "변경 전 값", example = "{\"name\": \"이전 이름\"}")
    String beforeValue,

    @Schema(description = "변경 후 값", example = "{\"name\": \"새 이름\"}")
    String afterValue,

    @Schema(description = "생성 일시", example = "2024-01-20T15:30:00")
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
