// src/main/java/org/frcpm/repositories/spring/BuildSeasonTemplateRepository.java

package org.frcpm.repositories.spring;

import org.frcpm.models.BuildSeasonTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for BuildSeasonTemplate entities.
 * 
 * Provides database access methods for build season template management,
 * including queries for template matching and recommendation.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-BuildSeason
 * @since Phase 4A.4 Build Season Timeline Templates
 */
@Repository
public interface BuildSeasonTemplateRepository extends JpaRepository<BuildSeasonTemplate, Long> {
    
    // =========================================================================
    // BASIC QUERIES
    // =========================================================================
    
    /**
     * Finds all active build season templates.
     */
    List<BuildSeasonTemplate> findByIsActiveTrueOrderByNameAsc();
    
    /**
     * Finds template by name (case-insensitive).
     */
    Optional<BuildSeasonTemplate> findByNameIgnoreCase(String name);
    
    /**
     * Finds templates by template type.
     */
    List<BuildSeasonTemplate> findByTemplateTypeAndIsActiveTrueOrderByNameAsc(BuildSeasonTemplate.TemplateType templateType);
    
    /**
     * Finds templates by competition level.
     */
    List<BuildSeasonTemplate> findByCompetitionLevelAndIsActiveTrueOrderByNameAsc(BuildSeasonTemplate.CompetitionLevel competitionLevel);
    
    /**
     * Finds templates by team size.
     */
    List<BuildSeasonTemplate> findByTeamSizeAndIsActiveTrueOrderByNameAsc(BuildSeasonTemplate.TeamSize teamSize);
    
    /**
     * Finds templates by experience level.
     */
    List<BuildSeasonTemplate> findByExperienceLevelAndIsActiveTrueOrderByNameAsc(BuildSeasonTemplate.ExperienceLevel experienceLevel);
    
    /**
     * Finds default templates.
     */
    List<BuildSeasonTemplate> findByIsDefaultTrueAndIsActiveTrueOrderByNameAsc();
    
    // =========================================================================
    // TEMPLATE MATCHING QUERIES
    // =========================================================================
    
    /**
     * Finds templates matching team characteristics.
     */
    @Query("SELECT t FROM BuildSeasonTemplate t " +
           "WHERE t.isActive = true " +
           "AND t.teamSize = :teamSize " +
           "AND t.experienceLevel = :experienceLevel " +
           "ORDER BY t.name ASC")
    List<BuildSeasonTemplate> findMatchingTemplates(@Param("teamSize") BuildSeasonTemplate.TeamSize teamSize,
                                                   @Param("experienceLevel") BuildSeasonTemplate.ExperienceLevel experienceLevel);
    
    /**
     * Finds templates suitable for a specific competition level and team configuration.
     */
    @Query("SELECT t FROM BuildSeasonTemplate t " +
           "WHERE t.isActive = true " +
           "AND t.competitionLevel = :competitionLevel " +
           "AND t.teamSize = :teamSize " +
           "AND t.experienceLevel = :experienceLevel " +
           "ORDER BY t.templateType ASC")
    List<BuildSeasonTemplate> findTemplatesForTeamConfiguration(@Param("competitionLevel") BuildSeasonTemplate.CompetitionLevel competitionLevel,
                                                               @Param("teamSize") BuildSeasonTemplate.TeamSize teamSize,
                                                               @Param("experienceLevel") BuildSeasonTemplate.ExperienceLevel experienceLevel);
    
    /**
     * Finds recommended templates based on multiple criteria.
     */
    @Query("SELECT t FROM BuildSeasonTemplate t " +
           "WHERE t.isActive = true " +
           "AND (t.isDefault = true OR " +
           "     (t.teamSize = :teamSize AND t.experienceLevel = :experienceLevel)) " +
           "ORDER BY t.isDefault DESC, t.name ASC")
    List<BuildSeasonTemplate> findRecommendedTemplates(@Param("teamSize") BuildSeasonTemplate.TeamSize teamSize,
                                                      @Param("experienceLevel") BuildSeasonTemplate.ExperienceLevel experienceLevel);
    
    // =========================================================================
    // SEARCH AND FILTERING
    // =========================================================================
    
