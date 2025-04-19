package org.frcpm.viewmodels;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.frcpm.models.Project;
import org.frcpm.services.MeetingService;
import org.frcpm.services.ProjectService;
import org.frcpm.services.SubsystemService;
import org.frcpm.services.TaskService;
import org.frcpm.utils.ShortcutManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for the MainViewModel class.
 */
@ExtendWith(MockitoExtension.class)
public class MainViewModelTest {
    
    @Mock
    private ProjectService projectService;
    
    @Mock
    private MeetingService meetingService;
    
    @Mock
    private TaskService taskService;
    
    @Mock
    private SubsystemService subsystemService;
    
    @Mock
    private ShortcutManager shortcutManager;
    
    private MainViewModel viewModel;
    
    @BeforeEach
    public void setUp() {
        // Do not stub any methods here that aren't used by every test
        // Only create the viewModel with mocked dependencies
        viewModel = new MainViewModel(
            projectService,
            meetingService,
            taskService,
            subsystemService,
            shortcutManager
        );
    }
    
    @Test
    public void testLoadProjects() {
        // Arrange
        List<Project> projects = new ArrayList<>();
        Project project1 = new Project("Project 1", LocalDate.now(), LocalDate.now().plusWeeks(6), LocalDate.now().plusWeeks(8));
        Project project2 = new Project("Project 2", LocalDate.now(), LocalDate.now().plusWeeks(6), LocalDate.now().plusWeeks(8));
        projects.add(project1);
        projects.add(project2);
        
        when(projectService.findAll()).thenReturn(projects);
        
        // Act
        viewModel.loadProjects();
        
        // Assert
        assertEquals(2, viewModel.getProjectList().size());
        assertEquals("Project 1", viewModel.getProjectList().get(0).getName());
        assertEquals("Project 2", viewModel.getProjectList().get(1).getName());
        assertNull(viewModel.getErrorMessage(), "Error message should be null after successful load");
        verify(projectService, times(2)).findAll(); // Once in constructor, once in loadProjects()
    }
    
    @Test
    public void testLoadProjectHandlesException() {
        // Arrange
        RuntimeException exception = new RuntimeException("Test exception");
        when(projectService.findAll()).thenThrow(exception);
        
        // Act
        viewModel.loadProjects();
        
        // Assert
        assertTrue(viewModel.getProjectList().isEmpty(), "Project list should be empty when load fails");
        assertNotNull(viewModel.getErrorMessage(), "Error message should not be null after exception");
        assertEquals("Failed to load projects from the database.", viewModel.getErrorMessage());
        verify(projectService, times(2)).findAll(); // Once in constructor, once in loadProjects()
    }
    
    @Test
    public void testOpenProject() {
        // Arrange
        Project project = new Project("Test Project", LocalDate.now(), LocalDate.now().plusWeeks(6), LocalDate.now().plusWeeks(8));
        
        // Act
        viewModel.openProject(project);
        
        // Assert
        assertEquals(project, viewModel.getSelectedProject());
        assertFalse(viewModel.isProjectTabDisabled());
        assertEquals("Test Project", viewModel.getProjectTabTitle());
    }
    
    @Test
    public void testOpenProjectWithNull() {
        // Arrange - set a project first to ensure we're testing a change
        Project project = new Project("Test Project", LocalDate.now(), LocalDate.now().plusWeeks(6), LocalDate.now().plusWeeks(8));
        viewModel.setSelectedProject(project);
        assertFalse(viewModel.isProjectTabDisabled());
        
        // Act
        viewModel.openProject(null);
        
        // Assert
        // Should not change the currently selected project when null is provided
        assertEquals(project, viewModel.getSelectedProject());
        assertFalse(viewModel.isProjectTabDisabled());
    }
    
    @Test
    public void testHandleCloseProject() {
        // Arrange
        Project project = new Project("Test Project", LocalDate.now(), LocalDate.now().plusWeeks(6), LocalDate.now().plusWeeks(8));
        viewModel.setSelectedProject(project);
        
        // Act
        viewModel.handleCloseProject();
        
        // Assert
        assertNull(viewModel.getSelectedProject());
        assertTrue(viewModel.isProjectTabDisabled());
    }
    
    @Test
    public void testFormatDate() {
        // Arrange
        LocalDate date = LocalDate.of(2025, 1, 15);
        
        // Act
        String formattedDate = viewModel.formatDate(date);
        
        // Assert
        assertEquals("01/15/2025", formattedDate);
    }
    
    @Test
    public void testFormatDateWithNull() {
        // Act
        String formattedDate = viewModel.formatDate(null);
        
        // Assert
        assertEquals("", formattedDate);
    }
    
    @Test
    public void testProjectTabTitleUpdatesWhenProjectChanges() {
        // Arrange
        Project project = new Project("New Project", LocalDate.now(), LocalDate.now().plusWeeks(6), LocalDate.now().plusWeeks(8));
        
        // Act
        viewModel.setSelectedProject(project);
        
        // Assert
        assertEquals("New Project", viewModel.getProjectTabTitle());
    }
    
    @Test
    public void testCanUseProjectCommandsReturnsTrueWhenProjectIsSelected() {
        // Arrange
        Project project = new Project("Test Project", LocalDate.now(), LocalDate.now().plusWeeks(6), LocalDate.now().plusWeeks(8));
        viewModel.setSelectedProject(project);
        
        // Assert
        assertTrue(viewModel.getAddTaskCommand().canExecute());
        assertTrue(viewModel.getAddMilestoneCommand().canExecute());
        assertTrue(viewModel.getScheduleMeetingCommand().canExecute());
        assertTrue(viewModel.getProjectPropertiesCommand().canExecute());
        assertTrue(viewModel.getProjectStatisticsCommand().canExecute());
    }
    
    @Test
    public void testCanUseProjectCommandsReturnsFalseWhenNoProjectIsSelected() {
        // Arrange
        viewModel.setSelectedProject(null);
        
        // Assert
        assertFalse(viewModel.getAddTaskCommand().canExecute());
        assertFalse(viewModel.getAddMilestoneCommand().canExecute());
        assertFalse(viewModel.getScheduleMeetingCommand().canExecute());
        assertFalse(viewModel.getProjectPropertiesCommand().canExecute());
        assertFalse(viewModel.getProjectStatisticsCommand().canExecute());
    }
}