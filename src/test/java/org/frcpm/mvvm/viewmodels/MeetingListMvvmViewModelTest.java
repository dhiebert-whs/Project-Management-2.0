// src/test/java/org/frcpm/mvvm/viewmodels/MeetingListMvvmViewModelTest.java
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
import org.frcpm.services.MeetingService;
import org.frcpm.services.impl.TestableMeetingServiceAsyncImpl;
import org.frcpm.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the MeetingListMvvmViewModel class.
 * FIXED: Uses proven methodology - mocks TestableMeetingServiceAsyncImpl directly.
 */
public class MeetingListMvvmViewModelTest {
    
    private MeetingService meetingService;
    
    private Project testProject;
    private List<Meeting> testMeetings;
    private MeetingListMvvmViewModel viewModel;
    
    @BeforeEach
    public void setUp() throws Exception {
        // Initialize TestModule
        TestModule.initialize();
        
        // Create test data
        setupTestData();
        
        // Create mock of the ASYNC implementation directly - CRITICAL for success
        TestableMeetingServiceAsyncImpl mockMeetingService = mock(TestableMeetingServiceAsyncImpl.class);
        
        // Configure findByProjectAsync to return test data
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<List<Meeting>> successCallback = 
                invocation.getArgument(1);
            successCallback.accept(testMeetings);
            return null;
        }).when(mockMeetingService).findByProjectAsync(any(), any(), any());
        
        // Configure deleteByIdAsync
        doAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<Boolean> successCallback = 
                invocation.getArgument(1);
            
            // Find meeting to delete
            Meeting meetingToDelete = testMeetings.stream()
                .filter(m -> m.getId().equals(id))
                .findFirst()
                .orElse(null);
            
            if (meetingToDelete != null) {
                // Check if meeting has attendances (similar to subteam members check)
                if (meetingToDelete.getAttendances() != null && !meetingToDelete.getAttendances().isEmpty()) {
                    // This should trigger error handling in the ViewModel
                    successCallback.accept(false);
                } else {
                    // Simulate successful deletion
                    testMeetings.removeIf(m -> m.getId().equals(id));
                    successCallback.accept(true);
                }
            } else {
                successCallback.accept(false);
            }
            return null;
        }).when(mockMeetingService).deleteByIdAsync(anyLong(), any(), any());
        
        // Configure getUpcomingMeetingsAsync
        doAnswer(invocation -> {
            Long projectId = invocation.getArgument(0);
            int days = invocation.getArgument(1);
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<List<Meeting>> successCallback = 
                invocation.getArgument(2);
            
            LocalDate today = LocalDate.now();
            LocalDate endDate = today.plusDays(days);
            
            List<Meeting> upcomingMeetings = testMeetings.stream()
                .filter(m -> m.getProject().getId().equals(projectId))
                .filter(m -> !m.getDate().isBefore(today) && !m.getDate().isAfter(endDate))
                .toList();
            
            successCallback.accept(upcomingMeetings);
            return null;
        }).when(mockMeetingService).getUpcomingMeetingsAsync(anyLong(), anyInt(), any(), any());
        
        // Register mock with TestModule - BOTH interface and implementation
        TestModule.setService(MeetingService.class, mockMeetingService);
        TestModule.setService(TestableMeetingServiceAsyncImpl.class, mockMeetingService);
        
        // Get service from TestModule
        meetingService = TestModule.getService(MeetingService.class);
        
        // Initialize JavaFX toolkit if needed
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
        
        // Create the view model on the JavaFX thread
        TestUtils.runOnFxThreadAndWait(() -> {
            viewModel = new MeetingListMvvmViewModel(meetingService);
        });
    }
    
    private void setupTestData() {
        // Create test project
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");
        
        // Create test meetings
        testMeetings = new ArrayList<>();
        
        Meeting dailyStandup = new Meeting();
        dailyStandup.setId(1L);
        dailyStandup.setDate(LocalDate.now().plusDays(1)); // Tomorrow
        dailyStandup.setStartTime(LocalTime.of(16, 0));
        dailyStandup.setEndTime(LocalTime.of(16, 30));
        dailyStandup.setNotes("Daily standup meeting");
        dailyStandup.setProject(testProject);
        testMeetings.add(dailyStandup);
        
        Meeting weeklyReview = new Meeting();
        weeklyReview.setId(2L);
        weeklyReview.setDate(LocalDate.now().plusDays(3)); // In 3 days
        weeklyReview.setStartTime(LocalTime.of(18, 0));
        weeklyReview.setEndTime(LocalTime.of(19, 30));
        weeklyReview.setNotes("Weekly progress review");
        weeklyReview.setProject(testProject);
        testMeetings.add(weeklyReview);
        
        Meeting designReview = new Meeting();
        designReview.setId(3L);
        designReview.setDate(LocalDate.now().plusDays(7)); // Next week
        designReview.setStartTime(LocalTime.of(17, 0));
        designReview.setEndTime(LocalTime.of(19, 0));
        designReview.setNotes("Robot design review with mentors");
        designReview.setProject(testProject);
        testMeetings.add(designReview);
        
        Meeting pastMeeting = new Meeting();
        pastMeeting.setId(4L);
        pastMeeting.setDate(LocalDate.now().minusDays(1)); // Yesterday
        pastMeeting.setStartTime(LocalTime.of(16, 0));
        pastMeeting.setEndTime(LocalTime.of(18, 0));
        pastMeeting.setNotes("Past meeting for testing");
        pastMeeting.setProject(testProject);
        testMeetings.add(pastMeeting);
    }
    
    @Test
    public void testInitialState() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Verify initial state
            assertTrue(viewModel.getMeetings().isEmpty());
            assertNull(viewModel.getSelectedMeeting());
            assertNull(viewModel.getCurrentProject());
            assertFalse(viewModel.isLoading());
            
            // Verify commands exist
            assertNotNull(viewModel.getLoadMeetingsCommand());
            assertNotNull(viewModel.getNewMeetingCommand());
            assertNotNull(viewModel.getEditMeetingCommand());
            assertNotNull(viewModel.getDeleteMeetingCommand());
            assertNotNull(viewModel.getRefreshMeetingsCommand());
            assertNotNull(viewModel.getViewAttendanceCommand());
            
            // Check command executability - use !isExecutable instead of isNotExecutable
            assertTrue(viewModel.getLoadMeetingsCommand().isExecutable());
            assertTrue(viewModel.getRefreshMeetingsCommand().isExecutable());
            assertFalse(viewModel.getNewMeetingCommand().isExecutable()); // No project
            assertFalse(viewModel.getDeleteMeetingCommand().isExecutable()); // No selection
            assertFalse(viewModel.getEditMeetingCommand().isExecutable()); // No selection
            assertFalse(viewModel.getViewAttendanceCommand().isExecutable()); // No selection
        });
    }
    
    @Test
    public void testInitWithProject() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize with project - this triggers findByProjectAsync
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
            // Verify meetings were loaded
            assertEquals(4, viewModel.getMeetings().size());
            
            // Verify meetings are sorted by date and time
            Meeting firstMeeting = viewModel.getMeetings().get(0);
            Meeting lastMeeting = viewModel.getMeetings().get(viewModel.getMeetings().size() - 1);
            assertTrue(firstMeeting.getDate().isBefore(lastMeeting.getDate()) || 
                      firstMeeting.getDate().equals(lastMeeting.getDate()));
            
            // New meeting command should now be executable
            assertTrue(viewModel.getNewMeetingCommand().isExecutable());
        });
    }
    
    @Test
    public void testLoadMeetingsCommand() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set project first
            viewModel.setCurrentProject(testProject);
            
            // Execute load command
            viewModel.getLoadMeetingsCommand().execute();
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Verify meetings were loaded
            assertEquals(4, viewModel.getMeetings().size());
            
            // Verify specific meetings
            assertTrue(viewModel.getMeetings().stream().anyMatch(m -> m.getNotes().contains("Daily standup")));
            assertTrue(viewModel.getMeetings().stream().anyMatch(m -> m.getNotes().contains("Weekly progress")));
            assertTrue(viewModel.getMeetings().stream().anyMatch(m -> m.getNotes().contains("Robot design")));
            assertTrue(viewModel.getMeetings().stream().anyMatch(m -> m.getNotes().contains("Past meeting")));
            
            // Error message should be cleared
            assertNull(viewModel.getErrorMessage());
        });
    }
    
    @Test
    public void testRefreshMeetingsCommand() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set project first
            viewModel.setCurrentProject(testProject);
            
            // Execute refresh command
            viewModel.getRefreshMeetingsCommand().execute();
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Verify meetings were loaded
            assertEquals(4, viewModel.getMeetings().size());
        });
    }
    
    @Test
    public void testMeetingSelection() {
        // Load meetings first
        TestUtils.runOnFxThreadAndWait(() -> {
            viewModel.initWithProject(testProject);
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initially no selection
            assertNull(viewModel.getSelectedMeeting());
            
            // Commands requiring selection should not be executable
            assertFalse(viewModel.getEditMeetingCommand().isExecutable());
            assertFalse(viewModel.getDeleteMeetingCommand().isExecutable());
            assertFalse(viewModel.getViewAttendanceCommand().isExecutable());
            
            // Select a meeting
            Meeting selectedMeeting = viewModel.getMeetings().get(0);
            viewModel.setSelectedMeeting(selectedMeeting);
            
            // Verify selection
            assertEquals(selectedMeeting, viewModel.getSelectedMeeting());
            
            // Commands requiring selection should now be executable
            assertTrue(viewModel.getEditMeetingCommand().isExecutable());
            assertTrue(viewModel.getDeleteMeetingCommand().isExecutable());
            assertTrue(viewModel.getViewAttendanceCommand().isExecutable());
        });
    }
    
    @Test
    public void testDeleteMeetingCommand() {
        // Load meetings first
        TestUtils.runOnFxThreadAndWait(() -> {
            viewModel.initWithProject(testProject);
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Get a meeting without attendances to delete
            Meeting meetingToDelete = viewModel.getMeetings().stream()
                .filter(m -> m.getNotes().contains("Daily standup"))
                .findFirst()
                .orElse(viewModel.getMeetings().get(0));
            
            // Select the meeting
            viewModel.setSelectedMeeting(meetingToDelete);
            
            // Execute delete command
            viewModel.getDeleteMeetingCommand().execute();
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Verify meeting was removed
            assertEquals(3, viewModel.getMeetings().size());
            assertFalse(viewModel.getMeetings().stream().anyMatch(m -> m.getNotes().contains("Daily standup")));
            
            // Verify selection was cleared
            assertNull(viewModel.getSelectedMeeting());
            
            // Error message should be cleared
            assertNull(viewModel.getErrorMessage());
        });
    }
    
    @Test
    public void testMeetingFiltering() {
        // Load meetings first
        TestUtils.runOnFxThreadAndWait(() -> {
            viewModel.initWithProject(testProject);
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initially showing all meetings
            assertEquals(4, viewModel.getMeetings().size());
            assertEquals(MeetingListMvvmViewModel.MeetingFilter.ALL, viewModel.getCurrentFilter());
            
            // Filter to upcoming meetings only
            viewModel.setFilter(MeetingListMvvmViewModel.MeetingFilter.UPCOMING);
            
            // Should show only future meetings (3 meetings)
            assertEquals(3, viewModel.getMeetings().size());
            assertTrue(viewModel.getMeetings().stream().allMatch(m -> 
                !m.getDate().isBefore(LocalDate.now())));
            
            // Filter to past meetings only
            viewModel.setFilter(MeetingListMvvmViewModel.MeetingFilter.PAST);
            
            // Should show only past meetings (1 meeting)
            assertEquals(1, viewModel.getMeetings().size());
            assertTrue(viewModel.getMeetings().stream().allMatch(m -> 
                m.getDate().isBefore(LocalDate.now())));
            
            // Back to all meetings
            viewModel.setFilter(MeetingListMvvmViewModel.MeetingFilter.ALL);
            assertEquals(4, viewModel.getMeetings().size());
        });
    }
    
    @Test
    public void testLoadUpcomingMeetings() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set project first
            viewModel.setCurrentProject(testProject);
            
            // Load upcoming meetings for next 5 days
            viewModel.loadUpcomingMeetingsAsync(5);
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Should have loaded meetings within 5 days (2 meetings)
            assertEquals(2, viewModel.getMeetings().size());
            
            // All should be upcoming
            assertTrue(viewModel.getMeetings().stream().allMatch(m -> 
                !m.getDate().isBefore(LocalDate.now()) && 
                !m.getDate().isAfter(LocalDate.now().plusDays(5))));
        });
    }
    
    @Test
    public void testErrorHandlingDuringLoad() {
        // Create error mock
        TestableMeetingServiceAsyncImpl errorMockService = mock(TestableMeetingServiceAsyncImpl.class);
        
        // Configure mock to throw error
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<Throwable> errorCallback = 
                invocation.getArgument(2);
            errorCallback.accept(new RuntimeException("Test error"));
            return null;
        }).when(errorMockService).findByProjectAsync(any(), any(), any());
        
        // Register error mock with TestModule
        TestModule.setService(MeetingService.class, errorMockService);
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Create new view model with error mock
            MeetingListMvvmViewModel errorViewModel = new MeetingListMvvmViewModel(
                TestModule.getService(MeetingService.class)
            );
            
            // Set project and execute load command
            errorViewModel.setCurrentProject(testProject);
            errorViewModel.getLoadMeetingsCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify error message
            assertNotNull(errorViewModel.getErrorMessage());
            assertTrue(errorViewModel.getErrorMessage().contains("Failed to load"));
            
            // Verify meetings are empty
            assertTrue(errorViewModel.getMeetings().isEmpty());
        });
    }
    
    @Test
    public void testLoadMeetingsWithoutProject() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Try to load meetings without setting project
            viewModel.getLoadMeetingsCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should remain empty and show warning
            assertTrue(viewModel.getMeetings().isEmpty());
        });
    }
    
    @Test
    public void testDispose() {
        // Load meetings first
        TestUtils.runOnFxThreadAndWait(() -> {
            viewModel.initWithProject(testProject);
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Verify meetings are not empty
            assertFalse(viewModel.getMeetings().isEmpty());
            
            // Call dispose
            viewModel.dispose();
            
            // Verify meetings are now empty
            assertTrue(viewModel.getMeetings().isEmpty());
        });
    }
    
    @Test
    public void testMeetingFilterEnum() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Test filter enum values
            assertEquals("All Meetings", MeetingListMvvmViewModel.MeetingFilter.ALL.toString());
            assertEquals("Upcoming Meetings", MeetingListMvvmViewModel.MeetingFilter.UPCOMING.toString());
            assertEquals("Past Meetings", MeetingListMvvmViewModel.MeetingFilter.PAST.toString());
        });
    }
    
    @Test
    public void testSetFilterWithNull() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set null filter should default to ALL
            viewModel.setFilter(null);
            assertEquals(MeetingListMvvmViewModel.MeetingFilter.ALL, viewModel.getCurrentFilter());
        });
    }
}