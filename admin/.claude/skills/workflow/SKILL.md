---
name: workflow
description: 요구사항부터 PR까지 기능 개발 전체 파이프라인을 실행합니다.
argument-hint: <기능명> <entity>
disable-model-invocation: true
allowed-tools: Bash(pnpm *) Bash(git *) Bash(gh *)
---

다음 기능의 전체 개발 파이프라인을 실행하세요: $ARGUMENTS

`<기능명> <entity>` 형식으로 파싱하세요. entity가 생략된 경우 기능명에서 추론하세요.

단계 중 하나라도 실패하면 즉시 중단하세요. 오류를 보고하고 사용자 입력을 기다리세요.

---

## 1단계: 기능 계획 수립

`.claude/skills/feature-plan/SKILL.md`를 읽고 지시에 따르세요.

⏸️ **게이트: 계획을 제시하고 진행 전에 사용자의 명시적 승인을 기다리세요.**

---

## 2단계: 파일 구조 생성

`.claude/skills/page-template/SKILL.md`를 읽고, $ARGUMENTS의 entity를 사용하여 지시에 따르세요.

---

## 3단계: 유효성 검사

`.claude/skills/validation/SKILL.md`를 읽고 지시에 따르세요.

유효성 검사를 통과하기 전까지 4단계로 진행하지 마세요.

---

## 4단계: 커밋

`.claude/skills/smart-commit/SKILL.md`를 읽고 지시에 따르세요.

⏸️ **게이트: 제안하는 커밋 그룹과 메시지를 사용자에게 보여주세요. 커밋 전에 확인을 기다리세요.**

---

## 5단계: PR 생성

`.claude/skills/smart-pr/SKILL.md`를 읽고 지시에 따르세요.

⏸️ **게이트: PR 제목과 본문 초안을 보여주세요. `gh pr create`를 실행하기 전에 확인을 기다리세요.**
