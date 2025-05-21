// src/test/java/org/frcpm/services/impl/VisualizationServiceTest.java
package org.frcpm.services.impl;

import de.saxsys.mvvmfx.MvvmFX;
import javafx.scene.layout.Pane;
import org.frcpm.charts.GanttChartFactory;
import org.frcpm.charts.TaskChartItem;
import org.frcpm.models.GanttChartData;
import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.models.TeamMember;
import org.frcpm.repositories.specific.MilestoneRepository;
import org.frcpm.repositories.specific.ProjectRepository;
import org.frcpm.repositories.specific.SubsystemRepository;
import org.frcpm.repositories.specific.TaskRepository;
import org.frcpm.services.BaseServiceTest;
import org.frcpm.services.GanttDataService;
import org.frcpm.services.VisualizationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for VisualizationService implementation using TestableVisualizationServiceImpl.
 */
public class VisualizationServiceTest extends BaseServiceTest {
    
    @Mock
    private ProjectRepository projectRepository;
    
    @Mock
    private TaskRepository taskRepository;
    
    @Mock
    private MilestoneRepository milestoneRepository;
    
    @Mock
    private SubsystemRepository subsystemRepository;
    
    @Mock
    private GanttDataService ganttDataService;
    
    @Mock
    private Pane mockPane;
    
    private VisualizationService visualizationService;
    
    private Project testProject;
    private Task testTask;
    private Milestone testMilestone;
    private Subsystem testSubsystem;
    private TeamMember testMember;
    private LocalDate now;
    private Map<String, Object> testGanttData;
    private List<GanttChartData> testTasksGanttData;
    private List<GanttChartData> testMilestonesGanttData;
    
    @Override
    protected void setupTestData() {
        // Initialize dates and objects
        now = LocalDate.now();
        testProject = createTestProject();
        testSubsystem = createTestSubsystem();
        testTask = createTestTask();
        testMilestone = createTestMilestone();
        testMember = createTestMember();
        testTasksGanttData = createTestTasksGanttData();
        testMilestonesGanttData = createTestMilestonesGanttData();
        testGanttData = createTestGanttData();
    }
    
    @Override
    @BeforeEach
    public void setUp() {
        // Execute parent setup first (initializes Mockito annotations)
        super.setUp();
        
        // Configure mock repository responses
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(taskRepository.findByProject(testProject)).thenReturn(List.of(testTask));
        when(milestoneRepository.findByProject(testProject)).thenReturn(List.of(testMilestone));
        
        // Configure GanttDataService mocks
        when(ganttDataService.formatTasksForGantt(eq(1L), any(), any())).thenReturn(testGanttData);
        when(ganttDataService.getGanttDataForDate(eq(1L), any())).thenReturn(testGanttData);
        
        // Create service with injected mocks
        visualizationService = new TestableVisualizationServiceImpl(
            projectRepository,
            taskRepository,
            milestoneRepository,
            subsystemRepository,
            ganttDataService
        );
        
        // Configure MVVMFx dependency injector for comprehensive testing
        MvvmFX.setCustomDependencyInjector(type -> {
            if (type == ProjectRepository.class) return projectRepository;
            if (type == TaskRepository.class) return taskRepository;
            if (type == MilestoneRepository.class) return milestoneRepository;
            if (type == SubsystemRepository.class) return subsystemRepository;
            if (type == GanttDataService.class) return ganttDataService;
            if (type == VisualizationService.class) return visualizationService;
            return null;
        });
    }
    
    @AfterEach
    @Override
    public void tearDown() throws Exception {
        // Clear MVVMFx dependency injector
        MvvmFX.setCustomDependencyInjector(null);
        
        // Call parent tearDown
        super.tearDown();
    }
    
    /**
     * Creates a test project for use in tests.
     * 
     * @return a test project
     */
    private Project createTestProject() {
        Project project = new Project("Test Project", now.minusDays(10), now.plusDays(80), now.plusDays(90));
        project.setId(1L);
        project.setDescription("Test Description");
        return project;
    }
    
