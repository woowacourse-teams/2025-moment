# Phase 3: Claude Code 규칙 파일 생성

> Status: PENDING
> Parent Plan: feature-documentation-system.md

## 목표

Claude Code가 기능 문서를 자동으로 읽고 업데이트하도록 하는 규칙 파일 생성

## 생성 파일

`.claude/rules/feature-tracking.md` (신규, ~50줄)

## 상세 내용

### 파일 구조

```markdown
# Feature Tracking

## 읽기 규칙
## 업데이트 규칙
### 새 기능 추가
### 기존 기능 수정
### 새 도메인 모듈 추가
### 새 도메인 이벤트 추가
## Status 값
## Feature ID 규칙
## Last Updated 타임스탬프
## Recent Changes
```

### 읽기 규칙

| 상황 | 읽기 순서 |
|------|-----------|
| 새 기능 시작 전 | FEATURES.md → 관련 {domain}.md |
| 크로스 도메인 기능 | FEATURES.md Cross-Domain Dependencies 확인 |
| 버그 수정 | 관련 {domain}.md에서 기존 동작 파악 |

### 업데이트 규칙

#### 새 기능 추가 시

1. `{domain}.md`에 다음 ID로 `IN_PROGRESS` 항목 추가
2. `FEATURES.md` Recent Changes에 기록
3. 완료 시: `DONE`으로 변경, 테스트/마이그레이션 정보 기입, 기능 수 업데이트

#### 기존 기능 수정 시

1. `{domain}.md` 해당 항목 업데이트
2. `FEATURES.md` Recent Changes에 변경 내역 기록

#### 새 도메인 모듈 추가 시

1. `{domain}.md` 신규 생성
2. `FEATURES.md` Quick Reference에 행 추가

#### 새 도메인 이벤트 추가 시

1. 발행 도메인의 `{domain}.md` "Domain Events Published" 업데이트
2. `FEATURES.md` Cross-Domain Dependencies에 행 추가

### Status 값

| Status | 의미 |
|--------|------|
| `DONE` | 테스트 포함 완전 구현 |
| `IN_PROGRESS` | 현재 구현 중 |
| `PLANNED` | 설계만 완료 |
| `DEPRECATED` | 제거 예정 |

### Feature ID 규칙

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

### Last Updated

- 파일 수정 시 상단의 "Last Updated" 날짜를 `YYYY-MM-DD` 형식으로 갱신

### Recent Changes

- `FEATURES.md`의 Recent Changes는 최근 20건만 유지
- 오래된 항목은 삭제

## 작업 순서

1. `.claude/rules/feature-tracking.md` 파일 생성
2. 위 내용을 실제 마크다운으로 작성
3. 기존 `.claude/rules/` 파일들과 형식 일관성 확인

## 선행 조건

- Phase 1, 2 완료 (규칙이 참조하는 파일들이 존재해야 함)

## 후행 조건

- 없음

## 검증 기준

- [ ] 규칙 파일이 `.claude/rules/` 디렉토리에 위치
- [ ] 모든 PREFIX가 실제 도메인 모듈과 매핑
- [ ] 규칙이 명확하고 모호하지 않음
- [ ] 기존 rules 파일(coding-style.md, git-workflow.md, testing.md 등)과 형식 일관성