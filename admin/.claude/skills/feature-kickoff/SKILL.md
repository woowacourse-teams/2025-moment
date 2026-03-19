---
name: feature-kickoff
description: Create a structured implementation plan before coding a new admin feature. Use when starting a new feature from scratch.
argument-hint: <feature-name>
disable-model-invocation: true
---

Create an implementation plan for: $ARGUMENTS

## Output Format

### 1) Scope
- In scope:
- Out of scope:

### 2) User Stories & AC
- Story 1: AC...
- Story 2: AC...

### 3) UI/UX Notes
- States: loading / empty / error
- Guardrails for destructive actions
- VIEWER vs ADMIN visibility

### 4) Query Plan
```typescript
<entity>: {
  all: ["<entity>"] as const,
  list: (filters) => [..., "list", filters] as const,
  detail: (id) => [..., "detail", id] as const,
}
```

### 5) File Plan
```
features/<entity>/
├── api/          # useXQuery.ts, useXMutation.ts
├── hooks/        # useXList.ts, useXDetail.ts
├── ui/           # Table, Modal, Filter + styles
├── types/        # entity.ts
└── index.ts

pages/
└── <Entity>ListPage.tsx, <Entity>DetailPage.tsx

shared/api/queryKeys.ts  # Add keys
```

### 6) PR Plan (2-4 PRs)
1. API + Types + Query Keys
2. Logic Hooks
3. UI Components
4. Pages + Routing

### 7) Edge Cases
- 401/403, concurrent updates, stale data

---

**Wait for user approval before proceeding to implementation.**
