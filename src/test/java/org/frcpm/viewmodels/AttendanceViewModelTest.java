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
        assertEquals(testMeeting, viewModel.getMeeting());
        assertEquals(testMeeting.getDate().toString(), viewModel.getMeetingDate());
        assertEquals(testMeeting.getStartTime() + " - " + testMeeting.getEndTime(), viewModel.getMeetingTime());
        assertEquals("Meeting Attendance", viewModel.getMeetingTitle());
    }

    @Test
    public void testLoadAttendanceData() {
        // Arrange
        viewModel.initWithMeeting(testMeeting);

        // Assert - verify service methods were called
        verify(teamMemberService).findAll();
        verify(attendanceService).findByMeeting(testMeeting);

        // Verify attendance records were created
        assertEquals(2, viewModel.getAttendanceRecords().size());
        
        // First member should have attendance
        AttendanceViewModel.AttendanceRecord record1 = viewModel.getAttendanceRecords().get(0);
        assertEquals(testMembers.get(0), record1.getTeamMember());
        assertTrue(record1.isPresent());
        assertEquals(LocalTime.of(16, 0), record1.getArrivalTime());
        assertEquals(LocalTime.of(18, 0), record1.getDepartureTime());
        
        // Second member should not have attendance
        AttendanceViewModel.AttendanceRecord record2 = viewModel.getAttendanceRecords().get(1);
        assertEquals(testMembers.get(1), record2.getTeamMember());
        assertFalse(record2.isPresent());
        assertNull(record2.getAttendance());
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
    }

    @Test
    public void testPresentMemberIds() {
        // Arrange
        viewModel.initWithMeeting(testMeeting);
        
        // Act
        List<Long> presentMemberIds = viewModel.getPresentMemberIds();
        
        // Assert
        assertEquals(1, presentMemberIds.size());
        assertEquals(testMembers.get(0).getId(), presentMemberIds.get(0));
    }

    @Test
    public void testAttendanceRecordPresenceChange() {
        // Arrange
        viewModel.initWithMeeting(testMeeting);
        AttendanceViewModel.AttendanceRecord record = viewModel.getAttendanceRecords().get(1);
        
        // Initially not present
        assertFalse(record.isPresent());
        assertNull(record.getArrivalTime());
        assertNull(record.getDepartureTime());
        
        // Act - mark as present
        record.setPresent(true);
        
        // Assert - default times should be set
        assertEquals(testMeeting.getStartTime(), record.getArrivalTime());
        assertEquals(testMeeting.getEndTime(), record.getDepartureTime());
    }

    @Test
    public void testCanSaveAttendance() {
        // Without meeting
        assertFalse(viewModel.getSaveAttendanceCommand().canExecute());
        
        // With meeting
        viewModel.initWithMeeting(testMeeting);
        assertTrue(viewModel.getSaveAttendanceCommand().canExecute());
    }

    @Test
    public void testCanLoadAttendanceData() {
        // Without meeting
        assertFalse(viewModel.getLoadAttendanceCommand().canExecute());
        
        // With meeting
        viewModel.initWithMeeting(testMeeting);
        assertTrue(viewModel.getLoadAttendanceCommand().canExecute());
    }
}