package org.frcpm.viewmodels;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import org.frcpm.binding.Command;
import org.frcpm.models.Component;
import org.frcpm.models.Task;
import org.frcpm.services.ComponentService;
import org.frcpm.services.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

@ExtendWith(MockitoExtension.class)
public class ComponentViewModelTest {

    @Mock
    private ComponentService mockComponentService;

    @Mock
    private TaskService mockTaskService;

    private ComponentViewModel viewModel;
    private Component testComponent;
    private Task testTask;

    @BeforeEach
    public void setUp() {
        // Create test data first
        setupTestData();
        
        // Create the ViewModel with mock services - don't configure mocks here
        viewModel = new ComponentViewModel(mockComponentService, mockTaskService);
    }
    
    private void setupTestData() {
        // Create test data
        testComponent = new Component("Test Component", "PART123");
        testComponent.setId(1L);
        testComponent.setDescription("Test component description");
        testComponent.setExpectedDelivery(LocalDate.now().plusWeeks(2));
        testComponent.setDelivered(false);

        // Create a test task
        testTask = new Task();
        testTask.setId(1L);
        testTask.setTitle("Test Task");
        testTask.setProgress(50);

        // Set up the relationship
        Set<Task> tasks = new HashSet<>();
        tasks.add(testTask);
        testComponent.setRequiredForTasks(tasks);
    }

    @Test
    public void testInitNewComponent() {
        // Act
        viewModel.initNewComponent();

        // Assert
        assertNotNull(viewModel.getComponent());
        assertEquals("", viewModel.nameProperty().get());
        assertEquals("", viewModel.partNumberProperty().get());
        assertEquals("", viewModel.descriptionProperty().get());
        assertNull(viewModel.expectedDeliveryProperty().get());
        assertNull(viewModel.actualDeliveryProperty().get());
        assertFalse(viewModel.deliveredProperty().get());
        assertTrue(viewModel.getRequiredForTasks().isEmpty());
        assertFalse(viewModel.isDirty());
        assertNull(viewModel.getErrorMessage()); // Make sure error message is cleared
    }

    @Test
    public void testInitExistingComponent() {
        // Act
        viewModel.initExistingComponent(testComponent);

        // Assert
        assertEquals(testComponent, viewModel.getComponent());
        assertEquals(testComponent.getName(), viewModel.nameProperty().get());
        assertEquals(testComponent.getPartNumber(), viewModel.partNumberProperty().get());
        assertEquals(testComponent.getDescription(), viewModel.descriptionProperty().get());
        assertEquals(testComponent.getExpectedDelivery(), viewModel.expectedDeliveryProperty().get());
        assertEquals(testComponent.getActualDelivery(), viewModel.actualDeliveryProperty().get());
        assertEquals(testComponent.isDelivered(), viewModel.deliveredProperty().get());
        assertEquals(1, viewModel.getRequiredForTasks().size());
        assertFalse(viewModel.isDirty());
        assertNull(viewModel.getErrorMessage()); // Make sure error message is cleared
    }

