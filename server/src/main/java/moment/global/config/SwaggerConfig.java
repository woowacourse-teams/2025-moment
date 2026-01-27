package moment.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        servers = {
                @Server(url = "https://dev.connectingmoment.com", description = "개발 서버"),
                @Server(url = "https://connectingmoment.com", description = "운영 서버")
        }
)
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("모멘트 API 문서")
                        .version("v1")
                        .description("모멘트 REST API 명세입니다.")
                )
                .tags(List.of(
                        // 1. 인증/사용자
                        new Tag().name("Auth API").description("인증/인가 관련 API 명세"),
                        new Tag().name("User API").description("사용자 관련 API 명세"),
                        new Tag().name("MyPage API").description("마이페이지 API 명세"),

                        // 2. 콘텐츠
                        new Tag().name("Moment API").description("모멘트 관련 API 명세"),
                        new Tag().name("Comment API").description("Comment 관련 API 명세"),
                        new Tag().name("Report API").description("신고 관련 API 명세"),

                        // 3. 알림
                        new Tag().name("Notification API").description("알림 관련 API 명세"),
                        new Tag().name("Push Notification API").description("푸시 알림 관련 API 명세"),

                        // 4. 그룹
                        new Tag().name("Group API").description("그룹 관리 관련 API 명세"),
                        new Tag().name("Group Invite API").description("그룹 초대 관련 API 명세"),
                        new Tag().name("Group Member API").description("그룹 멤버 관리 관련 API 명세"),
                        new Tag().name("Group Member Approval API").description("그룹 멤버 승인/강퇴/소유권 이전 관련 API 명세"),
                        new Tag().name("Group Moment API").description("그룹 모멘트 관련 API 명세"),
                        new Tag().name("Group Comment API").description("그룹 코멘트 관련 API 명세"),

                        // 5. 스토리지
                        new Tag().name("Storage API").description("S3 저장소 API 명세"),

                        // 6. 관리자
                        new Tag().name("Admin Auth API").description("관리자 인증 API"),
                        new Tag().name("Admin Account API").description("관리자 계정 관리 API (SUPER_ADMIN 전용)"),
                        new Tag().name("Admin User API").description("관리자용 사용자 관리 API"),
                        new Tag().name("Admin Group API").description("관리자용 그룹 관리 API"),
                        new Tag().name("Admin Session API").description("관리자 세션 관리 API (SUPER_ADMIN 전용)")
                ));
    }
}
