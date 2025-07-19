// src/main/java/org/frcpm/models/BuildSeasonTemplate.java

package org.frcpm.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Build Season Template for FRC teams.
 * 
 * Provides standardized project templates optimized for FIRST Robotics Competition
 * build seasons, including timeline milestones, task templates, and resource allocation.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-BuildSeason
 * @since Phase 4A.4 Build Season Timeline Templates
 */
@Entity
@Table(name = "build_season_templates")
public class BuildSeasonTemplate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TemplateType templateType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompetitionLevel competitionLevel;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TeamSize teamSize;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExperienceLevel experienceLevel;
    
    @Column(nullable = false)
    private Integer buildSeasonWeeks = 6;
    
    @Column(nullable = false)
    private Integer estimatedTotalHours;
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @Column(nullable = false)
    private Boolean isDefault = false;
    
    @OneToMany(mappedBy = "buildSeasonTemplate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BuildSeasonMilestone> milestones = new ArrayList<>();
    
    @OneToMany(mappedBy = "buildSeasonTemplate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BuildSeasonTaskTemplate> taskTemplates = new ArrayList<>();
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private TeamMember createdBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_id")
    private TeamMember updatedBy;
    
    // Template metadata
    @Column(length = 1000)
    private String keyObjectives;
    
    @Column(length = 1000)
    private String criticalSuccessFactors;
    
    @Column(length = 1000)
    private String commonRisks;
    
    @Column(length = 1000)
    private String resourceRequirements;
    
    @Column(length = 1000)
    private String recommendedTools;
    
    @Column(length = 1000)
    private String mentorRequirements;
    
    /**
     * Template types for different build season approaches
     */
    public enum TemplateType {
        STANDARD_6_WEEK("Standard 6-Week Build", "Traditional FRC build season timeline"),
        AGGRESSIVE_FAST_BUILD("Aggressive Fast Build", "Compressed timeline for experienced teams"),
        ITERATIVE_PROTOTYPE("Iterative Prototype", "Prototype-heavy approach with multiple iterations"),
        STRATEGY_FOCUSED("Strategy Focused", "Game strategy and alliance preparation focused"),
        ROOKIE_TEAM("Rookie Team", "First-year team with learning emphasis"),
        VETERAN_CHAMPIONSHIP("Veteran Championship", "Championship-caliber team approach"),
        REMOTE_HYBRID("Remote/Hybrid", "Remote and hybrid team coordination"),
        COMPETITION_SPECIFIC("Competition Specific", "Tailored for specific competition types");
        
        private final String displayName;
        private final String description;
        
        TemplateType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    /**
     * Competition levels with different complexity requirements
     */
    public enum CompetitionLevel {
        REGIONAL_ONLY("Regional Only", "Competing in regional events only"),
        DISTRICT_CHAMPIONSHIP("District Championship", "Aiming for district championship"),
        WORLD_CHAMPIONSHIP("World Championship", "Targeting FIRST Championship"),
        OFFSEASON_COMPETITIVE("Offseason Competitive", "Offseason competition focused"),
        DEMONSTRATION_OUTREACH("Demonstration/Outreach", "Education and outreach focused");
        
        private final String displayName;
        private final String description;
        
        CompetitionLevel(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    /**
     * Team size categories affecting resource allocation
     */
    public enum TeamSize {
        SMALL("Small Team", "5-15 members", 5, 15),
        MEDIUM("Medium Team", "16-30 members", 16, 30),
        LARGE("Large Team", "31-50 members", 31, 50),
        EXTRA_LARGE("Extra Large Team", "51+ members", 51, 100);
        
        private final String displayName;
        private final String description;
        private final int minMembers;
        private final int maxMembers;
        
        TeamSize(String displayName, String description, int minMembers, int maxMembers) {
            this.displayName = displayName;
            this.description = description;
            this.minMembers = minMembers;
            this.maxMembers = maxMembers;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public int getMinMembers() { return minMembers; }
        public int getMaxMembers() { return maxMembers; }
        
        public boolean containsSize(int memberCount) {
            return memberCount >= minMembers && memberCount <= maxMembers;
        }
    }
    
    /**
     * Experience levels affecting timeline and complexity
     */
    public enum ExperienceLevel {
        ROOKIE("Rookie", "First year team", 1.5),
        DEVELOPING("Developing", "2-3 years experience", 1.2),
        EXPERIENCED("Experienced", "4-7 years experience", 1.0),
        VETERAN("Veteran", "8+ years experience", 0.8),
        ELITE("Elite", "Championship-caliber team", 0.6);
        
        private final String displayName;
        private final String description;
        private final double timeMultiplier; // Affects task duration estimates
        
        ExperienceLevel(String displayName, String description, double timeMultiplier) {
            this.displayName = displayName;
            this.description = description;
            this.timeMultiplier = timeMultiplier;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public double getTimeMultiplier() { return timeMultiplier; }
    }
    
    // Constructors
    public BuildSeasonTemplate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public BuildSeasonTemplate(String name, String description, TemplateType templateType,
                              CompetitionLevel competitionLevel, TeamSize teamSize, 
                              ExperienceLevel experienceLevel) {
        this();
        this.name = name;
        this.description = description;
        this.templateType = templateType;
        this.competitionLevel = competitionLevel;
        this.teamSize = teamSize;
        this.experienceLevel = experienceLevel;
    }
    
    // Business Methods
    
    /**
     * Calculates adjusted task duration based on team experience level.
     */
    public int getAdjustedDuration(int baseDurationHours) {
        return (int) Math.ceil(baseDurationHours * experienceLevel.getTimeMultiplier());
    }
    
    /**
     * Determines if this template is suitable for a given team configuration.
     */
    public boolean isSuitableForTeam(int memberCount, int yearsExperience, boolean targetingChampionship) {
        // Check team size compatibility
        if (!teamSize.containsSize(memberCount)) {
            return false;
        }
        
        // Check experience level compatibility
        boolean experienceMatch = switch (experienceLevel) {
            case ROOKIE -> yearsExperience <= 1;
            case DEVELOPING -> yearsExperience >= 2 && yearsExperience <= 3;
            case EXPERIENCED -> yearsExperience >= 4 && yearsExperience <= 7;
            case VETERAN -> yearsExperience >= 8;
            case ELITE -> yearsExperience >= 8 && targetingChampionship;
        };
        
        return experienceMatch;
    }
    
    /**
     * Gets the recommended build season start date based on competition schedule.
     */
    public LocalDate getRecommendedStartDate(LocalDate kickoffDate) {
        return kickoffDate; // Standard FRC build season starts at kickoff
    }
    
    /**
     * Gets the recommended build season end date.
     */
    public LocalDate getRecommendedEndDate(LocalDate kickoffDate) {
        return kickoffDate.plusWeeks(buildSeasonWeeks);
    }
    
    /**
     * Calculates total estimated project duration in weeks.
     */
    public int getTotalWeeks() {
        return buildSeasonWeeks;
    }
    
    /**
     * Gets priority weight based on competition level.
     */
    public double getCompetitionWeight() {
        return switch (competitionLevel) {
            case REGIONAL_ONLY -> 1.0;
            case DISTRICT_CHAMPIONSHIP -> 1.2;
            case WORLD_CHAMPIONSHIP -> 1.5;
            case OFFSEASON_COMPETITIVE -> 0.8;
            case DEMONSTRATION_OUTREACH -> 0.6;
        };
    }
    
    /**
     * Validates template configuration for consistency.
     */
    public boolean isValidConfiguration() {
        if (name == null || name.trim().isEmpty()) return false;
        if (templateType == null) return false;
        if (competitionLevel == null) return false;
        if (teamSize == null) return false;
        if (experienceLevel == null) return false;
        if (buildSeasonWeeks <= 0) return false;
        if (estimatedTotalHours <= 0) return false;
        
        return true;
    }
    
    /**
     * Creates a copy of this template for customization.
     */
    public BuildSeasonTemplate createCopy(String newName) {
        BuildSeasonTemplate copy = new BuildSeasonTemplate();
        copy.setName(newName);
        copy.setDescription(this.description);
        copy.setTemplateType(this.templateType);
        copy.setCompetitionLevel(this.competitionLevel);
        copy.setTeamSize(this.teamSize);
        copy.setExperienceLevel(this.experienceLevel);
        copy.setBuildSeasonWeeks(this.buildSeasonWeeks);
        copy.setEstimatedTotalHours(this.estimatedTotalHours);
        copy.setKeyObjectives(this.keyObjectives);
        copy.setCriticalSuccessFactors(this.criticalSuccessFactors);
        copy.setCommonRisks(this.commonRisks);
        copy.setResourceRequirements(this.resourceRequirements);
        copy.setRecommendedTools(this.recommendedTools);
        copy.setMentorRequirements(this.mentorRequirements);
        return copy;
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public TemplateType getTemplateType() { return templateType; }
    public void setTemplateType(TemplateType templateType) { this.templateType = templateType; }
    
    public CompetitionLevel getCompetitionLevel() { return competitionLevel; }
    public void setCompetitionLevel(CompetitionLevel competitionLevel) { this.competitionLevel = competitionLevel; }
    
    public TeamSize getTeamSize() { return teamSize; }
    public void setTeamSize(TeamSize teamSize) { this.teamSize = teamSize; }
    
    public ExperienceLevel getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(ExperienceLevel experienceLevel) { this.experienceLevel = experienceLevel; }
    
    public Integer getBuildSeasonWeeks() { return buildSeasonWeeks; }
    public void setBuildSeasonWeeks(Integer buildSeasonWeeks) { this.buildSeasonWeeks = buildSeasonWeeks; }
    
    public Integer getEstimatedTotalHours() { return estimatedTotalHours; }
    public void setEstimatedTotalHours(Integer estimatedTotalHours) { this.estimatedTotalHours = estimatedTotalHours; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }
    
    public List<BuildSeasonMilestone> getMilestones() { return milestones; }
    public void setMilestones(List<BuildSeasonMilestone> milestones) { this.milestones = milestones; }
    
    public List<BuildSeasonTaskTemplate> getTaskTemplates() { return taskTemplates; }
    public void setTaskTemplates(List<BuildSeasonTaskTemplate> taskTemplates) { this.taskTemplates = taskTemplates; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public TeamMember getCreatedBy() { return createdBy; }
    public void setCreatedBy(TeamMember createdBy) { this.createdBy = createdBy; }
    
    public TeamMember getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(TeamMember updatedBy) { this.updatedBy = updatedBy; }
    
    public String getKeyObjectives() { return keyObjectives; }
    public void setKeyObjectives(String keyObjectives) { this.keyObjectives = keyObjectives; }
    
    public String getCriticalSuccessFactors() { return criticalSuccessFactors; }
    public void setCriticalSuccessFactors(String criticalSuccessFactors) { this.criticalSuccessFactors = criticalSuccessFactors; }
    
    public String getCommonRisks() { return commonRisks; }
    public void setCommonRisks(String commonRisks) { this.commonRisks = commonRisks; }
    
    public String getResourceRequirements() { return resourceRequirements; }
    public void setResourceRequirements(String resourceRequirements) { this.resourceRequirements = resourceRequirements; }
    
    public String getRecommendedTools() { return recommendedTools; }
    public void setRecommendedTools(String recommendedTools) { this.recommendedTools = recommendedTools; }
    
    public String getMentorRequirements() { return mentorRequirements; }
    public void setMentorRequirements(String mentorRequirements) { this.mentorRequirements = mentorRequirements; }
    
    @Override
    public String toString() {
        return String.format("BuildSeasonTemplate{id=%d, name='%s', type=%s, level=%s, size=%s, experience=%s}", 
                           id, name, templateType, competitionLevel, teamSize, experienceLevel);
    }
}