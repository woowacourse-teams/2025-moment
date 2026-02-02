# Naming Conventions

## Files

| Type | Pattern | Example |
|------|---------|---------|
| Query Hook | `use<Entity>Query.ts` | `useUsersQuery.ts` |
| Detail Query | `use<Entity>DetailQuery.ts` | `useUserDetailQuery.ts` |
| Mutation Hook | `use<Action><Entity>Mutation.ts` | `useDeleteUserMutation.ts` |
| Logic Hook | `use<Entity><Context>.ts` | `useUserList.ts` |
| Component | `<Entity><Type>.tsx` | `UserTable.tsx` |
| Styles | `<Component>.styles.ts` | `UserTable.styles.ts` |
| Types | `<entity>.ts` | `user.ts` |

## Variables & Functions

- **Components**: PascalCase (`UserTable`)
- **Hooks**: camelCase with `use` prefix (`useUserList`)
- **Handlers**: camelCase with `handle` prefix (`handleDelete`)
- **Boolean**: `is`/`has` prefix (`isLoading`, `hasError`)
- **Constants**: UPPER_SNAKE_CASE (`DEFAULT_PAGE_SIZE`)

## Query Keys

Hierarchical structure in `shared/api/queryKeys.ts`:
```typescript
queryKeys.users.all          // ["users"]
queryKeys.users.list(params) // ["users", "list", params]
queryKeys.users.detail(id)   // ["users", "detail", id]
```
