// src/test/java/org/frcpm/services/GanttChartTransformationServiceTest.java
package org.frcpm.services;

import org.frcpm.models.GanttChartData;
import org.frcpm.models.Milestone;
import org.frcpm.models.Task;
import org.frcpm.models.Subsystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GanttChartTransformationServiceTest {

    private GanttChartTransformationService service;
    
    @Mock
    private Task mockTask1;
    
    @Mock
    private Task mockTask2;
    
    @Mock
    private Milestone mockMilestone;
    
    @Mock
    private Subsystem mockSubsystem;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new GanttChartTransformationService();
        
        // Setup mock task 1
        when(mockTask1.getId()).thenReturn(1L);
        when(mockTask1.getTitle()).thenReturn("Task 1");
        when(mockTask1.getStartDate()).thenReturn(LocalDate.now().minusDays(5));
        when(mockTask1.getEndDate()).thenReturn(LocalDate.now().plusDays(5));
        when(mockTask1.getProgress()).thenReturn(50);
        when(mockTask1.getPriority()).thenReturn(Task.Priority.HIGH);
        when(mockTask1.isCompleted()).thenReturn(false);
        when(mockTask1.getSubsystem()).thenReturn(mockSubsystem);
        when(mockTask1.getPreDependencies()).thenReturn(Collections.emptySet());
        when(mockTask1.getAssignedTo()).thenReturn(Collections.emptySet());
        
        // Setup mock task 2
        when(mockTask2.getId()).thenReturn(2L);
        when(mockTask2.getTitle()).thenReturn("Task 2");
        when(mockTask2.getStartDate()).thenReturn(LocalDate.now());
        when(mockTask2.getEndDate()).thenReturn(LocalDate.now().plusDays(10));
        when(mockTask2.getProgress()).thenReturn(25);
        when(mockTask2.getPriority()).thenReturn(Task.Priority.MEDIUM);
        when(mockTask2.isCompleted()).thenReturn(false);
        when(mockTask2.getSubsystem()).thenReturn(mockSubsystem);
        
        Set<Task> task2Dependencies = new HashSet<>();
        task2Dependencies.add(mockTask1);
        when(mockTask2.getPreDependencies()).thenReturn(task2Dependencies);
        when(mockTask2.getAssignedTo()).thenReturn(Collections.emptySet());
        
        // Setup mock subsystem
        when(mockSubsystem.getName()).thenReturn("Test Subsystem");
        
        // Setup mock milestone
        when(mockMilestone.getId()).thenReturn(101L);
        when(mockMilestone.getName()).thenReturn("Test Milestone");
        when(mockMilestone.getDate()).thenReturn(LocalDate.now().plusDays(15));
        when(mockMilestone.isPassed()).thenReturn(false);
    }
    
    @Test
    void testTransformTasksToChartData() {
        // Arrange
        List<Task> tasks = Arrays.asList(mockTask1, mockTask2);
        
        // Act
        List<GanttChartData> result = service.transformTasksToChartData(tasks);
        
        // Assert
        assertEquals(2, result.size());
        
        GanttChartData data1 = result.get(0);
        assertEquals("task_1", data1.getId());
        assertEquals("Task 1", data1.getTitle());
        assertEquals(mockTask1.getStartDate(), data1.getStartDate());
        assertEquals(mockTask1.getEndDate(), data1.getEndDate());
        assertEquals(50, data1.getProgress());
        assertEquals("task", data1.getType());
        assertEquals("in-progress", data1.getStatus());
        assertEquals("Test Subsystem", data1.getSubsystem());
        
        GanttChartData data2 = result.get(1);
        assertEquals("task_2", data2.getId());
        assertEquals(1, data2.getDependencies().size());
        assertEquals("task_1", data2.getDependencies().get(0));
    }
    
    @Test
    void testTransformMilestonesToChartData() {
        // Arrange
        List<Milestone> milestones = Collections.singletonList(mockMilestone);
        
        // Act
        List<GanttChartData> result = service.transformMilestonesToChartData(milestones);
        
        // Assert
        assertEquals(1, result.size());
        
        GanttChartData data = result.get(0);
        assertEquals("milestone_101", data.getId());
        assertEquals("Test Milestone", data.getTitle());
        assertEquals(mockMilestone.getDate(), data.getStartDate());
        assertEquals(mockMilestone.getDate(), data.getEndDate());
        assertEquals(0, data.getProgress());
        assertEquals("milestone", data.getType());
        assertEquals("pending", data.getStatus());
        assertNotNull(data.getColor());
    }
    
    @Test
    void testFilterChartData() {
        // Arrange
        List<GanttChartData> chartDataList = new ArrayList<>();
        
        GanttChartData data1 = new GanttChartData();
        data1.setId("task_1");
        data1.setTitle("Task 1");
        data1.setStartDate(LocalDate.now().minusDays(5));
        data1.setEndDate(LocalDate.now().plusDays(5));
        data1.setProgress(50);
        data1.setType("task");
        data1.setStatus("in-progress");
        data1.setSubsystem("Test Subsystem");
        data1.setColor("#ff9800"); // Orange - HIGH priority
        chartDataList.add(data1);
        
        GanttChartData data2 = new GanttChartData();
        data2.setId("task_2");
        data2.setTitle("Task 2");
        data2.setStartDate(LocalDate.now());
        data2.setEndDate(LocalDate.now().plusDays(10));
        data2.setProgress(25);
        data2.setType("task");
        data2.setStatus("in-progress");
        data2.setSubsystem("Another Subsystem");
        data2.setColor("#2196f3"); // Blue - MEDIUM priority
        chartDataList.add(data2);
        
        // Act - Filter by subsystem
        List<GanttChartData> result1 = service.filterChartData(
            chartDataList, null, null, "Test Subsystem", null, null
        );
        
        // Assert
        assertEquals(1, result1.size());
        assertEquals("Task 1", result1.get(0).getTitle());
        
        // Act - Filter by date range
        List<GanttChartData> result2 = service.filterChartData(
            chartDataList, null, null, null, 
            LocalDate.now().plusDays(6), LocalDate.now().plusDays(15)
        );
        
        // Assert
        assertEquals(1, result2.size());
        assertEquals("Task 2", result2.get(0).getTitle());
        
        // Act - Filter by critical path (red color)
        List<GanttChartData> result3 = service.filterChartData(
            chartDataList, "CRITICAL_PATH", null, null, null, null
        );
        
        // Assert - Neither task should be in critical path
        assertEquals(0, result3.size());
    }
    
    @Test
    void testCreateDependencyData() {
        // Arrange
        List<Task> tasks = Arrays.asList(mockTask1, mockTask2);
        
        // Act
        List<Map<String, Object>> result = service.createDependencyData(tasks);
        
        // Assert
        assertEquals(1, result.size());
        
        Map<String, Object> dependency = result.get(0);
        assertEquals("task_1", dependency.get("source"));
        assertEquals("task_2", dependency.get("target"));
        assertEquals("finish-to-start", dependency.get("type"));
    }
    
    @Test
    void testTransformToChartJsFormat() {
        // Arrange
        List<GanttChartData> chartDataList = new ArrayList<>();
        
        GanttChartData data1 = new GanttChartData();
        data1.setId("task_1");
        data1.setTitle("Task 1");
        data1.setStartDate(LocalDate.now().minusDays(5));
        data1.setEndDate(LocalDate.now().plusDays(5));
        data1.setProgress(50);
        data1.setType("task");
        data1.setStatus("in-progress");
        data1.setSubsystem("Test Subsystem");
        data1.setColor("#ff9800");
        chartDataList.add(data1);
        
        // Act
        Map<String, Object> result = service.transformToChartJsFormat(chartDataList);
        
        // Assert
        assertTrue(result.containsKey("datasets"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> datasets = (List<Map<String, Object>>) result.get("datasets");
        assertEquals(1, datasets.size());
        
        Map<String, Object> dataset = datasets.get(0);
        assertEquals("task_1", dataset.get("id"));
        assertEquals("Task 1", dataset.get("label"));
        assertEquals("#ff9800", dataset.get("backgroundColor"));
        assertNotNull(dataset.get("borderColor"));
        assertEquals(50, dataset.get("progress"));
        assertEquals("task", dataset.get("type"));
        assertEquals("Test Subsystem", dataset.get("subsystem"));
    }
}