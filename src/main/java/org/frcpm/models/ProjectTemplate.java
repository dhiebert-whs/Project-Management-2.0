// src/main/java/org/frcpm/models/ProjectTemplate.java
// Phase 4A: Robot Subsystem Project Templates

package org.frcpm.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a reusable project template for FRC robot subsystems.
 * 
 * Project templates provide standardized starting points for common robot
 * subsystems, including predefined tasks, resource requirements, and timelines.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-4A
 * @since Phase 4A - Robot Build Season Optimization
 */
@Entity
@Table(name = "project_templates")
public class ProjectTemplate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Template name is required")
    @Size(max = 100, message = "Template name must not exceed 100 characters")
    @Column(name = "name", length = 100, nullable = false)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "subsystem_type", nullable = false)
    @NotNull(message = "Subsystem type is required")
    private SubsystemType subsystemType;
    
    @NotBlank(message = "Implementation type is required")
    @Size(max = 50, message = "Implementation type must not exceed 50 characters")
    @Column(name = "implementation_type", length = 50, nullable = false)
    private String implementationType; // e.g., "West Coast Drive", "Flywheel Shooter"
    
    @Min(1)
    @Max(20)
    @Column(name = "estimated_weeks", nullable = false)
    private int estimatedWeeks;
    
    @Min(1)
    @Max(50)
    @Column(name = "team_size", nullable = false)
    private int teamSize;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", nullable = false)
    private DifficultyLevel difficultyLevel = DifficultyLevel.INTERMEDIATE;
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Size(max = 50)
    @Column(name = "created_by", length = 50)
    private String createdBy;
    
    @OneToMany(mappedBy = "projectTemplate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskTemplate> taskTemplates = new ArrayList<>();
    
    @OneToMany(mappedBy = "projectTemplate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResourceRequirement> resourceRequirements = new ArrayList<>();
    
    @OneToMany(mappedBy = "projectTemplate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MilestoneTemplate> milestoneTemplates = new ArrayList<>();
    
    // Usage tracking
    @Column(name = "usage_count", nullable = false)
    private int usageCount = 0;
    
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
    
    // Build season context
    @Column(name = "build_priority", nullable = false)
    private int buildPriority = 5; // 1 = highest priority, 10 = lowest
    
    @Column(name = "parallel_development")
    private boolean parallelDevelopment = true; // Can be developed alongside other subsystems
    
    @ElementCollection
    @CollectionTable(name = "template_dependencies", joinColumns = @JoinColumn(name = "template_id"))
    @Column(name = "dependency_type")
    @Enumerated(EnumType.STRING)
    private List<SubsystemType> dependencies = new ArrayList<>();
    
    // Success metrics
    @Column(name = "avg_completion_rate")
    private Double avgCompletionRate;
    
    @Column(name = "avg_actual_weeks")
    private Double avgActualWeeks;
    
    @Column(name = "success_rating")
    private Double successRating;
    
    // Constructors
    
    public ProjectTemplate() {
        // Default constructor required by JPA
    }
    
    public ProjectTemplate(String name, SubsystemType subsystemType, String implementationType) {
        this.name = name;
        this.subsystemType = subsystemType;
        this.implementationType = implementationType;
        this.estimatedWeeks = subsystemType.getEstimatedWeeks();
        this.teamSize = subsystemType.getTypicalTeamSize();
        this.buildPriority = subsystemType.getBuildPriority();
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public SubsystemType getSubsystemType() {
        return subsystemType;
    }
    
    public void setSubsystemType(SubsystemType subsystemType) {
        this.subsystemType = subsystemType;
    }
    
    public String getImplementationType() {
        return implementationType;
    }
    
    public void setImplementationType(String implementationType) {
        this.implementationType = implementationType;
    }
    
    public int getEstimatedWeeks() {
        return estimatedWeeks;
    }
    
    public void setEstimatedWeeks(int estimatedWeeks) {
        this.estimatedWeeks = estimatedWeeks;
    }
    
    public int getTeamSize() {
        return teamSize;
    }
    
    public void setTeamSize(int teamSize) {
        this.teamSize = teamSize;
    }
    
    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }
    
    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public List<TaskTemplate> getTaskTemplates() {
        return taskTemplates;
    }
    
    public void setTaskTemplates(List<TaskTemplate> taskTemplates) {
        this.taskTemplates = taskTemplates;
    }
    
    public List<ResourceRequirement> getResourceRequirements() {
        return resourceRequirements;
    }
    
    public void setResourceRequirements(List<ResourceRequirement> resourceRequirements) {
        this.resourceRequirements = resourceRequirements;
    }
    
    public List<MilestoneTemplate> getMilestoneTemplates() {
        return milestoneTemplates;
    }
    
    public void setMilestoneTemplates(List<MilestoneTemplate> milestoneTemplates) {
        this.milestoneTemplates = milestoneTemplates;
    }
    
    public int getUsageCount() {
        return usageCount;
    }
    
    public void setUsageCount(int usageCount) {
        this.usageCount = usageCount;
    }
    
    public LocalDateTime getLastUsedAt() {
        return lastUsedAt;
    }
    
    public void setLastUsedAt(LocalDateTime lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }
    
    public int getBuildPriority() {
        return buildPriority;
    }
    
    public void setBuildPriority(int buildPriority) {
        this.buildPriority = buildPriority;
    }
    
    public boolean isParallelDevelopment() {
        return parallelDevelopment;
    }
    
    public void setParallelDevelopment(boolean parallelDevelopment) {
        this.parallelDevelopment = parallelDevelopment;
    }
    
    public List<SubsystemType> getDependencies() {
        return dependencies;
    }
    
    public void setDependencies(List<SubsystemType> dependencies) {
        this.dependencies = dependencies;
    }
    
    public Double getAvgCompletionRate() {
        return avgCompletionRate;
    }
    
    public void setAvgCompletionRate(Double avgCompletionRate) {
        this.avgCompletionRate = avgCompletionRate;
    }
    
    public Double getAvgActualWeeks() {
        return avgActualWeeks;
    }
    
    public void setAvgActualWeeks(Double avgActualWeeks) {
        this.avgActualWeeks = avgActualWeeks;
    }
    
    public Double getSuccessRating() {
        return successRating;
    }
    
    public void setSuccessRating(Double successRating) {
        this.successRating = successRating;
    }
    
    // Helper methods
    
    /**
     * Add a task template to this project template.
     */
    public void addTaskTemplate(TaskTemplate taskTemplate) {
        taskTemplates.add(taskTemplate);
        taskTemplate.setProjectTemplate(this);
    }
    
    /**
     * Remove a task template from this project template.
     */
    public void removeTaskTemplate(TaskTemplate taskTemplate) {
        taskTemplates.remove(taskTemplate);
        taskTemplate.setProjectTemplate(null);
    }
    
    /**
     * Add a resource requirement to this project template.
     */
    public void addResourceRequirement(ResourceRequirement resourceRequirement) {
        resourceRequirements.add(resourceRequirement);
        resourceRequirement.setProjectTemplate(this);
    }
    
    /**
     * Remove a resource requirement from this project template.
     */
    public void removeResourceRequirement(ResourceRequirement resourceRequirement) {
        resourceRequirements.remove(resourceRequirement);
        resourceRequirement.setProjectTemplate(null);
    }
    
    /**
     * Add a milestone template to this project template.
     */
    public void addMilestoneTemplate(MilestoneTemplate milestoneTemplate) {
        milestoneTemplates.add(milestoneTemplate);
        milestoneTemplate.setProjectTemplate(this);
    }
    
    /**
     * Remove a milestone template from this project template.
     */
    public void removeMilestoneTemplate(MilestoneTemplate milestoneTemplate) {
        milestoneTemplates.remove(milestoneTemplate);
        milestoneTemplate.setProjectTemplate(null);
    }
    
    /**
     * Increment usage count and update last used timestamp.
     */
    public void recordUsage() {
        this.usageCount++;
        this.lastUsedAt = LocalDateTime.now();
    }
    
    /**
     * Get formatted display name with subsystem icon.
     */
    public String getDisplayName() {
        return subsystemType.getIcon() + " " + name;
    }
    
    /**
     * Check if this template has dependencies.
     */
    public boolean hasDependencies() {
        return dependencies != null && !dependencies.isEmpty();
    }
    
    /**
     * Get estimated total hours for this template.
     */
    public double getEstimatedHours() {
        return estimatedWeeks * 40.0; // Assume 40 hours per week during build season
    }
    
    /**
     * Get difficulty level as string for display.
     */
    public String getDifficultyDisplay() {
        return difficultyLevel.getDisplayName();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return name + " (" + subsystemType.getDisplayName() + ")";
    }
    
    /**
     * Difficulty levels for project templates.
     */
    public enum DifficultyLevel {
        BEGINNER("Beginner", "Suitable for new teams or first-time builders"),
        INTERMEDIATE("Intermediate", "Requires some experience with FRC systems"),
        ADVANCED("Advanced", "Complex system requiring experienced team members"),
        EXPERT("Expert", "Cutting-edge implementation for highly experienced teams");
        
        private final String displayName;
        private final String description;
        
        DifficultyLevel(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getDescription() {
            return description;
        }
    }
}