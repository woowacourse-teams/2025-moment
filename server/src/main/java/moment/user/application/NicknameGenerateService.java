package moment.user.application;

import lombok.RequiredArgsConstructor;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.user.domain.NicknameGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NicknameGenerateService {

    private static final int LIMIT_RETRY_COUNT = 5;

    private final UserQueryService userQueryService;
    private final NicknameGenerator nicknameGenerator;

    public String createRandomNickname() {
        int retry = 0;

        while (true) {
            if (retry > LIMIT_RETRY_COUNT) {
                throw new MomentException(ErrorCode.USER_NICKNAME_GENERATION_FAILED);
            }

            String nickname = nicknameGenerator.generateNickname();
            boolean exists = userQueryService.existsByNickname(nickname);
            if (!exists) {
                return nickname;
            }

            retry++;
        }
    }
}
