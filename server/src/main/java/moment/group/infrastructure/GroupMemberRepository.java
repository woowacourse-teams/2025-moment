package moment.group.infrastructure;

import java.util.List;
import java.util.Optional;
import moment.group.domain.GroupMember;
import moment.group.domain.MemberStatus;
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
}
