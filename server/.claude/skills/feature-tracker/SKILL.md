---
name: feature-tracker
description: |
  Feature Registry 문서를 최신화합니다. 새 기능 구현, 기존 기능 수정, 도메인 이벤트 추가 등의 작업 완료 후 .claude/docs/features/ 문서들을 실제 코드 기반으로 갱신합니다.
  "/feature-tracker" 또는 "feature 문서 업데이트해줘" 요청 시 사용합니다.

  <example>
  Context: 새 기능 구현 완료 후
  user: "feature 문서 업데이트해줘"
  assistant: "최근 변경사항을 분석하고 Feature Registry를 업데이트하겠습니다."
  </example>

  <example>
  Context: 리팩토링이나 버그 수정 완료 후
  user: "/feature-tracker"
  assistant: "변경된 코드를 분석하여 해당 도메인의 feature 문서를 갱신하겠습니다."
  </example>
---

# Feature Registry Tracker

Feature Registry 문서(`docs/features/`)를 실제 코드 상태에 맞게 최신화하는 skill입니다.

## 실행 워크플로우

아래 단계를 **순서대로** 수행합니다.

---

### Phase 1: 변경사항 분석

#### 1-1. Git diff로 변경된 파일 파악

```bash
# 마지막 커밋 이후 변경사항 (staged + unstaged)
git diff --name-only HEAD

# 최근 N개 커밋의 변경 파일 (기본 3개)
git log --oneline -3 --name-only

# 현재 브랜치에서 main 이후 전체 변경 파일
git diff --name-only main...HEAD
```

변경된 파일들에서 다음을 추출합니다:

- **영향받는 도메인**: `src/main/java/moment/{domain}/` 경로에서 도메인 식별
- **변경 유형**: 새 파일 추가(A), 수정(M), 삭제(D)
- **레이어**: domain, infrastructure, service, presentation, dto 중 어디에 해당하는지

#### 1-2. 변경 유형 분류

| 변경 유형        | 업데이트 대상                                           | 예시                 |
|--------------|---------------------------------------------------|--------------------|
| 새 기능 추가      | `{domain}.md` + `FEATURES.md`                     | 새 API 엔드포인트, 새 서비스 |
| 기존 기능 수정     | `{domain}.md` + `FEATURES.md` Recent Changes      | 비즈니스 로직 변경, API 수정 |
| 새 도메인 모듈     | `{domain}.md` 신규 + `FEATURES.md` Quick Reference  | 새 패키지 추가           |
| 도메인 이벤트 추가   | `{domain}.md` Events + `FEATURES.md` Cross-Domain | 새 Event record     |
| 테스트 추가       | `{domain}.md` 테스트 섹션                              | 새 테스트 클래스          |
| DB 마이그레이션 추가 | `{domain}.md` 마이그레이션 섹션                           | 새 Flyway SQL       |
| 리팩토링         | `{domain}.md` Key Classes                         | 클래스명 변경, 레이어 이동    |

---

### Phase 2: 코드베이스 스캔

영향받는 도메인에 대해 실제 코드를 스캔합니다.

#### 2-1. 도메인별 코드 구조 스캔

```bash
# 해당 도메인의 전체 파일 구조
find src/main/java/moment/{domain}/ -name "*.java" | sort

# 엔티티 클래스
grep -rl "@Entity" src/main/java/moment/{domain}/domain/

# 컨트롤러 및 API 엔드포인트
grep -n "@PostMapping\|@GetMapping\|@PutMapping\|@PatchMapping\|@DeleteMapping" src/main/java/moment/{domain}/presentation/*.java

# 서비스 클래스
find src/main/java/moment/{domain}/service/ -name "*.java" | sort

# DTO 클래스
find src/main/java/moment/{domain}/dto/ -name "*.java" | sort

# 도메인 이벤트
find src/main/java/moment/{domain}/dto/ -name "*Event.java"

# 이벤트 발행 코드
grep -rn "publishEvent" src/main/java/moment/{domain}/

# 이벤트 핸들러
grep -rn "@TransactionalEventListener" src/main/java/moment/{domain}/
```

#### 2-2. 테스트 클래스 스캔

```bash
# 해당 도메인의 테스트 파일
find src/test/java -path "*/{domain}/*" -name "*Test.java" | sort

# E2E 테스트 식별
grep -rl "@Tag(\"e2e\")" src/test/java/ | grep "{domain}"
```

#### 2-3. DB 마이그레이션 스캔

