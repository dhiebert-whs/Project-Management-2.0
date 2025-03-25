package org.frcpm.controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.frcpm.models.Project;
import org.frcpm.services.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MainControllerTest {

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private MainController mainController;

    @Mock
    private TableView<Project> projectsTable;

    @Mock
    private TableColumn<Project, String> projectNameColumn;
    
    @Mock
    private TableColumn<Project, LocalDate> projectStartColumn;
    
    @Mock
    private TableColumn<Project, LocalDate> projectGoalColumn;
    
    @Mock
    private TableColumn<Project, LocalDate> projectDeadlineColumn;
    
    @Mock
    private Tab projectTab;
    
    @Mock
    private Menu recentProjectsMenu;

    @Mock
    private ActionEvent mockEvent;
    
    @Mock
    private Scene mockScene;
    
    @Mock
    private Window mockWindow;

    private List<Project> testProjects;

    @BeforeEach
    public void setUp() {
        // Initialize test projects
        Project project1 = new Project(
                "Test Project 1",
                LocalDate.now(),
                LocalDate.now().plusWeeks(6),
                LocalDate.now().plusWeeks(8)
        );
        project1.setId(1L);
        
        Project project2 = new Project(
                "Test Project 2",
                LocalDate.now().plusDays(7),
                LocalDate.now().plusWeeks(8),
                LocalDate.now().plusWeeks(10)
        );
        project2.setId(2L);
        
        testProjects = Arrays.asList(project1, project2);
        
        // Initialize controller by setting the mock fields
        assertSame(projectsTable, mainController.getProjectsTable());
        assertSame(projectNameColumn, mainController.getProjectNameColumn());
        assertSame(projectStartColumn, mainController.getProjectStartColumn());
        assertSame(projectGoalColumn, mainController.getProjectGoalColumn());
        assertSame(projectDeadlineColumn, mainController.getProjectDeadlineColumn());
        assertSame(projectTab, mainController.getProjectTab());
        assertSame(recentProjectsMenu, mainController.getRecentProjectsMenu());
        
        // Mock project service behavior
        when(projectService.findAll()).thenReturn(testProjects);
        
        // Mock table behavior
        when(mainController.getProjectsTable().getItems()).thenReturn(FXCollections.observableArrayList());
        
        // Mock event behavior for UI tests
        when(mockEvent.getSource()).thenReturn(new Button());
        Node mockNode = mock(Node.class);
        when(mockEvent.getSource()).thenReturn(mockNode);
        when(mockNode.getScene()).thenReturn(mockScene);
        when(mockScene.getWindow()).thenReturn(mockWindow);
    }

    @Test
    public void testInitialize() {
        // Call initialize via reflection (since it's private)
        try {
            mainController.testInitialize();

            // Verify that the table columns are set up
            verify(mainController.getProjectNameColumn()).setCellValueFactory(any());
            verify(mainController.getProjectStartColumn()).setCellValueFactory(any());
            verify(mainController.getProjectGoalColumn()).setCellValueFactory(any());
            verify(mainController.getProjectDeadlineColumn()).setCellValueFactory(any());
            
            // Verify that projects are loaded
            verify(projectService).findAll();
            
        } catch (Exception e) {
            fail("Exception during initialize: " + e.getMessage());
        }
    }

    @Test
    public void testLoadProjects() {
        // Call the method to test
        try {
            mainController.testLoadProjects();

            
            // Verify that projects are loaded from the service
            verify(projectService).findAll();
            
        } catch (Exception e) {
            fail("Exception during loadProjects: " + e.getMessage());
        }
    }

    @Test
    public void testHandleNewProject() {
        // This test can only verify that the method doesn't throw exceptions
        // since it involves FXMLLoader which can't be easily mocked
        assertDoesNotThrow(() -> mainController.testHandleNewProject(mockEvent));
    }
    
    @Test
    public void testSetupShortcuts() {
        // Simply test that the method doesn't throw exceptions
        assertDoesNotThrow(() -> mainController.setupShortcuts());
    }
    
    @Test
    public void testFileMenuHandlers() {
        // Test that these methods don't throw exceptions
        assertDoesNotThrow(() -> mainController.testHandleOpenProject(mockEvent));
        assertDoesNotThrow(() -> mainController.testHandleCloseProject(mockEvent));
        assertDoesNotThrow(() -> mainController.testHandleSave(mockEvent));
        assertDoesNotThrow(() -> mainController.testHandleSaveAs(mockEvent));
        assertDoesNotThrow(() -> mainController.testHandleImportProject(mockEvent));
        assertDoesNotThrow(() -> mainController.testHandleExportProject(mockEvent));
    }
    
    @Test
    public void testEditMenuHandlers() {
        // Test that these methods don't throw exceptions
        assertDoesNotThrow(() -> mainController.testHandleUndo(mockEvent));
        assertDoesNotThrow(() -> mainController.testHandleRedo(mockEvent));
        assertDoesNotThrow(() -> mainController.testHandleCut(mockEvent));
        assertDoesNotThrow(() -> mainController.testHandleCopy(mockEvent));
        assertDoesNotThrow(() -> mainController.testHandlePaste(mockEvent));
        assertDoesNotThrow(() -> mainController.testHandleDelete(mockEvent));
        assertDoesNotThrow(() -> mainController.testHandleSelectAll(mockEvent));
        assertDoesNotThrow(() -> mainController.testHandleFind(mockEvent));
    }
    
    @Test
    public void testViewMenuHandlers() {
        // Test that these methods don't throw exceptions
        assertDoesNotThrow(() -> mainController.testHandleViewDashboard(mockEvent));
        assertDoesNotThrow(() -> mainController.testHandleViewGantt(mockEvent));
        assertDoesNotThrow(() -> mainController.testHandleViewCalendar(mockEvent));
        assertDoesNotThrow(() -> mainController.testHandleViewDaily(mockEvent));
        assertDoesNotThrow(() -> mainController.testHandleRefresh(mockEvent));
    }
    
    @Test
    public void testProjectMenuHandlers() {
        // Test that these methods don't throw exceptions
        assertDoesNotThrow(() -> mainController.testHandleProjectProperties(mockEvent));
        assertDoesNotThrow(() -> mainController.testHandleAddMilestone(mockEvent));
        assertDoesNotThrow(() -> mainController.testHandleScheduleMeeting(mockEvent));
        assertDoesNotThrow(() -> mainController.testHandleAddTask(mockEvent));
        assertDoesNotThrow(() -> mainController.testHandleProjectStatistics(mockEvent));
    }
    
    @Test
    public void testTeamMenuHandlers() {
        // Test that these methods don't throw exceptions
        assertDoesNotThrow(() -> mainController.testHandleSubteams(mockEvent));
        assertDoesNotThrow(() -> mainController.testHandleMembers(mockEvent));
        assertDoesNotThrow(() -> mainController.testHandleTakeAttendance(mockEvent));
        assertDoesNotThrow(() -> mainController.testHandleAttendanceHistory(mockEvent));
    }
    
    @Test
    public void testToolsMenuHandlers() {
        // Test that these methods don't throw exceptions
        assertDoesNotThrow(() -> mainController.testHandleSettings(mockEvent));
        assertDoesNotThrow(() -> mainController.testHandleDatabaseManagement(mockEvent));
    }
    
    @Test
    public void testHelpMenuHandlers() {
        // Test that these methods don't throw exceptions
        assertDoesNotThrow(() -> mainController.testHandleUserGuide(mockEvent));
        assertDoesNotThrow(() -> mainController.testHandleAbout(mockEvent));
    }
}