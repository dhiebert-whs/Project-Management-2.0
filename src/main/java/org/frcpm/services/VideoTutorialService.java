// src/main/java/org/frcpm/services/VideoTutorialService.java

package org.frcpm.services;

import org.frcpm.models.VideoTutorial;
import org.frcpm.models.VideoTutorialSeries;
import org.frcpm.models.TeamMember;
import org.frcpm.models.Task;
import org.frcpm.models.Project;
import org.frcpm.models.WikiPage;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for VideoTutorial operations.
 * 
 * Provides comprehensive video tutorial management services including
 * content organization, learning analytics, engagement tracking, and
 * educational content management for FRC teams.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-KnowledgeManagement
 * @since Phase 4D.2 Video Tutorial Library Integration
 */
public interface VideoTutorialService {

    // =========================================================================
    // STANDARD SERVICE METHODS
    // =========================================================================

    /**
     * Creates a new video tutorial.
     */
    VideoTutorial create(VideoTutorial tutorial);

    /**
     * Updates an existing video tutorial.
     */
    VideoTutorial update(Long id, VideoTutorial tutorial);

    /**
     * Deletes a video tutorial (soft delete).
     */
    void delete(Long id);

    /**
     * Finds a video tutorial by ID.
     */
    Optional<VideoTutorial> findById(Long id);

    /**
     * Finds all active video tutorials.
     */
    List<VideoTutorial> findAll();

    /**
     * Checks if video tutorial exists by ID.
     */
    boolean existsById(Long id);

    /**
     * Counts total number of video tutorials.
     */
    long count();

    // =========================================================================
    // TUTORIAL MANAGEMENT
    // =========================================================================

    /**
     * Creates a new tutorial with validation.
     */
    VideoTutorial createTutorial(VideoTutorial tutorial);

    /**
     * Creates a tutorial with basic parameters.
     */
    VideoTutorial createTutorial(Integer teamNumber, Integer season, String title, String videoUrl,
                                VideoTutorial.TutorialCategory category, VideoTutorial.SkillLevel skillLevel,
                                TeamMember createdBy);

    /**
     * Updates an existing tutorial with validation.
     */
    VideoTutorial updateTutorial(Long tutorialId, VideoTutorial tutorial);

    /**
     * Archives a tutorial.
     */
    void archiveTutorial(Long tutorialId, String reason);

    /**
     * Finds all active tutorials for a team and season.
     */
    List<VideoTutorial> findActiveTutorials(Integer teamNumber, Integer season);

    // =========================================================================
    // CONTENT SEARCH AND FILTERING
    // =========================================================================

    /**
     * Searches tutorials by title.
     */
    List<VideoTutorial> searchByTitle(Integer teamNumber, Integer season, String searchTerm);

    /**
     * Searches tutorials by content.
     */
    List<VideoTutorial> searchByContent(Integer teamNumber, Integer season, String searchTerm);

    /**
     * Full text search across multiple fields.
     */
    List<VideoTutorial> fullTextSearch(Integer teamNumber, Integer season, String searchTerm);

    /**
     * Finds tutorials by category.
     */
    List<VideoTutorial> findByCategory(Integer teamNumber, Integer season, VideoTutorial.TutorialCategory category);

    /**
     * Finds tutorials by skill level.
     */
    List<VideoTutorial> findBySkillLevel(Integer teamNumber, Integer season, VideoTutorial.SkillLevel skillLevel);

    /**
     * Finds tutorials by tag.
     */
    List<VideoTutorial> findByTag(Integer teamNumber, Integer season, String tag);

    /**
     * Finds tutorials by topic.
     */
    List<VideoTutorial> findByTopic(Integer teamNumber, Integer season, String topic);

    /**
     * Finds tutorials by skill taught.
     */
    List<VideoTutorial> findBySkillTaught(Integer teamNumber, Integer season, String skill);

    /**
     * Advanced search with multiple criteria.
     */
    List<VideoTutorial> advancedSearch(Integer teamNumber, Integer season, Map<String, Object> criteria);

    // =========================================================================
    // SERIES MANAGEMENT
    // =========================================================================

