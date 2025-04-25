// src/test/java/org/frcpm/services/impl/GanttDataServiceImplIntegrationTest.java
package org.frcpm.services.impl;

import org.frcpm.models.GanttChartData;
import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.models.Subsystem;
import org.frcpm.repositories.specific.MilestoneRepository;
import org.frcpm.repositories.specific.ProjectRepository;
import org.frcpm.repositories.specific.TaskRepository;
import org.frcpm.services.GanttChartTransformationService;
import org.frcpm.services.GanttDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

public class GanttDataServiceImplIntegrationTest {

    private GanttDataServiceImpl ganttDataService;
    
    @Mock
    private ProjectRepository mockProjectRepo;
    
    @Mock
    private TaskRepository mockTaskRepo;
    
    @Mock
    private MilestoneRepository mockMilestoneRepo;
    
    @Mock
    private GanttChartTransformationService mockTransformationService;
    
    @Mock
    private Project mockProject;
    
    @Mock
    private Task mockTask1;
    
    @Mock
    private Task mockTask2;
    
    @Mock
    private Milestone mockMilestone;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Create service with mock repositories and transformation service
        ganttDataService = new GanttDataServiceImpl(
            mockProjectRepo, mockTaskRepo, mockMilestoneRepo, mockTransformationService
        );
        
        // Setup mock project
        when(mockProject.getId()).thenReturn(1L);
        when(mockProject.getName()).thenReturn("Test Project");
        when(mockProject.getStartDate()).thenReturn(LocalDate.now().minusDays(30));
        when(mockProject.getHardDeadline()).thenReturn(LocalDate.now().plusDays(60));
        
        // Setup mock tasks
        when(mockTask1.getId()).thenReturn(1L);
        when(mockTask1.getTitle()).thenReturn("Task 1");
        // Fix: Add priority for mockTask1
        when(mockTask1.getPriority()).thenReturn(Task.Priority.MEDIUM);
        
        when(mockTask2.getId()).thenReturn(2L);
        when(mockTask2.getTitle()).thenReturn("Task 2");
        // Fix: Add priority for mockTask2
        when(mockTask2.getPriority()).thenReturn(Task.Priority.HIGH);
        
        // Setup mock milestone
        when(mockMilestone.getId()).thenReturn(101L);
        when(mockMilestone.getName()).thenReturn("Test Milestone");
        
        // Setup repository responses
        when(mockProjectRepo.findById(1L)).thenReturn(Optional.of(mockProject));
        when(mockTaskRepo.findByProject(mockProject)).thenReturn(Arrays.asList(mockTask1, mockTask2));
        when(mockMilestoneRepo.findByProject(mockProject)).thenReturn(Collections.singletonList(mockMilestone));
        
        // Setup transformation service responses - using our own mock data instead of FromTask
        List<GanttChartData> mockTaskChartData = new ArrayList<>();
        
        GanttChartData taskData1 = new GanttChartData();
        taskData1.setId("task_1");
        taskData1.setTitle("Task 1");
        taskData1.setStartDate(LocalDate.now().minusDays(5));
        taskData1.setEndDate(LocalDate.now().plusDays(5));
        taskData1.setType("task");
        
        GanttChartData taskData2 = new GanttChartData();
        taskData2.setId("task_2");
        taskData2.setTitle("Task 2");
        taskData2.setStartDate(LocalDate.now());
        taskData2.setEndDate(LocalDate.now().plusDays(10));
        taskData2.setType("task");
        
        mockTaskChartData.add(taskData1);
        mockTaskChartData.add(taskData2);
        
        List<GanttChartData> mockMilestoneChartData = new ArrayList<>();
        GanttChartData milestoneData = new GanttChartData();
        milestoneData.setId("milestone_101");
        milestoneData.setTitle("Test Milestone");
        milestoneData.setStartDate(LocalDate.now().plusDays(15));
        milestoneData.setEndDate(LocalDate.now().plusDays(15));
        milestoneData.setType("milestone");
        mockMilestoneChartData.add(milestoneData);
        
