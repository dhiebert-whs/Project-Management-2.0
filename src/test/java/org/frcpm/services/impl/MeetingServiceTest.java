// src/test/java/org/frcpm/services/impl/MeetingServiceTest.java

package org.frcpm.services.impl;

import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.repositories.spring.MeetingRepository;
import org.frcpm.repositories.spring.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Test class for MeetingService implementation using Spring Boot testing patterns.
 * FIXED: Applied AttendanceServiceTest success pattern - removed unnecessary stubbing,
 * fixed deleteById logic, and corrected return type handling.
 */
@ExtendWith(MockitoExtension.class)
class MeetingServiceTest {
    
    @Mock
    private MeetingRepository meetingRepository;
    
    @Mock
    private ProjectRepository projectRepository;
    
    private MeetingServiceImpl meetingService;
    
    private Meeting testMeeting;
    private Project testProject;
    
    @BeforeEach
    void setUp() {
        // Create test objects ONLY - NO MOCK STUBBING HERE
        testProject = createTestProject();
        testMeeting = createTestMeeting();
        
        // Create service with injected mocks
        meetingService = new MeetingServiceImpl(
            meetingRepository,
            projectRepository
        );
        
        // ✅ FIXED: NO mock stubbing in setUp() - move to individual test methods
    }
    
    /**
     * Creates a test project for use in tests.
     */
    private Project createTestProject() {
        Project project = new Project();
        project.setId(1L);
        project.setName("Test Project");
        project.setStartDate(LocalDate.now().minusDays(10));
        project.setGoalEndDate(LocalDate.now().plusDays(80));
        project.setHardDeadline(LocalDate.now().plusDays(90));
        return project;
    }
    
    /**
     * Creates a test meeting for use in tests.
     */
    private Meeting createTestMeeting() {
        Meeting meeting = new Meeting();
        meeting.setId(1L);
        meeting.setDate(LocalDate.now().plusDays(5));
        meeting.setStartTime(LocalTime.of(9, 0));
        meeting.setEndTime(LocalTime.of(10, 30));
        meeting.setProject(testProject);
        meeting.setNotes("Test meeting notes");
        return meeting;
    }
    
    @Test
    void testFindById() {
        // Setup - Only stub what this test needs
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(testMeeting));
        
        // Execute
        Meeting result = meetingService.findById(1L);
        
        // Verify
        assertNotNull(result, "Result should not be null");
        assertEquals(1L, result.getId(), "Meeting ID should match");
        assertEquals(testProject.getId(), result.getProject().getId(), "Project ID should match");
        
