package moment.global.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Aspect
@Component
@Slf4j
@Profile({"test", "dev"})
public class RepositoryLogAspect {

    @Around("execution(public * org.springframework.data.repository.Repository+.*(..))")
    public Object logRepository(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Object result = joinPoint.proceed();

        stopWatch.stop();
        long totalTimeMillis = stopWatch.getTotalTimeMillis();

        log.info("query: [{}] totalTime: [{}] ms", joinPoint.getSignature().getName(), totalTimeMillis);

        return result;
    }
}
