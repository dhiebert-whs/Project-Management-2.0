// src/test/java/org/frcpm/services/impl/VisualizationServiceTest.java

package org.frcpm.services.impl;

import org.frcpm.models.GanttChartData;
import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.repositories.spring.MilestoneRepository;
import org.frcpm.repositories.spring.ProjectRepository;
import org.frcpm.repositories.spring.TaskRepository;
import org.frcpm.services.GanttDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for VisualizationService implementation using proven AttendanceServiceTest pattern.
 * Fixed to follow the systematic pattern that has been successful across 11 other service tests.
 */
@ExtendWith(MockitoExtension.class)
class VisualizationServiceTest {
    
    @Mock
    private ProjectRepository projectRepository;
    
    @Mock
    private TaskRepository taskRepository;
    
    @Mock
    private MilestoneRepository milestoneRepository;
    
    @Mock
    private GanttDataService ganttDataService;
    
    private VisualizationServiceImpl visualizationService;
    
    private Project testProject;
    private Task testTask;
    private Task testCompletedTask;
    private Task testOverdueTask;
    private Milestone testMilestone;
    private Subsystem testSubsystem;
    private LocalDate now;
    private Map<String, Object> testGanttData;
    
    @BeforeEach
    void setUp() {
        // Create test objects ONLY - no mock stubbing here
        now = LocalDate.now();
        testProject = createTestProject();
        testSubsystem = createTestSubsystem();
        testTask = createTestTask();
        testCompletedTask = createTestCompletedTask();
        testOverdueTask = createTestOverdueTask();
        testMilestone = createTestMilestone();
        testGanttData = createTestGanttData();
        
        // Create service with injected mocks
        visualizationService = new VisualizationServiceImpl(
            projectRepository, taskRepository, milestoneRepository, ganttDataService
        );
    }
    
    /**
     * Creates a test project for use in tests.
     */
    private Project createTestProject() {
        Project project = new Project("Test Project", now.minusDays(10), now.plusDays(80), now.plusDays(90));
        project.setId(1L);
        project.setDescription("Test Description");
        return project;
    }
    
    /**
     * Creates a test subsystem for use in tests.
     */
    private Subsystem createTestSubsystem() {
        Subsystem subsystem = new Subsystem("Test Subsystem");
        subsystem.setId(1L);
        return subsystem;
    }
    
    /**
     * Creates a test task for use in tests.
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
     * Creates a completed test task for use in tests.
     */
    private Task createTestCompletedTask() {
        Task task = new Task("Completed Task", testProject, testSubsystem);
        task.setId(2L);
        task.setDescription("Completed Description");
        task.setStartDate(now.minusDays(5));
        task.setEndDate(now.minusDays(1));
        task.setPriority(Task.Priority.HIGH);
        task.setProgress(100);
        return task;
    }
    
    /**
     * Creates an overdue test task for use in tests.
     */
    private Task createTestOverdueTask() {
        Task task = new Task("Overdue Task", testProject, testSubsystem);
        task.setId(3L);
        task.setDescription("Overdue Description");
        task.setStartDate(now.minusDays(10));
        task.setEndDate(now.minusDays(2));
        task.setPriority(Task.Priority.HIGH);
        task.setProgress(30);
        return task;
    }
    
    /**
     * Creates a test milestone for use in tests.
     */
    private Milestone createTestMilestone() {
        Milestone milestone = new Milestone("Test Milestone", now.plusDays(10), testProject);
        milestone.setId(1L);
        milestone.setDescription("Test Description");
        // Milestone.isPassed() is automatically calculated based on date vs LocalDate.now()
        // Since we set the date to now.plusDays(10), isPassed() will return false
        return milestone;
    }
    
    /**
     * Creates test Gantt data for use in tests.
     */
    private Map<String, Object> createTestGanttData() {
        Map<String, Object> data = new HashMap<>();
        
        List<GanttChartData> tasks = new ArrayList<>();
        GanttChartData taskData = new GanttChartData("task_1", "Test Task", now, now.plusDays(5));
        taskData.setType("task");
        taskData.setProgress(50);
        taskData.setColor("#4285f4");
        tasks.add(taskData);
        
        List<GanttChartData> milestones = new ArrayList<>();
        GanttChartData milestoneData = new GanttChartData("milestone_1", "Test Milestone", now.plusDays(10), now.plusDays(10));
        milestoneData.setType("milestone");
        milestoneData.setColor("#6f42c1");
        milestones.add(milestoneData);
        
        data.put("tasks", tasks);
        data.put("milestones", milestones);
        data.put("dependencies", new ArrayList<>());
        data.put("startDate", now.toString());
        data.put("endDate", now.plusDays(90).toString());
        data.put("projectName", "Test Project");
        
        return data;
    }
    
