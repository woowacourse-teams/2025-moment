package moment.auth.presentation;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import moment.auth.application.AuthService;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.user.dto.request.Authentication;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@RequiredArgsConstructor
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final AuthService authService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(Authentication.class)
                && parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        String token = getToken(request);
        return authService.getAuthenticationByToken(token);
    }

    private String getToken(HttpServletRequest request) throws MomentException {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new MomentException(ErrorCode.TOKEN_NOT_FOUND);
        }

        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName()
                        .equals("token"))
                .findFirst()
                .orElseThrow(() -> new MomentException(ErrorCode.TOKEN_NOT_FOUND))
                .getValue();
    }
}
