// src/main/java/org/frcpm/events/WebSocketEventPublisher.java

package org.frcpm.events;

import org.frcpm.web.dto.TaskUpdateMessage;
import org.frcpm.web.dto.ProjectNotification;
import org.frcpm.web.dto.ActivityMessage;
import org.frcpm.web.websocket.TaskUpdateController;
import org.frcpm.web.websocket.NotificationController;
import org.frcpm.web.websocket.ActivityController;
import org.frcpm.models.Task;
import org.frcpm.models.Project;
import org.frcpm.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Service for publishing real-time events to WebSocket subscribers.
 * 
 * ✅ PHASE 2C: PWA Development - Event Integration Bridge
 * 
 * This service bridges the existing Phase 2B service layer with the new
 * WebSocket real-time communication system, ensuring that all data changes
 * are immediately reflected across all connected devices.
 * 
 * Integrates seamlessly with:
 * - TaskService updates -> Real-time task progress
 * - ProjectService changes -> Live project notifications  
 * - User activities -> Team collaboration feeds
 * - COPPA compliance -> Age-appropriate real-time features
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 * @since Phase 2C - Progressive Web App Development
 */
@Service
public class WebSocketEventPublisher {
    
    private static final Logger LOGGER = Logger.getLogger(WebSocketEventPublisher.class.getName());
    
    @Autowired
    private TaskUpdateController taskUpdateController;
    
    @Autowired
    private NotificationController notificationController;
    
    @Autowired
    private ActivityController activityController;
    
    // =========================================================================
    // TASK EVENT PUBLISHING
    // =========================================================================
    
    /**
     * Publish task progress update to all project subscribers.
     * 
     * Called from TaskService when task progress is updated through any channel
     * (Web UI, mobile app, API, etc.) to ensure real-time synchronization.
     * 
     * @param task Updated task
     * @param oldProgress Previous progress value
     * @param updatedByUser User who made the update
     */
    public void publishTaskProgressUpdate(Task task, Integer oldProgress, User updatedByUser) {
        try {
            TaskUpdateMessage message = TaskUpdateMessage.progressUpdate(
                task.getId(),
                task.getProject().getId(),
                task.getTitle(),
                task.getProgress(),
                updatedByUser != null ? updatedByUser.getFullName() : "System"
            );
            
            // Add additional context
            message.setPriority(task.getPriority().name());
            message.setSubsystemName(task.getSubsystem().getName());
            
            // Add assignee information for targeted notifications
            if (!task.getAssignedTo().isEmpty()) {
                var firstAssignee = task.getAssignedTo().iterator().next();
                if (firstAssignee.getUser() != null) {
                    message.setAssigneeId(firstAssignee.getUser().getId());
                    message.setAssigneeName(firstAssignee.getFullName());
                }
            }
            
            // Broadcast to project subscribers
            taskUpdateController.broadcastTaskUpdate(message);
            
            // Send targeted notification to assignees
            task.getAssignedTo().forEach(assignee -> {
                if (assignee.getUser() != null) {
                    taskUpdateController.sendTaskUpdateToUser(assignee.getUser().getId(), message);
                }
            });
            
            // Create activity message for task progress
            if (updatedByUser != null) {
                ActivityMessage activity = ActivityMessage.taskProgressUpdate(
                    updatedByUser.getId(),
                    updatedByUser.getFullName(),
                    task.getId(),
                    task.getTitle(),
                    task.getProgress()
                );
                
                // Add user context
                if (updatedByUser.getTeamMember() != null && updatedByUser.getTeamMember().getSubteam() != null) {
                    activity.setSubteamName(updatedByUser.getTeamMember().getSubteam().getName());
                }
                activity.setUserRole(updatedByUser.getRole().name());
                
                activityController.broadcastProjectActivity(task.getProject().getId(), activity);
            }
            
            LOGGER.info(String.format("Published task progress update: Task %d from %d%% to %d%%", 
                                    task.getId(), oldProgress, task.getProgress()));
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error publishing task progress update", e);
        }
    }
    
