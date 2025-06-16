// src/main/java/org/frcpm/config/DatabaseConfig.java

package org.frcpm.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.sql.DataSource;

/**
 * Database configuration for FRC Project Management System.
 * ✅ FIXED: Consolidated JPA repository configuration here to avoid conflicts
 */
@Configuration
@EnableJpaRepositories(basePackages = "org.frcpm.repositories.spring")
@EnableJpaAuditing
public class DatabaseConfig {
    
    /**
     * H2 DataSource for development and test environments.
     * ✅ FIXED: Added test profile support
     */
    @Bean
    @Profile({"development", "test"})
    public DataSource h2DataSource() {
        return DataSourceBuilder.create()
            .driverClassName("org.h2.Driver")
            .url("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=LEGACY")
            .username("sa")
            .password("")
            .build();
    }
    
    /**
     * SQLite DataSource for production environment.
     */
    @Bean
    @Profile("production")
    public DataSource sqliteDataSource() {
        return DataSourceBuilder.create()
            .driverClassName("org.sqlite.JDBC")
            .url("jdbc:sqlite:./db/frc-project.db")
            .build();
    }
}