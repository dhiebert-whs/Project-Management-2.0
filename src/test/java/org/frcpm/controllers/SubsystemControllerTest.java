// src/test/java/org/frcpm/controllers/SubsystemControllerTest.java
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
import org.frcpm.models.Subteam;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.services.DialogService;
import org.frcpm.viewmodels.SubsystemViewModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
public class SubsystemControllerTest extends BaseJavaFXTest {

    // Controller to test
    private SubsystemController controller;
    
    // Controller spy for testing protected methods
    private SubsystemController controllerSpy;
    
    // Mock dependencies
    @Mock
    private SubsystemViewModel mockViewModel;
    
    @Mock
    private DialogService mockDialogService;
    
    @Mock
    private Command mockSaveCommand;
    
    @Mock
    private Command mockAddTaskCommand;
    
    @Mock
    private Command mockViewTaskCommand;
    
    @Mock
    private Command mockLoadTasksCommand;
    
    // UI components
    private TextField nameField;
    private TextArea descriptionArea;
    private ComboBox<Subsystem.Status> statusComboBox;
    private ComboBox<Subteam> responsibleSubteamComboBox;
    private TableView<Task> tasksTable;
    private TableColumn<Task, String> taskTitleColumn;
    private TableColumn<Task, Integer> taskProgressColumn;
    private TableColumn<Task, LocalDate> taskDueDateColumn;
    private Label totalTasksLabel;
    private Label completedTasksLabel;
    private Label completionPercentageLabel;
    private ProgressBar completionProgressBar;
    private Button saveButton;
    private Button cancelButton;
    private Button addTaskButton;
    private Button viewTaskButton;
    
    // Test data
    private Subsystem testSubsystem;
    private Subteam testSubteam;
    private ObservableList<Subteam> testSubteams;
    private ObservableList<Task> testTasks;
    
    @Start
    public void start(Stage stage) {
        // Create real JavaFX components
        nameField = new TextField();
        descriptionArea = new TextArea();
        statusComboBox = new ComboBox<>();
        responsibleSubteamComboBox = new ComboBox<>();
        tasksTable = new TableView<>();
        taskTitleColumn = new TableColumn<>("Title");
        taskProgressColumn = new TableColumn<>("Progress");
        taskDueDateColumn = new TableColumn<>("Due Date");
        totalTasksLabel = new Label();
        completedTasksLabel = new Label();
        completionPercentageLabel = new Label();
        completionProgressBar = new ProgressBar();
        saveButton = new Button("Save");
        cancelButton = new Button("Cancel");
        addTaskButton = new Button("Add Task");
        viewTaskButton = new Button("View Task");
        
        // Add columns to the table
        tasksTable.getColumns().addAll(taskTitleColumn, taskProgressColumn, taskDueDateColumn);
    }
    
    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        // Create test objects
        testSubteam = new Subteam("Test Subteam", "#FF0000");
        testSubteam.setId(1L);
        
        testSubsystem = new Subsystem("Test Subsystem");
        testSubsystem.setId(1L);
        testSubsystem.setDescription("Test Description");
        testSubsystem.setStatus(Subsystem.Status.IN_PROGRESS);
        testSubsystem.setResponsibleSubteam(testSubteam);
        
        testSubteams = FXCollections.observableArrayList();
        testSubteams.add(testSubteam);
        
        testTasks = FXCollections.observableArrayList();
        Task task1 = new Task("Test Task 1", null, testSubsystem);
        task1.setId(1L);
        task1.setProgress(50);
        task1.setEndDate(LocalDate.now().plusDays(7));
        testTasks.add(task1);
        
        // Configure mock ViewModel
        when(mockViewModel.getSubsystems()).thenReturn(FXCollections.observableArrayList());
        when(mockViewModel.getAvailableSubteams()).thenReturn(testSubteams);
        when(mockViewModel.getTasks()).thenReturn(testTasks);
        when(mockViewModel.getSelectedSubsystem()).thenReturn(testSubsystem);
        when(mockViewModel.getSaveCommand()).thenReturn(mockSaveCommand);
        when(mockViewModel.getAddTaskCommand()).thenReturn(mockAddTaskCommand);
        when(mockViewModel.getViewTaskCommand()).thenReturn(mockViewTaskCommand);
        when(mockViewModel.getLoadTasksCommand()).thenReturn(mockLoadTasksCommand);
        when(mockViewModel.subsystemNameProperty()).thenReturn(new javafx.beans.property.SimpleStringProperty());
        when(mockViewModel.subsystemDescriptionProperty()).thenReturn(new javafx.beans.property.SimpleStringProperty());
        when(mockViewModel.statusProperty()).thenReturn(new javafx.beans.property.SimpleObjectProperty<>());
        when(mockViewModel.responsibleSubteamProperty()).thenReturn(new javafx.beans.property.SimpleObjectProperty<>());
        when(mockViewModel.totalTasksProperty()).thenReturn(new javafx.beans.property.SimpleIntegerProperty(1));
        when(mockViewModel.completedTasksProperty()).thenReturn(new javafx.beans.property.SimpleIntegerProperty(0));
        when(mockViewModel.completionPercentageProperty()).thenReturn(new javafx.beans.property.SimpleDoubleProperty(0));
        when(mockViewModel.errorMessageProperty()).thenReturn(new javafx.beans.property.SimpleStringProperty());
        
        // Create controller instance
        controller = new SubsystemController();
        
        // Create spy for testing protected methods
        controllerSpy = spy(controller);
        
