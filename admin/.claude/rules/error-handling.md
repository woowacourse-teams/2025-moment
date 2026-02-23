---
paths:
  - "src/**/*.{ts,tsx}"
---

# Error Handling Rules

## API Errors

Global interceptor in `shared/api/client.ts`:
- **401**: Redirect to `/login`
- **403**: Show permission denied message
- **4xx/5xx**: Normalize error format

## Component States

Every data-fetching component must handle:

```tsx
if (isLoading) return <Loading />;
if (isError) return <Error onRetry={refetch} />;
if (!data?.length) return <Empty />;
return <Content data={data} />;
```

## Mutation Errors

- Show toast/alert with error message
- Do NOT close modal on error
- Log error details for debugging

## Error Boundaries

- Wrap pages with ErrorBoundary
- Provide fallback UI with retry option
