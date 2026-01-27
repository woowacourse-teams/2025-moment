# 배치 조회 패턴 기술 보고서

## 1. 개요

### 1.1 배경
그룹 내 나의 모멘트 조회 API에서 각 댓글의 `likeCount`, `hasLiked` 정보를 추가할 때 발생하는 N+1 문제를 해결하기 위해 배치 조회 패턴을 도입했습니다.

### 1.2 적용 대상 API
- `GET /api/v2/groups/{groupId}/my-moments`
- `GET /api/v2/groups/{groupId}/my-moments/unread`
- `GET /api/v2/groups/{groupId}/my-comments`
- `GET /api/v2/groups/{groupId}/my-comments/unread`

---

## 2. 문제 분석

### 2.1 N+1 문제란?
- 1개의 목록 조회 쿼리 + N개의 연관 데이터 조회 쿼리가 발생하는 문제
- 데이터가 늘어날수록 쿼리 수가 선형적으로 증가

### 2.2 구체적 문제 상황
```
페이지 사이즈: 10개 모멘트
평균 댓글 수: 5개/모멘트
총 댓글 수: 50개

단건 조회 시:
- likeCount 조회: 50회 (countByCommentId)
- hasLiked 조회: 50회 (existsByCommentIdAndMemberId)
- 총 쿼리: 100회
```

### 2.3 성능 영향
| 항목 | 단건 조회 | 배치 조회 |
|------|----------|----------|
| 쿼리 수 | 100회 | 2회 |
| 네트워크 라운드트립 | 100회 | 2회 |
| DB 커넥션 점유 | 높음 | 낮음 |
| 응답 시간 | 느림 | 빠름 |

---

## 3. 해결 방안: 배치 조회 패턴

### 3.1 Repository 레이어
```java
// 여러 댓글의 좋아요 수 배치 조회
@Query("""
    SELECT cl.comment.id, COUNT(cl)
    FROM CommentLike cl
    WHERE cl.comment.id IN :commentIds
    GROUP BY cl.comment.id
    """)
List<Object[]> countByCommentIdIn(@Param("commentIds") List<Long> commentIds);

// 특정 멤버가 좋아요한 댓글 ID 목록 조회
@Query("""
    SELECT cl.comment.id
    FROM CommentLike cl
    WHERE cl.comment.id IN :commentIds AND cl.member.id = :memberId
    """)
List<Long> findLikedCommentIds(
    @Param("commentIds") List<Long> commentIds,
    @Param("memberId") Long memberId
);
```

### 3.2 Service 레이어
```java
public Map<Long, Long> getCountsByCommentIds(List<Long> commentIds) {
    if (commentIds == null || commentIds.isEmpty()) {
        return Collections.emptyMap();
    }
    List<Object[]> results = likeRepository.countByCommentIdIn(commentIds);
    return results.stream()
            .collect(Collectors.toMap(
                    row -> (Long) row[0],
                    row -> (Long) row[1]
            ));
}

public Set<Long> getLikedCommentIds(List<Long> commentIds, Long memberId) {
    if (commentIds == null || commentIds.isEmpty()) {
        return Collections.emptySet();
    }
    return new HashSet<>(likeRepository.findLikedCommentIds(commentIds, memberId));
}
```

### 3.3 Facade 레이어
```java
// 모든 댓글 ID 수집
List<Long> allCommentIds = allComments.stream()
        .map(CommentComposition::id)
        .toList();

// 배치 조회
Map<Long, Long> commentLikeCountMap = commentLikeService.getCountsByCommentIds(allCommentIds);
Set<Long> likedCommentIds = commentLikeService.getLikedCommentIds(allCommentIds, memberId);

// 각 댓글에 적용
compositions.stream()
    .map(composition -> {
        long likeCount = commentLikeCountMap.getOrDefault(composition.id(), 0L);
        boolean hasLiked = likedCommentIds.contains(composition.id());
        return MyGroupMomentCommentResponse.of(composition, likeCount, hasLiked);
    })
    .toList();
```

---

## 4. 현재 시스템에서의 IN절 크기 분석

### 4.1 페이지네이션 설정
- `DEFAULT_PAGE_SIZE = 10` (한 페이지에 모멘트 10개)

### 4.2 예상 IN절 크기
| 시나리오 | 모멘트 수 | 평균 댓글/모멘트 | IN절 크기 |
|----------|----------|-----------------|----------|
| 최소 | 1 | 1 | 1 |
| 일반 | 10 | 5 | 50 |
| 활발 | 10 | 10 | 100 |
| 극단 | 10 | 20 | 200 |

### 4.3 MySQL IN절 성능 특성
- **1,000개 이하**: 성능 문제 없음
- **1,000~10,000개**: 주의 필요, 인덱스 필수
- **10,000개 이상**: 분할 처리 권장

현재 시스템은 최대 200개 수준으로 **전혀 문제없는 범위**입니다.

---

## 5. IN절이 과도하게 늘어날 경우 해결책

### 5.1 해결책 1: 청크 분할 처리 (Chunk Processing)

IN절 크기가 임계값을 초과할 경우 여러 번에 나눠서 조회합니다.

