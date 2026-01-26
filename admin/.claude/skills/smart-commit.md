# Skill: Smart Git Commit

## Purpose

To create **atomic, well-structured commits** with **clear Korean messages**, strictly on the **current branch**.

## Rules

1.  **Current Branch Only**: NEVER switch branches unless explicitly requested. Always verify you are on the expected branch.
2.  **Atomic Commits**: Split unrelated changes into separate commits. Do not mix refactoring with new features if possible.
3.  **Language**: Write all commit messages in **Korean**.
4.  **Verification**: Verify the changes using `git diff` before committing.

## Commit Message Convention

Follow the format: `type: Subject`

### Types

- `feat`: 새로운 기능 추가 (New feature)
- `fix`: 버그 수정 (Bug fix)
- `refactor`: 기능 변경 없는 코드 구조 개선 (Refactoring)
- `style`: 코드 포맷팅, 세미콜론 누락 등 (Formatting)
- `docs`: 문서 수정 (Documentation)
- `test`: 테스트 코드 추가/수정 (Tests)
- `chore`: 빌드 업무 수정, 패키지 매니저 설정 등 (Chores)

### Subject

- Use Korean.
- Be concise and descriptive.
- Example: `feat: 로그인 페이지 UI 구현`

## Workflow

1.  **Check Status**: `git status`
2.  **Analyze Diff**: `git diff` (Check what actually changed)
3.  **Group Changes**: Identify logical groups of changes.
4.  **Commit Loop**:
    - `git add <files>`
    - `git commit -m "<type>: <subject>"`
    - Repeat until `git status` is clean.
5.  **Final Check**: `git log -n <count>` to show what was committed.

## Example

If `login.ts` (logic) and `Login.css` (style) and `README.md` (docs) are modified:

1.  **Commit 1**:
    ```bash
    git add src/features/auth/login.ts
    git commit -m "feat: 로그인 유효성 검사 로직 추가"
    ```
2.  **Commit 2**:
    ```bash
    git add src/features/auth/Login.css
    git commit -m "style: 로그인 폼 반응형 스타일 적용"
    ```
3.  **Commit 3**:
    ```bash
    git add README.md
    git commit -m "docs: 프로젝트 실행 방법 업데이트"
    ```
