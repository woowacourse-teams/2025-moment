package moment.notification.infrastructure;

import java.util.List;
import moment.global.domain.TargetType;
import moment.notification.domain.Notification;
import moment.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByUserIdAndIsRead(Long userId, boolean isRead);

    @Query("""
            SELECT DISTINCT n.targetId
            FROM notifications n
            WHERE n.user.id = :userId AND n.isRead = :isRead AND n.targetType = :targetType
            """)
    List<Long> findAllByUserIdAndIsReadAndTargetType(@Param("userId") Long userId,
                                                     @Param("isRead") boolean isRead,
                                                     @Param("targetType") TargetType targetType);

    @Query("""
            SELECT n
            FROM notifications n
            WHERE n.targetId IN :targetIds AND n.isRead = :isRead AND n.targetType = :targetType
            """)
    List<Notification> findNotificationsBy(@Param("targetIds") List<Long> targetIds,
                                           @Param("isRead") boolean isRead,
                                           @Param("targetType")TargetType targetType);
}