    /**
     * Creates a test subsystem for use in tests.
     * 
     * @return a test subsystem
     */
    private Subsystem createTestSubsystem() {
        Subsystem subsystem = new Subsystem("Test Subsystem");
        subsystem.setId(1L);
        return subsystem;
    }
    
    /**
     * Creates a test task for use in tests.
     * 
     * @return a test task
     */
    private Task createTestTask() {
        Task task = new Task("Test Task", testProject, testSubsystem);
        task.setId(1L);
        task.setDescription("Test Description");
        task.setStartDate(now);
        task.setEndDate(now.plusDays(5));
        task.setPriority(Task.Priority.MEDIUM);
        task.setProgress(50);
        return task;
    }
    
    /**
     * Creates a test milestone for use in tests.
     * 
     * @return a test milestone
     */
    private Milestone createTestMilestone() {
        Milestone milestone = new Milestone("Test Milestone", now.plusDays(10), testProject);
        milestone.setId(1L);
        milestone.setDescription("Test Description");
        return milestone;
    }
    
    /**
     * Creates a test team member for use in tests.
     * 
     * @return a test team member
     */
    private TeamMember createTestMember() {
        TeamMember member = new TeamMember("user", "Test", "User", "test@example.com");
        member.setId(1L);
        return member;
    }
    
    /**
     * Creates test task GanttChartData for use in tests.
     * 
     * @return a list of GanttChartData for tasks
     */
    private List<GanttChartData> createTestTasksGanttData() {
        GanttChartData data = new GanttChartData("task_1", "Test Task", now, now.plusDays(5));
        data.setType("task");
        data.setProgress(50);
        data.setColor("#4285f4");
        data.setSubsystem("Test Subsystem");
        return List.of(data);
    }
    
    /**
     * Creates test milestone GanttChartData for use in tests.
     * 
     * @return a list of GanttChartData for milestones
     */
    private List<GanttChartData> createTestMilestonesGanttData() {
        GanttChartData data = new GanttChartData("milestone_1", "Test Milestone", now.plusDays(10), now.plusDays(10));
        data.setType("milestone");
        data.setColor("#6f42c1");
        return List.of(data);
    }
    
    /**
     * Creates test Gantt data for use in tests.
     * 
     * @return a map containing Gantt data
     */
    private Map<String, Object> createTestGanttData() {
        Map<String, Object> data = new HashMap<>();
        data.put("tasks", testTasksGanttData);
        data.put("milestones", testMilestonesGanttData);
        data.put("dependencies", new ArrayList<>());
        data.put("startDate", now.toString());
        data.put("endDate", now.plusDays(90).toString());
        data.put("projectName", "Test Project");
        return data;
    }
    
    @Test
    public void testCreateGanttChartPane() {
        // Setup mock for static GanttChartFactory method
        try (MockedStatic<GanttChartFactory> mockedChartFactory = mockStatic(GanttChartFactory.class)) {
            mockedChartFactory.when(() -> 
                GanttChartFactory.createGanttChart(
                    any(List.class), 
                    any(List.class), 
                    any(LocalDate.class), 
                    any(LocalDate.class), 
                    anyString(), 
                    anyBoolean()
                )
            ).thenReturn(mockPane);
            
            // Execute
            Pane result = visualizationService.createGanttChartPane(
                1L, 
                now, 
                now.plusDays(30), 
                "WEEK", 
                true, 
                true
            );
            
            // Verify
            assertNotNull(result);
            assertSame(mockPane, result);
            
            // Verify service calls
            verify(ganttDataService).formatTasksForGantt(1L, now, now.plusDays(30));
            
// Verify that GanttChartFactory was called with correct parameters
            mockedChartFactory.verify(() -> 
                GanttChartFactory.createGanttChart(
                    any(List.class), 
                    any(List.class), 
                    eq(now), 
                    eq(now.plusDays(30)), 
                    eq("WEEK"), 
                    eq(true)
                )
            );
        }
    }
    
