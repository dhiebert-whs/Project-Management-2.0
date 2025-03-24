package org.frcpm.services;

import org.frcpm.models.Meeting;
import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.repositories.RepositoryFactory;
import org.frcpm.repositories.specific.MeetingRepository;
import org.frcpm.repositories.specific.ProjectRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MeetingService.
 */
public class MeetingServiceTest {

    @Mock
    private MeetingRepository meetingRepository;
    
    @Mock
    private ProjectRepository projectRepository;
    
    private MeetingService meetingService;
    
    private Project testProject;
    private Meeting testMeeting;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        
        // Set up repository factory mock
        mockStatic(RepositoryFactory.class);
        when(RepositoryFactory.getMeetingRepository()).thenReturn(meetingRepository);
        when(RepositoryFactory.getProjectRepository()).thenReturn(projectRepository);
        
        // Create service instance
        meetingService = new ServiceFactory().getMeetingService();
        
        // Set up test objects
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");
        
        testMeeting = new Meeting();
        testMeeting.setId(1L);
        testMeeting.setProject(testProject);
        testMeeting.setTitle("Test Meeting");
        testMeeting.setDate(LocalDate.now());
        testMeeting.setStartTime(LocalTime.of(9, 0));
        testMeeting.setEndTime(LocalTime.of(10, 0));
        testMeeting.setLocation("Test Location");
        testMeeting.setRecurring(false);
        testMeeting.setCancelled(false);
        testMeeting.setMilestones(new ArrayList<>());
    }
    
    @Test
    public void testCreateOneTimeMeeting() {
        // Set up mocks
        when(meetingRepository.save(any(Meeting.class))).thenReturn(testMeeting);
        
        // Call method
        Meeting result = meetingService.createOneTimeMeeting(
                testProject, "Test Meeting", "Test Description",
                LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(10, 0),
                "Test Location");
        
        // Verify
        assertNotNull(result);
        assertEquals(testMeeting.getId(), result.getId());
        assertEquals(testMeeting.getTitle(), result.getTitle());
        assertEquals(testMeeting.getDate(), result.getDate());
        
        verify(meetingRepository, times(1)).save(any(Meeting.class));
    }
    
    @Test
    public void testCreateRecurringMeetings() {
        // Set up mocks
        when(meetingRepository.save(any(Meeting.class))).thenReturn(testMeeting);
        
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusWeeks(3);
        
        // Call method
        List<Meeting> results = meetingService.createRecurringMeetings(
                testProject, "Weekly Meeting", "Test Description",
                startDate, endDate, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(10, 0),
                "Test Location");
        
        // Verify
        assertNotNull(results);
        // Should create 4 meetings (1 for each week)
        assertEquals(4, results.size());
        
        verify(meetingRepository, times(4)).save(any(Meeting.class));
    }
    
    @Test
    public void testGetUpcomingMeetings() {
        // Set up mocks
        Meeting pastMeeting = new Meeting();
        pastMeeting.setDate(LocalDate.now().minusDays(1));
        pastMeeting.setCancelled(false);
        
        Meeting upcomingMeeting1 = new Meeting();
        upcomingMeeting1.setDate(LocalDate.now());
        upcomingMeeting1.setCancelled(false);
        
        Meeting upcomingMeeting2 = new Meeting();
        upcomingMeeting2.setDate(LocalDate.now().plusDays(1));
        upcomingMeeting2.setCancelled(false);
        
        Meeting cancelledMeeting = new Meeting();
        cancelledMeeting.setDate(LocalDate.now().plusDays(2));
        cancelledMeeting.setCancelled(true);
        
        List<Meeting> allMeetings = Arrays.asList(
                pastMeeting, upcomingMeeting1, upcomingMeeting2, cancelledMeeting);
        
        when(meetingRepository.findByProject(testProject)).thenReturn(allMeetings);
        
        // Call method
        List<Meeting> results = meetingService.getUpcomingMeetings(testProject);
        
        // Verify
        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.contains(upcomingMeeting1));
        assertTrue(results.contains(upcomingMeeting2));
        assertFalse(results.contains(pastMeeting));
        assertFalse(results.contains(cancelledMeeting));
        
        verify(meetingRepository, times(1)).findByProject(testProject);
    }
    
    @Test
    public void testCancelMeeting() {
        // Set up mocks
        Meeting meetingToCancel = new Meeting();
        meetingToCancel.setId(1L);
        meetingToCancel.setCancelled(false);
        
        Meeting cancelledMeeting = new Meeting();
        cancelledMeeting.setId(1L);
        cancelledMeeting.setCancelled(true);
        
        when(meetingRepository.update(any(Meeting.class))).thenReturn(cancelledMeeting);
        
        // Call method
        Meeting result = meetingService.cancelMeeting(meetingToCancel);
        
        // Verify
        assertNotNull(result);
        assertTrue(result.isCancelled());
        
        verify(meetingRepository, times(1)).update(any(Meeting.class));
    }
    
    @Test
    public void testFindConflictingMeetings() {
        // Set up mocks
        LocalDate testDate = LocalDate.now();
        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(11, 0);
        
        Meeting nonConflicting1 = new Meeting();
        nonConflicting1.setStartTime(LocalTime.of(8, 0));
        nonConflicting1.setEndTime(LocalTime.of(9, 0));
        nonConflicting1.setCancelled(false);
        
        Meeting conflicting1 = new Meeting();
        conflicting1.setStartTime(LocalTime.of(9, 30));
        conflicting1.setEndTime(LocalTime.of(10, 30));
        conflicting1.setCancelled(false);
        
        Meeting conflicting2 = new Meeting();
        conflicting2.setStartTime(LocalTime.of(10, 30));
        conflicting2.setEndTime(LocalTime.of(11, 30));
        conflicting2.setCancelled(false);
        
        Meeting conflicting3 = new Meeting();
        conflicting3.setStartTime(LocalTime.of(9, 0));
        conflicting3.setEndTime(LocalTime.of(12, 0));
        conflicting3.setCancelled(false);
        
        Meeting nonConflicting2 = new Meeting();
        nonConflicting2.setStartTime(LocalTime.of(11, 0));
        nonConflicting2.setEndTime(LocalTime.of(12, 0));
        nonConflicting2.setCancelled(false);
        
        Meeting cancelledConflicting = new Meeting();
        cancelledConflicting.setStartTime(LocalTime.of(10, 0));
        cancelledConflicting.setEndTime(LocalTime.of(11, 0));
        cancelledConflicting.setCancelled(true);
        
        List<Meeting> meetingsOnDate = Arrays.asList(
                nonConflicting1, conflicting1, conflicting2, conflicting3, 
                nonConflicting2, cancelledConflicting);
        
        when(meetingRepository.findByDate(testDate)).thenReturn(meetingsOnDate);
        
        // Call method
        List<Meeting> results = meetingService.findConflictingMeetings(testDate, startTime, endTime);
        
        // Verify
        assertNotNull(results);
        assertEquals(3, results.size());
        assertTrue(results.contains(conflicting1));
        assertTrue(results.contains(conflicting2));
        assertTrue(results.contains(conflicting3));
        assertFalse(results.contains(nonConflicting1));
        assertFalse(results.contains(nonConflicting2));
        assertFalse(results.contains(cancelledConflicting));
        
        verify(meetingRepository, times(1)).findByDate(testDate);
    }
}
