// src/main/java/org/frcpm/services/impl/WebViewBridgeServiceImpl.java
package org.frcpm.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import netscape.javascript.JSObject;
import org.frcpm.services.ServiceFactory;
import org.frcpm.services.TaskService;
import org.frcpm.mvvm.viewmodels.GanttChartMvvmViewModel;
import org.frcpm.services.MilestoneService;
import org.frcpm.services.WebViewBridgeService;


import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the WebViewBridgeService interface.
 * Handles communication between JavaFX and JavaScript in the WebView.
 */
public class WebViewBridgeServiceImpl implements WebViewBridgeService {
    
    private static final Logger LOGGER = Logger.getLogger(WebViewBridgeServiceImpl.class.getName());
    
    private WebEngine webEngine;
    private GanttChartMvvmViewModel viewModel;
    private final TaskService taskService;
    private final MilestoneService milestoneService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Creates a new WebViewBridgeServiceImpl with default services.
     */
    public WebViewBridgeServiceImpl() {
        this.taskService = ServiceFactory.getTaskService();
        this.milestoneService = ServiceFactory.getMilestoneService();
    }
    
    /**
     * Creates a new WebViewBridgeServiceImpl with specified services.
     * This constructor is mainly used for testing.
     * 
     * @param taskService the task service
     * @param milestoneService the milestone service
     */
    public WebViewBridgeServiceImpl(TaskService taskService, MilestoneService milestoneService) {
        this.taskService = taskService;
        this.milestoneService = milestoneService;
    }
    
    @Override
    public void initialize(WebEngine engine, GanttChartMvvmViewModel viewModel) {
        this.webEngine = engine;
        this.viewModel = viewModel;
        
        try {
            // Get the JavaScript window object
            JSObject window = (JSObject) webEngine.executeScript("window");
            
            // Create a bridge object to expose Java methods to JavaScript
            JavaBridge bridge = new JavaBridge();
            
            // Add the bridge to the JavaScript window
            window.setMember("javaBridge", bridge);
            
            // Initialize listeners in the view model
            initializeViewModelListeners();
            
            LOGGER.info("Bridge initialized successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing bridge", e);
        }
    }
    
// src/main/java/org/frcpm/services/impl/WebViewBridgeServiceImpl.java (continued)

