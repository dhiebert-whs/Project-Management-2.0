package org.frcpm.services;

import org.frcpm.models.Project;
import org.frcpm.repositories.specific.ProjectRepository;
import org.frcpm.services.impl.ProjectServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {
    
    @Mock
    private ProjectRepository projectRepository;
    
    private ProjectService projectService;
    
    @BeforeEach
    public void setUp() {
        projectService = new ProjectServiceImpl(projectRepository);
    }
    
    @Test
    public void testCreateProject() {
        // Setup
        String name = "Test Project";
        LocalDate startDate = LocalDate.now();
        LocalDate goalEndDate = startDate.plusWeeks(6);
        LocalDate hardDeadline = startDate.plusWeeks(8);
        
        Project createdProject = new Project(name, startDate, goalEndDate, hardDeadline);
        createdProject.setId(1L);
        
        when(projectRepository.save(any(Project.class))).thenReturn(createdProject);
        
        // Execute
        Project result = projectService.createProject(name, startDate, goalEndDate, hardDeadline);
        
        // Verify
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(name, result.getName());
        assertEquals(startDate, result.getStartDate());
        assertEquals(goalEndDate, result.getGoalEndDate());
        assertEquals(hardDeadline, result.getHardDeadline());
        
        verify(projectRepository).save(any(Project.class));
    }
    
    @Test
    public void testUpdateProject() {
        // Setup
        Long id = 1L;
        String name = "Updated Project";
        LocalDate startDate = LocalDate.now();
        LocalDate goalEndDate = startDate.plusWeeks(6);
        LocalDate hardDeadline = startDate.plusWeeks(8);
        String description = "Updated description";
        
        Project existingProject = new Project("Old Name", startDate.minusDays(1), 
            goalEndDate.minusDays(1), hardDeadline.minusDays(1));
        existingProject.setId(id);
        
        Project updatedProject = new Project(name, startDate, goalEndDate, hardDeadline);
        updatedProject.setId(id);
        updatedProject.setDescription(description);
        
        when(projectRepository.findById(id)).thenReturn(Optional.of(existingProject));
        when(projectRepository.save(any(Project.class))).thenReturn(updatedProject);
        
        // Execute
        Project result = projectService.updateProject(id, name, startDate, goalEndDate, 
                                                    hardDeadline, description);
        
        // Verify
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(name, result.getName());
        assertEquals(startDate, result.getStartDate());
        assertEquals(description, result.getDescription());
        
        verify(projectRepository).findById(id);
        verify(projectRepository).save(any(Project.class));
    }
    
    // Add more tests for other service methods
}