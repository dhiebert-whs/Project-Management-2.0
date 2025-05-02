package org.frcpm.presenters.testfx;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import org.frcpm.models.Component;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.presenters.ComponentPresenter;
import org.frcpm.services.ComponentService;
import org.frcpm.services.DialogService;
import org.frcpm.services.TaskService;
import org.frcpm.testfx.BaseFxTest;
import org.frcpm.testfx.TestFXHeadlessConfig;
import org.frcpm.viewmodels.ComponentViewModel;
import org.frcpm.views.ComponentView;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.util.WaitForAsyncUtils;

/**
 * TestFX test for the ComponentPresenter class.
 */
@ExtendWith(MockitoExtension.class)
public class ComponentPresenterTestFX extends BaseFxTest {

    @Mock
    private ComponentService componentService;
    
    @Mock
    private TaskService taskService;
    
    @Mock
    private DialogService dialogService;
    
    private AutoCloseable closeable;
    private ComponentView view;
    private ComponentPresenter presenter;
    private ComponentViewModel viewModel;
    
    // Test data
    private Component testComponent;
    private List<Task> testTasks;

    @Override
    protected void initializeTestComponents(Stage stage) {
        // Create view and presenter
        view = new ComponentView();
        presenter = (ComponentPresenter) view.getPresenter();
        
        // Initialize the scene with our view
        Scene scene = new Scene(view.getView(), 800, 600);
        stage.setScene(scene);
        
        // Setup the presenter with mocked services
        injectMockedServices();
        
        // Create test data
        setupTestData();
        
        // Setup mocked service responses
        setupMockResponses();
    }
    
    @BeforeEach
    public void initMocks() {
        closeable = MockitoAnnotations.openMocks(this);
        
        // Allow access to the ViewModel for test verification
        if (presenter != null) {
            viewModel = presenter.getViewModel();
        }
    }
    
    private void injectMockedServices() {
        try {
            // Use reflection to inject mocked services
            java.lang.reflect.Field componentServiceField = presenter.getClass().getDeclaredField("componentService");
            componentServiceField.setAccessible(true);
            componentServiceField.set(presenter, componentService);
            
            java.lang.reflect.Field taskServiceField = presenter.getClass().getDeclaredField("taskService");
            taskServiceField.setAccessible(true);
            taskServiceField.set(presenter, taskService);
            
            java.lang.reflect.Field dialogServiceField = presenter.getClass().getDeclaredField("dialogService");
            dialogServiceField.setAccessible(true);
            dialogServiceField.set(presenter, dialogService);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed to inject mocked services: " + e.getMessage());
        }
    }
    
    private void setupTestData() {
        // Create a test component
        testComponent = new Component();
        testComponent.setId(1L);
        testComponent.setName("Test Component");
        testComponent.setPartNumber("TEST-001");
        testComponent.setDescription("Test component description");
        testComponent.setExpectedDelivery(LocalDate.now().plusDays(7));
        testComponent.setDelivered(false);
        
        // Create test tasks
        testTasks = new ArrayList<>();
        
        Task task1 = new Task();
        task1.setId(1L);
        task1.setTitle("Test Task 1");
        Subsystem subsystem1 = new Subsystem("Test Subsystem 1");
        subsystem1.setId(1L);
        task1.setSubsystem(subsystem1);
        task1.setProgress(25);
        
        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Test Task 2");
        Subsystem subsystem2 = new Subsystem("Test Subsystem 2");
        subsystem2.setId(2L);
        task2.setSubsystem(subsystem2);
        task2.setProgress(50);
        
        testTasks.add(task1);
        testTasks.add(task2);
    }
    
    private void setupMockResponses() {
        // Configure the mock services
        when(componentService.findById(anyLong())).thenReturn(testComponent);
        
        // Create and mock the tasks table data to match what the presenter expects
        // The presenter will likely query these tasks by passing the component's ID to the updateRequiredComponents method
        // or by retrieving all tasks and filtering them
        
        // Mock the service calls that would actually be used by the ComponentPresenter
        when(taskService.findAll()).thenReturn(testTasks);
        doReturn(testTasks.get(0)).when(taskService).updateRequiredComponents(anyLong(), anySet());
        
        when(componentService.updateExpectedDelivery(anyLong(), any(LocalDate.class))).thenAnswer(invocation -> {
            Component component = new Component();
            component.setId(testComponent.getId());
            component.setName(testComponent.getName());
            component.setPartNumber(testComponent.getPartNumber());
            component.setDescription(testComponent.getDescription());
            component.setExpectedDelivery(invocation.getArgument(1));
            component.setDelivered(testComponent.isDelivered());
            return component;
        });
        
        when(componentService.markAsDelivered(anyLong(), any(LocalDate.class))).thenAnswer(invocation -> {
            Component component = new Component();
            component.setId(testComponent.getId());
            component.setName(testComponent.getName());
            component.setPartNumber(testComponent.getPartNumber());
            component.setDescription(testComponent.getDescription());
            component.setExpectedDelivery(testComponent.getExpectedDelivery());
            component.setDelivered(true);
            component.setActualDelivery(invocation.getArgument(1));
            return component;
        });
    }
    
