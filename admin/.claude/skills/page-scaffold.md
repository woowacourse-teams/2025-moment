# Skill: Page Scaffold

Generate starter skeleton for new Admin pages.

---

## Input

- Page name and route
- Entity (users, groups, moments)
- Page type: List / Detail

---

## List Page Output

**Files to generate:**
1. `pages/<Entity>ListPage.tsx` - Page component
2. `features/<entity>/hooks/use<Entity>List.ts` - Logic hook
3. `features/<entity>/ui/<Entity>Table.tsx` + styles
4. `features/<entity>/ui/<Entity>SearchFilter.tsx` + styles

**Page Template:**
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

  return (
    <Container>
      <Header><Title><Entity> Management</Title></Header>
      <<Entity>SearchFilter ... />
      <<Entity>Table ... />
      <Pagination ... />
    </Container>
  );
}
```

**Logic Hook Template:**
```typescript
export function use<Entity>List() {
  const [page, setPage] = useState(0);
  const [keyword, setKeyword] = useState("");
  const [searchKeyword, setSearchKeyword] = useState("");

  const { data, isLoading, isError } = use<Entity>sQuery({
    page, size: 20, keyword: searchKeyword,
  });

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

## Detail Page Output

**Files to generate:**
1. `pages/<Entity>DetailPage.tsx`
2. `features/<entity>/hooks/use<Entity>Detail.ts`
3. `features/<entity>/ui/<Entity>DetailCard.tsx` + styles
4. `features/<entity>/ui/<Entity>EditModal.tsx` + styles
5. `features/<entity>/ui/<Entity>DeleteModal.tsx` + styles

---

## Rules

- Pages are thin (composition only)
- Logic lives in `features/<entity>/hooks/`
- Styles in separate `*.styles.ts` files
