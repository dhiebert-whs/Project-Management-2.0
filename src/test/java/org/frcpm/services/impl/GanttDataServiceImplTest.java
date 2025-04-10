// src/test/java/org/frcpm/services/impl/GanttDataServiceImplTest.java
package org.frcpm.services.impl;

import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.repositories.specific.MilestoneRepository;
import org.frcpm.repositories.specific.ProjectRepository;
import org.frcpm.repositories.specific.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.Duration;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GanttDataServiceImplTest {

    private GanttDataServiceImpl ganttDataService;
    
    @Mock
    private ProjectRepository mockProjectRepository;
    
    @Mock
    private TaskRepository mockTaskRepository;
    
    @Mock
    private MilestoneRepository mockMilestoneRepository;
    
    @Mock
    private Project mockProject;
    
    @Mock
    private Task mockTask1;
    
    @Mock
    private Task mockTask2;
    
    @Mock
    private Milestone mockMilestone;
    
    @Mock
    private Subsystem mockSubsystem;
    
    private List<Task> mockTasks;
    private List<Milestone> mockMilestones;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Create test object with mock repositories
        ganttDataService = new GanttDataServiceImpl(
            mockProjectRepository, 
            mockTaskRepository, 
            mockMilestoneRepository
        );
        
        // Set up mock project
        when(mockProject.getId()).thenReturn(1L);
        when(mockProject.getName()).thenReturn("Test Project");
        when(mockProject.getStartDate()).thenReturn(LocalDate.now().minusDays(30));
        when(mockProject.getGoalEndDate()).thenReturn(LocalDate.now().plusDays(30));
        when(mockProject.getHardDeadline()).thenReturn(LocalDate.now().plusDays(60));
        
        // Set up mock subsystem
        when(mockSubsystem.getName()).thenReturn("Test Subsystem");
        
        // Set up mock tasks
        when(mockTask1.getId()).thenReturn(101L);
        when(mockTask1.getTitle()).thenReturn("Task 1");
        when(mockTask1.getDescription()).thenReturn("Task 1 Description");
        when(mockTask1.getStartDate()).thenReturn(LocalDate.now().minusDays(20));
        when(mockTask1.getEndDate()).thenReturn(LocalDate.now().plusDays(10));
        when(mockTask1.getProgress()).thenReturn(50);
        when(mockTask1.getPriority()).thenReturn(Task.Priority.HIGH);
        when(mockTask1.isCompleted()).thenReturn(false);
        when(mockTask1.getEstimatedDuration()).thenReturn(Duration.ofDays(30));
        when(mockTask1.getSubsystem()).thenReturn(mockSubsystem);
        when(mockTask1.getProject()).thenReturn(mockProject);
        when(mockTask1.getPreDependencies()).thenReturn(Collections.emptySet());
        when(mockTask1.getAssignedTo()).thenReturn(Collections.emptySet());
        
        when(mockTask2.getId()).thenReturn(102L);
        when(mockTask2.getTitle()).thenReturn("Task 2");
        when(mockTask2.getDescription()).thenReturn("Task 2 Description");
        when(mockTask2.getStartDate()).thenReturn(LocalDate.now().minusDays(10));
        when(mockTask2.getEndDate()).thenReturn(LocalDate.now().plusDays(20));
        when(mockTask2.getProgress()).thenReturn(25);
        when(mockTask2.getPriority()).thenReturn(Task.Priority.MEDIUM);
        when(mockTask2.isCompleted()).thenReturn(false);
        when(mockTask2.getEstimatedDuration()).thenReturn(Duration.ofDays(30));
        when(mockTask2.getSubsystem()).thenReturn(mockSubsystem);
        when(mockTask2.getProject()).thenReturn(mockProject);
        
        Set<Task> task2Dependencies = new HashSet<>();
        task2Dependencies.add(mockTask1);
        when(mockTask2.getPreDependencies()).thenReturn(task2Dependencies);
        when(mockTask2.getAssignedTo()).thenReturn(Collections.emptySet());
        
        mockTasks = Arrays.asList(mockTask1, mockTask2);
        
        // Set up mock milestone
        when(mockMilestone.getId()).thenReturn(201L);
        when(mockMilestone.getName()).thenReturn("Test Milestone");
        when(mockMilestone.getDescription()).thenReturn("Milestone Description");
        when(mockMilestone.getDate()).thenReturn(LocalDate.now().plusDays(15));
        when(mockMilestone.getProject()).thenReturn(mockProject);
        when(mockMilestone.isPassed()).thenReturn(false);
        
        mockMilestones = Collections.singletonList(mockMilestone);
        
        // Set up repository responses
        when(mockProjectRepository.findById(1L)).thenReturn(Optional.of(mockProject));
        when(mockTaskRepository.findByProject(mockProject)).thenReturn(mockTasks);
        when(mockMilestoneRepository.findByProject(mockProject)).thenReturn(mockMilestones);
    }
    
    @Test
    void testFormatTasksForGantt() {
        // Act
        Map<String, Object> result = ganttDataService.formatTasksForGantt(
            1L,
            LocalDate.now().minusDays(30),
            LocalDate.now().plusDays(60)
        );
        
        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("tasks"));
        assertTrue(result.containsKey("milestones"));
        assertTrue(result.containsKey("dependencies"));
        assertTrue(result.containsKey("startDate"));
        assertTrue(result.containsKey("endDate"));
        assertTrue(result.containsKey("projectName"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> tasks = (List<Map<String, Object>>) result.get("tasks");
        assertEquals(2, tasks.size());
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> milestones = (List<Map<String, Object>>) result.get("milestones");
        assertEquals(1, milestones.size());
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> dependencies = (List<Map<String, Object>>) result.get("dependencies");
        assertEquals(1, dependencies.size());
    }
    
    @Test
    void testApplyFiltersToGanttData() {
        // Arrange
        Map<String, Object> inputData = new HashMap<>();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> tasks = new ArrayList<>();
        
        Map<String, Object> task1 = new HashMap<>();
        task1.put("id", "101");
        task1.put("name", "Task 1");
        task1.put("priority", "HIGH");
        task1.put("progress", 50);
        task1.put("startDate", LocalDate.now().minusDays(20).toString());
        task1.put("endDate", LocalDate.now().plusDays(10).toString());
        tasks.add(task1);
        
        Map<String, Object> task2 = new HashMap<>();
        task2.put("id", "102");
        task2.put("name", "Task 2");
        task2.put("priority", "MEDIUM");
        task2.put("progress", 25);
        task2.put("startDate", LocalDate.now().minusDays(10).toString());
        task2.put("endDate", LocalDate.now().plusDays(20).toString());
        tasks.add(task2);
        
        inputData.put("tasks", tasks);
        
        Map<String, Object> filterCriteria = new HashMap<>();
        filterCriteria.put("filterType", "Critical Path");
        
        // Act
        Map<String, Object> result = ganttDataService.applyFiltersToGanttData(inputData, filterCriteria);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("tasks"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> filteredTasks = (List<Map<String, Object>>) result.get("tasks");
        
        // For the "Critical Path" filter, only HIGH priority tasks should be included
        assertEquals(0, filteredTasks.size());
    }
    
    @Test
    void testCalculateCriticalPath() {
        // Arrange
        when(mockTask1.getPriority()).thenReturn(Task.Priority.CRITICAL);
        
        // Act
        List<Long> criticalPath = ganttDataService.calculateCriticalPath(1L);
        
        // Assert
        assertNotNull(criticalPath);
        assertEquals(1, criticalPath.size());
        assertEquals(101L, criticalPath.get(0));
    }
    
    @Test
    void testGetTaskDependencies() {
        // Act
        Map<Long, List<Long>> dependencies = ganttDataService.getTaskDependencies(1L);
        
        // Assert
        assertNotNull(dependencies);
        assertEquals(2, dependencies.size());
        assertTrue(dependencies.containsKey(101L));
        assertTrue(dependencies.containsKey(102L));
        
        List<Long> task1Dependencies = dependencies.get(101L);
        assertEquals(0, task1Dependencies.size());
        
        List<Long> task2Dependencies = dependencies.get(102L);
        assertEquals(1, task2Dependencies.size());
        assertEquals(101L, task2Dependencies.get(0));
    }
    
    @Test
    void testIdentifyBottlenecks() {
        // Act
        List<Long> bottlenecks = ganttDataService.identifyBottlenecks(1L);
        
        // Assert
        assertNotNull(bottlenecks);
        assertEquals(1, bottlenecks.size());
        
        // In our test setup, task1 is a dependency for task2, so it should be identified as a bottleneck
        assertTrue(bottlenecks.contains(101L));
    }
}