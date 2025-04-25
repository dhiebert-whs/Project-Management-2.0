package org.frcpm.utils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import org.frcpm.config.DatabaseConfig;
import org.frcpm.models.*;
import org.frcpm.repositories.RepositoryFactory;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DatabaseTestUtilTest {

    @BeforeAll
    static void setup() {
        // Initialize database for all tests
        DatabaseConfig.initialize(true); // Force development mode
    }

    @AfterAll
    static void tearDown() {
        // Clean up all test data and shut down
        DatabaseTestUtil.cleanupTestData();
        DatabaseConfig.shutdown();
    }

    @Test
    @Order(1)
    public void testTestDatabase() {
        boolean result = DatabaseTestUtil.testDatabase();
        assertTrue(result, "Database test should succeed");
    }
    
    @Test
    @Order(2)
    public void testCreateTestProject() {
        Project project = DatabaseTestUtil.createTestProject();
        assertNotNull(project, "Project should not be null");
        assertNotNull(project.getId(), "Project ID should not be null");
        assertTrue(project.getName().startsWith("Test Project"), "Project name should start with 'Test Project'");
    }
    
    @Test
    @Order(3)
    public void testGetTestSubteam() {
        Subteam subteam = DatabaseTestUtil.getTestSubteam();
        assertNotNull(subteam, "Subteam should not be null");
        assertNotNull(subteam.getId(), "Subteam ID should not be null");
        assertTrue(subteam.getName().startsWith("Test Subteam"), "Subteam name should start with 'Test Subteam'");
    }
    
    @Test
    @Order(4)
    public void testGetTestMember() {
        TeamMember member = DatabaseTestUtil.getTestMember();
        assertNotNull(member, "Member should not be null");
        assertNotNull(member.getId(), "Member ID should not be null");
        assertTrue(member.getUsername().startsWith("testuser_"), "Member username should start with 'testuser_'");
    }
    
    @Test
    @Order(5)
    public void testGetTestSubsystem() {
        Subsystem subsystem = DatabaseTestUtil.getTestSubsystem();
        assertNotNull(subsystem, "Subsystem should not be null");
        assertNotNull(subsystem.getId(), "Subsystem ID should not be null");
        assertTrue(subsystem.getName().startsWith("Test Subsystem"), "Subsystem name should start with 'Test Subsystem'");
    }
    
    @Test
    @Order(6)
    public void testCreateTestTask() {
        String taskTitle = "Test Task " + System.currentTimeMillis();
        Task task = DatabaseTestUtil.createTestTask(taskTitle);
        assertNotNull(task, "Task should not be null");
        assertNotNull(task.getId(), "Task ID should not be null");
        assertEquals(taskTitle, task.getTitle(), "Task title should match");
        assertEquals(Duration.ofHours(2), task.getEstimatedDuration(), "Task duration should be 2 hours");
    }
    
    @Test
    @Order(7)
    public void testEntityRelationships() {
        // Create test entities with relationships
        Project project = DatabaseTestUtil.createTestProject();
        Subsystem subsystem = DatabaseTestUtil.getTestSubsystem();
        TeamMember member = DatabaseTestUtil.getTestMember();
        Task task = DatabaseTestUtil.createTestTask("Relationship Test Task");
        
        // Verify the relationships
        assertNotNull(task.getProject(), "Task should have a project");
        assertEquals(project.getId(), task.getProject().getId(), "Task should belong to the test project");
        
        assertNotNull(task.getSubsystem(), "Task should have a subsystem");
        assertEquals(subsystem.getId(), task.getSubsystem().getId(), "Task should belong to the test subsystem");
        
        assertTrue(task.getAssignedTo().contains(member), "Task should be assigned to the test member");
        
        // Verify bidirectional relationships (requires a fresh EntityManager to avoid stale data)
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            
            Project refreshedProject = em.find(Project.class, project.getId());
            boolean taskFound = refreshedProject.getTasks().stream()
                .anyMatch(t -> t.getId().equals(task.getId()));
            
            assertTrue(taskFound, "Project should contain the task in its tasks collection");
            
            tx.commit();
        } finally {
            em.close();
        }
    }
    
    @Test
    @Order(8)
    public void testDoInTransaction() {
        // Create a shared variable to store project ID
        final Long[] projectId = new Long[1];
        
        // Create a project using doInTransaction
        DatabaseTestUtil.doInTransaction(() -> {
            EntityManager em = DatabaseConfig.getEntityManager();
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                Project project = new Project(
                    "Transaction Test Project",
                    LocalDate.now(),
                    LocalDate.now().plusWeeks(4),
                    LocalDate.now().plusWeeks(6)
                );
                em.persist(project);
                em.flush(); // Important: Flush to ensure the ID is generated
                projectId[0] = project.getId(); // Capture ID
                tx.commit();
            } catch (Exception e) {
                if (tx != null && tx.isActive()) {
                    tx.rollback();
                }
                throw e;
            } finally {
                em.close();
            }
        });
        
        assertNotNull(projectId[0], "Project ID should not be null");
        
        // Verify the project exists
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            Project project = em.find(Project.class, projectId[0]);
            assertNotNull(project, "Project should exist after transaction");
            assertEquals("Transaction Test Project", project.getName(), "Project name should match");
        } finally {
            em.close();
        }
    }
    
    @Test
    @Order(9)
    public void testNestedTransactions() {
        // Create shared variables to store project ID and task ID
        final Long[] projectId = new Long[1];
        final Long[] taskId = new Long[1];
        
        // First transaction - create project and get ID
        DatabaseTestUtil.doInTransaction(() -> {
            EntityManager em = DatabaseConfig.getEntityManager();
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                Project project = new Project(
                    "Nested Transaction Project",
                    LocalDate.now(),
                    LocalDate.now().plusWeeks(4),
                    LocalDate.now().plusWeeks(6)
                );
                em.persist(project);
                em.flush(); // Important: Flush to ensure the ID is generated
                projectId[0] = project.getId(); // Capture ID
                tx.commit();
            } catch (Exception e) {
                if (tx != null && tx.isActive()) {
                    tx.rollback();
                }
                throw e;
            } finally {
                em.close();
            }
        });
        
        // Second transaction - create task referencing the project
        DatabaseTestUtil.doInTransaction(() -> {
            EntityManager em = DatabaseConfig.getEntityManager();
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                // Find the project we just created
                Project persistedProject = em.find(Project.class, projectId[0]);
                Subsystem subsystem = DatabaseTestUtil.getTestSubsystem();
                
                // Create a task for the project
                Task task = new Task("Nested Transaction Task", persistedProject, subsystem);
                em.persist(task);
                em.flush(); // Flush to ensure the ID is generated
                taskId[0] = task.getId(); // Capture ID
                tx.commit();
            } catch (Exception e) {
                if (tx != null && tx.isActive()) {
                    tx.rollback();
                }
                throw e;
            } finally {
                em.close();
            }
        });
        
        // Verify both the project and task exist
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            Project project = em.find(Project.class, projectId[0]);
            assertNotNull(project, "Project should exist after transaction");
            
            Task task = em.find(Task.class, taskId[0]);
            assertNotNull(task, "Task should exist after nested transaction");
            assertEquals("Nested Transaction Task", task.getTitle());
            assertEquals(projectId[0], task.getProject().getId());
        } finally {
            em.close();
        }
    }
    
    @Test
    @Order(10)
    public void testTransactionRollback() {
        boolean result = DatabaseTestUtil.testTransactionRollback();
        assertTrue(result, "Transaction rollback test should succeed");
    }
    
    @Test
    @Order(11)
    public void testTransactionRollbackWithException() {
        // Get initial project count
        long initialCount = RepositoryFactory.getProjectRepository().count();
        
        // Try to execute a transaction that will throw an exception
        try {
            DatabaseTestUtil.doInTransaction(() -> {
                EntityManager em = DatabaseConfig.getEntityManager();
                try {
                    // Create a valid project
                    Project project = new Project(
                        "Exception_Test_" + System.currentTimeMillis(),
                        LocalDate.now(),
                        LocalDate.now().plusWeeks(4),
                        LocalDate.now().plusWeeks(6)
                    );
                    em.persist(project);
                    
                    // Now throw an exception to trigger rollback
                    throw new RuntimeException("Test exception to trigger rollback");
                } finally {
                    em.close();
                }
            });
            
            fail("Expected exception was not thrown");
        } catch (RuntimeException e) {
            // Expected exception
        }
        
        // Check the final count - should be unchanged
        long finalCount = RepositoryFactory.getProjectRepository().count();
        assertEquals(initialCount, finalCount, "Project count should not change due to rollback");
    }
    
    @Test
    @Order(12)
    public void testCascadingCleanup() {
        // Create a project with a unique identifier
        String projectName = "Cascade_" + System.currentTimeMillis();
        final Long[] projectId = new Long[1];
        final Long[] taskId = new Long[1];
        
        // Create project and task
        DatabaseTestUtil.doInTransaction(() -> {
            EntityManager em = DatabaseConfig.getEntityManager();
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                
                // Create project
                Project project = new Project(
                    projectName,
                    LocalDate.now(),
                    LocalDate.now().plusWeeks(4),
                    LocalDate.now().plusWeeks(6)
                );
                em.persist(project);
                
                // Create subsystem
                Subsystem subsystem = new Subsystem(projectName + "_Subsystem");
                subsystem.setStatus(Subsystem.Status.IN_PROGRESS);
                em.persist(subsystem);
                
                // Create task linked to project and subsystem
                Task task = new Task(projectName + "_Task1", project, subsystem);
                task.setDescription("Test cascading deletion");
                task.setEstimatedDuration(Duration.ofHours(2));
                
                // Add task to project (this sets up the bidirectional relationship)
                project.addTask(task);
                
                em.persist(task);
                em.flush(); // Ensure IDs are generated
                
                // Store IDs for later verification
                projectId[0] = project.getId();
                taskId[0] = task.getId();
                
                tx.commit();
            } catch (Exception e) {
                if (tx != null && tx.isActive()) {
                    tx.rollback();
                }
                throw e;
            } finally {
                em.close();
            }
        });
        
        // Verify entities were created
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            Project createdProject = em.find(Project.class, projectId[0]);
            Task createdTask = em.find(Task.class, taskId[0]);
            
            assertNotNull(createdProject, "Project should be found");
            assertNotNull(createdTask, "Task should be found");
            assertEquals(projectName + "_Task1", createdTask.getTitle());
        } finally {
            em.close();
        }
        
        // Now delete the project - tasks should be deleted via cascade
        DatabaseTestUtil.doInTransaction(() -> {
            EntityManager deleteEm = DatabaseConfig.getEntityManager();
            EntityTransaction tx = deleteEm.getTransaction();
            try {
                tx.begin();
                Project projectToDelete = deleteEm.find(Project.class, projectId[0]);
                if (projectToDelete != null) {
                    deleteEm.remove(projectToDelete);
                }
                tx.commit();
            } catch (Exception e) {
                if (tx != null && tx.isActive()) {
                    tx.rollback();
                }
                throw e;
            } finally {
                deleteEm.close();
            }
        });
        
        // Verify project and task were deleted
        em = DatabaseConfig.getEntityManager();
        try {
            Project deletedProject = em.find(Project.class, projectId[0]);
            Task deletedTask = em.find(Task.class, taskId[0]);
            
            assertNull(deletedProject, "Project should be deleted");
            assertNull(deletedTask, "Task1 should be deleted");
        } finally {
            em.close();
        }
    }
    
    @Test
    @Order(13)
    public void testCleanupTestData() {
        // Create a test project
        String projectName = "Cleanup_Test_" + System.currentTimeMillis();
        final Long[] projectId = new Long[1];
        
        // Create the project
        DatabaseTestUtil.doInTransaction(() -> {
            EntityManager em = DatabaseConfig.getEntityManager();
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                Project project = new Project(
                    projectName,
                    LocalDate.now(),
                    LocalDate.now().plusWeeks(4),
                    LocalDate.now().plusWeeks(6)
                );
                em.persist(project);
                em.flush(); // Ensure ID is generated
                projectId[0] = project.getId();
                tx.commit();
            } catch (Exception e) {
                if (tx != null && tx.isActive()) {
                    tx.rollback();
                }
                throw e;
            } finally {
                em.close();
            }
        });
        
        // Verify the project exists
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            Project createdProject = em.find(Project.class, projectId[0]);
            assertNotNull(createdProject, "Project should exist before cleanup");
        } finally {
            em.close();
        }
        
        // Call the cleanup method
        DatabaseTestUtil.cleanupTestData();
        
        // Verify the project was deleted
        em = DatabaseConfig.getEntityManager();
        try {
            Project deletedProject = em.find(Project.class, projectId[0]);
            assertNull(deletedProject, "Project should be deleted after cleanup");
            
            // Check the repository as well
            boolean projectExists = RepositoryFactory.getProjectRepository().findAll().stream()
                .anyMatch(p -> p.getName().equals(projectName));
            
            assertFalse(projectExists, "Project should be deleted");
        } finally {
            em.close();
        }
    }
    
    @Test
    @Order(14)
    public void testIsDatabaseInUpdateMode() {
        boolean isUpdateMode = DatabaseTestUtil.isDatabaseInUpdateMode();
        
        // Since we forced development mode in setUp, we should expect not update mode
        assertFalse(isUpdateMode, "Database should not be in update mode when in development mode");
    }
}