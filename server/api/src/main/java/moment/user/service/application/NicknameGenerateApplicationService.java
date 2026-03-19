package moment.user.service.application;

import lombok.RequiredArgsConstructor;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.user.domain.NicknameGenerator;
import moment.user.dto.response.MomentRandomNicknameResponse;
import moment.user.service.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NicknameGenerateApplicationService {
    private static final int LIMIT_RETRY_COUNT = 5;

    private final UserService userService;
    private final NicknameGenerator nicknameGenerator;

    public MomentRandomNicknameResponse createRandomNickname() {
        String randomNickname = generate();
        return new MomentRandomNicknameResponse(randomNickname);
    }

    public String generate() {
        int retry = 0;

        while (true) {
            if (retry > LIMIT_RETRY_COUNT) {
                throw new MomentException(ErrorCode.USER_NICKNAME_GENERATION_FAILED);
            }

            String nickname = nicknameGenerator.generateNickname();
            boolean exists = userService.existsBy(nickname);
            if (!exists) {
                return nickname;
            }

            retry++;
        }
    }
}
