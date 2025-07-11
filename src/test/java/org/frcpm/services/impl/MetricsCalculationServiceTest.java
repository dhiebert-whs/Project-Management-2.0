// src/test/java/org/frcpm/services/impl/MetricsCalculationServiceTest.java

package org.frcpm.services.impl;

import org.frcpm.models.*;
import org.frcpm.repositories.spring.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Spring Boot test class for MetricsCalculationServiceImpl.
 * FIXED: Applied proven pattern from AttendanceServiceTest success template.
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

    private MetricsCalculationServiceImpl metricsService; // ✅ FIXED: Use implementation class

    private Project testProject;
    private List<Task> testTasks;
    private List<TeamMember> testTeamMembers;
    private List<Milestone> testMilestones;
    private List<Meeting> testMeetings;
    private List<Attendance> testAttendances;
    private Subsystem testSubsystem;

    @BeforeEach
    void setUp() {
        // ✅ FIXED: Create test objects ONLY - NO MOCK STUBBING HERE
        setupTestData();
        
        // Create service with mocked dependencies
        metricsService = new MetricsCalculationServiceImpl(
                projectRepository,
                taskRepository,
                teamMemberRepository,
                milestoneRepository,
                attendanceRepository,
                meetingRepository
        );
        
        // ✅ FIXED: NO mock stubbing in setUp() - move to individual test methods
    }

    private void setupTestData() {
        // Create test project
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");
        testProject.setDescription("Test project description");
        testProject.setStartDate(LocalDate.now().minusDays(30));
        testProject.setGoalEndDate(LocalDate.now().plusDays(30));
        testProject.setHardDeadline(LocalDate.now().plusDays(35));

        // Create test subsystem
        testSubsystem = new Subsystem();
        testSubsystem.setId(1L);
        testSubsystem.setName("Test Subsystem");
        testSubsystem.setDescription("Test subsystem description");

        // Create test team members
        testTeamMembers = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            TeamMember member = new TeamMember();
            member.setId((long) i);
            member.setUsername("user" + i);
            member.setFirstName("Member" + i);
            member.setLastName("Test");
            member.setEmail("member" + i + "@test.com");
            testTeamMembers.add(member);
        }

        // Create test tasks
        testTasks = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Task task = new Task();
            task.setId((long) i);
            task.setTitle("Test Task " + i);
            task.setDescription("Description for task " + i);
            task.setProgress(i * 20); // 20%, 40%, 60%, 80%, 100%
            task.setStartDate(LocalDate.now().minusDays(20));
            task.setEndDate(LocalDate.now().plusDays(10));
            task.setEstimatedDuration(Duration.ofHours(8));
            task.setActualDuration(Duration.ofHours(8 + i)); // Some variance
            task.setPriority(Task.Priority.values()[i % Task.Priority.values().length]);
            task.setProject(testProject);
            task.setSubsystem(testSubsystem);
            
            testTasks.add(task);
        }

        // Create test milestones
        testMilestones = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Milestone milestone = new Milestone();
            milestone.setId((long) i);
            milestone.setName("Milestone " + i);
            milestone.setDescription("Description for milestone " + i);
            milestone.setDate(LocalDate.now().minusDays(15 - i * 5));
            milestone.setProject(testProject);
            testMilestones.add(milestone);
        }

        // Create test meetings
        testMeetings = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            Meeting meeting = new Meeting();
            meeting.setId((long) i);
            meeting.setDate(LocalDate.now().minusDays(20 - i * 5));
            meeting.setStartTime(LocalTime.of(9, 0));
            meeting.setEndTime(LocalTime.of(10, 0));
            meeting.setProject(testProject);
            meeting.setAttendances(new ArrayList<>());
            testMeetings.add(meeting);
        }

        // Create test attendances
        testAttendances = new ArrayList<>();
        for (Meeting meeting : testMeetings) {
            List<Attendance> meetingAttendances = new ArrayList<>();
            for (int j = 0; j < testTeamMembers.size(); j++) {
                Attendance attendance = new Attendance();
                attendance.setId((long) (meeting.getId() * 10 + j));
                attendance.setMember(testTeamMembers.get(j));
                attendance.setMeeting(meeting);
                attendance.setPresent(j < 2); // First 2 members attend each meeting
                
                meetingAttendances.add(attendance);
                testAttendances.add(attendance);
            }
            meeting.setAttendances(meetingAttendances);
        }
    }

    @Test
    void testCalculateProjectProgressMetrics() {
        // Setup - Only stub what THIS test needs
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(taskRepository.findByProject(testProject)).thenReturn(testTasks);
        when(milestoneRepository.findByProject(testProject)).thenReturn(testMilestones);

        // Execute
        Map<String, Object> metrics = metricsService.calculateProjectProgressMetrics(1L);

        // Verify
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("completionPercentage"));
        assertTrue(metrics.containsKey("daysTotal"));
        assertTrue(metrics.containsKey("daysRemaining"));
        assertTrue(metrics.containsKey("scheduleVariance"));
        assertTrue(metrics.containsKey("taskStatusCounts"));
        assertTrue(metrics.containsKey("totalMilestones"));
        assertTrue(metrics.containsKey("passedMilestones"));

        // Verify expected values
        Double completionPercentage = (Double) metrics.get("completionPercentage");
        assertNotNull(completionPercentage);
        assertTrue(completionPercentage >= 0 && completionPercentage <= 100);

        // Verify repository interactions
        verify(projectRepository).findById(1L);
        verify(taskRepository).findByProject(testProject);
        verify(milestoneRepository).findByProject(testProject);
    }

    @Test
    void testCalculateProjectProgressMetrics_ProjectNotFound() {
        // Setup - Entity doesn't exist
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        // Execute
        Map<String, Object> metrics = metricsService.calculateProjectProgressMetrics(999L);

        // Verify
        assertNotNull(metrics);
        assertTrue(metrics.isEmpty());

        // Verify repository interactions
        verify(projectRepository).findById(999L);
        verify(taskRepository, never()).findByProject(any());
    }

    @Test
    void testCalculateProjectProgressMetrics_NullProjectId() {
        // Execute and verify exception
        assertThrows(IllegalArgumentException.class, () -> {
            metricsService.calculateProjectProgressMetrics(null);
        });

        // Verify repository was never called
        verify(projectRepository, never()).findById(any());
        verify(taskRepository, never()).findByProject(any());
    }

    @Test
    void testCalculateTeamPerformanceMetrics() {
        // Setup
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(meetingRepository.findByProject(testProject)).thenReturn(testMeetings);
        when(taskRepository.findByAssignedMember(any(TeamMember.class))).thenReturn(
                testTasks.subList(0, 2)); // Each member has 2 tasks

        // Execute
        Map<String, Object> metrics = metricsService.calculateTeamPerformanceMetrics(1L, null, null);

        // Verify
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("totalTeamMembers"));
        assertTrue(metrics.containsKey("averageAttendancePercentage"));
        assertTrue(metrics.containsKey("tasksPerMember"));
        assertTrue(metrics.containsKey("averageTasksPerMember"));

        // Verify repository interactions
        verify(projectRepository).findById(1L);
        verify(meetingRepository).findByProject(testProject);
    }

    @Test
    void testCalculateTeamPerformanceMetrics_WithDateRange() {
        // Setup
        LocalDate startDate = LocalDate.now().minusDays(15);
        LocalDate endDate = LocalDate.now().minusDays(5);
        
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(meetingRepository.findByProject(testProject)).thenReturn(testMeetings);
        when(taskRepository.findByAssignedMember(any(TeamMember.class))).thenReturn(
                testTasks.subList(0, 2));

        // Execute
        Map<String, Object> metrics = metricsService.calculateTeamPerformanceMetrics(1L, startDate, endDate);

        // Verify
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("totalTeamMembers"));
        assertTrue(metrics.containsKey("averageAttendancePercentage"));

        // Verify repository interactions
        verify(projectRepository).findById(1L);
        verify(meetingRepository).findByProject(testProject);
    }

    @Test
    void testCalculateTeamPerformanceMetrics_ProjectNotFound() {
        // Setup - Entity doesn't exist
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        // Execute
        Map<String, Object> metrics = metricsService.calculateTeamPerformanceMetrics(999L, null, null);

        // Verify
        assertNotNull(metrics);
        assertTrue(metrics.isEmpty());

        // Verify repository interactions
        verify(projectRepository).findById(999L);
        verify(meetingRepository, never()).findByProject(any());
    }

    @Test
    void testCalculateTaskCompletionMetrics() {
        // Setup
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(taskRepository.findByProject(testProject)).thenReturn(testTasks);

        // Execute
        Map<String, Object> metrics = metricsService.calculateTaskCompletionMetrics(1L);

        // Verify
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("taskCountsByStatus"));
        assertTrue(metrics.containsKey("taskCountsByPriority"));
        assertTrue(metrics.containsKey("completionBySubsystem"));
        assertTrue(metrics.containsKey("averageEstimatedDurationHours"));
        assertTrue(metrics.containsKey("averageActualDurationHours"));
        assertTrue(metrics.containsKey("bottleneckTaskCount"));

        // Verify task status counts
        @SuppressWarnings("unchecked")
        Map<String, Long> statusCounts = (Map<String, Long>) metrics.get("taskCountsByStatus");
        assertNotNull(statusCounts);
        assertTrue(statusCounts.containsKey("COMPLETED"));
        assertTrue(statusCounts.containsKey("IN_PROGRESS"));
        assertTrue(statusCounts.containsKey("NOT_STARTED"));

        // Verify repository interactions
        verify(projectRepository).findById(1L);
        verify(taskRepository).findByProject(testProject);
    }

    @Test
    void testCalculateTaskCompletionMetrics_ProjectNotFound() {
        // Setup - Entity doesn't exist
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        // Execute
        Map<String, Object> metrics = metricsService.calculateTaskCompletionMetrics(999L);

        // Verify
        assertNotNull(metrics);
        assertTrue(metrics.isEmpty());

        // Verify repository interactions
        verify(projectRepository).findById(999L);
        verify(taskRepository, never()).findByProject(any());
    }

    @Test
    void testCalculateAttendanceMetrics() {
        // Setup
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(meetingRepository.findByProject(testProject)).thenReturn(testMeetings);

        // Execute
        Map<String, Object> metrics = metricsService.calculateAttendanceMetrics(1L, null, null);

        // Verify
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("totalMeetings"));
        assertTrue(metrics.containsKey("totalAttendanceRecords"));
        assertTrue(metrics.containsKey("totalPresent"));
        assertTrue(metrics.containsKey("overallAttendanceRate"));
        assertTrue(metrics.containsKey("attendanceRateByMember"));
        assertTrue(metrics.containsKey("attendanceRateByDate"));

        // Verify repository interactions
        verify(projectRepository).findById(1L);
        verify(meetingRepository).findByProject(testProject);
    }

    @Test
    void testCalculateAttendanceMetrics_WithDateRange() {
        // Setup
        LocalDate startDate = LocalDate.now().minusDays(25);
        LocalDate endDate = LocalDate.now().minusDays(5);
        
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(meetingRepository.findByProject(testProject)).thenReturn(testMeetings);

        // Execute
        Map<String, Object> metrics = metricsService.calculateAttendanceMetrics(1L, startDate, endDate);

        // Verify
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("totalMeetings"));
        assertTrue(metrics.containsKey("overallAttendanceRate"));

        // Verify repository interactions
        verify(projectRepository).findById(1L);
        verify(meetingRepository).findByProject(testProject);
    }

    @Test
    void testCalculateTimelineDeviationMetrics() {
        // Setup
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(taskRepository.findByProject(testProject)).thenReturn(testTasks);

        // Execute
        Map<String, Object> metrics = metricsService.calculateTimelineDeviationMetrics(1L);

        // Verify
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("averageTaskDelay"));
        assertTrue(metrics.containsKey("tasksAtRiskCount"));
        assertTrue(metrics.containsKey("tasksAtRiskIds"));
        assertTrue(metrics.containsKey("projectedDelay"));
        assertTrue(metrics.containsKey("isOnSchedule"));
        assertTrue(metrics.containsKey("timelineDeviationPercentage"));

        // Verify repository interactions
        verify(projectRepository).findById(1L);
        verify(taskRepository).findByProject(testProject);
    }

    @Test
    void testCalculateTimelineDeviationMetrics_ProjectNotFound() {
        // Setup - Entity doesn't exist
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        // Execute
        Map<String, Object> metrics = metricsService.calculateTimelineDeviationMetrics(999L);

        // Verify
        assertNotNull(metrics);
        assertTrue(metrics.isEmpty());

        // Verify repository interactions
        verify(projectRepository).findById(999L);
        verify(taskRepository, never()).findByProject(any());
    }

    @Test
    void testCalculateIndividualPerformanceMetrics() {
        // Setup
        when(teamMemberRepository.findById(1L)).thenReturn(Optional.of(testTeamMembers.get(0)));
        when(taskRepository.findByAssignedMember(testTeamMembers.get(0))).thenReturn(testTasks.subList(0, 2));
        when(attendanceRepository.findByMember(testTeamMembers.get(0))).thenReturn(testAttendances.subList(0, 4));

        // Execute
        Map<String, Object> metrics = metricsService.calculateIndividualPerformanceMetrics(1L, null, null);

        // Verify
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("memberId"));
        assertTrue(metrics.containsKey("memberName"));
        assertTrue(metrics.containsKey("totalAssignedTasks"));
        assertTrue(metrics.containsKey("completedTasks"));
        assertTrue(metrics.containsKey("taskCompletionRate"));
        assertTrue(metrics.containsKey("totalMeetings"));
        assertTrue(metrics.containsKey("attendanceRate"));

        // Verify member information
        assertEquals(1L, metrics.get("memberId"));
        assertNotNull(metrics.get("memberName"));

        // Verify repository interactions
        verify(teamMemberRepository).findById(1L);
        verify(taskRepository).findByAssignedMember(testTeamMembers.get(0));
        verify(attendanceRepository).findByMember(testTeamMembers.get(0));
    }

    @Test
    void testCalculateIndividualPerformanceMetrics_MemberNotFound() {
        // Setup - Entity doesn't exist
        when(teamMemberRepository.findById(999L)).thenReturn(Optional.empty());

        // Execute
        Map<String, Object> metrics = metricsService.calculateIndividualPerformanceMetrics(999L, null, null);

        // Verify
        assertNotNull(metrics);
        assertTrue(metrics.isEmpty());

        // Verify repository interactions
        verify(teamMemberRepository).findById(999L);
        verify(taskRepository, never()).findByAssignedMember(any());
    }

    @Test
    void testCalculateIndividualPerformanceMetrics_WithDateRange() {
        // Setup
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now();
        
        when(teamMemberRepository.findById(1L)).thenReturn(Optional.of(testTeamMembers.get(0)));
        when(taskRepository.findByAssignedMember(testTeamMembers.get(0))).thenReturn(testTasks.subList(0, 2));
        when(attendanceRepository.findByMember(testTeamMembers.get(0))).thenReturn(testAttendances.subList(0, 4));

        // Execute
        Map<String, Object> metrics = metricsService.calculateIndividualPerformanceMetrics(1L, startDate, endDate);

        // Verify
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("memberId"));
        assertTrue(metrics.containsKey("totalAssignedTasks"));
        assertTrue(metrics.containsKey("attendanceRate"));

        // Verify repository interactions
        verify(teamMemberRepository).findById(1L);
        verify(taskRepository).findByAssignedMember(testTeamMembers.get(0));
        verify(attendanceRepository).findByMember(testTeamMembers.get(0));
    }

    @Test
    void testCalculateSubsystemPerformanceMetrics() {
        // Setup
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(taskRepository.findByProject(testProject)).thenReturn(testTasks);

        // Execute
        Map<String, Object> metrics = metricsService.calculateSubsystemPerformanceMetrics(1L);

        // Verify
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("subsystemMetrics"));
        assertTrue(metrics.containsKey("subsystemRankings"));

        // Verify subsystem metrics structure
        @SuppressWarnings("unchecked")
        Map<Long, Map<String, Object>> subsystemMetrics = 
                (Map<Long, Map<String, Object>>) metrics.get("subsystemMetrics");
        assertNotNull(subsystemMetrics);

        // Verify repository interactions
        verify(projectRepository).findById(1L);
        verify(taskRepository).findByProject(testProject);
    }

    @Test
    void testCalculateSubsystemPerformanceMetrics_ProjectNotFound() {
        // Setup - Entity doesn't exist
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        // Execute
        Map<String, Object> metrics = metricsService.calculateSubsystemPerformanceMetrics(999L);

        // Verify
        assertNotNull(metrics);
        assertTrue(metrics.isEmpty());

        // Verify repository interactions
        verify(projectRepository).findById(999L);
        verify(taskRepository, never()).findByProject(any());
    }

    @Test
    void testGenerateProjectHealthDashboard() {
        // Setup - Mock all dependencies needed for the dashboard
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(taskRepository.findByProject(testProject)).thenReturn(testTasks);
        when(milestoneRepository.findByProject(testProject)).thenReturn(testMilestones);
        when(meetingRepository.findByProject(testProject)).thenReturn(testMeetings);
        when(taskRepository.findByAssignedMember(any(TeamMember.class))).thenReturn(testTasks.subList(0, 2));

        // Execute
        Map<String, Object> dashboard = metricsService.generateProjectHealthDashboard(1L);

        // Verify
        assertNotNull(dashboard);
        assertTrue(dashboard.containsKey("projectId"));
        assertTrue(dashboard.containsKey("projectName"));
        assertTrue(dashboard.containsKey("overallCompletion"));
        assertTrue(dashboard.containsKey("scheduleVariance"));
        assertTrue(dashboard.containsKey("healthStatus"));
        assertTrue(dashboard.containsKey("overallHealthScore"));
        assertTrue(dashboard.containsKey("recommendations"));

        // Verify project information
        assertEquals(1L, dashboard.get("projectId"));
        assertEquals("Test Project", dashboard.get("projectName"));

        // Verify health status is valid
        String healthStatus = (String) dashboard.get("healthStatus");
        assertTrue(Arrays.asList("EXCELLENT", "GOOD", "WARNING", "CRITICAL").contains(healthStatus));

        // Verify recommendations list
        @SuppressWarnings("unchecked")
        List<String> recommendations = (List<String>) dashboard.get("recommendations");
        assertNotNull(recommendations);
        assertFalse(recommendations.isEmpty());

        // Verify repository interactions (multiple calls due to aggregating other metrics)
        verify(projectRepository, atLeast(4)).findById(1L);
        verify(taskRepository, atLeast(2)).findByProject(testProject);
    }

    @Test
    void testGenerateProjectHealthDashboard_ProjectNotFound() {
        // Setup - Entity doesn't exist
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        // Execute
        Map<String, Object> dashboard = metricsService.generateProjectHealthDashboard(999L);

        // Verify
        assertNotNull(dashboard);
        assertTrue(dashboard.isEmpty());

        // Verify repository interactions
        verify(projectRepository).findById(999L);
    }

    @Test
    void testAsyncMethods() {
        // Setup for async tests
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(taskRepository.findByProject(testProject)).thenReturn(testTasks);
        when(milestoneRepository.findByProject(testProject)).thenReturn(testMilestones);
        when(meetingRepository.findByProject(testProject)).thenReturn(testMeetings);
        when(teamMemberRepository.findById(1L)).thenReturn(Optional.of(testTeamMembers.get(0)));
        when(taskRepository.findByAssignedMember(any(TeamMember.class))).thenReturn(testTasks.subList(0, 2));
        when(attendanceRepository.findByMember(any(TeamMember.class))).thenReturn(testAttendances.subList(0, 4));

        // Test that async methods complete successfully
        // Note: In unit tests, @Async methods run synchronously

        // Test async project progress metrics
        var futureProgress = metricsService.calculateProjectProgressMetricsAsync(1L);
        assertNotNull(futureProgress);
        assertTrue(futureProgress.isDone());

        // Test async team performance metrics
        var futureTeam = metricsService.calculateTeamPerformanceMetricsAsync(1L, null, null);
        assertNotNull(futureTeam);
        assertTrue(futureTeam.isDone());

        // Test async task completion metrics
        var futureTask = metricsService.calculateTaskCompletionMetricsAsync(1L);
        assertNotNull(futureTask);
        assertTrue(futureTask.isDone());

        // Test async attendance metrics
        var futureAttendance = metricsService.calculateAttendanceMetricsAsync(1L, null, null);
        assertNotNull(futureAttendance);
        assertTrue(futureAttendance.isDone());

        // Test async timeline deviation metrics
        var futureTimeline = metricsService.calculateTimelineDeviationMetricsAsync(1L);
        assertNotNull(futureTimeline);
        assertTrue(futureTimeline.isDone());

        // Test async individual performance metrics
        var futureIndividual = metricsService.calculateIndividualPerformanceMetricsAsync(1L, null, null);
        assertNotNull(futureIndividual);
        assertTrue(futureIndividual.isDone());

        // Test async subsystem performance metrics
        var futureSubsystem = metricsService.calculateSubsystemPerformanceMetricsAsync(1L);
        assertNotNull(futureSubsystem);
        assertTrue(futureSubsystem.isDone());

        // Test async project health dashboard
        var futureDashboard = metricsService.generateProjectHealthDashboardAsync(1L);
        assertNotNull(futureDashboard);
        assertTrue(futureDashboard.isDone());
    }

    @Test
    void testErrorHandling() {
        // Test exception handling when repository throws exception
        when(projectRepository.findById(1L)).thenThrow(new RuntimeException("Database error"));

        // Should not throw exception, but return empty map
        Map<String, Object> metrics = metricsService.calculateProjectProgressMetrics(1L);
        assertNotNull(metrics);
        // Error handling should return empty map or partial results
    }

    @Test
    void testMetricsWithEmptyData() {
        // Setup with empty test data - ONLY stub what this test needs
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(taskRepository.findByProject(testProject)).thenReturn(Collections.emptyList());
        when(milestoneRepository.findByProject(testProject)).thenReturn(Collections.emptyList());
        // Remove unnecessary stubbing that caused the error

        // Execute
        Map<String, Object> metrics = metricsService.calculateProjectProgressMetrics(1L);

        // Verify
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("completionPercentage"));
        
        // Should handle empty data gracefully
        Double completionPercentage = (Double) metrics.get("completionPercentage");
        assertEquals(0.0, completionPercentage);
    }

    @Test
    void testNullInputValidation() {
        // Test null project ID for various methods
        assertThrows(IllegalArgumentException.class, () -> {
            metricsService.calculateProjectProgressMetrics(null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            metricsService.calculateTeamPerformanceMetrics(null, null, null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            metricsService.calculateTaskCompletionMetrics(null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            metricsService.calculateAttendanceMetrics(null, null, null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            metricsService.calculateTimelineDeviationMetrics(null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            metricsService.calculateIndividualPerformanceMetrics(null, null, null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            metricsService.calculateSubsystemPerformanceMetrics(null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            metricsService.generateProjectHealthDashboard(null);
        });

        // Verify repository was never called
        verify(projectRepository, never()).findById(any());
        verify(teamMemberRepository, never()).findById(any());
    }
}