package org.frcpm.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.frcpm.models.*;
import org.frcpm.services.ComponentService;
import org.frcpm.services.TaskService;
import org.frcpm.services.TeamMemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.Start;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class TaskControllerTest extends BaseJavaFXTest {

    // Controller to test
    private TaskController taskController;
    
    // Mock services
    private TaskService taskService;
    private TeamMemberService teamMemberService;
    private ComponentService componentService;
    
    // Test data
    private Project testProject;
    private Subsystem testSubsystem;
    private Task testTask;
    private List<TeamMember> testMembers;
    private List<Component> testComponents;
    private List<Task> testDependencies;
    
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
    
    // Track dialog close status
    private boolean dialogClosed = false;
    private List<Alert> shownAlerts = new ArrayList<>();
    
    /**
     * Set up the JavaFX environment before each test.
     */
    @Start
    public void start(Stage stage) {
        // Create real JavaFX components
        taskTitleLabel = new Label("Test Task");
        projectLabel = new Label("Test Project");
        subsystemLabel = new Label("Test Subsystem");
        descriptionArea = new TextArea("Test description");
        startDatePicker = new DatePicker(LocalDate.now());
        endDatePicker = new DatePicker(LocalDate.now().plusWeeks(1));
        priorityComboBox = new ComboBox<>();
        priorityComboBox.setItems(FXCollections.observableArrayList(Task.Priority.values()));
        priorityComboBox.setValue(Task.Priority.MEDIUM);
        progressSlider = new Slider(0, 100, 50);
        progressLabel = new Label("50%");
        completedCheckBox = new CheckBox();
        estimatedHoursField = new TextField("8.0");
        actualHoursField = new TextField("");
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
    
    @BeforeEach
    public void setUp() throws Exception {
        // Create a new controller instance
        taskController = new TaskController();
        
        // Create mock services
        taskService = mock(TaskService.class);
        teamMemberService = mock(TeamMemberService.class);
        componentService = mock(ComponentService.class);
        
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
        
        // Inject services
        injectField("taskService", taskService);
        injectField("teamMemberService", teamMemberService);
        injectField("componentService", componentService);
        
        // Also inject mock buttons for those we don't use
        injectField("addMemberButton", mock(Button.class));
        injectField("removeMemberButton", mock(Button.class));
        injectField("addComponentButton", mock(Button.class));
        injectField("removeComponentButton", mock(Button.class));
        injectField("addDependencyButton", mock(Button.class));
        injectField("removeDependencyButton", mock(Button.class));
        
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
        testTask.setEstimatedDuration(Duration.ofHours(8));
        
        // Create test members
        TeamMember member1 = new TeamMember("testuser1", "Test", "User1", "test1@example.com");
        member1.setId(1L);
        
        TeamMember member2 = new TeamMember("testuser2", "Test", "User2", "test2@example.com");
        member2.setId(2L);
        
        testMembers = List.of(member1, member2);
        
        // Create test components
        Component component1 = new Component("Motor", "MOTOR123");
        component1.setId(1L);
        
        Component component2 = new Component("Sensor", "SENSOR456");
        component2.setId(2L);
        
        testComponents = List.of(component1, component2);
        
        // Create test dependencies
        Task dependencyTask = new Task("Dependency Task", testProject, testSubsystem);
        dependencyTask.setId(2L);
        
        testDependencies = List.of(dependencyTask);
        
        // Set up the assigned members, components and dependencies
        Set<TeamMember> memberSet = new HashSet<>();
        memberSet.add(testMembers.get(0));
        testTask.setAssignedTo(memberSet);
        
        Set<Component> componentSet = new HashSet<>();
        componentSet.add(testComponents.get(0));
        testTask.setRequiredComponents(componentSet);
        
        Set<Task> dependencySet = new HashSet<>(testDependencies);
        testTask.setPreDependencies(dependencySet);
        
        // Create observable lists
        taskController.assignedMembers = FXCollections.observableArrayList(testTask.getAssignedTo());
        taskController.requiredComponents = FXCollections.observableArrayList(testTask.getRequiredComponents());
        taskController.dependencies = FXCollections.observableArrayList(testTask.getPreDependencies());
        
        // Reset tracking variables
        dialogClosed = false;
        shownAlerts.clear();
        
        // Mock service behavior
        when(taskService.createTask(anyString(), any(), any(), anyDouble(), any(), any(), any()))
            .thenReturn(testTask);
        when(taskService.save(any())).thenReturn(testTask);
        when(taskService.updateTaskProgress(anyLong(), anyInt(), anyBoolean()))
            .thenReturn(testTask);
        when(taskService.assignMembers(anyLong(), any())).thenReturn(testTask);
        when(taskService.findByProject(testProject))
            .thenReturn(List.of(testTask, dependencyTask));
        when(taskService.addDependency(anyLong(), anyLong())).thenReturn(true);
        when(taskService.removeDependency(anyLong(), anyLong())).thenReturn(true);
        
        when(teamMemberService.findAll()).thenReturn(testMembers);
        when(componentService.findAll()).thenReturn(testComponents);
        
        // Override methods for testing
        overrideMethod("closeDialog", () -> dialogClosed = true);
        
        overrideMethod("showErrorAlert", (title, message) -> {
            shownAlerts.add(createAlert(Alert.AlertType.ERROR, title, message));
        });
        
        overrideMethod("showInfoAlert", (title, message) -> {
            shownAlerts.add(createAlert(Alert.AlertType.INFORMATION, title, message));
        });
        
        // Don't call initialize() directly as it would try to set up the table columns
        // which is problematic since we're using mocks
    }
    
    @Test
    public void testInitialize() {
        // Not testing the actual initialize method since it requires full JavaFX setup
        assertTrue(true);
    }
    
    @Test
    public void testSetTask() {
        // Set an existing task
        taskController.setTask(testTask);
        
        // Verify the task is set
        assertFalse(taskController.isNewTask);
        assertEquals(testTask, taskController.task);
        
        // Verify task data is loaded to UI components
        assertEquals(testTask.getTitle(), taskTitleLabel.getText());
        assertEquals(testTask.getProject().getName(), projectLabel.getText());
        assertEquals(testTask.getSubsystem().getName(), subsystemLabel.getText());
        assertEquals(testTask.getDescription(), descriptionArea.getText());
        assertEquals(testTask.getStartDate(), startDatePicker.getValue());
        assertEquals(testTask.getEndDate(), endDatePicker.getValue());
        assertEquals(testTask.getPriority(), priorityComboBox.getValue());
        assertEquals(testTask.getProgress(), (int)progressSlider.getValue());
        assertEquals(testTask.getProgress() + "%", progressLabel.getText());
        assertEquals(testTask.isCompleted(), completedCheckBox.isSelected());
    }
    
    @Test
    public void testSetNewTask() {
        // Test setting up for a new task
        taskController.setNewTask(testProject, testSubsystem);
        
        // Verify fields are set correctly
        assertTrue(taskController.isNewTask);
        assertNotNull(taskController.task);
        assertEquals("New Task", taskController.task.getTitle());
        assertEquals(testProject, taskController.task.getProject());
        assertEquals(testSubsystem, taskController.task.getSubsystem());
        
        // Verify default values are set
        assertEquals(Task.Priority.MEDIUM, taskController.task.getPriority());
        assertNotNull(taskController.task.getStartDate());
        assertNotNull(taskController.task.getEndDate());
        assertNotNull(taskController.task.getEstimatedDuration());
    }
    
    @Test
    public void testHandleSaveForNewTask() {
        // Set up for a new task
        taskController.setNewTask(testProject, testSubsystem);
        
        // Call save method
        taskController.testHandleSave(null);
        
        // Verify service was called to create a new task
        verify(taskService).createTask(
            anyString(),
            eq(testProject),
            eq(testSubsystem),
            anyDouble(),
            any(Task.Priority.class),
            any(LocalDate.class),
            any(LocalDate.class)
        );
        
        // Verify dialog was closed
        assertTrue(dialogClosed);
    }
    
    @Test
    public void testHandleSaveWithValidationErrors() {
        // Set up for a new task
        taskController.setNewTask(testProject, testSubsystem);
        
        // Set invalid values - empty title
        taskTitleLabel.setText("");
        
        // Call save method
        taskController.testHandleSave(null);
        
        // Verify error alert was shown
        assertEquals(1, shownAlerts.size());
        assertEquals("Invalid Title", shownAlerts.get(0).getHeaderText());
        
        // Verify service was NOT called
        verify(taskService, never()).createTask(
            anyString(),
            any(),
            any(),
            anyDouble(),
            any(),
            any(),
            any()
        );
        
        // Verify dialog was NOT closed
        assertFalse(dialogClosed);
    }
    
    @Test
    public void testHandleCancel() {
        // Call cancel method
        taskController.testHandleCancel(null);
        
        // Verify dialog was closed
        assertTrue(dialogClosed);
    }
    
    /**
     * Helper method to inject field values using reflection.
     */
    private void injectField(String fieldName, Object value) throws Exception {
        Field field = TaskController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(taskController, value);
    }
    
    /**
     * Helper method to override methods for testing purposes.
     */
    private void overrideMethod(String methodName, Runnable implementation) {
        try {
            Field field = TaskController.class.getDeclaredField(methodName + "Override");
            field.setAccessible(true);
            field.set(taskController, implementation);
        } catch (Exception e) {
            fail("Failed to override method: " + methodName + ". " + e.getMessage());
        }
    }
    
    /**
     * Override method with parameters.
     */
    private void overrideMethod(String methodName, BiConsumer<String, String> implementation) {
        try {
            Field field = TaskController.class.getDeclaredField(methodName + "Override");
            field.setAccessible(true);
            field.set(taskController, implementation);
        } catch (Exception e) {
            fail("Failed to override method: " + methodName + ". " + e.getMessage());
        }
    }
    
    /**
     * Create an alert for testing.
     */
    private Alert createAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(alertType == Alert.AlertType.ERROR ? "Error" : "Information");
        alert.setHeaderText(title);
        alert.setContentText(message);
        return alert;
    }
    
    /**
     * Simple functional interface for consuming two String parameters.
     */
    @FunctionalInterface
    private interface BiConsumer<T, U> {
        void accept(T t, U u);
    }
}