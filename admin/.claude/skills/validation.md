# Skill: Validation

Run codebase validation after implementation.

---

## Command

```bash
pnpm run lint && tsc --noEmit && pnpm run test
```

---

## Process

1. **Execute** validation command
2. **On FAIL**: Auto-fix and re-validate (max 3 attempts)
3. **After 3 failures**: Report to user

---

## Output Format

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
Unresolved: [error details]
Manual intervention required.
```

---

## Auto-fixable Errors

| Type | Example |
|------|---------|
| Missing import | `'useState' is not defined` |
| Type mismatch | `Type 'string' not assignable to 'number'` |
| Unused variable | `'foo' declared but never used` |
