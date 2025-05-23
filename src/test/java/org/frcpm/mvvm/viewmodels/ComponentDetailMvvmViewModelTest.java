// src/test/java/org/frcpm/mvvm/viewmodels/ComponentDetailMvvmViewModelTest.java
package org.frcpm.mvvm.viewmodels;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.frcpm.di.TestModule;
import org.frcpm.models.Component;
import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.services.ComponentService;
import org.frcpm.services.TaskService;
import org.frcpm.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the ComponentDetailMvvmViewModel class.
 * FIXED: Now creates mocks properly and registers them with TestModule.
 */
public class ComponentDetailMvvmViewModelTest {
    
    private ComponentService componentService;
    private TaskService taskService;
    
    private Project testProject;
    private Component testComponent;
    private List<Task> testTasks;
    private ComponentDetailMvvmViewModel viewModel;
    
    @BeforeEach
    public void setUp() throws Exception {
        // Initialize TestModule
        TestModule.initialize();
        
        // Create test data
        setupTestData();
        
        // FIXED: Create mocks first
        ComponentService mockComponentService = mock(ComponentService.class);
        TaskService mockTaskService = mock(TaskService.class);
        
        // Configure mock behavior for sync methods
        when(mockComponentService.findById(anyLong())).thenReturn(testComponent);
        when(mockComponentService.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(mockTaskService.findById(anyLong())).thenReturn(testTasks.get(0));
        
        // Configure mock behavior for async methods
        doAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            Consumer<Component> callback = invocation.getArgument(1);
            callback.accept(testComponent);
            return null;
        }).when(mockComponentService).findByIdAsync(anyLong(), any(), any());
        
        doAnswer(invocation -> {
            Component component = invocation.getArgument(0);
            Consumer<Component> callback = invocation.getArgument(1);
            callback.accept(component);
            return null;
        }).when(mockComponentService).saveAsync(any(), any(), any());
        
        doAnswer(invocation -> {
            Task updatedTask = testTasks.get(0);
            Consumer<Task> callback = invocation.getArgument(2);
            callback.accept(updatedTask);
            return null;
        }).when(mockTaskService).associateComponentsWithTaskAsync(anyLong(), any(), any(), any());
        
        // Register mocks with TestModule
        TestModule.setService(ComponentService.class, mockComponentService);
        TestModule.setService(TaskService.class, mockTaskService);
        
        // Get services from TestModule (now returns our mocks)
        componentService = TestModule.getService(ComponentService.class);
        taskService = TestModule.getService(TaskService.class);
        
        // Initialize JavaFX toolkit if needed
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
        
