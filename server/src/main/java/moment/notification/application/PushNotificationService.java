package moment.notification.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moment.notification.domain.PushNotification;
import moment.notification.dto.request.DeviceEndPointRegisterRequest;
import moment.notification.infrastructure.PushNotificationRepository;
import moment.user.application.UserQueryService;
import moment.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PushNotificationService {

    private final UserQueryService userQueryService;
    private final PushNotificationRepository pushNotificationRepository;

    @Transactional
    public void registerDeviceEndpoint(DeviceEndPointRegisterRequest request) {
        User user = userQueryService.getUserById(request.userId());
        PushNotification pushNotification = new PushNotification(user, request.deviceEndpoint());
        pushNotificationRepository.save(pushNotification);
    }
}
