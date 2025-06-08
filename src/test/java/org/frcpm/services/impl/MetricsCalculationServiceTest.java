// src/test/java/org/frcpm/services/impl/MetricsCalculationServiceTest.java
package org.frcpm.services.impl;

import org.frcpm.models.*;
import org.frcpm.repositories.spring.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Test class for MetricsCalculationService implementation using Spring Boot testing patterns.
 */
@ExtendWith(MockitoExtension.class)
class MetricsCalculationServiceTest {
    
    @Mock
    private ProjectRepository projectRepository;
    
    @Mock
    private TaskRepository taskRepository;
    
    @Mock
    private TeamMemberRepository teamMemberRepository;
    
    @Mock
    private MilestoneRepository milestoneRepository;
    
    @Mock
    private AttendanceRepository attendanceRepository;
    
    @Mock
    private MeetingRepository meetingRepository;
    
    @Mock
    private SubsystemRepository subsystemRepository;
    
    private MetricsCalculationServiceImpl metricsService;
    
    private Project testProject;
    private Task testTask;
    private TeamMember testMember;
    private Milestone testMilestone;
    private Meeting testMeeting;
    private Attendance testAttendance;
    private Subsystem testSubsystem;
    private LocalDate now;
    
    @BeforeEach
    void setUp() {
        // Initialize date for consistent testing
        now = LocalDate.now();
        
        // Create test objects
        testProject = createTestProject();
        testSubsystem = createTestSubsystem();
        testTask = createTestTask();
        testMember = createTestMember();
        testMilestone = createTestMilestone();
        testMeeting = createTestMeeting();
        testAttendance = createTestAttendance();
        
        // Configure mock repository responses
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(taskRepository.findByProject(testProject)).thenReturn(List.of(testTask));
        when(teamMemberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(milestoneRepository.findByProject(testProject)).thenReturn(List.of(testMilestone));
        when(meetingRepository.findByProject(testProject)).thenReturn(List.of(testMeeting));
        when(attendanceRepository.findByMember(testMember)).thenReturn(List.of(testAttendance));
        when(subsystemRepository.findAll()).thenReturn(List.of(testSubsystem));
        
        // Create service with repository mocks
        metricsService = new MetricsCalculationServiceImpl(
            projectRepository,
            taskRepository,
            teamMemberRepository,
            milestoneRepository,
            attendanceRepository,
            meetingRepository,
            subsystemRepository
        );
    }
    
    /**
     * Creates a test project.
     */
    private Project createTestProject() {
        Project project = new Project("Test Project", now.minusDays(30), now.plusDays(60), now.plusDays(90));
        project.setId(1L);
        return project;
    }
    
    /**
     * Creates a test subsystem.
     */
    private Subsystem createTestSubsystem() {
        Subsystem subsystem = new Subsystem("Test Subsystem");
        subsystem.setId(1L);
        subsystem.setStatus(Subsystem.Status.IN_PROGRESS);
        return subsystem;
    }
    
    /**
     * Creates a test task.
     */
    private Task createTestTask() {
        Task task = new Task("Test Task", testProject, testSubsystem);
        task.setId(1L);
        task.setProgress(50);
        task.setStartDate(now.minusDays(10));
        task.setEndDate(now.plusDays(20));
        return task;
    }
    
    /**
     * Creates a test team member.
     */
    private TeamMember createTestMember() {
        TeamMember member = new TeamMember("user1", "John", "Doe", "john@example.com");
        member.setId(1L);
        return member;
    }
    
    /**
     * Creates a test milestone.
     */
    private Milestone createTestMilestone() {
        Milestone milestone = new Milestone("Test Milestone", now.plusDays(15), testProject);
        milestone.setId(1L);
        return milestone;
    }
    
    /**
     * Creates a test meeting.
     */
    private Meeting createTestMeeting() {
        Meeting meeting = new Meeting(now, LocalTime.of(9, 0), LocalTime.of(10, 0), testProject);
        meeting.setId(1L);
        return meeting;
    }
    
    /**
     * Creates a test attendance record.
     */
    private Attendance createTestAttendance() {
        Attendance attendance = new Attendance(testMeeting, testMember, true);
        attendance.setId(1L);
        return attendance;
    }
    
    @Test
    void testCalculateTeamPerformanceMetrics_WithDateRange() {
        // Execute
        LocalDate startDate = now.minusDays(10);
        LocalDate endDate = now.plusDays(10);
        Map<String, Object> metrics = metricsService.calculateTeamPerformanceMetrics(1L, startDate, endDate);
        
        // Verify
        assertNotNull(metrics);
        
        // Verify repository calls
        verify(projectRepository).findById(1L);
        verify(meetingRepository).findByProject(testProject);
    }
    
    @Test
    void testCalculateTaskCompletionMetrics() {
        // Execute
        Map<String, Object> metrics = metricsService.calculateTaskCompletionMetrics(1L);
        
        // Verify
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("taskCountsByStatus"));
        
        // Verify repository calls
        verify(projectRepository).findById(1L);
        verify(taskRepository).findByProject(testProject);
    }
    
