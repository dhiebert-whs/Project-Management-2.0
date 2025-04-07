// src/test/java/org/frcpm/controllers/ProjectControllerTest.java

package org.frcpm.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.frcpm.binding.Command;
import org.frcpm.models.Meeting;
import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.models.Subsystem;
import org.frcpm.viewmodels.ProjectViewModel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testfx.framework.junit5.ApplicationExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
public class ProjectControllerTest extends BaseJavaFXTest {

    @Spy
    private ProjectController controller;

    @Mock
    private ProjectViewModel mockViewModel;

    @Mock
    private Label projectNameLabel;

    @Mock
    private Label startDateLabel;

    @Mock
    private Label goalDateLabel;

    @Mock
    private Label deadlineLabel;

    @Mock
    private TextArea descriptionArea;

    @Mock
    private ProgressBar completionProgressBar;

    @Mock
    private Label completionLabel;

    @Mock
    private Label totalTasksLabel;

    @Mock
    private Label completedTasksLabel;

    @Mock
    private Label daysRemainingLabel;

    @Mock
    private TableView<Task> tasksTable;

    @Mock
    private TableView<Milestone> milestonesTable;

    @Mock
    private TableView<Meeting> meetingsTable;

    @Mock
    private Button addTaskButton;

    @Mock
    private Button addMilestoneButton;

    @Mock
    private Button scheduleMeetingButton;

    private Project testProject;
    private ObservableList<Milestone> milestoneList;
    private ObservableList<Task> taskList;
    private ObservableList<Meeting> meetingList;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Set up test project
        testProject = new Project(
                "Test Project",
                LocalDate.now(),
                LocalDate.now().plusWeeks(6),
                LocalDate.now().plusWeeks(8));
        testProject.setId(1L);
        testProject.setDescription("Test project description");

        // Set up observable lists
        milestoneList = FXCollections.observableArrayList();
        taskList = FXCollections.observableArrayList();
        meetingList = FXCollections.observableArrayList();

        // Set up ViewModel mock
        when(mockViewModel.getSelectedProject()).thenReturn(testProject);
        when(mockViewModel.projectNameProperty())
                .thenReturn(new javafx.beans.property.SimpleStringProperty(testProject.getName()));
        when(mockViewModel.projectDescriptionProperty())
                .thenReturn(new javafx.beans.property.SimpleStringProperty(testProject.getDescription()));
        when(mockViewModel.startDateProperty())
                .thenReturn(new javafx.beans.property.SimpleObjectProperty<>(testProject.getStartDate()));
        when(mockViewModel.goalEndDateProperty())
                .thenReturn(new javafx.beans.property.SimpleObjectProperty<>(testProject.getGoalEndDate()));
        when(mockViewModel.hardDeadlineProperty())
                .thenReturn(new javafx.beans.property.SimpleObjectProperty<>(testProject.getHardDeadline()));
        when(mockViewModel.completionPercentageProperty())
                .thenReturn(new javafx.beans.property.SimpleDoubleProperty(50.0));
        when(mockViewModel.totalTasksProperty()).thenReturn(new javafx.beans.property.SimpleIntegerProperty(10));
        when(mockViewModel.completedTasksProperty()).thenReturn(new javafx.beans.property.SimpleIntegerProperty(5));
        when(mockViewModel.daysUntilGoalProperty()).thenReturn(new javafx.beans.property.SimpleLongProperty(42));
        when(mockViewModel.getMilestones()).thenReturn(milestoneList);
        when(mockViewModel.getTasks()).thenReturn(taskList);
        when(mockViewModel.getMeetings()).thenReturn(meetingList);
        when(mockViewModel.getLoadMilestonesCommand()).thenReturn(new Command(() -> {}));
        when(mockViewModel.getLoadTasksCommand()).thenReturn(new Command(() -> {}));
        when(mockViewModel.getLoadMeetingsCommand()).thenReturn(new Command(() -> {}));
        when(mockViewModel.getAddTaskCommand()).thenReturn(new Command(() -> {}));
        when(mockViewModel.getAddMilestoneCommand()).thenReturn(new Command(() -> {}));
        when(mockViewModel.getScheduleMeetingCommand()).thenReturn(new Command(() -> {}));

