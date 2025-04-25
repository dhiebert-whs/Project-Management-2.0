// src/test/java/org/frcpm/viewmodels/GanttChartViewModelIntegrationTest.java
package org.frcpm.viewmodels;

import org.frcpm.models.GanttChartData;
import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Subteam;
import org.frcpm.services.GanttDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class GanttChartViewModelIntegrationTest {

    private GanttChartViewModel viewModel;
    
    @Mock
    private GanttDataService mockGanttDataService;
    
    @Mock
    private Project mockProject;
    
    @Mock
    private Subsystem mockSubsystem;
    
    @Mock
    private Subteam mockSubteam;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Create view model with mock service
        viewModel = new GanttChartViewModel(mockGanttDataService);
        
        // Setup mock project
        when(mockProject.getId()).thenReturn(1L);
        when(mockProject.getName()).thenReturn("Test Project");
        when(mockProject.getStartDate()).thenReturn(LocalDate.now().minusDays(30));
        when(mockProject.getHardDeadline()).thenReturn(LocalDate.now().plusDays(60));
        
        // Setup mock subsystem
        when(mockSubsystem.getId()).thenReturn(101L);
        when(mockSubsystem.getName()).thenReturn("Test Subsystem");
        
        // Setup mock subteam
        when(mockSubteam.getId()).thenReturn(201L);
        when(mockSubteam.getName()).thenReturn("Test Subteam");
        
        // Setup service response
        Map<String, Object> mockData = new HashMap<>();
        List<GanttChartData> mockTasks = new ArrayList<>();
        List<GanttChartData> mockMilestones = new ArrayList<>();
        
        GanttChartData mockTask1 = new GanttChartData();
        mockTask1.setId("task_1");
        mockTask1.setTitle("Task 1");
        mockTask1.setStartDate(LocalDate.now().minusDays(20));
        mockTask1.setEndDate(LocalDate.now().plusDays(10));
        mockTasks.add(mockTask1);
        
        GanttChartData mockMilestone1 = new GanttChartData();
        mockMilestone1.setId("milestone_101");
        mockMilestone1.setTitle("Milestone 1");
        mockMilestone1.setStartDate(LocalDate.now().plusDays(15));
        mockMilestone1.setEndDate(LocalDate.now().plusDays(15));
        mockMilestones.add(mockMilestone1);
        
        mockData.put("tasks", mockTasks);
        mockData.put("milestones", mockMilestones);
        mockData.put("dependencies", new ArrayList<>());
        mockData.put("startDate", LocalDate.now().minusDays(30).toString());
        mockData.put("endDate", LocalDate.now().plusDays(60).toString());
        mockData.put("projectName", "Test Project");
        
        when(mockGanttDataService.formatTasksForGantt(
                eq(1L), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(mockData);
        
        when(mockGanttDataService.applyFiltersToGanttData(any(), any()))
            .thenReturn(mockData);
    }
    
    @Test
    void testViewModelInitialization() {
        // Assert default state
        assertEquals(GanttChartViewModel.ViewMode.WEEK, viewModel.getViewMode());
        assertEquals(GanttChartViewModel.FilterOption.ALL_TASKS, viewModel.getFilterOption());
        assertTrue(viewModel.isShowMilestones());
        assertTrue(viewModel.isShowDependencies());
        assertFalse(viewModel.isDataLoaded());
        assertNull(viewModel.getProject());
        assertNotNull(viewModel.getRefreshCommand());
    }
    
    @Test
    void testSetProject() {
        // Act
        viewModel.setProject(mockProject);
        
        // Assert
        assertEquals(mockProject, viewModel.getProject());
        assertEquals(mockProject.getStartDate(), viewModel.getStartDate());
        assertEquals(mockProject.getHardDeadline(), viewModel.getEndDate());
        assertTrue(viewModel.isDataLoaded());
        assertNotNull(viewModel.getChartData());
        
        // Verify service was called
        verify(mockGanttDataService).formatTasksForGantt(
            eq(1L), any(LocalDate.class), any(LocalDate.class)
        );
    }
    
    @Test
    void testRefreshCommand() {
        // Arrange
        viewModel.setProject(mockProject);
        
        // Reset mock to clear the call from setProject
        reset(mockGanttDataService);
        
        // Setup mock service response again
        Map<String, Object> mockData = new HashMap<>();
        mockData.put("tasks", new ArrayList<>());
        mockData.put("milestones", new ArrayList<>());
        
        when(mockGanttDataService.formatTasksForGantt(
                eq(1L), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(mockData);
        
        // Act
        viewModel.getRefreshCommand().execute();
        
        // Assert
        verify(mockGanttDataService).formatTasksForGantt(
            eq(1L), any(LocalDate.class), any(LocalDate.class)
        );
    }
    
    @Test
    void testApplyFilter() {
        // Arrange
        viewModel.setProject(mockProject);
        
        // Reset mock to clear the call from setProject
        reset(mockGanttDataService);
        
        // Setup mock service response again
        Map<String, Object> mockData = new HashMap<>();
        mockData.put("tasks", new ArrayList<>());
        mockData.put("milestones", new ArrayList<>());
        
        when(mockGanttDataService.applyFiltersToGanttData(any(), any()))
            .thenReturn(mockData);
        
        // Act
        viewModel.setFilterOption(GanttChartViewModel.FilterOption.CRITICAL_PATH);
        
        // Assert - verify service was called with filter criteria
        verify(mockGanttDataService).applyFiltersToGanttData(any(), any());
    }
    
    @Test
    void testSubsystemFiltering() {
        // Arrange
        viewModel.setProject(mockProject);
        viewModel.setSelectedSubsystem(mockSubsystem);
        
        // Reset mock to clear previous calls
        reset(mockGanttDataService);
        
        // Setup mock service response
        Map<String, Object> mockData = new HashMap<>();
        mockData.put("tasks", new ArrayList<>());
        mockData.put("milestones", new ArrayList<>());
        
        when(mockGanttDataService.applyFiltersToGanttData(any(), any()))
            .thenReturn(mockData);
        
        // Act
        viewModel.getFilterBySubsystemCommand().execute();
        
        // Assert - verify service was called with filter criteria
        verify(mockGanttDataService).applyFiltersToGanttData(any(), argThat(map -> 
            map.containsKey("subsystem") && map.get("subsystem").equals("Test Subsystem")
        ));
    }
    
    @Test
    void testZoomCommands() {
        // Arrange
        viewModel.setProject(mockProject);
        LocalDate initialStartDate = viewModel.getStartDate();
        LocalDate initialEndDate = viewModel.getEndDate();
        
        // Act - zoom in
        viewModel.getZoomInCommand().execute();
        
        // Assert - date range should be reduced
        assertTrue(viewModel.getStartDate().isAfter(initialStartDate));
        assertTrue(viewModel.getEndDate().isBefore(initialEndDate));
        
        // Reset dates for zoom out test
        LocalDate zoomedInStartDate = viewModel.getStartDate();
        LocalDate zoomedInEndDate = viewModel.getEndDate();
        
        // Act - zoom out
        viewModel.getZoomOutCommand().execute();
        
        // Assert - date range should be expanded
        assertTrue(viewModel.getStartDate().isBefore(zoomedInStartDate) || viewModel.getStartDate().isEqual(zoomedInStartDate));
        assertTrue(viewModel.getEndDate().isAfter(zoomedInEndDate) || viewModel.getEndDate().isEqual(zoomedInEndDate));
    }
    
    @Test
    void testToggleCommands() {
        // Arrange
        assertTrue(viewModel.isShowMilestones());
        assertTrue(viewModel.isShowDependencies());
        
        // Act - toggle milestones
        viewModel.getToggleMilestonesCommand().execute();
        
        // Assert
        assertFalse(viewModel.isShowMilestones());
        
        // Act - toggle dependencies
        viewModel.getToggleDependenciesCommand().execute();
        
        // Assert
        assertFalse(viewModel.isShowDependencies());
    }
}