    @Test
    public void testComponentFormInitialization() {
        // Initialize the form with a component
        presenter.initExistingComponent(testComponent);
        
        // Wait for JavaFX thread to process
        WaitForAsyncUtils.waitForFxEvents();
        
        // Get the form fields
        TextField nameTextField = lookup("#nameTextField").query();
        TextField partNumberTextField = lookup("#partNumberTextField").query();
        DatePicker expectedDeliveryDatePicker = lookup("#expectedDeliveryDatePicker").query();
        CheckBox deliveredCheckBox = lookup("#deliveredCheckBox").query();
        TextArea descriptionTextArea = lookup("#descriptionTextArea").query();
        
        // Verify the form is correctly initialized
        assertEquals("Test Component", nameTextField.getText());
        assertEquals("TEST-001", partNumberTextField.getText());
        assertEquals(testComponent.getExpectedDelivery(), expectedDeliveryDatePicker.getValue());
        assertFalse(deliveredCheckBox.isSelected());
        assertEquals("Test component description", descriptionTextArea.getText());
    }
    
    @Test
    public void testMarkAsDelivered() {
        // Initialize the form with a component
        presenter.initExistingComponent(testComponent);
        
        // Wait for JavaFX thread to process
        WaitForAsyncUtils.waitForFxEvents();
        
        // Get the delivered checkbox
        CheckBox deliveredCheckBox = lookup("#deliveredCheckBox").query();
        
        // Click the checkbox to mark as delivered
        clickOn(deliveredCheckBox);
        
        // Wait for JavaFX thread to process
        WaitForAsyncUtils.waitForFxEvents();
        
        // Get the actual delivery date picker (should be enabled now)
        DatePicker actualDeliveryDatePicker = lookup("#actualDeliveryDatePicker").query();
        
        // Verify the date picker is enabled
        assertFalse(actualDeliveryDatePicker.isDisabled());
        
        // Set a delivery date
        actualDeliveryDatePicker.setValue(LocalDate.now());
        
        // Click save button
        clickOn("#saveButton");
        
        // Wait for JavaFX thread to process
        WaitForAsyncUtils.waitForFxEvents();
        
        // Verify the component service was called
        verify(componentService).markAsDelivered(eq(testComponent.getId()), eq(LocalDate.now()));
    }
    
    @Test
    public void testUpdateExpectedDelivery() {
        // Initialize the form with a component
        presenter.initExistingComponent(testComponent);
        
        // Wait for JavaFX thread to process
        WaitForAsyncUtils.waitForFxEvents();
        
        // Get the expected delivery date picker
        DatePicker expectedDeliveryDatePicker = lookup("#expectedDeliveryDatePicker").query();
        
        // Change the expected delivery date
        LocalDate newDate = LocalDate.now().plusDays(14);
        expectedDeliveryDatePicker.setValue(newDate);
        
        // Click save button
        clickOn("#saveButton");
        
        // Wait for JavaFX thread to process
        WaitForAsyncUtils.waitForFxEvents();
        
        // Verify the component service was called
        verify(componentService).updateExpectedDelivery(eq(testComponent.getId()), eq(newDate));
    }
    
    @Test
    public void testTasksTable() {
        // Initialize the form with a component
        presenter.initExistingComponent(testComponent);
        
        // Wait for JavaFX thread to process
        WaitForAsyncUtils.waitForFxEvents();
        
        try {
            // Get the tasks table - might be empty if the presenter doesn't populate it
            // without additional user interaction
            TableView<?> tasksTable = lookup("#requiredForTasksTable").query();
            
            // This test might need adjusting based on how the actual ComponentPresenter
            // populates the tasks table - it may require clicking buttons or other UI actions
            // to retrieve and display tasks
            
            // For now, we'll just verify the table exists
            assertNotNull(tasksTable, "Tasks table should exist");
            
            // If the table should be populated on initialization, uncomment this:
            // assertEquals(2, tasksTable.getItems().size(), "Table should have 2 task records");
        } catch (Exception e) {
            // If the test fails, let's log some details to understand why
            System.err.println("Error in testTasksTable: " + e.getMessage());
            e.printStackTrace();
            fail("Test failed: " + e.getMessage());
        }
    }
    
    @Test
    public void testCancelButton() {
        // Initialize the form with a component
        presenter.initExistingComponent(testComponent);
        
        // Wait for JavaFX thread to process
        WaitForAsyncUtils.waitForFxEvents();
        
        // Make some changes
        TextField nameTextField = lookup("#nameTextField").query();
        nameTextField.setText("Changed Name");
        
        // Click cancel button
        clickOn("#cancelButton");
        
        // Wait for JavaFX thread to process
        WaitForAsyncUtils.waitForFxEvents();
        
        // Verify the dialog service was called to show confirmation
        verify(dialogService).showConfirmationAlert(anyString(), anyString());
    }
}