// src/main/java/org/frcpm/web/dto/ActivityMessage.java

package org.frcpm.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * WebSocket message for team activity streams.
 * 
 * ✅ PHASE 2E-C: Enhanced with Complete Factory Method Set
 * ✅ FIXED: Added all missing factory methods for compilation
 * 
 * Tracks and broadcasts team member activities for enhanced
 * collaboration visibility during intense build sessions.
 * Now includes comprehensive Kanban and bulk operation support.
 * 
 * @author FRC Project Management Team - Phase 2E-C Complete
 * @version 2.0.0-2E-C-COMPLETE
 * @since Phase 2C - Progressive Web App Development
 */
public class ActivityMessage {
    
    @NotNull
    private Long userId;
    
    @NotBlank
    private String userName;
    
    @NotBlank
    private String action; // LOGGED_IN, TASK_UPDATED, PROJECT_JOINED, etc.
    
    @NotBlank
    private String description; // Human-readable activity description
    
    private Long relatedEntityId; // Task, Project, or other entity ID
    
    private String relatedEntityType; // TASK, PROJECT, MEETING, etc.
    
    private String relatedEntityName; // Name for display purposes
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    private String userRole; // STUDENT, MENTOR, ADMIN for context
    
    private String subteamName; // User's subteam for filtering
    
    private String iconClass; // CSS class for activity icon
    
    private String severity; // INFO, SUCCESS, WARNING for styling
    
    // ✅ NEW: Additional fields for enhanced activity tracking
    private String resourceType; // Type of resource being acted upon
    private Long resourceId; // ID of the resource
    private String resourceName; // Name of the resource for display
    
    // Default constructor
    public ActivityMessage() {
        this.timestamp = LocalDateTime.now();
        this.severity = "INFO";
    }
    
    // Main constructor
    public ActivityMessage(Long userId, String userName, String action, String description) {
        this();
        this.userId = userId;
        this.userName = userName;
        this.action = action;
        this.description = description;
    }
    
    // =========================================================================
    // FACTORY METHODS - Complete Set for All WebSocket Operations
    // =========================================================================

    /**
     * ✅ FIXED: Factory method for task progress updates.
     * Called from WebSocketEventPublisher.publishTaskProgressUpdate()
     */
    public static ActivityMessage taskProgressUpdate(Long userId, String userName, 
                                                   Long taskId, String taskName, Integer progress) {
        ActivityMessage activity = new ActivityMessage(
            userId,
            userName,
            "TASK_PROGRESS_UPDATED",
            String.format("updated '%s' to %d%% complete", taskName, progress)
        );
        activity.setRelatedEntityId(taskId);
        activity.setRelatedEntityType("TASK");
        activity.setRelatedEntityName(taskName);
        activity.setResourceType("TASK");
        activity.setResourceId(taskId);
        activity.setResourceName(taskName);
        activity.setIconClass("fas fa-tasks");
        activity.setSeverity(progress >= 100 ? "SUCCESS" : "INFO");
        return activity;
    }

    /**
     * ✅ FIXED: Factory method for task completion.
     * Called from WebSocketEventPublisher.publishTaskCompletion()
     */
    public static ActivityMessage taskCompleted(Long userId, String userName, Long taskId, String taskTitle) {
        ActivityMessage activity = new ActivityMessage(
            userId,
            userName,
            "TASK_COMPLETED",
            String.format("completed task '%s'", taskTitle)
        );
        activity.setRelatedEntityId(taskId);
        activity.setRelatedEntityType("TASK");
        activity.setRelatedEntityName(taskTitle);
        activity.setResourceType("TASK");
        activity.setResourceId(taskId);
        activity.setResourceName(taskTitle);
        activity.setIconClass("fas fa-check-circle");
        activity.setSeverity("SUCCESS");
        return activity;
    }

    /**
     * ✅ FIXED: Factory method for user login events.
     * Called from WebSocketEventPublisher.publishUserLogin()
     */
    public static ActivityMessage userLogin(Long userId, String userName, String userRole) {
        ActivityMessage activity = new ActivityMessage(
            userId,
            userName,
            "USER_LOGIN",
            "joined the session"
        );
        activity.setUserRole(userRole);
        activity.setResourceType("USER");
        activity.setResourceId(userId);
        activity.setResourceName(userName);
        activity.setIconClass("fas fa-sign-in-alt");
        activity.setSeverity("INFO");
        return activity;
    }

