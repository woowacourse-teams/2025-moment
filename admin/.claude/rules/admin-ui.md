---
paths:
  - "src/**/*.tsx"
---

# 어드민 UI 규칙

## 스타일링 (Emotion)

- 모든 스타일은 `@emotion/styled` 사용
- 스타일은 같은 `ui/` 디렉토리 안의 `*.styles.ts` 파일로 분리
- DOM에 전달되지 않는 props는 transient props 사용 (`$variant`)

## 컴포넌트 구조

**목록 페이지 레이아웃 순서:**
1. Header (제목 + 액션 버튼)
2. SearchFilter
3. Table
4. Pagination

**UI 컴포넌트는 순수하게 유지**: 데이터와 핸들러를 props로 받기만 하고, 내부에서 직접 데이터를 fetching하지 않습니다.

## 권한 처리

- `useAuth()`로 `user.role` 확인
- **VIEWER**: 데이터 변경 버튼 숨김 또는 비활성화
- **ADMIN**: 모든 기능 접근 가능
- 401/403 에러는 사용자에게 명확하게 안내

## 파괴적 동작 (삭제·정지 등)

반드시 다음 순서를 따릅니다:
1. ConfirmModal 표시
2. 사유 입력 필수
3. Audit API에 기록
