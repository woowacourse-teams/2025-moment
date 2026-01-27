package moment.admin.dto.response;

import java.util.List;

public record AdminGroupLogListResponse(
    List<AdminGroupLogResponse> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {
}
