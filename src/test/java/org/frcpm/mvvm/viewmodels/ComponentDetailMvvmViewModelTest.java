// src/test/java/org/frcpm/mvvm/viewmodels/ComponentDetailMvvmViewModelTest.java
package org.frcpm.mvvm.viewmodels;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
 * FIXED: Removed problematic casting to specific async implementation classes.
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
        
        // Get service references from TestModule (no more casting needed)
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
        // Configure mock service for loading tasks using direct stubbing
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<Component> successCallback = invocation.getArgument(1);
            successCallback.accept(testComponent);
            return null;
        }).when(componentService).findByIdAsync(anyLong(), any(), any());
        
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
                Thread.sleep(200);
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
    public void testPropertyChangesSetDirtyFlag() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize with existing component
            viewModel.initExistingComponent(testComponent);
            
            // Initially should not be dirty
            assertFalse(viewModel.isDirty());
            
            // Change name
            viewModel.setName("Updated Motors");
            assertTrue(viewModel.isDirty());
            
            // Reset dirty flag
            viewModel.setDirty(false);
            
            // Change part number
            viewModel.setPartNumber("CIM-002");
            assertTrue(viewModel.isDirty());
            
            // Reset dirty flag
            viewModel.setDirty(false);
            
            // Change description
            viewModel.setDescription("Updated description");
            assertTrue(viewModel.isDirty());
            
            // Reset dirty flag
            viewModel.setDirty(false);
            
            // Change expected delivery
            viewModel.setExpectedDelivery(LocalDate.now().plusDays(10));
            assertTrue(viewModel.isDirty());
            
            // Reset dirty flag
            viewModel.setDirty(false);
            
            // Change actual delivery
            viewModel.setActualDelivery(LocalDate.now());
            assertTrue(viewModel.isDirty());
            
            // Reset dirty flag
            viewModel.setDirty(false);
            
            // Change delivered status
            viewModel.setDelivered(true);
            assertTrue(viewModel.isDirty());
        });
    }
    
    @Test
    public void testDeliveredStatusAutoSetsDate() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new component
            viewModel.initNewComponent();
            viewModel.setName("Test Component");
            
            // Initially not delivered and no actual delivery date
            assertFalse(viewModel.isDelivered());
            assertNull(viewModel.getActualDelivery());
            
            // Mark as delivered
            viewModel.setDelivered(true);
            
            // Should automatically set actual delivery date to today
            assertTrue(viewModel.isDelivered());
            assertEquals(LocalDate.now(), viewModel.getActualDelivery());
        });
    }
    
    @Test
    public void testSaveNewComponent() {
        // Configure mock service for successful save using direct stubbing
        doAnswer(invocation -> {
            Component componentToSave = invocation.getArgument(0);
            componentToSave.setId(999L); // Simulate setting ID after save
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<Component> successCallback = invocation.getArgument(1);
            successCallback.accept(componentToSave);
            return null;
        }).when(componentService).saveAsync(any(), any(), any());
        
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
            
            // Verify loading state
            assertTrue(viewModel.isLoading());
            
            // Let async operations complete
            try {
                Thread.sleep(200);
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
    public void testSaveExistingComponent() {
        // Configure mock service for successful save using direct stubbing
        doAnswer(invocation -> {
            Component componentToSave = invocation.getArgument(0);
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<Component> successCallback = invocation.getArgument(1);
            successCallback.accept(componentToSave);
            return null;
        }).when(componentService).saveAsync(any(), any(), any());
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize with existing component
            viewModel.initExistingComponent(testComponent);
            
            // Update some properties
            viewModel.setName("Updated Motors");
            viewModel.setDescription("Updated description for drive motors");
            viewModel.setDelivered(true);
            viewModel.setActualDelivery(LocalDate.now());
            
            // Should be valid and dirty
            assertTrue(viewModel.isValid());
            assertTrue(viewModel.isDirty());
            
            // Save command should be executable
            assertTrue(viewModel.getSaveCommand().isExecutable());
            
            // Execute save command
            viewModel.getSaveCommand().execute();
            
            // Verify loading state
            assertTrue(viewModel.isLoading());
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should no longer be loading after save completes
            assertFalse(viewModel.isLoading());
            
            // Should no longer be dirty
            assertFalse(viewModel.isDirty());
        });
    }
    
    @Test
    public void testCancelCommand() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new component
            viewModel.initNewComponent();
            
            // Verify cancel command is executable
            assertTrue(viewModel.getCancelCommand().isExecutable());
            
            // Execute cancel command
            viewModel.getCancelCommand().execute();
            
            // This is mainly for the view to handle dialog closing
            // Just verify the command executed without error
        });
    }
    
    @Test
    public void testAddTask() {
        // Configure mock service for task association using direct stubbing
        doAnswer(invocation -> {
            Task updatedTask = invocation.getArgument(0);
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<Task> successCallback = invocation.getArgument(2);
            successCallback.accept(updatedTask);
            return null;
        }).when(taskService).associateComponentsWithTaskAsync(anyLong(), any(), any(), any());
        
        // Configure component service for refreshing using direct stubbing
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<Component> successCallback = invocation.getArgument(1);
            successCallback.accept(testComponent);
            return null;
        }).when(componentService).findByIdAsync(anyLong(), any(), any());
        
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
            
            // Should be loading
            assertTrue(viewModel.isLoading());
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should be marked as dirty
            assertTrue(viewModel.isDirty());
        });
    }
    
    @Test
    public void testAddTaskToNewComponent() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new component
            viewModel.initNewComponent();
            
            // Create a task to add
            Task newTask = new Task();
            newTask.setId(1L);
            newTask.setTitle("Test Task");
            
            // Should not be able to add task to unsaved component
            boolean result = viewModel.addTask(newTask);
            assertFalse(result);
            
            // Should have error message
            assertNotNull(viewModel.getErrorMessage());
            assertTrue(viewModel.getErrorMessage().contains("must be saved"));
        });
    }
    
    @Test
    public void testAddDuplicateTask() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize with existing component
            viewModel.initExistingComponent(testComponent);
            
            // Load tasks into the view model
            viewModel.getRequiredForTasks().addAll(testTasks);
            
            // Try to add a task that's already in the list
            Task existingTask = testTasks.get(0);
            boolean result = viewModel.addTask(existingTask);
            
            // Should return false for duplicate
            assertFalse(result);
            
            // Should have error message
            assertNotNull(viewModel.getErrorMessage());
            assertTrue(viewModel.getErrorMessage().contains("already associated"));
        });
    }
    
    @Test
    public void testAddNullTask() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize with existing component
            viewModel.initExistingComponent(testComponent);
            
            // Try to add null task
            boolean result = viewModel.addTask(null);
            
            // Should return false
            assertFalse(result);
        });
    }
    
    @Test
    public void testRemoveTask() {
        // Configure mock service for task disassociation using direct stubbing
        doAnswer(invocation -> {
            Task updatedTask = invocation.getArgument(0);
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<Task> successCallback = invocation.getArgument(2);
            successCallback.accept(updatedTask);
            return null;
        }).when(taskService).associateComponentsWithTaskAsync(anyLong(), any(), any(), any());
        
        // Configure component service for refreshing using direct stubbing
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<Component> successCallback = invocation.getArgument(1);
            successCallback.accept(testComponent);
            return null;
        }).when(componentService).findByIdAsync(anyLong(), any(), any());
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize with existing component
            viewModel.initExistingComponent(testComponent);
            
            // Load tasks into the view model
            viewModel.getRequiredForTasks().addAll(testTasks);
            
            // Select a task to remove
            Task taskToRemove = testTasks.get(0);
            viewModel.setSelectedTask(taskToRemove);
            
            // Remove task command should be executable
            assertTrue(viewModel.getRemoveTaskCommand().isExecutable());
            
            // Execute remove command
            viewModel.getRemoveTaskCommand().execute();
            
            // Should be loading
            assertTrue(viewModel.isLoading());
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should be marked as dirty
            assertTrue(viewModel.isDirty());
        });
    }
    
    @Test
    public void testRemoveTaskWithoutSelection() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize with existing component
            viewModel.initExistingComponent(testComponent);
            
            // Ensure no task is selected
            viewModel.setSelectedTask(null);
            
            // Remove task command should not be executable
            assertTrue(viewModel.getRemoveTaskCommand().isNotExecutable());
        });
    }
    
    @Test
    public void testTaskSelection() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize with existing component
            viewModel.initExistingComponent(testComponent);
            
            // Initially no selection
            assertNull(viewModel.getSelectedTask());
            
            // Select a task
            Task selectedTask = testTasks.get(0);
            viewModel.setSelectedTask(selectedTask);
            
            // Verify selection
            assertEquals(selectedTask, viewModel.getSelectedTask());
            
            // Remove command should now be executable
            assertTrue(viewModel.getRemoveTaskCommand().isExecutable());
        });
    }
    
    @Test
    public void testErrorHandlingDuringSave() {
        // Configure mock service to return an error using direct stubbing
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<Throwable> errorCallback = invocation.getArgument(2);
            errorCallback.accept(new RuntimeException("Save error"));
            return null;
        }).when(componentService).saveAsync(any(), any(), any());
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new component
            viewModel.initNewComponent();
            viewModel.setName("Test Component");
            
            // Execute save command
            viewModel.getSaveCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should have error message
            assertNotNull(viewModel.getErrorMessage());
            assertTrue(viewModel.getErrorMessage().contains("Failed to save"));
            
            // Should no longer be loading
            assertFalse(viewModel.isLoading());
        });
    }
    
    @Test
    public void testErrorHandlingDuringTaskLoad() {
        // Configure mock service to return an error using direct stubbing
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<Throwable> errorCallback = invocation.getArgument(2);
            errorCallback.accept(new RuntimeException("Load error"));
            return null;
        }).when(componentService).findByIdAsync(anyLong(), any(), any());
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize with existing component
            viewModel.initExistingComponent(testComponent);
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should have error message
            assertNotNull(viewModel.getErrorMessage());
            assertTrue(viewModel.getErrorMessage().contains("Failed to load"));
            
            // Should no longer be loading
            assertFalse(viewModel.isLoading());
        });
    }
    
    @Test
    public void testLoadingProperty() {
        // Configure mock service with delayed response using direct stubbing
        doAnswer(invocation -> {
            new Thread(() -> {
                try {
                    Thread.sleep(100);
                    Component componentToSave = invocation.getArgument(0);
                    @SuppressWarnings("unchecked")
                    java.util.function.Consumer<Component> successCallback = invocation.getArgument(1);
                    successCallback.accept(componentToSave);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
            return null;
        }).when(componentService).saveAsync(any(), any(), any());
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initially not loading
            assertFalse(viewModel.isLoading());
            
            // Initialize and trigger save
            viewModel.initNewComponent();
            viewModel.setName("Test Component");
            
            // Execute save to trigger loading
            viewModel.getSaveCommand().execute();
            
            // Should be loading
            assertTrue(viewModel.isLoading());
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should no longer be loading
            assertFalse(viewModel.isLoading());
        });
    }
    
    @Test
    public void testCurrentProjectProperty() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initially no current project
            assertNull(viewModel.getCurrentProject());
            
            // Set current project
            viewModel.setCurrentProject(testProject);
            
            // Verify project was set
            assertEquals(testProject, viewModel.getCurrentProject());
        });
    }
    
    @Test
    public void testSaveCommandExecutability() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new component
            viewModel.initNewComponent();
            
            // Initially not executable (not valid and not dirty)
            assertTrue(viewModel.getSaveCommand().isNotExecutable());
            
            // Set name to make it valid
            viewModel.setName("Test Component");
            
            // Now should be executable (valid and dirty)
            assertTrue(viewModel.getSaveCommand().isExecutable());
            
            // Clear dirty flag
            viewModel.setDirty(false);
            
            // Should not be executable (valid but not dirty)
            assertTrue(viewModel.getSaveCommand().isNotExecutable());
            
            // Make invalid again
            viewModel.setName("");
            
            // Should not be executable (not valid even though dirty)
            assertTrue(viewModel.getSaveCommand().isNotExecutable());
        });
    }
    
    @Test
    public void testAddTaskCommandExecutability() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new component
            viewModel.initNewComponent();
            
            // Add task command should not be executable for new component
            assertTrue(viewModel.getAddTaskCommand().isNotExecutable());
            
            // Initialize with existing component
            viewModel.initExistingComponent(testComponent);
            
            // Add task command should be executable for existing component
            assertTrue(viewModel.getAddTaskCommand().isExecutable());
        });
    }
    
    @Test
    public void testRemoveTaskCommandExecutability() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize with existing component
            viewModel.initExistingComponent(testComponent);
            
            // Initially not executable (no selection)
            assertTrue(viewModel.getRemoveTaskCommand().isNotExecutable());
            
            // Select a task
            viewModel.setSelectedTask(testTasks.get(0));
            
            // Should now be executable
            assertTrue(viewModel.getRemoveTaskCommand().isExecutable());
            
            // Clear selection
            viewModel.setSelectedTask(null);
            
            // Should not be executable again
            assertTrue(viewModel.getRemoveTaskCommand().isNotExecutable());
        });
    }
    
    @Test
    public void testNullComponentHandling() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Should throw exception for null component
            assertThrows(IllegalArgumentException.class, () -> {
                viewModel.initExistingComponent(null);
            });
        });
    }
    
    @Test
    public void testValidationWithEmptyName() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new component
            viewModel.initNewComponent();
            
            // Set empty name
            viewModel.setName("");
            
            // Should not be valid
            assertFalse(viewModel.isValid());
            assertNotNull(viewModel.getErrorMessage());
            assertTrue(viewModel.getErrorMessage().contains("name"));
            
            // Set whitespace-only name
            viewModel.setName("   ");
            
            // Should still not be valid
            assertFalse(viewModel.isValid());
            assertNotNull(viewModel.getErrorMessage());
            assertTrue(viewModel.getErrorMessage().contains("name"));
        });
    }
    
    @Test
    public void testAsyncServiceCasting() {
        // Verify that the services are available (no casting needed anymore)
        assertNotNull(componentService);
        assertNotNull(taskService);
        
        // The services should have async methods available via reflection
        // This test verifies the service injection works correctly
        assertTrue(componentService instanceof org.frcpm.services.ComponentService);
        assertTrue(taskService instanceof org.frcpm.services.TaskService);
    }
    
    @Test
    public void testDispose() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize the view model
            viewModel.initExistingComponent(testComponent);
            
            // Load some tasks
            viewModel.getRequiredForTasks().addAll(testTasks);
            
            // Verify tasks were loaded
            assertFalse(viewModel.getRequiredForTasks().isEmpty());
            
            // Call dispose
            viewModel.dispose();
            
            // Verify tasks were cleared
            assertTrue(viewModel.getRequiredForTasks().isEmpty());
        });
    }
    
    @Test
    public void testPropertyGettersAndSetters() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new component
            viewModel.initNewComponent();
            
            // Test all property getters and setters
            String testName = "Test Motor";
            viewModel.setName(testName);
            assertEquals(testName, viewModel.getName());
            
            String testPartNumber = "TEST-001";
            viewModel.setPartNumber(testPartNumber);
            assertEquals(testPartNumber, viewModel.getPartNumber());
            
            String testDescription = "Test description";
            viewModel.setDescription(testDescription);
            assertEquals(testDescription, viewModel.getDescription());
            
            LocalDate testExpectedDate = LocalDate.now().plusDays(5);
            viewModel.setExpectedDelivery(testExpectedDate);
            assertEquals(testExpectedDate, viewModel.getExpectedDelivery());
            
            LocalDate testActualDate = LocalDate.now();
            viewModel.setActualDelivery(testActualDate);
            assertEquals(testActualDate, viewModel.getActualDelivery());
            
            viewModel.setDelivered(true);
            assertTrue(viewModel.isDelivered());
            
            viewModel.setCurrentProject(testProject);
            assertEquals(testProject, viewModel.getCurrentProject());
        });
    }
    
    @Test
    public void testValidationWithDeliveredButNoActualDate() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new component
            viewModel.initNewComponent();
            viewModel.setName("Test Component");
            
            // Mark as delivered without setting actual delivery date
            viewModel.setActualDelivery(null); // Explicitly clear it
            viewModel.setDelivered(true);
            
            // Should be invalid
            assertFalse(viewModel.isValid());
            assertNotNull(viewModel.getErrorMessage());
            assertTrue(viewModel.getErrorMessage().contains("delivery date"));
        });
    }
}