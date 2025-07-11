// src/main/java/org/frcpm/web/dto/ActivityMessage.java

package org.frcpm.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * WebSocket message for team activity streams.
 * 
 * ✅ PHASE 2C: PWA Development - Real-time Activity Feeds
 * 
 * Tracks and broadcasts team member activities for enhanced
 * collaboration visibility during intense build sessions.
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
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
    
    // Factory methods for common activities

    
    public static ActivityMessage taskProgressUpdate(Long userId, String userName, 
                                                   Long taskId, String taskName, int progress) {
        ActivityMessage activity = new ActivityMessage(
            userId,
            userName,
            "TASK_PROGRESS_UPDATED",
            String.format("%s updated '%s' to %d%% complete", userName, taskName, progress)
        );
        activity.setRelatedEntityId(taskId);
        activity.setRelatedEntityType("TASK");
        activity.setRelatedEntityName(taskName);
        activity.setIconClass("fas fa-tasks");
        activity.setSeverity(progress >= 100 ? "SUCCESS" : "INFO");
        return activity;
    }
    

    
 
    
    // Getters and setters
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
    
    @Override
    public String toString() {
        return String.format("ActivityMessage{userId=%d, action='%s', description='%s', timestamp=%s}", 
                           userId, action, description, timestamp);
    }

    /**
     * Factory method for Kanban board moves.
     * 
     * ✅ NEW: Phase 2E-C Kanban support
     */
    public static ActivityMessage kanbanMove(Long userId, String userName, Long taskId, String taskTitle,
    String oldStatus, String newStatus) {
    ActivityMessage message = new ActivityMessage();
    message.setUserId(userId);
    message.setUserName(userName);
    message.setAction("KANBAN_MOVED");
    message.setDescription(String.format("moved \"%s\" from %s to %s", taskTitle, 
    getStatusDisplayName(oldStatus), getStatusDisplayName(newStatus)));
    message.setIconClass("fas fa-arrows-alt");
    message.setSeverity("INFO");
    message.setTimestamp(java.time.LocalDateTime.now());

    // Add additional context
    message.setResourceType("TASK");
    message.setResourceId(taskId);
    message.setResourceName(taskTitle);

    return message;
    }

    /**
    * Factory method for bulk operations.
    * 
    * ✅ NEW: Phase 2E-C bulk operations support
    */
    public static ActivityMessage bulkOperation(Long userId, String userName, String operationType, int taskCount) {
    ActivityMessage message = new ActivityMessage();
    message.setUserId(userId);
    message.setUserName(userName);
    message.setAction("BULK_OPERATION");
    message.setDescription(String.format("performed bulk %s on %d task%s", 
    operationType.toLowerCase(), taskCount, taskCount == 1 ? "" : "s"));
    message.setIconClass("fas fa-layer-group");
    message.setSeverity("INFO");
    message.setTimestamp(java.time.LocalDateTime.now());

    // Add additional context
    message.setResourceType("TASKS");
    message.setResourceId(null); // Multiple tasks
    message.setResourceName(taskCount + " tasks");

    return message;
    }

    /**
    * Helper method to get display names for Kanban statuses.
    */
    private static String getStatusDisplayName(String status) {
    switch (status) {
    case "TODO": return "To Do";
    case "IN_PROGRESS": return "In Progress";
    case "REVIEW": return "Review";
    case "COMPLETED": return "Completed";
    default: return status;
    }
    }

    // ADD THESE ADDITIONAL FACTORY METHODS IF ActivityMessage DOESN'T HAVE THEM:

    /**
    * Factory method for task progress updates.
    */
    public static ActivityMessage taskProgressUpdate(Long userId, String userName, Long taskId, String taskTitle, Integer progress) {
    ActivityMessage message = new ActivityMessage();
    message.setUserId(userId);
    message.setUserName(userName);
    message.setAction("PROGRESS_UPDATE");
    message.setDescription(String.format("updated progress on \"%s\" to %d%%", taskTitle, progress));
    message.setIconClass("fas fa-chart-line");
    message.setSeverity(progress >= 100 ? "SUCCESS" : "INFO");
    message.setTimestamp(java.time.LocalDateTime.now());

    message.setResourceType("TASK");
    message.setResourceId(taskId);
    message.setResourceName(taskTitle);

    return message;
    }

    /**
    * Factory method for task completion.
    */
    public static ActivityMessage taskCompleted(Long userId, String userName, Long taskId, String taskTitle) {
    ActivityMessage message = new ActivityMessage();
    message.setUserId(userId);
    message.setUserName(userName);
    message.setAction("TASK_COMPLETED");
    message.setDescription(String.format("completed task \"%s\"", taskTitle));
    message.setIconClass("fas fa-check-circle");
    message.setSeverity("SUCCESS");
    message.setTimestamp(java.time.LocalDateTime.now());

    message.setResourceType("TASK");
    message.setResourceId(taskId);
    message.setResourceName(taskTitle);

    return message;
    }

    /**
    * Factory method for user login.
    */
    public static ActivityMessage userLogin(Long userId, String userName, String userRole) {
    ActivityMessage message = new ActivityMessage();
    message.setUserId(userId);
    message.setUserName(userName);
    message.setAction("USER_LOGIN");
    message.setDescription("joined the session");
    message.setIconClass("fas fa-sign-in-alt");
    message.setSeverity("INFO");
    message.setTimestamp(java.time.LocalDateTime.now());

    message.setResourceType("USER");
    message.setResourceId(userId);
    message.setResourceName(userName);

    return message;
    }

    /**
    * Factory method for project joined.
    */
    public static ActivityMessage projectJoined(Long userId, String userName, Long projectId, String projectName) {
    ActivityMessage message = new ActivityMessage();
    message.setUserId(userId);
    message.setUserName(userName);
    message.setAction("PROJECT_JOINED");
    message.setDescription(String.format("joined project \"%s\"", projectName));
    message.setIconClass("fas fa-project-diagram");
    message.setSeverity("INFO");
    message.setTimestamp(java.time.LocalDateTime.now());

    message.setResourceType("PROJECT");
    message.setResourceId(projectId);
    message.setResourceName(projectName);

    return message;
    }

    /**
    * Factory method for meeting started.
    */
    public static ActivityMessage meetingStarted(Long userId, String userName, Long meetingId, String meetingTitle) {
    ActivityMessage message = new ActivityMessage();
    message.setUserId(userId);
    message.setUserName(userName);
    message.setAction("MEETING_STARTED");
    message.setDescription(String.format("started meeting \"%s\"", meetingTitle));
    message.setIconClass("fas fa-video");
    message.setSeverity("INFO");
    message.setTimestamp(java.time.LocalDateTime.now());

    message.setResourceType("MEETING");
    message.setResourceId(meetingId);
    message.setResourceName(meetingTitle);

    return message;
    }
}