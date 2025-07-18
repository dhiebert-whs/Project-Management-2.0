// src/main/java/org/frcpm/services/NotificationService.java
// Phase 2E-E: Enhanced Real-time Features - Mobile Notifications

package org.frcpm.services;

import org.frcpm.models.User;
import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.web.dto.NotificationDto;

import java.util.List;
import java.util.Map;

/**
 * Service interface for mobile notifications and critical alerts.
 * 
 * Provides push notifications, SMS alerts, and email notifications
 * for critical events during FRC build season operations.
 * 
 * Features:
 * - Push notifications for mobile devices
 * - SMS alerts for critical deadlines
 * - Email notifications for mentors
 * - Workshop status alerts
 * - COPPA-compliant notifications for students
 * 
 * @author FRC Project Management Team
 * @version 2.0.0-2E-E
 * @since Phase 2E-E - Enhanced Real-time Features
 */
public interface NotificationService {
    
    // =========================================================================
    // PUSH NOTIFICATIONS
    // =========================================================================
    
    /**
     * Send push notification to user's mobile device.
     * 
     * @param user Target user
     * @param title Notification title
     * @param message Notification message
     * @param type Notification type (INFO, WARNING, CRITICAL)
     * @param data Additional data payload
     * @return true if notification was sent successfully
     */
    boolean sendPushNotification(User user, String title, String message, 
                                NotificationType type, Map<String, Object> data);
    
    /**
     * Send push notification to multiple users.
     * 
     * @param users Target users
     * @param title Notification title
     * @param message Notification message
     * @param type Notification type
     * @param data Additional data payload
     * @return map of user IDs to success status
     */
    Map<Long, Boolean> sendBulkPushNotifications(List<User> users, String title, 
                                                String message, NotificationType type, 
                                                Map<String, Object> data);
    
    /**
     * Send push notification to all project members.
     * 
     * @param project Target project
     * @param title Notification title
     * @param message Notification message
     * @param type Notification type
     * @param excludeUser User to exclude from notification (optional)
     * @return number of notifications sent
     */
    int sendProjectPushNotification(Project project, String title, String message, 
                                   NotificationType type, User excludeUser);
    
    // =========================================================================
    // CRITICAL ALERTS
    // =========================================================================
    
    /**
     * Send critical alert to all available channels (push, SMS, email).
     * 
     * @param user Target user
     * @param title Alert title
     * @param message Alert message
     * @param urgencyLevel Urgency level (MEDIUM, HIGH, CRITICAL)
     * @param context Additional context for the alert
     * @return true if at least one channel succeeded
     */
    boolean sendCriticalAlert(User user, String title, String message, 
                             UrgencyLevel urgencyLevel, AlertContext context);
    
    /**
     * Send deadline alert for approaching project deadlines.
     * 
     * @param project Project with approaching deadline
     * @param daysRemaining Days until deadline
     * @param recipients Users to notify
     * @return number of alerts sent
     */
    int sendDeadlineAlert(Project project, int daysRemaining, List<User> recipients);
    
    /**
     * Send workshop emergency alert.
     * 
     * @param message Emergency message
     * @param location Workshop location (optional)
     * @param severity Emergency severity
     * @return number of alerts sent to all active users
     */
    int sendWorkshopEmergencyAlert(String message, String location, EmergencySeverity severity);
    
    /**
     * Send task assignment alert.
     * 
     * @param task Assigned task
     * @param assignee User assigned to task
     * @param assigner User who made the assignment
     * @return true if notification was sent
     */
    boolean sendTaskAssignmentAlert(Task task, User assignee, User assigner);
    
    // =========================================================================
    // EMAIL NOTIFICATIONS
    // =========================================================================
    
    /**
     * Send email notification.
     * 
     * @param user Target user
     * @param subject Email subject
     * @param htmlContent HTML email content
     * @param textContent Plain text fallback
     * @param priority Email priority
     * @return true if email was sent successfully
     */
    boolean sendEmailNotification(User user, String subject, String htmlContent, 
                                 String textContent, EmailPriority priority);
    
    /**
     * Send weekly summary email to mentors.
     * 
     * @param mentor Target mentor
     * @param projects Projects to include in summary
     * @param weekStartDate Start date for the summary week
     * @return true if email was sent
     */
    boolean sendWeeklySummaryEmail(User mentor, List<Project> projects, java.time.LocalDate weekStartDate);
    
