---
name: refactor-logic
description: UI 컴포넌트에서 비즈니스 로직을 features/<entity>/hooks/의 커스텀 훅으로 추출합니다.
argument-hint: <컴포넌트-파일>
disable-model-invocation: true
---

다음 컴포넌트에서 비즈니스 로직을 추출하세요: $ARGUMENTS

대상 컴포넌트를 읽고, 모든 상태(state), 이펙트(effects), 쿼리 호출을 파악한 뒤 커스텀 훅으로 이동하세요.

## 목표 구조

| 컨텍스트      | 훅 파일                      | 훅 이름             |
|--------------|------------------------------|---------------------|
| 목록 페이지   | `use<Entity>List.ts`         | `use<Entity>List`   |
| 상세 페이지   | `use<Entity>Detail.ts`       | `use<Entity>Detail` |
| 수정 모달     | `use<Entity>Edit.ts`         | `use<Entity>Edit`   |

위치: `features/<entity>/hooks/`

## 목록 훅 템플릿

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

## 상세 훅 템플릿

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

## 체크리스트

- [ ] 훅이 `features/<entity>/hooks/`에 위치하는지 확인
- [ ] 추출 후 UI 컴포넌트에 `useState`, `useEffect`, `useQuery`가 없는지 확인
- [ ] 훅이 필요한 데이터와 핸들러만 반환하는지 확인 (raw 쿼리 객체 노출 금지)
