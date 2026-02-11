package moment.notification.infrastructure;

import java.util.List;
import moment.notification.domain.PushNotification;
import moment.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PushNotificationRepository extends JpaRepository<PushNotification, Long> {

    List<PushNotification> findByUserId(Long userId);

    void deleteByDeviceEndpoint(String deviceEndpoint);

    boolean existsByUserAndDeviceEndpoint(User user, String deviceEndpoint);

    void deleteByUserAndDeviceEndpoint(User user, String deviceEndpoint);

    void deleteAllByUserId(Long userId);
}
