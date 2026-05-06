# Moment Admin CLAUDE.md

Moment 어드민 패널 개발 가이드입니다. **정확성과 추적 가능성**이 최우선입니다.

---

## 핵심 원칙

1. **권한 분리**: ADMIN (전체 접근) / VIEWER (읽기 전용)
2. **감사 로그**: 상태를 변경하는 모든 동작은 반드시 기록 (누가, 무엇을, 어떤 이유로)
3. **안전장치**: 파괴적 동작(삭제·정지 등)은 반드시 ConfirmModal + 사유 입력 필요

---

## 아키텍처 (FSD)

**의존성 방향**: `app` → `pages` → `widgets` → `features` → `entities` → `shared`

| 레이어 | 역할 | 구조 |
|--------|------|------|
| `pages/` | 페이지 조합 (로직 없음) | `<Entity>ListPage.tsx`, `<Entity>DetailPage.tsx` |
| `features/` | 비즈니스 로직 | `api/`, `hooks/`, `ui/`, `types/`, `index.ts` |
| `shared/` | 공통 인프라 | `api/`, `auth/`, `ui/` |

**feature 내부 구조**:
```
features/<entity>/
├── api/          # useXQuery.ts, useXMutation.ts
├── hooks/        # useXList.ts, useXDetail.ts, useXEdit.ts
├── ui/           # Component.tsx + Component.styles.ts
├── types/        # entity.ts
└── index.ts      # 외부 공개 API
```

---

## 네이밍 규칙

### 파일명

| 용도 | 패턴 | 예시 |
|------|------|------|
| 쿼리 훅 | `use<Entity>Query.ts` | `useUsersQuery.ts` |
| 뮤테이션 훅 | `use<Action><Entity>Mutation.ts` | `useDeleteUserMutation.ts` |
| 로직 훅 | `use<Entity><Context>.ts` | `useUserList.ts` |
| UI 컴포넌트 | `<Entity><Type>.tsx` | `UserTable.tsx` |
| 스타일 | `<Component>.styles.ts` | `UserTable.styles.ts` |

### 쿼리 키

`shared/api/queryKeys.ts`에 중앙 집중 관리:
```typescript
queryKeys.<entity>.list(params)
queryKeys.<entity>.detail(id)
```

---

## 구현 체크리스트

- [ ] 권한 확인 (`useAuth` → `user.role`)
- [ ] 로딩 / 에러 / 빈 데이터 상태 처리
- [ ] 파괴적 동작 → ConfirmModal + 사유 입력
- [ ] 스타일은 `*.styles.ts`로 분리

---

## 기술 스택

- React + TypeScript + Vite
- TanStack Query (서버 상태 관리)
- Emotion (스타일링)
- React Router (라우팅)
