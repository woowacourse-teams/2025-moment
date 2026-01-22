# Admin 세션 조회 N+1 쿼리 최적화 리포트

**작성일**: 2026-01-18
**작성자**: Claude Opus 4.5
**대상 모듈**: `moment.admin.service.session.AdminSessionService`

---

## 1. 개요

### 1.1 배경

Admin 세션 관리 기능에서 세션 목록 조회 시 **N+1 쿼리 문제**가 발생하고 있었습니다.
이 문제는 데이터 양이 증가할수록 성능 저하를 야기하며, 운영 환경에서 심각한 병목이 될 수 있습니다.

### 1.2 문제 정의

세션 조회 시 각 세션마다 관리자(Admin) 정보를 개별적으로 조회하는 방식으로 인해,
**N개의 세션 조회 시 N+1번의 데이터베이스 쿼리**가 발생했습니다.

---

## 2. 문제 분석

### 2.1 기존 코드 (Before)

```java
public List<AdminSessionResponse> getAllActiveSessions() {
    // 1번째 쿼리: 모든 활성 세션 조회
    List<AdminSession> activeSessions = adminSessionRepository
        .findAllByLogoutTimeIsNullOrderByLoginTimeDesc();

    return activeSessions.stream()
            .map(session -> {
                // N번째 쿼리: 각 세션마다 Admin 개별 조회 (N+1 발생!)
                Admin admin = adminRepository.findById(session.getAdminId())
                        .orElseThrow(() -> new MomentException(ErrorCode.ADMIN_NOT_FOUND));
                return AdminSessionResponse.from(session, admin);
            })
            .collect(Collectors.toList());
}
```

### 2.2 실행되는 SQL 쿼리 예시

세션이 100개인 경우:

```sql
-- 1번째 쿼리: 세션 목록 조회
SELECT * FROM admin_sessions WHERE logout_time IS NULL ORDER BY login_time DESC;

-- 2~101번째 쿼리: 각 세션의 Admin 개별 조회 (100번 반복)
SELECT * FROM admins WHERE id = 1;
SELECT * FROM admins WHERE id = 2;
SELECT * FROM admins WHERE id = 1;  -- 중복 조회 발생 가능
SELECT * FROM admins WHERE id = 3;
... (총 100번)
```

### 2.3 문제점

| 문제 | 설명 |
|------|------|
| **쿼리 수 증가** | 세션 N개 → N+1번 쿼리 실행 |
| **네트워크 오버헤드** | 매 쿼리마다 DB 왕복 비용 발생 |
| **중복 조회** | 동일 Admin이 여러 세션을 가진 경우 중복 조회 |
| **확장성 저하** | 데이터 증가에 따라 선형적 성능 저하 |

---

## 3. 해결 방안

### 3.1 선택한 방법: 배치 조회 (Batch Fetching)

**핵심 아이디어**: 개별 조회 대신 필요한 모든 ID를 수집하여 **한 번에 조회**

```java
public List<AdminSessionResponse> getAllActiveSessions() {
    List<AdminSession> activeSessions = adminSessionRepository
        .findAllByLogoutTimeIsNullOrderByLoginTimeDesc();

    return toSessionResponses(activeSessions);  // 배치 조회 적용
}

private List<AdminSessionResponse> toSessionResponses(List<AdminSession> sessions) {
    if (sessions.isEmpty()) {
        return List.of();
    }

    // 1. 모든 adminId 수집 (중복 제거)
    Set<Long> adminIds = sessions.stream()
            .map(AdminSession::getAdminId)
            .collect(Collectors.toSet());

    // 2. 한 번의 쿼리로 모든 Admin 조회
    Map<Long, Admin> adminMap = adminRepository.findAllByIdIn(adminIds).stream()
            .collect(Collectors.toMap(Admin::getId, Function.identity()));

    // 3. 메모리에서 매핑
    return sessions.stream()
            .map(session -> {
                Admin admin = adminMap.get(session.getAdminId());
                if (admin == null) {
                    throw new MomentException(ErrorCode.ADMIN_NOT_FOUND);
                }
                return AdminSessionResponse.from(session, admin);
            })
            .collect(Collectors.toList());
}
```

### 3.2 실행되는 SQL 쿼리 (After)

세션이 100개, 관리자가 10명인 경우:

```sql
-- 1번째 쿼리: 세션 목록 조회
SELECT * FROM admin_sessions WHERE logout_time IS NULL ORDER BY login_time DESC;

-- 2번째 쿼리: 필요한 Admin 일괄 조회 (단 1번!)
SELECT * FROM admins WHERE id IN (1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
```

