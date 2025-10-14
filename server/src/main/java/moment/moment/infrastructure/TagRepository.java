package moment.moment.infrastructure;

import java.util.Collection;
import java.util.List;
import moment.moment.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findAllByNameIn(Collection<String> names);
}
