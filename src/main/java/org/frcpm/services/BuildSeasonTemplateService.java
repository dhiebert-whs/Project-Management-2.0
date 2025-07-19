// src/main/java/org/frcpm/services/BuildSeasonTemplateService.java

package org.frcpm.services;

import org.frcpm.models.BuildSeasonTemplate;
import org.frcpm.models.BuildSeasonMilestone;
import org.frcpm.models.BuildSeasonTaskTemplate;
import org.frcpm.models.Project;
import org.frcpm.models.TeamMember;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for managing build season templates.
 * 
 * Provides comprehensive build season template management, including template
 * creation, customization, application to projects, and team recommendation.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-BuildSeason
 * @since Phase 4A.4 Build Season Timeline Templates
 */
public interface BuildSeasonTemplateService extends Service<BuildSeasonTemplate, Long> {
    
    // =========================================================================
    // TEMPLATE MANAGEMENT
    // =========================================================================
    
    /**
     * Creates a new build season template.
     * 
     * @param template the template to create
     * @return the created template
     * @throws IllegalArgumentException if template is invalid
     */
    BuildSeasonTemplate createTemplate(BuildSeasonTemplate template);
    
    /**
     * Creates a template with basic information.
     * 
     * @param name the template name
     * @param description the template description
     * @param templateType the template type
     * @param competitionLevel the competition level
     * @param teamSize the team size category
     * @param experienceLevel the team experience level
     * @return the created template
     */
    BuildSeasonTemplate createTemplate(String name, String description,
                                     BuildSeasonTemplate.TemplateType templateType,
                                     BuildSeasonTemplate.CompetitionLevel competitionLevel,
                                     BuildSeasonTemplate.TeamSize teamSize,
                                     BuildSeasonTemplate.ExperienceLevel experienceLevel);
    
    /**
     * Updates an existing build season template.
     * 
     * @param templateId the template ID
     * @param template the updated template data
     * @return the updated template
     */
    BuildSeasonTemplate updateTemplate(Long templateId, BuildSeasonTemplate template);
    
    /**
     * Creates a copy of an existing template.
     * 
     * @param templateId the source template ID
     * @param newName the name for the new template
     * @return the copied template
     */
    BuildSeasonTemplate copyTemplate(Long templateId, String newName);
    
    /**
     * Activates a build season template.
     * 
     * @param templateId the template ID
     * @return the activated template
     */
    BuildSeasonTemplate activateTemplate(Long templateId);
    
    /**
     * Deactivates a build season template.
     * 
     * @param templateId the template ID
     * @return the deactivated template
     */
    BuildSeasonTemplate deactivateTemplate(Long templateId);
    
    /**
     * Sets a template as default for its category.
     * 
     * @param templateId the template ID
     * @return the updated template
     */
    BuildSeasonTemplate setAsDefault(Long templateId);
    
    // =========================================================================
    // TEMPLATE DISCOVERY AND RECOMMENDATION
    // =========================================================================
    
    /**
     * Finds all active build season templates.
     * 
     * @return list of active templates
     */
    List<BuildSeasonTemplate> findActiveTemplates();
    
    /**
     * Finds templates by template type.
     * 
     * @param templateType the template type
     * @return list of matching templates
     */
    List<BuildSeasonTemplate> findByTemplateType(BuildSeasonTemplate.TemplateType templateType);
    
    /**
     * Finds templates by competition level.
     * 
     * @param competitionLevel the competition level
     * @return list of matching templates
     */
    List<BuildSeasonTemplate> findByCompetitionLevel(BuildSeasonTemplate.CompetitionLevel competitionLevel);
    
    /**
     * Finds templates suitable for a team configuration.
     * 
     * @param teamSize the team size category
     * @param experienceLevel the team experience level
     * @return list of suitable templates
     */
    List<BuildSeasonTemplate> findSuitableTemplates(BuildSeasonTemplate.TeamSize teamSize,
                                                   BuildSeasonTemplate.ExperienceLevel experienceLevel);
    
    /**
     * Recommends templates for a team based on their characteristics.
     * 
     * @param memberCount the number of team members
     * @param yearsExperience the team's years of experience
     * @param targetingChampionship whether the team is targeting championship
     * @return list of recommended templates
     */
    List<BuildSeasonTemplate> recommendTemplatesForTeam(int memberCount, int yearsExperience, 
                                                       boolean targetingChampionship);
    
    /**
     * Gets the best matching template for a team.
     * 
     * @param memberCount the number of team members
     * @param yearsExperience the team's years of experience
     * @param targetingChampionship whether the team is targeting championship
     * @return the best matching template, if any
     */
    Optional<BuildSeasonTemplate> getBestMatchingTemplate(int memberCount, int yearsExperience, 
                                                        boolean targetingChampionship);
    
