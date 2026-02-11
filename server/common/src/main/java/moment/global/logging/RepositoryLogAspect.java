package moment.global.logging;

import static net.logstash.logback.argument.StructuredArguments.kv;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Aspect
@Component
@Slf4j
public class RepositoryLogAspect {

    private static final long SLOW_QUERY_THRESHOLD_MS = 500;

    @Around("execution(public * org.springframework.data.repository.Repository+.*(..))")
    public Object logRepository(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Object result = joinPoint.proceed();

        stopWatch.stop();
        long totalTimeMillis = stopWatch.getTotalTimeMillis();
        String methodName = joinPoint.getSignature().getName();
        String repositoryName = joinPoint.getSignature().getDeclaringTypeName();

        if (totalTimeMillis > SLOW_QUERY_THRESHOLD_MS) {
            log.warn("Slow Repository",
                    kv("repository", repositoryName),
                    kv("method", methodName),
                    kv("duration_ms", totalTimeMillis)
            );
            return result;
        }

        log.debug("Repository Executed",
                kv("repository", repositoryName),
                kv("method", methodName),
                kv("duration_ms", totalTimeMillis)
        );

        return result;
    }
}
