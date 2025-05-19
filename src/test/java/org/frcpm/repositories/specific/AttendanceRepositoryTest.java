// src/test/java/org/frcpm/repositories/specific/AttendanceRepositoryTest.java
package org.frcpm.repositories.specific;

import jakarta.persistence.PersistenceException;
import org.frcpm.models.Attendance;
import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.models.TeamMember;
import org.frcpm.repositories.BaseRepositoryTest;
import org.frcpm.repositories.impl.AttendanceRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for AttendanceRepository.
 * Tests CRUD operations and specialized repository methods for attendance records.
 */
public class AttendanceRepositoryTest extends BaseRepositoryTest {

    private AttendanceRepository attendanceRepository;
    private Project testProject;
    private Meeting testMeeting1;
    private Meeting testMeeting2;
    private TeamMember testMember1;
    private TeamMember testMember2;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        attendanceRepository = new AttendanceRepositoryImpl();
    }

    @Override
    protected void setupTestData() {
        LOGGER.info("Setting up test data for AttendanceRepositoryTest");
        
        // Create test project
        beginTransaction();
        
        testProject = new Project();
        testProject.setName("Test Project");
        testProject.setStartDate(LocalDate.now());
        testProject.setGoalEndDate(LocalDate.now().plusMonths(1));
        testProject.setHardDeadline(LocalDate.now().plusMonths(2));
        testProject.setDescription("Test project description");
        em.persist(testProject);
        
        // Create test meetings
        testMeeting1 = new Meeting();
        testMeeting1.setProject(testProject);
        testMeeting1.setDate(LocalDate.now());
        testMeeting1.setStartTime(LocalTime.of(9, 0));
        testMeeting1.setEndTime(LocalTime.of(10, 0));
        // Meeting doesn't have setTitle and setLocation methods
        em.persist(testMeeting1);
        
        testMeeting2 = new Meeting();
        testMeeting2.setProject(testProject);
        testMeeting2.setDate(LocalDate.now().plusDays(1));
        testMeeting2.setStartTime(LocalTime.of(14, 0));
        testMeeting2.setEndTime(LocalTime.of(15, 0));
        // Meeting doesn't have setTitle and setLocation methods
        em.persist(testMeeting2);
        
        // Create test team members
        testMember1 = new TeamMember();
        testMember1.setFirstName("John");
        testMember1.setLastName("Doe");
        testMember1.setEmail("john.doe@example.com");
        testMember1.setUsername("johndoe");
        em.persist(testMember1);
        
        testMember2 = new TeamMember();
        testMember2.setFirstName("Jane");
        testMember2.setLastName("Smith");
        testMember2.setEmail("jane.smith@example.com");
        testMember2.setUsername("janesmith");
        em.persist(testMember2);
        
        commitTransaction();
    }
    
    @Test
    public void testSave() {
        // Create new attendance record
        Attendance attendance = new Attendance(testMeeting1, testMember1, true);
        attendance.setArrivalTime(LocalTime.of(9, 0));
        attendance.setDepartureTime(LocalTime.of(10, 0));
        
        // Save attendance
        Attendance savedAttendance = attendanceRepository.save(attendance);
        
        // Verify
        assertNotNull(savedAttendance.getId(), "ID should be assigned after save");
        assertNotNull(savedAttendance.getMeeting(), "Meeting should not be null");
        assertNotNull(savedAttendance.getMember(), "Member should not be null");
        assertTrue(savedAttendance.isPresent(), "Attendance should be marked as present");
        assertEquals(LocalTime.of(9, 0), savedAttendance.getArrivalTime(), "Arrival time should match");
        assertEquals(LocalTime.of(10, 0), savedAttendance.getDepartureTime(), "Departure time should match");
        
        // Verify it can be retrieved
        Optional<Attendance> retrievedAttendance = attendanceRepository.findById(savedAttendance.getId());
        assertTrue(retrievedAttendance.isPresent(), "Should be able to retrieve saved attendance");
        assertEquals(savedAttendance.getId(), retrievedAttendance.get().getId(), "IDs should match");
    }
    
    @Test
    public void testFindById() {
        // Create and save attendance
        beginTransaction();
        Attendance attendance = new Attendance(testMeeting1, testMember1, true);
        em.persist(attendance);
        commitTransaction();
        
        // Find by ID
        Optional<Attendance> found = attendanceRepository.findById(attendance.getId());
        
        // Verify
        assertTrue(found.isPresent(), "Should find attendance by ID");
        assertEquals(attendance.getId(), found.get().getId(), "IDs should match");
        
        // Fix: Don't directly compare Meeting objects to avoid LazyInitializationException
        // Instead, check IDs or specific properties
        assertNotNull(found.get().getMeeting(), "Meeting should not be null");
        assertNotNull(found.get().getMember(), "Member should not be null");
        
        // Additional verification inside a transaction to avoid LazyInitializationException
        beginTransaction();
        Attendance foundAttendance = em.find(Attendance.class, attendance.getId());
        assertEquals(testMeeting1.getId(), foundAttendance.getMeeting().getId(), "Meeting IDs should match");
        assertEquals(testMember1.getId(), foundAttendance.getMember().getId(), "Member IDs should match");
        commitTransaction();
    }
    
    @Test
    public void testFindAll() {
        // Create and save multiple attendance records
        beginTransaction();
        Attendance attendance1 = new Attendance(testMeeting1, testMember1, true);
        Attendance attendance2 = new Attendance(testMeeting1, testMember2, true);
        Attendance attendance3 = new Attendance(testMeeting2, testMember1, false);
        em.persist(attendance1);
        em.persist(attendance2);
        em.persist(attendance3);
        commitTransaction();
        
        // Find all
        List<Attendance> allAttendances = attendanceRepository.findAll();
        
        // Verify
        assertNotNull(allAttendances, "Result should not be null");
        assertEquals(3, allAttendances.size(), "Should find all attendance records");
    }
    
    @Test
    public void testUpdate() {
        // Create and save attendance
        beginTransaction();
        Attendance attendance = new Attendance(testMeeting1, testMember1, true);
        attendance.setArrivalTime(LocalTime.of(9, 0));
        attendance.setDepartureTime(LocalTime.of(10, 0));
        em.persist(attendance);
        commitTransaction();
        
        // Modify attendance
        attendance.setPresent(false);
        Attendance updatedAttendance = attendanceRepository.save(attendance);
        
        // Verify
        assertFalse(updatedAttendance.isPresent(), "Attendance should be marked as not present");
        assertNull(updatedAttendance.getArrivalTime(), "Arrival time should be null when not present");
        assertNull(updatedAttendance.getDepartureTime(), "Departure time should be null when not present");
        
        // Verify changes persisted
        Optional<Attendance> retrieved = attendanceRepository.findById(attendance.getId());
        assertTrue(retrieved.isPresent(), "Should be able to retrieve updated attendance");
        assertFalse(retrieved.get().isPresent(), "Retrieved attendance should be marked as not present");
    }
    
    @Test
    public void testDelete() {
        // Create and save attendance
        beginTransaction();
        Attendance attendance = new Attendance(testMeeting1, testMember1, true);
        em.persist(attendance);
        commitTransaction();
        
        // Verify it exists
        assertTrue(attendanceRepository.findById(attendance.getId()).isPresent(), 
                  "Attendance should exist before deletion");
        
        // Delete attendance
        attendanceRepository.delete(attendance);
        
        // Verify it no longer exists
        assertFalse(attendanceRepository.findById(attendance.getId()).isPresent(), 
                   "Attendance should not exist after deletion");
    }
    
    @Test
    public void testDeleteById() {
        // Create and save attendance
        beginTransaction();
        Attendance attendance = new Attendance(testMeeting1, testMember1, true);
        em.persist(attendance);
        commitTransaction();
        
        // Delete by ID
        boolean deleted = attendanceRepository.deleteById(attendance.getId());
        
        // Verify
        assertTrue(deleted, "Delete operation should return true for existing ID");
        assertFalse(attendanceRepository.findById(attendance.getId()).isPresent(), 
                   "Attendance should not exist after deletion");
        
        // Try to delete non-existent ID
        boolean deletedNonExistent = attendanceRepository.deleteById(-1L);
        assertFalse(deletedNonExistent, "Delete operation should return false for non-existent ID");
    }
    
    @Test
    public void testCount() {
        // Initially should be empty
        assertEquals(0, attendanceRepository.count(), "Initial count should be 0");
        
        // Add attendance records
        beginTransaction();
        Attendance attendance1 = new Attendance(testMeeting1, testMember1, true);
        Attendance attendance2 = new Attendance(testMeeting1, testMember2, false);
        em.persist(attendance1);
        em.persist(attendance2);
        commitTransaction();
        
        // Verify count
        assertEquals(2, attendanceRepository.count(), "Count should be 2 after adding records");
        
        // Delete one record
        attendanceRepository.delete(attendance1);
        
        // Verify count decreased
        assertEquals(1, attendanceRepository.count(), "Count should be 1 after deleting a record");
    }
    
    @Test
    public void testFindByMeeting() {
        // Create and save attendance records for different meetings
        beginTransaction();
        Attendance attendance1 = new Attendance(testMeeting1, testMember1, true);
        Attendance attendance2 = new Attendance(testMeeting1, testMember2, false);
        Attendance attendance3 = new Attendance(testMeeting2, testMember1, true);
        em.persist(attendance1);
        em.persist(attendance2);
        em.persist(attendance3);
        commitTransaction();
        
        // Find by meeting
        List<Attendance> meeting1Attendances = attendanceRepository.findByMeeting(testMeeting1);
        
        // Verify
        assertNotNull(meeting1Attendances, "Result should not be null");
        assertEquals(2, meeting1Attendances.size(), "Should find 2 attendances for Meeting 1");
        
        // Find by another meeting
        List<Attendance> meeting2Attendances = attendanceRepository.findByMeeting(testMeeting2);
        
        // Verify
        assertNotNull(meeting2Attendances, "Result should not be null");
        assertEquals(1, meeting2Attendances.size(), "Should find 1 attendance for Meeting 2");
    }
    
    @Test
    public void testFindByMember() {
        // Create and save attendance records for different members
        beginTransaction();
        Attendance attendance1 = new Attendance(testMeeting1, testMember1, true);
        Attendance attendance2 = new Attendance(testMeeting2, testMember1, false);
        Attendance attendance3 = new Attendance(testMeeting1, testMember2, true);
        em.persist(attendance1);
        em.persist(attendance2);
        em.persist(attendance3);
        commitTransaction();
        
        // Find by member
        List<Attendance> member1Attendances = attendanceRepository.findByMember(testMember1);
        
        // Verify
        assertNotNull(member1Attendances, "Result should not be null");
        assertEquals(2, member1Attendances.size(), "Should find 2 attendances for Member 1");
        
        // Find by another member
        List<Attendance> member2Attendances = attendanceRepository.findByMember(testMember2);
        
        // Verify
        assertNotNull(member2Attendances, "Result should not be null");
        assertEquals(1, member2Attendances.size(), "Should find 1 attendance for Member 2");
    }
    
    @Test
    public void testFindByMeetingAndMember() {
        // Create and save attendance records
        beginTransaction();
        Attendance attendance1 = new Attendance(testMeeting1, testMember1, true);
        Attendance attendance2 = new Attendance(testMeeting1, testMember2, false);
        Attendance attendance3 = new Attendance(testMeeting2, testMember1, true);
        em.persist(attendance1);
        em.persist(attendance2);
        em.persist(attendance3);
        commitTransaction();
        
        // Find by meeting and member
        Optional<Attendance> found = attendanceRepository.findByMeetingAndMember(testMeeting1, testMember1);
        
        // Fix: Prevent LazyInitializationException by not directly comparing Meeting objects
        // Instead, check IDs or specific properties
        assertTrue(found.isPresent(), "Should find attendance for specific meeting and member");
        
        // Additional verification inside a transaction to avoid LazyInitializationException
        beginTransaction();
        Attendance foundAttendance = em.find(Attendance.class, found.get().getId());
        assertEquals(testMeeting1.getId(), foundAttendance.getMeeting().getId(), "Meeting IDs should match");
        assertEquals(testMember1.getId(), foundAttendance.getMember().getId(), "Member IDs should match");
        assertTrue(foundAttendance.isPresent(), "Attendance should be marked as present");
        commitTransaction();
        
        // Find non-existent combination
        Optional<Attendance> notFound = attendanceRepository.findByMeetingAndMember(testMeeting2, testMember2);
        
        // Verify
        assertFalse(notFound.isPresent(), "Should not find attendance for non-existent combination");
    }
    
    @Test
    public void testFindByPresent() {
        // Create and save attendance records with different presence status
        beginTransaction();
        Attendance attendance1 = new Attendance(testMeeting1, testMember1, true);
        Attendance attendance2 = new Attendance(testMeeting1, testMember2, false);
        Attendance attendance3 = new Attendance(testMeeting2, testMember1, true);
        em.persist(attendance1);
        em.persist(attendance2);
        em.persist(attendance3);
        commitTransaction();
        
        // Find present attendances
        List<Attendance> presentAttendances = attendanceRepository.findByPresent(true);
        
        // Verify
        assertNotNull(presentAttendances, "Result should not be null");
        assertEquals(2, presentAttendances.size(), "Should find 2 present attendances");
        
        // Find absent attendances
        List<Attendance> absentAttendances = attendanceRepository.findByPresent(false);
        
        // Verify
        assertNotNull(absentAttendances, "Result should not be null");
        assertEquals(1, absentAttendances.size(), "Should find 1 absent attendance");
    }
    
    @Test
    public void testUniqueConstraint() {
        // Fix: We need to directly test with JPA to capture the PersistenceException
        beginTransaction();
        Attendance attendance1 = new Attendance(testMeeting1, testMember1, true);
        em.persist(attendance1);
        commitTransaction();
        
        // Try to create a duplicate record - directly using EntityManager to catch the actual exception
        beginTransaction();
        Attendance attendance2 = new Attendance(testMeeting1, testMember1, false);
        
        // Verify that persisting the duplicate throws an exception
        assertThrows(PersistenceException.class, () -> {
            em.persist(attendance2);
            em.flush(); // Force the exception to be thrown immediately
        }, "Should throw PersistenceException when saving duplicate attendance");
        
        // Rollback failed transaction
        rollbackTransaction();
    }
    
    @Test
    public void testAttendanceDurationCalculation() {
        // Create attendance with duration
        Attendance attendance = new Attendance(testMeeting1, testMember1, true);
        attendance.setArrivalTime(LocalTime.of(9, 0));
        attendance.setDepartureTime(LocalTime.of(10, 30));
        
        // Save attendance
        Attendance savedAttendance = attendanceRepository.save(attendance);
        
        // Verify duration calculation
        assertEquals(90, savedAttendance.getDurationMinutes(), "Duration should be 90 minutes");
        
        // Test with absent attendance
        Attendance absentAttendance = new Attendance(testMeeting1, testMember2, false);
        Attendance savedAbsentAttendance = attendanceRepository.save(absentAttendance);
        
        // Verify duration is 0 for absent
        assertEquals(0, savedAbsentAttendance.getDurationMinutes(), "Duration should be 0 minutes for absent attendance");
        
        // Test with missing departure time
        Attendance incompleteAttendance = new Attendance(testMeeting2, testMember1, true);
        incompleteAttendance.setArrivalTime(LocalTime.of(14, 0));
        Attendance savedIncompleteAttendance = attendanceRepository.save(incompleteAttendance);
        
        // Verify duration is 0 for incomplete attendance
        assertEquals(0, savedIncompleteAttendance.getDurationMinutes(), "Duration should be 0 minutes for incomplete attendance");
    }
    
    @Test
    public void testToString() {
        // Create attendance
        Attendance presentAttendance = new Attendance(testMeeting1, testMember1, true);
        Attendance absentAttendance = new Attendance(testMeeting1, testMember2, false);
        
        // Save attendances
        presentAttendance = attendanceRepository.save(presentAttendance);
        absentAttendance = attendanceRepository.save(absentAttendance);
        
        // Need to verify toString within a transaction to avoid LazyInitializationException
        beginTransaction();
        
        // Reload the entities to ensure they're attached to the session
        Attendance reloadedPresent = em.find(Attendance.class, presentAttendance.getId());
        Attendance reloadedAbsent = em.find(Attendance.class, absentAttendance.getId());
        
        // Verify toString includes presence status
        String presentString = reloadedPresent.toString();
        String absentString = reloadedAbsent.toString();
        
        assertTrue(presentString.contains("Present"), "Present attendance toString should contain 'Present'");
        assertTrue(absentString.contains("Absent"), "Absent attendance toString should contain 'Absent'");
        
        commitTransaction();
    }
}