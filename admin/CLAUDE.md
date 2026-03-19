# Admin CLAUDE.md

Development guide for Moment Admin Panel. **Correctness and traceability** are top priorities.

---

## Core Principles

1. **Authorization**: ADMIN (full access) / VIEWER (read-only)
2. **Audit Log**: Log all state-changing actions (actor, target, action, reason)
3. **Safety**: Destructive actions require ConfirmModal + reason

---

## Architecture (FSD)

**Dependency flow**: `app` → `pages` → `widgets` → `features` → `entities` → `shared`

| Layer | Role | Structure |
|-------|------|-----------|
| `pages/` | Page composition | `<Entity>ListPage.tsx`, `<Entity>DetailPage.tsx` |
| `features/` | Business logic | `api/`, `hooks/`, `ui/`, `types/`, `index.ts` |
| `shared/` | Common infrastructure | `api/`, `auth/`, `ui/` |

**Feature internal structure**:
```
features/<entity>/
├── api/          # useXQuery.ts, useXMutation.ts
├── hooks/        # useXList.ts, useXDetail.ts, useXEdit.ts
├── ui/           # Component.tsx + Component.styles.ts
├── types/        # entity.ts
└── index.ts      # Public API exports
```

---

## Conventions

### File Naming
| Purpose | Pattern | Example |
|---------|---------|---------|
| Query Hook | `use<Entity>Query.ts` | `useUsersQuery.ts` |
| Mutation Hook | `use<Action><Entity>Mutation.ts` | `useDeleteUserMutation.ts` |
| Logic Hook | `use<Entity><Context>.ts` | `useUserList.ts` |
| UI Component | `<Entity><Type>.tsx` | `UserTable.tsx` |
| Style | `<Component>.styles.ts` | `UserTable.styles.ts` |

### Query Keys
Centralized in `shared/api/queryKeys.ts`:
```typescript
queryKeys.<entity>.list(params)
queryKeys.<entity>.detail(id)
```

---

## Requirements Checklist

- [ ] Auth check (`useAuth` → `user.role`)
- [ ] Handle Loading / Error / Empty states
- [ ] Destructive actions → ConfirmModal + reason
- [ ] Styles separated into `*.styles.ts`

---

## Tech Stack

- React + TypeScript + Vite
- TanStack Query (server state)
- Emotion (styling)
- React Router (routing)
