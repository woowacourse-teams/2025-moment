package moment.admin.global.util;

/**
 * User-Agent 문자열을 파싱하여 브라우저, OS, 기기 유형을 추출하는 유틸리티
 */
public final class UserAgentParser {

    private UserAgentParser() {
        // 유틸리티 클래스
    }

    /**
     * User-Agent 문자열을 파싱하여 결과를 반환
     *
     * @param userAgent User-Agent 문자열
     * @return 파싱된 결과 (브라우저, OS, 기기 유형)
     */
    public static ParsedUserAgent parse(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return new ParsedUserAgent("Unknown", "Unknown", "Unknown");
        }

        String browser = parseBrowser(userAgent);
        String os = parseOs(userAgent);
        String deviceType = parseDeviceType(userAgent);

        return new ParsedUserAgent(browser, os, deviceType);
    }

    private static String parseBrowser(String userAgent) {
        // Edge 먼저 체크 (Chrome 식별자도 포함하므로)
        if (userAgent.contains("Edg/") || userAgent.contains("Edge/")) {
            return "Edge";
        }
        if (userAgent.contains("Chrome/")) {
            return "Chrome";
        }
        if (userAgent.contains("Firefox/")) {
            return "Firefox";
        }
        if (userAgent.contains("Safari/") && !userAgent.contains("Chrome/")) {
            return "Safari";
        }
        if (userAgent.contains("MSIE") || userAgent.contains("Trident/")) {
            return "Internet Explorer";
        }
        if (userAgent.contains("Opera/") || userAgent.contains("OPR/")) {
            return "Opera";
        }
        return "Unknown";
    }

    private static String parseOs(String userAgent) {
        // 모바일 OS 먼저 체크
        if (userAgent.contains("iPhone") || userAgent.contains("iPad") || userAgent.contains("iPod")) {
            return "iOS";
        }
        if (userAgent.contains("Android")) {
            return "Android";
        }
        // 데스크톱 OS
        if (userAgent.contains("Windows")) {
            return "Windows";
        }
        if (userAgent.contains("Mac OS X") || userAgent.contains("Macintosh")) {
            return "Mac OS";
        }
        if (userAgent.contains("Linux")) {
            return "Linux";
        }
        if (userAgent.contains("CrOS")) {
            return "Chrome OS";
        }
        return "Unknown";
    }

    private static String parseDeviceType(String userAgent) {
        // 태블릿 먼저 체크
        if (userAgent.contains("iPad") || userAgent.contains("Tablet")) {
            return "Tablet";
        }
        // 모바일 체크
        if (userAgent.contains("Mobile") || userAgent.contains("iPhone") || userAgent.contains("iPod")) {
            return "Mobile";
        }
        if (userAgent.contains("Android") && !userAgent.contains("Mobile")) {
            return "Tablet";  // Android without Mobile is usually a tablet
        }
        // 데스크톱 OS 확인
        if (userAgent.contains("Windows") || userAgent.contains("Macintosh")
                || userAgent.contains("Linux") || userAgent.contains("CrOS")) {
            return "Desktop";
        }
        return "Unknown";
    }

    /**
     * 파싱된 User-Agent 결과
     */
    public record ParsedUserAgent(
            String browser,
            String os,
            String deviceType
    ) {
    }
}
