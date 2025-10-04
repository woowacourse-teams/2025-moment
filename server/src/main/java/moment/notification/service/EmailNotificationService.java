package moment.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moment.comment.infrastructure.CommentRepository;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.moment.domain.Moment;
import moment.moment.domain.WriteType;
import moment.moment.infrastructure.MomentRepository;
import moment.user.application.UserQueryService;
import moment.user.domain.User;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationService {

    private final JavaMailSender mailSender;
    private final UserQueryService userQueryService;
    private final MomentRepository momentRepository;
    private final CommentRepository commentRepository;

    /*
    이메일 수신 여부 변경 기능 추가 시 주석 해제
    @Scheduled(cron = "0 0 21 * * ?", zone = "Asia/Seoul")
    */
    public void schedule() {
        List<User> users = userQueryService.findAll();
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().plusDays(1).atStartOfDay();

        for (User user : users) {
            int todayMomentCount = getTodayMomentCount(user, startOfDay, endOfDay);
            List<Moment> moments = getMoments(user);
            long totalTodayCommentsCount = getTotalTodayCommentsCount(moments, startOfDay, endOfDay);

            send(user, todayMomentCount, totalTodayCommentsCount);
        }
    }

    private int getTodayMomentCount(User user, LocalDateTime startOfDay, LocalDateTime endOfDay) {
        return momentRepository.countByMomenterAndWriteTypeAndCreatedAtBetween(
                user,
                WriteType.BASIC,
                startOfDay,
                endOfDay);
    }

    private List<Moment> getMoments(User user) {
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        return momentRepository.findByMomenterAndCreatedAtAfter(user, threeDaysAgo);
    }

    private long getTotalTodayCommentsCount(List<Moment> moments, LocalDateTime startOfDay, LocalDateTime endOfDay) {
        return moments.stream()
                .mapToLong(moment ->
                        commentRepository.countByMomentAndCreatedAtBetween(moment, startOfDay, endOfDay))
                .sum();
    }

    private void send(User user, long momentCount, long commentCount) {
        try {
            String email = user.getEmail();
            String subject = String.format("[Moment] %s님, 오늘 하루 어떤 순간이 특별했나요? 모멘트로 남겨보세요", user.getNickname());

            MimeMessage mimeMessage = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setTo(email);
            helper.setSubject(subject);

            String content = parseContent(user, momentCount, commentCount);
            helper.setText(content, true);

            mailSender.send(helper.getMimeMessage());
        } catch (MessagingException | MailException | IOException e) {
            log.error("리마인드 이메일 전송 실패: ", e);
            throw new MomentException(ErrorCode.EMAIL_SEND_FAILURE);
        }
    }

    private String loadEmailTemplate() throws IOException {
        try (
                InputStream inputStream = getClass().getClassLoader().getResourceAsStream("email/reminder.html")
        ) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line + "\r\n");
            }
            return stringBuilder.toString();
        }
    }

    private String parseContent(User user, long momentCount, long commentCount) throws IOException {
        return String.format(
                loadEmailTemplate(),
                user.getNickname(),
                getMomentCountContent(momentCount),
                getCommentCountContent(commentCount)
        );
    }

    private String getMomentCountContent(long momentCount) {
        if (momentCount == 0) {
            return "아직 오늘의 모멘트가 비어있어요.\n하루의 순간을 기록해보세요!";
        }
        return "오늘 하루의 모멘트를 남기셨네요.\n이제 달린 코멘트를 확인해보세요.";
    }

    private String getCommentCountContent(long commentCount) {
        if (commentCount == 0) {
            return "오늘 새로 달린 코멘트가 아직 없네요.\n조금만 기다리시면 곧 새로운 코멘트가 도착할 거예요!";
        }
        return String.format("오늘 새로운 코멘트 %s개 도착했어요.\n지금 바로 확인해보세요!", commentCount);
    }
}
