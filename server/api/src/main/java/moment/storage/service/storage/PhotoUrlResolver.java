package moment.storage.service.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PhotoUrlResolver {

    private static final String OPTIMIZED_EXTENSION = ".webp";
    private final String originalPathSegment;
    private final String targetPathSegment;

    public PhotoUrlResolver(
            @Value("${s3.bucket-path}") String originalPathSegment,
            @Value("${s3.optimized-bucket-path}") String targetPathSegment) {
        this.originalPathSegment = originalPathSegment;
        this.targetPathSegment = targetPathSegment;
    }

    public String resolve(String originalUrl) {
        if (originalUrl == null || originalUrl.isBlank()) {
            return originalUrl;
        }

        String urlWithChangedPath = originalUrl.replace(originalPathSegment, targetPathSegment);

        return changeExtensionToWebp(urlWithChangedPath);
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