    /**
     * Gets default templates for each category.
     * 
     * @return list of default templates
     */
    List<BuildSeasonTemplate> getDefaultTemplates();
    
    // =========================================================================
    // TEMPLATE CONTENT MANAGEMENT
    // =========================================================================
    
    /**
     * Adds a milestone to a build season template.
     * 
     * @param templateId the template ID
     * @param milestone the milestone to add
     * @return the added milestone
     */
    BuildSeasonMilestone addMilestone(Long templateId, BuildSeasonMilestone milestone);
    
    /**
     * Updates a milestone in a template.
     * 
     * @param milestoneId the milestone ID
     * @param milestone the updated milestone data
     * @return the updated milestone
     */
    BuildSeasonMilestone updateMilestone(Long milestoneId, BuildSeasonMilestone milestone);
    
    /**
     * Removes a milestone from a template.
     * 
     * @param milestoneId the milestone ID
     * @return true if the milestone was removed
     */
    boolean removeMilestone(Long milestoneId);
    
    /**
     * Gets all milestones for a template.
     * 
     * @param templateId the template ID
     * @return list of milestones ordered by timeline
     */
    List<BuildSeasonMilestone> getTemplateMilestones(Long templateId);
    
    /**
     * Adds a task template to a build season template.
     * 
     * @param templateId the template ID
     * @param taskTemplate the task template to add
     * @return the added task template
     */
    BuildSeasonTaskTemplate addTaskTemplate(Long templateId, BuildSeasonTaskTemplate taskTemplate);
    
    /**
     * Updates a task template in a template.
     * 
     * @param taskTemplateId the task template ID
     * @param taskTemplate the updated task template data
     * @return the updated task template
     */
    BuildSeasonTaskTemplate updateTaskTemplate(Long taskTemplateId, BuildSeasonTaskTemplate taskTemplate);
    
    /**
     * Removes a task template from a template.
     * 
     * @param taskTemplateId the task template ID
     * @return true if the task template was removed
     */
    boolean removeTaskTemplate(Long taskTemplateId);
    
    /**
     * Gets all task templates for a template.
     * 
     * @param templateId the template ID
     * @return list of task templates ordered by timeline and category
     */
    List<BuildSeasonTaskTemplate> getTemplateTaskTemplates(Long templateId);
    
    // =========================================================================
    // PROJECT APPLICATION
    // =========================================================================
    
    /**
     * Applies a build season template to create a new project.
     * 
     * @param templateId the template ID
     * @param projectName the project name
     * @param buildSeasonStart the build season start date
     * @param createdBy the team member creating the project
     * @return the created project with tasks and milestones
     */
    Project applyTemplateToProject(Long templateId, String projectName, LocalDate buildSeasonStart, 
                                 TeamMember createdBy);
    
    /**
     * Applies template tasks to an existing project.
     * 
     * @param templateId the template ID
     * @param projectId the existing project ID
     * @param buildSeasonStart the build season start date
     * @return list of created tasks
     */
    List<org.frcpm.models.Task> applyTemplateTasksToProject(Long templateId, Long projectId, 
                                                           LocalDate buildSeasonStart);
    
    /**
     * Applies template milestones to an existing project.
     * 
     * @param templateId the template ID
     * @param projectId the existing project ID
     * @param buildSeasonStart the build season start date
     * @return list of created milestones
     */
    List<org.frcpm.models.Milestone> applyTemplateMilestonesToProject(Long templateId, Long projectId, 
                                                                     LocalDate buildSeasonStart);
    
    /**
     * Customizes template application based on team characteristics.
     * 
     * @param templateId the template ID
     * @param projectName the project name
     * @param buildSeasonStart the build season start date
     * @param createdBy the team member creating the project
     * @param teamMemberCount the actual team size
     * @param teamExperience the team's experience level
     * @return the created project with customized timeline
     */
    Project applyCustomizedTemplate(Long templateId, String projectName, LocalDate buildSeasonStart,
                                  TeamMember createdBy, int teamMemberCount, 
                                  BuildSeasonTemplate.ExperienceLevel teamExperience);
    
    // =========================================================================
    // TEMPLATE ANALYSIS AND OPTIMIZATION
    // =========================================================================
    
    /**
     * Analyzes template timeline feasibility.
     * 
     * @param templateId the template ID
     * @param buildSeasonStart the proposed start date
     * @param teamSize the team size
     * @param experienceLevel the team experience level
     * @return analysis results with recommendations
     */
    Map<String, Object> analyzeTemplateFeasibility(Long templateId, LocalDate buildSeasonStart,
                                                  int teamSize, BuildSeasonTemplate.ExperienceLevel experienceLevel);
    
    /**
     * Gets template statistics and metrics.
     * 
     * @param templateId the template ID
     * @return template statistics
     */
    Map<String, Object> getTemplateStatistics(Long templateId);
    
