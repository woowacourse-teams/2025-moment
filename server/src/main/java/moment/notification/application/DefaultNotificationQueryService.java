package moment.notification.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import moment.global.domain.TargetType;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.notification.domain.Notification;
import moment.notification.infrastructure.NotificationRepository;
import moment.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DefaultNotificationQueryService implements NotificationQueryService {

    private final NotificationRepository notificationRepository;

    @Override
    public Notification getNotificationById(Long id) {

        return notificationRepository.findById(id)
                .orElseThrow(() -> new MomentException(ErrorCode.NOTIFICATION_NOT_FOUND));
    }

    public List<Notification> getUnreadContentsNotifications(User user, TargetType targetType) {

        return notificationRepository.findAllByUserAndIsReadAndTargetType(user, false, targetType);
    }
}
