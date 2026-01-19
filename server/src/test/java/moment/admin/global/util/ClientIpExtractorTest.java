package moment.admin.global.util;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayNameGeneration(ReplaceUnderscores.class)
class ClientIpExtractorTest {

    @Test
    void X_Forwarded_For_헤더가_있으면_해당_IP를_반환한다() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.100");

        String ip = ClientIpExtractor.extract(request);

        assertThat(ip).isEqualTo("192.168.1.100");
    }

    @Test
    void X_Forwarded_For에_여러_IP가_있으면_첫번째_IP를_반환한다() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.100, 10.0.0.1, 172.16.0.1");

        String ip = ClientIpExtractor.extract(request);

        assertThat(ip).isEqualTo("192.168.1.100");
    }

    @Test
    void X_Forwarded_For가_없으면_Proxy_Client_IP를_확인한다() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn("10.0.0.50");

        String ip = ClientIpExtractor.extract(request);

        assertThat(ip).isEqualTo("10.0.0.50");
    }

    @Test
    void 모든_헤더가_없으면_remoteAddr를_반환한다() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("HTTP_CLIENT_IP")).thenReturn(null);
        when(request.getHeader("HTTP_X_FORWARDED_FOR")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        String ip = ClientIpExtractor.extract(request);

        assertThat(ip).isEqualTo("127.0.0.1");
    }

    @Test
    void unknown_값은_무시하고_다음_헤더를_확인한다() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("unknown");
        when(request.getHeader("Proxy-Client-IP")).thenReturn("UNKNOWN");
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn("192.168.1.200");

        String ip = ClientIpExtractor.extract(request);

        assertThat(ip).isEqualTo("192.168.1.200");
    }

    @Test
    void 빈_값은_무시하고_다음_헤더를_확인한다() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("");
        when(request.getHeader("Proxy-Client-IP")).thenReturn("   ");
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("HTTP_CLIENT_IP")).thenReturn("172.16.0.100");

        String ip = ClientIpExtractor.extract(request);

        assertThat(ip).isEqualTo("172.16.0.100");
    }
}
