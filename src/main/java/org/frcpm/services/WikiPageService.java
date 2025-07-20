// src/main/java/org/frcpm/services/WikiPageService.java

package org.frcpm.services;

import org.frcpm.models.WikiPage;
import org.frcpm.models.TeamMember;
import org.frcpm.models.Project;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for WikiPage operations.
 * 
 * Provides comprehensive wiki page management services including
 * hierarchical documentation, version control, collaborative editing,
 * content management, and knowledge sharing for FRC teams.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-KnowledgeManagement
 * @since Phase 4D.1 Team Wiki and Documentation System
 */
public interface WikiPageService {

    // =========================================================================
    // STANDARD SERVICE METHODS
    // =========================================================================

    /**
     * Creates a new wiki page.
     */
    WikiPage create(WikiPage wikiPage);

    /**
     * Updates an existing wiki page.
     */
    WikiPage update(Long id, WikiPage wikiPage);

    /**
     * Deletes a wiki page (soft delete).
     */
    void delete(Long id);

    /**
     * Finds a wiki page by ID.
     */
    Optional<WikiPage> findById(Long id);

    /**
     * Finds all active wiki pages.
     */
    List<WikiPage> findAll();

    /**
     * Checks if wiki page exists by ID.
     */
    boolean existsById(Long id);

    /**
     * Counts total number of wiki pages.
     */
    long count();

    // =========================================================================
    // PAGE MANAGEMENT
    // =========================================================================

    /**
     * Creates a new wiki page with validation.
     */
    WikiPage createPage(WikiPage wikiPage);

    /**
     * Creates a page with basic parameters.
     */
    WikiPage createPage(Integer teamNumber, Integer season, String title, String content,
                       WikiPage.PageType pageType, TeamMember createdBy);

    /**
     * Updates an existing page with validation and version control.
     */
    WikiPage updatePage(Long pageId, WikiPage wikiPage);

    /**
     * Archives a wiki page.
     */
    void archivePage(Long pageId, String reason);

    /**
     * Finds all active pages for a team and season.
     */
    List<WikiPage> findActivePages(Integer teamNumber, Integer season);

    // =========================================================================
    // HIERARCHICAL NAVIGATION
    // =========================================================================

    /**
     * Finds root level pages (no parent).
     */
    List<WikiPage> findRootPages(Integer teamNumber, Integer season);

    /**
     * Finds child pages of a specific parent.
     */
    List<WikiPage> findChildPages(WikiPage parentPage);

    /**
     * Finds pages by hierarchy level.
     */
    List<WikiPage> findByPageLevel(Integer teamNumber, Integer season, Integer level);

    /**
     * Finds pages in a specific path.
     */
    List<WikiPage> findByPathPrefix(Integer teamNumber, Integer season, String pathPrefix);

    /**
     * Finds all descendants of a page.
     */
    List<WikiPage> findDescendants(WikiPage parentPage);

    /**
     * Creates hierarchical page structure.
     */
    WikiPage createChildPage(WikiPage parentPage, WikiPage childPage);

    /**
     * Moves page to new parent.
     */
    WikiPage movePageToParent(Long pageId, WikiPage newParent);

    /**
     * Calculates page path from root.
     */
    String calculatePagePath(WikiPage page);

    // =========================================================================
    // VERSION CONTROL
    // =========================================================================

    /**
     * Finds current versions only.
     */
    List<WikiPage> findCurrentVersions(Integer teamNumber, Integer season);

    /**
     * Finds version history for a page.
     */
    List<WikiPage> findVersionHistory(WikiPage currentVersion);

    /**
     * Finds latest version of a page by slug.
     */
    Optional<WikiPage> findLatestVersionBySlug(Integer teamNumber, Integer season, String slug);

    /**
     * Creates new version of a page.
     */
    WikiPage createNewVersion(Long pageId, String content, TeamMember updatedBy, String changeLog);

    /**
     * Reverts page to specific version.
     */
    WikiPage revertToVersion(Long pageId, Integer versionNumber);

    /**
     * Compares two versions of a page.
     */
    Map<String, Object> compareVersions(Long pageId, Integer version1, Integer version2);

    /**
     * Finds recently modified pages.
     */
    List<WikiPage> findRecentlyModified(Integer teamNumber, Integer season, LocalDateTime since);

    // =========================================================================
    // CONTENT SEARCH AND FILTERING
    // =========================================================================

    /**
     * Searches pages by title (case-insensitive).
     */
    List<WikiPage> searchByTitle(Integer teamNumber, Integer season, String searchTerm);

