// src/test/java/org/frcpm/services/impl/GanttDataServiceTest.java

package org.frcpm.services.impl;

import org.frcpm.models.GanttChartData;
import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.repositories.spring.MilestoneRepository;
import org.frcpm.repositories.spring.ProjectRepository;
import org.frcpm.repositories.spring.TaskRepository;
import org.frcpm.services.GanttChartTransformationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for GanttDataService implementation using Spring Boot testing patterns.
 * FIXED: Applied proven pattern from AttendanceServiceTest success template.
 */
@ExtendWith(MockitoExtension.class)
class GanttDataServiceTest {
    
    @Mock
    private ProjectRepository projectRepository;
    
    @Mock
    private TaskRepository taskRepository;
    
    @Mock
    private MilestoneRepository milestoneRepository;
    
    @Mock
    private GanttChartTransformationService transformationService;
    
    private GanttDataServiceImpl ganttDataService; // ✅ FIXED: Use implementation class
    
    private Project testProject;
    private Task testTask;
    private Milestone testMilestone;
    private LocalDate now;
    private List<GanttChartData> testTasksGanttData;
    private List<GanttChartData> testMilestonesGanttData;
    private List<Map<String, Object>> testDependencyData;
    
    @BeforeEach
    void setUp() {
        // ✅ FIXED: Create test objects ONLY - NO MOCK STUBBING HERE
        now = LocalDate.now();
        testProject = createTestProject();
        testTask = createTestTask();
        testMilestone = createTestMilestone();
        testTasksGanttData = createTestTasksGanttData();
        testMilestonesGanttData = createTestMilestonesGanttData();
        testDependencyData = createTestDependencyData();
        
        // Create service with injected mocks
        ganttDataService = new GanttDataServiceImpl(
            projectRepository,
            taskRepository,
            milestoneRepository,
            transformationService
        );
        
        // ✅ FIXED: NO mock stubbing in setUp() - move to individual test methods
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
     * Creates a test task for use in tests.
     */
    private Task createTestTask() {
        Task task = new Task("Test Task", testProject, null);
        task.setId(1L);
        task.setDescription("Test Description");
        task.setStartDate(now);
        task.setEndDate(now.plusDays(5));
        task.setPriority(Task.Priority.MEDIUM);
        return task;
    }
    
    /**
     * Creates a test milestone for use in tests.
     */
    private Milestone createTestMilestone() {
        Milestone milestone = new Milestone("Test Milestone", now.plusDays(10), testProject);
        milestone.setId(1L);
        milestone.setDescription("Test Description");
        return milestone;
    }
    
    /**
     * Creates test task GanttChartData for use in tests.
     */
    private List<GanttChartData> createTestTasksGanttData() {
        GanttChartData data = new GanttChartData("task_1", "Test Task", now, now.plusDays(5));
        data.setType("task");
        data.setProgress(50);
        data.setColor("#4285f4");
        return List.of(data);
    }
    
    /**
     * Creates test milestone GanttChartData for use in tests.
     */
    private List<GanttChartData> createTestMilestonesGanttData() {
        GanttChartData data = new GanttChartData("milestone_1", "Test Milestone", now.plusDays(10), now.plusDays(10));
        data.setType("milestone");
        data.setColor("#6f42c1");
        return List.of(data);
    }
    
    /**
     * Creates test dependency data for use in tests.
     */
    private List<Map<String, Object>> createTestDependencyData() {
        Map<String, Object> dependency = new HashMap<>();
        dependency.put("source", "task_2");
        dependency.put("target", "task_1");
        dependency.put("type", "finish-to-start");
        return List.of(dependency);
    }
    
    @Test
    void testFormatTasksForGantt() {
        // Setup - Only stub what THIS test needs
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(taskRepository.findByProject(testProject)).thenReturn(List.of(testTask));
        when(milestoneRepository.findByProject(testProject)).thenReturn(List.of(testMilestone));
        when(transformationService.transformTasksToChartData(anyList())).thenReturn(testTasksGanttData);
        when(transformationService.transformMilestonesToChartData(anyList())).thenReturn(testMilestonesGanttData);
        when(transformationService.createDependencyData(anyList())).thenReturn(testDependencyData);
        
        // Execute
        Map<String, Object> result = ganttDataService.formatTasksForGantt(1L, null, null);
        
        // Verify
        assertNotNull(result);
        assertEquals("Test Project", result.get("projectName"));
        
        @SuppressWarnings("unchecked")
        List<GanttChartData> tasks = (List<GanttChartData>) result.get("tasks");
        assertNotNull(tasks);
        assertEquals(1, tasks.size());
        assertEquals("Test Task", tasks.get(0).getTitle());
        
        @SuppressWarnings("unchecked")
        List<GanttChartData> milestones = (List<GanttChartData>) result.get("milestones");
        assertNotNull(milestones);
        assertEquals(1, milestones.size());
        assertEquals("Test Milestone", milestones.get(0).getTitle());
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> dependencies = (List<Map<String, Object>>) result.get("dependencies");
        assertNotNull(dependencies);
        assertEquals(1, dependencies.size());
        assertEquals("task_2", dependencies.get(0).get("source"));
        
        // Verify repository and service calls
        verify(projectRepository).findById(1L);
        verify(taskRepository).findByProject(testProject);
        verify(milestoneRepository).findByProject(testProject);
        verify(transformationService).transformTasksToChartData(List.of(testTask));
        verify(transformationService).transformMilestonesToChartData(List.of(testMilestone));
        verify(transformationService).createDependencyData(List.of(testTask));
    }
    
    @Test
    void testFormatTasksForGantt_ProjectNotFound() {
        // Setup - Entity doesn't exist
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute
        Map<String, Object> result = ganttDataService.formatTasksForGantt(999L, null, null);
        
        // Verify
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        // Verify repository calls
        verify(projectRepository).findById(999L);
        verify(taskRepository, never()).findByProject(any(Project.class));
        verify(milestoneRepository, never()).findByProject(any(Project.class));
    }
    
    @Test
    void testFormatTasksForGantt_NullProjectId() {
        // Execute and verify exception
        assertThrows(IllegalArgumentException.class, () -> {
            ganttDataService.formatTasksForGantt(null, null, null);
        });
        
        // Verify repository was never called
        verify(projectRepository, never()).findById(any());
        verify(taskRepository, never()).findByProject(any());
        verify(milestoneRepository, never()).findByProject(any());
    }
    
    @Test
    void testFormatTasksForGantt_WithDateRange() {
        // Setup specific date range
        LocalDate startDate = now.minusDays(5);
        LocalDate endDate = now.plusDays(15);
        
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(taskRepository.findByProject(testProject)).thenReturn(List.of(testTask));
        when(milestoneRepository.findByProject(testProject)).thenReturn(List.of(testMilestone));
        when(transformationService.transformTasksToChartData(anyList())).thenReturn(testTasksGanttData);
        when(transformationService.transformMilestonesToChartData(anyList())).thenReturn(testMilestonesGanttData);
        when(transformationService.createDependencyData(anyList())).thenReturn(testDependencyData);
        
        // Execute
        Map<String, Object> result = ganttDataService.formatTasksForGantt(1L, startDate, endDate);
        
        // Verify
        assertNotNull(result);
        assertEquals(startDate.toString(), result.get("startDate"));
        assertEquals(endDate.toString(), result.get("endDate"));
        
        // Verify repository and service calls
        verify(projectRepository).findById(1L);
        verify(taskRepository).findByProject(testProject);
        verify(milestoneRepository).findByProject(testProject);
    }
    
    @Test
    void testApplyFiltersToGanttData() {
        // Setup
        Map<String, Object> ganttData = new HashMap<>();
        ganttData.put("tasks", testTasksGanttData);
        ganttData.put("milestones", testMilestonesGanttData);
        
        Map<String, Object> filterCriteria = new HashMap<>();
        filterCriteria.put("filterType", "CRITICAL_PATH");
        filterCriteria.put("subsystem", "Drivetrain");
        filterCriteria.put("startDate", now.toString());
        filterCriteria.put("endDate", now.plusDays(20).toString());
        
        // Create filtered tasks and milestones
        List<GanttChartData> filteredTasks = new ArrayList<>(testTasksGanttData);
        List<GanttChartData> filteredMilestones = new ArrayList<>(testMilestonesGanttData);
        
        // Configure the mock to return our filtered lists
        when(transformationService.filterChartData(
            eq(testTasksGanttData), 
            eq("CRITICAL_PATH"), 
            isNull(), 
            eq("Drivetrain"), 
            eq(now), 
            eq(now.plusDays(20))
        )).thenReturn(filteredTasks);
        
        when(transformationService.filterChartData(
            eq(testMilestonesGanttData), 
            isNull(), 
            isNull(), 
            isNull(), 
            eq(now), 
            eq(now.plusDays(20))
        )).thenReturn(filteredMilestones);
        
        // Execute
        Map<String, Object> result = ganttDataService.applyFiltersToGanttData(ganttData, filterCriteria);
        
        // Verify
        assertNotNull(result);
        
        @SuppressWarnings("unchecked")
        List<GanttChartData> resultTasks = (List<GanttChartData>) result.get("tasks");
        assertNotNull(resultTasks);
        assertEquals(1, resultTasks.size());
        
        @SuppressWarnings("unchecked")
        List<GanttChartData> resultMilestones = (List<GanttChartData>) result.get("milestones");
        assertNotNull(resultMilestones);
        assertEquals(1, resultMilestones.size());
        
        // Verify transformation service calls
        verify(transformationService).filterChartData(
            eq(testTasksGanttData), 
            eq("CRITICAL_PATH"), 
            isNull(), 
            eq("Drivetrain"), 
            eq(now), 
            eq(now.plusDays(20))
        );
        
        verify(transformationService).filterChartData(
            eq(testMilestonesGanttData), 
            isNull(), 
            isNull(), 
            isNull(), 
            eq(now), 
            eq(now.plusDays(20))
        );
    }
    
    @Test
    void testApplyFiltersToGanttData_EmptyFilters() {
        // Setup
        Map<String, Object> ganttData = new HashMap<>();
        ganttData.put("tasks", testTasksGanttData);
        ganttData.put("milestones", testMilestonesGanttData);
        
        // Execute with empty filter criteria
        Map<String, Object> result = ganttDataService.applyFiltersToGanttData(ganttData, Collections.emptyMap());
        
        // Verify the original data is returned unchanged
        assertSame(ganttData, result);
        verify(transformationService, never()).filterChartData(anyList(), anyString(), anyString(), anyString(), any(), any());
    }
    
    @Test
    void testApplyFiltersToGanttData_NullInputs() {
        // Execute with null gantt data
        Map<String, Object> result1 = ganttDataService.applyFiltersToGanttData(null, Collections.emptyMap());
        assertNull(result1);
        
        // Execute with null filter criteria
        Map<String, Object> ganttData = new HashMap<>();
        Map<String, Object> result2 = ganttDataService.applyFiltersToGanttData(ganttData, null);
        assertSame(ganttData, result2);
        
        // Verify transformation service was never called
        verify(transformationService, never()).filterChartData(anyList(), anyString(), anyString(), anyString(), any(), any());
    }
    
    @Test
    void testCalculateCriticalPath() {
        // Setup
        Task criticalTask = new Task("Critical Task", testProject, null);
        criticalTask.setId(2L);
        criticalTask.setPriority(Task.Priority.CRITICAL);
        
        List<Task> tasks = List.of(testTask, criticalTask);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(taskRepository.findByProject(testProject)).thenReturn(tasks);
        
        // Execute
        List<Long> result = ganttDataService.calculateCriticalPath(1L);
        
        // Verify
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0));
        
