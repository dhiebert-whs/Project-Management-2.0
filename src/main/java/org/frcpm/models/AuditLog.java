// src/main/java/org/frcpm/models/AuditLog.java

package org.frcpm.models;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Audit log entity for tracking user actions and COPPA compliance.
 * 
 * Essential for maintaining compliance with children's privacy laws
 * by tracking all access to student data and system operations.
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
    
    // User who performed the action
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    // Subject user (for COPPA tracking when accessing student data)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_user_id")
    private User subjectUser;
    
    @Column(nullable = false, length = 100)
    private String action;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "ip_address", length = 45) // IPv6 compatible
    private String ipAddress;
    
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
    
    @Column(name = "session_id", length = 100)
    private String sessionId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditLevel level;
    
    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    // Additional metadata for compliance
    @Column(name = "resource_type", length = 50)
    private String resourceType; // e.g., "TeamMember", "Task", "Project"
    
    @Column(name = "resource_id")
    private Long resourceId;
    
    @Column(name = "coppa_relevant", nullable = false)
    private boolean coppaRelevant = false;
    
    // Constructors
    
    public AuditLog() {}
    
    public AuditLog(User user, String action, String description, AuditLevel level) {
        this.user = user;
        this.action = action;
        this.description = description;
        this.level = level;
    }
    
    // Getters and setters
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
        // Auto-mark as COPPA relevant if subject is under 13
        if (subjectUser != null && subjectUser.requiresCOPPACompliance()) {
            this.coppaRelevant = true;
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

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
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

    public boolean isCoppaRelevant() {
        return coppaRelevant;
    }

    public void setCoppaRelevant(boolean coppaRelevant) {
        this.coppaRelevant = coppaRelevant;
    }
}