    /**
     * Send COPPA consent email to parents.
     * 
     * @param parentEmail Parent's email address
     * @param studentName Student's name
     * @param consentToken Consent verification token
     * @return true if email was sent
     */
    boolean sendCOPPAConsentEmail(String parentEmail, String studentName, String consentToken);
    
    // =========================================================================
    // SMS ALERTS
    // =========================================================================
    
    /**
     * Send SMS alert for critical situations.
     * 
     * @param phoneNumber Target phone number
     * @param message SMS message (160 chars max)
     * @param urgency Message urgency level
     * @return true if SMS was sent successfully
     */
    boolean sendSMSAlert(String phoneNumber, String message, UrgencyLevel urgency);
    
    /**
     * Send SMS alert to user (uses their phone number from profile).
     * 
     * @param user Target user
     * @param message SMS message
     * @param urgency Message urgency level
     * @return true if SMS was sent
     */
    boolean sendSMSAlert(User user, String message, UrgencyLevel urgency);
    
    // =========================================================================
    // NOTIFICATION MANAGEMENT
    // =========================================================================
    
    /**
     * Register user's mobile device for push notifications.
     * 
     * @param user Target user
     * @param deviceToken FCM/APNS device token
     * @param deviceType Device type (ANDROID, IOS, WEB)
     * @param userAgent User agent string
     * @return true if device was registered successfully
     */
    boolean registerMobileDevice(User user, String deviceToken, DeviceType deviceType, String userAgent);
    
    /**
     * Unregister user's mobile device.
     * 
     * @param user Target user
     * @param deviceToken Device token to unregister
     * @return true if device was unregistered
     */
    boolean unregisterMobileDevice(User user, String deviceToken);
    
    /**
     * Get user's notification preferences.
     * 
     * @param user Target user
     * @return User's notification preferences
     */
    NotificationPreferences getUserNotificationPreferences(User user);
    
    /**
     * Update user's notification preferences.
     * 
     * @param user Target user
     * @param preferences New notification preferences
     * @return true if preferences were updated
     */
    boolean updateNotificationPreferences(User user, NotificationPreferences preferences);
    
    /**
     * Get recent notifications for user.
     * 
     * @param user Target user
     * @param limit Maximum number of notifications to return
     * @param unreadOnly Whether to return only unread notifications
     * @return List of recent notifications
     */
    List<NotificationDto> getRecentNotifications(User user, int limit, boolean unreadOnly);
    
    /**
     * Mark notification as read.
     * 
     * @param notificationId Notification ID
     * @param user User marking the notification as read
     * @return true if notification was marked as read
     */
    boolean markNotificationAsRead(Long notificationId, User user);
    
    /**
     * Mark all notifications as read for user.
     * 
     * @param user Target user
     * @return number of notifications marked as read
     */
    int markAllNotificationsAsRead(User user);
    
    // =========================================================================
    // WORKSHOP STATUS NOTIFICATIONS
    // =========================================================================
    
    /**
     * Send workshop opening notification.
     * 
     * @param openedBy User who opened the workshop
     * @param estimatedCloseTime Estimated workshop close time
     * @return number of notifications sent
     */
    int sendWorkshopOpenNotification(User openedBy, java.time.LocalDateTime estimatedCloseTime);
    
    /**
     * Send workshop closing notification.
     * 
     * @param closedBy User who closed the workshop
     * @param summary Workshop session summary
     * @return number of notifications sent
     */
    int sendWorkshopCloseNotification(User closedBy, String summary);
    
    /**
     * Send safety reminder notification.
     * 
     * @param message Safety reminder message
     * @param targetUsers Users to notify (null for all active users)
     * @return number of notifications sent
     */
    int sendSafetyReminder(String message, List<User> targetUsers);
    
    // =========================================================================
    // HELPER ENUMS AND CLASSES
    // =========================================================================
    
    /**
     * Notification type enumeration.
     */
    enum NotificationType {
        INFO,           // General information
        SUCCESS,        // Success confirmation
        WARNING,        // Warning message
        ERROR,          // Error notification
        CRITICAL,       // Critical alert
        DEADLINE,       // Deadline reminder
        ASSIGNMENT,     // Task assignment
        WORKSHOP,       // Workshop status
        EMERGENCY       // Emergency alert
    }
    
