package org.frcpm.models;

public enum SessionStatus {
    PLANNED("Planned"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    POSTPONED("Postponed");
    
    private final String displayName;
    
    SessionStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean isActive() {
        return this == IN_PROGRESS;
    }
    
    public boolean isComplete() {
        return this == COMPLETED || this == CANCELLED;
    }
    
    public boolean canBeStarted() {
        return this == PLANNED || this == POSTPONED;
    }
    
    public boolean canBeCancelled() {
        return this == PLANNED || this == POSTPONED || this == IN_PROGRESS;
    }
}