package moment.admin.dto.response;

import moment.user.domain.User;

public record AdminMemberUserInfo(
    Long userId,
    String email,
    String nickname
) {
    public static AdminMemberUserInfo from(User user) {
        return new AdminMemberUserInfo(
            user.getId(),
            user.getEmail(),
            user.getNickname()
        );
    }
}
