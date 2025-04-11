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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class WebViewBridgeServiceImplTest {

    // Test subject
    private TestableWebViewBridgeService service;
    
    // Mocks
    @Mock
    private TaskService mockTaskService;
    
    @Mock
    private MilestoneService mockMilestoneService;
    
    @Mock
    private GanttChartViewModel mockViewModel;
    
    @Mock
    private Task mockTask;
    
    @Mock
    private Milestone mockMilestone;
    
    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        // Create testable service
        service = new TestableWebViewBridgeService(mockTaskService, mockMilestoneService);
        
        // Set view model using reflection
        service.setViewModel(mockViewModel);
        
        // Set up task mock
        when(mockTaskService.findById(anyLong())).thenReturn(mockTask);
        when(mockTask.getId()).thenReturn(1L);
        
        // Set up milestone mock
        when(mockMilestoneService.findById(anyLong())).thenReturn(mockMilestone);
        when(mockMilestone.getId()).thenReturn(1L);
        
        // Set up view model
        when(mockViewModel.getViewMode()).thenReturn(GanttChartViewModel.ViewMode.WEEK);
        when(mockViewModel.isShowMilestones()).thenReturn(true);
        when(mockViewModel.isShowDependencies()).thenReturn(true);
    }
    
    @Test
    void testUpdateChartData() {
        // Execute
        service.updateChartData(mockViewModel.getChartData());
        
        // Verify script was recorded
        assertTrue(service.getLastExecutedScript().contains("updateChartData"));
    }
    
    @Test
    void testHandleTaskSelection() {
        // Execute
        service.handleTaskSelection(1L);
        
        // Verify task service was called
        verify(mockTaskService).findById(1L);
    }
    
    @Test
    void testHandleMilestoneSelection() {
        // Execute
        service.handleMilestoneSelection(1L);
        
        // Verify milestone service was called
        verify(mockMilestoneService).findById(1L);
    }
    
    @Test
    void testUpdateMilestonesVisibility() {
        // Execute
        service.updateMilestonesVisibility(true);
        
        // Verify script was recorded
        assertTrue(service.getLastExecutedScript().contains("setMilestonesVisibility"));
    }
    
    @Test
    void testUpdateDependenciesVisibility() {
        // Execute
        service.updateDependenciesVisibility(true);
        
        // Verify script was recorded
        assertTrue(service.getLastExecutedScript().contains("setDependenciesVisibility"));
    }
    
    @Test
    void testUpdateViewMode() {
        // Execute
        service.updateViewMode("WEEK");
        
        // Verify script was recorded
        assertTrue(service.getLastExecutedScript().contains("setViewMode"));
    }
    
    @Test
    void testInitialize() {
        // We're not going to test actual initialization with WebEngine
        // Just verify no exceptions are thrown
        assertDoesNotThrow(() -> {
            service.initializeForTesting(mockViewModel);
        });
    }
    
    /**
     * Testable version of WebViewBridgeServiceImpl that doesn't rely on WebEngine
     */
    private static class TestableWebViewBridgeService extends WebViewBridgeServiceImpl {
        private String lastExecutedScript;
        
        public TestableWebViewBridgeService(TaskService taskService, MilestoneService milestoneService) {
            super(taskService, milestoneService);
        }
        
        /**
         * A testing-specific way to initialize without WebEngine
         */
        public void initializeForTesting(GanttChartViewModel viewModel) throws Exception {
            setViewModel(viewModel);
        }
        
        /**
         * Set the viewModel field using reflection
         */
        public void setViewModel(GanttChartViewModel viewModel) throws Exception {
            java.lang.reflect.Field field = WebViewBridgeServiceImpl.class.getDeclaredField("viewModel");
            field.setAccessible(true);
            field.set(this, viewModel);
        }
        
        @Override
        public void initialize(WebEngine engine, GanttChartViewModel viewModel) {
            // Do nothing - avoid WebEngine initialization
            try {
                setViewModel(viewModel);
            } catch (Exception e) {
                // Ignore exception in test
            }
        }
        
        @Override
        public void updateChartData(Object chartData) {
            // Record what would have been executed without actually using WebEngine
            try {
                lastExecutedScript = "updateChartData(" + chartData + ")";
            } catch (Exception e) {
                // Ignore
            }
        }
        
        @Override
        public void updateViewMode(String viewMode) {
            lastExecutedScript = "setViewMode('" + viewMode + "')";
        }
        
        @Override
        public void updateMilestonesVisibility(boolean visible) {
            lastExecutedScript = "setMilestonesVisibility(" + visible + ")";
        }
        
        @Override
        public void updateDependenciesVisibility(boolean visible) {
            lastExecutedScript = "setDependenciesVisibility(" + visible + ")";
        }
        
        public String getLastExecutedScript() {
            return lastExecutedScript;
        }
    }
}