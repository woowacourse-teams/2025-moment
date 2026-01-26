# Skill: Refactor Logic Separation

## Purpose

Refactor a "fat" UI component by extracting business logic, state, and effects into a dedicated custom hook.
This enforces the **Separation of Concerns** rule in `CLAUDE.md`.

---

## Input

- Target component file (e.g., `GroupList.tsx`)
- (Optional) Desired hook name (default: `use<ComponentName>Logic`)

---

## Process

1.  **Analyze**:
    - Identify **UI** (JSX, pure rendering).
    - Identify **Logic** (`useState`, `useEffect`, `useQuery`, event handlers, data transformations).

2.  **Extract**:
    - Move all Logic into a new custom hook file.
    - Ensure the hook returns _only_ what the UI needs (data + handlers).

3.  **Simplify**:
    - Replace the logic in the original component with the hook call.
    - The component should become a "dumb" presenter.

---

## Output Rules

### 1. The Logic Hook (`useXLogic.ts`)

- Must contain **ALL** `useEffect`, `useQuery`, and complex `useState`.
- Must return a clean interface for the view.
- **Pattern**:
  ```typescript
  export const useGroupListLogic = () => {
    // ... logic ...
    return {
      state: { data, isLoading, isError }, // Data
      handlers: { handleCreate, handleDelete }, // Actions
    };
  };
  ```

### 2. The UI Component (`X.tsx`)

- Must NOT contain `useEffect`.
- Must merely bind the hook's return values to tags.
- **Pattern**:
  ```tsx
  export const GroupList = () => {
    const { state, handlers } = useGroupListLogic();

    if (state.isLoading) return <Loader />;
    return <div onClick={handlers.handleCreate}>...</div>;
  };
  ```

---

## Checklist

- [ ] Original component represents **View only**.
- [ ] No business logic remains in `.tsx`.
- [ ] Hook is co-located in the same directory (or `hooks/` subdir).
- [ ] Types are properly exported and shared.
