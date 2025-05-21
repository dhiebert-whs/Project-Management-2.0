// src/test/java/org/frcpm/services/impl/ReportGenerationServiceTest.java
package org.frcpm.services.impl;

import org.frcpm.models.*;
import org.frcpm.repositories.specific.*;
import org.frcpm.services.*;
import org.frcpm.services.impl.TestableReportGenerationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

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
 * Test class for ReportGenerationService implementation.
 */
public class ReportGenerationServiceTest extends BaseServiceTest {
    
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
    
    @Mock
    private MetricsCalculationService metricsService;
    
    @Mock
    private GanttDataService ganttDataService;
    
    @Mock
    private VisualizationService visualizationService;
    
    private ReportGenerationService reportService;
    
    private Project testProject;
    private Task testTask;
    private TeamMember testMember;
    private Milestone testMilestone;
    private Meeting testMeeting;
    private Attendance testAttendance;
    private Subsystem testSubsystem;
    private LocalDate now;
    
    @Override
    protected void setupTestData() {
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
        
        // Configure metrics service mocks
        Map<String, Object> mockMetrics = Map.of(
            "completionPercentage", 50.0,
            "daysTotal", 90,
            "daysRemaining", 45,
            "timeProgressPercentage", 50.0,
            "scheduleVariance", 0.0,
            "healthScore", 80.0,
            "healthStatus", "Healthy"
        );
        
        when(metricsService.calculateProjectProgressMetrics(anyLong())).thenReturn(mockMetrics);
        when(metricsService.calculateTeamPerformanceMetrics(anyLong(), any(), any())).thenReturn(mockMetrics);
        when(metricsService.calculateTaskCompletionMetrics(anyLong())).thenReturn(mockMetrics);
        when(metricsService.calculateAttendanceMetrics(anyLong(), any(), any())).thenReturn(mockMetrics);
        when(metricsService.calculateTimelineDeviationMetrics(anyLong())).thenReturn(mockMetrics);
        when(metricsService.calculateIndividualPerformanceMetrics(anyLong(), any(), any())).thenReturn(mockMetrics);
        when(metricsService.calculateSubsystemPerformanceMetrics(anyLong())).thenReturn(mockMetrics);
        when(metricsService.generateProjectHealthDashboard(anyLong())).thenReturn(mockMetrics);
        
        // Configure Gantt data service mocks
        when(ganttDataService.formatTasksForGantt(anyLong(), any(), any())).thenReturn(Map.of("tasks", List.of()));
        when(ganttDataService.calculateCriticalPath(anyLong())).thenReturn(List.of(1L));
        when(ganttDataService.identifyBottlenecks(anyLong())).thenReturn(List.of(1L));
    }
    
    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        
        // Create service with injected mocks
        reportService = new TestableReportGenerationServiceImpl(
            projectRepository,
            taskRepository,
            teamMemberRepository,
            milestoneRepository,
            attendanceRepository,
            meetingRepository,
            subsystemRepository,
            metricsService,
            ganttDataService,
            visualizationService
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
    public void testGenerateProjectSummaryReport() {
        // Execute
        Map<String, Object> report = reportService.generateProjectSummaryReport(1L);
        
        // Verify
        assertNotNull(report);
        assertTrue(report.containsKey("reportType"));
        assertTrue(report.containsKey("generatedDate"));
        assertTrue(report.containsKey("projectDetails"));
        
        // Verify repository calls
        verify(projectRepository).findById(1L);
        verify(taskRepository).findByProject(testProject);
        verify(milestoneRepository).findByProject(testProject);
    }
    
    @Test
    public void testGenerateProjectSummaryReport_ProjectNotFound() {
        // Setup
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());
        
        // Execute
        Map<String, Object> report = reportService.generateProjectSummaryReport(99L);
        
        // Verify
        assertNotNull(report);
        assertEquals("Project Summary", report.get("reportType"));
        assertTrue(report.containsKey("generatedDate"));
        
        // Verify repository calls
        verify(projectRepository).findById(99L);
        verify(taskRepository, never()).findByProject(any(Project.class));
    }
    
    @Test
    public void testGenerateTeamPerformanceReport() {
        // Execute
        Map<String, Object> report = reportService.generateTeamPerformanceReport(1L, null, null);
        
        // Verify
        assertNotNull(report);
        assertTrue(report.containsKey("reportType"));
        assertTrue(report.containsKey("teamMetrics"));
        
        // Verify calls
        verify(projectRepository).findById(1L);
        verify(metricsService).calculateTeamPerformanceMetrics(eq(1L), isNull(), isNull());
        verify(metricsService).calculateAttendanceMetrics(eq(1L), isNull(), isNull());
        verify(metricsService).calculateTaskCompletionMetrics(eq(1L));
    }
    
    @Test
    public void testGenerateTeamPerformanceReport_WithDateRange() {
        // Setup
        LocalDate startDate = now.minusDays(10);
        LocalDate endDate = now.plusDays(10);
        
        // Execute
        Map<String, Object> report = reportService.generateTeamPerformanceReport(1L, startDate, endDate);
        
        // Verify
        assertNotNull(report);
        assertEquals(startDate, report.get("startDate"));
        assertEquals(endDate, report.get("endDate"));
        
        // Verify calls
        verify(metricsService).calculateTeamPerformanceMetrics(eq(1L), eq(startDate), eq(endDate));
        verify(metricsService).calculateAttendanceMetrics(eq(1L), eq(startDate), eq(endDate));
    }
    