    /**
     * Optimizes template timeline based on constraints.
     * 
     * @param templateId the template ID
     * @param constraints optimization constraints
     * @return optimization suggestions
     */
    Map<String, Object> optimizeTemplate(Long templateId, Map<String, Object> constraints);
    
    /**
     * Validates template completeness and consistency.
     * 
     * @param templateId the template ID
     * @return validation results with any issues found
     */
    Map<String, Object> validateTemplate(Long templateId);
    
    /**
     * Compares multiple templates for selection.
     * 
     * @param templateIds the list of template IDs to compare
     * @return comparison analysis
     */
    Map<String, Object> compareTemplates(List<Long> templateIds);
    
    // =========================================================================
    // SEARCH AND FILTERING
    // =========================================================================
    
    /**
     * Searches templates by name or description.
     * 
     * @param searchTerm the search term
     * @return list of matching templates
     */
    List<BuildSeasonTemplate> searchTemplates(String searchTerm);
    
    /**
     * Finds templates by build season duration.
     * 
     * @param buildSeasonWeeks the build season duration in weeks
     * @return list of matching templates
     */
    List<BuildSeasonTemplate> findByDuration(int buildSeasonWeeks);
    
    /**
     * Finds templates within a total hours range.
     * 
     * @param minHours the minimum total hours
     * @param maxHours the maximum total hours
     * @return list of matching templates
     */
    List<BuildSeasonTemplate> findByHoursRange(int minHours, int maxHours);
    
    /**
     * Advanced template search with multiple criteria.
     * 
     * @param criteria search criteria map
     * @return list of matching templates
     */
    List<BuildSeasonTemplate> advancedSearch(Map<String, Object> criteria);
    
    // =========================================================================
    // BULK OPERATIONS
    // =========================================================================
    
    /**
     * Creates multiple templates from definitions.
     * 
     * @param templates the list of templates to create
     * @return list of created templates
     */
    List<BuildSeasonTemplate> createBulkTemplates(List<BuildSeasonTemplate> templates);
    
    /**
     * Updates multiple templates.
     * 
     * @param templateUpdates map of template IDs to updated data
     * @return list of updated templates
     */
    List<BuildSeasonTemplate> updateBulkTemplates(Map<Long, BuildSeasonTemplate> templateUpdates);
    
    /**
     * Activates multiple templates.
     * 
     * @param templateIds the list of template IDs to activate
     * @return number of templates activated
     */
    int activateBulkTemplates(List<Long> templateIds);
    
    /**
     * Deactivates multiple templates.
     * 
     * @param templateIds the list of template IDs to deactivate
     * @return number of templates deactivated
     */
    int deactivateBulkTemplates(List<Long> templateIds);
    
    // =========================================================================
    // ANALYTICS AND REPORTING
    // =========================================================================
    
    /**
     * Gets template usage analytics.
     * 
     * @return template usage statistics
     */
    Map<String, Object> getTemplateAnalytics();
    
    /**
     * Gets template type distribution.
     * 
     * @return distribution of templates by type
     */
    Map<BuildSeasonTemplate.TemplateType, Long> getTemplateTypeDistribution();
    
    /**
     * Gets competition level distribution.
     * 
     * @return distribution of templates by competition level
     */
    Map<BuildSeasonTemplate.CompetitionLevel, Long> getCompetitionLevelDistribution();
    
    /**
     * Gets average template characteristics by experience level.
     * 
     * @return average template metrics by experience level
     */
    Map<BuildSeasonTemplate.ExperienceLevel, Map<String, Object>> getTemplateCharacteristicsByExperience();
    
    /**
     * Finds templates that need validation or improvement.
     * 
     * @return list of templates requiring attention
     */
    List<BuildSeasonTemplate> findTemplatesNeedingAttention();
    
    // =========================================================================
    // STANDARD TEMPLATE CREATION
    // =========================================================================
    
    /**
     * Creates standard FRC build season templates.
     * 
     * @return list of created standard templates
     */
    List<BuildSeasonTemplate> createStandardTemplates();
    
    /**
     * Creates a rookie team template with appropriate timeline and tasks.
     * 
     * @return the created rookie template
     */
    BuildSeasonTemplate createRookieTeamTemplate();
    
    /**
     * Creates a veteran team template optimized for experienced teams.
     * 
     * @return the created veteran template
     */
    BuildSeasonTemplate createVeteranTeamTemplate();
    
    /**
     * Creates a championship-focused template for elite teams.
     * 
     * @return the created championship template
     */
    BuildSeasonTemplate createChampionshipTemplate();
    
    /**
     * Initializes default templates for first-time system setup.
     * 
     * @return list of initialized default templates
     */
    List<BuildSeasonTemplate> initializeDefaultTemplates();
}