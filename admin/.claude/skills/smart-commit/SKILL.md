---
name: smart-commit
description: Create atomic, well-structured git commits with Korean messages.
disable-model-invocation: true
allowed-tools: Bash(git *)
---

Create atomic commits for all current changes.

## Rules

- **Atomic commits**: one logical change per commit
- **Korean messages**: all commit messages in Korean
- **No Co-Authored-By** lines
- **Never switch branches**

## Commit Format

```
<type>: <subject>
```

| Type       | Usage              |
|------------|--------------------|
| `feat`     | New feature        |
| `fix`      | Bug fix            |
| `refactor` | Code restructuring |
| `style`    | Formatting only    |
| `docs`     | Documentation      |
| `test`     | Tests              |
| `chore`    | Build/config       |

## Workflow

1. `git status` — identify changed files
2. `git diff` — review what changed
3. Group files by logical unit
4. For each group:
   - `git add <specific-files>`
   - `git commit -m "<type>: <Korean subject>"`
5. `git log -n <count>` — verify result

## Example

```bash
git add src/features/user/api/useUsersQuery.ts
git commit -m "feat: 사용자 목록 조회 훅 추가"

git add src/features/user/ui/UserTable.tsx src/features/user/ui/UserTable.styles.ts
git commit -m "feat: 사용자 테이블 컴포넌트 구현"
```
