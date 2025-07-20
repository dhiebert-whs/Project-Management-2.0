// src/main/java/org/frcpm/repositories/spring/VideoTutorialRepository.java

package org.frcpm.repositories.spring;

import org.frcpm.models.VideoTutorial;
import org.frcpm.models.VideoTutorialSeries;
import org.frcpm.models.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for VideoTutorial entities.
 * 
 * Provides comprehensive data access for video tutorial management including
 * content organization, learning analytics, engagement tracking, and 
 * educational content management for FRC teams.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-KnowledgeManagement
 * @since Phase 4D.2 Video Tutorial Library Integration
 */
@Repository
public interface VideoTutorialRepository extends JpaRepository<VideoTutorial, Long> {

    // =========================================================================
    // BASIC QUERIES
    // =========================================================================

    /**
     * Finds all active tutorials for a specific team and season.
     */
    List<VideoTutorial> findByTeamNumberAndSeasonAndIsActiveTrue(Integer teamNumber, Integer season);

    /**
     * Finds tutorial by slug for a team and season.
     */
    Optional<VideoTutorial> findByTeamNumberAndSeasonAndSlugAndIsActiveTrue(Integer teamNumber, Integer season, String slug);

    /**
     * Finds tutorials by status for a team and season.
     */
    List<VideoTutorial> findByTeamNumberAndSeasonAndStatusAndIsActiveTrue(
        Integer teamNumber, Integer season, VideoTutorial.TutorialStatus status);

    /**
     * Finds tutorials by category for a team and season.
     */
    List<VideoTutorial> findByTeamNumberAndSeasonAndCategoryAndIsActiveTrue(
        Integer teamNumber, Integer season, VideoTutorial.TutorialCategory category);

    /**
     * Finds tutorials by skill level.
     */
    List<VideoTutorial> findByTeamNumberAndSeasonAndSkillLevelAndIsActiveTrue(
        Integer teamNumber, Integer season, VideoTutorial.SkillLevel skillLevel);

    /**
     * Finds tutorials by visibility level.
     */
    List<VideoTutorial> findByTeamNumberAndSeasonAndVisibilityAndIsActiveTrue(
        Integer teamNumber, Integer season, VideoTutorial.VisibilityLevel visibility);

    // =========================================================================
    // CONTENT SEARCH AND FILTERING
    // =========================================================================

