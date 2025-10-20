package moment.global.config;

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@Profile("prod")
public class DataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.write")
    public DataSource writeDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.read")
    public DataSource readDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public DataSource routingDataSource() {
        DataSourceRouter router = new DataSourceRouter();
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("write", writeDataSource());
        targetDataSources.put("read", readDataSource());
        router.setTargetDataSources(targetDataSources);
        router.setDefaultTargetDataSource(writeDataSource());
        return router;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(routingDataSource());
    }
}
