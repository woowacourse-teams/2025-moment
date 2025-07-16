package moment.global.dto.response;

import org.springframework.http.HttpStatus;

public record SuccessResponse<T>(int status, T data) {

    public static <T> SuccessResponse<T> of(HttpStatus httpStatus, T data) {
        return new SuccessResponse<>(httpStatus.value(), data);
    }
}
