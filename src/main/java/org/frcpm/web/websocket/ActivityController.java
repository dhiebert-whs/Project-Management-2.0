// src/main/java/org/frcpm/web/websocket/ActivityController.java

package org.frcpm.web.websocket;

import org.frcpm.web.dto.ActivityMessage;
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
 * WebSocket controller for team activity streams.
 * 
 * ✅ PHASE 2C: PWA Development - Real-time Activity Feeds
 * 
 * Tracks and broadcasts team member activities to provide visibility
 * into ongoing work and collaboration during intensive build sessions.
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 * @since Phase 2C - Progressive Web App Development
 */
@Controller
public class ActivityController {
    
    private static final Logger LOGGER = Logger.getLogger(ActivityController.class.getName());
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    /**
     * Handle activity updates from clients.
     * 
     * Team members can broadcast their current activities for enhanced
     * collaboration visibility and coordination.
     * 
     * @param activity Activity message from client
     * @param user Authenticated user
     */
    @MessageMapping("/activity/update")
    public void updateActivity(@Payload ActivityMessage activity,
                              @AuthenticationPrincipal UserPrincipal user) {
        
        try {
            if (user == null) {
                LOGGER.warning("Attempted to send activity update without authentication");
                return;
            }
            
            // Ensure activity is attributed to authenticated user
            activity.setUserId(user.getUser().getId());
            activity.setUserName(user.getFullName());
            activity.setUserRole(user.getUser().getRole().name());
            
            // Add subteam context if available
            if (user.getUser().getTeamMember() != null && 
                user.getUser().getTeamMember().getSubteam() != null) {
                activity.setSubteamName(user.getUser().getTeamMember().getSubteam().getName());
            }
            
            LOGGER.info(String.format("Activity update from %s: %s", 
                                    user.getUsername(), activity.getAction()));
            
            // Broadcast activity to team feed
            broadcastActivity(activity);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing activity update", e);
        }
    }
    
    /**
     * Broadcast activity to team activity feed.
     * 
     * Server-side method for system-generated activities.
     * 
     * @param activity Activity to broadcast
     */
    public void broadcastActivity(ActivityMessage activity) {
        try {
            // Send to global team activity feed
            messagingTemplate.convertAndSend("/topic/team/activity", activity);
            
            // Also send to subteam-specific feed if available
            if (activity.getSubteamName() != null && !activity.getSubteamName().isEmpty()) {
                String subteamDestination = "/topic/subteam/" + 
                    activity.getSubteamName().replaceAll("\\s+", "_").toLowerCase() + "/activity";
                messagingTemplate.convertAndSend(subteamDestination, activity);
            }
            
            LOGGER.info(String.format("Broadcasted activity: %s - %s", 
                                    activity.getUserName(), activity.getAction()));
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error broadcasting activity", e);
        }
    }
    
    /**
     * Broadcast user login/logout events.
     * 
     * Automatically called when users connect/disconnect from WebSocket.
     * 
     * @param activity Login/logout activity
     */
    public void broadcastUserPresence(ActivityMessage activity) {
        try {
            // Send presence updates to team presence channel
            messagingTemplate.convertAndSend("/topic/team/presence", activity);
            
            LOGGER.info(String.format("User presence update: %s - %s", 
                                    activity.getUserName(), activity.getAction()));
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error broadcasting user presence", e);
        }
    }
    
    /**
     * Send activity update to specific project subscribers.
     * 
     * Used for project-specific activities that should only be visible
     * to team members working on that particular project.
     * 
     * @param projectId Target project ID
     * @param activity Activity to broadcast
     */
    public void broadcastProjectActivity(Long projectId, ActivityMessage activity) {
        try {
            String destination = "/topic/project/" + projectId + "/activity";
            messagingTemplate.convertAndSend(destination, activity);
            
            LOGGER.info(String.format("Broadcasted project activity to %s: %s", 
                                    destination, activity.getAction()));
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error broadcasting project activity", e);
        }
    }
}