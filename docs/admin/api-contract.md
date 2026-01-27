# Admin API Contract

## Common Query Parameters

- page
- size
- sort
- q
- status
- dateFrom
- dateTo

---

## Dashboard

GET /admin/dashboard

```json
{
  "totalUserCount": number,
  "totalGroupCount": number,
  "totalMomentCount": number,
  "pendingComplaintCount": number
}
```

## Users

GET /admin/users
GET /admin/users/{userId}
PUT /admin/users/{userId}/status

```json
{
  "status": "BANNED",
  "reason": "..."
}
```

## Groups

GET /admin/groups
DELETE /admin/groups/{groupId}

## Moments

GET /admin/moments
DELETE /admin/moments/{momentId}
DELETE /admin/moments/{momentId}/comments/{commentId}

## Complaints

GET /admin/complaints
POST /admin/complaints/{complaintId}/resolve

```json
{
  "action": "DELETE_CONTENT | BAN_USER | DISMISS"
}
```
