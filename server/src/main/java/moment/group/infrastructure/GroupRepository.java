package moment.group.infrastructure;

import java.util.List;
import java.util.Optional;
import moment.group.domain.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GroupRepository extends JpaRepository<Group, Long> {

    Optional<Group> findById(Long id);

    @Query("SELECT g FROM Group g WHERE g.owner.id = :userId")
    List<Group> findByOwnerId(@Param("userId") Long userId);
}
