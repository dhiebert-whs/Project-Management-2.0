package org.frcpm.services;

import org.frcpm.config.DatabaseConfig;
import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.repositories.specific.MilestoneRepository;
import org.frcpm.repositories.specific.ProjectRepository;
import org.frcpm.repositories.specific.TaskRepository;
import org.frcpm.services.impl.GanttDataServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MeetingServiceTest {
    
    private MeetingService meetingService;
    private ProjectService projectService;
    
    private Project testProject;
    
    @BeforeEach
    public void setUp() {
        DatabaseConfig.initialize();
        
        meetingService = ServiceFactory.getMeetingService();
        projectService = ServiceFactory.getProjectService();
        
        // Create test project
        testProject = projectService.createProject(
            "Meeting Test Project", 
            LocalDate.now(), 
            LocalDate.now().plusWeeks(6), 
            LocalDate.now().plusWeeks(8)
        );
    }
    
    @AfterEach
    public void tearDown() {
        // Clean up test data
        try {
            // Delete meetings associated with test project
            List<Meeting> meetings = meetingService.findByProject(testProject);
            for (Meeting meeting : meetings) {
                meetingService.deleteById(meeting.getId());
            }
            
            // Delete test project
            projectService.deleteById(testProject.getId());
        } catch (Exception e) {
            // Log but continue with cleanup
            System.err.println("Error during cleanup: " + e.getMessage());
        }
        
        DatabaseConfig.shutdown();
    }
    
    @Test
    public void testCreateMeeting() {
        Meeting meeting = meetingService.createMeeting(
            LocalDate.now().plusDays(1),
            LocalTime.of(18, 0), // 6:00 PM
            LocalTime.of(20, 0), // 8:00 PM
            testProject.getId(),
            "Test meeting notes"
        );
        
        assertNotNull(meeting);
        assertNotNull(meeting.getId());
        assertEquals(LocalDate.now().plusDays(1), meeting.getDate());
        assertEquals(LocalTime.of(18, 0), meeting.getStartTime());
        assertEquals(LocalTime.of(20, 0), meeting.getEndTime());
        assertEquals("Test meeting notes", meeting.getNotes());
        assertEquals(testProject.getId(), meeting.getProject().getId());
    }
    
    @Test
    public void testUpdateMeetingDateTime() {
        // Create a meeting
        Meeting meeting = meetingService.createMeeting(
            LocalDate.now().plusDays(1),
            LocalTime.of(18, 0),
            LocalTime.of(20, 0),
            testProject.getId(),
            "Initial notes"
        );
        
        // Update date and time
        LocalDate newDate = LocalDate.now().plusDays(2);
        LocalTime newStartTime = LocalTime.of(19, 0);
        LocalTime newEndTime = LocalTime.of(21, 0);
        
        Meeting updated = meetingService.updateMeetingDateTime(
            meeting.getId(),
            newDate,
            newStartTime,
            newEndTime
        );
        
        assertNotNull(updated);
        assertEquals(newDate, updated.getDate());
        assertEquals(newStartTime, updated.getStartTime());
        assertEquals(newEndTime, updated.getEndTime());
    }
    
    @Test
    public void testUpdateNotes() {
        // Create a meeting
        Meeting meeting = meetingService.createMeeting(
            LocalDate.now().plusDays(1),
            LocalTime.of(18, 0),
            LocalTime.of(20, 0),
            testProject.getId(),
            "Initial notes"
        );
        
        // Update notes
        Meeting updated = meetingService.updateNotes(
            meeting.getId(),
            "Updated meeting notes"
        );
        
        assertNotNull(updated);
        assertEquals("Updated meeting notes", updated.getNotes());
    }
    
    @Test
    public void testFindByProject() {
        // Create meetings for the test project
        meetingService.createMeeting(
            LocalDate.now().plusDays(1),
            LocalTime.of(18, 0),
            LocalTime.of(20, 0),
            testProject.getId(),
            "Meeting 1"
        );
        
        meetingService.createMeeting(
            LocalDate.now().plusDays(3),
            LocalTime.of(18, 0),
            LocalTime.of(20, 0),
            testProject.getId(),
            "Meeting 2"
        );
        
        // Find meetings by project
        List<Meeting> meetings = meetingService.findByProject(testProject);
        assertNotNull(meetings);
        assertEquals(2, meetings.size());
        assertTrue(meetings.stream().allMatch(m -> m.getProject().getId().equals(testProject.getId())));
    }
    
    @Test
    public void testFindByDate() {
        LocalDate meetingDate = LocalDate.now().plusDays(5);
        
        // Create meetings on the same date
        meetingService.createMeeting(
            meetingDate,
            LocalTime.of(10, 0),
            LocalTime.of(12, 0),
            testProject.getId(),
            "Morning meeting"
        );
        
        meetingService.createMeeting(
            meetingDate,
            LocalTime.of(14, 0),
            LocalTime.of(16, 0),
            testProject.getId(),
            "Afternoon meeting"
        );
        
        // Create a meeting on a different date
        meetingService.createMeeting(
            LocalDate.now().plusDays(6),
            LocalTime.of(18, 0),
            LocalTime.of(20, 0),
            testProject.getId(),
            "Different day meeting"
        );
        
        // Find meetings by date
        List<Meeting> meetings = meetingService.findByDate(meetingDate);
        assertNotNull(meetings);
        assertEquals(2, meetings.size());
        assertTrue(meetings.stream().allMatch(m -> m.getDate().equals(meetingDate)));
    }
    
    @Test
    public void testFindByDateAfter() {
        // Define reference date here to avoid undefined variable error
        LocalDate referenceDate = LocalDate.now().plusDays(3);
        
        // Create meetings in different date ranges with unique identifiers
        // One before reference date
        meetingService.createMeeting(
            LocalDate.now().plusDays(2),
            LocalTime.of(18, 0),
            LocalTime.of(20, 0),
            testProject.getId(),
            "Meeting before reference date"
        );
        
        // Two after reference date - guaranteed count
        meetingService.createMeeting(
            LocalDate.now().plusDays(5),
            LocalTime.of(18, 0),
            LocalTime.of(20, 0),
            testProject.getId(),
            "Meeting after reference date 1"
        );
        
        meetingService.createMeeting(
            LocalDate.now().plusDays(7),
            LocalTime.of(18, 0),
            LocalTime.of(20, 0),
            testProject.getId(),
            "Meeting after reference date 2"
        );
        
        // Find meetings after the reference date
        List<Meeting> meetings = meetingService.findByDateAfter(referenceDate);
        
        // Use standard assert methods
        org.junit.jupiter.api.Assertions.assertNotNull(meetings);
        
        // Only count the meetings we explicitly created in this test
        long ourMeetingCount = meetings.stream()
            .filter(m -> m.getNotes() != null && 
                    (m.getNotes().equals("Meeting after reference date 1") || 
                        m.getNotes().equals("Meeting after reference date 2")))
            .count();
        
        org.junit.jupiter.api.Assertions.assertEquals(2, ourMeetingCount);
    }
    
    @Test
    public void testFindByDateBetween() {
        LocalDate startDate = LocalDate.now().plusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(15);
        
        // Create meetings in different date ranges with unique identifiers
        // One meeting before range
        meetingService.createMeeting(
            LocalDate.now().plusDays(2),
            LocalTime.of(18, 0),
            LocalTime.of(20, 0),
            testProject.getId(),
            "DateBetween_Before"
        );
        
        // Two meetings in range - guaranteed count
        meetingService.createMeeting(
            LocalDate.now().plusDays(7),
            LocalTime.of(18, 0),
            LocalTime.of(20, 0),
            testProject.getId(),
            "DateBetween_InRange1"
        );
        
        meetingService.createMeeting(
            LocalDate.now().plusDays(12),
            LocalTime.of(18, 0),
            LocalTime.of(20, 0),
            testProject.getId(),
            "DateBetween_InRange2"
        );
        
        // One meeting after range
        meetingService.createMeeting(
            LocalDate.now().plusDays(20),
            LocalTime.of(18, 0),
            LocalTime.of(20, 0),
            testProject.getId(),
            "DateBetween_After"
        );
        
        // Find meetings in the date range
        List<Meeting> meetings = meetingService.findByDateBetween(startDate, endDate);
        org.junit.jupiter.api.Assertions.assertNotNull(meetings);
        
        // Only count the meetings we explicitly created in this test with our identifiers
        long inRangeMeetingCount = meetings.stream()
            .filter(m -> m.getNotes() != null && 
                      (m.getNotes().equals("DateBetween_InRange1") || 
                       m.getNotes().equals("DateBetween_InRange2")))
            .count();
        
        org.junit.jupiter.api.Assertions.assertEquals(2, inRangeMeetingCount);
    }

    @Test
    public void testGetGanttDataForDate() {
        // Create mock date filter to test
        LocalDate testDate = LocalDate.now();
        
        // Create mock repositories instead of using undefined variables
        ProjectRepository mockProjectRepo = mock(ProjectRepository.class);
        TaskRepository mockTaskRepo = mock(TaskRepository.class);
        MilestoneRepository mockMilestoneRepo = mock(MilestoneRepository.class);
        
        // Mock transformation service to control output
        GanttChartTransformationService mockTransformationService = mock(GanttChartTransformationService.class);
        when(mockTransformationService.filterChartData(anyList(), any(), any(), any(), any(), any()))
            .thenAnswer(inv -> inv.getArgument(0)); // Return original list
        
        // Create service with mocks
        GanttDataServiceImpl serviceWithMocks = new GanttDataServiceImpl(
            mockProjectRepo, mockTaskRepo, mockMilestoneRepo, mockTransformationService);
        
        // Setup mock behavior for repositories to avoid NPE
        Project mockProject = mock(Project.class);
        when(mockProject.getStartDate()).thenReturn(LocalDate.now());
        when(mockProject.getHardDeadline()).thenReturn(LocalDate.now().plusDays(30));
        when(mockProjectRepo.findById(anyLong())).thenReturn(Optional.of(mockProject));
        when(mockTaskRepo.findByProject(any(Project.class))).thenReturn(Collections.emptyList());
        when(mockMilestoneRepo.findByProject(any(Project.class))).thenReturn(Collections.emptyList());
        
        // Test with fixed date
        Map<String, Object> result = serviceWithMocks.getGanttDataForDate(1L, testDate);
        
        // Verify date is passed correctly to filters
        assertTrue(result.containsKey("startDate"));
        assertEquals(testDate.format(DateTimeFormatter.ISO_LOCAL_DATE), result.get("startDate"));
    }
    
    @Test
    public void testGetUpcomingMeetings() {
        // Create meetings with different dates and unique identifiers
        // Two upcoming meetings within 7 days
        meetingService.createMeeting(
            LocalDate.now().plusDays(1),
            LocalTime.of(18, 0),
            LocalTime.of(20, 0),
            testProject.getId(),
            "Upcoming_Within7Days_1"
        );
        
        meetingService.createMeeting(
            LocalDate.now().plusDays(4),
            LocalTime.of(18, 0),
            LocalTime.of(20, 0),
            testProject.getId(),
            "Upcoming_Within7Days_2"
        );
        
        // One meeting too far in the future (outside 7 days window)
        meetingService.createMeeting(
            LocalDate.now().plusDays(10),
            LocalTime.of(18, 0),
            LocalTime.of(20, 0),
            testProject.getId(),
            "Upcoming_Outside7Days"
        );
        
        // Test finding upcoming meetings within 7 days
        List<Meeting> upcomingMeetings = meetingService.getUpcomingMeetings(testProject.getId(), 7);
        org.junit.jupiter.api.Assertions.assertNotNull(upcomingMeetings);
        
        // Only count the meetings we explicitly created in this test with our identifiers
        long upcomingMeetingCount = upcomingMeetings.stream()
            .filter(m -> m.getNotes() != null && 
                    (m.getNotes().equals("Upcoming_Within7Days_1") || 
                    m.getNotes().equals("Upcoming_Within7Days_2")))
            .count();
        
        org.junit.jupiter.api.Assertions.assertEquals(2, upcomingMeetingCount);
    }
    
    @Test
    public void testInvalidMeetingCreation() {
        // Test null date
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            meetingService.createMeeting(
                null, 
                LocalTime.of(18, 0),
                LocalTime.of(20, 0),
                testProject.getId(),
                "Test notes"
            );
        });
        assertTrue(exception.getMessage().contains("date cannot be null"));
        
        // Test null start time
        exception = assertThrows(IllegalArgumentException.class, () -> {
            meetingService.createMeeting(
                LocalDate.now().plusDays(1),
                null,
                LocalTime.of(20, 0),
                testProject.getId(),
                "Test notes"
            );
        });
        assertTrue(exception.getMessage().contains("start and end times cannot be null"));
        
        // Test invalid time range (end before start)
        exception = assertThrows(IllegalArgumentException.class, () -> {
            meetingService.createMeeting(
                LocalDate.now().plusDays(1),
                LocalTime.of(18, 0),
                LocalTime.of(17, 0), // End time before start time
                testProject.getId(),
                "Test notes"
            );
        });
        assertTrue(exception.getMessage().contains("end time cannot be before start time"));
        
        // Test null project ID
        exception = assertThrows(IllegalArgumentException.class, () -> {
            meetingService.createMeeting(
                LocalDate.now().plusDays(1),
                LocalTime.of(18, 0),
                LocalTime.of(20, 0),
                null,
                "Test notes"
            );
        });
        assertTrue(exception.getMessage().contains("Project ID cannot be null"));
    }
    
    @Test
    public void testNonExistentMeetingUpdates() {
        Long nonExistentId = 999999L;
        
        // Test updating date/time of non-existent meeting
        Meeting result = meetingService.updateMeetingDateTime(
            nonExistentId,
            LocalDate.now().plusDays(1),
            LocalTime.of(18, 0),
            LocalTime.of(20, 0)
        );
        assertNull(result);
        
        // Test updating notes of non-existent meeting
        result = meetingService.updateNotes(
            nonExistentId,
            "New notes"
        );
        assertNull(result);
    }
    
    @Test
    public void testInvalidMeetingTimeUpdates() {
        // Create a meeting
        Meeting meeting = meetingService.createMeeting(
            LocalDate.now().plusDays(1),
            LocalTime.of(18, 0),
            LocalTime.of(20, 0),
            testProject.getId(),
            "Initial notes"
        );
        
        // Test updating with end time before start time
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            meetingService.updateMeetingDateTime(
                meeting.getId(),
                meeting.getDate(),
                LocalTime.of(18, 0),
                LocalTime.of(17, 0) // End time before start time
            );
        });
        assertTrue(exception.getMessage().contains("end time cannot be before start time"));
    }
}
