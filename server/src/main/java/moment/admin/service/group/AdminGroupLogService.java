package moment.admin.service.group;

import java.util.List;
import lombok.RequiredArgsConstructor;
import moment.admin.domain.AdminGroupLog;
import moment.admin.domain.AdminGroupLogType;
import moment.admin.dto.response.AdminGroupLogListResponse;
import moment.admin.dto.response.AdminGroupLogResponse;
import moment.admin.infrastructure.AdminGroupLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminGroupLogService {

    private final AdminGroupLogRepository adminGroupLogRepository;

    public AdminGroupLogListResponse getGroupLogs(
        Long groupId,
        AdminGroupLogType type,
        int page,
        int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<AdminGroupLog> logs;

        if (groupId != null && type != null) {
            logs = adminGroupLogRepository.findByGroupIdAndType(groupId, type, pageable);
        } else if (groupId != null) {
            logs = adminGroupLogRepository.findByGroupId(groupId, pageable);
        } else if (type != null) {
            logs = adminGroupLogRepository.findByType(type, pageable);
        } else {
            logs = adminGroupLogRepository.findAll(pageable);
        }

        List<AdminGroupLogResponse> content = logs.getContent().stream()
            .map(AdminGroupLogResponse::from)
            .toList();

        return new AdminGroupLogListResponse(
            content,
            logs.getNumber(),
            logs.getSize(),
            logs.getTotalElements(),
            logs.getTotalPages()
        );
    }
}
