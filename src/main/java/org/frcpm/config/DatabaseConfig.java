// src/main/java/org/frcpm/config/DatabaseConfig.java

package org.frcpm.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.sql.DataSource;

/**
 * Database configuration for FRC Project Management System.
 * Configures data sources for different profiles and provides entity manager access.
 */
@Configuration
@EnableJpaRepositories(basePackages = "org.frcpm.repositories.spring")
@EnableJpaAuditing
public class DatabaseConfig {
    
    private static EntityManagerFactory entityManagerFactory;
    
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
    
    /**
     * Gets an EntityManager instance for direct JPA operations.
     * This method provides compatibility for services that need direct EntityManager access.
     * 
     * @return a new EntityManager instance
     */
    public static EntityManager getEntityManager() {
        if (entityManagerFactory == null) {
            // Initialize the EntityManagerFactory if not already done
            entityManagerFactory = Persistence.createEntityManagerFactory("frc-project-pu");
        }
        return entityManagerFactory.createEntityManager();
    }
    
    /**
     * Closes the EntityManagerFactory when the application shuts down.
     */
    public static void closeEntityManagerFactory() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }
}