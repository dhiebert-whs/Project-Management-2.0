package org.frcpm.services;

import org.frcpm.config.DatabaseConfig;
import org.frcpm.models.Project;
import org.frcpm.repositories.specific.ProjectRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {
    
    private ProjectService service;
    
    @BeforeEach
    public void setUp() {
        DatabaseConfig.initialize();
        service = ServiceFactory.getProjectService();
        
        // Add test data
        createTestProjects();
    }
    
    @AfterEach
    public void tearDown() {
        // Clean up test data
        cleanupTestProjects();
        DatabaseConfig.shutdown();
    }
    
    private void createTestProjects() {
        service.createProject(
            "Test Service Project 1", 
            LocalDate.now(), 
            LocalDate.now().plusWeeks(6), 
            LocalDate.now().plusWeeks(8)
        );
        
        service.createProject(
            "Test Service Project 2", 
            LocalDate.now().plusDays(7), 
            LocalDate.now().plusWeeks(8), 
            LocalDate.now().plusWeeks(10)
        );
    }
    
    private void cleanupTestProjects() {
        List<Project> projects = service.findAll();
        for (Project project : projects) {
            if (project.getName().startsWith("Test Service Project")) {
                service.delete(project);
            }
        }
    }
    
    @Test
    public void testFindAll() {
        List<Project> projects = service.findAll();
        assertNotNull(projects);
        assertTrue(projects.size() >= 2);
    }
    
    @Test
    public void testFindById() {
        // First, get a project ID from the DB
        List<Project> projects = service.findAll();
        Project firstProject = projects.stream()
            .filter(p -> p.getName().startsWith("Test Service Project"))
            .findFirst().orElseThrow();
        
        // Now test findById
        Project found = service.findById(firstProject.getId());
        assertNotNull(found);
        assertEquals(firstProject.getName(), found.getName());
    }
    
    @Test
    public void testFindByName() {
        List<Project> projects = service.findByName("Test Service Project");
        assertFalse(projects.isEmpty());
        assertTrue(projects.stream().allMatch(p -> p.getName().contains("Test Service Project")));
    }
    
    @Test
    public void testCreateProject() {
        Project created = service.createProject(
            "Test Create Service Project", 
            LocalDate.now(), 
            LocalDate.now().plusWeeks(4), 
            LocalDate.now().plusWeeks(6)
        );
        
        assertNotNull(created.getId());
        assertEquals("Test Create Service Project", created.getName());
        
        // Verify it was saved
        Project found = service.findById(created.getId());
        assertNotNull(found);
        assertEquals("Test Create Service Project", found.getName());
    }
    
    @Test
    public void testUpdateProject() {
        // First, create a project
        Project created = service.createProject(
            "Test Update Service Project", 
            LocalDate.now(), 
            LocalDate.now().plusWeeks(4), 
            LocalDate.now().plusWeeks(6)
        );
        
        // Now update it
        Project updated = service.updateProject(
            created.getId(),
            "Updated Service Project Name",
            created.getStartDate(),
            created.getGoalEndDate(),
            created.getHardDeadline(),
            "Updated description"
        );
        
        // Verify the update
        assertNotNull(updated);
        assertEquals("Updated Service Project Name", updated.getName());
        assertEquals("Updated description", updated.getDescription());
        
        // Check in DB
        Project found = service.findById(updated.getId());
        assertNotNull(found);
        assertEquals("Updated Service Project Name", found.getName());
        assertEquals("Updated description", found.getDescription());
    }
    
    @Test
    public void testDeleteById() {
        // First, create a project
        Project created = service.createProject(
            "Test DeleteById Service Project", 
            LocalDate.now(), 
            LocalDate.now().plusWeeks(4), 
            LocalDate.now().plusWeeks(6)
        );
        Long id = created.getId();
        
        // Now delete it by ID
        boolean result = service.deleteById(id);
        assertTrue(result);
        
        // Verify the deletion
        Project found = service.findById(id);
        assertNull(found);
    }
    
    @Test
    public void testProjectSummary() {
        // First, create a project
        Project created = service.createProject(
            "Test Summary Project", 
            LocalDate.now(), 
            LocalDate.now().plusWeeks(4), 
            LocalDate.now().plusWeeks(6)
        );
        
        // Get the summary
        Map<String, Object> summary = service.getProjectSummary(created.getId());
        
        // Verify summary contents
        assertNotNull(summary);
        assertEquals(created.getId(), summary.get("id"));
        assertEquals("Test Summary Project", summary.get("name"));
        assertEquals(created.getStartDate(), summary.get("startDate"));
        assertEquals(created.getGoalEndDate(), summary.get("goalEndDate"));
        assertEquals(created.getHardDeadline(), summary.get("hardDeadline"));
        assertNotNull(summary.get("totalTasks"));
        assertNotNull(summary.get("completedTasks"));
        assertNotNull(summary.get("completionPercentage"));
        assertNotNull(summary.get("daysUntilGoal"));
        assertNotNull(summary.get("daysUntilDeadline"));
        assertNotNull(summary.get("totalMilestones"));
    }
    
    @Test
    public void testFindByDeadlineBefore() {
        LocalDate futureDate = LocalDate.now().plusWeeks(9);
        List<Project> projects = service.findByDeadlineBefore(futureDate);
        assertFalse(projects.isEmpty());
        
        for (Project project : projects) {
            assertTrue(project.getHardDeadline().isBefore(futureDate));
        }
    }
    
    @Test
    public void testFindByStartDateAfter() {
        LocalDate pastDate = LocalDate.now().minusWeeks(1);
        List<Project> projects = service.findByStartDateAfter(pastDate);
        assertFalse(projects.isEmpty());
        
        for (Project project : projects) {
            assertTrue(project.getStartDate().isAfter(pastDate));
        }
    }
    
    @Test
    public void testInvalidProjectCreation() {
        // Test null name
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.createProject(
                null, 
                LocalDate.now(), 
                LocalDate.now().plusWeeks(4), 
                LocalDate.now().plusWeeks(6)
            );
        });
        assertTrue(exception.getMessage().contains("name cannot be empty"));
        
        // Test null dates
        exception = assertThrows(IllegalArgumentException.class, () -> {
            service.createProject(
                "Test Project", 
                null, 
                LocalDate.now().plusWeeks(4), 
                LocalDate.now().plusWeeks(6)
            );
        });
        assertTrue(exception.getMessage().contains("dates cannot be null"));
        
        // Test invalid date range
        exception = assertThrows(IllegalArgumentException.class, () -> {
            service.createProject(
                "Test Project", 
                LocalDate.now(), 
                LocalDate.now().minusWeeks(1), // Goal end before start
                LocalDate.now().plusWeeks(6)
            );
        });
        assertTrue(exception.getMessage().contains("Goal end date cannot be before start date"));
    }
}