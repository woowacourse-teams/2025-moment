package moment.admin.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * Spring Session JDBC 설정
 * HTTP 세션을 데이터베이스에 영속화하여 서버 재시작 후에도 세션을 유지합니다.
 */
@Configuration
@EnableJdbcHttpSession
public class SessionConfig {

    @Value("${admin.session.cookie-name:SESSION}")
    private String sessionCookieName;

    /**
     * 세션 쿠키 설정
     * Spring Session 쿠키 이름을 admin.session.cookie-name과 동일하게 유지합니다.
     *
     * @return CookieSerializer
     */
    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setCookieName(sessionCookieName);
        return serializer;
    }
}
