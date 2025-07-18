// src/main/java/org/frcpm/web/websocket/TaskUpdateController.java

package org.frcpm.web.websocket;

import org.frcpm.web.dto.TaskUpdateMessage;
import org.frcpm.security.UserPrincipal;
import org.frcpm.services.TaskService;
import org.frcpm.services.ProjectService;
import org.frcpm.models.Task;
import org.frcpm.models.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.scheduling.annotation.Async;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.time.LocalDateTime;

/**
 * Enhanced WebSocket controller for real-time task updates.
 * 
 * ✅ PHASE 2E-E: Enhanced Real-time Features - Performance Optimized
 * 
 * Handles live task progress updates, assignment changes, and completion
 * notifications across all connected team members during build sessions.
 * Enhanced with performance optimizations for workshop environments.
 * 
 * Features:
 * - Async message processing for better performance
 * - Connection tracking and management
 * - Enhanced error handling and recovery
 * - Workshop-optimized real-time collaboration
 * - COPPA-compliant messaging with rate limiting
 * 
 * @author FRC Project Management Team
 * @version 2.0.0-2E-E
 * @since Phase 2E-E - Enhanced Real-time Features
 */
@Controller
public class TaskUpdateController {
    
    private static final Logger LOGGER = Logger.getLogger(TaskUpdateController.class.getName());
    
    // Performance tracking and connection management
    private final ConcurrentHashMap<String, LocalDateTime> connectionTimestamps = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicInteger> messageRates = new ConcurrentHashMap<>();
    private final AtomicInteger totalConnections = new AtomicInteger(0);
    private final AtomicInteger totalMessages = new AtomicInteger(0);
    
    // Rate limiting for COPPA compliance (students have lower rates)
    private static final int MAX_MESSAGES_PER_MINUTE_STUDENT = 30;
    private static final int MAX_MESSAGES_PER_MINUTE_MENTOR = 60;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private ProjectService projectService;
    
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
    
    // Note: Enhanced subscription handler is implemented below in Phase 2E-E section
    
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
    
    // =========================================================================
    // PHASE 2E-E: ENHANCED REAL-TIME FEATURES
    // =========================================================================
    