**결과**: 101번 → **2번**으로 쿼리 수 감소

---

## 4. 이 방법을 선택한 이유

### 4.1 대안 비교

| 방법 | 장점 | 단점 | 적합성 |
|------|------|------|--------|
| **배치 조회** | 구현 간단, 명시적, 유지보수 용이 | 추가 코드 필요 | **선택** |
| **JPA Fetch Join** | JPA 네이티브 지원 | 엔티티 간 연관관계 필요 | 부적합 |
| **@EntityGraph** | 선언적, 간단 | 연관관계 필요, 유연성 낮음 | 부적합 |
| **Hibernate @BatchSize** | 설정만으로 적용 | 전역 설정, 세밀한 제어 어려움 | 부분 적합 |
| **QueryDSL** | 강력한 쿼리 빌더 | 추가 의존성, 학습 비용 | 과도함 |

### 4.2 배치 조회를 선택한 핵심 이유

#### 1) AdminSession과 Admin은 연관관계가 없음

```java
@Entity
public class AdminSession extends BaseEntity {
    @Column(name = "admin_id", nullable = false)
    private Long adminId;  // 단순 FK 값, @ManyToOne 아님
}
```

- `@ManyToOne` 관계가 아닌 **단순 ID 참조** 방식
- JPA Fetch Join, @EntityGraph는 연관관계가 필수
- 기존 설계를 유지하면서 최적화하려면 **배치 조회가 유일한 선택**

#### 2) 명시적이고 이해하기 쉬움

```java
// 의도가 명확하게 드러남
Set<Long> adminIds = sessions.stream()
        .map(AdminSession::getAdminId)
        .collect(Collectors.toSet());

Map<Long, Admin> adminMap = adminRepository.findAllByIdIn(adminIds)...
```

- 코드만 봐도 "ID를 모아서 한 번에 조회한다"는 의도가 명확
- 디버깅, 유지보수 시 이해하기 쉬움

#### 3) 테스트 용이성

- 순수 자바 로직으로 단위 테스트 가능
- JPA 마법(Proxy, Lazy Loading)에 의존하지 않음

---

## 5. 장점과 단점

### 5.1 장점

| 장점 | 설명 |
|------|------|
| **쿼리 수 대폭 감소** | N+1 → 2번 (최대 99% 감소 가능) |
| **중복 조회 제거** | `Set`으로 중복 ID 자동 제거 |
| **네트워크 비용 절감** | DB 왕복 횟수 최소화 |
| **예측 가능한 성능** | 데이터 양과 무관하게 일정한 쿼리 수 |
| **기존 설계 유지** | 연관관계 추가 없이 최적화 |
| **명시적 코드** | 숨겨진 동작 없이 의도 명확 |

### 5.2 단점

| 단점 | 설명 | 완화 방안 |
|------|------|----------|
| **추가 코드량** | 헬퍼 메서드 작성 필요 | 재사용 가능한 유틸리티로 추출 |
| **메모리 사용** | Map에 Admin 객체 보관 | 페이징 적용으로 데이터 양 제한 |
| **IN 절 제한** | DB별 IN 절 파라미터 수 제한 | 대량 데이터 시 청크 처리 |
| **트랜잭션 고려** | 조회 사이 데이터 변경 가능성 | `@Transactional(readOnly=true)` |

### 5.3 IN 절 제한 상세

| 데이터베이스 | IN 절 최대 파라미터 수 |
|-------------|----------------------|
| MySQL | 제한 없음 (메모리 한도 내) |
| PostgreSQL | 제한 없음 |
| Oracle | 1,000개 |
| SQL Server | 2,100개 |

현재 프로젝트는 **MySQL**을 사용하므로 실질적 제한 없음.
단, 수천 개 이상 조회 시 청크 분할 권장.

---

## 6. 알아야 할 기술적 지식

### 6.1 N+1 쿼리 문제란?

**정의**: 1번의 쿼리로 N개의 엔티티를 조회한 후, 각 엔티티의 연관 데이터를 조회하기 위해 N번의 추가 쿼리가 발생하는 현상.

```
총 쿼리 수 = 1 (목록 조회) + N (연관 데이터 조회) = N+1
```

**발생 조건**:
1. 연관 데이터가 지연 로딩(Lazy Loading)으로 설정됨
2. 또는 명시적으로 개별 조회하는 코드 작성

### 6.2 해결 전략 분류

