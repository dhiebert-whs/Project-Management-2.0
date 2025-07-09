// src/main/java/org/frcpm/web/websocket/AttendanceController.java

package org.frcpm.web.websocket;

import org.frcpm.web.dto.AttendanceUpdateMessage;
import org.frcpm.web.dto.TeamPresenceMessage;
import org.frcpm.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * WebSocket controller for real-time attendance coordination.
 * 
 * 🎯 PHASE 2D: AttendanceService WebSocket Integration
 * 
 * Provides real-time workshop presence tracking with:
 * - Live check-in/check-out notifications
 * - Workshop presence summaries
 * - Team coordination features
 * - COPPA-compliant attendance tracking
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 * @since Phase 2D - Real-time Attendance Features
 */
@Controller
public class AttendanceController {
    
    private static final Logger LOGGER = Logger.getLogger(AttendanceController.class.getName());
    
    private final SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    public AttendanceController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    
    // =========================================================================
    // WORKSHOP ATTENDANCE BROADCASTING
    // =========================================================================
    
    /**
     * Broadcast attendance update to all meeting subscribers.
     * 
     * @param message Attendance update message
     */
    public void broadcastAttendanceUpdate(AttendanceUpdateMessage message) {
        try {
            // Broadcast to meeting-specific channel
            String destination = "/topic/meeting/" + message.getMeetingId() + "/attendance";
            messagingTemplate.convertAndSend(destination, message);
            
            // Also broadcast to general workshop channel for dashboard widgets
            messagingTemplate.convertAndSend("/topic/workshop/attendance", message);
            
            LOGGER.info(String.format("Broadcast attendance update: %s - %s", 
                                    message.getMemberName(), message.getEventType()));
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error broadcasting attendance update", e);
        }
    }
    
    /**
     * Broadcast workshop presence summary to all subscribers.
     * 
     * @param message Team presence summary
     */
    public void broadcastPresenceUpdate(TeamPresenceMessage message) {
        try {
            // Broadcast to meeting-specific channel
            String destination = "/topic/meeting/" + message.getMeetingId() + "/presence";
            messagingTemplate.convertAndSend(destination, message);
            
            // Also broadcast to general workshop channel
            messagingTemplate.convertAndSend("/topic/workshop/presence", message);
            
            LOGGER.info(String.format("Broadcast presence update: %d/%d present (%d%%)", 
                                    message.getTotalPresent(), 
                                    message.getTotalExpected(),
                                    message.getAttendancePercentage().intValue()));
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error broadcasting presence update", e);
        }
    }
    
    /**
     * Send targeted attendance notification to specific user.
     * 
     * @param userId Target user ID
     * @param message Attendance notification
     */
    public void sendAttendanceNotificationToUser(Long userId, AttendanceUpdateMessage message) {
        try {
            String destination = "/user/" + userId + "/queue/attendance";
            messagingTemplate.convertAndSend(destination, message);
            
            LOGGER.info(String.format("Sent attendance notification to user %d", userId));
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error sending attendance notification to user", e);
        }
    }
    
    // =========================================================================
    // CLIENT MESSAGE HANDLERS
    // =========================================================================
    
    /**
     * Handle attendance update from client (mobile check-in, etc.).
     * 
     * @param message Attendance update from client
     * @param user Current authenticated user
     * @return Updated attendance message
     */
    @MessageMapping("/attendance/update")
    @SendTo("/topic/workshop/attendance")
    public AttendanceUpdateMessage handleAttendanceUpdate(@Payload AttendanceUpdateMessage message,
                                                         @AuthenticationPrincipal UserPrincipal user) {
        try {
            // Set the user who made the update
            if (user != null) {
                message.setUpdatedBy(user.getFullName());
            }
            
            // Add timestamp if not already set
            if (message.getTimestamp() == null) {
                message.setTimestamp(java.time.LocalDateTime.now());
            }
            
            LOGGER.info(String.format("Processed attendance update from client: %s - %s", 
                                    message.getMemberName(), message.getEventType()));
            
            return message;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error handling attendance update", e);
            return message;
        }
    }
    
    /**
     * Handle workshop presence request from client.
     * 
     * @param meetingId Meeting ID to get presence for
     * @param user Current authenticated user
     */
    @MessageMapping("/attendance/presence/request")
    public void handlePresenceRequest(@Payload Long meetingId,
                                     @AuthenticationPrincipal UserPrincipal user) {
        try {
            // This would typically trigger a service call to get current presence
            // and then broadcast the result - placeholder for now
            LOGGER.info(String.format("Presence request for meeting %d from user %s", 
                                    meetingId, user != null ? user.getUsername() : "unknown"));
            
            // TODO: Integrate with AttendanceService to get real presence data
            // and broadcast via broadcastPresenceUpdate()
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error handling presence request", e);
        }
    }
    
    // =========================================================================
    // WORKSHOP COORDINATION FEATURES
    // =========================================================================
    
    /**
     * Broadcast workshop status change (active, break, closed).
     * 
     * @param meetingId Meeting ID
     * @param status New workshop status
     * @param updatedBy User making the change
     */
    public void broadcastWorkshopStatusChange(Long meetingId, String status, String updatedBy) {
        try {
            TeamPresenceMessage statusMessage = new TeamPresenceMessage(meetingId, "Workshop Session");
            statusMessage.setWorkshopStatus(status);
            statusMessage.setLastUpdated(java.time.LocalDateTime.now());
            
            // Add status change to recent activity
            statusMessage.addRecentActivity(updatedBy, "STATUS_CHANGE", java.time.LocalTime.now());
            
            broadcastPresenceUpdate(statusMessage);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error broadcasting workshop status change", e);
        }
    }
    
    /**
     * Broadcast late arrival alert for team coordination.
     * 
     * @param message Late arrival message
     */
    public void broadcastLateArrival(AttendanceUpdateMessage message) {
        try {
            // Enhance message for late arrival
            message.setEventType("LATE_ARRIVAL");
            message.setNotes("Arrived after scheduled start time");
            
            // Broadcast to supervisors/mentors channel
            messagingTemplate.convertAndSend("/topic/workshop/alerts", message);
            
            // Also broadcast to general attendance channel
            broadcastAttendanceUpdate(message);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error broadcasting late arrival", e);
        }
    }
}