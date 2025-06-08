// src/test/java/org/frcpm/services/impl/ProjectServiceTest.java
package org.frcpm.services.impl;

import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.repositories.spring.ProjectRepository;
import org.frcpm.repositories.spring.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Test class for ProjectService implementation using Spring Boot testing patterns.
 */
@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {
    
    @Mock
    private ProjectRepository projectRepository;
    
    @Mock
    private TaskRepository taskRepository;
    
    private ProjectServiceImpl projectService;
    
    private Project testProject;
    private LocalDate now;
    
    @BeforeEach
    void setUp() {
        // Initialize the now variable first to avoid NullPointerException
        now = LocalDate.now();
        testProject = createTestProject();
        
        // Setup mocks with complete stubbing
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(projectRepository.findAll()).thenReturn(List.of(testProject));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Initialize the service with mocked repositories
        projectService = new ProjectServiceImpl(projectRepository, taskRepository);
    }
    
    /**
     * Creates a test project for use in tests.
     */
    private Project createTestProject() {
        Project project = new Project("Test Project", now, now.plusMonths(1), now.plusMonths(2));
        project.setId(1L);
        project.setDescription("Test Description");
        return project;
    }
    
    /**
     * Creates a test task for use in tests.
     */
    private Task createTestTask(Project project, boolean completed) {
        Task task = new Task("Test Task", project, null);
        task.setId(1L);
        task.setCompleted(completed);
        return task;
    }
    
    @Test
    void testFindById() {
        // Execute
        Project result = projectService.findById(1L);
        
        // Verify
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Project", result.getName());
        
        // Verify repository was called
        verify(projectRepository).findById(1L);
    }
    
    @Test
    void testFindById_NotFound() {
        // Setup
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());
        
        // Execute
        Project result = projectService.findById(99L);
        
        // Verify
        assertNull(result);
        
        // Verify repository was called
        verify(projectRepository).findById(99L);
    }
    
    @Test
    void testFindAll() {
        // Execute
        List<Project> results = projectService.findAll();
        
        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(1L, results.get(0).getId());
        
        // Verify repository was called
        verify(projectRepository).findAll();
    }
    
    @Test
    void testSave() {
        // Setup
        Project newProject = new Project("New Project", now, now.plusMonths(1), now.plusMonths(2));
        
        // Execute
        Project result = projectService.save(newProject);
        
        // Verify
        assertNotNull(result);
        assertEquals("New Project", result.getName());
        
        // Verify repository was called
        verify(projectRepository).save(newProject);
    }
    
    @Test
    void testDelete() {
        // Setup
        doNothing().when(projectRepository).delete(any(Project.class));
        
        // Execute
        projectService.delete(testProject);
        
        // Verify repository was called
        verify(projectRepository).delete(testProject);
    }
    
    @Test
    void testDeleteById() {
        // Setup
        when(projectRepository.deleteById(anyLong())).thenReturn(true);
        
        // Execute
        boolean result = projectService.deleteById(1L);
        
        // Verify
        assertTrue(result);
        
        // Verify repository was called
        verify(projectRepository).deleteById(1L);
    }
    
    @Test
    void testCount() {
        // Setup
        when(projectRepository.count()).thenReturn(5L);
        
        // Execute
        long result = projectService.count();
        
        // Verify
        assertEquals(5L, result);
        
        // Verify repository was called
        verify(projectRepository).count();
    }
    
    @Test
    void testFindByName() {
        // Setup
        List<Project> expectedProjects = List.of(testProject);
        when(projectRepository.findByNameContainingIgnoreCase("Test")).thenReturn(expectedProjects);
        
        // Execute
        List<Project> results = projectService.findByName("Test");
        
        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Test Project", results.get(0).getName());
        
        // Verify repository was called
        verify(projectRepository).findByNameContainingIgnoreCase("Test");
    }
    
    @Test
    void testFindByDeadlineBefore() {
        // Setup
        List<Project> expectedProjects = List.of(testProject);
        LocalDate deadline = now.plusMonths(3);
        when(projectRepository.findByHardDeadlineBefore(deadline)).thenReturn(expectedProjects);
        
        // Execute
        List<Project> results = projectService.findByDeadlineBefore(deadline);
        
        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        
        // Verify repository was called
        verify(projectRepository).findByHardDeadlineBefore(deadline);
    }
    
    @Test
    void testFindByStartDateAfter() {
        // Setup
        List<Project> expectedProjects = List.of(testProject);
        LocalDate startDate = now.minusDays(1);
        when(projectRepository.findByStartDateAfter(startDate)).thenReturn(expectedProjects);
        
        // Execute
        List<Project> results = projectService.findByStartDateAfter(startDate);
        
        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        
        // Verify repository was called
        verify(projectRepository).findByStartDateAfter(startDate);
    }
    
    @Test
    void testCreateProject() {
        // Setup
        String name = "New Project";
        LocalDate startDate = now;
        LocalDate goalEndDate = now.plusMonths(1);
        LocalDate hardDeadline = now.plusMonths(2);
        
        Project expectedProject = new Project(name, startDate, goalEndDate, hardDeadline);
        when(projectRepository.save(any(Project.class))).thenReturn(expectedProject);
        
        // Execute
        Project result = projectService.createProject(name, startDate, goalEndDate, hardDeadline);
        
        // Verify
        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(startDate, result.getStartDate());
        assertEquals(goalEndDate, result.getGoalEndDate());
        assertEquals(hardDeadline, result.getHardDeadline());
        
        // Verify repository was called
        verify(projectRepository).save(any(Project.class));
    }
    
    @Test
    void testCreateProject_InvalidName() {
        // Execute and verify
        assertThrows(IllegalArgumentException.class, () -> 
            projectService.createProject(null, now, now.plusMonths(1), now.plusMonths(2))
        );
        
        // Verify repository was not called
        verify(projectRepository, never()).save(any(Project.class));
    }
    
    @Test
    void testCreateProject_InvalidDates() {
        // Execute and verify
        assertThrows(IllegalArgumentException.class, () -> 
            projectService.createProject("New Project", now, now.minusDays(1), now.plusMonths(2))
        );
        
        // Verify repository was not called
        verify(projectRepository, never()).save(any(Project.class));
    }
    
    @Test
    void testUpdateProject() {
        // Setup
        Long id = 1L;
        String name = "Updated Project";
        LocalDate startDate = now.plusDays(1);
        LocalDate goalEndDate = now.plusMonths(2);
        LocalDate hardDeadline = now.plusMonths(3);
        String description = "Updated Description";
        
        when(projectRepository.findById(id)).thenReturn(Optional.of(testProject));
        when(projectRepository.save(any(Project.class))).thenAnswer(i -> i.getArgument(0));
        
        // Execute
        Project result = projectService.updateProject(id, name, startDate, goalEndDate, hardDeadline, description);
        
        // Verify
        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(startDate, result.getStartDate());
        assertEquals(goalEndDate, result.getGoalEndDate());
        assertEquals(hardDeadline, result.getHardDeadline());
        assertEquals(description, result.getDescription());
        
        // Verify repository was called
        verify(projectRepository).findById(id);
        verify(projectRepository).save(any(Project.class));
    }
    
    @Test
    void testUpdateProject_NotFound() {
        // Setup
        Long id = 99L;
        when(projectRepository.findById(id)).thenReturn(Optional.empty());
        
        // Execute
        Project result = projectService.updateProject(id, "Updated Name", null, null, null, null);
        
        // Verify
        assertNull(result);
        
        // Verify repository was called
        verify(projectRepository).findById(id);
        verify(projectRepository, never()).save(any(Project.class));
    }
    
    @Test
    void testGetProjectSummary() {
        // Setup
        Long projectId = 1L;
        List<Task> tasks = new ArrayList<>();
        // Add 3 tasks: 1 completed, 2 not completed
        tasks.add(createTestTask(testProject, true));
        tasks.add(createTestTask(testProject, false));
        tasks.add(createTestTask(testProject, false));
        
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(taskRepository.findByProject(testProject)).thenReturn(tasks);
        
        // Execute
        Map<String, Object> summary = projectService.getProjectSummary(projectId);
        
        // Verify
        assertNotNull(summary);
        assertEquals(projectId, summary.get("id"));
        assertEquals("Test Project", summary.get("name"));
        assertEquals(3, summary.get("totalTasks"));
        assertEquals(1, summary.get("completedTasks"));
        assertEquals(33.33333333333333, summary.get("completionPercentage")); // 1/3 * 100
        
        // Verify repository was called
        verify(projectRepository).findById(projectId);
        verify(taskRepository).findByProject(testProject);
    }
    
    @Test
    void testGetProjectSummary_ProjectNotFound() {
        // Setup
        Long projectId = 99L;
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());
        
        // Execute
        Map<String, Object> summary = projectService.getProjectSummary(projectId);
        
        // Verify
        assertNotNull(summary);
        assertTrue(summary.isEmpty());
        
        // Verify repository was called
        verify(projectRepository).findById(projectId);
        verify(taskRepository, never()).findByProject(any(Project.class));
    }
}