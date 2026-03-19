---
paths:
  - "src/**/*.tsx"
---

# Admin UI Rules

## Styling (Emotion)

- Use `@emotion/styled` for all styling
- Separate styles into `*.styles.ts` in the same `ui/` directory
- Use transient props (`$variant`) to avoid passing to DOM

## Component Structure

**List Page Layout**:
- Header (title + actions)
- SearchFilter
- Table
- Pagination

**UI components must be pure**: receive data and handlers as props, no internal data fetching.

## Authorization

- Use `useAuth()` to get `user.role`
- **VIEWER**: Hide/disable mutation buttons
- **ADMIN**: Full access
- Handle 401/403 gracefully

## Destructive Actions

All destructive actions (Delete, Ban) must:
1. Show ConfirmModal
2. Require reason input
3. Log to Audit API
