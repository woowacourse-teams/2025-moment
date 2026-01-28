# Admin Architecture

## Information Architecture (IA)

- Dashboard
- Users
- Groups
- Moments
- Complaints
- Audit Logs

---

## RBAC Model

| Role    | View | Modify | Delete     | Bulk Actions |
| ------- | ---- | ------ | ---------- | ------------ |
| OWNER   | ✅   | ✅     | ✅         | ✅           |
| MANAGER | ✅   | ✅     | ⚠️ Limited | ⚠️ Limited   |
| VIEWER  | ✅   | ❌     | ❌         | ❌           |

---

## Audit Log Schema

Every action must generate a log entry:

```ts
{
  actorId: string
  actorRole: string
  actionType: string
  targetType: 'USER' | 'GROUP' | 'MOMENT' | 'COMMENT'
  targetId: string
  before?: object
  after?: object
  reason?: string
  createdAt: ISODateString
}
```

## Data Flow

Admin UI
→ Shared API Client
→ Admin API
→ Audit Log Service

## Error Handling

All errors must be normalized into:

```ts
{
  code: string
  message: string
  fieldErrors?: Record<string, string>
  traceId?: string
}
```
