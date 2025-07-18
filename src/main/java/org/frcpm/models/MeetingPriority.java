package org.frcpm.models;

/**
 * Enumeration of meeting priorities for Phase 3A Meeting Management System.
 * 
 * Helps teams prioritize meetings and allocate appropriate resources and 
 * preparation time based on the meeting's importance and urgency.
 * 
 * @author FRC Project Management Team
 * @version 3.0.0-3A
 * @since Phase 3A - Meeting Management System
 */
public enum MeetingPriority {
    
    /**
     * Low priority meeting - optional attendance, minimal preparation.
     */
    LOW("Low", "Optional attendance, minimal preparation required", "🔵", "info", 1),
    
    /**
     * Medium priority meeting - recommended attendance, standard preparation.
     */
    MEDIUM("Medium", "Recommended attendance, standard preparation", "🟡", "warning", 2),
    
    /**
     * High priority meeting - required attendance, thorough preparation.
     */
    HIGH("High", "Required attendance, thorough preparation needed", "🟠", "danger", 3),
    
    /**
     * Critical priority meeting - mandatory attendance, extensive preparation.
     */
    CRITICAL("Critical", "Mandatory attendance, extensive preparation required", "🔴", "danger", 4),
    
    /**
     * Emergency priority meeting - immediate attendance required.
     */
    EMERGENCY("Emergency", "Immediate attendance required, urgent matter", "🚨", "danger", 5);
    
    private final String displayName;
    private final String description;
    private final String emoji;
    private final String bootstrapClass;
    private final int level;
    
    MeetingPriority(String displayName, String description, String emoji, String bootstrapClass, int level) {
        this.displayName = displayName;
        this.description = description;
        this.emoji = emoji;
        this.bootstrapClass = bootstrapClass;
        this.level = level;
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
    
    public int getLevel() {
        return level;
    }
    
    /**
     * Get display name with emoji for UI components.
     */
    public String getDisplayNameWithEmoji() {
        return emoji + " " + displayName;
    }
    
    /**
     * Check if this priority requires mandatory attendance.
     */
    public boolean requiresMandatoryAttendance() {
        return this == HIGH || this == CRITICAL || this == EMERGENCY;
    }
    
    /**
     * Check if this priority requires extensive preparation.
     */
    public boolean requiresExtensivePreparation() {
        return this == CRITICAL || this == EMERGENCY;
    }
    
    /**
     * Get suggested preparation time in hours for this priority level.
     */
    public double getSuggestedPreparationHours() {
        switch (this) {
            case LOW:
                return 0.0; // No preparation needed
            case MEDIUM:
                return 0.5; // 30 minutes
            case HIGH:
                return 1.0; // 1 hour
            case CRITICAL:
                return 2.0; // 2 hours
            case EMERGENCY:
                return 0.0; // No time for preparation
            default:
                return 0.5;
        }
    }
    
    /**
     * Get suggested advance notice in hours for this priority level.
     */
    public int getSuggestedAdvanceNoticeHours() {
        switch (this) {
            case LOW:
                return 48; // 2 days
            case MEDIUM:
                return 24; // 1 day
            case HIGH:
                return 72; // 3 days
            case CRITICAL:
                return 168; // 1 week
            case EMERGENCY:
                return 0; // Immediate
            default:
                return 24;
        }
    }
    
    /**
     * Get reminder intervals in minutes for this priority level.
     */
    public int[] getReminderIntervals() {
        switch (this) {
            case LOW:
                return new int[]{60}; // 1 hour before
            case MEDIUM:
                return new int[]{60, 1440}; // 1 hour and 1 day before
            case HIGH:
                return new int[]{30, 60, 1440, 4320}; // 30min, 1hr, 1day, 3days before
            case CRITICAL:
                return new int[]{15, 30, 60, 1440, 4320, 10080}; // 15min, 30min, 1hr, 1day, 3days, 1week before
            case EMERGENCY:
                return new int[]{0}; // Immediate notification
            default:
                return new int[]{60};
        }
    }
    
    /**
     * Check if this priority level is higher than another.
     */
    public boolean isHigherThan(MeetingPriority other) {
        return this.level > other.level;
    }
    
    /**
     * Check if this priority level is lower than another.
     */
    public boolean isLowerThan(MeetingPriority other) {
        return this.level < other.level;
    }
}