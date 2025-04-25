// src/test/java/org/frcpm/repositories/RepositoryPerformanceTest.java
package org.frcpm.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.frcpm.config.DatabaseConfig;
import org.frcpm.models.Project;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.models.TeamMember;
import org.frcpm.repositories.specific.ProjectRepository;
import org.frcpm.repositories.specific.TaskRepository;
import org.frcpm.utils.DatabaseTestUtil;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RepositoryPerformanceTest {
    
    private static final Logger LOGGER = Logger.getLogger(RepositoryPerformanceTest.class.getName());
    private static final int BATCH_SIZE = 50; // Number of entities to create for testing
    
    private static ProjectRepository projectRepository;
    private static TaskRepository taskRepository;
    private static Project testProject;
    private static Subsystem testSubsystem;
    private static TeamMember testMember;
    
    @BeforeAll
    public static void setUp() {
        DatabaseConfig.initialize(true); // Force development mode
        
        projectRepository = RepositoryFactory.getProjectRepository();
        taskRepository = RepositoryFactory.getTaskRepository();
        
        // Create test project
        testProject = DatabaseTestUtil.createTestProject();
        testSubsystem = DatabaseTestUtil.getTestSubsystem();
        testMember = DatabaseTestUtil.getTestMember();
        
        // Create batch of tasks for testing
        createBatchTestData();
    }
    
    @AfterAll
    public static void tearDown() {
        cleanupBatchTestData();
        DatabaseConfig.shutdown();
    }
    
    private static void createBatchTestData() {
        LOGGER.info("Creating batch test data with " + BATCH_SIZE + " tasks...");
        
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            em.getTransaction().begin();
            
            LocalDate startDate = LocalDate.now();
            
            for (int i = 0; i < BATCH_SIZE; i++) {
                Task task = new Task(
                    "Performance_Task_" + i,
                    testProject,
                    testSubsystem
                );
                
                // Set varying properties for query testing
                task.setStartDate(startDate.plusDays(i % 7));
                task.setEndDate(startDate.plusDays((i % 7) + 7));
                task.setProgress(i % 101); // 0-100
                task.setPriority(Task.Priority.fromValue(1 + (i % 4))); // 1-4
                
                // Set completed for some tasks
                if (i % 5 == 0) {
                    task.setCompleted(true);
                }
                
                // Assign the test member to some tasks
                if (i % 3 == 0) {
                    task.assignMember(testMember);
                }
                
                em.persist(task);
                
                // Flush every 20 entities to avoid memory issues
                if (i % 20 == 0) {
                    em.flush();
                }
            }
            
            em.getTransaction().commit();
            LOGGER.info("Batch test data created successfully.");
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            LOGGER.severe("Failed to create batch test data: " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
    
    private static void cleanupBatchTestData() {
        LOGGER.info("Cleaning up batch test data...");
        
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            em.getTransaction().begin();
            
            // Delete all performance test tasks
            Query query = em.createQuery(
                "DELETE FROM Task t WHERE t.title LIKE 'Performance_Task_%'"
            );
            int deleted = query.executeUpdate();
            
            em.getTransaction().commit();
            LOGGER.info("Cleaned up " + deleted + " test tasks.");
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            LOGGER.severe("Failed to clean up batch test data: " + e.getMessage());
        } finally {
            em.close();
        }
        
        // Use the utility to clean up other test data
        DatabaseTestUtil.cleanupTestData();
    }
    
    @Test
    @Order(1)
    public void testSimpleQueryPerformance() {
        LOGGER.info("Testing simple query performance...");
        
        // Benchmark findAll
        Instant start = Instant.now();
        List<Task> tasks = taskRepository.findAll();
        Instant end = Instant.now();
        
        long duration = Duration.between(start, end).toMillis();
        LOGGER.info("Find all tasks took " + duration + " ms for " + tasks.size() + " tasks");
        
        assertTrue(tasks.size() >= BATCH_SIZE, "Should find at least " + BATCH_SIZE + " tasks");
        assertTrue(duration < 2000, "Simple query should complete in under 2 seconds");
    }
    
    @Test
    @Order(2)
    public void testComplexJoinQuery() {
        LOGGER.info("Testing complex join query performance...");
        
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            // Build a complex query with multiple joins
            Instant start = Instant.now();
            
            TypedQuery<Object[]> query = em.createQuery(
                "SELECT t.title, s.name, tm.firstName, tm.lastName, p.name " +
                "FROM Task t " +
                "JOIN t.subsystem s " +
                "JOIN t.project p " +
                "LEFT JOIN t.assignedTo tm " +
                "WHERE t.completed = false " +
                "AND t.progress < 100 " +
                "AND t.priority IN (:priorities) " +
                "ORDER BY t.priority DESC, t.progress ASC",
                Object[].class
            );
            
            List<Task.Priority> priorities = List.of(
                Task.Priority.HIGH, 
                Task.Priority.CRITICAL
            );
            
            query.setParameter("priorities", priorities);
            
            List<Object[]> results = query.getResultList();
            Instant end = Instant.now();
            
            long duration = Duration.between(start, end).toMillis();
            LOGGER.info("Complex join query took " + duration + " ms for " + results.size() + " results");
            
            assertFalse(results.isEmpty(), "Complex query should return results");
            assertTrue(duration < 2000, "Complex query should complete in under 2 seconds");
            
            // Validate a few results
            for (Object[] row : results) {
                assertNotNull(row[0], "Task title should be included");
                assertNotNull(row[1], "Subsystem name should be included");
                assertNotNull(row[4], "Project name should be included");
            }
        } finally {
            em.close();
        }
    }
    
    @Test
    @Order(3)
    public void testPagination() {
        LOGGER.info("Testing pagination performance...");
        
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            int pageSize = 10;
            int totalPages = 0;
            List<Task> allResults = new ArrayList<>();
            
            // Test multiple pages
            for (int page = 0; page < 5; page++) {
                Instant start = Instant.now();
                
                TypedQuery<Task> query = em.createQuery(
                    "SELECT t FROM Task t " +
                    "WHERE t.title LIKE 'Performance_Task_%' " +
                    "ORDER BY t.title ASC",
                    Task.class
                );
                
                // Set pagination parameters
                query.setFirstResult(page * pageSize);
                query.setMaxResults(pageSize);
                
                List<Task> pageResults = query.getResultList();
                allResults.addAll(pageResults);
                Instant end = Instant.now();
                
                long duration = Duration.between(start, end).toMillis();
                LOGGER.info("Page " + page + " query took " + duration + " ms and returned " + 
                           pageResults.size() + " results");
                
                if (pageResults.size() < pageSize) {
                    // We've reached the end of the results
                    totalPages = page + 1;
                    break;
                }
                
                assertTrue(duration < 1000, "Paginated query should be fast (under 1 second)");
                assertEquals(pageSize, pageResults.size(), "Page size should match requested size");
                
                // Basic validation of sorting
                if (page > 0 && !pageResults.isEmpty() && !allResults.isEmpty()) {
                    String lastTitlePrevPage = allResults.get((page * pageSize) - 1).getTitle();
                    String firstTitleCurrentPage = pageResults.get(0).getTitle();
                    assertTrue(lastTitlePrevPage.compareTo(firstTitleCurrentPage) < 0,
                              "Results should be sorted correctly across pages");
                }
            }
            
            assertTrue(allResults.size() >= BATCH_SIZE, 
                      "All paginated results combined should equal batch size");
            
        } finally {
            em.close();
        }
    }
    
    @Test
    @Order(4)
    public void testGroupByQuery() {
        LOGGER.info("Testing group by query performance...");
        
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            Instant start = Instant.now();
            
            TypedQuery<Object[]> query = em.createQuery(
                "SELECT t.priority, COUNT(t), AVG(t.progress) " +
                "FROM Task t " +
                "WHERE t.title LIKE 'Performance_Task_%' " +
                "GROUP BY t.priority " +
                "ORDER BY t.priority ASC",
                Object[].class
            );
            
            List<Object[]> results = query.getResultList();
            Instant end = Instant.now();
            
            long duration = Duration.between(start, end).toMillis();
            LOGGER.info("Group by query took " + duration + " ms for " + results.size() + " groups");
            
            assertEquals(4, results.size(), "Should have 4 priority groups");
            assertTrue(duration < 1000, "Group by query should complete in under 1 second");
            
            // Verify the results make sense
            for (Object[] row : results) {
                Task.Priority priority = (Task.Priority) row[0];
                Long count = (Long) row[1];
                Double avgProgress = (Double) row[2];
                
                assertNotNull(priority, "Priority should not be null");
                assertTrue(count > 0, "Count should be positive");
                assertTrue(avgProgress >= 0 && avgProgress <= 100, 
                          "Average progress should be between 0 and 100");
                
                LOGGER.info("Priority " + priority + ": " + count + " tasks, " +
                           String.format("%.2f", avgProgress) + "% average progress");
            }
        } finally {
            em.close();
        }
    }
    
    @Test
    @Order(5)
    public void testBatchInsertPerformance() {
        LOGGER.info("Testing batch insert performance...");
        
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            int batchSize = 30;
            List<Long> savedIds = new ArrayList<>();
            
            em.getTransaction().begin();
            
            Instant start = Instant.now();
            
            for (int i = 0; i < batchSize; i++) {
                Task task = new Task(
                    "BatchInsert_Task_" + i,
                    testProject,
                    testSubsystem
                );
                task.setStartDate(LocalDate.now());
                task.setEndDate(LocalDate.now().plusDays(7));
                
                em.persist(task);
                
                // Flush periodically
                if (i % 10 == 0 && i > 0) {
                    em.flush();
                }
                
                // Store ID for later cleanup
                savedIds.add(task.getId());
            }
            
            em.getTransaction().commit();
            Instant end = Instant.now();
            
            long duration = Duration.between(start, end).toMillis();
            LOGGER.info("Batch insert of " + batchSize + " tasks took " + duration + " ms");
            
            assertTrue(duration < 2000, "Batch insert should complete in under 2 seconds");
            
            // Clean up the inserted tasks
            em.getTransaction().begin();
            Query deleteQuery = em.createQuery(
                "DELETE FROM Task t WHERE t.title LIKE 'BatchInsert_Task_%'"
            );
            int deleted = deleteQuery.executeUpdate();
            em.getTransaction().commit();
            
            assertEquals(batchSize, deleted, "All inserted tasks should be deleted");
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            LOGGER.severe("Failed in batch insert test: " + e.getMessage());
            fail("Batch insert test failed: " + e.getMessage());
        } finally {
            em.close();
        }
    }
}