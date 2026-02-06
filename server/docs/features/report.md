# Report Domain (PREFIX: RPT)

> Last Updated: 2026-02-03
> Features: 2

## 기능 목록

### RPT-001: 모멘트 신고

- **Status**: DONE
- **API**: `POST /api/v2/moments/{id}/reports`
- **Key Classes**:
    - Controller: `ReportController`
    - Facade: `ReportCreateFacadeService`
    - Application: `ReportApplicationService`
    - Entity: `Report`
- **Business Rules**: 모멘트에 대한 신고 생성, 신고 사유 선택 필수
- **Dependencies**: moment (MomentService)
- **Tests**: `ReportServiceTest`, `ReportRepositoryTest`

### RPT-002: 댓글 신고

- **Status**: DONE
- **API**: `POST /api/v2/comments/{id}/reports`
- **Key Classes**:
    - Controller: `ReportController`
    - Facade: `ReportCreateFacadeService`
    - Application: `ReportApplicationService`
    - Entity: `Report`
- **Business Rules**: 댓글에 대한 신고 생성, 신고 사유 선택 필수
- **Dependencies**: comment (CommentService)
- **Tests**: `ReportServiceTest`, `ReportRepositoryTest`

## 신고 사유 (ReportReason enum)

- `SPAM_OR_ADVERTISEMENT`
- `SEXUAL_CONTENT`
- `HATE_SPEECH_OR_DISCRIMINATION`
- `ABUSE_OR_HARASSMENT`
- `VIOLENT_OR_DANGEROUS_CONTENT`
- `PRIVACY_VIOLATION`
- `ILLEGAL_INFORMATION`

## 관련 엔티티

- `Report` (@Entity: "reports") - ReportReason enum

## 관련 테스트 클래스 (2개)

- `ReportServiceTest`, `ReportRepositoryTest`

## DB 마이그레이션

- V16: `V16__create_reports__mysql.sql`
