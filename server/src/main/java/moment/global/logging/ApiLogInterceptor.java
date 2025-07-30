package moment.global.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Slf4j
@Component
public class ApiLogInterceptor implements HandlerInterceptor {

    private static final String TRACE_ID_KEY = "traceId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put(TRACE_ID_KEY, traceId);
        log.info("ip: [{}], method: [{}], uri: [{}]", request.getRemoteAddr(), request.getMethod(), requestURI);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        log.info("status: [{}]", response.getStatus());
        MDC.remove(TRACE_ID_KEY);
    }
}