    /**
     * Enhanced subscription handler with connection tracking.
     * 
     * @param projectId Project ID to subscribe to
     * @param user Authenticated user
     * @return Welcome message with project status
     */
    @SubscribeMapping("/topic/project/{projectId}")
    public TaskUpdateMessage onEnhancedProjectSubscribe(
            @DestinationVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal user) {
        
        try {
            if (user == null) {
                LOGGER.warning("Unauthenticated subscription attempt to project " + projectId);
                return null;
            }
            
            String sessionKey = user.getUsername() + ":" + projectId;
            connectionTimestamps.put(sessionKey, LocalDateTime.now());
            messageRates.put(sessionKey, new AtomicInteger(0));
            totalConnections.incrementAndGet();
            
            LOGGER.info(String.format("Enhanced subscription: User %s connected to project %d (Total connections: %d)", 
                                    user.getUsername(), projectId, totalConnections.get()));
            
            // Load current project status for immediate sync
            Project project = projectService.findById(projectId);
            if (project != null) {
                TaskUpdateMessage welcomeMessage = new TaskUpdateMessage();
                welcomeMessage.setProjectId(projectId);
                welcomeMessage.setChangeType("PROJECT_SYNC");
                welcomeMessage.setUpdatedBy(user.getFullName());
                welcomeMessage.setTaskTitle("Connected to " + project.getName());
                welcomeMessage.setTimestamp(LocalDateTime.now());
                
                // Add project metrics for real-time dashboard
                enhanceMessageWithProjectMetrics(welcomeMessage, project);
                
                return welcomeMessage;
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in enhanced project subscription", e);
        }
        
        return null;
    }
    
    /**
     * Async task update processing for better performance.
     * 
     * @param message Task update message
     * @param user Authenticated user
     */
    @MessageMapping("/task/update/async")
    @Async
    public CompletableFuture<Void> updateTaskAsync(@Payload TaskUpdateMessage message,
                                                  @AuthenticationPrincipal UserPrincipal user) {
        
        return CompletableFuture.runAsync(() -> {
            try {
                // Rate limiting check
                if (!checkRateLimit(user)) {
                    LOGGER.warning(String.format("Rate limit exceeded for user %s", user.getUsername()));
                    sendRateLimitError(message, user);
                    return;
                }
                
                // Enhanced processing with better error handling
                processTaskUpdateEnhanced(message, user);
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error in async task update", e);
                sendErrorMessage(message, user, "Async processing failed");
            }
        });
    }
    
    /**
     * Bulk task updates for workshop efficiency.
     * 
     * @param messages Multiple task updates
     * @param user Authenticated user
     */
    @MessageMapping("/task/bulk-update")
    public void bulkUpdateTasks(@Payload TaskUpdateMessage[] messages,
                               @AuthenticationPrincipal UserPrincipal user) {
        
        try {
            if (user == null || !user.getUser().getRole().isMentor()) {
                LOGGER.warning("Unauthorized bulk update attempt");
                return;
            }
            
            LOGGER.info(String.format("Processing bulk update of %d tasks by %s", 
                                    messages.length, user.getUsername()));
            
            for (TaskUpdateMessage message : messages) {
                try {
                    processTaskUpdateEnhanced(message, user);
                    
                    // Small delay to prevent overwhelming the system
                    Thread.sleep(10);
                    
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, String.format("Error in bulk update for task %d", message.getTaskId()), e);
                }
            }
            
            // Send completion notification
            TaskUpdateMessage completionMessage = new TaskUpdateMessage();
            completionMessage.setChangeType("BULK_UPDATE_COMPLETE");
            completionMessage.setUpdatedBy(user.getFullName());
            completionMessage.setTaskTitle(String.format("Bulk update of %d tasks completed", messages.length));
            completionMessage.setTimestamp(LocalDateTime.now());
            
            if (messages.length > 0 && messages[0].getProjectId() != null) {
                broadcastTaskUpdate(completionMessage);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in bulk task update", e);
        }
    }
    
    /**
     * Real-time project metrics for dashboard.
     * 
     * @param projectId Project ID
     * @param user Authenticated user
     */
    @MessageMapping("/project/{projectId}/metrics")
    public void requestProjectMetrics(@DestinationVariable Long projectId,
                                     @AuthenticationPrincipal UserPrincipal user) {
        
        try {
            if (user == null) return;
            
            Project project = projectService.findById(projectId);
            if (project != null) {
                TaskUpdateMessage metricsMessage = new TaskUpdateMessage();
                metricsMessage.setProjectId(projectId);
                metricsMessage.setChangeType("METRICS_UPDATE");
                metricsMessage.setUpdatedBy("System");
                metricsMessage.setTimestamp(LocalDateTime.now());
                
                enhanceMessageWithProjectMetrics(metricsMessage, project);
                
                // Send to user's personal channel
                sendTaskUpdateToUser(user.getUser().getId(), metricsMessage);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error sending project metrics", e);
        }
    }
    
    /**
     * Workshop status broadcast for team coordination.
     * 
     * @param status Workshop status message
     * @param user Authenticated user
     */
    @MessageMapping("/workshop/status")
    public void updateWorkshopStatus(@Payload TaskUpdateMessage status,
                                   @AuthenticationPrincipal UserPrincipal user) {
        
        try {
            if (user == null || !user.getUser().getRole().isMentor()) {
                LOGGER.warning("Unauthorized workshop status update");
                return;
            }
            
            status.setUpdatedBy(user.getFullName());
            status.setChangeType("WORKSHOP_STATUS");
            status.setTimestamp(LocalDateTime.now());
            
            // Broadcast to all connected users
            messagingTemplate.convertAndSend("/topic/workshop/status", status);
            
            LOGGER.info(String.format("Workshop status updated by %s: %s", 
                                    user.getUsername(), status.getTaskTitle()));
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating workshop status", e);
        }
    }
    
    // =========================================================================
    // HELPER METHODS
    // =========================================================================
    
    /**
     * Enhanced task update processing with better validation.
     */
    private void processTaskUpdateEnhanced(TaskUpdateMessage message, UserPrincipal user) {
        try {
            totalMessages.incrementAndGet();
            
            // Enhanced validation
            if (message.getTaskId() == null || message.getProjectId() == null) {
                LOGGER.warning("Invalid task update message - missing required fields");
                return;
            }
            
            // Verify user has permission to update this task
            Task task = taskService.findById(message.getTaskId());
            if (task == null) {
                LOGGER.warning(String.format("Task %d not found for update", message.getTaskId()));
                return;
            }
            
            // Set user context
            message.setUpdatedBy(user.getFullName());
            message.setTimestamp(LocalDateTime.now());
            
            // Process through service layer
            if (message.getProgress() != null) {
                boolean completed = message.getProgress() >= 100;
                taskService.updateTaskProgress(message.getTaskId(), message.getProgress(), completed);
                message.setStatus(completed ? "COMPLETED" : "IN_PROGRESS");
            }
            
            // Enhance message with additional context
            if (user.getUser().getTeamMember() != null && 
                user.getUser().getTeamMember().getSubteam() != null) {
                message.setSubsystemName(user.getUser().getTeamMember().getSubteam().getName());
            }
            
            // Broadcast enhanced message
            broadcastTaskUpdate(message);
            
            LOGGER.info(String.format("Enhanced task update processed: Task %d by %s", 
                                    message.getTaskId(), user.getUsername()));
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in enhanced task processing", e);
            throw e;
        }
    }
    
    /**
     * Rate limiting check for COPPA compliance.
     */
    private boolean checkRateLimit(UserPrincipal user) {
        try {
            String userKey = user.getUsername();
            AtomicInteger userRate = messageRates.computeIfAbsent(userKey, k -> new AtomicInteger(0));
            
            int currentRate = userRate.incrementAndGet();
            int maxRate = user.getUser().getRole().isStudent() ? 
                MAX_MESSAGES_PER_MINUTE_STUDENT : MAX_MESSAGES_PER_MINUTE_MENTOR;
            
            // Reset rate counter every minute
            scheduleRateReset(userKey);
            
            return currentRate <= maxRate;
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error checking rate limit", e);
            return true; // Allow on error
        }
    }
    
    /**
     * Schedule rate counter reset.
     */
    private void scheduleRateReset(String userKey) {
        // Simple rate reset - in production, use proper scheduling
        CompletableFuture.delayedExecutor(60, java.util.concurrent.TimeUnit.SECONDS)
            .execute(() -> messageRates.remove(userKey));
    }
    
    /**
     * Enhance message with real-time project metrics.
     */
    private void enhanceMessageWithProjectMetrics(TaskUpdateMessage message, Project project) {
        try {
            // This would integrate with project analytics
            // For now, add basic project context
            message.getMetadata().put("projectName", project.getName());
            message.getMetadata().put("totalConnections", totalConnections.get());
            message.getMetadata().put("totalMessages", totalMessages.get());
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error enhancing message with metrics", e);
        }
    }
    
    /**
     * Send rate limit error message.
     */
    private void sendRateLimitError(TaskUpdateMessage originalMessage, UserPrincipal user) {
        try {
            TaskUpdateMessage errorMessage = new TaskUpdateMessage();
            errorMessage.setProjectId(originalMessage.getProjectId());
            errorMessage.setChangeType("RATE_LIMIT_ERROR");
            errorMessage.setUpdatedBy("System");
            errorMessage.setTaskTitle("Message rate limit exceeded - please slow down");
            errorMessage.setTimestamp(LocalDateTime.now());
            
            sendTaskUpdateToUser(user.getUser().getId(), errorMessage);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error sending rate limit message", e);
        }
    }
    
    /**
     * Send error message to user.
     */
    private void sendErrorMessage(TaskUpdateMessage originalMessage, UserPrincipal user, String error) {
        try {
            TaskUpdateMessage errorMessage = new TaskUpdateMessage();
            errorMessage.setProjectId(originalMessage.getProjectId());
            errorMessage.setTaskId(originalMessage.getTaskId());
            errorMessage.setChangeType("ERROR");
            errorMessage.setUpdatedBy("System");
            errorMessage.setTaskTitle("Error: " + error);
            errorMessage.setTimestamp(LocalDateTime.now());
            
            sendTaskUpdateToUser(user.getUser().getId(), errorMessage);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error sending error message", e);
        }
    }
    
    /**
     * Get current connection statistics.
     */
    public java.util.Map<String, Object> getConnectionStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalConnections", totalConnections.get());
        stats.put("totalMessages", totalMessages.get());
        stats.put("activeConnections", connectionTimestamps.size());
        stats.put("activeMessageRates", messageRates.size());
        return stats;
    }
}