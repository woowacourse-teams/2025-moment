package moment.group.infrastructure;

import java.util.Optional;
import moment.group.domain.GroupInviteLink;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupInviteLinkRepository extends JpaRepository<GroupInviteLink, Long> {

    Optional<GroupInviteLink> findByCode(String code);

    Optional<GroupInviteLink> findByGroupIdAndIsActiveTrue(Long groupId);
}
