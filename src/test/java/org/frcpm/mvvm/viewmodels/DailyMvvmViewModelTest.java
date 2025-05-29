// src/test/java/org/frcpm/mvvm/viewmodels/DailyMvvmViewModelTest.java - FIXED

package org.frcpm.mvvm.viewmodels;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.frcpm.di.TestModule;
import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.models.TeamMember;
import org.frcpm.services.MeetingService;
import org.frcpm.services.TaskService;
import org.frcpm.services.impl.MeetingServiceAsyncImpl;
import org.frcpm.services.impl.TaskServiceAsyncImpl;
import org.frcpm.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the DailyMvvmViewModel class.
 * FIXED: Uses proven methodology - mocks async implementation classes directly.
 */
public class DailyMvvmViewModelTest {
    
    private TaskServiceAsyncImpl taskServiceAsync;
    private MeetingServiceAsyncImpl meetingServiceAsync;
    
    private Project testProject;
    private List<Task> testTasks;
    private List<Meeting> testMeetings;
    private DailyMvvmViewModel viewModel;
    
    @BeforeEach
    public void setUp() throws Exception {
        // Initialize TestModule
        TestModule.initialize();
        
        // Create test data
        setupTestData();
        
        // Create mock services - CRITICAL: Mock the actual async implementations
        taskServiceAsync = mock(TaskServiceAsyncImpl.class);
        meetingServiceAsync = mock(MeetingServiceAsyncImpl.class);
        
        // Configure taskServiceAsync
        doAnswer(invocation -> {
            Project project = invocation.getArgument(0);
            Consumer<List<Task>> successCallback = invocation.getArgument(1);
            
            // Filter tasks by project
            List<Task> projectTasks = testTasks.stream()
                .filter(t -> t.getProject() != null && t.getProject().getId().equals(project.getId()))
                .toList();
            
            successCallback.accept(projectTasks);
            return null;
        }).when(taskServiceAsync).findByProjectAsync(any(Project.class), any(), any());
        
        // Configure meetingServiceAsync
        doAnswer(invocation -> {
            LocalDate date = invocation.getArgument(0);
            Consumer<List<Meeting>> successCallback = invocation.getArgument(1);
            
            // Filter meetings by date
            List<Meeting> meetingsForDate = testMeetings.stream()
                .filter(m -> m.getDate() != null && m.getDate().equals(date))
                .toList();
            
            successCallback.accept(meetingsForDate);
            return null;
        }).when(meetingServiceAsync).findByDateAsync(any(LocalDate.class), any(), any());
        
        // Register mocks with TestModule - BOTH interface and implementation
        TestModule.setService(TaskService.class, taskServiceAsync);
        TestModule.setService(TaskServiceAsyncImpl.class, taskServiceAsync);
        TestModule.setService(MeetingService.class, meetingServiceAsync);
        TestModule.setService(MeetingServiceAsyncImpl.class, meetingServiceAsync);
        
        // Initialize JavaFX toolkit if needed
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
        
        // Create the view model on the JavaFX thread
        TestUtils.runOnFxThreadAndWait(() -> {
            viewModel = new DailyMvvmViewModel(
                TestModule.getService(TaskService.class),
                TestModule.getService(MeetingService.class)
            );
        });
    }
    
    private void setupTestData() {
        // Create test project
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");
        
        // Create test tasks
        testTasks = new ArrayList<>();
        
        Task task1 = new Task();
        task1.setId(1L);
        task1.setTitle("Complete robot arm");
        task1.setStartDate(LocalDate.now().minusDays(2));
        task1.setEndDate(LocalDate.now().plusDays(1));
        //task1.setDueDate(LocalDate.now());
        task1.setProgress(50);
        task1.setProject(testProject);
        testTasks.add(task1);
        
        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Test drivetrain");
        task2.setStartDate(LocalDate.now());
        task2.setEndDate(LocalDate.now());
        //task2.setDueDate(LocalDate.now());
        task2.setProgress(25);
        task2.setProject(testProject);
        testTasks.add(task2);
        
        Task task3 = new Task();
        task3.setId(3L);
        task3.setTitle("Future task");
        task3.setStartDate(LocalDate.now().plusDays(1));
        task3.setEndDate(LocalDate.now().plusDays(3));
        //task3.setDueDate(LocalDate.now().plusDays(3));
        task3.setProgress(0);
        task3.setProject(testProject);
        testTasks.add(task3);
        
        // Create test meetings
        testMeetings = new ArrayList<>();
        
        Meeting meeting1 = new Meeting();
        meeting1.setId(1L);
        meeting1.setDate(LocalDate.now());
        meeting1.setStartTime(LocalTime.of(16, 0));
        meeting1.setEndTime(LocalTime.of(18, 0));
        meeting1.setNotes("Daily standup");
        meeting1.setProject(testProject);
        testMeetings.add(meeting1);
        
        Meeting meeting2 = new Meeting();
        meeting2.setId(2L);
        meeting2.setDate(LocalDate.now().plusDays(1));
        meeting2.setStartTime(LocalTime.of(15, 0));
        meeting2.setEndTime(LocalTime.of(17, 0));
        meeting2.setNotes("Design review");
        meeting2.setProject(testProject);
        testMeetings.add(meeting2);
    }
    
