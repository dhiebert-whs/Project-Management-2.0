// src/main/java/org/frcpm/services/MeetingNotificationService.java
// Phase 3A: Meeting Notification Service Interface

package org.frcpm.services;

import org.frcpm.models.Meeting;
import org.frcpm.models.User;
import org.frcpm.models.Project;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service interface for meeting notifications and reminders.
 * 
 * Provides comprehensive notification capabilities for meeting management,
 * including email reminders, SMS notifications, and real-time updates.
 * 
 * Features:
 * - Automated meeting reminders based on priority
 * - Email and SMS notifications
 * - Real-time WebSocket notifications
 * - Notification scheduling and management
 * - Attendance tracking notifications
 * - Meeting status change notifications
 * 
 * @author FRC Project Management Team
 * @version 3.0.0-3A
 * @since Phase 3A - Meeting Management System
 */
public interface MeetingNotificationService {
    
    // =========================================================================
    // MEETING REMINDERS
    // =========================================================================
    
    /**
     * Send meeting reminder to all attendees.
     * 
     * @param meeting the meeting to send reminders for
     * @param minutesBefore minutes before the meeting
     * @return number of reminders sent
     */
    int sendMeetingReminder(Meeting meeting, int minutesBefore);
    
    /**
     * Send meeting reminder to specific attendees.
     * 
     * @param meeting the meeting
     * @param attendees list of attendees to notify
     * @param minutesBefore minutes before the meeting
     * @return number of reminders sent
     */
    int sendMeetingReminder(Meeting meeting, List<User> attendees, int minutesBefore);
    
    /**
     * Schedule automatic reminders for a meeting.
     * 
     * @param meeting the meeting to schedule reminders for
     * @return number of reminders scheduled
     */
    int scheduleReminders(Meeting meeting);
    
    /**
     * Cancel scheduled reminders for a meeting.
     * 
     * @param meetingId the meeting ID
     * @return number of reminders cancelled
     */
    int cancelReminders(Long meetingId);
    
    /**
     * Process all pending meeting reminders.
     * 
     * @return number of reminders processed
     */
    int processPendingReminders();
    
    // =========================================================================
    // MEETING STATUS NOTIFICATIONS
    // =========================================================================
    
    /**
     * Notify attendees of meeting creation.
     * 
     * @param meeting the created meeting
     * @param createdBy user who created the meeting
     * @return number of notifications sent
     */
    int notifyMeetingCreated(Meeting meeting, User createdBy);
    
    /**
     * Notify attendees of meeting updates.
     * 
     * @param meeting the updated meeting
     * @param updatedBy user who updated the meeting
     * @param changes map of changed fields
     * @return number of notifications sent
     */
    int notifyMeetingUpdated(Meeting meeting, User updatedBy, Map<String, Object> changes);
    
    /**
     * Notify attendees of meeting cancellation.
     * 
     * @param meeting the cancelled meeting
     * @param cancelledBy user who cancelled the meeting
     * @param reason cancellation reason
     * @return number of notifications sent
     */
    int notifyMeetingCancelled(Meeting meeting, User cancelledBy, String reason);
    
    /**
     * Notify attendees of meeting rescheduling.
     * 
     * @param meeting the rescheduled meeting
     * @param rescheduledBy user who rescheduled the meeting
     * @param oldDateTime original date/time
     * @param newDateTime new date/time
     * @return number of notifications sent
     */
    int notifyMeetingRescheduled(Meeting meeting, User rescheduledBy, 
                                LocalDateTime oldDateTime, LocalDateTime newDateTime);
    
    /**
     * Notify when meeting is about to start.
     * 
     * @param meeting the meeting starting
     * @return number of notifications sent
     */
    int notifyMeetingStarting(Meeting meeting);
    
    // =========================================================================
    // ATTENDANCE NOTIFICATIONS
    // =========================================================================
    
    /**
     * Send attendance reminder to specific attendees.
     * 
     * @param meeting the meeting
     * @param attendees attendees who haven't responded
     * @return number of reminders sent
     */
    int sendAttendanceReminder(Meeting meeting, List<User> attendees);
    
    /**
     * Notify organizer of attendance responses.
     * 
     * @param meeting the meeting
     * @param attendanceUpdates recent attendance updates
     * @return number of notifications sent
     */
    int notifyAttendanceUpdates(Meeting meeting, Map<User, String> attendanceUpdates);
    
    /**
     * Send meeting summary after completion.
     * 
     * @param meeting the completed meeting
     * @param summary meeting summary
     * @param actionItems action items from the meeting
     * @return number of summaries sent
     */
    int sendMeetingSummary(Meeting meeting, String summary, List<String> actionItems);
    
