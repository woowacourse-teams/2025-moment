---
name: query-conventions
description: TanStack Query patterns and code templates for this admin codebase. Apply when writing useQuery or useMutation hooks.
user-invocable: false
---

## File Location

All query/mutation hooks go in `features/<entity>/api/`.

## Query Keys

Centralized in `shared/api/queryKeys.ts`. Always add new keys there:

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

## Mutation Hook Template

```typescript
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { apiClient } from "@shared/api/client";
import { queryKeys } from "@shared/api/queryKeys";

export function useDeleteUserMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ userId, reason }: { userId: string; reason: string }) =>
      apiClient.delete(`/admin/users/${userId}`, { data: { reason } }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.users.all });
    },
  });
}
```

## Invalidation Strategy

| Action        | Invalidate                        |
|---------------|-----------------------------------|
| Delete item   | `queryKeys.<entity>.all`          |
| Update item   | `queryKeys.<entity>.all`          |
| Update detail | `queryKeys.<entity>.detail(id)`   |

## Checklist

- [ ] Key defined in `shared/api/queryKeys.ts`
- [ ] Hook in `features/<entity>/api/`
- [ ] Mutation invalidates correct keys
- [ ] Mutation includes `reason` param for destructive actions
