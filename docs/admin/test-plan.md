# Admin Test Plan

## Unit Tests (Jest)

- RBAC permission checks
- Query parameter builders
- Error normalization logic

---

## Integration Tests

- List fetching with filters (mocked API)
- Mutation success/failure handling

---

## E2E Tests (Cypress)

### Scenario 1: Bulk Complaint Resolution

1. Login as MANAGER
2. Navigate to Complaints
3. Filter by PENDING
4. Select multiple complaints
5. Resolve as DELETE_CONTENT
6. Verify success message and updated status

### Scenario 2: Permission Denial

1. Login as VIEWER
2. Navigate to Users
3. Attempt to ban a user
4. Verify action is blocked and error shown

---

## Regression Policy

- Any bug fix must include a test
- Any new admin feature must extend this test plan
