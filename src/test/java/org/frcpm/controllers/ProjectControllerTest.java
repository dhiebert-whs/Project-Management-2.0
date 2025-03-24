package org.frcpm.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import org.frcpm.models.Project;
import org.frcpm.services.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.ApplicationExtension;

import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, ApplicationExtension.class})
public class ProjectControllerTest {
    
    @Mock
    private ProjectService projectService;
    
    private ProjectController projectController;
    
    @BeforeEach
    public void setUp() throws Exception {
        projectController = new ProjectController();
        // Set mocked service
        // Initialize controller
        // Inject test data
    }
    
    @Test
    public void testLoadProjectData() {
        // Setup test project
        Project project = new Project("Test Project", LocalDate.now(), 
            LocalDate.now().plusWeeks(6), LocalDate.now().plusWeeks(8));
        project.setId(1L);
        
        // Setup mock behavior
        when(projectService.getProjectSummary(1L)).thenReturn(
            createMockSummary(10, 5, 20)
        );
        
        // Execute
        projectController.setProject(project);
        
        // Verify
        // Assert UI updates correctly
    }
    
    private java.util.Map<String, Object> createMockSummary(int totalTasks, 
                                                           int completedTasks, 
                                                           long daysUntilGoal) {
        java.util.Map<String, Object> summary = new java.util.HashMap<>();
        summary.put("totalTasks", totalTasks);
        summary.put("completedTasks", completedTasks);
        summary.put("completionPercentage", (double)completedTasks / totalTasks * 100);
        summary.put("daysUntilGoal", daysUntilGoal);
        return summary;
    }
    
    // More tests
}