package moment.notification.infrastructure;

import java.util.Optional;
import moment.notification.domain.PushNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PushNotificationRepository extends JpaRepository<PushNotification, Long> {

    Optional<PushNotification> findByUserId(Long pushNotificationId);
}