    /**
     * ✅ FIXED: Factory method for project joining.
     * Called from WebSocketEventPublisher.publishProjectJoin()
     */
    public static ActivityMessage projectJoined(Long userId, String userName, Long projectId, String projectName) {
        ActivityMessage activity = new ActivityMessage(
            userId,
            userName,
            "PROJECT_JOINED",
            String.format("joined project '%s'", projectName)
        );
        activity.setRelatedEntityId(projectId);
        activity.setRelatedEntityType("PROJECT");
        activity.setRelatedEntityName(projectName);
        activity.setResourceType("PROJECT");
        activity.setResourceId(projectId);
        activity.setResourceName(projectName);
        activity.setIconClass("fas fa-project-diagram");
        activity.setSeverity("INFO");
        return activity;
    }

    /**
     * ✅ FIXED: Factory method for meeting started events.
     * Called from WebSocketEventPublisher.publishMeetingStart()
     */
    public static ActivityMessage meetingStarted(Long userId, String userName, Long meetingId, String meetingTitle) {
        ActivityMessage activity = new ActivityMessage(
            userId,
            userName,
            "MEETING_STARTED",
            String.format("started meeting '%s'", meetingTitle)
        );
        activity.setRelatedEntityId(meetingId);
        activity.setRelatedEntityType("MEETING");
        activity.setRelatedEntityName(meetingTitle);
        activity.setResourceType("MEETING");
        activity.setResourceId(meetingId);
        activity.setResourceName(meetingTitle);
        activity.setIconClass("fas fa-video");
        activity.setSeverity("INFO");
        return activity;
    }

    /**
     * ✅ NEW: Factory method for Kanban board moves.
     * Called from WebSocketEventPublisher.publishKanbanMove()
     */
    public static ActivityMessage kanbanMove(Long userId, String userName, Long taskId, String taskTitle,
                                           String oldStatus, String newStatus) {
        ActivityMessage activity = new ActivityMessage(
            userId,
            userName,
            "KANBAN_MOVED",
            String.format("moved '%s' from %s to %s", taskTitle, 
                         getStatusDisplayName(oldStatus), getStatusDisplayName(newStatus))
        );
        activity.setRelatedEntityId(taskId);
        activity.setRelatedEntityType("TASK");
        activity.setRelatedEntityName(taskTitle);
        activity.setResourceType("TASK");
        activity.setResourceId(taskId);
        activity.setResourceName(taskTitle);
        activity.setIconClass("fas fa-arrows-alt");
        activity.setSeverity("INFO");
        return activity;
    }

    /**
     * ✅ NEW: Factory method for bulk operations.
     * Called from WebSocketEventPublisher.publishBulkKanbanOperation()
     */
    public static ActivityMessage bulkOperation(Long userId, String userName, String operationType, int taskCount) {
        ActivityMessage activity = new ActivityMessage(
            userId,
            userName,
            "BULK_OPERATION",
            String.format("performed bulk %s on %d task%s", 
                         operationType.toLowerCase(), taskCount, taskCount == 1 ? "" : "s")
        );
        activity.setRelatedEntityType("TASKS");
        activity.setRelatedEntityName(taskCount + " tasks");
        activity.setResourceType("TASKS");
        activity.setResourceId(null); // Multiple tasks
        activity.setResourceName(taskCount + " tasks");
        activity.setIconClass("fas fa-layer-group");
        activity.setSeverity("INFO");
        return activity;
    }

    /**
     * ✅ NEW: Factory method for task creation events.
     * Called from WebSocketEventPublisher.publishTaskCreation()
     */
    public static ActivityMessage taskCreated(Long userId, String userName, Long taskId, String taskTitle) {
        ActivityMessage activity = new ActivityMessage(
            userId,
            userName,
            "TASK_CREATED",
            String.format("created new task '%s'", taskTitle)
        );
        activity.setRelatedEntityId(taskId);
        activity.setRelatedEntityType("TASK");
        activity.setRelatedEntityName(taskTitle);
        activity.setResourceType("TASK");
        activity.setResourceId(taskId);
        activity.setResourceName(taskTitle);
        activity.setIconClass("fas fa-plus-circle");
        activity.setSeverity("SUCCESS");
        return activity;
    }

    /**
     * ✅ NEW: Factory method for task assignment changes.
     */
    public static ActivityMessage taskAssigned(Long userId, String userName, Long taskId, 
                                             String taskTitle, String assigneeName) {
        ActivityMessage activity = new ActivityMessage(
            userId,
            userName,
            "TASK_ASSIGNED",
            String.format("assigned '%s' to %s", taskTitle, assigneeName)
        );
        activity.setRelatedEntityId(taskId);
        activity.setRelatedEntityType("TASK");
        activity.setRelatedEntityName(taskTitle);
        activity.setResourceType("TASK");
        activity.setResourceId(taskId);
        activity.setResourceName(taskTitle);
        activity.setIconClass("fas fa-user-tag");
        activity.setSeverity("INFO");
        return activity;
    }

