package org.frcpm.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import org.frcpm.models.*;
import org.frcpm.services.ComponentService;
import org.frcpm.services.TaskService;
import org.frcpm.services.TeamMemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TaskControllerTest {

    @Mock
    private TaskService taskService;
    
    @Mock
    private TeamMemberService teamMemberService;
    
    @Mock
    private ComponentService componentService;
    
    @InjectMocks
    private TaskController taskController;
    
    @Mock
    private Label taskTitleLabel;
    
    @Mock
    private Label projectLabel;
    
    @Mock
    private Label subsystemLabel;
    
    @Mock
    private TextArea descriptionArea;
    
    @Mock
    private DatePicker startDatePicker;
    
    @Mock
    private DatePicker endDatePicker;
    
    @Mock
    private ComboBox<Task.Priority> priorityComboBox;
    
    @Mock
    private Slider progressSlider;
    
    @Mock
    private Label progressLabel;
    
    @Mock
    private CheckBox completedCheckBox;
    
    @Mock
    private TextField estimatedHoursField;
    
    @Mock
    private TextField actualHoursField;
    
    @Mock
    private TableView<TeamMember> assignedMembersTable;
    
    @Mock
    private TableColumn<TeamMember, String> memberNameColumn;
    
    @Mock
    private TableColumn<TeamMember, String> memberSubteamColumn;
    
    @Mock
    private TableView<Component> requiredComponentsTable;
    
    @Mock
    private TableColumn<Component, String> componentNameColumn;
    
    @Mock
    private TableColumn<Component, String> componentPartNumberColumn;
    
    @Mock
    private TableColumn<Component, Boolean> componentDeliveredColumn;
    
    @Mock
    private TableView<Task> dependenciesTable;
    
    @Mock
    private TableColumn<Task, String> dependencyTitleColumn;
    
    @Mock
    private TableColumn<Task, Integer> dependencyProgressColumn;
    
    @Mock
    private Button saveButton;
    
    @Mock
    private Button cancelButton;
    
    @Mock
    private Button addMemberButton;
    
    @Mock
    private Button removeMemberButton;
    
    @Mock
    private Button addComponentButton;
    
    @Mock
    private Button removeComponentButton;
    
    @Mock
    private Button addDependencyButton;
    
    @Mock
    private Button removeDependencyButton;
    
    @Mock
    private Stage mockStage;
    
    @Mock
    private ActionEvent mockEvent;
    
    private Project testProject;
    private Subsystem testSubsystem;
    private Task testTask;
    private List<TeamMember> testMembers;
    private List<Component> testComponents;
    private List<Task> testDependencies;
    private ObservableList<TeamMember> assignedMembers;
    private ObservableList<Component> requiredComponents;
    private ObservableList<Task> dependencies;

    @BeforeEach
    public void setUp() {
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
        
        testMembers = Arrays.asList(member1, member2);
        
        // Create test components
        Component component1 = new Component("Motor", "MOTOR123");
        component1.setId(1L);
        
        Component component2 = new Component("Sensor", "SENSOR456");
        component2.setId(2L);
        
        testComponents = Arrays.asList(component1, component2);
        
        // Create test dependencies
        Task dependencyTask = new Task("Dependency Task", testProject, testSubsystem);
        dependencyTask.setId(2L);
        
        testDependencies = Arrays.asList(dependencyTask);
        
        // Set up observable lists
        assignedMembers = FXCollections.observableArrayList();
        requiredComponents = FXCollections.observableArrayList();
        dependencies = FXCollections.observableArrayList();
        
        // Add test members to the task
        Set<TeamMember> memberSet = new HashSet<>(testMembers.subList(0, 1)); // Just the first member
        testTask.setAssignedTo(memberSet);
        assignedMembers.addAll(memberSet);
        
        // Add test components to the task
        Set<Component> componentSet = new HashSet<>(testComponents.subList(0, 1)); // Just the first component
        testTask.setRequiredComponents(componentSet);
        requiredComponents.addAll(componentSet);
        
        // Add test dependencies to the task
        Set<Task> dependencySet = new HashSet<>(testDependencies);
        testTask.setPreDependencies(dependencySet);
        dependencies.addAll(dependencySet);
        
        // Initialize controller by setting the mock fields
        when(taskController.getTaskTitleLabel()).thenReturn(taskTitleLabel);
        when(taskController.getProjectLabel()).thenReturn(projectLabel);
        when(taskController.getSubsystemLabel()).thenReturn(subsystemLabel);
        when(taskController.getDescriptionArea()).thenReturn(descriptionArea);
        when(taskController.getStartDatePicker()).thenReturn(startDatePicker);
        when(taskController.getEndDatePicker()).thenReturn(endDatePicker);
        when(taskController.getPriorityComboBox()).thenReturn(priorityComboBox);
        when(taskController.getProgressSlider()).thenReturn(progressSlider);
        when(taskController.getProgressLabel()).thenReturn(progressLabel);
        when(taskController.getCompletedCheckBox()).thenReturn(completedCheckBox);
        when(taskController.getEstimatedHoursField()).thenReturn(estimatedHoursField);
        when(taskController.getActualHoursField()).thenReturn(actualHoursField);
        when(taskController.getAssignedMembersTable()).thenReturn(assignedMembersTable);
        when(taskController.getMemberNameColumn()).thenReturn(memberNameColumn);
        when(taskController.getMemberSubteamColumn()).thenReturn(memberSubteamColumn);
        when(taskController.getRequiredComponentsTable()).thenReturn(requiredComponentsTable);
        when(taskController.getComponentNameColumn()).thenReturn(componentNameColumn);
        when(taskController.getComponentPartNumberColumn()).thenReturn(componentPartNumberColumn);
        when(taskController.getComponentDeliveredColumn()).thenReturn(componentDeliveredColumn);
        when(taskController.getDependenciesTable()).thenReturn(dependenciesTable);
        when(taskController.getDependencyTitleColumn()).thenReturn(dependencyTitleColumn);
        when(taskController.getDependencyProgressColumn()).thenReturn(dependencyProgressColumn);
        when(taskController.getSaveButton()).thenReturn(saveButton);
        when(taskController.getCancelButton()).thenReturn(cancelButton);
        when(taskController.getAddMemberButton()).thenReturn(addMemberButton);
        when(taskController.getRemoveMemberButton()).thenReturn(removeMemberButton);
        when(taskController.getAddComponentButton()).thenReturn(addComponentButton);
        when(taskController.getRemoveComponentButton()).thenReturn(removeComponentButton);
        when(taskController.getAddDependencyButton()).thenReturn(addDependencyButton);
        when(taskController.getRemoveDependencyButton()).thenReturn(removeDependencyButton);
        
        // Set up observable lists using reflection
        try {
            java.lang.reflect.Field assignedMembersField = TaskController.class.getDeclaredField("assignedMembers");
            assignedMembersField.setAccessible(true);
            assignedMembersField.set(taskController, assignedMembers);
            
            java.lang.reflect.Field requiredComponentsField = TaskController.class.getDeclaredField("requiredComponents");
            requiredComponentsField.setAccessible(true);
            requiredComponentsField.set(taskController, requiredComponents);
            
            java.lang.reflect.Field dependenciesField = TaskController.class.getDeclaredField("dependencies");
            dependenciesField.setAccessible(true);
            dependenciesField.set(taskController, dependencies);
        } catch (Exception e) {
            fail("Failed to set fields via reflection: " + e.getMessage());
        }
        
        // Mock service behavior
        when(taskService.createTask(anyString(), any(), any(), anyDouble(), any(), any(), any())).thenReturn(testTask);
        when(taskService.save(any())).thenReturn(testTask);
        when(taskService.updateTaskProgress(anyLong(), anyInt(), anyBoolean())).thenReturn(testTask);
        when(taskService.assignMembers(anyLong(), any())).thenReturn(testTask);
        when(taskService.findByProject(testProject)).thenReturn(Arrays.asList(testTask, testDependencies.get(0)));
        when(taskService.addDependency(anyLong(), anyLong())).thenReturn(true);
        when(taskService.removeDependency(anyLong(), anyLong())).thenReturn(true);
        
        when(teamMemberService.findAll()).thenReturn(testMembers);
        when(componentService.findAll()).thenReturn(testComponents);
        
        // Mock UI component behavior
        when(startDatePicker.getValue()).thenReturn(testTask.getStartDate());
        when(endDatePicker.getValue()).thenReturn(testTask.getEndDate());
        when(priorityComboBox.getValue()).thenReturn(testTask.getPriority());
        when(progressSlider.getValue()).thenReturn((double) testTask.getProgress());
        when(completedCheckBox.isSelected()).thenReturn(testTask.isCompleted());
        when(estimatedHoursField.getText()).thenReturn("8.0");
        when(actualHoursField.getText()).thenReturn("");
        when(taskTitleLabel.getText()).thenReturn(testTask.getTitle());
        when(descriptionArea.getText()).thenReturn(testTask.getDescription());
        
        when(assignedMembersTable.getItems()).thenReturn(assignedMembers);
        when(requiredComponentsTable.getItems()).thenReturn(requiredComponents);
        when(dependenciesTable.getItems()).thenReturn(dependencies);
        
        when(saveButton.getScene()).thenReturn(mock(javafx.scene.Scene.class));
        when(saveButton.getScene().getWindow()).thenReturn(mockStage);
        
        when(assignedMembersTable.getSelectionModel()).thenReturn(mock(TableView.TableViewSelectionModel.class));
        when(requiredComponentsTable.getSelectionModel()).thenReturn(mock(TableView.TableViewSelectionModel.class));
        when(dependenciesTable.getSelectionModel()).thenReturn(mock(TableView.TableViewSelectionModel.class));
    }

    @Test
    public void testInitialize() {
        // Call initialize via reflection (since it's private)
        try {
            // Using test access method
            taskController.testInitialize();
            
            // Verify that progress slider is set up
            verify(priorityComboBox).setItems(any());
            
            // Verify that table columns are set up
            verify(memberNameColumn).setCellValueFactory(any());
            verify(memberSubteamColumn).setCellValueFactory(any());
            verify(componentNameColumn).setCellValueFactory(any());
            verify(componentPartNumberColumn).setCellValueFactory(any());
            verify(componentDeliveredColumn).setCellValueFactory(any());
            verify(dependencyTitleColumn).setCellValueFactory(any());
            verify(dependencyProgressColumn).setCellValueFactory(any());
            
            // Verify that button actions are set
            verify(saveButton).setOnAction(any());
            verify(cancelButton).setOnAction(any());
            verify(addMemberButton).setOnAction(any());
            verify(removeMemberButton).setOnAction(any());
            verify(addComponentButton).setOnAction(any());
            verify(removeComponentButton).setOnAction(any());
            verify(addDependencyButton).setOnAction(any());
            verify(removeDependencyButton).setOnAction(any());
            
        } catch (Exception e) {
            fail("Exception during initialize: " + e.getMessage());
        }
    }

    @Test
    public void testSetTask() {
        // Create a spy to capture values
        TaskController spyController = spy(taskController);

        // Test setting an existing task
        spyController.setTask(testTask);

        // Capture values
        boolean isNewTask = spyController.isNewTask;
        Task task = spyController.task;

        // Verify the task is set
        assertFalse(isNewTask);
        assertEquals(testTask, task);
        
        // Verify UI elements are updated
        verify(taskTitleLabel).setText(testTask.getTitle());
        verify(projectLabel).setText(testTask.getProject().getName());
        verify(subsystemLabel).setText(testTask.getSubsystem().getName());
        verify(descriptionArea).setText(testTask.getDescription());
        verify(startDatePicker).setValue(testTask.getStartDate());
        verify(endDatePicker).setValue(testTask.getEndDate());
        verify(priorityComboBox).setValue(testTask.getPriority());
        verify(progressSlider).setValue(testTask.getProgress());
        verify(progressLabel).setText(testTask.getProgress() + "%");
        verify(completedCheckBox).setSelected(testTask.isCompleted());
        verify(estimatedHoursField).setText(anyString());
    }
    
    @Test
    public void testSetNewTask() {
        // Test setting up for a new task
        taskController.setNewTask(testProject, testSubsystem);
        
        // Verify fields are set correctly
        assertTrue(taskController.isNewTask);
        assertNotNull(taskController.task);
        assertEquals(testProject, taskController.task.getProject());
        assertEquals(testSubsystem, taskController.task.getSubsystem());
        
        // Verify default values are set
        assertEquals(Task.Priority.MEDIUM, taskController.task.getPriority());
        assertNotNull(taskController.task.getStartDate());
        assertNotNull(taskController.task.getEndDate());
        assertNotNull(taskController.task.getEstimatedDuration());
    }
    
    @Test
    public void testLoadTaskData() {
        // Set up a task first
        taskController.setTask(testTask);
        
        // Call the method to test
        try {
            // Using test access method
            taskController.testLoadTaskData();
            
            // Verify UI elements are updated
            verify(taskTitleLabel, times(2)).setText(testTask.getTitle());
            verify(projectLabel, times(2)).setText(testTask.getProject().getName());
            verify(subsystemLabel, times(2)).setText(testTask.getSubsystem().getName());
            verify(descriptionArea, times(2)).setText(testTask.getDescription());
            
        } catch (Exception e) {
            fail("Exception during loadTaskData: " + e.getMessage());
        }
    }
    
    @Test
    public void testHandleSaveForNewTask() {
        // Set up for a new task
        taskController.setNewTask(testProject, testSubsystem);
        
        // Test saving
        taskController.handleSave(mockEvent);
        
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
        verify(mockStage).close();
    }
    
    @Test
    public void testHandleSaveForExistingTask() {
        // Set up for editing an existing task
        taskController.setTask(testTask);
        
        // Test saving
        taskController.handleSave(mockEvent);
        
        // Verify service was called to update the task
        verify(taskService).updateTaskProgress(
            testTask.getId(),
            testTask.getProgress(),
            testTask.isCompleted()
        );
        
        // Verify dialog was closed
        verify(mockStage).close();
    }
    
    @Test
    public void testHandleSaveWithValidationErrors() {
        // Set up for a new task with invalid values
        taskController.setNewTask(testProject, testSubsystem);
        
        // Mock empty title
        when(taskTitleLabel.getText()).thenReturn("");
        
        // Test saving
        taskController.handleSave(mockEvent);
        
        // Verify service was NOT called to create a new task
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
        verify(mockStage, never()).close();
    }
    
    @Test
    public void testHandleCancel() {
        // Test canceling
        taskController.handleCancel(mockEvent);
        
        // Verify dialog was closed
        verify(mockStage).close();
    }
    
    @Test
    public void testHandleAddMember() {
        // Mock dialog behavior
        ChoiceDialog<TeamMember> mockDialog = mock(ChoiceDialog.class);
        when(mockDialog.showAndWait()).thenReturn(java.util.Optional.of(testMembers.get(1)));
        
        // Create a special test controller that returns our mock dialog
        TaskController spyController = spy(taskController);
        doReturn(mockDialog).when(spyController).createMemberDialog(any());
        
        // Set up the task
        spyController.setTask(testTask);
        
        // Test adding a member
        spyController.handleAddMember(mockEvent);
        
        // Verify member is added
        assertEquals(2, assignedMembers.size());
        assertTrue(assignedMembers.contains(testMembers.get(1)));
    }
    
    @Test
    public void testHandleRemoveMember() {
        // Set up the task
        taskController.setTask(testTask);
        
        // Mock table selection
        when(assignedMembersTable.getSelectionModel().getSelectedItem()).thenReturn(testMembers.get(0));
        
        // Test removing a member
        taskController.handleRemoveMember(mockEvent);
        
        // Verify member is removed
        assertEquals(0, assignedMembers.size());
    }
    
    @Test
    public void testHandleAddComponent() {
        // Mock dialog behavior
        ChoiceDialog<Component> mockDialog = mock(ChoiceDialog.class);
        when(mockDialog.showAndWait()).thenReturn(java.util.Optional.of(testComponents.get(1)));
        
        // Create a special test controller that returns our mock dialog
        TaskController spyController = spy(taskController);
        doReturn(mockDialog).when(spyController).createComponentDialog(any());
        
        // Set up the task
        spyController.setTask(testTask);
        
        // Test adding a component
        spyController.handleAddComponent(mockEvent);
        
        // Verify component is added
        assertEquals(2, requiredComponents.size());
        assertTrue(requiredComponents.contains(testComponents.get(1)));
    }
    
    @Test
    public void testHandleRemoveComponent() {
        // Set up the task
        taskController.setTask(testTask);
        
        // Mock table selection
        when(requiredComponentsTable.getSelectionModel().getSelectedItem()).thenReturn(testComponents.get(0));
        
        // Test removing a component
        taskController.handleRemoveComponent(mockEvent);
        
        // Verify component is removed
        assertEquals(0, requiredComponents.size());
    }
    
    @Test
    public void testHandleAddDependency() {
        // Mock dialog behavior
        ChoiceDialog<Task> mockDialog = mock(ChoiceDialog.class);
        Task dependencyTask = testDependencies.get(0);
        when(mockDialog.showAndWait()).thenReturn(java.util.Optional.of(dependencyTask));
        
        // Create a special test controller that returns our mock dialog
        TaskController spyController = spy(taskController);
        doReturn(mockDialog).when(spyController).createDependencyDialog(any());
        
        // Set up the task
        Task newTask = new Task("New Task", testProject, testSubsystem);
        newTask.setId(3L);
        spyController.setTask(newTask);
        
        // Test adding a dependency
        dependencies.clear(); // Clear the list first
        spyController.handleAddDependency(mockEvent);
        
        // Verify dependency is added
        verify(taskService).addDependency(newTask.getId(), dependencyTask.getId());
    }
    
    @Test
    public void testHandleRemoveDependency() {
        // Set up the task
        taskController.setTask(testTask);
        
        // Mock table selection
        when(dependenciesTable.getSelectionModel().getSelectedItem()).thenReturn(testDependencies.get(0));
        
        // Test removing a dependency
        taskController.handleRemoveDependency(mockEvent);
        
        // Verify service is called
        verify(taskService).removeDependency(testTask.getId(), testDependencies.get(0).getId());
    }
    
    // Helper method to create a mock member dialog for testing
    private ChoiceDialog<TeamMember> createMemberDialog(List<TeamMember> availableMembers) {
        ChoiceDialog<TeamMember> dialog = mock(ChoiceDialog.class);
        when(dialog.showAndWait()).thenReturn(java.util.Optional.of(availableMembers.get(0)));
        return dialog;
    }
    
    // Helper method to create a mock component dialog for testing
    private ChoiceDialog<Component> createComponentDialog(List<Component> availableComponents) {
        ChoiceDialog<Component> dialog = mock(ChoiceDialog.class);
        when(dialog.showAndWait()).thenReturn(java.util.Optional.of(availableComponents.get(0)));
        return dialog;
    }
    
    // Helper method to create a mock dependency dialog for testing
    private ChoiceDialog<Task> createDependencyDialog(List<Task> availableTasks) {
        ChoiceDialog<Task> dialog = mock(ChoiceDialog.class);
        when(dialog.showAndWait()).thenReturn(java.util.Optional.of(availableTasks.get(0)));
        return dialog;
    }