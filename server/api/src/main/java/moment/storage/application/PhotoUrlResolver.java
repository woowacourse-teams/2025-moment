package moment.storage.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PhotoUrlResolver {

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

        int lastSlashIndex = urlWithChangedPath.lastIndexOf('/');
        int lastDotIndex = urlWithChangedPath.lastIndexOf('.');

        /*if (lastDotIndex > lastSlashIndex) {
            return urlWithChangedPath.substring(0, lastDotIndex);
        }*/
        // todo 확장자 제거 로직 disable
        return urlWithChangedPath;
    }
}
