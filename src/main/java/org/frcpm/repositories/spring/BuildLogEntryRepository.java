// src/main/java/org/frcpm/repositories/spring/BuildLogEntryRepository.java

package org.frcpm.repositories.spring;

import org.frcpm.models.BuildLogEntry;
import org.frcpm.models.TeamMember;
import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for BuildLogEntry entities.
 * 
 * Provides comprehensive data access for visual build log management including
 * timeline queries, photo management, progress tracking, and build process
 * documentation for FRC teams.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-KnowledgeManagement
 * @since Phase 4D.4 Visual Build Log with Photo Timeline
 */
@Repository
public interface BuildLogEntryRepository extends JpaRepository<BuildLogEntry, Long> {

    // =========================================================================
    // BASIC QUERIES
    // =========================================================================

    /**
     * Finds all active entries for a specific team and season.
     */
    List<BuildLogEntry> findByTeamNumberAndSeasonAndIsActiveTrue(Integer teamNumber, Integer season);

    /**
     * Finds entries by entry type for a team and season.
     */
    List<BuildLogEntry> findByTeamNumberAndSeasonAndEntryTypeAndIsActiveTrue(
        Integer teamNumber, Integer season, BuildLogEntry.EntryType entryType);

    /**
     * Finds entries by build phase for a team and season.
     */
    List<BuildLogEntry> findByTeamNumberAndSeasonAndBuildPhaseAndIsActiveTrue(
        Integer teamNumber, Integer season, BuildLogEntry.BuildPhase buildPhase);

    /**
     * Finds entries by completion status for a team and season.
     */
    List<BuildLogEntry> findByTeamNumberAndSeasonAndCompletionStatusAndIsActiveTrue(
        Integer teamNumber, Integer season, BuildLogEntry.CompletionStatus completionStatus);

    /**
     * Finds entries by visibility level for a team and season.
     */
    List<BuildLogEntry> findByTeamNumberAndSeasonAndVisibilityAndIsActiveTrue(
        Integer teamNumber, Integer season, BuildLogEntry.VisibilityLevel visibility);

    /**
     * Finds entries by priority for a team and season.
     */
    List<BuildLogEntry> findByTeamNumberAndSeasonAndPriorityAndIsActiveTrue(
        Integer teamNumber, Integer season, BuildLogEntry.Priority priority);

    // =========================================================================
    // TIMELINE AND CHRONOLOGICAL QUERIES
    // =========================================================================

