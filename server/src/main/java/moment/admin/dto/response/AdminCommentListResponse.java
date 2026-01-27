package moment.admin.dto.response;

import java.util.List;

public record AdminCommentListResponse(
    List<AdminCommentResponse> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {
}
