// src/main/java/org/frcpm/services/ProjectTemplateService.java
// Phase 4A: Service Interface for Project Templates

package org.frcpm.services;

import org.frcpm.models.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for managing robot subsystem project templates.
 * 
 * Provides operations for creating, managing, and applying project templates
 * to accelerate robot development workflows.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-4A
 * @since Phase 4A - Robot Build Season Optimization
 */
public interface ProjectTemplateService {
    
    // =========================================================================
    // BASIC CRUD OPERATIONS
    // =========================================================================
    
    /**
     * Create a new project template.
     */
    ProjectTemplate createTemplate(ProjectTemplate template);
    
    /**
     * Update an existing project template.
     */
    ProjectTemplate updateTemplate(ProjectTemplate template);
    
    /**
     * Find project template by ID.
     */
    Optional<ProjectTemplate> findById(Long id);
    
    /**
     * Find all active project templates.
     */
    List<ProjectTemplate> findAllActive();
    
    /**
     * Deactivate a project template (soft delete).
     */
    void deactivateTemplate(Long templateId);
    
    /**
     * Activate a project template.
     */
    void activateTemplate(Long templateId);
    
    // =========================================================================
    // TEMPLATE DISCOVERY AND SEARCH
    // =========================================================================
    
    /**
     * Find templates by subsystem type.
     */
    List<ProjectTemplate> findBySubsystemType(SubsystemType subsystemType);
    
    /**
     * Find templates by difficulty level.
     */
    List<ProjectTemplate> findByDifficultyLevel(ProjectTemplate.DifficultyLevel difficultyLevel);
    
    /**
     * Search templates by multiple criteria.
     */
    List<ProjectTemplate> searchTemplates(TemplateSearchCriteria criteria);
    
    /**
     * Find most popular templates (by usage count).
     */
    List<ProjectTemplate> findMostPopular(int limit);
    
    /**
     * Find recently used templates.
     */
    List<ProjectTemplate> findRecentlyUsed(int limit);
    
    /**
     * Find templates suitable for beginner teams.
     */
    List<ProjectTemplate> findBeginnerFriendly();
    
    /**
     * Find templates that can be developed in parallel.
     */
    List<ProjectTemplate> findParallelDevelopmentTemplates();
    
    /**
     * Find templates without dependencies.
     */
    List<ProjectTemplate> findIndependentTemplates();
    
    // =========================================================================
    // TEMPLATE APPLICATION
    // =========================================================================
    
    /**
     * Create a new project from a template.
     */
    Project createProjectFromTemplate(Long templateId, String projectName, String description);
    
    /**
     * Apply template to existing project.
     */
    void applyTemplateToProject(Long templateId, Long projectId);
    
    /**
     * Preview what will be created from a template.
     */
    TemplatePreview previewTemplate(Long templateId);
    
    /**
     * Get template recommendations for a project.
     */
    List<ProjectTemplate> getRecommendations(Project project);
    
    /**
     * Get template recommendations based on team profile.
     */
    List<ProjectTemplate> getRecommendationsForTeam(TeamProfile teamProfile);
    
    // =========================================================================
    // TEMPLATE MANAGEMENT
    // =========================================================================
    
    /**
     * Clone an existing template.
     */
    ProjectTemplate cloneTemplate(Long templateId, String newName);
    
    /**
     * Create template from existing project.
     */
    ProjectTemplate createTemplateFromProject(Long projectId, String templateName);
    
    /**
     * Update template usage statistics.
     */
    void recordTemplateUsage(Long templateId);
    
    /**
     * Update template success metrics.
     */
    void updateTemplateMetrics(Long templateId, double completionRate, double actualWeeks, double rating);
    
    // =========================================================================
    // TEMPLATE ANALYTICS
    // =========================================================================
    
    /**
     * Get template usage statistics.
     */
    Map<String, Object> getTemplateUsageStatistics();
    
    /**
     * Get subsystem popularity statistics.
     */
    Map<SubsystemType, Integer> getSubsystemPopularity();
    
    /**
     * Get template performance metrics.
     */
    Map<String, Object> getTemplatePerformanceMetrics(Long templateId);
    
    /**
     * Generate template effectiveness report.
     */
    Map<String, Object> generateEffectivenessReport();
    
    // =========================================================================
    // BUILD SEASON OPTIMIZATION
    // =========================================================================
    
    /**
     * Get recommended build sequence for multiple templates.
     */
    List<ProjectTemplate> getOptimalBuildSequence(List<Long> templateIds);
    
    /**
     * Find critical path through multiple subsystem templates.
     */
    List<ProjectTemplate> getCriticalPathTemplates(List<Long> templateIds);
    
    /**
     * Get resource requirements summary for templates.
     */
    Map<String, Object> getResourceRequirementsSummary(List<Long> templateIds);
    
    /**
     * Estimate total project timeline from templates.
     */
    Map<String, Object> estimateProjectTimeline(List<Long> templateIds);
    
    /**
     * Get team capacity requirements for templates.
     */
    Map<String, Object> getTeamCapacityRequirements(List<Long> templateIds);
    
    // =========================================================================
    // VALIDATION AND QUALITY
    // =========================================================================
    
    /**
     * Validate template completeness and quality.
     */
    List<String> validateTemplate(Long templateId);
    
    /**
     * Check template compatibility with project constraints.
     */
    List<String> checkTemplateCompatibility(Long templateId, Project project);
    
