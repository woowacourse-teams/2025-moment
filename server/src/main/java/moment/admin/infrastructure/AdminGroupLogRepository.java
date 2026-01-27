package moment.admin.infrastructure;

import java.util.List;
import moment.admin.domain.AdminGroupLog;
import moment.admin.domain.AdminGroupLogType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminGroupLogRepository extends JpaRepository<AdminGroupLog, Long> {

    Page<AdminGroupLog> findByGroupId(Long groupId, Pageable pageable);

    List<AdminGroupLog> findByGroupIdAndType(Long groupId, AdminGroupLogType type);
}
