package moment.question.service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moment.question.service.facade.QuestionFacadeService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuestionScheduler {

    private final QuestionFacadeService questionFacadeService;

    // 💡 매주 월요일 00시 01분 00초에 실행 (한국 시간 기준)
    @Scheduled(cron = "0 1 0 * * MON", zone = "Asia/Seoul")
    public void scheduleWeeklyCommonQuestion() {
        log.info("⏰ 주간 공통 질문 자동 생성 스케줄러가 작동을 시작합니다.");

        try {
            questionFacadeService.generateWeeklyCommonQuestion();
            log.info("✅ 주간 공통 질문 생성이 성공적으로 완료되었습니다.");
        } catch (Exception e) {
            log.error("🚨 주간 공통 질문 자동 생성 중 치명적인 오류가 발생했습니다.", e);
        }
    }
}
