# Group API 이미지 저장 및 조회 플로우 보고서

> **작성일**: 2026-01-29
> **브랜치**: `fix/#1033`
> **관련 커밋**: `92a18d50 feat: add image support for Group API`

---

## 1. 개요

이 보고서는 Moment 프로젝트의 Group API에서 이미지가 저장되고 조회되는 전체 플로우를 설명합니다.

### 핵심 아키텍처

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              이미지 처리 아키텍처                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  [클라이언트]                                                                │
│       │                                                                     │
│       ├──(1) 업로드 URL 요청────────────────────►  [서버]                    │
│       │                                              │                      │
│       │◄─────────────────────────────────────────────┤                      │
│       │  (2) Presigned URL + CloudFront URL 반환     │                      │
│       │                                              │                      │
│       ├──(3) Presigned URL로 S3 직접 업로드─────►  [AWS S3]                  │
│       │                                              │                      │
│       ├──(4) Moment/Comment 생성 요청────────────►  [서버]                   │
│       │      (imageUrl = CloudFront URL)             │                      │
│       │                                              │                      │
│       │                                         [DB에 저장]                  │
│       │                                              │                      │
│       ├──(5) 피드 조회 요청──────────────────────►  [서버]                   │
│       │                                              │                      │
│       │◄─────────────────────────────────────────────┤                      │
│       │  (6) 최적화된 CloudFront URL 응답            │                      │
│       │                                              │                      │
│       ├──(7) 이미지 다운로드────────────────────►  [CloudFront]              │
│       │                                              │                      │
└───────┴──────────────────────────────────────────────┴──────────────────────┘
```

---

## 2. 이미지 저장 플로우

### 2.1 업로드 URL 발급

클라이언트가 이미지를 업로드하기 전에 먼저 Presigned URL을 발급받아야 합니다.

#### 엔드포인트

```
POST /api/v2/storage/upload-url
```

#### 요청

```java
// UploadUrlRequest.java
public record UploadUrlRequest(
    @NotBlank(message = "이미지 이름을 입력해주세요.")
    @Schema(description = "확장자를 포함한 파일 이름", example = "vacation-photo.jpg")
    String imageName
) {}
```

**요청 예시**:
```json
{
  "imageName": "vacation-photo.jpg"
}
```

#### 처리 흐름

```
[FileStorageController]
        │
        ▼
[FileStorageService.getUploadUrl()]
        │
        ├── 사용자 존재 확인 (userService.getUserBy)
        │
        ├── 파일 경로 생성
        │   filePath = "{bucketPath}{UUID}{imageName}"
        │   예: "moment-dev/images/a1b2c3d4-e5f6-7890-abcd-1234567890ab/vacation-photo.jpg"
        │
        ▼
[AwsS3Client.getUploadUrl()]
        │
        ├── S3 Presigned PUT URL 생성 (5분 유효)
        │
        ├── CloudFront URL 생성
        │
        ▼
[UploadUrlResponse 반환]
```

#### 응답

```java
// UploadUrlResponse.java
public record UploadUrlResponse(
    @Schema(description = "S3 업로드를 위한 Presigned URL")
    String presignedUrl,

    @Schema(description = "저장 후 접근 가능한 CloudFront URL")
    String filePath
) {}
```

**응답 예시**:
```json
{
  "code": 200,
  "status": "OK",
  "data": {
    "presignedUrl": "https://techcourse-project-2025.s3.ap-northeast-2.amazonaws.com/moment-dev/images/a1b2c3d4/vacation-photo.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=...&X-Amz-SignedHeaders=host&X-Amz-Expires=300&X-Amz-Signature=...",
    "filePath": "https://d123456789.cloudfront.net/moment-dev/images/a1b2c3d4/vacation-photo.jpg"
  }
}
```

#### URL 구조 분석

| 구성 요소 | 값 | 설명 |
|-----------|-----|------|
| `presignedUrl` | S3 URL + 서명 | 5분간 유효한 PUT 업로드용 URL |
| `filePath` | CloudFront URL | 저장 후 이미지 접근용 URL |
| `bucketPath` | `moment-dev/images/` | 환경별 저장 경로 (Dev/Prod) |
| `UUID` | 랜덤 UUID | 파일 고유성 보장 |

---

### 2.2 S3 직접 업로드 (클라이언트)

클라이언트는 발급받은 `presignedUrl`을 사용하여 S3에 직접 업로드합니다.

```javascript
// 클라이언트 코드 예시 (JavaScript)
const uploadImage = async (presignedUrl, file) => {
  await fetch(presignedUrl, {
    method: 'PUT',
    body: file,
    headers: {
      'Content-Type': file.type
    }
  });
};
```

**장점**:
- 서버를 거치지 않아 서버 부하 최소화
- 대용량 파일 업로드 가능
- 빠른 업로드 속도

---

### 2.3 Moment 생성 시 이미지 저장

#### 엔드포인트

```
POST /api/v2/groups/{groupId}/moments
```

#### 요청

```java
// GroupMomentCreateRequest.java
public record GroupMomentCreateRequest(
    @NotBlank(message = "내용을 입력해주세요.")
    @Schema(description = "모멘트 내용", example = "오늘의 여행 사진")
    String content,

    @Schema(description = "모멘트 사진 저장 경로 (CloudFront URL)",
            example = "https://d123456789.cloudfront.net/moment-dev/images/a1b2c3d4/vacation-photo.jpg")
    String imageUrl,

    @Schema(description = "모멘트 사진 원본 이름", example = "vacation-photo.jpg")
    String imageName
) {}
```

**요청 예시**:
```json
{
  "content": "오늘의 여행",
  "imageUrl": "https://d123456789.cloudfront.net/moment-dev/images/a1b2c3d4/vacation-photo.jpg",
  "imageName": "vacation-photo.jpg"
}
```

#### 처리 흐름

```
[GroupMomentController.createMoment()]
        │
        ▼
