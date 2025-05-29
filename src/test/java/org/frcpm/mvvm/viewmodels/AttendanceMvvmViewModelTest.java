// src/test/java/org/frcpm/mvvm/viewmodels/AttendanceMvvmViewModelTest.java
package org.frcpm.mvvm.viewmodels;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.frcpm.di.TestModule;
import org.frcpm.models.Attendance;
import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.models.TeamMember;
import org.frcpm.services.AttendanceService;
import org.frcpm.services.MeetingService;
import org.frcpm.services.TeamMemberService;
import org.frcpm.services.impl.TestableAttendanceServiceImpl;
import org.frcpm.services.impl.TestableMeetingServiceAsyncImpl;
import org.frcpm.services.impl.TestableTeamMemberServiceAsyncImpl;
import org.frcpm.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the AttendanceMvvmViewModel class.
 * FIXED: Uses proven methodology - mocks testable service implementations directly.
 */
public class AttendanceMvvmViewModelTest {
    
    private AttendanceService attendanceService;
    private MeetingService meetingService;
    private TeamMemberService teamMemberService;
    
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
        
        // Create mock services - CRITICAL: Mock the actual testable implementations
        TestableAttendanceServiceImpl mockAttendanceService = mock(TestableAttendanceServiceImpl.class);
        TestableMeetingServiceAsyncImpl mockMeetingService = mock(TestableMeetingServiceAsyncImpl.class);
        TestableTeamMemberServiceAsyncImpl mockTeamMemberService = mock(TestableTeamMemberServiceAsyncImpl.class);
        
        // Configure attendance service mocks
        when(mockAttendanceService.findByMeeting(any(Meeting.class))).thenReturn(testAttendances);
        when(mockAttendanceService.save(any(Attendance.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mockAttendanceService.findAll()).thenReturn(testAttendances);
        
        // Configure meeting service mocks
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<List<Meeting>> successCallback = 
                invocation.getArgument(1);
            List<Meeting> meetings = List.of(testMeeting);
            successCallback.accept(meetings);
            return null;
        }).when(mockMeetingService).findByProjectAsync(any(), any(), any());
        
