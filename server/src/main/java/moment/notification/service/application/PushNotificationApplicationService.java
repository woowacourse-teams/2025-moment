package moment.notification.service.application;

import lombok.RequiredArgsConstructor;
import moment.notification.domain.PushNotificationCommand;
import moment.notification.domain.PushNotificationMessage;
import moment.notification.domain.PushNotificationSender;
import moment.notification.dto.request.DeviceEndPointRegisterRequest;
import moment.notification.service.notification.PushNotificationService;
import moment.user.domain.User;
import moment.user.service.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PushNotificationApplicationService {

    private final UserService userService;
    private final PushNotificationService pushNotificationService;
    private final PushNotificationSender pushNotificationSender;

    @Transactional
    public void registerDeviceEndpoint(DeviceEndPointRegisterRequest request) {
        User user = userService.getUserBy(request.userId());
        pushNotificationService.save(user, request.deviceEndpoint());
    }

    public void sendToDeviceEndpoint(long userId, PushNotificationMessage message) {
        User user = userService.getUserBy(userId);
        pushNotificationSender.send(new PushNotificationCommand(user, PushNotificationMessage.REPLY_TO_MOMENT));
    }
}
