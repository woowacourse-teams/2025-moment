package moment.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import moment.user.domain.ProviderType;
import moment.user.domain.User;

@Schema(description = "마이페이지 프로필 정보 응답")
public record MyPageProfileResponse(
        @Schema(description = "사용자 닉네임", example = "신비로운 해성의 이카루스")
        String nickname,

        @Schema(description = "사용자 이메일", example = "test123@gmail.com")
        String email,

        @Schema(description = "사용자 가입 유형", example = "EMAIL")
        ProviderType loginType
) {
    public static MyPageProfileResponse from(User user) {
        return new MyPageProfileResponse(
                user.getNickname(),
                user.getEmail(),
                user.getProviderType()
        );
    }
}
