package moment.admin.dto.response;

import java.util.List;

public record AdminMomentListResponse(
    List<AdminMomentResponse> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {
}
