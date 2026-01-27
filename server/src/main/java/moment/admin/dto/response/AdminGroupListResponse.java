package moment.admin.dto.response;

import java.util.List;
import org.springframework.data.domain.Page;

public record AdminGroupListResponse(
    List<AdminGroupSummary> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {
    public static AdminGroupListResponse from(Page<AdminGroupSummary> pageResult) {
        return new AdminGroupListResponse(
            pageResult.getContent(),
            pageResult.getNumber(),
            pageResult.getSize(),
            pageResult.getTotalElements(),
            pageResult.getTotalPages()
        );
    }
}
