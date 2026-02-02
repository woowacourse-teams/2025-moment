# Skill: Query Conventions

Rules for TanStack Query hooks.

---

## File Naming

| Type | Pattern | Example |
|------|---------|---------|
| List Query | `use<Entity>sQuery.ts` | `useUsersQuery.ts` |
| Detail Query | `use<Entity>DetailQuery.ts` | `useUserDetailQuery.ts` |
| Nested Query | `use<Parent><Child>Query.ts` | `useGroupMembersQuery.ts` |
| Mutation | `use<Action><Entity>Mutation.ts` | `useDeleteUserMutation.ts` |

## Location

All hooks in `features/<entity>/api/`

---

## Query Keys

Centralized in `shared/api/queryKeys.ts`:

```typescript
export const queryKeys = {
  users: {
    all: ["users"] as const,
    lists: () => [...queryKeys.users.all, "list"] as const,
    list: (filters: Record<string, unknown>) =>
      [...queryKeys.users.lists(), filters] as const,
    details: () => [...queryKeys.users.all, "detail"] as const,
    detail: (id: string) => [...queryKeys.users.details(), id] as const,
  },
};
```

---

## Query Hook Template

```typescript
import { useQuery } from "@tanstack/react-query";
import { apiClient } from "@shared/api/client";
import { queryKeys } from "@shared/api/queryKeys";

async function fetchUsers(params: UserListParams) {
  const { data } = await apiClient.get("/admin/users", { params });
  return data;
}

export function useUsersQuery(params: UserListParams) {
  return useQuery({
    queryKey: queryKeys.users.list(params),
    queryFn: () => fetchUsers(params),
  });
}
```

---

## Mutation Hook Template

```typescript
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { apiClient } from "@shared/api/client";
import { queryKeys } from "@shared/api/queryKeys";

export function useDeleteUserMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ userId, reason }) =>
      apiClient.delete(`/admin/users/${userId}`, { data: { reason } }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.users.all });
    },
  });
}
```

---

## Invalidation Strategy

| Action | Invalidate |
|--------|------------|
| Delete item | `queryKeys.<entity>.all` |
| Update item | `queryKeys.<entity>.all` |
| Update detail | `queryKeys.<entity>.detail(id)` |

---

## Checklist

- [ ] Key defined in `shared/api/queryKeys.ts`
- [ ] Hook in `features/<entity>/api/`
- [ ] Mutation invalidates correct keys
