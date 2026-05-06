---
name: smart-commit
description: 한국어 메시지로 원자적이고 잘 구조화된 git 커밋을 생성합니다.
disable-model-invocation: true
allowed-tools: Bash(git *)
---

현재 변경사항을 원자적 커밋으로 나누어 생성하세요.

## 규칙

- **원자적 커밋**: 논리적 변경 단위 하나당 커밋 하나
- **한국어 메시지**: 모든 커밋 메시지는 한국어로 작성
- **Co-Authored-By 금지**: 해당 줄 추가 안 함
- **브랜치 전환 금지**

## 커밋 형식

```
<type>: <제목>
```

| 타입       | 사용 시점           |
|------------|---------------------|
| `feat`     | 새로운 기능         |
| `fix`      | 버그 수정           |
| `refactor` | 코드 구조 개선      |
| `style`    | 포맷팅만 변경       |
| `docs`     | 문서 작업           |
| `test`     | 테스트 작업         |
| `chore`    | 빌드/설정 변경      |

## 작업 순서

1. `git status` — 변경된 파일 목록 확인
2. `git diff` — 변경 내용 검토
3. 논리적 단위로 파일 그룹화
4. 각 그룹에 대해:
   - `git add <특정-파일들>`
   - `git commit -m "<type>: <한국어 제목>"`
5. `git log -n <개수>` — 결과 확인

## 예시

```bash
git add src/features/user/api/useUsersQuery.ts
git commit -m "feat: 사용자 목록 조회 훅 추가"

git add src/features/user/ui/UserTable.tsx src/features/user/ui/UserTable.styles.ts
git commit -m "feat: 사용자 테이블 컴포넌트 구현"
```