    /**
     * Urgency level enumeration.
     */
    enum UrgencyLevel {
        LOW,            // Low priority
        MEDIUM,         // Medium priority
        HIGH,           // High priority
        CRITICAL        // Critical priority (immediate attention)
    }
    
    /**
     * Emergency severity enumeration.
     */
    enum EmergencySeverity {
        MINOR,          // Minor issue
        MODERATE,       // Moderate emergency
        MAJOR,          // Major emergency
        CRITICAL        // Critical emergency (safety concern)
    }
    
    /**
     * Email priority enumeration.
     */
    enum EmailPriority {
        LOW,
        NORMAL,
        HIGH,
        URGENT
    }
    
    /**
     * Device type enumeration.
     */
    enum DeviceType {
        ANDROID,        // Android device
        IOS,            // iOS device
        WEB,            // Web browser
        DESKTOP         // Desktop application
    }
    
    /**
     * Alert context for providing additional information.
     */
    class AlertContext {
        private Long projectId;
        private Long taskId;
        private String location;
        private Map<String, Object> metadata;
        
        public AlertContext() {}
        
        public AlertContext(Long projectId, Long taskId, String location) {
            this.projectId = projectId;
            this.taskId = taskId;
            this.location = location;
        }
        
        // Getters and setters
        public Long getProjectId() { return projectId; }
        public void setProjectId(Long projectId) { this.projectId = projectId; }
        
        public Long getTaskId() { return taskId; }
        public void setTaskId(Long taskId) { this.taskId = taskId; }
        
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        
        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    }
    
    /**
     * User notification preferences.
     */
    class NotificationPreferences {
        private boolean pushNotifications = true;
        private boolean emailNotifications = true;
        private boolean smsAlerts = false;
        private boolean deadlineReminders = true;
        private boolean taskAssignments = true;
        private boolean workshopUpdates = true;
        private boolean emergencyAlerts = true;
        private UrgencyLevel minimumUrgency = UrgencyLevel.MEDIUM;
        private java.time.LocalTime quietHoursStart;
        private java.time.LocalTime quietHoursEnd;
        
        public NotificationPreferences() {}
        
        // Getters and setters
        public boolean isPushNotifications() { return pushNotifications; }
        public void setPushNotifications(boolean pushNotifications) { this.pushNotifications = pushNotifications; }
        
        public boolean isEmailNotifications() { return emailNotifications; }
        public void setEmailNotifications(boolean emailNotifications) { this.emailNotifications = emailNotifications; }
        
        public boolean isSmsAlerts() { return smsAlerts; }
        public void setSmsAlerts(boolean smsAlerts) { this.smsAlerts = smsAlerts; }
        
        public boolean isDeadlineReminders() { return deadlineReminders; }
        public void setDeadlineReminders(boolean deadlineReminders) { this.deadlineReminders = deadlineReminders; }
        
        public boolean isTaskAssignments() { return taskAssignments; }
        public void setTaskAssignments(boolean taskAssignments) { this.taskAssignments = taskAssignments; }
        
        public boolean isWorkshopUpdates() { return workshopUpdates; }
        public void setWorkshopUpdates(boolean workshopUpdates) { this.workshopUpdates = workshopUpdates; }
        
        public boolean isEmergencyAlerts() { return emergencyAlerts; }
        public void setEmergencyAlerts(boolean emergencyAlerts) { this.emergencyAlerts = emergencyAlerts; }
        
        public UrgencyLevel getMinimumUrgency() { return minimumUrgency; }
        public void setMinimumUrgency(UrgencyLevel minimumUrgency) { this.minimumUrgency = minimumUrgency; }
        
        public java.time.LocalTime getQuietHoursStart() { return quietHoursStart; }
        public void setQuietHoursStart(java.time.LocalTime quietHoursStart) { this.quietHoursStart = quietHoursStart; }
        
        public java.time.LocalTime getQuietHoursEnd() { return quietHoursEnd; }
        public void setQuietHoursEnd(java.time.LocalTime quietHoursEnd) { this.quietHoursEnd = quietHoursEnd; }
    }
}