[MomentApplicationService.createMomentInGroup()]
        │
        ├── 사용자 조회 (userService.getUserBy)
        │
        ├── 그룹 멤버 조회 (memberService.getByGroupAndUser)
        │
        ├── Moment 엔티티 생성 (momentService.createInGroup)
        │
        ├── MomentImage 엔티티 생성 (imageUrl이 있는 경우)
        │   │
        │   └── [MomentImageService.create()]
        │           │
        │           ├── MomentImage.createNew(moment, imageUrl, imageName)
        │           │
        │           └── momentImageRepository.save()
        │
        ├── URL 최적화 (photoUrlResolver.resolve)
        │   │
        │   └── "moment-dev/images/" → "moment-optimized/images/"
        │
        ▼
[GroupMomentResponse 반환]
```

#### MomentImage 엔티티 저장

```java
// MomentImage.java
@Entity(name = "moment_images")
@SQLDelete(sql = "UPDATE moment_images SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class MomentImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "moment_id")
    private Moment moment;

    @Column(nullable = false, name = "url")
    private String imageUrl;  // CloudFront URL 저장

    @Column(nullable = false, name = "original_name")
    private String imageName;  // 원본 파일명

    private LocalDateTime deletedAt;  // Soft Delete
}
```

**DB 저장 예시**:

| id | moment_id | url | original_name | created_at |
|----|-----------|-----|---------------|------------|
| 1 | 42 | https://d123456789.cloudfront.net/moment-dev/images/a1b2c3d4/vacation-photo.jpg | vacation-photo.jpg | 2026-01-29 14:30:00 |

#### 응답

```java
// GroupMomentResponse.java
public record GroupMomentResponse(
    Long momentId,
    String content,
    String memberNickname,
    Long memberId,
    long likeCount,
    boolean hasLiked,
    long commentCount,
    String imageUrl,  // 최적화된 URL 반환
    LocalDateTime createdAt
) {}
```

**응답 예시**:
```json
{
  "code": 201,
  "status": "CREATED",
  "data": {
    "momentId": 42,
    "content": "오늘의 여행",
    "memberNickname": "여행자",
    "memberId": 5,
    "likeCount": 0,
    "hasLiked": false,
    "commentCount": 0,
    "imageUrl": "https://d123456789.cloudfront.net/moment-optimized/images/a1b2c3d4/vacation-photo.jpg",
    "createdAt": "2026-01-29T14:30:00"
  }
}
```

---

### 2.4 Comment 생성 시 이미지 저장

#### 엔드포인트

```
POST /api/v2/groups/{groupId}/moments/{momentId}/comments
```

#### 요청

```java
// GroupCommentCreateRequest.java
public record GroupCommentCreateRequest(
    @NotBlank(message = "내용을 입력해주세요.")
    @Schema(description = "댓글 내용")
    String content,

    @Schema(description = "댓글 이미지 저장 경로")
    String imageUrl,

    @Schema(description = "댓글 이미지 원본 이름")
    String imageName
) {}
```

#### 처리 흐름

```
[GroupCommentController.createComment()]
        │
        ▼
