// src/main/java/org/frcpm/services/AuditService.java

package org.frcpm.services;

import org.frcpm.models.AuditLevel;
import org.frcpm.models.AuditLog;
import org.frcpm.models.User;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for audit logging and compliance tracking.
 */
public interface AuditService {
    
    // Basic logging
    void logAction(User user, String action, String description);
    void logAction(User user, String action, String description, AuditLevel level);
    
    // COPPA-specific logging
    void logCOPPAAccess(User user, User subjectUser, String action, String description);
    void logDataAccess(User user, String resourceType, Long resourceId, String action);
    void logDataAccess(User user, User subjectUser, String resourceType, Long resourceId, String action);
    
    // Security event logging
    void logSecurityEvent(User user, String action, String description);
    void logSecurityAlert(User user, String action, String description);
    void logLoginAttempt(String username, String ipAddress, boolean successful);
    void logLogout(User user);
    
    // Query methods
    List<AuditLog> getRecentLogs(int days);
    List<AuditLog> getRecentCOPPALogs(int days);
    List<AuditLog> getSecurityEvents(int days);
    List<AuditLog> getUserActivity(User user, int days);
    
    // Statistics
    long getCOPPALogCount();
    long getSecurityEventCount(int days);
}