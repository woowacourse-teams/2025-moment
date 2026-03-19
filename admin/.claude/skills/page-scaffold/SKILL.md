---
name: page-scaffold
description: Generate starter file skeleton for new Admin pages following FSD architecture. Use when implementing a new list or detail page.
argument-hint: <entity> <list|detail>
disable-model-invocation: true
---

Generate Admin page scaffold for: $ARGUMENTS

Parse the arguments as `<entity> <list|detail>`. If the page type is omitted, generate both.

---

## List Page

**Files to create:**
1. `pages/<Entity>ListPage.tsx`
2. `features/<entity>/hooks/use<Entity>List.ts`
3. `features/<entity>/ui/<Entity>Table.tsx` + `<Entity>Table.styles.ts`
4. `features/<entity>/ui/<Entity>SearchFilter.tsx` + `<Entity>SearchFilter.styles.ts`

**Page template:**
```tsx
export default function <Entity>ListPage() {
  const { user } = useAuth();
  const isAdmin = user?.role === "ADMIN";

  const {
    <entities>, totalPages, currentPage,
    isLoading, isError,
    keyword, setKeyword, handleSearch, handlePageChange,
  } = use<Entity>List();

  if (isLoading) return <Loading />;
  if (isError) return <Error />;
  if (!<entities>.length) return <Empty />;

  return (
    <Container>
      <Header><Title><Entity> Management</Title></Header>
      <<Entity>SearchFilter keyword={keyword} onKeywordChange={setKeyword} onSearch={handleSearch} />
      <<Entity>Table <entities>={<entities>} isAdmin={isAdmin} />
      <Pagination currentPage={currentPage} totalPages={totalPages} onPageChange={handlePageChange} />
    </Container>
  );
}
```

**Logic hook template:**
```typescript
export function use<Entity>List() {
  const [page, setPage] = useState(0);
  const [keyword, setKeyword] = useState("");
  const [searchKeyword, setSearchKeyword] = useState("");

  const { data, isLoading, isError } = use<Entity>sQuery({ page, size: 20, keyword: searchKeyword });

  return {
    <entities>: data?.content ?? [],
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

## Detail Page

**Files to create:**
1. `pages/<Entity>DetailPage.tsx`
2. `features/<entity>/hooks/use<Entity>Detail.ts`
3. `features/<entity>/ui/<Entity>DetailCard.tsx` + styles
4. `features/<entity>/ui/<Entity>EditModal.tsx` + styles
5. `features/<entity>/ui/<Entity>DeleteModal.tsx` + styles

---

## Rules

- Pages are thin (composition only — no useState, useQuery inside)
- Logic lives in `features/<entity>/hooks/`
- Styles in separate `*.styles.ts` files (never inline)
- Add query keys to `shared/api/queryKeys.ts` before writing hooks
