package moment.storage.presentation;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import moment.storage.dto.request.UploadUrlRequest;
import moment.storage.dto.response.UploadUrlResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Moment API", description = "모멘트 관련 API 명세")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/storage")
public class StorageController {


    @GetMapping("/upload-url")
    public ResponseEntity<UploadUrlResponse> getUploadUrl(UploadUrlRequest request) {

    }
}
