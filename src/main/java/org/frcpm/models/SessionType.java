package org.frcpm.models;

public enum SessionType {
    DESIGN("Design Session"),
    BUILD("Build Session"),
    PROGRAMMING("Programming Session"),
    TESTING("Testing Session"),
    MACHINING("Machining Session"),
    ASSEMBLY("Assembly Session"),
    TRAINING("Training Session"),
    MEETING("Team Meeting"),
    COMPETITION_PREP("Competition Preparation");
    
    private final String displayName;
    
    SessionType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean requiresSafetyChecklist() {
        return this == BUILD || this == MACHINING || this == ASSEMBLY || this == TESTING;
    }
    
    public boolean requiresMentorSupervision() {
        return this == BUILD || this == MACHINING || this == TESTING;
    }
    
    public boolean isWorkshopSession() {
        return this != MEETING && this != TRAINING;
    }
    
    public boolean isHandsOnWork() {
        return this == BUILD || this == MACHINING || this == ASSEMBLY || this == TESTING;
    }
}