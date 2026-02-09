package moment.comment.infrastructure;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import moment.comment.domain.Comment;
import moment.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("""
                SELECT c.id
                FROM comments c
                WHERE c.commenter = :commenter
                ORDER BY c.createdAt DESC, c.id DESC
            """)
    List<Long> findFirstPageCommentIdsByCommenter(@Param("commenter") User commenter, Pageable pageable);

    @Query("""
            SELECT c
            FROM comments c
            WHERE c.id IN :ids
            ORDER BY c.createdAt DESC, c.id DESC
            """)
    List<Comment> findCommentsByIds(@Param("ids") List<Long> ids);

    @Query("""
            SELECT c
            FROM comments c
            WHERE c.id IN :ids
            ORDER BY c.createdAt DESC, c.id DESC
            """)
    List<Comment> findUnreadCommentsFirstPage(@Param("ids") List<Long> ids, Pageable pageable);

    @Query("""
                SELECT c.id
                FROM comments c
                WHERE c.commenter = :commenter AND (c.createdAt < :cursorTime OR (c.createdAt = :cursorTime AND c.id < :cursorId))
                ORDER BY c.createdAt DESC, c.id DESC
            """)
    List<Long> findNextPageCommentIdsByCommenter(@Param("commenter") User commenter,
                                                 @Param("cursorTime") LocalDateTime cursorDateTime,
                                                 @Param("cursorId") Long cursorId,
                                                 Pageable pageable);

    @Query("""
            SELECT c FROM comments c
            WHERE c.id IN :ids AND (c.createdAt < :cursorTime OR (c.createdAt = :cursorTime AND c.id < :cursorId))
            ORDER BY c.createdAt DESC, c.id DESC
            """)
    List<Comment> findUnreadCommentsNextPage(@Param("ids") List<Long> ids,
                                             @Param("cursorTime") LocalDateTime cursorDateTime,
                                             @Param("cursorId") Long cursorId,
                                             Pageable pageable);

    List<Comment> findAllByMomentIdIn(List<Long> momentIds);

    @Query("""
               SELECT m.id
               FROM moments m
               WHERE m.id IN :momentIds
               AND NOT EXISTS (
                   SELECT 1
                   FROM comments c
                   WHERE c.momentId = m.id
                   AND c.commenter.id = :commenterId
                  )
            """)
    List<Long> findMomentIdsNotCommentedOnByMe(@Param("momentIds") List<Long> momentIds,
                                               @Param("commenterId") Long commenterId);

    boolean existsByMomentIdAndCommenterId(Long momentId, Long commenterId);

    @Query("SELECT c.momentId FROM comments c WHERE c.id = :commentId")
    Optional<Long> findMomentIdById(Long commentId);

    long countByMomentId(Long momentId);

    @Query("""
        SELECT COUNT(c) FROM comments c
        WHERE c.momentId = :momentId
          AND c.commenter.id NOT IN :blockedUserIds
        """)
    long countByMomentIdExcludingBlocked(
            @Param("momentId") Long momentId,
            @Param("blockedUserIds") List<Long> blockedUserIds);

    // 그룹 내 나의 Comment 첫 페이지 조회 (member_id 기준)
    @Query("""
            SELECT c
            FROM comments c
            LEFT JOIN FETCH c.member
            WHERE c.member.id = :memberId
            ORDER BY c.id DESC
            """)
    List<Comment> findByMemberIdOrderByIdDesc(
            @Param("memberId") Long memberId,
            Pageable pageable);

    // 그룹 내 나의 Comment 다음 페이지 조회 (커서 기반)
    @Query("""
            SELECT c
            FROM comments c
            LEFT JOIN FETCH c.member
            WHERE c.member.id = :memberId AND c.id < :cursor
            ORDER BY c.id DESC
            """)
    List<Comment> findByMemberIdAndIdLessThanOrderByIdDesc(
            @Param("memberId") Long memberId,
            @Param("cursor") Long cursor,
            Pageable pageable);

    // 그룹 내 읽지 않은 나의 Comment 첫 페이지 조회
    @Query("""
            SELECT c
            FROM comments c
            LEFT JOIN FETCH c.member
            WHERE c.member.id = :memberId AND c.id IN :commentIds
            ORDER BY c.id DESC
            """)
    List<Comment> findByMemberIdAndIdInOrderByIdDesc(
            @Param("memberId") Long memberId,
            @Param("commentIds") List<Long> commentIds,
            Pageable pageable);

    // 그룹 내 읽지 않은 나의 Comment 다음 페이지 조회 (커서 기반)
    @Query("""
            SELECT c
            FROM comments c
            LEFT JOIN FETCH c.member
            WHERE c.member.id = :memberId AND c.id IN :commentIds AND c.id < :cursor
            ORDER BY c.id DESC
            """)
    List<Comment> findByMemberIdAndIdInAndIdLessThanOrderByIdDesc(
            @Param("memberId") Long memberId,
            @Param("commentIds") List<Long> commentIds,
            @Param("cursor") Long cursor,
            Pageable pageable);

    // ===== Admin 그룹 삭제/복원용 메서드 =====

    /**
     * 특정 모멘트들에 달린 코멘트 전체 Soft Delete
     */
    @Modifying
    @Query(value = "UPDATE comments SET deleted_at = NOW() WHERE moment_id IN :momentIds AND deleted_at IS NULL", nativeQuery = true)
    void softDeleteByMomentIds(@Param("momentIds") List<Long> momentIds);

    /**
     * 특정 모멘트들에 달린 코멘트 전체 복원
     */
    @Modifying
    @Query(value = "UPDATE comments SET deleted_at = NULL WHERE moment_id IN :momentIds AND deleted_at IS NOT NULL", nativeQuery = true)
    void restoreByMomentIds(@Param("momentIds") List<Long> momentIds);

    // ===== Admin 멤버 강제 추방용 메서드 =====

    /**
     * 특정 멤버의 모든 코멘트 Soft Delete
     */
    @Modifying
    @Query(value = "UPDATE comments SET deleted_at = NOW() WHERE member_id = :memberId AND deleted_at IS NULL", nativeQuery = true)
    int softDeleteByMemberId(@Param("memberId") Long memberId);

    // ===== Admin 콘텐츠 관리용 메서드 =====

    /**
     * 모멘트의 코멘트 목록 페이지네이션 조회
     */
    @Query("""
          SELECT c
          FROM comments c
          JOIN FETCH c.member mem
          JOIN FETCH mem.user
          WHERE c.momentId = :momentId
          """)
    Page<Comment> findByMomentId(@Param("momentId") Long momentId, Pageable pageable);

    /**
     * 그룹 내 특정 코멘트 조회
     */
    @Query("""
          SELECT c
          FROM comments c
          JOIN FETCH c.member mem
          JOIN FETCH mem.user
          JOIN moments m ON c.momentId = m.id
          WHERE c.id = :commentId AND m.group.id = :groupId
          """)
    Optional<Comment> findByIdAndGroupId(@Param("commentId") Long commentId, @Param("groupId") Long groupId);

    /**
     * 특정 모멘트의 코멘트 전체 Soft Delete
     */
    @Modifying
    @Query(value = "UPDATE comments SET deleted_at = NOW() WHERE moment_id = :momentId AND deleted_at IS NULL", nativeQuery = true)
    int softDeleteByMomentId(@Param("momentId") Long momentId);
}
