// src/test/java/org/frcpm/repositories/AttendanceRepositoryIntegrationTest.java

package org.frcpm.repositories;

import org.frcpm.models.Attendance;
import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.models.TeamMember;
import org.frcpm.repositories.spring.AttendanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for AttendanceRepository using Spring Boot @SpringBootTest.
 * Uses full Spring context instead of @DataJpaTest to avoid context loading issues.
 * 
 * @SpringBootTest loads the complete application context
 * @Transactional ensures each test runs in a transaction that's rolled back
 * @AutoConfigureMockMvc configures MockMvc (though not used in repository tests)
 */
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AttendanceRepositoryIntegrationTest {
    
    @Autowired
    private AttendanceRepository attendanceRepository;
    
    @Autowired
    private EntityManager entityManager;
    
    private Attendance testAttendance;
    private Meeting testMeeting;
    private TeamMember testMember;
    private Project testProject;
    
    @BeforeEach
    void setUp() {
        // Create test objects ONLY - no premature setup
        testProject = createTestProject();
        testMeeting = createTestMeeting();
        testMember = createTestMember();
        testAttendance = createTestAttendance();
    }
    
    /**
     * Creates a test project for use in tests.
     */
    private Project createTestProject() {
        Project project = new Project();
        project.setName("Test Project");
        project.setStartDate(LocalDate.now());
        project.setGoalEndDate(LocalDate.now().plusWeeks(6));
        project.setHardDeadline(LocalDate.now().plusWeeks(8));
        return project;
    }
    
    /**
     * Creates a test meeting for use in tests.
     */
    private Meeting createTestMeeting() {
        Meeting meeting = new Meeting();
        meeting.setDate(LocalDate.now().plusDays(1));
        meeting.setStartTime(LocalTime.of(10, 0));
        meeting.setEndTime(LocalTime.of(11, 0));
        meeting.setProject(testProject);
        return meeting;
    }
    
    /**
     * Creates a test team member for use in tests.
     */
    private TeamMember createTestMember() {
        TeamMember member = new TeamMember();
        member.setUsername("testuser");
        member.setFirstName("Test");
        member.setLastName("User");
        member.setEmail("test@example.com");
        return member;
    }
    
    /**
     * Creates a test attendance record for use in tests.
     */
    private Attendance createTestAttendance() {
        Attendance attendance = new Attendance(testMeeting, testMember, true);
        attendance.setArrivalTime(LocalTime.of(10, 0));
        attendance.setDepartureTime(LocalTime.of(11, 0));
        return attendance;
    }
    
    /**
     * Helper method to persist and flush an entity.
     * Replaces TestEntityManager's persistAndFlush functionality.
     */
    private <T> T persistAndFlush(T entity) {
        entityManager.persist(entity);
        entityManager.flush();
        return entity;
    }
    
    // ========== BASIC CRUD OPERATIONS ==========
    
    @Test
    void testSaveAndFindById() {
        // Setup - Persist dependencies first
        Project savedProject = persistAndFlush(testProject);
        testMeeting.setProject(savedProject);
        Meeting savedMeeting = persistAndFlush(testMeeting);
        TeamMember savedMember = persistAndFlush(testMember);
        
        testAttendance.setMeeting(savedMeeting);
        testAttendance.setMember(savedMember);
        
        // Execute - Save attendance
        Attendance savedAttendance = attendanceRepository.save(testAttendance);
        entityManager.flush();
        
        // Verify save
        assertThat(savedAttendance.getId()).isNotNull();
        
        // Execute - Find by ID
        Optional<Attendance> found = attendanceRepository.findById(savedAttendance.getId());
        
        // Verify find
        assertThat(found).isPresent();
        assertThat(found.get().getMeeting().getId()).isEqualTo(savedMeeting.getId());
        assertThat(found.get().getMember().getId()).isEqualTo(savedMember.getId());
        assertThat(found.get().isPresent()).isTrue();
        assertThat(found.get().getArrivalTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(found.get().getDepartureTime()).isEqualTo(LocalTime.of(11, 0));
    }
    
    @Test
    void testFindAll() {
        // Setup - Persist dependencies
        Project savedProject = persistAndFlush(testProject);
        testMeeting.setProject(savedProject);
        Meeting savedMeeting = persistAndFlush(testMeeting);
        TeamMember savedMember = persistAndFlush(testMember);
        
        testAttendance.setMeeting(savedMeeting);
        testAttendance.setMember(savedMember);
        
        // Execute - Save attendance
        attendanceRepository.save(testAttendance);
        entityManager.flush();
        
        // Execute - Find all
        List<Attendance> allAttendances = attendanceRepository.findAll();
        
        // Verify
        assertThat(allAttendances).hasSize(1);
        assertThat(allAttendances.get(0).getMeeting().getId()).isEqualTo(savedMeeting.getId());
        assertThat(allAttendances.get(0).getMember().getId()).isEqualTo(savedMember.getId());
    }
    
    @Test
    void testDeleteById() {
        // Setup - Persist dependencies and attendance
        Project savedProject = persistAndFlush(testProject);
        testMeeting.setProject(savedProject);
        Meeting savedMeeting = persistAndFlush(testMeeting);
        TeamMember savedMember = persistAndFlush(testMember);
        
        testAttendance.setMeeting(savedMeeting);
        testAttendance.setMember(savedMember);
        Attendance savedAttendance = persistAndFlush(testAttendance);
        
        // Verify exists before deletion
        assertThat(attendanceRepository.existsById(savedAttendance.getId())).isTrue();
        
        // Execute - Delete
        attendanceRepository.deleteById(savedAttendance.getId());
        entityManager.flush();
        
        // Verify deletion
        assertThat(attendanceRepository.existsById(savedAttendance.getId())).isFalse();
        assertThat(attendanceRepository.findById(savedAttendance.getId())).isEmpty();
    }
    
    @Test
    void testCount() {
        // Setup - Initial count should be 0
        assertThat(attendanceRepository.count()).isEqualTo(0);
        
        // Setup - Persist dependencies and attendance
        Project savedProject = persistAndFlush(testProject);
        testMeeting.setProject(savedProject);
        Meeting savedMeeting = persistAndFlush(testMeeting);
        TeamMember savedMember = persistAndFlush(testMember);
        
        testAttendance.setMeeting(savedMeeting);
        testAttendance.setMember(savedMember);
        attendanceRepository.save(testAttendance);
        entityManager.flush();
        
        // Execute and verify
        assertThat(attendanceRepository.count()).isEqualTo(1);
    }
    
    // ========== SPRING DATA JPA AUTO-IMPLEMENTED FINDER METHODS ==========
    
    @Test
    void testFindByMeeting() {
        // Setup - Persist dependencies and attendance
        Project savedProject = persistAndFlush(testProject);
        testMeeting.setProject(savedProject);
        Meeting savedMeeting = persistAndFlush(testMeeting);
        TeamMember savedMember = persistAndFlush(testMember);
        
        testAttendance.setMeeting(savedMeeting);
        testAttendance.setMember(savedMember);
        attendanceRepository.save(testAttendance);
        entityManager.flush();
        
        // Execute
        List<Attendance> results = attendanceRepository.findByMeeting(savedMeeting);
        
        // Verify
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getMeeting().getId()).isEqualTo(savedMeeting.getId());
        assertThat(results.get(0).getMember().getId()).isEqualTo(savedMember.getId());
        assertThat(results.get(0).isPresent()).isTrue();
    }
    
    @Test
    void testFindByMember() {
        // Setup - Persist dependencies and attendance
        Project savedProject = persistAndFlush(testProject);
        testMeeting.setProject(savedProject);
        Meeting savedMeeting = persistAndFlush(testMeeting);
        TeamMember savedMember = persistAndFlush(testMember);
        
        testAttendance.setMeeting(savedMeeting);
        testAttendance.setMember(savedMember);
        attendanceRepository.save(testAttendance);
        entityManager.flush();
        
        // Execute
        List<Attendance> results = attendanceRepository.findByMember(savedMember);
        
        // Verify
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getMeeting().getId()).isEqualTo(savedMeeting.getId());
        assertThat(results.get(0).getMember().getId()).isEqualTo(savedMember.getId());
    }
    
    @Test
    void testFindByMeetingAndMember() {
        // Setup - Persist dependencies and attendance
        Project savedProject = persistAndFlush(testProject);
        testMeeting.setProject(savedProject);
        Meeting savedMeeting = persistAndFlush(testMeeting);
        TeamMember savedMember = persistAndFlush(testMember);
        
        testAttendance.setMeeting(savedMeeting);
        testAttendance.setMember(savedMember);
        attendanceRepository.save(testAttendance);
        entityManager.flush();
        
        // Execute
        Optional<Attendance> result = attendanceRepository.findByMeetingAndMember(savedMeeting, savedMember);
        
        // Verify
        assertThat(result).isPresent();
        assertThat(result.get().getMeeting().getId()).isEqualTo(savedMeeting.getId());
        assertThat(result.get().getMember().getId()).isEqualTo(savedMember.getId());
        assertThat(result.get().isPresent()).isTrue();
    }
    
    @Test
    void testFindByMeetingAndMember_NotFound() {
        // Setup - Create different meeting and member (not persisted)
        TeamMember differentMember = new TeamMember();
        differentMember.setUsername("different");
        differentMember.setFirstName("Different");
        differentMember.setLastName("User");
        differentMember.setEmail("different@example.com");
        TeamMember savedDifferentMember = persistAndFlush(differentMember);
        
        Project savedProject = persistAndFlush(testProject);
        testMeeting.setProject(savedProject);
        Meeting savedMeeting = persistAndFlush(testMeeting);
        
        // Execute - Search for non-existent combination
        Optional<Attendance> result = attendanceRepository.findByMeetingAndMember(savedMeeting, savedDifferentMember);
        
        // Verify
        assertThat(result).isEmpty();
    }
    
    @Test
    void testFindByPresent() {
        // Setup - Create present and absent attendances
        Project savedProject = persistAndFlush(testProject);
        testMeeting.setProject(savedProject);
        Meeting savedMeeting = persistAndFlush(testMeeting);
        TeamMember savedMember = persistAndFlush(testMember);
        
        // Present attendance
        testAttendance.setMeeting(savedMeeting);
        testAttendance.setMember(savedMember);
        testAttendance.setPresent(true);
        attendanceRepository.save(testAttendance);
        
        // Absent attendance
        TeamMember absentMember = new TeamMember();
        absentMember.setUsername("absent");
        absentMember.setFirstName("Absent");
        absentMember.setLastName("User");
        absentMember.setEmail("absent@example.com");
        TeamMember savedAbsentMember = persistAndFlush(absentMember);
        
        Attendance absentAttendance = new Attendance(savedMeeting, savedAbsentMember, false);
        attendanceRepository.save(absentAttendance);
        entityManager.flush();
        
        // Execute - Find present
        List<Attendance> presentResults = attendanceRepository.findByPresent(true);
        
        // Verify present
        assertThat(presentResults).hasSize(1);
        assertThat(presentResults.get(0).isPresent()).isTrue();
        assertThat(presentResults.get(0).getMember().getUsername()).isEqualTo("testuser");
        
        // Execute - Find absent
        List<Attendance> absentResults = attendanceRepository.findByPresent(false);
        
        // Verify absent
        assertThat(absentResults).hasSize(1);
        assertThat(absentResults.get(0).isPresent()).isFalse();
        assertThat(absentResults.get(0).getMember().getUsername()).isEqualTo("absent");
    }
    
    // ========== CUSTOM @QUERY METHODS ==========
    
    @Test
    void testFindByMeetingId() {
        // Setup - Persist dependencies and attendance
        Project savedProject = persistAndFlush(testProject);
        testMeeting.setProject(savedProject);
        Meeting savedMeeting = persistAndFlush(testMeeting);
        TeamMember savedMember = persistAndFlush(testMember);
        
        testAttendance.setMeeting(savedMeeting);
        testAttendance.setMember(savedMember);
        attendanceRepository.save(testAttendance);
        entityManager.flush();
        
        // Execute
        List<Attendance> results = attendanceRepository.findByMeetingId(savedMeeting.getId());
        
        // Verify
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getMeeting().getId()).isEqualTo(savedMeeting.getId());
    }
    
    @Test
    void testFindByMemberId() {
        // Setup - Persist dependencies and attendance
        Project savedProject = persistAndFlush(testProject);
        testMeeting.setProject(savedProject);
        Meeting savedMeeting = persistAndFlush(testMeeting);
        TeamMember savedMember = persistAndFlush(testMember);
        
        testAttendance.setMeeting(savedMeeting);
        testAttendance.setMember(savedMember);
        attendanceRepository.save(testAttendance);
        entityManager.flush();
        
        // Execute
        List<Attendance> results = attendanceRepository.findByMemberId(savedMember.getId());
        
        // Verify
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getMember().getId()).isEqualTo(savedMember.getId());
    }
    
    @Test
    void testFindByMeetingIdAndMemberId() {
        // Setup - Persist dependencies and attendance
        Project savedProject = persistAndFlush(testProject);
        testMeeting.setProject(savedProject);
        Meeting savedMeeting = persistAndFlush(testMeeting);
        TeamMember savedMember = persistAndFlush(testMember);
        
        testAttendance.setMeeting(savedMeeting);
        testAttendance.setMember(savedMember);
        attendanceRepository.save(testAttendance);
        entityManager.flush();
        
        // Execute
        Optional<Attendance> result = attendanceRepository.findByMeetingIdAndMemberId(
            savedMeeting.getId(), savedMember.getId());
        
        // Verify
        assertThat(result).isPresent();
        assertThat(result.get().getMeeting().getId()).isEqualTo(savedMeeting.getId());
        assertThat(result.get().getMember().getId()).isEqualTo(savedMember.getId());
    }
    
    @Test
    void testFindByMeetingDateBetween() {
        // Setup - Create meetings on different dates
        Project savedProject = persistAndFlush(testProject);
        
        // Meeting 1 - Yesterday
        Meeting yesterdayMeeting = new Meeting();
        yesterdayMeeting.setDate(LocalDate.now().minusDays(1));
        yesterdayMeeting.setStartTime(LocalTime.of(9, 0));
        yesterdayMeeting.setEndTime(LocalTime.of(10, 0));
        yesterdayMeeting.setProject(savedProject);
        Meeting savedYesterdayMeeting = persistAndFlush(yesterdayMeeting);
        
        // Meeting 2 - Today
        testMeeting.setDate(LocalDate.now());
        testMeeting.setProject(savedProject);
        Meeting savedTodayMeeting = persistAndFlush(testMeeting);
        
        // Meeting 3 - Tomorrow (outside range)
        Meeting tomorrowMeeting = new Meeting();
        tomorrowMeeting.setDate(LocalDate.now().plusDays(1));
        tomorrowMeeting.setStartTime(LocalTime.of(11, 0));
        tomorrowMeeting.setEndTime(LocalTime.of(12, 0));
        tomorrowMeeting.setProject(savedProject);
        Meeting savedTomorrowMeeting = persistAndFlush(tomorrowMeeting);
        
        TeamMember savedMember = persistAndFlush(testMember);
        
        // Create attendances
        Attendance yesterdayAttendance = new Attendance(savedYesterdayMeeting, savedMember, true);
        Attendance todayAttendance = new Attendance(savedTodayMeeting, savedMember, true);
        Attendance tomorrowAttendance = new Attendance(savedTomorrowMeeting, savedMember, false);
        
        attendanceRepository.save(yesterdayAttendance);
        attendanceRepository.save(todayAttendance);
        attendanceRepository.save(tomorrowAttendance);
        entityManager.flush();
        
        // Execute - Find attendances for yesterday and today (exclude tomorrow)
        List<Attendance> results = attendanceRepository.findByMeetingDateBetween(
            LocalDate.now().minusDays(1), LocalDate.now());
        
        // Verify - Should find yesterday and today, but not tomorrow
        assertThat(results).hasSize(2);
        assertThat(results.stream().map(a -> a.getMeeting().getDate()))
            .containsExactlyInAnyOrder(LocalDate.now().minusDays(1), LocalDate.now());
    }
    
    @Test
    void testFindByProjectId() {
        // Setup - Create two projects with different attendances
        Project savedProject1 = persistAndFlush(testProject);
        
        Project project2 = new Project();
        project2.setName("Different Project");
        project2.setStartDate(LocalDate.now());
        project2.setGoalEndDate(LocalDate.now().plusWeeks(4));
        project2.setHardDeadline(LocalDate.now().plusWeeks(6));
        Project savedProject2 = persistAndFlush(project2);
        
        // Meeting for project 1
        testMeeting.setProject(savedProject1);
        Meeting savedMeeting1 = persistAndFlush(testMeeting);
        
        // Meeting for project 2
        Meeting meeting2 = new Meeting();
        meeting2.setDate(LocalDate.now().plusDays(2));
        meeting2.setStartTime(LocalTime.of(14, 0));
        meeting2.setEndTime(LocalTime.of(15, 0));
        meeting2.setProject(savedProject2);
        Meeting savedMeeting2 = persistAndFlush(meeting2);
        
        TeamMember savedMember = persistAndFlush(testMember);
        
        // Create attendances for both projects
        Attendance attendance1 = new Attendance(savedMeeting1, savedMember, true);
        Attendance attendance2 = new Attendance(savedMeeting2, savedMember, false);
        
        attendanceRepository.save(attendance1);
        attendanceRepository.save(attendance2);
        entityManager.flush();
        
        // Execute - Find attendances for project 1
        List<Attendance> results = attendanceRepository.findByProjectId(savedProject1.getId());
        
        // Verify - Should only find attendance for project 1
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getMeeting().getProject().getId()).isEqualTo(savedProject1.getId());
        assertThat(results.get(0).isPresent()).isTrue();
    }
    
    @Test
    void testCountByMemberId() {
        // Setup - Persist dependencies and create multiple attendances for same member
        Project savedProject = persistAndFlush(testProject);
        TeamMember savedMember = persistAndFlush(testMember);
        
        // Create multiple meetings
        Meeting meeting1 = new Meeting();
        meeting1.setDate(LocalDate.now());
        meeting1.setStartTime(LocalTime.of(9, 0));
        meeting1.setEndTime(LocalTime.of(10, 0));
        meeting1.setProject(savedProject);
        Meeting savedMeeting1 = persistAndFlush(meeting1);
        
        Meeting meeting2 = new Meeting();
        meeting2.setDate(LocalDate.now().plusDays(1));
        meeting2.setStartTime(LocalTime.of(11, 0));
        meeting2.setEndTime(LocalTime.of(12, 0));
        meeting2.setProject(savedProject);
        Meeting savedMeeting2 = persistAndFlush(meeting2);
        
        // Create attendances
        Attendance attendance1 = new Attendance(savedMeeting1, savedMember, true);
        Attendance attendance2 = new Attendance(savedMeeting2, savedMember, false);
        
        attendanceRepository.save(attendance1);
        attendanceRepository.save(attendance2);
        entityManager.flush();
        
        // Execute
        long count = attendanceRepository.countByMemberId(savedMember.getId());
        
        // Verify
        assertThat(count).isEqualTo(2);
    }
    
    @Test
    void testCountByMemberIdAndPresent() {
        // Setup - Persist dependencies and create attendances with different presence status
        Project savedProject = persistAndFlush(testProject);
        TeamMember savedMember = persistAndFlush(testMember);
        
        // Create multiple meetings
        Meeting meeting1 = new Meeting();
        meeting1.setDate(LocalDate.now());
        meeting1.setStartTime(LocalTime.of(9, 0));
        meeting1.setEndTime(LocalTime.of(10, 0));
        meeting1.setProject(savedProject);
        Meeting savedMeeting1 = persistAndFlush(meeting1);
        
        Meeting meeting2 = new Meeting();
        meeting2.setDate(LocalDate.now().plusDays(1));
        meeting2.setStartTime(LocalTime.of(11, 0));
        meeting2.setEndTime(LocalTime.of(12, 0));
        meeting2.setProject(savedProject);
        Meeting savedMeeting2 = persistAndFlush(meeting2);
        
        Meeting meeting3 = new Meeting();
        meeting3.setDate(LocalDate.now().plusDays(2));
        meeting3.setStartTime(LocalTime.of(13, 0));
        meeting3.setEndTime(LocalTime.of(14, 0));
        meeting3.setProject(savedProject);
        Meeting savedMeeting3 = persistAndFlush(meeting3);
        
        // Create attendances: 2 present, 1 absent
        Attendance attendance1 = new Attendance(savedMeeting1, savedMember, true);
        Attendance attendance2 = new Attendance(savedMeeting2, savedMember, true);
        Attendance attendance3 = new Attendance(savedMeeting3, savedMember, false);
        
        attendanceRepository.save(attendance1);
        attendanceRepository.save(attendance2);
        attendanceRepository.save(attendance3);
        entityManager.flush();
        
        // Execute - Count present
        long presentCount = attendanceRepository.countByMemberIdAndPresent(savedMember.getId(), true);
        
        // Execute - Count absent
        long absentCount = attendanceRepository.countByMemberIdAndPresent(savedMember.getId(), false);
        
        // Verify
        assertThat(presentCount).isEqualTo(2);
        assertThat(absentCount).isEqualTo(1);
    }
    
    // ========== ENTITY RELATIONSHIP VALIDATION ==========
    
    @Test
    void testUniqueConstraint() {
        // Setup - Persist dependencies
        Project savedProject = persistAndFlush(testProject);
        testMeeting.setProject(savedProject);
        Meeting savedMeeting = persistAndFlush(testMeeting);
        TeamMember savedMember = persistAndFlush(testMember);
        
        testAttendance.setMeeting(savedMeeting);
        testAttendance.setMember(savedMember);
        
        // Execute - Save first attendance
        attendanceRepository.save(testAttendance);
        entityManager.flush(); // This should succeed
        
        // Clear the persistence context to ensure fresh entity
        entityManager.clear();
        
        // Execute - Try to save duplicate attendance (same meeting + member)
        // THIS IS WHERE THE EXCEPTION SHOULD BE CAUGHT
        org.junit.jupiter.api.Assertions.assertThrows(
            org.springframework.dao.DataIntegrityViolationException.class,
            () -> {
                Attendance duplicateAttendance = new Attendance();
                duplicateAttendance.setMeeting(savedMeeting);
                duplicateAttendance.setMember(savedMember);
                duplicateAttendance.setPresent(false); // Different presence status but same meeting+member
                
                // The save operation itself may trigger the constraint violation
                attendanceRepository.save(duplicateAttendance);
                entityManager.flush(); // Ensure the save is flushed to database
            }
        );
    }
    
    @Test
    void testCascadingRelationships() {
        // Setup - Create meeting with attendance
        Project savedProject = persistAndFlush(testProject);
        testMeeting.setProject(savedProject);
        TeamMember savedMember = persistAndFlush(testMember);
        
        // Add attendance to meeting using helper method
        testMeeting.addAttendance(testAttendance);
        testAttendance.setMember(savedMember);
        
        // Execute - Save meeting (should cascade to attendance)
        Meeting savedMeeting = persistAndFlush(testMeeting);
        
        // Verify - Attendance should be persisted through cascade
        assertThat(savedMeeting.getAttendances()).hasSize(1);
        assertThat(savedMeeting.getAttendances().get(0).getId()).isNotNull();
        assertThat(savedMeeting.getAttendances().get(0).getMeeting()).isEqualTo(savedMeeting);
        
        // Verify attendance is in database
        List<Attendance> allAttendances = attendanceRepository.findAll();
        assertThat(allAttendances).hasSize(1);
        assertThat(allAttendances.get(0).getMeeting().getId()).isEqualTo(savedMeeting.getId());
    }
}