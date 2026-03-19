package moment.notification.infrastructure.expo;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moment.notification.domain.PushNotificationCommand;
import moment.notification.domain.PushNotificationMessage;
import moment.notification.domain.PushNotificationSender;
import moment.notification.infrastructure.PushNotificationRepository;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpoPushNotificationSender implements PushNotificationSender {

    private final PushNotificationRepository pushNotificationRepository;
    private final ExpoPushApiClient expoPushApiClient;

    @Override
    public void send(PushNotificationCommand command) {
        Long userId = command.user().getId();
        PushNotificationMessage message = command.message();
        String link = command.link();

        List<String> deviceTokens = pushNotificationRepository.findByUserId(userId)
                .stream()
                .map(pn -> pn.getDeviceEndpoint())
                .toList();

        if (deviceTokens.isEmpty()) {
            log.debug("No device tokens found for userId={}", userId);
            return;
        }

        Map<String, Object> data = (link != null)
                ? Map.of("link", link)
                : Map.of();

        List<ExpoPushMessage> messages = deviceTokens.stream()
                .map(token -> ExpoPushMessage.of(token, message, data))
                .toList();

        try {
            List<ExpoPushTicketResponse> tickets = expoPushApiClient.send(messages);
            log.info("Expo push sent to userId={}, tickets={}", userId, tickets.size());
        } catch (ExpoPushApiException e) {
            log.error("Failed to send Expo push to userId={}", userId, e);
        }
    }
}