    /**
     * Searches tutorials by title (case-insensitive).
     */
    @Query("SELECT vt FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber AND vt.season = :season " +
           "AND LOWER(vt.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "AND vt.isActive = true AND vt.isSearchable = true")
    List<VideoTutorial> searchByTitle(@Param("teamNumber") Integer teamNumber,
                                     @Param("season") Integer season,
                                     @Param("searchTerm") String searchTerm);

    /**
     * Searches tutorials by description and summary (case-insensitive).
     */
    @Query("SELECT vt FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber AND vt.season = :season " +
           "AND (LOWER(vt.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(vt.summary) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND vt.isActive = true AND vt.isSearchable = true")
    List<VideoTutorial> searchByContent(@Param("teamNumber") Integer teamNumber,
                                       @Param("season") Integer season,
                                       @Param("searchTerm") String searchTerm);

    /**
     * Full text search across title, description, and summary.
     */
    @Query("SELECT vt FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber AND vt.season = :season " +
           "AND (LOWER(vt.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(vt.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(vt.summary) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND vt.isActive = true AND vt.isSearchable = true " +
           "ORDER BY vt.searchRank DESC, vt.viewCount DESC")
    List<VideoTutorial> fullTextSearch(@Param("teamNumber") Integer teamNumber,
                                      @Param("season") Integer season,
                                      @Param("searchTerm") String searchTerm);

    /**
     * Finds tutorials by tag.
     */
    @Query("SELECT vt FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber AND vt.season = :season " +
           "AND :tag MEMBER OF vt.tags AND vt.isActive = true")
    List<VideoTutorial> findByTag(@Param("teamNumber") Integer teamNumber,
                                 @Param("season") Integer season,
                                 @Param("tag") String tag);

    /**
     * Finds tutorials by topic.
     */
    @Query("SELECT vt FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber AND vt.season = :season " +
           "AND :topic MEMBER OF vt.topics AND vt.isActive = true")
    List<VideoTutorial> findByTopic(@Param("teamNumber") Integer teamNumber,
                                   @Param("season") Integer season,
                                   @Param("topic") String topic);

    /**
     * Finds tutorials by skill taught.
     */
    @Query("SELECT vt FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber AND vt.season = :season " +
           "AND :skill MEMBER OF vt.skillsTaught AND vt.isActive = true")
    List<VideoTutorial> findBySkillTaught(@Param("teamNumber") Integer teamNumber,
                                         @Param("season") Integer season,
                                         @Param("skill") String skill);

    /**
     * Finds tutorials by duration range.
     */
    @Query("SELECT vt FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber AND vt.season = :season " +
           "AND vt.duration BETWEEN :minDuration AND :maxDuration AND vt.isActive = true")
    List<VideoTutorial> findByDurationRange(@Param("teamNumber") Integer teamNumber,
                                           @Param("season") Integer season,
                                           @Param("minDuration") Duration minDuration,
                                           @Param("maxDuration") Duration maxDuration);

    // =========================================================================
    // SERIES AND ORGANIZATION
    // =========================================================================

    /**
     * Finds tutorials in a specific series.
     */
    List<VideoTutorial> findBySeriesAndIsActiveTrueOrderBySeriesOrderAsc(VideoTutorialSeries series);

    /**
     * Finds tutorials not assigned to any series.
     */
    @Query("SELECT vt FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber AND vt.season = :season " +
           "AND vt.series IS NULL AND vt.isActive = true")
    List<VideoTutorial> findStandaloneTutorials(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds next tutorial in series.
     */
    @Query("SELECT vt FROM VideoTutorial vt WHERE vt.series = :series " +
           "AND vt.seriesOrder > :currentOrder AND vt.isActive = true " +
           "ORDER BY vt.seriesOrder ASC")
    Optional<VideoTutorial> findNextInSeries(@Param("series") VideoTutorialSeries series, 
                                            @Param("currentOrder") Integer currentOrder);

    /**
     * Finds previous tutorial in series.
     */
    @Query("SELECT vt FROM VideoTutorial vt WHERE vt.series = :series " +
           "AND vt.seriesOrder < :currentOrder AND vt.isActive = true " +
           "ORDER BY vt.seriesOrder DESC")
    Optional<VideoTutorial> findPreviousInSeries(@Param("series") VideoTutorialSeries series, 
                                                @Param("currentOrder") Integer currentOrder);

    /**
     * Finds first tutorial in series.
     */
    @Query("SELECT vt FROM VideoTutorial vt WHERE vt.series = :series " +
           "AND vt.isActive = true ORDER BY vt.seriesOrder ASC")
    Optional<VideoTutorial> findFirstInSeries(@Param("series") VideoTutorialSeries series);

    // =========================================================================
    // ACCESS CONTROL AND PERMISSIONS
    // =========================================================================

    /**
     * Finds tutorials viewable by specific access level.
     */
    List<VideoTutorial> findByTeamNumberAndSeasonAndViewPermissionAndIsActiveTrue(
        Integer teamNumber, Integer season, VideoTutorial.AccessLevel viewPermission);

    /**
     * Finds tutorials where user is authorized viewer.
     */
    @Query("SELECT vt FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber AND vt.season = :season " +
           "AND :userId MEMBER OF vt.authorizedViewers AND vt.isActive = true")
    List<VideoTutorial> findByAuthorizedViewer(@Param("teamNumber") Integer teamNumber,
                                              @Param("season") Integer season,
                                              @Param("userId") Long userId);

    /**
     * Finds public tutorials.
     */
    @Query("SELECT vt FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber AND vt.season = :season " +
           "AND vt.visibility = 'PUBLIC' AND vt.isActive = true")
    List<VideoTutorial> findPublicTutorials(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds restricted tutorials.
     */
    @Query("SELECT vt FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber AND vt.season = :season " +
           "AND vt.isRestricted = true AND vt.isActive = true")
    List<VideoTutorial> findRestrictedTutorials(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    // =========================================================================
    // CREATOR AND INSTRUCTOR QUERIES
    // =========================================================================

    /**
     * Finds tutorials created by specific user.
     */
    List<VideoTutorial> findByCreatedByAndTeamNumberAndSeasonAndIsActiveTrue(
        TeamMember createdBy, Integer teamNumber, Integer season);

    /**
     * Finds tutorials by instructor.
     */
    List<VideoTutorial> findByInstructorAndTeamNumberAndSeasonAndIsActiveTrue(
        TeamMember instructor, Integer teamNumber, Integer season);

    /**
     * Finds tutorials by contributor.
     */
    @Query("SELECT vt FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber AND vt.season = :season " +
           "AND :contributorId MEMBER OF vt.contributors AND vt.isActive = true")
    List<VideoTutorial> findByContributor(@Param("teamNumber") Integer teamNumber,
                                         @Param("season") Integer season,
                                         @Param("contributorId") Long contributorId);

    /**
     * Finds tutorials updated by specific user.
     */
    List<VideoTutorial> findByUpdatedByAndTeamNumberAndSeasonAndIsActiveTrue(
        TeamMember updatedBy, Integer teamNumber, Integer season);

    // =========================================================================
    // ANALYTICS AND ENGAGEMENT
    // =========================================================================

    /**
     * Finds most viewed tutorials.
     */
    @Query("SELECT vt FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber AND vt.season = :season " +
           "AND vt.isActive = true ORDER BY vt.viewCount DESC")
    List<VideoTutorial> findMostViewed(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds most completed tutorials.
     */
    @Query("SELECT vt FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber AND vt.season = :season " +
           "AND vt.isActive = true ORDER BY vt.completionCount DESC")
    List<VideoTutorial> findMostCompleted(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds highest rated tutorials.
     */
    @Query("SELECT vt FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber AND vt.season = :season " +
           "AND vt.ratingCount >= :minRatingCount AND vt.isActive = true " +
           "ORDER BY vt.averageRating DESC")
    List<VideoTutorial> findHighestRated(@Param("teamNumber") Integer teamNumber,
                                        @Param("season") Integer season,
                                        @Param("minRatingCount") Integer minRatingCount);

    /**
     * Finds tutorials with high engagement.
     */
    @Query("SELECT vt FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber AND vt.season = :season " +
           "AND vt.engagementScore >= :minScore AND vt.isActive = true " +
           "ORDER BY vt.engagementScore DESC")
    List<VideoTutorial> findHighEngagement(@Param("teamNumber") Integer teamNumber,
                                          @Param("season") Integer season,
                                          @Param("minScore") Double minScore);

    /**
     * Finds recently viewed tutorials.
     */
    @Query("SELECT vt FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber AND vt.season = :season " +
           "AND vt.lastViewed IS NOT NULL AND vt.isActive = true " +
           "ORDER BY vt.lastViewed DESC")
    List<VideoTutorial> findRecentlyViewed(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds tutorials with low completion rates.
     */
    @Query("SELECT vt FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber AND vt.season = :season " +
           "AND vt.viewCount > :minViews AND vt.completionRate < :maxCompletionRate " +
           "AND vt.isActive = true ORDER BY vt.completionRate ASC")
    List<VideoTutorial> findLowCompletionRate(@Param("teamNumber") Integer teamNumber,
                                             @Param("season") Integer season,
                                             @Param("minViews") Long minViews,
                                             @Param("maxCompletionRate") Double maxCompletionRate);

    // =========================================================================
    // FEATURED AND SPECIAL TUTORIALS
    // =========================================================================

    /**
     * Finds featured tutorials.
     */
    @Query("SELECT vt FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber AND vt.season = :season " +
           "AND vt.isFeatured = true AND vt.isActive = true " +
           "ORDER BY vt.featuredAt DESC")
    List<VideoTutorial> findFeatured(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds tutorials with interactive elements.
     */
    @Query("SELECT vt FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber AND vt.season = :season " +
           "AND (vt.hasInteractiveElements = true OR vt.hasQuiz = true) AND vt.isActive = true")
    List<VideoTutorial> findInteractive(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds tutorials with downloadable resources.
     */
    @Query("SELECT vt FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber AND vt.season = :season " +
           "AND vt.hasDownloadableResources = true AND vt.isActive = true")
    List<VideoTutorial> findWithResources(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds tutorials with subtitles.
     */
    @Query("SELECT vt FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber AND vt.season = :season " +
           "AND vt.hasSubtitles = true AND vt.isActive = true")
    List<VideoTutorial> findWithSubtitles(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds tutorials with transcripts.
     */
    @Query("SELECT vt FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber AND vt.season = :season " +
           "AND vt.hasTranscript = true AND vt.isActive = true")
    List<VideoTutorial> findWithTranscripts(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    // =========================================================================
    // VIDEO SOURCE AND TECHNICAL QUERIES
    // =========================================================================

    /**
     * Finds tutorials by video source.
     */
    List<VideoTutorial> findByTeamNumberAndSeasonAndVideoSourceAndIsActiveTrue(
        Integer teamNumber, Integer season, VideoTutorial.VideoSource videoSource);

    /**
     * Finds tutorials by video quality.
     */
    List<VideoTutorial> findByTeamNumberAndSeasonAndVideoQualityAndIsActiveTrue(
        Integer teamNumber, Integer season, VideoTutorial.VideoQuality videoQuality);

    /**
     * Finds tutorials by external video ID.
     */
    Optional<VideoTutorial> findByExternalVideoIdAndIsActiveTrue(String externalVideoId);

    /**
     * Finds tutorials requiring update.
     */
    @Query("SELECT vt FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber AND vt.season = :season " +
           "AND vt.needsUpdate = true AND vt.isActive = true")
    List<VideoTutorial> findNeedingUpdate(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    // =========================================================================
    // MODERATION AND QUALITY CONTROL
    // =========================================================================

    /**
     * Finds flagged tutorials.
     */
    @Query("SELECT vt FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber AND vt.season = :season " +
           "AND vt.isFlagged = true AND vt.isActive = true")
    List<VideoTutorial> findFlagged(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds tutorials pending moderation.
     */
    @Query("SELECT vt FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber AND vt.season = :season " +
           "AND vt.isModerated = false AND vt.status = 'UNDER_REVIEW' AND vt.isActive = true")
    List<VideoTutorial> findPendingModeration(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds tutorials by moderator.
     */
    List<VideoTutorial> findByModeratedByAndTeamNumberAndSeasonAndIsActiveTrue(
        TeamMember moderatedBy, Integer teamNumber, Integer season);

    /**
     * Finds tutorials by quality score range.
     */
    @Query("SELECT vt FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber AND vt.season = :season " +
           "AND vt.qualityScore BETWEEN :minScore AND :maxScore AND vt.isActive = true")
    List<VideoTutorial> findByQualityScoreRange(@Param("teamNumber") Integer teamNumber,
                                               @Param("season") Integer season,
                                               @Param("minScore") Integer minScore,
                                               @Param("maxScore") Integer maxScore);

    /**
     * Finds unmoderated tutorials.
     */
    @Query("SELECT vt FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber AND vt.season = :season " +
           "AND vt.isModerated = false AND vt.isActive = true")
    List<VideoTutorial> findUnmoderated(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    // =========================================================================
    // TIME-BASED QUERIES
    // =========================================================================

    /**
     * Finds tutorials created within date range.
     */
    @Query("SELECT vt FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber AND vt.season = :season " +
           "AND vt.createdAt BETWEEN :startDate AND :endDate AND vt.isActive = true")
    List<VideoTutorial> findCreatedInDateRange(@Param("teamNumber") Integer teamNumber,
                                              @Param("season") Integer season,
                                              @Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    /**
     * Finds tutorials updated within date range.
     */
    @Query("SELECT vt FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber AND vt.season = :season " +
           "AND vt.updatedAt BETWEEN :startDate AND :endDate AND vt.isActive = true")
    List<VideoTutorial> findUpdatedInDateRange(@Param("teamNumber") Integer teamNumber,
                                              @Param("season") Integer season,
                                              @Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    /**
     * Finds recently created tutorials.
     */
    @Query("SELECT vt FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber AND vt.season = :season " +
           "AND vt.createdAt >= :since AND vt.isActive = true ORDER BY vt.createdAt DESC")
    List<VideoTutorial> findRecentlyCreated(@Param("teamNumber") Integer teamNumber,
                                           @Param("season") Integer season,
                                           @Param("since") LocalDateTime since);

    /**
     * Finds stale tutorials (old and not updated).
     */
    @Query("SELECT vt FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber AND vt.season = :season " +
           "AND vt.updatedAt < :staleDate AND vt.viewCount < :minViews AND vt.isActive = true")
    List<VideoTutorial> findStaleTutorials(@Param("teamNumber") Integer teamNumber,
                                          @Param("season") Integer season,
                                          @Param("staleDate") LocalDateTime staleDate,
                                          @Param("minViews") Long minViews);

    // =========================================================================
    // STATISTICS AND ANALYTICS
    // =========================================================================

    /**
     * Counts tutorials by category.
     */
    @Query("SELECT vt.category, COUNT(vt) FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber " +
           "AND vt.season = :season AND vt.isActive = true GROUP BY vt.category")
    List<Object[]> countByCategory(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Counts tutorials by skill level.
     */
    @Query("SELECT vt.skillLevel, COUNT(vt) FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber " +
           "AND vt.season = :season AND vt.isActive = true GROUP BY vt.skillLevel")
    List<Object[]> countBySkillLevel(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Counts tutorials by status.
     */
    @Query("SELECT vt.status, COUNT(vt) FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber " +
           "AND vt.season = :season AND vt.isActive = true GROUP BY vt.status")
    List<Object[]> countByStatus(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Calculates total view count for team.
     */
    @Query("SELECT SUM(vt.viewCount) FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber " +
           "AND vt.season = :season AND vt.isActive = true")
    Optional<Long> findTotalViewCount(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Calculates total completion count for team.
     */
    @Query("SELECT SUM(vt.completionCount) FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber " +
           "AND vt.season = :season AND vt.isActive = true")
    Optional<Long> findTotalCompletionCount(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Calculates average engagement score.
     */
    @Query("SELECT AVG(vt.engagementScore) FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber " +
           "AND vt.season = :season AND vt.isActive = true")
    Optional<Double> findAverageEngagementScore(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Calculates average rating.
     */
    @Query("SELECT AVG(vt.averageRating) FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber " +
           "AND vt.season = :season AND vt.ratingCount > 0 AND vt.isActive = true")
    Optional<Double> findAverageRating(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds most common tags.
     */
    @Query("SELECT tag, COUNT(tag) FROM VideoTutorial vt JOIN vt.tags tag " +
           "WHERE vt.teamNumber = :teamNumber AND vt.season = :season AND vt.isActive = true " +
           "GROUP BY tag ORDER BY COUNT(tag) DESC")
    List<Object[]> findMostCommonTags(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds most common topics.
     */
    @Query("SELECT topic, COUNT(topic) FROM VideoTutorial vt JOIN vt.topics topic " +
           "WHERE vt.teamNumber = :teamNumber AND vt.season = :season AND vt.isActive = true " +
           "GROUP BY topic ORDER BY COUNT(topic) DESC")
    List<Object[]> findMostCommonTopics(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Calculates tutorial metrics summary.
     */
    @Query("SELECT AVG(vt.viewCount), AVG(vt.completionCount), AVG(vt.completionRate), AVG(vt.averageRating) " +
           "FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber AND vt.season = :season AND vt.isActive = true")
    List<Object[]> findTutorialMetrics(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    // =========================================================================
    // CROSS-SEASON ANALYSIS
    // =========================================================================

    /**
     * Finds tutorials across multiple seasons for a team.
     */
    @Query("SELECT vt FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber " +
           "AND vt.season IN :seasons AND vt.isActive = true " +
           "ORDER BY vt.season DESC, vt.title ASC")
    List<VideoTutorial> findMultiSeasonTutorials(@Param("teamNumber") Integer teamNumber, 
                                                @Param("seasons") List<Integer> seasons);

    /**
     * Finds tutorials with same title across seasons.
     */
    @Query("SELECT vt FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber AND vt.title = :title " +
           "AND vt.isActive = true ORDER BY vt.season DESC")
    List<VideoTutorial> findByTitleAcrossSeasons(@Param("teamNumber") Integer teamNumber, @Param("title") String title);

    /**
     * Finds evergreen tutorials (exist in multiple seasons).
     */
    @Query("SELECT vt.title, COUNT(DISTINCT vt.season) as seasonCount FROM VideoTutorial vt " +
           "WHERE vt.teamNumber = :teamNumber AND vt.isActive = true " +
           "GROUP BY vt.title HAVING COUNT(DISTINCT vt.season) > 1 " +
           "ORDER BY seasonCount DESC")
    List<Object[]> findEvergreenTutorials(@Param("teamNumber") Integer teamNumber);

    // =========================================================================
    // BULK OPERATIONS AND MAINTENANCE
    // =========================================================================

    /**
     * Counts total active tutorials for team and season.
     */
    @Query("SELECT COUNT(vt) FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber " +
           "AND vt.season = :season AND vt.isActive = true")
    Long countActiveTutorials(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds all active tutorials for export/backup.
     */
    @Query("SELECT vt FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber AND vt.season = :season " +
           "AND vt.isActive = true ORDER BY vt.category ASC, vt.title ASC")
    List<VideoTutorial> findAllActiveTutorials(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Updates search rank for all tutorials.
     */
    @Query("UPDATE VideoTutorial vt SET vt.searchRank = :rank WHERE vt.teamNumber = :teamNumber " +
           "AND vt.season = :season AND vt.isActive = true")
    void updateSearchRankForAllTutorials(@Param("teamNumber") Integer teamNumber,
                                        @Param("season") Integer season,
                                        @Param("rank") Integer rank);

    /**
     * Finds tutorials for sitemap generation.
     */
    @Query("SELECT vt.slug, vt.updatedAt FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber " +
           "AND vt.season = :season AND vt.isActive = true " +
           "AND vt.visibility IN ('PUBLIC', 'TEAM_ONLY') ORDER BY vt.updatedAt DESC")
    List<Object[]> findForSitemap(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    // =========================================================================
    // ARCHIVE AND HISTORY
    // =========================================================================

    /**
     * Finds archived tutorials.
     */
    @Query("SELECT vt FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber AND vt.season = :season " +
           "AND vt.isArchived = true ORDER BY vt.archivedAt DESC")
    List<VideoTutorial> findArchivedTutorials(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds tutorials archived within date range.
     */
    @Query("SELECT vt FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber AND vt.season = :season " +
           "AND vt.isArchived = true AND vt.archivedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY vt.archivedAt DESC")
    List<VideoTutorial> findArchivedInDateRange(@Param("teamNumber") Integer teamNumber,
                                               @Param("season") Integer season,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    /**
     * Finds tutorials by archive reason.
     */
    @Query("SELECT vt FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber AND vt.season = :season " +
           "AND vt.isArchived = true AND vt.archiveReason LIKE CONCAT('%', :reason, '%')")
    List<VideoTutorial> findByArchiveReason(@Param("teamNumber") Integer teamNumber,
                                           @Param("season") Integer season,
                                           @Param("reason") String reason);

    // =========================================================================
    // INTEGRATION AND RELATIONSHIPS
    // =========================================================================

    /**
     * Finds tutorials related to specific task.
     */
    @Query("SELECT vt FROM VideoTutorial vt JOIN vt.relatedTasks rt " +
           "WHERE rt.id = :taskId AND vt.isActive = true")
    List<VideoTutorial> findByRelatedTask(@Param("taskId") Long taskId);

    /**
     * Finds tutorials related to specific project.
     */
    @Query("SELECT vt FROM VideoTutorial vt JOIN vt.relatedProjects rp " +
           "WHERE rp.id = :projectId AND vt.isActive = true")
    List<VideoTutorial> findByRelatedProject(@Param("projectId") Long projectId);

    /**
     * Finds tutorials related to specific wiki page.
     */
    @Query("SELECT vt FROM VideoTutorial vt JOIN vt.relatedWikiPages rw " +
           "WHERE rw.id = :wikiPageId AND vt.isActive = true")
    List<VideoTutorial> findByRelatedWikiPage(@Param("wikiPageId") Long wikiPageId);

    /**
     * Finds tutorials without any related content.
     */
    @Query("SELECT vt FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber AND vt.season = :season " +
           "AND vt.relatedTasks IS EMPTY AND vt.relatedProjects IS EMPTY " +
           "AND vt.relatedWikiPages IS EMPTY AND vt.isActive = true")
    List<VideoTutorial> findUnrelatedTutorials(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    // =========================================================================
    // RECOMMENDATION QUERIES
    // =========================================================================

    /**
     * Finds similar tutorials by category and skill level.
     */
    @Query("SELECT vt FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber AND vt.season = :season " +
           "AND vt.category = :category AND vt.skillLevel = :skillLevel " +
           "AND vt.id != :excludeId AND vt.isActive = true " +
           "ORDER BY vt.engagementScore DESC")
    List<VideoTutorial> findSimilarTutorials(@Param("teamNumber") Integer teamNumber,
                                            @Param("season") Integer season,
                                            @Param("category") VideoTutorial.TutorialCategory category,
                                            @Param("skillLevel") VideoTutorial.SkillLevel skillLevel,
                                            @Param("excludeId") Long excludeId);

    /**
     * Finds beginner-friendly tutorials for new team members.
     */
    @Query("SELECT vt FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber AND vt.season = :season " +
           "AND vt.skillLevel = 'BEGINNER' AND vt.averageRating >= 4.0 " +
           "AND vt.isActive = true ORDER BY vt.engagementScore DESC")
    List<VideoTutorial> findBeginnerFriendlyTutorials(@Param("teamNumber") Integer teamNumber, 
                                                     @Param("season") Integer season);

    /**
     * Finds trending tutorials (high recent engagement).
     */
    @Query("SELECT vt FROM VideoTutorial vt WHERE vt.teamNumber = :teamNumber AND vt.season = :season " +
           "AND vt.lastViewed >= :recentCutoff AND vt.isActive = true " +
           "ORDER BY vt.engagementScore DESC")
    List<VideoTutorial> findTrendingTutorials(@Param("teamNumber") Integer teamNumber,
                                             @Param("season") Integer season,
                                             @Param("recentCutoff") LocalDateTime recentCutoff);
}