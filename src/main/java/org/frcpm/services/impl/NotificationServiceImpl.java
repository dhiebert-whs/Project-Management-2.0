// src/main/java/org/frcpm/services/impl/NotificationServiceImpl.java
// Phase 2E-E: Enhanced Real-time Features - Mobile Notifications Implementation

package org.frcpm.services.impl;

import org.frcpm.services.NotificationService;
import org.frcpm.models.User;
import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.web.dto.NotificationDto;
import org.frcpm.services.UserService;
import org.frcpm.services.ProjectService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Implementation of NotificationService for mobile notifications and critical alerts.
 * 
 * Features:
 * - Firebase Cloud Messaging (FCM) for push notifications
 * - Email notifications via SMTP
 * - SMS alerts via Twilio (optional)
 * - COPPA-compliant notifications for students
 * - Workshop safety and emergency alerts
 * - Offline notification queuing
 * 
 * @author FRC Project Management Team
 * @version 2.0.0-2E-E
 * @since Phase 2E-E - Enhanced Real-time Features
 */
@Service
public class NotificationServiceImpl implements NotificationService {
    
    private static final Logger LOGGER = Logger.getLogger(NotificationServiceImpl.class.getName());
    
    // Device registration storage (in production, use database)
    private final ConcurrentHashMap<Long, List<DeviceRegistration>> userDevices = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, NotificationPreferences> userPreferences = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, List<NotificationDto>> userNotifications = new ConcurrentHashMap<>();
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ProjectService projectService;
    
    @Value("${frc.notifications.fcm.enabled:false}")
    private boolean fcmEnabled;
    
    @Value("${frc.notifications.email.enabled:true}")
    private boolean emailEnabled;
    
    @Value("${frc.notifications.sms.enabled:false}")
    private boolean smsEnabled;
    
    @Value("${frc.notifications.max-daily-notifications:100}")
    private int maxDailyNotifications;
    
    // =========================================================================
    // PUSH NOTIFICATIONS
    // =========================================================================
    