    @Test
    public void testInitExistingComponentNull() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> viewModel.initExistingComponent(null));
    }

    @Test
    public void testPropertyGetters() {
        // Act
        viewModel.initExistingComponent(testComponent);
        StringProperty nameProperty = viewModel.nameProperty();
        StringProperty partNumberProperty = viewModel.partNumberProperty();
        StringProperty descriptionProperty = viewModel.descriptionProperty();
        ObjectProperty<LocalDate> expectedDeliveryProperty = viewModel.expectedDeliveryProperty();
        ObjectProperty<LocalDate> actualDeliveryProperty = viewModel.actualDeliveryProperty();
        BooleanProperty deliveredProperty = viewModel.deliveredProperty();

        // Assert
        assertNotNull(nameProperty);
        assertNotNull(partNumberProperty);
        assertNotNull(descriptionProperty);
        assertNotNull(expectedDeliveryProperty);
        assertNotNull(actualDeliveryProperty);
        assertNotNull(deliveredProperty);
        assertEquals(testComponent.getName(), nameProperty.get());
    }

    @Test
    public void testSaveCommand() {
        // Arrange
        when(mockComponentService.save(any(Component.class))).thenReturn(testComponent);
        viewModel.initExistingComponent(testComponent);
        Command saveCommand = viewModel.getSaveCommand();

        // Act - change a property to make it dirty
        viewModel.nameProperty().set("Updated Name");

        // Assert
        assertTrue(viewModel.isDirty());
        assertTrue(saveCommand.canExecute());

        // Act - execute the command
        saveCommand.execute();

        // Assert - verify the service was called
        verify(mockComponentService).save(any(Component.class));
        assertFalse(viewModel.isDirty());
        assertNull(viewModel.getErrorMessage()); // Error message should be cleared after successful save
    }

    @Test
    public void testSaveCommandValidationFailure() {
        // Arrange
        viewModel.initNewComponent();
        Command saveCommand = viewModel.getSaveCommand();

        // Act - set properties but leave name empty to cause validation failure
        viewModel.nameProperty().set("");
        viewModel.partNumberProperty().set("PART456");
        
        // Force dirty state for testing
        viewModel.dirtyProperty().set(true);

        // Assert
        assertTrue(viewModel.isDirty());
        
        // Validate to set error message and valid state
        viewModel.validate();
        assertFalse(viewModel.isValid()); // Should be invalid due to missing name
        assertFalse(saveCommand.canExecute()); // Save command should not be executable
        assertNotNull(viewModel.getErrorMessage());
        assertTrue(viewModel.getErrorMessage().contains("Component name is required"));
        
        // Verify service was never called
        verify(mockComponentService, never()).save(any());
    }

    @Test
    public void testValidation_ValidComponent() {
        // Arrange
        viewModel.initNewComponent();
        
        // Act - set valid properties
        viewModel.nameProperty().set("Valid Component");
        
        // Assert
        viewModel.validate(); // Explicitly validate
        assertTrue(viewModel.isValid());
        assertNull(viewModel.getErrorMessage());
    }
    
    @Test
    public void testValidation_DeliveredWithoutDate() {
        // Arrange
        viewModel.initNewComponent();
        
        // Act - set valid name but mark as delivered without delivery date
        viewModel.nameProperty().set("Valid Component");
        viewModel.deliveredProperty().set(true);
        viewModel.actualDeliveryProperty().set(null); // Clear the date that might be auto-set
        
        // Assert
        viewModel.validate(); // Explicitly validate
        assertFalse(viewModel.isValid());
        assertNotNull(viewModel.getErrorMessage());
        assertTrue(viewModel.getErrorMessage().contains("Actual delivery date is required"));
    }

    @Test
    public void testCancelCommand() {
        // Arrange
        viewModel.initExistingComponent(testComponent);
        Command cancelCommand = viewModel.getCancelCommand();

        // Assert
        assertTrue(cancelCommand.canExecute());

        // Act - execute the command (this just returns control to the controller)
        cancelCommand.execute();
    }

    @Test
    public void testAddTaskCommand() {
        // Arrange
        viewModel.initExistingComponent(testComponent);
        Command addTaskCommand = viewModel.getAddTaskCommand();

        // Assert
        assertTrue(addTaskCommand.canExecute());

        // Act - execute the command
        addTaskCommand.execute();
    }

    @Test
    public void testRemoveTaskCommand() {
        // Arrange
        when(mockTaskService.save(any(Task.class))).thenReturn(testTask);
        viewModel.initExistingComponent(testComponent);
        viewModel.setSelectedTask(testTask);
        Command removeTaskCommand = viewModel.getRemoveTaskCommand();

        // Assert
        assertTrue(removeTaskCommand.canExecute());

        // Act - execute the command
        removeTaskCommand.execute();

        // Assert - verify task was removed
        assertEquals(0, viewModel.getRequiredForTasks().size());
        verify(mockTaskService).save(any(Task.class));
    }

    @Test
    public void testRemoveTaskCommand_NullTask() {
        // Arrange
        viewModel.initExistingComponent(testComponent);
        viewModel.setSelectedTask(null);
        Command removeTaskCommand = viewModel.getRemoveTaskCommand();

        // Assert
        assertFalse(removeTaskCommand.canExecute());
    }
    
    @Test
    public void testRemoveTaskCommand_ExceptionHandling() {
        // Arrange
        viewModel.initExistingComponent(testComponent);
        viewModel.setSelectedTask(testTask);
        Command removeTaskCommand = viewModel.getRemoveTaskCommand();

        // Mock TaskService to throw exception
        doThrow(new RuntimeException("Test exception")).when(mockTaskService).save(any(Task.class));

        // Act
        removeTaskCommand.execute();

        // Assert
        assertNotNull(viewModel.getErrorMessage());
        assertTrue(viewModel.getErrorMessage().contains("Failed to remove task"));
    }

    @Test
    public void testPropertyChangesDirtyFlag() {
        // Arrange
        viewModel.initExistingComponent(testComponent);
        assertFalse(viewModel.isDirty());

        // Act - change name
        viewModel.nameProperty().set("New Name");

        // Assert
        assertTrue(viewModel.isDirty());

        // Act - change part number
        viewModel.partNumberProperty().set("NEW123");

        // Assert
        assertTrue(viewModel.isDirty());

        // Act - change description
        viewModel.descriptionProperty().set("New description");

        // Assert
        assertTrue(viewModel.isDirty());

        // Act - change expected delivery
        viewModel.expectedDeliveryProperty().set(LocalDate.now().plusDays(30));

        // Assert
        assertTrue(viewModel.isDirty());

        // Act - change actual delivery
        viewModel.actualDeliveryProperty().set(LocalDate.now());

        // Assert
        assertTrue(viewModel.isDirty());

        // Act - change delivered
        viewModel.deliveredProperty().set(true);

        // Assert
        assertTrue(viewModel.isDirty());
    }

    @Test
    public void testDeliveredSetActualDelivery() {
        // Arrange
        viewModel.initNewComponent();
        assertNull(viewModel.actualDeliveryProperty().get());

        // Act - mark as delivered
        viewModel.deliveredProperty().set(true);

        // Assert - actual delivery should be set to today
        assertNotNull(viewModel.actualDeliveryProperty().get());
        assertEquals(LocalDate.now(), viewModel.actualDeliveryProperty().get());
    }

    @Test
    public void testGetRequiredForTasks() {
        // Arrange
        viewModel.initExistingComponent(testComponent);
        
        // Act
        ObservableList<Task> tasks = viewModel.getRequiredForTasks();
        
        // Assert
        assertNotNull(tasks);
        assertEquals(1, tasks.size());
        assertEquals(testTask.getId(), tasks.get(0).getId());
    }
    
    @Test
    public void testLoadTasks_HandlesNullRequiredForTasks() {
        // Arrange
        testComponent.setRequiredForTasks(null);
        viewModel.initExistingComponent(testComponent);
        
        // Act - explicitly call loadTasks to test null handling
        viewModel.loadTasks();
        
        // Assert
        assertTrue(viewModel.getRequiredForTasks().isEmpty());
    }

    @Test
    public void testSetSelectedTask() {
        // Arrange
        viewModel.initExistingComponent(testComponent);
        assertNull(viewModel.getSelectedTask());
        
        // Act
        viewModel.setSelectedTask(testTask);
        
        // Assert
        assertEquals(testTask, viewModel.getSelectedTask());
        assertTrue(viewModel.getRemoveTaskCommand().canExecute());
    }
    
    @Test
    public void testCleanupResources() {
        // Arrange
        viewModel.initExistingComponent(testComponent);
        
        // Act
        viewModel.cleanupResources();
        
        // Assert - just make sure it doesn't throw exceptions
        // This is primarily testing that the method exists and can be called
    }
}