package io.cloudops.incidentservice.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class FlywayConfig {

    private static final String MIGRATION_LOCATION = "classpath:db/migration/oracle";

    // ─── EU (Primary — migrate en premier) ───────────────────────────────────
    @Bean(name = "flywayEU", initMethod = "migrate")
    public Flyway flywayEU(@Qualifier("euDataSource") DataSource euDataSource) {
        return Flyway.configure()
                .dataSource(euDataSource)
                .locations(MIGRATION_LOCATION)
                .baselineOnMigrate(true)
                .validateOnMigrate(true)
                .load();
    }

    // ─── AF (migrate après EU) ────────────────────────────────────────────────
    @Bean(name = "flywayAF", initMethod = "migrate")
    public Flyway flywayAF(@Qualifier("afDataSource") DataSource afDataSource) {
        return Flyway.configure()
                .dataSource(afDataSource)
                .locations(MIGRATION_LOCATION)
                .baselineOnMigrate(true)
                .validateOnMigrate(true)
                .load();
    }

    // ─── US (migrate après EU) ────────────────────────────────────────────────
    @Bean(name = "flywayUS", initMethod = "migrate")
    public Flyway flywayUS(@Qualifier("usDataSource") DataSource usDataSource) {
        return Flyway.configure()
                .dataSource(usDataSource)
                .locations(MIGRATION_LOCATION)
                .baselineOnMigrate(true)
                .validateOnMigrate(true)
                .load();
    }
}