    @Test
    public void testInitialState() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Verify initial state
            assertNotNull(viewModel.getSelectedDate());
            assertEquals(LocalDate.now(), viewModel.getSelectedDate());
            assertFalse(viewModel.isLoading());
            
            // Verify collections are empty
            assertTrue(viewModel.getTasks().isEmpty());
            assertTrue(viewModel.getMeetings().isEmpty());
            
            // Verify commands exist
            assertNotNull(viewModel.getAddTaskCommand());
            assertNotNull(viewModel.getEditTaskCommand());
            assertNotNull(viewModel.getAddMeetingCommand());
            assertNotNull(viewModel.getEditMeetingCommand());
            assertNotNull(viewModel.getTakeAttendanceCommand());
            assertNotNull(viewModel.getRefreshCommand());
            
            // Check command executability
            assertTrue(!viewModel.getEditTaskCommand().isExecutable()); // No task selected
            assertTrue(!viewModel.getEditMeetingCommand().isExecutable()); // No meeting selected
            assertTrue(!viewModel.getTakeAttendanceCommand().isExecutable()); // No meeting selected
            assertTrue(viewModel.getRefreshCommand().isExecutable()); // Always executable
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
            // Verify data was loaded for today's date
            assertFalse(viewModel.getTasks().isEmpty());
            assertFalse(viewModel.getMeetings().isEmpty());
            
            // Verify specific data
            assertEquals(2, viewModel.getTasks().size()); // Tasks active today
            assertEquals(1, viewModel.getMeetings().size()); // Meetings today
            
