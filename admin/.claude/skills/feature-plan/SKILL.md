---
name: feature-plan
description: 새로운 어드민 기능을 코딩하기 전에 구조화된 구현 계획을 작성합니다. 기능을 처음부터 시작할 때 사용하세요.
argument-hint: <기능명>
disable-model-invocation: true
---

다음 기능의 구현 계획을 작성하세요: $ARGUMENTS

## 출력 형식

### 1) 범위 (Scope)
- 포함 범위:
- 제외 범위:

### 2) 사용자 스토리 & 인수 조건 (AC)
- 스토리 1: AC...
- 스토리 2: AC...

### 3) UI/UX 고려사항
- 상태 처리: 로딩 / 빈 데이터 / 에러
- 파괴적 동작에 대한 안전장치
- VIEWER / ADMIN 권한별 노출 차이

### 4) 쿼리 키 계획
```typescript
<entity>: {
  all: ["<entity>"] as const,
  list: (filters) => [..., "list", filters] as const,
  detail: (id) => [..., "detail", id] as const,
}
```

### 5) 파일 계획
```
features/<entity>/
├── api/          # useXQuery.ts, useXMutation.ts
├── hooks/        # useXList.ts, useXDetail.ts
├── ui/           # Table, Modal, Filter + styles
├── types/        # entity.ts
└── index.ts

pages/
└── <Entity>ListPage.tsx, <Entity>DetailPage.tsx

shared/api/queryKeys.ts  # 키 추가
```

### 6) PR 계획 (2~4개)
1. API + 타입 + 쿼리 키
2. 로직 훅
3. UI 컴포넌트
4. 페이지 + 라우팅

### 7) 엣지 케이스
- 401/403 처리, 동시 업데이트, 오래된 데이터(stale data)

---

**구현을 진행하기 전에 반드시 사용자 승인을 기다리세요.**
