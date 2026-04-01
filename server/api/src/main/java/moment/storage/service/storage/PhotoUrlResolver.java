package moment.storage.service.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PhotoUrlResolver {

    private static final String OPTIMIZED_EXTENSION = ".webp";
    private static final String AMAZONAWS_HOST_SUFFIX = ".amazonaws.com";

    private final String originalPathSegment;
    private final String targetPathSegment;
    private final String cloudfrontDomain;

    public PhotoUrlResolver(
            @Value("${s3.bucket-path}") String originalPathSegment,
            @Value("${s3.optimized-bucket-path}") String targetPathSegment,
            @Value("${s3.cloudfront-domain}") String cloudfrontDomain) {
        this.originalPathSegment = originalPathSegment;
        this.targetPathSegment = targetPathSegment;
        this.cloudfrontDomain = cloudfrontDomain;
    }

    public String resolveToOriginal(String url) {
        if (url == null || url.isBlank()) {
            return url;
        }
        return toCloudfrontUrl(url);
    }

    public String resolve(String originalUrl) {
        if (originalUrl == null || originalUrl.isBlank()) {
            return originalUrl;
        }

        String cloudfrontUrl = toCloudfrontUrl(originalUrl);
        String urlWithChangedPath = cloudfrontUrl.replace(originalPathSegment, targetPathSegment);

        return changeExtensionToWebp(urlWithChangedPath);
    }

    private String toCloudfrontUrl(String url) {
        if (url.startsWith(cloudfrontDomain)) {
            return url;
        }
        int amazonawsIndex = url.indexOf(AMAZONAWS_HOST_SUFFIX);
        if (amazonawsIndex != -1) {
            String path = url.substring(amazonawsIndex + AMAZONAWS_HOST_SUFFIX.length());
            return cloudfrontDomain + path;
        }
        return url;
    }

    private String changeExtensionToWebp(String url) {
        int lastSlashIndex = url.lastIndexOf('/');
        int lastDotIndex = url.lastIndexOf('.');

        // 방어 로직: 파일명에 확장자가 없는 경우
        // (예: 도메인에만 '.'이 있거나 'https://domain.com/images/profile' 처럼 끝나는 경우)
        if (lastDotIndex <= lastSlashIndex) {
            return url + OPTIMIZED_EXTENSION;
        }

        // 기존 확장자를 제거하고 .webp 확장자 결합
        return url.substring(0, lastDotIndex) + OPTIMIZED_EXTENSION;
    }
}
