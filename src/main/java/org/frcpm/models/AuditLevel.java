// src/main/java/org/frcpm/models/AuditLevel.java

package org.frcpm.models;

/**
 * ✅ UPDATED: Audit level enumeration for categorizing audit log entries.
 * 
 * Enhanced for Phase 2B with:
 * - COPPA compliance specific level
 * - Security violation tracking
 * - Comprehensive audit categorization
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 * @since Phase 2B - COPPA Compliance & Security
 */
public enum AuditLevel {
    
    /**
     * General informational events (login, logout, normal operations).
     */
    INFO("Informational", "Normal system operations and user activities"),
    
    /**
     * Warning events that may require attention (failed login attempts, unusual activity).
     */
    WARNING("Warning", "Events that may indicate potential security issues"),
    
    /**
     * COPPA compliance-related events (access to data of users under 13).
     */
    COPPA_COMPLIANCE("COPPA Compliance", "Events related to protection of users under 13"),
    
    /**
     * Security alerts requiring immediate attention (suspicious activity, policy violations).
     */
    SECURITY_ALERT("Security Alert", "Critical security events requiring immediate review"),
    
    /**
     * ✅ NEW: Security or policy violations requiring investigation.
     */
    VIOLATION("Violation", "Security or policy violations requiring investigation and response");
    
    private final String displayName;
    private final String description;
    
    AuditLevel(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Determines if this audit level requires immediate attention.
     */
    public boolean requiresImmediateAttention() {
        return this == SECURITY_ALERT || this == VIOLATION;
    }
    
    /**
     * Determines if this audit level is related to COPPA compliance.
     */
    public boolean isCOPPARelated() {
        return this == COPPA_COMPLIANCE;
    }
    
    /**
     * Determines if this audit level indicates a security concern.
     */
    public boolean isSecurityRelated() {
        return this == WARNING || this == SECURITY_ALERT || this == VIOLATION;
    }
    
    /**
     * Gets the priority level for alerting systems.
     * 
     * @return priority level (1=highest, 5=lowest)
     */
    public int getPriority() {
        switch (this) {
            case VIOLATION:
                return 1;
            case SECURITY_ALERT:
                return 2;
            case COPPA_COMPLIANCE:
                return 3;
            case WARNING:
                return 4;
            case INFO:
            default:
                return 5;
        }
    }
}