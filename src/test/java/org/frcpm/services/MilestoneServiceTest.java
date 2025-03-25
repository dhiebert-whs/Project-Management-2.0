package org.frcpm.services;

import org.frcpm.config.DatabaseConfig;
import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class MilestoneServiceTest {
    
    private MilestoneService milestoneService;
    private ProjectService projectService;
    
    private Project testProject;
    
    @BeforeEach
    public void setUp() {
        DatabaseConfig.initialize();
        
        milestoneService = ServiceFactory.getMilestoneService();
        projectService = ServiceFactory.getProjectService();
        
        // Create test project
        testProject = projectService.createProject(
            "Milestone Test Project", 
            LocalDate.now(), 
            LocalDate.now().plusWeeks(6), 
            LocalDate.now().plusWeeks(8)
        );
    }
    
    @AfterEach
    public void tearDown() {
        // Clean up test data
        try {
            // Delete milestones associated with test project
            List<Milestone> milestones = milestoneService.findByProject(testProject);
            for (Milestone milestone : milestones) {
                milestoneService.deleteById(milestone.getId());
            }
            
            // Delete test project
            projectService.deleteById(testProject.getId());
        } catch (Exception e) {
            // Log but continue with cleanup
            System.err.println("Error during cleanup: " + e.getMessage());
        }
        
        DatabaseConfig.shutdown();
    }
    
    @Test
    public void testCreateMilestone() {
        Milestone milestone = milestoneService.createMilestone(
            "Test Milestone",
            LocalDate.now().plusWeeks(4),
            testProject.getId(),
            "Test milestone description"
        );
        
        assertNotNull(milestone);
        assertNotNull(milestone.getId());
        assertEquals("Test Milestone", milestone.getName());
        assertEquals(LocalDate.now().plusWeeks(4), milestone.getDate());
        assertEquals("Test milestone description", milestone.getDescription());
        assertEquals(testProject.getId(), milestone.getProject().getId());
    }
    
    @Test
    public void testUpdateMilestoneDate() {
        // Create a milestone
        Milestone milestone = milestoneService.createMilestone(
            "Update Date Milestone",
            LocalDate.now().plusWeeks(2),
            testProject.getId(),
            "Initial description"
        );
        
        // Update the date
        LocalDate newDate = LocalDate.now().plusWeeks(3);
        
        Milestone updated = milestoneService.updateMilestoneDate(
            milestone.getId(),
            newDate
        );
        
        assertNotNull(updated);
        assertEquals(newDate, updated.getDate());
    }
    
    @Test
    public void testUpdateDescription() {
        // Create a milestone
        Milestone milestone = milestoneService.createMilestone(
            "Update Description Milestone",
            LocalDate.now().plusWeeks(2),
            testProject.getId(),
            "Initial description"
        );
        
        // Update the description
        Milestone updated = milestoneService.updateDescription(
            milestone.getId(),
            "Updated milestone description"
        );
        
        assertNotNull(updated);
        assertEquals("Updated milestone description", updated.getDescription());
    }
    
    @Test
    public void testFindByProject() {
        // Create milestones for the test project
        milestoneService.createMilestone(
            "Milestone 1",
            LocalDate.now().plusWeeks(1),
            testProject.getId(),
            "Milestone 1 description"
        );
        
        milestoneService.createMilestone(
            "Milestone 2",
            LocalDate.now().plusWeeks(3),
            testProject.getId(),
            "Milestone 2 description"
        );
        
        // Find milestones by project
        List<Milestone> milestones = milestoneService.findByProject(testProject);
        assertNotNull(milestones);
        assertEquals(2, milestones.size());
        assertTrue(milestones.stream().allMatch(m -> m.getProject().getId().equals(testProject.getId())));
    }
    
    @Test
    public void testFindByDateBefore() {
        // Create milestones with different dates
        milestoneService.createMilestone(
            "Early Milestone",
            LocalDate.now().plusWeeks(1),
            testProject.getId(),
            "Early milestone description"
        );
        
        milestoneService.createMilestone(
            "Late Milestone",
            LocalDate.now().plusWeeks(5),
            testProject.getId(),
            "Late milestone description"
        );
        
        // Find milestones before a date
        LocalDate cutoffDate = LocalDate.now().plusWeeks(3);
        List<Milestone> milestones = milestoneService.findByDateBefore(cutoffDate);
        assertNotNull(milestones);
        assertFalse(milestones.isEmpty());
        
        for (Milestone milestone : milestones) {
            assertTrue(milestone.getDate().isBefore(cutoffDate));
        }
    }
    
    @Test
    public void testFindByDateAfter() {
        // Create milestones with different dates
        milestoneService.createMilestone(
            "Early Milestone",
            LocalDate.now().plusWeeks(1),
            testProject.getId(),
            "Early milestone description"
        );
        
        milestoneService.createMilestone(
            "Late Milestone",
            LocalDate.now().plusWeeks(5),
            testProject.getId(),
            "Late milestone description"
        );
        
        // Find milestones after a date
        LocalDate cutoffDate = LocalDate.now().plusWeeks(3);
        List<Milestone> milestones = milestoneService.findByDateAfter(cutoffDate);
        assertNotNull(milestones);
        assertFalse(milestones.isEmpty());
        
        for (Milestone milestone : milestones) {
            assertTrue(milestone.getDate().isAfter(cutoffDate));
        }
    }
    
    @Test
    public void testFindByDateBetween() {
        // Create milestones with different dates
        milestoneService.createMilestone(
            "Early Milestone",
            LocalDate.now().plusWeeks(1),
            testProject.getId(),
            "Early milestone description"
        );
        
        milestoneService.createMilestone(
            "Middle Milestone",
            LocalDate.now().plusWeeks(3),
            testProject.getId(),
            "Middle milestone description"
        );
        
        milestoneService.createMilestone(
            "Late Milestone",
            LocalDate.now().plusWeeks(5),
            testProject.getId(),
            "Late milestone description"
        );
        
        // Find milestones in a date range
        LocalDate startDate = LocalDate.now().plusWeeks(2);
        LocalDate endDate = LocalDate.now().plusWeeks(4);
        
        List<Milestone> milestones = milestoneService.findByDateBetween(startDate, endDate);
        assertNotNull(milestones);
        assertFalse(milestones.isEmpty());
        
        for (Milestone milestone : milestones) {
            assertTrue(!milestone.getDate().isBefore(startDate) && !milestone.getDate().isAfter(endDate));
        }
    }
    
    @Test
    public void testGetUpcomingMilestones() {
        // Create milestones with different dates
        milestoneService.createMilestone(
            "Soon Milestone",
            LocalDate.now().plusDays(2),
            testProject.getId(),
            "Soon milestone description"
        );
        
        milestoneService.createMilestone(
            "Near Future Milestone",
            LocalDate.now().plusDays(6),
            testProject.getId(),
            "Near future milestone description"
        );
        
        milestoneService.createMilestone(
            "Far Future Milestone",
            LocalDate.now().plusDays(15),
            testProject.getId(),
            "Far future milestone description"
        );
        
        // Test finding upcoming milestones within 7 days
        List<Milestone> upcomingMilestones = milestoneService.getUpcomingMilestones(testProject.getId(), 7);
        assertNotNull(upcomingMilestones);
        assertEquals(2, upcomingMilestones.size());
        
        // Verify all returned milestones are within the next 7 days
        LocalDate sevenDaysFromNow = LocalDate.now().plusDays(7);
        assertTrue(upcomingMilestones.stream().allMatch(m -> 
            !m.getDate().isBefore(LocalDate.now()) && !m.getDate().isAfter(sevenDaysFromNow)
        ));
    }
    
    @Test
    public void testInvalidMilestoneCreation() {
        // Test null name
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            milestoneService.createMilestone(
                null, 
                LocalDate.now().plusWeeks(1),
                testProject.getId(),
                "Test description"
            );
        });
        assertTrue(exception.getMessage().contains("name cannot be empty"));
        
        // Test null date
        exception = assertThrows(IllegalArgumentException.class, () -> {
            milestoneService.createMilestone(
                "Test Milestone", 
                null,
                testProject.getId(),
                "Test description"
            );
        });
        assertTrue(exception.getMessage().contains("date cannot be null"));
        
        // Test invalid project ID
        exception = assertThrows(IllegalArgumentException.class, () -> {
            milestoneService.createMilestone(
                "Test Milestone", 
                LocalDate.now().plusWeeks(1),
                null,
                "Test description"
            );
        });
        assertTrue(exception.getMessage().contains("Project ID cannot be null"));
    }
}
