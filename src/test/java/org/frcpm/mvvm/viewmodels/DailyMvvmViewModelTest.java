// src/test/java/org/frcpm/mvvm/viewmodels/DailyMvvmViewModelTest.java
package org.frcpm.mvvm.viewmodels;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.frcpm.di.TestModule;
import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.models.TaskStatus;
import org.frcpm.models.TeamMember;
import org.frcpm.services.MeetingService;
import org.frcpm.services.TaskService;
import org.frcpm.services.impl.TestableMeetingServiceAsyncImpl;
import org.frcpm.services.impl.TestableTaskServiceAsyncImpl;
import org.frcpm.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the DailyMvvmViewModel class.
 * FIXED: Uses proven methodology - mocks testable service implementations directly.
 */
public class DailyMvvmViewModelTest {
    
    private TaskService taskService;
    private MeetingService meetingService;
    
    private Project testProject;
    private List<Task> testTasks;
    private List<Meeting> testMeetings;
    private TeamMember testMember;
    private DailyMvvmViewModel viewModel;
    
    @BeforeEach
    public void setUp() throws Exception {
        // Initialize TestModule
        TestModule.initialize();
        
        // Create test data
        setupTestData();
        
        // Create mock services - CRITICAL: Mock the actual testable implementations
        TestableTaskServiceAsyncImpl mockTaskService = mock(TestableTaskServiceAsyncImpl.class);
        TestableMeetingServiceAsyncImpl mockMeetingService = mock(TestableMeetingServiceAsyncImpl.class);
        
        // Configure task service mocks
        doAnswer(invocation -> {
            LocalDate date = invocation.getArgument(0);
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<List<Task>> successCallback = 
                invocation.getArgument(1);
            
            // Filter tasks by due date
            List<Task> tasksForDate = testTasks.stream()
                .filter(task -> task.getDueDate() != null && task.getDueDate().equals(date))
                .toList();
            
            successCallback.accept(tasksForDate);
            return null;
        }).when(mockTaskService).findTasksDueTodayAsync(any(), any(), any());
        
        doAnswer(invocation -> {
            TeamMember member = invocation.getArgument(0);
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<List<Task>> successCallback = 
                invocation.getArgument(1);
            
            // Filter tasks by assigned member
            List<Task> memberTasks = testTasks.stream()
                .filter(task -> task.getAssignedMembers() != null && 
                               task.getAssignedMembers().contains(member))
                .toList();
            
            successCallback.accept(memberTasks);
            return null;
        }).when(mockTaskService).findByAssignedMemberAsync(any(), any(), any());
        
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<List<Task>> successCallback = 
                invocation.getArgument(0);
            successCallback.accept(testTasks);
            return null;
        }).when(mockTaskService).findAllAsync(any(), any());
        
        // Configure meeting service mocks
        doAnswer(invocation -> {
            LocalDate date = invocation.getArgument(0);
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<List<Meeting>> successCallback = 
                invocation.getArgument(1);
            
            // Filter meetings by date
            List<Meeting> meetingsForDate = testMeetings.stream()
                .filter(meeting -> meeting.getDate() != null && meeting.getDate().equals(date))
                .toList();
            
            successCallback.accept(meetingsForDate);
            return null;
        }).when(mockMeetingService).findByDateAsync(any(), any(), any());
        
        doAnswer(invocation -> {
            Project project = invocation.getArgument(0);
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<List<Meeting>> successCallback = 
                invocation.getArgument(1);
            
            // Filter meetings by project
            List<Meeting> projectMeetings = testMeetings.stream()
                .filter(meeting -> meeting.getProject() != null && 
                                 meeting.getProject().getId().equals(project.getId()))
                .toList();
            
            successCallback.accept(projectMeetings);
            return null;
        }).when(mockMeetingService).findByProjectAsync(any(), any(), any());
        
        // Register mocks with TestModule - BOTH interface and implementation
        TestModule.setService(TaskService.class, mockTaskService);
        TestModule.setService(TestableTaskServiceAsyncImpl.class, mockTaskService);
        TestModule.setService(MeetingService.class, mockMeetingService);
        TestModule.setService(TestableMeetingServiceAsyncImpl.class, mockMeetingService);
        
        // Get services from TestModule
        taskService = TestModule.getService(TaskService.class);
        meetingService = TestModule.getService(MeetingService.class);
        
        // Initialize JavaFX toolkit if needed
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
        
        // Create the view model on the JavaFX thread
        TestUtils.runOnFxThreadAndWait(() -> {
            viewModel = new DailyMvvmViewModel(taskService, meetingService);
        });
    }
    
    private void setupTestData() {
        // Create test project
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");
        
        // Create test team member
        testMember = new TeamMember();
        testMember.setId(1L);
        testMember.setFirstName("John");
        testMember.setLastName("Doe");
        testMember.setEmail("john.doe@example.com");
        
        // Create test tasks
        testTasks = new ArrayList<>();
        
        Task todayTask1 = new Task();
        todayTask1.setId(1L);
        todayTask1.setTitle("Complete robot assembly");
        todayTask1.setDescription("Finish assembling the robot chassis");
        todayTask1.setDueDate(LocalDate.now());
        todayTask1.setStatus(TaskStatus.IN_PROGRESS);
        todayTask1.setAssignedMembers(List.of(testMember));
        todayTask1.setProject(testProject);
        testTasks.add(todayTask1);
        
        Task todayTask2 = new Task();
        todayTask2.setId(2L);
        todayTask2.setTitle("Test drivetrain");
        todayTask2.setDescription("Run tests on the drivetrain system");
        todayTask2.setDueDate(LocalDate.now());
        todayTask2.setStatus(TaskStatus.TODO);
        todayTask2.setProject(testProject);
        testTasks.add(todayTask2);
        
        Task tomorrowTask = new Task();
        tomorrowTask.setId(3L);
        tomorrowTask.setTitle("Program autonomous");
        tomorrowTask.setDescription("Write autonomous code");
        tomorrowTask.setDueDate(LocalDate.now().plusDays(1));
        tomorrowTask.setStatus(TaskStatus.TODO);
        tomorrowTask.setAssignedMembers(List.of(testMember));
        tomorrowTask.setProject(testProject);
        testTasks.add(tomorrowTask);
        
        Task overdueTask = new Task();
        overdueTask.setId(4L);
        overdueTask.setTitle("Order parts");
        overdueTask.setDescription("Order missing parts for robot");
        overdueTask.setDueDate(LocalDate.now().minusDays(2));
        overdueTask.setStatus(TaskStatus.TODO);
        overdueTask.setProject(testProject);
        testTasks.add(overdueTask);
        
        // Create test meetings
        testMeetings = new ArrayList<>();
        
        Meeting todayMeeting = new Meeting();
        todayMeeting.setId(1L);
        todayMeeting.setDate(LocalDate.now());
        todayMeeting.setStartTime(LocalTime.of(16, 0));
        todayMeeting.setEndTime(LocalTime.of(18, 0));
        todayMeeting.setNotes("Daily standup meeting");
        todayMeeting.setProject(testProject);
        testMeetings.add(todayMeeting);
        
        Meeting tomorrowMeeting = new Meeting();
        tomorrowMeeting.setId(2L);
        tomorrowMeeting.setDate(LocalDate.now().plusDays(1));
        tomorrowMeeting.setStartTime(LocalTime.of(17, 0));
        tomorrowMeeting.setEndTime(LocalTime.of(19, 0));
        tomorrowMeeting.setNotes("Design review meeting");
        tomorrowMeeting.setProject(testProject);
        testMeetings.add(tomorrowMeeting);
    }
    
    @Test
    public void testInitialState() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Verify initial state
            assertTrue(viewModel.getTodaysTasks().isEmpty());
            assertTrue(viewModel.getTodaysMeetings().isEmpty());
            assertTrue(viewModel.getMyTasks().isEmpty());
            assertNull(viewModel.getCurrentProject());
            assertNull(viewModel.getCurrentMember());
            assertFalse(viewModel.isLoading());
            
            // Verify commands exist
            assertNotNull(viewModel.getLoadTodaysDataCommand());
            assertNotNull(viewModel.getLoadMyTasksCommand());
            assertNotNull(viewModel.getRefreshCommand());
            assertNotNull(viewModel.getMarkTaskCompleteCommand());
            
            // Check command executability - use !isExecutable instead of isNotExecutable
            assertTrue(viewModel.getLoadTodaysDataCommand().isExecutable());
            assertTrue(viewModel.getRefreshCommand().isExecutable());
            assertFalse(viewModel.getLoadMyTasksCommand().isExecutable()); // No member set
            assertFalse(viewModel.getMarkTaskCompleteCommand().isExecutable()); // No task selected
        });
    }
    
    @Test
    public void testInitWithProject() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize with project
            viewModel.initWithProject(testProject);
            
            // Verify project was set
            assertEquals(testProject, viewModel.getCurrentProject());
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Verify today's data was loaded
            assertEquals(2, viewModel.getTodaysTasks().size()); // 2 tasks due today
            assertEquals(1, viewModel.getTodaysMeetings().size()); // 1 meeting today
            
            // Verify task details
            assertTrue(viewModel.getTodaysTasks().stream().anyMatch(t -> t.getTitle().equals("Complete robot assembly")));
            assertTrue(viewModel.getTodaysTasks().stream().anyMatch(t -> t.getTitle().equals("Test drivetrain")));
            
            // Verify meeting details
            assertEquals("Daily standup meeting", viewModel.getTodaysMeetings().get(0).getNotes());
        });
    }
    
    @Test
    public void testLoadTodaysDataCommand() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set project and execute load today's data command
            viewModel.setCurrentProject(testProject);
            viewModel.getLoadTodaysDataCommand().execute();
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Verify today's tasks were loaded
            assertEquals(2, viewModel.getTodaysTasks().size());
            
            // Verify today's meetings were loaded
            assertEquals(1, viewModel.getTodaysMeetings().size());
            
            // Error message should be cleared
            assertNull(viewModel.getErrorMessage());
        });
    }
    
    @Test
    public void testLoadMyTasksCommand() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set current member
            viewModel.setCurrentMember(testMember);
            
            // Load my tasks command should now be executable
            assertTrue(viewModel.getLoadMyTasksCommand().isExecutable());
            
            // Execute load my tasks command
            viewModel.getLoadMyTasksCommand().execute();
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Verify my tasks were loaded (2 tasks assigned to testMember)
            assertEquals(2, viewModel.getMyTasks().size());
            
            // Verify task details
            assertTrue(viewModel.getMyTasks().stream().anyMatch(t -> t.getTitle().equals("Complete robot assembly")));
            assertTrue(viewModel.getMyTasks().stream().anyMatch(t -> t.getTitle().equals("Program autonomous")));
        });
    }
    
    @Test
    public void testTaskPriorityFiltering() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Load today's data
            viewModel.initWithProject(testProject);
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initially showing all tasks
            assertEquals(2, viewModel.getTodaysTasks().size());
            
            // Get high priority tasks (should be empty with current test data)
            List<Task> highPriorityTasks = viewModel.getHighPriorityTasks();
            assertTrue(highPriorityTasks.isEmpty()); // No high priority tasks in test data
            
            // Get overdue tasks
            List<Task> overdueTasks = viewModel.getOverdueTasks();
            assertEquals(1, overdueTasks.size()); // 1 overdue task
            assertEquals("Order parts", overdueTasks.get(0).getTitle());
        });
    }
    
    @Test
    public void testTaskStatusCounting() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Load today's data
            viewModel.initWithProject(testProject);
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Verify task counts by status
            assertEquals(1, viewModel.getInProgressTasksCount()); // 1 in progress
            assertEquals(1, viewModel.getTodoTasksCount()); // 1 todo (for today)
            assertEquals(0, viewModel.getCompletedTasksCount()); // 0 completed
            
            // Verify total tasks count
            assertEquals(2, viewModel.getTotalTasksCount());
        });
    }
    
    @Test
    public void testMarkTaskComplete() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Load today's data
            viewModel.initWithProject(testProject);
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Select a task
            Task taskToComplete = viewModel.getTodaysTasks().get(0);
            viewModel.setSelectedTask(taskToComplete);
            
            // Mark task complete command should be executable
            assertTrue(viewModel.getMarkTaskCompleteCommand().isExecutable());
            
            // Execute mark complete command
            viewModel.getMarkTaskCompleteCommand().execute();
            
            // Verify task status was updated
            assertEquals(TaskStatus.DONE, taskToComplete.getStatus());
        });
    }
    
    @Test
    public void testDailyProgressCalculation() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Load today's data
            viewModel.initWithProject(testProject);
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initial progress (0 completed out of 2 = 0%)
            assertEquals(0.0, viewModel.getDailyProgress(), 0.01);
            
            // Mark one task as complete
            Task task = viewModel.getTodaysTasks().get(0);
            task.setStatus(TaskStatus.DONE);
            viewModel.refreshProgress();
            
            // Progress should be 50% (1 out of 2)
            assertEquals(50.0, viewModel.getDailyProgress(), 0.01);
        });
    }
    
    @Test
    public void testUpcomingMeetingsAlert() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Create a meeting that starts soon (within 30 minutes)
            Meeting urgentMeeting = new Meeting();
            urgentMeeting.setId(3L);
            urgentMeeting.setDate(LocalDate.now());
            urgentMeeting.setStartTime(LocalTime.now().plusMinutes(15));
            urgentMeeting.setEndTime(LocalTime.now().plusMinutes(75));
            urgentMeeting.setNotes("Urgent design review");
            urgentMeeting.setProject(testProject);
            
            // Add to today's meetings
            viewModel.getTodaysMeetings().add(urgentMeeting);
            
            // Check for upcoming meetings
            List<Meeting> upcomingMeetings = viewModel.getUpcomingMeetings();
            assertEquals(1, upcomingMeetings.size());
            assertEquals("Urgent design review", upcomingMeetings.get(0).getNotes());
        });
    }
    
    @Test
    public void testMemberTaskFiltering() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set current member and load tasks
            viewModel.setCurrentMember(testMember);
            viewModel.getLoadMyTasksCommand().execute();
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Verify only tasks assigned to the member are loaded
            assertEquals(2, viewModel.getMyTasks().size());
            
            for (Task task : viewModel.getMyTasks()) {
                assertTrue(task.getAssignedMembers().contains(testMember));
            }
        });
    }
    
    @Test
    public void testRefreshCommand() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set project and member
            viewModel.setCurrentProject(testProject);
            viewModel.setCurrentMember(testMember);
            
            // Execute refresh command
            viewModel.getRefreshCommand().execute();
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Verify all data was refreshed
            assertEquals(2, viewModel.getTodaysTasks().size());
            assertEquals(1, viewModel.getTodaysMeetings().size());
            assertEquals(2, viewModel.getMyTasks().size());
        });
    }
    
    @Test
    public void testTaskSelection() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Load today's data
            viewModel.initWithProject(testProject);
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initially no task selected
            assertNull(viewModel.getSelectedTask());
            assertFalse(viewModel.getMarkTaskCompleteCommand().isExecutable());
            
            // Select a task
            Task selectedTask = viewModel.getTodaysTasks().get(0);
            viewModel.setSelectedTask(selectedTask);
            
            // Verify selection
            assertEquals(selectedTask, viewModel.getSelectedTask());
            
            // Mark complete command should now be executable (if task is not already complete)
            if (!selectedTask.getStatus().equals(TaskStatus.DONE)) {
                assertTrue(viewModel.getMarkTaskCompleteCommand().isExecutable());
            }
        });
    }
    
    @Test
    public void testDailyStatusSummary() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Load today's data
            viewModel.initWithProject(testProject);
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Get daily status summary
            String summary = viewModel.getDailyStatusSummary();
            assertNotNull(summary);
            
            // Summary should contain task counts and meeting info
            assertTrue(summary.contains("2")); // Total tasks
            assertTrue(summary.contains("1")); // Meetings
            assertTrue(summary.toLowerCase().contains("task"));
            assertTrue(summary.toLowerCase().contains("meeting"));
        });
    }
    
    @Test
    public void testErrorHandlingDuringLoad() {
        // Create error mock
        TestableTaskServiceAsyncImpl errorMockService = mock(TestableTaskServiceAsyncImpl.class);
        
        // Configure mock to throw error
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<Throwable> errorCallback = 
                invocation.getArgument(2);
            errorCallback.accept(new RuntimeException("Test error"));
            return null;
        }).when(errorMockService).findTasksDueTodayAsync(any(), any(), any());
        
        // Register error mock with TestModule
        TestModule.setService(TaskService.class, errorMockService);
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Create new view model with error mock
            DailyMvvmViewModel errorViewModel = new DailyMvvmViewModel(
                TestModule.getService(TaskService.class),
                TestModule.getService(MeetingService.class)
            );
            
            // Set project and execute load command
            errorViewModel.setCurrentProject(testProject);
            errorViewModel.getLoadTodaysDataCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify error message
            assertNotNull(errorViewModel.getErrorMessage());
            assertTrue(errorViewModel.getErrorMessage().contains("Failed to load"));
            
            // Verify tasks are empty
            assertTrue(errorViewModel.getTodaysTasks().isEmpty());
        });
    }
    
    @Test
    public void testPropertyBindings() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Test project property
            viewModel.setCurrentProject(testProject);
            assertEquals(testProject, viewModel.getCurrentProject());
            assertEquals(testProject, viewModel.currentProjectProperty().get());
            
            // Test member property
            viewModel.setCurrentMember(testMember);
            assertEquals(testMember, viewModel.getCurrentMember());
            assertEquals(testMember, viewModel.currentMemberProperty().get());
            
            // Test loading property
            assertFalse(viewModel.isLoading());
            assertEquals(Boolean.FALSE, viewModel.loadingProperty().get());
        });
    }
    
    @Test
    public void testTimeBasedFiltering() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Load data
            viewModel.initWithProject(testProject);
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Test morning tasks (tasks due in AM)
            List<Task> morningTasks = viewModel.getMorningTasks();
            // All tasks are due today but don't have specific times, so this will be empty
            assertTrue(morningTasks.isEmpty());
            
            // Test afternoon tasks (tasks due in PM)
            List<Task> afternoonTasks = viewModel.getAfternoonTasks();
            // All tasks are due today but don't have specific times, so this will be empty
            assertTrue(afternoonTasks.isEmpty());
            
            // Test current hour meetings
            List<Meeting> currentHourMeetings = viewModel.getCurrentHourMeetings();
            // Meeting is at 16:00, will be empty unless current time matches
            assertTrue(currentHourMeetings.size() <= 1);
        });
    }
    
    @Test
    public void testDispose() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Load some data first
            viewModel.initWithProject(testProject);
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Verify data was loaded
            assertFalse(viewModel.getTodaysTasks().isEmpty());
            assertFalse(viewModel.getTodaysMeetings().isEmpty());
            
            // Call dispose
            viewModel.dispose();
            
            // Verify collections were cleared
            assertTrue(viewModel.getTodaysTasks().isEmpty());
            assertTrue(viewModel.getTodaysMeetings().isEmpty());
            assertTrue(viewModel.getMyTasks().isEmpty());
        });
    }
}