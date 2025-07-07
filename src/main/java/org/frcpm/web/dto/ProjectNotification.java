// src/main/java/org/frcpm/web/dto/ProjectNotification.java

package org.frcpm.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

/**
 * WebSocket message for project-wide notifications.
 * 
 * ✅ PHASE 2C: PWA Development - Real-time Project Communication
 * 
 * Handles system-wide announcements, deadline alerts, and important
 * project updates that need immediate team attention.
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 * @since Phase 2C - Progressive Web App Development
 */
public class ProjectNotification {
    
    @NotNull
    private Long projectId;
    
    @NotBlank
    private String title;
    
    @NotBlank
    private String message;
    
    @NotBlank
    private String type; // INFO, WARNING, ALERT, SUCCESS, DEADLINE, MILESTONE
    
    private String sender; // User who triggered the notification
    
    private String priority; // LOW, MEDIUM, HIGH, CRITICAL
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    private String category; // TASK, MILESTONE, DEADLINE, MEETING, SYSTEM
    
    private Map<String, Object> data; // Additional context data
    
    private String actionUrl; // Optional URL for click actions
    
    private boolean persistent; // Whether notification should persist
    
    private Long targetUserId; // For user-specific notifications
    
    private String iconClass; // CSS class for notification icon
    
    // Default constructor
    public ProjectNotification() {
        this.timestamp = LocalDateTime.now();
        this.data = new HashMap<>();
        this.persistent = false;
        this.priority = "MEDIUM";
    }
    
    // Main constructor
    public ProjectNotification(Long projectId, String title, String message, 
                             String type, String sender) {
        this();
        this.projectId = projectId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.sender = sender;
    }
    
    // Factory methods for common notification types
    public static ProjectNotification deadlineAlert(Long projectId, String projectName, 
                                                   String deadline, String sender) {
        ProjectNotification notification = new ProjectNotification(
            projectId, 
            "Deadline Alert", 
            String.format("Project '%s' deadline approaching: %s", projectName, deadline),
            "DEADLINE", 
            sender
        );
        notification.setPriority("HIGH");
        notification.setCategory("DEADLINE");
        notification.setPersistent(true);
        notification.setIconClass("fas fa-clock");
        return notification;
    }
    
    public static ProjectNotification milestoneAchieved(Long projectId, String milestoneName, String sender) {
        ProjectNotification notification = new ProjectNotification(
            projectId,
            "Milestone Achieved!",
            String.format("Congratulations! Milestone '%s' has been completed.", milestoneName),
            "SUCCESS",
            sender
        );
        notification.setPriority("MEDIUM");
        notification.setCategory("MILESTONE");
        notification.setIconClass("fas fa-trophy");
        return notification;
    }
    
    public static ProjectNotification meetingReminder(Long projectId, String meetingDetails, String sender) {
        ProjectNotification notification = new ProjectNotification(
            projectId,
            "Meeting Reminder",
            meetingDetails,
            "INFO",
            sender
        );
        notification.setPriority("MEDIUM");
        notification.setCategory("MEETING");
        notification.setIconClass("fas fa-calendar");
        return notification;
    }
    
    public static ProjectNotification systemAlert(Long projectId, String alertMessage, String sender) {
        ProjectNotification notification = new ProjectNotification(
            projectId,
            "System Alert",
            alertMessage,
            "ALERT",
            sender
        );
        notification.setPriority("CRITICAL");
        notification.setCategory("SYSTEM");
        notification.setPersistent(true);
        notification.setIconClass("fas fa-exclamation-triangle");
        return notification;
    }
    
    // Helper methods
    public void addData(String key, Object value) {
        this.data.put(key, value);
    }
    
    public Object getData(String key) {
        return this.data.get(key);
    }
    
    // Getters and setters
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }
    
    public String getActionUrl() { return actionUrl; }
    public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }
    
    public boolean isPersistent() { return persistent; }
    public void setPersistent(boolean persistent) { this.persistent = persistent; }
    
    public Long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }
    
    public String getIconClass() { return iconClass; }
    public void setIconClass(String iconClass) { this.iconClass = iconClass; }
    
    @Override
    public String toString() {
        return String.format("ProjectNotification{projectId=%d, type='%s', title='%s', sender='%s'}", 
                           projectId, type, title, sender);
    }
}