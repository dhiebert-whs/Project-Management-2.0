// src/main/java/org/frcpm/services/BuildLogEntryService.java

package org.frcpm.services;

import org.frcpm.models.BuildLogEntry;
import org.frcpm.models.TeamMember;
import org.frcpm.models.Project;
import org.frcpm.models.Task;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for BuildLogEntry operations.
 * 
 * Provides comprehensive visual build log management services including
 * timeline generation, photo management, progress tracking, milestone
 * documentation, and collaborative build process documentation.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-KnowledgeManagement
 * @since Phase 4D.4 Visual Build Log with Photo Timeline
 */
public interface BuildLogEntryService {

    // =========================================================================
    // STANDARD SERVICE METHODS
    // =========================================================================

    /**
     * Creates a new build log entry.
     */
    BuildLogEntry create(BuildLogEntry entry);

    /**
     * Updates an existing build log entry.
     */
    BuildLogEntry update(Long id, BuildLogEntry entry);

    /**
     * Deletes a build log entry (soft delete).
     */
    void delete(Long id);

    /**
     * Finds a build log entry by ID.
     */
    Optional<BuildLogEntry> findById(Long id);

    /**
     * Finds all active build log entries.
     */
    List<BuildLogEntry> findAll();

    /**
     * Checks if build log entry exists by ID.
     */
    boolean existsById(Long id);

    /**
     * Counts total number of build log entries.
     */
    long count();

    // =========================================================================
    // ENTRY MANAGEMENT
    // =========================================================================

    /**
     * Creates a new build log entry with validation.
     */
    BuildLogEntry createEntry(BuildLogEntry entry);

    /**
     * Creates an entry with basic parameters.
     */
    BuildLogEntry createEntry(Integer teamNumber, Integer season, String title, 
                             BuildLogEntry.EntryType entryType, BuildLogEntry.BuildPhase buildPhase,
                             TeamMember createdBy);

    /**
     * Creates a work session entry.
     */
    BuildLogEntry createWorkSession(Integer teamNumber, Integer season, String title, String workAccomplished,
                                   LocalDateTime startTime, LocalDateTime endTime, TeamMember createdBy);

    /**
     * Creates a milestone entry.
     */
    BuildLogEntry createMilestone(Integer teamNumber, Integer season, String title, String milestoneDescription,
                                 Double progressPercentage, TeamMember createdBy);

    /**
     * Updates an existing entry with validation.
     */
    BuildLogEntry updateEntry(Long entryId, BuildLogEntry entry);

    /**
     * Archives a build log entry.
     */
    void archiveEntry(Long entryId, String reason);

    /**
     * Finds all active entries for a team and season.
     */
    List<BuildLogEntry> findActiveEntries(Integer teamNumber, Integer season);

    /**
     * Finds entries by type for a team and season.
     */
    List<BuildLogEntry> findByEntryType(Integer teamNumber, Integer season, BuildLogEntry.EntryType entryType);

    /**
     * Finds entries by build phase for a team and season.
     */
    List<BuildLogEntry> findByBuildPhase(Integer teamNumber, Integer season, BuildLogEntry.BuildPhase buildPhase);

    // =========================================================================
    // TIMELINE OPERATIONS
    // =========================================================================

    /**
     * Generates timeline entries for visualization.
     */
    List<BuildLogEntry> generateTimeline(Integer teamNumber, Integer season);

