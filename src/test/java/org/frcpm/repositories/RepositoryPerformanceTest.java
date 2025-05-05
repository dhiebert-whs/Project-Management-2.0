package org.frcpm.repositories;

import org.frcpm.config.DatabaseConfig;
import org.frcpm.models.*;
import org.frcpm.repositories.impl.JpaRepositoryImpl;
import org.frcpm.repositories.specific.ProjectRepository;
import org.frcpm.repositories.specific.TaskRepository;
import org.frcpm.utils.TestDatabaseCleaner;
import org.junit.jupiter.api.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance tests for repository implementations.
 * These tests verify that repository operations meet performance expectations.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RepositoryPerformanceTest {
    
    private static final Logger LOGGER = Logger.getLogger(RepositoryPerformanceTest.class.getName());
    
    // Realistic performance thresholds based on test environment capabilities
    // Adjusted to allow for CI/CD environment variations
    private static final long BATCH_INSERT_TIME_MS = 5000; // 5 seconds for 100 entities
    private static final long BATCH_UPDATE_TIME_MS = 5000; // 5 seconds for 100 entities
    private static final long BATCH_DELETE_TIME_MS = 5000; // 5 seconds for 100 entities
    private static final long QUERY_TIME_MS = 1000; // 1 second for complex query
    
    private static final int BATCH_SIZE = 100;
    
    private static ProjectRepository projectRepo;
    private static TaskRepository taskRepo;
    private static List<Long> projectIds;
    private static List<Long> taskIds;
    
    @BeforeAll
    static void setup() {
        // Initialize database with development settings for consistent testing
        DatabaseConfig.initialize(true);
        
        // Get repositories
        projectRepo = RepositoryFactory.getProjectRepository();
        taskRepo = RepositoryFactory.getTaskRepository();
        
        // Initialize ID lists
        projectIds = new ArrayList<>();
        taskIds = new ArrayList<>();
        
        // Clean up any data from previous test runs
        TestDatabaseCleaner.clearTestDatabase();
    }
    
    @AfterAll
    static void cleanup() {
        // Clean up test data
        try {
            EntityManager em = DatabaseConfig.getEntityManager();
            EntityTransaction tx = em.getTransaction();
            
            try {
                tx.begin();
                
                // Delete tasks first
                if (!taskIds.isEmpty()) {
                    em.createQuery("DELETE FROM Task t WHERE t.id IN :ids")
                      .setParameter("ids", taskIds)
                      .executeUpdate();
                }
                
                // Then delete projects
                if (!projectIds.isEmpty()) {
                    em.createQuery("DELETE FROM Project p WHERE p.id IN :ids")
                      .setParameter("ids", projectIds)
                      .executeUpdate();
                }
                
                tx.commit();
            } catch (Exception e) {
                if (tx.isActive()) {
                    tx.rollback();
                }
                LOGGER.severe("Error cleaning up test data: " + e.getMessage());
            } finally {
                em.close();
            }
        } catch (Exception e) {
            LOGGER.severe("Failed to clean up test data: " + e.getMessage());
        }
        
        // Shutdown database
        DatabaseConfig.shutdown();
    }
    
    @Test
    @Order(1)
    @DisplayName("Test batch insert performance")
    public void testBatchInsertPerformance() {
        // Prepare test data
        List<Project> projects = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        for (int i = 0; i < BATCH_SIZE; i++) {
            Project project = new Project(
                "Performance Test Project " + i,
                today,
                today.plusWeeks(6),
                today.plusWeeks(8)
            );
            project.setDescription("Performance test description " + i);
            projects.add(project);
        }
        
        // Measure batch insert time
        long startTime = System.currentTimeMillis();
        
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        
        try {
            tx.begin();
            
            for (int i = 0; i < projects.size(); i++) {
                Project project = projects.get(i);
                em.persist(project);
                
                // Flush every 20 entities to avoid memory issues
                if (i % 20 == 0 && i > 0) {
                    em.flush();
                    em.clear();
                }
                
                // Keep track of IDs for cleanup
                projectIds.add(project.getId());
            }
            
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Failed to insert batch", e);
        } finally {
            em.close();
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        LOGGER.info("Batch insert of " + BATCH_SIZE + " projects took " + duration + " ms");
        
        // Assert performance is acceptable
        assertTrue(duration < BATCH_INSERT_TIME_MS,
                "Batch insert performance is worse than expected: " + duration + " ms");
    }
    
    @Test
    @Order(2)
    @DisplayName("Test batch update performance")
    public void testBatchUpdatePerformance() {
        // Skip if no projects were created
        assumeTrue(!projectIds.isEmpty(), "No projects available for update test");
        
        // Measure batch update time
        long startTime = System.currentTimeMillis();
        
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        
        try {
            tx.begin();
            
            // Update all projects
            em.createQuery("UPDATE Project p SET p.description = CONCAT(p.description, ' - Updated') " +
                          "WHERE p.id IN :ids")
              .setParameter("ids", projectIds)
              .executeUpdate();
            
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Failed to update batch", e);
        } finally {
            em.close();
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        LOGGER.info("Batch update of " + projectIds.size() + " projects took " + duration + " ms");
        
        // Assert performance is acceptable
        assertTrue(duration < BATCH_UPDATE_TIME_MS,
                "Batch update performance is worse than expected: " + duration + " ms");
    }
    
    @Test
    @Order(3)
    @DisplayName("Test query performance")
    public void testQueryPerformance() {
        // Skip if no projects were created
        assumeTrue(!projectIds.isEmpty(), "No projects available for query test");
        
        // Measure query time
        long startTime = System.currentTimeMillis();
        
        // Execute a complex query
        List<Project> results = projectRepo.findAll();
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        LOGGER.info("Query for all projects took " + duration + " ms, returned " + results.size() + " results");
        
        // Assert performance is acceptable
        assertTrue(duration < QUERY_TIME_MS,
                "Query performance is worse than expected: " + duration + " ms");
        
        // Verify query results
        assertFalse(results.isEmpty(), "Query should return results");
        assertTrue(results.size() >= BATCH_SIZE, "Query should return at least " + BATCH_SIZE + " results");
    }
    
    @Test
    @Order(4)
    @DisplayName("Test batch delete performance")
    public void testBatchDeletePerformance() {
        // Skip if no projects were created
        assumeTrue(!projectIds.isEmpty(), "No projects available for delete test");
        
        // Measure batch delete time
        long startTime = System.currentTimeMillis();
        
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        
        try {
            tx.begin();
            
            // Delete all projects created in this test
            int deleted = em.createQuery("DELETE FROM Project p WHERE p.id IN :ids")
                           .setParameter("ids", projectIds)
                           .executeUpdate();
            
            tx.commit();
            
            LOGGER.info("Deleted " + deleted + " projects");
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Failed to delete batch", e);
        } finally {
            em.close();
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        LOGGER.info("Batch delete of " + projectIds.size() + " projects took " + duration + " ms");
        
        // Assert performance is acceptable
        assertTrue(duration < BATCH_DELETE_TIME_MS,
                "Batch delete performance is worse than expected: " + duration + " ms");
        
        // Clear IDs list since we've deleted the projects
        projectIds.clear();
    }
    
    @Test
    @Order(5)
    @DisplayName("Test repository implementation overhead")
    public void testRepositoryImplementationOverhead() {
        // Create test data
        Project project = new Project(
            "Performance Test Overhead Project",
            LocalDate.now(),
            LocalDate.now().plusWeeks(6),
            LocalDate.now().plusWeeks(8)
        );
        
        // Measure repository save time
        long repoStartTime = System.currentTimeMillis();
        Project savedProject = projectRepo.save(project);
        long repoEndTime = System.currentTimeMillis();
        long repoDuration = repoEndTime - repoStartTime;
        
        // Add to cleanup list
        projectIds.add(savedProject.getId());
        
        // Measure direct EntityManager time
        Project project2 = new Project(
            "Performance Test Direct EM Project",
            LocalDate.now(),
            LocalDate.now().plusWeeks(6),
            LocalDate.now().plusWeeks(8)
        );
        
        long emStartTime = System.currentTimeMillis();
        
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        
        try {
            tx.begin();
            em.persist(project2);
            em.flush();
            tx.commit();
            
            // Add to cleanup list
            projectIds.add(project2.getId());
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Failed to persist directly", e);
        } finally {
            em.close();
        }
        
        long emEndTime = System.currentTimeMillis();
        long emDuration = emEndTime - emStartTime;
        
        LOGGER.info("Repository save took " + repoDuration + " ms, direct EntityManager took " + emDuration + " ms");
        
        // Repository overhead should be reasonable
        // The repository adds transaction management, error handling, etc.
        // so it will be slower than direct EntityManager usage
        // but should not be more than 3x slower
        assertTrue(repoDuration < emDuration * 3,
                "Repository implementation overhead is too high: repo=" + repoDuration + "ms, em=" + emDuration + "ms");
    }
    
    // Helper method to ensure test doesn't run if a condition isn't met
    private void assumeTrue(boolean condition, String message) {
        if (!condition) {
            LOGGER.warning("Skipping test: " + message);
            Assumptions.assumeTrue(condition, message);
        }
    }
}