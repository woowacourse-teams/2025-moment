package moment.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import moment.user.domain.Level;
import moment.user.domain.ProviderType;
import moment.user.domain.User;

@Schema(description = "마이페이지 프로필 정보 응답")
public record MyPageProfileResponse(
        @Schema(description = "사용자 닉네임", example = "신비로운 해성의 이카루스")
        String nickname,

        @Schema(description = "사용자 이메일", example = "test123@gmail.com")
        String email,

        @Schema(description = "사용자 레벨", example = "METEOR")
        Level level,

        @Schema(description = "사용자가 사용 가능한 별조각", example = "180")
        Integer availableStar,

        @Schema(description = "사용자 경험치", example = "150")
        Integer expStar,

        @Schema(description = "다음 레벨 요구 경험치", example = "200")
        Integer nextStepExp,

        @Schema(description = "사용자 가입 유형", example = "EMAIL")
        ProviderType loginType,

        @Schema(description = "이메일 구독 여부", example = "false")
        Boolean emailSubscription
) {
    public static MyPageProfileResponse from(User user) {
        return new MyPageProfileResponse(
                user.getNickname(),
                user.getEmail(),
                user.getLevel(),
                user.getAvailableStar(),
                user.getExpStar(),
                user.getLevel().getNextLevelRequiredStars(),
                user.getProviderType(),
                user.getEmailSubscription()
        );
    }
}
