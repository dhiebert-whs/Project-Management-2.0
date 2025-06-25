// src/main/java/org/frcpm/services/impl/AuditServiceImpl.java

package org.frcpm.services.impl;

import org.frcpm.models.AuditLevel;
import org.frcpm.models.AuditLog;
import org.frcpm.models.User;
import org.frcpm.repositories.spring.AuditLogRepository;
import org.frcpm.services.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

/**
 * Implementation of audit logging service for security and compliance.
 */
@Service
@Transactional
public class AuditServiceImpl implements AuditService {
    
    private static final Logger LOGGER = Logger.getLogger(AuditServiceImpl.class.getName());
    
    private final AuditLogRepository auditLogRepository;
    
    @Autowired
    public AuditServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }
    
    // =========================================================================
    // BASIC LOGGING METHODS
    // =========================================================================
    
    @Override
    public void logAction(User user, String action, String description) {
        logAction(user, action, description, AuditLevel.INFO);
    }
    
    @Override
    public void logAction(User user, String action, String description, AuditLevel level) {
        try {
            AuditLog log = new AuditLog(user, action, description, level);
            enrichLogWithRequestData(log);
            auditLogRepository.save(log);
        } catch (Exception e) {
            LOGGER.severe("Failed to save COPPA audit log: " + e.getMessage());
        }
    }
    
    @Override
    public void logDataAccess(User user, String resourceType, Long resourceId, String action) {
        logDataAccess(user, null, resourceType, resourceId, action);
    }
    
    @Override
    public void logDataAccess(User user, User subjectUser, String resourceType, Long resourceId, String action) {
        try {
            AuditLevel level = (subjectUser != null && subjectUser.requiresCOPPACompliance()) 
                ? AuditLevel.COPPA_COMPLIANCE : AuditLevel.INFO;
            
            String description = String.format("Accessed %s (ID: %d)", resourceType, resourceId);
            
            AuditLog log = new AuditLog(user, action, description, level);
            log.setSubjectUser(subjectUser);
            log.setResourceType(resourceType);
            log.setResourceId(resourceId);
            
            if (subjectUser != null && subjectUser.requiresCOPPACompliance()) {
                log.setCoppaRelevant(true);
            }
            
            enrichLogWithRequestData(log);
            auditLogRepository.save(log);
        } catch (Exception e) {
            LOGGER.severe("Failed to save data access audit log: " + e.getMessage());
        }
    }
    
    // =========================================================================
    // SECURITY EVENT LOGGING
    // =========================================================================
    
    @Override
    public void logSecurityEvent(User user, String action, String description) {
        logAction(user, action, description, AuditLevel.WARNING);
    }
    
    @Override
    public void logSecurityAlert(User user, String action, String description) {
        logAction(user, action, description, AuditLevel.SECURITY_ALERT);
    }
    
    @Override
    public void logLoginAttempt(String username, String ipAddress, boolean successful) {
        try {
            String action = successful ? "LOGIN_SUCCESS" : "LOGIN_FAILED";
            String description = String.format("Login attempt for username: %s from IP: %s", 
                username, ipAddress);
            
            AuditLevel level = successful ? AuditLevel.INFO : AuditLevel.WARNING;
            
            AuditLog log = new AuditLog(null, action, description, level);
            log.setIpAddress(ipAddress);
            enrichLogWithRequestData(log);
            
            auditLogRepository.save(log);
        } catch (Exception e) {
            LOGGER.severe("Failed to save login audit log: " + e.getMessage());
        }
    }
    
    @Override
    public void logLogout(User user) {
        logAction(user, "LOGOUT", "User logged out successfully");
    }
    
    // =========================================================================
    // QUERY METHODS
    // =========================================================================
    
    @Override
    public List<AuditLog> getRecentLogs(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return auditLogRepository.findRecentLogs(since);
    }
    
    @Override
    public List<AuditLog> getRecentCOPPALogs(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return auditLogRepository.findRecentCOPPALogs(since);
    }
    
    @Override
    public List<AuditLog> getSecurityEvents(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<AuditLevel> securityLevels = List.of(
            AuditLevel.WARNING, 
            AuditLevel.SECURITY_ALERT, 
            AuditLevel.VIOLATION
        );
        return auditLogRepository.findSecurityEventsSince(securityLevels, since);
    }
    
    @Override
    public List<AuditLog> getUserActivity(User user, int days) {
        // This would need a custom query - simplified implementation
        return auditLogRepository.findByUser(user);
    }
    
    // =========================================================================
    // STATISTICS
    // =========================================================================
    
    @Override
    public long getCOPPALogCount() {
        return auditLogRepository.countCOPPARelevantLogs();
    }
    
    @Override
    public long getSecurityEventCount(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return auditLogRepository.countByLevelSince(AuditLevel.SECURITY_ALERT, since) +
               auditLogRepository.countByLevelSince(AuditLevel.VIOLATION, since);
    }
    
    // =========================================================================
    // PRIVATE HELPER METHODS
    // =========================================================================
    
    /**
     * Enriches audit log with HTTP request data when available.
     */
    private void enrichLogWithRequestData(AuditLog log) {
        try {
            ServletRequestAttributes attrs = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                
                // Set IP address
                log.setIpAddress(getClientIpAddress(request));
                
                // Set user agent
                String userAgent = request.getHeader("User-Agent");
                if (userAgent != null && userAgent.length() > 500) {
                    userAgent = userAgent.substring(0, 500); // Truncate if too long
                }
                log.setUserAgent(userAgent);
                
                // Set session ID
                if (request.getSession(false) != null) {
                    log.setSessionId(request.getSession().getId());
                }
            }
        } catch (Exception e) {
            // Ignore - request context might not be available
        }
    }
    
    /**
     * Gets the real client IP address considering proxy headers.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    // =========================================================================
    // COPPA-SPECIFIC LOGGING - ✅ FIXED: Implement missing method
    // =========================================================================
    
    @Override
    public void logCOPPAAccess(User user, User subjectUser, String action, String description) {
        try {
            AuditLog log = new AuditLog(user, action, description, AuditLevel.COPPA_COMPLIANCE);
            log.setSubjectUser(subjectUser);
            log.setCoppaRelevant(true);
            
            enrichLogWithRequestData(log);
            auditLogRepository.save(log);
        } catch (Exception e) {
            LOGGER.severe("Failed to save COPPA audit log: " + e.getMessage());
        }
    }
}