package org.frcpm.services;

import org.frcpm.config.DatabaseConfig;
import org.frcpm.models.Subteam;
import org.frcpm.models.Subsystem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class SubsystemServiceTest {
    
    private SubsystemService subsystemService;
    private SubteamService subteamService;
    
    private Subteam testSubteam;
    
    @BeforeEach
    public void setUp() {
        DatabaseConfig.initialize();
        
        subsystemService = ServiceFactory.getSubsystemService();
        subteamService = ServiceFactory.getSubteamService();
        
        // Create test subteam
        testSubteam = subteamService.createSubteam(
            "Subsystem Test Subteam",
            "#00FF00",
            "Testing, Integration"
        );
    }
    
    @AfterEach
    public void tearDown() {
        // Clean up test data
        try {
            // Delete subsystems with test names
            List<Subsystem> subsystems = subsystemService.findAll();
            for (Subsystem subsystem : subsystems) {
                if (subsystem.getName().startsWith("Test Subsystem")) {
                    subsystemService.deleteById(subsystem.getId());
                }
            }
            
            // Delete test subteam
            subteamService.deleteById(testSubteam.getId());
        } catch (Exception e) {
            // Log but continue with cleanup
            System.err.println("Error during cleanup: " + e.getMessage());
        }
        
        DatabaseConfig.shutdown();
    }
    
    @Test
    public void testCreateSubsystem() {
        Subsystem subsystem = subsystemService.createSubsystem(
            "Test Subsystem 1",
            "Test subsystem description",
            Subsystem.Status.NOT_STARTED,
            testSubteam.getId()
        );
        
        assertNotNull(subsystem);
        assertNotNull(subsystem.getId());
        assertEquals("Test Subsystem 1", subsystem.getName());
        assertEquals("Test subsystem description", subsystem.getDescription());
        assertEquals(Subsystem.Status.NOT_STARTED, subsystem.getStatus());
        assertNotNull(subsystem.getResponsibleSubteam());
        assertEquals(testSubteam.getId(), subsystem.getResponsibleSubteam().getId());
    }
    
    @Test
    public void testCreateSubsystemWithoutSubteam() {
        Subsystem subsystem = subsystemService.createSubsystem(
            "Test Subsystem No Subteam",
            "Subsystem with no responsible subteam",
            Subsystem.Status.IN_PROGRESS,
            null
        );
        
        assertNotNull(subsystem);
        assertNotNull(subsystem.getId());
        assertEquals("Test Subsystem No Subteam", subsystem.getName());
        assertEquals(Subsystem.Status.IN_PROGRESS, subsystem.getStatus());
        assertNull(subsystem.getResponsibleSubteam());
    }
    
    @Test
    public void testFindByName() {
        // Create a subsystem
        subsystemService.createSubsystem(
            "Test Subsystem Find",
            "Subsystem for find test",
            Subsystem.Status.NOT_STARTED,
            null
        );
        
        // Find by name
        Optional<Subsystem> found = subsystemService.findByName("Test Subsystem Find");
        assertTrue(found.isPresent());
        assertEquals("Test Subsystem Find", found.get().getName());
    }
    
    @Test
    public void testFindByStatus() {
        // Create subsystems with different statuses
        subsystemService.createSubsystem(
            "Test Subsystem Not Started",
            "Not started subsystem",
            Subsystem.Status.NOT_STARTED,
            null
        );
        
        subsystemService.createSubsystem(
            "Test Subsystem In Progress",
            "In progress subsystem",
            Subsystem.Status.IN_PROGRESS,
            null
        );
        
        subsystemService.createSubsystem(
            "Test Subsystem Completed",
            "Completed subsystem",
            Subsystem.Status.COMPLETED,
            null
        );
        
        // Find by status - NOT_STARTED
        List<Subsystem> notStarted = subsystemService.findByStatus(Subsystem.Status.NOT_STARTED);
        assertFalse(notStarted.isEmpty());
        assertTrue(notStarted.stream().allMatch(s -> s.getStatus() == Subsystem.Status.NOT_STARTED));
        assertTrue(notStarted.stream().anyMatch(s -> s.getName().equals("Test Subsystem Not Started")));
        
        // Find by status - IN_PROGRESS
        List<Subsystem> inProgress = subsystemService.findByStatus(Subsystem.Status.IN_PROGRESS);
        assertFalse(inProgress.isEmpty());
        assertTrue(inProgress.stream().allMatch(s -> s.getStatus() == Subsystem.Status.IN_PROGRESS));
        assertTrue(inProgress.stream().anyMatch(s -> s.getName().equals("Test Subsystem In Progress")));
        
        // Find by status - COMPLETED
        List<Subsystem> completed = subsystemService.findByStatus(Subsystem.Status.COMPLETED);
        assertFalse(completed.isEmpty());
        assertTrue(completed.stream().allMatch(s -> s.getStatus() == Subsystem.Status.COMPLETED));
        assertTrue(completed.stream().anyMatch(s -> s.getName().equals("Test Subsystem Completed")));
    }
    
    @Test
    public void testFindByResponsibleSubteam() {
        // Create subsystems with and without a responsible subteam
        subsystemService.createSubsystem(
            "Test Subsystem With Subteam",
            "Subsystem with responsible subteam",
            Subsystem.Status.NOT_STARTED,
            testSubteam.getId()
        );
        
        subsystemService.createSubsystem(
            "Test Subsystem Without Subteam",
            "Subsystem without responsible subteam",
            Subsystem.Status.NOT_STARTED,
            null
        );
        
        // Find by responsible subteam
        List<Subsystem> found = subsystemService.findByResponsibleSubteam(testSubteam);
        assertFalse(found.isEmpty());
        assertTrue(found.stream().allMatch(s -> 
            s.getResponsibleSubteam() != null && 
            s.getResponsibleSubteam().getId().equals(testSubteam.getId())
        ));
        assertTrue(found.stream().anyMatch(s -> s.getName().equals("Test Subsystem With Subteam")));
    }
    
    @Test
    public void testUpdateStatus() {
        // Create a subsystem
        Subsystem subsystem = subsystemService.createSubsystem(
            "Test Subsystem Status Update",
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
        
        // Check DB record
        Subsystem found = subsystemService.findById(updated.getId());
        assertEquals(Subsystem.Status.IN_PROGRESS, found.getStatus());
    }
    
    @Test
    public void testAssignResponsibleSubteam() {
        // Create a subsystem without a subteam
        Subsystem subsystem = subsystemService.createSubsystem(
            "Test Subsystem Assign Subteam",
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
        
        // Check DB record
        Subsystem found = subsystemService.findById(updated.getId());
        assertNotNull(found.getResponsibleSubteam());
        assertEquals(testSubteam.getId(), found.getResponsibleSubteam().getId());
    }
    
    @Test
    public void testRemoveResponsibleSubteam() {
        // Create a subsystem with a subteam
        Subsystem subsystem = subsystemService.createSubsystem(
            "Test Subsystem Remove Subteam",
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
        
        // Check DB record
        Subsystem found = subsystemService.findById(updated.getId());
        assertNull(found.getResponsibleSubteam());
    }
    
    @Test
    public void testDeleteById() {
        // Create a subsystem
        Subsystem subsystem = subsystemService.createSubsystem(
            "Test Subsystem Delete",
            "Subsystem for delete test",
            Subsystem.Status.NOT_STARTED,
            null
        );
        
        Long id = subsystem.getId();
        
        // Delete the subsystem
        boolean result = subsystemService.deleteById(id);
        assertTrue(result);
        
        // Verify the deletion
        Subsystem found = subsystemService.findById(id);
        assertNull(found);
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
        subsystemService.createSubsystem(
            "Test Subsystem Duplicate",
            "Original subsystem",
            Subsystem.Status.NOT_STARTED,
            null
        );
        
        exception = assertThrows(IllegalArgumentException.class, () -> {
            subsystemService.createSubsystem(
                "Test Subsystem Duplicate", // Same name
                "Duplicate subsystem",
                Subsystem.Status.IN_PROGRESS,
                null
            );
        });
        assertTrue(exception.getMessage().contains("already exists"));
    }
}
