package moment.global.logging;

import static net.logstash.logback.argument.StructuredArguments.kv;

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
    private static final String DELIMITER = "=".repeat(70);

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String uri = request.getRequestURI();

        if (uri.startsWith("/swagger-ui/") || uri.startsWith("/v3/api-docs")) {
            chain.doFilter(servletRequest, servletResponse);
            return;
        }

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
        log.info("API Request Start",
                kv("ip",request.getRemoteAddr()),
                kv("method",request.getMethod()),
                kv("uri", request.getRequestURI())
        );
    }

    private void endApiLogging(HttpServletRequest request, HttpServletResponse response, long duration) {
        log.info("API Request End",
                kv("ip",request.getRemoteAddr()),
                kv("method",request.getMethod()),
                kv("uri",request.getRequestURI()),
                kv("status",response.getStatus()),
                kv("durattion_ms",duration),
                kv("tag", "API_RESPONSE_TIME")
        );
    }
}
