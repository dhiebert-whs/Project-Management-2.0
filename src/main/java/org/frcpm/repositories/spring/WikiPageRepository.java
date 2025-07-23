// src/main/java/org/frcpm/repositories/spring/WikiPageRepository.java

package org.frcpm.repositories.spring;

import org.frcpm.models.WikiPage;
import org.frcpm.models.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for WikiPage entities.
 * 
 * Provides comprehensive data access for wiki page management including
 * hierarchical navigation, version control, content search, collaboration
 * tracking, and analytics for FRC team knowledge management.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-KnowledgeManagement
 * @since Phase 4D.1 Team Wiki and Documentation System
 */
@Repository
public interface WikiPageRepository extends JpaRepository<WikiPage, Long> {

    // =========================================================================
    // BASIC QUERIES
    // =========================================================================

    /**
     * Finds all active pages for a specific team and season.
     */
    List<WikiPage> findByTeamNumberAndSeasonAndIsActiveTrue(Integer teamNumber, Integer season);

    /**
     * Finds page by slug for a team and season.
     */
    Optional<WikiPage> findByTeamNumberAndSeasonAndSlugAndIsActiveTrue(Integer teamNumber, Integer season, String slug);

    /**
     * Finds pages by status for a team and season.
     */
    List<WikiPage> findByTeamNumberAndSeasonAndStatusAndIsActiveTrue(
        Integer teamNumber, Integer season, WikiPage.PageStatus status);

    /**
     * Finds pages by type for a team and season.
     */
    List<WikiPage> findByTeamNumberAndSeasonAndPageTypeAndIsActiveTrue(
        Integer teamNumber, Integer season, WikiPage.PageType pageType);

    /**
     * Finds pages by visibility level.
     */
    List<WikiPage> findByTeamNumberAndSeasonAndVisibilityAndIsActiveTrue(
        Integer teamNumber, Integer season, WikiPage.VisibilityLevel visibility);

    // =========================================================================
    // HIERARCHICAL NAVIGATION
    // =========================================================================

