package moment.global.logging;

import static java.util.stream.Collectors.joining;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Slf4j
@Profile({"test", "dev"})
public class ControllerLogAspect {

    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
    public void allController() {
    }

    @Before("allController()")
    public void logControllerRequest(JoinPoint joinPoint) {
        getRequest(joinPoint);
    }

    @AfterReturning(pointcut = "allController()", returning = "responseBody")
    public void logControllerResponse(Object responseBody) {
        getResponse(responseBody);
    }

    private void getRequest(JoinPoint joinPoint) {
        HttpServletRequest request = getHttpServletRequest();
        String queryParameters = getQueryParameters(request);
        String body = getBody(joinPoint);
        boolean hasToken = hasToken(request);

        log.debug("request: [{}], parameters: [{}], hasToken: [{}]", body, queryParameters, hasToken);
    }

    private HttpServletRequest getHttpServletRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return requestAttributes.getRequest();
    }

    private String getQueryParameters(HttpServletRequest request) {
        String queryParameters = request.getParameterMap()
                .entrySet()
                .stream()
                .map(entry -> "%s = %s".formatted(entry.getKey(), entry.getValue()[0]))
                .collect(joining(", "));

        if (queryParameters.isEmpty()) {
            return null;
        }
        return queryParameters;
    }

    private String getBody(JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Parameter[] parameters = methodSignature.getMethod().getParameters();
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            Object arg = args[i];
            if (param.isAnnotationPresent(RequestBody.class)) {
                return arg.toString();
            }
        }
        return null;
    }

    private boolean hasToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return false;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("token")) {
                return true;
            }
        }
        return false;
    }

    private void getResponse(Object responseBody) {
        log.info("response: [{}]", responseBody);
    }
}