    /**
     * ✅ NEW: Factory method for task deadline alerts.
     */
    public static ActivityMessage taskDeadlineAlert(Long taskId, String taskTitle, long daysRemaining) {
        ActivityMessage activity = new ActivityMessage(
            null, // System generated
            "System",
            "DEADLINE_ALERT",
            String.format("Task '%s' due in %d day%s", taskTitle, daysRemaining, daysRemaining == 1 ? "" : "s")
        );
        activity.setRelatedEntityId(taskId);
        activity.setRelatedEntityType("TASK");
        activity.setRelatedEntityName(taskTitle);
        activity.setResourceType("TASK");
        activity.setResourceId(taskId);
        activity.setResourceName(taskTitle);
        activity.setIconClass("fas fa-exclamation-triangle");
        activity.setSeverity(daysRemaining <= 1 ? "WARNING" : "INFO");
        return activity;
    }

    /**
     * ✅ NEW: Factory method for milestone achievements.
     */
    public static ActivityMessage milestoneAchieved(Long userId, String userName, Long projectId, 
                                                  String projectName, String milestoneName) {
        ActivityMessage activity = new ActivityMessage(
            userId,
            userName,
            "MILESTONE_ACHIEVED",
            String.format("achieved milestone '%s' in project '%s'", milestoneName, projectName)
        );
        activity.setRelatedEntityId(projectId);
        activity.setRelatedEntityType("PROJECT");
        activity.setRelatedEntityName(projectName);
        activity.setResourceType("MILESTONE");
        activity.setResourceName(milestoneName);
        activity.setIconClass("fas fa-trophy");
        activity.setSeverity("SUCCESS");
        return activity;
    }

    /**
     * ✅ NEW: Factory method for system alerts.
     */
    public static ActivityMessage systemAlert(String alertMessage, String priority) {
        ActivityMessage activity = new ActivityMessage(
            null, // System generated
            "System",
            "SYSTEM_ALERT",
            alertMessage
        );
        activity.setResourceType("SYSTEM");
        activity.setResourceName("System Alert");
        activity.setIconClass("fas fa-exclamation-circle");
        activity.setSeverity(priority.toUpperCase());
        return activity;
    }

    // =========================================================================
    // UTILITY METHODS
    // =========================================================================

    /**
     * Helper method to get display names for Kanban statuses.
     */
    private static String getStatusDisplayName(String status) {
        if (status == null) return "Unknown";
        
        switch (status.toUpperCase()) {
            case "TODO": return "To Do";
            case "IN_PROGRESS": return "In Progress";
            case "REVIEW": return "Review";
            case "COMPLETED": return "Completed";
            default: return status;
        }
    }

    /**
     * Check if this activity is user-generated vs system-generated.
     */
    public boolean isUserGenerated() {
        return userId != null && !"System".equals(userName);
    }

    /**
     * Check if this activity is related to a specific task.
     */
    public boolean isTaskRelated() {
        return "TASK".equals(relatedEntityType) || "TASK".equals(resourceType);
    }

    /**
     * Check if this activity is Kanban-related.
     */
    public boolean isKanbanActivity() {
        return action != null && action.contains("KANBAN");
    }
    
    // =========================================================================
    // GETTERS AND SETTERS - Complete Set
    // =========================================================================
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Long getRelatedEntityId() { return relatedEntityId; }
    public void setRelatedEntityId(Long relatedEntityId) { this.relatedEntityId = relatedEntityId; }
    
    public String getRelatedEntityType() { return relatedEntityType; }
    public void setRelatedEntityType(String relatedEntityType) { this.relatedEntityType = relatedEntityType; }
    
    public String getRelatedEntityName() { return relatedEntityName; }
    public void setRelatedEntityName(String relatedEntityName) { this.relatedEntityName = relatedEntityName; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }
    
    public String getSubteamName() { return subteamName; }
    public void setSubteamName(String subteamName) { this.subteamName = subteamName; }
    
    public String getIconClass() { return iconClass; }
    public void setIconClass(String iconClass) { this.iconClass = iconClass; }
    
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    
    // ✅ NEW: Getters and setters for additional resource fields
    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    
    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }
    
    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }
    
    @Override
    public String toString() {
        return String.format("ActivityMessage{userId=%s, action='%s', description='%s', " +
                           "resourceType='%s', resourceName='%s', severity='%s', timestamp=%s}", 
                           userId, action, description, resourceType, resourceName, severity, timestamp);
    }
}