        // Verify repository was called exactly once with the correct ID
        verify(meetingRepository, times(1)).findById(1L);
    }
    
    @Test
    void testFindAll() {
        // Setup - Only stub what this test needs
        when(meetingRepository.findAll()).thenReturn(List.of(testMeeting));
        
        // Execute
        List<Meeting> results = meetingService.findAll();
        
        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(1L, results.get(0).getId());
        
        // Verify repository was called
        verify(meetingRepository).findAll();
    }
    
    @Test
    void testSave() {
        // Setup - Only stub what this test needs
        when(meetingRepository.save(any(Meeting.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Setup
        Meeting newMeeting = new Meeting();
        newMeeting.setDate(LocalDate.now().plusDays(2));
        newMeeting.setStartTime(LocalTime.of(14, 0));
        newMeeting.setEndTime(LocalTime.of(15, 0));
        newMeeting.setProject(testProject);
        
        // Execute
        Meeting result = meetingService.save(newMeeting);
        
        // Verify
        assertNotNull(result);
        assertEquals(testProject, result.getProject());
        
        // Verify repository was called
        verify(meetingRepository).save(newMeeting);
    }
    
    @Test
    void testDelete() {
        // Setup
        doNothing().when(meetingRepository).delete(any(Meeting.class));
        
        // Execute
        meetingService.delete(testMeeting);
        
        // Verify repository was called
        verify(meetingRepository).delete(testMeeting);
    }
    
    @Test
    void testDeleteById() {
        // Setup - Mock both existsById and deleteById as service calls both
        when(meetingRepository.existsById(1L)).thenReturn(true);
        doNothing().when(meetingRepository).deleteById(anyLong());
        
        // Execute
        boolean result = meetingService.deleteById(1L);
        
        // Verify result
        assertTrue(result);
        
        // Verify repository calls in correct order
        verify(meetingRepository).existsById(1L);
        verify(meetingRepository).deleteById(1L);
    }
    
    @Test
    void testDeleteById_NotExists() {
        // Setup - Meeting doesn't exist
        when(meetingRepository.existsById(999L)).thenReturn(false);
        
        // Execute
        boolean result = meetingService.deleteById(999L);
        
        // Verify result
        assertFalse(result);
        
        // Verify repository calls
        verify(meetingRepository).existsById(999L);
        verify(meetingRepository, never()).deleteById(anyLong());
    }
    
    @Test
    void testCount() {
        // Setup - Only stub what this test needs
        when(meetingRepository.count()).thenReturn(5L);
        
        // Execute
        long result = meetingService.count();
        
        // Verify
        assertEquals(5L, result);
        
        // Verify repository was called
        verify(meetingRepository).count();
    }
    
    @Test
    void testFindByProject() {
        // Setup - Only stub what this test needs
        when(meetingRepository.findByProject(testProject)).thenReturn(List.of(testMeeting));
        
        // Execute
        List<Meeting> results = meetingService.findByProject(testProject);
        
        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(testMeeting, results.get(0));
        
        // Verify repository was called
        verify(meetingRepository).findByProject(testProject);
    }
    
    @Test
    void testFindByDate() {
        // Setup - Only stub what this test needs
        LocalDate testDate = LocalDate.now().plusDays(5);
        when(meetingRepository.findByDate(testDate)).thenReturn(List.of(testMeeting));
        
        // Execute
        List<Meeting> results = meetingService.findByDate(testDate);
        
        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(testMeeting, results.get(0));
        
        // Verify repository was called
        verify(meetingRepository).findByDate(testDate);
    }
    
    @Test
    void testFindByDateAfter() {
        // Setup - Only stub what this test needs
        LocalDate testDate = LocalDate.now();
        when(meetingRepository.findByDateAfter(testDate)).thenReturn(List.of(testMeeting));
        
        // Execute
        List<Meeting> results = meetingService.findByDateAfter(testDate);
        
        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(testMeeting, results.get(0));
        
        // Verify repository was called
        verify(meetingRepository).findByDateAfter(testDate);
    }
    
    @Test
    void testFindByDateBetween() {
        // Setup - Only stub what this test needs
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(10);
        when(meetingRepository.findByDateBetween(startDate, endDate)).thenReturn(List.of(testMeeting));
        
        // Execute
        List<Meeting> results = meetingService.findByDateBetween(startDate, endDate);
        
        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(testMeeting, results.get(0));
        
        // Verify repository was called
        verify(meetingRepository).findByDateBetween(startDate, endDate);
    }
    
    @Test
    void testCreateMeeting() {
        // Setup - Only stub what this test needs
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(meetingRepository.save(any(Meeting.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Execute
        Meeting result = meetingService.createMeeting(
            LocalDate.now().plusDays(3),
            LocalTime.of(11, 0),
            LocalTime.of(12, 0),
            1L,
            "New meeting notes"
        );
        
        // Verify
        assertNotNull(result);
        assertEquals(LocalDate.now().plusDays(3), result.getDate());
        assertEquals(LocalTime.of(11, 0), result.getStartTime());
        assertEquals(LocalTime.of(12, 0), result.getEndTime());
        assertEquals("New meeting notes", result.getNotes());
        assertEquals(testProject, result.getProject());
        
        // Verify repository calls
        verify(projectRepository).findById(1L);
        verify(meetingRepository).save(any(Meeting.class));
    }
    
    @Test
    void testCreateMeeting_ProjectNotFound() {
        // Setup - Only stub what this test needs
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            meetingService.createMeeting(
                LocalDate.now().plusDays(3),
                LocalTime.of(11, 0),
                LocalTime.of(12, 0),
                999L,
                "New meeting notes"
            );
        });
        
        // Verify exception message
        assertTrue(exception.getMessage().contains("Project not found"));
        
        // Verify repository calls
        verify(projectRepository).findById(999L);
        verify(meetingRepository, never()).save(any(Meeting.class));
    }
    
    @Test
    void testUpdateMeetingDateTime() {
        // Setup - Only stub what this test needs
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(testMeeting));
        when(meetingRepository.save(any(Meeting.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        LocalDate newDate = LocalDate.now().plusDays(7);
        LocalTime newStartTime = LocalTime.of(10, 0);
        LocalTime newEndTime = LocalTime.of(11, 30);
        
        // Execute
        Meeting result = meetingService.updateMeetingDateTime(1L, newDate, newStartTime, newEndTime);
        
        // Verify
        assertNotNull(result);
        assertEquals(newDate, result.getDate());
        assertEquals(newStartTime, result.getStartTime());
        assertEquals(newEndTime, result.getEndTime());
        
        // Verify repository calls
        verify(meetingRepository).findById(1L);
        verify(meetingRepository).save(any(Meeting.class));
    }
    
    @Test
    void testUpdateMeetingDateTime_MeetingNotFound() {
        // Setup - Only stub what this test needs
        when(meetingRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute
        Meeting result = meetingService.updateMeetingDateTime(
            999L,
            LocalDate.now().plusDays(7),
            LocalTime.of(10, 0),
            LocalTime.of(11, 30)
        );
        
        // Verify
        assertNull(result);
        
        // Verify repository calls
        verify(meetingRepository).findById(999L);
        verify(meetingRepository, never()).save(any(Meeting.class));
    }
    
    @Test
    void testUpdateNotes() {
        // Setup - Only stub what this test needs
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(testMeeting));
        when(meetingRepository.save(any(Meeting.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Execute
        Meeting result = meetingService.updateNotes(1L, "Updated notes");
        
        // Verify
        assertNotNull(result);
        assertEquals("Updated notes", result.getNotes());
        
        // Verify repository calls
        verify(meetingRepository).findById(1L);
        verify(meetingRepository).save(any(Meeting.class));
    }
    
    @Test
    void testGetUpcomingMeetings() {
        // Setup - Only stub what this test needs
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(meetingRepository.findByProjectAndDateBetween(eq(testProject), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(List.of(testMeeting));
        
        // Execute
        List<Meeting> results = meetingService.getUpcomingMeetings(1L, 7);
        
        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(testMeeting, results.get(0));
        
        // Verify repository calls
        verify(projectRepository).findById(1L);
        verify(meetingRepository).findByProjectAndDateBetween(eq(testProject), any(LocalDate.class), any(LocalDate.class));
    }
    
    @Test
    void testGetUpcomingMeetings_ProjectNotFound() {
        // Setup - Only stub what this test needs
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            meetingService.getUpcomingMeetings(999L, 7);
        });
        
        // Verify exception message
        assertTrue(exception.getMessage().contains("Project not found"));
        
        // Verify repository calls
        verify(projectRepository).findById(999L);
        verify(meetingRepository, never()).findByProjectAndDateBetween(any(), any(LocalDate.class), any(LocalDate.class));
    }
}