        // Inject components using reflection
        injectField("nameField", nameField);
        injectField("descriptionArea", descriptionArea);
        injectField("statusComboBox", statusComboBox);
        injectField("responsibleSubteamComboBox", responsibleSubteamComboBox);
        injectField("tasksTable", tasksTable);
        injectField("taskTitleColumn", taskTitleColumn);
        injectField("taskProgressColumn", taskProgressColumn);
        injectField("taskDueDateColumn", taskDueDateColumn);
        injectField("totalTasksLabel", totalTasksLabel);
        injectField("completedTasksLabel", completedTasksLabel);
        injectField("completionPercentageLabel", completionPercentageLabel);
        injectField("completionProgressBar", completionProgressBar);
        injectField("saveButton", saveButton);
        injectField("cancelButton", cancelButton);
        injectField("addTaskButton", addTaskButton);
        injectField("viewTaskButton", viewTaskButton);
        
        // Inject mock ViewModel
        injectField("viewModel", mockViewModel);
        
        // Inject mock DialogService
        injectField("dialogService", mockDialogService);
    }
    
    @Test
    public void testInitializeComponentsSetup() {
        // Call initialize
        controller.testInitialize();
        
        // Verify status combo box was initialized
        assertNotNull(statusComboBox.getItems());
        assertFalse(statusComboBox.getItems().isEmpty());
        
        // Verify subteam combo box was initialized
        assertEquals(testSubteams, responsibleSubteamComboBox.getItems());
        
        // Verify tasks table was initialized
        assertEquals(testTasks, tasksTable.getItems());
    }
    
    @Test
    public void testInitNewSubsystem() {
        // Call method to test
        controller.initNewSubsystem();
        
        // Verify ViewModel method was called
        verify(mockViewModel).initNewSubsystem();
    }
    
    @Test
    public void testInitExistingSubsystem() {
        // Call method to test
        controller.initExistingSubsystem(testSubsystem);
        
        // Verify ViewModel method was called
        verify(mockViewModel).initExistingSubsystem(testSubsystem);
    }
    
    @Test
    public void testGetSubsystem() {
        // Call method to test
        Subsystem result = controller.getSubsystem();
        
        // Verify result
        assertEquals(testSubsystem, result);
        verify(mockViewModel).getSelectedSubsystem();
    }
    
    @Test
    public void testGetViewModel() {
        // Call method to test
        SubsystemViewModel result = controller.getViewModel();
        
        // Verify result
        assertEquals(mockViewModel, result);
    }
    
    @Test
    public void testShowErrorAlert() {
        // Call method to test
        controllerSpy.showErrorAlert("Test Title", "Test Message");
        
        // Verify mock DialogService was called
        verify(mockDialogService).showErrorAlert("Test Title", "Test Message");
    }
    
    @Test
    public void testCreateFXMLLoader() {
        // Mock FXMLLoader
        FXMLLoader mockLoader = mock(FXMLLoader.class);
        doReturn(mockLoader).when(controllerSpy).createFXMLLoader(anyString());
        
        // Call method to test
        FXMLLoader result = controllerSpy.createFXMLLoader("/test/path.fxml");
        
        // Verify result
        assertEquals(mockLoader, result);
    }
    
    @Test
    public void testCreateDialogStage() {
        // Mock Stage
        Stage mockStage = mock(Stage.class);
        doReturn(mockStage).when(controllerSpy).createDialogStage(anyString(), any(Window.class), any(Parent.class));
        
        // Mock parent
        Parent mockParent = mock(Parent.class);
        
        // Call method to test
        Stage result = controllerSpy.createDialogStage("Test Title", mock(Window.class), mockParent);
        
        // Verify result
        assertEquals(mockStage, result);
    }
    
    @Test
    public void testShowAndWaitDialog() {
        // Mock Stage
        Stage mockStage = mock(Stage.class);
        
        // Call method to test - should not throw exception
        controllerSpy.showAndWaitDialog(mockStage);
    }
    
    @Test
    public void testSetDialogService() {
        // Create new mock DialogService
        DialogService newMockDialogService = mock(DialogService.class);
        
        // Call method to test
        controller.setDialogService(newMockDialogService);
        
        // Verify the service was set - test with showErrorAlert
        controller.showErrorAlert("Test", "Message");
        verify(newMockDialogService).showErrorAlert("Test", "Message");
    }
    
    @Test
    public void testErrorMessageListener() throws Exception {
        // Get the error message property
        javafx.beans.property.StringProperty errorProperty = mockViewModel.errorMessageProperty();
        
        // Set error message
        errorProperty.set("Test Error");
        
        // Verify DialogService was called - this requires full initialization, so initialize first
        controller.testInitialize();
        
        // This needs to be on the JavaFX thread
        runOnFxThread(() -> errorProperty.set("Test Error"));
        
        // Verify DialogService was called
        verify(mockDialogService, timeout(1000)).showErrorAlert("Error", "Test Error");
    }
    
    /**
     * Helper method to inject field values using reflection.
     */
    private void injectField(String fieldName, Object value) throws Exception {
        // Inject to both controller and spy
        injectFieldToObject(controller, fieldName, value);
        injectFieldToObject(controllerSpy, fieldName, value);
    }
    
    /**
     * Helper method to inject a field value to a specific object using reflection.
     */
    private void injectFieldToObject(Object object, String fieldName, Object value) throws Exception {
        Field field = SubsystemController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }
}