    /**
     * Finds tutorials in a specific series.
     */
    List<VideoTutorial> findBySeries(VideoTutorialSeries series);

    /**
     * Finds standalone tutorials (not in any series).
     */
    List<VideoTutorial> findStandaloneTutorials(Integer teamNumber, Integer season);

    /**
     * Adds tutorial to series.
     */
    VideoTutorial addToSeries(Long tutorialId, VideoTutorialSeries series, Integer order);

    /**
     * Removes tutorial from series.
     */
    VideoTutorial removeFromSeries(Long tutorialId);

    /**
     * Reorders tutorials within a series.
     */
    void reorderSeriesTutorials(VideoTutorialSeries series, Map<Long, Integer> newOrders);

    /**
     * Finds next tutorial in series.
     */
    Optional<VideoTutorial> findNextInSeries(VideoTutorial tutorial);

    /**
     * Finds previous tutorial in series.
     */
    Optional<VideoTutorial> findPreviousInSeries(VideoTutorial tutorial);

    // =========================================================================
    // ACCESS CONTROL AND PERMISSIONS
    // =========================================================================

    /**
     * Finds tutorials viewable by specific access level.
     */
    List<VideoTutorial> findViewableTutorials(Integer teamNumber, Integer season, VideoTutorial.AccessLevel accessLevel);

    /**
     * Finds tutorials viewable by user.
     */
    List<VideoTutorial> findViewableTutorialsByUser(Integer teamNumber, Integer season, Long userId, String userRole);

    /**
     * Checks if user can view tutorial.
     */
    boolean canUserViewTutorial(Long tutorialId, Long userId, String userRole);

    /**
     * Adds authorized viewer to tutorial.
     */
    VideoTutorial addAuthorizedViewer(Long tutorialId, Long userId);

    /**
     * Removes authorized viewer from tutorial.
     */
    VideoTutorial removeAuthorizedViewer(Long tutorialId, Long userId);

    /**
     * Finds public tutorials.
     */
    List<VideoTutorial> findPublicTutorials(Integer teamNumber, Integer season);

    // =========================================================================
    // CREATOR AND INSTRUCTOR MANAGEMENT
    // =========================================================================

    /**
     * Finds tutorials created by user.
     */
    List<VideoTutorial> findByCreatedBy(TeamMember createdBy, Integer teamNumber, Integer season);

    /**
     * Finds tutorials by instructor.
     */
    List<VideoTutorial> findByInstructor(TeamMember instructor, Integer teamNumber, Integer season);

    /**
     * Finds tutorials by contributor.
     */
    List<VideoTutorial> findByContributor(Integer teamNumber, Integer season, Long contributorId);

    /**
     * Adds contributor to tutorial.
     */
    VideoTutorial addContributor(Long tutorialId, Long contributorId);

    /**
     * Removes contributor from tutorial.
     */
    VideoTutorial removeContributor(Long tutorialId, Long contributorId);

    /**
     * Sets primary instructor for tutorial.
     */
    VideoTutorial setInstructor(Long tutorialId, TeamMember instructor);

    // =========================================================================
    // ANALYTICS AND ENGAGEMENT
    // =========================================================================

    /**
     * Records a view of a tutorial.
     */
    void recordView(Long tutorialId, Long userId);

    /**
     * Records completion of a tutorial.
     */
    void recordCompletion(Long tutorialId, Long userId);

    /**
     * Adds rating to tutorial.
     */
    VideoTutorial rateTutorial(Long tutorialId, TeamMember rater, Integer rating);

    /**
     * Finds most viewed tutorials.
     */
    List<VideoTutorial> findMostViewed(Integer teamNumber, Integer season, Integer limit);

    /**
     * Finds most completed tutorials.
     */
    List<VideoTutorial> findMostCompleted(Integer teamNumber, Integer season, Integer limit);

    /**
     * Finds highest rated tutorials.
     */
    List<VideoTutorial> findHighestRated(Integer teamNumber, Integer season, Integer minRatingCount, Integer limit);

    /**
     * Finds tutorials with high engagement.
     */
    List<VideoTutorial> findHighEngagement(Integer teamNumber, Integer season, Double minScore, Integer limit);

