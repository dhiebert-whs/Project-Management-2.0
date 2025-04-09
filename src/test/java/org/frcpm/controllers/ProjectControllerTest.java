package org.frcpm.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.frcpm.binding.Command;
import org.frcpm.models.Meeting;
import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.models.Subsystem;
import org.frcpm.services.DialogService;
import org.frcpm.viewmodels.ProjectViewModel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for the ProjectController class with MVVM pattern.
 * This test class focuses on testing the controller without initializing JavaFX components.
 */
public class ProjectControllerTest {

    @Spy
    private ProjectController controller;

    @Mock
    private ProjectViewModel mockViewModel;
    
    @Mock
    private DialogService mockDialogService;
    
    @Mock
    private Command mockLoadTasksCommand;
    
    @Mock
    private Command mockLoadMilestonesCommand;
    
    @Mock
    private Command mockLoadMeetingsCommand;
    
    @Mock
    private Command mockAddTaskCommand;
    
    @Mock
    private Command mockAddMilestoneCommand;
    
    @Mock
    private Command mockScheduleMeetingCommand;

    @Mock
    private Project mockProject;
    
    @Mock
    private Task mockTask;
    
    @Mock
    private Milestone mockMilestone;
    
    @Mock
    private Meeting mockMeeting;
    
    @Mock
    private Alert mockAlert;
    
    @Mock
    private FXMLLoader mockLoader;
    
    @Mock
    private TaskController mockTaskController;
    
    @Mock
    private MilestoneController mockMilestoneController;
    
    @Mock
    private MeetingController mockMeetingController;
    
    @Mock
    private Stage mockStage;
    
    @Mock
    private Window mockWindow;
    
    @Mock
    private ChoiceDialog<Subsystem> mockChoiceDialog;

    private ObservableList<Task> taskList;
    private ObservableList<Milestone> milestoneList;
    private ObservableList<Meeting> meetingList;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Set up observable lists
        taskList = FXCollections.observableArrayList();
        milestoneList = FXCollections.observableArrayList();
        meetingList = FXCollections.observableArrayList();

        // Set up mock project
        when(mockProject.getId()).thenReturn(1L);
        when(mockProject.getName()).thenReturn("Test Project");
        when(mockProject.getDescription()).thenReturn("Test Description");
        when(mockProject.getStartDate()).thenReturn(LocalDate.now());
        when(mockProject.getGoalEndDate()).thenReturn(LocalDate.now().plusWeeks(5));
        when(mockProject.getHardDeadline()).thenReturn(LocalDate.now().plusWeeks(6));

        // Set up ViewModel mocks
        when(mockViewModel.getSelectedProject()).thenReturn(mockProject);
        when(mockViewModel.projectNameProperty())
                .thenReturn(new javafx.beans.property.SimpleStringProperty("Test Project"));
        when(mockViewModel.projectDescriptionProperty())
                .thenReturn(new javafx.beans.property.SimpleStringProperty("Test Description"));
        when(mockViewModel.startDateProperty())
                .thenReturn(new javafx.beans.property.SimpleObjectProperty<>(LocalDate.now()));
        when(mockViewModel.goalEndDateProperty())
                .thenReturn(new javafx.beans.property.SimpleObjectProperty<>(LocalDate.now().plusWeeks(5)));
        when(mockViewModel.hardDeadlineProperty())
                .thenReturn(new javafx.beans.property.SimpleObjectProperty<>(LocalDate.now().plusWeeks(6)));
        when(mockViewModel.completionPercentageProperty())
                .thenReturn(new javafx.beans.property.SimpleDoubleProperty(50.0));
        when(mockViewModel.totalTasksProperty()).thenReturn(new javafx.beans.property.SimpleIntegerProperty(10));
        when(mockViewModel.completedTasksProperty()).thenReturn(new javafx.beans.property.SimpleIntegerProperty(5));
        when(mockViewModel.daysUntilGoalProperty()).thenReturn(new javafx.beans.property.SimpleLongProperty(42));
        when(mockViewModel.errorMessageProperty())
                .thenReturn(new javafx.beans.property.SimpleStringProperty());
        
