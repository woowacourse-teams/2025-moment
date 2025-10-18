package moment.comment.dto.tobe;

import moment.comment.domain.Echo;

public record EchoDetail(
        Long id,
        String echoType,
        Long userId,
        String userName
) {

    public static EchoDetail from(Echo echo) {
        return new EchoDetail(
                echo.getId(),
                echo.getEchoType(),
                echo.getUser().getId(),
                echo.getUser().getNickname()
        );
    }
}
