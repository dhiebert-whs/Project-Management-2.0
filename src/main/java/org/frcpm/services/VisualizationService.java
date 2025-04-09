package org.frcpm.services;

import java.util.List;
import java.util.Map;

/**
 * Service interface for visualization-related operations.
 * This service provides methods to generate visualization data for
 * various charts and dashboard components.
 */
public interface VisualizationService {

    /**
     * Get project completion percentage data.
     * 
     * @param projectId The ID of the project
     * @return Map containing completion data by category
     */
    Map<String, Object> getProjectCompletionData(Long projectId);
    
    /**
     * Get task status summary data.
     * 
     * @param projectId The ID of the project
     * @return Map containing task counts by status
     */
    Map<String, Integer> getTaskStatusSummary(Long projectId);
    
    /**
     * Get upcoming deadlines data.
     * 
     * @param projectId The ID of the project
     * @param daysAhead Number of days to look ahead
     * @return List of upcoming deadline data
     */
    List<Map<String, Object>> getUpcomingDeadlines(Long projectId, int daysAhead);
    
    /**
     * Get subsystem progress data.
     * 
     * @param projectId The ID of the project
     * @return Map of subsystem names to completion percentages
     */
    Map<String, Double> getSubsystemProgress(Long projectId);
    
    /**
     * Get at-risk tasks data.
     * 
     * @param projectId The ID of the project
     * @return List of at-risk task data
     */
    List<Map<String, Object>> getAtRiskTasks(Long projectId);
    
    /**
     * Generate SVG export data for visualization.
     * 
     * @param chartData The chart data to export
     * @param chartType The type of chart being exported
     * @return String containing SVG data
     */
    String generateSvgExport(Map<String, Object> chartData, String chartType);
    
    /**
     * Generate PDF report for visualization.
     * 
     * @param projectId The ID of the project
     * @param reportType The type of report to generate
     * @return Byte array containing PDF data
     */
    byte[] generatePdfReport(Long projectId, String reportType);
}