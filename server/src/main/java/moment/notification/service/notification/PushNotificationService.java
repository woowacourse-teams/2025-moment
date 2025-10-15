package moment.notification.service.notification;

import lombok.RequiredArgsConstructor;
import moment.notification.domain.PushNotification;
import moment.notification.infrastructure.PushNotificationRepository;
import moment.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PushNotificationService {

    private final PushNotificationRepository pushNotificationRepository;

    @Transactional
    public void save(User user, String deviceEndpoint) {
        if(pushNotificationRepository.existsByUserAndDeviceEndpoint(user, deviceEndpoint)) {
            return;
        }
        pushNotificationRepository.save(new PushNotification(user, deviceEndpoint));
    }

    @Transactional
    public void deleteBy(User user) {
        pushNotificationRepository.deleteByUser(user);
    }
}