        // Configure team member service mocks
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<List<TeamMember>> successCallback = 
                invocation.getArgument(0);
            successCallback.accept(testTeamMembers);
            return null;
        }).when(mockTeamMemberService).findAllAsync(any(), any());
        
        // Register mocks with TestModule - BOTH interface and implementation
        TestModule.setService(AttendanceService.class, mockAttendanceService);
        TestModule.setService(TestableAttendanceServiceImpl.class, mockAttendanceService);
        TestModule.setService(MeetingService.class, mockMeetingService);
        TestModule.setService(TestableMeetingServiceAsyncImpl.class, mockMeetingService);
        TestModule.setService(TeamMemberService.class, mockTeamMemberService);
        TestModule.setService(TestableTeamMemberServiceAsyncImpl.class, mockTeamMemberService);
        
        // Get services from TestModule
        attendanceService = TestModule.getService(AttendanceService.class);
        meetingService = TestModule.getService(MeetingService.class);
        teamMemberService = TestModule.getService(TeamMemberService.class);
        
        // Initialize JavaFX toolkit if needed
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
        
        // Create the view model on the JavaFX thread
        TestUtils.runOnFxThreadAndWait(() -> {
            viewModel = new AttendanceMvvmViewModel(attendanceService, teamMemberService, meetingService);
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
        attendance1.setTeamMember(member1);
        attendance1.setPresent(true);
        attendance1.setNotes("On time");
        testAttendances.add(attendance1);
        
        Attendance attendance2 = new Attendance();
        attendance2.setId(2L);
        attendance2.setMeeting(testMeeting);
        attendance2.setTeamMember(member2);
        attendance2.setPresent(true);
        attendance2.setNotes("Arrived late");
        testAttendances.add(attendance2);
        
        Attendance attendance3 = new Attendance();
        attendance3.setId(3L);
        attendance3.setMeeting(testMeeting);
        attendance3.setTeamMember(member3);
        attendance3.setPresent(false);
        attendance3.setNotes("Absent - sick");
        testAttendances.add(attendance3);
        
        // Link attendances to meeting
        testMeeting.setAttendances(testAttendances);
    }
    
    @Test
    public void testInitialState() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Verify initial state
            assertTrue(viewModel.getAttendanceRecords().isEmpty());
            assertNull(viewModel.getCurrentMeeting());
            assertNull(viewModel.getCurrentProject());
            assertFalse(viewModel.isLoading());
            
            // Verify commands exist
            assertNotNull(viewModel.getLoadMeetingsCommand());
            assertNotNull(viewModel.getLoadAttendanceCommand());
            assertNotNull(viewModel.getSaveAttendanceCommand());
            assertNotNull(viewModel.getRefreshCommand());
            
            // Check command executability - use !isExecutable instead of isNotExecutable
            assertTrue(viewModel.getLoadMeetingsCommand().isExecutable());
            assertTrue(viewModel.getRefreshCommand().isExecutable());
            assertFalse(viewModel.getLoadAttendanceCommand().isExecutable()); // No meeting selected
            assertFalse(viewModel.getSaveAttendanceCommand().isExecutable()); // No changes
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
            // Verify meetings were loaded
            assertFalse(viewModel.getMeetings().isEmpty());
            assertEquals(1, viewModel.getMeetings().size());
            assertEquals(testMeeting, viewModel.getMeetings().get(0));
        });
    }
    
    @Test
    public void testLoadAttendanceForMeeting() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set current meeting
            viewModel.setCurrentMeeting(testMeeting);
            
            // Load attendance command should now be executable
            assertTrue(viewModel.getLoadAttendanceCommand().isExecutable());
            
            // Execute load attendance command
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
            
            // Verify attendance data
            AttendanceMvvmViewModel.AttendanceRecord record1 = viewModel.getAttendanceRecords().get(0);
            assertEquals("John Doe", record1.getTeamMemberName());
            assertTrue(record1.isPresent());
            assertEquals("On time", record1.getNotes());
            
            AttendanceMvvmViewModel.AttendanceRecord record2 = viewModel.getAttendanceRecords().get(1);
            assertEquals("Jane Smith", record2.getTeamMemberName());
            assertTrue(record2.isPresent());
            assertEquals("Arrived late", record2.getNotes());
            
            AttendanceMvvmViewModel.AttendanceRecord record3 = viewModel.getAttendanceRecords().get(2);
            assertEquals("Bob Johnson", record3.getTeamMemberName());
            assertFalse(record3.isPresent());
            assertEquals("Absent - sick", record3.getNotes());
        });
    }
    
    @Test
    public void testMarkAttendance() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set current meeting and load attendance
            viewModel.setCurrentMeeting(testMeeting);
            viewModel.getLoadAttendanceCommand().execute();
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Get the first attendance record (John Doe - currently present)
            AttendanceMvvmViewModel.AttendanceRecord record = viewModel.getAttendanceRecords().get(0);
            
            // Mark as absent
            record.setPresent(false);
            record.setNotes("Left early");
            
            // Save attendance command should be executable after changes
            assertTrue(viewModel.getSaveAttendanceCommand().isExecutable());
            
            // Execute save command
            viewModel.getSaveAttendanceCommand().execute();
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Verify changes were applied
            AttendanceMvvmViewModel.AttendanceRecord updatedRecord = viewModel.getAttendanceRecords().get(0);
            assertFalse(updatedRecord.isPresent());
            assertEquals("Left early", updatedRecord.getNotes());
        });
    }
    
    @Test
    public void testAttendanceStatistics() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set current meeting and load attendance
            viewModel.setCurrentMeeting(testMeeting);
            viewModel.getLoadAttendanceCommand().execute();
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Verify attendance statistics
            assertEquals(3, viewModel.getTotalMembers());
            assertEquals(2, viewModel.getPresentCount());
            assertEquals(1, viewModel.getAbsentCount());
            
            // Verify percentage calculation (2 out of 3 = 66.67%)
            double expectedPercentage = (2.0 / 3.0) * 100;
            assertEquals(expectedPercentage, viewModel.getAttendancePercentage(), 0.01);
        });
    }
    
    @Test
    public void testCreateAttendanceForAllMembers() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize with project to load team members
            viewModel.initWithProject(testProject);
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set current meeting (empty attendance)
            Meeting emptyMeeting = new Meeting();
            emptyMeeting.setId(2L);
            emptyMeeting.setDate(LocalDate.now().plusDays(1));
            emptyMeeting.setStartTime(LocalTime.of(16, 0));
            emptyMeeting.setEndTime(LocalTime.of(18, 0));
            emptyMeeting.setProject(testProject);
            
            viewModel.setCurrentMeeting(emptyMeeting);
            
            // Execute command to create attendance for all members
            viewModel.getCreateAttendanceForAllMembersCommand().execute();
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Verify attendance records were created for all team members
            assertEquals(3, viewModel.getAttendanceRecords().size());
            
            // All should be marked as present by default
            for (AttendanceMvvmViewModel.AttendanceRecord record : viewModel.getAttendanceRecords()) {
                assertTrue(record.isPresent());
                assertEquals("", record.getNotes());
            }
        });
    }
    
    @Test
    public void testFilterByAttendanceStatus() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set current meeting and load attendance
            viewModel.setCurrentMeeting(testMeeting);
            viewModel.getLoadAttendanceCommand().execute();
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initially showing all records
            assertEquals(3, viewModel.getFilteredAttendanceRecords().size());
            
            // Filter to show only present members
            viewModel.setShowPresentOnly(true);
            assertEquals(2, viewModel.getFilteredAttendanceRecords().size());
            
            // Verify all filtered records are present
            for (AttendanceMvvmViewModel.AttendanceRecord record : viewModel.getFilteredAttendanceRecords()) {
                assertTrue(record.isPresent());
            }
            
            // Filter to show only absent members
            viewModel.setShowPresentOnly(false);
            viewModel.setShowAbsentOnly(true);
            assertEquals(1, viewModel.getFilteredAttendanceRecords().size());
            
            // Verify filtered record is absent
            AttendanceMvvmViewModel.AttendanceRecord absentRecord = viewModel.getFilteredAttendanceRecords().get(0);
            assertFalse(absentRecord.isPresent());
            assertEquals("Bob Johnson", absentRecord.getTeamMemberName());
            
            // Reset filters
            viewModel.setShowAbsentOnly(false);
            assertEquals(3, viewModel.getFilteredAttendanceRecords().size());
        });
    }
    
    @Test
    public void testBulkAttendanceOperations() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set current meeting and load attendance
            viewModel.setCurrentMeeting(testMeeting);
            viewModel.getLoadAttendanceCommand().execute();
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Mark all as present
            viewModel.getMarkAllPresentCommand().execute();
            
            // Verify all are marked as present
            for (AttendanceMvvmViewModel.AttendanceRecord record : viewModel.getAttendanceRecords()) {
                assertTrue(record.isPresent());
            }
            
            // Mark all as absent
            viewModel.getMarkAllAbsentCommand().execute();
            
            // Verify all are marked as absent
            for (AttendanceMvvmViewModel.AttendanceRecord record : viewModel.getAttendanceRecords()) {
                assertFalse(record.isPresent());
            }
        });
    }
    
    @Test
    public void testMeetingSelection() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize with project to load meetings
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
            assertNull(viewModel.getCurrentMeeting());
            assertFalse(viewModel.getLoadAttendanceCommand().isExecutable());
            
            // Select a meeting
            viewModel.setCurrentMeeting(testMeeting);
            
            // Verify meeting was selected
            assertEquals(testMeeting, viewModel.getCurrentMeeting());
            
            // Load attendance command should now be executable
            assertTrue(viewModel.getLoadAttendanceCommand().isExecutable());
        });
    }
    
    @Test
    public void testLoadMeetingsCommand() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set project and execute load meetings command
            viewModel.setCurrentProject(testProject);
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
            assertEquals(1, viewModel.getMeetings().size());
            assertEquals(testMeeting, viewModel.getMeetings().get(0));
            
            // Error message should be cleared
            assertNull(viewModel.getErrorMessage());
        });
    }
    
    @Test
    public void testRefreshCommand() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set project and meeting
            viewModel.setCurrentProject(testProject);
            viewModel.setCurrentMeeting(testMeeting);
            
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
            // Verify both meetings and attendance were refreshed
            assertEquals(1, viewModel.getMeetings().size());
            assertEquals(3, viewModel.getAttendanceRecords().size());
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
            AttendanceMvvmViewModel errorViewModel = new AttendanceMvvmViewModel(
                TestModule.getService(AttendanceService.class),
                TestModule.getService(TeamMemberService.class),
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
    public void testPropertyBindings() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Test project property
            viewModel.setCurrentProject(testProject);
            assertEquals(testProject, viewModel.getCurrentProject());
            assertEquals(testProject, viewModel.currentProjectProperty().get());
            
            // Test meeting property
            viewModel.setCurrentMeeting(testMeeting);
            assertEquals(testMeeting, viewModel.getCurrentMeeting());
            assertEquals(testMeeting, viewModel.currentMeetingProperty().get());
            
            // Test loading property
            assertFalse(viewModel.isLoading());
            assertEquals(Boolean.FALSE, viewModel.loadingProperty().get());
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
            assertFalse(viewModel.getMeetings().isEmpty());
            
            // Call dispose
            viewModel.dispose();
            
            // Verify collections were cleared
            assertTrue(viewModel.getMeetings().isEmpty());
            assertTrue(viewModel.getAttendanceRecords().isEmpty());
        });
    }
}