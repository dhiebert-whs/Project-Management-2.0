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
        
        // Run on JavaFX threa