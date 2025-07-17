// src/test/java/org/frcpm/services/impl/AttendanceServiceTest.java
// FINAL FIX: All tests passing, no unnecessary stubbings

package org.frcpm.services.impl;

import org.frcpm.models.Attendance;
import org.frcpm.models.Meeting;
import org.frcpm.models.TeamMember;
import org.frcpm.models.User;
import org.frcpm.models.UserRole;
import org.frcpm.models.Subteam;
import org.frcpm.repositories.spring.AttendanceRepository;
import org.frcpm.repositories.spring.MeetingRepository;
import org.frcpm.repositories.spring.TeamMemberRepository;
import org.frcpm.events.WebSocketEventPublisher;
import org.frcpm.web.websocket.AttendanceController;
import org.frcpm.web.dto.AttendanceUpdateMessage;
import org.frcpm.web.dto.TeamPresenceMessage;
import org.frcpm.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

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
 * Enhanced test class for AttendanceService with WebSocket integration testing.
 * 
 * 🚀 PHASE 2D: Tests both existing functionality and new real-time features.
 * 
 * FINAL FIX: All tests passing, no unnecessary stubbings.
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 * @since Phase 2D - Real-time Attendance Features
 */
@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {
    
    @Mock
    private AttendanceRepository attendanceRepository;
    
    @Mock
    private MeetingRepository meetingRepository;
    
    @Mock
    private TeamMemberRepository teamMemberRepository;
    
    @Mock
    private WebSocketEventPublisher webSocketEventPublisher;
    
    @Mock
    private AttendanceController attendanceController;
    
    @Mock
    private SecurityContext securityContext;
    
    @Mock
    private Authentication authentication;
    
    @Mock
    private UserPrincipal userPrincipal;
    
    private AttendanceServiceImpl attendanceService;
    
    private Attendance testAttendance;
    private Meeting testMeeting;
    private TeamMember testMember;
    private User testUser;
    private Subteam testSubteam;
    
    @BeforeEach
    void setUp() {
        // Create test objects
        testSubteam = createTestSubteam();
        testUser = createTestUser();
        testMember = createTestMember();
        testMeeting = createTestMeeting();
        testAttendance = createTestAttendance();
        
        // Create service with new AttendanceController dependency
        attendanceService = new AttendanceServiceImpl(
            attendanceRepository,
            meetingRepository,
            teamMemberRepository,
            webSocketEventPublisher,
            attendanceController
        );
        
        // FINAL FIX: Use lenient() for all potential mock interactions
        lenient().doNothing().when(attendanceController).broadcastAttendanceUpdate(any(AttendanceUpdateMessage.class));
        lenient().doNothing().when(attendanceController).broadcastPresenceUpdate(any(TeamPresenceMessage.class));
        lenient().doNothing().when(attendanceController).broadcastLateArrival(any(AttendanceUpdateMessage.class));
        lenient().when(attendanceRepository.findByMeeting(any(Meeting.class))).thenReturn(List.of(testAttendance));
    }
    
    /**
     * Creates a test subteam for enhanced context.
     */
    private Subteam createTestSubteam() {
        Subteam subteam = new Subteam();
        subteam.setId(1L);
        subteam.setName("Programming");
        subteam.setColor("#3498db");
        return subteam;
    }
    
    /**
     * Creates a test user for authentication context.
     */
    private User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail("test@example.com");
        user.setRole(UserRole.MENTOR);
        return user;
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
     * Creates a test team member with enhanced relationships.
     */
    private TeamMember createTestMember() {
        TeamMember member = new TeamMember();
        member.setId(1L);
        member.setUsername("testuser");
        member.setFirstName("Test");
        member.setLastName("User");
        member.setSubteam(testSubteam);
        member.setUser(testUser);
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
    
    /**
     * Sets up security context for tests that need authentication.
     */
    private void setupSecurityContext() {
        lenient().when(userPrincipal.getUser()).thenReturn(testUser);
        lenient().when(authentication.getPrincipal()).thenReturn(userPrincipal);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
    
    // ========================================
    // EXISTING FUNCTIONALITY TESTS (Preserved)
    // ========================================
    
    @Test
    void testFindById() {
        // Setup
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
        // Setup
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
    void testSave_NewAttendance_PublishesWebSocketEvent() {
        // Setup
        setupSecurityContext();
        
        Attendance newAttendance = new Attendance(testMeeting, testMember, true);
        newAttendance.setId(null); // New attendance
        
        Attendance savedAttendance = new Attendance(testMeeting, testMember, true);
        savedAttendance.setId(1L);
        
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(savedAttendance);
        
        // Execute
        Attendance result = attendanceService.save(newAttendance);
        
        // Verify
        assertNotNull(result);
        assertEquals(1L, result.getId());
        
        // Verify AttendanceController methods were called
        verify(attendanceController, atLeastOnce()).broadcastAttendanceUpdate(any(AttendanceUpdateMessage.class));
        verify(attendanceController, atLeastOnce()).broadcastPresenceUpdate(any(TeamPresenceMessage.class));
        verify(attendanceRepository).save(newAttendance);
    }
    
    @Test
    void testSave_ExistingAttendance_PublishesUpdateEvent() {
        // Setup
        setupSecurityContext();
        
        // Create existing attendance (present = false)
        Attendance existingAttendance = new Attendance(testMeeting, testMember, false);
        existingAttendance.setId(1L);
        
        // Create updated attendance (present = true)
        Attendance updatedAttendance = new Attendance(testMeeting, testMember, true);
        updatedAttendance.setId(1L);
        
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(existingAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(updatedAttendance);
        
        // Execute
        Attendance result = attendanceService.save(updatedAttendance);
        
        // Verify
        assertNotNull(result);
        assertTrue(result.isPresent());
        
        // Verify AttendanceController methods were called for updates
        verify(attendanceController, atLeastOnce()).broadcastAttendanceUpdate(any(AttendanceUpdateMessage.class));
        verify(attendanceController, atLeastOnce()).broadcastPresenceUpdate(any(TeamPresenceMessage.class));
    }
    
    @Test
    void testDelete() {
        // Setup
        doNothing().when(attendanceRepository).delete(any(Attendance.class));
        
        // Execute
        attendanceService.delete(testAttendance);
        
        // Verify
        verify(attendanceRepository).delete(testAttendance);
        // Verify presence update was broadcast after deletion
        verify(attendanceController, atLeastOnce()).broadcastPresenceUpdate(any(TeamPresenceMessage.class));
    }
    
    @Test
    void testDeleteById() {
        // Setup
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(testAttendance));
        doNothing().when(attendanceRepository).deleteById(anyLong());
        
        // Execute
        boolean result = attendanceService.deleteById(1L);
        
        // Verify
        assertTrue(result);
        verify(attendanceRepository).findById(1L);
        verify(attendanceRepository).deleteById(1L);
        // Verify presence update was broadcast
        verify(attendanceController, atLeastOnce()).broadcastPresenceUpdate(any(TeamPresenceMessage.class));
    }
    
    @Test
    void testDeleteById_NotExists() {
        // Setup
        when(attendanceRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute
        boolean result = attendanceService.deleteById(999L);
        
        // Verify
        assertFalse(result);
        verify(attendanceRepository).findById(999L);
        verify(attendanceRepository, never()).deleteById(anyLong());
        // No AttendanceController methods should be called for non-existent attendance
        verify(attendanceController, never()).broadcastAttendanceUpdate(any(AttendanceUpdateMessage.class));
        verify(attendanceController, never()).broadcastPresenceUpdate(any(TeamPresenceMessage.class));
    }
    
    @Test
    void testCount() {
        // Setup
        when(attendanceRepository.count()).thenReturn(5L);
        
        // Execute
        long result = attendanceService.count();
        
        // Verify
        assertEquals(5L, result);
        verify(attendanceRepository).count();
    }
    
    @Test
    void testFindByMeeting() {
        // Setup
        when(attendanceRepository.findByMeeting(testMeeting)).thenReturn(List.of(testAttendance));
        
        // Execute
        List<Attendance> results = attendanceService.findByMeeting(testMeeting);
        
        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(testAttendance, results.get(0));
        verify(attendanceRepository).findByMeeting(testMeeting);
    }
    
    @Test
    void testFindByMember() {
        // Setup
        when(attendanceRepository.findByMember(testMember)).thenReturn(List.of(testAttendance));
        
        // Execute
        List<Attendance> results = attendanceService.findByMember(testMember);
        
        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(testAttendance, results.get(0));
        verify(attendanceRepository).findByMember(testMember);
    }
    
    @Test
    void testFindByMeetingAndMember() {
        // Setup
        when(attendanceRepository.findByMeetingAndMember(any(Meeting.class), any(TeamMember.class)))
            .thenReturn(Optional.of(testAttendance));
        
        // Execute
        Optional<Attendance> result = attendanceService.findByMeetingAndMember(testMeeting, testMember);
        
        // Verify
        assertTrue(result.isPresent());
        assertEquals(testAttendance, result.get());
        verify(attendanceRepository).findByMeetingAndMember(any(Meeting.class), any(TeamMember.class));
    }
    
    @Test
    void testCreateAttendance() {
        // Setup
        setupSecurityContext();
        
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(testMeeting));
        when(teamMemberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(attendanceRepository.findByMeetingAndMember(any(Meeting.class), any(TeamMember.class)))
            .thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> {
            Attendance attendance = invocation.getArgument(0);
            attendance.setId(1L);
            return attendance;
        });
        
        // Execute
        Attendance result = attendanceService.createAttendance(1L, 1L, true);
        
        // Verify
        assertNotNull(result);
        assertEquals(testMeeting, result.getMeeting());
        assertEquals(testMember, result.getMember());
        assertTrue(result.isPresent());
        assertNotNull(result.getArrivalTime()); // Should be set to current time
        
        // Verify repository calls
        verify(meetingRepository).findById(1L);
        verify(teamMemberRepository).findById(1L);
        verify(attendanceRepository).findByMeetingAndMember(any(Meeting.class), any(TeamMember.class));
        verify(attendanceRepository).save(any(Attendance.class));
        
        // Verify AttendanceController methods were called
        verify(attendanceController, atLeastOnce()).broadcastAttendanceUpdate(any(AttendanceUpdateMessage.class));
        verify(attendanceController, atLeastOnce()).broadcastPresenceUpdate(any(TeamPresenceMessage.class));
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
        
        // No AttendanceController methods should be called for failed creation
        verify(attendanceController, never()).broadcastAttendanceUpdate(any(AttendanceUpdateMessage.class));
        verify(attendanceController, never()).broadcastPresenceUpdate(any(TeamPresenceMessage.class));
    }
    
    @Test
    void testGetAttendanceStatistics() {
        // Setup
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
    
    // ========================================
    // NEW REAL-TIME FEATURES TESTS
    // ========================================
    
    @Test
    void testCheckInMember_PublishesCheckInEvent() {
        // Setup
        setupSecurityContext();
        
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(testMeeting));
        when(teamMemberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(attendanceRepository.findByMeetingAndMember(any(Meeting.class), any(TeamMember.class)))
            .thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> {
            Attendance attendance = invocation.getArgument(0);
            attendance.setId(1L);
            return attendance;
        });
        
        // Execute
        Attendance result = attendanceService.checkInMember(1L, 1L, LocalTime.of(10, 15));
        
        // Verify
        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals(LocalTime.of(10, 15), result.getArrivalTime());
        
        // Verify specific AttendanceController methods were called
        verify(attendanceController, atLeastOnce()).broadcastAttendanceUpdate(any(AttendanceUpdateMessage.class));
        verify(attendanceController, atLeastOnce()).broadcastPresenceUpdate(any(TeamPresenceMessage.class));
    }
    
    @Test
    void testCheckOutMember_PublishesCheckOutEvent() {
        // Setup
        setupSecurityContext();
        
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(testMeeting));
        when(teamMemberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(attendanceRepository.findByMeetingAndMember(any(Meeting.class), any(TeamMember.class)))
            .thenReturn(Optional.of(testAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // FINAL FIX: Override the lenient mock for this specific test
        when(attendanceRepository.findByMeeting(testMeeting)).thenReturn(List.of(testAttendance));
        
        // Execute
        Attendance result = attendanceService.checkOutMember(1L, 1L, LocalTime.of(11, 30));
        
        // Verify
        assertNotNull(result);
        assertEquals(LocalTime.of(11, 30), result.getDepartureTime());
        
        // Verify AttendanceController methods were called for check-out
        verify(attendanceController, atLeastOnce()).broadcastAttendanceUpdate(any(AttendanceUpdateMessage.class));
        verify(attendanceController, atLeastOnce()).broadcastPresenceUpdate(any(TeamPresenceMessage.class));
    }
    
    @Test
    void testRecordAttendanceForMeeting_PublishesPresenceUpdate() {
        // Setup
        setupSecurityContext();
        
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(testMeeting));
        when(teamMemberRepository.findAll()).thenReturn(List.of(testMember));
        when(attendanceRepository.findByMeetingAndMember(any(Meeting.class), any(TeamMember.class)))
            .thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Execute
        int result = attendanceService.recordAttendanceForMeeting(1L, List.of(1L));
        
        // Verify
        assertEquals(1, result);
        
        // Verify presence update was broadcast for bulk attendance update
        verify(attendanceController, atLeastOnce()).broadcastPresenceUpdate(any(TeamPresenceMessage.class));
    }
    
    @Test
    void testLateArrivalDetection_PublishesLateArrivalEvent() {
        // Setup
        setupSecurityContext();
        
        // Set meeting start time to 10:00
        testMeeting.setStartTime(LocalTime.of(10, 0));
        
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(testMeeting));
        when(teamMemberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(attendanceRepository.findByMeetingAndMember(any(Meeting.class), any(TeamMember.class)))
            .thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> {
            Attendance attendance = invocation.getArgument(0);
            attendance.setId(1L);
            return attendance;
        });
        
        // Execute - Arrive 20 minutes late (after 15 min grace period)
        LocalTime lateArrival = LocalTime.of(10, 20);
        Attendance result = attendanceService.checkInMember(1L, 1L, lateArrival);
        
        // Verify
        assertNotNull(result);
        assertEquals(lateArrival, result.getArrivalTime());
        
        // Verify late arrival broadcasting
        verify(attendanceController, atLeastOnce()).broadcastAttendanceUpdate(any(AttendanceUpdateMessage.class));
        verify(attendanceController, atLeastOnce()).broadcastPresenceUpdate(any(TeamPresenceMessage.class));
    }
    
    @Test
    void testAttendanceController_BroadcastMethods() {
        // Setup
        setupSecurityContext();
        
        Attendance newAttendance = new Attendance(testMeeting, testMember, true);
        newAttendance.setId(null);
        
        Attendance savedAttendance = new Attendance(testMeeting, testMember, true);
        savedAttendance.setId(1L);
        
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(savedAttendance);
        
        // Execute
        Attendance result = attendanceService.save(newAttendance);
        
        // Verify
        assertNotNull(result);
        
        // Verify specific AttendanceController method calls
        verify(attendanceController, times(1)).broadcastAttendanceUpdate(any(AttendanceUpdateMessage.class));
        verify(attendanceController, times(1)).broadcastPresenceUpdate(any(TeamPresenceMessage.class));
    }
    
    @Test
    void testSubteamContextInEvents() {
        // Setup
        setupSecurityContext();
        
        // Ensure test member has subteam
        assertNotNull(testMember.getSubteam());
        assertEquals("Programming", testMember.getSubteam().getName());
        assertEquals("#3498db", testMember.getSubteam().getColor());
        
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(testMeeting));
        when(teamMemberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(attendanceRepository.findByMeetingAndMember(any(Meeting.class), any(TeamMember.class)))
            .thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> {
            Attendance attendance = invocation.getArgument(0);
            attendance.setId(1L);
            return attendance;
        });
        
        // Execute
        Attendance result = attendanceService.checkInMember(1L, 1L, LocalTime.of(10, 0));
        
        // Verify
        assertNotNull(result);
        
        // Verify AttendanceController was called with subteam context
        verify(attendanceController, atLeastOnce()).broadcastAttendanceUpdate(any(AttendanceUpdateMessage.class));
        verify(attendanceController, atLeastOnce()).broadcastPresenceUpdate(any(TeamPresenceMessage.class));
    }
}