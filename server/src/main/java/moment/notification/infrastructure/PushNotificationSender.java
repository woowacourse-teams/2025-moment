package moment.notification.infrastructure;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import moment.notification.application.PushNotificationService;
import moment.notification.dto.PushNotificationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PushNotificationSender implements PushNotificationService {

    private final PushNotificationRepository pushNotificationRepository;
    private final FirebaseMessaging firebaseMessaging;

    public PushNotificationSender(
        PushNotificationRepository pushNotificationRepository,
        @Autowired(required = false) FirebaseMessaging firebaseMessaging
    ) {
        this.pushNotificationRepository = pushNotificationRepository;
        this.firebaseMessaging = firebaseMessaging;
    }

    @Override
    public void send(PushNotificationRequest request) {
        if (firebaseMessaging == null) {
            log.warn("FirebaseMessaging is not configured. Skipping push notification sending.");
            return;
        }

        pushNotificationRepository.findByUserId(request.userId())
            .ifPresentOrElse(
                pushNotification -> {
                    Message message = buildMessage(request, pushNotification.getDeviceEndpoint());
                    ApiFuture<String> future = firebaseMessaging.sendAsync(message);
                    addSendCallback(future, request.userId());
                },
                () -> log.warn("Push notification token not found for user: {}", request.userId())
            );
    }

    private Message buildMessage(PushNotificationRequest request, String deviceEndpoint) {
        Notification notification = Notification.builder()
            .setTitle(request.title())
            .setBody(request.body())
            .build();

        return Message.builder()
            .setToken(deviceEndpoint)
            .setNotification(notification)
            .build();
    }

    private void addSendCallback(ApiFuture<String> future, Long userId) {
        ApiFutures.addCallback(future, new ApiFutureCallback<>() {
            @Override
            public void onSuccess(String result) {
            }

            @Override
            public void onFailure(Throwable throwable) {
                log.error("Failed to send push notification for user {}", userId, throwable);
            }
        }, MoreExecutors.directExecutor());
    }
}