    /**
     * Finds root level pages (no parent).
     */
    @Query("SELECT p FROM WikiPage p WHERE p.teamNumber = :teamNumber AND p.season = :season " +
           "AND p.parentPage IS NULL AND p.isActive = true ORDER BY p.sortOrder ASC, p.title ASC")
    List<WikiPage> findRootPages(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds child pages of a specific parent.
     */
    List<WikiPage> findByParentPageAndIsActiveTrueOrderBySortOrderAscTitleAsc(WikiPage parentPage);

    /**
     * Finds pages by hierarchy level.
     */
    @Query("SELECT p FROM WikiPage p WHERE p.teamNumber = :teamNumber AND p.season = :season " +
           "AND p.pageLevel = :level AND p.isActive = true ORDER BY p.sortOrder ASC")
    List<WikiPage> findByPageLevel(@Param("teamNumber") Integer teamNumber, 
                                  @Param("season") Integer season, 
                                  @Param("level") Integer level);

    // Note: findByPathPrefix query removed - LIKE CONCAT validation issues in H2

    // Note: findDescendants query removed - LIKE CONCAT validation issues in H2

    // =========================================================================
    // VERSION CONTROL
    // =========================================================================

    /**
     * Finds current versions only.
     */
    @Query("SELECT p FROM WikiPage p WHERE p.teamNumber = :teamNumber AND p.season = :season " +
           "AND p.isCurrentVersion = true AND p.isActive = true")
    List<WikiPage> findCurrentVersions(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds version history for a page.
     */
    @Query("SELECT p FROM WikiPage p WHERE p.currentVersion = :currentVersion " +
           "ORDER BY p.version DESC")
    List<WikiPage> findVersionHistory(@Param("currentVersion") WikiPage currentVersion);

    /**
     * Finds latest version of a page by slug.
     */
    @Query("SELECT p FROM WikiPage p WHERE p.teamNumber = :teamNumber AND p.season = :season " +
           "AND p.slug = :slug AND p.isCurrentVersion = true AND p.isActive = true")
    Optional<WikiPage> findLatestVersionBySlug(@Param("teamNumber") Integer teamNumber,
                                              @Param("season") Integer season,
                                              @Param("slug") String slug);

    /**
     * Finds pages by change type.
     */
    List<WikiPage> findByTeamNumberAndSeasonAndChangeTypeAndIsActiveTrue(
        Integer teamNumber, Integer season, WikiPage.ChangeType changeType);

    /**
     * Finds recently modified pages.
     */
    @Query("SELECT p FROM WikiPage p WHERE p.teamNumber = :teamNumber AND p.season = :season " +
           "AND p.isCurrentVersion = true AND p.updatedAt >= :since AND p.isActive = true " +
           "ORDER BY p.updatedAt DESC")
    List<WikiPage> findRecentlyModified(@Param("teamNumber") Integer teamNumber,
                                       @Param("season") Integer season,
                                       @Param("since") LocalDateTime since);

    // =========================================================================
    // CONTENT SEARCH AND FILTERING
    // =========================================================================

    // Note: searchByTitle query removed - LIKE CONCAT validation issues in H2

    // Note: searchByContent query removed - LIKE CONCAT validation issues in H2

    // Note: fullTextSearch query removed - LIKE CONCAT validation issues in H2

    /**
     * Finds pages by tag.
     */
    @Query("SELECT p FROM WikiPage p WHERE p.teamNumber = :teamNumber AND p.season = :season " +
           "AND :tag MEMBER OF p.tags AND p.isCurrentVersion = true AND p.isActive = true")
    List<WikiPage> findByTag(@Param("teamNumber") Integer teamNumber,
                            @Param("season") Integer season,
                            @Param("tag") String tag);

    /**
     * Finds pages by category.
     */
    @Query("SELECT p FROM WikiPage p WHERE p.teamNumber = :teamNumber AND p.season = :season " +
           "AND :category MEMBER OF p.categories AND p.isCurrentVersion = true AND p.isActive = true")
    List<WikiPage> findByCategory(@Param("teamNumber") Integer teamNumber,
                                 @Param("season") Integer season,
                                 @Param("category") String category);

    /**
     * Finds pages with specific content features.
     */
    @Query("SELECT p FROM WikiPage p WHERE p.teamNumber = :teamNumber AND p.season = :season " +
           "AND p.isCurrentVersion = true AND p.isActive = true AND " +
           "(:hasCode = false OR p.hasCodeBlocks = true) AND " +
           "(:hasImages = false OR p.hasImages = true) AND " +
           "(:hasVideos = false OR p.hasVideos = true) AND " +
           "(:hasAttachments = false OR p.hasAttachments = true)")
    List<WikiPage> findByContentFeatures(@Param("teamNumber") Integer teamNumber,
                                        @Param("season") Integer season,
                                        @Param("hasCode") Boolean hasCode,
                                        @Param("hasImages") Boolean hasImages,
                                        @Param("hasVideos") Boolean hasVideos,
                                        @Param("hasAttachments") Boolean hasAttachments);

    // =========================================================================
    // ACCESS CONTROL AND PERMISSIONS
    // =========================================================================

    /**
     * Finds pages viewable by specific access level.
     */
    List<WikiPage> findByTeamNumberAndSeasonAndViewPermissionAndIsCurrentVersionTrueAndIsActiveTrue(
        Integer teamNumber, Integer season, WikiPage.AccessLevel viewPermission);

    /**
     * Finds pages editable by specific access level.
     */
    List<WikiPage> findByTeamNumberAndSeasonAndEditPermissionAndIsCurrentVersionTrueAndIsActiveTrue(
        Integer teamNumber, Integer season, WikiPage.AccessLevel editPermission);

    /**
     * Finds pages where user is authorized editor.
     */
    @Query("SELECT p FROM WikiPage p WHERE p.teamNumber = :teamNumber AND p.season = :season " +
           "AND :userId MEMBER OF p.authorizedEditors AND p.isCurrentVersion = true AND p.isActive = true")
    List<WikiPage> findByAuthorizedEditor(@Param("teamNumber") Integer teamNumber,
                                         @Param("season") Integer season,
                                         @Param("userId") Long userId);

    /**
     * Finds pages where user is authorized viewer.
     */
    @Query("SELECT p FROM WikiPage p WHERE p.teamNumber = :teamNumber AND p.season = :season " +
           "AND :userId MEMBER OF p.authorizedViewers AND p.isCurrentVersion = true AND p.isActive = true")
    List<WikiPage> findByAuthorizedViewer(@Param("teamNumber") Integer teamNumber,
                                         @Param("season") Integer season,
                                         @Param("userId") Long userId);

    /**
     * Finds locked pages.
     */
    @Query("SELECT p FROM WikiPage p WHERE p.teamNumber = :teamNumber AND p.season = :season " +
           "AND p.isLocked = true AND (p.lockedUntil IS NULL OR p.lockedUntil > CURRENT_TIMESTAMP) " +
           "AND p.isCurrentVersion = true AND p.isActive = true")
    List<WikiPage> findLockedPages(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds pages requiring approval.
     */
    @Query("SELECT p FROM WikiPage p WHERE p.teamNumber = :teamNumber AND p.season = :season " +
           "AND p.requireApproval = true AND p.approvedBy IS NULL " +
           "AND p.isCurrentVersion = true AND p.isActive = true")
    List<WikiPage> findPendingApproval(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    // =========================================================================
    // COLLABORATION AND ACTIVITY
    // =========================================================================

    /**
     * Finds pages created by specific user.
     */
    List<WikiPage> findByCreatedByAndTeamNumberAndSeasonAndIsCurrentVersionTrueAndIsActiveTrue(
        TeamMember createdBy, Integer teamNumber, Integer season);

    /**
     * Finds pages last updated by specific user.
     */
    List<WikiPage> findByLastUpdatedByAndTeamNumberAndSeasonAndIsCurrentVersionTrueAndIsActiveTrue(
        TeamMember lastUpdatedBy, Integer teamNumber, Integer season);

    /**
     * Finds pages approved by specific user.
     */
    List<WikiPage> findByApprovedByAndTeamNumberAndSeasonAndIsCurrentVersionTrueAndIsActiveTrue(
        TeamMember approvedBy, Integer teamNumber, Integer season);

    /**
     * Finds most active pages (by edit count).
     */
    @Query("SELECT p FROM WikiPage p WHERE p.teamNumber = :teamNumber AND p.season = :season " +
           "AND p.isCurrentVersion = true AND p.isActive = true " +
           "ORDER BY p.editCount DESC")
    List<WikiPage> findMostActive(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds most viewed pages.
     */
    @Query("SELECT p FROM WikiPage p WHERE p.teamNumber = :teamNumber AND p.season = :season " +
           "AND p.isCurrentVersion = true AND p.isActive = true " +
           "ORDER BY p.viewCount DESC")
    List<WikiPage> findMostViewed(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds recently viewed pages.
     */
    @Query("SELECT p FROM WikiPage p WHERE p.teamNumber = :teamNumber AND p.season = :season " +
           "AND p.lastViewed IS NOT NULL AND p.isCurrentVersion = true AND p.isActive = true " +
           "ORDER BY p.lastViewed DESC")
    List<WikiPage> findRecentlyViewed(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    // =========================================================================
    // FEATURED AND SPECIAL PAGES
    // =========================================================================

    /**
     * Finds featured pages.
     */
    @Query("SELECT p FROM WikiPage p WHERE p.teamNumber = :teamNumber AND p.season = :season " +
           "AND p.isFeatured = true AND p.isCurrentVersion = true AND p.isActive = true " +
           "ORDER BY p.searchRank DESC, p.viewCount DESC")
    List<WikiPage> findFeaturedPages(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds pages with high ratings.
     */
    @Query("SELECT p FROM WikiPage p WHERE p.teamNumber = :teamNumber AND p.season = :season " +
           "AND p.averageRating >= :minRating AND p.ratingCount >= :minRatingCount " +
           "AND p.isCurrentVersion = true AND p.isActive = true " +
           "ORDER BY p.averageRating DESC")
    List<WikiPage> findHighRatedPages(@Param("teamNumber") Integer teamNumber,
                                     @Param("season") Integer season,
                                     @Param("minRating") Double minRating,
                                     @Param("minRatingCount") Integer minRatingCount);

    /**
     * Finds popular pages (high view count and recent activity).
     */
    @Query("SELECT p FROM WikiPage p WHERE p.teamNumber = :teamNumber AND p.season = :season " +
           "AND p.viewCount >= :minViews AND p.lastViewed >= :recentCutoff " +
           "AND p.isCurrentVersion = true AND p.isActive = true " +
           "ORDER BY p.viewCount DESC, p.lastViewed DESC")
    List<WikiPage> findPopularPages(@Param("teamNumber") Integer teamNumber,
                                   @Param("season") Integer season,
                                   @Param("minViews") Long minViews,
                                   @Param("recentCutoff") LocalDateTime recentCutoff);

    /**
     * Finds orphaned pages (no parent and no children).
     */
    @Query("SELECT p FROM WikiPage p WHERE p.teamNumber = :teamNumber AND p.season = :season " +
           "AND p.parentPage IS NULL AND p.childPages IS EMPTY " +
           "AND p.isCurrentVersion = true AND p.isActive = true")
    List<WikiPage> findOrphanedPages(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    // =========================================================================
    // STATISTICS AND ANALYTICS
    // =========================================================================

    /**
     * Counts pages by type.
     */
    @Query("SELECT p.pageType, COUNT(p) FROM WikiPage p WHERE p.teamNumber = :teamNumber " +
           "AND p.season = :season AND p.isCurrentVersion = true AND p.isActive = true " +
           "GROUP BY p.pageType")
    List<Object[]> countByPageType(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Counts pages by status.
     */
    @Query("SELECT p.status, COUNT(p) FROM WikiPage p WHERE p.teamNumber = :teamNumber " +
           "AND p.season = :season AND p.isCurrentVersion = true AND p.isActive = true " +
           "GROUP BY p.status")
    List<Object[]> countByStatus(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Calculates total view count for team.
     */
    @Query("SELECT SUM(p.viewCount) FROM WikiPage p WHERE p.teamNumber = :teamNumber " +
           "AND p.season = :season AND p.isCurrentVersion = true AND p.isActive = true")
    Optional<Long> findTotalViewCount(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Calculates total word count for team.
     */
    @Query("SELECT SUM(p.wordCount) FROM WikiPage p WHERE p.teamNumber = :teamNumber " +
           "AND p.season = :season AND p.isCurrentVersion = true AND p.isActive = true")
    Optional<Integer> findTotalWordCount(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds most common tags.
     */
    @Query("SELECT tag, COUNT(tag) FROM WikiPage p JOIN p.tags tag " +
           "WHERE p.teamNumber = :teamNumber AND p.season = :season " +
           "AND p.isCurrentVersion = true AND p.isActive = true " +
           "GROUP BY tag ORDER BY COUNT(tag) DESC")
    List<Object[]> findMostCommonTags(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds most common categories.
     */
    @Query("SELECT cat, COUNT(cat) FROM WikiPage p JOIN p.categories cat " +
           "WHERE p.teamNumber = :teamNumber AND p.season = :season " +
           "AND p.isCurrentVersion = true AND p.isActive = true " +
           "GROUP BY cat ORDER BY COUNT(cat) DESC")
    List<Object[]> findMostCommonCategories(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Calculates average page metrics.
     */
    @Query("SELECT AVG(p.viewCount), AVG(p.editCount), AVG(p.wordCount), AVG(p.averageRating) " +
           "FROM WikiPage p WHERE p.teamNumber = :teamNumber AND p.season = :season " +
           "AND p.isCurrentVersion = true AND p.isActive = true")
    List<Object[]> findAverageMetrics(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    // =========================================================================
    // MAINTENANCE AND CLEANUP
    // =========================================================================

    /**
     * Finds pages with no content.
     */
    @Query("SELECT p FROM WikiPage p WHERE p.teamNumber = :teamNumber AND p.season = :season " +
           "AND (p.content IS NULL OR p.content = '' OR p.wordCount = 0) " +
           "AND p.isCurrentVersion = true AND p.isActive = true")
    List<WikiPage> findEmptyPages(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds pages needing update (old and low activity).
     */
    @Query("SELECT p FROM WikiPage p WHERE p.teamNumber = :teamNumber AND p.season = :season " +
           "AND p.updatedAt < :staleDate AND p.viewCount < :minViews " +
           "AND p.isCurrentVersion = true AND p.isActive = true")
    List<WikiPage> findStalePages(@Param("teamNumber") Integer teamNumber,
                                 @Param("season") Integer season,
                                 @Param("staleDate") LocalDateTime staleDate,
                                 @Param("minViews") Long minViews);

    /**
     * Finds broken links (pages referenced but don't exist).
     */
    @Query("SELECT DISTINCT link.url FROM WikiPage p JOIN p.externalLinks link " +
           "WHERE p.teamNumber = :teamNumber AND p.season = :season " +
           "AND link.linkType = 'internal' AND p.isCurrentVersion = true AND p.isActive = true")
    List<String> findInternalLinks(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds duplicate slugs (should be unique per team/season).
     */
    @Query("SELECT p.slug, COUNT(p) FROM WikiPage p WHERE p.teamNumber = :teamNumber " +
           "AND p.season = :season AND p.isCurrentVersion = true AND p.isActive = true " +
           "GROUP BY p.slug HAVING COUNT(p) > 1")
    List<Object[]> findDuplicateSlugs(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    // =========================================================================
    // ARCHIVE AND HISTORY
    // =========================================================================

    /**
     * Finds archived pages.
     */
    @Query("SELECT p FROM WikiPage p WHERE p.teamNumber = :teamNumber AND p.season = :season " +
           "AND p.isArchived = true ORDER BY p.archivedAt DESC")
    List<WikiPage> findArchivedPages(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds pages archived within date range.
     */
    @Query("SELECT p FROM WikiPage p WHERE p.teamNumber = :teamNumber AND p.season = :season " +
           "AND p.isArchived = true AND p.archivedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY p.archivedAt DESC")
    List<WikiPage> findArchivedInDateRange(@Param("teamNumber") Integer teamNumber,
                                          @Param("season") Integer season,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    // Note: findByArchiveReason query removed - LIKE query type issues in H2

    // =========================================================================
    // CROSS-SEASON ANALYSIS
    // =========================================================================

    /**
     * Finds pages across multiple seasons for a team.
     */
    @Query("SELECT p FROM WikiPage p WHERE p.teamNumber = :teamNumber " +
           "AND p.season IN :seasons AND p.isCurrentVersion = true AND p.isActive = true " +
           "ORDER BY p.season DESC, p.title ASC")
    List<WikiPage> findMultiSeasonPages(@Param("teamNumber") Integer teamNumber, @Param("seasons") List<Integer> seasons);

    /**
     * Finds pages with same title across seasons.
     */
    @Query("SELECT p FROM WikiPage p WHERE p.teamNumber = :teamNumber AND p.title = :title " +
           "AND p.isCurrentVersion = true AND p.isActive = true ORDER BY p.season DESC")
    List<WikiPage> findByTitleAcrossSeasons(@Param("teamNumber") Integer teamNumber, @Param("title") String title);

    /**
     * Finds carryover pages (exist in multiple seasons).
     */
    @Query("SELECT p.title, COUNT(DISTINCT p.season) as seasonCount FROM WikiPage p " +
           "WHERE p.teamNumber = :teamNumber AND p.isCurrentVersion = true AND p.isActive = true " +
           "GROUP BY p.title HAVING COUNT(DISTINCT p.season) > 1 " +
           "ORDER BY seasonCount DESC")
    List<Object[]> findCarryoverPages(@Param("teamNumber") Integer teamNumber);

    // =========================================================================
    // BULK OPERATIONS
    // =========================================================================

    /**
     * Counts total active pages for team and season.
     */
    @Query("SELECT COUNT(p) FROM WikiPage p WHERE p.teamNumber = :teamNumber " +
           "AND p.season = :season AND p.isCurrentVersion = true AND p.isActive = true")
    Long countActivePages(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    /**
     * Finds all current pages for export/backup.
     */
    @Query("SELECT p FROM WikiPage p WHERE p.teamNumber = :teamNumber AND p.season = :season " +
           "AND p.isCurrentVersion = true AND p.isActive = true " +
           "ORDER BY p.pagePath ASC")
    List<WikiPage> findAllCurrentPages(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);

    // Note: updateSearchRankForAllPages operation removed - use service layer for UPDATE operations

    /**
     * Finds pages for sitemap generation.
     */
    @Query("SELECT p.slug, p.updatedAt FROM WikiPage p WHERE p.teamNumber = :teamNumber " +
           "AND p.season = :season AND p.isCurrentVersion = true AND p.isActive = true " +
           "AND p.visibility IN ('PUBLIC', 'TEAM_ONLY') ORDER BY p.updatedAt DESC")
    List<Object[]> findForSitemap(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);
}