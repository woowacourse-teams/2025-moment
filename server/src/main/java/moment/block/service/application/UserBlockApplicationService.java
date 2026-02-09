package moment.block.service.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import moment.block.domain.UserBlock;
import moment.block.dto.response.UserBlockListResponse;
import moment.block.dto.response.UserBlockResponse;
import moment.block.service.block.UserBlockService;
import moment.user.domain.User;
import moment.user.service.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserBlockApplicationService {

    private final UserService userService;
    private final UserBlockService userBlockService;

    @Transactional
    public UserBlockResponse blockUser(Long blockerId, Long blockedUserId) {
        User blocker = userService.getUserBy(blockerId);
        User blockedUser = userService.getUserBy(blockedUserId);
        UserBlock userBlock = userBlockService.block(blocker, blockedUser);
        return UserBlockResponse.from(userBlock);
    }

    @Transactional
    public void unblockUser(Long blockerId, Long blockedUserId) {
        User blocker = userService.getUserBy(blockerId);
        User blockedUser = userService.getUserBy(blockedUserId);
        userBlockService.unblock(blocker, blockedUser);
    }

    public List<Long> getBlockedUserIds(Long userId) {
        return userBlockService.getBlockedUserIds(userId);
    }

    public boolean isBlocked(Long userId1, Long userId2) {
        return userBlockService.isBlocked(userId1, userId2);
    }

    public List<UserBlockListResponse> getBlockedUsers(Long userId) {
        User blocker = userService.getUserBy(userId);
        List<UserBlock> blocks = userBlockService.getBlockedUsers(blocker);
        return blocks.stream()
                .map(UserBlockListResponse::from)
                .toList();
    }
}