    @Test
    void testCreateGanttChartPane() {
        // Setup - Only stub what THIS test needs
        when(ganttDataService.formatTasksForGantt(eq(1L), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(testGanttData);
        
        LocalDate startDate = now;
        LocalDate endDate = now.plusDays(30);
        
        // Execute
        Map<String, Object> result = visualizationService.createGanttChartPane(
            1L, startDate, endDate, "WEEK", true, true
        );
        
        // Verify
        assertNotNull(result);
        assertTrue(result.containsKey("viewMode"));
        assertEquals("WEEK", result.get("viewMode"));
        assertTrue(result.containsKey("showMilestones"));
        assertTrue((Boolean) result.get("showMilestones"));
        assertTrue(result.containsKey("showDependencies"));
        assertTrue((Boolean) result.get("showDependencies"));
        assertEquals("gantt", result.get("chartType"));
        
        // Verify underlying data is included
        assertTrue(result.containsKey("tasks"));
        assertTrue(result.containsKey("milestones"));
        
        // Verify service calls
        verify(ganttDataService).formatTasksForGantt(1L, startDate, endDate);
    }
    
    @Test
    void testCreateGanttChartPane_EmptyData() {
        // Setup - GanttDataService returns empty map
        when(ganttDataService.formatTasksForGantt(anyLong(), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(Collections.emptyMap());
        
        // Execute
        Map<String, Object> result = visualizationService.createGanttChartPane(
            1L, now, now.plusDays(30), "WEEK", true, true
        );
        
        // Verify
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        // Verify service calls
        verify(ganttDataService).formatTasksForGantt(eq(1L), any(LocalDate.class), any(LocalDate.class));
    }
    
    @Test
    void testCreateGanttChartPane_NullData() {
        // Setup - GanttDataService returns null
        when(ganttDataService.formatTasksForGantt(anyLong(), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(null);
        
        // Execute
        Map<String, Object> result = visualizationService.createGanttChartPane(
            1L, now, now.plusDays(30), "MONTH", false, false
        );
        
        // Verify
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        // Verify service calls
        verify(ganttDataService).formatTasksForGantt(eq(1L), any(LocalDate.class), any(LocalDate.class));
    }
    
    @Test
    void testCreateDailyChartPane() {
        // Setup - Only stub what THIS test needs
        when(ganttDataService.getGanttDataForDate(eq(1L), eq(now)))
            .thenReturn(testGanttData);
        
        // Execute
        Map<String, Object> result = visualizationService.createDailyChartPane(1L, now);
        
        // Verify
        assertNotNull(result);
        assertTrue(result.containsKey("date"));
        assertEquals(now.toString(), result.get("date"));
        assertEquals("daily", result.get("chartType"));
        assertTrue(result.containsKey("tasks"));
        assertTrue(result.containsKey("milestones"));
        
        // Verify service calls
        verify(ganttDataService).getGanttDataForDate(1L, now);
    }
    
    @Test
    void testCreateDailyChartPane_NullDate() {
        // Setup - Should use current date when null
        when(ganttDataService.getGanttDataForDate(eq(1L), any(LocalDate.class)))
            .thenReturn(testGanttData);
        
        // Execute
        Map<String, Object> result = visualizationService.createDailyChartPane(1L, null);
        
        // Verify
        assertNotNull(result);
        assertTrue(result.containsKey("date"));
        assertEquals("daily", result.get("chartType"));
        
        // Verify service calls - should use current date
        verify(ganttDataService).getGanttDataForDate(eq(1L), any(LocalDate.class));
    }
    
    @Test
    void testCreateDailyChartPane_EmptyData() {
        // Setup - GanttDataService returns empty map
        when(ganttDataService.getGanttDataForDate(anyLong(), any(LocalDate.class)))
            .thenReturn(Collections.emptyMap());
        
        // Execute
        Map<String, Object> result = visualizationService.createDailyChartPane(1L, now);
        
        // Verify
        assertNotNull(result);
        assertEquals(now.toString(), result.get("date"));
        assertEquals("daily", result.get("chartType"));
        assertTrue(result.containsKey("tasks"));
        assertTrue(result.containsKey("milestones"));
        
        // Should have empty lists for tasks and milestones
        @SuppressWarnings("unchecked")
        List<Object> tasks = (List<Object>) result.get("tasks");
        assertTrue(tasks.isEmpty());
        
        @SuppressWarnings("unchecked")
        List<Object> milestones = (List<Object>) result.get("milestones");
        assertTrue(milestones.isEmpty());
        
        // Verify service calls
        verify(ganttDataService).getGanttDataForDate(eq(1L), eq(now));
    }
    
    @Test
    void testGetProjectCompletionData() {
        // Setup - Only stub what THIS test needs
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(taskRepository.findByProject(testProject)).thenReturn(List.of(testTask, testCompletedTask));
        
        // Execute
        Map<String, Object> result = visualizationService.getProjectCompletionData(1L);
        
        // Verify
        assertNotNull(result);
        assertTrue(result.containsKey("overall"));
        assertTrue(result.containsKey("bySubsystem"));
        assertTrue(result.containsKey("timeElapsed"));
        
        // Should have 50% completion (1 completed out of 2 tasks)
        assertEquals(50, result.get("overall"));
        
        // Verify subsystem completion
        @SuppressWarnings("unchecked")
        Map<String, Integer> subsystemCompletion = (Map<String, Integer>) result.get("bySubsystem");
        assertNotNull(subsystemCompletion);
        assertTrue(subsystemCompletion.containsKey("Test Subsystem"));
        
        // Verify repository calls
        verify(projectRepository).findById(1L);
        verify(taskRepository).findByProject(testProject);
    }
    
    @Test
    void testGetProjectCompletionData_ProjectNotFound() {
        // Setup - Project not found
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute
        Map<String, Object> result = visualizationService.getProjectCompletionData(999L);
        
        // Verify
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        // Verify repository calls
        verify(projectRepository).findById(999L);
        verify(taskRepository, never()).findByProject(any());
    }
    
    @Test
    void testGetTaskStatusSummary() {
        // Setup - Only stub what THIS test needs
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(taskRepository.findByProject(testProject)).thenReturn(List.of(testTask, testCompletedTask, testOverdueTask));
        
        // Execute
        Map<String, Integer> result = visualizationService.getTaskStatusSummary(1L);
        
        // Verify
        assertNotNull(result);
        assertTrue(result.containsKey("notStarted"));
        assertTrue(result.containsKey("inProgress"));
        assertTrue(result.containsKey("completed"));
        
        // Should have 0 not started, 2 in progress (50% and 30%), 1 completed (100%)
        assertEquals(0, result.get("notStarted"));
        assertEquals(2, result.get("inProgress"));
        assertEquals(1, result.get("completed"));
        
        // Verify repository calls
        verify(projectRepository).findById(1L);
        verify(taskRepository).findByProject(testProject);
    }
    
    @Test
    void testGetTaskStatusSummary_ProjectNotFound() {
        // Setup - Project not found
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute
        Map<String, Integer> result = visualizationService.getTaskStatusSummary(999L);
        
        // Verify
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        // Verify repository calls
        verify(projectRepository).findById(999L);
        verify(taskRepository, never()).findByProject(any());
    }
    
    @Test
    void testGetUpcomingDeadlines() {
        // Setup - Only stub what THIS test needs  
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(taskRepository.findByProject(testProject)).thenReturn(List.of(testTask, testCompletedTask));
        when(milestoneRepository.findByProject(testProject)).thenReturn(List.of(testMilestone));
        
        // Execute
        List<Map<String, Object>> result = visualizationService.getUpcomingDeadlines(1L, 14);
        
        // Verify
        assertNotNull(result);
        
        // Should include the upcoming task (testTask ends in 5 days, within 14 day window)
        // Should include the upcoming milestone (testMilestone is in 10 days, within 14 day window)
        // testCompletedTask is completed so should be filtered out
        
        boolean hasTask = result.stream().anyMatch(item -> 
            "task".equals(item.get("type")) && "Test Task".equals(item.get("title"))
        );
        boolean hasMilestone = result.stream().anyMatch(item -> 
            "milestone".equals(item.get("type")) && "Test Milestone".equals(item.get("title"))
        );
        
        assertTrue(hasTask, "Should include upcoming test task");
        assertTrue(hasMilestone, "Should include upcoming test milestone");
        
        // Verify repository calls were made correctly
        verify(projectRepository).findById(1L);
        verify(taskRepository).findByProject(testProject);
        verify(milestoneRepository).findByProject(testProject);
        
        // Check result structure
        for (Map<String, Object> item : result) {
            assertTrue(item.containsKey("type"));
            assertTrue(item.containsKey("title"));
            assertTrue(item.containsKey("date"));
            
            String type = (String) item.get("type");
            assertTrue("task".equals(type) || "milestone".equals(type));
        }
    }
    
    @Test
    void testGetUpcomingDeadlines_ProjectNotFound() {
        // Setup - Project not found
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute
        List<Map<String, Object>> result = visualizationService.getUpcomingDeadlines(999L, 7);
        
        // Verify
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        // Verify repository calls
        verify(projectRepository).findById(999L);
        verify(taskRepository, never()).findByProject(any());
        verify(milestoneRepository, never()).findByProject(any());
    }
    
    @Test
    void testGetSubsystemProgress() {
        // Setup - Only stub what THIS test needs
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(taskRepository.findByProject(testProject)).thenReturn(List.of(testTask, testCompletedTask));
        
        // Execute
        Map<String, Double> result = visualizationService.getSubsystemProgress(1L);
        
        // Verify
        assertNotNull(result);
        assertTrue(result.containsKey("Test Subsystem"));
        
        // Should have average progress of 75.0 (50 + 100) / 2
        assertEquals(75.0, result.get("Test Subsystem"));
        
        // Verify repository calls
        verify(projectRepository).findById(1L);
        verify(taskRepository).findByProject(testProject);
    }
    
    @Test
    void testGetSubsystemProgress_ProjectNotFound() {
        // Setup - Project not found
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute
        Map<String, Double> result = visualizationService.getSubsystemProgress(999L);
        
        // Verify
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        // Verify repository calls
        verify(projectRepository).findById(999L);
        verify(taskRepository, never()).findByProject(any());
    }
    
    @Test
    void testGetAtRiskTasks() {
        // Setup - Only stub what THIS test needs
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(taskRepository.findByProject(testProject)).thenReturn(List.of(testTask, testOverdueTask));
        
        // Execute
        List<Map<String, Object>> result = visualizationService.getAtRiskTasks(1L);
        
        // Verify
        assertNotNull(result);
        assertFalse(result.isEmpty());
        
        // Should include the overdue task
        boolean hasOverdueTask = result.stream()
            .anyMatch(item -> "Overdue Task".equals(item.get("title")));
        assertTrue(hasOverdueTask);
        
        // Find the overdue task and verify its properties
        Map<String, Object> overdueTask = result.stream()
            .filter(item -> "Overdue Task".equals(item.get("title")))
            .findFirst()
            .orElse(null);
        
        assertNotNull(overdueTask);
        assertTrue(overdueTask.containsKey("reason"));
        assertTrue(overdueTask.containsKey("progress"));
        assertEquals(30, overdueTask.get("progress"));
        
        // Verify repository calls
        verify(projectRepository).findById(1L);
        verify(taskRepository).findByProject(testProject);
    }
    
    @Test
    void testGetAtRiskTasks_ProjectNotFound() {
        // Setup - Project not found
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute
        List<Map<String, Object>> result = visualizationService.getAtRiskTasks(999L);
        
        // Verify
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        // Verify repository calls
        verify(projectRepository).findById(999L);
        verify(taskRepository, never()).findByProject(any());
    }
    
    @Test
    void testGenerateSvgExport() {
        // Execute
        String result = visualizationService.generateSvgExport(testGanttData, "gantt");
        
        // Verify
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.startsWith("<?xml"));
        assertTrue(result.contains("<svg"));
        assertTrue(result.contains("Chart Export - gantt"));
        assertTrue(result.contains("</svg>"));
        
        // Should contain basic SVG structure
        assertTrue(result.contains("<rect"));
        assertTrue(result.contains("<text"));
    }
    
    @Test
    void testGenerateSvgExport_EmptyData() {
        // Execute with empty data
        String result = visualizationService.generateSvgExport(Collections.emptyMap(), "daily");
        
        // Verify
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.startsWith("<?xml"));
        assertTrue(result.contains("Chart Export - daily"));
    }
    
    @Test
    void testGeneratePdfReport() {
        // Setup - Only stub what THIS test needs
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        
        // Execute
        byte[] result = visualizationService.generatePdfReport(1L, "gantt");
        
        // Verify
        assertNotNull(result);
        // Current implementation returns empty byte array
        assertEquals(0, result.length);
        
        // Verify repository calls
        verify(projectRepository).findById(1L);
    }
    
    @Test
    void testGeneratePdfReport_ProjectNotFound() {
        // Setup - Project not found
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute
        byte[] result = visualizationService.generatePdfReport(999L, "daily");
        
        // Verify
        assertNotNull(result);
        assertEquals(0, result.length);
        
        // Verify repository calls
        verify(projectRepository).findById(999L);
    }
    
    @Test
    void testServiceConstructorDependencies() {
        // Verify service was constructed correctly with all dependencies
        assertNotNull(visualizationService);
        
        // Test that service handles edge cases gracefully
        when(ganttDataService.formatTasksForGantt(anyLong(), any(), any()))
            .thenThrow(new RuntimeException("Test exception"));
        
        // Should not throw exception, should return empty map
        Map<String, Object> result = visualizationService.createGanttChartPane(
            1L, now, now.plusDays(30), "WEEK", true, true
        );
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}