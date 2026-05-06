---
name: validation
description: lint, 타입 체크, 테스트를 실행합니다. 오류는 최대 3회 시도까지 자동으로 수정합니다.
disable-model-invocation: true
allowed-tools: Bash(pnpm *)
---

전체 유효성 검사를 실행하고 오류가 있으면 자동으로 수정하세요.

## 실행 명령어

```bash
pnpm run lint && tsc --noEmit && pnpm run test
```

## 진행 방식

1. 유효성 검사 명령어 실행
2. 실패하면 오류를 진단하고 자동으로 수정
3. 유효성 검사 재실행 (최대 3회 시도)
4. 3회 시도 후에도 실패하면 전체 오류 내용과 함께 사용자에게 보고

## 자동 수정 가능한 오류 유형

| 타입             | 예시                                              |
|-----------------|---------------------------------------------------|
| 누락된 import    | `'useState' is not defined`                       |
| 타입 불일치      | `Type 'string' not assignable to 'number'`        |
| 미사용 변수      | `'foo' declared but never used`                   |
| 누락된 return    | `Function lacks ending return statement`          |

## 출력 형식

**통과:**
```
✅ 유효성 검사 통과
- lint: ✓
- tsc: ✓
- test: ✓
```

**실패 (자동 수정 중):**
```
❌ 유효성 검사 실패 (시도 1/3)
오류: src/features/user/ui/UserList.tsx:15 - 'useState' is not defined
수정 중...
```

**실패 (수동 처리 필요):**
```
❌ 유효성 검사 실패 (3회 시도 후)
미해결 오류: [상세 내용]
수동 처리가 필요합니다.
```
