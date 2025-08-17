package moment.user.application;

import lombok.RequiredArgsConstructor;
import moment.user.domain.User;
import moment.user.dto.response.MyPageProfileResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private final UserQueryService userQueryService;

    public MyPageProfileResponse getProfile(Long userId) {
        User user = userQueryService.getUserById(userId);
        return MyPageProfileResponse.from(user);
    }
}