    /**
     * Searches pages by content (case-insensitive).
     */
    List<WikiPage> searchByContent(Integer teamNumber, Integer season, String searchTerm);

    /**
     * Full text search across title, content, and summary.
     */
    List<WikiPage> fullTextSearch(Integer teamNumber, Integer season, String searchTerm);

    /**
     * Finds pages by tag.
     */
    List<WikiPage> findByTag(Integer teamNumber, Integer season, String tag);

    /**
     * Finds pages by category.
     */
    List<WikiPage> findByCategory(Integer teamNumber, Integer season, String category);

    /**
     * Finds pages with specific content features.
     */
    List<WikiPage> findByContentFeatures(Integer teamNumber, Integer season,
                                        Boolean hasCode, Boolean hasImages, 
                                        Boolean hasVideos, Boolean hasAttachments);

    /**
     * Advanced search with multiple criteria.
     */
    List<WikiPage> advancedSearch(Integer teamNumber, Integer season, Map<String, Object> criteria);

    // =========================================================================
    // ACCESS CONTROL AND PERMISSIONS
    // =========================================================================

    /**
     * Finds pages viewable by specific access level.
     */
    List<WikiPage> findViewablePages(Integer teamNumber, Integer season, WikiPage.AccessLevel accessLevel);

    /**
     * Finds pages editable by specific access level.
     */
    List<WikiPage> findEditablePages(Integer teamNumber, Integer season, WikiPage.AccessLevel accessLevel);

    /**
     * Finds pages where user is authorized editor.
     */
    List<WikiPage> findByAuthorizedEditor(Integer teamNumber, Integer season, Long userId);

    /**
     * Finds pages where user is authorized viewer.
     */
    List<WikiPage> findByAuthorizedViewer(Integer teamNumber, Integer season, Long userId);

    /**
     * Checks if user can view page.
     */
    boolean canUserViewPage(Long pageId, Long userId);

    /**
     * Checks if user can edit page.
     */
    boolean canUserEditPage(Long pageId, Long userId);

    /**
     * Adds authorized editor to page.
     */
    WikiPage addAuthorizedEditor(Long pageId, Long userId);

    /**
     * Removes authorized editor from page.
     */
    WikiPage removeAuthorizedEditor(Long pageId, Long userId);

    // =========================================================================
    // COLLABORATION AND ACTIVITY
    // =========================================================================

    /**
     * Finds pages created by specific user.
     */
    List<WikiPage> findByCreatedBy(TeamMember createdBy, Integer teamNumber, Integer season);

    /**
     * Finds pages last updated by specific user.
     */
    List<WikiPage> findByLastUpdatedBy(TeamMember lastUpdatedBy, Integer teamNumber, Integer season);

    /**
     * Finds pages approved by specific user.
     */
    List<WikiPage> findByApprovedBy(TeamMember approvedBy, Integer teamNumber, Integer season);

    /**
     * Finds most active pages (by edit count).
     */
    List<WikiPage> findMostActive(Integer teamNumber, Integer season);

    /**
     * Finds most viewed pages.
     */
    List<WikiPage> findMostViewed(Integer teamNumber, Integer season);

    /**
     * Finds recently viewed pages.
     */
    List<WikiPage> findRecentlyViewed(Integer teamNumber, Integer season);

    /**
     * Records page view.
     */
    void recordPageView(Long pageId, Long userId);

    /**
     * Adds comment to page.
     */
    WikiPage addComment(Long pageId, String comment, TeamMember author);

    // =========================================================================
    // APPROVAL WORKFLOW
    // =========================================================================

    /**
     * Finds pages pending approval.
     */
    List<WikiPage> findPendingApproval(Integer teamNumber, Integer season);

    /**
     * Submits page for approval.
     */
    WikiPage submitForApproval(Long pageId);

    /**
     * Approves a page.
     */
    WikiPage approvePage(Long pageId, TeamMember approver, String notes);

    /**
     * Rejects a page.
     */
    WikiPage rejectPage(Long pageId, String reason);

    /**
     * Finds pages requiring approval based on content.
     */
    List<WikiPage> findRequiringApproval(Integer teamNumber, Integer season);

    // =========================================================================
    // LOCK MANAGEMENT
    // =========================================================================

    /**
     * Finds locked pages.
     */
    List<WikiPage> findLockedPages(Integer teamNumber, Integer season);

    /**
     * Locks a page for editing.
     */
    WikiPage lockPage(Long pageId, TeamMember lockedBy, LocalDateTime lockedUntil);

