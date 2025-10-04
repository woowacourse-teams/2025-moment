package moment.notification.infrastructure;

import java.util.List;
import moment.global.domain.TargetType;
import moment.notification.domain.Notification;
import moment.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByUserAndIsRead(User user, Boolean isRead);

    List<Notification> findAllByUserAndIsReadAndTargetType(User user, Boolean isRead, TargetType targetType);

    @Query("""
            SELECT DISTINCT n.targetId
            FROM notifications n
            WHERE n.user.id = :userId AND n.isRead = :isRead AND n.targetType = :targetType
            """)
    List<Long> findAllByUserIdAndIsReadAndTargetType(@Param("userId") Long userId,
                                                     @Param("isRead") Boolean isRead,
                                                     @Param("targetType") TargetType targetType);
}
