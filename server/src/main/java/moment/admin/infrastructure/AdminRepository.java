package moment.admin.infrastructure;

import moment.admin.domain.Admin;
import moment.admin.domain.AdminRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    // 기존 메서드
    Optional<Admin> findByEmail(String email);

    boolean existsByEmail(String email);

    // ===== 새로 추가된 메서드 =====

    /**
     * 특정 역할의 관리자 수 조회 (마지막 SUPER_ADMIN 체크용)
     * @param role 관리자 역할
     * @return 해당 역할의 관리자 수
     */
    long countByRole(AdminRole role);

    /**
     * 모든 관리자 조회 (차단된 것 포함, 페이징)
     * @param pageable 페이징 정보
     * @return 관리자 페이지
     */
    @Query("SELECT a FROM admins a WHERE a.deletedAt IS NULL OR a.deletedAt IS NOT NULL ORDER BY a.createdAt DESC")
    Page<Admin> findAllIncludingDeleted(Pageable pageable);

    /**
     * 차단된 관리자 복원 (Soft Delete 해제)
     * @param id 관리자 ID
     */
    @Modifying
    @Query("UPDATE admins a SET a.deletedAt = NULL WHERE a.id = :id")
    void restoreDeleted(@Param("id") Long id);
}