    /**
     * Unlocks a page.
     */
    WikiPage unlockPage(Long pageId);

    /**
     * Checks if page is locked.
     */
    boolean isPageLocked(Long pageId);

    /**
     * Forces unlock of a page (admin only).
     */
    WikiPage forceUnlockPage(Long pageId, TeamMember unlockedBy);

    // =========================================================================
    // FEATURED AND SPECIAL PAGES
    // =========================================================================

    /**
     * Finds featured pages.
     */
    List<WikiPage> findFeaturedPages(Integer teamNumber, Integer season);

    /**
     * Sets page as featured.
     */
    WikiPage setPageFeatured(Long pageId, boolean featured);

    /**
     * Finds pages with high ratings.
     */
    List<WikiPage> findHighRatedPages(Integer teamNumber, Integer season, Double minRating, Integer minRatingCount);

    /**
     * Finds popular pages (high view count and recent activity).
     */
    List<WikiPage> findPopularPages(Integer teamNumber, Integer season, Long minViews, LocalDateTime recentCutoff);

    /**
     * Finds orphaned pages (no parent and no children).
     */
    List<WikiPage> findOrphanedPages(Integer teamNumber, Integer season);

    /**
     * Rates a page.
     */
    WikiPage ratePage(Long pageId, TeamMember rater, Integer rating);

    // =========================================================================
    // CONTENT MANAGEMENT
    // =========================================================================

    /**
     * Updates page content with validation.
     */
    WikiPage updateContent(Long pageId, String content, TeamMember updatedBy);

    /**
     * Adds attachment to page.
     */
    WikiPage addAttachment(Long pageId, String attachmentUrl, String description);

    /**
     * Removes attachment from page.
     */
    WikiPage removeAttachment(Long pageId, String attachmentUrl);

    /**
     * Updates page metadata.
     */
    WikiPage updateMetadata(Long pageId, String title, String summary, List<String> tags, List<String> categories);

    /**
     * Processes page content for special features.
     */
    WikiPage processContentFeatures(Long pageId);

    /**
     * Validates page content.
     */
    List<String> validatePageContent(String content);

    // =========================================================================
    // TEMPLATE AND STRUCTURE
    // =========================================================================

    /**
     * Creates page from template.
     */
    WikiPage createPageFromTemplate(WikiPage template, Integer teamNumber, Integer season, String title);

    /**
     * Saves page as template.
     */
    WikiPage saveAsTemplate(Long pageId, String templateName);

    /**
     * Finds available templates.
     */
    List<WikiPage> findTemplates(Integer teamNumber);

    /**
     * Applies page structure from template.
     */
    List<WikiPage> applyTemplateStructure(WikiPage template, Integer teamNumber, Integer season);

    // =========================================================================
    // STATISTICS AND ANALYTICS
    // =========================================================================

    /**
     * Counts pages by type.
     */
    Map<WikiPage.PageType, Long> countByPageType(Integer teamNumber, Integer season);

    /**
     * Counts pages by status.
     */
    Map<WikiPage.PageStatus, Long> countByStatus(Integer teamNumber, Integer season);

    /**
     * Calculates total view count for team.
     */
    Long calculateTotalViewCount(Integer teamNumber, Integer season);

    /**
     * Calculates total word count for team.
     */
    Integer calculateTotalWordCount(Integer teamNumber, Integer season);

    /**
     * Finds most common tags.
     */
    Map<String, Long> findMostCommonTags(Integer teamNumber, Integer season);

    /**
     * Finds most common categories.
     */
    Map<String, Long> findMostCommonCategories(Integer teamNumber, Integer season);

    /**
     * Calculates average page metrics.
     */
    Map<String, Double> calculateAverageMetrics(Integer teamNumber, Integer season);

    /**
     * Generates wiki analytics report.
     */
    Map<String, Object> generateAnalyticsReport(Integer teamNumber, Integer season);

    // =========================================================================
    // MAINTENANCE AND CLEANUP
    // =========================================================================

    /**
     * Finds pages with no content.
     */
    List<WikiPage> findEmptyPages(Integer teamNumber, Integer season);

    /**
     * Finds pages needing update (old and low activity).
     */
    List<WikiPage> findStalePages(Integer teamNumber, Integer season, LocalDateTime staleDate, Long minViews);

    /**
     * Finds broken links in pages.
     */
    List<String> findBrokenLinks(Integer teamNumber, Integer season);

    /**
     * Finds duplicate slugs.
     */
    Map<String, Long> findDuplicateSlugs(Integer teamNumber, Integer season);