[CommentApplicationService.createCommentInGroup()]
        │
        ├── 사용자/멤버/모멘트 조회
        │
        ├── Comment 엔티티 생성
        │
        ├── CommentImage 엔티티 생성 (imageUrl이 있는 경우)
        │   │
        │   └── [CommentImageService.create()]
        │
        ├── URL 최적화 (photoUrlResolver.resolve)
        │
        ▼
[GroupCommentResponse 반환]
```

---

## 3. 이미지 조회 플로우

### 3.1 URL 최적화 로직 (PhotoUrlResolver)

조회 시 저장된 URL을 최적화된 경로로 변환합니다.

```java
// PhotoUrlResolver.java
@Component
public class PhotoUrlResolver {

    private final String originalPathSegment;    // "moment-dev/images/"
    private final String targetPathSegment;      // "moment-optimized/images/"

    public String resolve(String originalUrl) {
        if (originalUrl == null || originalUrl.isBlank()) {
            return originalUrl;
        }

        // 경로 변환
        return originalUrl.replace(originalPathSegment, targetPathSegment);
    }
}
```

**변환 예시**:

| 단계 | URL |
|------|-----|
| 저장된 URL | `https://d123456789.cloudfront.net/moment-dev/images/a1b2c3d4/photo.jpg` |
| 최적화된 URL | `https://d123456789.cloudfront.net/moment-optimized/images/a1b2c3d4/photo.jpg` |

**목적**: CloudFront Lambda@Edge를 통한 이미지 리사이징/압축 최적화

---

### 3.2 Moment 피드 조회

#### 엔드포인트

```
GET /api/v2/groups/{groupId}/moments?cursor={cursor}
```

#### 처리 흐름

```
[GroupMomentController.getGroupMoments()]
        │
        ▼
[MomentApplicationService.getGroupMoments()]
        │
        ├── 그룹 멤버 확인
        │
        ├── Moment 목록 조회 (커서 기반 페이징)
        │
        ├── MomentImage 배치 조회 (N+1 방지)
        │   │
        │   └── [MomentImageService.getMomentImageByMoment()]
        │           │
        │           └── Map<Moment, MomentImage> 반환
        │
        ├── 각 Moment에 대해:
        │   │
        │   ├── 좋아요 수 조회
        │   ├── 좋아요 여부 확인
        │   ├── 댓글 수 조회
        │   │
        │   └── URL 최적화
        │       photoUrlResolver.resolve(image.getImageUrl())
        │
        ▼
[GroupMomentListResponse 반환]
```

#### 응답

```java
// GroupMomentListResponse.java
public record GroupMomentListResponse(
    List<GroupMomentResponse> moments,
    Long nextCursor
) {}
```

**응답 예시**:
```json
{
  "code": 200,
  "status": "OK",
  "data": {
    "moments": [
      {
        "momentId": 42,
        "content": "오늘의 여행",
        "memberNickname": "여행자",
        "memberId": 5,
        "likeCount": 15,
        "hasLiked": true,
        "commentCount": 3,
        "imageUrl": "https://d123456789.cloudfront.net/moment-optimized/images/a1b2c3d4/vacation-photo.jpg",
        "createdAt": "2026-01-29T14:30:00"
      },
      {
        "momentId": 41,
        "content": "점심 메뉴",
        "memberNickname": "미식가",
        "memberId": 3,
        "likeCount": 8,
        "hasLiked": false,
        "commentCount": 1,
        "imageUrl": null,
        "createdAt": "2026-01-29T12:00:00"
      }
    ],
    "nextCursor": 41
  }
}
```

---

### 3.3 Comment 목록 조회

#### 엔드포인트

```
GET /api/v2/groups/{groupId}/moments/{momentId}/comments
```

#### 처리 흐름

```
[GroupCommentController.getComments()]
        │
        ▼
[CommentApplicationService.getCommentsInGroup()]
        │
        ├── 그룹 멤버 확인
        │
        ├── Comment 목록 조회
        │
        ├── CommentImage 배치 조회
        │   │
        │   └── Map<Comment, CommentImage> 반환
        │
        ├── 각 Comment에 대해:
        │   │
        │   ├── 좋아요 수/여부 조회
        │   │
        │   └── URL 최적화
        │
        ▼
[List<GroupCommentResponse> 반환]
```

