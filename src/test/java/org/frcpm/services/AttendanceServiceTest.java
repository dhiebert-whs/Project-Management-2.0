package org.frcpm.services;

import org.frcpm.config.DatabaseConfig;
import org.frcpm.models.Attendance;
import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.models.TeamMember;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AttendanceServiceTest {

    private AttendanceService attendanceService;
    private MeetingService meetingService;
    private ProjectService projectService;
    private TeamMemberService teamMemberService;

    private Project testProject;
    private Meeting testMeeting;
    private TeamMember testMember1;
    private TeamMember testMember2;

    @BeforeEach
    public void setUp() {
        DatabaseConfig.initialize();

        attendanceService = ServiceFactory.getAttendanceService();
        meetingService = ServiceFactory.getMeetingService();
        projectService = ServiceFactory.getProjectService();
        teamMemberService = ServiceFactory.getTeamMemberService();

        // Create test project
        testProject = projectService.createProject(
                "Attendance Test Project",
                LocalDate.now(),
                LocalDate.now().plusWeeks(6),
                LocalDate.now().plusWeeks(8));

        // Create test meeting
        testMeeting = meetingService.createMeeting(
                LocalDate.now().plusDays(1),
                LocalTime.of(18, 0), // 6:00 PM
                LocalTime.of(20, 0), // 8:00 PM
                testProject.getId(),
                "Test meeting notes");

        // Create test team members
        testMember1 = teamMemberService.createTeamMember(
                "attendancetest1",
                "Attendance",
                "Test1",
                "attendance1@example.com",
                "555-1234",
                false);

        testMember2 = teamMemberService.createTeamMember(
                "attendancetest2",
                "Attendance",
                "Test2",
                "attendance2@example.com",
                "555-5678",
                false);
    }

    @AfterEach
    public void tearDown() {
        // Clean up test data
        try {
            // Delete attendance records
            List<Attendance> attendances = attendanceService.findByMeeting(testMeeting);
            for (Attendance attendance : attendances) {
                attendanceService.deleteById(attendance.getId());
            }

            // Delete meeting
            meetingService.deleteById(testMeeting.getId());

            // Delete team members
            teamMemberService.deleteById(testMember1.getId());
            teamMemberService.deleteById(testMember2.getId());

            // Delete project
            projectService.deleteById(testProject.getId());
        } catch (Exception e) {
            // Log but continue with cleanup
            System.err.println("Error during cleanup: " + e.getMessage());
        }

        DatabaseConfig.shutdown();
    }

    @Test
    public void testCreateAttendance() {
        Attendance attendance = attendanceService.createAttendance(
                testMeeting.getId(),
                testMember1.getId(),
                true);

        assertNotNull(attendance);
        assertNotNull(attendance.getId());
        assertEquals(testMeeting.getId(), attendance.getMeeting().getId());
        assertEquals(testMember1.getId(), attendance.getMember().getId());
        assertTrue(attendance.isPresent());

        // Check if default times are set
        assertNotNull(attendance.getArrivalTime());
        assertEquals(testMeeting.getStartTime(), attendance.getArrivalTime());
        assertNotNull(attendance.getDepartureTime());
        assertEquals(testMeeting.getEndTime(), attendance.getDepartureTime());
    }

    @Test
    public void testCreateAbsenceAttendance() {
        Attendance attendance = attendanceService.createAttendance(
                testMeeting.getId(),
                testMember1.getId(),
                false);

        assertNotNull(attendance);
        assertNotNull(attendance.getId());
        assertEquals(testMeeting.getId(), attendance.getMeeting().getId());
        assertEquals(testMember1.getId(), attendance.getMember().getId());
        assertFalse(attendance.isPresent());

        // Check that times are not set for absences
        assertNull(attendance.getArrivalTime());
        assertNull(attendance.getDepartureTime());
    }

    @Test
    public void testUpdateAttendance() {
        // Create an attendance record
        Attendance attendance = attendanceService.createAttendance(
                testMeeting.getId(),
                testMember1.getId(),
                true);

        // Update the attendance
        LocalTime newArrival = LocalTime.of(18, 15); // 15 minutes late
        LocalTime newDeparture = LocalTime.of(19, 45); // 15 minutes early

        Attendance updated = attendanceService.updateAttendance(
                attendance.getId(),
                true,
                newArrival,
                newDeparture);

        assertNotNull(updated);
        assertTrue(updated.isPresent());
        assertEquals(newArrival, updated.getArrivalTime());
        assertEquals(newDeparture, updated.getDepartureTime());

        // Check DB record
        Optional<Attendance> found = attendanceService.findByMeetingAndMember(testMeeting, testMember1);
        assertTrue(found.isPresent());
        assertEquals(newArrival, found.get().getArrivalTime());
        assertEquals(newDeparture, found.get().getDepartureTime());
    }

    @Test
    public void testUpdateAttendanceToAbsent() {
        // Create a present attendance record
        Attendance attendance = attendanceService.createAttendance(
                testMeeting.getId(),
                testMember1.getId(),
                true);

        // Update to absent
        Attendance updated = attendanceService.updateAttendance(
                attendance.getId(),
                false,
                null,
                null);

        assertNotNull(updated);
        assertFalse(updated.isPresent());
        assertNull(updated.getArrivalTime());
        assertNull(updated.getDepartureTime());

        // Check DB record
        Optional<Attendance> found = attendanceService.findByMeetingAndMember(testMeeting, testMember1);
        assertTrue(found.isPresent());
        assertFalse(found.get().isPresent());
        assertNull(found.get().getArrivalTime());
        assertNull(found.get().getDepartureTime());
    }

    @Test
    public void testFindByMeeting() {
        // Create attendance records
        attendanceService.createAttendance(
                testMeeting.getId(),
                testMember1.getId(),
                true);

        attendanceService.createAttendance(
                testMeeting.getId(),
                testMember2.getId(),
                false);

        // Find by meeting
        List<Attendance> attendances = attendanceService.findByMeeting(testMeeting);
        assertNotNull(attendances);
        assertEquals(2, attendances.size());
        assertTrue(attendances.stream().allMatch(a -> a.getMeeting().getId().equals(testMeeting.getId())));
    }

    @Test
    public void testFindByMember() {
        // Create attendance records for the same member but different meetings
        Meeting anotherMeeting = meetingService.createMeeting(
                LocalDate.now().plusDays(3),
                LocalTime.of(18, 0),
                LocalTime.of(20, 0),
                testProject.getId(),
                "Another test meeting");

        attendanceService.createAttendance(
                testMeeting.getId(),
                testMember1.getId(),
                true);

        attendanceService.createAttendance(
                anotherMeeting.getId(),
                testMember1.getId(),
                false);

        // Find by member
        List<Attendance> attendances = attendanceService.findByMember(testMember1);
        assertNotNull(attendances);
        assertEquals(2, attendances.size());
        assertTrue(attendances.stream().allMatch(a -> a.getMember().getId().equals(testMember1.getId())));

        // Clean up the additional meeting
        attendanceService.findByMeeting(anotherMeeting).forEach(a -> attendanceService.deleteById(a.getId()));
        meetingService.deleteById(anotherMeeting.getId());
    }

    @Test
    public void testFindByMeetingAndMember() {
        // Create attendance records
        attendanceService.createAttendance(
                testMeeting.getId(),
                testMember1.getId(),
                true);

        attendanceService.createAttendance(
                testMeeting.getId(),
                testMember2.getId(),
                false);

        // Find by meeting and member
        Optional<Attendance> attendance = attendanceService.findByMeetingAndMember(testMeeting, testMember1);
        assertTrue(attendance.isPresent());
        assertEquals(testMeeting.getId(), attendance.get().getMeeting().getId());
        assertEquals(testMember1.getId(), attendance.get().getMember().getId());
        assertTrue(attendance.get().isPresent());

        attendance = attendanceService.findByMeetingAndMember(testMeeting, testMember2);
        assertTrue(attendance.isPresent());
        assertEquals(testMeeting.getId(), attendance.get().getMeeting().getId());
        assertEquals(testMember2.getId(), attendance.get().getMember().getId());
        assertFalse(attendance.get().isPresent());
    }

    @Test
    public void testRecordAttendanceForMeeting() {
        // Create a third member for this test
        TeamMember testMember3 = teamMemberService.createTeamMember(
                "attendancetest3",
                "Attendance",
                "Test3",
                "attendance3@example.com",
                "555-9012",
                false);

        // Record attendance for all members, with members 1 and 3 present
        List<Long> presentMemberIds = Arrays.asList(testMember1.getId(), testMember3.getId());
        int count = attendanceService.recordAttendanceForMeeting(testMeeting.getId(), presentMemberIds);

        // Should have created 3 attendance records
        assertEquals(3, count);

        // Verify attendance records
        List<Attendance> attendances = attendanceService.findByMeeting(testMeeting);
        assertEquals(3, attendances.size());

        // Check member 1 is present
        Optional<Attendance> member1Attendance = attendanceService.findByMeetingAndMember(testMeeting, testMember1);
        assertTrue(member1Attendance.isPresent());
        assertTrue(member1Attendance.get().isPresent());

        // Check member 2 is absent
        Optional<Attendance> member2Attendance = attendanceService.findByMeetingAndMember(testMeeting, testMember2);
        assertTrue(member2Attendance.isPresent());
        assertFalse(member2Attendance.get().isPresent());

        // Check member 3 is present
        Optional<Attendance> member3Attendance = attendanceService.findByMeetingAndMember(testMeeting, testMember3);
        assertTrue(member3Attendance.isPresent());
        assertTrue(member3Attendance.get().isPresent());

        // Clean up the additional member
        attendanceService.findByMember(testMember3).forEach(a -> attendanceService.deleteById(a.getId()));
        teamMemberService.deleteById(testMember3.getId());
    }

    @Test
    public void testGetAttendanceStatistics() {
        // Create a series of meetings
        Meeting meeting1 = meetingService.createMeeting(
                LocalDate.now().minusDays(7),
                LocalTime.of(18, 0),
                LocalTime.of(20, 0),
                testProject.getId(),
                "Past meeting 1");

        Meeting meeting2 = meetingService.createMeeting(
                LocalDate.now().minusDays(3),
                LocalTime.of(18, 0),
                LocalTime.of(20, 0),
                testProject.getId(),
                "Past meeting 2");

        // Record attendance - member1 attended both, member2 missed one
        attendanceService.createAttendance(meeting1.getId(), testMember1.getId(), true);
        attendanceService.createAttendance(meeting1.getId(), testMember2.getId(), true);
        attendanceService.createAttendance(meeting2.getId(), testMember1.getId(), true);
        attendanceService.createAttendance(meeting2.getId(), testMember2.getId(), false);
        attendanceService.createAttendance(testMeeting.getId(), testMember1.getId(), true);
        attendanceService.createAttendance(testMeeting.getId(), testMember2.getId(), true);

        // Get statistics for member1
        Map<String, Object> member1Stats = attendanceService.getAttendanceStatistics(testMember1.getId());
        assertNotNull(member1Stats);
        assertEquals(testMember1.getId(), member1Stats.get("memberId"));
        assertEquals(testMember1.getFullName(), member1Stats.get("memberName"));
        assertEquals(3, member1Stats.get("totalMeetings"));
        assertEquals(3L, member1Stats.get("presentCount")); // All 3 meetings
        assertEquals(0, member1Stats.get("absentCount"));
        assertEquals(100.0, member1Stats.get("attendanceRate"));

        // Get statistics for member2
        Map<String, Object> member2Stats = attendanceService.getAttendanceStatistics(testMember2.getId());
        assertNotNull(member2Stats);
        assertEquals(testMember2.getId(), member2Stats.get("memberId"));
        assertEquals(testMember2.getFullName(), member2Stats.get("memberName"));
        assertEquals(3, member2Stats.get("totalMeetings"));
        assertEquals(2L, member2Stats.get("presentCount")); // 2 of 3 meetings
        assertEquals(1, member2Stats.get("absentCount"));
        assertEquals(66.67, member2Stats.get("attendanceRate"));
        ;

        // Clean up the additional meetings
        attendanceService.findByMeeting(meeting1).forEach(a -> attendanceService.deleteById(a.getId()));
        attendanceService.findByMeeting(meeting2).forEach(a -> attendanceService.deleteById(a.getId()));
        meetingService.deleteById(meeting1.getId());
        meetingService.deleteById(meeting2.getId());
    }

    @Test
    public void testDeleteById() {
        // Create an attendance record
        Attendance attendance = attendanceService.createAttendance(
                testMeeting.getId(),
                testMember1.getId(),
                true);

        Long id = attendance.getId();

        // Delete the attendance record
        boolean result = attendanceService.deleteById(id);
        assertTrue(result);

        // Verify the deletion
        Optional<Attendance> found = attendanceService.findByMeetingAndMember(testMeeting, testMember1);
        assertFalse(found.isPresent());
    }

    @Test
    public void testInvalidAttendanceCreation() {
        // Test null meeting ID
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.createAttendance(
                    null,
                    testMember1.getId(),
                    true);
        });
        assertTrue(exception.getMessage().contains("Meeting ID and Member ID cannot be null"));

        // Test null member ID
        exception = assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.createAttendance(
                    testMeeting.getId(),
                    null,
                    true);
        });
        assertTrue(exception.getMessage().contains("Meeting ID and Member ID cannot be null"));

        // Test non-existent meeting ID
        exception = assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.createAttendance(
                    999999L,
                    testMember1.getId(),
                    true);
        });
        assertTrue(exception.getMessage().contains("Meeting not found"));

        // Test non-existent member ID
        exception = assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.createAttendance(
                    testMeeting.getId(),
                    999999L,
                    true);
        });
        assertTrue(exception.getMessage().contains("Team member not found"));
    }

    @Test
    public void testInvalidAttendanceUpdate() {
        // Create an attendance record
        Attendance attendance = attendanceService.createAttendance(
                testMeeting.getId(),
                testMember1.getId(),
                true);

        // Test null attendance ID
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.updateAttendance(
                    null,
                    true,
                    LocalTime.of(18, 15),
                    LocalTime.of(19, 45));
        });
        assertTrue(exception.getMessage().contains("Attendance ID cannot be null"));

        // Test invalid departure time (before arrival)
        exception = assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.updateAttendance(
                    attendance.getId(),
                    true,
                    LocalTime.of(19, 0),
                    LocalTime.of(18, 0) // Before arrival time
            );
        });
        assertTrue(exception.getMessage().contains("Departure time cannot be before arrival time"));
    }
}