```
┌─────────────────────────────────────────────────────────┐
│                    N+1 해결 전략                          │
├─────────────────────────────────────────────────────────┤
│  JPA 연관관계 기반          │  수동 최적화                   │
│  ─────────────────────────│───────────────────────────   │
│  • Fetch Join              │  • 배치 조회 (IN 절)          │
│  • @EntityGraph            │  • DTO Projection           │
│  • @BatchSize              │  • Native Query             │
│  • @Fetch(FetchMode.SUBSELECT) │  • 캐싱                  │
└─────────────────────────────────────────────────────────┘
```

### 6.3 배치 조회 패턴

```java
// 기본 패턴
public <T, ID, R> List<R> batchFetch(
    List<T> entities,
    Function<T, ID> idExtractor,
    Function<Collection<ID>, Map<ID, ?>> batchLoader,
    BiFunction<T, ?, R> mapper
) {
    Set<ID> ids = entities.stream()
        .map(idExtractor)
        .collect(Collectors.toSet());

    Map<ID, ?> loadedMap = batchLoader.apply(ids);

    return entities.stream()
        .map(e -> mapper.apply(e, loadedMap.get(idExtractor.apply(e))))
        .collect(Collectors.toList());
}
```

### 6.4 Spring Data JPA의 findAllById vs findAllByIdIn

| 메서드 | 동작 | 권장 상황 |
|--------|------|----------|
| `findAllById(Iterable<ID>)` | JPA 표준, 내부적으로 개별 조회 가능 | 소량 데이터 |
| `findAllByIdIn(Collection<ID>)` | 명시적 IN 절, 단일 쿼리 보장 | **대량 데이터** |

```java
// Repository 정의
List<Admin> findAllByIdIn(Collection<Long> ids);

// 생성되는 SQL
SELECT * FROM admins WHERE id IN (?, ?, ?, ...);
```

### 6.5 Map 기반 조회의 시간 복잡도

| 연산 | 시간 복잡도 |
|------|------------|
| `Map.get(key)` | O(1) - 해시맵 |
| `List.contains()` | O(n) - 선형 탐색 |
| `Set.contains()` | O(1) - 해시셋 |

Map을 사용하면 N개 세션에 대해 **O(N)** 시간에 매핑 완료.

---

## 7. 성능 비교

### 7.1 이론적 분석

| 지표 | Before (N+1) | After (배치) | 개선율 |
|------|--------------|--------------|--------|
| 쿼리 수 (N=100) | 101 | 2 | **98% 감소** |
| 쿼리 수 (N=1000) | 1,001 | 2 | **99.8% 감소** |
| DB 왕복 | N+1 회 | 2회 | 대폭 감소 |
| 메모리 | 낮음 | Map 크기만큼 | 약간 증가 |

### 7.2 예상 응답 시간 (가정: 쿼리당 1ms)

| 세션 수 | Before | After | 개선 |
|---------|--------|-------|------|
| 10 | ~11ms | ~2ms | 5.5x |
| 100 | ~101ms | ~2ms | 50x |
| 1,000 | ~1,001ms | ~2ms | 500x |

---

## 8. 적용된 코드 위치

### 8.1 변경 파일

| 파일 | 변경 내용 |
|------|----------|
| `AdminRepository.java` | `findAllByIdIn()` 메서드 추가 |
| `AdminSessionService.java` | `toSessionResponses()` 헬퍼 메서드 추가 |

### 8.2 영향받는 메서드

- `getAllActiveSessions()`
- `getActiveSessionsByAdminId()`
- `getFilteredActiveSessions()`
- `getSessionHistory()`

---

## 9. 결론

### 9.1 핵심 요약

1. **문제**: N+1 쿼리로 인한 성능 저하
2. **원인**: 세션마다 Admin을 개별 조회
3. **해결**: 배치 조회 패턴 적용 (IN 절 활용)
4. **결과**: 쿼리 수 98% 이상 감소

### 9.2 향후 고려사항

- [ ] 대량 데이터 조회 시 청크 분할 적용 검토
- [ ] 쿼리 성능 모니터링 로그 추가 고려
- [ ] 캐싱 전략 검토 (자주 조회되는 Admin 정보)

### 9.3 참고 자료

- [Hibernate N+1 Select Problem](https://vladmihalcea.com/n-plus-1-query-problem/)
- [Spring Data JPA - Derived Query Methods](https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html)
- [JPA Performance Tuning](https://thorben-janssen.com/tips-to-boost-your-hibernate-performance/)

---

*이 문서는 코드 리팩토링 과정에서 적용된 최적화 기법을 설명하기 위해 작성되었습니다.*