#### 응답

```java
// GroupCommentResponse.java
public record GroupCommentResponse(
    Long commentId,
    String content,
    String memberNickname,
    Long memberId,
    long likeCount,
    boolean hasLiked,
    String imageUrl,  // 최적화된 URL
    LocalDateTime createdAt
) {}
```

---

### 3.4 나의 그룹 피드 조회 (상세 페이지)

#### 엔드포인트

```
GET /api/v2/users/me/groups/{groupId}/feeds
```

#### 처리 흐름

```
[MyGroupMomentPageFacadeService.getMyGroupFeed()]
        │
        ├── Moment 목록 조회
        │
        ├── MomentImage 배치 조회
        │
        ├── Comment 배치 조회 (각 Moment별)
        │
        ├── CommentImage 배치 조회
        │
        ├── 모든 URL 최적화
        │
        ▼
[MyGroupFeedResponse 반환]
```

---

## 4. 데이터 흐름 다이어그램

### 4.1 이미지 저장 시퀀스

```
┌──────────┐      ┌────────┐      ┌─────┐      ┌────┐
│ 클라이언트│      │  서버   │      │ S3  │      │ DB │
└────┬─────┘      └───┬────┘      └──┬──┘      └─┬──┘
     │                │               │           │
     │ 1. 업로드 URL 요청             │           │
     │ ─────────────►│               │           │
     │                │               │           │
     │ 2. Presigned URL 생성         │           │
     │                │◄──────────────│           │
     │                │               │           │
     │ 3. 응답 (presignedUrl, filePath)          │
     │ ◄──────────────│               │           │
     │                │               │           │
     │ 4. 이미지 업로드 (PUT)         │           │
     │ ──────────────────────────────►│           │
     │                │               │           │
     │ 5. 업로드 완료                 │           │
     │ ◄──────────────────────────────│           │
     │                │               │           │
     │ 6. Moment 생성 요청            │           │
     │    (imageUrl = CloudFront URL) │           │
     │ ─────────────►│               │           │
     │                │               │           │
     │                │ 7. Moment 저장           │
     │                │ ─────────────────────────►│
     │                │               │           │
     │                │ 8. MomentImage 저장       │
     │                │ ─────────────────────────►│
     │                │               │           │
     │ 9. 응답 (최적화된 imageUrl)    │           │
     │ ◄──────────────│               │           │
     │                │               │           │
```

### 4.2 이미지 조회 시퀀스

```
┌──────────┐      ┌────────┐      ┌────┐      ┌───────────┐
│ 클라이언트│      │  서버   │      │ DB │      │ CloudFront│
└────┬─────┘      └───┬────┘      └─┬──┘      └─────┬─────┘
     │                │              │               │
     │ 1. 피드 조회 요청             │               │
     │ ─────────────►│              │               │
     │                │              │               │
     │                │ 2. Moment 조회              │
     │                │ ────────────►│               │
     │                │              │               │
     │                │ 3. MomentImage 배치 조회    │
     │                │ ────────────►│               │
     │                │              │               │
     │                │ 4. URL 최적화               │
     │                │   (photoUrlResolver.resolve)│
     │                │              │               │
     │ 5. 응답                       │               │
     │    (최적화된 imageUrl 포함)   │               │
     │ ◄──────────────│              │               │
     │                │              │               │
     │ 6. 이미지 요청 (CDN URL)                      │
     │ ─────────────────────────────────────────────►│
     │                │              │               │
     │ 7. 최적화된 이미지 반환                       │
     │ ◄─────────────────────────────────────────────│
     │                │              │               │
```

---

## 5. URL 변환 상세

### 5.1 저장 시 URL

| 단계 | URL |
|------|-----|
| **Presigned URL** | `https://techcourse-project-2025.s3.ap-northeast-2.amazonaws.com/moment-dev/images/{UUID}/photo.jpg?X-Amz-...` |
| **CloudFront URL (저장용)** | `https://d123456789.cloudfront.net/moment-dev/images/{UUID}/photo.jpg` |
| **DB 저장 값** | `https://d123456789.cloudfront.net/moment-dev/images/{UUID}/photo.jpg` |

### 5.2 조회 시 URL

| 단계 | URL |
|------|-----|
| **DB에서 조회** | `https://d123456789.cloudfront.net/moment-dev/images/{UUID}/photo.jpg` |
| **PhotoUrlResolver 변환** | `moment-dev/images/` → `moment-optimized/images/` |
| **응답 URL** | `https://d123456789.cloudfront.net/moment-optimized/images/{UUID}/photo.jpg` |