    /**
     * Finds recently viewed tutorials.
     */
    List<VideoTutorial> findRecentlyViewed(Integer teamNumber, Integer season, Integer limit);

    /**
     * Updates engagement metrics for tutorial.
     */
    void updateEngagementMetrics(Long tutorialId);

    // =========================================================================
    // FEATURED AND SPECIAL TUTORIALS
    // =========================================================================

    /**
     * Finds featured tutorials.
     */
    List<VideoTutorial> findFeaturedTutorials(Integer teamNumber, Integer season);

    /**
     * Sets tutorial as featured.
     */
    VideoTutorial setTutorialFeatured(Long tutorialId, boolean featured);

    /**
     * Finds interactive tutorials.
     */
    List<VideoTutorial> findInteractiveTutorials(Integer teamNumber, Integer season);

    /**
     * Finds tutorials with resources.
     */
    List<VideoTutorial> findTutorialsWithResources(Integer teamNumber, Integer season);

    /**
     * Finds tutorials with subtitles.
     */
    List<VideoTutorial> findTutorialsWithSubtitles(Integer teamNumber, Integer season);

    /**
     * Finds tutorials with transcripts.
     */
    List<VideoTutorial> findTutorialsWithTranscripts(Integer teamNumber, Integer season);

    /**
     * Finds beginner-friendly tutorials.
     */
    List<VideoTutorial> findBeginnerFriendlyTutorials(Integer teamNumber, Integer season, Integer limit);

    // =========================================================================
    // VIDEO SOURCE AND TECHNICAL MANAGEMENT
    // =========================================================================

    /**
     * Finds tutorials by video source.
     */
    List<VideoTutorial> findByVideoSource(Integer teamNumber, Integer season, VideoTutorial.VideoSource videoSource);

    /**
     * Finds tutorial by external video ID.
     */
    Optional<VideoTutorial> findByExternalVideoId(String externalVideoId);

    /**
     * Updates video metadata (duration, quality, etc.).
     */
    VideoTutorial updateVideoMetadata(Long tutorialId, Duration duration, VideoTutorial.VideoQuality quality);

    /**
     * Adds subtitle to tutorial.
     */
    VideoTutorial addSubtitle(Long tutorialId, String subtitleUrl);

    /**
     * Adds transcript to tutorial.
     */
    VideoTutorial addTranscript(Long tutorialId, String transcript);

    /**
     * Processes video content features.
     */
    VideoTutorial processContentFeatures(Long tutorialId);

    // =========================================================================
    // MODERATION AND QUALITY CONTROL
    // =========================================================================

    /**
     * Finds flagged tutorials.
     */
    List<VideoTutorial> findFlaggedTutorials(Integer teamNumber, Integer season);

    /**
     * Finds tutorials pending moderation.
     */
    List<VideoTutorial> findPendingModeration(Integer teamNumber, Integer season);

    /**
     * Flags tutorial for review.
     */
    VideoTutorial flagTutorial(Long tutorialId, String reason);

    /**
     * Moderates tutorial.
     */
    VideoTutorial moderateTutorial(Long tutorialId, TeamMember moderator, boolean approved);

    /**
     * Sets quality score for tutorial.
     */
    VideoTutorial setQualityScore(Long tutorialId, Integer qualityScore);

    /**
     * Finds tutorials by quality score range.
     */
    List<VideoTutorial> findByQualityScoreRange(Integer teamNumber, Integer season, Integer minScore, Integer maxScore);

    /**
     * Validates tutorial content.
     */
    List<String> validateTutorialContent(VideoTutorial tutorial);

    // =========================================================================
    // TIME-BASED OPERATIONS
    // =========================================================================