    /**
     * Publish task completion notification.
     * 
     * Celebrates task completion with enhanced notifications and activity updates.
     * 
     * @param task Completed task
     * @param completedByUser User who completed the task
     */
    public void publishTaskCompletion(Task task, User completedByUser) {
        try {
            // Create completion message
            TaskUpdateMessage message = TaskUpdateMessage.taskCompleted(
                task.getId(),
                task.getProject().getId(),
                task.getTitle(),
                completedByUser != null ? completedByUser.getFullName() : "System"
            );
            
            message.setPriority(task.getPriority().name());
            message.setSubsystemName(task.getSubsystem().getName());
            
            taskUpdateController.broadcastTaskUpdate(message);
            
            // Create celebration notification
            ProjectNotification celebration = new ProjectNotification(
                task.getProject().getId(),
                "Task Completed! 🎉",
                String.format("%s completed '%s' - Great work!", 
                             completedByUser != null ? completedByUser.getFullName() : "Someone", 
                             task.getTitle()),
                "SUCCESS",
                completedByUser != null ? completedByUser.getFullName() : "System"
            );
            celebration.setPriority("MEDIUM");
            celebration.setCategory("TASK");
            celebration.setIconClass("fas fa-check-circle");
            
            notificationController.broadcastNotification(celebration);
            
            // Create activity message
            if (completedByUser != null) {
                ActivityMessage activity = ActivityMessage.taskCompleted(
                    completedByUser.getId(),
                    completedByUser.getFullName(),
                    task.getId(),
                    task.getTitle()
                );
                
                if (completedByUser.getTeamMember() != null && completedByUser.getTeamMember().getSubteam() != null) {
                    activity.setSubteamName(completedByUser.getTeamMember().getSubteam().getName());
                }
                activity.setUserRole(completedByUser.getRole().name());
                
                activityController.broadcastProjectActivity(task.getProject().getId(), activity);
            }
            
            LOGGER.info(String.format("Published task completion: Task %d '%s'", task.getId(), task.getTitle()));
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error publishing task completion", e);
        }
    }
    
    /**
     * Publish new task creation notification.
     * 
     * @param task Newly created task
     * @param createdByUser User who created the task
     */
    public void publishTaskCreation(Task task, User createdByUser) {
        try {
            TaskUpdateMessage message = TaskUpdateMessage.taskCreated(
                task.getId(),
                task.getProject().getId(),
                task.getTitle(),
                createdByUser != null ? createdByUser.getFullName() : "System"
            );
            
            message.setPriority(task.getPriority().name());
            message.setSubsystemName(task.getSubsystem().getName());
            
            taskUpdateController.broadcastTaskUpdate(message);
            
            // Notify assignees if any
            task.getAssignedTo().forEach(assignee -> {
                if (assignee.getUser() != null) {
                    ProjectNotification assignment = new ProjectNotification(
                        task.getProject().getId(),
                        "New Task Assignment",
                        String.format("You've been assigned to '%s'", task.getTitle()),
                        "INFO",
                        createdByUser != null ? createdByUser.getFullName() : "System"
                    );
                    assignment.setTargetUserId(assignee.getUser().getId());
                    assignment.setCategory("TASK");
                    assignment.setIconClass("fas fa-tasks");
                    
                    notificationController.sendNotificationToUser(assignee.getUser().getId(), assignment);
                }
            });
            
            LOGGER.info(String.format("Published task creation: Task %d '%s'", task.getId(), task.getTitle()));
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error publishing task creation", e);
        }
    }
    
    // =========================================================================
    // PROJECT EVENT PUBLISHING
    // =========================================================================
    
    /**
     * Publish project deadline alert.
     * 
     * @param project Project approaching deadline
     * @param daysRemaining Days until deadline
     */
    public void publishDeadlineAlert(Project project, long daysRemaining) {
        try {
            String urgencyLevel = daysRemaining <= 1 ? "CRITICAL" : 
                                 daysRemaining <= 3 ? "HIGH" : "MEDIUM";
            
            String message = daysRemaining == 0 ? 
                "Deadline is TODAY!" :
                String.format("Deadline in %d day%s", daysRemaining, daysRemaining == 1 ? "" : "s");
            
            ProjectNotification alert = ProjectNotification.deadlineAlert(
                project.getId(),
                project.getName(),
                message,
                "System"
            );
            alert.setPriority(urgencyLevel);
            
            notificationController.broadcastNotification(alert);
            
            LOGGER.info(String.format("Published deadline alert: Project %d, %d days remaining", 
                                    project.getId(), daysRemaining));
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error publishing deadline alert", e);
        }
    }
    
    /**
     * Publish milestone achievement notification.
     * 
     * @param project Project with achieved milestone
     * @param milestoneName Name of achieved milestone
     * @param achievedByUser User who marked milestone as complete
     */
    public void publishMilestoneAchievement(Project project, String milestoneName, User achievedByUser) {
        try {
            ProjectNotification celebration = ProjectNotification.milestoneAchieved(
                project.getId(),
                milestoneName,
                achievedByUser != null ? achievedByUser.getFullName() : "System"
            );
            
            notificationController.broadcastNotification(celebration);
            
            LOGGER.info(String.format("Published milestone achievement: Project %d, Milestone '%s'", 
                                    project.getId(), milestoneName));
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error publishing milestone achievement", e);
        }
    }
    
