package moment.notification.infrastructure;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import moment.notification.domain.PushNotificationSender;
import moment.notification.domain.PushNotificationCommand;
import moment.notification.domain.PushNotificationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FcmPushNotificationSender implements PushNotificationSender {

    private final PushNotificationRepository pushNotificationRepository;
    private final FirebaseMessaging firebaseMessaging;

    public FcmPushNotificationSender(
            PushNotificationRepository pushNotificationRepository,
            @Autowired(required = false) FirebaseMessaging firebaseMessaging
    ) {
        this.pushNotificationRepository = pushNotificationRepository;
        this.firebaseMessaging = firebaseMessaging;
    }

    @Override
    public void send(PushNotificationCommand pushNotificationCommand) {
        if (firebaseMessaging == null) {
            log.warn("FirebaseMessaging is not configured. Skipping push notification sending.");
            return;
        }
        Long userId = pushNotificationCommand.user().getId();

        pushNotificationRepository.findByUserId(userId)
                .ifPresentOrElse(
                        pushNotification -> {
                            Message message = buildMessage(
                                    pushNotificationCommand.message(),
                                    pushNotification.getDeviceEndpoint());

                            ApiFuture<String> future = firebaseMessaging.sendAsync(message);
                            addSendCallback(future, userId);
                            log.info("Push notification sent successfully.");
                        },
                        () -> log.warn("Push notification token not found for user: {}", userId)
                );
    }

    private Message buildMessage(PushNotificationMessage pushNotificationMessage, String deviceEndpoint) {
        Notification notification = Notification.builder()
                .setTitle(pushNotificationMessage.getTitle())
                .setBody(pushNotificationMessage.getBody())
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
