# Skill: Query Conventions (Vite + TS + TanStack Query)

## Purpose

Enforce consistent data-layer conventions for the Admin app.

These rules are **mandatory** whenever writing `useQuery` / `useMutation` and related API calls.

---

## Key Rules (Must Follow)

### 1) Co-locate when used in one place (inside `api/`)

If a query/mutation is used by only one page/feature:

- Place it under `features/<feature>/api/`
- Keep **hook + request function + local types** in the **same file**
- Do NOT create separate `types/` or `constants/` folders prematurely

✅ Good:

- `features/admin-complaint/api/complaintsList.query.ts` contains:
  - `useAdminComplaintsQuery`
  - `fetchAdminComplaints`
  - local request/response types only needed here

❌ Avoid:

- Splitting into `features/<feature>/api/*` + `features/<feature>/types/*` for single-use code
- Creating deep folder hierarchies for one hook

### 2) Extract only when reused (2+ call sites)

Extract shared code only when:

- A request function or type is used in **2+ locations**, or
- The type belongs to a stable cross-feature domain model

When extracting:

- Put shared query keys in `admin/src/shared/queryKeys.ts`
- Put shared API client logic in `admin/src/shared/api/*`
- Put shared types in `admin/src/shared/types/*` (only if truly shared)

### 3) Query keys are centralized

All query keys must be defined in:

- `admin/src/shared/queryKeys.ts`

Hooks must reference keys from this module.
No ad-hoc keys inside feature files.

### 4) Deterministic keys + stable params

- Query keys must be deterministic and serializable
- Use a **single params object** and keep its shape stable
- Prefer explicit params (avoid spreading random objects)

### 5) Error handling must be normalized

- All requests must go through the shared API client
- UI must handle loading/empty/error states explicitly

### 6) Invalidate strategy

After mutations:

- Prefer invalidating only the minimal affected keys
- If unsure, invalidate the list + detail keys for the entity

---

## Checklist (Before You Finish)

- [ ] Key is defined in `admin/src/shared/queryKeys.ts`
- [ ] Hook file lives under `features/<feature>/api/`
- [ ] Co-location respected (single usage → single file includes hook+fetch+local types)
- [ ] Loading/empty/error states exist
- [ ] Mutation invalidates correct keys
