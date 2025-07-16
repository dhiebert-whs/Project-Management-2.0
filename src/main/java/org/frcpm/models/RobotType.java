package org.frcpm.models;

public enum RobotType {
    COMPETITION("Competition Robot"),
    PRACTICE("Practice Robot"),
    PROTOTYPE("Prototype"),
    DEMO("Demonstration Robot");
    
    private final String displayName;
    
    RobotType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean requiresInspection() {
        return this == COMPETITION;
    }
    
    public boolean isCompetitionEligible() {
        return this == COMPETITION;
    }
    
    public boolean isForTesting() {
        return this == PRACTICE || this == PROTOTYPE;
    }
    
    public int getPriorityOrder() {
        return switch (this) {
            case COMPETITION -> 1;
            case PRACTICE -> 2;
            case PROTOTYPE -> 3;
            case DEMO -> 4;
        };
    }
}