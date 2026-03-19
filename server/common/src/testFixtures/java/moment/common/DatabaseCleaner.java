package moment.common;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Table;
import jakarta.persistence.metamodel.EntityType;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DatabaseCleaner implements InitializingBean {

    @PersistenceContext
    private EntityManager entityManager;

    private List<String> tableNames;

    @Override
    public void afterPropertiesSet() {
        tableNames = entityManager.getMetamodel().getEntities().stream()
                .map(this::getTableName)
                .collect(Collectors.toList());
    }

    private String getTableName(EntityType<?> entity) {
        Table tableAnnotation = entity.getJavaType().getAnnotation(Table.class);
        if (tableAnnotation != null && tableAnnotation.name() != null && !tableAnnotation.name().isEmpty()) {
            return tableAnnotation.name();
        }
        return entity.getName();
    }

    @Transactional
    public void clean() {
        entityManager.clear();
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();

        for (String tableName : tableNames) {
            entityManager.createNativeQuery("TRUNCATE TABLE " + tableName).executeUpdate();
            entityManager.createNativeQuery("ALTER TABLE " + tableName + " ALTER COLUMN ID RESTART WITH 1")
                    .executeUpdate();
        }

        // Spring Session 테이블 정리 (JPA 엔티티가 아니므로 수동으로 정리)
        entityManager.createNativeQuery("TRUNCATE TABLE SPRING_SESSION_ATTRIBUTES").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE SPRING_SESSION").executeUpdate();

        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
    }
}
