package org.frcpm.services;

import org.frcpm.config.DatabaseConfig;
import org.frcpm.models.Subteam;
import org.frcpm.models.Subsystem;
import org.frcpm.repositories.RepositoryFactory;
import org.frcpm.repositories.specific.SubsystemRepository;
import org.frcpm.repositories.specific.SubteamRepository;
import org.frcpm.utils.TestDatabaseCleaner;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class SubsystemServiceTest {
    
    private static final Logger LOGGER = Logger.getLogger(SubsystemServiceTest.class.getName());
    
    private SubsystemService subsystemService;
    private SubteamService subteamService;
    private SubsystemRepository subsystemRepository;
    private SubteamRepository subteamRepository;
    
    private Subteam testSubteam;
    
    @BeforeEach
    public void setUp() {
        // Initialize database in development mode for clean state
        DatabaseConfig.initialize(true);
        
        // Get services and repositories
        subsystemService = ServiceFactory.getSubsystemService();
        subteamService = ServiceFactory.getSubteamService();
        subsystemRepository = RepositoryFactory.getSubsystemRepository();
        subteamRepository = RepositoryFactory.getSubteamRepository();
        
        // Clear database first
        TestDatabaseCleaner.clearTestDatabase();
        
        // Create test subteam
        testSubteam = subteamService.createSubteam(
            "Subsystem Test Subteam",
            "#00FF00",
            "Testing, Integration"
        );
    }
    
    @AfterEach
    public void tearDown() {
        // Clean up test data - using direct entity manager for more control
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        
        try {
            tx.begin();
            
            // Delete subsystems first due to foreign key constraints
            em.createQuery("DELETE FROM Subsystem s WHERE s.name LIKE 'Test Subsystem%'")
              .executeUpdate();
            
            // Delete test subteam
            em.createQuery("DELETE FROM Subteam t WHERE t.id = :id")
              .setParameter("id", testSubteam.getId())
              .executeUpdate();
            
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            LOGGER.warning("Error during test cleanup: " + e.getMessage());
        } finally {
            em.close();
        }
        
        // Shutdown database
        DatabaseConfig.shutdown();
    }
    
    @Test
    public void testCreateSubsystem() {
        // Create a unique name to avoid conflicts
        String subsystemName = "Test Subsystem Create " + System.currentTimeMillis();
        
        Subsystem subsystem = subsystemService.createSubsystem(
            subsystemName,
            "Test subsystem description",
            Subsystem.Status.NOT_STARTED,
            testSubteam.getId()
        );
        
        assertNotNull(subsystem);
        assertNotNull(subsystem.getId());
        assertEquals(subsystemName, subsystem.getName());
        assertEquals("Test subsystem description", subsystem.getDescription());
        assertEquals(Subsystem.Status.NOT_STARTED, subsystem.getStatus());
        
        // Verify the responsible subteam relationship
        assertNotNull(subsystem.getResponsibleSubteam());
        assertEquals(testSubteam.getId(), subsystem.getResponsibleSubteam().getId());
        
        // Verify in database using repository directly
        Optional<Subsystem> found = subsystemRepository.findById(subsystem.getId());
        assertTrue(found.isPresent());
        assertEquals(subsystemName, found.get().getName());
    }
    
    @Test
    public void testCreateSubsystemWithoutSubteam() {
        // Create a unique name to avoid conflicts
        String subsystemName = "Test Subsystem NoSubteam " + System.currentTimeMillis();
        
        Subsystem subsystem = subsystemService.createSubsystem(
            subsystemName,
            "Subsystem with no responsible subteam",
            Subsystem.Status.IN_PROGRESS,
            null
        );
        
        assertNotNull(subsystem);
        assertNotNull(subsystem.getId());
        assertEquals(subsystemName, subsystem.getName());
        assertEquals(Subsystem.Status.IN_PROGRESS, subsystem.getStatus());
        assertNull(subsystem.getResponsibleSubteam());
    }
    
    @Test
    public void testFindByName() {
        // Create a unique name to avoid conflicts
        String subsystemName = "Test Subsystem FindByName " + System.currentTimeMillis();
        
        // Create a subsystem
        subsystemService.createSubsystem(
            subsystemName,
            "Subsystem for find test",
            Subsystem.Status.NOT_STARTED,
            null
        );
        
        // Find by name
        Optional<Subsystem> found = subsystemService.findByName(subsystemName);
        assertTrue(found.isPresent());
        assertEquals(subsystemName, found.get().getName());
    }
    
    @Test
    public void testFindByStatus() {
        // Create unique names to avoid conflicts
        String notStartedName = "Test Subsystem Status NotStarted " + System.currentTimeMillis();
        String inProgressName = "Test Subsystem Status InProgress " + System.currentTimeMillis();
        String completedName = "Test Subsystem Status Completed " + System.currentTimeMillis();
        
        // Create subsystems with different statuses
        subsystemService.createSubsystem(
            notStartedName,
            "Not started subsystem",
            Subsystem.Status.NOT_STARTED,
            null
        );
        
        subsystemService.createSubsystem(
            inProgressName,
            "In progress subsystem",
            Subsystem.Status.IN_PROGRESS,
            null
        );
        
        subsystemService.createSubsystem(
            completedName,
            "Completed subsystem",
            Subsystem.Status.COMPLETED,
            null
        );
        
        // Find by status - NOT_STARTED
        List<Subsystem> notStarted = subsystemService.findByStatus(Subsystem.Status.NOT_STARTED);
        assertFalse(notStarted.isEmpty());
        assertTrue(notStarted.stream().allMatch(s -> s.getStatus() == Subsystem.Status.NOT_STARTED));
        assertTrue(notStarted.stream().anyMatch(s -> s.getName().equals(notStartedName)));
        
        // Find by status - IN_PROGRESS
        List<Subsystem> inProgress = subsystemService.findByStatus(Subsystem.Status.IN_PROGRESS);
        assertFalse(inProgress.isEmpty());
        assertTrue(inProgress.stream().allMatch(s -> s.getStatus() == Subsystem.Status.IN_PROGRESS));
        assertTrue(inProgress.stream().anyMatch(s -> s.getName().equals(inProgressName)));
        
        // Find by status - COMPLETED
        List<Subsystem> completed = subsystemService.findByStatus(Subsystem.Status.COMPLETED);
        assertFalse(completed.isEmpty());
        assertTrue(completed.stream().allMatch(s -> s.getStatus() == Subsystem.Status.COMPLETED));
        assertTrue(completed.stream().anyMatch(s -> s.getName().equals(completedName)));
    }
    
    @Test
    public void testFindByResponsibleSubteam() {
        // Create unique names to avoid conflicts
        String withSubteamName = "Test Subsystem WithSubteam " + System.currentTimeMillis();
        String withoutSubteamName = "Test Subsystem WithoutSubteam " + System.currentTimeMillis();
        
        // Create subsystems with and without a responsible subteam
        subsystemService.createSubsystem(
            withSubteamName,
            "Subsystem with responsible subteam",
            Subsystem.Status.NOT_STARTED,
            testSubteam.getId()
        );
        
        subsystemService.createSubsystem(
            withoutSubteamName,
            "Subsystem without responsible subteam",
            Subsystem.Status.NOT_STARTED,
            null
        );
        
        // Find by responsible subteam
        List<Subsystem> found = subsystemService.findByResponsibleSubteam(testSubteam);
        assertFalse(found.isEmpty());
        
        // Check each found subsystem has the right subteam
        for (Subsystem subsystem : found) {
            assertNotNull(subsystem.getResponsibleSubteam());
            assertEquals(testSubteam.getId(), subsystem.getResponsibleSubteam().getId());
        }
        
        // Verify our test subsystem is in the results
        boolean foundTestSubsystem = found.stream()
            .anyMatch(s -> s.getName().equals(withSubteamName));
        assertTrue(foundTestSubsystem, "Test subsystem with subteam should be found");
    }
    
    @Test
    public void testUpdateStatus() {
        // Create a unique name to avoid conflicts
        String subsystemName = "Test Subsystem Status Update " + System.currentTimeMillis();
        
        // Create a subsystem
        Subsystem subsystem = subsystemService.createSubsystem(
            subsystemName,
            "Subsystem for status update test",
            Subsystem.Status.NOT_STARTED,
            null
        );
        
        // Update status
        Subsystem updated = subsystemService.updateStatus(
            subsystem.getId(),
            Subsystem.Status.IN_PROGRESS
        );
        
        assertNotNull(updated);
        assertEquals(Subsystem.Status.IN_PROGRESS, updated.getStatus());
        
        // Check DB record using repository directly
        Optional<Subsystem> found = subsystemRepository.findById(updated.getId());
        assertTrue(found.isPresent());
        assertEquals(Subsystem.Status.IN_PROGRESS, found.get().getStatus());
    }
    
    @Test
    public void testAssignResponsibleSubteam() {
        // Create a unique name to avoid conflicts
        String subsystemName = "Test Subsystem Assign Subteam " + System.currentTimeMillis();
        
        // Create a subsystem without a subteam
        Subsystem subsystem = subsystemService.createSubsystem(
            subsystemName,
            "Subsystem for subteam assignment test",
            Subsystem.Status.NOT_STARTED,
            null
        );
        
        assertNull(subsystem.getResponsibleSubteam());
        
        // Assign a subteam
        Subsystem updated = subsystemService.assignResponsibleSubteam(
            subsystem.getId(),
            testSubteam.getId()
        );
        
        assertNotNull(updated);
        assertNotNull(updated.getResponsibleSubteam());
        assertEquals(testSubteam.getId(), updated.getResponsibleSubteam().getId());
        
        // Check DB record using repository directly
        Optional<Subsystem> found = subsystemRepository.findById(updated.getId());
        assertTrue(found.isPresent());
        assertNotNull(found.get().getResponsibleSubteam());
        assertEquals(testSubteam.getId(), found.get().getResponsibleSubteam().getId());
    }
    
    @Test
    public void testRemoveResponsibleSubteam() {
        // Create a unique name to avoid conflicts
        String subsystemName = "Test Subsystem Remove Subteam " + System.currentTimeMillis();
        
        // Create a subsystem with a subteam
        Subsystem subsystem = subsystemService.createSubsystem(
            subsystemName,
            "Subsystem for subteam removal test",
            Subsystem.Status.NOT_STARTED,
            testSubteam.getId()
        );
        
        assertNotNull(subsystem.getResponsibleSubteam());
        
        // Remove the subteam
        Subsystem updated = subsystemService.assignResponsibleSubteam(
            subsystem.getId(),
            null
        );
        
        assertNotNull(updated);
        assertNull(updated.getResponsibleSubteam());
        
        // Check DB record using repository directly
        Optional<Subsystem> found = subsystemRepository.findById(updated.getId());
        assertTrue(found.isPresent());
        assertNull(found.get().getResponsibleSubteam());
    }
    
    @Test
    public void testDeleteById() {
        // Create a unique name to avoid conflicts
        String subsystemName = "Test Subsystem Delete " + System.currentTimeMillis();
        
        // Create a subsystem
        Subsystem subsystem = subsystemService.createSubsystem(
            subsystemName,
            "Subsystem for delete test",
            Subsystem.Status.NOT_STARTED,
            null
        );
        
        Long id = subsystem.getId();
        
        // Delete the subsystem
        boolean result = subsystemService.deleteById(id);
        assertTrue(result);
        
        // Verify the deletion using repository directly
        Optional<Subsystem> found = subsystemRepository.findById(id);
        assertFalse(found.isPresent());
    }
    
    @Test
    public void testInvalidSubsystemCreation() {
        // Test null name
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            subsystemService.createSubsystem(
                null,
                "Invalid subsystem",
                Subsystem.Status.NOT_STARTED,
                null
            );
        });
        assertTrue(exception.getMessage().contains("name cannot be empty"));
        
        // Test duplicate name
        String uniqueName = "Test Subsystem Duplicate " + System.currentTimeMillis();
        
        // Create first subsystem
        subsystemService.createSubsystem(
            uniqueName,
            "Original subsystem",
            Subsystem.Status.NOT_STARTED,
            null
        );
        
        // Try to create duplicate
        exception = assertThrows(IllegalArgumentException.class, () -> {
            subsystemService.createSubsystem(
                uniqueName, // Same name
                "Duplicate subsystem",
                Subsystem.Status.IN_PROGRESS,
                null
            );
        });
        assertTrue(exception.getMessage().contains("already exists"));
    }
}