    @Test
    public void testCreateGanttChartPane_NoData() {
        // Setup - GanttDataService returns empty map
        when(ganttDataService.formatTasksForGantt(anyLong(), any(), any())).thenReturn(Collections.emptyMap());
        
        // Execute
        Pane result = visualizationService.createGanttChartPane(1L, now, now.plusDays(30), "WEEK", true, true);
        
        // Verify
        assertNotNull(result);
        assertTrue(result instanceof Pane);
        assertEquals(0, result.getChildren().size());
        
        // Verify service calls
        verify(ganttDataService).formatTasksForGantt(1L, now, now.plusDays(30));
    }
    
    @Test
    public void testCreateGanttChartPane_NullDates() {
        // Setup mock for static GanttChartFactory method
        try (MockedStatic<GanttChartFactory> mockedChartFactory = mockStatic(GanttChartFactory.class)) {
            mockedChartFactory.when(() -> 
                GanttChartFactory.createGanttChart(
                    any(List.class), 
                    any(List.class), 
                    any(LocalDate.class), 
                    any(LocalDate.class), 
                    anyString(), 
                    anyBoolean()
                )
            ).thenReturn(mockPane);
            
            // Execute with null dates - should use defaults
            Pane result = visualizationService.createGanttChartPane(1L, null, null, "MONTH", true, true);
            
            // Verify
            assertNotNull(result);
            assertSame(mockPane, result);
            
            // Verify service calls
            verify(ganttDataService).formatTasksForGantt(1L, null, null);
            
            // Verify GanttChartFactory was called and dates were derived from data
            mockedChartFactory.verify(() -> 
                GanttChartFactory.createGanttChart(
                    any(List.class), 
                    any(List.class), 
                    any(LocalDate.class), 
                    any(LocalDate.class), 
                    eq("MONTH"), 
                    eq(true)
                )
            );
        }
    }
    
    @Test
    public void testCreateDailyChartPane() {
        // Setup mock for static GanttChartFactory method
        try (MockedStatic<GanttChartFactory> mockedChartFactory = mockStatic(GanttChartFactory.class)) {
            mockedChartFactory.when(() -> 
                GanttChartFactory.createDailyChart(
                    any(List.class), 
                    any(List.class), 
                    any(LocalDate.class)
                )
            ).thenReturn(mockPane);
            
            // Execute
            Pane result = visualizationService.createDailyChartPane(1L, now);
            
            // Verify
            assertNotNull(result);
            assertSame(mockPane, result);
            
            // Verify service calls
            verify(ganttDataService).getGanttDataForDate(1L, now);
            
            // Verify GanttChartFactory was called with correct parameters
            mockedChartFactory.verify(() -> 
                GanttChartFactory.createDailyChart(
                    any(List.class), 
                    any(List.class), 
                    eq(now)
                )
            );
        }
    }
    
    @Test
    public void testCreateDailyChartPane_NoData() {
        // Setup - GanttDataService returns empty map
        when(ganttDataService.getGanttDataForDate(anyLong(), any())).thenReturn(Collections.emptyMap());
        
        // Setup mock for static GanttChartFactory method for empty chart
        try (MockedStatic<GanttChartFactory> mockedChartFactory = mockStatic(GanttChartFactory.class)) {
            mockedChartFactory.when(() -> 
                GanttChartFactory.createDailyChart(
                    eq(Collections.emptyList()), 
                    eq(Collections.emptyList()), 
                    any(LocalDate.class)
                )
            ).thenReturn(mockPane);
            
            // Execute
            Pane result = visualizationService.createDailyChartPane(1L, now);
            
            // Verify
            assertNotNull(result);
            assertSame(mockPane, result);
            
            // Verify service calls
            verify(ganttDataService).getGanttDataForDate(1L, now);
            
            // Verify GanttChartFactory was called with empty lists
            mockedChartFactory.verify(() -> 
                GanttChartFactory.createDailyChart(
                    anyList(), 
                    anyList(), 
                    eq(now)
                )
            );
        }
    }
    
