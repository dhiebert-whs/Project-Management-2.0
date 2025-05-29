// src/test/java/org/frcpm/mvvm/viewmodels/AttendanceMvvmViewModelTest.java - FIXED

package org.frcpm.mvvm.viewmodels;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.frcpm.di.TestModule;
import org.frcpm.models.Attendance;
import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.models.TeamMember;
import org.frcpm.services.AttendanceService;
import org.frcpm.services.MeetingService;
import org.frcpm.services.TeamMemberService;
import org.frcpm.services.impl.AttendanceServiceAsyncImpl;
import org.frcpm.services.impl.MeetingServiceAsyncImpl;
import org.frcpm.services.impl.TeamMemberServiceAsyncImpl;
import org.frcpm.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the AttendanceMvvmViewModel class.
 * FIXED: Uses proven methodology - mocks async implementation classes directly.
 */
public class AttendanceMvvmViewModelTest {
    
    private AttendanceServiceAsyncImpl attendanceServiceAsync;
    private MeetingServiceAsyncImpl meetingServiceAsync;
    private TeamMemberServiceAsyncImpl teamMemberServiceAsync;
    
    private Project testProject;
    private Meeting testMeeting;
    private List<TeamMember> testTeamMembers;
    private List<Attendance> testAttendances;
    private AttendanceMvvmViewModel viewModel;
    
