# Admin Requirements

## Admin Personas

### OWNER

- Full access
- Can manage admins and system-level actions

### MANAGER

- Can moderate content and users
- Cannot manage admin roles

### VIEWER

- Read-only access
- No destructive actions

---

## Core User Stories

### User Management

- As an admin, I can view all users with filters and pagination
- As an admin, I can ban/unban a user with a reason
- As an admin, I can see a user's activity summary

### Group Management

- As an admin, I can view all groups
- As an admin, I can delete an inappropriate group

### Content Management

- As an admin, I can view all moments globally
- As an admin, I can delete a moment or comment

### Complaint Management

- As an admin, I can view reported content
- As an admin, I can resolve complaints with predefined actions

---

## Acceptance Criteria (Examples)

- All destructive actions require confirmation
- All state changes create an audit log entry
- Unauthorized roles cannot see or trigger restricted actions
- Lists must support search, filter, and pagination

---

## Non-Functional Requirements

- RBAC enforced at UI and API level
- Actions must be idempotent
- UI must prevent accidental destructive actions
- Admin errors must be human-readable
