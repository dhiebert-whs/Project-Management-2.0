// src/test/java/org/frcpm/mvvm/viewmodels/TaskListMvvmViewModelTest.java
package org.frcpm.mvvm.viewmodels;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.frcpm.di.TestModule;
import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.services.TaskService;
import org.frcpm.services.impl.TaskServiceAsyncImpl;
import org.frcpm.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the TaskListMvvmViewModel class.
 */
public class TaskListMvvmViewModelTest {
    
    private TaskService taskService;
    private TaskServiceAsyncImpl taskServiceAsync;
    
    private Project testProject;
    private List<Task> testTasks;
    private TaskListMvvmViewModel viewModel;
    
    @BeforeEach
    public void setUp() throws Exception {
        // Initialize TestModule
        TestModule.initialize();
        
        // Create test data
        setupTestData();
        
        // Get service references from TestModule
        taskService = TestModule.getService(TaskService.class);
        taskServiceAsync = (TaskServiceAsyncImpl) taskService;
        
        // Initialize JavaFX toolkit if needed
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
        
        // Create the view model on the JavaFX thread
        TestUtils.runOnFxThreadAndWait(() -> {
            viewModel = new TaskListMvvmViewModel(taskService);
        });
    }
    
    private void setupTestData() {
        // Create test project
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");
        testProject.setStartDate(LocalDate.now().minusDays(30));
        testProject.setGoalEndDate(LocalDate.now().plusDays(30));
        
        // Create test tasks
        testTasks = new ArrayList<>();
        
        Task task1 = new Task();
        task1.setId(1L);
        task1.setTitle("Design Robot Frame");
        task1.setProject(testProject);
        task1.setCompleted(false);
        task1.setProgress(25);
        task1.setStartDate(LocalDate.now().minusDays(5));
        task1.setEndDate(LocalDate.now().plusDays(10));
        task1.setPriority(Task.Priority.HIGH);
        testTasks.add(task1);
        
        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Program Drive System");
        task2.setProject(testProject);
        task2.setCompleted(true);
        task2.setProgress(100);
        task2.setStartDate(LocalDate.now().minusDays(15));
        task2.setEndDate(LocalDate.now().minusDays(5));
        task2.setPriority(Task.Priority.MEDIUM);
        testTasks.add(task2);
        
        Task task3 = new Task();
        task3.setId(3L);
        task3.setTitle("Test Autonomous Mode");
        task3.setProject(testProject);
        task3.setCompleted(false);
        task3.setProgress(0);
        task3.setStartDate(LocalDate.now().plusDays(5));
        task3.setEndDate(LocalDate.now().plusDays(20));
        task3.setPriority(Task.Priority.CRITICAL);
        testTasks.add(task3);
        
        Task task4 = new Task();
        task4.setId(4L);
        task4.setTitle("Build Intake Mechanism");
        task4.setProject(testProject);
        task4.setCompleted(false);
        task4.setProgress(75);
        task4.setStartDate(LocalDate.now().minusDays(10));
        task4.setEndDate(LocalDate.now().plusDays(5));
        task4.setPriority(Task.Priority.HIGH);
        testTasks.add(task4);
    }
    
    @Test
    public void testInitialState() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Check that the viewModel is not null
            assertNotNull(viewModel, "ViewModel should not be null");
            
            // Check initial values
            assertTrue(viewModel.getTasks().isEmpty());
            assertNull(viewModel.getSelectedTask());
            assertNull(viewModel.getCurrentProject());
            assertFalse(viewModel.isLoading());
            
            // Verify commands exist
            assertNotNull(viewModel.getLoadTasksCommand());
            assertNotNull(viewModel.getNewTaskCommand());
            assertNotNull(viewModel.getEditTaskCommand());
            assertNotNull(viewModel.getDeleteTaskCommand());
            assertNotNull(viewModel.getRefreshTasksCommand());
            
