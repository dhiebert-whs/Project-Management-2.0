// src/test/java/org/frcpm/repositories/specific/MeetingRepositoryTest.java
package org.frcpm.repositories.specific;

import org.frcpm.models.Attendance;
import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.models.TeamMember;
import org.frcpm.repositories.BaseRepositoryTest;
import org.frcpm.repositories.impl.MeetingRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the MeetingRepository implementation.
 */
public class MeetingRepositoryTest extends BaseRepositoryTest {

    private MeetingRepository meetingRepository;
    private Project testProject;
    private Project secondProject;
    private TeamMember teamMember1;
    private TeamMember teamMember2;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        meetingRepository = new MeetingRepositoryImpl();
    }

    @Override
    protected void setupTestData() {
        // Create test projects and team members
        beginTransaction();
        
        testProject = new Project("Test Project", 
                LocalDate.now().minusDays(10), 
                LocalDate.now().plusDays(80), 
                LocalDate.now().plusDays(90));
        em.persist(testProject);
        
        secondProject = new Project("Second Project", 
                LocalDate.now().minusDays(5), 
                LocalDate.now().plusDays(25), 
                LocalDate.now().plusDays(30));
        em.persist(secondProject);
        
        teamMember1 = new TeamMember();
        teamMember1.setFirstName("John");
        teamMember1.setLastName("Doe");
        teamMember1.setEmail("john.doe@example.com");
        teamMember1.setUsername("johndoe");
        em.persist(teamMember1);
        
        teamMember2 = new TeamMember();
        teamMember2.setFirstName("Jane");
        teamMember2.setLastName("Smith");
        teamMember2.setEmail("jane.smith@example.com");
        teamMember2.setUsername("janesmith");
        em.persist(teamMember2);
        
        commitTransaction();
    }

    @Test
    public void testSave() {
        // Create a new meeting
        Meeting meeting = new Meeting(
                LocalDate.now().plusDays(5),
                LocalTime.of(9, 0),
                LocalTime.of(10, 30),
                testProject);
        meeting.setNotes("Test meeting notes");

        // Save the meeting
        Meeting savedMeeting = meetingRepository.save(meeting);

        // Verify saved meeting
        assertNotNull(savedMeeting);
        assertNotNull(savedMeeting.getId());
        assertEquals(testProject.getId(), savedMeeting.getProject().getId());
        assertEquals("Test meeting notes", savedMeeting.getNotes());
        assertEquals(LocalTime.of(9, 0), savedMeeting.getStartTime());
        assertEquals(LocalTime.of(10, 30), savedMeeting.getEndTime());
    }

    @Test
    public void testFindById() {
        // Create and save a meeting
        beginTransaction();
        Meeting meeting = new Meeting(
                LocalDate.now().plusDays(3),
                LocalTime.of(14, 0),
                LocalTime.of(15, 0),
                testProject);
        meeting.setNotes("Find by ID test");
        em.persist(meeting);
        Long meetingId = meeting.getId();
        commitTransaction();

        // Find by ID
        Optional<Meeting> found = meetingRepository.findById(meetingId);

        // Verify the result
        assertTrue(found.isPresent());
        assertEquals("Find by ID test", found.get().getNotes());
        assertEquals(testProject.getId(), found.get().getProject().getId());
    }

    @Test
    public void testFindAll() {
        // Create multiple meetings
        beginTransaction();
        Meeting meeting1 = new Meeting(
                LocalDate.now().plusDays(1),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                testProject);
        
        Meeting meeting2 = new Meeting(
                LocalDate.now().plusDays(2),
                LocalTime.of(11, 0),
                LocalTime.of(12, 0),
                testProject);
        
        Meeting meeting3 = new Meeting(
                LocalDate.now().plusDays(3),
                LocalTime.of(13, 0),
                LocalTime.of(14, 0),
                secondProject);
        
        em.persist(meeting1);
        em.persist(meeting2);
        em.persist(meeting3);
        commitTransaction();

        // Find all meetings
        List<Meeting> meetings = meetingRepository.findAll();

        // Verify results
        assertNotNull(meetings);
        assertEquals(3, meetings.size());
    }

    @Test
    public void testUpdate() {
        // Create and save a meeting
        beginTransaction();
        Meeting meeting = new Meeting(
                LocalDate.now().plusDays(7),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                testProject);
        meeting.setNotes("Original notes");
        em.persist(meeting);
        Long meetingId = meeting.getId();
        commitTransaction();

        // Update the meeting
        Optional<Meeting> found = meetingRepository.findById(meetingId);
        assertTrue(found.isPresent());
        
        Meeting toUpdate = found.get();
        toUpdate.setDate(LocalDate.now().plusDays(8));
        toUpdate.setStartTime(LocalTime.of(10, 0));
        toUpdate.setEndTime(LocalTime.of(11, 30));
        toUpdate.setNotes("Updated notes");
        
        meetingRepository.save(toUpdate);

        // Verify the update
        Optional<Meeting> updated = meetingRepository.findById(meetingId);
        assertTrue(updated.isPresent());
        assertEquals(LocalDate.now().plusDays(8), updated.get().getDate());
        assertEquals(LocalTime.of(10, 0), updated.get().getStartTime());
        assertEquals(LocalTime.of(11, 30), updated.get().getEndTime());
        assertEquals("Updated notes", updated.get().getNotes());
    }

    @Test
    public void testDelete() {
        // Create and save a meeting
        beginTransaction();
        Meeting meeting = new Meeting(
                LocalDate.now().plusDays(5),
                LocalTime.of(13, 0),
                LocalTime.of(14, 0),
                testProject);
        em.persist(meeting);
        Long meetingId = meeting.getId();
        commitTransaction();

        // Verify it exists
        Optional<Meeting> found = meetingRepository.findById(meetingId);
        assertTrue(found.isPresent());

        // Delete the meeting
        meetingRepository.delete(found.get());

        // Verify it was deleted
        Optional<Meeting> deleted = meetingRepository.findById(meetingId);
        assertFalse(deleted.isPresent());
    }

    @Test
    public void testDeleteById() {
        // Create and save a meeting
        beginTransaction();
        Meeting meeting = new Meeting(
                LocalDate.now().plusDays(6),
                LocalTime.of(15, 0),
                LocalTime.of(16, 0),
                testProject);
        em.persist(meeting);
        Long meetingId = meeting.getId();
        commitTransaction();

        // Verify it exists
        Optional<Meeting> found = meetingRepository.findById(meetingId);
        assertTrue(found.isPresent());

        // Delete by ID
        boolean result = meetingRepository.deleteById(meetingId);
        assertTrue(result);

        // Verify it was deleted
        Optional<Meeting> deleted = meetingRepository.findById(meetingId);
        assertFalse(deleted.isPresent());
    }

    @Test
    public void testCount() {
        // Create multiple meetings
        beginTransaction();
        Meeting meeting1 = new Meeting(
                LocalDate.now().plusDays(1),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                testProject);
        
        Meeting meeting2 = new Meeting(
                LocalDate.now().plusDays(2),
                LocalTime.of(11, 0),
                LocalTime.of(12, 0),
                testProject);
        
        em.persist(meeting1);
        em.persist(meeting2);
        commitTransaction();

        // Count meetings
        long count = meetingRepository.count();

        // Verify count
        assertEquals(2, count);
    }

    @Test
    public void testFindByProject() {
        // Create meetings for different projects
        beginTransaction();
        Meeting meeting1 = new Meeting(
                LocalDate.now().plusDays(1),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                testProject);
        
        Meeting meeting2 = new Meeting(
                LocalDate.now().plusDays(2),
                LocalTime.of(11, 0),
                LocalTime.of(12, 0),
                testProject);
        
        Meeting meeting3 = new Meeting(
                LocalDate.now().plusDays(3),
                LocalTime.of(13, 0),
                LocalTime.of(14, 0),
                secondProject);
        
        em.persist(meeting1);
        em.persist(meeting2);
        em.persist(meeting3);
        commitTransaction();

        // Find meetings for testProject
        List<Meeting> projectMeetings = meetingRepository.findByProject(testProject);

        // Verify results
        assertNotNull(projectMeetings);
        assertEquals(2, projectMeetings.size());
        assertTrue(projectMeetings.stream().allMatch(m -> m.getProject().getId().equals(testProject.getId())));
    }

    @Test
    public void testFindByDate() {
        // Create meetings on different dates
        LocalDate testDate = LocalDate.now().plusDays(5);
        
        beginTransaction();
        Meeting meeting1 = new Meeting(
                LocalDate.now().plusDays(1),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                testProject);
        
        Meeting meeting2 = new Meeting(
                testDate,
                LocalTime.of(11, 0),
                LocalTime.of(12, 0),
                testProject);
        
        Meeting meeting3 = new Meeting(
                testDate,
                LocalTime.of(13, 0),
                LocalTime.of(14, 0),
                secondProject);
        
        em.persist(meeting1);
        em.persist(meeting2);
        em.persist(meeting3);
        commitTransaction();

        // Find meetings on the test date
        List<Meeting> meetingsOnDate = meetingRepository.findByDate(testDate);

        // Verify results
        assertNotNull(meetingsOnDate);
        assertEquals(2, meetingsOnDate.size());
        assertTrue(meetingsOnDate.stream().allMatch(m -> m.getDate().equals(testDate)));
    }

    @Test
    public void testFindByDateAfter() {
        // Create meetings on different dates
        LocalDate cutoffDate = LocalDate.now().plusDays(5);
        
        beginTransaction();
        Meeting meeting1 = new Meeting(
                LocalDate.now().plusDays(1),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                testProject);
        
        Meeting meeting2 = new Meeting(
                LocalDate.now().plusDays(5),
                LocalTime.of(11, 0),
                LocalTime.of(12, 0),
                testProject);
        
        Meeting meeting3 = new Meeting(
                LocalDate.now().plusDays(10),
                LocalTime.of(13, 0),
                LocalTime.of(14, 0),
                secondProject);
        
        em.persist(meeting1);
        em.persist(meeting2);
        em.persist(meeting3);
        commitTransaction();

        // Find meetings after the cutoff date
        List<Meeting> meetingsAfterDate = meetingRepository.findByDateAfter(cutoffDate);

        // Verify results
        assertNotNull(meetingsAfterDate);
        assertEquals(1, meetingsAfterDate.size());
        assertEquals(LocalDate.now().plusDays(10), meetingsAfterDate.get(0).getDate());
    }

    @Test
    public void testFindByDateBetween() {
        // Create meetings on different dates
        LocalDate startDate = LocalDate.now().plusDays(3);
        LocalDate endDate = LocalDate.now().plusDays(7);
        
        beginTransaction();
        Meeting meeting1 = new Meeting(
                LocalDate.now().plusDays(1),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                testProject);
        
        Meeting meeting2 = new Meeting(
                LocalDate.now().plusDays(5),
                LocalTime.of(11, 0),
                LocalTime.of(12, 0),
                testProject);
        
        Meeting meeting3 = new Meeting(
                LocalDate.now().plusDays(10),
                LocalTime.of(13, 0),
                LocalTime.of(14, 0),
                secondProject);
        
        em.persist(meeting1);
        em.persist(meeting2);
        em.persist(meeting3);
        commitTransaction();

        // Find meetings between the dates
        List<Meeting> meetingsBetweenDates = meetingRepository.findByDateBetween(startDate, endDate);

        // Verify results
        assertNotNull(meetingsBetweenDates);
        assertEquals(1, meetingsBetweenDates.size());
        assertEquals(LocalDate.now().plusDays(5), meetingsBetweenDates.get(0).getDate());
    }

    @Test
    public void testMeetingWithAttendance() {
        // Create a meeting with attendance records
        beginTransaction();
        Meeting meeting = new Meeting(
                LocalDate.now().plusDays(5),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                testProject);
        em.persist(meeting);
        
        // Add attendance records
        Attendance attendance1 = new Attendance();
        attendance1.setMeeting(meeting);
        attendance1.setMember(teamMember1);
        attendance1.setPresent(true);
        
        Attendance attendance2 = new Attendance();
        attendance2.setMeeting(meeting);
        attendance2.setMember(teamMember2);
        attendance2.setPresent(false);
        
        meeting.addAttendance(attendance1);
        meeting.addAttendance(attendance2);
        
        em.persist(attendance1);
        em.persist(attendance2);
        
        Long meetingId = meeting.getId();
        commitTransaction();

        // Important: Open a new transaction to fetch and test the meeting
        beginTransaction();
        Meeting retrievedMeeting = em.find(Meeting.class, meetingId);
        
        // Now we can access the attendances collection within transaction
        assertEquals(2, retrievedMeeting.getAttendances().size());
        assertEquals(1, retrievedMeeting.getPresentCount());
        assertEquals(50.0, retrievedMeeting.getAttendancePercentage());
        commitTransaction();
    }

    @Test
    public void testRemoveAttendance() {
        // Create a meeting with attendance records
        beginTransaction();
        Meeting meeting = new Meeting(
                LocalDate.now().plusDays(7),
                LocalTime.of(14, 0),
                LocalTime.of(15, 0),
                testProject);
        em.persist(meeting);
        
        // Add attendance records
        Attendance attendance1 = new Attendance();
        attendance1.setMeeting(meeting);
        attendance1.setMember(teamMember1);
        attendance1.setPresent(true);
        
        Attendance attendance2 = new Attendance();
        attendance2.setMeeting(meeting);
        attendance2.setMember(teamMember2);
        attendance2.setPresent(true);
        
        meeting.addAttendance(attendance1);
        meeting.addAttendance(attendance2);
        
        em.persist(attendance1);
        em.persist(attendance2);
        
        Long meetingId = meeting.getId();
        commitTransaction();

        // Verify attendances within transaction
        beginTransaction();
        Meeting retrievedMeeting = em.find(Meeting.class, meetingId);
        assertEquals(2, retrievedMeeting.getAttendances().size());
        
        // Remove one attendance
        Attendance attendanceToRemove = retrievedMeeting.getAttendances().get(0);
        retrievedMeeting.removeAttendance(attendanceToRemove);
        em.remove(attendanceToRemove);
        em.merge(retrievedMeeting);
        commitTransaction();
        
        // Verify attendance was removed - in a new transaction
        beginTransaction();
        retrievedMeeting = em.find(Meeting.class, meetingId);
        assertEquals(1, retrievedMeeting.getAttendances().size());
        assertEquals(1, retrievedMeeting.getPresentCount());
        assertEquals(100.0, retrievedMeeting.getAttendancePercentage());
        commitTransaction();
    }

    @Test
    public void testMeetingHelperMethods() {
        // Test toString method
        Meeting meeting = new Meeting(
                LocalDate.now().plusDays(1),
                LocalTime.of(9, 0),
                LocalTime.of(10, 30),
                testProject);
        
        String meetingString = meeting.toString();
        assertTrue(meetingString.contains(LocalDate.now().plusDays(1).toString()));
        assertTrue(meetingString.contains("09:00"));
        assertTrue(meetingString.contains("10:30"));
    }

    @Test
    public void testMeetingWithZeroAttendance() {
        // Create a meeting with no attendance records
        Meeting meeting = new Meeting(
                LocalDate.now().plusDays(2),
                LocalTime.of(15, 0),
                LocalTime.of(16, 0),
                testProject);
        
        Meeting savedMeeting = meetingRepository.save(meeting);
        
        // Verify attendance calculation with no records
        assertEquals(0, savedMeeting.getPresentCount());
        assertEquals(0.0, savedMeeting.getAttendancePercentage());
    }

    @Test
    public void testFindByDateBetweenWithMultipleProjects() {
        // Create meetings for multiple projects within the same date range
        LocalDate startDate = LocalDate.now().plusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(10);
        
        beginTransaction();
        // Meetings for testProject
        Meeting meeting1 = new Meeting(
                LocalDate.now().plusDays(6),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                testProject);
        
        Meeting meeting2 = new Meeting(
                LocalDate.now().plusDays(8),
                LocalTime.of(11, 0),
                LocalTime.of(12, 0),
                testProject);
        
        // Meetings for secondProject
        Meeting meeting3 = new Meeting(
                LocalDate.now().plusDays(7),
                LocalTime.of(13, 0),
                LocalTime.of(14, 0),
                secondProject);
        
        em.persist(meeting1);
        em.persist(meeting2);
        em.persist(meeting3);
        commitTransaction();

        // Find all meetings in date range
        List<Meeting> meetingsBetweenDates = meetingRepository.findByDateBetween(startDate, endDate);
        
        // Verify results
        assertNotNull(meetingsBetweenDates);
        assertEquals(3, meetingsBetweenDates.size());
        
        // Test finding meetings for a specific project in date range
        // This requires a more complex query than what's in the interface
        // We'll do it manually with EntityManager
        beginTransaction();
        List<Meeting> projectMeetingsInRange = em.createQuery(
                "SELECT m FROM Meeting m WHERE m.project = :project AND " +
                "m.date >= :startDate AND m.date <= :endDate", Meeting.class)
            .setParameter("project", testProject)
            .setParameter("startDate", startDate)
            .setParameter("endDate", endDate)
            .getResultList();
        commitTransaction();
        
        assertNotNull(projectMeetingsInRange);
        assertEquals(2, projectMeetingsInRange.size());
        assertTrue(projectMeetingsInRange.stream().allMatch(m -> m.getProject().getId().equals(testProject.getId())));
    }
}