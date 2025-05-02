package org.frcpm.repositories;

import org.frcpm.config.DatabaseConfig;
import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.repositories.specific.MeetingRepository;
import org.frcpm.repositories.specific.ProjectRepository;
import org.frcpm.utils.TestEnvironmentSetup;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({MockitoExtension.class, TestEnvironmentSetup.class})
public class MeetingRepositoryTest {
    
    private static final Logger LOGGER = Logger.getLogger(MeetingRepositoryTest.class.getName());
    
    private MeetingRepository repository;
    private ProjectRepository projectRepository;
    private Project testProject;
    
    @BeforeEach
    public void setUp() {
        // Force development mode for testing
        System.setProperty("app.db.dev", "true");
        
        // Initialize a clean database for each test
        DatabaseConfig.reinitialize(true);
        
        repository = RepositoryFactory.getMeetingRepository();
        projectRepository = RepositoryFactory.getProjectRepository();
        
        // Create test project in a transaction
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            
            testProject = new Project(
                "Test Meeting Project", 
                LocalDate.now(), 
                LocalDate.now().plusWeeks(6), 
                LocalDate.now().plusWeeks(8)
            );
            
            em.persist(testProject);
            tx.commit();
            
            // Create test meetings in their own transaction
            createTestMeetings();
            
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            LOGGER.severe("Error setting up test data: " + e.getMessage());
            e.printStackTrace();
            fail("Failed to set up test data: " + e.getMessage());
        } finally {
            em.close();
        }
    }
    
    @AfterEach
    public void tearDown() {
        try {
            // Clean up test data in reverse order
            cleanupTestMeetings();
            
            EntityManager em = DatabaseConfig.getEntityManager();
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                Project project = em.find(Project.class, testProject.getId());
                if (project != null) {
                    em.remove(project);
                }
                tx.commit();
            } catch (Exception e) {
                if (tx.isActive()) {
                    tx.rollback();
                }
                LOGGER.warning("Error cleaning up test project: " + e.getMessage());
            } finally {
                em.close();
            }
        } finally {
            DatabaseConfig.shutdown();
        }
    }
    
    private void createTestMeetings() {
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            
            // Get a fresh reference to the project
            Project project = em.find(Project.class, testProject.getId());
            
            Meeting meeting1 = new Meeting(
                LocalDate.now().plusDays(1),
                LocalTime.of(18, 0), // 6:00 PM
                LocalTime.of(20, 0), // 8:00 PM
                project
            );
            meeting1.setNotes("First meeting notes");
            
            Meeting meeting2 = new Meeting(
                LocalDate.now().plusDays(3),
                LocalTime.of(16, 0), // 4:00 PM
                LocalTime.of(18, 30), // 6:30 PM
                project
            );
            meeting2.setNotes("Second meeting notes");
            
            Meeting meeting3 = new Meeting(
                LocalDate.now().plusDays(7),
                LocalTime.of(10, 0), // 10:00 AM
                LocalTime.of(12, 0), // 12:00 PM
                project
            );
            meeting3.setNotes("Third meeting notes");
            
            em.persist(meeting1);
            em.persist(meeting2);
            em.persist(meeting3);
            
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            LOGGER.severe("Error creating test meetings: " + e.getMessage());
            e.printStackTrace();
            fail("Failed to create test meetings: " + e.getMessage());
        } finally {
            em.close();
        }
    }
    
    private void cleanupTestMeetings() {
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            
            // Get a fresh reference to the project
            Project project = em.find(Project.class, testProject.getId());
            
            // Use a native query to avoid cache issues
            em.createQuery("DELETE FROM Meeting m WHERE m.project = :project")
                .setParameter("project", project)
                .executeUpdate();
            
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            LOGGER.warning("Error cleaning up test meetings: " + e.getMessage());
        } finally {
            em.close();
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
        assertNotNull(meetings);
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
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        Meeting saved = null;
        
        try {
            tx.begin();
            // Get a fresh reference to the project
            Project project = em.find(Project.class, testProject.getId());
            
            Meeting newMeeting = new Meeting(
                LocalDate.now().plusDays(10),
                LocalTime.of(14, 0), // 2:00 PM
                LocalTime.of(16, 0), // 4:00 PM
                project
            );
            newMeeting.setNotes("Test save meeting notes");
            
            em.persist(newMeeting);
            tx.commit();
            
            // Need to set the ID field manually
            saved = newMeeting;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            LOGGER.severe("Error saving test meeting: " + e.getMessage());
            e.printStackTrace();
            fail("Failed to save test meeting: " + e.getMessage());
        } finally {
            em.close();
        }
        
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
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        Meeting saved = null;
        
        try {
            tx.begin();
            // Get a fresh reference to the project
            Project project = em.find(Project.class, testProject.getId());
            
            // First, create a meeting
            Meeting meeting = new Meeting(
                LocalDate.now().plusDays(15),
                LocalTime.of(14, 0), // 2:00 PM
                LocalTime.of(16, 0), // 4:00 PM
                project
            );
            
            em.persist(meeting);
            em.flush();
            
            // Now update it
            meeting.setDate(LocalDate.now().plusDays(16));
            meeting.setStartTime(LocalTime.of(15, 0)); // 3:00 PM
            meeting.setNotes("Updated meeting notes");
            
            em.merge(meeting);
            tx.commit();
            
            // Need to set the ID field manually
            saved = meeting;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            LOGGER.severe("Error updating test meeting: " + e.getMessage());
            e.printStackTrace();
            fail("Failed to update test meeting: " + e.getMessage());
        } finally {
            em.close();
        }
        
        // Verify the update through the repository
        Optional<Meeting> found = repository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(LocalDate.now().plusDays(16), found.get().getDate());
        assertEquals(LocalTime.of(15, 0), found.get().getStartTime());
        assertEquals("Updated meeting notes", found.get().getNotes());
    }
    
    @Test
    public void testDelete() {
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        Meeting saved = null;
        
        try {
            tx.begin();
            // Get a fresh reference to the project
            Project project = em.find(Project.class, testProject.getId());
            
            // First, create a meeting
            Meeting meeting = new Meeting(
                LocalDate.now().plusDays(20),
                LocalTime.of(14, 0), // 2:00 PM
                LocalTime.of(16, 0), // 4:00 PM
                project
            );
            
            em.persist(meeting);
            tx.commit();
            
            // Need to set the ID field manually
            saved = meeting;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            LOGGER.severe("Error creating meeting for delete test: " + e.getMessage());
            e.printStackTrace();
            fail("Failed to create meeting for delete test: " + e.getMessage());
        } finally {
            em.close();
        }
        
        Long id = saved.getId();
        
        // Now delete it
        repository.delete(saved);
        
        // Verify the deletion
        Optional<Meeting> found = repository.findById(id);
        assertFalse(found.isPresent());
    }
    
    @Test
    public void testDeleteById() {
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        Meeting saved = null;
        
        try {
            tx.begin();
            // Get a fresh reference to the project
            Project project = em.find(Project.class, testProject.getId());
            
            // First, create a meeting
            Meeting meeting = new Meeting(
                LocalDate.now().plusDays(25),
                LocalTime.of(14, 0), // 2:00 PM
                LocalTime.of(16, 0), // 4:00 PM
                project
            );
            
            em.persist(meeting);
            tx.commit();
            
            // Need to set the ID field manually
            saved = meeting;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            LOGGER.severe("Error creating meeting for deleteById test: " + e.getMessage());
            e.printStackTrace();
            fail("Failed to create meeting for deleteById test: " + e.getMessage());
        } finally {
            em.close();
        }
        
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
        
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        Meeting saved = null;
        
        try {
            tx.begin();
            // Get a fresh reference to the project
            Project project = em.find(Project.class, testProject.getId());
            
            // Add a new meeting
            Meeting meeting = new Meeting(
                LocalDate.now().plusDays(30),
                LocalTime.of(14, 0), // 2:00 PM
                LocalTime.of(16, 0), // 4:00 PM
                project
            );
            
            em.persist(meeting);
            tx.commit();
            
            // Need to set the ID field manually
            saved = meeting;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            LOGGER.severe("Error creating meeting for count test: " + e.getMessage());
            e.printStackTrace();
            fail("Failed to create meeting for count test: " + e.getMessage());
        } finally {
            em.close();
        }
        
        // Verify count increased
        long newCount = repository.count();
        assertEquals(initialCount + 1, newCount);
    }
}