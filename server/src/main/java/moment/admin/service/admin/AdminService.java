package moment.admin.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moment.admin.domain.Admin;
import moment.admin.domain.AdminRole;
import moment.admin.infrastructure.AdminRepository;
import moment.admin.global.exception.AdminErrorCode;
import moment.admin.global.exception.AdminException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public Admin authenticateAdmin(String email, String password) {
        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new AdminException(AdminErrorCode.LOGIN_FAILED));

        // 비밀번호 검증
        if (!passwordEncoder.matches(password, admin.getPassword())) {
            throw new AdminException(AdminErrorCode.LOGIN_FAILED);
        }

        // 차단된 관리자 검증 (방어적 프로그래밍)
        // @SQLRestriction으로 인해 이미 차단된 관리자는 조회되지 않지만, 명시적으로 검증
        if (admin.isBlocked()) {
            log.warn("Blocked admin login attempt: email={}", email);
            throw new AdminException(AdminErrorCode.LOGIN_FAILED);
        }

        return admin;
    }

    public Admin getAdminById(Long id) {
        return adminRepository.findById(id)
                .orElseThrow(() -> new AdminException(AdminErrorCode.NOT_FOUND));
    }

    @Transactional
    public Admin createAdmin(String email, String name, String password) {
        return createAdmin(email, name, password, AdminRole.ADMIN);
    }

    @Transactional
    public Admin createAdmin(String email, String name, String password, AdminRole role) {
        if (adminRepository.existsByEmail(email)) {
            throw new AdminException(AdminErrorCode.DUPLICATE_EMAIL);
        }

        String hashedPassword = passwordEncoder.encode(password);
        Admin admin = new Admin(email, name, hashedPassword, role);
        return adminRepository.save(admin);
    }

    public boolean existsByEmail(String email) {
        return adminRepository.existsByEmail(email);
    }

    public void validateAdminRegistrationPermission(Long adminId) {
        Admin admin = getAdminById(adminId);
        if (!admin.canRegisterAdmin()) {
            throw new AdminException(AdminErrorCode.UNAUTHORIZED);
        }
    }

    // ===== 새로 추가된 메서드 =====

    /**
     * 모든 관리자 조회 (차단된 것 포함, 페이징)
     * @param pageable 페이징 정보
     * @return 관리자 페이지
     */
    public Page<Admin> getAllAdmins(Pageable pageable) {
        return adminRepository.findAllIncludingDeleted(pageable);
    }

    /**
     * 활성 관리자만 조회 (차단된 것 제외, 페이징)
     * @param pageable 페이징 정보
     * @return 관리자 페이지
     */
    public Page<Admin> getAllActiveAdmins(Pageable pageable) {
        return adminRepository.findAll(pageable);  // @SQLRestriction으로 deletedAt IS NULL 자동 적용
    }

    /**
     * 모든 활성 관리자 조회 (페이징 없이)
     * 필터 드롭다운 등에서 사용
     * @return 관리자 목록
     */
    public java.util.List<Admin> getAllAdminsWithoutPaging() {
        return adminRepository.findAll();  // @SQLRestriction으로 deletedAt IS NULL 자동 적용
    }

    /**
     * 관리자 차단 (Soft Delete)
     * @param adminId 차단할 관리자 ID
     */
    @Transactional
    public void blockAdmin(Long adminId) {
        Admin admin = getAdminById(adminId);

        // 마지막 SUPER_ADMIN 차단 방지
        validateNotLastSuperAdmin(adminId);

        adminRepository.delete(admin);  // Soft Delete
        log.info("Admin blocked: adminId={}, email={}", adminId, admin.getEmail());
    }

    /**
     * 관리자 차단 해제
     * @param adminId 차단 해제할 관리자 ID
     */
    @Transactional
    public void unblockAdmin(Long adminId) {
        adminRepository.restoreDeleted(adminId);
        log.info("Admin unblocked: adminId={}", adminId);
    }

    /**
     * 자기 자신 차단 방지 검증
     * @param currentAdminId 현재 로그인한 관리자 ID
     * @param targetAdminId 차단 대상 관리자 ID
     */
    public void validateNotSelfBlock(Long currentAdminId, Long targetAdminId) {
        if (currentAdminId.equals(targetAdminId)) {
            throw new AdminException(AdminErrorCode.CANNOT_BLOCK_SELF);
        }
    }

    /**
     * 마지막 SUPER_ADMIN 차단 방지 검증
     * @param adminId 차단 대상 관리자 ID
     */
    public void validateNotLastSuperAdmin(Long adminId) {
        Admin admin = getAdminById(adminId);

        if (admin.isSuperAdmin()) {
            long superAdminCount = adminRepository.countByRole(AdminRole.SUPER_ADMIN);
            if (superAdminCount <= 1) {
                throw new AdminException(AdminErrorCode.CANNOT_BLOCK_LAST_SUPER_ADMIN);
            }
        }
    }
}
