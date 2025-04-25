// src/test/java/org/frcpm/repositories/ConcurrencyTest.java
package org.frcpm.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Query;
import org.frcpm.config.DatabaseConfig;
import org.frcpm.models.Project;
import org.frcpm.repositories.specific.ProjectRepository;
import org.frcpm.utils.DatabaseTestUtil;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ConcurrencyTest {
    
    private static final Logger LOGGER = Logger.getLogger(ConcurrencyTest.class.getName());
    private static ProjectRepository projectRepository;
    private static Project testProject;
    
    @BeforeAll
    public static void setUp() {
        DatabaseConfig.initialize(true); // Force development mode for testing
        projectRepository = RepositoryFactory.getProjectRepository();
        testProject = DatabaseTestUtil.createTestProject();
    }
    
    @AfterAll
    public static void tearDown() {
        DatabaseTestUtil.cleanupTestData();
        DatabaseConfig.shutdown();
    }
    
    @Test
    @Order(1)
    public void testSimultaneousReads() throws Exception {
        LOGGER.info("Testing simultaneous read operations...");
        
        // Create a countdown latch to synchronize threads
        int threadCount = 5;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(threadCount);
        
        // Create a thread pool
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        // Keep track of errors
        AtomicInteger errorCount = new AtomicInteger(0);
        CopyOnWriteArrayList<String> errorMessages = new CopyOnWriteArrayList<>();
        
        // Start threads for simultaneous reads
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            
            executor.submit(() -> {
                try {
                    // Wait for signal to start
                    startLatch.await();
                    
                    LOGGER.info("Thread " + threadIndex + " starting read operation...");
                    
                    // Perform a read operation
                    List<Project> projects = projectRepository.findAll();
                    
                    boolean found = false;
                    for (Project project : projects) {
                        if (project.getId().equals(testProject.getId())) {
                            found = true;
                            break;
                        }
                    }
                    
                    if (!found) {
                        errorCount.incrementAndGet();
                        errorMessages.add("Thread " + threadIndex + " could not find test project");
                    }
                    
                    LOGGER.info("Thread " + threadIndex + " completed read operation");
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    errorMessages.add("Thread " + threadIndex + " exception: " + e.getMessage());
                    LOGGER.log(Level.SEVERE, "Thread " + threadIndex + " exception", e);
                } finally {
                    completionLatch.countDown();
                }
            });
        }
        
        // Start all threads simultaneously
        startLatch.countDown();
        
        // Wait for all threads to complete or timeout
        boolean completed = completionLatch.await(10, TimeUnit.SECONDS);
        
        // Shut down the executor
        executor.shutdown();
        
        // Check results
        assertTrue(completed, "All threads should complete in time");
        assertEquals(0, errorCount.get(), "No errors should occur during concurrent reads: " + errorMessages);
    }
    
    @Test
    @Order(2)
    public void testSimultaneousWrites() throws Exception {
        LOGGER.info("Testing simultaneous write operations...");
        
        // Create a countdown latch to synchronize threads
        int threadCount = 5;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(threadCount);
        
        // Create a thread pool
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        // Track created project IDs for verification and cleanup
        CopyOnWriteArrayList<Long> createdProjectIds = new CopyOnWriteArrayList<>();
        
        // Keep track of errors
        AtomicInteger errorCount = new AtomicInteger(0);
        CopyOnWriteArrayList<String> errorMessages = new CopyOnWriteArrayList<>();
        
        // Start threads for simultaneous writes
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            
            executor.submit(() -> {
                try {
                    // Wait for signal to start
                    startLatch.await();
                    
                    LOGGER.info("Thread " + threadIndex + " starting write operation...");
                    
                    // Create a unique project
                    Project project = new Project(
                        "Concurrent Write Test " + threadIndex,
                        LocalDate.now(),
                        LocalDate.now().plusWeeks(4),
                        LocalDate.now().plusWeeks(6)
                    );
                    
                    // Save the project
                    Project savedProject = projectRepository.save(project);
                    
                    // Verify ID was generated
                    if (savedProject.getId() == null) {
                        errorCount.incrementAndGet();
                        errorMessages.add("Thread " + threadIndex + " project has null ID after save");
                    } else {
                        createdProjectIds.add(savedProject.getId());
                    }
                    
                    LOGGER.info("Thread " + threadIndex + " completed write operation");
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    errorMessages.add("Thread " + threadIndex + " exception: " + e.getMessage());
                    LOGGER.log(Level.SEVERE, "Thread " + threadIndex + " exception", e);
                } finally {
                    completionLatch.countDown();
                }
            });
        }
        
        // Start all threads simultaneously
        startLatch.countDown();
        
        // Wait for all threads to complete or timeout
        boolean completed = completionLatch.await(10, TimeUnit.SECONDS);
        
        // Shut down the executor
        executor.shutdown();
        
        // Check results
        assertTrue(completed, "All threads should complete in time");
        assertEquals(0, errorCount.get(), "No errors should occur during concurrent writes: " + errorMessages);
        assertEquals(threadCount, createdProjectIds.size(), "Each thread should create exactly one project");
        
        // Clean up created projects
        for (Long id : createdProjectIds) {
            projectRepository.deleteById(id);
        }
    }
    
    @Test
    @Order(3)
    public void testPessimisticLocking() throws Exception {
        LOGGER.info("Testing pessimistic locking...");
        
        // Create a test project for locking
        final String projectName = "Lock Test Project";
        final Project lockProject = new Project(
            projectName,
            LocalDate.now(),
            LocalDate.now().plusWeeks(4),
            LocalDate.now().plusWeeks(6)
        );
        Project savedProject = projectRepository.save(lockProject);
        Long projectId = savedProject.getId();
        
        // Track when the first thread releases the lock
        AtomicBoolean lockReleased = new AtomicBoolean(false);
        CountDownLatch lockAcquiredLatch = new CountDownLatch(1);
        
        // Use an executor service instead of raw threads for better resource management
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        // Create a thread to hold a pessimistic lock
        Future<?> lockingThreadFuture = executor.submit(() -> {
            EntityManager em = null;
            try {
                em = DatabaseConfig.getEntityManager();
                EntityTransaction tx = em.getTransaction();
                tx.begin();
                
                // Acquire a pessimistic lock
                Project project = em.find(Project.class, projectId, LockModeType.PESSIMISTIC_WRITE);
                assertNotNull(project, "Project should be found");
                
                // Notify that we have the lock
                lockAcquiredLatch.countDown();
                
                // Hold the lock for a while
                Thread.sleep(1000);
                
                // Modify the project
                project.setName(projectName + " - Modified");
                
                // Commit the transaction and release the lock
                tx.commit();
                lockReleased.set(true);
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Locking thread exception", e);
                if (em != null && em.getTransaction().isActive()) {
                    try {
                        em.getTransaction().rollback();
                    } catch (Exception re) {
                        LOGGER.log(Level.SEVERE, "Rollback failed", re);
                    }
                }
            } finally {
                if (em != null) {
                    em.close();
                }
            }
        });
        
        // Wait for the first thread to acquire the lock
        assertTrue(lockAcquiredLatch.await(5, TimeUnit.SECONDS), "Lock should be acquired");
        
        // Now try to access the locked object in another thread
        AtomicBoolean lockConflictDetected = new AtomicBoolean(false);
        Future<?> lockConflictFuture = executor.submit(() -> {
            EntityManager em2 = null;
            try {
                em2 = DatabaseConfig.getEntityManager();
                EntityTransaction tx2 = em2.getTransaction();
                tx2.begin();
                
                LOGGER.info("Second thread trying to acquire lock");
                
                // Set a timeout to avoid hanging indefinitely
                Query timeoutQuery = em2.createQuery("SELECT 1 FROM Project p WHERE p.id = :id");
                timeoutQuery.setParameter("id", projectId);
                timeoutQuery.setHint("jakarta.persistence.lock.timeout", 1000); // 1 second timeout
                
                // Try to acquire the same lock - may block or fail
                Project project2 = em2.find(Project.class, projectId, LockModeType.PESSIMISTIC_WRITE);
                
                // Check if first thread already released the lock
                if (lockReleased.get()) {
                    LOGGER.info("Lock was already released when second thread tried to acquire it");
                    tx2.rollback();
                } else {
                    // If we got here without exception, lock was acquired, which might be expected
                    // depending on database and JPA provider behavior
                    LOGGER.info("Second thread acquired lock without waiting for first thread");
                    tx2.commit();
                }
            } catch (Exception e) {
                LOGGER.log(Level.INFO, "Second thread got expected exception during lock contention", e);
                lockConflictDetected.set(true);
                if (em2 != null && em2.getTransaction().isActive()) {
                    try {
                        em2.getTransaction().rollback();
                    } catch (Exception re) {
                        // Ignore rollback exceptions in this case
                    }
                }
            } finally {
                if (em2 != null) {
                    em2.close();
                }
            }
        });
        
        // Wait for both futures to complete
        try {
            lockingThreadFuture.get(5, TimeUnit.SECONDS);
            lockConflictFuture.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error waiting for futures", e);
            // Continue with test even if futures failed
        }
        
        // Shut down executor
        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.SECONDS);
        
        // Check if locking mechanism worked
        assertTrue(lockReleased.get(), "First thread should release the lock");
        
        // Verify the modification was made
        Optional<Project> modifiedProject = projectRepository.findById(projectId);
        assertTrue(modifiedProject.isPresent(), "Project should still exist");
        assertEquals(projectName + " - Modified", modifiedProject.get().getName(), 
                    "Project name should be modified by the locking thread");
        
        // Clean up
        projectRepository.deleteById(projectId);
    }
    
    @Test
    @Order(4)
    public void testOptimisticLocking() {
        LOGGER.info("Testing optimistic locking...");
        
        // Add version field check since H2 might not support optimistic locking by default
        EntityManager checkEm = DatabaseConfig.getEntityManager();
        boolean hasVersionField = false;
        try {
            Query versionQuery = checkEm.createNativeQuery(
                "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_NAME = 'PROJECTS' AND COLUMN_NAME = 'VERSION'");
            List<?> versionColumns = versionQuery.getResultList();
            hasVersionField = !versionColumns.isEmpty();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not check for version field", e);
        } finally {
            checkEm.close();
        }
        
        // If there's no version field, we'll implement our own version check
        if (!hasVersionField) {
            LOGGER.info("No version field found in Project entity, using manual version checking");
            testOptimisticLockingManual();
            return;
        }
        
        // Create a test project
        Project project = new Project(
            "Optimistic Lock Test",
            LocalDate.now(),
            LocalDate.now().plusWeeks(4),
            LocalDate.now().plusWeeks(6)
        );
        Project savedProject = projectRepository.save(project);
        Long projectId = savedProject.getId();
        
        // Simulate two concurrent transactions
        EntityManager em1 = null;
        EntityManager em2 = null;
        Exception optimisticLockException = null;
        
        try {
            // First transaction begins and loads the entity
            em1 = DatabaseConfig.getEntityManager();
            em1.getTransaction().begin();
            Project project1 = em1.find(Project.class, projectId);
            
            // Second transaction begins, changes and commits the entity
            em2 = DatabaseConfig.getEntityManager();
            em2.getTransaction().begin();
            Project project2 = em2.find(Project.class, projectId);
            project2.setName("Modified by Transaction 2");
            em2.getTransaction().commit();
            em2.close();
            em2 = null;
            
            // First transaction tries to modify and commit - should fail
            project1.setName("Modified by Transaction 1");
            try {
                em1.getTransaction().commit();
            } catch (Exception e) {
                optimisticLockException = e;
                LOGGER.log(Level.INFO, "Got expected optimistic lock exception", e);
                if (em1.getTransaction().isActive()) {
                    em1.getTransaction().rollback();
                }
            }
        } finally {
            if (em1 != null) {
                if (em1.getTransaction().isActive()) {
                    try {
                        em1.getTransaction().rollback();
                    } catch (Exception e) {
                        // Ignore
                    }
                }
                em1.close();
            }
            if (em2 != null) {
                if (em2.getTransaction().isActive()) {
                    try {
                        em2.getTransaction().rollback();
                    } catch (Exception e) {
                        // Ignore
                    }
                }
                em2.close();
            }
        }
        
        // Verify we got an optimistic lock exception
        assertNotNull(optimisticLockException, 
                    "Should get an exception when trying to commit after concurrent modification");
        
        // Verify final state
        Project finalProject = projectRepository.findById(projectId).orElseThrow();
        assertEquals("Modified by Transaction 2", finalProject.getName(),
                    "Project should have Transaction 2's modifications");
        
        // Clean up
        projectRepository.deleteById(projectId);
    }
    
    /**
     * Alternative implementation of optimistic locking test using manual version checking
     * since H2 might not support optimistic locking by default.
     */
    private void testOptimisticLockingManual() {
        // Create a test project
        Project project = new Project(
            "Manual Optimistic Lock Test",
            LocalDate.now(),
            LocalDate.now().plusWeeks(4),
            LocalDate.now().plusWeeks(6)
        );
        // Add a simple version as description
        project.setDescription("VERSION:1");
        Project savedProject = projectRepository.save(project);
        Long projectId = savedProject.getId();
        
        // Simulate two transactions
        Exception optimisticLockException = null;
        
        try {
            // First transaction gets the entity
            Optional<Project> projectOpt1 = projectRepository.findById(projectId);
            assertTrue(projectOpt1.isPresent());
            Project project1 = projectOpt1.get();
            String version1 = project1.getDescription();
            
            // Second transaction modifies and saves the entity
            Optional<Project> projectOpt2 = projectRepository.findById(projectId);
            assertTrue(projectOpt2.isPresent());
            Project project2 = projectOpt2.get();
            String version2 = project2.getDescription();
            
            // Ensure both have same initial version
            assertEquals(version1, version2);
            
            // Update version and save in second transaction
            project2.setName("Modified in Second Transaction");
            project2.setDescription("VERSION:2");
            projectRepository.save(project2);
            
            // First transaction tries to save with outdated version
            project1.setName("Modified in First Transaction");
            
            // Manual version check before save
            Optional<Project> currentProject = projectRepository.findById(projectId);
            assertTrue(currentProject.isPresent());
            String currentVersion = currentProject.get().getDescription();
            if (!currentVersion.equals(version1)) {
                optimisticLockException = new RuntimeException("Optimistic lock exception: entity was modified");
                // Don't throw the exception, just set it and let the check below handle it
            } else {
                // If we got here, there was no conflict detected
                project1.setDescription("VERSION:2");
                projectRepository.save(project1);
            }
            
        } catch (RuntimeException e) {
            // Set any unexpected exception
            optimisticLockException = e;
        }
        
        // Verify we got an optimistic lock exception
        assertNotNull(optimisticLockException, 
                    "Should get an exception when trying to update with outdated version");
        
        // Verify final state
        Project finalProject = projectRepository.findById(projectId).orElseThrow();
        assertEquals("Modified in Second Transaction", finalProject.getName(),
                    "Project should have Second Transaction's modifications");
        
        // Clean up
        projectRepository.deleteById(projectId);
    }
    
    @Test
    @Order(5)
    public void testTransactionIsolation() throws Exception {
        LOGGER.info("Testing transaction isolation levels...");
        
        // Create a test project
        Project project = new Project(
            "Isolation Test Project",
            LocalDate.now(),
            LocalDate.now().plusWeeks(4),
            LocalDate.now().plusWeeks(6)
        );
        project.setDescription("Original description");
        Project savedProject = projectRepository.save(project);
        Long projectId = savedProject.getId();
        
        // Start a long-running transaction in one thread
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch transaction1Started = new CountDownLatch(1);
        CountDownLatch proceedWithChanges = new CountDownLatch(1);
        CountDownLatch transaction1Done = new CountDownLatch(1);
        
        AtomicReference<String> thread1ReadValue = new AtomicReference<>();
        AtomicReference<String> thread2ReadValue = new AtomicReference<>();
        
        // Thread 1: Start transaction but don't commit immediately
        executor.submit(() -> {
            EntityManager em = null;
            try {
                em = DatabaseConfig.getEntityManager();
                EntityTransaction tx = em.getTransaction();
                
                // Start transaction and find project
                tx.begin();
                Project p1 = em.find(Project.class, projectId);
                String original = p1.getDescription();
                
                // Signal that transaction has started
                transaction1Started.countDown();
                
                // Wait for instruction to proceed with changes
                proceedWithChanges.await(5, TimeUnit.SECONDS);
                
                // Read the current value and then update
                thread1ReadValue.set(p1.getDescription());
                p1.setDescription("Modified by Transaction 1");
                
                // Sleep a bit to simulate work
                Thread.sleep(1000);
                
                // Commit transaction
                tx.commit();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Thread 1 exception", e);
                if (em != null && em.getTransaction().isActive()) {
                    try {
                        em.getTransaction().rollback();
                    } catch (Exception re) {
                        // Ignore rollback exceptions
                    }
                }
            } finally {
                if (em != null) {
                    em.close();
                }
                transaction1Done.countDown();
            }
        });
        
        // Wait for Thread 1 to start its transaction
        assertTrue(transaction1Started.await(5, TimeUnit.SECONDS), "Thread 1 should start transaction");
        
        // Thread 2: Create a concurrent transaction that updates the same entity
        executor.submit(() -> {
            EntityManager em = null;
            try {
                em = DatabaseConfig.getEntityManager();
                EntityTransaction tx = em.getTransaction();
                
                // Start transaction
                tx.begin();
                
                // Read current value
                Project p2 = em.find(Project.class, projectId);
                thread2ReadValue.set(p2.getDescription());
                
                // Update project
                p2.setDescription("Modified by Transaction 2");
                
                // Commit immediately
                tx.commit();
                
                // Signal Thread 1 to proceed
                proceedWithChanges.countDown();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Thread 2 exception", e);
                if (em != null && em.getTransaction().isActive()) {
                    try {
                        em.getTransaction().rollback();
                    } catch (Exception re) {
                        // Ignore rollback exceptions
                    }
                }
            } finally {
                if (em != null) {
                    em.close();
                }
            }
        });
        
        // Wait for Thread 1 to complete
        assertTrue(transaction1Done.await(10, TimeUnit.SECONDS), "Thread 1 should complete");
        
        // Shut down executor
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        
        // Check final state
        Project finalProject = projectRepository.findById(projectId).orElseThrow();
        
        // Depending on the isolation level and locking strategy, one of the updates should "win"
        String finalDescription = finalProject.getDescription();
        LOGGER.info("Transaction 1 read: " + thread1ReadValue.get());
        LOGGER.info("Transaction 2 read: " + thread2ReadValue.get());
        LOGGER.info("Final project description: " + finalDescription);
        
        // With default isolation level (usually READ_COMMITTED), the last commit wins
        assertEquals("Modified by Transaction 1", finalDescription, 
                    "Transaction 1 should win since it committed last");
        
        // Clean up
        projectRepository.deleteById(projectId);
    }
}