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
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Service for publishing real-time events to WebSocket subscribers.
 * 
 * ✅ PHASE 2E-C: Enhanced WebSocket Integration - COMPILATION FIXED
 * ✅ FIXED: All DTO method calls aligned with enhanced classes
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
 * - Kanban operations -> Real-time board synchronization ✅ NEW
 * 
 * @author FRC Project Management Team - Phase 2E-C Fixed
 * @version 2.0.0-2E-C-FIXED
 * @since Phase 2C - Progressive Web App Development
 */
@Service
public class WebSocketEventPublisher {
    
    private static final Logger LOGGER = Logger.getLogger(WebSocketEventPublisher.class.getName());
    
    @Autowired
    @Lazy
    private TaskUpdateController taskUpdateController;
    
    @Autowired
    @Lazy
    private NotificationController notificationController;
    
    @Autowired
    @Lazy
    private ActivityController activityController;
    
    // =========================================================================
    // TASK EVENT PUBLISHING - ✅ ENHANCED WITH FIXED DTO CALLS
    // =========================================================================
    
    /**
     * Publish task progress update to all project subscribers.
     * 
     * ✅ FIXED: Uses enhanced TaskUpdateMessage.progressUpdate() factory method
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
            
            // ✅ FIXED: Create activity message using correct factory method
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
            
            LOGGER.info(String.format("Published task progress update: Task %d from %s%% to %d%%", 
                                    task.getId(), oldProgress != null ? oldProgress : "?", task.getProgress()));
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error publishing task progress update", e);
        }
    }
    
    /**
     * Publish task completion notification.
     * 
     * ✅ FIXED: Uses enhanced TaskUpdateMessage.taskCompleted() factory method
     * 
     * @param task Completed task
     * @param completedByUser User who completed the task
     */
    public void publishTaskCompletion(Task task, User completedByUser) {
        try {
            // ✅ FIXED: Use correct factory method
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
            
            // ✅ FIXED: Create activity message using correct factory method
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
     * ✅ FIXED: Uses enhanced TaskUpdateMessage.taskCreated() factory method
     * 
     * @param task Newly created task
     * @param createdByUser User who created the task
     */
    public void publishTaskCreation(Task task, User createdByUser) {
        try {
            // ✅ FIXED: Use correct factory method
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
            
            // ✅ FIXED: Create activity message using correct factory method
            if (createdByUser != null) {
                ActivityMessage activity = ActivityMessage.taskCreated(
                    createdByUser.getId(),
                    createdByUser.getFullName(),
                    task.getId(),
                    task.getTitle()
                );
                
                if (createdByUser.getTeamMember() != null && createdByUser.getTeamMember().getSubteam() != null) {
                    activity.setSubteamName(createdByUser.getTeamMember().getSubteam().getName());
                }
                activity.setUserRole(createdByUser.getRole().name());
                
                activityController.broadcastProjectActivity(task.getProject().getId(), activity);
            }
            
            LOGGER.info(String.format("Published task creation: Task %d '%s'", task.getId(), task.getTitle()));
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error publishing task creation", e);
        }
    }
    
    // =========================================================================
    // PROJECT EVENT PUBLISHING - ✅ ENHANCED
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
            
            // ✅ FIXED: Create activity message using correct factory method
            if (achievedByUser != null) {
                ActivityMessage activity = ActivityMessage.milestoneAchieved(
                    achievedByUser.getId(),
                    achievedByUser.getFullName(),
                    project.getId(),
                    project.getName(),
                    milestoneName
                );
                
                if (achievedByUser.getTeamMember() != null && achievedByUser.getTeamMember().getSubteam() != null) {
                    activity.setSubteamName(achievedByUser.getTeamMember().getSubteam().getName());
                }
                activity.setUserRole(achievedByUser.getRole().name());
                
                activityController.broadcastProjectActivity(project.getId(), activity);
            }
            
            LOGGER.info(String.format("Published milestone achievement: Project %d, Milestone '%s'", 
                                    project.getId(), milestoneName));
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error publishing milestone achievement", e);
        }
    }
    
    // =========================================================================
    // USER ACTIVITY PUBLISHING - ✅ ENHANCED
    // =========================================================================
    
