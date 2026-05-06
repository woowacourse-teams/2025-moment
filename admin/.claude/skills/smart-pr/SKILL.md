---
name: smart-pr
description: 한국어 제목과 설명으로 GitHub 풀 리퀘스트를 생성합니다.
disable-model-invocation: true
allowed-tools: Bash(gh *) Bash(git *)
---

현재 브랜치의 풀 리퀘스트를 생성하세요.

## 규칙

- 모든 내용은 한국어로 작성
- `.github/pull_request_template.md`가 있으면 해당 형식을 따를 것
- 간결하게 작성 — 구현 세부사항보다 핵심 변경사항에 집중

## PR 제목 형식

```
<type>: <제목>
```

예시: `feat: 사용자 목록 페이지 구현`

## PR 본문 템플릿

```markdown
# 📋 연관 이슈

close #<이슈번호>

# 🚀 작업 내용

- <핵심 변경사항 1>
- <핵심 변경사항 2>

# 💬 리뷰 중점 사항

- <리뷰어가 집중해야 할 부분> (해당 시)

# 📸 스크린샷

(없으면 생략)
```

## 작업 순서

1. `git log main..HEAD --oneline` — 이 브랜치의 커밋 확인
2. `git diff main...HEAD --stat` — 변경 범위 파악
3. 커밋 기반으로 제목과 본문 초안 작성
4. `gh pr create --title "..." --body "..."`
5. PR URL 출력
