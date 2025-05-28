// src/test/java/org/frcpm/mvvm/viewmodels/MeetingDetailMvvmViewModelTest.java
package org.frcpm.mvvm.viewmodels;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.function.Consumer;

import org.frcpm.di.TestModule;
import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.services.MeetingService;
import org.frcpm.services.impl.TestableMeetingServiceAsyncImpl;
import org.frcpm.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the MeetingDetailMvvmViewModel class.
 * FIXED: Properly handles service casting and creates ViewModel correctly.
 */
public class MeetingDetailMvvmViewModelTest {
    
    private MeetingService meetingService;
    private MeetingDetailMvvmViewModel viewModel;
    
    private Meeting testMeeting;
    private Project testProject;
    
    @BeforeEach
    public void setUp() throws Exception {
        // Initialize TestModule
        TestModule.initialize();
        
        // Create test data
        setupTestData();
        
        // Create mock service - CRITICAL: Mock the actual async implementation
        TestableMeetingServiceAsyncImpl mockMeetingService = mock(TestableMeetingServiceAsyncImpl.class);
        
        // Configure mock behavior for MeetingService
        when(mockMeetingService.findById(anyLong())).thenReturn(testMeeting);
        when(mockMeetingService.save(any(Meeting.class))).thenAnswer(inv -> inv.getArgument(0));
        
        // Mock async methods for MeetingService
        doAnswer(invocation -> {
            LocalDate date = invocation.getArgument(0);
            LocalTime startTime = invocation.getArgument(1);
            LocalTime endTime = invocation.getArgument(2);
            Long projectId = invocation.getArgument(3);
            String notes = invocation.getArgument(4);
            Consumer<Meeting> callback = invocation.getArgument(5);
            
            Meeting newMeeting = new Meeting();
            newMeeting.setId(100L);
            newMeeting.setDate(date);
            newMeeting.setStartTime(startTime);
            newMeeting.setEndTime(endTime);
            newMeeting.setProject(testProject);
            newMeeting.setNotes(notes);
            callback.accept(newMeeting);
            return null;
        }).when(mockMeetingService).createMeetingAsync(any(), any(), any(), anyLong(), anyString(), any(), any());
        
        doAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            LocalDate date = invocation.getArgument(1);
            LocalTime startTime = invocation.getArgument(2);
            LocalTime endTime = invocation.getArgument(3);
            Consumer<Meeting> callback = invocation.getArgument(4);
            
            testMeeting.setDate(date);
            testMeeting.setStartTime(startTime);
            testMeeting.setEndTime(endTime);
            callback.accept(testMeeting);
            return null;
        }).when(mockMeetingService).updateMeetingDateTimeAsync(anyLong(), any(), any(), any(), any(), any());
        
        doAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            String notes = invocation.getArgument(1);
            Consumer<Meeting> callback = invocation.getArgument(2);
            
            testMeeting.setNotes(notes);
            callback.accept(testMeeting);
            return null;
        }).when(mockMeetingService).updateNotesAsync(anyLong(), anyString(), any(), any());
        
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
            viewModel = new MeetingDetailMvvmViewModel(meetingService);
        });
    }
    
    private void setupTestData() {
        // Create test project
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");
        
        // Create test meeting
        testMeeting = new Meeting();
        testMeeting.setId(1L);
        testMeeting.setDate(LocalDate.of(2024, 3, 15));
        testMeeting.setStartTime(LocalTime.of(16, 0));
        testMeeting.setEndTime(LocalTime.of(18, 0));
        testMeeting.setNotes("Weekly team meeting to discuss progress");
        testMeeting.setProject(testProject);
    }
    
    @Test
    public void testInitialStateForNewMeeting() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new meeting
            viewModel.initNewMeeting(testProject);
            
            // Verify initial state
            assertTrue(viewModel.isNewMeeting());
            assertEquals("", viewModel.getNotes());
            assertEquals(testProject, viewModel.getProject());
            assertFalse(viewModel.isDirty());
            assertTrue(viewModel.isValid()); // Should be valid with default values
            
            // Default time values should be set
            assertEquals("16:00", viewModel.getStartTimeString());
            assertEquals("18:00", viewModel.getEndTimeString());
            assertEquals(LocalDate.now(), viewModel.getDate());
            
            // Verify commands
            assertNotNull(viewModel.getSaveCommand());
            assertNotNull(viewModel.getCancelCommand());
            
            // Save command should not be executable (not dirty)
            assertFalse(viewModel.getSaveCommand().isExecutable());
        });
    }
    
    @Test
    public void testInitExistingMeeting() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for an existing meeting
            viewModel.initExistingMeeting(testMeeting);
            
            // Verify state
            assertFalse(viewModel.isNewMeeting());
            assertEquals("Weekly team meeting to discuss progress", viewModel.getNotes());
            assertEquals(testProject, viewModel.getProject());
            assertEquals(testMeeting, viewModel.getMeeting());
            assertFalse(viewModel.isDirty());
            assertTrue(viewModel.isValid());
            
            // Verify time and date values
            assertEquals(LocalDate.of(2024, 3, 15), viewModel.getDate());
            assertEquals("16:00", viewModel.getStartTimeString());
            assertEquals("18:00", viewModel.getEndTimeString());
        });
    }
    
    @Test
    public void testValidation() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new meeting
            viewModel.initNewMeeting(testProject);
            
            // Initially valid with default values
            assertTrue(viewModel.isValid());
            assertNull(viewModel.getErrorMessage());
            
            // Set invalid start time
            viewModel.setStartTimeString("invalid");
            
            // Should be invalid due to invalid time format
            assertFalse(viewModel.isValid());
            assertNotNull(viewModel.getErrorMessage());
            assertTrue(viewModel.getErrorMessage().contains("Start time format"));
            
            // Set valid start time
            viewModel.setStartTimeString("17:00");
            
            // Should be valid again
            assertTrue(viewModel.isValid());
            assertNull(viewModel.getErrorMessage());
            
            // Set end time before start time
            viewModel.setEndTimeString("16:30");
            
            // Should be invalid due to time ordering
            assertFalse(viewModel.isValid());
            assertNotNull(viewModel.getErrorMessage());
            assertTrue(viewModel.getErrorMessage().contains("End time must be after start time"));
            
            // Fix end time
            viewModel.setEndTimeString("18:30");
            
            // Should be valid again
            assertTrue(viewModel.isValid());
            assertNull(viewModel.getErrorMessage());
        });
    }
    
    @Test
    public void testTimeParsingUtilities() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Test parseTime method
            LocalTime time1 = MeetingDetailMvvmViewModel.parseTime("16:30");
            assertEquals(LocalTime.of(16, 30), time1);
            
            // Test invalid time
            LocalTime time2 = MeetingDetailMvvmViewModel.parseTime("invalid");
            assertNull(time2);
            
            // Test null time
            LocalTime time3 = MeetingDetailMvvmViewModel.parseTime(null);
            assertNull(time3);
            
            // Test formatTime method
            String formatted1 = MeetingDetailMvvmViewModel.formatTime(LocalTime.of(9, 15));
            assertEquals("09:15", formatted1);
            
            // Test null time formatting
            String formatted2 = MeetingDetailMvvmViewModel.formatTime(null);
            assertEquals("", formatted2);
        });
    }
    
    @Test
    public void testSaveNewMeeting() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new meeting
            viewModel.initNewMeeting(testProject);
            
            // Set meeting properties
            viewModel.setDate(LocalDate.of(2024, 4, 1));
            viewModel.setStartTimeString("14:00");
            viewModel.setEndTimeString("16:00");
            viewModel.setNotes("New meeting notes");
            
            // Should be valid and dirty
            assertTrue(viewModel.isValid());
            assertTrue(viewModel.isDirty());
            
            // Save command should be executable
            assertTrue(viewModel.getSaveCommand().isExecutable());
            
            // Execute save command
            viewModel.getSaveCommand().execute();
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Should no longer be loading after save completes
            assertFalse(viewModel.isLoading());
            
            // Should no longer be a new meeting
            assertFalse(viewModel.isNewMeeting());
            
            // Should no longer be dirty
            assertFalse(viewModel.isDirty());
        });
    }
    
    @Test
    public void testUpdateExistingMeeting() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for an existing meeting
            viewModel.initExistingMeeting(testMeeting);
            
            // Change some properties
            viewModel.setDate(LocalDate.of(2024, 4, 2));
            viewModel.setStartTimeString("15:30");
            viewModel.setEndTimeString("17:30");
            viewModel.setNotes("Updated meeting notes");
            
            // Should be valid and dirty
            assertTrue(viewModel.isValid());
            assertTrue(viewModel.isDirty());
            
            // Save command should be executable
            assertTrue(viewModel.getSaveCommand().isExecutable());
            
            // Execute save command
            viewModel.getSaveCommand().execute();
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Should no longer be loading after save completes
            assertFalse(viewModel.isLoading());
            
            // Should no longer be dirty
            assertFalse(viewModel.isDirty());
        });
    }
    
    @Test
    public void testDirtyFlag() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new meeting
            viewModel.initNewMeeting(testProject);
            
            // Initially not dirty
            assertFalse(viewModel.isDirty());
            
            // Change date
            viewModel.setDate(LocalDate.of(2024, 5, 1));
            
            // Should be dirty now
            assertTrue(viewModel.isDirty());
            
            // Change other properties
            viewModel.setStartTimeString("10:00");
            viewModel.setEndTimeString("12:00");
            viewModel.setNotes("Test notes");
            
            // Should still be dirty
            assertTrue(viewModel.isDirty());
        });
    }
    
    @Test
    public void testPropertyBindings() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new meeting
            viewModel.initNewMeeting(testProject);
            
            // Test date property
            LocalDate testDate = LocalDate.of(2024, 6, 15);
            viewModel.setDate(testDate);
            assertEquals(testDate, viewModel.getDate());
            assertEquals(testDate, viewModel.dateProperty().get());
            
            // Test start time string property
            viewModel.setStartTimeString("09:30");
            assertEquals("09:30", viewModel.getStartTimeString());
            assertEquals("09:30", viewModel.startTimeStringProperty().get());
            
            // Test end time string property
            viewModel.setEndTimeString("11:30");
            assertEquals("11:30", viewModel.getEndTimeString());
            assertEquals("11:30", viewModel.endTimeStringProperty().get());
            
            // Test notes property
            viewModel.setNotes("Test notes content");
            assertEquals("Test notes content", viewModel.getNotes());
            assertEquals("Test notes content", viewModel.notesProperty().get());
            
            // Test project property
            assertEquals(testProject, viewModel.getProject());
            assertEquals(testProject, viewModel.projectProperty().get());
        });
    }
    
    @Test
    public void testValidationWithNullDate() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new meeting
            viewModel.initNewMeeting(testProject);
            
            // Set date to null
            viewModel.setDate(null);
            
            // Should be invalid
            assertFalse(viewModel.isValid());
            assertNotNull(viewModel.getErrorMessage());
            assertTrue(viewModel.getErrorMessage().contains("Meeting date cannot be empty"));
        });
    }
    
    @Test
    public void testValidationWithEmptyTimes() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new meeting
            viewModel.initNewMeeting(testProject);
            
            // Set empty start time
            viewModel.setStartTimeString("");
            
            // Should be invalid
            assertFalse(viewModel.isValid());
            assertNotNull(viewModel.getErrorMessage());
            assertTrue(viewModel.getErrorMessage().contains("Start time is required"));
            
            // Fix start time, set empty end time
            viewModel.setStartTimeString("16:00");
            viewModel.setEndTimeString("");
            
            // Should be invalid
            assertFalse(viewModel.isValid());
            assertNotNull(viewModel.getErrorMessage());
            assertTrue(viewModel.getErrorMessage().contains("End time is required"));
        });
    }
    
    @Test
    public void testSaveCommandExecutability() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new meeting
            viewModel.initNewMeeting(testProject);
            
            // Initially not executable (not dirty)
            assertFalse(viewModel.getSaveCommand().isExecutable());
            
            // Make it dirty but invalid
            viewModel.setStartTimeString("invalid");
            
            // Still not executable (invalid)
            assertFalse(viewModel.getSaveCommand().isExecutable());
            
            // Make it valid and dirty
            viewModel.setStartTimeString("17:00");
            viewModel.setNotes("Some notes"); // Make it dirty
            
            // Should be executable now
            assertTrue(viewModel.getSaveCommand().isExecutable());
        });
    }
    
    @Test
    public void testInitWithNullMeeting() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize with null meeting should treat as new meeting
            viewModel.initExistingMeeting(null);
            
            // Should behave like new meeting
            assertTrue(viewModel.isNewMeeting());
            assertNull(viewModel.getProject());
            assertEquals("", viewModel.getNotes());
        });
    }
    
    @Test
    public void testInitWithNullProject() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize new meeting with null project
            viewModel.initNewMeeting(null);
            
            // Should still work but project will be null
            assertTrue(viewModel.isNewMeeting());
            assertNull(viewModel.getProject());
        });
    }
    
    @Test
    public void testCancelCommand() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new meeting
            viewModel.initNewMeeting(testProject);
            
            // Cancel command should always be executable
            assertTrue(viewModel.getCancelCommand().isExecutable());
            
            // Execute cancel command (just logs in ViewModel)
            viewModel.getCancelCommand().execute();
            
            // Should still be executable
            assertTrue(viewModel.getCancelCommand().isExecutable());
        });
    }
    
    @Test
    public void testErrorHandlingDuringSave() {
        // Create error mock
        TestableMeetingServiceAsyncImpl errorMockService = mock(TestableMeetingServiceAsyncImpl.class);
        
        // Configure mock to throw error during save
        doAnswer(invocation -> {
            Consumer<Throwable> errorCallback = invocation.getArgument(6);
            errorCallback.accept(new RuntimeException("Test error"));
            return null;
        }).when(errorMockService).createMeetingAsync(any(), any(), any(), anyLong(), anyString(), any(), any());
        
        // Register error mock
        TestModule.setService(MeetingService.class, errorMockService);
        TestModule.setService(TestableMeetingServiceAsyncImpl.class, errorMockService);
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Create new view model with error mock
            MeetingDetailMvvmViewModel errorViewModel = new MeetingDetailMvvmViewModel(
                TestModule.getService(MeetingService.class)
            );
            
            // Initialize for a new meeting
            errorViewModel.initNewMeeting(testProject);
            
            // Set required properties
            errorViewModel.setDate(LocalDate.of(2024, 4, 1));
            errorViewModel.setStartTimeString("14:00");
            errorViewModel.setEndTimeString("16:00");
            errorViewModel.setNotes("Test notes");
            
            // Execute save command
            errorViewModel.getSaveCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should have error message
            assertNotNull(errorViewModel.getErrorMessage());
            assertTrue(errorViewModel.getErrorMessage().contains("Failed to"));
            
            // Should no longer be loading
            assertFalse(errorViewModel.isLoading());
        });
    }
    
    @Test
    public void testDispose() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize with existing meeting
            viewModel.initExistingMeeting(testMeeting);
            
            // Call dispose
            viewModel.dispose();
            
            // Verify dispose was called (base class method)
            // No specific assertions needed as dispose calls parent
        });
    }
}