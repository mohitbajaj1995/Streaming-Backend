package com.easyliveline.streamingbackend.util;

import jakarta.annotation.PostConstruct;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;

@Component
public class TenantFlywayRunner {

    private final DataSource dataSource;

    @Autowired
    public TenantFlywayRunner(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void migrate() {
        List<String> tenantSchemas = List.of("royal", "highspeed");

        for (String schema : tenantSchemas) {
            try {
                Flyway flyway = Flyway.configure()
                        .dataSource(dataSource)
                        .schemas(schema)
                        .locations("classpath:db/migration/common")
                        .baselineOnMigrate(true)
                        .cleanDisabled(true)
                        .loggers("slf4j")
                        .load();

                // CLEAN the schema — this deletes all tables, views, data, etc.
//                flyway.clean();

                flyway.repair();

                // MIGRATE — re-apply migrations from scratch
                flyway.migrate();

            } catch (Exception e) {
                System.err.println("Flyway migration failed for schema " + schema + ": " + e.getMessage());
            }
        }
    }
}