    /**
     * Get template quality score.
     */
    double getTemplateQualityScore(Long templateId);
    
    // =========================================================================
    // BULK OPERATIONS
    // =========================================================================
    
    /**
     * Import templates from external source.
     */
    List<ProjectTemplate> importTemplates(List<Map<String, Object>> templateData);
    
    /**
     * Export templates to external format.
     */
    List<Map<String, Object>> exportTemplates(List<Long> templateIds);
    
    /**
     * Bulk update template properties.
     */
    void bulkUpdateTemplates(List<Long> templateIds, Map<String, Object> updates);
    
    /**
     * Bulk deactivate templates.
     */
    void bulkDeactivateTemplates(List<Long> templateIds);
    
    // =========================================================================
    // HELPER CLASSES
    // =========================================================================
    
    /**
     * Search criteria for template discovery.
     */
    class TemplateSearchCriteria {
        private SubsystemType subsystemType;
        private ProjectTemplate.DifficultyLevel difficultyLevel;
        private Integer maxWeeks;
        private Integer maxTeamSize;
        private String searchTerm;
        private Boolean parallelDevelopment;
        private Double minSuccessRating;
        
        // Constructors, getters, and setters
        public TemplateSearchCriteria() {}
        
        public SubsystemType getSubsystemType() { return subsystemType; }
        public void setSubsystemType(SubsystemType subsystemType) { this.subsystemType = subsystemType; }
        
        public ProjectTemplate.DifficultyLevel getDifficultyLevel() { return difficultyLevel; }
        public void setDifficultyLevel(ProjectTemplate.DifficultyLevel difficultyLevel) { this.difficultyLevel = difficultyLevel; }
        
        public Integer getMaxWeeks() { return maxWeeks; }
        public void setMaxWeeks(Integer maxWeeks) { this.maxWeeks = maxWeeks; }
        
        public Integer getMaxTeamSize() { return maxTeamSize; }
        public void setMaxTeamSize(Integer maxTeamSize) { this.maxTeamSize = maxTeamSize; }
        
        public String getSearchTerm() { return searchTerm; }
        public void setSearchTerm(String searchTerm) { this.searchTerm = searchTerm; }
        
        public Boolean getParallelDevelopment() { return parallelDevelopment; }
        public void setParallelDevelopment(Boolean parallelDevelopment) { this.parallelDevelopment = parallelDevelopment; }
        
        public Double getMinSuccessRating() { return minSuccessRating; }
        public void setMinSuccessRating(Double minSuccessRating) { this.minSuccessRating = minSuccessRating; }
    }
    
    /**
     * Preview of what will be created from a template.
     */
    class TemplatePreview {
        private ProjectTemplate template;
        private int totalTasks;
        private int totalMilestones;
        private double estimatedHours;
        private double estimatedCost;
        private List<String> requiredSkills;
        private List<String> dependencies;
        
        // Constructors, getters, and setters
        public TemplatePreview(ProjectTemplate template) {
            this.template = template;
        }
        
        public ProjectTemplate getTemplate() { return template; }
        public void setTemplate(ProjectTemplate template) { this.template = template; }
        
        public int getTotalTasks() { return totalTasks; }
        public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }
        
        public int getTotalMilestones() { return totalMilestones; }
        public void setTotalMilestones(int totalMilestones) { this.totalMilestones = totalMilestones; }
        
        public double getEstimatedHours() { return estimatedHours; }
        public void setEstimatedHours(double estimatedHours) { this.estimatedHours = estimatedHours; }
        
        public double getEstimatedCost() { return estimatedCost; }
        public void setEstimatedCost(double estimatedCost) { this.estimatedCost = estimatedCost; }
        
        public List<String> getRequiredSkills() { return requiredSkills; }
        public void setRequiredSkills(List<String> requiredSkills) { this.requiredSkills = requiredSkills; }
        
        public List<String> getDependencies() { return dependencies; }
        public void setDependencies(List<String> dependencies) { this.dependencies = dependencies; }
    }
    
    /**
     * Team profile for template recommendations.
     */
    class TeamProfile {
        private int teamSize;
        private ProjectTemplate.DifficultyLevel experienceLevel;
        private List<String> availableSkills;
        private int availableWeeks;
        private double budget;
        private boolean isRookieTeam;
        
        // Constructors, getters, and setters
        public TeamProfile() {}
        
        public int getTeamSize() { return teamSize; }
        public void setTeamSize(int teamSize) { this.teamSize = teamSize; }
        
        public ProjectTemplate.DifficultyLevel getExperienceLevel() { return experienceLevel; }
        public void setExperienceLevel(ProjectTemplate.DifficultyLevel experienceLevel) { this.experienceLevel = experienceLevel; }
        
        public List<String> getAvailableSkills() { return availableSkills; }
        public void setAvailableSkills(List<String> availableSkills) { this.availableSkills = availableSkills; }
        
        public int getAvailableWeeks() { return availableWeeks; }
        public void setAvailableWeeks(int availableWeeks) { this.availableWeeks = availableWeeks; }
        
        public double getBudget() { return budget; }
        public void setBudget(double budget) { this.budget = budget; }
        
        public boolean isRookieTeam() { return isRookieTeam; }
        public void setRookieTeam(boolean rookieTeam) { isRookieTeam = rookieTeam; }
    }
}