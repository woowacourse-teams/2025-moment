package moment.moment.infrastructure;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import moment.moment.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByName(String name);

    List<Tag> findAllByName(String name);

    List<Tag> findAllByNameIn(Collection<String> names);

    boolean existsByName(String name);
}
