package moment.report.infrastructure;

import java.util.List;
import moment.global.domain.TargetType;
import moment.report.domain.Report;
import moment.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReportRepository extends JpaRepository<Report, Long> {
    long countByTargetTypeAndTargetId(TargetType targetType, Long targetId);

    @Query("SELECT r.targetId FROM reports r WHERE r.user.id = :userId AND r.targetType = :targetType")
    List<Long> findAllTargetIdByUserIdAndTargetType(Long userId, TargetType targetType);
}
