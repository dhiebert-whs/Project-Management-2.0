// src/main/java/org/frcpm/repositories/spring/VideoTutorialSeriesRepository.java

package org.frcpm.repositories.spring;

import org.frcpm.models.VideoTutorialSeries;
import org.frcpm.models.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for VideoTutorialSeries entities.
 * 
 * Provides comprehensive data access for video tutorial series management
 * including course organization, learning path analytics, and educational
 * sequence management for FRC teams.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-KnowledgeManagement
 * @since Phase 4D.2 Video Tutorial Library Integration
 */
@Repository
public interface VideoTutorialSeriesRepository extends JpaRepository<VideoTutorialSeries, Long> {

    // =========================================================================
    // BASIC QUERIES
    // =========================================================================

    /**
     * Finds all active series for a specific team and season.
     */
    List<VideoTutorialSeries> findByTeamNumberAndSeasonAndIsActiveTrue(Integer teamNumber, Integer season);

    /**
     * Finds series by slug for a team and season.
     */
    Optional<VideoTutorialSeries> findByTeamNumberAndSeasonAndSlugAndIsActiveTrue(
        Integer teamNumber, Integer season, String slug);

    /**
     * Finds series by status for a team and season.
     */
    List<VideoTutorialSeries> findByTeamNumberAndSeasonAndStatusAndIsActiveTrue(
        Integer teamNumber, Integer season, VideoTutorialSeries.SeriesStatus status);

    /**
     * Finds series by type for a team and season.
     */
    List<VideoTutorialSeries> findByTeamNumberAndSeasonAndSeriesTypeAndIsActiveTrue(
        Integer teamNumber, Integer season, VideoTutorialSeries.SeriesType seriesType);

    /**
     * Finds series by category for a team and season.
     */
    List<VideoTutorialSeries> findByTeamNumberAndSeasonAndCategoryAndIsActiveTrue(
        Integer teamNumber, Integer season, VideoTutorialSeries.SeriesCategory category);

    /**
     * Finds series by target skill level.
     */
    List<VideoTutorialSeries> findByTeamNumberAndSeasonAndTargetSkillLevelAndIsActiveTrue(
        Integer teamNumber, Integer season, VideoTutorialSeries.SkillLevel targetSkillLevel);

    /**
     * Finds series by visibility level.
     */
    List<VideoTutorialSeries> findByTeamNumberAndSeasonAndVisibilityAndIsActiveTrue(
        Integer teamNumber, Integer season, VideoTutorialSeries.VisibilityLevel visibility);

    // =========================================================================
    // CONTENT SEARCH AND FILTERING
    // =========================================================================

