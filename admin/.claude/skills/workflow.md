# Skill: Full Development Workflow

End-to-end development cycle from requirements to PR.

---

## Overview

```
Step 1: Requirements → User Approval ⏸️
Step 2: Implementation
Step 3: Validation
Step 4: Commit
Step 5: PR Creation → PR URL ✅
```

---

## Steps

### Step 1: Requirements
**Skill**: `feature-kickoff.md`
- Write implementation plan
- **Gate**: User approval required

### Step 2: Implementation
**Skills**: `page-scaffold.md`, `query-conventions.md`, `refactor-logic.md`
- Write code following FSD
- Order: `shared/` → `features/` → `pages/`

### Step 3: Validation
**Skill**: `validation.md`
- Run: `pnpm run lint && tsc --noEmit && pnpm run test`
- Auto-fix up to 3 attempts

### Step 4: Commit
**Skill**: `smart-commit.md`
- Atomic commits, Korean messages

### Step 5: PR Creation
**Skill**: `smart-pr.md`
- Korean PR in template format
- Run `gh pr create`

---

## Error Handling

| Situation | Action |
|-----------|--------|
| Validation fail | Auto-fix → retry (max 3) |
| 3 failures | Report to user |
| Git conflict | Report to user |
