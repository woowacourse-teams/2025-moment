package moment.global.config;

import com.zaxxer.hikari.HikariDataSource;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

@Configuration
@Profile("prod")
public class DataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.write")
    public DataSource writeDataSource() {
        HikariDataSource ds = DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
        ds.setPoolName("write");
        ds.setMaximumPoolSize(15);
        ds.setMaximumPoolSize(15);
        ds.setConnectionTimeout(30000);
        ds.setIdleTimeout(600000);
        ds.setMaxLifetime(1800000);
        return ds;
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.read")
    public DataSource readDataSource() {
        HikariDataSource ds = DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
        ds.setPoolName("read");
        ds.setMaximumPoolSize(15);
        ds.setMaximumPoolSize(15);
        ds.setConnectionTimeout(30000);
        ds.setIdleTimeout(600000);
        ds.setMaxLifetime(1800000);
        return ds;
    }

    @Bean
    @DependsOn({"readDataSource", "writeDataSource"})
    public DataSource routingDataSource(
            @Qualifier("readDataSource") DataSource readDataSource,
            @Qualifier("writeDataSource") DataSource writeDataSource
    ) {
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("read", readDataSource);
        targetDataSources.put("write", writeDataSource);

        RoutingDataSource routingDataSource = new RoutingDataSource();
        routingDataSource.setDefaultTargetDataSource(writeDataSource);
        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.afterPropertiesSet();

        return routingDataSource;
    }

    @Bean
    @Primary
    @DependsOn("routingDataSource")
    public DataSource dataSource(@Qualifier("routingDataSource") DataSource routingDataSource) {
        return new LazyConnectionDataSourceProxy(routingDataSource);
    }

    @Bean
    @DependsOn({"readDataSource", "writeDataSource"})
    public MeterBinder hikariMetricsBinder(
            @Qualifier("readDataSource") DataSource readDataSource,
            @Qualifier("writeDataSource") DataSource writeDataSource
    ) {
        return registry -> {
            if (readDataSource instanceof HikariDataSource readHikari) {
                readHikari.setMetricRegistry(registry);
            }
            if (writeDataSource instanceof HikariDataSource writeHikari) {
                writeHikari.setMetricRegistry(registry);
            }
        };
    }
}