    @Test
    public void testCreateDailyChartPane_NullDate() {
        // Setup mock for static GanttChartFactory method
        try (MockedStatic<GanttChartFactory> mockedChartFactory = mockStatic(GanttChartFactory.class)) {
            mockedChartFactory.when(() -> 
                GanttChartFactory.createDailyChart(
                    any(List.class), 
                    any(List.class), 
                    any(LocalDate.class)
                )
            ).thenReturn(mockPane);
            
            // Execute with null date - should use today's date
            Pane result = visualizationService.createDailyChartPane(1L, null);
            
            // Verify
            assertNotNull(result);
            assertSame(mockPane, result);
            
            // Verify service calls
            verify(ganttDataService).getGanttDataForDate(eq(1L), any(LocalDate.class));
            
            // Verify GanttChartFactory was called with today's date
            mockedChartFactory.verify(() -> 
                GanttChartFactory.createDailyChart(
                    any(List.class), 
                    any(List.class), 
                    any(LocalDate.class)
                )
            );
        }
    }
    
    @Test
    public void testConvertGanttChartDataToTaskChartItems() {
        // Get an instance of the implementation to test the conversion method
        TestableVisualizationServiceImpl serviceImpl = (TestableVisualizationServiceImpl) visualizationService;
        
        // Execute
        List<TaskChartItem> result = serviceImpl.convertGanttChartDataToTaskChartItems(testTasksGanttData);
        
        // Verify
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Task", result.get(0).getTitle());
        assertEquals(now, result.get(0).getStartDate());
        assertEquals(now.plusDays(5), result.get(0).getEndDate());
        assertEquals(50, result.get(0).getProgress());
        assertEquals("#4285f4", result.get(0).getColor());
        assertEquals("Test Subsystem", result.get(0).getSubsystem());
    }
    
    @Test
    public void testConvertGanttChartDataToTaskChartItems_NullInput() {
        // Get an instance of the implementation to test the conversion method
        TestableVisualizationServiceImpl serviceImpl = (TestableVisualizationServiceImpl) visualizationService;
        
        // Execute with null input
        List<TaskChartItem> result = serviceImpl.convertGanttChartDataToTaskChartItems(null);
        
        // Verify
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    public void testConvertGanttChartDataToTaskChartItems_EmptyInput() {
        // Get an instance of the implementation to test the conversion method
        TestableVisualizationServiceImpl serviceImpl = (TestableVisualizationServiceImpl) visualizationService;
        
        // Execute with empty input
        List<TaskChartItem> result = serviceImpl.convertGanttChartDataToTaskChartItems(Collections.emptyList());
        
        // Verify
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    public void testGetProjectCompletionData() {
        // Setup - Configure task responses for completion calculation
        Task completedTask = new Task("Completed Task", testProject, testSubsystem);
        completedTask.setCompleted(true);
        
        List<Task> tasks = List.of(testTask, completedTask);
        when(taskRepository.findByProject(testProject)).thenReturn(tasks);
        
        // Execute
        Map<String, Object> result = visualizationService.getProjectCompletionData(1L);
        
        // Verify
        assertNotNull(result);
        assertEquals(50, result.get("overall")); // 1 out of 2 tasks completed = 50%
        
        @SuppressWarnings("unchecked")
        Map<String, Integer> subsystemCompletion = (Map<String, Integer>) result.get("bySubsystem");
        assertNotNull(subsystemCompletion);
        assertEquals(50, subsystemCompletion.get("Test Subsystem")); // 1 out of 2 tasks for the subsystem
        
        // Verify repository calls
        verify(projectRepository).findById(1L);
        verify(taskRepository).findByProject(testProject);
    }
    
    @Test
    public void testGetProjectCompletionData_ProjectNotFound() {
        // Setup
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());
        
        // Execute
        Map<String, Object> result = visualizationService.getProjectCompletionData(99L);
        
        // Verify
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        // Verify repository calls
        verify(projectRepository).findById(99L);
        verify(taskRepository, never()).findByProject(any(Project.class));
    }
    
    @Test
    public void testGetTaskStatusSummary() {
        // Setup - Configure tasks with different statuses
        Task notStartedTask = new Task("Not Started Task", testProject, testSubsystem);
        notStartedTask.setProgress(0);
        
        Task inProgressTask = new Task("In Progress Task", testProject, testSubsystem);
        inProgressTask.setProgress(50);
        
        Task completedTask = new Task("Completed Task", testProject, testSubsystem);
        completedTask.setProgress(100);
        
        List<Task> tasks = List.of(notStartedTask, inProgressTask, completedTask);
        when(taskRepository.findByProject(testProject)).thenReturn(tasks);
        
        // Execute
        Map<String, Integer> result = visualizationService.getTaskStatusSummary(1L);
        
        // Verify
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(1, result.get("notStarted"));
        assertEquals(1, result.get("inProgress"));
        assertEquals(1, result.get("completed"));
        
        // Verify repository calls
        verify(projectRepository).findById(1L);
        verify(taskRepository).findByProject(testProject);
    }
    
    @Test
    public void testGetTaskStatusSummary_ProjectNotFound() {
        // Setup
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());
        
        // Execute
        Map<String, Integer> result = visualizationService.getTaskStatusSummary(99L);
        
        // Verify
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        // Verify repository calls
        verify(projectRepository).findById(99L);
        verify(taskRepository, never()).findByProject(any(Project.class));
    }
    
