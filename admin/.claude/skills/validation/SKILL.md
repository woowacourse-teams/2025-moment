---
name: validation
description: Run lint, type-check, and tests. Auto-fix errors up to 3 attempts before reporting.
disable-model-invocation: true
allowed-tools: Bash(pnpm *)
---

Run full validation and auto-fix any errors.

## Command

```bash
pnpm run lint && tsc --noEmit && pnpm run test
```

## Process

1. Run the validation command
2. If it fails, diagnose and fix errors automatically
3. Re-run validation (max 3 attempts total)
4. If still failing after 3 attempts, report to user with full error details

## Auto-fixable Errors

| Type             | Example                                       |
|------------------|-----------------------------------------------|
| Missing import   | `'useState' is not defined`                   |
| Type mismatch    | `Type 'string' not assignable to 'number'`    |
| Unused variable  | `'foo' declared but never used`               |
| Missing return   | `Function lacks ending return statement`      |

## Output

**PASS:**
```
✅ Validation PASSED
- lint: ✓
- tsc: ✓
- test: ✓
```

**FAIL (auto-fixing):**
```
❌ Validation FAILED (attempt 1/3)
Error: src/features/user/ui/UserList.tsx:15 - 'useState' is not defined
Fixing...
```

**FAIL (manual needed):**
```
❌ Validation FAILED (after 3 attempts)
Unresolved errors: [details]
Manual intervention required.
```
