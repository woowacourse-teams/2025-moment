package moment.admin.dto.response;

import java.util.List;
import org.springframework.data.domain.Page;

public record AdminGroupMemberListResponse(
    List<AdminGroupMemberResponse> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {
    public static AdminGroupMemberListResponse from(Page<AdminGroupMemberResponse> pageResult) {
        return new AdminGroupMemberListResponse(
            pageResult.getContent(),
            pageResult.getNumber(),
            pageResult.getSize(),
            pageResult.getTotalElements(),
            pageResult.getTotalPages()
        );
    }
}
