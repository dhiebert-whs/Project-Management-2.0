package org.frcpm.models;

/**
 * Enumeration of meeting statuses for Phase 3A Meeting Management System.
 * 
 * Tracks the lifecycle of meetings from initial scheduling through completion,
 * enabling better meeting workflow management and reporting.
 * 
 * @author FRC Project Management Team
 * @version 3.0.0-3A
 * @since Phase 3A - Meeting Management System
 */
public enum MeetingStatus {
    
    /**
     * Meeting has been scheduled but not yet started.
     */
    SCHEDULED("Scheduled", "Meeting has been scheduled", "📅", "primary"),
    
    /**
     * Meeting is confirmed and ready to proceed.
     */
    CONFIRMED("Confirmed", "Meeting is confirmed and ready", "✅", "success"),
    
    /**
     * Meeting is currently in progress.
     */
    IN_PROGRESS("In Progress", "Meeting is currently underway", "▶️", "warning"),
    
    /**
     * Meeting has been completed successfully.
     */
    COMPLETED("Completed", "Meeting has been completed", "✅", "success"),
    
    /**
     * Meeting has been cancelled before taking place.
     */
    CANCELLED("Cancelled", "Meeting has been cancelled", "❌", "danger"),
    
    /**
     * Meeting was scheduled but participants did not attend.
     */
    NO_SHOW("No Show", "Meeting was scheduled but no one attended", "👻", "secondary"),
    
    /**
     * Meeting was postponed to a later date.
     */
    POSTPONED("Postponed", "Meeting has been postponed", "⏳", "info"),
    
    /**
     * Meeting is on hold pending further decisions.
     */
    ON_HOLD("On Hold", "Meeting is on hold", "⏸️", "secondary"),
    
    /**
     * Meeting requires rescheduling due to conflicts.
     */
    NEEDS_RESCHEDULING("Needs Rescheduling", "Meeting needs to be rescheduled", "🔄", "warning");
    
    private final String displayName;
    private final String description;
    private final String emoji;
    private final String bootstrapClass;
    
    MeetingStatus(String displayName, String description, String emoji, String bootstrapClass) {
        this.displayName = displayName;
        this.description = description;
        this.emoji = emoji;
        this.bootstrapClass = bootstrapClass;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getEmoji() {
        return emoji;
    }
    
    public String getBootstrapClass() {
        return bootstrapClass;
    }
    
    /**
     * Get display name with emoji for UI components.
     */
    public String getDisplayNameWithEmoji() {
        return emoji + " " + displayName;
    }
    
    /**
     * Check if this status indicates the meeting is still active/upcoming.
     */
    public boolean isActive() {
        return this == SCHEDULED || 
               this == CONFIRMED || 
               this == IN_PROGRESS || 
               this == POSTPONED || 
               this == ON_HOLD ||
               this == NEEDS_RESCHEDULING;
    }
    
    /**
     * Check if this status indicates the meeting is completed.
     */
    public boolean isCompleted() {
        return this == COMPLETED;
    }
    
    /**
     * Check if this status indicates the meeting was cancelled or didn't happen.
     */
    public boolean isCancelled() {
        return this == CANCELLED || this == NO_SHOW;
    }
    
    /**
     * Check if this status allows editing the meeting details.
     */
    public boolean allowsEditing() {
        return this == SCHEDULED || 
               this == CONFIRMED || 
               this == POSTPONED || 
               this == ON_HOLD ||
               this == NEEDS_RESCHEDULING;
    }
    
    /**
     * Check if this status allows attendance recording.
     */
    public boolean allowsAttendanceRecording() {
        return this == IN_PROGRESS || this == COMPLETED;
    }
    
    /**
     * Get the next logical status transition options.
     */
    public MeetingStatus[] getNextStatusOptions() {
        switch (this) {
            case SCHEDULED:
                return new MeetingStatus[]{CONFIRMED, IN_PROGRESS, POSTPONED, ON_HOLD, CANCELLED};
            case CONFIRMED:
                return new MeetingStatus[]{IN_PROGRESS, POSTPONED, CANCELLED};
            case IN_PROGRESS:
                return new MeetingStatus[]{COMPLETED, POSTPONED, ON_HOLD};
            case POSTPONED:
                return new MeetingStatus[]{SCHEDULED, CONFIRMED, CANCELLED};
            case ON_HOLD:
                return new MeetingStatus[]{SCHEDULED, CONFIRMED, CANCELLED};
            case NEEDS_RESCHEDULING:
                return new MeetingStatus[]{SCHEDULED, CONFIRMED, CANCELLED};
            default:
                return new MeetingStatus[]{};
        }
    }
}