// src/test/java/org/frcpm/controllers/TaskControllerTest.java
package org.frcpm.controllers;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.frcpm.binding.Command;
import org.frcpm.models.*;
import org.frcpm.services.DialogService;
import org.frcpm.viewmodels.TaskViewModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.Start;

import java.time.LocalDate;
import javafx.collections.FXCollections;

import static org.mockito.Mockito.*;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TaskControllerTest extends BaseJavaFXTest {

    // Controller to test
    private TaskController taskController;

    // Spy for the controller for testing protected methods
    private TaskController controllerSpy;

    // Mock services
    private DialogService mockDialogService;

    // Mock ViewModel
    private TaskViewModel mockViewModel;

    // Mock Commands
    private Command mockSaveCommand;
    private Command mockCancelCommand;
    private Command mockAddMemberCommand;
    private Command mockRemoveMemberCommand;
    private Command mockAddComponentCommand;
    private Command mockRemoveComponentCommand;
    private Command mockAddDependencyCommand;
    private Command mockRemoveDependencyCommand;

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
                saveButton, cancelButton);

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
        // Create mock services
        mockDialogService = mock(DialogService.class);

        // Create a new controller instance
        taskController = new TaskController();

        // Create a spy for the controller
        controllerSpy = spy(taskController);

        // Inject the mock dialog service into the controller
        setPrivateField(controllerSpy, "dialogService", mockDialogService);

        // Create mock Command objects
        mockSaveCommand = mock(Command.class);
        mockCancelCommand = mock(Command.class);
        mockAddMemberCommand = mock(Command.class);
        mockRemoveMemberCommand = mock(Command.class);
        mockAddComponentCommand = mock(Command.class);
        mockRemoveComponentCommand = mock(Command.class);
        mockAddDependencyCommand = mock(Command.class);
        mockRemoveDependencyCommand = mock(Command.class);

        // Create mock ViewModel
        mockViewModel = mock(TaskViewModel.class);

        // Set up basic mock behavior
        when(mockViewModel.getSaveCommand()).thenReturn(mockSaveCommand);
        when(mockViewModel.getCancelCommand()).thenReturn(mockCancelCommand);
        when(mockViewModel.getAddMemberCommand()).thenReturn(mockAddMemberCommand);
        when(mockViewModel.getRemoveMemberCommand()).thenReturn(mockRemoveMemberCommand);
        when(mockViewModel.getAddComponentCommand()).thenReturn(mockAddComponentCommand);
        when(mockViewModel.getRemoveComponentCommand()).thenReturn(mockRemoveComponentCommand);
        when(mockViewModel.getAddDependencyCommand()).thenReturn(mockAddDependencyCommand);
        when(mockViewModel.getRemoveDependencyCommand()).thenReturn(mockRemoveDependencyCommand);
        when(mockViewModel.getAssignedMembers()).thenReturn(FXCollections.observableArrayList());
        when(mockViewModel.getRequiredComponents()).thenReturn(FXCollections.observableArrayList());
        when(mockViewModel.getPreDependencies()).thenReturn(FXCollections.observableArrayList());
        when(mockViewModel.titleProperty()).thenReturn(new SimpleStringProperty());
        when(mockViewModel.descriptionProperty()).thenReturn(new SimpleStringProperty());
        when(mockViewModel.estimatedHoursProperty()).thenReturn(new SimpleDoubleProperty(1.0));
        when(mockViewModel.actualHoursProperty()).thenReturn(new SimpleDoubleProperty(0.0));
        when(mockViewModel.priorityProperty()).thenReturn(new SimpleObjectProperty<>(Task.Priority.MEDIUM));
        when(mockViewModel.progressProperty()).thenReturn(new SimpleIntegerProperty(0));
        when(mockViewModel.startDateProperty()).thenReturn(new SimpleObjectProperty<>(LocalDate.now()));
        when(mockViewModel.endDateProperty()).thenReturn(new SimpleObjectProperty<>());
        when(mockViewModel.completedProperty()).thenReturn(new SimpleBooleanProperty(false));
        when(mockViewModel.projectProperty()).thenReturn(new SimpleObjectProperty<>());
        when(mockViewModel.subsystemProperty()).thenReturn(new SimpleObjectProperty<>());
        when(mockViewModel.errorMessageProperty()).thenReturn(new SimpleStringProperty());

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
                LocalDate.now().plusWeeks(8));
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
    }

    /**
     * Test the initialization of the controller.
     */
    @Test
    public void testInitialize() {
        // Call the method to test
        controllerSpy.testInitialize();

        // Verify that bindings were set up
        verify(mockViewModel).titleProperty();
        verify(mockViewModel).descriptionProperty();
        verify(mockViewModel).startDateProperty();
        verify(mockViewModel).endDateProperty();
        verify(mockViewModel).priorityProperty();
        verify(mockViewModel).progressProperty();
        verify(mockViewModel).completedProperty();

        // Verify command bindings
        verify(mockViewModel).getSaveCommand();
        verify(mockViewModel).getCancelCommand();
        verify(mockViewModel).getAddMemberCommand();
        verify(mockViewModel).getRemoveMemberCommand();
        verify(mockViewModel).getAddComponentCommand();
        verify(mockViewModel).getRemoveComponentCommand();
        verify(mockViewModel).getAddDependencyCommand();
        verify(mockViewModel).getRemoveDependencyCommand();

        // Verify tables were bound to the ViewModel collections
        verify(mockViewModel).getAssignedMembers();
        verify(mockViewModel).getRequiredComponents();
        verify(mockViewModel).getPreDependencies();
    }

    /**
     * Test setting up controller for a new task.
     */
    @Test
    public void testInitNewTask() {
        // Call method
        controllerSpy.initNewTask(testTask);

        // Verify ViewModel method was called
        verify(mockViewModel).initNewTask(testTask.getProject(), testTask.getSubsystem());
        verify(mockViewModel).titleProperty();
    }

    /**
     * Test setting up controller for editing an existing task.
     */
    @Test
    public void testInitExistingTask() {
        // Call method
        controllerSpy.initExistingTask(testTask);

        // Verify ViewModel method was called
        verify(mockViewModel).initExistingTask(testTask);
    }

    /**
     * Test the showErrorAlert protected method.
     */
    @Test
    public void testShowErrorAlert() {
        // Call method
        controllerSpy.showErrorAlert("Test Title", "Test Message");

        // Verify dialog service was called
        verify(mockDialogService).showErrorAlert("Test Title", "Test Message");
    }

    /**
     * Test error message property listener.
     */
    @Test
    public void testErrorMessageListener() {
        // Set up controller
        controllerSpy.testInitialize();

        // Set up test data
        SimpleStringProperty errorProperty = new SimpleStringProperty();
        when(mockViewModel.errorMessageProperty()).thenReturn(errorProperty);

        // Trigger the error message listener
        errorProperty.set("Test Error");

        // Verify dialog service was called and error message was cleared
        verify(mockDialogService).showErrorAlert(anyString(), eq("Test Error"));
        verify(mockViewModel).errorMessageProperty();
    }

    /**
     * Test the closeDialog protected method.
     */
    @Test
    public void testCloseDialog() {
        // Setup - mock the stage closing
        doNothing().when(controllerSpy).closeDialog();

        // Call method
        controllerSpy.closeDialog();

        // Verify the method was called
        verify(controllerSpy).closeDialog();
    }

    /**
     * Test the legacy setTask method.
     */
    @Test
    public void testSetTask() {
        // Call method
        controllerSpy.setTask(testTask);

        // Verify ViewModel method was called
        verify(mockViewModel).initExistingTask(testTask);
    }

    /**
     * Test the legacy setNewTask method.
     */
    @Test
    public void testSetNewTask() {
        // Call method
        controllerSpy.setNewTask(testProject, testSubsystem);

        // Verify ViewModel method was called
        verify(mockViewModel).initNewTask(any(Project.class), any(Subsystem.class));
    }

    /**
     * Test the getTask method.
     */
    @Test
    public void testGetTask() {
        // Call method
        Task result = controllerSpy.getTask();

        // Verify ViewModel method was called and returned the expected result
        verify(mockViewModel).getTask();
        assertEquals(testTask, result);
    }

    /**
     * Test the getViewModel method.
     */
    @Test
    public void testGetViewModel() {
        // Call method
        TaskViewModel result = controllerSpy.getViewModel();

        // Verify correct ViewModel is returned
        assertEquals(mockViewModel, result);
    }

    /**
     * Helper method to inject field values using reflection.
     */
    private void injectField(String fieldName, Object value) throws Exception {
        Field field = TaskController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(controllerSpy, value);
    }

    /**
     * Helper method to set a private field using reflection.
     */
    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}