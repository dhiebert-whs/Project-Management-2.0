// src/main/java/org/frcpm/models/DependencyType.java
package org.frcpm.models;

/**
 * Enum representing different types of task dependencies.
 * Follows project management standard dependency types for FRC build season planning.
 * 
 * @author FRC Project Management Team
 * @version 2.0.0-Phase2E-D
 * @since Phase 2E-D - Advanced Task Management
 */
public enum DependencyType {
    
    /**
     * Finish-to-Start (FS): Predecessor task must finish before successor can start.
     * Most common dependency type in FRC projects.
     * Example: "Design chassis" must finish before "Fabricate chassis" can start.
     */
    FINISH_TO_START("Finish-to-Start", "FS", "Predecessor must finish before successor starts"),
    
    /**
     * Start-to-Start (SS): Predecessor task must start before successor can start.
     * Used when tasks can overlap but have a start dependency.
     * Example: "Order materials" must start before "Inventory tracking" can start.
     */
    START_TO_START("Start-to-Start", "SS", "Predecessor must start before successor starts"),
    
    /**
     * Finish-to-Finish (FF): Predecessor task must finish before successor can finish.
     * Used when tasks must complete together.
     * Example: "Program autonomous" must finish before "Test autonomous" can finish.
     */
    FINISH_TO_FINISH("Finish-to-Finish", "FF", "Predecessor must finish before successor finishes"),
    
    /**
     * Start-to-Finish (SF): Predecessor task must start before successor can finish.
     * Rarely used but available for complex scheduling.
     * Example: "Competition setup" must start before "Practice rounds" can finish.
     */
    START_TO_FINISH("Start-to-Finish", "SF", "Predecessor must start before successor finishes"),
    
    /**
     * Blocking dependency: Hard blocking relationship (most restrictive).
     * Used for critical path items that absolutely cannot proceed without predecessor.
     * Example: "Safety review" blocks "Robot operation".
     */
    BLOCKING("Blocking", "BLOCK", "Successor is completely blocked until predecessor completes"),
    
    /**
     * Soft dependency: Preference but not hard requirement.
     * Used for recommended but not mandatory sequencing.
     * Example: "Driver training" should happen before "Competition practice" but isn't required.
     */
    SOFT("Soft Dependency", "SOFT", "Recommended sequence but not mandatory");
    
    private final String displayName;
    private final String shortCode;
    private final String description;
    
    DependencyType(String displayName, String shortCode, String description) {
        this.displayName = displayName;
        this.shortCode = shortCode;
        this.description = description;
    }
    
    /**
     * Get the user-friendly display name.
     * 
     * @return Display name for UI
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Get the short code for compact display.
     * 
     * @return Short code (FS, SS, FF, SF, BLOCK, SOFT)
     */
    public String getShortCode() {
        return shortCode;
    }
    
    /**
     * Get the detailed description of the dependency type.
     * 
     * @return Description explaining the dependency behavior
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Check if this dependency type is a hard constraint.
     * 
     * @return true if this dependency blocks successor tasks
     */
    public boolean isHardConstraint() {
        return this == FINISH_TO_START || this == START_TO_START || 
               this == FINISH_TO_FINISH || this == START_TO_FINISH || 
               this == BLOCKING;
    }
    
    /**
     * Check if this dependency type is suitable for critical path analysis.
     * 
     * @return true if this dependency should be considered in critical path calculations
     */
    public boolean isCriticalPathRelevant() {
        return this != SOFT;
    }
    
    /**
     * Get the CSS class for visual representation.
     * 
     * @return CSS class name for styling
     */
    public String getCssClass() {
        switch (this) {
            case BLOCKING:
                return "dependency-blocking text-danger";
            case FINISH_TO_START:
                return "dependency-finish-start text-primary";
            case START_TO_START:
                return "dependency-start-start text-info";
            case FINISH_TO_FINISH:
                return "dependency-finish-finish text-warning";
            case START_TO_FINISH:
                return "dependency-start-finish text-secondary";
            case SOFT:
                return "dependency-soft text-muted";
            default:
                return "dependency-default";
        }
    }
    
    /**
     * Get the icon class for visual representation.
     * 
     * @return Font Awesome icon class
     */
    public String getIconClass() {
        switch (this) {
            case BLOCKING:
                return "fas fa-ban";
            case FINISH_TO_START:
                return "fas fa-arrow-right";
            case START_TO_START:
                return "fas fa-play";
            case FINISH_TO_FINISH:
                return "fas fa-stop";
            case START_TO_FINISH:
                return "fas fa-step-forward";
            case SOFT:
                return "fas fa-link";
            default:
                return "fas fa-project-diagram";
        }
    }
    
    /**
     * Get default dependency type for FRC projects.
     * 
     * @return Most commonly used dependency type
     */
    public static DependencyType getDefault() {
        return FINISH_TO_START;
    }
    
    /**
     * Get dependency types suitable for build season planning.
     * 
     * @return Array of recommended dependency types for FRC teams
     */
    public static DependencyType[] getBuildSeasonTypes() {
        return new DependencyType[] {
            FINISH_TO_START,
            START_TO_START,
            BLOCKING,
            SOFT
        };
    }
    
    /**
     * Parse dependency type from string representation.
     * 
     * @param value String value (name, display name, or short code)
     * @return DependencyType or null if not found
     */
    public static DependencyType fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        String upperValue = value.trim().toUpperCase();
        
        // Try to match by name first
        for (DependencyType type : values()) {
            if (type.name().equals(upperValue)) {
                return type;
            }
        }
        
        // Try to match by short code
        for (DependencyType type : values()) {
            if (type.getShortCode().equals(upperValue)) {
                return type;
            }
        }
        
        // Try to match by display name (case-insensitive)
        for (DependencyType type : values()) {
            if (type.getDisplayName().equalsIgnoreCase(value.trim())) {
                return type;
            }
        }
        
        return null;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}