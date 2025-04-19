package org.frcpm.viewmodels;

import org.frcpm.models.Attendance;
import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.models.TeamMember;
import org.frcpm.services.AttendanceService;
import org.frcpm.services.MeetingService;
import org.frcpm.services.TeamMemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the AttendanceViewModel class.
 */
public class AttendanceViewModelTest {

    @Mock
    private AttendanceService attendanceService;

    @Mock
    private TeamMemberService teamMemberService;

    @Mock
    private MeetingService meetingService;

    private AttendanceViewModel viewModel;
    private Meeting testMeeting;
    private Project testProject;
    private List<TeamMember> testMembers;
    private List<Attendance> testAttendances;

    @BeforeEach
    public void setUp() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);

        // Create test data
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");

        testMeeting = new Meeting();
        testMeeting.setId(1L);
        testMeeting.setProject(testProject);
        testMeeting.setDate(LocalDate.now());
        testMeeting.setStartTime(LocalTime.of(16, 0));
        testMeeting.setEndTime(LocalTime.of(18, 0));
        testMeeting.setNotes("Test meeting");

        // Create test team members
        testMembers = new ArrayList<>();
        TeamMember member1 = new TeamMember("user1", "John", "Doe", "john@example.com");
        member1.setId(1L);
        TeamMember member2 = new TeamMember("user2", "Jane", "Smith", "jane@example.com");
        member2.setId(2L);
        testMembers.add(member1);
        testMembers.add(member2);

        // Create test attendance records
        testAttendances = new ArrayList<>();
        Attendance attendance1 = new Attendance(testMeeting, member1, true);
        attendance1.setId(1L);
        attendance1.setArrivalTime(LocalTime.of(16, 0));
        attendance1.setDepartureTime(LocalTime.of(18, 0));
        testAttendances.add(attendance1);

        // Set up mock service responses
        when(teamMemberService.findAll()).thenReturn(testMembers);
        when(attendanceService.findByMeeting(any(Meeting.class))).thenReturn(testAttendances);
        
        // Create the ViewModel with mocked services
        viewModel = new AttendanceViewModel(attendanceService, teamMemberService, meetingService);
    }

    @Test
    public void testInitWithMeeting() {
        // Act
        viewModel.initWithMeeting(testMeeting);

        // Assert
        assertEquals(testMeeting, viewModel.getMeeting(), "Meeting should be set correctly");
        assertEquals(testMeeting.getDate().toString(), viewModel.getMeetingDate(), "Meeting date should be set correctly");
        assertEquals(testMeeting.getStartTime() + " - " + testMeeting.getEndTime(), viewModel.getMeetingTime(), "Meeting time should be formatted correctly");
        assertEquals("Meeting Attendance", viewModel.getMeetingTitle(), "Meeting title should be set correctly");
    }

    @Test
    public void testLoadAttendanceData() {
        // Arrange
        viewModel.initWithMeeting(testMeeting);

        // Assert - verify service methods were called
        verify(teamMemberService).findAll();
        verify(attendanceService).findByMeeting(testMeeting);

        // Verify attendance records were created
        assertEquals(2, viewModel.getAttendanceRecords().size(), "Should create records for all team members");
        
        // First member should have attendance
        AttendanceViewModel.AttendanceRecord record1 = viewModel.getAttendanceRecords().get(0);
        assertEquals(testMembers.get(0), record1.getTeamMember(), "First record should be for first team member");
        assertTrue(record1.isPresent(), "First member should be marked as present");
        assertEquals(LocalTime.of(16, 0), record1.getArrivalTime(), "First member arrival time should match attendance record");
        assertEquals(LocalTime.of(18, 0), record1.getDepartureTime(), "First member departure time should match attendance record");
        
        // Second member should not have attendance
        AttendanceViewModel.AttendanceRecord record2 = viewModel.getAttendanceRecords().get(1);
        assertEquals(testMembers.get(1), record2.getTeamMember(), "Second record should be for second team member");
        assertFalse(record2.isPresent(), "Second member should be marked as not present");
        assertNull(record2.getAttendance(), "Second member should not have an attendance record");
    }

    @Test
    public void testSaveAttendance() {
        // Arrange
        when(attendanceService.createAttendance(anyLong(), anyLong(), anyBoolean()))
            .thenAnswer(invocation -> {
                Long meetingId = invocation.getArgument(0);
                Long memberId = invocation.getArgument(1);
                Boolean present = invocation.getArgument(2);
                
                Attendance newAttendance = new Attendance();
                newAttendance.setId(100L);
                newAttendance.setMeeting(testMeeting);
                
                TeamMember member = testMembers.stream()
                    .filter(m -> m.getId().equals(memberId))
                    .findFirst()
                    .orElse(null);
                newAttendance.setMember(member);
                newAttendance.setPresent(present);
                
                return newAttendance;
            });
        
        when(attendanceService.updateAttendance(anyLong(), anyBoolean(), any(), any()))
            .thenAnswer(invocation -> {
                Long attendanceId = invocation.getArgument(0);
                Boolean present = invocation.getArgument(1);
                LocalTime arrivalTime = invocation.getArgument(2);
                LocalTime departureTime = invocation.getArgument(3);
                
                Attendance attendance = testAttendances.stream()
                    .filter(a -> a.getId().equals(attendanceId))
                    .findFirst()
                    .orElse(new Attendance());
                
                attendance.setPresent(present);
                attendance.setArrivalTime(arrivalTime);
                attendance.setDepartureTime(departureTime);
                
                return attendance;
            });
        
        viewModel.initWithMeeting(testMeeting);
        
        // Get the second record (which has no attendance) and mark it as present
        AttendanceViewModel.AttendanceRecord record = viewModel.getAttendanceRecords().get(1);
        record.setPresent(true);
        record.setArrivalTime(LocalTime.of(16, 15));
        record.setDepartureTime(LocalTime.of(17, 45));
        
        // Ensure dirty flag is set for save command to be executable
        viewModel.setDirty(true);
        
        // Act
        viewModel.getSaveAttendanceCommand().execute();
        
        // Assert
        // Verify createAttendance was called for the second member
        verify(attendanceService).createAttendance(
            eq(testMeeting.getId()),
            eq(testMembers.get(1).getId()),
            eq(true)
        );
        
        // Verify updateAttendance was called for both members
        verify(attendanceService, times(2)).updateAttendance(
            anyLong(),
            anyBoolean(),
            any(LocalTime.class),
            any(LocalTime.class)
        );
        
        // Verify dirty flag was cleared
        assertFalse(viewModel.isDirty(), "Dirty flag should be cleared after save");
    }

    @Test
    public void testPresentMemberIds() {
        // Arrange
        viewModel.initWithMeeting(testMeeting);
        
        // Act
        List<Long> presentMemberIds = viewModel.getPresentMemberIds();
        
        // Assert
        assertEquals(1, presentMemberIds.size(), "Should have one present member");
        assertEquals(testMembers.get(0).getId(), presentMemberIds.get(0), "Present member ID should match first member");
    }

    @Test
    public void testAttendanceRecordPresenceChanged() {
        // Arrange
        viewModel.initWithMeeting(testMeeting);
        
        // We need to create a new Attendance with the meeting for this specific test
        // This is necessary to ensure the Attendance record has the meeting reference
        Attendance attendanceForTest = new Attendance();
        attendanceForTest.setMeeting(testMeeting);
        attendanceForTest.setPresent(false);
        
        AttendanceViewModel.AttendanceRecord record = new AttendanceViewModel.AttendanceRecord(
            testMembers.get(1), attendanceForTest);
        
        // Initially not present
        assertFalse(record.isPresent(), "Second member should initially be marked as not present");
        assertNull(record.getArrivalTime(), "Second member should not have an arrival time initially");
        assertNull(record.getDepartureTime(), "Second member should not have a departure time initially");
        
        // Act - mark as present
        record.setPresent(true);
        
        // Assert - default times should be set
        assertEquals(testMeeting.getStartTime(), record.getArrivalTime(), 
            "Arrival time should default to meeting start time when member is marked present");
        assertEquals(testMeeting.getEndTime(), record.getDepartureTime(), 
            "Departure time should default to meeting end time when member is marked present");
    }

    @Test
    public void testCanSaveAttendance() {
        // Without meeting
        assertFalse(viewModel.getSaveAttendanceCommand().canExecute(), "Should not be able to save without a meeting");
        
        // With meeting but not dirty
        viewModel.initWithMeeting(testMeeting);
        assertFalse(viewModel.getSaveAttendanceCommand().canExecute(), "Should not be able to save when not dirty");
        
        // With meeting and dirty
        viewModel.setDirty(true);
        assertTrue(viewModel.getSaveAttendanceCommand().canExecute(), "Should be able to save with meeting and dirty flag");
    }

    @Test
    public void testCanLoadAttendanceData() {
        // Without meeting
        assertFalse(viewModel.getLoadAttendanceCommand().canExecute(), "Should not be able to load data without a meeting");
        
        // With meeting
        viewModel.initWithMeeting(testMeeting);
        assertTrue(viewModel.getLoadAttendanceCommand().canExecute(), "Should be able to load data with a meeting");
    }
    
    @Test
    public void testValidationWithMeeting() {
        // Act
        viewModel.initWithMeeting(testMeeting);
        
        // Assert
        assertTrue(viewModel.isValid(), "ViewModel should be valid with a meeting");
        assertNull(viewModel.getErrorMessage(), "Error message should be null when valid");
    }
    
    @Test
    public void testValidationWithoutMeeting() {
        // Already in initial state - no meeting
        
        // Assert
        assertFalse(viewModel.isValid(), "ViewModel should not be valid without a meeting");
        assertNotNull(viewModel.getErrorMessage(), "Error message should not be null when invalid");
        assertTrue(viewModel.getErrorMessage().contains("No meeting selected"), 
            "Error message should mention missing meeting");
    }
    
    @Test
    public void testUpdateRecordTimes() {
        // Arrange
        viewModel.initWithMeeting(testMeeting);
        AttendanceViewModel.AttendanceRecord record = viewModel.getAttendanceRecords().get(0);
        LocalTime newArrival = LocalTime.of(16, 30);
        LocalTime newDeparture = LocalTime.of(17, 30);
        
        // Act
        viewModel.updateRecordTimes(record, newArrival, newDeparture);
        
        // Assert
        assertEquals(newArrival, record.getArrivalTime(), "Arrival time should be updated");
        assertEquals(newDeparture, record.getDepartureTime(), "Departure time should be updated");
        assertTrue(viewModel.isDirty(), "ViewModel should be marked as dirty after update");
    }
    
    @Test
    public void testUpdateRecordTimesWithInvalidTimes() {
        // Arrange
        viewModel.initWithMeeting(testMeeting);
        AttendanceViewModel.AttendanceRecord record = viewModel.getAttendanceRecords().get(0);
        LocalTime originalArrival = record.getArrivalTime();
        LocalTime originalDeparture = record.getDepartureTime();
        LocalTime newArrival = LocalTime.of(17, 30);
        LocalTime newDeparture = LocalTime.of(16, 30); // Before arrival time
        
        // Act
        viewModel.updateRecordTimes(record, newArrival, newDeparture);
        
        // Assert
        assertEquals(originalArrival, record.getArrivalTime(), "Arrival time should not be updated with invalid times");
        assertEquals(originalDeparture, record.getDepartureTime(), "Departure time should not be updated with invalid times");
        assertNotNull(viewModel.getErrorMessage(), "Error message should be set with invalid times");
        assertTrue(viewModel.getErrorMessage().contains("Departure time cannot be before arrival time"), 
            "Error message should mention invalid time order");
    }
    
    @Test
    public void testParseTime() {
        // Valid time
        LocalTime time = viewModel.parseTime("14:30");
        assertEquals(LocalTime.of(14, 30), time, "Should correctly parse valid time");
        
        // Invalid time
        LocalTime invalidTime = viewModel.parseTime("invalid");
        assertNull(invalidTime, "Should return null for invalid time format");
        assertNotNull(viewModel.getErrorMessage(), "Error message should be set for invalid time format");
    }
    
    @Test
    public void testFormatTime() {
        // Valid time
        String formattedTime = viewModel.formatTime(LocalTime.of(14, 30));
        assertEquals("14:30", formattedTime, "Should correctly format time");
        
        // Null time
        String nullTime = viewModel.formatTime(null);
        assertEquals("", nullTime, "Should return empty string for null time");
    }
    
    @Test
    public void testPropertyListenersMarkDirty() {
        // Arrange
        viewModel.initWithMeeting(testMeeting);
        viewModel.setDirty(false);
        
        // Act - set selected record
        viewModel.setSelectedRecord(viewModel.getAttendanceRecords().get(0));
        
        // Assert
        assertTrue(viewModel.isDirty(), "Setting selected record should mark viewModel as dirty");
    }
}