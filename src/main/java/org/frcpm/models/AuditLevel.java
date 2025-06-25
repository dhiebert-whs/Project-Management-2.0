// src/main/java/org/frcpm/models/AuditLevel.java

package org.frcpm.models;

/**
 * Audit log severity levels for categorizing security events.
 */
public enum AuditLevel {
    INFO("Information", "Normal system operation"),
    WARNING("Warning", "Unusual but not necessarily harmful activity"),
    COPPA_COMPLIANCE("COPPA Compliance", "Access to data of users under 13"),
    SECURITY_ALERT("Security Alert", "Potential security concern"),
    VIOLATION("Violation", "Policy or compliance violation");
    
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
}