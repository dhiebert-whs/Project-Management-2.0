// src/test/java/org/frcpm/services/impl/WebViewBridgeServiceImplTest.java
package org.frcpm.services.impl;

import javafx.scene.web.WebEngine;
import org.frcpm.models.Milestone;
import org.frcpm.models.Task;
import org.frcpm.services.MilestoneService;
import org.frcpm.services.TaskService;
import org.frcpm.viewmodels.GanttChartViewModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class WebViewBridgeServiceImplTest {

    private WebViewBridgeServiceImpl bridgeService;
    
    @Mock
    private WebEngine mockWebEngine;
    
    @Mock
    private GanttChartViewModel mockViewModel;
    
    @Mock
    private TaskService mockTaskService;
    
    @Mock
    private MilestoneService mockMilestoneService;
    
    @Mock
    private Task mockTask;
    
    @Mock
    private Milestone mockMilestone;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Create test object with mock services
        bridgeService = new WebViewBridgeServiceImpl(mockTaskService, mockMilestoneService);
        
        // Set up mock response for services
        when(mockTaskService.findById(anyLong())).thenReturn(mockTask);
        when(mockMilestoneService.findById(anyLong())).thenReturn(mockMilestone);
    }
    
    @Test
    void testInitialize() {
        // Arrange
        when(mockWebEngine.executeScript(anyString())).thenReturn(null);
        
        // Act
        bridgeService.initialize(mockWebEngine, mockViewModel);
        
        // Nothing to verify explicitly - just make sure no exceptions are thrown
    }
    
    @Test
    void testUpdateChartData() {
        // Arrange
        Map<String, Object> testData = new HashMap<>();
        testData.put("test", "value");
        
        when(mockWebEngine.executeScript(anyString())).thenReturn(null);
        
        // Need to initialize first
        bridgeService.initialize(mockWebEngine, mockViewModel);
        
        // Act
        bridgeService.updateChartData(testData);
        
        // Only verify that script execution was attempted
        // (we can't verify the exact string because of JSON conversion)
        verify(mockWebEngine, atLeastOnce()).executeScript(anyString());
    }
    
    @Test
    void testHandleTaskSelection() {
        // Arrange
        Long taskId = 123L;
        
        // Need to initialize first
        bridgeService.initialize(mockWebEngine, mockViewModel);
        
        // Act
        bridgeService.handleTaskSelection(taskId);
        
        // Assert
        verify(mockTaskService).findById(taskId);
        
        // Note: Can't verify the Platform.runLater call directly
    }
    
    @Test
    void testHandleMilestoneSelection() {
        // Arrange
        Long milestoneId = 456L;
        
        // Need to initialize first
        bridgeService.initialize(mockWebEngine, mockViewModel);
        
        // Act
        bridgeService.handleMilestoneSelection(milestoneId);
        
        // Assert
        verify(mockMilestoneService).findById(milestoneId);
        
        // Note: Can't verify the Platform.runLater call directly
    }
    
    @Test
    void testUpdateViewMode() {
        // Arrange
        String viewMode = "Week";
        
        when(mockWebEngine.executeScript(anyString())).thenReturn(null);
        
        // Need to initialize first
        bridgeService.initialize(mockWebEngine, mockViewModel);
        
        // Act
        bridgeService.updateViewMode(viewMode);
        
        // Only verify that script execution was attempted
        verify(mockWebEngine, atLeastOnce()).executeScript(anyString());
    }
    
    @Test
    void testUpdateMilestonesVisibility() {
        // Arrange
        boolean visible = true;
        
        when(mockWebEngine.executeScript(anyString())).thenReturn(null);
        
        // Need to initialize first
        bridgeService.initialize(mockWebEngine, mockViewModel);
        
        // Act
        bridgeService.updateMilestonesVisibility(visible);
        
        // Only verify that script execution was attempted
        verify(mockWebEngine, atLeastOnce()).executeScript(anyString());
    }
    
    @Test
    void testUpdateDependenciesVisibility() {
        // Arrange
        boolean visible = false;
        
        when(mockWebEngine.executeScript(anyString())).thenReturn(null);
        
        // Need to initialize first
        bridgeService.initialize(mockWebEngine, mockViewModel);
        
        // Act
        bridgeService.updateDependenciesVisibility(visible);
        
        // Only verify that script execution was attempted
        verify(mockWebEngine, atLeastOnce()).executeScript(anyString());
    }
}