package org.frcpm.models;

/**
 * Enumeration of meeting types for Phase 3A Meeting Management System.
 * 
 * Each meeting type has specific characteristics and purposes that help
 * teams organize and prepare for different kinds of meetings effectively.
 * 
 * @author FRC Project Management Team
 * @version 3.0.0-3A
 * @since Phase 3A - Meeting Management System
 */
public enum MeetingType {
    
    /**
     * Regular team meeting for general coordination and updates.
     */
    TEAM_MEETING("Team Meeting", "Regular team coordination and updates", "👥"),
    
    /**
     * Design review meeting for technical discussions and decisions.
     */
    DESIGN_REVIEW("Design Review", "Technical design discussions and approvals", "🔧"),
    
    /**
     * Strategy session for planning and strategic decisions.
     */
    STRATEGY_SESSION("Strategy Session", "Strategic planning and decision making", "🎯"),
    
    /**
     * Build workshop meeting for hands-on construction work.
     */
    BUILD_WORKSHOP("Build Workshop", "Hands-on construction and assembly work", "🔨"),
    
    /**
     * Testing session for robot testing and debugging.
     */
    TESTING_SESSION("Testing Session", "Robot testing and performance evaluation", "🔍"),
    
    /**
     * Training meeting for skills development and learning.
     */
    TRAINING("Training", "Skills development and educational sessions", "📚"),
    
    /**
     * Competition preparation meeting.
     */
    COMPETITION_PREP("Competition Prep", "Competition preparation and strategy", "🏆"),
    
    /**
     * Safety meeting for discussing safety protocols and procedures.
     */
    SAFETY_MEETING("Safety Meeting", "Safety protocols and procedures", "🦺"),
    
    /**
     * Outreach meeting for community engagement activities.
     */
    OUTREACH("Outreach", "Community engagement and outreach activities", "🌟"),
    
    /**
     * Sponsor meeting for interactions with team sponsors.
     */
    SPONSOR_MEETING("Sponsor Meeting", "Meetings with team sponsors and partners", "🤝"),
    
    /**
     * Parent meeting for parent and family communications.
     */
    PARENT_MEETING("Parent Meeting", "Parent and family communications", "👪"),
    
    /**
     * Mentor meeting for mentor-specific discussions.
     */
    MENTOR_MEETING("Mentor Meeting", "Mentor coordination and planning", "👨‍🏫"),
    
    /**
     * Leadership meeting for team leadership discussions.
     */
    LEADERSHIP("Leadership", "Team leadership and management discussions", "👑"),
    
    /**
     * Post-mortem meeting for reviewing completed activities.
     */
    POST_MORTEM("Post-Mortem", "Review and analysis of completed activities", "📊"),
    
    /**
     * Emergency meeting for urgent matters.
     */
    EMERGENCY("Emergency", "Urgent matters requiring immediate attention", "🚨"),
    
    /**
     * Other type of meeting not covered by standard categories.
     */
    OTHER("Other", "Other meeting types not covered by standard categories", "📋");
    
    private final String displayName;
    private final String description;
    private final String emoji;
    
    MeetingType(String displayName, String description, String emoji) {
        this.displayName = displayName;
        this.description = description;
        this.emoji = emoji;
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
    
    /**
     * Get display name with emoji for UI components.
     */
    public String getDisplayNameWithEmoji() {
        return emoji + " " + displayName;
    }
    
    /**
     * Check if this meeting type typically requires preparation.
     */
    public boolean requiresPreparation() {
        return this == DESIGN_REVIEW || 
               this == STRATEGY_SESSION || 
               this == COMPETITION_PREP || 
               this == SPONSOR_MEETING || 
               this == PARENT_MEETING ||
               this == LEADERSHIP;
    }
    
    /**
     * Check if this meeting type is typically longer duration.
     */
    public boolean isLongDuration() {
        return this == BUILD_WORKSHOP || 
               this == TESTING_SESSION || 
               this == TRAINING || 
               this == COMPETITION_PREP;
    }
    
    /**
     * Get suggested duration in minutes for this meeting type.
     */
    public int getSuggestedDurationMinutes() {
        switch (this) {
            case BUILD_WORKSHOP:
            case TESTING_SESSION:
                return 180; // 3 hours
            case TRAINING:
            case COMPETITION_PREP:
                return 150; // 2.5 hours
            case DESIGN_REVIEW:
            case STRATEGY_SESSION:
                return 120; // 2 hours
            case TEAM_MEETING:
            case SAFETY_MEETING:
                return 90; // 1.5 hours
            case SPONSOR_MEETING:
            case PARENT_MEETING:
            case MENTOR_MEETING:
            case LEADERSHIP:
                return 60; // 1 hour
            case POST_MORTEM:
                return 45; // 45 minutes
            case EMERGENCY:
                return 30; // 30 minutes
            default:
                return 60; // 1 hour default
        }
    }
}