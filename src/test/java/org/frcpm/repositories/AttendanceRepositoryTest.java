package org.frcpm.repositories;

import org.frcpm.config.DatabaseConfig;
import org.frcpm.models.Attendance;
import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.models.TeamMember;
import org.frcpm.repositories.specific.AttendanceRepository;
import org.frcpm.repositories.specific.MeetingRepository;
import org.frcpm.repositories.specific.ProjectRepository;
import org.frcpm.repositories.specific.TeamMemberRepository;
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
public class AttendanceRepositoryTest {
    
    private AttendanceRepository repository;
    private ProjectRepository projectRepository;
    private MeetingRepository meetingRepository;
    private TeamMemberRepository teamMemberRepository;
    
    private Project testProject;
    private Meeting testMeeting;
    private TeamMember testMember1;
    private TeamMember testMember2;
    
    @BeforeEach
    public void setUp() {
        DatabaseConfig.initialize();
        repository = RepositoryFactory.getAttendanceRepository();
        projectRepository = RepositoryFactory.getProjectRepository();
        meetingRepository = RepositoryFactory.getMeetingRepository();
        teamMemberRepository = RepositoryFactory.getTeamMemberRepository();
        
        // Create test project
        testProject = new Project(
            "Test Attendance Project", 
            LocalDate.now(), 
            LocalDate.now().plusWeeks(6), 
            LocalDate.now().plusWeeks(8)
        );
        testProject = projectRepository.save(testProject);
        
        // Create test meeting
        testMeeting = new Meeting(
            LocalDate.now().plusDays(1),
            LocalTime.of(18, 0), // 6:00 PM
            LocalTime.of(20, 0), // 8:00 PM
            testProject
        );
        testMeeting = meetingRepository.save(testMeeting);
        
        // Create test team members
        testMember1 = new TeamMember("attendancetest1", "Attendance", "Test1", "attendance1@example.com");
        testMember1 = teamMemberRepository.save(testMember1);
        
        testMember2 = new TeamMember("attendancetest2", "Attendance", "Test2", "attendance2@example.com");
        testMember2 = teamMemberRepository.save(testMember2);
        
        // Add test data
        createTestAttendanceRecords();
    }
    
    @AfterEach
    public void tearDown() {
        // Clean up test data
        cleanupTestAttendanceRecords();
        meetingRepository.delete(testMeeting);
        teamMemberRepository.delete(testMember1);
        teamMemberRepository.delete(testMember2);
        projectRepository.delete(testProject);
        DatabaseConfig.shutdown();
    }
    
    private void createTestAttendanceRecords() {
        Attendance attendance1 = new Attendance(testMeeting, testMember1, true);
        attendance1.setArrivalTime(LocalTime.of(18, 0));
        attendance1.setDepartureTime(LocalTime.of(20, 0));
        
        Attendance attendance2 = new Attendance(testMeeting, testMember2, false);
        
        repository.save(attendance1);
        repository.save(attendance2);
    }
    
    private void cleanupTestAttendanceRecords() {
        // Find and delete all attendance records for test meeting
        List<Attendance> attendances = repository.findByMeeting(testMeeting);
        for (Attendance attendance : attendances) {
            repository.delete(attendance);
        }
    }
    
    @Test
    public void testFindAll() {
        List<Attendance> attendances = repository.findAll();
        assertNotNull(attendances);
        assertTrue(attendances.size() >= 2);
    }
    
    @Test
    public void testFindById() {
        // First, get an attendance ID from the DB
        List<Attendance> attendances = repository.findByMeeting(testMeeting);
        Attendance firstAttendance = attendances.get(0);
        
        // Now test findById
        Optional<Attendance> found = repository.findById(firstAttendance.getId());
        assertTrue(found.isPresent());
        assertEquals(firstAttendance.getMember().getId(), found.get().getMember().getId());
        assertEquals(firstAttendance.getMeeting().getId(), found.get().getMeeting().getId());
    }
    
    @Test
    public void testFindByMeeting() {
        List<Attendance> attendances = repository.findByMeeting(testMeeting);
        assertFalse(attendances.isEmpty());
        assertTrue(attendances.stream().allMatch(a -> a.getMeeting().getId().equals(testMeeting.getId())));
        assertEquals(2, attendances.size());
    }
    
    @Test
    public void testFindByMember() {
        List<Attendance> attendances = repository.findByMember(testMember1);
        assertFalse(attendances.isEmpty());
        assertTrue(attendances.stream().allMatch(a -> a.getMember().getId().equals(testMember1.getId())));
        assertEquals(1, attendances.size());
    }
    
    @Test
    public void testFindByMeetingAndMember() {
        Optional<Attendance> attendance = repository.findByMeetingAndMember(testMeeting, testMember1);
        assertTrue(attendance.isPresent());
        assertEquals(testMeeting.getId(), attendance.get().getMeeting().getId());
        assertEquals(testMember1.getId(), attendance.get().getMember().getId());
        assertTrue(attendance.get().isPresent());
    }
    
    @Test
    public void testFindByPresent() {
        List<Attendance> presentAttendances = repository.findByPresent(true);
        assertFalse(presentAttendances.isEmpty());
        assertTrue(presentAttendances.stream().allMatch(Attendance::isPresent));
        
        List<Attendance> absentAttendances = repository.findByPresent(false);
        assertFalse(absentAttendances.isEmpty());
        assertTrue(absentAttendances.stream().noneMatch(Attendance::isPresent));
    }
    
