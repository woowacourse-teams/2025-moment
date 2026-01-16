---
name: Git Commit Helper
description: git diff를 분석하여 설명적인 커밋 메시지를 생성합니다. 사용자가 커밋 메시지 작성이나 스테이징된 변경사항 검토를 요청할 때 사용합니다.
hooks:
  PostToolUse:
    - matcher: "Bash"
      hooks:
        - type: command
          command: "mkdir -p ./logs && echo \"[$(date)] Git Commit Helper: Analyzed git diff for commit message\" >> ./logs/git-commit-helper.log"
---

# Git Commit Helper

## 빠른 시작

스테이징된 변경사항을 분석하고 커밋 메시지를 생성합니다:

```bash
# 스테이징된 변경사항 보기
git diff --staged

# 변경사항을 기반으로 커밋 메시지 생성
# (Claude가 diff를 분석하고 메시지를 제안합니다)
```

## 커밋 메시지 형식

Conventional Commits 형식을 따릅니다:

```
<type>: <description>

[선택적 본문]

[선택적 footer]
```

### 유형(Type)

- **feat**: 새로운 기능
- **fix**: 버그 수정
- **docs**: 문서 변경
- **style**: 코드 스타일 변경 (포맷팅, 세미콜론 누락)
- **refactor**: 코드 리팩토링
- **test**: 테스트 추가 또는 수정
- **chore**: 유지보수 작업
- **ci/cd**: 통합 및 배포 관련 작업

### 예시

**기능 커밋:**

```
feat: JWT 인증 추가

다음을 포함한 JWT 기반 인증 시스템 구현:
- 토큰 생성을 포함한 로그인 엔드포인트
- 토큰 검증 미들웨어
- 리프레시 토큰 지원
```

**버그 수정:**

```
fix: 사용자 프로필의 null 값 처리

사용자 프로필 필드가 null일 때 크래시 방지.
중첩된 속성에 접근하기 전에 null 체크 추가.
```

**리팩토링:**

```
refactor: 쿼리 빌더 간소화

공통 쿼리 패턴을 재사용 가능한 함수로 추출.
데이터베이스 레이어의 코드 중복 제거.
```

## 변경사항 분석

커밋되는 내용을 검토합니다:

```bash
# 변경된 파일 표시
git status

# 상세 변경사항 표시
git diff --staged

# 통계 표시
git diff --staged --stat

# 특정 파일의 변경사항 표시
git diff --staged path/to/file
```

## 커밋 메시지 가이드라인

**해야 할 것:**

- 명령조를 사용하기 ("기능 추가" 아님 "기능을 추가합니다" 아님)
- 첫 번째 줄을 50자 이하로 유지
- 첫 글자를 대문자로
- 요약 끝에 마침표 없음
- 본문에는 "무엇"이 아니라 "왜"를 설명

**하지 말아야 할 것:**

- "업데이트" 또는 "수정함" 같은 모호한 메시지 사용
- 요약줄에 기술적 구현 세부사항 포함
- 요약줄에 단락 작성
- 과거형 사용

## 다중 파일 커밋

여러 관련 변경사항을 커밋할 때:

```
refactor: 인증 모듈 재구성

- 컨트롤러에서 서비스 레이어로 인증 로직 이동
- 검증을 별도의 유효성 검사자로 추출
- 새로운 구조를 사용하도록 테스트 업데이트
- 인증 흐름을 위한 통합 테스트 추가

Breaking change: 인증 서비스는 이제 설정 객체가 필요함
```

## Breaking Changes

Breaking Changes를 명확하게 표시합니다:

```
feat!: API 응답 형식 재구성

BREAKING CHANGE: 모든 API 응답이 이제 JSON:API 사양을 따릅니다

이전 형식:
{ "data": {...}, "status": "ok" }

새 형식:
{ "data": {...}, "meta": {...} }

마이그레이션 가이드: 새로운 응답 구조를 처리하도록 클라이언트 코드 업데이트
```

## 템플릿 워크플로우

1. **변경사항 검토**: `git diff --staged`
2. **유형 식별**: feat, fix, refactor 등인가?
4. **요약 작성**: 간결하고 명령조로
5. **본문 추가**: 왜인지, 어떤 영향이 있는지 설명
6. **Breaking Changes 표시**: 해당되는 경우

## 인터랙티브 커밋 헬퍼

선택적 스테이징을 위해 `git add -p` 사용:

```bash
# 인터랙티브하게 변경사항 스테이징
git add -p

# 스테이징된 것 검토
git diff --staged

# 메시지와 함께 커밋
git commit -m "type: description"
```

## 커밋 수정

마지막 커밋 메시지 수정:

```bash
# 커밋 메시지만 수정
git commit --amend

# 수정하고 더 많은 변경사항 추가
git add forgotten-file.js
git commit --amend --no-edit
```

## 모범 사례

1. **원자적 커밋** - 논리적 단위로 한 커밋
2. **커밋 전 테스트** - 코드 동작 확인
3. **이슈 참조** - 해당되는 경우 이슈 번호 포함
4. **집중 유지** - 관련 없는 변경사항 혼합 금지
5. **인간을 위해 작성** - 미래의 자신이 읽을 것입니다

## 커밋 메시지 체크리스트

- [ ] 유형이 적절한가 (feat/fix/docs/등)
- [ ] 요약이 50자 이하인가
- [ ] 요약이 명령조를 사용하는가
- [ ] 본문이 "무엇"이 아니라 "왜"를 설명하는가
- [ ] Breaking Changes가 명확하게 표시되어 있는가
- [ ] 관련 이슈 번호가 포함되어 있는가
