// src/test/java/org/frcpm/services/impl/AttendanceServiceTest.java

package org.frcpm.services.impl;

import org.frcpm.models.Attendance;
import org.frcpm.models.Meeting;
import org.frcpm.models.TeamMember;
import org.frcpm.repositories.spring.AttendanceRepository;
import org.frcpm.repositories.spring.MeetingRepository;
import org.frcpm.repositories.spring.TeamMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Test class for AttendanceService implementation using Spring Boot testing patterns.
 */
@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {
    
    @Mock
    private AttendanceRepository attendanceRepository;
    
    @Mock
    private MeetingRepository meetingRepository;
    
    @Mock
    private TeamMemberRepository teamMemberRepository;
    
    private AttendanceServiceImpl attendanceService;
    
    private Attendance testAttendance;
    private Meeting testMeeting;
    private TeamMember testMember;
    
    @BeforeEach
    void setUp() {
        // Create test objects
        testMeeting = createTestMeeting();
        testMember = createTestMember();
        testAttendance = createTestAttendance();
        
        // Configure mock repository responses
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(testAttendance));
        when(attendanceRepository.findAll()).thenReturn(List.of(testAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Configure meeting repository
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(testMeeting));
        
        // Configure team member repository
        when(teamMemberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(teamMemberRepository.findAll()).thenReturn(List.of(testMember));
        
        // Configure attendance repository specialized methods
        when(attendanceRepository.findByMeeting(testMeeting)).thenReturn(List.of(testAttendance));
        when(attendanceRepository.findByMember(testMember)).thenReturn(List.of(testAttendance));
        when(attendanceRepository.findByMeetingAndMember(any(Meeting.class), any(TeamMember.class)))
            .thenReturn(Optional.of(testAttendance));
        
        // Create service with injected mocks
        attendanceService = new AttendanceServiceImpl(
            attendanceRepository,
            meetingRepository,
            teamMemberRepository
        );
    }
    
    /**
     * Creates a test meeting for use in tests.
     */
    private Meeting createTestMeeting() {
        Meeting meeting = new Meeting();
        meeting.setId(1L);
        meeting.setDate(LocalDate.now().plusDays(1));
        meeting.setStartTime(LocalTime.of(10, 0));
        meeting.setEndTime(LocalTime.of(11, 0));
        return meeting;
    }
    
    /**
     * Creates a test team member for use in tests.
     */
    private TeamMember createTestMember() {
        TeamMember member = new TeamMember();
        member.setId(1L);
        member.setUsername("testuser");
        member.setFirstName("Test");
        member.setLastName("User");
        return member;
    }
    
    /**
     * Creates a test attendance record for use in tests.
     */
    private Attendance createTestAttendance() {
        Attendance attendance = new Attendance(testMeeting, testMember, true);
        attendance.setId(1L);
        attendance.setArrivalTime(LocalTime.of(10, 0));
        attendance.setDepartureTime(LocalTime.of(11, 0));
        return attendance;
    }
    
    @Test
    void testFindById() {
        // Execute
        Attendance result = attendanceService.findById(1L);
        
        // Verify
        assertNotNull(result, "Result should not be null");
        assertEquals(1L, result.getId(), "Attendance ID should match");
        assertEquals(testMeeting, result.getMeeting(), "Meeting should match");
        assertEquals(testMember, result.getMember(), "Team member should match");
        
        // Verify repository was called exactly once with the correct ID
        verify(attendanceRepository, times(1)).findById(1L);
    }
    
    @Test
    void testFindAll() {
        // Execute
        List<Attendance> results = attendanceService.findAll();
        
        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(1L, results.get(0).getId());
        
        // Verify repository was called
        verify(attendanceRepository).findAll();
    }
    
    @Test
    void testSave() {
        // Setup
        Attendance newAttendance = new Attendance(testMeeting, testMember, false);
        
        // Execute
        Attendance result = attendanceService.save(newAttendance);
        
        // Verify
        assertNotNull(result);
        assertEquals(testMeeting, result.getMeeting());
        assertEquals(testMember, result.getMember());
        assertFalse(result.isPresent());
        
        // Verify repository was called
        verify(attendanceRepository).save(newAttendance);
    }
    
    @Test
    void testDelete() {
        // Setup
        doNothing().when(attendanceRepository).delete(any(Attendance.class));
        
        // Execute
        attendanceService.delete(testAttendance);
        
        // Verify repository was called
        verify(attendanceRepository).delete(testAttendance);
    }
    
    @Test
    void testDeleteById() {
        // Setup - Use doNothing() for void methods instead of when().thenReturn()
        doNothing().when(attendanceRepository).deleteById(anyLong());
        
        // Execute - deleteById returns void, so don't capture return value
        attendanceService.deleteById(1L);
        
        // Verify repository was called
        verify(attendanceRepository).deleteById(1L);
    }
    
    @Test
    void testCount() {
        // Setup
        when(attendanceRepository.count()).thenReturn(5L);
        
        // Execute
        long result = attendanceService.count();
        
        // Verify
        assertEquals(5L, result);
        
        // Verify repository was called
        verify(attendanceRepository).count();
    }
    
    @Test
    void testFindByMeeting() {
        // Execute
        List<Attendance> results = attendanceService.findByMeeting(testMeeting);
        
        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(testAttendance, results.get(0));
        
        // Verify repository was called
        verify(attendanceRepository).findByMeeting(testMeeting);
    }
    
    @Test
    void testFindByMember() {
        // Execute
        List<Attendance> results = attendanceService.findByMember(testMember);
        
        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(testAttendance, results.get(0));
        
        // Verify repository was called
        verify(attendanceRepository).findByMember(testMember);
    }
    
    @Test
    void testFindByMeetingAndMember() {
        // Execute
        Optional<Attendance> result = attendanceService.findByMeetingAndMember(testMeeting, testMember);
        
        // Verify
        assertTrue(result.isPresent());
        assertEquals(testAttendance, result.get());
        
        // Verify repository was called
        verify(attendanceRepository).findByMeetingAndMember(any(Meeting.class), any(TeamMember.class));
    }
    
    @Test
    void testCreateAttendance() {
        // Setup - create a new attendance for test
        when(attendanceRepository.findByMeetingAndMember(any(Meeting.class), any(TeamMember.class)))
            .thenReturn(Optional.empty());
        
        // Execute
        Attendance result = attendanceService.createAttendance(1L, 1L, true);
        
        // Verify
        assertNotNull(result);
        assertEquals(testMeeting, result.getMeeting());
        assertEquals(testMember, result.getMember());
        assertTrue(result.isPresent());
        
        // Verify repository calls
        verify(meetingRepository).findById(1L);
        verify(teamMemberRepository).findById(1L);
        verify(attendanceRepository).findByMeetingAndMember(any(Meeting.class), any(TeamMember.class));
        verify(attendanceRepository).save(any(Attendance.class));
    }
    
    @Test
    void testCreateAttendance_MeetingNotFound() {
        // Setup
        when(meetingRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute and verify
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.createAttendance(999L, 1L, true);
        });
        
        // Verify exception message
        assertEquals("Meeting not found with ID: 999", exception.getMessage());
        
        // Verify repository calls
        verify(meetingRepository).findById(999L);
        verify(teamMemberRepository, never()).findById(anyLong());
        verify(attendanceRepository, never()).findByMeetingAndMember(any(Meeting.class), any(TeamMember.class));
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }
    
    @Test
    void testGetAttendanceStatistics() {
        // Execute
        Map<String, Object> result = attendanceService.getAttendanceStatistics(1L);
        
        // Verify
        assertNotNull(result);
        assertEquals(1L, result.get("memberId"));
        assertEquals("Test User", result.get("memberName"));
        assertEquals(1, result.get("totalMeetings"));
        assertEquals(1L, result.get("presentCount"));
        assertEquals(0, result.get("absentCount"));
        assertEquals(100.0, result.get("attendanceRate"));
        
        // Verify repository calls
        verify(teamMemberRepository).findById(1L);
        verify(attendanceRepository).findByMember(testMember);
    }
}