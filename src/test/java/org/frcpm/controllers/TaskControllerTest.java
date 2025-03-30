package org.frcpm.controllers;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.frcpm.models.*;
import org.frcpm.viewmodels.TaskViewModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.Start;

import java.lang.reflect.Field;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class TaskControllerTest extends BaseJavaFXTest {

    // Controller to test
    private TaskController taskController;
    
    // Mock ViewModel
    private TaskViewModel mockViewModel;
    
    // UI components - real JavaFX components
    private Label taskTitleLabel;
    private Label projectLabel;
    private Label subsystemLabel;
    private TextArea descriptionArea;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private ComboBox<Task.Priority> priorityComboBox;
    private Slider progressSlider;
    private Label progressLabel;
    private CheckBox completedCheckBox;
    private TextField estimatedHoursField;
    private TextField actualHoursField;
    private Button saveButton;
    private Button cancelButton;
    
    // Test data
    private Project testProject;
    private Subsystem testSubsystem;
    private Task testTask;
    
    /**
     * Set up the JavaFX environment before each test.
     * This is invoked by TestFX before each test method.
     */
    @Start
    public void start(Stage stage) {
        // Create real JavaFX components
        taskTitleLabel = new Label();
        projectLabel = new Label();
        subsystemLabel = new Label();
        descriptionArea = new TextArea();
        startDatePicker = new DatePicker();
        endDatePicker = new DatePicker();
        priorityComboBox = new ComboBox<>();
        priorityComboBox.getItems().addAll(Task.Priority.values());
        priorityComboBox.setValue(Task.Priority.MEDIUM);
        progressSlider = new Slider(0, 100, 0);
        progressLabel = new Label("0%");
        completedCheckBox = new CheckBox();
        estimatedHoursField = new TextField();
        actualHoursField = new TextField();
        saveButton = new Button("Save");
        cancelButton = new Button("Cancel");
        
        // Create a layout to hold the components
        VBox root = new VBox(10);
        root.getChildren().addAll(
            taskTitleLabel, projectLabel, subsystemLabel,
            descriptionArea, startDatePicker, endDatePicker,
            priorityComboBox, progressSlider, progressLabel,
            completedCheckBox, estimatedHoursField, actualHoursField,
            saveButton, cancelButton
        );
        
        // Set up and show the stage
        Scene scene = new Scene(root, 400, 600);
        stage.setScene(scene);
        stage.show();
    }
    
    /**
     * Set up the test data and mock objects before each test.
     */
    @BeforeEach
    public void setUp() throws Exception {
        // Create a new controller instance
        taskController = new TaskController();
        
        // Create mock ViewModel
        mockViewModel = mock(TaskViewModel.class);
        
        // Inject components into controller using reflection
        injectField("taskTitleLabel", taskTitleLabel);
        injectField("projectLabel", projectLabel);
        injectField("subsystemLabel", subsystemLabel);
        injectField("descriptionArea", descriptionArea);
        injectField("startDatePicker", startDatePicker);
        injectField("endDatePicker", endDatePicker);
        injectField("priorityComboBox", priorityComboBox);
        injectField("progressSlider", progressSlider);
        injectField("progressLabel", progressLabel);
        injectField("completedCheckBox", completedCheckBox);
        injectField("estimatedHoursField", estimatedHoursField);
        injectField("actualHoursField", actualHoursField);
        injectField("saveButton", saveButton);
        injectField("cancelButton", cancelButton);
        
        // Create mock tables and columns since they're problematic in tests
        injectField("assignedMembersTable", mock(TableView.class));
        injectField("memberNameColumn", mock(TableColumn.class));
        injectField("memberSubteamColumn", mock(TableColumn.class));
        injectField("requiredComponentsTable", mock(TableView.class));
        injectField("componentNameColumn", mock(TableColumn.class));
        injectField("componentPartNumberColumn", mock(TableColumn.class));
        injectField("componentDeliveredColumn", mock(TableColumn.class));
        injectField("dependenciesTable", mock(TableView.class));
        injectField("dependencyTitleColumn", mock(TableColumn.class));
        injectField("dependencyProgressColumn", mock(TableColumn.class));
        
        // Also inject mock buttons for those we don't use
        injectField("addMemberButton", mock(Button.class));
        injectField("removeMemberButton", mock(Button.class));
        injectField("addComponentButton", mock(Button.class));
        injectField("removeComponentButton", mock(Button.class));
        injectField("addDependencyButton", mock(Button.class));
        injectField("removeDependencyButton", mock(Button.class));
        
        // Inject the mock ViewModel
        injectField("viewModel", mockViewModel);
        
        // Create test project
        testProject = new Project(
                "Test Project",
                LocalDate.now(),
                LocalDate.now().plusWeeks(6),
                LocalDate.now().plusWeeks(8)
        );
        testProject.setId(1L);
        
        // Create test subsystem
        testSubsystem = new Subsystem("Drivetrain");
        testSubsystem.setId(1L);
        
        // Create test task
        testTask = new Task("Test Task", testProject, testSubsystem);
        testTask.setId(1L);
        testTask.setDescription("Test task description");
        testTask.setStartDate(LocalDate.now());
        testTask.setEndDate(LocalDate.now().plusWeeks(1));
        testTask.setPriority(Task.Priority.MEDIUM);
        testTask.setProgress(50);
        testTask.setCompleted(false);
        
        // Set up mock ViewModel behavior
        when(mockViewModel.getTask()).thenReturn(testTask);
        when(mockViewModel.isValid()).thenReturn(true);
        
        // Initialize the controller
        taskController.testInitialize();
    }
    
    /**
     * Test the initialization of the controller.
     */
    @Test
    public void testInitialize() {
        // Verify that the buttons have actions set
        assertNotNull(saveButton.getOnAction());
        assertNotNull(cancelButton.getOnAction());
    }
    
    /**
     * Test setting up controller for a new task.
     */
    @Test
    public void testSetNewTask() {
        // Call method
        taskController.setNewTask(testProject, testSubsystem);
        
        // Verify ViewModel method was called
        verify(mockViewModel).initNewTask(testProject, testSubsystem);
    }
    
    /**
     * Test setting up controller for editing an existing task.
     */
    @Test
    public void testSetTask() {
        // Call method
        taskController.setTask(testTask);
        
        // Verify ViewModel method was called
        verify(mockViewModel).initExistingTask(testTask);
    }
    
    /**
     * Test getting the task from the ViewModel.
     */
    @Test
    public void testGetTask() {
        // Test
        Task result = taskController.getTask();
        
        // Verify
        assertEquals(testTask, result);
        verify(mockViewModel).getTask();
    }
    
    /**
     * Test the initNewTask method for creating a new task.
     */
    @Test
    public void testInitNewTask() {
        // Call method
        taskController.initNewTask(testTask);
        
        // Verify ViewModel method was called
        verify(mockViewModel).initNewTask(testTask.getProject(), testTask.getSubsystem());
        verify(mockViewModel).titleProperty();
    }
    
    /**
     * Test the initExistingTask method for editing an existing task.
     */
    @Test
    public void testInitExistingTask() {
        // Call method
        taskController.initExistingTask(testTask);
        
        // Verify ViewModel method was called
        verify(mockViewModel).initExistingTask(testTask);
    }
    
    /**
     * Helper method to inject field values using reflection.
     */
    private void injectField(String fieldName, Object value) throws Exception {
        Field field = TaskController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(taskController, value);
    }
}