        // Verify repository calls
        verify(projectRepository).findById(1L);
        verify(taskRepository).findByProject(testProject);
    }
    
    @Test
    void testCalculateCriticalPath_ProjectNotFound() {
        // Setup - Entity doesn't exist
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute
        List<Long> result = ganttDataService.calculateCriticalPath(999L);
        
        // Verify
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        // Verify repository calls
        verify(projectRepository).findById(999L);
        verify(taskRepository, never()).findByProject(any(Project.class));
    }
    
    @Test
    void testCalculateCriticalPath_NullProjectId() {
        // Execute and verify exception
        assertThrows(IllegalArgumentException.class, () -> {
            ganttDataService.calculateCriticalPath(null);
        });
        
        // Verify repository was never called
        verify(projectRepository, never()).findById(any());
        verify(taskRepository, never()).findByProject(any());
    }
    
    @Test
    void testGetGanttDataForDate() {
        // Setup a specific date
        LocalDate targetDate = now.plusDays(3);
        
        // Mock the formatTasksForGantt method by using spy
        GanttDataServiceImpl serviceSpy = spy(ganttDataService);
        
        Map<String, Object> fullData = new HashMap<>();
        fullData.put("tasks", testTasksGanttData);
        fullData.put("milestones", testMilestonesGanttData);
        
        doReturn(fullData).when(serviceSpy).formatTasksForGantt(eq(1L), isNull(), isNull());
        doReturn(fullData).when(serviceSpy).applyFiltersToGanttData(eq(fullData), any());
        
        // Execute
        Map<String, Object> result = serviceSpy.getGanttDataForDate(1L, targetDate);
        
        // Verify
        assertNotNull(result);
        assertEquals(fullData, result);
        
        // Verify methods were called with the correct filter criteria
        verify(serviceSpy).formatTasksForGantt(1L, null, null);
        
        // Capture the filter criteria argument
        ArgumentCaptor<Map<String, Object>> filterCaptor = ArgumentCaptor.forClass(Map.class);
        verify(serviceSpy).applyFiltersToGanttData(eq(fullData), filterCaptor.capture());
        
        Map<String, Object> filterCriteria = filterCaptor.getValue();
        assertEquals(targetDate.toString(), filterCriteria.get("startDate"));
        assertEquals(targetDate.plusDays(1).toString(), filterCriteria.get("endDate"));
    }
    
    @Test
    void testGetGanttDataForDate_NullDate() {
        // Setup 
        GanttDataServiceImpl serviceSpy = spy(ganttDataService);
        
        Map<String, Object> fullData = new HashMap<>();
        fullData.put("tasks", testTasksGanttData);
        fullData.put("milestones", testMilestonesGanttData);
        
        doReturn(fullData).when(serviceSpy).formatTasksForGantt(eq(1L), isNull(), isNull());
        doReturn(fullData).when(serviceSpy).applyFiltersToGanttData(eq(fullData), any());
        
        // Execute with null date (should use current date)
        Map<String, Object> result = serviceSpy.getGanttDataForDate(1L, null);
        
        // Verify
        assertNotNull(result);
        assertEquals(fullData, result);
        
        // Verify methods were called
        verify(serviceSpy).formatTasksForGantt(1L, null, null);
        verify(serviceSpy).applyFiltersToGanttData(eq(fullData), any());
    }
    
    @Test
    void testGetGanttDataForDate_NullProjectId() {
        // Execute and verify exception
        assertThrows(IllegalArgumentException.class, () -> {
            ganttDataService.getGanttDataForDate(null, now);
        });
    }
    
    @Test
    void testGetTaskDependencies() {
        // Setup tasks with dependencies
        Task dependentTask = new Task("Dependent Task", testProject, null);
        dependentTask.setId(2L);
        
        // Dependencies are now managed through TaskDependencyService
        // For testing purposes, we'll skip setting up dependencies
        // TODO: Update test to use TaskDependencyService
        
        List<Task> tasks = List.of(testTask, dependentTask);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(taskRepository.findByProject(testProject)).thenReturn(tasks);
        
        // Execute
        Map<Long, List<Long>> result = ganttDataService.getTaskDependencies(1L);
        
        // Verify
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey(1L));
        assertEquals(1, result.get(1L).size());
        assertEquals(2L, result.get(1L).get(0));
        
        // Verify repository calls
        verify(projectRepository).findById(1L);
        verify(taskRepository).findByProject(testProject);
    }
    
    @Test
    void testGetTaskDependencies_ProjectNotFound() {
        // Setup - Entity doesn't exist
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute
        Map<Long, List<Long>> result = ganttDataService.getTaskDependencies(999L);
        
        // Verify
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        // Verify repository calls
        verify(projectRepository).findById(999L);
        verify(taskRepository, never()).findByProject(any(Project.class));
    }
    
    @Test
    void testGetTaskDependencies_NullProjectId() {
        // Execute and verify exception
        assertThrows(IllegalArgumentException.class, () -> {
            ganttDataService.getTaskDependencies(null);
        });
        
        // Verify repository was never called
        verify(projectRepository, never()).findById(any());
        verify(taskRepository, never()).findByProject(any());
    }
    
    @Test
    void testIdentifyBottlenecks() {
        // Setup tasks with dependencies
        Task task1 = new Task("Task 1", testProject, null);
        task1.setId(1L);
        
        Task task2 = new Task("Task 2", testProject, null);
        task2.setId(2L);
        
        Task task3 = new Task("Task 3", testProject, null);
        task3.setId(3L);
        
        Task task4 = new Task("Task 4", testProject, null);
        task4.setId(4L);
        
        // Dependencies are now managed through TaskDependencyService
        // For testing purposes, we'll skip setting up dependencies
        // TODO: Update test to use TaskDependencyService
        // Set up task priorities to simulate bottleneck detection
        task1.setPriority(Task.Priority.LOW);
        task2.setPriority(Task.Priority.MEDIUM);
        task3.setPriority(Task.Priority.CRITICAL); // Most "connected" based on priority
        task4.setPriority(Task.Priority.HIGH);
        
        List<Task> tasks = List.of(task1, task2, task3, task4);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(taskRepository.findByProject(testProject)).thenReturn(tasks);
        
        // Execute
        List<Long> result = ganttDataService.identifyBottlenecks(1L);
        
        // Verify
        assertNotNull(result);
        assertEquals(1, result.size()); // Top 25% of 4 tasks = 1 task
        assertEquals(3L, result.get(0)); // Task3 should be identified as the bottleneck
        
        // Verify repository calls
        verify(projectRepository).findById(1L);
        verify(taskRepository).findByProject(testProject);
    }
    
    @Test
    void testIdentifyBottlenecks_ProjectNotFound() {
        // Setup - Entity doesn't exist
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute
        List<Long> result = ganttDataService.identifyBottlenecks(999L);
        
        // Verify
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        // Verify repository calls
        verify(projectRepository).findById(999L);
        verify(taskRepository, never()).findByProject(any(Project.class));
    }
    
    @Test
    void testIdentifyBottlenecks_NullProjectId() {
        // Execute and verify exception
        assertThrows(IllegalArgumentException.class, () -> {
            ganttDataService.identifyBottlenecks(null);
        });
        
        // Verify repository was never called
        verify(projectRepository, never()).findById(any());
        verify(taskRepository, never()).findByProject(any());
    }
    
    @Test
    void testGetTransformationService() {
        // Execute
        GanttChartTransformationService result = ganttDataService.getTransformationService();
        
        // Verify
        assertNotNull(result);
        assertSame(transformationService, result);
    }
}