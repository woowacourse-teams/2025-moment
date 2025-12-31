package moment.admin.service.admin;

import lombok.RequiredArgsConstructor;
import moment.admin.domain.Admin;
import moment.admin.infrastructure.AdminRepository;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public Admin authenticateAdmin(String email, String password) {
        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new MomentException(ErrorCode.ADMIN_LOGIN_FAILED));

        if (!passwordEncoder.matches(password, admin.getPassword())) {
            throw new MomentException(ErrorCode.ADMIN_LOGIN_FAILED);
        }

        return admin;
    }

    public Admin getAdminById(Long id) {
        return adminRepository.findById(id)
                .orElseThrow(() -> new MomentException(ErrorCode.ADMIN_NOT_FOUND));
    }

    @Transactional
    public Admin createAdmin(String email, String name, String password) {
        if (adminRepository.existsByEmail(email)) {
            throw new MomentException(ErrorCode.ADMIN_EMAIL_CONFLICT);
        }

        String hashedPassword = passwordEncoder.encode(password);
        Admin admin = new Admin(email, name, hashedPassword);
        return adminRepository.save(admin);
    }

    public boolean existsByEmail(String email) {
        return adminRepository.existsByEmail(email);
    }
}