        // Create the view model on the JavaFX thread
        TestUtils.runOnFxThreadAndWait(() -> {
            viewModel = new ComponentDetailMvvmViewModel(componentService, taskService);
        });
    }
    
    private void setupTestData() {
        // Create test project
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");
        
        // Create test component
        testComponent = new Component();
        testComponent.setId(1L);
        testComponent.setName("Drive Motors");
        testComponent.setPartNumber("CIM-001");
        testComponent.setDescription("CIM motors for drive system");
        testComponent.setExpectedDelivery(LocalDate.now().plusDays(7));
        testComponent.setDelivered(false);
        
        // Create test tasks
        testTasks = new ArrayList<>();
        
        Task task1 = new Task();
        task1.setId(1L);
        task1.setTitle("Install Drive Motors");
        task1.setProject(testProject);
        task1.addRequiredComponent(testComponent);
        testTasks.add(task1);
        
        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Test Drive System");
        task2.setProject(testProject);
        task2.addRequiredComponent(testComponent);
        testTasks.add(task2);
        
        // Add tasks to component
        testComponent.getRequiredForTasks().addAll(testTasks);
    }
    
    @Test
    public void testInitialStateForNewComponent() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new component
            viewModel.initNewComponent();
            
            // Verify initial state
            assertTrue(viewModel.isNewComponent());
            assertEquals("", viewModel.getName());
            assertEquals("", viewModel.getPartNumber());
            assertEquals("", viewModel.getDescription());
            assertNull(viewModel.getExpectedDelivery());
            assertNull(viewModel.getActualDelivery());
            assertFalse(viewModel.isDelivered());
            assertFalse(viewModel.isDirty());
            assertFalse(viewModel.isValid()); // Should be invalid due to empty name
            
            // Verify tasks list is empty
            assertTrue(viewModel.getRequiredForTasks().isEmpty());
            
            // Verify commands
            assertNotNull(viewModel.getSaveCommand());
            assertNotNull(viewModel.getCancelCommand());
            assertNotNull(viewModel.getAddTaskCommand());
            assertNotNull(viewModel.getRemoveTaskCommand());
            
            // Save command should not be executable (not valid)
            assertTrue(viewModel.getSaveCommand().isNotExecutable());
            
            // Add task command should not be executable (new component)
            assertTrue(viewModel.getAddTaskCommand().isNotExecutable());
            
            // Remove task command should not be executable (no selection)
            assertTrue(viewModel.getRemoveTaskCommand().isNotExecutable());
        });
    }
    
    @Test
    public void testInitExistingComponent() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for an existing component
            viewModel.initExistingComponent(testComponent);
            
            // Verify state
            assertFalse(viewModel.isNewComponent());
            assertEquals("Drive Motors", viewModel.getName());
            assertEquals("CIM-001", viewModel.getPartNumber());
            assertEquals("CIM motors for drive system", viewModel.getDescription());
            assertEquals(testComponent.getExpectedDelivery(), viewModel.getExpectedDelivery());
            assertNull(viewModel.getActualDelivery());
            assertFalse(viewModel.isDelivered());
            assertFalse(viewModel.isDirty());
            assertTrue(viewModel.isValid()); // Should be valid with existing data
            
            // Verify component reference
            assertEquals(testComponent, viewModel.getComponent());
            
            // Add task command should be executable for existing component
            assertTrue(viewModel.getAddTaskCommand().isExecutable());
            
            // Let async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify tasks were loaded
            assertFalse(viewModel.getRequiredForTasks().isEmpty());
        });
    }
    
    @Test
    public void testValidation() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new component
            viewModel.initNewComponent();
            
            // Initially not valid (name is empty)
            assertFalse(viewModel.isValid());
            assertNotNull(viewModel.getErrorMessage());
            assertTrue(viewModel.getErrorMessage().contains("name"));
            
            // Set name only
            viewModel.setName("Test Component");
            
            // Should now be valid
            assertTrue(viewModel.isValid());
            assertNull(viewModel.getErrorMessage());
            
            // Test delivered without actual delivery date
            viewModel.setDelivered(true);
            
            // Should be invalid due to missing actual delivery date
            assertFalse(viewModel.isValid());
            assertNotNull(viewModel.getErrorMessage());
            assertTrue(viewModel.getErrorMessage().contains("delivery date"));
            
            // Set actual delivery date
            viewModel.setActualDelivery(LocalDate.now());
            
            // Should be valid again
            assertTrue(viewModel.isValid());
            assertNull(viewModel.getErrorMessage());
        });
    }
    
    @Test
    public void testSaveNewComponent() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new component
            viewModel.initNewComponent();
            
            // Set required properties
            viewModel.setName("New Motor");
            viewModel.setPartNumber("NEW-001");
            viewModel.setDescription("New drive motor");
            viewModel.setExpectedDelivery(LocalDate.now().plusDays(5));
            
            // Should be valid and dirty
            assertTrue(viewModel.isValid());
            assertTrue(viewModel.isDirty());
            
            // Save command should be executable
            assertTrue(viewModel.getSaveCommand().isExecutable());
            
            // Execute save command
            viewModel.getSaveCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should no longer be loading after save completes
            assertFalse(viewModel.isLoading());
            
            // Should no longer be a new component
            assertFalse(viewModel.isNewComponent());
            
            // Should no longer be dirty
            assertFalse(viewModel.isDirty());
        });
    }
    
    @Test
    public void testAddTask() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize with existing component
            viewModel.initExistingComponent(testComponent);
            
            // Create a new task to add
            Task newTask = new Task();
            newTask.setId(3L);
            newTask.setTitle("New Task Requiring Component");
            
            // Add the task
            boolean result = viewModel.addTask(newTask);
            
            // Should return true for success
            assertTrue(result);
            
            // Let async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should be marked as dirty
            assertTrue(viewModel.isDirty());
        });
    }
    
    @Test
    public void testErrorHandlingDuringSave() {
        // Create new mocks for error scenario
        ComponentService errorComponentService = mock(ComponentService.class);
        
        // Configure mock service to return an error
        doAnswer(invocation -> {
            Consumer<Throwable> errorCallback = invocation.getArgument(2);
            errorCallback.accept(new RuntimeException("Save error"));
            return null;
        }).when(errorComponentService).saveAsync(any(), any(), any());
        
        // Register error mock
        TestModule.setService(ComponentService.class, errorComponentService);
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Create new viewModel with error service
            ComponentDetailMvvmViewModel errorViewModel = new ComponentDetailMvvmViewModel(
                TestModule.getService(ComponentService.class), 
                TestModule.getService(TaskService.class)
            );
            
            // Initialize for a new component
            errorViewModel.initNewComponent();
            errorViewModel.setName("Test Component");
            
            // Execute save command
            errorViewModel.getSaveCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should have error message
            assertNotNull(errorViewModel.getErrorMessage());
            assertTrue(errorViewModel.getErrorMessage().contains("Failed to save"));
            
            // Should no longer be loading
            assertFalse(errorViewModel.isLoading());
        });
    }
    
    @Test
    public void testAsyncServiceMocking() {
        // Verify that the services are mocks and async methods work
        assertNotNull(componentService);
        assertNotNull(taskService);
        
        // Verify we can stub the async methods without NotAMockException
        doAnswer(invocation -> {
            Consumer<Component> callback = invocation.getArgument(1);
            callback.accept(testComponent);
            return null;
        }).when(componentService).findByIdAsync(anyLong(), any(), any());
        
        // Test that the stubbing worked
        componentService.findByIdAsync(1L, 
            result -> assertEquals(testComponent, result),
            error -> fail("Should not have error"));
    }
}