// src/main/java/org/frcpm/models/AuditLog.java

package org.frcpm.models;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * ✅ UPDATED: Audit log entity for security and COPPA compliance tracking.
 * 
 * Enhanced for Phase 2B with:
 * - COPPA-specific logging fields
 * - Resource tracking for data access
 * - Enhanced audit levels for compliance
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 * @since Phase 2B - COPPA Compliance & Security
 */
@Entity
@Table(name = "audit_logs")
@EntityListeners(AuditingEntityListener.class)
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_user_id")
    private User subjectUser; // User being accessed (for COPPA tracking)

    @Column(name = "session_id")
    private String sessionId;   
    
    @Column(nullable = false)
    private String action;
    
    @Column(length = 1000)
    private String description;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditLevel level;
    
    @CreatedDate
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    // =========================================================================
    // ✅ NEW: COPPA COMPLIANCE FIELDS
    // =========================================================================
    
    /**
     * Indicates if this log entry is relevant to COPPA compliance.
     * Set to true for any access to data of users under 13.
     */
    @Column(name = "coppa_relevant", nullable = false)
    private boolean coppaRelevant = false;
    
    /**
     * Type of resource being accessed (User, Project, Task, etc.).
     */
    @Column(name = "resource_type")
    private String resourceType;
    
    /**
     * ID of the specific resource being accessed.
     */
    @Column(name = "resource_id")
    private Long resourceId;
    
    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================
    
    public AuditLog() {}
    
    public AuditLog(User user, String action, String description) {
        this.user = user;
        this.action = action;
        this.description = description;
        this.level = AuditLevel.INFO;
    }
    
    public AuditLog(User user, String action, String description, AuditLevel level) {
        this.user = user;
        this.action = action;
        this.description = description;
        this.level = level;
    }
    
    // =========================================================================
    // BUSINESS LOGIC METHODS
    // =========================================================================
    
    /**
     * Marks this audit log as COPPA-relevant.
     */
    public void markAsCOPPARelevant() {
        this.coppaRelevant = true;
        if (this.level == AuditLevel.INFO) {
            this.level = AuditLevel.COPPA_COMPLIANCE;
        }
    }
    
    /**
     * Sets resource information for data access tracking.
     */
    public void setResourceInfo(String resourceType, Long resourceId) {
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }
    
    /**
     * Checks if this log entry involves access to a user under 13.
     */
    public boolean involvesMinor() {
        return subjectUser != null && subjectUser.requiresCOPPACompliance();
    }
    
    // =========================================================================
    // GETTERS AND SETTERS
    // =========================================================================
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getSubjectUser() {
        return subjectUser;
    }

    public void setSubjectUser(User subjectUser) {
        this.subjectUser = subjectUser;
        // Automatically mark as COPPA relevant if subject is under 13
        if (subjectUser != null && subjectUser.requiresCOPPACompliance()) {
            markAsCOPPARelevant();
        }
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public AuditLevel getLevel() {
        return level;
    }

    public void setLevel(AuditLevel level) {
        this.level = level;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    // ✅ NEW: COPPA compliance field getters and setters
    
    public boolean isCoppaRelevant() {
        return coppaRelevant;
    }

    public void setCoppaRelevant(boolean coppaRelevant) {
        this.coppaRelevant = coppaRelevant;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }
    
    @Override
    public String toString() {
        return String.format("AuditLog{id=%d, action='%s', user='%s', level=%s, timestamp=%s, coppaRelevant=%s}", 
                           id, action, user != null ? user.getUsername() : "null", level, timestamp, coppaRelevant);
    }
}