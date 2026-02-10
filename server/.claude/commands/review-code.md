---
description: 에이전트 팀을 구성하여 현재 브랜치의 코드 변경사항을 다각도로 리뷰합니다. 변경 규모에 따라 팀 규모를 조정하며, 반론 검증을 거쳐 종합 보고서를 작성합니다.
---

# Review Code Command

현재 브랜치(`git diff main...HEAD`)의 코드 변경사항을 리뷰하기 위한 에이전트 팀을 구성하세요.

## 리더 역할

<leader_role>
당신은 코드 리뷰 팀의 리더입니다. 직접 코드를 분석하지 않고, 팀 조율과 최종 종합에만 집중합니다.

- Phase 1: 사전 분석 수행 + 팀 규모 결정 + 팀 스폰 + 맥락 전달
- Phase 2: 리뷰어 보고 수집 + devils-advocate에게 전달
- Phase 3: 반론 반영하여 최종 보고서 작성 + 팀 종료

코드를 직접 읽거나 수정하지 마세요. 모든 분석은 팀원에게 위임합니다.
</leader_role>

## 인자 처리

`$ARGUMENTS`가 제공된 경우:

- **base branch 지정**: `$ARGUMENTS`가 브랜치명이면 `git diff {branch}...HEAD` 사용 (예: `/review-code develop`)
- **초점 영역 지정**: `$ARGUMENTS`가 키워드면 해당 리뷰어에 가중치 부여 (예: `/review-code security`)
- **비어있으면**: `git diff main...HEAD` 기본 사용

## Phase 1: 사전 분석 및 팀 구성

### 1-1. 사전 분석 (리더 수행)

팀 생성 전 리더가 먼저 수행:

```bash
# 1. 변경 범위 파악
git log main..HEAD --oneline
git diff main...HEAD --stat
git diff main...HEAD --name-only

# 2. 변경된 도메인 모듈 식별
git diff main...HEAD --name-only | grep "src/main" | cut -d'/' -f5 | sort -u

# 3. 변경 규모 확인
git diff main...HEAD --shortstat
```

### 1-2. 팀 규모 결정

사전 분석 결과에 따라 팀 규모를 조정합니다. 항상 6명을 스폰하면 비효율적이므로, 변경 규모에 맞게 선택하세요.

| 규모      | 기준                    | 팀 구성                                   |
|---------|-----------------------|----------------------------------------|
| **소규모** | 변경 파일 ≤5개, 단일 도메인     | 가장 관련도 높은 리뷰어 2-3명, devils-advocate 생략 |
| **중규모** | 변경 파일 6-15개 또는 2개 도메인 | 관련 리뷰어 3-4명 + devils-advocate          |
| **대규모** | 변경 파일 >15개 또는 3개+ 도메인 | 전원 투입 (리뷰어 5명 + devils-advocate)       |

리뷰어 선택 우선순위: 변경 내용에 가장 관련도가 높은 리뷰어부터 선택합니다.

- DB 마이그레이션/쿼리 변경 → database-reviewer 필수
- 새 엔드포인트/인증 변경 → security-reviewer 필수
- 새 모듈/레이어 변경 → architecture-reviewer 필수
- 테스트 변경 → test-reviewer 필수
- 코드 구조/컨벤션 → code-quality-reviewer 필수

### 1-3. 리뷰어 정의

모든 리뷰어의 공통 설정:

- **모델**: opus
- **서브에이전트 타입**: general-purpose

#### architecture-reviewer (아키텍처 리뷰어)

<reviewer_spec>
역할: Clean Architecture 레이어 규칙, DDD 모듈 경계, 의존성 방향 검증
참고 파일: `.claude/CLAUDE.md`, `.claude/rules/coding-style.md`, `.claude/rules/patterns.md`, 기존 모듈 구조
집중 영역: 이 프로젝트는 Facade → Application → Domain Service 호출 체인을 엄격히 준수하므로, 레이어 역방향 의존이나 Facade 책임 비대화를 중점 검토
</reviewer_spec>

#### code-quality-reviewer (코드 품질 리뷰어)

<reviewer_spec>
역할: Kent Beck의 Tidy First 원칙, 코드 가독성, 컨벤션 준수 검증
참고 파일: `.claude/rules/coding-style.md`, `.claude/agents/code-reviewer.md`
집중 영역: 구조적 변경과 행동적 변경의 커밋 분리 여부, Dead code 여부, Lombok 사용 규칙 준수
</reviewer_spec>

#### security-reviewer (보안 리뷰어)

<reviewer_spec>
역할: 인가 취약점, IDOR, 쿼리 인젝션, 데이터 노출 검증
참고 파일: `.claude/agents/security-reviewer.md`, `.claude/rules/security.md`, auth/ 모듈
집중 영역: 이 프로젝트는 userId를 PathVariable이나 쿠키 JWT에서 추출하는 패턴을 사용하므로 IDOR 위험이 높음. @AuthenticationPrincipal 누락과 리소스 소유자 검증을 중점
검토
</reviewer_spec>

#### database-reviewer (DB/성능 리뷰어)

