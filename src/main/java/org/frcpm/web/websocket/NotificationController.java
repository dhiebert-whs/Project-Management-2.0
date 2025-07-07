// src/main/java/org/frcpm/web/websocket/NotificationController.java

package org.frcpm.web.websocket;

import org.frcpm.web.dto.ProjectNotification;
import org.frcpm.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * WebSocket controller for real-time notifications.
 * 
 * ✅ PHASE 2C: PWA Development - Real-time Notification System
 * 
 * Handles project-wide announcements, deadline alerts, milestone celebrations,
 * and system notifications for immediate team awareness during build season.
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 * @since Phase 2C - Progressive Web App Development
 */
@Controller
public class NotificationController {
    
    private static final Logger LOGGER = Logger.getLogger(NotificationController.class.getName());
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    /**
     * Handle notification broadcasts from authenticated users.
     * 
     * Allows mentors and admins to send real-time announcements
     * to all team members working on a project.
     * 
     * @param notification Notification to broadcast
     * @param user Authenticated user sending notification
     */
    @MessageMapping("/notification/send")
    public void sendNotification(@Payload ProjectNotification notification,
                                @AuthenticationPrincipal UserPrincipal user) {
        
        try {
            // Validate user permissions for sending notifications
            if (user == null) {
                LOGGER.warning("Attempted to send notification without authentication");
                return;
            }
            
            // Only mentors and admins can send project-wide notifications
            if (!user.getUser().getRole().isMentor() && 
                !user.getUser().getRole().equals(org.frcpm.models.UserRole.ADMIN)) {
                LOGGER.warning(String.format("User %s attempted to send notification without permission", 
                                            user.getUsername()));
                return;
            }
            
            // Set sender information
            notification.setSender(user.getFullName());
            
            LOGGER.info(String.format("Broadcasting notification from %s: %s", 
                                    user.getUsername(), notification.getTitle()));
            
            // Broadcast to all project subscribers
            broadcastNotification(notification);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing notification send request", e);
        }
    }
    
    /**
     * Broadcast notification to all project subscribers.
     * 
     * Server-side method for system-generated notifications.
     * 
     * @param notification Notification to broadcast
     */
    public void broadcastNotification(ProjectNotification notification) {
        try {
            if (notification.getProjectId() != null) {
                String destination = "/topic/project/" + notification.getProjectId() + "/notifications";
                messagingTemplate.convertAndSend(destination, notification);
                
                LOGGER.info(String.format("Broadcasted notification to %s: %s (Type: %s)", 
                                        destination, notification.getTitle(), notification.getType()));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error broadcasting notification", e);
        }
    }
    
    /**
     * Send notification to specific user.
     * 
     * Used for personal notifications like task assignments or mentions.
     * 
     * @param userId Target user ID
     * @param notification Notification to send
     */
    public void sendNotificationToUser(Long userId, ProjectNotification notification) {
        try {
            String destination = "/user/" + userId + "/queue/notifications";
            messagingTemplate.convertAndSend(destination, notification);
            
            LOGGER.info(String.format("Sent notification to user %d: %s", userId, notification.getTitle()));
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error sending notification to user", e);
        }
    }
    
    /**
     * Send system-wide alert to all connected users.
     * 
     * Used for critical system announcements that all users should see
     * regardless of their current project context.
     * 
     * @param notification System alert notification
     */
    public void broadcastSystemAlert(ProjectNotification notification) {
        try {
            messagingTemplate.convertAndSend("/topic/system/alerts", notification);
            
            LOGGER.info(String.format("Broadcasted system alert: %s (Priority: %s)", 
                                    notification.getTitle(), notification.getPriority()));
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error broadcasting system alert", e);
        }
    }
}