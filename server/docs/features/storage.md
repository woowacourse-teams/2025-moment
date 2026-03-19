# Storage Domain (PREFIX: STG)

> Last Updated: 2026-02-03
> Features: 1

## 기능 목록

### STG-001: 업로드 URL 발급

- **Status**: DONE
- **API**: `POST /api/v2/storage/upload-url`
- **Key Classes**:
    - Controller: `FileStorageController`
    - Domain: `FileStorageService`
    - Infrastructure: `AwsS3Client`
- **Business Rules**: AWS S3 Presigned URL 발급, 클라이언트에서 직접 업로드
- **Dependencies**: 없음 (외부: AWS S3)
- **Tests**: `FileStorageServiceTest`, `PhotoUrlResolverTest`, `AwsS3ClientTest`

## 관련 엔티티

- 없음 (Presigned URL 방식, 엔티티 불필요)

## 관련 테스트 클래스 (3개)

- `FileStorageServiceTest`, `PhotoUrlResolverTest`, `AwsS3ClientTest`

## DB 마이그레이션

- 없음
