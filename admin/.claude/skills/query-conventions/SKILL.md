---
name: query-conventions
description: 이 어드민 코드베이스의 TanStack Query 패턴 및 코드 템플릿입니다. useQuery 또는 useMutation 훅을 작성할 때 적용하세요.
user-invocable: false
---

## 파일 위치

모든 쿼리/뮤테이션 훅은 `features/<entity>/api/`에 위치합니다.

## 쿼리 키 (Query Keys)

`shared/api/queryKeys.ts`에 중앙 집중 관리합니다. 새 키는 항상 여기에 추가하세요:

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

## 쿼리 훅 템플릿

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

## 뮤테이션 훅 템플릿

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

## 캐시 무효화 전략

| 동작            | 무효화 대상                               |
|----------------|------------------------------------------|
| 항목 삭제       | `queryKeys.<entity>.all`                 |
| 항목 수정       | `queryKeys.<entity>.all`                 |
| 상세 정보 수정  | `queryKeys.<entity>.detail(id)`          |

## 체크리스트

- [ ] `shared/api/queryKeys.ts`에 키 정의 완료
- [ ] `features/<entity>/api/`에 훅 위치
- [ ] 뮤테이션이 올바른 키를 무효화하는지 확인
- [ ] 파괴적 동작에 `reason` 파라미터 포함 여부 확인
