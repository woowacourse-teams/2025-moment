package moment.admin.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moment.admin.service.admin.AdminService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final AdminService adminService;

    @Value("${admin.initial.email}")
    private String initialEmail;

    @Value("${admin.initial.password}")
    private String initialPassword;

    @Value("${admin.initial.name}")
    private String initialName;

    @Override
    public void run(String... args) throws Exception {
        if (!adminService.existsByEmail(initialEmail)) {
            adminService.createAdmin(initialEmail, initialName, initialPassword);
            log.info("✅ 초기 관리자 계정 생성: {}", initialEmail);
            log.warn("⚠️  프로덕션 환경에서는 반드시 초기 비밀번호를 변경하세요!");
        }
    }
}