    @Test
    public void testGetUpcomingDeadlines() {
        // Setup - Configure upcoming tasks and milestones
        Task upcomingTask = new Task("Upcoming Task", testProject, testSubsystem);
        upcomingTask.setStartDate(now);
        upcomingTask.setEndDate(now.plusDays(3));
        upcomingTask.setProgress(50);
        
        List<Task> tasks = List.of(upcomingTask);
        when(taskRepository.findByProject(testProject)).thenReturn(tasks);
        
        Milestone upcomingMilestone = new Milestone("Upcoming Milestone", now.plusDays(5), testProject);
        upcomingMilestone.setId(2L);
        
        List<Milestone> milestones = List.of(upcomingMilestone);
        when(milestoneRepository.findByProject(testProject)).thenReturn(milestones);
        
        // Execute with 7 days ahead
        List<Map<String, Object>> result = visualizationService.getUpcomingDeadlines(1L, 7);
        
        // Verify
        assertNotNull(result);
        assertEquals(2, result.size()); // 1 task and 1 milestone
        
        // First item should be the task (earlier deadline)
        assertEquals("task", result.get(0).get("type"));
        assertEquals("Upcoming Task", result.get(0).get("title"));
        assertEquals(now.plusDays(3), result.get(0).get("date"));
        
        // Second item should be the milestone
        assertEquals("milestone", result.get(1).get("type"));
        assertEquals("Upcoming Milestone", result.get(1).get("title"));
        assertEquals(now.plusDays(5), result.get(1).get("date"));
        
        // Verify repository calls
        verify(projectRepository).findById(1L);
        verify(taskRepository).findByProject(testProject);
        verify(milestoneRepository).findByProject(testProject);
    }
    
    @Test
    public void testGetUpcomingDeadlines_NoDeadlines() {
        // Setup - No upcoming deadlines
        when(taskRepository.findByProject(testProject)).thenReturn(Collections.emptyList());
        when(milestoneRepository.findByProject(testProject)).thenReturn(Collections.emptyList());
        
        // Execute
        List<Map<String, Object>> result = visualizationService.getUpcomingDeadlines(1L, 7);
        
        // Verify
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        // Verify repository calls
        verify(projectRepository).findById(1L);
        verify(taskRepository).findByProject(testProject);
        verify(milestoneRepository).findByProject(testProject);
    }
    
