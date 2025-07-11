// src/main/java/org/frcpm/web/dto/TaskUpdateMessage.java

package org.frcpm.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * WebSocket message for real-time task updates.
 * 
 * ✅ PHASE 2C: PWA Development - Real-time Task Communication
 * 
 * This DTO carries task update information across WebSocket connections
 * to provide instant feedback for team collaboration during build season.
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 * @since Phase 2C - Progressive Web App Development
 */
public class TaskUpdateMessage {
    
    @NotNull
    private Long taskId;
    
    @NotNull
    private Long projectId;
    
    @NotNull
    private String taskTitle;
    
    @Min(0)
    @Max(100)
    private Integer progress;
    
    private String status; // CREATED, UPDATED, DELETED, PROGRESS_CHANGED, COMPLETED
    
    private String updatedBy; // User who made the change
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    private String changeType; // Specific type of change
    
    private String priority; // Task priority for UI styling
    
    private String subsystemName; // Context for better notifications
    
    private Long assigneeId; // For targeted notifications
    
    private String assigneeName; // For display purposes

    // ADD THESE NEW FIELDS FOR KANBAN SUPPORT:

    private String oldStatus; // Previous Kanban status
    private String newStatus; // New Kanban status  
    private Integer oldPosition; // Previous position in column
    private Integer newPosition; // New position in column
    private String kanbanColumn; // Current Kanban column
    
    // Default constructor
    public TaskUpdateMessage() {
        this.timestamp = LocalDateTime.now();
    }
    
    // Main constructor for task updates
    public TaskUpdateMessage(Long taskId, Long projectId, String taskTitle, 
                           Integer progress, String status, String updatedBy, String changeType) {
        this();
        this.taskId = taskId;
        this.projectId = projectId;
        this.taskTitle = taskTitle;
        this.progress = progress;
        this.status = status;
        this.updatedBy = updatedBy;
        this.changeType = changeType;
    }
    
    // Factory methods for common scenarios
    public static TaskUpdateMessage progressUpdate(Long taskId, Long projectId, String taskTitle,
                                                  Integer progress, String updatedBy) {
        return new TaskUpdateMessage(taskId, projectId, taskTitle, progress, 
                                   progress >= 100 ? "COMPLETED" : "IN_PROGRESS", 
                                   updatedBy, "PROGRESS_CHANGED");
    }
    
    public static TaskUpdateMessage taskCreated(Long taskId, Long projectId, String taskTitle, String updatedBy) {
        return new TaskUpdateMessage(taskId, projectId, taskTitle, 0, "CREATED", updatedBy, "CREATED");
    }
    
    public static TaskUpdateMessage taskCompleted(Long taskId, Long projectId, String taskTitle, String updatedBy) {
        return new TaskUpdateMessage(taskId, projectId, taskTitle, 100, "COMPLETED", updatedBy, "COMPLETED");
    }

    // ADD THESE NEW FACTORY METHODS FOR KANBAN OPERATIONS:

    /**
     * Factory method for Kanban drag-and-drop moves.
     */
    public static TaskUpdateMessage kanbanMove(Long taskId, Long projectId, String taskTitle,
        String oldStatus, String newStatus, 
        Integer newProgress, String updatedBy) {
        TaskUpdateMessage message = new TaskUpdateMessage(taskId, projectId, taskTitle, 
                newProgress, "KANBAN_MOVED", updatedBy, "KANBAN_MOVED");
        message.setOldStatus(oldStatus);
        message.setNewStatus(newStatus);
        message.setKanbanColumn(newStatus);
        return message;
    }

    /**
    * Factory method for task status changes.
    */
    public static TaskUpdateMessage statusChanged(Long taskId, Long projectId, String taskTitle,
        String newStatus, Integer progress, String updatedBy) {
        String changeType = "COMPLETED".equals(newStatus) ? "COMPLETED" : "STATUS_CHANGED";
        TaskUpdateMessage message = new TaskUpdateMessage(taskId, projectId, taskTitle, 
                progress, newStatus, updatedBy, changeType);
        message.setNewStatus(newStatus);
        message.setKanbanColumn(newStatus);
        return message;
    }

    /**
    * Factory method for bulk operations.
    */
    public static TaskUpdateMessage bulkOperation(String operation, int taskCount, String updatedBy) {
        TaskUpdateMessage message = new TaskUpdateMessage();
        message.setChangeType("BULK_OPERATION");
        message.setUpdatedBy(updatedBy);
        message.setTaskTitle(String.format("Bulk %s on %d tasks", operation, taskCount));
        message.setStatus("BULK_" + operation.toUpperCase());
        return message;
    }
    
    // Getters and setters
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    
    public String getTaskTitle() { return taskTitle; }
    public void setTaskTitle(String taskTitle) { this.taskTitle = taskTitle; }
    
    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getChangeType() { return changeType; }
    public void setChangeType(String changeType) { this.changeType = changeType; }
    
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    
    public String getSubsystemName() { return subsystemName; }
    public void setSubsystemName(String subsystemName) { this.subsystemName = subsystemName; }
    
    public Long getAssigneeId() { return assigneeId; }
    public void setAssigneeId(Long assigneeId) { this.assigneeId = assigneeId; }
    
    public String getAssigneeName() { return assigneeName; }
    public void setAssigneeName(String assigneeName) { this.assigneeName = assigneeName; }

    public String getOldStatus() { return oldStatus; }
    public void setOldStatus(String oldStatus) { this.oldStatus = oldStatus; }
    
    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
    
    public Integer getOldPosition() { return oldPosition; }
    public void setOldPosition(Integer oldPosition) { this.oldPosition = oldPosition; }
    
    public Integer getNewPosition() { return newPosition; }
    public void setNewPosition(Integer newPosition) { this.newPosition = newPosition; }
    
    public String getKanbanColumn() { return kanbanColumn; }
    public void setKanbanColumn(String kanbanColumn) { this.kanbanColumn = kanbanColumn; }
    
    
    @Override
    public String toString() {
        return String.format("TaskUpdateMessage{taskId=%d, changeType='%s', status='%s', " +
                            "kanbanColumn='%s', updatedBy='%s', timestamp=%s}", 
                           taskId, changeType, status, kanbanColumn, updatedBy, timestamp);
    }
}