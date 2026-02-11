package moment.support;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import moment.moment.domain.Moment;
import moment.moment.infrastructure.MomentRepository;
import moment.user.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class MomentCreatedAtHelper {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    MomentRepository momentRepository;

    @Transactional
    public Moment saveMomentWithCreatedAt(String content, User momenter, LocalDateTime createdAt) {
        Moment moment = momentRepository.save(new Moment(content, momenter));
        entityManager.flush();

        entityManager.createNativeQuery("UPDATE moments SET created_at = ? WHERE id = ?")
                .setParameter(1 , createdAt)
                .setParameter(2 , moment.getId())
                .executeUpdate();

        entityManager.clear();
        return momentRepository.findById(moment.getId()).orElseThrow();
    }
}