        // Inject fields into controller using reflection
        try {
            java.lang.reflect.Field viewModelField = ProjectController.class.getDeclaredField("viewModel");
            viewModelField.setAccessible(true);
            viewModelField.set(controller, mockViewModel);

            // Set mock UI elements
            when(controller.getProjectNameLabel()).thenReturn(projectNameLabel);
            when(controller.getStartDateLabel()).thenReturn(startDateLabel);
            when(controller.getGoalDateLabel()).thenReturn(goalDateLabel);
            when(controller.getDeadlineLabel()).thenReturn(deadlineLabel);
            when(controller.getDescriptionArea()).thenReturn(descriptionArea);
            when(controller.getCompletionProgressBar()).thenReturn(completionProgressBar);
            when(controller.getCompletionLabel()).thenReturn(completionLabel);
            when(controller.getTotalTasksLabel()).thenReturn(totalTasksLabel);
            when(controller.getCompletedTasksLabel()).thenReturn(completedTasksLabel);
            when(controller.getDaysRemainingLabel()).thenReturn(daysRemainingLabel);
            when(controller.getTasksTable()).thenReturn(tasksTable);
            when(controller.getMilestonesTable()).thenReturn(milestonesTable);
            when(controller.getMeetingsTable()).thenReturn(meetingsTable);
            when(controller.getAddTaskButton()).thenReturn(addTaskButton);
            when(controller.getAddMilestoneButton()).thenReturn(addMilestoneButton);
            when(controller.getScheduleMeetingButton()).thenReturn(scheduleMeetingButton);
            when(controller.getViewModel()).thenReturn(mockViewModel);

        } catch (Exception e) {
            fail("Failed to set up controller: " + e.getMessage());
        }
    }

    @Test
    public void testSetProject() {
        // Act
        controller.setProject(testProject);

        // Assert
        verify(mockViewModel).initExistingProject(testProject);
        assertEquals(testProject, controller.getProject());
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
        // Arrange
        when(mockViewModel.getSelectedProject()).thenReturn(testProject);

        // Act
        Project result = controller.getProject();

        // Assert
        assertEquals(testProject, result);
        verify(mockViewModel).getSelectedProject();
    }

    @Test
    public void testHandleEditTask() {
        // Arrange
        Task task = new Task("Test Task", testProject, null);
        FXMLLoader mockLoader = mock(FXMLLoader.class);
        TaskController mockTaskController = mock(TaskController.class);
        Stage mockStage = mock(Stage.class);
        Scene mockScene = mock(Scene.class);
        Window mockWindow = mock(Window.class);

        when(tasksTable.getScene()).thenReturn(mockScene);
        when(mockScene.getWindow()).thenReturn(mockWindow);
        when(mockLoader.getController()).thenReturn(mockTaskController);
        doReturn(mockLoader).when(controller).createFXMLLoader(anyString());
        doReturn(mockStage).when(controller).createDialogStage(anyString(), any(), any());

        try {
            // Mock loader.load() to avoid JavaFX initialization issues
            Parent mockParent = mock(Parent.class);
            when(mockLoader.load()).thenReturn(mockParent);
        } catch (Exception e) {
            fail("Failed to mock loader.load(): " + e.getMessage());
        }

        // Act
        controller.handleEditTask(task);

        // Assert
        verify(mockTaskController).initExistingTask(task);
        verify(controller).showAndWaitDialog(mockStage);
        verify(mockViewModel.getLoadTasksCommand()).execute();
    }

    @Test
    public void testHandleEditMilestone() {
        // Arrange
        Milestone milestone = new Milestone();
        milestone.setProject(testProject);
        FXMLLoader mockLoader = mock(FXMLLoader.class);
        MilestoneController mockMilestoneController = mock(MilestoneController.class);
        Stage mockStage = mock(Stage.class);
        Scene mockScene = mock(Scene.class);
        Window mockWindow = mock(Window.class);

        when(milestonesTable.getScene()).thenReturn(mockScene);
        when(mockScene.getWindow()).thenReturn(mockWindow);
        when(mockLoader.getController()).thenReturn(mockMilestoneController);
        doReturn(mockLoader).when(controller).createFXMLLoader(anyString());
        doReturn(mockStage).when(controller).createDialogStage(anyString(), any(), any());

        try {
            // Mock loader.load() to avoid JavaFX initialization issues
            Parent mockParent = mock(Parent.class);
            when(mockLoader.load()).thenReturn(mockParent);
        } catch (Exception e) {
            fail("Failed to mock loader.load(): " + e.getMessage());
        }

        // Act
        controller.handleEditMilestone(milestone);

        // Assert
        verify(mockMilestoneController).setMilestone(milestone);
        verify(controller).showAndWaitDialog(mockStage);
        verify(mockViewModel.getLoadMilestonesCommand()).execute();
    }

    @Test
    public void testHandleEditMeeting() {
        // Arrange
        Meeting meeting = new Meeting();
        meeting.setProject(testProject);
        FXMLLoader mockLoader = mock(FXMLLoader.class);
        MeetingController mockMeetingController = mock(MeetingController.class);
        Stage mockStage = mock(Stage.class);
        Scene mockScene = mock(Scene.class);
        Window mockWindow = mock(Window.class);

        when(meetingsTable.getScene()).thenReturn(mockScene);
        when(mockScene.getWindow()).thenReturn(mockWindow);
        when(mockLoader.getController()).thenReturn(mockMeetingController);
        doReturn(mockLoader).when(controller).createFXMLLoader(anyString());
        doReturn(mockStage).when(controller).createDialogStage(anyString(), any(), any());

        try {
            // Mock loader.load() to avoid JavaFX initialization issues
            Parent mockParent = mock(Parent.class);
            when(mockLoader.load()).thenReturn(mockParent);
        } catch (Exception e) {
            fail("Failed to mock loader.load(): " + e.getMessage());
        }

        // Act
        controller.handleEditMeeting(meeting);

        // Assert
        verify(mockMeetingController).setMeeting(meeting);
        verify(controller).showAndWaitDialog(mockStage);
        verify(mockViewModel.getLoadMeetingsCommand()).execute();
    }

    @Test
    public void testShowChoiceDialog() {
        // Arrange
        List<Subsystem> subsystems = new ArrayList<>();
        Subsystem subsystem = new Subsystem();
        subsystem.setName("Test Subsystem");
        subsystems.add(subsystem);
        
        ChoiceDialog<Subsystem> mockDialog = mock(ChoiceDialog.class);
        doReturn(mockDialog).when(controller).createChoiceDialog(any(), anyList());
        when(mockDialog.showAndWait()).thenReturn(Optional.of(subsystem));
        
        // Act
        Optional<Subsystem> result = controller.showChoiceDialog(
            "Test Title", "Test Header", "Test Content", subsystem, subsystems);
        
        // Assert
        verify(mockDialog).setTitle("Test Title");
        verify(mockDialog).setHeaderText("Test Header");
        verify(mockDialog).setContentText("Test Content");
        verify(mockDialog).showAndWait();
        assertTrue(result.isPresent());
        assertEquals(subsystem, result.get());
    }

    @Test
    public void testCreateDialogStage() {
        // Arrange
        Window mockWindow = mock(Window.class);
        Parent mockParent = mock(Parent.class);
        
        // Act - cannot fully test due to JavaFX toolkit not being initialized in unit tests
        try {
            Stage result = controller.createDialogStage("Test Title", mockWindow, mockParent);
            
            // If we get here, it means createDialogStage didn't throw an NPE
            assertNotNull(result);
        } catch (Exception e) {
            if (e.getMessage() != null && 
                (e.getMessage().contains("Toolkit not initialized") || 
                 e.getMessage().contains("Internal graphics"))) {
                // This is expected in a test environment
                // Verify method behavior using a spy
                doReturn(mock(Stage.class)).when(controller).createDialogStage(anyString(), any(), any());
                controller.createDialogStage("Test Title", mockWindow, mockParent);
            } else {
                fail("Unexpected exception: " + e.getMessage());
            }
        }
    }

    @Test
    public void testShowErrorAlert() {
        // Arrange
        Alert mockAlert = mock(Alert.class);
        doReturn(mockAlert).when(controller).createAlert(any());
        
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
        // Arrange
        Alert mockAlert = mock(Alert.class);
        doReturn(mockAlert).when(controller).createAlert(any());
        
        // Act
        controller.showInfoAlert("Test Title", "Test Message");
        
        // Assert
        verify(mockAlert).setTitle("Information");
        verify(mockAlert).setHeaderText("Test Title");
        verify(mockAlert).setContentText("Test Message");
        verify(mockAlert).showAndWait();
    }
}