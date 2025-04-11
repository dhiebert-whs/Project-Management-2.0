package org.frcpm.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data model for Gantt chart visualization.
 * This class is used to transfer task data to the Chart.js visualization.
 */
public class GanttChartData {
    
    private String id;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private int progress;
    private String type; // "task", "milestone", etc.
    private String status;
    private List<String> dependencies;
    private String assignee;
    private String subsystem;
    private String color;
    
    // Constructors
    
    public GanttChartData() {
        this.dependencies = new ArrayList<>();
    }
    
    public GanttChartData(String id, String title, LocalDate startDate, LocalDate endDate) {
        this();
        this.id = id;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
    }
    
    // Create from Task
    public static GanttChartData fromTask(Task task) {
        GanttChartData data = new GanttChartData(
                "task_" + task.getId(),
                task.getTitle(),
                task.getStartDate(),
                task.getEndDate());
        
        data.setProgress(task.getProgress());
        data.setType("task");
        data.setStatus(task.isCompleted() ? "completed" : "in-progress");
        
        // Set dependencies
        for (Task dependency : task.getPreDependencies()) {
            data.addDependency("task_" + dependency.getId());
        }
        
        // Set assignee (first team member if multiple)
        if (!task.getAssignedTo().isEmpty()) {
            data.setAssignee(task.getAssignedTo().iterator().next().getFullName());
        }
        
        // Set subsystem
        if (task.getSubsystem() != null) {
            data.setSubsystem(task.getSubsystem().getName());
        }
        
        // Set color based on priority
        switch (task.getPriority()) {
            case LOW:
                data.setColor("#28a745"); // Green
                break;
            case MEDIUM:
                data.setColor("#ffc107"); // Yellow
                break;
            case HIGH:
                data.setColor("#fd7e14"); // Orange
                break;
            case CRITICAL:
                data.setColor("#dc3545"); // Red
                break;
        }
        
        return data;
    }
    
    // Create from Milestone
    public static GanttChartData fromMilestone(Milestone milestone) {
        GanttChartData data = new GanttChartData(
                "milestone_" + milestone.getId(),
                milestone.getName(),
                milestone.getDate(),
                milestone.getDate());
        
        data.setProgress(milestone.isPassed() ? 100 : 0);
        data.setType("milestone");
        data.setStatus(milestone.isPassed() ? "completed" : "pending");
        data.setColor("#6f42c1"); // Purple
        
        return data;
    }
    
    // Getters and Setters
    
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
        this.progress = progress;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
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
        this.dependencies = dependencies;
    }
    
    public void addDependency(String dependency) {
        this.dependencies.add(dependency);
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
}