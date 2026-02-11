package moment.storage.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.UUID;
import moment.config.TestTags;
import moment.storage.dto.request.UploadUrlRequest;
import moment.storage.dto.response.UploadUrlResponse;
import moment.storage.infrastructure.AwsS3Client;
import moment.user.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@Tag(TestTags.UNIT)
@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class FileStorageServiceTest {

    @InjectMocks
    FileStorageService fileStorageService;

    @Mock
    AwsS3Client awsS3Client;

    @Mock
    UserService userService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fileStorageService, "bucketPath", "test/images");
    }

    @Test
    void 이미지_업로드_url을_생성한다() {
        // given
        String fixedUuidString = "a1b2c3d4-e5f6-7890-1234-567890abcdef";
        UUID fixedUuid = UUID.fromString(fixedUuidString);
        UploadUrlRequest request = new UploadUrlRequest("test-image.jpg", "jpg");
        UploadUrlResponse expected = new UploadUrlResponse(
                "http://ap2-northest-s3/test-bucket/images/test-image.jpg",
                "https://test-cloudfront.example.com/test/images/test-image.jpg"
        );
        given(awsS3Client.getUploadUrl(any(String.class))).willReturn(expected);
        ArgumentCaptor<String> filePathCaptor = ArgumentCaptor.forClass(String.class);

        // when
        UploadUrlResponse uploadUrl;
        try (MockedStatic<UUID> mockedUuid = Mockito.mockStatic(UUID.class)) {
            mockedUuid.when(UUID::randomUUID).thenReturn(fixedUuid);
            uploadUrl = fileStorageService.getUploadUrl(request, 1L);
        }

        // then
        assertThat(uploadUrl).isEqualTo(expected);
        verify(awsS3Client).getUploadUrl(filePathCaptor.capture());
        String actualFilePath = filePathCaptor.getValue();
        assertThat(actualFilePath).isEqualTo("test/images/" + fixedUuidString + ".jpg");
    }

    @Test
    void 공백이_포함된_파일명으로_요청해도_S3_key에_UUID와_확장자만_포함된다() {
        // given
        String fixedUuidString = "a1b2c3d4-e5f6-7890-1234-567890abcdef";
        UUID fixedUuid = UUID.fromString(fixedUuidString);
        UploadUrlRequest request = new UploadUrlRequest(
                "Screenshot 2026-01-28 at 2.35.41 PM.png", "png"
        );
        UploadUrlResponse expected = new UploadUrlResponse("presigned-url", "cloudfront-url");
        given(awsS3Client.getUploadUrl(any(String.class))).willReturn(expected);
        ArgumentCaptor<String> filePathCaptor = ArgumentCaptor.forClass(String.class);

        // when
        try (MockedStatic<UUID> mockedUuid = Mockito.mockStatic(UUID.class)) {
            mockedUuid.when(UUID::randomUUID).thenReturn(fixedUuid);
            fileStorageService.getUploadUrl(request, 1L);
        }

        // then
        verify(awsS3Client).getUploadUrl(filePathCaptor.capture());
        String actualFilePath = filePathCaptor.getValue();
        assertThat(actualFilePath).isEqualTo("test/images/" + fixedUuidString + ".png");
        assertThat(actualFilePath).doesNotContain(" ");
        assertThat(actualFilePath).doesNotContain("Screenshot");
    }
}
