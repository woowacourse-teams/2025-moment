package moment.storage.service.storage;

import moment.storage.dto.response.UploadUrlResponse;

public interface FileUploadClient {

    UploadUrlResponse getUploadUrl(String filePath);
}
