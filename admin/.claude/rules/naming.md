# 네이밍 규칙

## 파일명

| 용도 | 패턴 | 예시 |
|------|------|------|
| 쿼리 훅 | `use<Entity>Query.ts` | `useUsersQuery.ts` |
| 상세 쿼리 훅 | `use<Entity>DetailQuery.ts` | `useUserDetailQuery.ts` |
| 뮤테이션 훅 | `use<Action><Entity>Mutation.ts` | `useDeleteUserMutation.ts` |
| 로직 훅 | `use<Entity><Context>.ts` | `useUserList.ts` |
| 컴포넌트 | `<Entity><Type>.tsx` | `UserTable.tsx` |
| 스타일 | `<Component>.styles.ts` | `UserTable.styles.ts` |
| 타입 | `<entity>.ts` | `user.ts` |

## 변수 & 함수

- **컴포넌트**: PascalCase (`UserTable`)
- **훅**: camelCase + `use` 접두사 (`useUserList`)
- **이벤트 핸들러**: camelCase + `handle` 접두사 (`handleDelete`)
- **불리언**: `is`/`has` 접두사 (`isLoading`, `hasError`)
- **상수**: UPPER_SNAKE_CASE (`DEFAULT_PAGE_SIZE`)

## 쿼리 키

`shared/api/queryKeys.ts`에 계층 구조로 관리:
```typescript
queryKeys.users.all          // ["users"]
queryKeys.users.list(params) // ["users", "list", params]
queryKeys.users.detail(id)   // ["users", "detail", id]
```
