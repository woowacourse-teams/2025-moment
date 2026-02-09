---
description: 5인 리뷰어 팀을 구성하여 구현 계획(plan) 파일을 다각도로 검증합니다. 아키텍처/DB/보안/도메인/반론 관점에서 리뷰 후 종합 보고서를 작성합니다.
---

# Review Plan Command

$ARGUMENTS 파일의 구현 계획을 리뷰하기 위한 에이전트 팀을 구성하세요.

## 팀 구성 (5명)

### 1. architecture-reviewer (아키텍처 리뷰어)
- 모델: sonnet
- 서브에이전트 타입: Explore (읽기 전용)
- 역할: Clean Architecture 레이어 규칙, DDD 원칙, 모듈 간 의존성 방향 검증
- 검토 항목:
  - 레이어 간 의존성 방향이 올바른지 (presentation → service → domain, 역방향 금지)
  - Application Service ↔ Domain Service 역할 분리가 적절한지
  - 외부 모듈이 반드시 Application Service를 통해 접근하는지
  - Facade Service의 책임이 과도하지 않은지
  - 기존 프로젝트의 모듈 구조(report/, like/ 등)와 일관성이 있는지
- 참고할 파일: .claude/CLAUDE.md, .claude/docs/features/FEATURES.md, 기존 like/, report/ 모듈 구조

### 2. database-reviewer (DB/쿼리 리뷰어)
- 모델: sonnet
- 서브에이전트 타입: Explore (읽기 전용)
- 역할: DDL 설계, 인덱스 전략, JPQL 쿼리 성능, Flyway 마이그레이션 검증
- 검토 항목:
  - DDL이 기존 마이그레이션 컨벤션(V32 등)과 일치하는지
  - UNIQUE KEY와 인덱스 전략이 쿼리 패턴에 최적화되어 있는지
  - 쿼리 성능 문제 (NOT IN 대량 데이터, 실행 계획, 인덱스 활용)
  - 더미값이나 매직 넘버 패턴의 안전성과 대안
  - 네이티브 쿼리 사용 시 H2 호환성
- 참고할 파일: src/main/resources/db/migration/mysql/ 디렉토리, 기존 Repository 파일들

### 3. security-reviewer (보안 리뷰어)
- 모델: sonnet
- 서브에이전트 타입: Explore (읽기 전용)
- 역할: 인가 취약점, IDOR, 비즈니스 로직 우회 가능성 검증
- 검토 항목:
  - 인증된 사용자만 자신의 리소스를 관리할 수 있는지 (IDOR 방지)
  - 비즈니스 로직 우회 가능한 시나리오가 없는지
  - Soft Delete 상태의 레코드를 통한 정보 노출 가능성
  - Race condition: 동시 요청 시 데이터 정합성
  - 조회 필터링뿐 아니라 쓰기(생성/수정) 경로에서도 검증이 되는지
- 참고할 파일: auth/ 모듈의 인증 흐름, 기존 report/ 모듈의 인가 패턴

### 4. domain-reviewer (도메인/비즈니스 로직 리뷰어)
- 모델: sonnet
- 서브에이전트 타입: Explore (읽기 전용)
- 역할: 비즈니스 규칙 정합성, 엣지 케이스, 이벤트 흐름 검증
- 검토 항목:
  - 비즈니스 정책이 모든 조회/쓰기 경로에서 일관되게 적용되는지
  - 기존 콘텐츠의 처리 정책 (삭제 vs 숨김)이 명확한지
  - 알림 필터링에서 누락된 이벤트 타입이 없는지
  - 상태 변경 후 기존 데이터에 대한 영향 (소급 적용 여부)
  - 엣지 케이스: count 불일치, 빈 결과, 동시 상태 변경 등
- 참고할 파일: .claude/docs/features/ 관련 도메인 문서들, NotificationEventHandler.java, 관련 ApplicationService 파일들

### 5. devils-advocate (반론 제기자)
- 모델: sonnet
- 서브에이전트 타입: Explore (읽기 전용)
- 역할: 계획의 약점, 누락된 시나리오, 과도한 복잡성 지적
- 검토 항목:
  - 이 설계가 과도하게 복잡한 부분은 없는지 (더 단순한 대안 제시)
  - 구현 순서에서 리스크가 높은 단계는 어디인지
  - 테스트 계획이 충분한지, 누락된 테스트 시나리오
  - 향후 확장성 문제
  - Java 메모리 필터링 vs DB 쿼리 필터링 선택의 트레이드오프
  - 다른 팀원들의 발견 사항에 대해 적극적으로 반론 제기

## 팀 운영 규칙

1. 모든 팀원은 먼저 대상 plan 파일을 읽고, 필요한 기존 코드 파일을 탐색한 뒤 리뷰를 시작할 것
2. architecture-reviewer, database-reviewer, security-reviewer, domain-reviewer 4명을 **동시에** 스폰
3. devils-advocate도 **동시에** 스폰하여 독자적 분석을 먼저 수행
4. 4명의 리뷰어 보고가 완료되면 devils-advocate에게 다른 리뷰어들의 발견 사항을 전달하여 반론 요청
5. 리더는 위임 모드(delegation mode)로 운영하여 직접 코드를 건드리지 않고 조율에만 집중
6. 모든 팀원의 리뷰가 끝날 때까지 기다린 후 최종 보고서를 작성

## 보고 형식

각 리뷰어는 다음 형식으로 보고합니다:

```
### CRITICAL (반드시 수정)
- [영역] 문제 설명 + 수정 제안

### HIGH (강력 권고)
- [영역] 문제 설명 + 수정 제안

### MEDIUM (개선 권고)
- [영역] 문제 설명 + 수정 제안

### SUGGESTION (참고)
- [영역] 제안 사항

### GOOD (잘된 점)
- [영역] 긍정적 평가
```

## 최종 보고서 형식

리더는 모든 리뷰를 종합하여 다음 형식으로 최종 보고서를 작성합니다:

### CRITICAL (반드시 수정)
- [영역] 문제 설명 + 수정 제안 (발견자 명시)

### HIGH (강력 권고)
- [영역] 문제 설명 + 수정 제안 (발견자 명시)

### MEDIUM (개선 권고)
- [영역] 문제 설명 + 수정 제안 (발견자 명시)

### SUGGESTION (참고)
- [영역] 제안 사항

### GOOD (잘된 점)
- [영역] 긍정적 평가

devils-advocate의 반론을 반영하여 심각도를 조정한 근거를 포함합니다.

## 실행 흐름

```
1. 팀 생성 (TeamCreate)
2. 태스크 5개 생성 (TaskCreate)
3. 5명 에이전트 동시 스폰 (Task x5, run_in_background)
4. 4명 리뷰어 보고 수신 대기
5. 4명 보고 완료 → devils-advocate에게 전달 (SendMessage)
6. devils-advocate 반론 수신
7. 최종 종합 보고서 작성
8. 팀 종료 (shutdown_request → TeamDelete)
```