<reviewer_spec>
역할: DDL 설계, 인덱스 전략, 쿼리 성능, Flyway 마이그레이션 검증
참고 파일: `.claude/agents/database-reviewer.md`, `src/main/resources/db/migration/mysql/`, 기존 Repository
집중 영역: 이 프로젝트는 Soft Delete(@SQLDelete/@SQLRestriction)와 커서 기반 페이지네이션을 사용하므로, 인덱스 설계와 N+1 쿼리를 중점 검토
</reviewer_spec>

#### test-reviewer (테스트 리뷰어)

<reviewer_spec>
역할: 테스트 커버리지, TDD 준수, Mock 정책 준수, 누락 시나리오 검증
참고 파일: `.claude/rules/testing.md`, `.claude/agents/tdd-guide.md`, 기존 테스트 파일 패턴
집중 영역:
- 커스텀 쿼리(`@Query`, 복잡한 메서드 네이밍)에 대한 Repository 테스트(`@DataJpaTest`) 존재 여부
- Service 테스트에서 Repository/내부 Service Mock 사용 여부 (사용 시 CRITICAL)
- Mock이 외부 API(Firebase, S3 등)에만 사용되었는지
- 정상/예외/엣지 케이스 커버리지
- 한글 서술형 테스트명 사용
</reviewer_spec>

#### devils-advocate (반론 제기자)

- **모델**: opus (다른 리뷰어보다 높은 추론 능력 필요)

<reviewer_spec>
역할: 다른 리뷰어들의 발견 사항에 반론을 제기하여 진짜 중요한 이슈만 남기는 필터 역할

2단계 작업:

- Phase A (병렬): 변경된 코드를 직접 읽고 맥락 파악, 기존 코드베이스 패턴과 비교
- Phase B (순차): 5명 리뷰어 보고 수신 후 각 이슈에 대해 판정

반론 기준:

- 과잉 지적인가? 프로젝트 규모/단계에 비해 과도한 요구
- 맥락을 무시했는가? 의도적 트레이드오프를 결함으로 오인
- 기존 코드와 일관성 있는가? 기존에도 같은 패턴이 있는데 새 코드만 지적
- 실제 영향이 있는가? 이론적 문제 vs 현실적으로 발생 가능한 문제
- False positive인가? 프레임워크가 이미 처리하는 부분을 중복 지적
- 수정 비용 대비 가치가 있는가?

판정 유형:

- AGREE: 정당한 지적, 심각도 유지
- DOWNGRADE: 심각도를 낮춰야 함 + 근거
- DISMISS: 이슈 기각 + 상세 근거
- UPGRADE: 심각도를 높여야 함 + 근거
  </reviewer_spec>

## Phase 2: 에이전트 메시지 전달

### 리뷰어에게 전달하는 메시지 템플릿

각 리뷰어를 스폰할 때 다음 구조로 맥락을 전달합니다:

```
<role>{리뷰어 역할 설명}</role>

<context>
- 브랜치: {branch_name}
- Base: {base_branch}
- 변경 파일: {file_list}
- 변경 도메인: {domains}
- 커밋 목록: {commit_list}
</context>

<instructions>
1. `git diff {base}...HEAD`를 실행하여 변경 사항 확인
2. 참고 파일({reference_files})을 읽고 체크리스트 기반으로 리뷰
3. 변경된 파일만 집중 분석하되, 필요시 기존 코드를 참조하여 맥락 파악
4. 결과를 아래 보고 형식으로 작성하여 리더에게 SendMessage로 전달
5. 보고 전송 후 작업 완료
</instructions>

<report_format>
## {리뷰어명} 리뷰 결과

### CRITICAL (반드시 수정)
- **[카테고리]** 문제 설명 (`파일:라인`)
    - 영향: 어떤 문제가 발생할 수 있는지
    - 수정: 구체적 수정 방안 + 코드 예시

### HIGH (강력 권고)
- **[카테고리]** 문제 설명 (`파일:라인`)
    - 수정: 수정 방안

### MEDIUM (개선 권고)
- **[카테고리]** 문제 설명 (`파일:라인`)
    - 제안: 개선 방안

### SUGGESTION (참고)
- **[카테고리]** 제안 사항

### GOOD (잘된 점)
- **[카테고리]** 긍정적 평가
</report_format>
```

### 보고 예시 (one-shot)

리뷰어가 이해할 수 있도록 아래 예시를 메시지에 포함하세요:

```
<example_report>
## security-reviewer 리뷰 결과

### CRITICAL (반드시 수정)

- **[IDOR]** 댓글 삭제 시 소유자 검증 누락 (`CommentService.java:45`)
    - 영향: 인증된 사용자가 타인의 댓글 ID를 추측하여 삭제할 수 있음
    - 수정: `if (!comment.getCommenter().getId().equals(userId))` 검증 추가

### HIGH (강력 권고)

- **[@Valid 누락]** BlockCreateRequest에 @Valid 미적용 (`BlockController.java:23`)
    - 수정: `@Valid @RequestBody BlockCreateRequest request`로 변경

### GOOD (잘된 점)

- **[인가]** 모든 엔드포인트에 @AuthenticationPrincipal 적용 완료
- **[파라미터 바인딩]** JPQL 쿼리 전부 파라미터 바인딩 사용
</example_report>
```

