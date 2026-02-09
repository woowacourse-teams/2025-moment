package moment.block.service.block;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import moment.block.domain.UserBlock;
import moment.block.infrastructure.UserBlockRepository;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserBlockService {

    private final UserBlockRepository userBlockRepository;

    @Transactional
    public UserBlock block(User blocker, User blockedUser) {
        validateNotSelf(blocker, blockedUser);
        validateNotAlreadyBlocked(blocker, blockedUser);

        Optional<UserBlock> deletedBlock = userBlockRepository
                .findByBlockerAndBlockedUserIncludeDeleted(blocker.getId(), blockedUser.getId());

        if (deletedBlock.isPresent()) {
            UserBlock existing = deletedBlock.get();
            existing.restore();
            return existing;
        }

        return userBlockRepository.save(new UserBlock(blocker, blockedUser));
    }

    @Transactional
    public void unblock(User blocker, User blockedUser) {
        UserBlock userBlock = userBlockRepository.findByBlockerAndBlockedUser(blocker, blockedUser)
                .orElseThrow(() -> new MomentException(ErrorCode.BLOCK_NOT_FOUND));
        userBlockRepository.delete(userBlock);
    }

    public List<Long> getBlockedUserIds(Long userId) {
        List<Long> blockedUserIds = userBlockRepository.findBlockedUserIds(userId);
        if (blockedUserIds == null || blockedUserIds.isEmpty()) {
            return Collections.emptyList();
        }
        return blockedUserIds;
    }

    public boolean isBlocked(Long userId1, Long userId2) {
        return userBlockRepository.existsBidirectionalBlock(userId1, userId2);
    }

    public List<UserBlock> getBlockedUsers(User blocker) {
        return userBlockRepository.findAllByBlocker(blocker);
    }

    private void validateNotSelf(User blocker, User blockedUser) {
        if (blocker.getId().equals(blockedUser.getId())) {
            throw new MomentException(ErrorCode.BLOCK_SELF);
        }
    }

    private void validateNotAlreadyBlocked(User blocker, User blockedUser) {
        if (userBlockRepository.existsByBlockerAndBlockedUser(blocker, blockedUser)) {
            throw new MomentException(ErrorCode.BLOCK_ALREADY_EXISTS);
        }
    }
}