```java
private static final int BATCH_CHUNK_SIZE = 1000;

public Map<Long, Long> getCountsByCommentIds(List<Long> commentIds) {
    if (commentIds == null || commentIds.isEmpty()) {
        return Collections.emptyMap();
    }

    // 청크 분할 처리
    Map<Long, Long> result = new HashMap<>();
    for (int i = 0; i < commentIds.size(); i += BATCH_CHUNK_SIZE) {
        List<Long> chunk = commentIds.subList(
            i,
            Math.min(i + BATCH_CHUNK_SIZE, commentIds.size())
        );
        List<Object[]> chunkResults = likeRepository.countByCommentIdIn(chunk);
        chunkResults.forEach(row ->
            result.put((Long) row[0], (Long) row[1])
        );
    }
    return result;
}
```

**장점**: 구현 단순, 기존 코드 변경 최소화
**단점**: 청크 수만큼 쿼리 증가

### 5.2 해결책 2: 임시 테이블 활용 (Temporary Table)

대량의 ID를 임시 테이블에 삽입 후 JOIN하는 방식입니다.

```sql
-- 1. 임시 테이블 생성
CREATE TEMPORARY TABLE temp_comment_ids (id BIGINT PRIMARY KEY);

-- 2. 배치 INSERT
INSERT INTO temp_comment_ids VALUES (1), (2), (3), ...;

-- 3. JOIN으로 조회
SELECT cl.comment_id, COUNT(cl.id)
FROM comment_likes cl
INNER JOIN temp_comment_ids t ON cl.comment_id = t.id
GROUP BY cl.comment_id;

-- 4. 임시 테이블 삭제
DROP TEMPORARY TABLE temp_comment_ids;
```

**장점**: 매우 큰 IN절에서도 성능 유지
**단점**: 트랜잭션 관리 복잡, 구현 복잡도 증가

### 5.3 해결책 3: 비정규화 (Denormalization)

좋아요 수를 Comment 엔티티에 직접 저장합니다.

```java
@Entity
public class Comment {
    // 기존 필드...

    @Column(name = "like_count")
    private long likeCount = 0;

    public void incrementLikeCount() {
        this.likeCount++;
    }

    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }
}
```

**장점**: 조회 시 추가 쿼리 불필요
**단점**:
- 좋아요 토글 시 Comment 엔티티도 업데이트 필요
- 동시성 문제 고려 필요 (낙관적 락 또는 비관적 락)
- 데이터 정합성 관리 필요

### 5.4 해결책 4: 캐싱 (Caching)

Redis 등을 활용하여 좋아요 수를 캐싱합니다.

```java
@Cacheable(value = "commentLikeCounts", key = "#commentId")
public long getCount(Long commentId) {
    return likeRepository.countByCommentId(commentId);
}

@CacheEvict(value = "commentLikeCounts", key = "#commentId")
public void evictCount(Long commentId) {
    // 캐시 무효화
}
```

**장점**: DB 부하 감소, 응답 속도 향상
**단점**:
- 캐시 인프라 필요
- 캐시 무효화 전략 필요
- 실시간성 저하 가능

### 5.5 권장 해결책 선택 가이드

| 상황 | 권장 해결책 |
|------|------------|
| IN절 1,000개 미만 | 현재 방식 유지 |
| IN절 1,000~10,000개 | 청크 분할 처리 |
| IN절 10,000개 이상 | 비정규화 또는 캐싱 |
| 읽기 부하가 매우 높음 | 캐싱 + 비정규화 |
| 실시간성이 중요 | 비정규화 (동시성 고려) |

---

## 6. 인덱스 권장사항

### 6.1 현재 필요한 인덱스

```sql
-- comment_likes 테이블
CREATE INDEX idx_comment_likes_comment_id ON comment_likes(comment_id);
CREATE INDEX idx_comment_likes_comment_member ON comment_likes(comment_id, member_id);

-- moment_likes 테이블
CREATE INDEX idx_moment_likes_moment_id ON moment_likes(moment_id);
CREATE INDEX idx_moment_likes_moment_member ON moment_likes(moment_id, member_id);
```

### 6.2 쿼리 실행 계획 확인

```sql
EXPLAIN SELECT cl.comment_id, COUNT(cl.id)
FROM comment_likes cl
WHERE cl.comment_id IN (1, 2, 3, ...)
GROUP BY cl.comment_id;
```

`Using index`가 표시되면 커버링 인덱스로 최적화된 것입니다.

---

## 7. 모니터링 권장사항

### 7.1 주요 모니터링 지표
- 쿼리 실행 시간 (Slow Query Log)
- IN절 평균 크기
- 응답 시간 (P50, P95, P99)
- DB 커넥션 풀 사용률

### 7.2 알림 임계값 설정
- 쿼리 실행 시간 > 100ms: 경고
- IN절 크기 > 500: 주의
- 응답 시간 P95 > 500ms: 경고

---

## 8. 결론

### 8.1 현재 상태
- 배치 조회 패턴 적용 완료
- IN절 크기: 최대 200개 수준 (안전 범위)
- N+1 문제 해결: 100회 → 2회 쿼리

### 8.2 향후 확장 시
- IN절 1,000개 초과 시 청크 분할 처리 도입
- 읽기 부하 증가 시 캐싱 또는 비정규화 검토
- 적절한 인덱스 유지 및 쿼리 성능 모니터링

### 8.3 관련 파일
- `CommentLikeRepository.java` - 배치 조회 쿼리
- `CommentLikeService.java` - 배치 조회 서비스
- `MyGroupMomentPageFacadeService.java` - 배치 조회 적용
- `MyGroupCommentPageFacadeService.java` - 배치 조회 적용

---

*작성일: 2026-01-25*
*작성자: Claude Opus 4.5*
