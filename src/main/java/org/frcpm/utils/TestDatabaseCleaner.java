// src/main/java/org/frcpm/utils/TestDatabaseCleaner.java
package org.frcpm.utils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.frcpm.config.DatabaseConfig;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestDatabaseCleaner {
    private static final Logger LOGGER = Logger.getLogger(TestDatabaseCleaner.class.getName());

    public static void clearTestDatabase() {
        EntityManagerFactory emf = DatabaseConfig.getEntityManagerFactory();
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            
            // Disable referential integrity checks
            em.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();
            
            // Clear tables in order
            em.createNativeQuery("TRUNCATE TABLE task_components").executeUpdate();
            em.createNativeQuery("TRUNCATE TABLE task_assignments").executeUpdate();
            em.createNativeQuery("TRUNCATE TABLE task_dependencies").executeUpdate();
            em.createNativeQuery("TRUNCATE TABLE attendances").executeUpdate();
            em.createNativeQuery("TRUNCATE TABLE tasks").executeUpdate();
            em.createNativeQuery("TRUNCATE TABLE meetings").executeUpdate();
            em.createNativeQuery("TRUNCATE TABLE components").executeUpdate();
            em.createNativeQuery("TRUNCATE TABLE milestones").executeUpdate();
            em.createNativeQuery("TRUNCATE TABLE team_members").executeUpdate();
            em.createNativeQuery("TRUNCATE TABLE subsystems").executeUpdate();
            em.createNativeQuery("TRUNCATE TABLE subteams").executeUpdate();
            em.createNativeQuery("TRUNCATE TABLE projects").executeUpdate();
            
            // Re-enable referential integrity
            em.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
            
            em.getTransaction().commit();
            LOGGER.info("Test database cleared successfully");
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            LOGGER.log(Level.SEVERE, "Failed to clear test database", e);
        } finally {
            em.close();
        }
    }
}