### 5.3 환경별 설정

| 환경 | bucket-path | optimized-bucket-path |
|------|-------------|----------------------|
| **Dev** | `moment-dev/images/` | `moment-optimized/images/` |
| **Prod** | `moment-prod/images/` | `moment-optimized-prod/images/` |

---

## 6. API 엔드포인트 요약

### 6.1 이미지 업로드 관련

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v2/storage/upload-url` | 업로드 URL 발급 |

### 6.2 Moment 관련

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v2/groups/{groupId}/moments` | Moment 생성 (이미지 포함) |
| GET | `/api/v2/groups/{groupId}/moments` | Moment 목록 조회 |

### 6.3 Comment 관련

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v2/groups/{groupId}/moments/{momentId}/comments` | Comment 생성 (이미지 포함) |
| GET | `/api/v2/groups/{groupId}/moments/{momentId}/comments` | Comment 목록 조회 |

### 6.4 나의 그룹 피드

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v2/users/me/groups/{groupId}/feeds` | 나의 그룹 피드 상세 조회 |
| GET | `/api/v2/users/me/groups/{groupId}/comments` | 나의 그룹 댓글 목록 조회 |

---

## 7. 주요 파일 위치

### 7.1 Storage 모듈

| 파일 | 경로 |
|------|------|
| 컨트롤러 | `storage/presentation/FileStorageController.java` |
| 서비스 | `storage/application/FileStorageService.java` |
| S3 클라이언트 | `storage/infrastructure/AwsS3Client.java` |
| URL 변환기 | `storage/application/PhotoUrlResolver.java` |

### 7.2 Moment 모듈

| 파일 | 경로 |
|------|------|
| 컨트롤러 | `group/presentation/GroupMomentController.java` |
| 애플리케이션 서비스 | `moment/service/application/MomentApplicationService.java` |
| 이미지 서비스 | `moment/service/moment/MomentImageService.java` |
| 이미지 엔티티 | `moment/domain/MomentImage.java` |
| 요청 DTO | `moment/dto/request/GroupMomentCreateRequest.java` |
| 응답 DTO | `moment/dto/response/GroupMomentResponse.java` |

### 7.3 Comment 모듈

| 파일 | 경로 |
|------|------|
| 컨트롤러 | `group/presentation/GroupCommentController.java` |
| 애플리케이션 서비스 | `comment/service/application/CommentApplicationService.java` |
| 이미지 서비스 | `comment/service/comment/CommentImageService.java` |
| 이미지 엔티티 | `comment/domain/CommentImage.java` |
| 요청 DTO | `comment/dto/request/GroupCommentCreateRequest.java` |
| 응답 DTO | `comment/dto/response/GroupCommentResponse.java` |

---

## 8. 성능 최적화

### 8.1 배치 조회

N+1 문제 방지를 위해 배치 조회 패턴 적용:

```java
// MomentImageService.java
public Map<Moment, MomentImage> getMomentImageByMoment(List<Moment> moments) {
    List<MomentImage> images = momentImageRepository.findAllByMomentIn(moments);
    return images.stream()
        .collect(Collectors.toMap(
            MomentImage::getMoment,
            Function.identity()
        ));
}
```

### 8.2 CDN 캐싱

- CloudFront를 통한 글로벌 캐싱
- 최적화 경로로 변환하여 Lambda@Edge 리사이징 활용

### 8.3 Soft Delete

```java
@SQLDelete(sql = "UPDATE moment_images SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
```

---

## 9. 요약

| 항목 | 내용 |
|------|------|
| **업로드 방식** | 클라이언트 직접 업로드 (Presigned URL) |
| **저장 URL** | CloudFront URL (`moment-dev/images/...`) |
| **조회 URL** | 최적화 CloudFront URL (`moment-optimized/images/...`) |
| **이미지 엔티티** | `MomentImage`, `CommentImage` |
| **삭제 방식** | Soft Delete (`deleted_at` 필드) |
| **성능 최적화** | 배치 조회, CDN 캐싱 |

---

## 10. 변경 이력

| 날짜 | 커밋 | 설명 |
|------|------|------|
| 2026-01-29 | `92a18d50` | Group API 이미지 지원 기능 추가 |
| 2026-01-29 | `1655ee63` | 그룹 API 이미지 기능 세부 계획 문서 추가 |