### devils-advocate에게 전달하는 메시지 (Phase B)

5명 리뷰어 보고 완료 후:

```
<role>반론 제기자로서 각 리뷰어의 발견 사항을 검증합니다.</role>

<context>
Phase A에서 파악한 코드 맥락을 기반으로 아래 발견 사항들을 검증하세요.
</context>

<reviewer_findings>
{5명 리뷰어의 보고 내용 전체}
</reviewer_findings>

<instructions>
각 이슈에 대해 AGREE/DOWNGRADE/DISMISS/UPGRADE 판정을 내리고,
아래 형식으로 반론 보고를 작성하여 리더에게 SendMessage로 전달하세요.
보고 전송 후 작업 완료.
</instructions>

<report_format>
## devils-advocate 반론 보고

### 반론 요약
- 원본 이슈: N건
- AGREE (유지): N건
- DOWNGRADE (하향): N건
- DISMISS (기각): N건
- UPGRADE (상향): N건

### 상세 반론

#### [원본 이슈 제목] (발견자: {reviewer_name}, 원본 등급: {severity})
- **판정**: AGREE / DOWNGRADE / DISMISS / UPGRADE
- **조정 등급**: {adjusted_severity} (변경된 경우)
- **근거**: 구체적 이유 + 코드 증거
</report_format>
```

## Phase 3: 팀 운영 흐름

```
[Phase 1: 사전 분석 + 팀 구성]
1. 리더: 사전 분석 (git log, diff stat, 도메인 식별, 변경 규모 확인)
2. 팀 규모 결정 (소/중/대규모)
3. 팀 생성 (TeamCreate)
4. 태스크 생성 (TaskCreate) - 선택된 리뷰어 수 + devils-advocate(해당 시)
5. 에이전트 동시 스폰 (Task, team_name 지정) - 메시지 템플릿으로 맥락 전달

[Phase 2: 반론 검증] (devils-advocate 포함 시)
6. 리뷰어 보고 수신 대기
7. 전원 보고 완료 → devils-advocate에게 전체 발견 사항 전달 (SendMessage)
8. devils-advocate: Phase B 수행 → 반론 보고 수신

[Phase 3: 종합]
9. 리더가 최종 종합 보고서 작성
   - devils-advocate가 있는 경우: 반론 반영하여 심각도 조정
   - devils-advocate가 없는 경우: 리뷰어 보고를 직접 종합
   - DISMISS된 이슈는 최종 보고서에서 제외 (사유만 별도 기록)
   - DOWNGRADE/UPGRADE된 이슈는 조정된 등급으로 반영
10. 팀 종료 (shutdown_request → TeamDelete)
```

## 최종 보고서 형식

리더는 모든 리뷰를 종합하여 다음 필수 요소를 포함한 보고서를 작성합니다:

```markdown
# Code Review Report

**브랜치**: {branch_name} | **Base**: {base_branch}
**커밋 수**: {N}개 | **변경 파일**: {N}개 (+{added}/-{removed})
**변경 도메인**: {domains} | **팀 규모**: {N}명

---

## Summary

| 등급 | 건수 | 발견자 |
|------|------|--------|
| CRITICAL | N | reviewer1, reviewer2 |
| HIGH | N | reviewer3 |
| MEDIUM | N | reviewer1, reviewer4 |

## Verdict: APPROVE / REQUEST CHANGES / COMMENT

> APPROVE: CRITICAL 0건, HIGH 0건
> REQUEST CHANGES: CRITICAL 또는 HIGH 1건 이상
> COMMENT: MEDIUM 또는 SUGGESTION만 존재

---

## CRITICAL Issues (반드시 수정)

### 1. [이슈 제목]

- **발견자**: {reviewer_name}
- **파일**: `path/to/File.java:line`
- **문제**: 설명
- **영향**: 영향 범위
- **수정**: 구체적 수정 방안

## HIGH Issues (강력 권고)

(동일 형식)

## MEDIUM Issues (개선 권고)

(동일 형식)

## SUGGESTION (참고)

(간략 목록)

## GOOD (잘된 점)

(리뷰어별 긍정 평가 종합)

---

## 반론 검증 결과 (devils-advocate 포함 시)

| 원본 이슈 | 원본 등급 | 판정 | 조정 등급 | 사유 요약 |
|-----------|----------|------|----------|----------|
| 이슈 제목 | HIGH | DOWNGRADE | MEDIUM | 현재 규모에서 영향 미미 |

### 기각된 이슈 (참고용)

- **[이슈 제목]** (원본: {severity}, 발견자: {reviewer}) - 기각 사유

---

## 리뷰어별 상세 보고

<details>
<summary>{reviewer_name}</summary>
(원본 보고 첨부)
</details>
```

## 사용 예시

```bash
# 기본 사용 (main 대비 현재 브랜치 리뷰)
/review-code

# 특정 base branch 지정
/review-code develop

# 특정 영역 집중
/review-code security
```
