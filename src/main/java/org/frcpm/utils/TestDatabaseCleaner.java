// src/main/java/org/frcpm/utils/TestDatabaseCleaner.java

package org.frcpm.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Spring Boot compatible test database cleaner.
 * Uses Spring's JdbcTemplate for safe database cleanup in tests.
 */
@Component
public class TestDatabaseCleaner {
    
    private static final Logger LOGGER = Logger.getLogger(TestDatabaseCleaner.class.getName());
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    /**
     * Clears all test data from the database.
     * Safe for use in Spring Boot integration tests.
     */
    @Transactional
    public void clearTestDatabase() {
        try {
            LOGGER.info("Clearing test database...");
            
            // Disable referential integrity checks (H2 specific)
            jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
            
            // Clear tables in dependency order
            jdbcTemplate.execute("DELETE FROM audit_logs");
            jdbcTemplate.execute("DELETE FROM task_components");
            jdbcTemplate.execute("DELETE FROM task_assignments");
            jdbcTemplate.execute("DELETE FROM task_dependencies");
            jdbcTemplate.execute("DELETE FROM attendances");
            jdbcTemplate.execute("DELETE FROM tasks");
            jdbcTemplate.execute("DELETE FROM meetings");
            jdbcTemplate.execute("DELETE FROM components");
            jdbcTemplate.execute("DELETE FROM milestones");
            jdbcTemplate.execute("DELETE FROM team_members");
            jdbcTemplate.execute("DELETE FROM subsystems");
            jdbcTemplate.execute("DELETE FROM subteams");
            jdbcTemplate.execute("DELETE FROM projects");
            jdbcTemplate.execute("DELETE FROM users");
            
            // Re-enable referential integrity
            jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
            
            LOGGER.info("Test database cleared successfully");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to clear test database", e);
            throw new RuntimeException("Database cleanup failed", e);
        }
    }
    
    /**
     * Clears only test-specific data (data with "test" in names).
     * Safer alternative that preserves sample data.
     */
    @Transactional
    public void clearTestDataOnly() {
        try {
            LOGGER.info("Clearing test-specific data...");
            
            // Clear only test data (safer approach)
            jdbcTemplate.execute("DELETE FROM tasks WHERE title LIKE '%test%' OR title LIKE '%Test%'");
            jdbcTemplate.execute("DELETE FROM projects WHERE name LIKE '%test%' OR name LIKE '%Test%'");
            jdbcTemplate.execute("DELETE FROM users WHERE username LIKE '%test%' OR username LIKE '%Test%'");
            
            LOGGER.info("Test data cleared successfully");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to clear test data", e);
            throw new RuntimeException("Test data cleanup failed", e);
        }
    }
}