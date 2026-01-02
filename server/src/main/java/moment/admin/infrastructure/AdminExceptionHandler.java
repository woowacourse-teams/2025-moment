package moment.admin.infrastructure;

import static net.logstash.logback.argument.StructuredArguments.kv;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(basePackages = "moment.admin.presentation")
@Slf4j
public class AdminExceptionHandler {

    @ExceptionHandler(MomentException.class)
    public String handleMomentException(MomentException e, Model model, HttpServletRequest request) {
        ErrorCode errorCode = e.getErrorCode();

        // 로깅
        log.warn("Admin MomentException",
                kv("path", request.getRequestURI()),
                kv("errorCode", errorCode.name()),
                kv("message", errorCode.getMessage())
        );

        // 인증 관련 에러는 로그인 페이지로 리다이렉트
        if (errorCode == ErrorCode.ADMIN_UNAUTHORIZED ||
                errorCode == ErrorCode.ADMIN_NOT_FOUND ||
                errorCode == ErrorCode.ADMIN_LOGIN_FAILED) {
            return "redirect:/admin/login?error=" + errorCode.getMessage();
        }

        // 그 외 비즈니스 에러는 에러 페이지
        model.addAttribute("errorCode", errorCode.getCode());
        model.addAttribute("errorMessage", errorCode.getMessage());
        model.addAttribute("statusCode", e.getStatus().value());
        return "admin/error/error";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException e, Model model, HttpServletRequest request) {

        ErrorCode errorCode = ErrorCode.REQUEST_INVALID;

        log.error("Admin IllegalArgumentException",
                kv("path", request.getRequestURI()),
                kv("message", e.getMessage()),
                e
        );

        model.addAttribute("errorMessage", errorCode.getMessage());
        model.addAttribute("statusCode", errorCode.getStatus().value());
        return "admin/error/error";
    }

    @ExceptionHandler(NullPointerException.class)
    public String handleNullPointerException(NullPointerException e, Model model, HttpServletRequest request) {

        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

        log.error("Admin NullPointerException",
                kv("path", request.getRequestURI()),
                kv("message", e.getMessage()),
                kv("stackTrace", e.getStackTrace()[0].toString()),
                e
        );

        model.addAttribute("errorMessage", errorCode.getMessage());
        model.addAttribute("statusCode", errorCode.getStatus().value());
        return "admin/error/500";
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model, HttpServletRequest request) {

        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

        log.error("Admin Unhandled Exception",
                kv("path", request.getRequestURI()),
                kv("exception", e.getClass().getSimpleName()),
                kv("message", e.getMessage()),
                e
        );

        model.addAttribute("errorMessage", errorCode.getMessage());
        model.addAttribute("statusCode", errorCode.getStatus().value());
        return "admin/error/500";
    }
}
