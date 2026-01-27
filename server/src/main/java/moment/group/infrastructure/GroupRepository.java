package moment.group.infrastructure;

import java.util.List;
import java.util.Optional;
import moment.group.domain.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GroupRepository extends JpaRepository<Group, Long> {

    Optional<Group> findById(Long id);

    @Query("SELECT g FROM Group g WHERE g.owner.id = :userId")
    List<Group> findByOwnerId(@Param("userId") Long userId);

    // ===== Admin API용 메서드 =====

    /**
     * 모든 그룹 조회 (삭제된 그룹 포함)
     */
    @Query(value = "SELECT * FROM moment_groups ORDER BY created_at DESC",
           countQuery = "SELECT COUNT(*) FROM moment_groups",
           nativeQuery = true)
    Page<Group> findAllIncludingDeleted(Pageable pageable);

    /**
     * ID로 그룹 조회 (삭제된 그룹 포함)
     */
    @Query(value = "SELECT * FROM moment_groups WHERE id = :id", nativeQuery = true)
    Optional<Group> findByIdIncludingDeleted(@Param("id") Long id);

    /**
     * 활성 그룹 수 조회
     */
    @Query("SELECT COUNT(g) FROM Group g")
    long countActiveGroups();

    /**
     * 삭제된 그룹 수 조회
     */
    @Query(value = "SELECT COUNT(*) FROM moment_groups WHERE deleted_at IS NOT NULL", nativeQuery = true)
    long countDeletedGroups();

    /**
     * 전체 그룹 수 조회 (활성 + 삭제)
     */
    @Query(value = "SELECT COUNT(*) FROM moment_groups", nativeQuery = true)
    long countAllIncludingDeleted();

    /**
     * 오늘 생성된 그룹 수 조회
     */
    @Query(value = "SELECT COUNT(*) FROM moment_groups WHERE DATE(created_at) = CURRENT_DATE", nativeQuery = true)
    long countTodayCreatedGroups();
}
