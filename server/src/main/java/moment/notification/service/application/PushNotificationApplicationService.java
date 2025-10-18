package moment.notification.service.application;

import lombok.RequiredArgsConstructor;
import moment.notification.domain.PushNotificationCommand;
import moment.notification.domain.PushNotificationMessage;
import moment.notification.domain.PushNotificationSender;
import moment.notification.dto.request.DeviceEndpointRequest;
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
    public void registerDeviceEndpoint(long userId, DeviceEndpointRequest request) {
        User user = userService.getUserBy(userId);
        pushNotificationService.save(user, request.deviceEndpoint());
    }

    public void sendToDeviceEndpoint(long userId, PushNotificationMessage message) {
        User user = userService.getUserBy(userId);
        pushNotificationSender.send(new PushNotificationCommand(user, message));
    }

    @Transactional
    public void deleteDeviceEndpoint(long userId, DeviceEndpointRequest request) {
        User user = userService.getUserBy(userId);
        pushNotificationService.deleteBy(user, request.deviceEndpoint());
    }
}
