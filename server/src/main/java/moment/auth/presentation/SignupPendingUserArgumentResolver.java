package moment.auth.presentation;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import moment.auth.application.AuthService;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@RequiredArgsConstructor
public class SignupPendingUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final AuthService authService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(String.class)
                && parameter.hasParameterAnnotation(PendingAuthenticationPrincipal.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        String pendingToken = getPendingToken(request);
        return authService.getPendingAuthenticationByToken(pendingToken);
    }

    private String getPendingToken(HttpServletRequest request) throws MomentException {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new MomentException(ErrorCode.TOKEN_NOT_FOUND);
        }

        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName()
                        .equals("pendingToken"))
                .findFirst()
                .orElseThrow(() -> new MomentException(ErrorCode.TOKEN_NOT_FOUND))
                .getValue();
    }
}
