# Phase 4: CLAUDE.md 업데이트

> Status: PENDING
> Parent Plan: feature-documentation-system.md

## 목표

CLAUDE.md를 현재 코드베이스 상태에 맞게 수정하고, Feature Registry 참조 추가

## 수정 파일

`.claude/CLAUDE.md` (기존 파일 3곳 수정)

## 상세 변경 사항

### 변경 1: `## 참고` 섹션에 Feature Registry 참조 추가

**위치**: CLAUDE.md 하단 `## 참고` 섹션

**추가 내용**:

```markdown
### Feature Registry
- **기능 인덱스**: `.claude/docs/features/FEATURES.md` — 전체 기능 목록 및 상태
- **도메인별 상세**: `.claude/docs/features/{domain}.md` — 도메인별 기능, API, 클래스, 테스트 매핑
- **추적 규칙**: `.claude/rules/feature-tracking.md` — 기능 문서 유지 규칙
```

**삽입 위치**: `### 기존 구현 참조` 바로 위

---

### 변경 2: 모듈 구조 업데이트

**위치**: `### 모듈 구조 (도메인 주도 모듈러 모놀리스)` 섹션

**현재 (Before)**:

```
src/main/java/moment/
├── auth/          # 인증/인가 (JWT)
├── comment/       # 댓글 (도메인명: "Echo")
├── moment/        # 핵심 모멘트 게시물
├── notification/  # 알림 (SSE + Firebase Push)
├── report/        # 콘텐츠 신고
├── reward/        # 보상/포인트
├── storage/       # 파일 저장소 (AWS S3)
├── user/          # 사용자 & 레벨
└── global/        # 공유 인프라
```

**수정 후 (After)**:

```
src/main/java/moment/
├── admin/         # 관리자 (세션 기반 인증, 사용자/그룹/콘텐츠 관리)
├── auth/          # 인증/인가 (JWT, Google OAuth, Apple Sign-in)
├── comment/       # 댓글 (도메인명: "Echo")
├── group/         # 그룹 (CRUD, 멤버 관리, 초대, 그룹 모멘트/코멘트)
├── like/          # 좋아요 (모멘트/코멘트 좋아요 토글)
├── moment/        # 핵심 모멘트 게시물
├── notification/  # 알림 (SSE + Firebase Push)
├── report/        # 콘텐츠 신고
├── storage/       # 파일 저장소 (AWS S3)
├── user/          # 사용자 관리
└── global/        # 공유 인프라
```

**변경 내용 요약**:
- `reward/` 제거 (코드베이스에 존재하지 않음 - 실제 디렉토리 검증 필요)
- `admin/` 추가
- `group/` 추가
- `like/` 추가
- `auth/` 설명 보강 (Google OAuth, Apple Sign-in)
- `user/` 설명에서 "& 레벨" 제거

---

### 변경 3: 사용자 레벨 시스템 관련 내용 제거/수정

**위치**: `## 도메인 규칙` > `### 사용자 레벨 시스템`

**현재 (Before)**:

```markdown
### 사용자 레벨 시스템
- `expStar` 기반 15개 레벨: `ASTEROID_WHITE`(0) → `GAS_GIANT_SKY`(32000+)
- `User.addStarAndUpdateLevel()`로 자동 업데이트
```

**판단 필요**: User 엔티티에 expStar/Level 관련 필드가 실제로 존재하는지 확인

- 존재하는 경우: 내용 유지 (현재 상태 그대로)
- 존재하지 않는 경우: 해당 섹션 전체 제거

**확인 방법**: `User.java`에서 `expStar`, `Level`, `addStarAndUpdateLevel` 존재 여부 grep

---

## 작업 순서

1. `User.java`에서 expStar/Level 관련 코드 존재 여부 확인
2. `src/main/java/moment/` 하위 실제 디렉토리 목록 확인 (reward 존재 여부)
3. 변경 1: Feature Registry 참조 추가
4. 변경 2: 모듈 구조 업데이트
5. 변경 3: 레벨 시스템 섹션 처리 (확인 결과에 따라)

## 선행 조건

- Phase 1, 2, 3 모두 완료 (참조할 파일들이 존재해야 함)

## 후행 조건

- 없음

## 검증 기준

- [ ] CLAUDE.md의 모듈 구조가 `src/main/java/moment/` 실제 디렉토리와 일치
- [ ] Feature Registry 링크 경로가 올바른지 확인
- [ ] `reward/` 모듈이 실제 존재하지 않으면 제거 확인
- [ ] 레벨 시스템 관련 내용이 실제 코드 상태와 일치
- [ ] CLAUDE.md 전체가 유효한 마크다운인지 확인