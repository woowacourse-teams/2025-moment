package moment.notification.infrastructure.expo;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import moment.config.TestTags;
import moment.fixture.UserFixture;
import moment.notification.domain.PushNotification;
import moment.notification.domain.PushNotificationCommand;
import moment.notification.domain.PushNotificationMessage;
import moment.notification.infrastructure.PushNotificationRepository;
import moment.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@Tag(TestTags.UNIT)
@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class ExpoPushNotificationSenderTest {

    @InjectMocks
    private ExpoPushNotificationSender expoPushNotificationSender;

    @Mock
    private PushNotificationRepository pushNotificationRepository;

    @Mock
    private ExpoPushApiClient expoPushApiClient;

    private User user;

    @BeforeEach
    void setUp() {
        user = UserFixture.createUser();
        ReflectionTestUtils.setField(user, "id", 1L);
    }

    @Test
    void 디바이스_토큰이_있으면_Expo_API로_발송한다() {
        // given
        PushNotificationCommand command = new PushNotificationCommand(user,
                PushNotificationMessage.REPLY_TO_MOMENT);
        PushNotification pushNotification = new PushNotification(user, "ExponentPushToken[xxx]");

        when(pushNotificationRepository.findByUserId(user.getId()))
                .thenReturn(List.of(pushNotification));
        when(expoPushApiClient.send(anyList()))
                .thenReturn(List.of(new ExpoPushTicketResponse("ticket-1", "ok", null, null)));

        // when
        expoPushNotificationSender.send(command);

        // then
        verify(expoPushApiClient).send(anyList());
    }

    @Test
    void 디바이스_토큰이_없으면_발송하지_않는다() {
        // given
        PushNotificationCommand command = new PushNotificationCommand(user,
                PushNotificationMessage.REPLY_TO_MOMENT);

        when(pushNotificationRepository.findByUserId(user.getId()))
                .thenReturn(List.of());

        // when
        expoPushNotificationSender.send(command);

        // then
        verify(expoPushApiClient, never()).send(anyList());
    }

    @Test
    void 여러_디바이스에_배치_발송한다() {
        // given
        PushNotificationCommand command = new PushNotificationCommand(user,
                PushNotificationMessage.REPLY_TO_MOMENT);
        PushNotification pn1 = new PushNotification(user, "ExponentPushToken[aaa]");
        PushNotification pn2 = new PushNotification(user, "ExponentPushToken[bbb]");

        when(pushNotificationRepository.findByUserId(user.getId()))
                .thenReturn(List.of(pn1, pn2));
        when(expoPushApiClient.send(anyList()))
                .thenReturn(List.of(
                        new ExpoPushTicketResponse("ticket-1", "ok", null, null),
                        new ExpoPushTicketResponse("ticket-2", "ok", null, null)));

        // when
        expoPushNotificationSender.send(command);

        // then
        verify(expoPushApiClient).send(anyList());
    }

    @Test
    void 발송_실패_시에도_예외를_전파하지_않는다() {
        // given
        PushNotificationCommand command = new PushNotificationCommand(user,
                PushNotificationMessage.REPLY_TO_MOMENT);
        PushNotification pushNotification = new PushNotification(user, "ExponentPushToken[xxx]");

        when(pushNotificationRepository.findByUserId(user.getId()))
                .thenReturn(List.of(pushNotification));
        when(expoPushApiClient.send(anyList()))
                .thenThrow(new ExpoPushApiException("API error", new RuntimeException()));

        // when - should not throw
        expoPushNotificationSender.send(command);

        // then
        verify(expoPushApiClient).send(anyList());
    }
}
