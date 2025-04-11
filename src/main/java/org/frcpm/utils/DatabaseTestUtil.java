package org.frcpm.utils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;
import org.frcpm.config.DatabaseConfig;

import java.util.List;
import java.util.logging.Logger;

/**
 * Utility for testing database connectivity and configuration.
 */
public class DatabaseTestUtil {
    private static final Logger LOGGER = Logger.getLogger(DatabaseTestUtil.class.getName());
    
    /**
     * Tests database connectivity and schema.
     * 
     * @return true if the database is properly set up, false otherwise
     */
    public static boolean testDatabase() {
        LOGGER.info("Testing database connection and schema...");
        
        try {
            // Initialize the database
            DatabaseConfig.initialize();
            
            // Get an EntityManager
            EntityManagerFactory emf = DatabaseConfig.getEntityManagerFactory();
            EntityManager em = emf.createEntityManager();
            
            try {
                // Test entity tables
                String[] tables = {"Project", "Task", "TeamMember", "Subteam", "Subsystem", 
                                  "Component", "Meeting", "Attendance", "Milestone"};
                
                for (String table : tables) {
                    Query query = em.createQuery("SELECT COUNT(e) FROM " + table + " e");
                    Long count = (Long) query.getSingleResult();
                    LOGGER.info(table + " table exists with " + count + " records");
                }
                
                // Test a join query
                Query query = em.createQuery(
                    "SELECT t.title, s.name FROM Task t JOIN t.subsystem s WHERE t.progress > 0");
                List<?> results = query.getResultList();
                LOGGER.info("Join query returned " + results.size() + " results");
                
                LOGGER.info("Database test completed successfully!");
                return true;
            } finally {
                em.close();
            }
        } catch (Exception e) {
            LOGGER.severe("Database test failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}