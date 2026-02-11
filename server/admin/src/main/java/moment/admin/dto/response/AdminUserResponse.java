package moment.admin.dto.response;

import moment.user.domain.ProviderType;
import moment.user.domain.User;

import java.time.LocalDateTime;

public record AdminUserResponse(
    Long id,
    String email,
    String nickname,
    ProviderType providerType,
    LocalDateTime createdAt
) {
    public static AdminUserResponse from(User user) {
        return new AdminUserResponse(
            user.getId(),
            user.getEmail(),
            user.getNickname(),
            user.getProviderType(),
            user.getCreatedAt()
        );
    }
}
