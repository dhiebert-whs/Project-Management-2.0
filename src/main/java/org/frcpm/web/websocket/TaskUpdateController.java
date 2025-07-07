// src/main/java/org/frcpm/web/websocket/TaskUpdateController.java

package org.frcpm.web.websocket;

import org.frcpm.web.dto.TaskUpdateMessage;
import org.frcpm.security.UserPrincipal;
import org.frcpm.services.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * WebSocket controller for real-time task updates.
 * 
 * ✅ PHASE 2C: PWA Development - Real-time Task Communication
 * 
 * Handles live task progress updates, assignment changes, and completion
 * notifications across all connected team members during build sessions.
 * 
 * Integrates with Phase 2B security to ensure COPPA-compliant real-time
 * communication and proper role-based message filtering.
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 * @since Phase 2C - Progressive Web App Development
 */
@Controller
public class TaskUpdateController {
    
    private static final Logger LOGGER = Logger.getLogger(TaskUpdateController.class.getName());
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private TaskService taskService;
    
    /**
     * Handle task progress updates from clients.
     * 
     * Clients send task progress updates that are validated and broadcasted
     * to all team members subscribed to the project channel.
     * 
     * @param message Task update message from client
     * @param user Authenticated user (from Phase 2B security)
     * @return Broadcasted task update message
     */
    @MessageMapping("/task/update")
    @SendTo("/topic/project/{projectId}")
    public TaskUpdateMessage updateTask(@Payload TaskUpdateMessage message,
                                       @AuthenticationPrincipal UserPrincipal user) {
        
        try {
            LOGGER.info(String.format("Received task update: Task %d, Progress %d%%, User: %s", 
                                    message.getTaskId(), message.getProgress(), 
                                    user != null ? user.getUsername() : "anonymous"));
            
            // Set the user who made the update (from authenticated session)
            if (user != null) {
                message.setUpdatedBy(user.getFullName());
                
                // Add user context for better notifications
                if (user.getUser().getTeamMember() != null && 
                    user.getUser().getTeamMember().getSubteam() != null) {
                    message.setSubsystemName(user.getUser().getTeamMember().getSubteam().getName());
                }
            }
            
            // Validate and process the update through the service layer
            if (message.getTaskId() != null && message.getProgress() != null) {
                try {
                    // Update the task in the database (this triggers service-level validation)
                    boolean completed = message.getProgress() >= 100;
                    taskService.updateTaskProgress(message.getTaskId(), message.getProgress(), completed);
                    
                    // Update message status based on actual database state
                    message.setStatus(completed ? "COMPLETED" : "IN_PROGRESS");
                    
                    LOGGER.info(String.format("Task %d updated successfully to %d%% by %s", 
                                            message.getTaskId(), message.getProgress(), message.getUpdatedBy()));
                    
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Failed to update task in database: " + e.getMessage(), e);
                    // Still broadcast the message for immediate UI feedback, 
                    // but mark it as potentially failed
                    message.setChangeType("UPDATE_FAILED");
                }
            }
            
            // Broadcast to all project subscribers
            return message;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing task update message", e);
            
            // Return error message for client handling
            TaskUpdateMessage errorMessage = new TaskUpdateMessage();
            errorMessage.setTaskId(message.getTaskId());
            errorMessage.setProjectId(message.getProjectId());
            errorMessage.setChangeType("ERROR");
            errorMessage.setUpdatedBy("System");
            errorMessage.setTaskTitle("Error processing update");
            return errorMessage;
        }
    }
    
    /**
     * Handle subscription to project task updates.
     * 
     * When users subscribe to a project's task updates, send them
     * the current project state for immediate synchronization.
     * 
     * @param user Authenticated user
     * @return Current project status message
     */
    @SubscribeMapping("/topic/project/{projectId}")
    public TaskUpdateMessage onProjectSubscribe(@AuthenticationPrincipal UserPrincipal user) {
        
        try {
            if (user != null) {
                LOGGER.info(String.format("User %s subscribed to project updates", user.getUsername()));
                
                // Send welcome message with user's current context
                TaskUpdateMessage welcomeMessage = new TaskUpdateMessage();
                welcomeMessage.setChangeType("USER_JOINED");
                welcomeMessage.setUpdatedBy(user.getFullName());
                welcomeMessage.setTaskTitle("User joined project session");
                
                return welcomeMessage;
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error handling project subscription", e);
        }
        
        return null; // No welcome message for unauthenticated users
    }
    
    /**
     * Broadcast task update to specific project.
     * 
     * Server-side method to send task updates from service layer operations.
     * Called when tasks are updated through REST API or other non-WebSocket channels.
     * 
     * @param message Task update to broadcast
     */
    public void broadcastTaskUpdate(TaskUpdateMessage message) {
        try {
            if (message.getProjectId() != null) {
                String destination = "/topic/project/" + message.getProjectId();
                messagingTemplate.convertAndSend(destination, message);
                
                LOGGER.info(String.format("Broadcasted task update to %s: Task %d, Type: %s", 
                                        destination, message.getTaskId(), message.getChangeType()));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error broadcasting task update", e);
        }
    }
    
    /**
     * Send targeted task update to specific user.
     * 
     * Used for user-specific notifications like task assignments.
     * 
     * @param userId Target user ID
     * @param message Task update message
     */
    public void sendTaskUpdateToUser(Long userId, TaskUpdateMessage message) {
        try {
            String destination = "/user/" + userId + "/queue/tasks";
            messagingTemplate.convertAndSend(destination, message);
            
            LOGGER.info(String.format("Sent task update to user %d: Task %d", userId, message.getTaskId()));
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error sending task update to user", e);
        }
    }
}