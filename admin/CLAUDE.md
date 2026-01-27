# Admin CLAUDE.md

This document defines strict rules and goals for building the **Moment Admin Panel**.
Claude must follow this file when working inside `/admin`.

---

## Goal

Build an **operational, production-grade Admin Panel** for Moment that prioritizes:

- Safety
- Traceability
- Bulk operations
- Clear authorization boundaries

Speed matters, but **correctness and auditability are mandatory**.

---

## MVP Required Features (Non-negotiable)

Every Admin feature must support:

1. **Authorization (ADMIN / VIEWER)**
   - Roles: **ADMIN**, **VIEWER**
   - **Route-level guard**: Admin pages require authenticated admin access
   - **Action-level guard**:
     - **VIEWER** is read-only (no mutations, no bulk actions)
     - **ADMIN** can perform mutations and bulk actions
   - Unauthorized actions must be **hidden/disabled** and handle **401/403** gracefully

2. **Audit Log**
   - Every destructive or state-changing action must be logged
   - Required fields: actor, target, action, before/after, timestamp, reason
   - Audit logs are mandatory for:
     - user status changes (ban/unban/withdraw)
     - group deletion
     - moment/comment deletion
     - complaint resolution
     - any bulk action

3. **List UX Standards**
   - Search
   - Filter
   - Sort
   - Pagination

4. **Bulk Actions**
   - Multi-select + batch operations
   - Confirmation required
   - **ADMIN only**

5. **Danger Guardrails**
   - Confirmation modal
   - Reason input
   - Clear irreversible action warning
   - **ADMIN only**

---

## Architecture Rules (Feature-Sliced Design)

Strictly follow the **Feature-Sliced Design (FSD)** methodology. Dependency flow must be **unidirectional** (Top → Bottom). Lower layers **cannot** import from higher layers.

### [L0] `app/` (Application Layer)

- **Role**: App initialization, global providers (React Query, Emotion), and global styling.
- **Rule**: Minimal logic. Composition only.

### [L1] `pages/` (Page Layer)

- **Role**: Composition of widgets and features into full pages.
- **Rule**: Routing and data fetching orchestration. No direct business logic.

### [L2] `widgets/` (Composition Layer)

- **Role**: Large, reusable self-contained blocks (e.g., `Header`, `Sidebar`, `UserTable`).
- **Rule**: Combines features and entities. Shared across pages.

### [L3] `features/` (Action Layer)

- **Role**: User interactions and business processes that provide value (e.g., `UpdateUserStatus`, `BulkDeleteGroups`).
- **Structure (Strict)**:
  - `ui/`: Pure presentational components. Includes `*.styles.ts`. No `useEffect` or complex state.
  - `model/`: Logic hooks (`useX.ts`). State, handlers, mutations, and side effects.
  - `api/`: React Query hooks (`useQuery`, `useMutation`).
  - `types/`: Feature-scoped TypeScript definitions.
  - `index.ts`: **Public API**. Only export what is needed externally.
- **Rules**: Features can import from `entities` and `shared`.

### [L4] `entities/` (Business Layer)

- **Role**: Business domain entities (e.g., `User`, `Group`, `Complaint`).
- **Structure**: `index.ts`, `api/`, `model/`, `ui/`.
- **Rules**: Entities **cannot** import from `features` or `widgets`.

### [L5] `shared/` (Infrastructure Layer)

- **Role**: Reusable infrastructure and UI kit.
- **Contents**: `api/` (Axios client, query keys), `auth/` (Providers, Guards), `ui/` (Design System), `lib/` (Utils).
- **Rules**: Purest layer. No business logic. No imports from higher layers.

---

## Technical Standards

- **Unidirectional Flow**: `app` → `pages` → `widgets` → `features` → `entities` → `shared`.
- **Public API**: Each slice MUST have an `index.ts` to export its public interface. Crossing-slice imports must only use the public API.
- **Logic Separation**: Keep Emotion styles in `ui/*.styles.ts`. Keep business logic in `model/` or dedicated hooks.
- **API Management**: Use TanStack React Query for all server state. Hierarchical query keys are mandatory and should be stored in `shared/api/queryKeys.ts`.

---

## Styling Rules (Emotion)

- Styling must use **Emotion**.
- Style code must be separated into `*.styles.ts` files and placed **inside the same `ui/` directory**.
- Keep `.tsx` focused on markup and composition.
- Avoid large inline styled blocks inside `.tsx`.

---

## Data & API Rules

- Use **TanStack React Query**
- Query keys must be deterministic and hierarchical
- All API responses must be normalized through a shared client
- Error format must follow the Admin API contract

---

## Testing Rules (Mandatory)

- **Unit Tests (Jest)**
  - Authorization logic (ADMIN/VIEWER)
  - Validation & parameter builders
  - Error normalization (if applicable)

- **E2E Tests (Cypress)**
  - At least 2 critical admin flows:
    - **Bulk moderation flow (ADMIN)**
    - **Permission denial flow (VIEWER)**

No feature is complete without tests.

---

## Documentation Rules

Before implementing a feature:

- Update `../docs/admin/requirements.md`
- Update `../docs/admin/architecture.md`
- Update `../docs/admin/api-contract.md`
- Update `../docs/admin/test-plan.md`

Documentation is part of the deliverable.

---

## Source of Truth

- `../docs/admin/*` is the source of truth for Admin requirements, architecture, API contract, and test plan.
- If any other document conflicts, follow `../docs/admin/*`.

---

## Pull Request Rules

- PRs must be small and scoped
- Include:
  - Summary
  - Screenshots (if UI)
  - Test results
- Use diff-based AI summaries when possible
