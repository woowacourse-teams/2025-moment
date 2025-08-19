package moment.global.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@Profile({"test", "dev"})
public class ServiceLogAspect {

    @Pointcut("@within(org.springframework.stereotype.Service) && !within(*..*QueryService) && !@annotation(moment.global.logging.NoLogging)")
    public void serviceWithoutQueryAndNoLogging() {
    }

    @Around("serviceWithoutQueryAndNoLogging()")
    public Object logService(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("start service: [{}]", joinPoint.getSignature().getName());

        try {
            return joinPoint.proceed();
        } finally {
            log.info("end service: [{}]", joinPoint.getSignature().getName());
        }
    }
}
