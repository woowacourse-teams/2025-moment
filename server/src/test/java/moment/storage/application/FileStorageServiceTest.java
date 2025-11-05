package moment.storage.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.UUID;
import moment.config.TestTags;
import moment.storage.dto.request.UploadUrlRequest;
import moment.storage.dto.response.UploadUrlResponse;
import moment.storage.infrastructure.AwsS3Client;
import moment.user.service.user.UserService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@Tag(TestTags.UNIT)
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

    @InjectMocks
    FileStorageService fileStorageService;

    @Mock
    AwsS3Client awsS3Client;

    @Mock
    UserService userService;

    @Test
    void 이미지_업로드_url을_생성한다() {
        // given
        String fixedUuidString = "a1b2c3d4-e5f6-7890-1234-567890abcdef";
        UUID fixedUuid = UUID.fromString(fixedUuidString);
        String originalFilename = "test-image.jpg";
        UploadUrlRequest request = new UploadUrlRequest(originalFilename, "jpg");
        String expectedUrl = "http://ap2-northest-s3/test-bucket/images/test-image.jpg";
        String expectedFilePath = "test-bucket/images/test-image.jpg";
        UploadUrlResponse expected = new UploadUrlResponse(expectedUrl, expectedFilePath);
        UploadUrlResponse uploadUrl;
        given(awsS3Client.getUploadUrl(any(String.class))).willReturn(expected);

        // when
        try (MockedStatic<UUID> mockedUuid = Mockito.mockStatic(UUID.class)) {
            mockedUuid.when(UUID::randomUUID).thenReturn(fixedUuid);
            uploadUrl = fileStorageService.getUploadUrl(request, 1L);
        }

        // that
        assertThat(uploadUrl).isEqualTo(expected);
    }
}