            // Check command executability
            assertTrue(viewModel.getLoadTasksCommand().isExecutable());
            assertTrue(viewModel.getRefreshTasksCommand().isExecutable());
            assertTrue(viewModel.getDeleteTaskCommand().isNotExecutable()); // No selection
            assertTrue(viewModel.getEditTaskCommand().isNotExecutable()); // No selection
            assertTrue(viewModel.getNewTaskCommand().isNotExecutable()); // No project
        });
    }
    
    @Test
    public void testInitWithProject() {
        // Configure the mock service to return test data
        doAnswer(invocation -> {
            // Get the success callback and call it with test data
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<List<Task>> successCallback = 
                invocation.getArgument(1);
            successCallback.accept(testTasks);
            return null;
        }).when(taskServiceAsync).findByProjectAsync(any(), any(), any());
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize with project
            viewModel.initWithProject(testProject);
            
            // Verify project was set
            assertEquals(testProject, viewModel.getCurrentProject());
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify tasks were loaded
            assertFalse(viewModel.getTasks().isEmpty());
            assertEquals(4, viewModel.getTasks().size());
            assertEquals("Design Robot Frame", viewModel.getTasks().get(0).getTitle());
            assertEquals("Program Drive System", viewModel.getTasks().get(1).getTitle());
            assertEquals("Test Autonomous Mode", viewModel.getTasks().get(2).getTitle());
            assertEquals("Build Intake Mechanism", viewModel.getTasks().get(3).getTitle());
            
            // New task command should now be executable
            assertTrue(viewModel.getNewTaskCommand().isExecutable());
        });
    }
    
    @Test
    public void testLoadTasksCommand() {
        // Configure the mock service to return test data
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<List<Task>> successCallback = 
                invocation.getArgument(1);
            successCallback.accept(testTasks);
            return null;
        }).when(taskServiceAsync).findByProjectAsync(any(), any(), any());
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set project first
            viewModel.setCurrentProject(testProject);
            
            // Execute load command
            viewModel.getLoadTasksCommand().execute();
            
            // Verify loading state
            assertTrue(viewModel.isLoading());
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify tasks were loaded
            assertFalse(viewModel.getTasks().isEmpty());
            assertEquals(4, viewModel.getTasks().size());
            
            // Should no longer be loading
            assertFalse(viewModel.isLoading());
            
            // Error message should be cleared
            assertNull(viewModel.getErrorMessage());
        });
    }
    
    @Test
    public void testRefreshTasksCommand() {
        // Configure the mock service to return test data
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<List<Task>> successCallback = 
                invocation.getArgument(1);
            successCallback.accept(testTasks);
            return null;
        }).when(taskServiceAsync).findByProjectAsync(any(), any(), any());
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set project first
            viewModel.setCurrentProject(testProject);
            
            // Execute refresh command
            viewModel.getRefreshTasksCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify tasks were loaded
            assertFalse(viewModel.getTasks().isEmpty());
            assertEquals(4, viewModel.getTasks().size());
        });
    }
    
    @Test
    public void testTaskSelection() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initially no selection
            assertNull(viewModel.getSelectedTask());
            
            // Commands requiring selection should not be executable
            assertTrue(viewModel.getEditTaskCommand().isNotExecutable());
            assertTrue(viewModel.getDeleteTaskCommand().isNotExecutable());
            
            // Select a task
            Task selectedTask = testTasks.get(0);
            viewModel.setSelectedTask(selectedTask);
            
            // Verify selection
            assertEquals(selectedTask, viewModel.getSelectedTask());
            
            // Commands requiring selection should now be executable
            assertTrue(viewModel.getEditTaskCommand().isExecutable());
            assertTrue(viewModel.getDeleteTaskCommand().isExecutable());
        });
    }
    
    @Test
    public void testNewTaskCommand() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initially not executable (no project)
            assertTrue(viewModel.getNewTaskCommand().isNotExecutable());
            
            // Set project
            viewModel.setCurrentProject(testProject);
            
            // Should now be executable
            assertTrue(viewModel.getNewTaskCommand().isExecutable());
            
            // Execute command
            viewModel.getNewTaskCommand().execute();
            
            // This command is mainly for the view to handle
            // Just verify it executed without error
        });
    }
    
    @Test
    public void testEditTaskCommand() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initially not executable (no selection)
            assertTrue(viewModel.getEditTaskCommand().isNotExecutable());
            
            // Select a task
            viewModel.setSelectedTask(testTasks.get(0));
            
            // Should now be executable
            assertTrue(viewModel.getEditTaskCommand().isExecutable());
            
            // Execute command
            viewModel.getEditTaskCommand().execute();
            
            // This command is mainly for the view to handle
            // Just verify it executed without error
        });
    }
    
    @Test
    public void testDeleteTaskCommand() {
        // Configure the mock service for successful deletion
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<Boolean> successCallback = 
                invocation.getArgument(1);
            successCallback.accept(true);
            return null;
        }).when(taskServiceAsync).deleteByIdAsync(anyLong(), any(), any());
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Add tasks to the list
            viewModel.getTasks().addAll(testTasks);
            
            // Select a task
            Task taskToDelete = testTasks.get(0);
            viewModel.setSelectedTask(taskToDelete);
            
            // Should be executable
            assertTrue(viewModel.getDeleteTaskCommand().isExecutable());
            
            // Execute delete command
            viewModel.getDeleteTaskCommand().execute();
            
            // Verify loading state
            assertTrue(viewModel.isLoading());
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify task was removed from list
            assertFalse(viewModel.getTasks().contains(taskToDelete));
            assertEquals(3, viewModel.getTasks().size());
            
            // Selection should be cleared
            assertNull(viewModel.getSelectedTask());
            
            // Should no longer be loading
            assertFalse(viewModel.isLoading());
            
            // Error message should be cleared
            assertNull(viewModel.getErrorMessage());
        });
    }
    
    @Test
    public void testDeleteTaskCommandFailure() {
        // Configure the mock service for failed deletion
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<Boolean> successCallback = 
                invocation.getArgument(1);
            successCallback.accept(false);
            return null;
        }).when(taskServiceAsync).deleteByIdAsync(anyLong(), any(), any());
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Add tasks to the list
            viewModel.getTasks().addAll(testTasks);
            
            // Select a task
            Task taskToDelete = testTasks.get(0);
            viewModel.setSelectedTask(taskToDelete);
            
            // Execute delete command
            viewModel.getDeleteTaskCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Task should still be in the list (deletion failed)
            assertTrue(viewModel.getTasks().contains(taskToDelete));
            assertEquals(4, viewModel.getTasks().size());
            
            // Should have error message
            assertNotNull(viewModel.getErrorMessage());
            assertTrue(viewModel.getErrorMessage().contains("Failed to delete"));
        });
    }
    
    @Test
    public void testDeleteTaskWithNullSelection() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Ensure no selection
            viewModel.setSelectedTask(null);
            
            // Command should not be executable
            assertTrue(viewModel.getDeleteTaskCommand().isNotExecutable());
        });
    }
    
    @Test
    public void testDeleteTaskWithNullId() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Create task without ID
            Task taskWithoutId = new Task();
            taskWithoutId.setTitle("No ID Task");
            
            // Add to list and select
            viewModel.getTasks().add(taskWithoutId);
            viewModel.setSelectedTask(taskWithoutId);
            
            // Execute delete command
            viewModel.getDeleteTaskCommand().execute();
            
            // Should handle gracefully and not call service
            verify(taskServiceAsync, never()).deleteByIdAsync(any(), any(), any());
        });
    }
    
    @Test
    public void testErrorHandlingDuringLoad() {
        // Configure the mock service to return an error
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<Throwable> errorCallback = 
                invocation.getArgument(2);
            errorCallback.accept(new RuntimeException("Service error"));
            return null;
        }).when(taskServiceAsync).findByProjectAsync(any(), any(), any());
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set project first
            viewModel.setCurrentProject(testProject);
            
            // Execute load command
            viewModel.getLoadTasksCommand().execute();
            
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
            
            // Tasks list should remain empty
            assertTrue(viewModel.getTasks().isEmpty());
        });
    }
    
    @Test
    public void testErrorHandlingDuringDelete() {
        // Configure the mock service to return an error
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<Throwable> errorCallback = 
                invocation.getArgument(2);
            errorCallback.accept(new RuntimeException("Delete error"));
            return null;
        }).when(taskServiceAsync).deleteByIdAsync(anyLong(), any(), any());
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Add tasks to the list
            viewModel.getTasks().addAll(testTasks);
            
            // Select a task
            Task taskToDelete = testTasks.get(0);
            viewModel.setSelectedTask(taskToDelete);
            
            // Execute delete command
            viewModel.getDeleteTaskCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should have error message
            assertNotNull(viewModel.getErrorMessage());
            assertTrue(viewModel.getErrorMessage().contains("Failed to delete"));
            
            // Should no longer be loading
            assertFalse(viewModel.isLoading());
            
            // Task should still be in the list
            assertTrue(viewModel.getTasks().contains(taskToDelete));
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
            
            // New task command should now be executable
            assertTrue(viewModel.getNewTaskCommand().isExecutable());
        });
    }
    
    @Test
    public void testLoadingProperty() {
        // Configure the mock service with a delay to test loading state
        doAnswer(invocation -> {
            // Simulate async delay
            new Thread(() -> {
                try {
                    Thread.sleep(100);
                    @SuppressWarnings("unchecked")
                    java.util.function.Consumer<List<Task>> successCallback = 
                        invocation.getArgument(1);
                    successCallback.accept(testTasks);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
            return null;
        }).when(taskServiceAsync).findByProjectAsync(any(), any(), any());
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initially not loading
            assertFalse(viewModel.isLoading());
            
            // Set project first
            viewModel.setCurrentProject(testProject);
            
            // Execute load command
            viewModel.getLoadTasksCommand().execute();
            
            // Should be loading immediately
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
    public void testTasksListManipulation() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initially empty
            assertTrue(viewModel.getTasks().isEmpty());
            
            // Add tasks
            viewModel.getTasks().addAll(testTasks);
            
            // Verify size
            assertEquals(4, viewModel.getTasks().size());
            
            // Verify specific tasks
            assertTrue(viewModel.getTasks().contains(testTasks.get(0)));
            assertTrue(viewModel.getTasks().contains(testTasks.get(1)));
            assertTrue(viewModel.getTasks().contains(testTasks.get(2)));
            assertTrue(viewModel.getTasks().contains(testTasks.get(3)));
            
            // Clear list
            viewModel.getTasks().clear();
            
            // Should be empty again
            assertTrue(viewModel.getTasks().isEmpty());
        });
    }
    
    @Test
    public void testCommandExecutabilityWithoutProject() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // No project set
            assertNull(viewModel.getCurrentProject());
            
            // Commands that don't require project should be executable
            assertTrue(viewModel.getLoadTasksCommand().isExecutable());
            assertTrue(viewModel.getRefreshTasksCommand().isExecutable());
            
            // New task command requires project
            assertTrue(viewModel.getNewTaskCommand().isNotExecutable());
            
            // Commands that require selection should not be executable
            assertTrue(viewModel.getEditTaskCommand().isNotExecutable());
            assertTrue(viewModel.getDeleteTaskCommand().isNotExecutable());
        });
    }
    
    @Test
    public void testCommandExecutabilityWithProjectAndSelection() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set project and select a task
            viewModel.setCurrentProject(testProject);
            viewModel.setSelectedTask(testTasks.get(0));
            
            // All commands should be executable
            assertTrue(viewModel.getLoadTasksCommand().isExecutable());
            assertTrue(viewModel.getNewTaskCommand().isExecutable());
            assertTrue(viewModel.getRefreshTasksCommand().isExecutable());
            assertTrue(viewModel.getEditTaskCommand().isExecutable());
            assertTrue(viewModel.getDeleteTaskCommand().isExecutable());
        });
    }
    
    @Test
    public void testMultipleLoadOperations() {
        // Configure the mock service to return test data
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<List<Task>> successCallback = 
                invocation.getArgument(1);
            successCallback.accept(testTasks);
            return null;
        }).when(taskServiceAsync).findByProjectAsync(any(), any(), any());
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set project first
            viewModel.setCurrentProject(testProject);
            
            // Execute load command multiple times
            viewModel.getLoadTasksCommand().execute();
            viewModel.getRefreshTasksCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should still have correct data
            assertEquals(4, viewModel.getTasks().size());
            
            // Verify service was called multiple times
            verify(taskServiceAsync, atLeast(2)).findByProjectAsync(any(), any(), any());
        });
    }
    
    @Test
    public void testTaskSelectionClearing() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Select a task
            viewModel.setSelectedTask(testTasks.get(0));
            assertNotNull(viewModel.getSelectedTask());
            
            // Clear selection
            viewModel.setSelectedTask(null);
            assertNull(viewModel.getSelectedTask());
            
            // Commands requiring selection should not be executable
            assertTrue(viewModel.getEditTaskCommand().isNotExecutable());
            assertTrue(viewModel.getDeleteTaskCommand().isNotExecutable());
        });
    }
    
    @Test
    public void testTaskPriorityHandling() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Add tasks to list
            viewModel.getTasks().addAll(testTasks);
            
            // Verify tasks with different priorities exist
            boolean foundHigh = false;
            boolean foundMedium = false;
            boolean foundCritical = false;
            
            for (Task task : viewModel.getTasks()) {
                if (task.getPriority() == Task.Priority.HIGH) foundHigh = true;
                if (task.getPriority() == Task.Priority.MEDIUM) foundMedium = true;
                if (task.getPriority() == Task.Priority.CRITICAL) foundCritical = true;
            }
            
            assertTrue(foundHigh);
            assertTrue(foundMedium);
            assertTrue(foundCritical);
        });
    }
    
    @Test
    public void testTaskCompletionStatus() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Add tasks to list
            viewModel.getTasks().addAll(testTasks);
            
            // Verify we have both completed and incomplete tasks
            boolean foundCompleted = false;
            boolean foundIncomplete = false;
            
            for (Task task : viewModel.getTasks()) {
                if (task.isCompleted()) foundCompleted = true;
                else foundIncomplete = true;
            }
            
            assertTrue(foundCompleted);
            assertTrue(foundIncomplete);
        });
    }
    
    @Test
    public void testAsyncServiceCasting() {
        // Verify that the service can be cast to our async implementation
        assertNotNull(taskServiceAsync);
        assertTrue(taskServiceAsync instanceof TaskServiceAsyncImpl);
        assertSame(taskService, taskServiceAsync);
    }
    
    @Test
    public void testLoadTasksWithoutProject() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Ensure no project is set
            viewModel.setCurrentProject(null);
            
            // Execute load command
            viewModel.getLoadTasksCommand().execute();
            
            // Should not call service since no project
            verify(taskServiceAsync, never()).findByProjectAsync(any(), any(), any());
            
            // Tasks should remain empty
            assertTrue(viewModel.getTasks().isEmpty());
        });
    }
}