// src/test/java/org/frcpm/mvvm/viewmodels/TaskListMvvmViewModelTest.java
package org.frcpm.mvvm.viewmodels;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.frcpm.di.TestModule;
import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.services.TaskService;
import org.frcpm.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the TaskListMvvmViewModel class.
 * FIXED: Now creates mocks properly and registers them with TestModule.
 */
public class TaskListMvvmViewModelTest {
    
    private TaskService taskService;
    
    private Project testProject;
    private List<Task> testTasks;
    private TaskListMvvmViewModel viewModel;
    
    @BeforeEach
    public void setUp() throws Exception {
        // Initialize TestModule
        TestModule.initialize();
        
        // Create test data
        setupTestData();
        
        // FIXED: Create mocks first
        TaskService mockTaskService = mock(TaskService.class);
        
        // Configure mock behavior for sync methods
        when(mockTaskService.findByProject(any())).thenReturn(testTasks);
        when(mockTaskService.deleteById(anyLong())).thenReturn(true);
        
        // Configure mock behavior for async methods
        doAnswer(invocation -> {
            Consumer<List<Task>> callback = invocation.getArgument(1);
            callback.accept(testTasks);
            return null;
        }).when(mockTaskService).findByProjectAsync(any(), any(), any());
        
        doAnswer(invocation -> {
            Consumer<Boolean> callback = invocation.getArgument(1);
            callback.accept(true);
            return null;
        }).when(mockTaskService).deleteByIdAsync(anyLong(), any(), any());
        
        // Register mock with TestModule
        TestModule.setService(TaskService.class, mockTaskService);
        
        // Get service from TestModule (now returns our mock)
        taskService = TestModule.getService(TaskService.class);
        
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
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize with project
            viewModel.initWithProject(testProject);
            
            // Verify project was set
            assertEquals(testProject, viewModel.getCurrentProject());
            
            // Let async operations complete
            try {
                Thread.sleep(100);
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
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set project first
            viewModel.setCurrentProject(testProject);
            
            // Execute load command
            viewModel.getLoadTasksCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(100);
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
    public void testDeleteTaskCommand() {
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
            
            // Let async operations complete
            try {
                Thread.sleep(100);
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
        // Create new mock for failure scenario
        TaskService failureTaskService = mock(TaskService.class);
        
        // Configure mock service for failed deletion
        doAnswer(invocation -> {
            Consumer<Boolean> callback = invocation.getArgument(1);
            callback.accept(false);
            return null;
        }).when(failureTaskService).deleteByIdAsync(anyLong(), any(), any());
        
        // Register failure mock
        TestModule.setService(TaskService.class, failureTaskService);
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Create new viewModel with failure service
            TaskListMvvmViewModel failureViewModel = new TaskListMvvmViewModel(
                TestModule.getService(TaskService.class)
            );
            
            // Add tasks to the list
            failureViewModel.getTasks().addAll(testTasks);
            
            // Select a task
            Task taskToDelete = testTasks.get(0);
            failureViewModel.setSelectedTask(taskToDelete);
            
            // Execute delete command
            failureViewModel.getDeleteTaskCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Task should still be in the list (deletion failed)
            assertTrue(failureViewModel.getTasks().contains(taskToDelete));
            assertEquals(4, failureViewModel.getTasks().size());
            
            // Should have error message
            assertNotNull(failureViewModel.getErrorMessage());
            assertTrue(failureViewModel.getErrorMessage().contains("Failed to delete"));
        });
    }
    
    @Test
    public void testErrorHandlingDuringLoad() {
        // Create new mock for error scenario
        TaskService errorTaskService = mock(TaskService.class);
        
        // Configure mock service to return an error
        doAnswer(invocation -> {
            Consumer<Throwable> errorCallback = invocation.getArgument(2);
            errorCallback.accept(new RuntimeException("Service error"));
            return null;
        }).when(errorTaskService).findByProjectAsync(any(), any(), any());
        
        // Register error mock
        TestModule.setService(TaskService.class, errorTaskService);
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Create new viewModel with error service
            TaskListMvvmViewModel errorViewModel = new TaskListMvvmViewModel(
                TestModule.getService(TaskService.class)
            );
            
            // Set project first
            errorViewModel.setCurrentProject(testProject);
            
            // Execute load command
            errorViewModel.getLoadTasksCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should have error message
            assertNotNull(errorViewModel.getErrorMessage());
            assertTrue(errorViewModel.getErrorMessage().contains("Failed to load"));
            
            // Should no longer be loading
            assertFalse(errorViewModel.isLoading());
            
            // Tasks list should remain empty
            assertTrue(errorViewModel.getTasks().isEmpty());
        });
    }
    
    @Test
    public void testAsyncServiceMocking() {
        // Verify that the service is a mock and async methods work
        assertNotNull(taskService);
        
        // Verify we can stub the async methods without NotAMockException
        doAnswer(invocation -> {
            Consumer<List<Task>> callback = invocation.getArgument(1);
            callback.accept(testTasks);
            return null;
        }).when(taskService).findByProjectAsync(any(), any(), any());
        
        // Test that the stubbing worked
        taskService.findByProjectAsync(testProject, 
            result -> assertEquals(testTasks.size(), result.size()),
            error -> fail("Should not have error"));
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
        // Configure mock with a delay to test loading state
        TaskService delayTaskService = mock(TaskService.class);
        
        doAnswer(invocation -> {
            // Simulate async delay
            new Thread(() -> {
                try {
                    Thread.sleep(50);
                    Consumer<List<Task>> callback = invocation.getArgument(1);
                    callback.accept(testTasks);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
            return null;
        }).when(delayTaskService).findByProjectAsync(any(), any(), any());
        
        // Register delay mock
        TestModule.setService(TaskService.class, delayTaskService);
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Create new viewModel with delay service
            TaskListMvvmViewModel delayViewModel = new TaskListMvvmViewModel(
                TestModule.getService(TaskService.class)
            );
            
            // Initially not loading
            assertFalse(delayViewModel.isLoading());
            
            // Set project first
            delayViewModel.setCurrentProject(testProject);
            
            // Execute load command
            delayViewModel.getLoadTasksCommand().execute();
            
            // Should be loading immediately
            assertTrue(delayViewModel.isLoading());
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should no longer be loading
            assertFalse(delayViewModel.isLoading());
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
    public void testRefreshTasksCommand() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set project first
            viewModel.setCurrentProject(testProject);
            
            // Execute refresh command
            viewModel.getRefreshTasksCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify tasks were loaded
            assertFalse(viewModel.getTasks().isEmpty());
            assertEquals(4, viewModel.getTasks().size());
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
}