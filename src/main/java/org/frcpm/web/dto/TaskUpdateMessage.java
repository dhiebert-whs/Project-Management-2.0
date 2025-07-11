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
 * ✅ PHASE 2E-C: Enhanced with Kanban Board Support
 * ✅ FIXED: Added missing Kanban fields for drag-and-drop operations
 * 
 * This DTO carries task update information across WebSocket connections
 * to provide instant feedback for team collaboration during build season.
 * Now includes comprehensive Kanban board support for real-time updates.
 * 
 * @author FRC Project Management Team - Phase 2E-C Enhanced
 * @version 2.0.0-2E-C-KANBAN
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

    // ✅ NEW: KANBAN BOARD SUPPORT FIELDS - Phase 2E-C
    private String oldStatus; // Previous Kanban status (TODO, IN_PROGRESS, REVIEW, COMPLETED)
    private String newStatus; // New Kanban status after move
    private Integer oldPosition; // Previous position in column
    private Integer newPosition; // New position in column
    private String kanbanColumn; // Current Kanban column for UI updates
    
    // Additional Kanban context fields
    private String kanbanAction; // MOVE, REORDER, STATUS_CHANGE for detailed tracking
    private String sourceColumn; // Source column for cross-column moves
    private String targetColumn; // Target column for cross-column moves
    
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
    
    // =========================================================================
    // FACTORY METHODS - Enhanced with Kanban Support
    // =========================================================================
    
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

    /**
     * ✅ NEW: Factory method for Kanban drag-and-drop moves.
     * 
     * Creates a comprehensive Kanban move message with all necessary context
     * for real-time board synchronization across connected clients.
     * 
     * @param taskId Task being moved
     * @param projectId Project containing the task
     * @param taskTitle Task title for display
     * @param oldStatus Previous Kanban status
     * @param newStatus New Kanban status
     * @param newProgress Updated progress based on status
     * @param updatedBy User performing the move
     * @return Configured TaskUpdateMessage for Kanban move
     */
    public static TaskUpdateMessage kanbanMove(Long taskId, Long projectId, String taskTitle,
                                             String oldStatus, String newStatus, 
                                             Integer newProgress, String updatedBy) {
        TaskUpdateMessage message = new TaskUpdateMessage(taskId, projectId, taskTitle, 
                                                        newProgress, "KANBAN_MOVED", updatedBy, "KANBAN_MOVED");
        message.setOldStatus(oldStatus);
        message.setNewStatus(newStatus);
        message.setKanbanColumn(newStatus);
        message.setKanbanAction("MOVE");
        message.setSourceColumn(oldStatus);
        message.setTargetColumn(newStatus);
        return message;
    }

    /**
     * ✅ NEW: Factory method for Kanban position reordering within same column.
     * 
     * @param taskId Task being reordered
     * @param projectId Project containing the task
     * @param taskTitle Task title
     * @param kanbanStatus Current Kanban status (unchanged)
     * @param oldPosition Previous position in column
     * @param newPosition New position in column
     * @param updatedBy User performing the reorder
     * @return Configured TaskUpdateMessage for position change
     */
    public static TaskUpdateMessage kanbanReorder(Long taskId, Long projectId, String taskTitle,
                                                 String kanbanStatus, Integer oldPosition, 
                                                 Integer newPosition, String updatedBy) {
        TaskUpdateMessage message = new TaskUpdateMessage(taskId, projectId, taskTitle, 
                                                        null, "KANBAN_REORDERED", updatedBy, "KANBAN_REORDERED");
        message.setOldStatus(kanbanStatus);
        message.setNewStatus(kanbanStatus);
        message.setKanbanColumn(kanbanStatus);
        message.setOldPosition(oldPosition);
        message.setNewPosition(newPosition);
        message.setKanbanAction("REORDER");
        message.setSourceColumn(kanbanStatus);
        message.setTargetColumn(kanbanStatus);
        return message;
    }

    /**
     * ✅ NEW: Factory method for task status changes (non-Kanban).
     * 
     * @param taskId Task with status change
     * @param projectId Project containing the task
     * @param taskTitle Task title
     * @param newStatus New task status
     * @param progress Current progress
     * @param updatedBy User making the change
     * @return Configured TaskUpdateMessage for status change
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
     * ✅ NEW: Factory method for bulk operations.
     * 
     * @param operation Type of bulk operation (COMPLETE, ASSIGN, DELETE, etc.)
     * @param taskCount Number of tasks affected
     * @param projectId Project containing the tasks
     * @param updatedBy User performing the bulk operation
     * @return Configured TaskUpdateMessage for bulk operation
     */
    public static TaskUpdateMessage bulkOperation(String operation, int taskCount, Long projectId, String updatedBy) {
        TaskUpdateMessage message = new TaskUpdateMessage();
        message.setProjectId(projectId);
        message.setChangeType("BULK_OPERATION");
        message.setUpdatedBy(updatedBy);
        message.setTaskTitle(String.format("Bulk %s on %d task%s", operation, taskCount, taskCount == 1 ? "" : "s"));
        message.setStatus("BULK_" + operation.toUpperCase());
        message.setKanbanAction("BULK");
        message.setTimestamp(LocalDateTime.now());
        return message;
    }

    /**
     * ✅ NEW: Factory method for Kanban board refresh signals.
     * 
     * @param projectId Project to refresh
     * @param reason Reason for refresh
     * @return Configured TaskUpdateMessage for board refresh
     */
    public static TaskUpdateMessage kanbanRefresh(Long projectId, String reason) {
        TaskUpdateMessage message = new TaskUpdateMessage();
        message.setProjectId(projectId);
        message.setChangeType("KANBAN_REFRESH");
        message.setUpdatedBy("System");
        message.setTaskTitle("Kanban board refresh: " + reason);
        message.setStatus("REFRESH");
        message.setKanbanAction("REFRESH");
        message.setTimestamp(LocalDateTime.now());
        return message;
    }
    
    // =========================================================================
    // GETTERS AND SETTERS - Complete set including new Kanban fields
    // =========================================================================
    
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

    // ✅ NEW: Kanban-specific getters and setters
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
    
    public String getKanbanAction() { return kanbanAction; }
    public void setKanbanAction(String kanbanAction) { this.kanbanAction = kanbanAction; }
    
    public String getSourceColumn() { return sourceColumn; }
    public void setSourceColumn(String sourceColumn) { this.sourceColumn = sourceColumn; }
    
    public String getTargetColumn() { return targetColumn; }
    public void setTargetColumn(String targetColumn) { this.targetColumn = targetColumn; }

    // =========================================================================
    // UTILITY METHODS
    // =========================================================================
    
    /**
     * Check if this is a Kanban-related update.
     * 
     * @return true if this message represents a Kanban operation
     */
    public boolean isKanbanUpdate() {
        return "KANBAN_MOVED".equals(changeType) || 
               "KANBAN_REORDERED".equals(changeType) || 
               "KANBAN_REFRESH".equals(changeType) ||
               kanbanAction != null;
    }
    
    /**
     * Check if this is a cross-column Kanban move.
     * 
     * @return true if task moved between different Kanban columns
     */
    public boolean isCrossColumnMove() {
        return oldStatus != null && newStatus != null && !oldStatus.equals(newStatus);
    }
    
    /**
     * Check if this is a same-column reorder.
     * 
     * @return true if task was reordered within the same column
     */
    public boolean isSameColumnReorder() {
        return oldStatus != null && newStatus != null && oldStatus.equals(newStatus) &&
               oldPosition != null && newPosition != null && !oldPosition.equals(newPosition);
    }
    
    @Override
    public String toString() {
        return String.format("TaskUpdateMessage{taskId=%d, changeType='%s', status='%s', " +
                           "kanbanColumn='%s', oldStatus='%s', newStatus='%s', " +
                           "progress=%d, updatedBy='%s', timestamp=%s}", 
                          taskId, changeType, status, kanbanColumn, oldStatus, newStatus, 
                          progress, updatedBy, timestamp);
    }
}