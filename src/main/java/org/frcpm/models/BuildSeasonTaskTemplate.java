// src/main/java/org/frcpm/models/BuildSeasonTaskTemplate.java

package org.frcpm.models;

import jakarta.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Build Season Task Template for FRC project templates.
 * 
 * Defines standardized task templates that can be instantiated when creating
 * projects from build season templates, providing consistent task structure
 * and estimation across different FRC teams.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-BuildSeason
 * @since Phase 4A.4 Build Season Timeline Templates
 */
@Entity
@Table(name = "build_season_task_templates")
public class BuildSeasonTaskTemplate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "build_season_template_id", nullable = false)
    private BuildSeasonTemplate buildSeasonTemplate;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskCategory category;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskPriority priority;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskComplexity complexity;
    
    // Timeline and scheduling
    @Column(nullable = false)
    private Integer weekOffset; // Weeks from build season start
    
    @Column(nullable = false)
    private Integer estimatedHours;
    
    @Column(nullable = false)
    private Integer bufferHours = 0; // Additional time buffer
    
    @Column(nullable = false)
    private Boolean isParallelizable = false; // Can run in parallel with other tasks
    
    // Requirements and dependencies
    @Column(length = 1000)
    private String prerequisites;
    
    @Column(length = 1000)
    private String deliverables;
    
    @Column(length = 1000)
    private String acceptanceCriteria;
    
    @Column(length = 500)
    private String requiredSkills;
    
    @Column(length = 500)
    private String requiredRoles;
    
    @Column(nullable = false)
    private Integer minTeamMembers = 1;
    
    @Column(nullable = false)
    private Integer maxTeamMembers = 4;
    
    // Resources and tools
    @Column(length = 1000)
    private String requiredTools;
    
    @Column(length = 1000)
    private String requiredMaterials;
    
    @Column(length = 1000)
    private String safetyConsiderations;
    
    // Quality and validation
    @Column(length = 1000)
    private String qualityChecks;
    
    @Column(length = 1000)
    private String testingRequirements;
    
    @Column(nullable = false)
    private Boolean requiresMentorApproval = false;
    
    @Column(nullable = false)
    private Boolean requiresQualityCheck = false;
    
    // Template metadata
    @Column(nullable = false)
    private Integer orderIndex = 0; // Order within the week
    
    @Column(nullable = false)
    private Boolean isOptional = false;
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Task categories for organizing build season work
     */
    public enum TaskCategory {
        STRATEGY("Strategy & Game Analysis", "Game strategy and field analysis"),
        DESIGN("Design & CAD", "Robot design and computer-aided design"),
        PROTOTYPE("Prototyping", "Proof of concept and testing"),
        MANUFACTURING("Manufacturing", "Physical construction and fabrication"),
        PROGRAMMING("Programming", "Software development and controls"),
        TESTING("Testing & Validation", "Robot testing and performance validation"),
        DOCUMENTATION("Documentation", "Technical documentation and reports"),
        PROJECT_MANAGEMENT("Project Management", "Planning, coordination, and oversight"),
        OUTREACH("Outreach & Marketing", "Community engagement and team promotion"),
        LOGISTICS("Logistics & Operations", "Travel, equipment, and event preparation"),
        TRAINING("Training & Development", "Skill building and education"),
        SAFETY("Safety & Compliance", "Safety protocols and rule compliance"),
        INTEGRATION("Integration & Assembly", "Combining subsystems and final assembly"),
        COMPETITION_PREP("Competition Preparation", "Competition-specific preparation");
        
        private final String displayName;
        private final String description;
        
        TaskCategory(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    /**
     * Task priority levels for scheduling and resource allocation
     */
    public enum TaskPriority {
        CRITICAL("Critical", "Essential for competition readiness"),
        HIGH("High", "Important for competitive performance"),
        MEDIUM("Medium", "Beneficial for team success"),
        LOW("Low", "Nice to have if time permits"),
        OPTIONAL("Optional", "Can be deferred to offseason");
        
        private final String displayName;
        private final String description;
        
        TaskPriority(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    /**
     * Task complexity levels affecting time estimates and skill requirements
     */
    public enum TaskComplexity {
        BASIC("Basic", "Simple task, minimal experience required", 1.0),
        INTERMEDIATE("Intermediate", "Moderate complexity, some experience needed", 1.3),
        ADVANCED("Advanced", "Complex task, significant experience required", 1.7),
        EXPERT("Expert", "Highly complex, expert-level skills required", 2.2),
        RESEARCH("Research", "Unknown complexity, research required", 2.5);
        
        private final String displayName;
        private final String description;
        private final double complexityMultiplier;
        
        TaskComplexity(String displayName, String description, double complexityMultiplier) {
            this.displayName = displayName;
            this.description = description;
            this.complexityMultiplier = complexityMultiplier;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public double getComplexityMultiplier() { return complexityMultiplier; }
    }
    
    // Constructors
    public BuildSeasonTaskTemplate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public BuildSeasonTaskTemplate(BuildSeasonTemplate template, String name, String description,
                                  TaskCategory category, TaskPriority priority, TaskComplexity complexity,
                                  int weekOffset, int estimatedHours) {
        this();
        this.buildSeasonTemplate = template;
        this.name = name;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.complexity = complexity;
        this.weekOffset = weekOffset;
        this.estimatedHours = estimatedHours;
    }
    
    // Business Methods
    
    /**
     * Calculates adjusted task duration based on team experience and complexity.
     */
    public int getAdjustedDuration(BuildSeasonTemplate.ExperienceLevel teamExperience) {
        double baseHours = estimatedHours + bufferHours;
        double experienceMultiplier = teamExperience.getTimeMultiplier();
        double complexityMultiplier = complexity.getComplexityMultiplier();
        
        return (int) Math.ceil(baseHours * experienceMultiplier * complexityMultiplier);
    }
    
    /**
     * Determines if this task can be started based on team size and skills.
     */
    public boolean canBeStarted(int availableMembers, String teamSkills) {
        if (availableMembers < minTeamMembers) return false;
        
        // Simple skill check (could be enhanced with more sophisticated matching)
        if (requiredSkills != null && !requiredSkills.isEmpty()) {
            String[] requiredSkillArray = requiredSkills.toLowerCase().split(",");
            String teamSkillsLower = teamSkills != null ? teamSkills.toLowerCase() : "";
            
            for (String skill : requiredSkillArray) {
                if (!teamSkillsLower.contains(skill.trim())) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Gets the recommended team size for this task.
     */
    public int getRecommendedTeamSize() {
        return switch (complexity) {
            case BASIC -> Math.min(2, maxTeamMembers);
            case INTERMEDIATE -> Math.min(3, maxTeamMembers);
            case ADVANCED -> Math.min(4, maxTeamMembers);
            case EXPERT -> maxTeamMembers;
            case RESEARCH -> Math.min(2, maxTeamMembers); // Small research teams
        };
    }
    
    /**
     * Determines if this task is on the critical path for competition readiness.
     */
    public boolean isCriticalPath() {
        return priority == TaskPriority.CRITICAL || 
               category == TaskCategory.COMPETITION_PREP ||
               category == TaskCategory.INTEGRATION;
    }
    
    /**
     * Calculates the risk factor based on complexity and requirements.
     */
    public double getRiskFactor() {
        double riskFactor = 1.0;
        
        // Complexity increases risk
        riskFactor *= complexity.getComplexityMultiplier();
        
        // Priority affects risk (critical tasks have higher risk of project impact)
        riskFactor *= switch (priority) {
            case CRITICAL -> 1.5;
            case HIGH -> 1.2;
            case MEDIUM -> 1.0;
            case LOW -> 0.8;
            case OPTIONAL -> 0.5;
        };
        
        // Dependency on approval increases risk
        if (requiresMentorApproval || requiresQualityCheck) {
            riskFactor *= 1.2;
        }
        
        return riskFactor;
    }
    
    /**
     * Creates a Task instance from this template.
     */
    public Task createTask(Project project, Subsystem subsystem) {
        Task task = new Task();
        task.setTitle(this.name);
        task.setDescription(this.description);
        task.setProject(project);
        task.setSubsystem(subsystem);
        task.setEstimatedDuration(Duration.ofHours(this.estimatedHours));
        
        // Map template priority to task priority
        task.setPriority(switch (this.priority) {
            case CRITICAL, HIGH -> Task.Priority.HIGH;
            case MEDIUM -> Task.Priority.MEDIUM;
            case LOW, OPTIONAL -> Task.Priority.LOW;
        });
        
        task.setProgress(0);
        task.setCompleted(false);
        
        return task;
    }
    
    /**
     * Validates task template configuration.
     */
    public boolean isValidConfiguration() {
        if (name == null || name.trim().isEmpty()) return false;
        if (category == null) return false;
        if (priority == null) return false;
        if (complexity == null) return false;
        if (weekOffset < 0) return false;
        if (estimatedHours <= 0) return false;
        if (minTeamMembers <= 0 || maxTeamMembers <= 0) return false;
        if (minTeamMembers > maxTeamMembers) return false;
        
        return true;
    }
    
    /**
     * Creates a copy of this task template for a different build season template.
     */
    public BuildSeasonTaskTemplate createCopy(BuildSeasonTemplate newTemplate) {
        BuildSeasonTaskTemplate copy = new BuildSeasonTaskTemplate();
        copy.setBuildSeasonTemplate(newTemplate);
        copy.setName(this.name);
        copy.setDescription(this.description);
        copy.setCategory(this.category);
        copy.setPriority(this.priority);
        copy.setComplexity(this.complexity);
        copy.setWeekOffset(this.weekOffset);
        copy.setEstimatedHours(this.estimatedHours);
        copy.setBufferHours(this.bufferHours);
        copy.setIsParallelizable(this.isParallelizable);
        copy.setPrerequisites(this.prerequisites);
        copy.setDeliverables(this.deliverables);
        copy.setAcceptanceCriteria(this.acceptanceCriteria);
        copy.setRequiredSkills(this.requiredSkills);
        copy.setRequiredRoles(this.requiredRoles);
        copy.setMinTeamMembers(this.minTeamMembers);
        copy.setMaxTeamMembers(this.maxTeamMembers);
        copy.setRequiredTools(this.requiredTools);
        copy.setRequiredMaterials(this.requiredMaterials);
        copy.setSafetyConsiderations(this.safetyConsiderations);
        copy.setQualityChecks(this.qualityChecks);
        copy.setTestingRequirements(this.testingRequirements);
        copy.setRequiresMentorApproval(this.requiresMentorApproval);
        copy.setRequiresQualityCheck(this.requiresQualityCheck);
        copy.setOrderIndex(this.orderIndex);
        copy.setIsOptional(this.isOptional);
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
    
    public TaskCategory getCategory() { return category; }
    public void setCategory(TaskCategory category) { this.category = category; }
    
    public TaskPriority getPriority() { return priority; }
    public void setPriority(TaskPriority priority) { this.priority = priority; }
    
    public TaskComplexity getComplexity() { return complexity; }
    public void setComplexity(TaskComplexity complexity) { this.complexity = complexity; }
    
    public Integer getWeekOffset() { return weekOffset; }
    public void setWeekOffset(Integer weekOffset) { this.weekOffset = weekOffset; }
    
    public Integer getEstimatedHours() { return estimatedHours; }
    public void setEstimatedHours(Integer estimatedHours) { this.estimatedHours = estimatedHours; }
    
    public Integer getBufferHours() { return bufferHours; }
    public void setBufferHours(Integer bufferHours) { this.bufferHours = bufferHours; }
    
    public Boolean getIsParallelizable() { return isParallelizable; }
    public void setIsParallelizable(Boolean isParallelizable) { this.isParallelizable = isParallelizable; }
    
    public String getPrerequisites() { return prerequisites; }
    public void setPrerequisites(String prerequisites) { this.prerequisites = prerequisites; }
    
    public String getDeliverables() { return deliverables; }
    public void setDeliverables(String deliverables) { this.deliverables = deliverables; }
    
    public String getAcceptanceCriteria() { return acceptanceCriteria; }
    public void setAcceptanceCriteria(String acceptanceCriteria) { this.acceptanceCriteria = acceptanceCriteria; }
    
    public String getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(String requiredSkills) { this.requiredSkills = requiredSkills; }
    
    public String getRequiredRoles() { return requiredRoles; }
    public void setRequiredRoles(String requiredRoles) { this.requiredRoles = requiredRoles; }
    
    public Integer getMinTeamMembers() { return minTeamMembers; }
    public void setMinTeamMembers(Integer minTeamMembers) { this.minTeamMembers = minTeamMembers; }
    
    public Integer getMaxTeamMembers() { return maxTeamMembers; }
    public void setMaxTeamMembers(Integer maxTeamMembers) { this.maxTeamMembers = maxTeamMembers; }
    
    public String getRequiredTools() { return requiredTools; }
    public void setRequiredTools(String requiredTools) { this.requiredTools = requiredTools; }
    
    public String getRequiredMaterials() { return requiredMaterials; }
    public void setRequiredMaterials(String requiredMaterials) { this.requiredMaterials = requiredMaterials; }
    
    public String getSafetyConsiderations() { return safetyConsiderations; }
    public void setSafetyConsiderations(String safetyConsiderations) { this.safetyConsiderations = safetyConsiderations; }
    
    public String getQualityChecks() { return qualityChecks; }
    public void setQualityChecks(String qualityChecks) { this.qualityChecks = qualityChecks; }
    
    public String getTestingRequirements() { return testingRequirements; }
    public void setTestingRequirements(String testingRequirements) { this.testingRequirements = testingRequirements; }
    
    public Boolean getRequiresMentorApproval() { return requiresMentorApproval; }
    public void setRequiresMentorApproval(Boolean requiresMentorApproval) { this.requiresMentorApproval = requiresMentorApproval; }
    
    public Boolean getRequiresQualityCheck() { return requiresQualityCheck; }
    public void setRequiresQualityCheck(Boolean requiresQualityCheck) { this.requiresQualityCheck = requiresQualityCheck; }
    
    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
    
    public Boolean getIsOptional() { return isOptional; }
    public void setIsOptional(Boolean isOptional) { this.isOptional = isOptional; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @Override
    public String toString() {
        return String.format("BuildSeasonTaskTemplate{id=%d, name='%s', category=%s, priority=%s, week=%d, hours=%d}", 
                           id, name, category, priority, weekOffset, estimatedHours);
    }
}