    @Test
    public void testSave() {
        // Create new test meeting and member
        Meeting newMeeting = new Meeting(
            LocalDate.now().plusDays(5),
            LocalTime.of(18, 0),
            LocalTime.of(20, 0),
            testProject
        );
        newMeeting = meetingRepository.save(newMeeting);
        
        TeamMember newMember = new TeamMember("attendancesave", "Save", "Test", "save@example.com");
        newMember = teamMemberRepository.save(newMember);
        
        // Create new attendance
        Attendance newAttendance = new Attendance(newMeeting, newMember, true);
        newAttendance.setArrivalTime(LocalTime.of(18, 15));
        newAttendance.setDepartureTime(LocalTime.of(19, 45));
        
        Attendance saved = repository.save(newAttendance);
        assertNotNull(saved.getId());
        
        // Verify it was saved
        Optional<Attendance> found = repository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(newMeeting.getId(), found.get().getMeeting().getId());
        assertEquals(newMember.getId(), found.get().getMember().getId());
        assertEquals(LocalTime.of(18, 15), found.get().getArrivalTime());
        
        // Clean up
        repository.delete(saved);
        meetingRepository.delete(newMeeting);
        teamMemberRepository.delete(newMember);
    }
    
    @Test
    public void testUpdate() {
        // First, get an attendance record
        Optional<Attendance> attendanceOpt = repository.findByMeetingAndMember(testMeeting, testMember1);
        assertTrue(attendanceOpt.isPresent());
        Attendance attendance = attendanceOpt.get();
        
        // Now update it
        attendance.setArrivalTime(LocalTime.of(18, 10));
        attendance.setDepartureTime(LocalTime.of(19, 50));
        Attendance updated = repository.save(attendance);
        
        // Verify the update
        assertEquals(LocalTime.of(18, 10), updated.getArrivalTime());
        assertEquals(LocalTime.of(19, 50), updated.getDepartureTime());
        
        // Check in DB
        Optional<Attendance> found = repository.findById(updated.getId());
        assertTrue(found.isPresent());
        assertEquals(LocalTime.of(18, 10), found.get().getArrivalTime());
        assertEquals(LocalTime.of(19, 50), found.get().getDepartureTime());
    }
    
    @Test
    public void testDelete() {
        // First, create a new attendance record
        Meeting newMeeting = new Meeting(
            LocalDate.now().plusDays(10),
            LocalTime.of(18, 0),
            LocalTime.of(20, 0),
            testProject
        );
        newMeeting = meetingRepository.save(newMeeting);
        
        TeamMember newMember = new TeamMember("attendancedelete", "Delete", "Test", "delete@example.com");
        newMember = teamMemberRepository.save(newMember);
        
        Attendance attendance = new Attendance(newMeeting, newMember, true);
        Attendance saved = repository.save(attendance);
        Long id = saved.getId();
        
        // Now delete it
        repository.delete(saved);
        
        // Verify the deletion
        Optional<Attendance> found = repository.findById(id);
        assertFalse(found.isPresent());
        
        // Clean up
        meetingRepository.delete(newMeeting);
        teamMemberRepository.delete(newMember);
    }
    
    @Test
    public void testDeleteById() {
        // First, create a new attendance record
        Meeting newMeeting = new Meeting(
            LocalDate.now().plusDays(15),
            LocalTime.of(18, 0),
            LocalTime.of(20, 0),
            testProject
        );
        newMeeting = meetingRepository.save(newMeeting);
        
        TeamMember newMember = new TeamMember("attendancedeletebyid", "DeleteById", "Test", "deletebyid@example.com");
        newMember = teamMemberRepository.save(newMember);
        
        Attendance attendance = new Attendance(newMeeting, newMember, true);
        Attendance saved = repository.save(attendance);
        Long id = saved.getId();
        
        // Now delete it by ID
        boolean result = repository.deleteById(id);
        assertTrue(result);
        
        // Verify the deletion
        Optional<Attendance> found = repository.findById(id);
        assertFalse(found.isPresent());
        
        // Clean up
        meetingRepository.delete(newMeeting);
        teamMemberRepository.delete(newMember);
    }
    
    @Test
    public void testCount() {
        long initialCount = repository.count();
        
        // Add a new attendance record
        Meeting newMeeting = new Meeting(
            LocalDate.now().plusDays(20),
            LocalTime.of(18, 0),
            LocalTime.of(20, 0),
            testProject
        );
        newMeeting = meetingRepository.save(newMeeting);
        
        TeamMember newMember = new TeamMember("attendancecount", "Count", "Test", "count@example.com");
        newMember = teamMemberRepository.save(newMember);
        
        Attendance attendance = new Attendance(newMeeting, newMember, true);
        repository.save(attendance);
        
        // Verify count increased
        long newCount = repository.count();
        assertEquals(initialCount + 1, newCount);
        
        // Clean up
        Optional<Attendance> saved = repository.findByMeetingAndMember(newMeeting, newMember);
        if (saved.isPresent()) {
            repository.delete(saved.get());
        }
        meetingRepository.delete(newMeeting);
        teamMemberRepository.delete(newMember);
    }
}