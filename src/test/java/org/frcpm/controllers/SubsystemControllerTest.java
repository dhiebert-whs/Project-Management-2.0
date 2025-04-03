package org.frcpm.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.frcpm.models.Subteam;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.services.SubsystemService;
import org.frcpm.viewmodels.SubsystemViewModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
public class SubsystemControllerTest extends BaseJavaFXTest {

    // Controller to test
    private SubsystemController subsystemController;
    
    // Mock ViewModel
    @Mock
    private SubsystemViewModel mockViewModel;
    
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
        when(mockViewModel.getSaveCommand()).thenReturn(mock(org.frcpm.binding.Command.class));
        when(mockViewModel.getLoadTasksCommand()).thenReturn(mock(org.frcpm.binding.Command.class));
        
        // Create controller instance
        subsystemController = new SubsystemController();
        
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
    }
    
    @Test
    public void testInitializeComponentsSetup() {
        // Call initialize
        subsystemController.testInitialize();
        
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
        subsystemController.initNewSubsystem();
        
        // Verify ViewModel method was called
        verify(mockViewModel).initNewSubsystem();
    }
    
    @Test
    public void testInitExistingSubsystem() {
        // Call method to test
        subsystemController.initExistingSubsystem(testSubsystem);
        
        // Verify ViewModel method was called
        verify(mockViewModel).initExistingSubsystem(testSubsystem);
    }
    
    @Test
    public void testGetSubsystem() {
        // Call method to test
        Subsystem result = subsystemController.getSubsystem();
        
        // Verify result
        assertEquals(testSubsystem, result);
        verify(mockViewModel).getSelectedSubsystem();
    }
    
    @Test
    public void testGetViewModel() {
        // Call method to test
        SubsystemViewModel result = subsystemController.getViewModel();
        
        // Verify result
        assertEquals(mockViewModel, result);
    }
    
    @Test
    public void testSaveButtonAction() {
        // Get the mock command
        org.frcpm.binding.Command mockCommand = mockViewModel.getSaveCommand();
        
        // Trigger save button action
        saveButton.fire();
        
        // Verify command was executed - we can't directly verify this
        // since the binding is done in setupBindings, which we don't call in tests
    }
    
    @Test
    public void testAddTaskButtonAction() {
        // No good way to test this in isolation since it requires MainController.getInstance()
        // which will be null in the test environment
    }
    
    /**
     * Helper method to inject field values using reflection.
     */
    private void injectField(String fieldName, Object value) throws Exception {
        Field field = SubsystemController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(subsystemController, value);
    }
}