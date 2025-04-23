package org.frcpm.utils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import org.frcpm.config.DatabaseConfig;
import org.frcpm.models.*;
import org.frcpm.repositories.RepositoryFactory;
import org.frcpm.repositories.specific.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility for testing database connectivity, schema, and providing test data.
 */
public class DatabaseTestUtil {
    private static final Logger LOGGER = Logger.getLogger(DatabaseTestUtil.class.getName());
    
    // Cache for test entities to avoid creating duplicates in multiple tests
    private static final Map<String, Object> testEntitiesCache = new HashMap<>();
    
    /**
     * Tests database connectivity and schema.
     * 
     * @return true if the database is properly set up, false otherwise
     */
    public static boolean testDatabase() {
        LOGGER.info("Testing database connection and schema...");
        
        try {
            // Initialize the database with test configuration
            DatabaseConfig.initialize();
            
            // Get an EntityManager
            EntityManagerFactory emf = DatabaseConfig.getEntityManagerFactory();
            EntityManager em = emf.createEntityManager();
            
            try {
                // Test entity tables
                String[] tables = {
                    "Project", "Task", "TeamMember", "Subteam", "Subsystem", 
                    "Component", "Meeting", "Attendance", "Milestone"
                };
                
                for (String table : tables) {
                    Query query = em.createQuery("SELECT COUNT(e) FROM " + table + " e");
                    Long count = (Long) query.getSingleResult();
                    LOGGER.info(table + " table exists with " + count + " records");
                }
                
                // Test a simple join query
                Query simpleJoinQuery = em.createQuery(
                    "SELECT t.title, s.name FROM Task t JOIN t.subsystem s");
                List<?> simpleResults = simpleJoinQuery.getResultList();
                LOGGER.info("Simple join query returned " + simpleResults.size() + " results");
                
                // Test a more complex join query with conditions
                Query complexJoinQuery = em.createQuery(
                    "SELECT t.title, s.name, p.name FROM Task t " +
                    "JOIN t.subsystem s " + 
                    "JOIN t.project p " +
                    "WHERE t.progress > 0 AND t.completed = false");
                List<?> complexResults = complexJoinQuery.getResultList();
                LOGGER.info("Complex join query returned " + complexResults.size() + " results");
                
                // Test a group by query
                Query groupByQuery = em.createQuery(
                    "SELECT s.name, COUNT(t) FROM Task t " +
                    "JOIN t.subsystem s " +
                    "GROUP BY s.name");
                List<?> groupByResults = groupByQuery.getResultList();
                LOGGER.info("Group by query returned " + groupByResults.size() + " results");
                
                // Test a subquery
                Query subQuery = em.createQuery(
                    "SELECT m.name FROM Milestone m " +
                    "WHERE m.project IN (SELECT p FROM Project p WHERE p.name LIKE '%FRC%')");
                List<?> subQueryResults = subQuery.getResultList();
                LOGGER.info("Subquery returned " + subQueryResults.size() + " results");
                
                // Test lazy loading and relationship traversal
                boolean relationshipsWorking = testRelationships(em);
                if (!relationshipsWorking) {
                    LOGGER.warning("Relationship traversal test failed");
                    return false;
                }
                
                LOGGER.info("Database test completed successfully!");
                return true;
            } finally {
                em.close();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Database test failed: " + e.getMessage(), e);
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Tests relationship traversal between entities.
     * 
     * @param em the EntityManager to use
     * @return true if relationships can be traversed, false otherwise
     */
    private static boolean testRelationships(EntityManager em) {
        try {
            // Test Project -> Tasks relationship
            Query projectQuery = em.createQuery("SELECT p FROM Project p WHERE SIZE(p.tasks) > 0");
            List<Project> projects = projectQuery.getResultList();
            
            if (!projects.isEmpty()) {
                Project project = projects.get(0);
                LOGGER.info("Testing Project[" + project.getId() + "] -> Tasks relationship");
                
                if (project.getTasks().isEmpty()) {
                    LOGGER.warning("Project has no tasks even though query said it should");
                    return false;
                }
                
                // Test Task -> Subsystem relationship
                Task task = project.getTasks().get(0);
                LOGGER.info("Testing Task[" + task.getId() + "] -> Subsystem relationship");
                
                Subsystem subsystem = task.getSubsystem();
                if (subsystem == null) {
                    LOGGER.warning("Task has no subsystem");
                    return false;
                }
                
                // Test Subsystem -> Subteam relationship
                LOGGER.info("Testing Subsystem[" + subsystem.getId() + "] -> Subteam relationship");
                
                Subteam subteam = subsystem.getResponsibleSubteam();
                if (subteam != null) {
                    // Test Subteam -> TeamMember relationship
                    LOGGER.info("Testing Subteam[" + subteam.getId() + "] -> TeamMember relationship");
                    
                    List<TeamMember> members = subteam.getMembers();
                    if (members.isEmpty()) {
                        LOGGER.info("Subteam has no members (not an error, just information)");
                    } else {
                        LOGGER.info("Subteam has " + members.size() + " members");
                    }
                } else {
                    LOGGER.info("Subsystem has no responsible subteam (not an error, just information)");
                }
            } else {
                LOGGER.info("No projects with tasks found for relationship testing (not an error for empty database)");
            }
            
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error testing relationships: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Creates a standard test project with related entities for use in tests.
     * Reuses cached entities if they already exist.
     * 
     * @return the created or cached test project
     */
    public static Project createTestProject() {
        // Check cache first
        if (testEntitiesCache.containsKey("testProject")) {
            return (Project) testEntitiesCache.get("testProject");
        }
        
        LOGGER.info("Creating standard test project...");
        
        // Get repositories
        ProjectRepository projectRepo = RepositoryFactory.getProjectRepository();
        SubteamRepository subteamRepo = RepositoryFactory.getSubteamRepository();
        TeamMemberRepository memberRepo = RepositoryFactory.getTeamMemberRepository();
        SubsystemRepository subsystemRepo = RepositoryFactory.getSubsystemRepository();
        
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            
            // Create test project
            LocalDate today = LocalDate.now();
            Project project = new Project(
                "Test Project " + System.currentTimeMillis(), 
                today, 
                today.plusWeeks(4), 
                today.plusWeeks(6)
            );
            project.setDescription("Test project for unit tests");
            projectRepo.save(project);
            
            // Create test subteam
            Subteam subteam = new Subteam("Test Subteam " + System.currentTimeMillis(), "#FF0000");
            subteam.setSpecialties("Testing, Automation");
            subteamRepo.save(subteam);
            
            // Create test team member
            TeamMember member = new TeamMember(
                "testuser_" + System.currentTimeMillis(), 
                "Test", 
                "User", 
                "test@example.com"
            );
            member.setSkills("Testing");
            member.setSubteam(subteam);
            memberRepo.save(member);
            
            // Create test subsystem
            Subsystem subsystem = new Subsystem("Test Subsystem " + System.currentTimeMillis());
            subsystem.setDescription("Test subsystem for unit tests");
            subsystem.setStatus(Subsystem.Status.IN_PROGRESS);
            subsystem.setResponsibleSubteam(subteam);
            subsystemRepo.save(subsystem);
            
            tx.commit();
            
            // Add to cache
            testEntitiesCache.put("testProject", project);
            testEntitiesCache.put("testSubteam", subteam);
            testEntitiesCache.put("testMember", member);
            testEntitiesCache.put("testSubsystem", subsystem);
            
            LOGGER.info("Test project and related entities created successfully");
            return project;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            LOGGER.log(Level.SEVERE, "Error creating test project: " + e.getMessage(), e);
            throw new RuntimeException("Failed to create test project", e);
        } finally {
            em.close();
        }
    }
    
    /**
     * Retrieves a standard test subteam.
     * Creates one if it doesn't exist yet.
     * 
     * @return the test subteam
     */
    public static Subteam getTestSubteam() {
        if (!testEntitiesCache.containsKey("testSubteam")) {
            createTestProject(); // This will create all test entities
        }
        return (Subteam) testEntitiesCache.get("testSubteam");
    }
    
    /**
     * Retrieves a standard test team member.
     * Creates one if it doesn't exist yet.
     * 
     * @return the test team member
     */
    public static TeamMember getTestMember() {
        if (!testEntitiesCache.containsKey("testMember")) {
            createTestProject(); // This will create all test entities
        }
        return (TeamMember) testEntitiesCache.get("testMember");
    }
    
    /**
     * Retrieves a standard test subsystem.
     * Creates one if it doesn't exist yet.
     * 
     * @return the test subsystem
     */
    public static Subsystem getTestSubsystem() {
        if (!testEntitiesCache.containsKey("testSubsystem")) {
            createTestProject(); // This will create all test entities
        }
        return (Subsystem) testEntitiesCache.get("testSubsystem");
    }
    
    /**
     * Creates a test task linked to the standard test project and subsystem.
     * 
     * @param title the title for the task
     * @return the created test task
     */
    public static Task createTestTask(String title) {
        LOGGER.info("Creating test task: " + title);
        
        TaskRepository taskRepo = RepositoryFactory.getTaskRepository();
        
        Project project = createTestProject();
        Subsystem subsystem = getTestSubsystem();
        TeamMember member = getTestMember();
        
        Task task = new Task(title, project, subsystem);
        task.setDescription("Test task created by DatabaseTestUtil");
        task.setEstimatedDuration(Duration.ofHours(2));
        task.setPriority(Task.Priority.MEDIUM);
        task.setStartDate(LocalDate.now());
        task.setEndDate(LocalDate.now().plusDays(7));
        task.assignMember(member);
        
        return taskRepo.save(task);
    }
    
    /**
     * Performs transaction management for test code.
     * 
     * @param action the database action to perform inside a transaction
     */
    public static void doInTransaction(Runnable action) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            
            try {
                action.run();
                tx.commit();
            } catch (Exception e) {
                if (tx.isActive()) {
                    tx.rollback();
                }
                throw e;
            }
        } finally {
            em.close();
        }
    }
    
    /**
     * Cleans up test data created by the utility.
     * Should be called in @AfterAll or similar test lifecycle methods.
     */
    public static void cleanupTestData() {
        LOGGER.info("Cleaning up test data...");
        
        // Only clean up if we have test entities
        if (testEntitiesCache.isEmpty()) {
            LOGGER.info("No test entities to clean up");
            return;
        }
        
        try {
            doInTransaction(() -> {
                TaskRepository taskRepo = RepositoryFactory.getTaskRepository();
                SubsystemRepository subsystemRepo = RepositoryFactory.getSubsystemRepository();
                TeamMemberRepository memberRepo = RepositoryFactory.getTeamMemberRepository();
                SubteamRepository subteamRepo = RepositoryFactory.getSubteamRepository();
                ProjectRepository projectRepo = RepositoryFactory.getProjectRepository();
                
                // Find all tasks associated with test project
                if (testEntitiesCache.containsKey("testProject")) {
                    Project project = (Project) testEntitiesCache.get("testProject");
                    List<Task> tasks = taskRepo.findByProject(project);
                    
                    // Clean up tasks first (due to foreign key constraints)
                    for (Task task : tasks) {
                        // Clear associations
                        task.getAssignedTo().clear();
                        task.getPreDependencies().clear();
                        task.getRequiredComponents().clear();
                        taskRepo.save(task);
                        
                        // Now delete
                        taskRepo.delete(task);
                    }
                }
                
                // Delete test subsystem
                if (testEntitiesCache.containsKey("testSubsystem")) {
                    Subsystem subsystem = (Subsystem) testEntitiesCache.get("testSubsystem");
                    subsystemRepo.delete(subsystem);
                }
                
                // Delete test team member
                if (testEntitiesCache.containsKey("testMember")) {
                    TeamMember member = (TeamMember) testEntitiesCache.get("testMember");
                    memberRepo.delete(member);
                }
                
                // Delete test subteam
                if (testEntitiesCache.containsKey("testSubteam")) {
                    Subteam subteam = (Subteam) testEntitiesCache.get("testSubteam");
                    subteamRepo.delete(subteam);
                }
                
                // Delete test project
                if (testEntitiesCache.containsKey("testProject")) {
                    Project project = (Project) testEntitiesCache.get("testProject");
                    projectRepo.delete(project);
                }
                
                // Clear cache
                testEntitiesCache.clear();
            });
            
            LOGGER.info("Test data cleanup completed successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error cleaning up test data: " + e.getMessage(), e);
        }
    }
    
    /**
     * Tests transaction rollback by deliberately creating an error.
     * 
     * @return true if transaction rollback works correctly, false otherwise
     */
    public static boolean testTransactionRollback() {
        LOGGER.info("Testing transaction rollback...");
        
        try {
            // Get initial count
            ProjectRepository projectRepo = RepositoryFactory.getProjectRepository();
            long initialCount = projectRepo.count();
            
            try {
                // Try to execute a transaction that will fail
                doInTransaction(() -> {
                    // Create a valid project
                    Project project = new Project(
                        "Rollback Test Project", 
                        LocalDate.now(), 
                        LocalDate.now().plusWeeks(4), 
                        LocalDate.now().plusWeeks(6)
                    );
                    projectRepo.save(project);
                    
                    // Now create an invalid project (null required field)
                    // This should cause the transaction to be rolled back
                    Project invalidProject = new Project();
                    invalidProject.setName(null); // This will cause a constraint violation
                    projectRepo.save(invalidProject);
                });
                
                // If we get here, the invalid operation didn't throw an exception
                LOGGER.severe("Transaction should have failed but didn't");
                return false;
            } catch (Exception e) {
                // This is expected - now check that the count didn't change
                long finalCount = projectRepo.count();
                
                if (finalCount == initialCount) {
                    LOGGER.info("Transaction rollback works correctly");
                    return true;
                } else {
                    LOGGER.severe("Transaction rollback failed - count changed from " + 
                                 initialCount + " to " + finalCount);
                    return false;
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error testing transaction rollback: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Checks if the database is in update mode.
     * 
     * @return true if the database is in update mode, false if in create mode
     */
    public static boolean isDatabaseInUpdateMode() {
        LOGGER.info("Checking database schema mode...");
        
        try {
            // Create a timestamp in the database
            String marker = "TEST_MARKER_" + System.currentTimeMillis();
            
            // Create a project with the marker
            ProjectRepository projectRepo = RepositoryFactory.getProjectRepository();
            Project project = new Project(
                marker, 
                LocalDate.now(), 
                LocalDate.now().plusWeeks(4), 
                LocalDate.now().plusWeeks(6)
            );
            Project saved = projectRepo.save(project);
            Long projectId = saved.getId();
            
            // Shutdown the database
            DatabaseConfig.shutdown();
            
            // Re-initialize the database
            DatabaseConfig.initialize();
            
            // Try to find the project again
            Optional<Project> found = projectRepo.findById(projectId);
            
            // Delete the test project
            if (found.isPresent()) {
                projectRepo.delete(found.get());
            }
            
            // If the project was found, the database is in update mode
            boolean isUpdateMode = found.isPresent();
            LOGGER.info("Database is in " + (isUpdateMode ? "UPDATE" : "CREATE") + " mode");
            
            return isUpdateMode;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error checking database mode: " + e.getMessage(), e);
            return false;
        }
    }
}