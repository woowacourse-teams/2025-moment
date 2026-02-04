# Feature Tracking

## 읽기 규칙

| 상황 | 읽기 순서 |
|------|-----------|
| 새 기능 시작 전 | `.claude/docs/features/FEATURES.md` → 관련 `{domain}.md` |
| 크로스 도메인 기능 | `FEATURES.md` Cross-Domain Dependencies 확인 |
| 버그 수정 | 관련 `{domain}.md`에서 기존 동작 파악 |

## 업데이트 규칙

### 새 기능 추가 시

1. `{domain}.md`에 다음 ID로 `IN_PROGRESS` 항목 추가
2. `FEATURES.md` Recent Changes에 기록
3. 완료 시: `DONE`으로 변경, 테스트/마이그레이션 정보 기입, Quick Reference 기능 수 업데이트

### 기존 기능 수정 시

1. `{domain}.md` 해당 항목 업데이트
2. `FEATURES.md` Recent Changes에 변경 내역 기록

### 새 도메인 모듈 추가 시

1. `{domain}.md` 신규 생성
2. `FEATURES.md` Quick Reference에 행 추가

### 새 도메인 이벤트 추가 시

1. 발행 도메인의 `{domain}.md` "Domain Events Published" 섹션 업데이트
2. `FEATURES.md` Cross-Domain Dependencies에 행 추가

## Status 값

| Status | 의미 |
|--------|------|
| `DONE` | 테스트 포함 완전 구현 |
| `IN_PROGRESS` | 현재 구현 중 |
| `PLANNED` | 설계만 완료 |
| `DEPRECATED` | 제거 예정 |

## Feature ID 규칙

- 형식: `{PREFIX}-{NNN}` (3자리 숫자, 0-padded)
- 접두사 목록:

| PREFIX | 도메인 |
|--------|--------|
| AUTH | auth |
| USER | user |
| MOM | moment |
| CMT | comment |
| GRP | group |
| LIK | like |
| NTF | notification |
| RPT | report |
| STG | storage |
| ADM | admin |
| GLB | global |

## Last Updated 타임스탬프

- 파일 수정 시 상단의 "Last Updated" 날짜를 `YYYY-MM-DD` 형식으로 갱신

## Recent Changes

- `FEATURES.md`의 Recent Changes는 최근 20건만 유지
- 오래된 항목은 삭제
