# Skill: Refactor Logic Separation

Extract business logic from UI components into custom hooks.

---

## Input

- Target component file
- Context: List / Detail / Edit

---

## Naming Convention

| Context | File | Hook |
|---------|------|------|
| List Page | `use<Entity>List.ts` | `use<Entity>List` |
| Detail Page | `use<Entity>Detail.ts` | `use<Entity>Detail` |
| Edit Modal | `use<Entity>Edit.ts` | `use<Entity>Edit` |

Location: `features/<entity>/hooks/`

---

## List Hook Template

```typescript
export function useUserList() {
  const [page, setPage] = useState(0);
  const [keyword, setKeyword] = useState("");
  const [searchKeyword, setSearchKeyword] = useState("");

  const { data, isLoading, isError } = useUsersQuery({
    page, size: 20, keyword: searchKeyword,
  });

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

---

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
    // Edit Modal
    isEditModalOpen,
    handleOpenEditModal: () => setIsEditModalOpen(true),
    handleCloseEditModal: () => setIsEditModalOpen(false),
    // Delete Modal
    isDeleteModalOpen,
    handleOpenDeleteModal: () => setIsDeleteModalOpen(true),
    handleCloseDeleteModal: () => setIsDeleteModalOpen(false),
    handleDelete: async (reason: string) => {
      await deleteGroupMutation.mutateAsync({ groupId, reason });
    },
  };
}
```

---

## Edit Hook Template

```typescript
export function useUserEdit({ userId, initialNickname, onSuccess }) {
  const [nickname, setNickname] = useState(initialNickname);
  const updateUserMutation = useUpdateUserMutation();

  useEffect(() => { setNickname(initialNickname); }, [initialNickname]);

  return {
    nickname, setNickname,
    isValid: nickname.trim() && nickname !== initialNickname,
    isPending: updateUserMutation.isPending,
    handleSubmit: async () => {
      await updateUserMutation.mutateAsync({ userId, nickname });
      onSuccess();
    },
  };
}
```

---

## Checklist

- [ ] Hook in `features/<entity>/hooks/`
- [ ] UI component has no `useState`, `useEffect`, `useQuery`
- [ ] Hook returns only needed data and handlers
