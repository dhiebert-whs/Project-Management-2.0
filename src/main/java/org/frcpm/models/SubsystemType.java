// src/main/java/org/frcpm/models/SubsystemType.java
// Phase 4A: Robot Subsystem Classification

package org.frcpm.models;

/**
 * Enumeration of standard FRC robot subsystems.
 * 
 * This enum defines the common subsystems found in FRC robots,
 * used for project template organization and task categorization.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-4A
 * @since Phase 4A - Robot Build Season Optimization
 */
public enum SubsystemType {
    
    // Drive Systems
    DRIVETRAIN("Drivetrain", "Robot movement and locomotion", "🚗", 
              "Tank Drive, West Coast Drive, Swerve Drive, Mecanum Drive"),
    
    // Game Piece Manipulation
    INTAKE("Intake", "Game piece collection and acquisition", "🔄", 
           "Roller Intake, Pneumatic Claw, Flex Wheel, Conveyor"),
    
    SHOOTER("Shooter", "Game piece launching and scoring", "🎯", 
            "Flywheel Shooter, Catapult, Linear Actuator, Pneumatic"),
    
    // Robot Functions
    CLIMBER("Climber", "End game climbing and hanging", "🧗", 
            "Telescoping Arm, Pneumatic Lift, Winch System, Hook Mechanism"),
    
    AUTONOMOUS("Autonomous", "Autonomous period programming and sensors", "🤖", 
               "Path Planning, Vision Targeting, Encoder Navigation, Gyro Systems"),
    
    // Support Systems
    PNEUMATICS("Pneumatics", "Compressed air systems and actuators", "💨", 
               "Compressor, Solenoids, Cylinders, Pressure Management"),
    
    ELECTRICAL("Electrical", "Power distribution and wiring", "⚡", 
               "PDP/PDH, Motor Controllers, Sensors, Wiring Harness"),
    
    PROGRAMMING("Programming", "Robot code and control systems", "💻", 
                "Drive Code, Autonomous, Sensor Integration, Dashboard"),
    
    // Specialized Systems  
    VISION("Vision", "Computer vision and targeting systems", "👁️", 
           "Limelight, PhotonVision, AprilTags, Object Detection"),
    
    MECHANISM("Mechanism", "Custom mechanisms and specialty functions", "⚙️", 
              "Custom Manipulator, Specialty Actuator, Unique Design"),
    
    // Project Management
    ASSEMBLY("Assembly", "Robot assembly and integration", "🔧", 
             "Frame Assembly, Component Integration, System Testing"),
    
    TESTING("Testing", "Robot testing and validation", "🧪", 
            "Subsystem Testing, Integration Testing, Competition Prep");
    
    private final String displayName;
    private final String description;
    private final String icon;
    private final String commonTypes;
    
    SubsystemType(String displayName, String description, String icon, String commonTypes) {
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.commonTypes = commonTypes;
    }
    
    /**
     * Get the human-readable display name.
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Get the subsystem description.
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Get the emoji icon for UI display.
     */
    public String getIcon() {
        return icon;
    }
    
    /**
     * Get common implementation types for this subsystem.
     */
    public String getCommonTypes() {
        return commonTypes;
    }
    
    /**
     * Get formatted display string with icon.
     */
    public String getDisplayWithIcon() {
        return icon + " " + displayName;
    }
    
    /**
     * Check if this subsystem is a core robot system.
     */
    public boolean isCoreSubsystem() {
        return this == DRIVETRAIN || this == INTAKE || this == SHOOTER || 
               this == CLIMBER || this == AUTONOMOUS;
    }
    
    /**
     * Check if this subsystem is a support system.
     */
    public boolean isSupportSubsystem() {
        return this == PNEUMATICS || this == ELECTRICAL || this == PROGRAMMING;
    }
    
    /**
     * Check if this subsystem is specialized/advanced.
     */
    public boolean isSpecializedSubsystem() {
        return this == VISION || this == MECHANISM;
    }
    
    /**
     * Check if this subsystem is project management related.
     */
    public boolean isProjectManagementSubsystem() {
        return this == ASSEMBLY || this == TESTING;
    }
    
    /**
     * Get typical development priority for build season.
     * Lower numbers = higher priority.
     */
    public int getBuildPriority() {
        return switch (this) {
            case DRIVETRAIN -> 1;
            case ELECTRICAL, PROGRAMMING -> 2;
            case INTAKE, SHOOTER -> 3;
            case AUTONOMOUS -> 4;
            case PNEUMATICS -> 5;
            case CLIMBER -> 6;
            case VISION -> 7;
            case MECHANISM -> 8;
            case ASSEMBLY, TESTING -> 9;
        };
    }
    
    /**
     * Get estimated development weeks for this subsystem.
     */
    public int getEstimatedWeeks() {
        return switch (this) {
            case DRIVETRAIN -> 2;
            case INTAKE, SHOOTER -> 3;
            case CLIMBER -> 2;
            case AUTONOMOUS -> 4;
            case PNEUMATICS -> 1;
            case ELECTRICAL -> 3;
            case PROGRAMMING -> 6;
            case VISION -> 2;
            case MECHANISM -> 3;
            case ASSEMBLY -> 2;
            case TESTING -> 1;
        };
    }
    
    /**
     * Get typical team size for this subsystem.
     */
    public int getTypicalTeamSize() {
        return switch (this) {
            case DRIVETRAIN -> 4;
            case INTAKE, SHOOTER -> 3;
            case CLIMBER -> 2;
            case AUTONOMOUS -> 2;
            case PNEUMATICS -> 1;
            case ELECTRICAL -> 2;
            case PROGRAMMING -> 3;
            case VISION -> 2;
            case MECHANISM -> 2;
            case ASSEMBLY -> 6;
            case TESTING -> 4;
        };
    }
}