    @BeforeEach
    public void setUp() throws Exception {
        // Initialize TestModule
        TestModule.initialize();
        
        // Create test data
        setupTestData();
        
        // Create mock services - CRITICAL: Mock the actual async implementations
        attendanceServiceAsync = mock(AttendanceServiceAsyncImpl.class);
        meetingServiceAsync = mock(MeetingServiceAsyncImpl.class);
        teamMemberServiceAsync = mock(TeamMemberServiceAsyncImpl.class);
        
        // Configure attendanceServiceAsync
        doAnswer(invocation -> {
            Meeting meeting = invocation.getArgument(0);
            Consumer<List<Attendance>> successCallback = invocation.getArgument(1);
            successCallback.accept(testAttendances);
            return null;
        }).when(attendanceServiceAsync).findByMeetingAsync(any(Meeting.class), any(), any());
        
        doAnswer(invocation -> {
            Long meetingId = invocation.getArgument(0);
            List<Long> presentMemberIds = invocation.getArgument(1);
            Consumer<Integer> successCallback = invocation.getArgument(2);
            successCallback.accept(presentMemberIds.size());
            return null;
        }).when(attendanceServiceAsync).recordAttendanceForMeetingAsync(anyLong(), anyList(), any(), any());
        
        doAnswer(invocation -> {
            Long attendanceId = invocation.getArgument(0);
            boolean present = invocation.getArgument(1);
            LocalTime arrivalTime = invocation.getArgument(2);
            LocalTime departureTime = invocation.getArgument(3);
            Consumer<Attendance> successCallback = invocation.getArgument(4);
            
            Attendance attendance = testAttendances.stream()
                .filter(a -> a.getId().equals(attendanceId))
                .findFirst()
                .orElse(null);
                
            if (attendance != null) {
                attendance.setPresent(present);
                attendance.setArrivalTime(arrivalTime);
                attendance.setDepartureTime(departureTime);
                successCallback.accept(attendance);
            }
            
            return null;
        }).when(attendanceServiceAsync).updateAttendanceAsync(anyLong(), anyBoolean(), any(), any(), any(), any());
        
        // Configure teamMemberServiceAsync
        doAnswer(invocation -> {
            Consumer<List<TeamMember>> successCallback = invocation.getArgument(0);
            successCallback.accept(testTeamMembers);
            return null;
        }).when(teamMemberServiceAsync).findAllAsync(any(), any());
        
        // Configure meetingServiceAsync
        // No specific configuration needed for this test
        
        // Register mocks with TestModule - BOTH interface and implementation
        TestModule.setService(AttendanceService.class, attendanceServiceAsync);
        TestModule.setService(AttendanceServiceAsyncImpl.class, attendanceServiceAsync);
        TestModule.setService(MeetingService.class, meetingServiceAsync);
        TestModule.setService(MeetingServiceAsyncImpl.class, meetingServiceAsync);
        TestModule.setService(TeamMemberService.class, teamMemberServiceAsync);
        TestModule.setService(TeamMemberServiceAsyncImpl.class, teamMemberServiceAsync);
        
        // Initialize JavaFX toolkit if needed
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
        
        // Create the view model on the JavaFX thread
        TestUtils.runOnFxThreadAndWait(() -> {
            viewModel = new AttendanceMvvmViewModel(
                TestModule.getService(AttendanceService.class),
                TestModule.getService(TeamMemberService.class),
                TestModule.getService(MeetingService.class)
            );
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
        testMeeting.setDate(LocalDate.now());
        testMeeting.setStartTime(LocalTime.of(16, 0));
        testMeeting.setEndTime(LocalTime.of(18, 0));
        testMeeting.setNotes("Weekly team meeting");
        testMeeting.setProject(testProject);
        
        // Create test team members
        testTeamMembers = new ArrayList<>();
        
        TeamMember member1 = new TeamMember();
        member1.setId(1L);
        member1.setFirstName("John");
        member1.setLastName("Doe");
        member1.setEmail("john.doe@example.com");
        testTeamMembers.add(member1);
        
        TeamMember member2 = new TeamMember();
        member2.setId(2L);
        member2.setFirstName("Jane");
        member2.setLastName("Smith");
        member2.setEmail("jane.smith@example.com");
        testTeamMembers.add(member2);
        
        TeamMember member3 = new TeamMember();
        member3.setId(3L);
        member3.setFirstName("Bob");
        member3.setLastName("Johnson");
        member3.setEmail("bob.johnson@example.com");
        testTeamMembers.add(member3);
        
        // Create test attendances
        testAttendances = new ArrayList<>();
        
        Attendance attendance1 = new Attendance();
        attendance1.setId(1L);
        attendance1.setMeeting(testMeeting);
        attendance1.setMember(member1);
        attendance1.setPresent(true);
        attendance1.setArrivalTime(testMeeting.getStartTime());
        attendance1.setDepartureTime(testMeeting.getEndTime());
        testAttendances.add(attendance1);
        
        Attendance attendance2 = new Attendance();
        attendance2.setId(2L);
        attendance2.setMeeting(testMeeting);
        attendance2.setMember(member2);
        attendance2.setPresent(true);
        attendance2.setArrivalTime(testMeeting.getStartTime().plusMinutes(15));
        attendance2.setDepartureTime(testMeeting.getEndTime());
        testAttendances.add(attendance2);
        
        Attendance attendance3 = new Attendance();
        attendance3.setId(3L);
        attendance3.setMeeting(testMeeting);
        attendance3.setMember(member3);
        attendance3.setPresent(false);
        attendance3.setArrivalTime(null);
        attendance3.setDepartureTime(null);
        testAttendances.add(attendance3);
    }
    
    @Test
    public void testInitialState() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Verify initial state
            assertTrue(viewModel.getAttendanceRecords().isEmpty());
            assertNull(viewModel.getMeeting());
            assertFalse(viewModel.isLoading());
            
            // Verify commands exist
            assertNotNull(viewModel.getLoadAttendanceCommand());
            assertNotNull(viewModel.getSaveAttendanceCommand());
            assertNotNull(viewModel.getCancelCommand());
            assertNotNull(viewModel.getSetTimeCommand());
            
            // Check command executability
            assertTrue(!viewModel.getLoadAttendanceCommand().isExecutable()); // No meeting selected
            assertTrue(!viewModel.getSaveAttendanceCommand().isExecutable()); // No records loaded
            assertTrue(viewModel.getCancelCommand().isExecutable()); // Always executable
            assertTrue(!viewModel.getSetTimeCommand().isExecutable()); // No record selected
        });
    }
    
    @Test
    public void testInitWithMeeting() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize with meeting
            viewModel.initWithMeeting(testMeeting);
            
            // Verify meeting was set
            assertEquals(testMeeting, viewModel.getMeeting());
            
            // LoadAttendance command should be executable now
            assertTrue(viewModel.getLoadAttendanceCommand().isExecutable());
        });
        
        // Execute load attendance command
        TestUtils.runOnFxThreadAndWait(() -> {
            viewModel.getLoadAttendanceCommand().execute();
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Verify attendance records were loaded
            assertEquals(3, viewModel.getAttendanceRecords().size());
            
            // Verify specific record details
            AttendanceMvvmViewModel.AttendanceRecord record1 = viewModel.getAttendanceRecords().get(0);
            assertEquals("John", record1.getTeamMember().getFirstName());
            assertTrue(record1.isPresent());
            
            AttendanceMvvmViewModel.AttendanceRecord record3 = viewModel.getAttendanceRecords().get(2);
            assertEquals("Bob", record3.getTeamMember().getFirstName());
            assertFalse(record3.isPresent());
        });
    }
    
    @Test
    public void testLoadAttendanceCommand() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set meeting first
            viewModel.initWithMeeting(testMeeting);
            
            // Execute load command
            viewModel.getLoadAttendanceCommand().execute();
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Verify attendance records were loaded
            assertEquals(3, viewModel.getAttendanceRecords().size());
            
            // SaveAttendance command should not be executable (not dirty)
            assertTrue(!viewModel.getSaveAttendanceCommand().isExecutable());
        });
    }
    
    @Test
    public void testSaveAttendanceCommand() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set meeting and load attendance
            viewModel.initWithMeeting(testMeeting);
            viewModel.getLoadAttendanceCommand().execute();
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Store the initial state for comparison
        final boolean[] initialDirtyState = new boolean[1];
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Get initial dirty state
            initialDirtyState[0] = viewModel.isDirty();
            
            // Modify a record to make the view model dirty
            AttendanceMvvmViewModel.AttendanceRecord record = viewModel.getAttendanceRecords().get(2); // Bob (absent)
            record.setPresent(true); // Change to present
            
            // ViewModel should be dirty now
            assertTrue(viewModel.isDirty());
            
            // Save command should be executable
            assertTrue(viewModel.getSaveAttendanceCommand().isExecutable());
            
            // Execute save command
            viewModel.getSaveAttendanceCommand().execute();
        });
        
        // Let async operations complete
        try {
            Thread.sleep(500); // Use a longer delay
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // After saving, the ViewModel should no longer be dirty
            assertFalse(viewModel.isDirty(), "ViewModel should not be dirty after saving");
            
            // Verify the change persisted - Bob should still be present
            AttendanceMvvmViewModel.AttendanceRecord record = viewModel.getAttendanceRecords().get(2);
            assertTrue(record.isPresent(), "Record should still be present after save");
            
            // Save command should no longer be executable (since we're not dirty)
            assertFalse(viewModel.getSaveAttendanceCommand().isExecutable(), 
                    "Save command should not be executable after saving");
        });
    }
    
    @Test
    public void testUpdateRecordTimes() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set meeting and load attendance
            viewModel.initWithMeeting(testMeeting);
            viewModel.getLoadAttendanceCommand().execute();
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Get a record to update
            AttendanceMvvmViewModel.AttendanceRecord record = viewModel.getAttendanceRecords().get(0); // John (present)
            
            // Update times
            LocalTime newArrivalTime = LocalTime.of(16, 15);
            LocalTime newDepartureTime = LocalTime.of(18, 30);
            
            // Call the method directly
            viewModel.updateRecordTimes(record, newArrivalTime, newDepartureTime);
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Verify record was updated
            AttendanceMvvmViewModel.AttendanceRecord record = viewModel.getAttendanceRecords().get(0);
            assertEquals(LocalTime.of(16, 15), record.getArrivalTime());
            assertEquals(LocalTime.of(18, 30), record.getDepartureTime());
        });
    }
    
    @Test
    public void testTimeFormatting() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Test parsing time strings
            LocalTime time1 = viewModel.parseTime("16:30");
            assertEquals(LocalTime.of(16, 30), time1);
            
            LocalTime time2 = viewModel.parseTime("invalid");
            assertNull(time2);
            
            // Test formatting times
            String formatted = viewModel.formatTime(LocalTime.of(9, 15));
            assertEquals("09:15", formatted);
        });
    }
    
    @Test
    public void testGetPresentMemberIds() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set meeting and load attendance
            viewModel.initWithMeeting(testMeeting);
            viewModel.getLoadAttendanceCommand().execute();
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Get present member IDs
            List<Long> presentMemberIds = viewModel.getPresentMemberIds();
            
            // Should have 2 present members (John and Jane)
            assertEquals(2, presentMemberIds.size());
            assertTrue(presentMemberIds.contains(1L)); // John
            assertTrue(presentMemberIds.contains(2L)); // Jane
            assertFalse(presentMemberIds.contains(3L)); // Bob (absent)
        });
    }
    
    @Test
    public void testLoadAttendanceErrorHandling() {
        // Create error mock
        AttendanceServiceAsyncImpl errorMockService = mock(AttendanceServiceAsyncImpl.class);
        
        // Configure mock to throw error
        doAnswer(invocation -> {
            Consumer<Throwable> errorCallback = invocation.getArgument(2);
            errorCallback.accept(new RuntimeException("Test error"));
            return null;
        }).when(errorMockService).findByMeetingAsync(any(), any(), any());
        
        // Register error mock
        TestModule.setService(AttendanceService.class, errorMockService);
        TestModule.setService(AttendanceServiceAsyncImpl.class, errorMockService);
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Create a new view model with the error mock
            AttendanceMvvmViewModel errorViewModel = new AttendanceMvvmViewModel(
                TestModule.getService(AttendanceService.class),
                TestModule.getService(TeamMemberService.class),
                TestModule.getService(MeetingService.class)
            );
            
            // Set meeting
            errorViewModel.initWithMeeting(testMeeting);
            
            // Execute load command
            errorViewModel.getLoadAttendanceCommand().execute();
            
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
    public void testDispose() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set meeting and load attendance
            viewModel.initWithMeeting(testMeeting);
            viewModel.getLoadAttendanceCommand().execute();
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Verify records are loaded
            assertFalse(viewModel.getAttendanceRecords().isEmpty());
            
            // Call dispose
            viewModel.dispose();
            
            // Records should be cleared
            assertTrue(viewModel.getAttendanceRecords().isEmpty());
        });
    }
}