```bash
# 해당 도메인 관련 마이그레이션
ls src/main/resources/db/migration/mysql/ | sort
```

#### 2-4. 에러 코드 스캔

```bash
# 해당 도메인 프리픽스의 에러 코드
grep -n "\"[A-Z]*-[0-9]*\"" src/main/java/moment/global/exception/ErrorCode.java
```

---

### Phase 3: 도메인별 문서 업데이트 (`{domain}.md`)

#### 3-1. 새 기능 추가 시

다음 Feature ID를 결정합니다:

- 해당 도메인 문서에서 마지막 Feature ID 확인
- `{PREFIX}-{NNN+1}` 형태로 새 ID 부여

**새 기능 항목 템플릿**:

```markdown
### {PREFIX}-{NNN}: {기능명}

- **Status**: DONE
- **API**: `{METHOD} {endpoint}`
- **Key Classes**:
    - Controller: `{ControllerName}`
    - Facade: `{FacadeServiceName}` (있는 경우)
    - Application: `{ApplicationServiceName}` (있는 경우)
    - Domain: `{DomainServiceName}`
    - Entity: `{EntityName}`
    - DTO: `{RequestDTO}`, `{ResponseDTO}`
- **Business Rules**: {비즈니스 규칙 설명}
- **Dependencies**: {의존하는 다른 도메인} ({서비스명})
- **Tests**: {테스트 클래스 목록}, {E2E 테스트} (E2E)
- **Error Codes**: {관련 에러 코드 범위}
```

#### 3-2. 기존 기능 수정 시

해당 Feature 항목의 변경된 필드만 업데이트합니다:

- API 경로 변경 → **API** 필드 수정
- 새 클래스 추가 → **Key Classes** 필드 수정
- 비즈니스 규칙 변경 → **Business Rules** 수정
- 테스트 추가 → **Tests** 수정

#### 3-3. 섹션 업데이트

**Domain Events Published** (이벤트 추가/변경 시):

```markdown
## Domain Events Published

| Event | 구독자 | 설명 | 상태 |
|-------|--------|------|------|
| `{EventName}` | `{HandlerClass}` | {설명} | ✅ 활성 |
```

상태 값:

- `✅ 활성`: 발행 + 구독 모두 정상
- `⚠️ dead code`: record만 존재, 사용되지 않음
- `⚠️ 미발행`: handler 존재하지만 발행 코드 없음

**관련 엔티티** (엔티티 추가/변경 시):

```markdown
## 관련 엔티티

- `{EntityName}` (@Entity: "{table_name}") - {특이사항}
```

**관련 테스트 클래스** (테스트 추가 시):

```markdown
## 관련 테스트 클래스 ({N}개)

- `{TestClassName}`, `{TestClassName2}`
- `{E2ETestClassName}` (E2E)
```

**DB 마이그레이션** (마이그레이션 추가 시):

```markdown
## DB 마이그레이션

- V{N}: `V{N}__{description}.sql`
```

#### 3-4. 메타데이터 갱신

파일 상단의 메타데이터를 갱신합니다:

```markdown
> Last Updated: {오늘 날짜 YYYY-MM-DD}
> Features: {실제 기능 수}
```

---

### Phase 4: FEATURES.md 업데이트

#### 4-1. Last Updated 갱신

```markdown
> Last Updated: {오늘 날짜 YYYY-MM-DD}
```

#### 4-2. Quick Reference 테이블 갱신

기능 수가 변경된 도메인의 행을 업데이트합니다:

```markdown
| {도메인} | {PREFIX} | {새 기능 수} | {상태} | [{domain}.md]({domain}.md) |
```

- **기능 수**: 해당 `{domain}.md`에 있는 Feature 항목 수와 일치
- **상태**: IN_PROGRESS 기능이 있으면 `IN_PROGRESS`, 아니면 `DONE`
- **총 N개 기능**: 모든 도메인 기능 수의 합으로 갱신

#### 4-3. Cross-Domain Dependencies 갱신 (이벤트 변경 시)

새 이벤트가 추가되었거나 기존 이벤트가 변경된 경우:

```markdown
| `{EventName}` | {발행 도메인} | `{발행 클래스}` | {구독 도메인} | `{구독 클래스}` | {비고} |
```

**이벤트 파일 위치**도 업데이트:

```markdown
- `moment/{domain}/dto/{EventName}.java`
```

비고 값:

- `✅ 활성`: publishEvent() 호출 + @TransactionalEventListener 핸들러 모두 존재
- `⚠️ 미사용 dead code`: Event record만 존재
- `⚠️ handler만 존재, 미발행`: 핸들러는 있지만 publishEvent() 없음

#### 4-4. Recent Changes 추가

**최상단에** 새 항목을 추가합니다:

```markdown
| {오늘 날짜 YYYY-MM-DD} | {도메인} | {Feature ID} | {변경 내용 요약} |
```

규칙:

- 최대 20건만 유지 (21건 이상이면 가장 오래된 항목 삭제)
- 여러 Feature가 함께 변경된 경우 `{ID1}, {ID2}` 형태로 기재
- 변경 내용은 간결하게 (한 줄)

---

### Phase 5: 새 도메인 모듈 추가 (해당 시에만)

완전히 새로운 도메인 모듈이 추가된 경우에만 수행합니다.

#### 5-1. 새 도메인 파일 생성

`docs/features/{domain}.md` 파일을 아래 템플릿으로 생성:

```markdown
# {Domain} Domain (PREFIX: {PREFIX})

> Last Updated: {YYYY-MM-DD}
> Features: {N}

## 기능 목록

### {PREFIX}-001: {기능명}

- **Status**: DONE
- **API**: `{METHOD} {endpoint}`
- **Key Classes**:
    - Controller: `{ControllerName}`
    - Domain: `{DomainServiceName}`
    - Entity: `{EntityName}`
    - DTO: `{RequestDTO}`, `{ResponseDTO}`
- **Business Rules**: {비즈니스 규칙}
- **Dependencies**: {의존 도메인}
- **Tests**: {테스트 클래스}
- **Error Codes**: {에러 코드}

## Domain Events Published

| Event | 구독자 | 설명 | 상태 |
|-------|--------|------|------|

## 관련 에러 코드

- (해당 시 작성)

## 관련 엔티티

- `{EntityName}` (@Entity: "{table_name}")

## 관련 테스트 클래스 ({N}개)

- {테스트 클래스 목록}

## DB 마이그레이션

- {해당 시 작성}
```

#### 5-2. FEATURES.md Quick Reference에 행 추가

새 도메인 행을 알파벳 순서에 맞게 삽입합니다.

---

### Phase 6: 검증

모든 업데이트 완료 후 다음을 검증합니다:

#### 6-1. 정합성 검증

- [ ] 각 `{domain}.md`의 `Features:` 숫자 == 실제 Feature 항목 수
- [ ] `FEATURES.md` Quick Reference의 기능 수 == 각 도메인 파일의 Features 숫자
- [ ] `FEATURES.md`의 "총 N개 기능" == 모든 도메인 기능 수의 합
- [ ] Recent Changes가 20건 이내
- [ ] 모든 Last Updated 날짜가 오늘 날짜

#### 6-2. 코드-문서 일치 검증

- [ ] 문서에 기재된 클래스명이 실제 코드에 존재
- [ ] API 엔드포인트가 실제 컨트롤러와 일치
- [ ] 이벤트 발행/구독 상태가 실제 코드와 일치
- [ ] 테스트 클래스명이 실제 테스트 파일과 일치

---

## Feature ID 프리픽스 참조

| PREFIX | 도메인          |
|--------|--------------|
| AUTH   | auth         |
| USER   | user         |
| MOM    | moment       |
| CMT    | comment      |
| GRP    | group        |
| LIK    | like         |
| NTF    | notification |
| RPT    | report       |
| STG    | storage      |
| ADM    | admin        |
| GLB    | global       |

## Status 값 참조

| Status        | 의미           |
|---------------|--------------|
| `DONE`        | 테스트 포함 완전 구현 |
| `IN_PROGRESS` | 현재 구현 중      |
| `PLANNED`     | 설계만 완료       |
| `DEPRECATED`  | 제거 예정        |

## 주의사항

1. **코드 기반으로만 작성**: 추측하지 않고 반드시 코드를 스캔하여 확인
2. **최소 변경**: 영향받는 부분만 수정, 관련 없는 기존 내용은 건드리지 않음
3. **정합성 우선**: 숫자, 클래스명, API 경로 등은 반드시 실제 코드와 일치
4. **중복 방지**: 이미 문서화된 기능을 다시 추가하지 않음
5. **이벤트 상태 정확히**: publishEvent() 코드와 @TransactionalEventListener 핸들러 존재 여부를 모두 확인하여 상태 결정
