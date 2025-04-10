// src/test/java/org/frcpm/viewmodels/GanttChartViewModelTest.java
package org.frcpm.viewmodels;

import org.frcpm.models.Project;
import org.frcpm.services.GanttDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class GanttChartViewModelTest {

    private GanttChartViewModel viewModel;
    
    @Mock
    private GanttDataService ganttDataService;
    
    @Mock
    private Project mockProject;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Create test object with mock service
        viewModel = new GanttChartViewModel(ganttDataService);
        
        // Set up mock project
        when(mockProject.getId()).thenReturn(1L);
        when(mockProject.getName()).thenReturn("Test Project");
        when(mockProject.getStartDate()).thenReturn(LocalDate.now().minusDays(30));
        when(mockProject.getHardDeadline()).thenReturn(LocalDate.now().plusDays(60));
        
        // Set up mock service response
        Map<String, Object> mockData = new HashMap<>();
        mockData.put("tasks", Collections.emptyList());
        mockData.put("milestones", Collections.emptyList());
        mockData.put("dependencies", Collections.emptyList());
        
        when(ganttDataService.formatTasksForGantt(
                eq(1L), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(mockData);
        
        when(ganttDataService.applyFiltersToGanttData(any(), any()))
            .thenReturn(mockData);
    }
    
    @Test
    void testSetProject() {
        // Act
        viewModel.setProject(mockProject);
        
        // Assert
        assertEquals(mockProject, viewModel.getProject());
        assertEquals(mockProject.getStartDate(), viewModel.getStartDate());
        assertEquals(mockProject.getHardDeadline(), viewModel.getEndDate());
    }
    
    @Test
    void testViewModeProperty() {
        // Arrange
        GanttChartViewModel.ViewMode expectedMode = GanttChartViewModel.ViewMode.DAY;
        
        // Act
        viewModel.setViewMode(expectedMode);
        
        // Assert
        assertEquals(expectedMode, viewModel.getViewMode());
        assertEquals(expectedMode, viewModel.viewModeProperty().get());
    }
    
    @Test
    void testFilterOptionProperty() {
        // Arrange
        GanttChartViewModel.FilterOption expectedFilter = GanttChartViewModel.FilterOption.CRITICAL_PATH;
        
        // Act
        viewModel.setFilterOption(expectedFilter);
        
        // Assert
        assertEquals(expectedFilter, viewModel.getFilterOption());
        assertEquals(expectedFilter, viewModel.filterOptionProperty().get());
    }
    
    @Test
    void testToggleMilestonesCommand() {
        // Arrange
        boolean initialState = viewModel.isShowMilestones();
        
        // Act
        viewModel.getToggleMilestonesCommand().execute();
        
        // Assert
        assertEquals(!initialState, viewModel.isShowMilestones());
    }
    
    @Test
    void testToggleDependenciesCommand() {
        // Arrange
        boolean initialState = viewModel.isShowDependencies();
        
        // Act
        viewModel.getToggleDependenciesCommand().execute();
        
        // Assert
        assertEquals(!initialState, viewModel.isShowDependencies());
    }
    
    @Test
    void testCanLoadData() {
        // Arrange - no project set
        
        // Act & Assert
        assertFalse(viewModel.getRefreshCommand().canExecute());
        
        // Arrange - set project
        viewModel.setProject(mockProject);
        
        // Act & Assert
        assertTrue(viewModel.getRefreshCommand().canExecute());
    }
    
    @Test
    void testRefreshCommand() {
        // Arrange
        viewModel.setProject(mockProject);
        
        // Act
        viewModel.getRefreshCommand().execute();
        
        // Assert
        verify(ganttDataService).formatTasksForGantt(
            eq(mockProject.getId()), 
            any(LocalDate.class), 
            any(LocalDate.class)
        );
        assertNotNull(viewModel.getChartData());
        assertTrue(viewModel.isDataLoaded());
    }
    
    @Test
    void testClearErrorMessage() {
        // Arrange - set error message via reflection since setErrorMessage is protected
        try {
            java.lang.reflect.Method method = BaseViewModel.class.getDeclaredMethod(
                "setErrorMessage", String.class);
            method.setAccessible(true);
            method.invoke(viewModel, "Test error");
        } catch (Exception e) {
            fail("Failed to set error message: " + e.getMessage());
        }
        
        // Act
        viewModel.clearErrorMessage();
        
        // Assert
        assertEquals("", viewModel.errorMessageProperty().get());
    }
}