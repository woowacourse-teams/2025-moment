# PR Command

Generate PR content in markdown format based on commits in the current branch.

## Rules

1. **Base on commits**: Only describe changes from commits on the current branch (not in main)
2. **Use template**: Follow the PR template format exactly
3. **Korean content**: Write PR content in Korean
4. **Extract issue number**: Get issue number from branch name (e.g., `feat/#1062` → `1062`)
5. **Output only**: Generate markdown content for the user to copy. Do NOT create PR directly.

## Process

1. Run `git log main..HEAD --oneline` to see commits on this branch
2. Run `git diff main...HEAD` to understand all changes
3. Generate PR title and body in markdown format
4. Present the content for user to copy

## PR Template

```markdown
**Title:** `<type>: <description in Korean>`

**Body:**

# 📋 연관 이슈

close #<issue-number-from-branch>

# 🚀 작업 내용

- <summarize each major change as bullet points>

# 💬 리뷰 중점 사항

- <areas that need careful review>

## 📸 Test Screenshot

<!-- 테스트 결과나 UI 변경 사항의 스크린샷을 첨부하세요 -->

## 📝 Additional Description

<!-- 추가적인 설명이나 고려사항이 있다면 작성하세요 -->

## 🔗 Related Links

<!-- 관련된 문서나 참고 자료 링크 -->
```