    // =========================================================================
    // NOTIFICATION CHANNELS
    // =========================================================================
    
    /**
     * Send email notification.
     * 
     * @param recipients list of email recipients
     * @param subject email subject
     * @param body email body (HTML)
     * @param templateName email template name
     * @param templateData template data
     * @return true if sent successfully
     */
    boolean sendEmailNotification(List<String> recipients, String subject, String body, 
                                 String templateName, Map<String, Object> templateData);
    
    /**
     * Send SMS notification.
     * 
     * @param recipients list of phone numbers
     * @param message SMS message
     * @return true if sent successfully
     */
    boolean sendSMSNotification(List<String> recipients, String message);
    
    /**
     * Send WebSocket notification.
     * 
     * @param projectId project ID for targeting
     * @param notificationType notification type
     * @param message notification message
     * @param metadata additional metadata
     * @return true if sent successfully
     */
    boolean sendWebSocketNotification(Long projectId, String notificationType, 
                                     String message, Map<String, Object> metadata);
    
    // =========================================================================
    // NOTIFICATION PREFERENCES
    // =========================================================================
    
    /**
     * Get notification preferences for a user.
     * 
     * @param user the user
     * @return notification preferences map
     */
    Map<String, Boolean> getNotificationPreferences(User user);
    
    /**
     * Update notification preferences for a user.
     * 
     * @param user the user
     * @param preferences notification preferences
     * @return true if updated successfully
     */
    boolean updateNotificationPreferences(User user, Map<String, Boolean> preferences);
    
    /**
     * Check if user should receive notification type.
     * 
     * @param user the user
     * @param notificationType notification type
     * @return true if user should receive notification
     */
    boolean shouldReceiveNotification(User user, String notificationType);
    
    // =========================================================================
    // NOTIFICATION HISTORY
    // =========================================================================
    
    /**
     * Get notification history for a meeting.
     * 
     * @param meetingId the meeting ID
     * @return list of notifications sent
     */
    List<Map<String, Object>> getNotificationHistory(Long meetingId);
    
    /**
     * Get notification history for a user.
     * 
     * @param user the user
     * @param limit maximum number of notifications
     * @return list of recent notifications
     */
    List<Map<String, Object>> getNotificationHistory(User user, int limit);
    
    /**
     * Mark notification as read.
     * 
     * @param notificationId notification ID
     * @param user the user
     * @return true if marked as read
     */
    boolean markNotificationAsRead(Long notificationId, User user);
    
    // =========================================================================
    // BULK OPERATIONS
    // =========================================================================
    
    /**
     * Send bulk meeting notifications.
     * 
     * @param meetings list of meetings
     * @param notificationType notification type
     * @param templateName template name
     * @return number of notifications sent
     */
    int sendBulkNotifications(List<Meeting> meetings, String notificationType, String templateName);
    
    /**
     * Process scheduled notifications for project.
     * 
     * @param project the project
     * @return number of notifications processed
     */
    int processScheduledNotifications(Project project);
    
    // =========================================================================
    // ASYNC OPERATIONS
    // =========================================================================
    
    /**
     * Send meeting reminder asynchronously.
     * 
     * @param meeting the meeting
     * @param minutesBefore minutes before meeting
     * @return future result
     */
    CompletableFuture<Integer> sendMeetingReminderAsync(Meeting meeting, int minutesBefore);
    
    /**
     * Send meeting notification asynchronously.
     * 
     * @param meeting the meeting
     * @param notificationType notification type
     * @param message notification message
     * @return future result
     */
    CompletableFuture<Integer> sendMeetingNotificationAsync(Meeting meeting, 
                                                          String notificationType, 
                                                          String message);
    
    /**
     * Process all pending reminders asynchronously.
     * 
     * @return future result
     */
    CompletableFuture<Integer> processPendingRemindersAsync();
    
    // =========================================================================
    // NOTIFICATION STATISTICS
    // =========================================================================
    
    /**
     * Get notification statistics for a project.
     * 
     * @param projectId the project ID
     * @return notification statistics
     */
    Map<String, Object> getNotificationStatistics(Long projectId);
    
    /**
     * Get notification delivery statistics.
     * 
     * @return delivery statistics
     */
    Map<String, Object> getDeliveryStatistics();
    
    /**
     * Get notification effectiveness metrics.
     * 
     * @param projectId the project ID
     * @return effectiveness metrics
     */
    Map<String, Object> getNotificationEffectiveness(Long projectId);
}