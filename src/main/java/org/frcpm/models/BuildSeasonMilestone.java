// src/main/java/org/frcpm/models/BuildSeasonMilestone.java

package org.frcpm.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Build Season Milestone for FRC project templates.
 * 
 * Represents key milestones and deadlines within a build season template,
 * providing timeline structure and progress tracking points.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-BuildSeason
 * @since Phase 4A.4 Build Season Timeline Templates
 */
@Entity
@Table(name = "build_season_milestones")
public class BuildSeasonMilestone {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "build_season_template_id", nullable = false)
    private BuildSeasonTemplate buildSeasonTemplate;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MilestoneType milestoneType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MilestonePriority priority;
    
    // Timeline positioning
    @Column(nullable = false)
    private Integer weekOffset; // Weeks from build season start (0 = kickoff week)
    
    @Column(nullable = false)
    private Integer dayOffset; // Days within the week (0 = Monday)
    
    @Column(nullable = false)
    private Integer orderIndex; // Order within the same day
    
    // Requirements and deliverables
    @Column(length = 1000)
    private String deliverables;
    
    @Column(length = 1000)
    private String acceptanceCriteria;
    
    @Column(length = 1000)
    private String dependencies;
    
    @Column(length = 1000)
    private String risks;
    
    // Resource requirements
    @Column(length = 500)
    private String requiredRoles;
    
    @Column(nullable = false)
    private Integer estimatedHours = 0;
    
    @Column(nullable = false)
    private Boolean isMandatory = true;
    
    @Column(nullable = false)
    private Boolean isFlexible = false; // Can be moved if needed
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Types of milestones in the build season
     */
    public enum MilestoneType {
        KICKOFF("Kickoff Event", "Game reveal and rules release"),
        DESIGN_REVIEW("Design Review", "Design concept approval checkpoint"),
        PROTOTYPE_COMPLETE("Prototype Complete", "Working prototype demonstration"),
        CAD_COMPLETE("CAD Complete", "Complete robot design in CAD"),
        MANUFACTURING_START("Manufacturing Start", "Begin robot construction"),
        DRIVETRAIN_COMPLETE("Drivetrain Complete", "Drivetrain assembly and testing"),
        PROGRAMMING_MILESTONE("Programming Milestone", "Software development checkpoint"),
        ROBOT_ASSEMBLY("Robot Assembly", "Final robot assembly"),
        INITIAL_TESTING("Initial Testing", "First robot functionality tests"),
        COMPETITION_READY("Competition Ready", "Robot ready for competition"),
        BAG_AND_TAG("Bag and Tag", "Official build season end"),
        COMPETITION_EVENT("Competition Event", "Regional or district competition"),
        POST_COMPETITION("Post Competition", "Competition review and improvements"),
        SAFETY_INSPECTION("Safety Inspection", "Robot safety and rules compliance"),
        DOCUMENTATION("Documentation", "Technical documentation completion"),
        MENTOR_REVIEW("Mentor Review", "Mentor approval checkpoint"),
        STUDENT_PRESENTATION("Student Presentation", "Student-led progress presentation"),
        EXTERNAL_DEMO("External Demo", "Public demonstration or outreach");
        
        private final String displayName;
        private final String description;
        
        MilestoneType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    /**
     * Priority levels for milestone scheduling
     */
    public enum MilestonePriority {
        CRITICAL("Critical", "Must not be missed, affects competition readiness"),
        HIGH("High", "Important for project success"),
        MEDIUM("Medium", "Beneficial but not essential"),
        LOW("Low", "Nice to have if time permits"),
        OPTIONAL("Optional", "Can be skipped if necessary");
        
        private final String displayName;
        private final String description;
        
        MilestonePriority(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    // Constructors
    public BuildSeasonMilestone() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public BuildSeasonMilestone(BuildSeasonTemplate template, String name, String description,
                               MilestoneType milestoneType, MilestonePriority priority,
                               int weekOffset, int dayOffset) {
        this();
        this.buildSeasonTemplate = template;
        this.name = name;
        this.description = description;
        this.milestoneType = milestoneType;
        this.priority = priority;
        this.weekOffset = weekOffset;
        this.dayOffset = dayOffset;
    }
    
    // Business Methods
    
    /**
     * Calculates the actual date for this milestone based on build season start date.
     */
    public LocalDate getActualDate(LocalDate buildSeasonStart) {
        return buildSeasonStart.plusWeeks(weekOffset).plusDays(dayOffset);
    }
    
    /**
     * Determines if this milestone is critical to competition readiness.
     */
    public boolean isCriticalPath() {
        return priority == MilestonePriority.CRITICAL || 
               milestoneType == MilestoneType.COMPETITION_READY ||
               milestoneType == MilestoneType.BAG_AND_TAG ||
               milestoneType == MilestoneType.COMPETITION_EVENT;
    }
    
    /**
     * Checks if this milestone can be moved to accommodate schedule changes.
     */
    public boolean canBeRescheduled() {
        return isFlexible && priority != MilestonePriority.CRITICAL;
    }
    
    /**
     * Gets the milestone's position in the build season as a percentage.
     */
    public double getSeasonProgressPercentage(int totalBuildSeasonWeeks) {
        double totalDays = totalBuildSeasonWeeks * 7.0;
        double milestoneDays = (weekOffset * 7) + dayOffset;
        return (milestoneDays / totalDays) * 100.0;
    }
    
    /**
     * Determines if this milestone is in the same week as another.
     */
    public boolean isSameWeek(BuildSeasonMilestone other) {
        return this.weekOffset.equals(other.weekOffset);
    }
    
    /**
     * Calculates days between this milestone and another.
     */
    public int getDaysUntil(BuildSeasonMilestone other) {
        int thisTotalDays = (weekOffset * 7) + dayOffset;
        int otherTotalDays = (other.weekOffset * 7) + other.dayOffset;
        return otherTotalDays - thisTotalDays;
    }
    
    /**
     * Validates milestone timing and dependencies.
     */
    public boolean isValidTiming(int totalBuildSeasonWeeks) {
        if (weekOffset < 0 || weekOffset >= totalBuildSeasonWeeks) return false;
        if (dayOffset < 0 || dayOffset > 6) return false;
        
        // Special validation for fixed milestones
        if (milestoneType == MilestoneType.KICKOFF && (weekOffset != 0 || dayOffset != 0)) {
            return false; // Kickoff must be week 0, day 0
        }
        
        return true;
    }
    
    /**
     * Creates a copy of this milestone for a different template.
     */
    public BuildSeasonMilestone createCopy(BuildSeasonTemplate newTemplate) {
        BuildSeasonMilestone copy = new BuildSeasonMilestone();
        copy.setBuildSeasonTemplate(newTemplate);
        copy.setName(this.name);
        copy.setDescription(this.description);
        copy.setMilestoneType(this.milestoneType);
        copy.setPriority(this.priority);
        copy.setWeekOffset(this.weekOffset);
        copy.setDayOffset(this.dayOffset);
        copy.setOrderIndex(this.orderIndex);
        copy.setDeliverables(this.deliverables);
        copy.setAcceptanceCriteria(this.acceptanceCriteria);
        copy.setDependencies(this.dependencies);
        copy.setRisks(this.risks);
        copy.setRequiredRoles(this.requiredRoles);
        copy.setEstimatedHours(this.estimatedHours);
        copy.setIsMandatory(this.isMandatory);
        copy.setIsFlexible(this.isFlexible);
        return copy;
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public BuildSeasonTemplate getBuildSeasonTemplate() { return buildSeasonTemplate; }
    public void setBuildSeasonTemplate(BuildSeasonTemplate buildSeasonTemplate) { this.buildSeasonTemplate = buildSeasonTemplate; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public MilestoneType getMilestoneType() { return milestoneType; }
    public void setMilestoneType(MilestoneType milestoneType) { this.milestoneType = milestoneType; }
    
    public MilestonePriority getPriority() { return priority; }
    public void setPriority(MilestonePriority priority) { this.priority = priority; }
    
    public Integer getWeekOffset() { return weekOffset; }
    public void setWeekOffset(Integer weekOffset) { this.weekOffset = weekOffset; }
    
    public Integer getDayOffset() { return dayOffset; }
    public void setDayOffset(Integer dayOffset) { this.dayOffset = dayOffset; }
    
    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
    
    public String getDeliverables() { return deliverables; }
    public void setDeliverables(String deliverables) { this.deliverables = deliverables; }
    
    public String getAcceptanceCriteria() { return acceptanceCriteria; }
    public void setAcceptanceCriteria(String acceptanceCriteria) { this.acceptanceCriteria = acceptanceCriteria; }
    
    public String getDependencies() { return dependencies; }
    public void setDependencies(String dependencies) { this.dependencies = dependencies; }
    
    public String getRisks() { return risks; }
    public void setRisks(String risks) { this.risks = risks; }
    
    public String getRequiredRoles() { return requiredRoles; }
    public void setRequiredRoles(String requiredRoles) { this.requiredRoles = requiredRoles; }
    
    public Integer getEstimatedHours() { return estimatedHours; }
    public void setEstimatedHours(Integer estimatedHours) { this.estimatedHours = estimatedHours; }
    
    public Boolean getIsMandatory() { return isMandatory; }
    public void setIsMandatory(Boolean isMandatory) { this.isMandatory = isMandatory; }
    
    public Boolean getIsFlexible() { return isFlexible; }
    public void setIsFlexible(Boolean isFlexible) { this.isFlexible = isFlexible; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @Override
    public String toString() {
        return String.format("BuildSeasonMilestone{id=%d, name='%s', week=%d, day=%d, type=%s, priority=%s}", 
                           id, name, weekOffset, dayOffset, milestoneType, priority);
    }
}