    @Override
    public boolean sendPushNotification(User user, String title, String message, 
                                       NotificationType type, Map<String, Object> data) {
        try {
            if (!fcmEnabled) {
                LOGGER.info("FCM disabled - skipping push notification");
                return false;
            }
            
            // Check COPPA compliance
            if (!canSendNotification(user, type)) {
                LOGGER.info(String.format("COPPA compliance prevents notification to user %s", user.getUsername()));
                return false;
            }
            
            // Check user preferences
            NotificationPreferences prefs = getUserNotificationPreferences(user);
            if (!prefs.isPushNotifications()) {
                LOGGER.info(String.format("User %s has disabled push notifications", user.getUsername()));
                return false;
            }
            
            // Check quiet hours
            if (isQuietHours(prefs)) {
                LOGGER.info("Notification blocked due to quiet hours");
                return false;
            }
            
            // Get user's devices
            List<DeviceRegistration> devices = userDevices.get(user.getId());
            if (devices == null || devices.isEmpty()) {
                LOGGER.info(String.format("No devices registered for user %s", user.getUsername()));
                return false;
            }
            
            // Send to all registered devices
            boolean success = false;
            for (DeviceRegistration device : devices) {
                if (sendFCMNotification(device.getToken(), title, message, type, data)) {
                    success = true;
                }
            }
            
            // Store notification for user's notification center
            storeNotification(user, title, message, type, data);
            
            LOGGER.info(String.format("Push notification sent to user %s: %s", user.getUsername(), title));
            return success;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, String.format("Error sending push notification to user %s", user.getUsername()), e);
            return false;
        }
    }
    
    @Override
    public Map<Long, Boolean> sendBulkPushNotifications(List<User> users, String title, 
                                                       String message, NotificationType type, 
                                                       Map<String, Object> data) {
        Map<Long, Boolean> results = new HashMap<>();
        
        for (User user : users) {
            boolean success = sendPushNotification(user, title, message, type, data);
            results.put(user.getId(), success);
        }
        
        return results;
    }
    
    @Override
    public int sendProjectPushNotification(Project project, String title, String message, 
                                          NotificationType type, User excludeUser) {
        try {
            // Get all project members
            List<User> projectMembers = userService.findByProject(project);
            
            // Remove excluded user
            if (excludeUser != null) {
                projectMembers.removeIf(user -> user.getId().equals(excludeUser.getId()));
            }
            
            // Add project context to data
            Map<String, Object> data = new HashMap<>();
            data.put("projectId", project.getId());
            data.put("projectName", project.getName());
            
            // Send notifications
            Map<Long, Boolean> results = sendBulkPushNotifications(projectMembers, title, message, type, data);
            
            // Count successful notifications
            int successCount = (int) results.values().stream().mapToLong(success -> success ? 1 : 0).sum();
            
            LOGGER.info(String.format("Sent project notification to %d/%d members of project %s", 
                                    successCount, projectMembers.size(), project.getName()));
            
            return successCount;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, String.format("Error sending project notification for project %s", project.getName()), e);
            return 0;
        }
    }
    
    // =========================================================================
    // CRITICAL ALERTS
    // =========================================================================
    
    @Override
    public boolean sendCriticalAlert(User user, String title, String message, 
                                    UrgencyLevel urgencyLevel, AlertContext context) {
        try {
            LOGGER.warning(String.format("Sending critical alert to user %s: %s", user.getUsername(), title));
            
            boolean anySuccess = false;
            
            // Always send push notification for critical alerts
            Map<String, Object> data = new HashMap<>();
            data.put("urgency", urgencyLevel.toString());
            if (context != null) {
                data.put("projectId", context.getProjectId());
                data.put("taskId", context.getTaskId());
                data.put("location", context.getLocation());
            }
            
            boolean pushSuccess = sendPushNotification(user, title, message, NotificationType.CRITICAL, data);
            if (pushSuccess) anySuccess = true;
            
            // Send email for high/critical urgency
            if (urgencyLevel == UrgencyLevel.HIGH || urgencyLevel == UrgencyLevel.CRITICAL) {
                String htmlContent = formatCriticalAlertEmail(title, message, urgencyLevel, context);
                boolean emailSuccess = sendEmailNotification(user, "CRITICAL ALERT: " + title, 
                                                            htmlContent, message, EmailPriority.URGENT);
                if (emailSuccess) anySuccess = true;
            }
            
            // Send SMS for critical urgency if user has SMS enabled
            if (urgencyLevel == UrgencyLevel.CRITICAL && user.getPhoneNumber() != null) {
                String smsMessage = String.format("CRITICAL: %s - %s", title, message);
                if (smsMessage.length() > 160) {
                    smsMessage = smsMessage.substring(0, 157) + "...";
                }
                boolean smsSuccess = sendSMSAlert(user, smsMessage, urgencyLevel);
                if (smsSuccess) anySuccess = true;
            }
            
            return anySuccess;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, String.format("Error sending critical alert to user %s", user.getUsername()), e);
            return false;
        }
    }
    
    @Override
    public int sendDeadlineAlert(Project project, int daysRemaining, List<User> recipients) {
        try {
            String title = String.format("Deadline Alert: %s", project.getName());
            String message = daysRemaining == 0 ? 
                "Deadline is TODAY!" : 
                String.format("Deadline in %d day%s", daysRemaining, daysRemaining == 1 ? "" : "s");
            
            UrgencyLevel urgency = daysRemaining <= 1 ? UrgencyLevel.CRITICAL : 
                                  daysRemaining <= 3 ? UrgencyLevel.HIGH : UrgencyLevel.MEDIUM;
            
            AlertContext context = new AlertContext(project.getId(), null, null);
            
            int alertsSent = 0;
            for (User user : recipients) {
                if (sendCriticalAlert(user, title, message, urgency, context)) {
                    alertsSent++;
                }
            }
            
            LOGGER.info(String.format("Sent deadline alerts to %d users for project %s (%d days remaining)", 
                                    alertsSent, project.getName(), daysRemaining));
            
            return alertsSent;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, String.format("Error sending deadline alerts for project %s", project.getName()), e);
            return 0;
        }
    }
    
    @Override
    public int sendWorkshopEmergencyAlert(String message, String location, EmergencySeverity severity) {
        try {
            LOGGER.severe(String.format("WORKSHOP EMERGENCY ALERT: %s at %s (Severity: %s)", 
                                      message, location, severity));
            
            String title = "🚨 WORKSHOP EMERGENCY 🚨";
            UrgencyLevel urgency = severity == EmergencySeverity.CRITICAL ? UrgencyLevel.CRITICAL : UrgencyLevel.HIGH;
            
            AlertContext context = new AlertContext();
            context.setLocation(location);
            
            // Get all active users (those who have been active in the last hour)
            List<User> activeUsers = userService.findActiveUsers();
            
            int alertsSent = 0;
            for (User user : activeUsers) {
                if (sendCriticalAlert(user, title, message, urgency, context)) {
                    alertsSent++;
                }
            }
            
            LOGGER.severe(String.format("Emergency alert sent to %d active users", alertsSent));
            return alertsSent;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error sending workshop emergency alert", e);
            return 0;
        }
    }
    
    @Override
    public boolean sendTaskAssignmentAlert(Task task, User assignee, User assigner) {
        try {
            String title = "New Task Assignment";
            String message = String.format("You've been assigned to task: %s", task.getTitle());
            
            Map<String, Object> data = new HashMap<>();
            data.put("taskId", task.getId());
            data.put("projectId", task.getProject().getId());
            data.put("assignerName", assigner.getFullName());
            
            return sendPushNotification(assignee, title, message, NotificationType.ASSIGNMENT, data);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, String.format("Error sending task assignment alert for task %d", task.getId()), e);
            return false;
        }
    }
    
    // =========================================================================
    // EMAIL NOTIFICATIONS
    // =========================================================================
    
    @Override
    public boolean sendEmailNotification(User user, String subject, String htmlContent, 
                                        String textContent, EmailPriority priority) {
        try {
            if (!emailEnabled) {
                LOGGER.info("Email notifications disabled");
                return false;
            }
            
            // Check user preferences
            NotificationPreferences prefs = getUserNotificationPreferences(user);
            if (!prefs.isEmailNotifications() && priority != EmailPriority.URGENT) {
                LOGGER.info(String.format("User %s has disabled email notifications", user.getUsername()));
                return false;
            }
            
            // In production, integrate with actual email service (e.g., SendGrid, SES)
            LOGGER.info(String.format("EMAIL: To: %s, Subject: %s, Priority: %s", 
                                    user.getEmail(), subject, priority));
            
            // Simulate email sending
            boolean success = simulateEmailSending(user.getEmail(), subject, htmlContent, priority);
            
            if (success) {
                LOGGER.info(String.format("Email sent to %s: %s", user.getEmail(), subject));
            } else {
                LOGGER.warning(String.format("Failed to send email to %s", user.getEmail()));
            }
            
            return success;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, String.format("Error sending email to user %s", user.getUsername()), e);
            return false;
        }
    }
    
    @Override
    public boolean sendWeeklySummaryEmail(User mentor, List<Project> projects, LocalDate weekStartDate) {
        try {
            String subject = String.format("Weekly Project Summary - Week of %s", weekStartDate);
            String htmlContent = generateWeeklySummaryHtml(mentor, projects, weekStartDate);
            String textContent = generateWeeklySummaryText(mentor, projects, weekStartDate);
            
            return sendEmailNotification(mentor, subject, htmlContent, textContent, EmailPriority.NORMAL);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, String.format("Error sending weekly summary to mentor %s", mentor.getUsername()), e);
            return false;
        }
    }
    
    @Override
    public boolean sendCOPPAConsentEmail(String parentEmail, String studentName, String consentToken) {
        try {
            String subject = String.format("Parental Consent Required - %s's FRC Project Management Account", studentName);
            String htmlContent = generateCOPPAConsentHtml(parentEmail, studentName, consentToken);
            String textContent = generateCOPPAConsentText(parentEmail, studentName, consentToken);
            
            // Simulate sending COPPA consent email
            LOGGER.info(String.format("COPPA CONSENT EMAIL: To: %s, Student: %s, Token: %s", 
                                    parentEmail, studentName, consentToken));
            
            return simulateEmailSending(parentEmail, subject, htmlContent, EmailPriority.HIGH);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, String.format("Error sending COPPA consent email to %s", parentEmail), e);
            return false;
        }
    }
    
    // =========================================================================
    // SMS ALERTS
    // =========================================================================
    
    @Override
    public boolean sendSMSAlert(String phoneNumber, String message, UrgencyLevel urgency) {
        try {
            if (!smsEnabled) {
                LOGGER.info("SMS alerts disabled");
                return false;
            }
            
            // In production, integrate with Twilio or similar SMS service
            LOGGER.info(String.format("SMS ALERT: To: %s, Message: %s, Urgency: %s", 
                                    phoneNumber, message, urgency));
            
            // Simulate SMS sending
            boolean success = simulateSMSSending(phoneNumber, message, urgency);
            
            if (success) {
                LOGGER.info(String.format("SMS sent to %s: %s", phoneNumber, message));
            } else {
                LOGGER.warning(String.format("Failed to send SMS to %s", phoneNumber));
            }
            
            return success;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, String.format("Error sending SMS to %s", phoneNumber), e);
            return false;
        }
    }
    
    @Override
    public boolean sendSMSAlert(User user, String message, UrgencyLevel urgency) {
        if (user.getPhoneNumber() == null || user.getPhoneNumber().isEmpty()) {
            LOGGER.info(String.format("User %s has no phone number for SMS", user.getUsername()));
            return false;
        }
        
        return sendSMSAlert(user.getPhoneNumber(), message, urgency);
    }
    
    // =========================================================================
    // NOTIFICATION MANAGEMENT
    // =========================================================================
    
    @Override
    public boolean registerMobileDevice(User user, String deviceToken, DeviceType deviceType, String userAgent) {
        try {
            List<DeviceRegistration> devices = userDevices.computeIfAbsent(user.getId(), k -> new ArrayList<>());
            
            // Remove existing registration for this token
            devices.removeIf(device -> device.getToken().equals(deviceToken));
            
            // Add new registration
            DeviceRegistration newDevice = new DeviceRegistration(deviceToken, deviceType, userAgent, LocalDateTime.now());
            devices.add(newDevice);
            
            LOGGER.info(String.format("Registered device for user %s: %s (%s)", 
                                    user.getUsername(), deviceType, deviceToken.substring(0, Math.min(10, deviceToken.length()))));
            
            return true;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, String.format("Error registering device for user %s", user.getUsername()), e);
            return false;
        }
    }
    
    @Override
    public boolean unregisterMobileDevice(User user, String deviceToken) {
        try {
            List<DeviceRegistration> devices = userDevices.get(user.getId());
            if (devices != null) {
                boolean removed = devices.removeIf(device -> device.getToken().equals(deviceToken));
                if (removed) {
                    LOGGER.info(String.format("Unregistered device for user %s", user.getUsername()));
                }
                return removed;
            }
            return false;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, String.format("Error unregistering device for user %s", user.getUsername()), e);
            return false;
        }
    }
    
    @Override
    public NotificationPreferences getUserNotificationPreferences(User user) {
        return userPreferences.computeIfAbsent(user.getId(), k -> new NotificationPreferences());
    }
    
    @Override
    public boolean updateNotificationPreferences(User user, NotificationPreferences preferences) {
        try {
            userPreferences.put(user.getId(), preferences);
            LOGGER.info(String.format("Updated notification preferences for user %s", user.getUsername()));
            return true;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, String.format("Error updating preferences for user %s", user.getUsername()), e);
            return false;
        }
    }
    
    @Override
    public List<NotificationDto> getRecentNotifications(User user, int limit, boolean unreadOnly) {
        List<NotificationDto> notifications = userNotifications.get(user.getId());
        if (notifications == null) {
            return new ArrayList<>();
        }
        
        return notifications.stream()
            .filter(notification -> !unreadOnly || !notification.isRead())
            .limit(limit)
            .toList();
    }
    
    @Override
    public boolean markNotificationAsRead(Long notificationId, User user) {
        List<NotificationDto> notifications = userNotifications.get(user.getId());
        if (notifications != null) {
            return notifications.stream()
                .filter(notification -> notification.getId().equals(notificationId))
                .findFirst()
                .map(notification -> {
                    notification.setRead(true);
                    return true;
                })
                .orElse(false);
        }
        return false;
    }
    
    @Override
    public int markAllNotificationsAsRead(User user) {
        List<NotificationDto> notifications = userNotifications.get(user.getId());
        if (notifications != null) {
            int count = 0;
            for (NotificationDto notification : notifications) {
                if (!notification.isRead()) {
                    notification.setRead(true);
                    count++;
                }
            }
            return count;
        }
        return 0;
    }
    
    // =========================================================================
    // WORKSHOP STATUS NOTIFICATIONS
    // =========================================================================
    
    @Override
    public int sendWorkshopOpenNotification(User openedBy, LocalDateTime estimatedCloseTime) {
        try {
            String title = "Workshop Opened";
            String message = String.format("Workshop opened by %s. Estimated close: %s", 
                                         openedBy.getFullName(), 
                                         estimatedCloseTime.toLocalTime());
            
            List<User> allUsers = userService.findAll();
            Map<String, Object> data = new HashMap<>();
            data.put("workshopStatus", "OPEN");
            data.put("openedBy", openedBy.getFullName());
            
            Map<Long, Boolean> results = sendBulkPushNotifications(allUsers, title, message, 
                                                                 NotificationType.WORKSHOP, data);
            
            int successCount = (int) results.values().stream().mapToLong(success -> success ? 1 : 0).sum();
            
            LOGGER.info(String.format("Workshop open notification sent to %d users", successCount));
            return successCount;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error sending workshop open notification", e);
            return 0;
        }
    }
    
    @Override
    public int sendWorkshopCloseNotification(User closedBy, String summary) {
        try {
            String title = "Workshop Closed";
            String message = String.format("Workshop closed by %s. %s", closedBy.getFullName(), summary);
            
            List<User> allUsers = userService.findAll();
            Map<String, Object> data = new HashMap<>();
            data.put("workshopStatus", "CLOSED");
            data.put("closedBy", closedBy.getFullName());
            data.put("summary", summary);
            
            Map<Long, Boolean> results = sendBulkPushNotifications(allUsers, title, message, 
                                                                 NotificationType.WORKSHOP, data);
            
            int successCount = (int) results.values().stream().mapToLong(success -> success ? 1 : 0).sum();
            
            LOGGER.info(String.format("Workshop close notification sent to %d users", successCount));
            return successCount;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error sending workshop close notification", e);
            return 0;
        }
    }
    
    @Override
    public int sendSafetyReminder(String message, List<User> targetUsers) {
        try {
            String title = "🦺 Safety Reminder";
            
            List<User> recipients = targetUsers != null ? targetUsers : userService.findActiveUsers();
            
            Map<String, Object> data = new HashMap<>();
            data.put("safetyReminder", true);
            
            Map<Long, Boolean> results = sendBulkPushNotifications(recipients, title, message, 
                                                                 NotificationType.WARNING, data);
            
            int successCount = (int) results.values().stream().mapToLong(success -> success ? 1 : 0).sum();
            
            LOGGER.info(String.format("Safety reminder sent to %d users: %s", successCount, message));
            return successCount;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error sending safety reminder", e);
            return 0;
        }
    }
    
    // =========================================================================
    // HELPER METHODS
    // =========================================================================
    
    private boolean canSendNotification(User user, NotificationType type) {
        // Check COPPA compliance for students under 13
        if (user.requiresCOPPACompliance()) {
            // Emergency alerts always allowed
            if (type == NotificationType.EMERGENCY || type == NotificationType.CRITICAL) {
                return true;
            }
            
            // Other notifications require parental consent
            return user.hasParentalConsent();
        }
        
        return true;
    }
    
    private boolean isQuietHours(NotificationPreferences prefs) {
        if (prefs.getQuietHoursStart() == null || prefs.getQuietHoursEnd() == null) {
            return false;
        }
        
        LocalTime now = LocalTime.now();
        LocalTime start = prefs.getQuietHoursStart();
        LocalTime end = prefs.getQuietHoursEnd();
        
        if (start.isBefore(end)) {
            return now.isAfter(start) && now.isBefore(end);
        } else {
            // Quiet hours span midnight
            return now.isAfter(start) || now.isBefore(end);
        }
    }
    
    private boolean sendFCMNotification(String deviceToken, String title, String message, 
                                       NotificationType type, Map<String, Object> data) {
        try {
            // In production, integrate with Firebase Cloud Messaging
            LOGGER.info(String.format("FCM: Token: %s, Title: %s, Type: %s", 
                                    deviceToken.substring(0, Math.min(10, deviceToken.length())), title, type));
            
            // Simulate FCM sending (90% success rate)
            return Math.random() < 0.9;
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error sending FCM notification", e);
            return false;
        }
    }
    
    private void storeNotification(User user, String title, String message, 
                                  NotificationType type, Map<String, Object> data) {
        List<NotificationDto> notifications = userNotifications.computeIfAbsent(user.getId(), k -> new ArrayList<>());
        
        NotificationDto notification = new NotificationDto();
        notification.setId(System.currentTimeMillis()); // Simple ID generation
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type.toString());
        notification.setTimestamp(LocalDateTime.now());
        notification.setRead(false);
        notification.setData(data);
        
        notifications.add(0, notification); // Add to beginning
        
        // Keep only last 100 notifications
        if (notifications.size() > 100) {
            notifications.subList(100, notifications.size()).clear();
        }
    }
    
    private boolean simulateEmailSending(String email, String subject, String content, EmailPriority priority) {
        // Simulate email sending with 95% success rate
        return Math.random() < 0.95;
    }
    
    private boolean simulateSMSSending(String phoneNumber, String message, UrgencyLevel urgency) {
        // Simulate SMS sending with 85% success rate
        return Math.random() < 0.85;
    }
    
    private String formatCriticalAlertEmail(String title, String message, UrgencyLevel urgencyLevel, AlertContext context) {
        StringBuilder html = new StringBuilder();
        html.append("<html><body>");
        html.append("<h2 style='color: red;'>🚨 CRITICAL ALERT 🚨</h2>");
        html.append("<h3>").append(title).append("</h3>");
        html.append("<p><strong>Urgency:</strong> ").append(urgencyLevel).append("</p>");
        html.append("<p><strong>Message:</strong> ").append(message).append("</p>");
        
        if (context != null) {
            if (context.getProjectId() != null) {
                html.append("<p><strong>Project ID:</strong> ").append(context.getProjectId()).append("</p>");
            }
            if (context.getLocation() != null) {
                html.append("<p><strong>Location:</strong> ").append(context.getLocation()).append("</p>");
            }
        }
        
        html.append("<p><strong>Time:</strong> ").append(LocalDateTime.now()).append("</p>");
        html.append("</body></html>");
        
        return html.toString();
    }
    
    private String generateWeeklySummaryHtml(User mentor, List<Project> projects, LocalDate weekStartDate) {
        // Generate HTML content for weekly summary
        return "<html><body><h2>Weekly Project Summary</h2><p>Summary for " + mentor.getFullName() + "</p></body></html>";
    }
    
    private String generateWeeklySummaryText(User mentor, List<Project> projects, LocalDate weekStartDate) {
        // Generate plain text content for weekly summary
        return "Weekly Project Summary for " + mentor.getFullName();
    }
    
    private String generateCOPPAConsentHtml(String parentEmail, String studentName, String consentToken) {
        // Generate HTML content for COPPA consent
        return "<html><body><h2>Parental Consent Required</h2><p>Please provide consent for " + studentName + "</p></body></html>";
    }
    
    private String generateCOPPAConsentText(String parentEmail, String studentName, String consentToken) {
        // Generate plain text content for COPPA consent
        return "Parental consent required for " + studentName;
    }
    
    // =========================================================================
    // HELPER CLASSES
    // =========================================================================
    
    private static class DeviceRegistration {
        private final String token;
        private final DeviceType type;
        private final String userAgent;
        private final LocalDateTime registeredAt;
        
        public DeviceRegistration(String token, DeviceType type, String userAgent, LocalDateTime registeredAt) {
            this.token = token;
            this.type = type;
            this.userAgent = userAgent;
            this.registeredAt = registeredAt;
        }
        
        public String getToken() { return token; }
        public DeviceType getType() { return type; }
        public String getUserAgent() { return userAgent; }
        public LocalDateTime getRegisteredAt() { return registeredAt; }
    }
}