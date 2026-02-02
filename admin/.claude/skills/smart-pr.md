# Skill: Smart PR

Generate clear PR title and description.

---

## Rules

1. **Korean content**: All text in Korean
2. **Concise**: Focus on core changes
3. **Follow template**: Use `.github/pull_request_template.md`

---

## PR Title

Format: `<type>: <subject>`

Example: `feat: 사용자 목록 페이지 구현`

---

## PR Body Template

```markdown
# 📋 연관 이슈

close #<issue_number>

# 🚀 작업 내용

- <Core change 1>
- <Core change 2>

# 💬 리뷰 중점 사항

- <Focus point> (if applicable)

# 📸 Test Screenshot

(Omit if none)

# 📝 Additional Description

(Omit if none)
```

---

## Workflow

1. Analyze commits
2. Draft title
3. Fill template (focus on 🚀 작업 내용)
4. Run `gh pr create`