    /**
     * Searches series by title (case-insensitive).
     */
    @Query("SELECT vts FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber AND vts.season = :season " +
           "AND LOWER(vts.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "AND vts.isActive = true AND vts.isSearchable = true")
    List<VideoTutorialSeries> searchByTitle(@Param("teamNumber") Integer teamNumber,
                                           @Param("season") Integer season,
                                           @Param("searchTerm") String searchTerm);

    /**
     * Searches series by description and summary (case-insensitive).
     */
    @Query("SELECT vts FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber AND vts.season = :season " +
           "AND (LOWER(vts.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(vts.summary) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND vts.isActive = true AND vts.isSearchable = true")
    List<VideoTutorialSeries> searchByContent(@Param("teamNumber") Integer teamNumber,
                                             @Param("season") Integer season,
                                             @Param("searchTerm") String searchTerm);

    /**
     * Full text search across title, description, and summary.
     */
    @Query("SELECT vts FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber AND vts.season = :season " +
           "AND (LOWER(vts.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(vts.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(vts.summary) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND vts.isActive = true AND vts.isSearchable = true " +
           "ORDER BY vts.searchRank DESC, vts.viewCount DESC")
    List<VideoTutorialSeries> fullTextSearch(@Param("teamNumber") Integer teamNumber,
                                            @Param("season") Integer season,
                                            @Param("searchTerm") String searchTerm);

    /**
     * Finds series by tag.
     */
    @Query("SELECT vts FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber AND vts.season = :season " +
           "AND :tag MEMBER OF vts.tags AND vts.isActive = true")
    List<VideoTutorialSeries> findByTag(@Param("teamNumber") Integer teamNumber,
                                       @Param("season") Integer season,
                                       @Param("tag") String tag);

    /**
     * Finds series by topic.
     */
    @Query("SELECT vts FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber AND vts.season = :season " +
           "AND :topic MEMBER OF vts.topics AND vts.isActive = true")
    List<VideoTutorialSeries> findByTopic(@Param("teamNumber") Integer teamNumber,
                                         @Param("season") Integer season,
                                         @Param("topic") String topic);

    /**
     * Finds series by skill covered.
     */
    @Query("SELECT vts FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber AND vts.season = :season " +
           "AND :skill MEMBER OF vts.skillsCovered AND vts.isActive = true")
    List<VideoTutorialSeries> findBySkillCovered(@Param("teamNumber") Integer teamNumber,
                                                 @Param("season") Integer season,
                                                 @Param("skill") String skill);

    /**
     * Finds series by completion time range.
     */
    @Query("SELECT vts FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber AND vts.season = :season " +
           "AND vts.estimatedCompletionHours BETWEEN :minHours AND :maxHours AND vts.isActive = true")
    List<VideoTutorialSeries> findByCompletionTimeRange(@Param("teamNumber") Integer teamNumber,
                                                        @Param("season") Integer season,
                                                        @Param("minHours") Integer minHours,
                                                        @Param("maxHours") Integer maxHours);

    // =========================================================================
    // ACCESS CONTROL AND PERMISSIONS
    // =========================================================================

    /**
     * Finds series viewable by specific access level.
     */
    List<VideoTutorialSeries> findByTeamNumberAndSeasonAndViewPermissionAndIsActiveTrue(
        Integer teamNumber, Integer season, VideoTutorialSeries.AccessLevel viewPermission);

    /**
     * Finds series where user is authorized viewer.
     */
    @Query("SELECT vts FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber AND vts.season = :season " +
           "AND :userId MEMBER OF vts.authorizedViewers AND vts.isActive = true")
    List<VideoTutorialSeries> findByAuthorizedViewer(@Param("teamNumber") Integer teamNumber,
                                                     @Param("season") Integer season,
                                                     @Param("userId") Long userId);

    /**
     * Finds public series.
     */
    @Query("SELECT vts FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber AND vts.season = :season " +
           "AND vts.visibility = 'PUBLIC' AND vts.isActive = true")
    List<VideoTutorialSeries> findPublicSeries(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    // =========================================================================
    // CREATOR AND INSTRUCTOR QUERIES
    // =========================================================================

    /**
     * Finds series created by specific user.
     */
    List<VideoTutorialSeries> findByCreatedByAndTeamNumberAndSeasonAndIsActiveTrue(
        TeamMember createdBy, Integer teamNumber, Integer season);

    /**
     * Finds series by primary instructor.
     */
    List<VideoTutorialSeries> findByPrimaryInstructorAndTeamNumberAndSeasonAndIsActiveTrue(
        TeamMember primaryInstructor, Integer teamNumber, Integer season);

    /**
     * Finds series by instructor.
     */
    @Query("SELECT vts FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber AND vts.season = :season " +
           "AND :instructorId MEMBER OF vts.instructors AND vts.isActive = true")
    List<VideoTutorialSeries> findByInstructor(@Param("teamNumber") Integer teamNumber,
                                              @Param("season") Integer season,
                                              @Param("instructorId") Long instructorId);

    /**
     * Finds series by contributor.
     */
    @Query("SELECT vts FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber AND vts.season = :season " +
           "AND :contributorId MEMBER OF vts.contributors AND vts.isActive = true")
    List<VideoTutorialSeries> findByContributor(@Param("teamNumber") Integer teamNumber,
                                               @Param("season") Integer season,
                                               @Param("contributorId") Long contributorId);

    /**
     * Finds series updated by specific user.
     */
    List<VideoTutorialSeries> findByUpdatedByAndTeamNumberAndSeasonAndIsActiveTrue(
        TeamMember updatedBy, Integer teamNumber, Integer season);

    // =========================================================================
    // ANALYTICS AND ENGAGEMENT
    // =========================================================================

    /**
     * Finds most viewed series.
     */
    @Query("SELECT vts FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber AND vts.season = :season " +
           "AND vts.isActive = true ORDER BY vts.viewCount DESC")
    List<VideoTutorialSeries> findMostViewed(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds most enrolled series.
     */
    @Query("SELECT vts FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber AND vts.season = :season " +
           "AND vts.isActive = true ORDER BY vts.enrollmentCount DESC")
    List<VideoTutorialSeries> findMostEnrolled(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds most completed series.
     */
    @Query("SELECT vts FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber AND vts.season = :season " +
           "AND vts.isActive = true ORDER BY vts.completionCount DESC")
    List<VideoTutorialSeries> findMostCompleted(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds highest rated series.
     */
    @Query("SELECT vts FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber AND vts.season = :season " +
           "AND vts.ratingCount >= :minRatingCount AND vts.isActive = true " +
           "ORDER BY vts.averageRating DESC")
    List<VideoTutorialSeries> findHighestRated(@Param("teamNumber") Integer teamNumber,
                                              @Param("season") Integer season,
                                              @Param("minRatingCount") Integer minRatingCount);

    /**
     * Finds series with high engagement.
     */
    @Query("SELECT vts FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber AND vts.season = :season " +
           "AND vts.engagementScore >= :minScore AND vts.isActive = true " +
           "ORDER BY vts.engagementScore DESC")
    List<VideoTutorialSeries> findHighEngagement(@Param("teamNumber") Integer teamNumber,
                                                 @Param("season") Integer season,
                                                 @Param("minScore") Double minScore);

    /**
     * Finds recently viewed series.
     */
    @Query("SELECT vts FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber AND vts.season = :season " +
           "AND vts.lastViewed IS NOT NULL AND vts.isActive = true " +
           "ORDER BY vts.lastViewed DESC")
    List<VideoTutorialSeries> findRecentlyViewed(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds series with low completion rates.
     */
    @Query("SELECT vts FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber AND vts.season = :season " +
           "AND vts.enrollmentCount > :minEnrollments AND vts.completionRate < :maxCompletionRate " +
           "AND vts.isActive = true ORDER BY vts.completionRate ASC")
    List<VideoTutorialSeries> findLowCompletionRate(@Param("teamNumber") Integer teamNumber,
                                                    @Param("season") Integer season,
                                                    @Param("minEnrollments") Long minEnrollments,
                                                    @Param("maxCompletionRate") Double maxCompletionRate);

    // =========================================================================
    // FEATURED AND SPECIAL SERIES
    // =========================================================================

    /**
     * Finds featured series.
     */
    @Query("SELECT vts FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber AND vts.season = :season " +
           "AND vts.isFeatured = true AND vts.isActive = true " +
           "ORDER BY vts.featuredAt DESC")
    List<VideoTutorialSeries> findFeatured(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds series requiring sequential completion.
     */
    @Query("SELECT vts FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber AND vts.season = :season " +
           "AND vts.requiresSequentialCompletion = true AND vts.isActive = true")
    List<VideoTutorialSeries> findSequentialSeries(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds completed series (status = COMPLETED).
     */
    @Query("SELECT vts FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber AND vts.season = :season " +
           "AND vts.status = 'COMPLETED' AND vts.isActive = true")
    List<VideoTutorialSeries> findCompletedSeries(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds series in progress.
     */
    @Query("SELECT vts FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber AND vts.season = :season " +
           "AND vts.status = 'IN_PROGRESS' AND vts.isActive = true")
    List<VideoTutorialSeries> findInProgressSeries(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    // =========================================================================
    // LEARNING PATH AND PROGRESSION
    // =========================================================================

    /**
     * Finds beginner series.
     */
    @Query("SELECT vts FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber AND vts.season = :season " +
           "AND vts.targetSkillLevel = 'BEGINNER' AND vts.isActive = true " +
           "ORDER BY vts.engagementScore DESC")
    List<VideoTutorialSeries> findBeginnerSeries(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds intermediate series.
     */
    @Query("SELECT vts FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber AND vts.season = :season " +
           "AND vts.targetSkillLevel = 'INTERMEDIATE' AND vts.isActive = true " +
           "ORDER BY vts.engagementScore DESC")
    List<VideoTutorialSeries> findIntermediateSeries(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds advanced series.
     */
    @Query("SELECT vts FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber AND vts.season = :season " +
           "AND vts.targetSkillLevel = 'ADVANCED' AND vts.isActive = true " +
           "ORDER BY vts.engagementScore DESC")
    List<VideoTutorialSeries> findAdvancedSeries(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds series with specific tutorial count range.
     */
    @Query("SELECT vts FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber AND vts.season = :season " +
           "AND vts.tutorialCount BETWEEN :minCount AND :maxCount AND vts.isActive = true")
    List<VideoTutorialSeries> findByTutorialCountRange(@Param("teamNumber") Integer teamNumber,
                                                       @Param("season") Integer season,
                                                       @Param("minCount") Integer minCount,
                                                       @Param("maxCount") Integer maxCount);

    // =========================================================================
    // MODERATION AND QUALITY CONTROL
    // =========================================================================

    /**
     * Finds series pending moderation.
     */
    @Query("SELECT vts FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber AND vts.season = :season " +
           "AND vts.isModerated = false AND vts.status = 'UNDER_REVIEW' AND vts.isActive = true")
    List<VideoTutorialSeries> findPendingModeration(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds series by moderator.
     */
    List<VideoTutorialSeries> findByModeratedByAndTeamNumberAndSeasonAndIsActiveTrue(
        TeamMember moderatedBy, Integer teamNumber, Integer season);

    /**
     * Finds series by quality score range.
     */
    @Query("SELECT vts FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber AND vts.season = :season " +
           "AND vts.qualityScore BETWEEN :minScore AND :maxScore AND vts.isActive = true")
    List<VideoTutorialSeries> findByQualityScoreRange(@Param("teamNumber") Integer teamNumber,
                                                      @Param("season") Integer season,
                                                      @Param("minScore") Integer minScore,
                                                      @Param("maxScore") Integer maxScore);

    /**
     * Finds unmoderated series.
     */
    @Query("SELECT vts FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber AND vts.season = :season " +
           "AND vts.isModerated = false AND vts.isActive = true")
    List<VideoTutorialSeries> findUnmoderated(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds quality checked series.
     */
    @Query("SELECT vts FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber AND vts.season = :season " +
           "AND vts.isQualityChecked = true AND vts.isActive = true")
    List<VideoTutorialSeries> findQualityChecked(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    // =========================================================================
    // TIME-BASED QUERIES
    // =========================================================================

    /**
     * Finds series created within date range.
     */
    @Query("SELECT vts FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber AND vts.season = :season " +
           "AND vts.createdAt BETWEEN :startDate AND :endDate AND vts.isActive = true")
    List<VideoTutorialSeries> findCreatedInDateRange(@Param("teamNumber") Integer teamNumber,
                                                     @Param("season") Integer season,
                                                     @Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);

    /**
     * Finds series updated within date range.
     */
    @Query("SELECT vts FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber AND vts.season = :season " +
           "AND vts.updatedAt BETWEEN :startDate AND :endDate AND vts.isActive = true")
    List<VideoTutorialSeries> findUpdatedInDateRange(@Param("teamNumber") Integer teamNumber,
                                                     @Param("season") Integer season,
                                                     @Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);

    /**
     * Finds recently created series.
     */
    @Query("SELECT vts FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber AND vts.season = :season " +
           "AND vts.createdAt >= :since AND vts.isActive = true ORDER BY vts.createdAt DESC")
    List<VideoTutorialSeries> findRecentlyCreated(@Param("teamNumber") Integer teamNumber,
                                                  @Param("season") Integer season,
                                                  @Param("since") LocalDateTime since);

    // =========================================================================
    // STATISTICS AND ANALYTICS
    // =========================================================================

    /**
     * Counts series by category.
     */
    @Query("SELECT vts.category, COUNT(vts) FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber " +
           "AND vts.season = :season AND vts.isActive = true GROUP BY vts.category")
    List<Object[]> countByCategory(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Counts series by type.
     */
    @Query("SELECT vts.seriesType, COUNT(vts) FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber " +
           "AND vts.season = :season AND vts.isActive = true GROUP BY vts.seriesType")
    List<Object[]> countBySeriesType(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Counts series by skill level.
     */
    @Query("SELECT vts.targetSkillLevel, COUNT(vts) FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber " +
           "AND vts.season = :season AND vts.isActive = true GROUP BY vts.targetSkillLevel")
    List<Object[]> countBySkillLevel(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Counts series by status.
     */
    @Query("SELECT vts.status, COUNT(vts) FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber " +
           "AND vts.season = :season AND vts.isActive = true GROUP BY vts.status")
    List<Object[]> countByStatus(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Calculates total view count for all series.
     */
    @Query("SELECT SUM(vts.viewCount) FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber " +
           "AND vts.season = :season AND vts.isActive = true")
    Optional<Long> findTotalViewCount(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Calculates total enrollment count.
     */
    @Query("SELECT SUM(vts.enrollmentCount) FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber " +
           "AND vts.season = :season AND vts.isActive = true")
    Optional<Long> findTotalEnrollmentCount(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Calculates total completion count.
     */
    @Query("SELECT SUM(vts.completionCount) FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber " +
           "AND vts.season = :season AND vts.isActive = true")
    Optional<Long> findTotalCompletionCount(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Calculates average engagement score.
     */
    @Query("SELECT AVG(vts.engagementScore) FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber " +
           "AND vts.season = :season AND vts.isActive = true")
    Optional<Double> findAverageEngagementScore(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Calculates average rating.
     */
    @Query("SELECT AVG(vts.averageRating) FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber " +
           "AND vts.season = :season AND vts.ratingCount > 0 AND vts.isActive = true")
    Optional<Double> findAverageRating(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds most common tags.
     */
    @Query("SELECT tag, COUNT(tag) FROM VideoTutorialSeries vts JOIN vts.tags tag " +
           "WHERE vts.teamNumber = :teamNumber AND vts.season = :season AND vts.isActive = true " +
           "GROUP BY tag ORDER BY COUNT(tag) DESC")
    List<Object[]> findMostCommonTags(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds most common topics.
     */
    @Query("SELECT topic, COUNT(topic) FROM VideoTutorialSeries vts JOIN vts.topics topic " +
           "WHERE vts.teamNumber = :teamNumber AND vts.season = :season AND vts.isActive = true " +
           "GROUP BY topic ORDER BY COUNT(topic) DESC")
    List<Object[]> findMostCommonTopics(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Calculates series metrics summary.
     */
    @Query("SELECT AVG(vts.viewCount), AVG(vts.enrollmentCount), AVG(vts.completionCount), " +
           "AVG(vts.completionRate), AVG(vts.averageRating), AVG(vts.tutorialCount) " +
           "FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber AND vts.season = :season AND vts.isActive = true")
    List<Object[]> findSeriesMetrics(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    // =========================================================================
    // CROSS-SEASON ANALYSIS
    // =========================================================================

    /**
     * Finds series across multiple seasons for a team.
     */
    @Query("SELECT vts FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber " +
           "AND vts.season IN :seasons AND vts.isActive = true " +
           "ORDER BY vts.season DESC, vts.title ASC")
    List<VideoTutorialSeries> findMultiSeasonSeries(@Param("teamNumber") Integer teamNumber, 
                                                    @Param("seasons") List<Integer> seasons);

    /**
     * Finds series with same title across seasons.
     */
    @Query("SELECT vts FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber AND vts.title = :title " +
           "AND vts.isActive = true ORDER BY vts.season DESC")
    List<VideoTutorialSeries> findByTitleAcrossSeasons(@Param("teamNumber") Integer teamNumber, @Param("title") String title);

    /**
     * Finds recurring series (exist in multiple seasons).
     */
    @Query("SELECT vts.title, COUNT(DISTINCT vts.season) as seasonCount FROM VideoTutorialSeries vts " +
           "WHERE vts.teamNumber = :teamNumber AND vts.isActive = true " +
           "GROUP BY vts.title HAVING COUNT(DISTINCT vts.season) > 1 " +
           "ORDER BY seasonCount DESC")
    List<Object[]> findRecurringSeries(@Param("teamNumber") Integer teamNumber);

    // =========================================================================
    // BULK OPERATIONS AND MAINTENANCE
    // =========================================================================

    /**
     * Counts total active series for team and season.
     */
    @Query("SELECT COUNT(vts) FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber " +
           "AND vts.season = :season AND vts.isActive = true")
    Long countActiveSeries(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds all active series for export/backup.
     */
    @Query("SELECT vts FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber AND vts.season = :season " +
           "AND vts.isActive = true ORDER BY vts.category ASC, vts.title ASC")
    List<VideoTutorialSeries> findAllActiveSeries(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Updates search rank for all series.
     */
    @Query("UPDATE VideoTutorialSeries vts SET vts.searchRank = :rank WHERE vts.teamNumber = :teamNumber " +
           "AND vts.season = :season AND vts.isActive = true")
    void updateSearchRankForAllSeries(@Param("teamNumber") Integer teamNumber,
                                     @Param("season") Integer season,
                                     @Param("rank") Integer rank);

    /**
     * Finds series for sitemap generation.
     */
    @Query("SELECT vts.slug, vts.updatedAt FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber " +
           "AND vts.season = :season AND vts.isActive = true " +
           "AND vts.visibility IN ('PUBLIC', 'TEAM_ONLY') ORDER BY vts.updatedAt DESC")
    List<Object[]> findForSitemap(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    // =========================================================================
    // ARCHIVE AND HISTORY
    // =========================================================================

    /**
     * Finds archived series.
     */
    @Query("SELECT vts FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber AND vts.season = :season " +
           "AND vts.isArchived = true ORDER BY vts.archivedAt DESC")
    List<VideoTutorialSeries> findArchivedSeries(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds series archived within date range.
     */
    @Query("SELECT vts FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber AND vts.season = :season " +
           "AND vts.isArchived = true AND vts.archivedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY vts.archivedAt DESC")
    List<VideoTutorialSeries> findArchivedInDateRange(@Param("teamNumber") Integer teamNumber,
                                                      @Param("season") Integer season,
                                                      @Param("startDate") LocalDateTime startDate,
                                                      @Param("endDate") LocalDateTime endDate);

    /**
     * Finds series by archive reason.
     */
    @Query("SELECT vts FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber AND vts.season = :season " +
           "AND vts.isArchived = true AND vts.archiveReason LIKE CONCAT('%', :reason, '%')")
    List<VideoTutorialSeries> findByArchiveReason(@Param("teamNumber") Integer teamNumber,
                                                  @Param("season") Integer season,
                                                  @Param("reason") String reason);

    // =========================================================================
    // RECOMMENDATION QUERIES
    // =========================================================================

    /**
     * Finds similar series by category and skill level.
     */
    @Query("SELECT vts FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber AND vts.season = :season " +
           "AND vts.category = :category AND vts.targetSkillLevel = :skillLevel " +
           "AND vts.id != :excludeId AND vts.isActive = true " +
           "ORDER BY vts.engagementScore DESC")
    List<VideoTutorialSeries> findSimilarSeries(@Param("teamNumber") Integer teamNumber,
                                                @Param("season") Integer season,
                                                @Param("category") VideoTutorialSeries.SeriesCategory category,
                                                @Param("skillLevel") VideoTutorialSeries.SkillLevel skillLevel,
                                                @Param("excludeId") Long excludeId);

    /**
     * Finds trending series (high recent engagement).
     */
    @Query("SELECT vts FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber AND vts.season = :season " +
           "AND vts.lastViewed >= :recentCutoff AND vts.isActive = true " +
           "ORDER BY vts.engagementScore DESC")
    List<VideoTutorialSeries> findTrendingSeries(@Param("teamNumber") Integer teamNumber,
                                                 @Param("season") Integer season,
                                                 @Param("recentCutoff") LocalDateTime recentCutoff);

    /**
     * Finds quick series (short completion time).
     */
    @Query("SELECT vts FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber AND vts.season = :season " +
           "AND vts.estimatedCompletionHours <= :maxHours AND vts.isActive = true " +
           "ORDER BY vts.engagementScore DESC")
    List<VideoTutorialSeries> findQuickSeries(@Param("teamNumber") Integer teamNumber,
                                              @Param("season") Integer season,
                                              @Param("maxHours") Integer maxHours);

    /**
     * Finds comprehensive series (long completion time).
     */
    @Query("SELECT vts FROM VideoTutorialSeries vts WHERE vts.teamNumber = :teamNumber AND vts.season = :season " +
           "AND vts.estimatedCompletionHours >= :minHours AND vts.isActive = true " +
           "ORDER BY vts.engagementScore DESC")
    List<VideoTutorialSeries> findComprehensiveSeries(@Param("teamNumber") Integer teamNumber,
                                                      @Param("season") Integer season,
                                                      @Param("minHours") Integer minHours);
}