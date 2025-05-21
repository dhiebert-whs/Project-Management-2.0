// src/main/java/org/frcpm/models/TaskStatus.java
package org.frcpm.models;

/**
 * Enum representing the status of a task.
 */
public enum TaskStatus {
    NOT_STARTED("Not Started"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed");
    
    private final String displayName;
    
    TaskStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}