// src/test/java/org/frcpm/mvvm/viewmodels/DashboardMvvmViewModelTest.java
package org.frcpm.mvvm.viewmodels;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.frcpm.di.TestModule;
import org.frcpm.models.Meeting;
import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.services.MeetingService;
import org.frcpm.services.MilestoneService;
import org.frcpm.services.TaskService;
import org.frcpm.services.impl.TestableMeetingServiceAsyncImpl;
import org.frcpm.services.impl.TestableMilestoneServiceAsyncImpl;
import org.frcpm.services.impl.TestableTaskServiceAsyncImpl;
import org.frcpm.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the DashboardMvvmViewModel class.
 * FIXED: Uses proper mock pattern instead of invalid casting.
 */
public class DashboardMvvmViewModelTest {
    
    private TaskService taskService;
    private MilestoneService milestoneService;
    private MeetingService meetingService;
    
    private Project testProject;
    private List<Task> testTasks;
    private List<Milestone> testMilestones;
    private List<Meeting> testMeetings;
    private DashboardMvvmViewModel viewModel;
    
    @BeforeEach
    public void setUp() throws Exception {
        // Initialize TestModule first
        TestModule.initialize();
        
        // Create test data
        setupTestData();
        
        // Create testable async implementations (NOT pure mocks)
        // These have the async methods that DashboardMvvmViewModel needs
        TestableTaskServiceAsyncImpl mockTaskService = mock(TestableTaskServiceAsyncImpl.class);
        TestableMilestoneServiceAsyncImpl mockMilestoneService = mock(TestableMilestoneServiceAsyncImpl.class);
        TestableMeetingServiceAsyncImpl mockMeetingService = mock(TestableMeetingServiceAsyncImpl.class);
        
        // Configure mock behavior for async methods
        // Task service async methods
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<List<Task>> callback = invocation.getArgument(1);
            callback.accept(testTasks);
            return null;
        }).when(mockTaskService).findByProjectAsync(any(), any(), any());
        
        // Milestone service async methods
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<List<Milestone>> callback = invocation.getArgument(1);
            callback.accept(testMilestones);
            return null;
        }).when(mockMilestoneService).findByProjectAsync(any(), any(), any());
        
        // Meeting service async methods
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<List<Meeting>> callback = invocation.getArgument(2);
            callback.accept(testMeetings);
            return null;
        }).when(mockMeetingService).getUpcomingMeetingsAsync(anyLong(), anyInt(), any(), any());
        
        // Register mocks with TestModule
        TestModule.setService(TaskService.class, mockTaskService);
        TestModule.setService(MilestoneService.class, mockMilestoneService);
        TestModule.setService(MeetingService.class, mockMeetingService);
        
        // Get service references from TestModule (now returns mocks)
        taskService = TestModule.getService(TaskService.class);
        milestoneService = TestModule.getService(MilestoneService.class);
        meetingService = TestModule.getService(MeetingService.class);
        
        // Initialize JavaFX toolkit if needed
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
        
        // Create the view model on the JavaFX thread
        TestUtils.runOnFxThreadAndWait(() -> {
            viewModel = new DashboardMvvmViewModel(taskService, milestoneService, meetingService);
        });
    }
    
    private void setupTestData() {
        // Create test project
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");
        testProject.setStartDate(LocalDate.now().minusDays(30));
        testProject.setGoalEndDate(LocalDate.now().plusDays(30));
        testProject.setHardDeadline(LocalDate.now().plusDays(45));
        
        // Create test tasks
        testTasks = new ArrayList<>();
        
        Task task1 = new Task();
        task1.setId(1L);
        task1.setTitle("Task 1");
        task1.setProject(testProject);
        task1.setCompleted(false);
        task1.setProgress(50);
        task1.setEndDate(LocalDate.now().plusDays(5));
        testTasks.add(task1);
        
        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Task 2");
        task2.setProject(testProject);
        task2.setCompleted(true);
        task2.setProgress(100);
        task2.setEndDate(LocalDate.now().plusDays(10));
        testTasks.add(task2);
        
        Task task3 = new Task();
        task3.setId(3L);
        task3.setTitle("Task 3");
        task3.setProject(testProject);
        task3.setCompleted(false);
        task3.setProgress(0);
        task3.setEndDate(LocalDate.now().plusDays(15));
        testTasks.add(task3);
        
        // Create test milestones
        testMilestones = new ArrayList<>();
        
        Milestone milestone1 = new Milestone();
        milestone1.setId(1L);
        milestone1.setProject(testProject);
        milestone1.setDate(LocalDate.now().plusDays(7));
        testMilestones.add(milestone1);
        
        Milestone milestone2 = new Milestone();
        milestone2.setId(2L);
        milestone2.setProject(testProject);
        milestone2.setDate(LocalDate.now().plusDays(20));
        testMilestones.add(milestone2);
        
        // Create test meetings
        testMeetings = new ArrayList<>();
        
        Meeting meeting1 = new Meeting();
        meeting1.setId(1L);
        meeting1.setDate(LocalDate.now().plusDays(3));
        testMeetings.add(meeting1);
        
        Meeting meeting2 = new Meeting();
        meeting2.setId(2L);
        meeting2.setDate(LocalDate.now().plusDays(8));
        testMeetings.add(meeting2);
    }
    
    @Test
    public void testInitialState() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Check that the viewModel is not null
            assertNotNull(viewModel, "ViewModel should not be null");
            
            // Check initial values
            assertNull(viewModel.getProject());
            assertEquals("", viewModel.getProjectName());
            assertNull(viewModel.getStartDate());
            assertNull(viewModel.getGoalEndDate());
            assertNull(viewModel.getHardDeadline());
            assertEquals(0.0, viewModel.getProgressPercentage());
            assertEquals(0, viewModel.getDaysRemaining());
            assertFalse(viewModel.isLoading());
            
            // Check collections are empty
            assertTrue(viewModel.getUpcomingTasks().isEmpty());
            assertTrue(viewModel.getUpcomingMilestones().isEmpty());
            assertTrue(viewModel.getUpcomingMeetings().isEmpty());
            
            // Check selections are null
            assertNull(viewModel.getSelectedTask());
            assertNull(viewModel.getSelectedMilestone());
            assertNull(viewModel.getSelectedMeeting());
            
            // Verify commands exist
            assertNotNull(viewModel.getRefreshCommand());
            
            // Check chart data exists
            assertNotNull(viewModel.getTaskStatusChartData());
            assertNotNull(viewModel.getProgressChartData());
            assertFalse(viewModel.getTaskStatusChartData().isEmpty());
            assertFalse(viewModel.getProgressChartData().isEmpty());
        });
    }
    
    @Test
    public void testSetProject() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set project
            viewModel.setProject(testProject);
            
            // Verify project info was updated
            assertEquals(testProject, viewModel.getProject());
            assertEquals("Test Project", viewModel.getProjectName());
            assertEquals(testProject.getStartDate(), viewModel.getStartDate());
            assertEquals(testProject.getGoalEndDate(), viewModel.getGoalEndDate());
            assertEquals(testProject.getHardDeadline(), viewModel.getHardDeadline());
            
            // Verify days remaining calculation
            assertTrue(viewModel.getDaysRemaining() > 0);
            
            // Let async operations complete
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify data was loaded
            assertFalse(viewModel.getUpcomingTasks().isEmpty());
            assertFalse(viewModel.getUpcomingMilestones().isEmpty());
            assertFalse(viewModel.getUpcomingMeetings().isEmpty());
            
            // Should no longer be loading
            assertFalse(viewModel.isLoading());
        });
    }
    
    @Test
    public void testRefreshCommand() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set project first
            viewModel.setProject(testProject);
            
            // Execute refresh command
            assertTrue(viewModel.getRefreshCommand().isExecutable());
            viewModel.getRefreshCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify services were called
            verify((TestableTaskServiceAsyncImpl)taskService, atLeastOnce()).findByProjectAsync(any(), any(), any());
            verify((TestableMilestoneServiceAsyncImpl)milestoneService, atLeastOnce()).findByProjectAsync(any(), any(), any());
            verify((TestableMeetingServiceAsyncImpl)meetingService, atLeastOnce()).getUpcomingMeetingsAsync(anyLong(), anyInt(), any(), any());
        });
    }
    
    @Test
    public void testUpcomingTasksFiltering() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set project to trigger loading
            viewModel.setProject(testProject);
            
            // Let async operations complete
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify only incomplete tasks with future end dates are shown
            // Task 1: incomplete, future end date - should be included
            // Task 2: completed - should be excluded
            // Task 3: incomplete, future end date - should be included
            assertEquals(2, viewModel.getUpcomingTasks().size());
            
            boolean foundTask1 = false;
            boolean foundTask3 = false;
            for (Task task : viewModel.getUpcomingTasks()) {
                if (task.getId().equals(1L)) foundTask1 = true;
                if (task.getId().equals(3L)) foundTask3 = true;
                // Should not find completed task
                assertNotEquals(Long.valueOf(2L), task.getId());
            }
            assertTrue(foundTask1);
            assertTrue(foundTask3);
        });
    }
    
    @Test
    public void testTaskSelection() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initially no selection
            assertNull(viewModel.getSelectedTask());
            
            // Select a task
            Task selectedTask = testTasks.get(0);
            viewModel.setSelectedTask(selectedTask);
            
            // Verify selection
            assertEquals(selectedTask, viewModel.getSelectedTask());
        });
    }
    
    @Test
    public void testMilestoneSelection() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initially no selection
            assertNull(viewModel.getSelectedMilestone());
            
            // Select a milestone
            Milestone selectedMilestone = testMilestones.get(0);
            viewModel.setSelectedMilestone(selectedMilestone);
            
            // Verify selection
            assertEquals(selectedMilestone, viewModel.getSelectedMilestone());
        });
    }
    
    @Test
    public void testMeetingSelection() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initially no selection
            assertNull(viewModel.getSelectedMeeting());
            
            // Select a meeting
            Meeting selectedMeeting = testMeetings.get(0);
            viewModel.setSelectedMeeting(selectedMeeting);
            
            // Verify selection
            assertEquals(selectedMeeting, viewModel.getSelectedMeeting());
        });
    }
    
    @Test
    public void testProgressCalculation() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set project to trigger progress calculation
            viewModel.setProject(testProject);
            
            // Let async operations complete
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Progress should be average of all tasks: (50 + 100 + 0) / 3 = 50
            assertEquals(50.0, viewModel.getProgressPercentage(), 0.1);
        });
    }
    
    @Test
    public void testTaskStatusChartData() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Verify chart data structure exists
            assertNotNull(viewModel.getTaskStatusChartData());
            assertEquals(3, viewModel.getTaskStatusChartData().size());
            
            // Verify chart data labels
            boolean foundNotStarted = false;
            boolean foundInProgress = false;
            boolean foundCompleted = false;
            
            for (javafx.scene.chart.PieChart.Data data : viewModel.getTaskStatusChartData()) {
                if ("Not Started".equals(data.getName())) foundNotStarted = true;
                if ("In Progress".equals(data.getName())) foundInProgress = true;
                if ("Completed".equals(data.getName())) foundCompleted = true;
            }
            
            assertTrue(foundNotStarted);
            assertTrue(foundInProgress);
            assertTrue(foundCompleted);
        });
    }
    
    @Test
    public void testProgressChartData() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Verify chart data structure exists
            assertNotNull(viewModel.getProgressChartData());
            assertFalse(viewModel.getProgressChartData().isEmpty());
            
            // Should have one series for progress
            assertEquals(1, viewModel.getProgressChartData().size());
            assertEquals("Progress", viewModel.getProgressChartData().get(0).getName());
        });
    }
    
    @Test
    public void testErrorHandlingDuringLoad() {
        // Create fresh mock with error behavior
        TestableTaskServiceAsyncImpl errorTaskService = mock(TestableTaskServiceAsyncImpl.class);
        
        // Configure task service to return an error
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<Throwable> errorCallback = invocation.getArgument(2);
            errorCallback.accept(new RuntimeException("Service error"));
            return null;
        }).when(errorTaskService).findByProjectAsync(any(), any(), any());
        
        // Register error service
        TestModule.setService(TaskService.class, errorTaskService);
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Create new viewModel with error service
            DashboardMvvmViewModel errorViewModel = new DashboardMvvmViewModel(
                TestModule.getService(TaskService.class), 
                milestoneService, 
                meetingService
            );
            
            // Set project to trigger loading
            errorViewModel.setProject(testProject);
            
            // Let async operations complete
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should have error message
            assertNotNull(errorViewModel.getErrorMessage());
            assertTrue(errorViewModel.getErrorMessage().contains("tasks"));
            
            // Should no longer be loading
            assertFalse(errorViewModel.isLoading());
            
            // Upcoming tasks should be empty due to error
            assertTrue(errorViewModel.getUpcomingTasks().isEmpty());
        });
    }
    
    @Test
    public void testDaysRemainingCalculation() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Create project with specific dates
            Project projectWithDates = new Project();
            projectWithDates.setId(2L);
            projectWithDates.setName("Dated Project");
            projectWithDates.setStartDate(LocalDate.now().minusDays(10));
            projectWithDates.setGoalEndDate(LocalDate.now().plusDays(20));
            
            // Set project
            viewModel.setProject(projectWithDates);
            
            // Verify days remaining calculation
            assertEquals(20, viewModel.getDaysRemaining());
            
            // Test with past end date
            Project pastProject = new Project();
            pastProject.setId(3L);
            pastProject.setName("Past Project");
            pastProject.setGoalEndDate(LocalDate.now().minusDays(5));
            
            viewModel.setProject(pastProject);
            
            // Should be 0 for past dates
            assertEquals(0, viewModel.getDaysRemaining());
        });
    }
    
    @Test
    public void testNullProjectHandling() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set valid project first
            viewModel.setProject(testProject);
            assertEquals(testProject, viewModel.getProject());
            
            // Set to null
            viewModel.setProject(null);
            
            // Verify state was cleared
            assertNull(viewModel.getProject());
            assertEquals("", viewModel.getProjectName());
            assertNull(viewModel.getStartDate());
            assertNull(viewModel.getGoalEndDate());
            assertNull(viewModel.getHardDeadline());
            assertEquals(0.0, viewModel.getProgressPercentage());
            assertEquals(0, viewModel.getDaysRemaining());
            
            // Collections should be cleared
            assertTrue(viewModel.getUpcomingTasks().isEmpty());
            assertTrue(viewModel.getUpcomingMilestones().isEmpty());
            assertTrue(viewModel.getUpcomingMeetings().isEmpty());
        });
    }
    
    @Test
    public void testLoadingProperty() {
        // Configure services with delayed responses
        TestableTaskServiceAsyncImpl delayedTaskService = mock(TestableTaskServiceAsyncImpl.class);
        
        doAnswer(invocation -> {
            new Thread(() -> {
                try {
                    Thread.sleep(100);
                    @SuppressWarnings("unchecked")
                    java.util.function.Consumer<List<Task>> successCallback = invocation.getArgument(1);
                    successCallback.accept(testTasks);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
            return null;
        }).when(delayedTaskService).findByProjectAsync(any(), any(), any());
        
        // Register delayed service
        TestModule.setService(TaskService.class, delayedTaskService);
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Create new viewModel with delayed service
            DashboardMvvmViewModel delayedViewModel = new DashboardMvvmViewModel(
                TestModule.getService(TaskService.class), 
                milestoneService, 
                meetingService
            );
            
            // Initially not loading
            assertFalse(delayedViewModel.isLoading());
            
            // Execute refresh command
            delayedViewModel.setProject(testProject);
            delayedViewModel.getRefreshCommand().execute();
            
            // Let operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should no longer be loading
            assertFalse(delayedViewModel.isLoading());
        });
    }
    
    @Test
    public void testDispose() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set up some data
            viewModel.getUpcomingTasks().addAll(testTasks);
            viewModel.getUpcomingMilestones().addAll(testMilestones);
            viewModel.getUpcomingMeetings().addAll(testMeetings);
            
            // Verify data exists
            assertFalse(viewModel.getUpcomingTasks().isEmpty());
            assertFalse(viewModel.getUpcomingMilestones().isEmpty());
            assertFalse(viewModel.getUpcomingMeetings().isEmpty());
            
            // Call dispose
            viewModel.dispose();
            
            // Verify collections were cleared
            assertTrue(viewModel.getUpcomingTasks().isEmpty());
            assertTrue(viewModel.getUpcomingMilestones().isEmpty());
            assertTrue(viewModel.getUpcomingMeetings().isEmpty());
            
            // Verify selections were cleared
            assertNull(viewModel.getSelectedTask());
            assertNull(viewModel.getSelectedMilestone());
            assertNull(viewModel.getSelectedMeeting());
        });
    }
}