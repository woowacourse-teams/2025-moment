---
name: refactor-logic
description: Extract business logic from UI components into custom hooks in features/<entity>/hooks/.
argument-hint: <component-file>
disable-model-invocation: true
---

Extract business logic from: $ARGUMENTS

Read the target component, identify all state, effects, and query calls, then move them into a custom hook.

## Target Structure

| Context     | Hook file                    | Hook name         |
|-------------|------------------------------|-------------------|
| List Page   | `use<Entity>List.ts`         | `use<Entity>List` |
| Detail Page | `use<Entity>Detail.ts`       | `use<Entity>Detail` |
| Edit Modal  | `use<Entity>Edit.ts`         | `use<Entity>Edit` |

Location: `features/<entity>/hooks/`

## List Hook Template

```typescript
export function useUserList() {
  const [page, setPage] = useState(0);
  const [keyword, setKeyword] = useState("");
  const [searchKeyword, setSearchKeyword] = useState("");

  const { data, isLoading, isError } = useUsersQuery({ page, size: 20, keyword: searchKeyword });

  return {
    users: data?.content ?? [],
    totalPages: data?.totalPages ?? 0,
    currentPage: page,
    isLoading, isError,
    keyword, setKeyword,
    handleSearch: () => { setSearchKeyword(keyword); setPage(0); },
    handlePageChange: setPage,
  };
}
```

## Detail Hook Template

```typescript
export function useGroupDetail(groupId: string) {
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);

  const { data: group, isLoading, isError } = useGroupDetailQuery(groupId);
  const deleteGroupMutation = useDeleteGroupMutation();

  return {
    group, isLoading, isError,
    isDeleting: deleteGroupMutation.isPending,
    isEditModalOpen,
    handleOpenEditModal: () => setIsEditModalOpen(true),
    handleCloseEditModal: () => setIsEditModalOpen(false),
    isDeleteModalOpen,
    handleOpenDeleteModal: () => setIsDeleteModalOpen(true),
    handleCloseDeleteModal: () => setIsDeleteModalOpen(false),
    handleDelete: async (reason: string) => {
      await deleteGroupMutation.mutateAsync({ groupId, reason });
    },
  };
}
```

## Checklist

- [ ] Hook lives in `features/<entity>/hooks/`
- [ ] UI component has no `useState`, `useEffect`, or `useQuery` after extraction
- [ ] Hook returns only needed data and handlers (no raw query objects)
