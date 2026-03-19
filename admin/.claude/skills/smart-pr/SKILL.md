---
name: smart-pr
description: Generate and create a GitHub pull request with Korean title and description.
disable-model-invocation: true
allowed-tools: Bash(gh *), Bash(git *)
---

Create a pull request for the current branch.

## Rules

- All content in Korean
- Follow `.github/pull_request_template.md` if it exists
- Concise — focus on core changes, not implementation detail

## PR Title Format

```
<type>: <subject>
```

Example: `feat: 사용자 목록 페이지 구현`

## PR Body Template

```markdown
# 📋 연관 이슈

close #<issue_number>

# 🚀 작업 내용

- <핵심 변경사항 1>
- <핵심 변경사항 2>

# 💬 리뷰 중점 사항

- <리뷰어가 집중해야 할 부분> (해당 시)

# 📸 스크린샷

(없으면 생략)
```

## Workflow

1. `git log main..HEAD --oneline` — review commits on this branch
2. `git diff main...HEAD --stat` — understand scope of changes
3. Draft title and body based on commits
4. `gh pr create --title "..." --body "..."`
5. Output the PR URL
