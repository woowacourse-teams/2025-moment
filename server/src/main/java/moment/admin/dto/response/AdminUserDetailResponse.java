package moment.admin.dto.response;

import java.time.LocalDateTime;
import moment.user.domain.ProviderType;
import moment.user.domain.User;

public record AdminUserDetailResponse(
    Long id,
    String email,
    String nickname,
    ProviderType providerType,
    LocalDateTime createdAt,
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
