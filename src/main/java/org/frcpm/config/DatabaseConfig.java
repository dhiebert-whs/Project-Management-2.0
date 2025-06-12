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
 * Configures data sources for different profiles.
 * 
 * FIXED: Removed manual EntityManager management that conflicted with Spring Boot.
 * Spring Boot will automatically manage EntityManager instances.
 */
@Configuration
@EnableJpaRepositories(basePackages = "org.frcpm.repositories.spring")
@EnableJpaAuditing
public class DatabaseConfig {
    
    /**
     * H2 DataSource for development environment.
     */
    @Bean
    @Profile("development")
    public DataSource h2DataSource() {
        return DataSourceBuilder.create()
            .driverClassName("org.h2.Driver")
            .url("jdbc:h2:./db/frc-project-dev;DB_CLOSE_ON_EXIT=FALSE;DB_CLOSE_DELAY=-1")
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
    
    /* 
     * REMOVED: Manual EntityManager management that conflicts with Spring Boot
     * 
     * Spring Boot automatically provides EntityManager through:
     * - @PersistenceContext EntityManager injection
     * - JpaRepository auto-implementation 
     * - @Transactional transaction management
     * 
     * If services need direct EntityManager access, inject it via:
     * @PersistenceContext private EntityManager entityManager;
     */
}