    @Test
    void testCalculateAttendanceMetrics() {
        // Setup
        testMeeting.addAttendance(testAttendance);
        
        // Execute
        Map<String, Object> metrics = metricsService.calculateAttendanceMetrics(1L, null, null);
        
        // Verify
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("totalMeetings"));
        assertTrue(metrics.containsKey("overallAttendanceRate"));
        
        // Verify repository calls
        verify(projectRepository).findById(1L);
        verify(meetingRepository).findByProject(testProject);
    }
    
    @Test
    void testCalculateTimelineDeviationMetrics() {
        // Execute
        Map<String, Object> metrics = metricsService.calculateTimelineDeviationMetrics(1L);
        
        // Verify
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("projectedDelay"));
        assertTrue(metrics.containsKey("isOnSchedule"));
        
        // Verify repository calls
        verify(projectRepository).findById(1L);
        verify(taskRepository).findByProject(testProject);
        verify(milestoneRepository).findByProject(testProject);
    }
    
    @Test
    void testCalculateIndividualPerformanceMetrics() {
        // Setup
        testTask.getAssignedTo().add(testMember);
        
        // Execute
        Map<String, Object> metrics = metricsService.calculateIndividualPerformanceMetrics(1L, null, null);
        
        // Verify
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("memberId"));
        assertTrue(metrics.containsKey("memberName"));
        
        // Verify repository calls
        verify(teamMemberRepository).findById(1L);
        verify(attendanceRepository).findByMember(testMember);
    }
    
    @Test
    void testCalculateSubsystemPerformanceMetrics() {
        // Setup
        testTask.setSubsystem(testSubsystem);
        
        // Execute
        Map<String, Object> metrics = metricsService.calculateSubsystemPerformanceMetrics(1L);
        
        // Verify
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("totalSubsystems"));
        
        // Verify repository calls
        verify(projectRepository).findById(1L);
        verify(taskRepository).findByProject(testProject);
    }
    
    @Test
    void testGenerateProjectHealthDashboard() {
        // Execute
        Map<String, Object> dashboard = metricsService.generateProjectHealthDashboard(1L);
        
        // Verify
        assertNotNull(dashboard);
        assertTrue(dashboard.containsKey("healthScore"));
        assertTrue(dashboard.containsKey("healthStatus"));
        
        // Verify repository calls - using atLeastOnce() instead of times(1)
        verify(projectRepository, atLeastOnce()).findById(1L);
    }
    void testCalculateProjectProgressMetrics() {
        // Execute
        Map<String, Object> metrics = metricsService.calculateProjectProgressMetrics(1L);
        
        // Verify
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("completionPercentage"));
        assertTrue(metrics.containsKey("daysTotal"));
        assertTrue(metrics.containsKey("daysRemaining"));
        assertTrue(metrics.containsKey("timeProgressPercentage"));
        
        // Verify repository calls
        verify(projectRepository).findById(1L);
        verify(taskRepository).findByProject(testProject);
    }
    
    @Test
    void testCalculateProjectProgressMetrics_ProjectNotFound() {
        // Setup
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());
        
        // Execute
        Map<String, Object> metrics = metricsService.calculateProjectProgressMetrics(99L);
        
        // Verify
        assertNotNull(metrics);
        assertTrue(metrics.isEmpty());
        
        // Verify repository calls
        verify(projectRepository).findById(99L);
        verify(taskRepository, never()).findByProject(any(Project.class));
    }
    
    @Test
    void testCalculateTeamPerformanceMetrics() {
        // Setup
        testTask.getAssignedTo().add(testMember);
        testMeeting.addAttendance(testAttendance);
        
        // Execute
        Map<String, Object> metrics = metricsService.calculateTeamPerformanceMetrics(1L, null, null);
        
        // Verify
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("totalTeamMembers"));
        assertTrue(metrics.containsKey("averageAttendancePercentage"));
        
        // Verify repository calls
        verify(projectRepository).findById(1L);
        verify(meetingRepository).findByProject(testProject);
    }
    
    @Test