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
    public static ActivityMessage userLogin(Long userId, String userName, String userRole) {
        ActivityMessage activity = new ActivityMessage(
            userId, 
            userName, 
            "LOGGED_IN", 
            userName + " joined the session"
        );
        activity.setUserRole(userRole);
        activity.setIconClass("fas fa-sign-in-alt");
        activity.setSeverity("SUCCESS");
        return activity;
    }
    
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
    
    public static ActivityMessage taskCompleted(Long userId, String userName, 
                                              Long taskId, String taskName) {
        ActivityMessage activity = new ActivityMessage(
            userId,
            userName,
            "TASK_COMPLETED",
            String.format("%s completed task '%s'", userName, taskName)
        );
        activity.setRelatedEntityId(taskId);
        activity.setRelatedEntityType("TASK");
        activity.setRelatedEntityName(taskName);
        activity.setIconClass("fas fa-check-circle");
        activity.setSeverity("SUCCESS");
        return activity;
    }
    
    public static ActivityMessage projectJoined(Long userId, String userName, 
                                               Long projectId, String projectName) {
        ActivityMessage activity = new ActivityMessage(
            userId,
            userName,
            "PROJECT_JOINED",
            String.format("%s joined project '%s'", userName, projectName)
        );
        activity.setRelatedEntityId(projectId);
        activity.setRelatedEntityType("PROJECT");
        activity.setRelatedEntityName(projectName);
        activity.setIconClass("fas fa-project-diagram");
        activity.setSeverity("INFO");
        return activity;
    }
    
    public static ActivityMessage meetingStarted(Long userId, String userName, 
                                                Long meetingId, String meetingTitle) {
        ActivityMessage activity = new ActivityMessage(
            userId,
            userName,
            "MEETING_STARTED",
            String.format("Meeting '%s' started by %s", meetingTitle, userName)
        );
        activity.setRelatedEntityId(meetingId);
        activity.setRelatedEntityType("MEETING");
        activity.setRelatedEntityName(meetingTitle);
        activity.setIconClass("fas fa-video");
        activity.setSeverity("INFO");
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
}