    /**
     * Publish user login activity.
     * 
     * ✅ FIXED: Uses correct ActivityMessage.userLogin() factory method
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
     * ✅ FIXED: Uses correct ActivityMessage.projectJoined() factory method
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
    // MEETING EVENT PUBLISHING - ✅ ENHANCED
    // =========================================================================
    
    /**
     * Publish meeting start notification.
     * 
     * ✅ FIXED: Uses correct ActivityMessage.meetingStarted() factory method
     * 
     * @param meetingId Meeting ID
     * @param meetingTitle Meeting title
     * @param startedByUser User who started the meeting
     * @param projectId Associated project ID
     */
    public void publishMeetingStart(Long meetingId, String meetingTitle, User startedByUser, Long projectId) {
        try {
            // ✅ FIXED: Create activity message using correct factory method
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
    // SYSTEM EVENT PUBLISHING - ✅ ENHANCED
    // =========================================================================
    
    /**
     * Publish system alert to all users.
     * 
     * ✅ FIXED: Uses correct ActivityMessage.systemAlert() factory method
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
            
            // ✅ FIXED: Create activity message using correct factory method
            ActivityMessage activity = ActivityMessage.systemAlert(alertMessage, priority);
            activityController.broadcastActivity(activity);
            
            LOGGER.info(String.format("Published system alert: %s (Priority: %s)", alertMessage, priority));
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error publishing system alert", e);
        }
    }
    
    // =========================================================================
    // KANBAN BOARD OPERATIONS - ✅ NEW PHASE 2E-C FEATURES
    // =========================================================================

    /**
     * Publish Kanban board move notification.
     * 
     * ✅ NEW: Phase 2E-C Kanban drag-and-drop support
     * ✅ FIXED: Uses enhanced TaskUpdateMessage.kanbanMove() factory method
     * 
     * @param task Task that was moved
     * @param oldStatus Previous Kanban status
     * @param newStatus New Kanban status
     * @param movedByUser User who moved the task
     */
    public void publishKanbanMove(Task task, String oldStatus, String newStatus, User movedByUser) {
        try {
            // Determine new progress based on Kanban status
            Integer newProgress = getProgressFromKanbanStatus(newStatus, task.getProgress());
            
            // ✅ FIXED: Create Kanban move message using enhanced factory method
            TaskUpdateMessage message = TaskUpdateMessage.kanbanMove(
                task.getId(),
                task.getProject().getId(),
                task.getTitle(),
                oldStatus,
                newStatus,
                newProgress,
                movedByUser != null ? movedByUser.getFullName() : "System"
            );
            
            // Add additional context
            message.setPriority(task.getPriority().name());
            message.setSubsystemName(task.getSubsystem().getName());
            
            // Broadcast via WebSocket controller
            taskUpdateController.broadcastTaskUpdate(message);
            
            // ✅ FIXED: Create activity message using correct factory method
            if (movedByUser != null) {
                ActivityMessage activity = ActivityMessage.kanbanMove(
                    movedByUser.getId(),
                    movedByUser.getFullName(),
                    task.getId(),
                    task.getTitle(),
                    oldStatus,
                    newStatus
                );
                
                // Add user context
                if (movedByUser.getTeamMember() != null && movedByUser.getTeamMember().getSubteam() != null) {
                    activity.setSubteamName(movedByUser.getTeamMember().getSubteam().getName());
                }
                activity.setUserRole(movedByUser.getRole().name());
                
                activityController.broadcastProjectActivity(task.getProject().getId(), activity);
            }
            
            LOGGER.info(String.format("Published Kanban move: Task %d from %s to %s by %s", 
                                    task.getId(), oldStatus, newStatus, 
                                    movedByUser != null ? movedByUser.getFullName() : "System"));
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error publishing Kanban move", e);
        }
    }

    /**
     * Publish bulk Kanban operation notification.
     * 
     * ✅ NEW: Phase 2E-C bulk operations support
     * ✅ FIXED: Uses enhanced TaskUpdateMessage.bulkOperation() factory method
     * 
     * @param operationType Type of bulk operation (COMPLETE, ASSIGN, etc.)
     * @param taskCount Number of tasks affected
     * @param projectId Project ID
     * @param performedByUser User who performed the operation
     */
    public void publishBulkKanbanOperation(String operationType, int taskCount, Long projectId, User performedByUser) {
        try {
            // ✅ FIXED: Create bulk operation message using enhanced factory method
            TaskUpdateMessage message = TaskUpdateMessage.bulkOperation(
                operationType,
                taskCount,
                projectId,
                performedByUser != null ? performedByUser.getFullName() : "System"
            );
            
            // Broadcast to project subscribers
            taskUpdateController.broadcastTaskUpdate(message);
            
            // ✅ FIXED: Create activity message using correct factory method
            if (performedByUser != null) {
                ActivityMessage activity = ActivityMessage.bulkOperation(
                    performedByUser.getId(),
                    performedByUser.getFullName(),
                    operationType,
                    taskCount
                );
                
                if (performedByUser.getTeamMember() != null && performedByUser.getTeamMember().getSubteam() != null) {
                    activity.setSubteamName(performedByUser.getTeamMember().getSubteam().getName());
                }
                activity.setUserRole(performedByUser.getRole().name());
                
                activityController.broadcastProjectActivity(projectId, activity);
            }
            
            LOGGER.info(String.format("Published bulk Kanban operation: %s on %d tasks in project %d", 
                                    operationType, taskCount, projectId));
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error publishing bulk Kanban operation", e);
        }
    }

    /**
     * Publish Kanban board refresh notification.
     * 
     * ✅ NEW: Force refresh of Kanban boards
     * ✅ FIXED: Uses enhanced TaskUpdateMessage.kanbanRefresh() factory method
     * 
     * @param projectId Project ID to refresh
     * @param reason Reason for refresh
     */
    public void publishKanbanRefresh(Long projectId, String reason) {
        try {
            // ✅ FIXED: Create refresh message using enhanced factory method
            TaskUpdateMessage refreshMessage = TaskUpdateMessage.kanbanRefresh(projectId, reason);
            
            taskUpdateController.broadcastTaskUpdate(refreshMessage);
            
            LOGGER.info(String.format("Published Kanban refresh for project %d: %s", projectId, reason));
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error publishing Kanban refresh", e);
        }
    }
    
    // =========================================================================
    // UTILITY METHODS - ✅ NEW HELPER METHODS
    // =========================================================================
    
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
    
    /**
     * ✅ NEW: Determine progress value based on Kanban status.
     * 
     * @param kanbanStatus New Kanban status
     * @param currentProgress Current task progress
     * @return Appropriate progress value for the status
     */
    private Integer getProgressFromKanbanStatus(String kanbanStatus, Integer currentProgress) {
        if (kanbanStatus == null) {
            return currentProgress != null ? currentProgress : 0;
        }
        
        switch (kanbanStatus.toUpperCase()) {
            case "TODO":
                return 0;
            case "IN_PROGRESS":
                // Keep current progress if it's reasonable, otherwise set to 25%
                return (currentProgress != null && currentProgress > 0 && currentProgress < 75) ? 
                       currentProgress : 25;
            case "REVIEW":
                // Set to 75% minimum for review status
                return (currentProgress != null && currentProgress >= 75) ? currentProgress : 75;
            case "COMPLETED":
                return 100;
            default:
                return currentProgress != null ? currentProgress : 0;
        }
    }
    
    /**
     * ✅ NEW: Validate that user can perform real-time operations on task.
     * 
     * @param task Task being modified
     * @param user User attempting the operation
     * @return true if operation is allowed
     */
    private boolean canPerformRealtimeOperation(Task task, User user) {
        if (user == null || task == null) {
            return false;
        }
        
        // Check COPPA compliance
        if (!isRealTimeFeaturesEnabled(user)) {
            return false;
        }
        
        // Admins and mentors can always perform operations
        if (user.getRole().isMentor() || user.getRole() == org.frcpm.models.UserRole.ADMIN) {
            return true;
        }
        
        // Students can only operate on tasks assigned to them
        if (user.getRole().isStudent()) {
            return task.getAssignedTo().stream()
                .anyMatch(member -> member.getUser() != null && 
                         member.getUser().getId().equals(user.getId()));
        }
        
        return false;
    }
}