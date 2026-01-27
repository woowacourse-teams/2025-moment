package moment.group.infrastructure;

import java.util.List;
import java.util.Optional;
import moment.group.domain.GroupMember;
import moment.group.domain.MemberStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    List<GroupMember> findByGroupIdAndStatus(Long groupId, MemberStatus status);

    Optional<GroupMember> findByGroupIdAndUserId(Long groupId, Long userId);

    @Query(value = "SELECT * FROM group_members m WHERE m.group_id = :groupId AND m.user_id = :userId",
           nativeQuery = true)
    Optional<GroupMember> findByGroupIdAndUserIdIncludeDeleted(
        @Param("groupId") Long groupId,
        @Param("userId") Long userId
    );

    boolean existsByGroupIdAndNicknameAndDeletedAtIsNull(Long groupId, String nickname);

    @Query("SELECT m FROM GroupMember m WHERE m.user.id = :userId AND m.status = 'APPROVED'")
    List<GroupMember> findApprovedMembershipsByUserId(@Param("userId") Long userId);

    long countByGroupIdAndStatus(Long groupId, MemberStatus status);

    // ===== Admin API용 메서드 =====

    /**
     * 전체 승인된 멤버 수 조회
     */
    @Query("SELECT COUNT(gm) FROM GroupMember gm WHERE gm.status = 'APPROVED'")
    long countTotalApprovedMembers();

    /**
     * 그룹의 승인된 멤버 목록 조회 (페이지네이션)
     */
    @Query("SELECT gm FROM GroupMember gm JOIN FETCH gm.user WHERE gm.group.id = :groupId AND gm.status = 'APPROVED'")
    Page<GroupMember> findApprovedMembersByGroupId(@Param("groupId") Long groupId, Pageable pageable);

    /**
     * 그룹의 대기 멤버 목록 조회 (페이지네이션)
     */
    @Query("SELECT gm FROM GroupMember gm JOIN FETCH gm.user WHERE gm.group.id = :groupId AND gm.status = 'PENDING'")
    Page<GroupMember> findPendingMembersByGroupId(@Param("groupId") Long groupId, Pageable pageable);

    /**
     * 그룹의 Owner 조회
     */
    @Query("SELECT gm FROM GroupMember gm JOIN FETCH gm.user WHERE gm.group.id = :groupId AND gm.role = 'OWNER'")
    Optional<GroupMember> findOwnerByGroupId(@Param("groupId") Long groupId);
}
