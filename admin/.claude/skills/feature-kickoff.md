# Skill: Feature Kickoff (Admin)

## Purpose

Create a high-quality implementation plan **before coding**.
This skill outputs:

- scope and acceptance criteria
- file-level implementation plan
- PR slicing plan (small, reviewable PRs)
- edge cases + guardrails
- test plan (unit + E2E)
- docs update checklist

Do NOT write code in this step unless explicitly requested.

---

## Input

Provide (or infer) the following:

- Feature name (e.g., "Complaint moderation")
- Target roles: ADMIN / VIEWER behaviors
- Entity endpoints involved (from `docs/admin/api-contract.md`)
- MVP scope vs out-of-scope
- Any deadlines or constraints (optional)

---

## Output Format (STRICT)

### 1) Scope

- In scope:
- Out of scope:

### 2) User Stories & Acceptance Criteria

- Story 1:
  - AC:
- Story 2:
  - AC:

(Keep AC testable and unambiguous.)

### 3) UI/UX Notes

- States: loading / empty / error
- Guardrails (confirm + reason) for destructive actions
- What VIEWER can see vs cannot do

### 4) Data & Query Plan

- Which query keys will be added (names only)
- Query params (search/filter/sort/pagination)
- Mutation invalidation strategy

### 5) File Plan

List exact files to create/modify, grouped by:

- pages/
- features/
- shared/
- docs/

### 6) PR Plan (Small & Scoped)

Propose 2–4 PRs maximum.
For each PR:

- Goal
- Files touched
- Validation

### 7) Edge Cases / Risks

- e.g., 401/403, already-resolved, concurrent updates, stale lists
- performance risks (large lists) and mitigations

### 8) Tests

- Unit (Jest):
- E2E (Cypress):
  - Scenario 1:
  - Scenario 2:

### 9) Docs Updates Checklist

- [ ] docs/admin/requirements.md
- [ ] docs/admin/architecture.md
- [ ] docs/admin/api-contract.md
- [ ] docs/admin/test-plan.md

---

## Rules

- Base all API assumptions on `docs/admin/api-contract.md`
- Keep the plan realistic for a frontend-only implementation
- If backend is missing, explicitly propose MSW mocking
- Avoid large refactors; prefer incremental PRs
