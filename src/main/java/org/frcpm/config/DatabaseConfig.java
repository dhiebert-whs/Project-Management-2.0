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
 * ✅ UPDATED: Converted to use PostgreSQL for development and production, H2 for tests only
 */
@Configuration
@EnableJpaRepositories(basePackages = "org.frcpm.repositories.spring")
@EnableJpaAuditing
public class DatabaseConfig {
    
    /**
     * H2 DataSource for test environment only.
     */
    @Bean
    @Profile("test")
    public DataSource h2DataSource() {
        return DataSourceBuilder.create()
            .driverClassName("org.h2.Driver")
            .url("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=LEGACY")
            .username("sa")
            .password("")
            .build();
    }
    
    /**
     * PostgreSQL DataSource for development environment with sample data.
     * Configuration is handled via application-development.yml
     */
    // Development datasource configured via application-development.yml
    
    /**
     * PostgreSQL DataSource for production environment.
     * Configuration is handled via application-production.yml and environment variables
     */
    // Production datasource configured via application-production.yml
}