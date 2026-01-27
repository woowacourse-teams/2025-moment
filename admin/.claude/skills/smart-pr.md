# Skill: Smart Pull Request (PR)

## Purpose

To generate a **clear, concise, Korean** PR title and description based on the project template.

## Rules

1.  **Title First**: Always provide the PR title first.
2.  **Korean Only**: Write all descriptive text in Korean.
3.  **Conciseness**: Focus on the core changes. Do not be verbose.
4.  **Template Adherence**: Follow the structure of `.github/pull_request_template.md`.
5.  **Optional Sections**: Screenshots (`📸 Test Screenshot`) and Links (`🔗 Related Links`) can be omitted if not strictly necessary.

## PR Structure

### 1. Title

Format: `type: Subject` (Same as commit convention)
Example: `feat: 로그인 페이지 UI 구현`

### 2. Description (Template)

```markdown
# 📋 연관 이슈

close #<issue_number> (If applicable, otherwise omit or write '-')

# 🚀 작업 내용

- <Core Change 1>
- <Core Change 2>

# 💬 리뷰 중점 사항

- <Focus Point 1> (If applicable)

# 📸 Test Screenshot

(Omit if none)

# 📝 Additional Description

(Omit if none)

# 🔗 Related Links

(Omit if none)
```

## Workflow

1.  **Analyze Changes**: Review what has been committed/changed.
2.  **Draft Title**: Create a concise title like `feat: ...`.
3.  **Draft Body**: Fill in the template sections.
    - **Focus on `🚀 작업 내용`**: List the most important technical or functional changes.
    - **Keep `💬 리뷰 중점 사항` concise**: Only ask for specific feedback if needed.
4.  **Output**: Present the Title and Markdown Content to the user.
