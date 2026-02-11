package moment.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import moment.user.domain.ProviderType;
import moment.user.domain.User;

@Schema(description = "사용자 상세 정보 응답")
public record AdminUserDetailResponse(
    @Schema(description = "사용자 ID", example = "1")
    Long id,

    @Schema(description = "사용자 이메일", example = "user@example.com")
    String email,

    @Schema(description = "사용자 닉네임", example = "홍길동")
    String nickname,

    @Schema(description = "로그인 제공자 타입", example = "KAKAO")
    ProviderType providerType,

    @Schema(description = "가입 일시", example = "2024-01-15T10:30:00")
    LocalDateTime createdAt,

    @Schema(description = "탈퇴 일시 (Soft Delete)", example = "null")
    LocalDateTime deletedAt
) {
    public static AdminUserDetailResponse from(User user) {
        return new AdminUserDetailResponse(
            user.getId(),
            user.getEmail(),
            user.getNickname(),
            user.getProviderType(),
            user.getCreatedAt(),
            user.getDeletedAt()
        );
    }
}
