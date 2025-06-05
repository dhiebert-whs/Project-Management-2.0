// src/main/java/org/frcpm/services/GanttChartTransformationService.java
package org.frcpm.services;

import org.frcpm.models.GanttChartData;
import org.frcpm.models.Milestone;
import org.frcpm.models.Task;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service interface for transforming domain entities to Gantt chart-ready data formats.
 * This service handles the conversion between domain models and presentation formats
 * required for Chart.js visualization.
 */
public interface GanttChartTransformationService {
    
    /**
     * Transforms a list of tasks into GanttChartData objects.
     * 
     * @param tasks the list of tasks to transform
     * @return a list of GanttChartData objects
     */
    List<GanttChartData> transformTasksToChartData(List<Task> tasks);
    
    /**
     * Transforms a list of milestones into GanttChartData objects.
     * 
     * @param milestones the list of milestones to transform
     * @return a list of GanttChartData objects
     */
    List<GanttChartData> transformMilestonesToChartData(List<Milestone> milestones);
    
    /**
     * Transforms GanttChartData objects into a format ready for Chart.js.
     * 
     * @param chartDataList the list of GanttChartData objects
     * @return a map containing the formatted data for Chart.js
     */
    Map<String, Object> transformToChartJsFormat(List<GanttChartData> chartDataList);
    
    /**
     * Filters chart data based on specified criteria.
     * 
     * @param chartDataList the list of GanttChartData objects
     * @param filterType the type of filter to apply
     * @param subteam optional subteam to filter by
     * @param subsystem optional subsystem to filter by
     * @param startDate optional start date to filter by
     * @param endDate optional end date to filter by
     * @return the filtered list of GanttChartData objects
     */
    List<GanttChartData> filterChartData(
            List<GanttChartData> chartDataList,
            String filterType,
            String subteam,
            String subsystem,
            LocalDate startDate,
            LocalDate endDate);
    
    /**
     * Creates dependency relationship data for Chart.js.
     * 
     * @param tasks the list of tasks
     * @return a list of dependency objects
     */
    List<Map<String, Object>> createDependencyData(List<Task> tasks);
}