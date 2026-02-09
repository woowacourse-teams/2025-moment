package moment.moment.infrastructure;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import moment.moment.domain.Moment;
import moment.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MomentRepository extends JpaRepository<Moment, Long> {

    @Query("""
            SELECT m FROM moments m
            JOIN FETCH m.momenter
            WHERE m.momenter = :momenter
            ORDER BY m.createdAt DESC, m.id DESC
            """)
    List<Moment> findMyMomentFirstPage(@Param("momenter") User momenter, Pageable pageable);

    @Query("""
            SELECT m FROM moments m
            JOIN FETCH m.momenter
            WHERE m.id IN :ids
            ORDER BY m.createdAt DESC, m.id DESC
            """)
    List<Moment> findMyUnreadMomentFirstPage(@Param("ids") List<Long> ids, Pageable pageable);

    @Query("""
            SELECT m FROM moments m
            JOIN FETCH m.momenter
            WHERE m.momenter = :momenter AND (m.createdAt < :cursorTime OR (m.createdAt = :cursorTime AND m.id < :cursorId))
            ORDER BY m.createdAt DESC, m.id DESC
            """)
    List<Moment> findMyMomentsNextPage(@Param("momenter") User momenter,
                                       @Param("cursorTime") LocalDateTime cursorDateTime,
                                       @Param("cursorId") Long cursorId,
                                       Pageable pageable);

    @Query("""
            SELECT m FROM moments m
            JOIN FETCH m.momenter
            WHERE m.id IN :ids AND (m.createdAt < :cursorTime OR (m.createdAt = :cursorTime AND m.id < :cursorId))
            ORDER BY m.createdAt DESC, m.id DESC
            """)
    List<Moment> findMyUnreadMomentNextPage(@Param("ids") List<Long> ids,
                                            @Param("cursorTime") LocalDateTime cursorDateTime,
                                            @Param("cursorId") Long cursorId,
                                            Pageable pageable);

    @Query("""
        SELECT m.id FROM moments m
        WHERE 
            m.momenter.id <> :userId
            AND m.createdAt >= :someDaysAgo
            AND m.id NOT IN :reportedMoments 
    """)
    List<Long> findMomentIdsExcludingReported(@Param("userId") Long userId,
                                              @Param("someDaysAgo") LocalDateTime someDaysAgo,
                                              @Param("reportedMoments") List<Long> reportedMoments);

    @Query("""
        SELECT m.id FROM moments m
        WHERE 
            m.momenter.id <> :userId
            AND m.createdAt >= :someDaysAgo
    """)
    List<Long> findMomentIds(
            @Param("userId") Long userId,
            @Param("someDaysAgo") LocalDateTime someDaysAgo);

    void deleteById(Long momentId);

    @Query("""
          SELECT m
          FROM moments m
          LEFT JOIN FETCH m.momenter
          LEFT JOIN FETCH m.member
          WHERE m.id IN :momentIds
           """)
    List<Moment> findAllWithMomenterAndMemberByIds(@Param("momentIds")List<Long> momentIds);

    @Query("""
          SELECT m
          FROM moments m
          LEFT JOIN FETCH m.momenter
          LEFT JOIN FETCH m.member
          WHERE m.group.id = :groupId
            AND m.momenter.id NOT IN :blockedUserIds
          ORDER BY m.id DESC
           """)
    List<Moment> findByGroupIdOrderByIdDesc(
        @Param("groupId") Long groupId,
        @Param("blockedUserIds") List<Long> blockedUserIds,
        Pageable pageable);

    @Query("""
          SELECT m
          FROM moments m
          LEFT JOIN FETCH m.momenter
          LEFT JOIN FETCH m.member
          WHERE m.group.id = :groupId AND m.id < :cursor
            AND m.momenter.id NOT IN :blockedUserIds
          ORDER BY m.id DESC
           """)
    List<Moment> findByGroupIdAndIdLessThanOrderByIdDesc(
        @Param("groupId") Long groupId,
        @Param("cursor") Long cursor,
        @Param("blockedUserIds") List<Long> blockedUserIds,
        Pageable pageable);

    @Query("""
          SELECT m
          FROM moments m
          LEFT JOIN FETCH m.momenter
          LEFT JOIN FETCH m.member
          WHERE m.group.id = :groupId AND m.member.id = :memberId
          ORDER BY m.id DESC
           """)
    List<Moment> findByGroupIdAndMemberIdOrderByIdDesc(
        @Param("groupId") Long groupId,
        @Param("memberId") Long memberId,
        Pageable pageable);

    @Query("""
          SELECT m
          FROM moments m
          LEFT JOIN FETCH m.momenter
          LEFT JOIN FETCH m.member
          WHERE m.group.id = :groupId AND m.member.id = :memberId AND m.id < :cursor
          ORDER BY m.id DESC
           """)
    List<Moment> findByGroupIdAndMemberIdAndIdLessThanOrderByIdDesc(
        @Param("groupId") Long groupId,
        @Param("memberId") Long memberId,
        @Param("cursor") Long cursor,
        Pageable pageable);

    @Query("""
        SELECT m.id FROM moments m
        WHERE
            m.group.id = :groupId
            AND m.momenter.id <> :userId
            AND m.createdAt >= :someDaysAgo
            AND m.momenter.id NOT IN :blockedUserIds
    """)
    List<Long> findMomentIdsInGroup(
            @Param("groupId") Long groupId,
            @Param("userId") Long userId,
            @Param("someDaysAgo") LocalDateTime someDaysAgo,
            @Param("blockedUserIds") List<Long> blockedUserIds);

    @Query("""
        SELECT m.id FROM moments m
        WHERE
            m.group.id = :groupId
            AND m.momenter.id <> :userId
            AND m.createdAt >= :someDaysAgo
            AND m.id NOT IN :reportedMoments
            AND m.momenter.id NOT IN :blockedUserIds
    """)
    List<Long> findMomentIdsInGroupExcludingReported(
            @Param("groupId") Long groupId,
            @Param("userId") Long userId,
            @Param("someDaysAgo") LocalDateTime someDaysAgo,
            @Param("reportedMoments") List<Long> reportedMoments,
            @Param("blockedUserIds") List<Long> blockedUserIds);

    @Query("""
          SELECT m
          FROM moments m
          LEFT JOIN FETCH m.momenter
          LEFT JOIN FETCH m.member
          WHERE m.group.id = :groupId
            AND m.member.id = :memberId
            AND m.id IN :momentIds
          ORDER BY m.id DESC
           """)
    List<Moment> findByGroupIdAndMemberIdAndIdIn(
            @Param("groupId") Long groupId,
            @Param("memberId") Long memberId,
            @Param("momentIds") List<Long> momentIds,
            Pageable pageable);

    @Query("""
          SELECT m
          FROM moments m
          LEFT JOIN FETCH m.momenter
          LEFT JOIN FETCH m.member
          WHERE m.group.id = :groupId
            AND m.member.id = :memberId
            AND m.id IN :momentIds
            AND m.id < :cursor
          ORDER BY m.id DESC
           """)
    List<Moment> findByGroupIdAndMemberIdAndIdInAndIdLessThan(
            @Param("groupId") Long groupId,
            @Param("memberId") Long memberId,
            @Param("momentIds") List<Long> momentIds,
            @Param("cursor") Long cursor,
            Pageable pageable);

    // ===== Admin 그룹 삭제/복원용 메서드 =====

    /**
     * 그룹의 모든 모멘트 Soft Delete
     */
    @Modifying
    @Query(value = "UPDATE moments SET deleted_at = NOW() WHERE group_id = :groupId AND deleted_at IS NULL", nativeQuery = true)
    void softDeleteByGroupId(@Param("groupId") Long groupId);

    /**
     * 그룹의 모든 모멘트 복원
     */
    @Modifying
    @Query(value = "UPDATE moments SET deleted_at = NULL WHERE group_id = :groupId AND deleted_at IS NOT NULL", nativeQuery = true)
    void restoreByGroupId(@Param("groupId") Long groupId);

    /**
     * 그룹의 모든 모멘트 ID 조회 (코멘트 삭제용)
     */
    @Query(value = "SELECT id FROM moments WHERE group_id = :groupId", nativeQuery = true)
    List<Long> findAllIdsByGroupId(@Param("groupId") Long groupId);

    // ===== Admin 멤버 강제 추방용 메서드 =====

    /**
     * 특정 멤버의 모든 모멘트 Soft Delete
     */
    @Modifying
    @Query(value = "UPDATE moments SET deleted_at = NOW() WHERE member_id = :memberId AND deleted_at IS NULL", nativeQuery = true)
    int softDeleteByMemberId(@Param("memberId") Long memberId);

    /**
     * 특정 멤버의 모든 모멘트 ID 조회 (코멘트 삭제용)
     */
    @Query(value = "SELECT id FROM moments WHERE member_id = :memberId", nativeQuery = true)
    List<Long> findAllIdsByMemberId(@Param("memberId") Long memberId);

    // ===== Admin 콘텐츠 관리용 메서드 =====

    /**
     * 그룹의 모멘트 목록 페이지네이션 조회
     */
    @Query("""
          SELECT m
          FROM moments m
          JOIN FETCH m.member mem
          JOIN FETCH mem.user
          WHERE m.group.id = :groupId
          """)
    Page<Moment> findByGroupId(@Param("groupId") Long groupId, Pageable pageable);

    /**
     * 그룹 내 특정 모멘트 조회
     */
    @Query("""
          SELECT m
          FROM moments m
          JOIN FETCH m.member mem
          JOIN FETCH mem.user
          WHERE m.id = :momentId AND m.group.id = :groupId
          """)
    Optional<Moment> findByIdAndGroupId(@Param("momentId") Long momentId, @Param("groupId") Long groupId);
}
