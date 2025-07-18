// src/main/java/org/frcpm/repositories/spring/ProjectTemplateRepository.java
// Phase 4A: Repository for Project Templates

package org.frcpm.repositories.spring;

import org.frcpm.models.ProjectTemplate;
import org.frcpm.models.SubsystemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for ProjectTemplate entities.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-4A
 * @since Phase 4A - Robot Build Season Optimization
 */
@Repository
public interface ProjectTemplateRepository extends JpaRepository<ProjectTemplate, Long> {
    
    /**
     * Find all active project templates.
     */
    List<ProjectTemplate> findByIsActiveTrue();
    
    /**
     * Find templates by subsystem type.
     */
    List<ProjectTemplate> findBySubsystemTypeAndIsActiveTrue(SubsystemType subsystemType);
    
    /**
     * Find templates by difficulty level.
     */
    List<ProjectTemplate> findByDifficultyLevelAndIsActiveTrue(ProjectTemplate.DifficultyLevel difficultyLevel);
    
    /**
     * Find templates by name containing search term.
     */
    List<ProjectTemplate> findByNameContainingIgnoreCaseAndIsActiveTrue(String searchTerm);
    
    /**
     * Find templates ordered by usage count (most popular first).
     */
    // @Query("SELECT pt FROM ProjectTemplate pt WHERE pt.isActive = true ORDER BY pt.usageCount DESC")
    // List<ProjectTemplate> findMostPopularTemplates();
    
    /**
     * Find templates ordered by build priority.
     */
    // @Query("SELECT pt FROM ProjectTemplate pt WHERE pt.isActive = true ORDER BY pt.buildPriority ASC")
    // List<ProjectTemplate> findByBuildPriorityOrder();
    
    /**
     * Find templates suitable for parallel development.
     */
    List<ProjectTemplate> findByParallelDevelopmentTrueAndIsActiveTrue();
    
    /**
     * Find templates by estimated duration range.
     */
    // @Query("SELECT pt FROM ProjectTemplate pt WHERE pt.isActive = true " +
    //        "AND pt.estimatedWeeks >= :minWeeks AND pt.estimatedWeeks <= :maxWeeks")
    // List<ProjectTemplate> findByEstimatedWeeksBetween(@Param("minWeeks") int minWeeks, 
    //                                                  @Param("maxWeeks") int maxWeeks);
    
    /**
     * Find templates by team size requirement.
     */
    // @Query("SELECT pt FROM ProjectTemplate pt WHERE pt.isActive = true " +
    //        "AND pt.teamSize <= :maxTeamSize")
    // List<ProjectTemplate> findByTeamSizeLessThanEqual(@Param("maxTeamSize") int maxTeamSize);
    
    /**
     * Find templates created by specific user.
     */
    List<ProjectTemplate> findByCreatedByAndIsActiveTrue(String createdBy);
    
    /**
     * Find templates with high success rating.
     */
    // @Query("SELECT pt FROM ProjectTemplate pt WHERE pt.isActive = true " +
    //        "AND pt.successRating IS NOT NULL AND pt.successRating >= :minRating " +
    //        "ORDER BY pt.successRating DESC")
    // List<ProjectTemplate> findHighRatedTemplates(@Param("minRating") double minRating);
    
    /**
     * Find templates without dependencies.
     * Simplified: Use LEFT JOIN to find templates with no dependencies
     */
    // @Query("SELECT pt FROM ProjectTemplate pt LEFT JOIN pt.dependencies d " +
    //        "WHERE pt.isActive = true AND d IS NULL")
    // List<ProjectTemplate> findIndependentTemplates();
    
    /**
     * Find templates that depend on a specific subsystem.
     * Simplified: Use JOIN to find templates with specific dependency
     */
    // @Query("SELECT DISTINCT pt FROM ProjectTemplate pt JOIN pt.dependencies d " +
    //        "WHERE pt.isActive = true AND d = :dependencyType")
    // List<ProjectTemplate> findTemplatesWithDependency(@Param("dependencyType") SubsystemType dependencyType);
    
    /**
     * Get template usage statistics.
     * Simplified: Split into separate queries to avoid complex aggregation
     */
    // @Query("SELECT pt.subsystemType, COUNT(pt) " +
    //        "FROM ProjectTemplate pt WHERE pt.isActive = true " +
    //        "GROUP BY pt.subsystemType")
    // List<Object[]> getUsageStatisticsBySubsystem();
    
    /**
     * Find recently used templates.
     */
    // @Query("SELECT pt FROM ProjectTemplate pt WHERE pt.isActive = true " +
    //        "AND pt.lastUsedAt IS NOT NULL " +
    //        "ORDER BY pt.lastUsedAt DESC")
    // List<ProjectTemplate> findRecentlyUsedTemplates();
    
    /**
     * Find templates for beginner teams.
     */
    // @Query("SELECT pt FROM ProjectTemplate pt WHERE pt.isActive = true " +
    //        "AND pt.difficultyLevel IN ('BEGINNER', 'INTERMEDIATE') " +
    //        "ORDER BY pt.difficultyLevel ASC, pt.usageCount DESC")
    // List<ProjectTemplate> findBeginnerFriendlyTemplates();
    
    /**
     * Search templates by multiple criteria.
     */
    // @Query("SELECT pt FROM ProjectTemplate pt WHERE pt.isActive = true " +
    //        "AND (:subsystemType IS NULL OR pt.subsystemType = :subsystemType) " +
    //        "AND (:difficultyLevel IS NULL OR pt.difficultyLevel = :difficultyLevel) " +
    //        "AND (:maxWeeks IS NULL OR pt.estimatedWeeks <= :maxWeeks) " +
    //        "AND (:maxTeamSize IS NULL OR pt.teamSize <= :maxTeamSize) " +
    //        "AND (:searchTerm IS NULL OR pt.name LIKE %:searchTerm% OR pt.description LIKE %:searchTerm%) " +
    //        "ORDER BY pt.usageCount DESC")
    // List<ProjectTemplate> searchTemplates(@Param("subsystemType") SubsystemType subsystemType,
    //                                     @Param("difficultyLevel") ProjectTemplate.DifficultyLevel difficultyLevel,
    //                                     @Param("maxWeeks") Integer maxWeeks,
    //                                     @Param("maxTeamSize") Integer maxTeamSize,
    //                                     @Param("searchTerm") String searchTerm);
}