    /**
     * Searches templates by name or description.
     */
    @Query("SELECT t FROM BuildSeasonTemplate t " +
           "WHERE t.isActive = true " +
           "AND (LOWER(t.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "     OR LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY t.name ASC")
    List<BuildSeasonTemplate> searchTemplates(@Param("searchTerm") String searchTerm);
    
    /**
     * Finds templates with specific build season duration.
     */
    List<BuildSeasonTemplate> findByBuildSeasonWeeksAndIsActiveTrueOrderByNameAsc(Integer buildSeasonWeeks);
    
    /**
     * Finds templates within a total hours range.
     */
    @Query("SELECT t FROM BuildSeasonTemplate t " +
           "WHERE t.isActive = true " +
           "AND t.estimatedTotalHours BETWEEN :minHours AND :maxHours " +
           "ORDER BY t.estimatedTotalHours ASC")
    List<BuildSeasonTemplate> findByHoursRange(@Param("minHours") Integer minHours,
                                              @Param("maxHours") Integer maxHours);
    
    // =========================================================================
    // ANALYTICS AND STATISTICS
    // =========================================================================
    
    /**
     * Gets template usage statistics by type.
     */
    @Query("SELECT t.templateType, COUNT(t) FROM BuildSeasonTemplate t " +
           "WHERE t.isActive = true " +
           "GROUP BY t.templateType " +
           "ORDER BY COUNT(t) DESC")
    List<Object[]> getTemplateTypeStatistics();
    
    /**
     * Gets template distribution by competition level.
     */
    @Query("SELECT t.competitionLevel, COUNT(t) FROM BuildSeasonTemplate t " +
           "WHERE t.isActive = true " +
           "GROUP BY t.competitionLevel " +
           "ORDER BY COUNT(t) DESC")
    List<Object[]> getCompetitionLevelDistribution();
    
    /**
     * Gets average estimated hours by experience level.
     */
    @Query("SELECT t.experienceLevel, AVG(t.estimatedTotalHours), COUNT(t) FROM BuildSeasonTemplate t " +
           "WHERE t.isActive = true " +
           "GROUP BY t.experienceLevel " +
           "ORDER BY AVG(t.estimatedTotalHours) DESC")
    List<Object[]> getAverageHoursByExperience();
    
    /**
     * Finds most popular templates (could be based on usage metrics if tracked).
     */
    @Query("SELECT t FROM BuildSeasonTemplate t " +
           "WHERE t.isActive = true " +
           "ORDER BY t.isDefault DESC, t.name ASC")
    List<BuildSeasonTemplate> findMostPopularTemplates();
    
    // =========================================================================
    // TEMPLATE VALIDATION AND MANAGEMENT
    // =========================================================================
    
    /**
     * Counts active templates by template type.
     */
    @Query("SELECT COUNT(t) FROM BuildSeasonTemplate t " +
           "WHERE t.isActive = true " +
           "AND t.templateType = :templateType")
    long countByTemplateType(@Param("templateType") BuildSeasonTemplate.TemplateType templateType);
    
    /**
     * Finds templates created by a specific team member.
     */
    @Query("SELECT t FROM BuildSeasonTemplate t " +
           "WHERE t.createdBy.id = :createdById " +
           "AND t.isActive = true " +
           "ORDER BY t.updatedAt DESC")
    List<BuildSeasonTemplate> findByCreatedBy(@Param("createdById") Long createdById);
    
    /**
     * Finds templates that need validation (missing key information).
     */
    @Query("SELECT t FROM BuildSeasonTemplate t " +
           "WHERE t.isActive = true " +
           "AND (t.keyObjectives IS NULL OR t.keyObjectives = '' " +
           "     OR t.criticalSuccessFactors IS NULL OR t.criticalSuccessFactors = '' " +
           "     OR t.estimatedTotalHours IS NULL OR t.estimatedTotalHours = 0) " +
           "ORDER BY t.updatedAt ASC")
    List<BuildSeasonTemplate> findTemplatesNeedingValidation();
    
    /**
     * Finds complete templates with all metadata filled.
     */
    @Query("SELECT t FROM BuildSeasonTemplate t " +
           "WHERE t.isActive = true " +
           "AND t.keyObjectives IS NOT NULL AND t.keyObjectives != '' " +
           "AND t.criticalSuccessFactors IS NOT NULL AND t.criticalSuccessFactors != '' " +
           "AND t.commonRisks IS NOT NULL AND t.commonRisks != '' " +
           "AND t.resourceRequirements IS NOT NULL AND t.resourceRequirements != '' " +
           "AND t.estimatedTotalHours > 0 " +
           "ORDER BY t.name ASC")
    List<BuildSeasonTemplate> findCompleteTemplates();
    
    // =========================================================================
    // BULK OPERATIONS AND MAINTENANCE
    // =========================================================================
    
    /**
     * Deactivates templates by IDs.
     */
    @Query("UPDATE BuildSeasonTemplate t SET t.isActive = false " +
           "WHERE t.id IN :templateIds")
    int deactivateTemplates(@Param("templateIds") List<Long> templateIds);
    
    /**
     * Reactivates templates by IDs.
     */
    @Query("UPDATE BuildSeasonTemplate t SET t.isActive = true " +
           "WHERE t.id IN :templateIds")
    int reactivateTemplates(@Param("templateIds") List<Long> templateIds);
    
    /**
     * Updates default status for templates.
     */
    @Query("UPDATE BuildSeasonTemplate t SET t.isDefault = :isDefault " +
           "WHERE t.id IN :templateIds")
    int updateDefaultStatus(@Param("templateIds") List<Long> templateIds, 
                           @Param("isDefault") Boolean isDefault);
}