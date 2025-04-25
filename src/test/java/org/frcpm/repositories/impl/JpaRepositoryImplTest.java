// src/test/java/org/frcpm/repositories/impl/JpaRepositoryImplTest.java
package org.frcpm.repositories.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import org.frcpm.config.DatabaseConfig;
import org.frcpm.models.Project;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.repositories.Repository;
import org.frcpm.repositories.RepositoryFactory;
import org.frcpm.utils.DatabaseTestUtil;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JpaRepositoryImplTest {
    
    private Repository<Project, Long> repository;
    
    @BeforeEach
    public void setUp() {
        DatabaseConfig.initialize(true); // Force development mode
        // Use ProjectRepositoryImpl as a concrete implementation of JpaRepositoryImpl
        repository = new ProjectRepositoryImpl();
        
        // Add test data
        createTestEntities();
    }
    
    @AfterEach
    public void tearDown() {
        // Clean up test data
        cleanupTestEntities();
        DatabaseConfig.shutdown();
    }
    
    private void createTestEntities() {
        Project project1 = new Project(
            "Test Base Project 1", 
            LocalDate.now(), 
            LocalDate.now().plusWeeks(6), 
            LocalDate.now().plusWeeks(8)
        );
        
        Project project2 = new Project(
            "Test Base Project 2", 
            LocalDate.now().plusDays(7), 
            LocalDate.now().plusWeeks(8), 
            LocalDate.now().plusWeeks(10)
        );
        
        repository.save(project1);
        repository.save(project2);
    }
    
    private void cleanupTestEntities() {
        List<Project> entities = repository.findAll();
        for (Project entity : entities) {
            if (entity.getName().startsWith("Test Base") || 
                entity.getName().startsWith("Test FindAll") ||
                entity.getName().startsWith("Test Transaction") ||
                entity.getName().startsWith("Test Exception") ||
                entity.getName().startsWith("Test Lazy")) {
                repository.delete(entity);
            }
        }
    }
    
    @Test
    @Order(1)
    public void testFindAll() {
        // First let's count how many test entities we have
        List<Project> allEntities = repository.findAll();
        int initialCount = allEntities.size();
        
        // Now add two specific test entities that we can verify
        Project testProject1 = new Project(
            "Test FindAll Project 1", 
            LocalDate.now(), 
            LocalDate.now().plusWeeks(4), 
            LocalDate.now().plusWeeks(6)
        );
        Project testProject2 = new Project(
            "Test FindAll Project 2", 
            LocalDate.now(), 
            LocalDate.now().plusWeeks(4), 
            LocalDate.now().plusWeeks(6)
        );
        
        repository.save(testProject1);
        repository.save(testProject2);
        
        // Get the list again and verify
        List<Project> entities = repository.findAll();
        assertNotNull(entities);
        assertTrue(entities.size() >= initialCount + 2);
        
        // Verify our test entities are in the results
        boolean foundProject1 = false;
        boolean foundProject2 = false;
        
        for (Project project : entities) {
            if (project.getName().equals("Test FindAll Project 1")) {
                foundProject1 = true;
            }
            if (project.getName().equals("Test FindAll Project 2")) {
                foundProject2 = true;
            }
        }
        
        assertTrue(foundProject1, "Test project 1 was not found in results");
        assertTrue(foundProject2, "Test project 2 was not found in results");
    }
    
    @Test
    @Order(2)
    public void testFindById() {
        // First, get an entity ID from the DB
        List<Project> entities = repository.findAll();
        Project firstEntity = entities.stream()
            .filter(e -> e.getName().startsWith("Test Base Project"))
            .findFirst().orElseThrow();
        
        // Now test findById
        Optional<Project> found = repository.findById(firstEntity.getId());
        assertTrue(found.isPresent());
        assertEquals(firstEntity.getName(), found.get().getName());
    }
    
    @Test
    @Order(3)
    public void testSave() {
        Project newEntity = new Project(
            "Test Base Save Project", 
            LocalDate.now(), 
            LocalDate.now().plusWeeks(4), 
            LocalDate.now().plusWeeks(6)
        );
        
        Project saved = repository.save(newEntity);
        assertNotNull(saved.getId());
        
        // Verify it was saved
        Optional<Project> found = repository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Test Base Save Project", found.get().getName());
    }
    
    @Test
    @Order(4)
    public void testUpdate() {
        // First, create an entity
        Project entity = new Project(
            "Test Base Update Project", 
            LocalDate.now(), 
            LocalDate.now().plusWeeks(4), 
            LocalDate.now().plusWeeks(6)
        );
        Project saved = repository.save(entity);
        
        // Now update it
        saved.setName("Updated Base Project Name");
        Project updated = repository.save(saved);
        
        // Verify the update
        assertEquals("Updated Base Project Name", updated.getName());
        
        // Check in DB
        Optional<Project> found = repository.findById(updated.getId());
        assertTrue(found.isPresent());
        assertEquals("Updated Base Project Name", found.get().getName());
    }
    
    @Test
    @Order(5)
    public void testDelete() {
        // First, create an entity
        Project entity = new Project(
            "Test Base Delete Project", 
            LocalDate.now(), 
            LocalDate.now().plusWeeks(4), 
            LocalDate.now().plusWeeks(6)
        );
        Project saved = repository.save(entity);
        Long id = saved.getId();
        
        // Now delete it
        repository.delete(saved);
        
        // Verify the deletion
        Optional<Project> found = repository.findById(id);
        assertFalse(found.isPresent());
    }
    
    @Test
    @Order(6)
    public void testDeleteById() {
        // First, create an entity
        Project entity = new Project(
            "Test Base DeleteById Project", 
            LocalDate.now(), 
            LocalDate.now().plusWeeks(4), 
            LocalDate.now().plusWeeks(6)
        );
        Project saved = repository.save(entity);
        Long id = saved.getId();
        
        // Now delete it by ID
        boolean result = repository.deleteById(id);
        assertTrue(result);
        
        // Verify the deletion
        Optional<Project> found = repository.findById(id);
        assertFalse(found.isPresent());
    }
    
    @Test
    @Order(7)
    public void testCount() {
        // Get initial count
        long initialCount = repository.count();
        
        // Add a specific number of test entities
        Project entity1 = new Project(
            "Test Count Project 1", 
            LocalDate.now(), 
            LocalDate.now().plusWeeks(4), 
            LocalDate.now().plusWeeks(6)
        );
        Project entity2 = new Project(
            "Test Count Project 2", 
            LocalDate.now(), 
            LocalDate.now().plusWeeks(4), 
            LocalDate.now().plusWeeks(6)
        );
        
        repository.save(entity1);
        repository.save(entity2);
        
        // Verify count increased by exactly the number we added
        long newCount = repository.count();
        assertEquals(initialCount + 2, newCount, 
                    "Count should increase by exactly 2 after adding 2 projects");
    }
    
    // NEW TEST METHODS BELOW
    
    @Test
    @Order(8)
    public void testTransactionCommit() {
        // Create a unique project name
        String projectName = "Test Transaction Commit " + System.currentTimeMillis();
        
        // Create and save a project in an explicit transaction
        EntityManager em = DatabaseConfig.getEntityManager();
        Long projectId = null;
        
        try {
            em.getTransaction().begin();
            
            Project project = new Project(
                projectName,
                LocalDate.now(),
                LocalDate.now().plusWeeks(4),
                LocalDate.now().plusWeeks(6)
            );
            
            em.persist(project);
            em.flush(); // Force ID generation
            projectId = project.getId();
            
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            fail("Exception during transaction: " + e.getMessage());
        } finally {
            em.close();
        }
        
        // Verify the project was saved
        assertNotNull(projectId, "Project ID should not be null after commit");
        
        Optional<Project> found = repository.findById(projectId);
        assertTrue(found.isPresent(), "Project should exist after transaction commit");
        assertEquals(projectName, found.get().getName(), "Project name should match");
    }
    
    @Test
    @Order(9)
    public void testTransactionRollback() {
        // Count existing projects
        long initialCount = repository.count();
        
        // Try to create a project but roll back
        EntityManager em = DatabaseConfig.getEntityManager();
        Long projectId = null;
        
        try {
            em.getTransaction().begin();
            
            Project project = new Project(
                "Test Transaction Rollback",
                LocalDate.now(),
                LocalDate.now().plusWeeks(4),
                LocalDate.now().plusWeeks(6)
            );
            
            em.persist(project);
            em.flush(); // Force ID generation
            projectId = project.getId();
            
            // Instead of committing, roll back
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
        
        // Verify the project was not saved
        assertNotNull(projectId, "Project ID should have been generated before rollback");
        
        Optional<Project> found = repository.findById(projectId);
        assertFalse(found.isPresent(), "Project should not exist after transaction rollback");
        
        // Verify count hasn't changed
        long finalCount = repository.count();
        assertEquals(initialCount, finalCount, "Project count should be unchanged after rollback");
    }
    
    @Test
    @Order(10)
    public void testExceptionHandling() {
        // Create a project with null start date (which is not allowed)
        Project invalidProject = new Project();
        invalidProject.setName("Test Exception Project");
        // Omitting required fields to cause an exception
        
        // Try to save the invalid project
        Exception exception = assertThrows(RuntimeException.class, () -> {
            repository.save(invalidProject);
        });
        
        // Verify the exception was a PersistenceException or has a PersistenceException cause
        Throwable cause = exception;
        boolean foundPersistenceException = false;
        
        while (cause != null) {
            if (cause instanceof PersistenceException) {
                foundPersistenceException = true;
                break;
            }
            cause = cause.getCause();
        }
        
        assertTrue(foundPersistenceException, 
                  "Exception should be or have cause of PersistenceException");
    }
    
    @Test
    @Order(11)
    public void testLazyLoading() {
        // First create a project with tasks
        Project project = new Project(
            "Test Lazy Loading Project",
            LocalDate.now(),
            LocalDate.now().plusWeeks(4),
            LocalDate.now().plusWeeks(6)
        );
        project = repository.save(project);
        
        Subsystem subsystem = DatabaseTestUtil.getTestSubsystem();
        
        // Now add tasks to the project
        Task task1 = new Task("Test Lazy Task 1", project, subsystem);
        Task task2 = new Task("Test Lazy Task 2", project, subsystem);
        
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            em.getTransaction().begin();
            
            // Use JPA directly to add tasks
            em.persist(task1);
            em.persist(task2);
            
            // Update project's task list
            project.getTasks().add(task1);
            project.getTasks().add(task2);
            em.merge(project);
            
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            fail("Failed to set up lazy loading test: " + e.getMessage());
        } finally {
            em.close();
        }
        
        // Now test lazy loading in a separate transaction
        em = DatabaseConfig.getEntityManager();
        try {
            // Find the project
            Project foundProject = em.find(Project.class, project.getId());
            assertNotNull(foundProject, "Project should exist");
            
            // Access lazy-loaded tasks
            List<Task> tasks = foundProject.getTasks();
            assertNotNull(tasks, "Tasks collection should not be null");
            assertFalse(tasks.isEmpty(), "Tasks should be loaded");
            assertEquals(2, tasks.size(), "Project should have 2 tasks");
            
            // Verify task details were loaded
            boolean foundTask1 = false;
            boolean foundTask2 = false;
            for (Task task : tasks) {
                if (task.getTitle().equals("Test Lazy Task 1")) {
                    foundTask1 = true;
                }
                if (task.getTitle().equals("Test Lazy Task 2")) {
                    foundTask2 = true;
                }
            }
            
            assertTrue(foundTask1, "Task 1 should be loaded");
            assertTrue(foundTask2, "Task 2 should be loaded");
        } finally {
            em.close();
        }
        
        // Clean up tasks
        em = DatabaseConfig.getEntityManager();
        try {
            em.getTransaction().begin();
            
            // Use JPQL to delete the tasks we created
            Query query = em.createQuery(
                "DELETE FROM Task t WHERE t.title LIKE 'Test Lazy Task%'"
            );
            query.executeUpdate();
            
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } finally {
            em.close();
        }
    }
    
    @Test
    @Order(12)
    public void testCustomQueryExecution() {
        // Create test data
        String uniquePrefix = "Custom_" + System.currentTimeMillis();
        Project project1 = new Project(
            uniquePrefix + "_Project1",
            LocalDate.now(),
            LocalDate.now().plusWeeks(4),
            LocalDate.now().plusWeeks(6)
        );
        project1.setDescription("Test custom query 1");
        
        Project project2 = new Project(
            uniquePrefix + "_Project2",
            LocalDate.now(),
            LocalDate.now().plusWeeks(5),
            LocalDate.now().plusWeeks(7)
        );
        project2.setDescription("Test custom query 2");
        
        repository.save(project1);
        repository.save(project2);
        
        // Execute a custom query
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            // Create a custom JPQL query
            TypedQuery<Project> query = em.createQuery(
                "SELECT p FROM Project p WHERE p.name LIKE :prefix", 
                Project.class
            );
            query.setParameter("prefix", uniquePrefix + "%");
            
            List<Project> results = query.getResultList();
            
            // Verify results
            assertNotNull(results, "Query results should not be null");
            assertEquals(2, results.size(), "Query should return both projects");
            
            boolean foundProject1 = false;
            boolean foundProject2 = false;
            for (Project p : results) {
                if (p.getName().equals(uniquePrefix + "_Project1")) {
                    foundProject1 = true;
                }
                if (p.getName().equals(uniquePrefix + "_Project2")) {
                    foundProject2 = true;
                }
            }
            
            assertTrue(foundProject1, "Custom query should find project 1");
            assertTrue(foundProject2, "Custom query should find project 2");
        } finally {
            em.close();
        }
    }
}