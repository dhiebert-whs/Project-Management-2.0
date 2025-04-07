package org.frcpm.viewmodels;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
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

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

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
        // Create the ViewModel with mock services
        viewModel = new ComponentViewModel(mockComponentService, mockTaskService);

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

        // Mock service methods
        when(mockComponentService.save(any(Component.class))).thenReturn(testComponent);
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
        assertTrue(saveCommand.canExecute());

        // Act - execute the command
        saveCommand.execute();

        // Assert - verify the service was NOT called due to validation failure
        verify(mockComponentService, never()).save(any(Component.class));
        assertTrue(viewModel.isDirty());
        assertNotEquals("", viewModel.errorMessageProperty().get());
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
        viewModel.initExistingComponent(testComponent);
        viewModel.setSelectedTask(testTask);
        Command removeTaskCommand = viewModel.getRemoveTaskCommand();

        // Mock TaskService behavior
        when(mockTaskService.save(any(Task.class))).thenReturn(testTask);

        // Assert
        assertTrue(removeTaskCommand.canExecute());

        // Act - execute the command
        removeTaskCommand.execute();

        // Assert - verify task was removed
        assertEquals(0, viewModel.getRequiredForTasks().size());
        verify(mockTaskService).save(any(Task.class));
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
}