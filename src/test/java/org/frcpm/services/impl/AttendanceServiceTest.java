// src/test/java/org/frcpm/services/impl/AttendanceServiceTest.java

package org.frcpm.services.impl;

import de.saxsys.mvvmfx.MvvmFX;
import org.frcpm.models.Attendance;
import org.frcpm.models.Meeting;
import org.frcpm.models.TeamMember;
import org.frcpm.repositories.specific.AttendanceRepository;
import org.frcpm.repositories.specific.MeetingRepository;
import org.frcpm.repositories.specific.TeamMemberRepository;
import org.frcpm.services.AttendanceService;
import org.frcpm.services.BaseServiceTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

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
 * Test class for AttendanceService implementation using TestableAttendanceServiceImpl.
 */
public class AttendanceServiceTest extends BaseServiceTest {
    
    @Mock
    private AttendanceRepository attendanceRepository;
    
    @Mock
    private MeetingRepository meetingRepository;
    
    @Mock
    private TeamMemberRepository teamMemberRepository;
    
    private AttendanceService attendanceService;
    
    private Attendance testAttendance;
    private Meeting testMeeting;
    private TeamMember testMember;
    
    @Override
    protected void setupTestData() {
        // Initialize test objects
        testMeeting = createTestMeeting();
        testMember = createTestMember();
        testAttendance = createTestAttendance();
    }
    
    @Override
    @BeforeEach
    public void setUp() {
        // Execute parent setup first (initializes Mockito annotations)
        super.setUp();
        
        // Configure mock repository responses
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(testAttendance));
        when(attendanceRepository.findAll()).thenReturn(List.of(testAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Configure meeting repository
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(testMeeting));
        
        // Configure team member repository
        when(teamMemberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        
        // Create service with injected mocks
        attendanceService = new TestableAttendanceServiceImpl(
            attendanceRepository,
            meetingRepository,
            teamMemberRepository
        );
        
        // Configure MVVMFx dependency injector for comprehensive testing
        MvvmFX.setCustomDependencyInjector(type -> {
            if (type == AttendanceRepository.class) return attendanceRepository;
            if (type == MeetingRepository.class) return meetingRepository;
            if (type == TeamMemberRepository.class) return teamMemberRepository;
            if (type == AttendanceService.class) return attendanceService;
            return null;
        });
    }
    
    @AfterEach
    @Override
    public void tearDown() throws Exception {
        // Clear MVVMFx dependency injector
        MvvmFX.setCustomDependencyInjector(null);
        
        // Call parent tearDown
        super.tearDown();
    }
    
    /**
     * Creates a test meeting for use in tests.
     * 
     * @return a test meeting
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
     * 
     * @return a test team member
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
     * 
     * @return a test attendance record
     */
    private Attendance createTestAttendance() {
        Attendance attendance = new Attendance(testMeeting, testMember, true);
        attendance.setId(1L);
        attendance.setArrivalTime(LocalTime.of(10, 0));
        attendance.setDepartureTime(LocalTime.of(11, 0));
        return attendance;
    }
    
    @Test
    public void testFindById() {
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
    public void testFindAll() {
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
    public void testSave() {
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
    public void testDelete() {
        // Setup
        doNothing().when(attendanceRepository).delete(any(Attendance.class));
        
        // Execute
        attendanceService.delete(testAttendance);
        
        // Verify repository was called
        verify(attendanceRepository).delete(testAttendance);
    }
    
    @Test
    public void testDeleteById() {
        // Setup
        when(attendanceRepository.deleteById(anyLong())).thenReturn(true);
        
        // Execute
        boolean result = attendanceService.deleteById(1L);
        
        // Verify
        assertTrue(result);
        
        // Verify repository was called
        verify(attendanceRepository).deleteById(1L);
    }
    
    @Test
    public void testCount() {
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
    public void testFindByMeeting() {
        // Setup
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
    public void testFindByMember() {
        // Setup
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
    public void testFindByMeetingAndMember() {
        // Setup
        when(attendanceRepository.findByMeetingAndMember(testMeeting, testMember))
            .thenReturn(Optional.of(testAttendance));
        
        // Execute
        Optional<Attendance> result = attendanceService.findByMeetingAndMember(testMeeting, testMember);
        
        // Verify
        assertTrue(result.isPresent());
        assertEquals(testAttendance, result.get());
        
        // Verify repository was called
        verify(attendanceRepository).findByMeetingAndMember(testMeeting, testMember);
    }
    
    @Test
    public void testCreateAttendance() {
        // Setup
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
    public void testCreateAttendance_ExistingRecord() {
        // Setup
        when(attendanceRepository.findByMeetingAndMember(any(Meeting.class), any(TeamMember.class)))
            .thenReturn(Optional.of(testAttendance));
        
        // Execute
        Attendance result = attendanceService.createAttendance(1L, 1L, false);
        
        // Verify
        assertNotNull(result);
        assertEquals(testMeeting, result.getMeeting());
        assertEquals(testMember, result.getMember());
        assertFalse(result.isPresent());
        
        // Verify repository calls
        verify(meetingRepository).findById(1L);
        verify(teamMemberRepository).findById(1L);
        verify(attendanceRepository).findByMeetingAndMember(any(Meeting.class), any(TeamMember.class));
        verify(attendanceRepository).