            // Verify commands that require project are executable
            assertTrue(viewModel.getAddTaskCommand().isExecutable());
            assertTrue(viewModel.getAddMeetingCommand().isExecutable());
        });
    }
    
    @Test
    public void testLoadDataForDate() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set project
            viewModel.setCurrentProject(testProject);
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Change date to tomorrow
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            viewModel.setSelectedDate(tomorrow);
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Verify tomorrow's data was loaded
            assertEquals(2, viewModel.getTasks().size()); // Task 1 and task 3 span tomorrow
            assertEquals(1, viewModel.getMeetings().size()); // Meeting 2 is tomorrow
            
            // Verify specific content
            assertTrue(viewModel.getTasks().stream().anyMatch(t -> t.getTitle().equals("Complete robot arm")));
            assertTrue(viewModel.getTasks().stream().anyMatch(t -> t.getTitle().equals("Future task")));
            assertTrue(viewModel.getMeetings().stream().anyMatch(m -> m.getNotes().equals("Design review")));
        });
    }
    
    @Test
    public void testTaskSelection() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set project and load data
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
            assertFalse(viewModel.getEditTaskCommand().isExecutable());
            
            // Select a task
            Task task = viewModel.getTasks().get(0);
            viewModel.setSelectedTask(task);
            
            // Verify selection
            assertEquals(task, viewModel.getSelectedTask());
            assertTrue(viewModel.getEditTaskCommand().isExecutable());
        });
    }
    
    @Test
    public void testMeetingSelection() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set project and load data
            viewModel.initWithProject(testProject);
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initially no meeting selected
            assertNull(viewModel.getSelectedMeeting());
            assertFalse(viewModel.getEditMeetingCommand().isExecutable());
            assertFalse(viewModel.getTakeAttendanceCommand().isExecutable());
            
            // Select a meeting
            Meeting meeting = viewModel.getMeetings().get(0);
            viewModel.setSelectedMeeting(meeting);
            
            // Verify selection
            assertEquals(meeting, viewModel.getSelectedMeeting());
            assertTrue(viewModel.getEditMeetingCommand().isExecutable());
            assertTrue(viewModel.getTakeAttendanceCommand().isExecutable());
        });
    }
    
    @Test
    public void testRefreshCommand() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set project and load initial data
            viewModel.initWithProject(testProject);
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Add a new task to test data
        Task newTask = new Task();
        newTask.setId(4L);
        newTask.setTitle("New urgent task");
        newTask.setStartDate(LocalDate.now());
        newTask.setEndDate(LocalDate.now());
        //newTask.setDueDate(LocalDate.now());
        newTask.setProject(testProject);
        testTasks.add(newTask);
        
        TestUtils.runOnFxThreadAndWait(() -> {
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
            // Verify data was refreshed and includes new task
            assertEquals(3, viewModel.getTasks().size()); // Now includes the new task
            assertTrue(viewModel.getTasks().stream().anyMatch(t -> t.getTitle().equals("New urgent task")));
        });
    }
    
    @Test
    public void testFormattedDate() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set a specific date
            LocalDate testDate = LocalDate.of(2024, 3, 15);
            viewModel.setSelectedDate(testDate);
            
            // Verify formatted date
            String formattedDate = viewModel.getFormattedDate();
            assertTrue(formattedDate.contains("March"));
            assertTrue(formattedDate.contains("15"));
            assertTrue(formattedDate.contains("2024"));
        });
    }
    
    @Test
    public void testLoadDataErrorHandling() {
        // Create error mock
        TaskServiceAsyncImpl errorMockService = mock(TaskServiceAsyncImpl.class);
        
        // Configure mock to throw error
        doAnswer(invocation -> {
            Consumer<Throwable> errorCallback = invocation.getArgument(2);
            errorCallback.accept(new RuntimeException("Test error"));
            return null;
        }).when(errorMockService).findByProjectAsync(any(Project.class), any(), any());
        
        // Register error mock
        TestModule.setService(TaskService.class, errorMockService);
        TestModule.setService(TaskServiceAsyncImpl.class, errorMockService);
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Create a new view model with the error mock
            DailyMvvmViewModel errorViewModel = new DailyMvvmViewModel(
                TestModule.getService(TaskService.class),
                TestModule.getService(MeetingService.class)
            );
            
            // Set project
            errorViewModel.setCurrentProject(testProject);
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify error handling
            assertNotNull(errorViewModel.getErrorMessage());
            assertTrue(errorViewModel.getErrorMessage().contains("Failed to load"));
            assertFalse(errorViewModel.isLoading());
        });
    }
    
    @Test
    public void testPropertyBindings() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Test project property
            viewModel.setCurrentProject(testProject);
            assertEquals(testProject, viewModel.getCurrentProject());
            assertEquals(testProject, viewModel.currentProjectProperty().get());
            
            // Test date property
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            viewModel.setSelectedDate(tomorrow);
            assertEquals(tomorrow, viewModel.getSelectedDate());
            assertEquals(tomorrow, viewModel.selectedDateProperty().get());
            
            // Test loading property
            assertFalse(viewModel.isLoading());
            assertEquals(Boolean.FALSE, viewModel.loadingProperty().get());
        });
    }
    
    @Test
    public void testIsTaskOnDate() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set project and load data
            viewModel.initWithProject(testProject);
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Change date to tomorrow
        TestUtils.runOnFxThreadAndWait(() -> {
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            viewModel.setSelectedDate(tomorrow);
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Verify task filtering by date works correctly
            assertEquals(2, viewModel.getTasks().size());
            
            // Task 1 spans today and tomorrow
            assertTrue(viewModel.getTasks().stream().anyMatch(t -> t.getTitle().equals("Complete robot arm")));
            
            // Task 2 is only for today, should not be in tomorrow's list
            assertFalse(viewModel.getTasks().stream().anyMatch(t -> t.getTitle().equals("Test drivetrain")));
            
            // Task 3 starts tomorrow, should be in tomorrow's list
            assertTrue(viewModel.getTasks().stream().anyMatch(t -> t.getTitle().equals("Future task")));
        });
    }
    
    @Test
    public void testLoadWithNullProject() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Try to load data with null project
            viewModel.setCurrentProject(null);
            
            // Should not cause errors
            assertNull(viewModel.getCurrentProject());
            assertTrue(viewModel.getTasks().isEmpty());
            assertTrue(viewModel.getMeetings().isEmpty());
        });
    }
    
    @Test
    public void testDispose() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize with data
            viewModel.initWithProject(testProject);
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Verify data is loaded
            assertFalse(viewModel.getTasks().isEmpty());
            assertFalse(viewModel.getMeetings().isEmpty());
            
            // Call dispose
            viewModel.dispose();
            
            // Collections should be cleared
            assertTrue(viewModel.getTasks().isEmpty());
            assertTrue(viewModel.getMeetings().isEmpty());
        });
    }
}