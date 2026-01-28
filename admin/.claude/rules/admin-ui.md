# Admin UI Rules

This document defines specific rules for building the **Moment Admin Panel UI**.

## Styling (Emotion)

- All styling must use **Emotion** (`@emotion/styled`).
- Component logic and styles must be separated.
- Place styles in a `*.styles.ts` file within the same `ui/` directory as the component.
- Example:
  - `features/example/ui/ExampleComponent.tsx`
  - `features/example/ui/ExampleComponent.styles.ts`

## Component Guidelines

- Use shared UI components from `@shared/ui` (e.g., `Button`, `Modal`) to ensure consistency.
- Maintain a consistent layout for list pages:
  - **Header**: Title and primary actions (e.g., "Create")
  - **Filters**: Search and filter controls
  - **Table**: Data display with selection and row actions
  - **Pagination**: Standard pagination navigation
- UI components should be pure and presentational. Pass handlers and data as props.

## Logic Separation

- Extract business logic, data fetching, and side effects into custom hooks (`use*.ts`).
- Avoid large component files. A component should focus on _how_ things look, not _what_ they do.

## Safety & Audit Logs

- All destructive or state-changing actions (Delete, Ban, Role Change) must:
  - Require a **Confirmation Modal**.
  - Request a **Reason** for the action.
  - Call the **Audit Log API** to track the change (actor, target, action, reason).
- Display clear warnings for irreversible actions.

## Authorization & Guards

- Use `@shared/auth/AuthGuard` to protect admin-only routes.
- Use the `user.role` from `useAuth` to conditionally render UI:
  - **VIEWER**: Read-only access. Disable or hide mutation buttons (Delete, Create, Edit).
  - **ADMIN**: Full access.
- Gracefully handle `401 Unauthorized` and `403 Forbidden` API responses.
