package moment.storage.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import moment.auth.presentation.AuthenticationPrincipal;
import moment.global.dto.response.ErrorResponse;
import moment.global.dto.response.SuccessResponse;
import moment.storage.service.storage.FileStorageService;
import moment.storage.dto.request.UploadUrlRequest;
import moment.storage.dto.response.UploadUrlResponse;
import moment.user.dto.request.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Storage API", description = "S3 저장소 API 명세")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/storage")
public class FileStorageController {

    private final FileStorageService fileStorageService;

    @Operation(summary = "이미지 업로드 url 생성", description = "s3에 업로드 하기 위한 url을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "이미지 업로드 url 생성 성공"),
            @ApiResponse(responseCode = "401", description = """
                    - [T-005] 토큰을 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = """
                    - [U-002] 존재하지 않는 사용자입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @PostMapping("/upload-url")
    public ResponseEntity<SuccessResponse<UploadUrlResponse>> getUploadUrl(
            @Valid @RequestBody UploadUrlRequest request,
            @AuthenticationPrincipal Authentication authentication
            ) {
        UploadUrlResponse response = fileStorageService.getUploadUrl(request, authentication.id());
        HttpStatus status = HttpStatus.OK;

        return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
    }
}
