package moment.notification.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import moment.comment.infrastructure.CommentRepository;
import moment.moment.domain.Moment;
import moment.moment.domain.WriteType;
import moment.moment.infrastructure.MomentRepository;
import moment.user.application.UserQueryService;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceTest {

    @InjectMocks
    private EmailNotificationService emailNotificationService;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private UserQueryService userQueryService;

    @Mock
    private MomentRepository momentRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private MimeMessage mimeMessage;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("test@example.com", "password123", "tester", ProviderType.EMAIL);
    }

    @Test
    @DisplayName("스케줄러가 실행되면 모든 사용자에게 리마인더 이메일을 전송한다.")
    void schedule_sendsEmailsToAllUsers() {
        // given
        Moment moment = mock(Moment.class);
        when(userQueryService.findAll()).thenReturn(List.of(user));

        when(momentRepository.countByMomenterAndWriteTypeAndCreatedAtBetween(
                any(User.class),
                any(WriteType.class),
                any(LocalDateTime.class),
                any(LocalDateTime.class))
        ).thenReturn(1);

        when(momentRepository.findByMomenterAndCreatedAtAfter(
                any(User.class),
                any(LocalDateTime.class))
        ).thenReturn(List.of(moment));

        when(commentRepository.countByMomentAndCreatedAtBetween(
                any(Moment.class),
                any(LocalDateTime.class),
                any(LocalDateTime.class))
        ).thenReturn(2L);

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // when
        emailNotificationService.schedule();

        // then
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    @DisplayName("사용자가 없으면 이메일을 전송하지 않는다.")
    void schedule_doesNotSendEmails_whenNoUsers() {
        // given
        when(userQueryService.findAll()).thenReturn(Collections.emptyList());

        // when
        emailNotificationService.schedule();

        // then
        verify(mailSender, never()).send(any(MimeMessage.class));
    }
}
