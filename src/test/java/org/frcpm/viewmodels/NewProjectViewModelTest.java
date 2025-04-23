package org.frcpm.viewmodels;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.time.LocalDate;

import org.frcpm.models.Project;
import org.frcpm.services.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Unit tests for the NewProjectViewModel class.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class NewProjectViewModelTest {
    
    @Mock
    private ProjectService projectService;
    
    private NewProjectViewModel viewModel;
    private Project testProject;
    
    @BeforeEach
    public void setUp() {
        // Create test project
        testProject = new Project(
            "Test Project",
            LocalDate.now(),
            LocalDate.now().plusWeeks(6),
            LocalDate.now().plusWeeks(8)
        );
        testProject.setId(1L);
        testProject.setDescription("Test project description");
        
        // Set up service mock behavior
        when(projectService.createProject(anyString(), any(LocalDate.class), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(testProject);
        when(projectService.updateProject(anyLong(), anyString(), any(LocalDate.class), any(LocalDate.class), any(LocalDate.class), anyString()))
            .thenReturn(testProject);
        
        // Create the view model
        viewModel = new NewProjectViewModel(projectService);
    }
    
    @Test
    public void testInitialState() {
        // Assert default property values
        assertEquals("", viewModel.getProjectName());
        assertNotNull(viewModel.getStartDate());
        assertNotNull(viewModel.getGoalEndDate());
        assertNotNull(viewModel.getHardDeadline());
        assertEquals("", viewModel.getDescription());
        assertFalse(viewModel.isInputValid()); // Should be invalid initially since project name is empty
        assertNull(viewModel.getCreatedProject());
        assertNull(viewModel.getErrorMessage());
        assertFalse(viewModel.isDirty());
    }
    
    @Test
    public void testValidateInputWithValidData() {
        // Arrange
        viewModel.setProjectName("Test Project");
        LocalDate today = LocalDate.now();
        viewModel.setStartDate(today);
        viewModel.setGoalEndDate(today.plusWeeks(6));
        viewModel.setHardDeadline(today.plusWeeks(8));
        
        // Assert
        assertTrue(viewModel.getCreateProjectCommand().canExecute());
    }
    
    @Test
    public void testValidateInputWithEmptyName() {
        // Arrange
        viewModel.setProjectName("");
        LocalDate today = LocalDate.now();
        viewModel.setStartDate(today);
        viewModel.setGoalEndDate(today.plusWeeks(6));
        viewModel.setHardDeadline(today.plusWeeks(8));
        
        // Assert
        assertFalse(viewModel.getCreateProjectCommand().canExecute());
    }
    
    @Test
    public void testValidateInputWithNullDates() {
        // Arrange
        viewModel.setProjectName("Test Project");
        viewModel.setStartDate(null);
        
        // Assert
        assertFalse(viewModel.getCreateProjectCommand().canExecute());
    }
    
    @Test
    public void testValidateInputWithInvalidDateRelations() {
        // Arrange
        viewModel.setProjectName("Test Project");
        LocalDate today = LocalDate.now();
        viewModel.setStartDate(today);
        viewModel.setGoalEndDate(today.minusDays(1)); // Goal date before start date
        viewModel.setHardDeadline(today.plusWeeks(8));
        
        // Assert
        assertFalse(viewModel.getCreateProjectCommand().canExecute());
    }
    
    @Test
    public void testCreateProjectSuccessWithDescription() {
        // Arrange
        viewModel.setProjectName("Test Project");
        LocalDate today = LocalDate.now();
        viewModel.setStartDate(today);
        viewModel.setGoalEndDate(today.plusWeeks(6));
        viewModel.setHardDeadline(today.plusWeeks(8));
        viewModel.setDescription("Test project description");
        
        // Act
        viewModel.createProject();
        
        // Assert
        verify(projectService).createProject(
            viewModel.getProjectName(),
            viewModel.getStartDate(),
            viewModel.getGoalEndDate(),
            viewModel.getHardDeadline()
        );
        
        verify(projectService).updateProject(
            testProject.getId(),
            viewModel.getProjectName(),
            viewModel.getStartDate(),
            viewModel.getGoalEndDate(),
            viewModel.getHardDeadline(),
            viewModel.getDescription()
        );
        
        assertEquals(testProject, viewModel.getCreatedProject());
        assertNull(viewModel.getErrorMessage());
    }
    
    @Test
    public void testCreateProjectSuccessWithoutDescription() {
        // Arrange
        viewModel.setProjectName("Test Project");
        LocalDate today = LocalDate.now();
        viewModel.setStartDate(today);
        viewModel.setGoalEndDate(today.plusWeeks(6));
        viewModel.setHardDeadline(today.plusWeeks(8));
        viewModel.setDescription(""); // Empty description
        
        // Act
        viewModel.createProject();
        
        // Assert
        verify(projectService).createProject(
            viewModel.getProjectName(),
            viewModel.getStartDate(),
            viewModel.getGoalEndDate(),
            viewModel.getHardDeadline()
        );
        
        // Update should not be called for empty description
        verify(projectService, never()).updateProject(
            anyLong(),
            anyString(),
            any(LocalDate.class),
            any(LocalDate.class),
            any(LocalDate.class),
            anyString()
        );
        
        assertEquals(testProject, viewModel.getCreatedProject());
        
        // Change this line from:
        // assertEquals("", viewModel.getErrorMessage());
        // To:
        assertNull(viewModel.getErrorMessage());
    }
    
    @Test
    public void testCreateProjectHandlesException() {
        // Arrange
        viewModel.setProjectName("Test Project");
        LocalDate today = LocalDate.now();
        viewModel.setStartDate(today);
        viewModel.setGoalEndDate(today.plusWeeks(6));
        viewModel.setHardDeadline(today.plusWeeks(8));
        
        // Set up service to throw exception
        reset(projectService); // Clear previous stubbing
        when(projectService.createProject(anyString(), any(LocalDate.class), any(LocalDate.class), any(LocalDate.class)))
            .thenThrow(new RuntimeException("Test exception"));
        
        // Act
        viewModel.createProject();
        
        // Assert
        verify(projectService).createProject(
            viewModel.getProjectName(),
            viewModel.getStartDate(),
            viewModel.getGoalEndDate(),
            viewModel.getHardDeadline()
        );
        
        assertNull(viewModel.getCreatedProject());
        assertFalse(viewModel.getErrorMessage().isEmpty());
        assertTrue(viewModel.getErrorMessage().contains("Test exception"));
    }
    
    @Test
    public void testPropertyBindings() {
        // Test that property changes are properly tracked
        
        // Arrange
        String newName = "New Project Name";
        LocalDate newStartDate = LocalDate.now().plusDays(1);
        LocalDate newGoalDate = LocalDate.now().plusWeeks(7);
        LocalDate newDeadline = LocalDate.now().plusWeeks(9);
        String newDescription = "New project description";
        
        // Act
        viewModel.projectNameProperty().set(newName);
        viewModel.startDateProperty().set(newStartDate);
        viewModel.goalEndDateProperty().set(newGoalDate);
        viewModel.hardDeadlineProperty().set(newDeadline);
        viewModel.descriptionProperty().set(newDescription);
        
        // Assert
        assertEquals(newName, viewModel.getProjectName());
        assertEquals(newStartDate, viewModel.getStartDate());
        assertEquals(newGoalDate, viewModel.getGoalEndDate());
        assertEquals(newDeadline, viewModel.getHardDeadline());
        assertEquals(newDescription, viewModel.getDescription());
    }
}