package moment.notification.infrastructure;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.core.ApiFuture;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import java.util.List;
import moment.config.TestTags;
import moment.fixture.UserFixture;
import moment.notification.domain.PushNotification;
import moment.notification.domain.PushNotificationCommand;
import moment.notification.domain.PushNotificationMessage;
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
class FcmPushNotificationSenderTest {

    @InjectMocks
    private FcmPushNotificationSender fcmPushNotificationSender;

    @Mock
    private PushNotificationRepository pushNotificationRepository;

    @Mock
    private FirebaseMessaging firebaseMessaging;

    @Mock
    private ApiFuture<String> mockApiFuture;

    private User user;

    @BeforeEach
    void setUp() {
        user = UserFixture.createUser();
        ReflectionTestUtils.setField(user, "id", 1L);
    }

    @Test
    void 푸시_알림을_성공적으로_전송한다() {
        // given
        PushNotificationCommand pushNotificationCommand = new PushNotificationCommand(user,
                PushNotificationMessage.REPLY_TO_MOMENT);
        PushNotification pushNotification = new PushNotification(user, "device-token");

        when(pushNotificationRepository.findByUserId(user.getId())).thenReturn(List.of(pushNotification));
        when(firebaseMessaging.sendAsync(any(Message.class))).thenReturn(mockApiFuture);

        // when
        fcmPushNotificationSender.send(pushNotificationCommand);

        // then
        verify(firebaseMessaging).sendAsync(any(Message.class));
    }

    @Test
    void FirebaseMessaging_빈이_설정되지_않았으면_알림을_보내지_않는다() {
        // given
        FcmPushNotificationSender senderWithNoFcm = new FcmPushNotificationSender(pushNotificationRepository, null);
        PushNotificationCommand pushNotificationCommand = new PushNotificationCommand(user,
                PushNotificationMessage.REPLY_TO_MOMENT);

        // when
        senderWithNoFcm.send(pushNotificationCommand);

        // then
        verify(pushNotificationRepository, never()).findByUserId(anyLong());
        verify(firebaseMessaging, never()).sendAsync(any(Message.class));
    }

    @Test
    void 사용자의_디바이스_토큰_정보가_저장되어_있지_않으면_알림을_보내지_않는다() {
        // given
        PushNotificationCommand pushNotificationCommand = new PushNotificationCommand(user,
                PushNotificationMessage.REPLY_TO_MOMENT);
        when(pushNotificationRepository.findByUserId(user.getId())).thenReturn(List.of());

        // when
        fcmPushNotificationSender.send(pushNotificationCommand);

        // then
        verify(pushNotificationRepository).findByUserId(user.getId());
        verify(firebaseMessaging, never()).sendAsync(any(Message.class));
    }
}
