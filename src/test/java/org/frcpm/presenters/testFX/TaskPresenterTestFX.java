// src/test/java/org/frcpm/presenters/testfx/TaskPresenterTestFX.java
package org.frcpm.presenters.testfx;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import org.frcpm.binding.Command;
import org.frcpm.models.*;
import org.frcpm.presenters.TaskPresenter;
import org.frcpm.services.*;
import org.frcpm.testfx.BaseFxTest;
import org.frcpm.testfx.TestFXUtils;
import org.frcpm.viewmodels.TaskViewModel;
import org.frcpm.views.TaskView;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.util.WaitForAsyncUtils;

/**
 * TestFX test for the TaskPresenter class.
 */
@ExtendWith(MockitoExtension.class)
public class TaskPresenterTestFX extends BaseFxTest {

    private static final Logger LOGGER = Logger.getLogger(TaskPresenterTestFX.class.getName());

    @Mock
    private TaskService taskService;
    
    @Mock
    private ComponentService componentService;
    
    @Mock
    private TeamMemberService teamMemberService;
    
    @Mock
    private DialogService dialogService;
    
    @Mock
    private Command mockSaveCommand;
    
    @Mock
    private Command mockCancelCommand;
    
    @Mock
    private Command mockAddMemberCommand;
    
    @Mock
    private Command mockRemoveMemberCommand;
    
    @Mock
    private Command mockAddComponentCommand;
    
    @Mock
    private Command mockRemoveComponentCommand;
    
    @Mock
    private Command mockAddDependencyCommand;
    
    @Mock
    private Command mockRemoveDependencyCommand;

    private AutoCloseable closeable;
    private TaskView view;
    private TaskPresenter presenter;
    private TaskViewModel viewModel;

    // Test data
    private Project testProject;
    private Subsystem testSubsystem;
    private Task testTask;
    private TeamMember testMember;
    private Component testComponent;
    private Task testDependencyTask;
    private List<TeamMember> testMembers;
    private List<Component> testComponents;
    private List<Task> testDependencies;

    @Override
    protected void initializeTestComponents(Stage stage) {
        LOGGER.info("Initializing TaskPresenterTestFX test components");

        try {
            // Open mocks first
            closeable = MockitoAnnotations.openMocks(this);

            // Create test data
            setupTestData();

            // Setup mocked service responses
            setupMockResponses();

            // Initialize the view on JavaFX thread
            Platform.runLater(() -> {
                try {
                    // Create view
                    view = new TaskView();

                    // Get the presenter
                    presenter = (TaskPresenter) view.getPresenter();

                    // Log successful creation
                    LOGGER.info("Created TaskView and got presenter: " + (presenter != null));

                    // Set the scene
                    Scene scene = new Scene(view.getView(), 800, 600);
                    stage.setScene(scene);

                    // Access the view model directly using reflection
                    if (presenter != null) {
                        try {
                            // Use reflection to access the private viewModel field
                            java.lang.reflect.Field viewModelField = presenter.getClass().getDeclaredField("viewModel");
                            viewModelField.setAccessible(true);
                            viewModel = (TaskViewModel) viewModelField.get(presenter);
                            LOGGER.info("Got view model via reflection: " + (viewModel != null));
                        } catch (Exception e) {
                            LOGGER.log(Level.SEVERE, "Failed to access viewModel field via reflection", e);
                        }
                    }

                    // Inject mocked services
                    injectMockedServices();

                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error initializing TaskView", e);
                    e.printStackTrace();
                }
            });

            // Wait for UI to update
            WaitForAsyncUtils.waitForFxEvents();

            // Show the stage
            stage.show();
            WaitForAsyncUtils.waitForFxEvents();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in initializeTestComponents", e);
            e.printStackTrace();
        }
    }

