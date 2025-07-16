package org.frcpm.models;

public enum CompetitionType {
    SCRIMMAGE("Scrimmage"),
    DISTRICT("District Event"),
    REGIONAL("Regional Championship"),
    DISTRICT_CHAMPIONSHIP("District Championship"),
    CHAMPIONSHIP("FIRST Championship"),
    OFF_SEASON("Off-season Event");
    
    private final String displayName;
    
    CompetitionType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean isOfficial() {
        return this != SCRIMMAGE && this != OFF_SEASON;
    }
    
    public boolean isChampionshipLevel() {
        return this == DISTRICT_CHAMPIONSHIP || this == CHAMPIONSHIP;
    }
    
    public boolean isRegularSeason() {
        return this == DISTRICT || this == REGIONAL;
    }
    
    public int getPriorityOrder() {
        return switch (this) {
            case CHAMPIONSHIP -> 1;
            case DISTRICT_CHAMPIONSHIP -> 2;
            case REGIONAL -> 3;
            case DISTRICT -> 4;
            case SCRIMMAGE -> 5;
            case OFF_SEASON -> 6;
        };
    }
}