    @Test
    public void testGetSubsystemProgress() {
        // Setup - Configure tasks for different subsystems
        Subsystem subsystem1 = new Subsystem("Subsystem 1");
        subsystem1.setId(1L);
        
        Subsystem subsystem2 = new Subsystem("Subsystem 2");
        subsystem2.setId(2L);
        
        Task task1 = new Task("Task 1", testProject, subsystem1);
        task1.setProgress(25);
        
        Task task2 = new Task("Task 2", testProject, subsystem1);
        task2.setProgress(75);
        
        Task task3 = new Task("Task 3", testProject, subsystem2);
        task3.setProgress(100);
        
        List<Task> tasks = List.of(task1, task2, task3);
        when(taskRepository.findByProject(testProject)).thenReturn(tasks);
        
        // Execute
        Map<String, Double> result = visualizationService.getSubsystemProgress(1L);
        
        // Verify
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(50.0, result.get("Subsystem 1")); // Average of 25% and 75%
        assertEquals(100.0, result.get("Subsystem 2")); // Just 100%
        
        // Verify repository calls
        verify(projectRepository).findById(1L);
        verify(taskRepository).findByProject(testProject);
    }
    
    @Test
    public void testGetAtRiskTasks() {
        // Setup - Configure at-risk tasks
        
        // Past due date task
        Task overdueTak = new Task("Overdue Task", testProject, testSubsystem);
        overdueTak.setStartDate(now.minusDays(10));
        overdueTak.setEndDate(now.minusDays(2));
        overdueTak.setProgress(50);
        
        // Behind schedule task
        Task behindScheduleTask = new Task("Behind Schedule Task", testProject, testSubsystem);
        behindScheduleTask.setStartDate(now.minusDays(10));
        behindScheduleTask.setEndDate(now.plusDays(10));
        behindScheduleTask.setProgress(20); // Should be around 50% based on time
        
        // On-track task (not at risk)
        Task onTrackTask = new Task("On Track Task", testProject, testSubsystem);
        onTrackTask.setStartDate(now.minusDays(5));
        onTrackTask.setEndDate(now.plusDays(5));
        onTrackTask.setProgress(50);
        
        List<Task> tasks = List.of(overdueTak, behindScheduleTask, onTrackTask);
        when(taskRepository.findByProject(testProject)).thenReturn(tasks);
        
        // Execute
        List<Map<String, Object>> result = visualizationService.getAtRiskTasks(1L);
        
        // Verify
        assertNotNull(result);
        assertEquals(2, result.size()); // 2 at-risk tasks
        
        // First item should be the overdue task
        Map<String, Object> overdueResult = result.stream()
            .filter(m -> "Overdue Task".equals(m.get("title")))
            .findFirst()
            .orElse(null);
        assertNotNull(overdueResult);
        assertEquals("Past due date", overdueResult.get("reason"));
        
        // Second item should be the behind schedule task
        Map<String, Object> behindScheduleResult = result.stream()
            .filter(m -> "Behind Schedule Task".equals(m.get("title")))
            .findFirst()
            .orElse(null);
        assertNotNull(behindScheduleResult);
        assertEquals("Behind schedule", behindScheduleResult.get("reason"));
        
        // Verify repository calls
        verify(projectRepository).findById(1L);
        verify(taskRepository).findByProject(testProject);
    }
    
    @Test
    public void testGenerateSvgExport() {
        // Execute
        String result = visualizationService.generateSvgExport(testGanttData, "gantt");
        
        // Verify
        assertNotNull(result);
        assertTrue(result.startsWith("<?xml"));
        assertTrue(result.contains("<svg"));
        assertTrue(result.contains("Chart Export - gantt"));
        
        // Should contain basic SVG structure
        assertTrue(result.contains("<rect"));
        assertTrue(result.contains("<text"));
        assertTrue(result.contains("</svg>"));
    }
    
    @Test
    public void testGeneratePdfReport() {
        // Execute
        byte[] result = visualizationService.generatePdfReport(1L, "gantt");
        
        // Verify
        assertNotNull(result);
        assertEquals(0, result.length); // Current implementation returns empty byte array
        
        // Verify repository calls
        verify(projectRepository).findById(1L);
    }
}