        // Set up collection mocks
        when(mockViewModel.getTasks()).thenReturn(taskList);
        when(mockViewModel.getMilestones()).thenReturn(milestoneList);
        when(mockViewModel.getMeetings()).thenReturn(meetingList);
        
        // Set up command mocks
        when(mockViewModel.getLoadTasksCommand()).thenReturn(mockLoadTasksCommand);
        when(mockViewModel.getLoadMilestonesCommand()).thenReturn(mockLoadMilestonesCommand);
        when(mockViewModel.getLoadMeetingsCommand()).thenReturn(mockLoadMeetingsCommand);
        when(mockViewModel.getAddTaskCommand()).thenReturn(mockAddTaskCommand);
        when(mockViewModel.getAddMilestoneCommand()).thenReturn(mockAddMilestoneCommand);
        when(mockViewModel.getScheduleMeetingCommand()).thenReturn(mockScheduleMeetingCommand);

        // Set the mock ViewModel on the controller
        controller.setViewModel(mockViewModel);
        controller.setDialogService(mockDialogService);
        
        // Set up mock test models
        when(mockTask.getTitle()).thenReturn("Test Task");
        when(mockTask.getSubsystem()).thenReturn(mock(Subsystem.class));
        when(mockTask.getProgress()).thenReturn(50);
        when(mockTask.getEndDate()).thenReturn(LocalDate.now().plusDays(7));
        
        when(mockMilestone.getName()).thenReturn("Test Milestone");
        when(mockMilestone.getDate()).thenReturn(LocalDate.now().plusDays(14));
        
        when(mockMeeting.getDate()).thenReturn(LocalDate.now().plusDays(3));
        when(mockMeeting.getStartTime()).thenReturn(java.time.LocalTime.of(9, 0));
        when(mockMeeting.getEndTime()).thenReturn(java.time.LocalTime.of(10, 30));
        
        // Mock alert creation
        doReturn(mockAlert).when(controller).createAlert(any(Alert.AlertType.class));
        
        // Mock FXML loading
        doReturn(mockLoader).when(controller).createFXMLLoader(anyString());
        
        // Mock dialog stage creation
        doReturn(mockStage).when(controller).createDialogStage(anyString(), any(), any());
        
        // Mock window from component
        doReturn(mockWindow).when(controller).getWindowFromComponent(any());
        
        // Mock choice dialog creation
        doReturn(mockChoiceDialog).when(controller).createChoiceDialog(any(), anyList());
        
        try {
            Parent mockParent = mock(Parent.class);
            when(mockLoader.load()).thenReturn(mockParent);
        } catch (IOException e) {
            fail("Error setting up mock loader: " + e.getMessage());
        }
        
