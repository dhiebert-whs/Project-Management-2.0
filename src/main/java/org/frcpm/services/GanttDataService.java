package org.frcpm.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;


/**
 * Service interface for Gantt chart data operations.
 * This service provides methods to create, format, and analyze
 * task data for Gantt chart visualizations.
 */
public interface GanttDataService {

    /**
     * Format tasks data for Gantt chart visualization.
     * 
     * @param projectId The ID of the project
     * @param startDate Optional start date filter
     * @param endDate Optional end date filter
     * @return Map containing formatted Gantt chart data
     */
    Map<String, Object> formatTasksForGantt(Long projectId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Apply filters to Gantt chart data.
     * 
     * @param ganttData The Gantt chart data to filter
     * @param filterCriteria Map containing filter criteria
     * @return Filtered Gantt chart data
     */
    Map<String, Object> applyFiltersToGanttData(Map<String, Object> ganttData, Map<String, Object> filterCriteria);
    
    /**
     * Calculate critical path for project tasks.
     * 
     * @param projectId The ID of the project
     * @return List of task IDs that form the critical path
     */
    List<Long> calculateCriticalPath(Long projectId);
    
    /**
     * Get Gantt data for a specific date (daily view).
     * 
     * @param projectId The ID of the project
     * @param date The date to retrieve data for
     * @return Map containing Gantt data for specified date
     */
    Map<String, Object> getGanttDataForDate(Long projectId, LocalDate date);
    
    /**
     * Get dependencies between tasks.
     * 
     * @param projectId The ID of the project
     * @return Map of task IDs to lists of dependent task IDs
     */
    Map<Long, List<Long>> getTaskDependencies(Long projectId);
    
    /**
     * Identify bottleneck tasks in the project.
     * 
     * @param projectId The ID of the project
     * @return List of task IDs identified as bottlenecks
     */
    List<Long> identifyBottlenecks(Long projectId);

    /**
     * Get the transformation service used by this data service.
     * 
     * @return The GanttChartTransformationService instance
     */
    GanttChartTransformationService getTransformationService();
}