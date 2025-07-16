package org.frcpm.models;

public enum RobotStatus {
    DESIGN("Design phase"),
    PROTOTYPING("Prototyping"),
    MANUFACTURING("Manufacturing"),
    ASSEMBLY("Assembly"),
    TESTING("Testing"),
    PROGRAMMING("Programming"),
    INTEGRATION("Integration testing"),
    COMPETITION_READY("Competition ready"),
    COMPETING("Competing"),
    MAINTENANCE("Under maintenance"),
    RETIRED("Retired");
    
    private final String description;
    
    RobotStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isActiveStatus() {
        return this != RETIRED;
    }
    
    public boolean isProductionReady() {
        return this == COMPETITION_READY || this == COMPETING;
    }
    
    public boolean isInDevelopment() {
        return this == DESIGN || this == PROTOTYPING || this == MANUFACTURING || 
               this == ASSEMBLY || this == TESTING || this == PROGRAMMING || this == INTEGRATION;
    }
    
    public int getProgressPercentage() {
        return switch (this) {
            case DESIGN -> 10;
            case PROTOTYPING -> 20;
            case MANUFACTURING -> 40;
            case ASSEMBLY -> 60;
            case TESTING -> 70;
            case PROGRAMMING -> 80;
            case INTEGRATION -> 90;
            case COMPETITION_READY -> 100;
            case COMPETING -> 100;
            case MAINTENANCE -> 95;
            case RETIRED -> 100;
        };
    }
}