    @Test
    public void testGenerateMilestoneStatusReport() {
        // Execute
        Map<String, Object> report = reportService.generateMilestoneStatusReport(1L);
        
        // Verify
        assertNotNull(report);
        assertTrue(report.containsKey("reportType"));
        assertTrue(report.containsKey("milestoneStatistics"));
        
        // Verify repository calls
        verify(projectRepository).findById(1L);
        verify(milestoneRepository).findByProject(testProject);
    }
    
    @Test
    public void testGenerateAttendanceReport() {
        // Setup
        testMeeting.addAttendance(testAttendance);
        
        // Execute
        Map<String, Object> report = reportService.generateAttendanceReport(1L, null, null);
        
        // Verify
        assertNotNull(report);
        assertTrue(report.containsKey("reportType"));
        assertTrue(report.containsKey("meetingStatistics"));
        
        // Verify repository calls
        verify(projectRepository).findById(1L);
        verify(meetingRepository).findByProject(testProject);
    }
    
    @Test
    public void testGenerateSubsystemProgressReport() {
        // Execute
        Map<String, Object> report = reportService.generateSubsystemProgressReport(1L);
        
        // Verify
        assertNotNull(report);
        assertTrue(report.containsKey("reportType"));
        assertTrue(report.containsKey("subsystemStatistics"));
        
        // Verify repository calls
        verify(projectRepository).findById(1L);
        verify(taskRepository).findByProject(testProject);
    }
    
    @Test
    public void testGenerateTeamMemberReport() {
        // Execute
        Map<String, Object> report = reportService.generateTeamMemberReport(1L, null, null);
        
        // Verify
        assertNotNull(report);
        assertTrue(report.containsKey("reportType"));
        assertTrue(report.containsKey("memberDetails"));
        
        // Verify repository calls
        verify(teamMemberRepository).findById(1L);
        verify(attendanceRepository).findByMember(testMember);
    }
    
    @Test
    public void testGenerateProjectTimelineReport() {
        // Execute
        Map<String, Object> report = reportService.generateProjectTimelineReport(1L);
        
        // Verify
        assertNotNull(report);
        assertTrue(report.containsKey("reportType"));
        assertTrue(report.containsKey("timelineEvents"));
        
        // Verify repository calls
        verify(projectRepository).findById(1L);
        verify(milestoneRepository).findByProject(testProject);
        verify(ganttDataService).formatTasksForGantt(eq(1L), isNull(), isNull());
        verify(ganttDataService).calculateCriticalPath(1L);
        verify(ganttDataService).identifyBottlenecks(1L);
    }
    
    @Test
    public void testExportReportToPdf() {
        // Setup
        Map<String, Object> reportData = Map.of(
            "reportType", "Project Summary",
            "projectDetails", Map.of("name", "Test Project")
        );
        
        // Execute
        byte[] pdfData = reportService.exportReportToPdf(reportData, "Project Summary");
        
        // Verify
        assertNotNull(pdfData);
        assertTrue(pdfData.length > 0);
    }
    
    @Test
    public void testExportReportToCsv() {
        // Setup
        Map<String, Object> reportData = Map.of(
            "reportType", "Project Summary",
            "projectDetails", Map.of("name", "Test Project")
        );
        
        // Execute
        String csvData = reportService.exportReportToCsv(reportData, "Project Summary");
        
        // Verify
        assertNotNull(csvData);
        assertTrue(csvData.length() > 0);
    }
    
    @Test
    public void testGenerateCustomReport() {
        // Setup
        List<String> metrics = List.of("projectProgress", "teamPerformance", "taskCompletion");
        
        // Execute
        Map<String, Object> report = reportService.generateCustomReport(1L, metrics, null, null);
        
        // Verify
        assertNotNull(report);
        assertTrue(report.containsKey("reportType"));
        assertTrue(report.containsKey("selectedMetrics"));
        assertTrue(report.containsKey("summary"));
        
        // Verify calls
        verify(projectRepository).findById(1L);
        verify(metricsService).calculateProjectProgressMetrics(1L);
        verify(metricsService).calculateTeamPerformanceMetrics(eq(1L), isNull(), isNull());
        verify(metricsService).calculateTaskCompletionMetrics(1L);
    }
    
    @Test
    public void testGenerateCustomReport_WithDateRange() {
        // Setup
        List<String> metrics = List.of("attendance");
        LocalDate startDate = now.minusDays(10);
        LocalDate endDate = now.plusDays(10);
        
        // Execute
        Map<String, Object> report = reportService.generateCustomReport(1L, metrics, startDate, endDate);
        
        // Verify
        assertNotNull(report);
        assertEquals(startDate, report.get("startDate"));
        assertEquals(endDate, report.get("endDate"));
        
        // Verify calls
        verify(metricsService).calculateAttendanceMetrics(eq(1L), eq(startDate), eq(endDate));
    }
}