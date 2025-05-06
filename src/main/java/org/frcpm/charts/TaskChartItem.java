// src/main/java/org/frcpm/charts/TaskChartItem.java
package org.frcpm.charts;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Data model for Chart-FX chart items.
 * This class represents a single task or milestone in the Gantt chart.
 */
public class TaskChartItem {
    private final StringProperty id = new SimpleStringProperty();
    private final StringProperty title = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> startDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> endDate = new SimpleObjectProperty<>();
    private final IntegerProperty progress = new SimpleIntegerProperty();
    private final StringProperty type = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();
    private final List<String> dependencies = new ArrayList<>();
    private final StringProperty assignee = new SimpleStringProperty();
    private final StringProperty subsystem = new SimpleStringProperty();
    private final StringProperty color = new SimpleStringProperty();
    
    /**
     * Creates a new TaskChartItem.
     */
    public TaskChartItem() {
    }
    
    /**
     * Creates a new TaskChartItem with the specified values.
     * 
     * @param id the ID of the task or milestone
     * @param title the title of the task or milestone
     * @param startDate the start date of the task or milestone
     * @param endDate the end date of the task or milestone
     */
    public TaskChartItem(String id, String title, LocalDate startDate, LocalDate endDate) {
        this.id.set(id);
        this.title.set(title);
        this.startDate.set(startDate);
        this.endDate.set(endDate);
    }
    
    /**
     * Creates a TaskChartItem from a GanttChartData object.
     * 
     * @param data the GanttChartData object
     * @return the TaskChartItem
     */
    public static TaskChartItem fromGanttChartData(org.frcpm.models.GanttChartData data) {
        TaskChartItem item = new TaskChartItem(
                data.getId(),
                data.getTitle(),
                data.getStartDate(),
                data.getEndDate()
        );
        
        item.setProgress(data.getProgress());
        item.setType(data.getType());
        item.setStatus(data.getStatus());
        item.setDependencies(data.getDependencies());
        item.setAssignee(data.getAssignee());
        item.setSubsystem(data.getSubsystem());
        item.setColor(data.getColor());
        
        return item;
    }
    
    // Getters and Setters with JavaFX Properties
    
    public String getId() {
        return id.get();
    }
    
    public void setId(String id) {
        this.id.set(id);
    }
    
    public StringProperty idProperty() {
        return id;
    }
    
    public String getTitle() {
        return title.get();
    }
    
    public void setTitle(String title) {
        this.title.set(title);
    }
    
    public StringProperty titleProperty() {
        return title;
    }
    
    public LocalDate getStartDate() {
        return startDate.get();
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate.set(startDate);
    }
    
    public ObjectProperty<LocalDate> startDateProperty() {
        return startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate.get();
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate.set(endDate);
    }
    
    public ObjectProperty<LocalDate> endDateProperty() {
        return endDate;
    }
    
    public int getProgress() {
        return progress.get();
    }
    
    public void setProgress(int progress) {
        this.progress.set(progress);
    }
    
    public IntegerProperty progressProperty() {
        return progress;
    }
    
    public String getType() {
        return type.get();
    }
    
    public void setType(String type) {
        this.type.set(type);
    }
    
    public StringProperty typeProperty() {
        return type;
    }
    
    public String getStatus() {
        return status.get();
    }
    
    public void setStatus(String status) {
        this.status.set(status);
    }
    
    public StringProperty statusProperty() {
        return status;
    }
    
    public List<String> getDependencies() {
        return dependencies;
    }
    
    public void setDependencies(List<String> dependencies) {
        this.dependencies.clear();
        if (dependencies != null) {
            this.dependencies.addAll(dependencies);
        }
    }
    
    public void addDependency(String dependency) {
        this.dependencies.add(dependency);
    }
    
    public String getAssignee() {
        return assignee.get();
    }
    
    public void setAssignee(String assignee) {
        this.assignee.set(assignee);
    }
    
    public StringProperty assigneeProperty() {
        return assignee;
    }
    
    public String getSubsystem() {
        return subsystem.get();
    }
    
    public void setSubsystem(String subsystem) {
        this.subsystem.set(subsystem);
    }
    
    public StringProperty subsystemProperty() {
        return subsystem;
    }
    
    public String getColor() {
        return color.get();
    }
    
    public void setColor(String color) {
        this.color.set(color);
    }
    
    public StringProperty colorProperty() {
        return color;
    }
    
    /**
     * Determines if this is a milestone (i.e., a point in time rather than a duration).
     * 
     * @return true if this is a milestone, false otherwise
     */
    public boolean isMilestone() {
        return "milestone".equals(getType());
    }
    
    /**
     * Determines if this task is completed.
     * 
     * @return true if this task is completed, false otherwise
     */
    public boolean isCompleted() {
        return "completed".equals(getStatus()) || getProgress() >= 100;
    }
}