    /**
     * Initializes listeners on the view model properties.
     */
    private void initializeViewModelListeners() {
        // Listen for chart data changes
        viewModel.chartDataProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateChartData(newVal);
            }
        });
        
        // Listen for view mode changes
        viewModel.viewModeProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateViewMode(newVal.toString());
            }
        });
        
        // Listen for visibility changes
        viewModel.showMilestonesProperty().addListener((obs, oldVal, newVal) -> {
            updateMilestonesVisibility(newVal);
        });
        
        viewModel.showDependenciesProperty().addListener((obs, oldVal, newVal) -> {
            updateDependenciesVisibility(newVal);
        });
    }
    
    @Override
    public void updateChartData(Object chartData) {
        if (webEngine == null) {
            LOGGER.warning("WebEngine not initialized");
            return;
        }
        
        try {
            // Convert chart data to JSON
            String json = objectMapper.writeValueAsString(chartData);
            
            // Update chart data in JavaScript
            Platform.runLater(() -> {
                try {
                    webEngine.executeScript("updateChartData(" + json + ")");
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error updating chart data in JavaScript", e);
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error converting chart data to JSON", e);
        }
    }
    
    @Override
    public void handleTaskSelection(Long taskId) {
        if (taskId == null || viewModel == null) {
            return;
        }
        
        try {
            // Load task by ID
            var task = taskService.findById(taskId);
            
            // Update selected task in view model
            Platform.runLater(() -> {
                viewModel.setSelectedTask(task);
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error handling task selection", e);
        }
    }
    
    @Override
    public void handleMilestoneSelection(Long milestoneId) {
        if (milestoneId == null || viewModel == null) {
            return;
        }
        
        try {
            // Load milestone by ID
            var milestone = milestoneService.findById(milestoneId);
            
            // Update selected milestone in view model
            Platform.runLater(() -> {
                viewModel.setSelectedMilestone(milestone);
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error handling milestone selection", e);
        }
    }
    
    @Override
    public void updateViewMode(String viewMode) {
        if (webEngine == null) {
            LOGGER.warning("WebEngine not initialized");
            return;
        }
        
        try {
            // Update view mode in JavaScript
            Platform.runLater(() -> {
                try {
                    webEngine.executeScript("setViewMode('" + viewMode + "')");
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error updating view mode in JavaScript", e);
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating view mode", e);
        }
    }
    
    @Override
    public void updateMilestonesVisibility(boolean visible) {
        if (webEngine == null) {
            LOGGER.warning("WebEngine not initialized");
            return;
        }
        
        try {
            // Update milestones visibility in JavaScript
            Platform.runLater(() -> {
                try {
                    webEngine.executeScript("setMilestonesVisibility(" + visible + ")");
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error updating milestones visibility in JavaScript", e);
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating milestones visibility", e);
        }
    }
    
    @Override
    public void updateDependenciesVisibility(boolean visible) {
        if (webEngine == null) {
            LOGGER.warning("WebEngine not initialized");
            return;
        }
        
        try {
            // Update dependencies visibility in JavaScript
            Platform.runLater(() -> {
                try {
                    webEngine.executeScript("setDependenciesVisibility(" + visible + ")");
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error updating dependencies visibility in JavaScript", e);
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating dependencies visibility", e);
        }
    }
    
    @Override
    public byte[] exportChart(String format) {
        if (webEngine == null) {
            LOGGER.warning("WebEngine not initialized");
            return new byte[0];
        }
        
        try {
            // Call export function in JavaScript
            Object result = webEngine.executeScript("exportChart('" + format + "')");
            
            // Convert result to byte array
            if (result instanceof String) {
                String base64 = (String) result;
                // Remove data URL prefix if present
                if (base64.startsWith("data:")) {
                    base64 = base64.substring(base64.indexOf(",") + 1);
                }
                return java.util.Base64.getDecoder().decode(base64);
            }
            
            return new byte[0];
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error exporting chart", e);
            return new byte[0];
        }
    }
    
    /**
     * JavaBridge class for exposing Java methods to JavaScript.
     */
    public class JavaBridge {
        
        /**
         * Called from JavaScript when a task is selected.
         * 
         * @param taskId the ID of the selected task
         */
        public void onTaskSelected(String taskId) {
            try {
                Long id = Long.parseLong(taskId);
                handleTaskSelection(id);
            } catch (NumberFormatException e) {
                LOGGER.log(Level.WARNING, "Invalid task ID: " + taskId, e);
            }
        }
        
        /**
         * Called from JavaScript when a milestone is selected.
         * 
         * @param milestoneId the ID of the selected milestone
         */
        public void onMilestoneSelected(String milestoneId) {
            try {
                Long id = Long.parseLong(milestoneId);
                handleMilestoneSelection(id);
            } catch (NumberFormatException e) {
                LOGGER.log(Level.WARNING, "Invalid milestone ID: " + milestoneId, e);
            }
        }
        
        /**
         * Called from JavaScript to log messages.
         * 
         * @param message the message to log
         */
        public void log(String message) {
            LOGGER.info("JS: " + message);
        }
        
        /**
         * Called from JavaScript to log errors.
         * 
         * @param error the error to log
         */
        public void logError(String error) {
            LOGGER.severe("JS Error: " + error);
        }
        
        /**
         * Called from JavaScript when the chart is ready.
         */
        public void onChartReady() {
            LOGGER.info("Chart ready");
            
            // If we have data, update it
            if (viewModel.getChartData() != null) {
                updateChartData(viewModel.getChartData());
            }
            
            // Update view mode
            updateViewMode(viewModel.getViewMode().toString());
            
            // Update visibility settings
            updateMilestonesVisibility(viewModel.isShowMilestones());
            updateDependenciesVisibility(viewModel.isShowDependencies());
        }
    }


}