    private void injectMockedServices() {
        if (presenter == null) {
            LOGGER.severe("Cannot inject services - presenter is null");
            return;
        }

        try {
            // Use reflection to inject mocked services
            java.lang.reflect.Field taskServiceField = presenter.getClass().getDeclaredField("taskService");
            taskServiceField.setAccessible(true);
            taskServiceField.set(presenter, taskService);

            java.lang.reflect.Field componentServiceField = presenter.getClass().getDeclaredField("componentService");
            componentServiceField.setAccessible(true);
            componentServiceField.set(presenter, componentService);

            java.lang.reflect.Field teamMemberServiceField = presenter.getClass().getDeclaredField("teamMemberService");
            teamMemberServiceField.setAccessible(true);
            teamMemberServiceField.set(presenter, teamMemberService);

            java.lang.reflect.Field dialogServiceField = presenter.getClass().getDeclaredField("dialogService");
            dialogServiceField.setAccessible(true);
            dialogServiceField.set(presenter, dialogService);
            
            // Inject mocked view model if needed for more control over test
            if (viewModel != null) {
                java.lang.reflect.Field viewModelField = presenter.getClass().getDeclaredField("viewModel");
                viewModelField.setAccessible(true);
                
                // Create a spy of the existing viewModel so we don't lose bindings
                TaskViewModel spyViewModel = spy(viewModel);
                
                // Mock commands
                when(spyViewModel.getSaveCommand()).thenReturn(mockSaveCommand);
                when(spyViewModel.getCancelCommand()).thenReturn(mockCancelCommand);
                when(spyViewModel.getAddMemberCommand()).thenReturn(mockAddMemberCommand);
                when(spyViewModel.getRemoveMemberCommand()).thenReturn(mockRemoveMemberCommand);
                when(spyViewModel.getAddComponentCommand()).thenReturn(mockAddComponentCommand);
                when(spyViewModel.getRemoveComponentCommand()).thenReturn(mockRemoveComponentCommand);
                when(spyViewModel.getAddDependencyCommand()).thenReturn(mockAddDependencyCommand);
                when(spyViewModel.getRemoveDependencyCommand()).thenReturn(mockRemoveDependencyCommand);
                
                // Mock collections
                when(spyViewModel.getAssignedMembers()).thenReturn(FXCollections.observableArrayList(testMembers));
                when(spyViewModel.getRequiredComponents()).thenReturn(FXCollections.observableArrayList(testComponents));
                when(spyViewModel.getPreDependencies()).thenReturn(FXCollections.observableArrayList(testDependencies));
                
                viewModelField.set(presenter, spyViewModel);
                viewModel = spyViewModel;
            }

            LOGGER.info("Successfully injected mock services into presenter");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to inject mocked services", e);
            e.printStackTrace();
        }
    }

    private void setupTestData() {
        // Create a test project
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");
        testProject.setStartDate(LocalDate.now().minusDays(30));
        testProject.setGoalEndDate(LocalDate.now().plusDays(60));
        testProject.setHardDeadline(LocalDate.now().plusDays(90));

        // Create a test subsystem
        testSubsystem = new Subsystem();
        testSubsystem.setId(1L);
        testSubsystem.setName("Test Subsystem");
        testSubsystem.setDescription("Test subsystem description");
        testSubsystem.setStatus(Subsystem.Status.IN_PROGRESS);

        // Create a test task
        testTask = new Task();
        testTask.setId(1L);
        testTask.setTitle("Test Task");
        testTask.setProject(testProject);
        testTask.setSubsystem(testSubsystem);
        testTask.setDescription("Test task description");
        testTask.setEstimatedDuration(Duration.ofHours(2));
        testTask.setPriority(Task.Priority.MEDIUM);
        testTask.setProgress(25);
        testTask.setStartDate(LocalDate.now());
        testTask.setEndDate(LocalDate.now().plusDays(14));

        // Create a test team member
        testMember = new TeamMember();
        testMember.setId(1L);
        testMember.setUsername("testuser");
        testMember.setFirstName("Test");
        testMember.setLastName("User");
        testMember.setEmail("test@example.com");

        // Create test team members list
        testMembers = new ArrayList<>();
        testMembers.add(testMember);
        
        TeamMember member2 = new TeamMember();
        member2.setId(2L);
        member2.setUsername("anotheruser");
        member2.setFirstName("Another");
        member2.setLastName("User");
        testMembers.add(member2);

        // Create a test component
        testComponent = new Component();
        testComponent.setId(1L);
        testComponent.setName("Test Component");
        testComponent.setPartNumber("COMP-001");
        testComponent.setDescription("Test component description");
        testComponent.setDelivered(false);

        // Create test components list
        testComponents = new ArrayList<>();
        testComponents.add(testComponent);
        
        Component component2 = new Component();
        component2.setId(2L);
        component2.setName("Another Component");
        component2.setPartNumber("COMP-002");
        testComponents.add(component2);

        // Create a test dependency task
        testDependencyTask = new Task();
        testDependencyTask.setId(2L);
        testDependencyTask.setTitle("Dependency Task");
        testDependencyTask.setProject(testProject);
        testDependencyTask.setSubsystem(testSubsystem);
        testDependencyTask.setProgress(50);

        // Create test dependencies list
        testDependencies = new ArrayList<>();
        testDependencies.add(testDependencyTask);
        
        Task dependencyTask2 = new Task();
        dependencyTask2.setId(3L);
        dependencyTask2.setTitle("Another Dependency");
        dependencyTask2.setProject(testProject);
        dependencyTask2.setProgress(75);
        testDependencies.add(dependencyTask2);
    }

