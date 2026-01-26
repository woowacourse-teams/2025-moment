# Skill: Page Scaffold (Admin)

## Purpose

Generate a clean, consistent starting scaffold for a new Admin page.

A scaffold is a **starter skeleton** (layout + states + TODOs) that:

- compiles immediately
- includes loading/empty/error states
- provides a predictable place to plug in hooks and UI components

Do NOT implement full business logic unless explicitly asked.

---

## Input (What you must ask for / infer)

- Page name and route (e.g., `Complaints`, `/admin/complaints`)
- Entity (users, groups, moments, complaints)
- Page type: `List` or `Detail`
- Whether actions exist (ADMIN-only mutations) or read-only (VIEWER)

---

## Output (What to generate)

### For List Pages

Generate:

1. `Page` component (route-level composition only)
2. Basic UI layout:
   - Title + short description
   - Filter/Search bar placeholder
   - Table/List placeholder
   - Pagination placeholder
3. Data states:
   - Loading
   - Error
   - Empty
4. A TODO section listing what must be wired next:
   - query hook to call
   - columns / row actions
   - filters to implement
   - URL query sync (optional)

### For Detail Pages

Generate:

1. `Page` component
2. Summary header
3. Sections placeholders (Overview, Activity, Actions)
4. Data states
5. TODOs for actions + audit log view

---

## Required UI State Conventions

- Loading: show a skeleton or simple loading block
- Error: show message + retry button
- Empty: show empty state copy

---

## Output Example Structure (List Page)

- `pages/admin/<entity>/<Entity>ListPage.tsx`
  - Uses a hook (placeholder import)
  - Renders `Filters`, `Table`, `Pagination`
  - Handles loading/error/empty
  - Contains TODO comments for wiring

---

## Rules

- Pages must remain thin (composition only)
- Domain logic must live in `features/*`
- Use shared components if available; otherwise create minimal placeholders
- Keep code TypeScript-safe with explicit types
