// src/main/java/org/frcpm/services/impl/GanttChartTransformationServiceImpl.java
package org.frcpm.services.impl;

import org.frcpm.models.GanttChartData;
import org.frcpm.models.Milestone;
import org.frcpm.models.Task;
import org.frcpm.services.GanttChartTransformationService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Spring Boot implementation of GanttChartTransformationService.
 * Service for transforming domain entities to Gantt chart-ready data formats.
 * This service handles the conversion between domain models and presentation formats
 * required for Chart.js visualization.
 */
@Service("ganttChartTransformationService")
public class GanttChartTransformationServiceImpl implements GanttChartTransformationService {
    
    /**
     * Transforms a list of tasks into GanttChartData objects.
     * 
     * @param tasks the list of tasks to transform
     * @return a list of GanttChartData objects
     */
    @Override
    public List<GanttChartData> transformTasksToChartData(List<Task> tasks) {
        if (tasks == null) {
            return new ArrayList<>();
        }
        
        return tasks.stream()
            .map(GanttChartData::fromTask)
            .collect(Collectors.toList());
    }
    
    /**
     * Transforms a list of milestones into GanttChartData objects.
     * 
     * @param milestones the list of milestones to transform
     * @return a list of GanttChartData objects
     */
    @Override
    public List<GanttChartData> transformMilestonesToChartData(List<Milestone> milestones) {
        if (milestones == null) {
            return new ArrayList<>();
        }
        
        return milestones.stream()
            .map(GanttChartData::fromMilestone)
            .collect(Collectors.toList());
    }
    
    /**
     * Transforms GanttChartData objects into a format ready for Chart.js.
     * 
     * @param chartDataList the list of GanttChartData objects
     * @return a map containing the formatted data for Chart.js
     */
    @Override
    public Map<String, Object> transformToChartJsFormat(List<GanttChartData> chartDataList) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> datasets = new ArrayList<>();
        
        for (GanttChartData data : chartDataList) {
            Map<String, Object> dataset = new HashMap<>();
            dataset.put("id", data.getId());
            dataset.put("label", data.getTitle());
            dataset.put("backgroundColor", data.getColor());
            dataset.put("borderColor", adjustBorderColor(data.getColor()));
            dataset.put("borderWidth", 1);
            dataset.put("data", createDataPoint(data));
            dataset.put("progress", data.getProgress());
            dataset.put("type", data.getType());
            dataset.put("dependencies", data.getDependencies());
            
            if (data.getAssignee() != null) {
                dataset.put("assignee", data.getAssignee());
            }
            
            if (data.getSubsystem() != null) {
                dataset.put("subsystem", data.getSubsystem());
            }
            
            datasets.add(dataset);
        }
        
        result.put("datasets", datasets);
        return result;
    }
    
    /**
     * Creates a data point for Chart.js from GanttChartData.
     * 
     * @param data the GanttChartData
     * @return a list containing the data point
     */
    private List<Map<String, Object>> createDataPoint(GanttChartData data) {
        List<Map<String, Object>> dataPoints = new ArrayList<>();
        Map<String, Object> dataPoint = new HashMap<>();
        
        dataPoint.put("x", new Object[] {
            data.getStartDate().toString(),
            data.getEndDate().toString()
        });
        dataPoint.put("y", data.getTitle());
        
        dataPoints.add(dataPoint);
        return dataPoints;
    }
    
    /**
     * Adjusts a color for use as a border color (typically darker).
     * 
     * @param color the fill color
     * @return the border color
     */
    private String adjustBorderColor(String color) {
        // Simple implementation - could be enhanced with actual color manipulation
        if (color == null) {
            return "#666666";
        }
        
        // If it's an rgba color, increase opacity
        if (color.startsWith("rgba")) {
            return color.replace("rgba", "rgb").replace(",0.7)", ")");
        }
        
        // For hex colors, just return the original for now
        // A more sophisticated implementation would darken the hex color
        return color;
    }
    
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
    @Override
    public List<GanttChartData> filterChartData(
            List<GanttChartData> chartDataList,
            String filterType,
            String subteam,
            String subsystem,
            LocalDate startDate,
            LocalDate endDate) {
        
        if (chartDataList == null || chartDataList.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<GanttChartData> filteredList = new ArrayList<>(chartDataList);
        
        // Apply date range filter
        if (startDate != null) {
            filteredList = filteredList.stream()
                .filter(data -> !data.getEndDate().isBefore(startDate))
                .collect(Collectors.toList());
        }
        
        if (endDate != null) {
            filteredList = filteredList.stream()
                .filter(data -> !data.getStartDate().isAfter(endDate))
                .collect(Collectors.toList());
        }
        
        // Apply subsystem filter
        if (subsystem != null && !subsystem.isEmpty()) {
            filteredList = filteredList.stream()
                .filter(data -> subsystem.equals(data.getSubsystem()))
                .collect(Collectors.toList());
        }
        
        // Apply additional filters based on filter type
        if (filterType != null) {
            switch (filterType) {
                case "CRITICAL_PATH":
                    // For simplicity, just show items with red color (critical tasks)
                    filteredList = filteredList.stream()
                        .filter(data -> data.getColor() != null && data.getColor().contains("255"))
                        .collect(Collectors.toList());
                    break;
                case "BEHIND_SCHEDULE":
                    // Show tasks that are behind schedule (progress < expected)
                    LocalDate today = LocalDate.now();
                    filteredList = filteredList.stream()
                        .filter(data -> {
                            if (!"task".equals(data.getType())) {
                                return false;
                            }
                            
                            if (data.getStartDate().isAfter(today) || data.getEndDate().isBefore(today)) {
                                return false;
                            }
                            
                            long totalDays = data.getStartDate().until(data.getEndDate()).getDays();
                            if (totalDays == 0) {
                                return data.getProgress() < 100;
                            }
                            
                            long daysPassed = data.getStartDate().until(today).getDays();
                            int expectedProgress = (int) (daysPassed * 100 / totalDays);
                            return data.getProgress() < expectedProgress;
                        })
                        .collect(Collectors.toList());
                    break;
                // Add more filter types as needed
            }
        }
        
        return filteredList;
    }
    
    /**
     * Creates dependency relationship data for Chart.js.
     * 
     * @param tasks the list of tasks
     * @return a list of dependency objects
     */
    @Override
    public List<Map<String, Object>> createDependencyData(List<Task> tasks) {
        List<Map<String, Object>> dependencies = new ArrayList<>();
        
        for (Task task : tasks) {
            Set<Task> preDependencies = task.getPreDependencies();
            if (preDependencies != null && !preDependencies.isEmpty()) {
                for (Task dependency : preDependencies) {
                    Map<String, Object> dep = new HashMap<>();
                    dep.put("source", "task_" + dependency.getId());
                    dep.put("target", "task_" + task.getId());
                    dep.put("type", "finish-to-start");
                    dependencies.add(dep);
                }
            }
        }
        
        return dependencies;
    }
}