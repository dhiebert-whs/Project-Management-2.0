package org.frcpm.models;

/**
 * Enum representing the status of a task.
 */
public enum TaskStatus {
    /**
     * Task has not been started yet (0% progress)
     */
    NOT_STARTED,
    
    /**
     * Task is in progress (1-99% progress)
     */
    IN_PROGRESS,
    
    /**
     * Task is completed (100% progress)
     */
    COMPLETED
}