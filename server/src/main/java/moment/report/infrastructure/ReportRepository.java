package moment.report.infrastructure;

import moment.global.domain.TargetType;
import moment.report.domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
    long countByTargetTypeAndTargetId(TargetType targetType, Long targetId);
}