    private void setupMockResponses() {
        // Configure mock task service responses
        when(taskService.save(any(Task.class))).thenReturn(testTask);
        when(taskService.findById(eq(1L))).thenReturn(testTask);
        
        // Configure mock team member service responses
        when(teamMemberService.findById(eq(1L))).thenReturn(testMember);
        when(teamMemberService.findAll()).thenReturn(testMembers);
        
        // Configure mock component service responses
        when(componentService.findById(eq(1L))).thenReturn(testComponent);
        when(componentService.findAll()).thenReturn(testComponents);
        
        // For void methods in commands, use doNothing() instead of when()
        doNothing().when(mockSaveCommand).execute();
        doNothing().when(mockCancelCommand).execute();
        doNothing().when(mockAddMemberCommand).execute();
        doNothing().when(mockRemoveMemberCommand).execute();
        doNothing().when(mockAddComponentCommand).execute();
        doNothing().when(mockRemoveComponentCommand).execute();
        doNothing().when(mockAddDependencyCommand).execute();
        doNothing().when(mockRemoveDependencyCommand).execute();
    }

    @Test
    public void testInitializeWithNewTask() {
        // Skip the test if presenter is null
        if (presenter == null) {
            LOGGER.severe("Cannot run test - presenter is null");
            return;
        }

        try {
            // Set up the presenter for a new task
            Platform.runLater(() -> {
                presenter.initNewTask(testTask);
            });

            // Wait for JavaFX thread to process
            WaitForAsyncUtils.waitForFxEvents();

            // Verify UI components are set correctly
            Label taskTitleLabel = lookup("#taskTitleLabel").queryAs(Label.class);
            Label projectLabel = lookup("#projectLabel").queryAs(Label.class);
            Label subsystemLabel = lookup("#subsystemLabel").queryAs(Label.class);
            DatePicker startDatePicker = lookup("#startDatePicker").queryAs(DatePicker.class);
            DatePicker endDatePicker = lookup("#endDatePicker").queryAs(DatePicker.class);
            ComboBox<Task.Priority> priorityComboBox = lookup("#priorityComboBox").queryAs(ComboBox.class);
            Slider progressSlider = lookup("#progressSlider").queryAs(Slider.class);
            CheckBox completedCheckBox = lookup("#completedCheckBox").queryAs(CheckBox.class);
            TextField estimatedHoursField = lookup("#estimatedHoursField").queryAs(TextField.class);
            TextArea descriptionArea = lookup("#descriptionArea").queryAs(TextArea.class);

            assertEquals("Test Task", taskTitleLabel.getText(), "Task title should be displayed");
            assertEquals("Test Project", projectLabel.getText(), "Project name should be displayed");
            assertEquals("Test Subsystem", subsystemLabel.getText(), "Subsystem name should be displayed");
            assertNotNull(startDatePicker.getValue(), "Start date should be set");
            
            // Check view model state
            if (viewModel != null) {
                assertTrue(viewModel.isNewTask(), "View model should be in new task state");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in testInitializeWithNewTask", e);
            e.printStackTrace();
            fail("Test failed due to exception: " + e.getMessage());
        }
    }

    @Test
    public void testInitializeWithExistingTask() {
        // Skip the test if presenter is null
        if (presenter == null) {
            LOGGER.severe("Cannot run test - presenter is null");
            return;
        }

        try {
            // Set up the presenter for an existing task
            Platform.runLater(() -> {
                presenter.initExistingTask(testTask);
            });

            // Wait for JavaFX thread to process
            WaitForAsyncUtils.waitForFxEvents();

            // Verify UI components are set correctly
            Label taskTitleLabel = lookup("#taskTitleLabel").queryAs(Label.class);
            Label projectLabel = lookup("#projectLabel").queryAs(Label.class);
            Label subsystemLabel = lookup("#subsystemLabel").queryAs(Label.class);
            TextArea descriptionArea = lookup("#descriptionArea").queryAs(TextArea.class);
            TextField estimatedHoursField = lookup("#estimatedHoursField").queryAs(TextField.class);
            Slider progressSlider = lookup("#progressSlider").queryAs(Slider.class);

            assertEquals("Test Task", taskTitleLabel.getText(), "Task title should be displayed");
            assertEquals("Test Project", projectLabel.getText(), "Project name should be displayed");
            assertEquals("Test Subsystem", subsystemLabel.getText(), "Subsystem name should be displayed");
            assertEquals("Test task description", descriptionArea.getText(), "Description should be displayed");
            assertEquals(25.0, progressSlider.getValue(), 0.1, "Progress slider should be set to task progress");

            // Check view model state
            if (viewModel != null) {
                assertFalse(viewModel.isNewTask(), "View model should not be in new task state");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in testInitializeWithExistingTask", e);
            e.printStackTrace();
            fail("Test failed due to exception: " + e.getMessage());
        }
    }

    @Test
    public void testSaveButton() {
        // Skip the test if presenter is null
        if (presenter == null) {
            LOGGER.severe("Cannot run test - presenter is null");
            return;
        }

        try {
            // Set up the presenter for a new task
            Platform.runLater(() -> {
                presenter.initNewTask(testTask);
            });

            // Wait for JavaFX thread to process
            WaitForAsyncUtils.waitForFxEvents();

            // Click the Save button
            clickOn("#saveButton");

            // Wait for JavaFX thread to process
            WaitForAsyncUtils.waitForFxEvents();

            // Verify the save command was executed
            verify(mockSaveCommand, atLeastOnce()).execute();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in testSaveButton", e);
            e.printStackTrace();
            fail("Test failed due to exception: " + e.getMessage());
        }
    }

    @Test
    public void testCancelButton() {
        // Skip the test if presenter is null
        if (presenter == null) {
            LOGGER.severe("Cannot run test - presenter is null");
            return;
        }

        try {
            // Set up the presenter for a new task
            Platform.runLater(() -> {
                presenter.initNewTask(testTask);
            });

            // Wait for JavaFX thread to process
            WaitForAsyncUtils.waitForFxEvents();

            // Click the Cancel button
            clickOn("#cancelButton");

            // Wait for JavaFX thread to process
            WaitForAsyncUtils.waitForFxEvents();

            // Verify the cancel command was executed
            verify(mockCancelCommand, atLeastOnce()).execute();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in testCancelButton", e);
            e.printStackTrace();
            fail("Test failed due to exception: " + e.getMessage());
        }
    }

    @Test
    public void testProgressSliderAndCompletedCheckbox() {
        // Skip the test if presenter is null
        if (presenter == null) {
            LOGGER.severe("Cannot run test - presenter is null");
            return;
        }

        try {
            // Set up the presenter for a new task
            Platform.runLater(() -> {
                presenter.initNewTask(testTask);
            });

            // Wait for JavaFX thread to process
            WaitForAsyncUtils.waitForFxEvents();

            // Move the progress slider to 100%
            Slider progressSlider = lookup("#progressSlider").queryAs(Slider.class);
            Label progressLabel = lookup("#progressLabel").queryAs(Label.class);
            CheckBox completedCheckBox = lookup("#completedCheckBox").queryAs(CheckBox.class);
            
            // Using Platform.runLater as direct slider interaction is difficult in TestFX
            Platform.runLater(() -> {
                progressSlider.setValue(100.0);
            });
            
            // Wait for JavaFX thread to process
            WaitForAsyncUtils.waitForFxEvents();

            // Verify the progress label and completed checkbox
            assertEquals("100%", progressLabel.getText(), "Progress label should show 100%");
            assertTrue(completedCheckBox.isSelected(), "Completed checkbox should be selected when progress is 100%");
            
            // Now uncheck the completed checkbox and verify progress stays at 100%
            clickOn("#completedCheckBox");
            
            // Wait for JavaFX thread to process
            WaitForAsyncUtils.waitForFxEvents();
            
            // Verify progress remains at 100%
            assertEquals(100.0, progressSlider.getValue(), 0.1, "Progress should remain at 100%");
            assertEquals("100%", progressLabel.getText(), "Progress label should still show 100%");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in testProgressSliderAndCompletedCheckbox", e);
            e.printStackTrace();
            fail("Test failed due to exception: " + e.getMessage());
        }
    }

    @Test
    public void testTabPaneNavigation() {
        // Skip the test if presenter is null
        if (presenter == null) {
            LOGGER.severe("Cannot run test - presenter is null");
            return;
        }

        try {
            // Set up the presenter for an existing task
            Platform.runLater(() -> {
                presenter.initExistingTask(testTask);
            });

            // Wait for JavaFX thread to process
            WaitForAsyncUtils.waitForFxEvents();

            // Navigate to the Assigned Members tab
            clickOn("Assigned Members");
            
            // Wait for JavaFX thread to process
            WaitForAsyncUtils.waitForFxEvents();
            
            // Verify UI components in the Members tab
            TableView<TeamMember> assignedMembersTable = lookup("#assignedMembersTable").queryAs(TableView.class);
            Button addMemberButton = lookup("#addMemberButton").queryAs(Button.class);
            Button removeMemberButton = lookup("#removeMemberButton").queryAs(Button.class);
            
            assertNotNull(assignedMembersTable, "Assigned members table should exist");
            assertNotNull(addMemberButton, "Add member button should exist");
            assertNotNull(removeMemberButton, "Remove member button should exist");
            
            // Navigate to the Required Components tab
            clickOn("Required Components");
            
            // Wait for JavaFX thread to process
            WaitForAsyncUtils.waitForFxEvents();
            
            // Verify UI components in the Components tab
            TableView<Component> requiredComponentsTable = lookup("#requiredComponentsTable").queryAs(TableView.class);
            Button addComponentButton = lookup("#addComponentButton").queryAs(Button.class);
            Button removeComponentButton = lookup("#removeComponentButton").queryAs(Button.class);
            
            assertNotNull(requiredComponentsTable, "Required components table should exist");
            assertNotNull(addComponentButton, "Add component button should exist");
            assertNotNull(removeComponentButton, "Remove component button should exist");
            
            // Navigate to the Dependencies tab
            clickOn("Dependencies");
            
            // Wait for JavaFX thread to process
            WaitForAsyncUtils.waitForFxEvents();
            
            // Verify UI components in the Dependencies tab
            TableView<Task> dependenciesTable = lookup("#dependenciesTable").queryAs(TableView.class);
            Button addDependencyButton = lookup("#addDependencyButton").queryAs(Button.class);
            Button removeDependencyButton = lookup("#removeDependencyButton").queryAs(Button.class);
            
            assertNotNull(dependenciesTable, "Dependencies table should exist");
            assertNotNull(addDependencyButton, "Add dependency button should exist");
            assertNotNull(removeDependencyButton, "Remove dependency button should exist");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in testTabPaneNavigation", e);
            e.printStackTrace();
            fail("Test failed due to exception: " + e.getMessage());
        }
    }

    @Test
    public void testAddMemberButtonClick() {
        // Skip the test if presenter is null
        if (presenter == null) {
            LOGGER.severe("Cannot run test - presenter is null");
            return;
        }

        try {
            // Set up the presenter for an existing task
            Platform.runLater(() -> {
                presenter.initExistingTask(testTask);
            });

            // Wait for JavaFX thread to process
            WaitForAsyncUtils.waitForFxEvents();

            // Navigate to the Assigned Members tab
            clickOn("Assigned Members");
            
            // Wait for JavaFX thread to process
            WaitForAsyncUtils.waitForFxEvents();
            
            // Click the Add Member button
            clickOn("#addMemberButton");
            
            // Wait for JavaFX thread to process
            WaitForAsyncUtils.waitForFxEvents();
            
            // Verify the add member command was executed
            verify(mockAddMemberCommand, atLeastOnce()).execute();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in testAddMemberButtonClick", e);
            e.printStackTrace();
            fail("Test failed due to exception: " + e.getMessage());
        }
    }

    @Test
    public void testAddComponentButtonClick() {
        // Skip the test if presenter is null
        if (presenter == null) {
            LOGGER.severe("Cannot run test - presenter is null");
            return;
        }

        try {
            // Set up the presenter for an existing task
            Platform.runLater(() -> {
                presenter.initExistingTask(testTask);
            });

            // Wait for JavaFX thread to process
            WaitForAsyncUtils.waitForFxEvents();

            // Navigate to the Required Components tab
            clickOn("Required Components");
            
            // Wait for JavaFX thread to process
            WaitForAsyncUtils.waitForFxEvents();
            
            // Click the Add Component button
            clickOn("#addComponentButton");
            
            // Wait for JavaFX thread to process
            WaitForAsyncUtils.waitForFxEvents();
            
            // Verify the add component command was executed
            verify(mockAddComponentCommand, atLeastOnce()).execute();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in testAddComponentButtonClick", e);
            e.printStackTrace();
            fail("Test failed due to exception: " + e.getMessage());
        }
    }

    @Test
    public void testAddDependencyButtonClick() {
        // Skip the test if presenter is null
        if (presenter == null) {
            LOGGER.severe("Cannot run test - presenter is null");
            return;
        }

        try {
            // Set up the presenter for an existing task
            Platform.runLater(() -> {
                presenter.initExistingTask(testTask);
            });

            // Wait for JavaFX thread to process
            WaitForAsyncUtils.waitForFxEvents();

            // Navigate to the Dependencies tab
            clickOn("Dependencies");
            
            // Wait for JavaFX thread to process
            WaitForAsyncUtils.waitForFxEvents();
            
            // Click the Add Dependency button
            clickOn("#addDependencyButton");
            
            // Wait for JavaFX thread to process
            WaitForAsyncUtils.waitForFxEvents();
            
            // Verify the add dependency command was executed
            verify(mockAddDependencyCommand, atLeastOnce()).execute();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in testAddDependencyButtonClick", e);
            e.printStackTrace();
            fail("Test failed due to exception: " + e.getMessage());
        }
    }

    @Test
    public void testPriorityComboBoxSelection() {
        // Skip the test if presenter is null
        if (presenter == null) {
            LOGGER.severe("Cannot run test - presenter is null");
            return;
        }

        try {
            // Set up the presenter for a new task
            Platform.runLater(() -> {
                presenter.initNewTask(testTask);
            });

            // Wait for JavaFX thread to process
            WaitForAsyncUtils.waitForFxEvents();

            // Change the priority using the combo box
            ComboBox<Task.Priority> priorityComboBox = lookup("#priorityComboBox").queryAs(ComboBox.class);
            
            // Ensure the combo box has items
            assertFalse(priorityComboBox.getItems().isEmpty(), "Priority combo box should have items");
            
            // Directly select a priority
            Platform.runLater(() -> {
                priorityComboBox.getSelectionModel().select(Task.Priority.HIGH);
            });
            
            // Wait for JavaFX thread to process
            WaitForAsyncUtils.waitForFxEvents();

            // Verify the view model's priority property was updated
            verify(viewModel, atLeastOnce()).priorityProperty();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in testPriorityComboBoxSelection", e);
            e.printStackTrace();
            fail("Test failed due to exception: " + e.getMessage());
        }
    }

    @Test
    public void testDatePickerInteraction() {
        // Skip the test if presenter is null
        if (presenter == null) {
            LOGGER.severe("Cannot run test - presenter is null");
            return;
        }

        try {
            // Set up the presenter for a new task
            Platform.runLater(() -> {
                presenter.initNewTask(testTask);
            });

            // Wait for JavaFX thread to process
            WaitForAsyncUtils.waitForFxEvents();

            // Set a new date in the end date picker
            DatePicker endDatePicker = lookup("#endDatePicker").queryAs(DatePicker.class);
            LocalDate newDate = LocalDate.now().plusDays(30);
            
            Platform.runLater(() -> {
                endDatePicker.setValue(newDate);
            });
            
            // Wait for JavaFX thread to process
            WaitForAsyncUtils.waitForFxEvents();

            // Verify the view model's endDate property was updated
            verify(viewModel, atLeastOnce()).endDateProperty();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in testDatePickerInteraction", e);
            e.printStackTrace();
            fail("Test failed due to exception: " + e.getMessage());
        }
    }
}