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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Fixed test class for AttendanceService implementation using Spring Boot testing patterns.
 * FIXED: Removed unnecessary stubbing and corrected deleteById test logic.
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
        // Setup - Only stub what this test needs
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(testAttendance));
        
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
        // Setup - Only stub what this test needs
        when(attendanceRepository.findAll()).thenReturn(List.of(testAttendance));
        
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
        // Setup - Only stub what this test needs
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
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
        // Setup - Mock both existsById and deleteById as the service calls both
        when(attendanceRepository.existsById(1L)).thenReturn(true);
        doNothing().when(attendanceRepository).deleteById(anyLong());
        
        // Execute
        boolean result = attendanceService.deleteById(1L);
        
        // Verify result
        assertTrue(result);
        
        // Verify repository was called in correct order
        verify(attendanceRepository).existsById(1L);
        verify(attendanceRepository).deleteById(1L);
    }
    
    @Test
    void testDeleteById_NotExists() {
        // Setup - Attendance doesn't exist
        when(attendanceRepository.existsById(999L)).thenReturn(false);
        
        // Execute
        boolean result = attendanceService.deleteById(999L);
        
        // Verify result
        assertFalse(result);
        
        // Verify repository calls
        verify(attendanceRepository).existsById(999L);
        verify(attendanceRepository, never()).deleteById(anyLong());
    }
    
    @Test
    void testCount() {
        // Setup - Only stub what this test needs
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
        // Setup - Only stub what this test needs
        when(attendanceRepository.findByMeeting(testMeeting)).thenReturn(List.of(testAttendance));
        
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
        // Setup - Only stub what this test needs
        when(attendanceRepository.findByMember(testMember)).thenReturn(List.of(testAttendance));
        
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
        // Setup - Only stub what this test needs
        when(attendanceRepository.findByMeetingAndMember(any(Meeting.class), any(TeamMember.class)))
            .thenReturn(Optional.of(testAttendance));
        
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
        // Setup - Only stub what this test needs
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(testMeeting));
        when(teamMemberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(attendanceRepository.findByMeetingAndMember(any(Meeting.class), any(TeamMember.class)))
            .thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
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
        // Setup - Only stub what this test needs
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
        // Setup - Only stub what this test needs
        when(teamMemberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(attendanceRepository.findByMember(testMember)).thenReturn(List.of(testAttendance));
        
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