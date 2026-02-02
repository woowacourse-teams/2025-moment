# Skill: Smart Commit

Create atomic, well-structured commits.

---

## Rules

1. **Current branch only**: Never switch branches
2. **Atomic commits**: One logical change per commit
3. **Korean messages**: All commit messages in Korean
4. **No Co-Authored-By**: Do not add co-author line

---

## Commit Format

```
<type>: <subject>
```

| Type | Usage |
|------|-------|
| `feat` | New feature |
| `fix` | Bug fix |
| `refactor` | Code restructuring |
| `style` | Formatting |
| `docs` | Documentation |
| `test` | Tests |
| `chore` | Build/config |

---

## Workflow

1. `git status` - Check changes
2. `git diff` - Review changes
3. Group by logical unit
4. For each group:
   - `git add <files>`
   - `git commit -m "<type>: <subject>"`
5. `git log -n <count>` - Verify commits

---

## Example

```bash
# Commit 1: Feature
git add src/features/auth/login.ts
git commit -m "feat: 로그인 유효성 검사 로직 추가"

# Commit 2: Style
git add src/features/auth/Login.styles.ts
git commit -m "style: 로그인 폼 반응형 스타일 적용"
```
