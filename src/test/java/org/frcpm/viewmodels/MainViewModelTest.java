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
        projects.add(new Project("Project 1", LocalDate.now(), LocalDate.now().plusWeeks(6), LocalDate.now().plusWeeks(8)));
        projects.add(new Project("Project 2", LocalDate.now(), LocalDate.now().plusWeeks(6), LocalDate.now().plusWeeks(8)));
        
        when(projectService.findAll()).thenReturn(projects);
        
        // Act
        viewModel.loadProjects();
        
        // Assert
        assertEquals(2, viewModel.getProjectList().size());
        assertEquals("Project 1", viewModel.getProjectList().get(0).getName());
        assertEquals("Project 2", viewModel.getProjectList().get(1).getName());
        verify(projectService, times(1)).findAll();
    }
    
    @Test
    public void testLoadProjectsHandlesException() {
        // Arrange
        when(projectService.findAll()).thenThrow(new RuntimeException("Test exception"));
        
        // Act
        viewModel.loadProjects();
        
        // Assert
        assertTrue(viewModel.getProjectList().isEmpty());
        assertNotNull(viewModel.getErrorMessage());
        assertFalse(viewModel.getErrorMessage().isEmpty());
        verify(projectService, times(1)).findAll();
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
        // Act
        viewModel.openProject(null);
        
        // Assert
        assertNull(viewModel.getSelectedProject());
        assertTrue(viewModel.isProjectTabDisabled());
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