    /**
     * Updates search rankings for all pages.
     */
    void updateSearchRankings(Integer teamNumber, Integer season);

    /**
     * Rebuilds page paths for consistency.
     */
    void rebuildPagePaths(Integer teamNumber, Integer season);

    /**
     * Cleans up orphaned versions.
     */
    void cleanupOrphanedVersions(Integer teamNumber, Integer season);

    // =========================================================================
    // ARCHIVE AND HISTORY
    // =========================================================================

    /**
     * Finds archived pages.
     */
    List<WikiPage> findArchivedPages(Integer teamNumber, Integer season);

    /**
     * Finds pages archived within date range.
     */
    List<WikiPage> findArchivedInDateRange(Integer teamNumber, Integer season, 
                                          LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Finds pages by archive reason.
     */
    List<WikiPage> findByArchiveReason(Integer teamNumber, Integer season, String reason);

    /**
     * Restores archived page.
     */
    WikiPage restoreArchivedPage(Long pageId);

    /**
     * Permanently deletes archived page.
     */
    void permanentlyDeletePage(Long pageId);

    // =========================================================================
    // CROSS-SEASON ANALYSIS
    // =========================================================================

    /**
     * Finds pages across multiple seasons for a team.
     */
    List<WikiPage> findMultiSeasonPages(Integer teamNumber, List<Integer> seasons);

    /**
     * Finds pages with same title across seasons.
     */
    List<WikiPage> findByTitleAcrossSeasons(Integer teamNumber, String title);

    /**
     * Finds carryover pages (exist in multiple seasons).
     */
    Map<String, Long> findCarryoverPages(Integer teamNumber);

    /**
     * Copies page structure to new season.
     */
    List<WikiPage> copyStructureToNewSeason(Integer teamNumber, Integer fromSeason, Integer toSeason);

    // =========================================================================
    // BULK OPERATIONS
    // =========================================================================

    /**
     * Counts total active pages for team and season.
     */
    Long countActivePages(Integer teamNumber, Integer season);

    /**
     * Finds all current pages for export/backup.
     */
    List<WikiPage> findAllCurrentPages(Integer teamNumber, Integer season);

    /**
     * Creates multiple pages.
     */
    List<WikiPage> createBulkPages(List<WikiPage> pages);

    /**
     * Updates multiple pages.
     */
    List<WikiPage> updateBulkPages(Map<Long, WikiPage> pageUpdates);

    /**
     * Archives multiple pages.
     */
    void bulkArchivePages(List<Long> pageIds, String reason);

    /**
     * Exports pages for external analysis.
     */
    List<Map<String, Object>> exportPages(Integer teamNumber, Integer season);

    // =========================================================================
    // INTEGRATION AND EXTERNAL SYSTEMS
    // =========================================================================

    /**
     * Imports page from external format.
     */
    WikiPage importPageFromExternalFormat(Map<String, Object> data, String format);

    /**
     * Exports page to external format.
     */
    Map<String, Object> exportPageToExternalFormat(Long pageId, String format);

    /**
     * Syncs page with external documentation systems.
     */
    WikiPage syncWithExternalSystem(Long pageId, String externalSystem);

    /**
     * Generates sitemap for public pages.
     */
    List<Map<String, Object>> generateSitemap(Integer teamNumber, Integer season);

    // =========================================================================
    // NOTIFICATION AND EVENTS
    // =========================================================================

    /**
     * Notifies subscribers of page changes.
     */
    void notifyPageSubscribers(Long pageId, String changeType);

    /**
     * Subscribes user to page notifications.
     */
    void subscribeToPage(Long pageId, Long userId);

    /**
     * Unsubscribes user from page notifications.
     */
    void unsubscribeFromPage(Long pageId, Long userId);

    /**
     * Finds users subscribed to page.
     */
    List<TeamMember> findPageSubscribers(Long pageId);

    // =========================================================================
    // VALIDATION AND BUSINESS RULES
    // =========================================================================

    /**
     * Validates wiki page data.
     */
    List<String> validatePage(WikiPage wikiPage);

    /**
     * Validates page hierarchy constraints.
     */
    boolean validatePageHierarchy(WikiPage page);

    /**
     * Validates page slug uniqueness.
     */
    boolean validateSlugUniqueness(String slug, Integer teamNumber, Integer season, Long excludePageId);

    /**
     * Validates user permissions for page operation.
     */
    boolean validateUserPermissions(Long pageId, Long userId, String operation);

    /**
     * Validates content quality and completeness.
     */
    Map<String, Object> validateContentQuality(String content);
}