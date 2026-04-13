package moment.global.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 기본 비동기 스레드풀 숫자: 1코어인 것을 생각해 2개로 설정
        executor.setCorePoolSize(2);

        // 최대 대기할 수 있는 비동기 스레드 숫자
        executor.setQueueCapacity(50);

        // 최대로 대기하는 경우 비동기 스레드 상한선은 4개까지
        executor.setMaxPoolSize(4);

        executor.setThreadNamePrefix("Noti-Async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
