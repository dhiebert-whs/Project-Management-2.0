package org.frcpm.repositories;

import org.frcpm.config.DatabaseConfig;
import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.repositories.specific.MeetingRepository;
import org.frcpm.repositories.specific.ProjectRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class MeetingRepositoryTest {
    
    private MeetingRepository repository;
    private ProjectRepository projectRepository;
    private Project testProject;
    
    @BeforeEach
    public void setUp() {
        DatabaseConfig.initialize();
        repository = RepositoryFactory.getMeetingRepository();
        projectRepository = RepositoryFactory.getProjectRepository();
        
        // Create test project
        testProject = new Project(
            "Test Meeting Project", 
            LocalDate.now(), 
            LocalDate.now().plusWeeks(6), 
            LocalDate.now().plusWeeks(8)
        );
        testProject = projectRepository.save(testProject);
        
        // Add test data
        createTestMeetings();
    }
    
    @AfterEach
    public void tearDown() {
        // Clean up test data
        cleanupTestMeetings();
        projectRepository.delete(testProject);
        DatabaseConfig.shutdown();
    }
    
    private void createTestMeetings() {
        Meeting meeting1 = new Meeting(
            LocalDate.now().plusDays(1),
            LocalTime.of(18, 0), // 6:00 PM
            LocalTime.of(20, 0), // 8:00 PM
            testProject
        );
        meeting1.setNotes("First meeting notes");
        
        Meeting meeting2 = new Meeting(
            LocalDate.now().plusDays(3),
            LocalTime.of(16, 0), // 4:00 PM
            LocalTime.of(18, 30), // 6:30 PM
            testProject
        );
        meeting2.setNotes("Second meeting notes");
        
        Meeting meeting3 = new Meeting(
            LocalDate.now().plusDays(7),
            LocalTime.of(10, 0), // 10:00 AM
            LocalTime.of(12, 0), // 12:00 PM
            testProject
        );
        meeting3.setNotes("Third meeting notes");
        
        repository.save(meeting1);
        repository.save(meeting2);
        repository.save(meeting3);
    }
    
    private void cleanupTestMeetings() {
        List<Meeting> meetings = repository.findByProject(testProject);
        for (Meeting meeting : meetings) {
            repository.delete(meeting);
        }
    }
    
    @Test
    public void testFindAll() {
        List<Meeting> meetings = repository.findAll();
        assertNotNull(meetings);
        assertTrue(meetings.size() >= 3);
    }
    
    @Test
    public void testFindById() {
        // First, get a meeting ID from the DB
        List<Meeting> meetings = repository.findByProject(testProject);
        Meeting firstMeeting = meetings.get(0);
        
        // Now test findById
        Optional<Meeting> found = repository.findById(firstMeeting.getId());
        assertTrue(found.isPresent());
        assertEquals(firstMeeting.getDate(), found.get().getDate());
        assertEquals(firstMeeting.getStartTime(), found.get().getStartTime());
    }
    
    @Test
    public void testFindByProject() {
        List<Meeting> meetings = repository.findByProject(testProject);
        assertFalse(meetings.isEmpty());
        assertTrue(meetings.stream().allMatch(m -> m.getProject().getId().equals(testProject.getId())));
        assertEquals(3, meetings.size());
    }
    
    @Test
    public void testFindByDate() {
        LocalDate meetingDate = LocalDate.now().plusDays(3);
        List<Meeting> meetings = repository.findByDate(meetingDate);
        assertFalse(meetings.isEmpty());
        
        for (Meeting meeting : meetings) {
            assertEquals(meetingDate, meeting.getDate());
        }
        
        // Should find exactly one meeting on this date
        assertEquals(1, meetings.size());
    }
    
    @Test
    public void testFindByDateAfter() {
        LocalDate cutoffDate = LocalDate.now().plusDays(2);
        List<Meeting> meetings = repository.findByDateAfter(cutoffDate);
        assertFalse(meetings.isEmpty());
        
        for (Meeting meeting : meetings) {
            assertTrue(meeting.getDate().isAfter(cutoffDate));
        }
        
        // Should find the second and third meetings
        assertEquals(2, meetings.size());
    }
    
    @Test
    public void testFindByDateBetween() {
        LocalDate startDate = LocalDate.now().plusDays(2);
        LocalDate endDate = LocalDate.now().plusDays(5);
        
        List<Meeting> meetings = repository.findByDateBetween(startDate, endDate);
        assertFalse(meetings.isEmpty());
        
        for (Meeting meeting : meetings) {
            assertTrue(!meeting.getDate().isBefore(startDate) && !meeting.getDate().isAfter(endDate));
        }
        
        // Should find the second meeting
        assertEquals(1, meetings.size());
    }
    
    @Test
    public void testSave() {
        Meeting newMeeting = new Meeting(
            LocalDate.now().plusDays(10),
            LocalTime.of(14, 0), // 2:00 PM
            LocalTime.of(16, 0), // 4:00 PM
            testProject
        );
        newMeeting.setNotes("Test save meeting notes");
        
        Meeting saved = repository.save(newMeeting);
        assertNotNull(saved.getId());
        
        // Verify it was saved
        Optional<Meeting> found = repository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(LocalDate.now().plusDays(10), found.get().getDate());
        assertEquals(LocalTime.of(14, 0), found.get().getStartTime());
        assertEquals("Test save meeting notes", found.get().getNotes());
    }
    
    @Test
    public void testUpdate() {
        // First, create a meeting
        Meeting meeting = new Meeting(
            LocalDate.now().plusDays(15),
            LocalTime.of(14, 0), // 2:00 PM
            LocalTime.of(16, 0), // 4:00 PM
            testProject
        );
        Meeting saved = repository.save(meeting);
        
        // Now update it
        saved.setDate(LocalDate.now().plusDays(16));
        saved.setStartTime(LocalTime.of(15, 0)); // 3:00 PM
        saved.setNotes("Updated meeting notes");
        Meeting updated = repository.save(saved);
        
        // Verify the update
        assertEquals(LocalDate.now().plusDays(16), updated.getDate());
        assertEquals(LocalTime.of(15, 0), updated.getStartTime());
        assertEquals("Updated meeting notes", updated.getNotes());
        
        // Check in DB
        Optional<Meeting> found = repository.findById(updated.getId());
        assertTrue(found.isPresent());
        assertEquals(LocalDate.now().plusDays(16), found.get().getDate());
        assertEquals(LocalTime.of(15, 0), found.get().getStartTime());
        assertEquals("Updated meeting notes", found.get().getNotes());
    }
    
    @Test
    public void testDelete() {
        // First, create a meeting
        Meeting meeting = new Meeting(
            LocalDate.now().plusDays(20),
            LocalTime.of(14, 0), // 2:00 PM
            LocalTime.of(16, 0), // 4:00 PM
            testProject
        );
        Meeting saved = repository.save(meeting);
        Long id = saved.getId();
        
        // Now delete it
        repository.delete(saved);
        
        // Verify the deletion
        Optional<Meeting> found = repository.findById(id);
        assertFalse(found.isPresent());
    }
    
    @Test
    public void testDeleteById() {
        // First, create a meeting
        Meeting meeting = new Meeting(
            LocalDate.now().plusDays(25),
            LocalTime.of(14, 0), // 2:00 PM
            LocalTime.of(16, 0), // 4:00 PM
            testProject
        );
        Meeting saved = repository.save(meeting);
        Long id = saved.getId();
        
        // Now delete it by ID
        boolean result = repository.deleteById(id);
        assertTrue(result);
        
        // Verify the deletion
        Optional<Meeting> found = repository.findById(id);
        assertFalse(found.isPresent());
    }
    
    @Test
    public void testCount() {
        long initialCount = repository.count();
        
        // Add a new meeting
        Meeting meeting = new Meeting(
            LocalDate.now().plusDays(30),
            LocalTime.of(14, 0), // 2:00 PM
            LocalTime.of(16, 0), // 4:00 PM
            testProject
        );
        repository.save(meeting);
        
        // Verify count increased
        long newCount = repository.count();
        assertEquals(initialCount + 1, newCount);
    }
}