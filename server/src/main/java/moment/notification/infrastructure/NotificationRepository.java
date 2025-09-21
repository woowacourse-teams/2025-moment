package moment.notification.infrastructure;

import java.util.List;
import moment.global.domain.TargetType;
import moment.notification.domain.Notification;
import moment.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByUserAndIsRead(User user, Boolean isRead);

    List<Notification> findAllByUserAndIsReadAndTargetType(User user, Boolean isRead, TargetType targetType);
}
