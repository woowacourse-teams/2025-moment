package moment.admin.global.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 클라이언트 IP 주소 추출 유틸리티
 * 프록시/로드밸런서를 거친 경우에도 실제 클라이언트 IP를 추출합니다.
 */
public final class ClientIpExtractor {

    private static final String UNKNOWN = "unknown";
    private static final String X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String PROXY_CLIENT_IP = "Proxy-Client-IP";
    private static final String WL_PROXY_CLIENT_IP = "WL-Proxy-Client-IP";
    private static final String HTTP_CLIENT_IP = "HTTP_CLIENT_IP";
    private static final String HTTP_X_FORWARDED_FOR = "HTTP_X_FORWARDED_FOR";

    private ClientIpExtractor() {
        // 유틸리티 클래스 인스턴스화 방지
    }

    /**
     * HttpServletRequest에서 클라이언트 IP 주소를 추출합니다.
     * 프록시/로드밸런서 헤더를 순차적으로 확인하고,
     * 없으면 remoteAddr을 반환합니다.
     *
     * @param request HTTP 요청
     * @return 클라이언트 IP 주소
     */
    public static String extract(HttpServletRequest request) {
        String ip = getHeaderValue(request, X_FORWARDED_FOR);

        if (isBlankOrUnknown(ip)) {
            ip = getHeaderValue(request, PROXY_CLIENT_IP);
        }
        if (isBlankOrUnknown(ip)) {
            ip = getHeaderValue(request, WL_PROXY_CLIENT_IP);
        }
        if (isBlankOrUnknown(ip)) {
            ip = getHeaderValue(request, HTTP_CLIENT_IP);
        }
        if (isBlankOrUnknown(ip)) {
            ip = getHeaderValue(request, HTTP_X_FORWARDED_FOR);
        }
        if (isBlankOrUnknown(ip)) {
            ip = request.getRemoteAddr();
        }

        // X-Forwarded-For는 여러 IP를 포함할 수 있음 (쉼표로 구분)
        // 첫 번째 IP가 원본 클라이언트 IP
        return extractFirstIp(ip);
    }

    private static String getHeaderValue(HttpServletRequest request, String headerName) {
        return request.getHeader(headerName);
    }

    private static boolean isBlankOrUnknown(String value) {
        return value == null || value.isBlank() || UNKNOWN.equalsIgnoreCase(value);
    }

    private static String extractFirstIp(String ip) {
        if (ip != null && ip.contains(",")) {
            return ip.split(",")[0].trim();
        }
        return ip;
    }
}
