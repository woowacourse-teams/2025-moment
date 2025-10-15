package moment.global.logging;

import static net.logstash.logback.argument.StructuredArguments.kv;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Aspect
@Component
@Slf4j
public class ServiceLogAspect {

    private static final long SLOW_SERVICE_THRESHOLD_MS = 500;

    @Pointcut("@within(org.springframework.stereotype.Service) && !within(*..*QueryService) && !@annotation(moment.global.logging.NoLogging)")
    public void serviceWithoutQueryAndNoLogging() {
    }

    @Around("serviceWithoutQueryAndNoLogging()")
    public Object logService(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        try {
            stopWatch.start();
            return joinPoint.proceed();
        } finally {
            stopWatch.stop();
            long totalTimeMillis = stopWatch.getTotalTimeMillis();

            if (totalTimeMillis > SLOW_SERVICE_THRESHOLD_MS) {
                log.warn("Slow Service",
                        kv("method", joinPoint.getSignature().toShortString()),
                        kv("duration_ms",totalTimeMillis)
                );
            }
            if (log.isDebugEnabled()) {
                log.debug("Service duration",
                        kv("method", joinPoint.getSignature().toShortString()),
                        kv("duration_ms", totalTimeMillis)
                );
            }
        }
    }
}
