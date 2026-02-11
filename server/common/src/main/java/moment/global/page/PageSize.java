package moment.global.page;

import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import org.springframework.data.domain.PageRequest;

public record PageSize(int size) {
    public PageSize {
        if (size <= 0 || size > 100) {
            throw new MomentException(ErrorCode.MOMENTS_LIMIT_INVALID);
        }
    }

    public boolean hasNextPage(int targetSize) {
        return targetSize > size;
    }

    public PageRequest getPageRequest() {
        return PageRequest.of(0, size + 1);
    }
}
