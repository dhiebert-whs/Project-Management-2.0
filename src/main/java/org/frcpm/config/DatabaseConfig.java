package org.frcpm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.io.File;

/**
 * Database configuration for the FRC Project Management System.
 * 
 * This configuration preserves the existing JPA infrastructure while
 * adding Spring Boot data source management. It supports:
 * - H2 database for development (preserves existing setup)
 * - SQLite database for production deployment
 * - HikariCP connection pooling (managed by Spring Boot)
 * - Integration with existing repository implementations
 * 
 * The configuration maintains compatibility with all existing:
 * - JPA entities (Project, Task, TeamMember, etc.)
 * - Repository implementations (10 custom implementations)
 * - Service layer database operations
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 * @since 2.0.0 (Spring Boot Migration)
 */
@Configuration
@EnableJpaRepositories(basePackages = "org.frcpm.repositories")
@EnableTransactionManagement
public class DatabaseConfig {

    /**
     * H2 DataSource for development environment.
     * 
     * Preserves the existing H2 database setup with the same connection
     * parameters used in the JavaFX version. This ensures data continuity
     * during migration and testing.
     * 
     * Database file location: ./db/frc-project-dev
     * 
     * @return configured H2 DataSource
     */
    @Bean
    @Profile("development")
    public DataSource h2DataSource() {
        // Ensure database directory exists
        createDatabaseDirectory();
        
        return DataSourceBuilder.create()
            .driverClassName("org.h2.Driver")
            .url("jdbc:h2:./db/frc-project-dev;DB_CLOSE_ON_EXIT=FALSE;DB_CLOSE_DELAY=-1;MODE=LEGACY")
            .username("sa")
            .password("")
            .build();
    }

    /**
     * SQLite DataSource for production environment.
     * 
     * SQLite provides a reliable, serverless database solution perfect
     * for FRC team deployments. It offers better reliability than H2
     * for production use while maintaining the lightweight footprint
     * needed for team environments.
     * 
     * Database file location: ./db/frc-project.db
     * 
     * @return configured SQLite DataSource
     */
    @Bean
    @Profile("production")
    public DataSource sqliteDataSource() {
        // Ensure database directory exists
        createDatabaseDirectory();
        
        return DataSourceBuilder.create()
            .driverClassName("org.sqlite.JDBC")
            .url("jdbc:sqlite:./db/frc-project.db")
            .build();
    }

    /**
     * Test DataSource for testing environment.
     * 
     * Uses in-memory H2 database for fast, isolated testing.
     * Each test run gets a fresh database instance.
     * 
     * @return configured test DataSource
     */
    @Bean
    @Profile("test")
    public DataSource testDataSource() {
        return DataSourceBuilder.create()
            .driverClassName("org.h2.Driver")
            .url("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=LEGACY")
            .username("sa")
            .password("")
            .build();
    }

    /**
     * Ensures the database directory exists for file-based databases.
     * 
     * Creates the ./db/ directory if it doesn't exist, preventing
     * database connection failures on first startup.
     */
    private void createDatabaseDirectory() {
        File dbDir = new File("db");
        if (!dbDir.exists()) {
            boolean created = dbDir.mkdirs();
            if (created) {
                System.out.println("Created database directory: " + dbDir.getAbsolutePath());
            }
        }
    }
}