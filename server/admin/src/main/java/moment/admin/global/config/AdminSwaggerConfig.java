package moment.admin.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminSwaggerConfig {

    @Bean
    public OpenAPI adminOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("모멘트 Admin API 문서")
                        .version("v1")
                        .description("모멘트 관리자 REST API 명세입니다.")
                )
                .tags(List.of(
                        new Tag().name("Admin Auth API").description("관리자 인증 API"),
                        new Tag().name("Admin Account API").description("관리자 계정 관리 API (SUPER_ADMIN 전용)"),
                        new Tag().name("Admin User API").description("관리자용 사용자 관리 API"),
                        new Tag().name("Admin Group API").description("관리자용 그룹 관리 API"),
                        new Tag().name("Admin Session API").description("관리자 세션 관리 API (SUPER_ADMIN 전용)")
                ));
    }
}
