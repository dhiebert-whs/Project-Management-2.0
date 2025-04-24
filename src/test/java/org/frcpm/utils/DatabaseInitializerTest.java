package org.frcpm.utils;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.Query;

import org.frcpm.config.DatabaseConfig;
import org.frcpm.models.*;
import org.frcpm.repositories.specific.*;
import org.frcpm.repositories.RepositoryFactory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Tests for the DatabaseInitializer class to verify proper database initialization,
 * schema verification, and sample data creation.
 */
public class DatabaseInitializerTest {
    private static final Logger LOGGER = Logger.getLogger(DatabaseInitializerTest.class.getName());
    
    @BeforeEach
    public void setUp() {
        // Ensure database is fully shut down before each test
        DatabaseConfig.shutdown();
        
        // Reset database state
        System.setProperty("app.db.dev", "true");
        
        // Reinitialize with clean development mode
        DatabaseConfig.reinitialize(true);
    }

    @AfterEach
    public void tearDown() {
        // Clean up database after each test
        DatabaseConfig.shutdown();
    }
    
    @Test
    @DisplayName("Test database initialization with basic schema verification")
    public void testDatabaseInitialization() {
        // Initialize database
        boolean initResult = DatabaseInitializer.initialize(false);
        assertTrue(initResult, "Database initialization should succeed");
        
        // Verify schema was created correctly by checking if we can access tables
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            // Test if each entity table exists
            assertDoesNotThrow(() -> em.createQuery("SELECT p FROM Project p").getResultList(), 
                    "Project table should exist");
            assertDoesNotThrow(() -> em.createQuery("SELECT t FROM Task t").getResultList(), 
                    "Task table should exist");
            assertDoesNotThrow(() -> em.createQuery("SELECT m FROM Milestone m").getResultList(), 
                    "Milestone table should exist");
            assertDoesNotThrow(() -> em.createQuery("SELECT tm FROM TeamMember tm").getResultList(), 
                    "TeamMember table should exist");
            assertDoesNotThrow(() -> em.createQuery("SELECT s FROM Subteam s").getResultList(), 
                    "Subteam table should exist");
        } finally {
            em.close();
        }
    }
    
    @Test
    @DisplayName("Test sample data creation")
    public void testSampleDataCreation() {
        // Initialize with sample data
        boolean initResult = DatabaseInitializer.initialize(true);
        assertTrue(initResult, "Database initialization with sample data should succeed");
        
        // Get repositories for verification
        ProjectRepository projectRepository = RepositoryFactory.getProjectRepository();
        TaskRepository taskRepository = RepositoryFactory.getTaskRepository();
        MilestoneRepository milestoneRepository = RepositoryFactory.getMilestoneRepository();
        
        // Verify sample projects were created
        List<Project> projects = projectRepository.findAll();
        assertFalse(projects.isEmpty(), "Sample projects should be created");
        
        // Verify at least one project has a name and dates
        if (!projects.isEmpty()) {
            Project firstProject = projects.get(0);
            assertNotNull(firstProject.getName(), "Project should have a name");
            assertNotNull(firstProject.getStartDate(), "Project should have a start date");
            assertNotNull(firstProject.getGoalEndDate(), "Project should have a goal end date");
            assertNotNull(firstProject.getHardDeadline(), "Project should have a hard deadline");
            
            // Verify project has tasks and milestones
            List<Task> tasks = taskRepository.findByProject(firstProject);
            assertFalse(tasks.isEmpty(), "Project should have tasks");
            
            List<Milestone> milestones = milestoneRepository.findByProject(firstProject);
            assertFalse(milestones.isEmpty(), "Project should have milestones");
        }
    }
    
    @Test
    @DisplayName("Test database connection check")
    public void testDatabaseConnection() {
        // Test connection check
        boolean connectionOk = DatabaseInitializer.checkDatabaseConnection();
        assertTrue(connectionOk, "Database connection check should succeed");
    }
    
    @Test
    @DisplayName("Test development mode database initialization")
    public void testDevelopmentMode() {
        // Initialize in development mode
        DatabaseConfig.setDevelopmentMode(true);
        boolean initResult = DatabaseInitializer.initialize(false);
        assertTrue(initResult, "Database initialization in development mode should succeed");
        
        // Get repository for verification
        ProjectRepository projectRepository = RepositoryFactory.getProjectRepository();
        
        // Create a test project
        Project testProject = new Project(
            "Development Mode Test", 
            LocalDate.now(), 
            LocalDate.now().plusDays(30), 
            LocalDate.now().plusDays(60)
        );
        testProject = projectRepository.save(testProject);
        Long projectId = testProject.getId();
        
        // Verify the project was saved
        Optional<Project> savedProject = projectRepository.findById(projectId);
        assertTrue(savedProject.isPresent(), "Project should be saved in development mode");
        
        // Reinitialize database - should wipe the data in development mode
        DatabaseConfig.shutdown();
        DatabaseConfig.setDevelopmentMode(true);
        DatabaseInitializer.initialize(false);
        
        // Project should not exist anymore
        Optional<Project> projectAfterReinit = projectRepository.findById(projectId);
        assertFalse(projectAfterReinit.isPresent(), "Project should be wiped after reinitialization in development mode");
    }
    
    @Test
    @DisplayName("Test production mode database initialization")
    public void testProductionMode() {
        // Make sure we start clean
        DatabaseConfig.shutdown();
        
        // Force production mode with a test-specific database
        System.clearProperty("app.db.dev");
        DatabaseConfig.setDevelopmentMode(false);
        
        // Generate unique DB name for this test run to avoid file locking issues
        String testDbName = "test-prod-" + System.currentTimeMillis();
        System.setProperty("app.db.name", testDbName);
        DatabaseConfig.setDatabaseName(testDbName);
        
        // First, initialize the database directly through DatabaseConfig
        DatabaseConfig.initialize();
        
        // Now use DatabaseInitializer, which should work with the already initialized config
        boolean initResult = DatabaseInitializer.initialize(false);
        assertTrue(initResult, "Database initialization in production mode should succeed");
        
        // Get repository for verification
        ProjectRepository projectRepository = RepositoryFactory.getProjectRepository();
        
        // Create a test project
        String uniqueName = "Production Mode Test " + System.currentTimeMillis();
        Project testProject = new Project(
            uniqueName, 
            LocalDate.now(), 
            LocalDate.now().plusDays(30), 
            LocalDate.now().plusDays(60)
        );
        testProject = projectRepository.save(testProject);
        Long projectId = testProject.getId();
        
        // Verify the project was saved
        Optional<Project> savedProject = projectRepository.findById(projectId);
        assertTrue(savedProject.isPresent(), "Project should be saved in production mode");
        
        // Reinitialize database - should preserve data in production mode
        DatabaseConfig.shutdown();
        DatabaseConfig.setDevelopmentMode(false);
        DatabaseConfig.setDatabaseName(testDbName);
        DatabaseConfig.initialize();
        
        // Project should still exist
        Optional<Project> projectAfterReinit = projectRepository.findById(projectId);
        assertTrue(projectAfterReinit.isPresent(), "Project should be preserved after reinitialization in production mode");
        assertEquals(uniqueName, projectAfterReinit.get().getName(), "Project data should be preserved correctly");
        
        // Clean up test project
        projectRepository.delete(projectAfterReinit.get());
    }
    
    @Test
    @DisplayName("Test entity relationships")
    public void testEntityRelationships() {
        // Initialize with sample data
        boolean initResult = DatabaseInitializer.initialize(true);
        assertTrue(initResult, "Database initialization with sample data should succeed");
        
        // Create a separate EntityManager specifically for this test
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            // Start a transaction to keep the session open
            em.getTransaction().begin();
            
            // Query directly with this EntityManager to avoid session issues
            List<Project> projects = em.createQuery("SELECT p FROM Project p", Project.class).getResultList();
            assertFalse(projects.isEmpty(), "Sample projects should be created");
            
            for (Project project : projects) {
                List<Task> tasks = em.createQuery(
                    "SELECT t FROM Task t WHERE t.project = :project", Task.class)
                    .setParameter("project", project)
                    .getResultList();
                
                if (!tasks.isEmpty()) {
                    Task firstTask = tasks.get(0);
                    
                    // Test that we can navigate from Task to Project
                    assertEquals(project.getId(), firstTask.getProject().getId(), 
                            "Task should be linked to correct project");
                    
                    // Test that we can navigate from Task to Subsystem if it has one
                    if (firstTask.getSubsystem() != null) {
                        // Force initialization inside transaction
                        String subsystemName = firstTask.getSubsystem().getName();
                        assertNotNull(subsystemName, 
                                "Should be able to access subsystem name through task");
                        
                        // Test that we can navigate from Subsystem to Subteam if it has one
                        if (firstTask.getSubsystem().getResponsibleSubteam() != null) {
                            // Force initialization inside transaction
                            String subteamName = firstTask.getSubsystem().getResponsibleSubteam().getName();
                            assertNotNull(subteamName, 
                                    "Should be able to access subteam name through subsystem");
                        }
                    }
                    
                    // Test that we can access team members assigned to task
                    if (!firstTask.getAssignedTo().isEmpty()) {
                        TeamMember member = firstTask.getAssignedTo().iterator().next();
                        assertNotNull(member.getUsername(), "Should be able to access team member data through task");
                    }
                    
                    // Found a task with relationships - test passed
                    em.getTransaction().commit();
                    return;
                }
            }
            
            // If we get here, none of the projects had tasks with relationships
            em.getTransaction().commit();
            // This is not necessarily a failure, but we should log it
            LOGGER.warning("No tasks with relationships found to test");
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}