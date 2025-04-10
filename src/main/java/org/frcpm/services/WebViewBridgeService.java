// src/main/java/org/frcpm/services/WebViewBridgeService.java
package org.frcpm.services;

import javafx.scene.web.WebEngine;
import org.frcpm.viewmodels.GanttChartViewModel;

/**
 * Service interface for the bridge between JavaFX and JavaScript.
 * This service handles communication between the JavaFX application and 
 * the JavaScript code running in a WebView.
 */
public interface WebViewBridgeService {
    
    /**
     * Initializes the bridge between JavaFX and JavaScript.
     * 
     * @param engine the WebEngine to initialize
     * @param viewModel the ViewModel to connect
     */
    void initialize(WebEngine engine, GanttChartViewModel viewModel);
    
    /**
     * Updates the chart data in the WebView.
     * 
     * @param chartData the chart data to update
     */
    void updateChartData(Object chartData);
    
    /**
     * Handles task selection from the chart.
     * 
     * @param taskId the ID of the selected task
     */
    void handleTaskSelection(Long taskId);
    
    /**
     * Handles milestone selection from the chart.
     * 
     * @param milestoneId the ID of the selected milestone
     */
    void handleMilestoneSelection(Long milestoneId);
    
    /**
     * Updates the view mode in the chart.
     * 
     * @param viewMode the view mode to set
     */
    void updateViewMode(String viewMode);
    
    /**
     * Updates the visibility of milestones in the chart.
     * 
     * @param visible whether milestones should be visible
     */
    void updateMilestonesVisibility(boolean visible);
    
    /**
     * Updates the visibility of dependencies in the chart.
     * 
     * @param visible whether dependencies should be visible
     */
    void updateDependenciesVisibility(boolean visible);
    
    /**
     * Exports the chart as an image or data file.
     * 
     * @param format the export format (e.g., "png", "json")
     * @return the exported data
     */
    byte[] exportChart(String format);
}