    /**
     * Finds timeline entries within date range.
     */
    List<BuildLogEntry> findTimelineEntriesInDateRange(Integer teamNumber, Integer season, 
                                                      LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Finds timeline highlights (milestones and featured entries).
     */
    List<BuildLogEntry> findTimelineHighlights(Integer teamNumber, Integer season);

    /**
     * Finds entries by build season day.
     */
    List<BuildLogEntry> findByBuildSeasonDay(Integer teamNumber, Integer season, Integer day);

    /**
     * Finds entries by build week.
     */
    List<BuildLogEntry> findByBuildWeek(Integer teamNumber, Integer season, Integer week);

    /**
     * Finds recent entries for dashboard.
     */
    List<BuildLogEntry> findRecentEntries(Integer teamNumber, Integer season, Integer days);

    /**
     * Generates timeline data for visualization components.
     */
    Map<String, Object> generateTimelineData(Integer teamNumber, Integer season);

    /**
     * Calculates build season day for entry date.
     */
    void calculateBuildSeasonDay(Long entryId, LocalDateTime kickoffDate);

    // =========================================================================
    // CONTENT SEARCH AND FILTERING
    // =========================================================================

    /**
     * Searches entries by title.
     */
    List<BuildLogEntry> searchByTitle(Integer teamNumber, Integer season, String searchTerm);

    /**
     * Searches entries by content.
     */
    List<BuildLogEntry> searchByContent(Integer teamNumber, Integer season, String searchTerm);

    /**
     * Full text search across multiple fields.
     */
    List<BuildLogEntry> fullTextSearch(Integer teamNumber, Integer season, String searchTerm);

    /**
     * Advanced search with multiple criteria.
     */
    List<BuildLogEntry> advancedSearch(Integer teamNumber, Integer season, Map<String, Object> criteria);

    /**
     * Finds entries by tag.
     */
    List<BuildLogEntry> findByTag(Integer teamNumber, Integer season, String tag);

    /**
     * Finds entries by subsystem.
     */
    List<BuildLogEntry> findBySubsystem(Integer teamNumber, Integer season, String subsystem);

    /**
     * Finds entries by component.
     */
    List<BuildLogEntry> findByComponent(Integer teamNumber, Integer season, String component);

    /**
     * Finds entries by completion status.
     */
    List<BuildLogEntry> findByCompletionStatus(Integer teamNumber, Integer season, 
                                              BuildLogEntry.CompletionStatus status);

    // =========================================================================
    // PHOTO AND MEDIA MANAGEMENT
    // =========================================================================

    /**
     * Adds a photo to an entry.
     */
    BuildLogEntry addPhoto(Long entryId, String photoUrl, String caption);

    /**
     * Removes a photo from an entry.
     */
    BuildLogEntry removePhoto(Long entryId, String photoUrl);

    /**
     * Sets primary photo for an entry.
     */
    BuildLogEntry setPrimaryPhoto(Long entryId, String photoUrl, String caption);

    /**
     * Adds a video to an entry.
     */
    BuildLogEntry addVideo(Long entryId, String videoUrl);

    /**
     * Removes a video from an entry.
     */
    BuildLogEntry removeVideo(Long entryId, String videoUrl);

    /**
     * Finds entries with photos.
     */
    List<BuildLogEntry> findEntriesWithPhotos(Integer teamNumber, Integer season);

    /**
     * Finds entries with videos.
     */
    List<BuildLogEntry> findEntriesWithVideos(Integer teamNumber, Integer season);

    /**
     * Finds photo-rich entries for galleries.
     */
    List<BuildLogEntry> findPhotoRichEntries(Integer teamNumber, Integer season, Integer minPhotos);

    /**
     * Finds entries without media.
     */
    List<BuildLogEntry> findEntriesWithoutMedia(Integer teamNumber, Integer season);

    /**
     * Gets all photo URLs for team gallery.
     */
    List<String> getAllPhotoUrls(Integer teamNumber, Integer season);

    /**
     * Generates photo gallery data.
     */
    Map<String, Object> generatePhotoGalleryData(Integer teamNumber, Integer season);

    // =========================================================================
    // PROJECT AND TASK ASSOCIATIONS
    // =========================================================================

    /**
     * Associates entry with project.
     */
    BuildLogEntry associateWithProject(Long entryId, Project project);

    /**
     * Associates entry with task.
     */
    BuildLogEntry associateWithTask(Long entryId, Task task);

    /**
     * Removes project association.
     */
    BuildLogEntry removeProjectAssociation(Long entryId);

    /**
     * Removes task association.
     */
    BuildLogEntry removeTaskAssociation(Long entryId);

    /**
     * Finds entries related to project.
     */
    List<BuildLogEntry> findByProject(Project project, Integer teamNumber, Integer season);

    /**
     * Finds entries related to task.
     */
    List<BuildLogEntry> findByTask(Task task, Integer teamNumber, Integer season);

    /**
     * Finds unassociated entries.
     */
    List<BuildLogEntry> findUnassociatedEntries(Integer teamNumber, Integer season);

    /**
     * Finds entries without tasks.
     */
    List<BuildLogEntry> findEntriesWithoutTasks(Integer teamNumber, Integer season);

    // =========================================================================
    // PARTICIPANT MANAGEMENT
    // =========================================================================

    /**
     * Adds participant to entry.
     */
    BuildLogEntry addParticipant(Long entryId, Long participantId, String participantName);

    /**
     * Removes participant from entry.
     */
    BuildLogEntry removeParticipant(Long entryId, Long participantId);

    /**
     * Finds entries by creator.
     */
    List<BuildLogEntry> findByCreator(TeamMember creator, Integer teamNumber, Integer season);

    /**
     * Finds entries by participant.
     */
    List<BuildLogEntry> findByParticipant(Integer teamNumber, Integer season, Long participantId);

    /**
     * Gets participant statistics.
     */
    Map<String, Object> getParticipantStatistics(Integer teamNumber, Integer season, Long participantId);

    /**
     * Finds most active contributors.
     */
    List<Object[]> findMostActiveContributors(Integer teamNumber, Integer season);

    // =========================================================================
    // PROGRESS AND MILESTONE TRACKING
    // =========================================================================

    /**
     * Updates progress percentage for entry.
     */
    BuildLogEntry updateProgress(Long entryId, Double progressPercentage);

    /**
     * Marks entry as milestone.
     */
    BuildLogEntry markAsMilestone(Long entryId, String milestoneDescription);

    /**
     * Removes milestone designation.
     */
    BuildLogEntry unmarkAsMilestone(Long entryId);

    /**
     * Finds milestone entries.
     */
    List<BuildLogEntry> findMilestoneEntries(Integer teamNumber, Integer season);

    /**
     * Finds entries by progress range.
     */
    List<BuildLogEntry> findByProgressRange(Integer teamNumber, Integer season, 
                                           Double minProgress, Double maxProgress);

    /**
     * Finds completed entries.
     */
    List<BuildLogEntry> findCompletedEntries(Integer teamNumber, Integer season);

    /**
     * Finds problematic entries (blocked, on hold, failed).
     */
    List<BuildLogEntry> findProblematicEntries(Integer teamNumber, Integer season);

    /**
     * Calculates overall progress for team.
     */
    Double calculateOverallProgress(Integer teamNumber, Integer season);

    /**
     * Generates progress report.
     */
    Map<String, Object> generateProgressReport(Integer teamNumber, Integer season);

    // =========================================================================
    // TECHNICAL DOCUMENTATION
    // =========================================================================

    /**
     * Adds material to entry.
     */
    BuildLogEntry addMaterial(Long entryId, String material);

    /**
     * Adds tool to entry.
     */
    BuildLogEntry addTool(Long entryId, String tool);

    /**
     * Adds technique to entry.
     */
    BuildLogEntry addTechnique(Long entryId, String technique);

    /**
     * Updates technical notes.
     */
    BuildLogEntry updateTechnicalNotes(Long entryId, String technicalNotes);

    /**
     * Updates safety notes.
     */
    BuildLogEntry updateSafetyNotes(Long entryId, String safetyNotes);

    /**
     * Adds measurement to entry.
     */
    BuildLogEntry addMeasurement(Long entryId, Double value, String unit, String description);

    /**
     * Finds entries by material.
     */
    List<BuildLogEntry> findByMaterial(Integer teamNumber, Integer season, String material);

    /**
     * Finds entries by tool.
     */
    List<BuildLogEntry> findByTool(Integer teamNumber, Integer season, String tool);

    /**
     * Finds entries by technique.
     */
    List<BuildLogEntry> findByTechnique(Integer teamNumber, Integer season, String technique);

    /**
     * Finds entries with safety notes.
     */
    List<BuildLogEntry> findEntriesWithSafetyNotes(Integer teamNumber, Integer season);

    /**
     * Finds entries with measurements.
     */
    List<BuildLogEntry> findEntriesWithMeasurements(Integer teamNumber, Integer season);

    // =========================================================================
    // WORK SESSION TRACKING
    // =========================================================================

    /**
     * Sets work session times for entry.
     */
    BuildLogEntry setWorkSession(Long entryId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Records work duration.
     */
    BuildLogEntry recordWorkDuration(Long entryId, Integer durationMinutes);

    /**
     * Finds entries with work duration tracking.
     */
    List<BuildLogEntry> findEntriesWithWorkDuration(Integer teamNumber, Integer season);

    /**
     * Finds long work sessions.
     */
    List<BuildLogEntry> findLongWorkSessions(Integer teamNumber, Integer season, Integer minMinutes);

    /**
     * Calculates total work hours.
     */
    Double calculateTotalWorkHours(Integer teamNumber, Integer season);

    /**
     * Finds entries by work location.
     */
    List<BuildLogEntry> findByWorkLocation(Integer teamNumber, Integer season, String location);

    /**
     * Generates work session report.
     */
    Map<String, Object> generateWorkSessionReport(Integer teamNumber, Integer season);

    // =========================================================================
    // QUALITY AND ENGAGEMENT
    // =========================================================================

    /**
     * Updates content quality score.
     */
    BuildLogEntry updateContentQuality(Long entryId);

    /**
     * Records view for entry.
     */
    BuildLogEntry recordView(Long entryId, Long userId);

    /**
     * Records helpful vote.
     */
    BuildLogEntry recordHelpfulVote(Long entryId, Long userId);

    /**
     * Updates engagement metrics.
     */
    BuildLogEntry updateEngagementMetrics(Long entryId);

    /**
     * Finds most viewed entries.
     */
    List<BuildLogEntry> findMostViewed(Integer teamNumber, Integer season, Integer limit);

    /**
     * Finds highest engagement entries.
     */
    List<BuildLogEntry> findHighEngagement(Integer teamNumber, Integer season, Double minScore, Integer limit);

    /**
     * Finds highest quality entries.
     */
    List<BuildLogEntry> findHighQuality(Integer teamNumber, Integer season, Integer minScore, Integer limit);

    /**
     * Finds most helpful entries.
     */
    List<BuildLogEntry> findMostHelpful(Integer teamNumber, Integer season, Integer minVotes, Integer limit);

    /**
     * Sets entry as featured.
     */
    BuildLogEntry setFeatured(Long entryId, boolean featured);

    /**
     * Finds featured entries.
     */
    List<BuildLogEntry> findFeaturedEntries(Integer teamNumber, Integer season);

    // =========================================================================
    // COMPETITION AND EVENT TRACKING
    // =========================================================================

    /**
     * Sets target event for entry.
     */
    BuildLogEntry setTargetEvent(Long entryId, String event, LocalDateTime targetDate);

    /**
     * Marks entry as competition ready.
     */
    BuildLogEntry markCompetitionReady(Long entryId, boolean ready);

    /**
     * Finds entries by target event.
     */
    List<BuildLogEntry> findByTargetEvent(Integer teamNumber, Integer season, String event);

    /**
     * Finds competition-ready entries.
     */
    List<BuildLogEntry> findCompetitionReadyEntries(Integer teamNumber, Integer season);

    /**
     * Finds entries with approaching deadlines.
     */
    List<BuildLogEntry> findWithApproachingDeadlines(Integer teamNumber, Integer season, Integer days);

    /**
     * Generates competition readiness report.
     */
    Map<String, Object> generateCompetitionReadinessReport(Integer teamNumber, Integer season);

    // =========================================================================
    // TAGGING AND CATEGORIZATION
    // =========================================================================

    /**
     * Adds tag to entry.
     */
    BuildLogEntry addTag(Long entryId, String tag);

    /**
     * Removes tag from entry.
     */
    BuildLogEntry removeTag(Long entryId, String tag);

    /**
     * Sets tags for entry.
     */
    BuildLogEntry setTags(Long entryId, List<String> tags);

    /**
     * Finds most common tags.
     */
    Map<String, Long> findMostCommonTags(Integer teamNumber, Integer season);

    /**
     * Generates tag cloud data.
     */
    Map<String, Object> generateTagCloudData(Integer teamNumber, Integer season);

    /**
     * Auto-generates tags for entry.
     */
    BuildLogEntry autoGenerateTags(Long entryId);

    // =========================================================================
    // MODERATION AND REVIEW
    // =========================================================================

    /**
     * Submits entry for review.
     */
    BuildLogEntry submitForReview(Long entryId);

    /**
     * Reviews entry.
     */
    BuildLogEntry reviewEntry(Long entryId, TeamMember reviewer, String comments, boolean approved);

    /**
     * Flags entry for moderation.
     */
    BuildLogEntry flagEntry(Long entryId, String reason);

    /**
     * Moderates entry.
     */
    BuildLogEntry moderateEntry(Long entryId, TeamMember moderator, boolean approved);

    /**
     * Finds entries pending review.
     */
    List<BuildLogEntry> findPendingReview(Integer teamNumber, Integer season);

    /**
     * Finds entries pending moderation.
     */
    List<BuildLogEntry> findPendingModeration(Integer teamNumber, Integer season);

    /**
     * Finds flagged entries.
     */
    List<BuildLogEntry> findFlaggedEntries(Integer teamNumber, Integer season);

    /**
     * Finds low quality entries.
     */
    List<BuildLogEntry> findLowQualityEntries(Integer teamNumber, Integer season, Integer maxScore);

    // =========================================================================
    // STATISTICS AND ANALYTICS
    // =========================================================================

    /**
     * Counts entries by entry type.
     */
    Map<BuildLogEntry.EntryType, Long> countByEntryType(Integer teamNumber, Integer season);

    /**
     * Counts entries by build phase.
     */
    Map<BuildLogEntry.BuildPhase, Long> countByBuildPhase(Integer teamNumber, Integer season);

    /**
     * Counts entries by completion status.
     */
    Map<BuildLogEntry.CompletionStatus, Long> countByCompletionStatus(Integer teamNumber, Integer season);

    /**
     * Counts entries by subsystem.
     */
    Map<String, Long> countBySubsystem(Integer teamNumber, Integer season);

    /**
     * Calculates total photo count.
     */
    Long calculateTotalPhotoCount(Integer teamNumber, Integer season);

    /**
     * Calculates average content quality.
     */
    Double calculateAverageContentQuality(Integer teamNumber, Integer season);

    /**
     * Calculates average engagement score.
     */
    Double calculateAverageEngagementScore(Integer teamNumber, Integer season);

    /**
     * Finds most used materials.
     */
    Map<String, Long> findMostUsedMaterials(Integer teamNumber, Integer season);

    /**
     * Finds most used tools.
     */
    Map<String, Long> findMostUsedTools(Integer teamNumber, Integer season);

    /**
     * Generates comprehensive analytics report.
     */
    Map<String, Object> generateAnalyticsReport(Integer teamNumber, Integer season);

    /**
     * Generates build progress dashboard data.
     */
    Map<String, Object> generateDashboardData(Integer teamNumber, Integer season);

    // =========================================================================
    // CROSS-SEASON ANALYSIS
    // =========================================================================

    /**
     * Finds entries across multiple seasons.
     */
    List<BuildLogEntry> findMultiSeasonEntries(Integer teamNumber, List<Integer> seasons);

    /**
     * Finds subsystem work across seasons.
     */
    List<BuildLogEntry> findSubsystemAcrossSeasons(Integer teamNumber, String subsystem);

    /**
     * Finds recurring build patterns.
     */
    List<Object[]> findRecurringBuildPatterns(Integer teamNumber);

    /**
     * Compares seasons performance.
     */
    Map<String, Object> compareSeasons(Integer teamNumber, List<Integer> seasons);

    /**
     * Analyzes team evolution across seasons.
     */
    Map<String, Object> analyzeTeamEvolution(Integer teamNumber, List<Integer> seasons);

    // =========================================================================
    // BULK OPERATIONS
    // =========================================================================

    /**
     * Counts total active entries.
     */
    Long countActiveEntries(Integer teamNumber, Integer season);

    /**
     * Finds all active entries for export.
     */
    List<BuildLogEntry> findAllActiveEntries(Integer teamNumber, Integer season);

    /**
     * Creates multiple entries.
     */
    List<BuildLogEntry> createBulkEntries(List<BuildLogEntry> entries);

    /**
     * Updates multiple entries.
     */
    List<BuildLogEntry> updateBulkEntries(Map<Long, BuildLogEntry> entryUpdates);

    /**
     * Archives multiple entries.
     */
    void bulkArchiveEntries(List<Long> entryIds, String reason);

    /**
     * Updates search rankings.
     */
    void updateSearchRankings(Integer teamNumber, Integer season);

    /**
     * Updates all content quality scores.
     */
    void updateAllContentQuality(Integer teamNumber, Integer season);

    // =========================================================================
    // ARCHIVE AND HISTORY
    // =========================================================================

    /**
     * Finds archived entries.
     */
    List<BuildLogEntry> findArchivedEntries(Integer teamNumber, Integer season);

    /**
     * Restores archived entry.
     */
    BuildLogEntry restoreArchivedEntry(Long entryId);

    /**
     * Permanently deletes entry.
     */
    void permanentlyDeleteEntry(Long entryId);

    /**
     * Finds entries archived within date range.
     */
    List<BuildLogEntry> findArchivedInDateRange(Integer teamNumber, Integer season, 
                                               LocalDateTime startDate, LocalDateTime endDate);

    // =========================================================================
    // INTEGRATION AND EXPORT
    // =========================================================================

    /**
     * Exports entry to external format.
     */
    Map<String, Object> exportToExternalFormat(Long entryId, String format);

    /**
     * Imports entry from external source.
     */
    BuildLogEntry importFromExternalSource(Map<String, Object> entryData, String sourceType);

    /**
     * Generates sitemap data.
     */
    List<Map<String, Object>> generateSitemap(Integer teamNumber, Integer season);

    /**
     * Syncs with external photo services.
     */
    BuildLogEntry syncPhotosWithExternalService(Long entryId, String serviceType);

    /**
     * Generates RSS feed data.
     */
    Map<String, Object> generateRssFeedData(Integer teamNumber, Integer season);

    // =========================================================================
    // VALIDATION AND BUSINESS RULES
    // =========================================================================

    /**
     * Validates entry data.
     */
    List<String> validateEntry(BuildLogEntry entry);

    /**
     * Validates photo URL accessibility.
     */
    boolean validatePhotoUrl(String photoUrl);

    /**
     * Validates user permissions for entry operation.
     */
    boolean validateUserPermissions(Long entryId, Long userId, String operation);

    /**
     * Checks entry content quality.
     */
    Map<String, Object> checkContentQuality(Long entryId);

    /**
     * Suggests improvements for entry.
     */
    List<String> suggestImprovements(Long entryId);
}