package moment.storage.presentation;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import moment.global.dto.response.SuccessResponse;
import moment.storage.application.FileStorageService;
import moment.storage.dto.request.UploadUrlRequest;
import moment.storage.dto.response.UploadUrlResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "STORAGE API", description = "S3 저장소 API 명세")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/storage")
public class FileStorageController {

    private final FileStorageService fileStorageService;

    @GetMapping("/upload-url")
    public ResponseEntity<SuccessResponse<UploadUrlResponse>> getUploadUrl(
            @Valid @RequestBody UploadUrlRequest request
    ) {
        UploadUrlResponse response = fileStorageService.getUploadUrl(request);
        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }
}