    /**
     * Finds all entries for timeline display ordered by entry date.
     */
    @Query("SELECT ble FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber AND ble.season = :season " +
           "AND ble.isActive = true ORDER BY ble.entryDateTime DESC")
    List<BuildLogEntry> findTimelineEntries(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds entries within date range for timeline.
     */
    @Query("SELECT ble FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber AND ble.season = :season " +
           "AND ble.entryDateTime BETWEEN :startDate AND :endDate AND ble.isActive = true " +
           "ORDER BY ble.entryDateTime ASC")
    List<BuildLogEntry> findTimelineEntriesInDateRange(@Param("teamNumber") Integer teamNumber,
                                                      @Param("season") Integer season,
                                                      @Param("startDate") LocalDateTime startDate,
                                                      @Param("endDate") LocalDateTime endDate);

    /**
     * Finds entries by build season day.
     */
    @Query("SELECT ble FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber AND ble.season = :season " +
           "AND ble.buildSeasonDay = :day AND ble.isActive = true ORDER BY ble.entryDateTime ASC")
    List<BuildLogEntry> findByBuildSeasonDay(@Param("teamNumber") Integer teamNumber,
                                            @Param("season") Integer season,
                                            @Param("day") Integer day);

    /**
     * Finds entries by build week.
     */
    @Query("SELECT ble FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber AND ble.season = :season " +
           "AND ble.buildWeek = :week AND ble.isActive = true ORDER BY ble.entryDateTime ASC")
    List<BuildLogEntry> findByBuildWeek(@Param("teamNumber") Integer teamNumber,
                                       @Param("season") Integer season,
                                       @Param("week") Integer week);

    /**
     * Finds timeline highlights (featured or milestone entries).
     */
    @Query("SELECT ble FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber AND ble.season = :season " +
           "AND (ble.isHighlight = true OR ble.isMilestone = true OR ble.isFeatured = true) " +
           "AND ble.isActive = true ORDER BY ble.entryDateTime DESC")
    List<BuildLogEntry> findTimelineHighlights(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds recent entries for dashboard display.
     */
    @Query("SELECT ble FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber AND ble.season = :season " +
           "AND ble.entryDateTime >= :since AND ble.isActive = true " +
           "ORDER BY ble.entryDateTime DESC")
    List<BuildLogEntry> findRecentEntries(@Param("teamNumber") Integer teamNumber,
                                         @Param("season") Integer season,
                                         @Param("since") LocalDateTime since);

    // =========================================================================
    // CONTENT SEARCH AND FILTERING
    // =========================================================================

    // Note: searchByTitle query removed - LIKE CONCAT validation issues in H2

    // Note: searchByContent query removed - LIKE CONCAT validation issues in H2

    // Note: fullTextSearch query removed - LIKE CONCAT validation issues in H2

    /**
     * Finds entries by tag.
     */
    @Query("SELECT ble FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber AND ble.season = :season " +
           "AND :tag MEMBER OF ble.tags AND ble.isActive = true")
    List<BuildLogEntry> findByTag(@Param("teamNumber") Integer teamNumber,
                                 @Param("season") Integer season,
                                 @Param("tag") String tag);

    /**
     * Finds entries by subsystem.
     */
    @Query("SELECT ble FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber AND ble.season = :season " +
           "AND LOWER(ble.subsystem) = LOWER(:subsystem) AND ble.isActive = true " +
           "ORDER BY ble.entryDateTime DESC")
    List<BuildLogEntry> findBySubsystem(@Param("teamNumber") Integer teamNumber,
                                       @Param("season") Integer season,
                                       @Param("subsystem") String subsystem);

    /**
     * Finds entries by component.
     */
    @Query("SELECT ble FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber AND ble.season = :season " +
           "AND LOWER(ble.component) = LOWER(:component) AND ble.isActive = true " +
           "ORDER BY ble.entryDateTime DESC")
    List<BuildLogEntry> findByComponent(@Param("teamNumber") Integer teamNumber,
                                       @Param("season") Integer season,
                                       @Param("component") String component);

    // =========================================================================
    // PROJECT AND TASK RELATIONS
    // =========================================================================

    /**
     * Finds entries related to specific project.
     */
    List<BuildLogEntry> findByRelatedProjectAndTeamNumberAndSeasonAndIsActiveTrue(
        Project relatedProject, Integer teamNumber, Integer season);

    /**
     * Finds entries related to specific task.
     */
    List<BuildLogEntry> findByRelatedTaskAndTeamNumberAndSeasonAndIsActiveTrue(
        Task relatedTask, Integer teamNumber, Integer season);

    /**
     * Finds entries without project association.
     */
    @Query("SELECT ble FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber AND ble.season = :season " +
           "AND ble.relatedProject IS NULL AND ble.isActive = true " +
           "ORDER BY ble.entryDateTime DESC")
    List<BuildLogEntry> findUnassociatedEntries(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds entries without task association.
     */
    @Query("SELECT ble FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber AND ble.season = :season " +
           "AND ble.relatedTask IS NULL AND ble.isActive = true " +
           "ORDER BY ble.entryDateTime DESC")
    List<BuildLogEntry> findEntriesWithoutTasks(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    // =========================================================================
    // CREATOR AND PARTICIPANT QUERIES
    // =========================================================================

    /**
     * Finds entries created by specific team member.
     */
    List<BuildLogEntry> findByCreatedByAndTeamNumberAndSeasonAndIsActiveTrue(
        TeamMember createdBy, Integer teamNumber, Integer season);

    /**
     * Finds entries where team member participated.
     */
    @Query("SELECT ble FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber AND ble.season = :season " +
           "AND :participantId MEMBER OF ble.participantIds AND ble.isActive = true " +
           "ORDER BY ble.entryDateTime DESC")
    List<BuildLogEntry> findByParticipant(@Param("teamNumber") Integer teamNumber,
                                         @Param("season") Integer season,
                                         @Param("participantId") Long participantId);

    /**
     * Finds entries updated by specific team member.
     */
    List<BuildLogEntry> findByUpdatedByAndTeamNumberAndSeasonAndIsActiveTrue(
        TeamMember updatedBy, Integer teamNumber, Integer season);

    /**
     * Finds entries reviewed by specific team member.
     */
    List<BuildLogEntry> findByReviewedByAndTeamNumberAndSeasonAndIsActiveTrue(
        TeamMember reviewedBy, Integer teamNumber, Integer season);

    // =========================================================================
    // PHOTO AND MEDIA QUERIES
    // =========================================================================

    /**
     * Finds entries with photos.
     */
    @Query("SELECT ble FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber AND ble.season = :season " +
           "AND ble.photoCount > 0 AND ble.isActive = true ORDER BY ble.entryDateTime DESC")
    List<BuildLogEntry> findEntriesWithPhotos(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds entries with videos.
     */
    @Query("SELECT ble FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber AND ble.season = :season " +
           "AND ble.videoCount > 0 AND ble.isActive = true ORDER BY ble.entryDateTime DESC")
    List<BuildLogEntry> findEntriesWithVideos(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds entries with high photo count for photo galleries.
     */
    @Query("SELECT ble FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber AND ble.season = :season " +
           "AND ble.photoCount >= :minPhotos AND ble.isActive = true " +
           "ORDER BY ble.photoCount DESC, ble.entryDateTime DESC")
    List<BuildLogEntry> findPhotoRichEntries(@Param("teamNumber") Integer teamNumber,
                                            @Param("season") Integer season,
                                            @Param("minPhotos") Integer minPhotos);

    /**
     * Finds entries without any media.
     */
    @Query("SELECT ble FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber AND ble.season = :season " +
           "AND ble.photoCount = 0 AND ble.videoCount = 0 AND ble.isActive = true " +
           "ORDER BY ble.entryDateTime DESC")
    List<BuildLogEntry> findEntriesWithoutMedia(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds all photo URLs for team photo gallery.
     */
    @Query("SELECT photoUrl FROM BuildLogEntry ble JOIN ble.photoUrls photoUrl " +
           "WHERE ble.teamNumber = :teamNumber AND ble.season = :season " +
           "AND ble.isActive = true AND ble.isPublic = true")
    List<String> findAllPhotoUrls(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    // =========================================================================
    // PROGRESS AND MILESTONE TRACKING
    // =========================================================================

    /**
     * Finds milestone entries.
     */
    @Query("SELECT ble FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber AND ble.season = :season " +
           "AND (ble.isMilestone = true OR ble.entryType = 'MILESTONE') AND ble.isActive = true " +
           "ORDER BY ble.entryDateTime ASC")
    List<BuildLogEntry> findMilestoneEntries(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds entries by progress percentage range.
     */
    @Query("SELECT ble FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber AND ble.season = :season " +
           "AND ble.progressPercentage BETWEEN :minProgress AND :maxProgress AND ble.isActive = true " +
           "ORDER BY ble.progressPercentage DESC")
    List<BuildLogEntry> findByProgressRange(@Param("teamNumber") Integer teamNumber,
                                           @Param("season") Integer season,
                                           @Param("minProgress") Double minProgress,
                                           @Param("maxProgress") Double maxProgress);

    /**
     * Finds completed work entries.
     */
    @Query("SELECT ble FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber AND ble.season = :season " +
           "AND ble.completionStatus = 'COMPLETED' AND ble.isActive = true " +
           "ORDER BY ble.entryDateTime DESC")
    List<BuildLogEntry> findCompletedEntries(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds blocked or problematic entries.
     */
    @Query("SELECT ble FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber AND ble.season = :season " +
           "AND (ble.completionStatus IN ('BLOCKED', 'ON_HOLD', 'FAILED') OR ble.problemCount > 0 OR ble.requiresFollowUp = true) " +
           "AND ble.isActive = true ORDER BY ble.entryDateTime DESC")
    List<BuildLogEntry> findProblematicEntries(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    // =========================================================================
    // ANALYTICS AND ENGAGEMENT
    // =========================================================================

    /**
     * Finds most viewed entries.
     */
    @Query("SELECT ble FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber AND ble.season = :season " +
           "AND ble.isActive = true ORDER BY ble.viewCount DESC")
    List<BuildLogEntry> findMostViewed(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    // Note: High engagement query removed - entity may not have engagementScore field

    // Note: High quality query removed - entity may not have contentQualityScore field

    /**
     * Finds most helpful entries (high helpful votes).
     */
    @Query("SELECT ble FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber AND ble.season = :season " +
           "AND ble.helpfulVotes >= :minVotes AND ble.isActive = true " +
           "ORDER BY ble.helpfulVotes DESC")
    List<BuildLogEntry> findMostHelpful(@Param("teamNumber") Integer teamNumber,
                                       @Param("season") Integer season,
                                       @Param("minVotes") Integer minVotes);

    /**
     * Finds featured entries.
     */
    @Query("SELECT ble FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber AND ble.season = :season " +
           "AND ble.isFeatured = true AND ble.isActive = true " +
           "ORDER BY ble.entryDateTime DESC")
    List<BuildLogEntry> findFeaturedEntries(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    // =========================================================================
    // TECHNICAL AND SPECIFICATION QUERIES
    // =========================================================================

    /**
     * Finds entries by material used.
     */
    @Query("SELECT ble FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber AND ble.season = :season " +
           "AND :material MEMBER OF ble.materialsUsed AND ble.isActive = true " +
           "ORDER BY ble.entryDateTime DESC")
    List<BuildLogEntry> findByMaterial(@Param("teamNumber") Integer teamNumber,
                                      @Param("season") Integer season,
                                      @Param("material") String material);

    /**
     * Finds entries by tool used.
     */
    @Query("SELECT ble FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber AND ble.season = :season " +
           "AND :tool MEMBER OF ble.toolsUsed AND ble.isActive = true " +
           "ORDER BY ble.entryDateTime DESC")
    List<BuildLogEntry> findByTool(@Param("teamNumber") Integer teamNumber,
                                  @Param("season") Integer season,
                                  @Param("tool") String tool);

    /**
     * Finds entries by technique used.
     */
    @Query("SELECT ble FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber AND ble.season = :season " +
           "AND :technique MEMBER OF ble.techniquesUsed AND ble.isActive = true " +
           "ORDER BY ble.entryDateTime DESC")
    List<BuildLogEntry> findByTechnique(@Param("teamNumber") Integer teamNumber,
                                       @Param("season") Integer season,
                                       @Param("technique") String technique);

    /**
     * Finds entries with safety notes.
     */
    @Query("SELECT ble FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber AND ble.season = :season " +
           "AND ble.safetyNotes IS NOT NULL AND ble.safetyNotes != '' AND ble.isActive = true " +
           "ORDER BY ble.entryDateTime DESC")
    List<BuildLogEntry> findEntriesWithSafetyNotes(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds entries with measurements.
     */
    @Query("SELECT ble FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber AND ble.season = :season " +
           "AND ble.measurementValue IS NOT NULL AND ble.isActive = true " +
           "ORDER BY ble.entryDateTime DESC")
    List<BuildLogEntry> findEntriesWithMeasurements(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    // =========================================================================
    // WORK SESSION AND TIME TRACKING
    // =========================================================================

    /**
     * Finds entries with work duration tracking.
     */
    @Query("SELECT ble FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber AND ble.season = :season " +
           "AND ble.workDurationMinutes IS NOT NULL AND ble.workDurationMinutes > 0 AND ble.isActive = true " +
           "ORDER BY ble.workDurationMinutes DESC")
    List<BuildLogEntry> findEntriesWithWorkDuration(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds long work sessions.
     */
    @Query("SELECT ble FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber AND ble.season = :season " +
           "AND ble.workDurationMinutes >= :minMinutes AND ble.isActive = true " +
           "ORDER BY ble.workDurationMinutes DESC")
    List<BuildLogEntry> findLongWorkSessions(@Param("teamNumber") Integer teamNumber,
                                            @Param("season") Integer season,
                                            @Param("minMinutes") Integer minMinutes);

    /**
     * Calculates total work hours for team and season.
     */
    @Query("SELECT SUM(ble.workDurationMinutes) FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber " +
           "AND ble.season = :season AND ble.workDurationMinutes IS NOT NULL AND ble.isActive = true")
    Optional<Long> calculateTotalWorkMinutes(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds entries by work location.
     */
    @Query("SELECT ble FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber AND ble.season = :season " +
           "AND LOWER(ble.workLocation) = LOWER(:location) AND ble.isActive = true " +
           "ORDER BY ble.entryDateTime DESC")
    List<BuildLogEntry> findByWorkLocation(@Param("teamNumber") Integer teamNumber,
                                          @Param("season") Integer season,
                                          @Param("location") String location);

    // =========================================================================
    // COMPETITION AND EVENT TRACKING
    // =========================================================================

    /**
     * Finds entries targeting specific event.
     */
    @Query("SELECT ble FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber AND ble.season = :season " +
           "AND LOWER(ble.targetEvent) = LOWER(:event) AND ble.isActive = true " +
           "ORDER BY ble.targetDate ASC")
    List<BuildLogEntry> findByTargetEvent(@Param("teamNumber") Integer teamNumber,
                                         @Param("season") Integer season,
                                         @Param("event") String event);

    /**
     * Finds competition-ready entries.
     */
    @Query("SELECT ble FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber AND ble.season = :season " +
           "AND ble.isCompetitionReady = true AND ble.isActive = true " +
           "ORDER BY ble.entryDateTime DESC")
    List<BuildLogEntry> findCompetitionReadyEntries(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds entries with approaching deadlines.
     */
    @Query("SELECT ble FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber AND ble.season = :season " +
           "AND ble.targetDate BETWEEN :now AND :deadline AND ble.completionStatus != 'COMPLETED' " +
           "AND ble.isActive = true ORDER BY ble.targetDate ASC")
    List<BuildLogEntry> findWithApproachingDeadlines(@Param("teamNumber") Integer teamNumber,
                                                     @Param("season") Integer season,
                                                     @Param("now") LocalDateTime now,
                                                     @Param("deadline") LocalDateTime deadline);

    // =========================================================================
    // MODERATION AND QUALITY CONTROL
    // =========================================================================

    /**
     * Finds entries pending moderation.
     */
    @Query("SELECT ble FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber AND ble.season = :season " +
           "AND ble.isModerated = false AND ble.isActive = true " +
           "ORDER BY ble.entryDateTime ASC")
    List<BuildLogEntry> findPendingModeration(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds flagged entries.
     */
    @Query("SELECT ble FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber AND ble.season = :season " +
           "AND ble.isFlagged = true AND ble.isActive = true " +
           "ORDER BY ble.entryDateTime DESC")
    List<BuildLogEntry> findFlaggedEntries(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds entries by moderator.
     */
    List<BuildLogEntry> findByModeratedByAndTeamNumberAndSeasonAndIsActiveTrue(
        TeamMember moderatedBy, Integer teamNumber, Integer season);

    // Note: Low quality entries query removed - entity may not have contentQualityScore field

    // =========================================================================
    // STATISTICS AND ANALYTICS
    // =========================================================================

    /**
     * Counts entries by entry type.
     */
    @Query("SELECT ble.entryType, COUNT(ble) FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber " +
           "AND ble.season = :season AND ble.isActive = true GROUP BY ble.entryType")
    List<Object[]> countByEntryType(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Counts entries by build phase.
     */
    @Query("SELECT ble.buildPhase, COUNT(ble) FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber " +
           "AND ble.season = :season AND ble.isActive = true GROUP BY ble.buildPhase")
    List<Object[]> countByBuildPhase(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Counts entries by completion status.
     */
    @Query("SELECT ble.completionStatus, COUNT(ble) FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber " +
           "AND ble.season = :season AND ble.isActive = true GROUP BY ble.completionStatus")
    List<Object[]> countByCompletionStatus(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Counts entries by subsystem.
     */
    @Query("SELECT ble.subsystem, COUNT(ble) FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber " +
           "AND ble.season = :season AND ble.subsystem IS NOT NULL AND ble.isActive = true " +
           "GROUP BY ble.subsystem ORDER BY COUNT(ble) DESC")
    List<Object[]> countBySubsystem(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Calculates total photo count.
     */
    @Query("SELECT SUM(ble.photoCount) FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber " +
           "AND ble.season = :season AND ble.isActive = true")
    Optional<Long> calculateTotalPhotoCount(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    // Note: Average content quality and engagement score queries removed - entity may not have these metric fields

    /**
     * Finds most common tags.
     */
    @Query("SELECT tag, COUNT(tag) FROM BuildLogEntry ble JOIN ble.tags tag " +
           "WHERE ble.teamNumber = :teamNumber AND ble.season = :season AND ble.isActive = true " +
           "GROUP BY tag ORDER BY COUNT(tag) DESC")
    List<Object[]> findMostCommonTags(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds most used materials.
     */
    @Query("SELECT material, COUNT(material) FROM BuildLogEntry ble JOIN ble.materialsUsed material " +
           "WHERE ble.teamNumber = :teamNumber AND ble.season = :season AND ble.isActive = true " +
           "GROUP BY material ORDER BY COUNT(material) DESC")
    List<Object[]> findMostUsedMaterials(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds most used tools.
     */
    @Query("SELECT tool, COUNT(tool) FROM BuildLogEntry ble JOIN ble.toolsUsed tool " +
           "WHERE ble.teamNumber = :teamNumber AND ble.season = :season AND ble.isActive = true " +
           "GROUP BY tool ORDER BY COUNT(tool) DESC")
    List<Object[]> findMostUsedTools(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    // =========================================================================
    // CROSS-SEASON ANALYSIS
    // =========================================================================

    /**
     * Finds entries across multiple seasons.
     */
    @Query("SELECT ble FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber " +
           "AND ble.season IN :seasons AND ble.isActive = true " +
           "ORDER BY ble.season DESC, ble.entryDateTime DESC")
    List<BuildLogEntry> findMultiSeasonEntries(@Param("teamNumber") Integer teamNumber,
                                              @Param("seasons") List<Integer> seasons);

    /**
     * Finds entries with same subsystem across seasons.
     */
    @Query("SELECT ble FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber " +
           "AND ble.subsystem = :subsystem AND ble.isActive = true " +
           "ORDER BY ble.season DESC, ble.entryDateTime DESC")
    List<BuildLogEntry> findSubsystemAcrossSeasons(@Param("teamNumber") Integer teamNumber,
                                                   @Param("subsystem") String subsystem);

    /**
     * Finds recurring build patterns.
     */
    @Query("SELECT ble.subsystem, ble.component, COUNT(DISTINCT ble.season) as seasonCount " +
           "FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber " +
           "AND ble.subsystem IS NOT NULL AND ble.component IS NOT NULL AND ble.isActive = true " +
           "GROUP BY ble.subsystem, ble.component HAVING COUNT(DISTINCT ble.season) > 1 " +
           "ORDER BY seasonCount DESC")
    List<Object[]> findRecurringBuildPatterns(@Param("teamNumber") Integer teamNumber);

    // =========================================================================
    // BULK OPERATIONS AND MAINTENANCE
    // =========================================================================

    /**
     * Counts total active entries for team and season.
     */
    @Query("SELECT COUNT(ble) FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber " +
           "AND ble.season = :season AND ble.isActive = true")
    Long countActiveEntries(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds all active entries for export.
     */
    @Query("SELECT ble FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber AND ble.season = :season " +
           "AND ble.isActive = true ORDER BY ble.entryDateTime ASC")
    List<BuildLogEntry> findAllActiveEntries(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Updates search rankings for all entries.
     */
    @Query("UPDATE BuildLogEntry ble SET ble.searchRank = :rank WHERE ble.teamNumber = :teamNumber " +
           "AND ble.season = :season AND ble.isActive = true")
    void updateSearchRankForAllEntries(@Param("teamNumber") Integer teamNumber,
                                      @Param("season") Integer season,
                                      @Param("rank") Integer rank);

    /**
     * Finds entries for sitemap generation.
     */
    @Query("SELECT ble.id, ble.updatedAt FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber " +
           "AND ble.season = :season AND ble.isActive = true " +
           "AND ble.visibility IN ('PUBLIC', 'FRC_COMMUNITY') ORDER BY ble.updatedAt DESC")
    List<Object[]> findForSitemap(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    // =========================================================================
    // ARCHIVE AND HISTORY
    // =========================================================================

    /**
     * Finds archived entries.
     */
    @Query("SELECT ble FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber AND ble.season = :season " +
           "AND ble.isArchived = true ORDER BY ble.archivedAt DESC")
    List<BuildLogEntry> findArchivedEntries(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds entries archived within date range.
     */
    @Query("SELECT ble FROM BuildLogEntry ble WHERE ble.teamNumber = :teamNumber AND ble.season = :season " +
           "AND ble.isArchived = true AND ble.archivedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY ble.archivedAt DESC")
    List<BuildLogEntry> findArchivedInDateRange(@Param("teamNumber") Integer teamNumber,
                                               @Param("season") Integer season,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    // Note: findByArchiveReason query removed - LIKE query type issues in H2
}