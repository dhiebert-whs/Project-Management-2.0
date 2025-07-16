package org.frcpm.models;

public enum SeasonStatus {
    UPCOMING("Upcoming season"),
    KICKOFF("Kickoff event"),
    BUILD_SEASON("Build season active"),
    STOP_BUILD("Stop build - bag and tag"),
    COMPETITION("Competition season"),
    COMPLETED("Season completed"),
    ARCHIVED("Archived season");
    
    private final String description;
    
    SeasonStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isActive() {
        return this == BUILD_SEASON || this == COMPETITION;
    }
    
    public boolean isBuildActive() {
        return this == BUILD_SEASON;
    }
    
    public boolean isCompetitionActive() {
        return this == COMPETITION;
    }
    
    public boolean isComplete() {
        return this == COMPLETED || this == ARCHIVED;
    }
}