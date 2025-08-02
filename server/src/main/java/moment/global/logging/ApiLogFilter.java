package moment.global.logging;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ApiLogFilter implements Filter {

    private static final String TRACE_ID_KEY = "traceId";
    public static final String DELIMITER = "======================================================================";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String traceId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put(TRACE_ID_KEY, traceId);

        startApiLogging(request);

        long start = System.currentTimeMillis();

        chain.doFilter(servletRequest, servletResponse);

        long end = System.currentTimeMillis();
        long duration = end - start;

        endApiLogging(request, response, duration);

        MDC.remove(TRACE_ID_KEY);
    }

    private void startApiLogging(HttpServletRequest request) {
        log.info(DELIMITER);

        log.info("ip: [{}], method: [{}], uri: [{}]",
                request.getRemoteAddr(),
                request.getMethod(),
                request.getRequestURI());
    }

    private void endApiLogging(HttpServletRequest request, HttpServletResponse response, long duration) {
        log.info("ip: [{}], method: [{}], uri: [{}], status: [{}], duration:[{}ms], tag: [API_RESPONSE_TIME]",
                request.getRemoteAddr(),
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                duration);

        log.info(DELIMITER);
    }
}
