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

## Architecture Rules

- `pages/`
  - Routing & composition only
  - No business logic
- `features/`
  - **Separation of Concerns (Strict)**:
    - **UI Components (`.tsx`)**: Pure presentational. Receive data/handlers as props or via hooks. No `useEffect` or complex state.
    - **Logic Hooks (`useX.ts`)**: Container data, side effects, handlers, mutations.
    - **API Hooks (`api/`)**: Pure server state management (React Query).

- `shared/`
  - Design system
  - API client
  - Guards, utilities, error normalization

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

- Update `docs/admin/requirements.md`
- Update `docs/admin/architecture.md`
- Update `docs/admin/api-contract.md`
- Update `docs/admin/test-plan.md`

Documentation is part of the deliverable.

---

## Source of Truth

- `docs/admin/*` is the source of truth for Admin requirements, architecture, API contract, and test plan.
- If any other document conflicts, follow `docs/admin/*`.

---

## Pull Request Rules

- PRs must be small and scoped
- Include:
  - Summary
  - Screenshots (if UI)
  - Test results
- Use diff-based AI summaries when possible