    // =========================================================================
    // USER ACTIVITY PUBLISHING
    // =========================================================================
    
    /**
     * Publish user login activity.
     * 
     * @param user User who logged in
     */
    public void publishUserLogin(User user) {
        try {
            ActivityMessage activity = ActivityMessage.userLogin(
                user.getId(),
                user.getFullName(),
                user.getRole().name()
            );
            
            if (user.getTeamMember() != null && user.getTeamMember().getSubteam() != null) {
                activity.setSubteamName(user.getTeamMember().getSubteam().getName());
            }
            
            activityController.broadcastUserPresence(activity);
            
            LOGGER.info(String.format("Published user login: %s", user.getUsername()));
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error publishing user login", e);
        }
    }
    
    /**
     * Publish user joining project activity.
     * 
     * @param user User joining project
     * @param project Project being joined
     */
    public void publishProjectJoin(User user, Project project) {
        try {
            ActivityMessage activity = ActivityMessage.projectJoined(
                user.getId(),
                user.getFullName(),
                project.getId(),
                project.getName()
            );
            
            if (user.getTeamMember() != null && user.getTeamMember().getSubteam() != null) {
                activity.setSubteamName(user.getTeamMember().getSubteam().getName());
            }
            activity.setUserRole(user.getRole().name());
            
            activityController.broadcastProjectActivity(project.getId(), activity);
            
            LOGGER.info(String.format("Published project join: User %s joined Project %d", 
                                    user.getUsername(), project.getId()));
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error publishing project join", e);
        }
    }
    
    // =========================================================================
    // MEETING EVENT PUBLISHING
    // =========================================================================
    
    /**
     * Publish meeting start notification.
     * 
     * @param meetingId Meeting ID
     * @param meetingTitle Meeting title
     * @param startedByUser User who started the meeting
     * @param projectId Associated project ID
     */
    public void publishMeetingStart(Long meetingId, String meetingTitle, User startedByUser, Long projectId) {
        try {
            // Create activity message
            ActivityMessage activity = ActivityMessage.meetingStarted(
                startedByUser.getId(),
                startedByUser.getFullName(),
                meetingId,
                meetingTitle
            );
            
            if (startedByUser.getTeamMember() != null && startedByUser.getTeamMember().getSubteam() != null) {
                activity.setSubteamName(startedByUser.getTeamMember().getSubteam().getName());
            }
            activity.setUserRole(startedByUser.getRole().name());
            
            activityController.broadcastProjectActivity(projectId, activity);
            
            // Create notification
            ProjectNotification notification = ProjectNotification.meetingReminder(
                projectId,
                String.format("Meeting '%s' has started", meetingTitle),
                startedByUser.getFullName()
            );
            
            notificationController.broadcastNotification(notification);
            
            LOGGER.info(String.format("Published meeting start: Meeting %d '%s'", meetingId, meetingTitle));
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error publishing meeting start", e);
        }
    }
    
    // =========================================================================
    // SYSTEM EVENT PUBLISHING
    // =========================================================================
    
    /**
     * Publish system alert to all users.
     * 
     * @param alertMessage Alert message
     * @param priority Alert priority (LOW, MEDIUM, HIGH, CRITICAL)
     */
    public void publishSystemAlert(String alertMessage, String priority) {
        try {
            ProjectNotification alert = new ProjectNotification();
            alert.setProjectId(0L); // System-wide alert
            alert.setTitle("System Alert");
            alert.setMessage(alertMessage);
            alert.setType("ALERT");
            alert.setPriority(priority);
            alert.setSender("System");
            alert.setCategory("SYSTEM");
            alert.setPersistent(true);
            alert.setIconClass("fas fa-exclamation-triangle");
            
            notificationController.broadcastSystemAlert(alert);
            
            LOGGER.info(String.format("Published system alert: %s (Priority: %s)", alertMessage, priority));
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error publishing system alert", e);
        }
    }
    
    /**
     * Check if real-time features are enabled for user (COPPA compliance).
     * 
     * Users under 13 may have restricted real-time features per COPPA requirements.
     * 
     * @param user User to check
     * @return true if real-time features are enabled
     */
    private boolean isRealTimeFeaturesEnabled(User user) {
        if (user == null) {
            return false;
        }
        
        // Check COPPA compliance - users under 13 may have restrictions
        if (user.requiresCOPPACompliance() && !user.hasParentalConsent()) {
            return false; // No real-time features without parental consent
        }
        
        return true;
    }
}