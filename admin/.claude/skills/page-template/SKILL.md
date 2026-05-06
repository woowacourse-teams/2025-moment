---
name: page-template
description: FSD 아키텍처를 따르는 새 어드민 페이지의 기본 파일 구조를 생성합니다. 새 목록 또는 상세 페이지를 구현할 때 사용하세요.
argument-hint: <entity> <list|detail>
disable-model-invocation: true
---

다음 어드민 페이지 스캐폴드를 생성하세요: $ARGUMENTS

인수를 `<entity> <list|detail>` 형식으로 파싱하세요. 페이지 타입이 생략된 경우 두 가지 모두 생성하세요.

---

## 목록 페이지 (List Page)

**생성할 파일:**
1. `pages/<Entity>ListPage.tsx`
2. `features/<entity>/hooks/use<Entity>List.ts`
3. `features/<entity>/ui/<Entity>Table.tsx` + `<Entity>Table.styles.ts`
4. `features/<entity>/ui/<Entity>SearchFilter.tsx` + `<Entity>SearchFilter.styles.ts`

**페이지 템플릿:**
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
      <Header><Title><Entity> 관리</Title></Header>
      <<Entity>SearchFilter keyword={keyword} onKeywordChange={setKeyword} onSearch={handleSearch} />
      <<Entity>Table <entities>={<entities>} isAdmin={isAdmin} />
      <Pagination currentPage={currentPage} totalPages={totalPages} onPageChange={handlePageChange} />
    </Container>
  );
}
```

**로직 훅 템플릿:**
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

## 상세 페이지 (Detail Page)

**생성할 파일:**
1. `pages/<Entity>DetailPage.tsx`
2. `features/<entity>/hooks/use<Entity>Detail.ts`
3. `features/<entity>/ui/<Entity>DetailCard.tsx` + styles
4. `features/<entity>/ui/<Entity>EditModal.tsx` + styles
5. `features/<entity>/ui/<Entity>DeleteModal.tsx` + styles

---

## 규칙

- 페이지는 얇게 유지 (조합만 담당 — useState, useQuery를 직접 쓰지 않음)
- 로직은 `features/<entity>/hooks/`에 위치
- 스타일은 별도 `*.styles.ts` 파일로 분리 (인라인 스타일 금지)
- 훅 작성 전에 `shared/api/queryKeys.ts`에 쿼리 키를 먼저 추가할 것
