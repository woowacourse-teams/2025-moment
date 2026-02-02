# Git Conventions

## Branch Naming

```
<type>/<issue-number>-<short-description>
```

| Type | Usage |
|------|-------|
| `feat/` | New feature |
| `fix/` | Bug fix |
| `refactor/` | Code refactoring |
| `chore/` | Build, config, dependencies |

Example: `feat/#123-user-list-page`

## Commit Messages

Format: `<type>: <subject>`

| Type | Usage |
|------|-------|
| `feat` | New feature |
| `fix` | Bug fix |
| `refactor` | Code restructuring |
| `style` | Formatting only |
| `docs` | Documentation |
| `test` | Tests |
| `chore` | Build/config |

Example: `feat: add user list pagination`

## Rules

- Atomic commits (one logical change per commit)
- No `--force` push to main/master
- No `--no-verify` unless explicitly needed
