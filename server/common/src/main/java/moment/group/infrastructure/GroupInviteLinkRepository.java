package moment.group.infrastructure;

import java.util.Optional;
import moment.group.domain.GroupInviteLink;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupInviteLinkRepository extends JpaRepository<GroupInviteLink, Long> {

    Optional<GroupInviteLink> findByCode(String code);

    Optional<GroupInviteLink> findByGroupIdAndIsActiveTrue(Long groupId);

    /**
     * Admin용: 그룹의 초대링크 조회 (활성/비활성 모두)
     */
    Optional<GroupInviteLink> findFirstByGroupIdOrderByCreatedAtDesc(Long groupId);
}
