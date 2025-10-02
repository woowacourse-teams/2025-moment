package moment.notification.infrastructure;

import java.util.List;
import moment.notification.domain.PushNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface PushNotificationRepository extends JpaRepository<PushNotification, Long> {

    List<PushNotification> findByUserId(Long userId);

    @Transactional
    void deleteByDeviceEndpoint(String deviceEndpoint);
}