        // Mock getController() for loaders
        when(mockLoader.getController())
                .thenReturn(mockTaskController)  // First call returns TaskController
                .thenReturn(mockMilestoneController)  // Second call returns MilestoneController
                .thenReturn(mockMeetingController);   // Third call returns MeetingController
    }

    @Test
    public void testSetProject() {
        // Act
        controller.setProject(mockProject);

        // Assert
        verify(mockViewModel).initExistingProject(mockProject);
        assertEquals(mockProject, controller.getProject());
    }

    @Test
    public void testGetViewModel() {
        // Act
        ProjectViewModel result = controller.getViewModel();

        // Assert
        assertEquals(mockViewModel, result);
    }

    @Test
    public void testGetProject() {
        // Act
        Project result = controller.getProject();

        // Assert
        assertEquals(mockProject, result);
        verify(mockViewModel).getSelectedProject();
    }

    @Test
    public void testHandleEditTask() {
        // Act
        controller.handleEditTask(mockTask);

        // Assert
        verify(mockTaskController).initExistingTask(mockTask);
        verify(controller).showAndWaitDialog(mockStage);
        verify(mockLoadTasksCommand).execute();
    }

    @Test
    public void testHandleEditMilestone() {
        // Arrange
        Milestone mockMilestone = mock(Milestone.class);
        FXMLLoader mockLoader = mock(FXMLLoader.class);
        Parent mockParent = mock(Parent.class);
        Stage mockStage = mock(Stage.class);
        MilestoneController mockMilestoneController = mock(MilestoneController.class);
        
        try {
            // Mock the FXMLLoader behavior
            doReturn(mockLoader).when(controller).createFXMLLoader(anyString());
            when(mockLoader.load()).thenReturn(mockParent);
            when(mockLoader.getController()).thenReturn(mockMilestoneController);
            
            // Mock the dialog behavior
            doReturn(mockStage).when(controller).createDialogStage(anyString(), any(), any());
            doNothing().when(controller).showAndWaitDialog(any(Stage.class));
            
            // Act
            controller.handleEditMilestone(mockMilestone);
            
            // Assert
            verify(mockLoader).load();
            verify(mockMilestoneController).initExistingMilestone(mockMilestone); // Updated method name
            verify(controller).showAndWaitDialog(mockStage);
            verify(mockViewModel).getLoadMilestonesCommand();
        } catch (IOException e) {
            fail("Test should not throw exception: " + e.getMessage());
        }
    }

    @Test
    public void testHandleEditMeeting() {
        // Act
        controller.handleEditMeeting(mockMeeting);

        // Assert
        verify(mockMeetingController).setMeeting(mockMeeting);
        verify(controller).showAndWaitDialog(mockStage);
        verify(mockLoadMeetingsCommand).execute();
    }

    @Test
    public void testShowChoiceDialog() {
        // Arrange
        List<Subsystem> subsystems = new ArrayList<>();
        Subsystem subsystem = mock(Subsystem.class);
        subsystems.add(subsystem);
        
        when(mockChoiceDialog.showAndWait()).thenReturn(Optional.of(subsystem));
        
        // Act
        Optional<Subsystem> result = controller.showChoiceDialog(
            "Test Title", "Test Header", "Test Content", subsystem, subsystems);
        
        // Assert
        verify(mockChoiceDialog).setTitle("Test Title");
        verify(mockChoiceDialog).setHeaderText("Test Header");
        verify(mockChoiceDialog).setContentText("Test Content");
        verify(mockChoiceDialog).showAndWait();
        assertTrue(result.isPresent());
        assertEquals(subsystem, result.get());
    }

    @Test
    public void testShowErrorAlert() {
        // Act
        controller.showErrorAlert("Test Title", "Test Message");
        
        // Assert
        verify(mockAlert).setTitle("Error");
        verify(mockAlert).setHeaderText("Test Title");
        verify(mockAlert).setContentText("Test Message");
        verify(mockAlert).showAndWait();
    }

    @Test
    public void testShowInfoAlert() {
        // Act
        controller.showInfoAlert("Test Title", "Test Message");
        
        // Assert
        verify(mockAlert).setTitle("Information");
        verify(mockAlert).setHeaderText("Test Title");
        verify(mockAlert).setContentText("Test Message");
        verify(mockAlert).showAndWait();
    }
    
    @Test
    public void testSetViewModel() {
        // Arrange
        ProjectViewModel newViewModel = mock(ProjectViewModel.class);
        when(newViewModel.errorMessageProperty()).thenReturn(new javafx.beans.property.SimpleStringProperty());
        
        // Act
        controller.setViewModel(newViewModel);
        
        // Assert
        assertEquals(newViewModel, controller.getViewModel());
    }
    
    @Test
    public void testErrorMessageListener() {
        // Arrange
        javafx.beans.property.SimpleStringProperty errorProperty = new javafx.beans.property.SimpleStringProperty();
        when(mockViewModel.errorMessageProperty()).thenReturn(errorProperty);
        controller.setViewModel(mockViewModel); // Reconnect the listener
        
        // Act - Simulate error message change
        errorProperty.set("Test Error");
        
        // Assert
        verify(controller).showErrorAlert(eq("Error"), eq("Test Error"));
        assertEquals("", errorProperty.get()); // Error message should be cleared
    }
    
    @Test
    public void testCreateDialogStage() {
        // Arrange
        Window mockWindow = mock(Window.class);
        Parent mockParent = mock(Parent.class);
        
        doCallRealMethod().when(controller).createDialogStage(anyString(), any(), any());
        
        try {
            // Act - This will throw an exception due to JavaFX not being initialized, but we can still test the mocked version
            Stage result = controller.createDialogStage("Test Title", mockWindow, mockParent);
            fail("Expected an exception due to JavaFX not being initialized");
        } catch (Exception e) {
            // This is expected in a test environment without JavaFX runtime
            // Now test with the mocked version
            doReturn(mockStage).when(controller).createDialogStage(anyString(), any(), any());
            Stage mockedResult = controller.createDialogStage("Test Title", mockWindow, mockParent);
            
            // Assert
            assertNotNull(mockedResult);
            assertEquals(mockStage, mockedResult);
        }
    }
    
    @Test
    public void testShowAndWaitDialog() {
        // Arrange
        doCallRealMethod().when(controller).showAndWaitDialog(any());
        
        // Act
        controller.showAndWaitDialog(mockStage);
        
        // Assert - verify no exception is thrown
        // In real code, this would call showAndWait() on the stage
        verify(mockStage, never()).showAndWait(); // Never called because we're in a test environment
    }
    
    @Test
    public void testCreateAlert() {
        // Arrange
        doCallRealMethod().when(controller).createAlert(any());
        
        try {
            // Act
            Alert result = controller.createAlert(Alert.AlertType.INFORMATION);
            
            // This should fail because we're in a test environment without JavaFX
            fail("Expected an exception due to JavaFX not being initialized");
        } catch (Exception e) {
            // This is expected
            // Now test with mock
            Alert mockedResult = controller.createAlert(Alert.AlertType.INFORMATION);
            
            // Assert
            assertNotNull(mockedResult);
            assertEquals(mockAlert, mockedResult);
        }
    }
    
    @Test
    public void testGetWindowFromComponent() {
        // Arrange
        Control mockControl = mock(Control.class);
        Scene mockScene = mock(Scene.class);
        
        when(mockControl.getScene()).thenReturn(mockScene);
        when(mockScene.getWindow()).thenReturn(mockWindow);
        
        doCallRealMethod().when(controller).getWindowFromComponent(any());
        
        // Act
        Window result = controller.getWindowFromComponent(mockControl);
        
        // Assert
        assertEquals(mockWindow, result);
    }
    
    @Test
    public void testGetWindowFromComponentWithNulls() {
        // Arrange
        Control mockControl = mock(Control.class);
        when(mockControl.getScene()).thenReturn(null);
        
        doCallRealMethod().when(controller).getWindowFromComponent(any());
        
        // Act
        Window result = controller.getWindowFromComponent(mockControl);
        
        // Assert
        assertNull(result);
        
        // Also test with null control
        result = controller.getWindowFromComponent(null);
        assertNull(result);
    }
    
    @Test
    public void testCreateFXMLLoader() {
        // We can't fully test this due to JavaFX toolkit not being initialized in unit tests
        doCallRealMethod().when(controller).createFXMLLoader(anyString());
        
        try {
            // Act
            FXMLLoader result = controller.createFXMLLoader("/fxml/TaskView.fxml");
            
            // This might fail because we're in a test environment without JavaFX
            assertNotNull(result);
        } catch (Exception e) {
            // This might happen in CI environments
            // Now test with mock
            FXMLLoader mockedResult = controller.createFXMLLoader("/fxml/TaskView.fxml");
            
            // Assert
            assertNotNull(mockedResult);
            assertEquals(mockLoader, mockedResult);
        }
    }
    
    @Test
    public void testHandleEditTaskWithNull() {
        // Act
        controller.handleEditTask(null);
        
        // Assert - verify nothing happens
        verify(mockLoader, never()).getController();
        verify(controller, never()).showAndWaitDialog(any());
        verify(mockLoadTasksCommand, never()).execute();
    }
    
    @Test
    public void testHandleEditTaskWithException() throws IOException {
        // Arrange
        when(mockLoader.load()).thenThrow(new IOException("Test exception"));
        
        // Act
        controller.handleEditTask(mockTask);
        
        // Assert
        verify(mockLoader).load();
        verify(controller).showErrorAlert(contains("Error"), contains("Failed to open"));
        verify(mockLoadTasksCommand, never()).execute();
    }
}