        when(mockTransformationService.transformTasksToChartData(anyList())).thenReturn(mockTaskChartData);
        when(mockTransformationService.transformMilestonesToChartData(anyList())).thenReturn(mockMilestoneChartData);
        when(mockTransformationService.createDependencyData(anyList())).thenReturn(new ArrayList<>());
    }
    
    @Test
    void testFormatTasksForGantt() {
        // Act
        Map<String, Object> result = ganttDataService.formatTasksForGantt(
            1L, LocalDate.now().minusDays(10), LocalDate.now().plusDays(20)
        );
        
        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("tasks"));
        assertTrue(result.containsKey("milestones"));
        assertTrue(result.containsKey("dependencies"));
        assertTrue(result.containsKey("startDate"));
        assertTrue(result.containsKey("endDate"));
        assertTrue(result.containsKey("projectName"));
        
        // Verify transformation service calls
        verify(mockTransformationService).transformTasksToChartData(anyList());
        verify(mockTransformationService).transformMilestonesToChartData(anyList());
        verify(mockTransformationService).createDependencyData(anyList());
    }
    
    @Test
    void testApplyFiltersToGanttData() {
        // Arrange
        Map<String, Object> mockData = new HashMap<>();
        List<GanttChartData> mockTasks = new ArrayList<>();
        mockTasks.add(new GanttChartData());
        mockTasks.add(new GanttChartData());
        mockData.put("tasks", mockTasks);
        
        List<GanttChartData> mockMilestones = new ArrayList<>();
        mockMilestones.add(new GanttChartData());
        mockData.put("milestones", mockMilestones);
        
        Map<String, Object> filterCriteria = new HashMap<>();
        filterCriteria.put("filterType", "CRITICAL_PATH");
        filterCriteria.put("subsystem", "Test Subsystem");
        filterCriteria.put("startDate", LocalDate.now().toString());
        
        // Setup transformation service response for filter - match actual parameter pattern
        when(mockTransformationService.filterChartData(
            anyList(), 
            eq("CRITICAL_PATH"),  // exact value for filterType
            isNull(),             // null for subteam
            eq("Test Subsystem"), // exact value for subsystem
            any(LocalDate.class), // any LocalDate for startDate
            isNull()              // null for endDate
        )).thenReturn(Collections.singletonList(new GanttChartData()));
        
        // Setup for milestones filter call
        when(mockTransformationService.filterChartData(
            anyList(),
            isNull(),             // null for filterType
            isNull(),             // null for subteam
            isNull(),             // null for subsystem
            any(LocalDate.class), // any LocalDate for startDate
            isNull()              // null for endDate
        )).thenReturn(Collections.singletonList(new GanttChartData()));
        
        // Act
        Map<String, Object> result = ganttDataService.applyFiltersToGanttData(mockData, filterCriteria);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("tasks"));
        assertTrue(result.containsKey("milestones"));
        
        // Verify transformation service calls
        verify(mockTransformationService).filterChartData(
            anyList(), 
            eq("CRITICAL_PATH"),  // exact value for filterType
            isNull(),             // null for subteam
            eq("Test Subsystem"), // exact value for subsystem
            any(LocalDate.class), // any LocalDate for startDate
            isNull()              // null for endDate
        );
        
        verify(mockTransformationService).filterChartData(
            anyList(),
            isNull(),             // null for filterType
            isNull(),             // null for subteam
            isNull(),             // null for subsystem
            any(LocalDate.class), // any LocalDate for startDate
            isNull()              // null for endDate
        );
    }
    
    @Test
    void testCalculateCriticalPath() {
        // Arrange
        when(mockTask1.getPriority()).thenReturn(Task.Priority.MEDIUM);
        when(mockTask2.getPriority()).thenReturn(Task.Priority.CRITICAL);
        
        // Act
        List<Long> result = ganttDataService.calculateCriticalPath(1L);
        
        // Assert
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0)); // Only mockTask2 is CRITICAL
    }
    
    @Test
    void testGetGanttDataForDate() {
        // Arrange - Set up formatTasksForGantt to return a specific result
        Map<String, Object> mockFullData = new HashMap<>();
        mockFullData.put("tasks", Arrays.asList(GanttChartData.fromTask(mockTask1), GanttChartData.fromTask(mockTask2)));
        mockFullData.put("milestones", Collections.singletonList(GanttChartData.fromMilestone(mockMilestone)));
        mockFullData.put("startDate", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        mockFullData.put("endDate", LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE));
        
        // Create spy to allow partial mocking
        GanttDataServiceImpl spyService = spy(ganttDataService);
        doReturn(mockFullData).when(spyService).formatTasksForGantt(anyLong(), any(), any());
        
        // Also mock applyFiltersToGanttData
        doReturn(mockFullData).when(spyService).applyFiltersToGanttData(any(), any());
        
        // Act
        Map<String, Object> result = spyService.getGanttDataForDate(1L, LocalDate.now());
        
        // Assert
        assertNotNull(result);
        assertEquals(mockFullData, result);
        
        // Verify method calls
        verify(spyService).formatTasksForGantt(eq(1L), isNull(), isNull());
        verify(spyService).applyFiltersToGanttData(eq(mockFullData), any());
    }
    
    @Test
    void testGetTaskDependencies() {
        // Arrange
        Set<Task> task1Dependencies = Collections.emptySet();
        when(mockTask1.getPreDependencies()).thenReturn(task1Dependencies);
        
        Set<Task> task2Dependencies = new HashSet<>();
        task2Dependencies.add(mockTask1);
        when(mockTask2.getPreDependencies()).thenReturn(task2Dependencies);
        
        // Act
        Map<Long, List<Long>> result = ganttDataService.getTaskDependencies(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey(1L));
        assertTrue(result.containsKey(2L));
        
        assertEquals(0, result.get(1L).size()); // No dependencies for task1
        assertEquals(1, result.get(2L).size()); // task1 is a dependency for task2
        assertEquals(1L, result.get(2L).get(0));
    }
    
    @Test
    void testIdentifyBottlenecks() {
        // Arrange - Create task dependencies to identify bottlenecks
        Set<Task> task1Dependencies = Collections.emptySet();
        when(mockTask1.getPreDependencies()).thenReturn(task1Dependencies);
        
        Set<Task> task2Dependencies = new HashSet<>();
        task2Dependencies.add(mockTask1);
        when(mockTask2.getPreDependencies()).thenReturn(task2Dependencies);
        
        // Act
        List<Long> result = ganttDataService.identifyBottlenecks(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size()); // With our simple setup, only 1 task will be identified as a bottleneck
    }
    
    @Test
    void testProjectNotFound() {
        // Arrange
        when(mockProjectRepo.findById(999L)).thenReturn(Optional.empty());
        
        // Act
        Map<String, Object> result = ganttDataService.formatTasksForGantt(999L, null, null);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        // Verify repository was called but transformation service was not
        verify(mockProjectRepo).findById(999L);
        verifyNoInteractions(mockTransformationService);
    }
    
    @Test
    void testNullProjectId() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            ganttDataService.formatTasksForGantt(null, null, null);
        });
        
        assertEquals("Project ID cannot be null", exception.getMessage());
    }
}