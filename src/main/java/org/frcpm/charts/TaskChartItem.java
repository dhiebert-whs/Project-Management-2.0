// src/main/java/org/frcpm/charts/TaskChartItem.java
package org.frcpm.charts;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.frcpm.models.GanttChartData;

/**
 * Data model for Chart-FX Gantt chart items.
 * This class represents a task or milestone item on a Gantt chart.
 */
public class TaskChartItem {
    
    private String id;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private int progress;
    private TaskType type;
    private String status;
    private List<String> dependencies;
    private String assignee;
    private String subsystem;
    private String color;
    
    /**
     * Enum representing the type of chart item.
     */
    public enum TaskType {
        TASK,
        MILESTONE
    }
    
    /**
     * Creates a new empty TaskChartItem.
     */
    public TaskChartItem() {
        this.dependencies = new ArrayList<>();
        this.progress = 0;
        this.type = TaskType.TASK;
    }
    
    /**
     * Creates a new TaskChartItem with the specified parameters.
     * 
     * @param id the item ID
     * @param title the item title
     * @param startDate the start date
     * @param endDate the end date
     * @param type the item type
     */
    public TaskChartItem(String id, String title, LocalDate startDate, LocalDate endDate, TaskType type) {
        this();
        this.id = id;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.type = type;
    }
    
    /**
     * Creates a task chart item from GanttChartData.
     * 
     * @param data the GanttChartData to convert
     * @return the TaskChartItem
     */
    public static TaskChartItem fromGanttChartData(GanttChartData data) {
        if (data == null) {
            return null;
        }
        
        TaskChartItem item = new TaskChartItem();
        item.setId(data.getId());
        item.setTitle(data.getTitle());
        item.setStartDate(data.getStartDate());
        item.setEndDate(data.getEndDate());
        item.setProgress(data.getProgress());
        
        // Set type based on GanttChartData type
        if ("milestone".equals(data.getType())) {
            item.setType(TaskType.MILESTONE);
        } else {
            item.setType(TaskType.TASK);
        }
        
        item.setStatus(data.getStatus());
        item.setDependencies(new ArrayList<>(data.getDependencies()));
        item.setAssignee(data.getAssignee());
        item.setSubsystem(data.getSubsystem());
        item.setColor(data.getColor());
        
        return item;
    }
    
    /**
     * Creates a GanttChartData from this TaskChartItem.
     * 
     * @return the GanttChartData
     */
    public GanttChartData toGanttChartData() {
        GanttChartData data = new GanttChartData(id, title, startDate, endDate);
        data.setProgress(progress);
        data.setType(type == TaskType.MILESTONE ? "milestone" : "task");
        data.setStatus(status);
        
        if (dependencies != null) {
            for (String dependency : dependencies) {
                data.addDependency(dependency);
            }
        }
        
        data.setAssignee(assignee);
        data.setSubsystem(subsystem);
        data.setColor(color);
        
        return data;
    }
    
    /**
     * Gets the duration in days between start and end date.
     * 
     * @return the duration in days
     */
    public long getDurationDays() {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return startDate.until(endDate).getDays() + 1; // inclusive
    }
    
    /**
     * Checks if this item is on a specific date.
     * 
     * @param date the date to check
     * @return true if the item is on the date
     */
    public boolean isOnDate(LocalDate date) {
        if (date == null || startDate == null || endDate == null) {
            return false;
        }
        
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }
    
    /**
     * Checks if this item is completed.
     * 
     * @return true if the item is completed
     */
    public boolean isCompleted() {
        return progress >= 100 || "completed".equals(status);
    }
    
    // Getters and setters
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = Math.min(100, Math.max(0, progress));
    }

    public TaskType getType() {
        return type;
    }

    public void setType(TaskType type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies != null ? dependencies : new ArrayList<>();
    }
    
    public void addDependency(String dependency) {
        if (dependency != null && !dependencies.contains(dependency)) {
            dependencies.add(dependency);
        }
    }
    
    public boolean removeDependency(String dependency) {
        return dependencies.remove(dependency);
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public String getSubsystem() {
        return subsystem;
    }

    public void setSubsystem(String subsystem) {
        this.subsystem = subsystem;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
    
    @Override
    public String toString() {
        return title + " (" + id + ")";
    }
}