package moment.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(description = "마이페이지 나의 보상 기록 페이지 조회 응답")
public record MyRewardHistoryPageResponse(
        @Schema(description = "마이페이지 나의 보상 내역 목록")
        List<MyRewardHistoryResponse> items,

        @Schema(description = "현재 페이지 넘버", example = "1")
        int currentPageNum,

        @Schema(description = "페이지 사이즈", example = "10")
        int pageSize,

        @Schema(description = "총 페이지 개수", example = "112")
        int totalPages

) {
    public static MyRewardHistoryPageResponse from(Page<MyRewardHistoryResponse> page) {
        List<MyRewardHistoryResponse> items = page.getContent();
        int currentPageNum = page.getNumber();
        int pageSize = page.getSize();
        int totalPages = page.getTotalPages();
        return new MyRewardHistoryPageResponse(items, currentPageNum, pageSize, totalPages);
    }
}
