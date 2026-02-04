# Commit Command

Create git commits for the current staged or unstaged changes.

## Rules

1. **Atomic Commits**: Split changes by responsibility/purpose. Each commit should represent ONE logical change.
   - Design system changes → separate commit
   - API/mutation changes → separate commit
   - UI/page changes → separate commit
2. **Language**: Commit messages MUST be written in Korean
3. **No Co-Authored-By**: NEVER include `Co-Authored-By` lines
4. **Conventional Commits**: Use prefix based on change type:
   - `feat:` - New feature
   - `fix:` - Bug fix
   - `refactor:` - Code refactoring
   - `chore:` - Build, config, or tooling changes
   - `docs:` - Documentation changes
   - `style:` - Code style changes (formatting, etc.)
   - `test:` - Adding or updating tests
5. **Concise**: Keep message clear and concise (1-2 sentences max)
6. **Focus on "why"**: Describe the purpose, not the implementation details

## Process

1. Run `git status` to see changes
2. Run `git diff` to understand what changed
3. Run `git log --oneline -5` to match existing commit style
4. Stage specific files (avoid `git add -A`)
5. Create commit with Korean message

## Example

```bash
git commit -m "feat: 회원 탈퇴 기능 구현"
git commit -m "fix: 로그인 시 토큰 갱신 오류 수정"
git commit -m "refactor: API 호출 로직 분리"
```