    /**
     * Finds tutorials created within date range.
     */
    List<VideoTutorial> findCreatedInDateRange(Integer teamNumber, Integer season, 
                                             LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Finds recently created tutorials.
     */
    List<VideoTutorial> findRecentlyCreated(Integer teamNumber, Integer season, Integer days, Integer limit);

    /**
     * Finds stale tutorials (old and not updated).
     */
    List<VideoTutorial> findStaleTutorials(Integer teamNumber, Integer season, Integer days, Long minViews);

    /**
     * Finds tutorials needing update.
     */
    List<VideoTutorial> findTutorialsNeedingUpdate(Integer teamNumber, Integer season);

    /**
     * Marks tutorial as needing update.
     */
    VideoTutorial markTutorialForUpdate(Long tutorialId, String reason);

    // =========================================================================
    // INTERACTIVE FEATURES
    // =========================================================================

    /**
     * Adds quiz to tutorial.
     */
    VideoTutorial addQuiz(Long tutorialId, List<String> quizQuestions);

    /**
     * Adds chapter markers to tutorial.
     */
    VideoTutorial addChapters(Long tutorialId, List<String> chapters);

    /**
     * Adds key timestamps to tutorial.
     */
    VideoTutorial addKeyTimestamps(Long tutorialId, List<String> timestamps);

    /**
     * Adds downloadable resource to tutorial.
     */
    VideoTutorial addResource(Long tutorialId, String resourceUrl, String description);

    /**
     * Removes resource from tutorial.
     */
    VideoTutorial removeResource(Long tutorialId, String resourceUrl);

    // =========================================================================
    // CONTENT RELATIONSHIPS
    // =========================================================================

    /**
     * Associates tutorial with task.
     */
    VideoTutorial associateWithTask(Long tutorialId, Task task);

    /**
     * Associates tutorial with project.
     */
    VideoTutorial associateWithProject(Long tutorialId, Project project);

    /**
     * Associates tutorial with wiki page.
     */
    VideoTutorial associateWithWikiPage(Long tutorialId, WikiPage wikiPage);

    /**
     * Finds tutorials related to task.
     */
    List<VideoTutorial> findByRelatedTask(Long taskId);

    /**
     * Finds tutorials related to project.
     */
    List<VideoTutorial> findByRelatedProject(Long projectId);

    /**
     * Finds tutorials related to wiki page.
     */
    List<VideoTutorial> findByRelatedWikiPage(Long wikiPageId);

    /**
     * Finds unrelated tutorials.
     */
    List<VideoTutorial> findUnrelatedTutorials(Integer teamNumber, Integer season);

    // =========================================================================
    // RECOMMENDATIONS
    // =========================================================================

    /**
     * Finds similar tutorials.
     */
    List<VideoTutorial> findSimilarTutorials(Long tutorialId, Integer limit);

    /**
     * Finds recommended tutorials for user.
     */
    List<VideoTutorial> findRecommendedTutorials(Integer teamNumber, Integer season, Long userId, Integer limit);

    /**
     * Finds trending tutorials.
     */
    List<VideoTutorial> findTrendingTutorials(Integer teamNumber, Integer season, Integer days, Integer limit);

    /**
     * Finds tutorials by learning path.
     */
    List<VideoTutorial> findTutorialsByLearningPath(Integer teamNumber, Integer season, String skillPath);

    // =========================================================================
    // STATISTICS AND ANALYTICS
    // =========================================================================

    /**
     * Counts tutorials by category.
     */
    Map<VideoTutorial.TutorialCategory, Long> countByCategory(Integer teamNumber, Integer season);

    /**
     * Counts tutorials by skill level.
     */
    Map<VideoTutorial.SkillLevel, Long> countBySkillLevel(Integer teamNumber, Integer season);

    /**
     * Counts tutorials by status.
     */
    Map<VideoTutorial.TutorialStatus, Long> countByStatus(Integer teamNumber, Integer season);

    /**
     * Calculates total view count.
     */
    Long calculateTotalViewCount(Integer teamNumber, Integer season);

    /**
     * Calculates total completion count.
     */
    Long calculateTotalCompletionCount(Integer teamNumber, Integer season);

    /**
     * Calculates average engagement score.
     */
    Double calculateAverageEngagementScore(Integer teamNumber, Integer season);

    /**
     * Calculates average rating.
     */
    Double calculateAverageRating(Integer teamNumber, Integer season);

    /**
     * Finds most common tags.
     */
    Map<String, Long> findMostCommonTags(Integer teamNumber, Integer season);

    /**
     * Finds most common topics.
     */
    Map<String, Long> findMostCommonTopics(Integer teamNumber, Integer season);

    /**
     * Generates tutorial analytics report.
     */
    Map<String, Object> generateAnalyticsReport(Integer teamNumber, Integer season);

    // =========================================================================
    // CROSS-SEASON ANALYSIS
    // =========================================================================

    /**
     * Finds tutorials across multiple seasons.
     */
    List<VideoTutorial> findMultiSeasonTutorials(Integer teamNumber, List<Integer> seasons);

    /**
     * Finds tutorials with same title across seasons.
     */
    List<VideoTutorial> findByTitleAcrossSeasons(Integer teamNumber, String title);

    /**
     * Finds evergreen tutorials.
     */
    Map<String, Long> findEvergreenTutorials(Integer teamNumber);

    /**
     * Copies tutorial to new season.
     */
    VideoTutorial copyToNewSeason(Long tutorialId, Integer newSeason);

    // =========================================================================
    // BULK OPERATIONS
    // =========================================================================

    /**
     * Counts total active tutorials.
     */
    Long countActiveTutorials(Integer teamNumber, Integer season);

    /**
     * Finds all active tutorials for export.
     */
    List<VideoTutorial> findAllActiveTutorials(Integer teamNumber, Integer season);

    /**
     * Creates multiple tutorials.
     */
    List<VideoTutorial> createBulkTutorials(List<VideoTutorial> tutorials);

    /**
     * Updates multiple tutorials.
     */
    List<VideoTutorial> updateBulkTutorials(Map<Long, VideoTutorial> tutorialUpdates);

    /**
     * Archives multiple tutorials.
     */
    void bulkArchiveTutorials(List<Long> tutorialIds, String reason);

    /**
     * Updates search rankings.
     */
    void updateSearchRankings(Integer teamNumber, Integer season);

    // =========================================================================
    // ARCHIVE AND HISTORY
    // =========================================================================

    /**
     * Finds archived tutorials.
     */
    List<VideoTutorial> findArchivedTutorials(Integer teamNumber, Integer season);

    /**
     * Restores archived tutorial.
     */
    VideoTutorial restoreArchivedTutorial(Long tutorialId);

    /**
     * Permanently deletes tutorial.
     */
    void permanentlyDeleteTutorial(Long tutorialId);

    // =========================================================================
    // INTEGRATION AND EXTERNAL SYSTEMS
    // =========================================================================

    /**
     * Imports tutorial from external source.
     */
    VideoTutorial importFromExternalSource(Map<String, Object> tutorialData, String sourceType);

    /**
     * Exports tutorial to external format.
     */
    Map<String, Object> exportToExternalFormat(Long tutorialId, String format);

    /**
     * Syncs with external video platform.
     */
    VideoTutorial syncWithExternalPlatform(Long tutorialId);

    /**
     * Generates sitemap data.
     */
    List<Map<String, Object>> generateSitemap(Integer teamNumber, Integer season);

    // =========================================================================
    // LEARNING PROGRESS TRACKING
    // =========================================================================

    /**
     * Records learning progress for user.
     */
    void recordLearningProgress(Long tutorialId, Long userId, Double progressPercentage);

    /**
     * Gets user's learning progress.
     */
    Double getUserProgress(Long tutorialId, Long userId);

    /**
     * Finds tutorials in progress for user.
     */
    List<VideoTutorial> findTutorialsInProgress(Integer teamNumber, Integer season, Long userId);

    /**
     * Finds completed tutorials for user.
     */
    List<VideoTutorial> findCompletedTutorials(Integer teamNumber, Integer season, Long userId);

    /**
     * Generates learning report for user.
     */
    Map<String, Object> generateUserLearningReport(Integer teamNumber, Integer season, Long userId);

    // =========================================================================
    // VALIDATION AND BUSINESS RULES
    // =========================================================================

    /**
     * Validates tutorial data.
     */
    List<String> validateTutorial(VideoTutorial tutorial);

    /**
     * Validates video URL accessibility.
     */
    boolean validateVideoUrl(String videoUrl);

    /**
     * Validates user permissions for tutorial operation.
     */
    boolean validateUserPermissions(Long tutorialId, Long userId, String operation);

    /**
     * Checks tutorial content quality.
     */
    Map<String, Object> checkContentQuality(Long tutorialId);
}