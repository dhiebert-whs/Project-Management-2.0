// src/main/java/org/frcpm/services/impl/WikiPageServiceImpl.java

package org.frcpm.services.impl;

import org.frcpm.models.WikiPage;
import org.frcpm.models.TeamMember;
import org.frcpm.models.Project;
import org.frcpm.repositories.spring.WikiPageRepository;
import org.frcpm.services.WikiPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Implementation of WikiPageService.
 * 
 * Provides comprehensive wiki page management services including
 * hierarchical documentation, version control, collaborative editing,
 * content management, and knowledge sharing for FRC teams.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-KnowledgeManagement
 * @since Phase 4D.1 Team Wiki and Documentation System
 */
@Service
@Transactional
public class WikiPageServiceImpl implements WikiPageService {

    @Autowired
    private WikiPageRepository wikiPageRepository;

    // =========================================================================
    // STANDARD SERVICE METHODS
    // =========================================================================

    @Override
    public WikiPage create(WikiPage wikiPage) {
        return createPage(wikiPage);
    }

    @Override
    public WikiPage update(Long id, WikiPage wikiPage) {
        return updatePage(id, wikiPage);
    }

    @Override
    public void delete(Long id) {
        archivePage(id, "Deleted by user");
    }

    @Override
    public Optional<WikiPage> findById(Long id) {
        return wikiPageRepository.findById(id);
    }

    @Override
    public List<WikiPage> findAll() {
        return wikiPageRepository.findAll().stream()
                .filter(WikiPage::getIsActive)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(Long id) {
        return wikiPageRepository.existsById(id);
    }

    @Override
    public long count() {
        return wikiPageRepository.count();
    }

    // =========================================================================
    // PAGE MANAGEMENT
    // =========================================================================

    @Override
    public WikiPage createPage(WikiPage wikiPage) {
        validatePage(wikiPage);
        
        // Set defaults
        if (wikiPage.getStatus() == null) {
            wikiPage.setStatus(WikiPage.PageStatus.DRAFT);
        }
        if (wikiPage.getVisibility() == null) {
            wikiPage.setVisibility(WikiPage.VisibilityLevel.TEAM_ONLY);
        }
        if (wikiPage.getVersion() == null) {
            wikiPage.setVersion(1);
        }
        if (wikiPage.getIsCurrentVersion() == null) {
            wikiPage.setIsCurrentVersion(true);
        }
        if (wikiPage.getCreatedAt() == null) {
            wikiPage.setCreatedAt(LocalDateTime.now());
        }
        
        // Generate slug if not provided
        if (wikiPage.getSlug() == null || wikiPage.getSlug().isEmpty()) {
            wikiPage.setSlug(generateSlug(wikiPage.getTitle(), wikiPage.getTeamNumber(), wikiPage.getSeason()));
        }
        
        // Calculate hierarchy information
        calculateHierarchyInfo(wikiPage);
        
        // Process content features
        processContentFeatures(wikiPage.getId());
        
        return wikiPageRepository.save(wikiPage);
    }

    @Override
    public WikiPage createPage(Integer teamNumber, Integer season, String title, String content,
                             WikiPage.PageType pageType, TeamMember createdBy) {
        WikiPage page = new WikiPage();
        page.setTeamNumber(teamNumber);
        page.setSeason(season);
        page.setTitle(title);
        page.setContent(content);
        page.setPageType(pageType);
        page.setCreatedBy(createdBy);
        page.setLastUpdatedBy(createdBy);
        page.setCreatedAt(LocalDateTime.now());
        page.setUpdatedAt(LocalDateTime.now());
        
        return createPage(page);
    }

    @Override
    public WikiPage updatePage(Long pageId, WikiPage wikiPage) {
        Optional<WikiPage> existingPageOpt = wikiPageRepository.findById(pageId);
        if (!existingPageOpt.isPresent()) {
            throw new RuntimeException("Wiki page not found with id: " + pageId);
        }
        
        WikiPage existingPage = existingPageOpt.get();
        validatePage(wikiPage);
        
        // Create new version if content changed
        if (!Objects.equals(existingPage.getContent(), wikiPage.getContent())) {
            return createNewVersion(pageId, wikiPage.getContent(), wikiPage.getLastUpdatedBy(), 
                                  wikiPage.getChangeLog());
        }
        
        // Update metadata only
        existingPage.setTitle(wikiPage.getTitle());
        existingPage.setSummary(wikiPage.getSummary());
        existingPage.setTags(wikiPage.getTags());
        existingPage.setCategories(wikiPage.getCategories());
        existingPage.setVisibility(wikiPage.getVisibility());
        existingPage.setLastUpdatedBy(wikiPage.getLastUpdatedBy());
        existingPage.setUpdatedAt(LocalDateTime.now());
        
        return wikiPageRepository.save(existingPage);
    }

    @Override
    public void archivePage(Long pageId, String reason) {
        Optional<WikiPage> pageOpt = wikiPageRepository.findById(pageId);
        if (pageOpt.isPresent()) {
            WikiPage page = pageOpt.get();
            page.setIsArchived(true);
            page.setArchiveReason(reason);
            page.setArchivedAt(LocalDateTime.now());
            page.setIsActive(false);
            wikiPageRepository.save(page);
        }
    }

    @Override
    public List<WikiPage> findActivePages(Integer teamNumber, Integer season) {
        return wikiPageRepository.findByTeamNumberAndSeasonAndIsActiveTrue(teamNumber, season);
    }

    // =========================================================================
    // HIERARCHICAL NAVIGATION
    // =========================================================================

    @Override
    public List<WikiPage> findRootPages(Integer teamNumber, Integer season) {
        return wikiPageRepository.findRootPages(teamNumber, season);
    }

    @Override
    public List<WikiPage> findChildPages(WikiPage parentPage) {
        return wikiPageRepository.findByParentPageAndIsActiveTrueOrderBySortOrderAscTitleAsc(parentPage);
    }

    @Override
    public List<WikiPage> findByPageLevel(Integer teamNumber, Integer season, Integer level) {
        return wikiPageRepository.findByPageLevel(teamNumber, season, level);
    }

    @Override
    public List<WikiPage> findByPathPrefix(Integer teamNumber, Integer season, String pathPrefix) {
        return wikiPageRepository.findByPathPrefix(teamNumber, season, pathPrefix);
    }

    @Override
    public List<WikiPage> findDescendants(WikiPage parentPage) {
        return wikiPageRepository.findDescendants(parentPage.getTeamNumber(), 
                                                 parentPage.getSeason(), 
                                                 parentPage.getPagePath());
    }

    @Override
    public WikiPage createChildPage(WikiPage parentPage, WikiPage childPage) {
        childPage.setParentPage(parentPage);
        childPage.setPageLevel(parentPage.getPageLevel() + 1);
        calculateHierarchyInfo(childPage);
        return createPage(childPage);
    }

    @Override
    public WikiPage movePageToParent(Long pageId, WikiPage newParent) {
        Optional<WikiPage> pageOpt = wikiPageRepository.findById(pageId);
        if (!pageOpt.isPresent()) {
            throw new RuntimeException("Wiki page not found with id: " + pageId);
        }
        
        WikiPage page = pageOpt.get();
        page.setParentPage(newParent);
        if (newParent != null) {
            page.setPageLevel(newParent.getPageLevel() + 1);
        } else {
            page.setPageLevel(0);
        }
        
        calculateHierarchyInfo(page);
        
        // Update all descendants
        List<WikiPage> descendants = findDescendants(page);
        for (WikiPage descendant : descendants) {
            calculateHierarchyInfo(descendant);
            wikiPageRepository.save(descendant);
        }
        
        return wikiPageRepository.save(page);
    }

    @Override
    public String calculatePagePath(WikiPage page) {
        if (page.getParentPage() == null) {
            return "/" + page.getSlug();
        }
        
        List<String> pathComponents = new ArrayList<>();
        WikiPage current = page;
        
        while (current != null) {
            pathComponents.add(0, current.getSlug());
            current = current.getParentPage();
        }
        
        return "/" + String.join("/", pathComponents);
    }

    // =========================================================================
    // VERSION CONTROL
    // =========================================================================

    @Override
    public List<WikiPage> findCurrentVersions(Integer teamNumber, Integer season) {
        return wikiPageRepository.findCurrentVersions(teamNumber, season);
    }

    @Override
    public List<WikiPage> findVersionHistory(WikiPage currentVersion) {
        return wikiPageRepository.findVersionHistory(currentVersion);
    }

    @Override
    public Optional<WikiPage> findLatestVersionBySlug(Integer teamNumber, Integer season, String slug) {
        return wikiPageRepository.findLatestVersionBySlug(teamNumber, season, slug);
    }

    @Override
    public WikiPage createNewVersion(Long pageId, String content, TeamMember updatedBy, String changeLog) {
        Optional<WikiPage> currentPageOpt = wikiPageRepository.findById(pageId);
        if (!currentPageOpt.isPresent()) {
            throw new RuntimeException("Wiki page not found with id: " + pageId);
        }
        
        WikiPage currentPage = currentPageOpt.get();
        
        // Mark current version as not current
        currentPage.setIsCurrentVersion(false);
        wikiPageRepository.save(currentPage);
        
        // Create new version
        WikiPage newVersion = new WikiPage();
        copyPageProperties(currentPage, newVersion);
        newVersion.setId(null); // New entity
        newVersion.setContent(content);
        newVersion.setVersion(currentPage.getVersion() + 1);
        newVersion.setIsCurrentVersion(true);
        newVersion.setCurrentVersion(currentPage.getCurrentVersion());
        newVersion.setLastUpdatedBy(updatedBy);
        newVersion.setUpdatedAt(LocalDateTime.now());
        newVersion.setChangeLog(changeLog);
        newVersion.setChangeType(determineChangeType(currentPage.getContent(), content));
        
        // Update edit count and other metrics
        newVersion.setEditCount(currentPage.getEditCount() + 1);
        
        processContentFeatures(newVersion.getId());
        
        return wikiPageRepository.save(newVersion);
    }

    @Override
    public WikiPage revertToVersion(Long pageId, Integer versionNumber) {
        Optional<WikiPage> currentPageOpt = wikiPageRepository.findById(pageId);
        if (!currentPageOpt.isPresent()) {
            throw new RuntimeException("Wiki page not found with id: " + pageId);
        }
        
        WikiPage currentPage = currentPageOpt.get();
        List<WikiPage> history = findVersionHistory(currentPage.getCurrentVersion());
        
        Optional<WikiPage> targetVersionOpt = history.stream()
                .filter(p -> p.getVersion().equals(versionNumber))
                .findFirst();
        
        if (!targetVersionOpt.isPresent()) {
            throw new RuntimeException("Version " + versionNumber + " not found");
        }
        
        WikiPage targetVersion = targetVersionOpt.get();
        return createNewVersion(pageId, targetVersion.getContent(), currentPage.getLastUpdatedBy(), 
                              "Reverted to version " + versionNumber);
    }

    @Override
    public Map<String, Object> compareVersions(Long pageId, Integer version1, Integer version2) {
        // Implementation would include diff algorithms for content comparison
        Map<String, Object> comparison = new HashMap<>();
        comparison.put("pageId", pageId);
        comparison.put("version1", version1);
        comparison.put("version2", version2);
        comparison.put("differences", calculateContentDifferences(pageId, version1, version2));
        return comparison;
    }

    @Override
    public List<WikiPage> findRecentlyModified(Integer teamNumber, Integer season, LocalDateTime since) {
        return wikiPageRepository.findRecentlyModified(teamNumber, season, since);
    }

    // =========================================================================
    // CONTENT SEARCH AND FILTERING
    // =========================================================================

    @Override
    public List<WikiPage> searchByTitle(Integer teamNumber, Integer season, String searchTerm) {
        return wikiPageRepository.searchByTitle(teamNumber, season, searchTerm);
    }

    @Override
    public List<WikiPage> searchByContent(Integer teamNumber, Integer season, String searchTerm) {
        return wikiPageRepository.searchByContent(teamNumber, season, searchTerm);
    }

    @Override
    public List<WikiPage> fullTextSearch(Integer teamNumber, Integer season, String searchTerm) {
        return wikiPageRepository.fullTextSearch(teamNumber, season, searchTerm);
    }

    @Override
    public List<WikiPage> findByTag(Integer teamNumber, Integer season, String tag) {
        return wikiPageRepository.findByTag(teamNumber, season, tag);
    }

    @Override
    public List<WikiPage> findByCategory(Integer teamNumber, Integer season, String category) {
        return wikiPageRepository.findByCategory(teamNumber, season, category);
    }

    @Override
    public List<WikiPage> findByContentFeatures(Integer teamNumber, Integer season,
                                               Boolean hasCode, Boolean hasImages, 
                                               Boolean hasVideos, Boolean hasAttachments) {
        return wikiPageRepository.findByContentFeatures(teamNumber, season, hasCode, hasImages, hasVideos, hasAttachments);
    }

    @Override
    public List<WikiPage> advancedSearch(Integer teamNumber, Integer season, Map<String, Object> criteria) {
        // Start with all current pages
        List<WikiPage> results = findCurrentVersions(teamNumber, season);
        
        // Apply filters based on criteria
        if (criteria.containsKey("title")) {
            String title = (String) criteria.get("title");
            results = results.stream()
                    .filter(p -> p.getTitle().toLowerCase().contains(title.toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        if (criteria.containsKey("pageType")) {
            WikiPage.PageType pageType = (WikiPage.PageType) criteria.get("pageType");
            results = results.stream()
                    .filter(p -> p.getPageType() == pageType)
                    .collect(Collectors.toList());
        }
        
        if (criteria.containsKey("status")) {
            WikiPage.PageStatus status = (WikiPage.PageStatus) criteria.get("status");
            results = results.stream()
                    .filter(p -> p.getStatus() == status)
                    .collect(Collectors.toList());
        }
        
        if (criteria.containsKey("createdAfter")) {
            LocalDateTime createdAfter = (LocalDateTime) criteria.get("createdAfter");
            results = results.stream()
                    .filter(p -> p.getCreatedAt().isAfter(createdAfter))
                    .collect(Collectors.toList());
        }
        
        return results;
    }

    // =========================================================================
    // ACCESS CONTROL AND PERMISSIONS
    // =========================================================================

    @Override
    public List<WikiPage> findViewablePages(Integer teamNumber, Integer season, WikiPage.AccessLevel accessLevel) {
        return wikiPageRepository.findByTeamNumberAndSeasonAndViewPermissionAndIsCurrentVersionTrueAndIsActiveTrue(
                teamNumber, season, accessLevel);
    }

    @Override
    public List<WikiPage> findEditablePages(Integer teamNumber, Integer season, WikiPage.AccessLevel accessLevel) {
        return wikiPageRepository.findByTeamNumberAndSeasonAndEditPermissionAndIsCurrentVersionTrueAndIsActiveTrue(
                teamNumber, season, accessLevel);
    }

    @Override
    public List<WikiPage> findByAuthorizedEditor(Integer teamNumber, Integer season, Long userId) {
        return wikiPageRepository.findByAuthorizedEditor(teamNumber, season, userId);
    }

    @Override
    public List<WikiPage> findByAuthorizedViewer(Integer teamNumber, Integer season, Long userId) {
        return wikiPageRepository.findByAuthorizedViewer(teamNumber, season, userId);
    }

    @Override
    public boolean canUserViewPage(Long pageId, Long userId) {
        Optional<WikiPage> pageOpt = wikiPageRepository.findById(pageId);
        if (!pageOpt.isPresent()) {
            return false;
        }
        
        WikiPage page = pageOpt.get();
        
        // Check visibility level
        if (page.getVisibility() == WikiPage.VisibilityLevel.PUBLIC) {
            return true;
        }
        
        // Check authorized viewers
        if (page.getAuthorizedViewers().contains(userId)) {
            return true;
        }
        
        // Check if user is creator or has edit permissions
        return canUserEditPage(pageId, userId);
    }

    @Override
    public boolean canUserEditPage(Long pageId, Long userId) {
        Optional<WikiPage> pageOpt = wikiPageRepository.findById(pageId);
        if (!pageOpt.isPresent()) {
            return false;
        }
        
        WikiPage page = pageOpt.get();
        
        // Check if page is locked
        if (isPageLocked(pageId)) {
            return false;
        }
        
        // Check if user is creator
        if (page.getCreatedBy() != null && page.getCreatedBy().getId().equals(userId)) {
            return true;
        }
        
        // Check authorized editors
        return page.getAuthorizedEditors().contains(userId);
    }

    @Override
    public WikiPage addAuthorizedEditor(Long pageId, Long userId) {
        Optional<WikiPage> pageOpt = wikiPageRepository.findById(pageId);
        if (!pageOpt.isPresent()) {
            throw new RuntimeException("Wiki page not found with id: " + pageId);
        }
        
        WikiPage page = pageOpt.get();
        if (!page.getAuthorizedEditors().contains(userId)) {
            page.getAuthorizedEditors().add(userId);
            return wikiPageRepository.save(page);
        }
        
        return page;
    }

    @Override
    public WikiPage removeAuthorizedEditor(Long pageId, Long userId) {
        Optional<WikiPage> pageOpt = wikiPageRepository.findById(pageId);
        if (!pageOpt.isPresent()) {
            throw new RuntimeException("Wiki page not found with id: " + pageId);
        }
        
        WikiPage page = pageOpt.get();
        page.getAuthorizedEditors().remove(userId);
        return wikiPageRepository.save(page);
    }

    // =========================================================================
    // COLLABORATION AND ACTIVITY
    // =========================================================================

    @Override
    public List<WikiPage> findByCreatedBy(TeamMember createdBy, Integer teamNumber, Integer season) {
        return wikiPageRepository.findByCreatedByAndTeamNumberAndSeasonAndIsCurrentVersionTrueAndIsActiveTrue(
                createdBy, teamNumber, season);
    }

    @Override
    public List<WikiPage> findByLastUpdatedBy(TeamMember lastUpdatedBy, Integer teamNumber, Integer season) {
        return wikiPageRepository.findByLastUpdatedByAndTeamNumberAndSeasonAndIsCurrentVersionTrueAndIsActiveTrue(
                lastUpdatedBy, teamNumber, season);
    }

    @Override
    public List<WikiPage> findByApprovedBy(TeamMember approvedBy, Integer teamNumber, Integer season) {
        return wikiPageRepository.findByApprovedByAndTeamNumberAndSeasonAndIsCurrentVersionTrueAndIsActiveTrue(
                approvedBy, teamNumber, season);
    }

    @Override
    public List<WikiPage> findMostActive(Integer teamNumber, Integer season) {
        return wikiPageRepository.findMostActive(teamNumber, season);
    }

    @Override
    public List<WikiPage> findMostViewed(Integer teamNumber, Integer season) {
        return wikiPageRepository.findMostViewed(teamNumber, season);
    }

    @Override
    public List<WikiPage> findRecentlyViewed(Integer teamNumber, Integer season) {
        return wikiPageRepository.findRecentlyViewed(teamNumber, season);
    }

    @Override
    public void recordPageView(Long pageId, Long userId) {
        Optional<WikiPage> pageOpt = wikiPageRepository.findById(pageId);
        if (pageOpt.isPresent()) {
            WikiPage page = pageOpt.get();
            page.setViewCount(page.getViewCount() + 1);
            page.setLastViewed(LocalDateTime.now());
            page.setLastViewedBy(userId);
            wikiPageRepository.save(page);
        }
    }

    @Override
    public WikiPage addComment(Long pageId, String comment, TeamMember author) {
        // Implementation would add to comments collection
        Optional<WikiPage> pageOpt = wikiPageRepository.findById(pageId);
        if (pageOpt.isPresent()) {
            WikiPage page = pageOpt.get();
            // Add comment to page's comment collection
            // This would require a separate Comment entity in a full implementation
            return wikiPageRepository.save(page);
        }
        throw new RuntimeException("Wiki page not found with id: " + pageId);
    }

    // =========================================================================
    // APPROVAL WORKFLOW
    // =========================================================================

    @Override
    public List<WikiPage> findPendingApproval(Integer teamNumber, Integer season) {
        return wikiPageRepository.findPendingApproval(teamNumber, season);
    }

    @Override
    public WikiPage submitForApproval(Long pageId) {
        Optional<WikiPage> pageOpt = wikiPageRepository.findById(pageId);
        if (!pageOpt.isPresent()) {
            throw new RuntimeException("Wiki page not found with id: " + pageId);
        }
        
        WikiPage page = pageOpt.get();
        page.setStatus(WikiPage.PageStatus.PENDING_APPROVAL);
        page.setSubmittedForApprovalAt(LocalDateTime.now());
        return wikiPageRepository.save(page);
    }

    @Override
    public WikiPage approvePage(Long pageId, TeamMember approver, String notes) {
        Optional<WikiPage> pageOpt = wikiPageRepository.findById(pageId);
        if (!pageOpt.isPresent()) {
            throw new RuntimeException("Wiki page not found with id: " + pageId);
        }
        
        WikiPage page = pageOpt.get();
        page.setStatus(WikiPage.PageStatus.PUBLISHED);
        page.setApprovedBy(approver);
        page.setApprovedAt(LocalDateTime.now());
        page.setApprovalNotes(notes);
        return wikiPageRepository.save(page);
    }

    @Override
    public WikiPage rejectPage(Long pageId, String reason) {
        Optional<WikiPage> pageOpt = wikiPageRepository.findById(pageId);
        if (!pageOpt.isPresent()) {
            throw new RuntimeException("Wiki page not found with id: " + pageId);
        }
        
        WikiPage page = pageOpt.get();
        page.setStatus(WikiPage.PageStatus.DRAFT);
        page.setRejectionReason(reason);
        page.setRejectedAt(LocalDateTime.now());
        return wikiPageRepository.save(page);
    }

    @Override
    public List<WikiPage> findRequiringApproval(Integer teamNumber, Integer season) {
        return findPendingApproval(teamNumber, season);
    }

    // =========================================================================
    // LOCK MANAGEMENT
    // =========================================================================

    @Override
    public List<WikiPage> findLockedPages(Integer teamNumber, Integer season) {
        return wikiPageRepository.findLockedPages(teamNumber, season);
    }

    @Override
    public WikiPage lockPage(Long pageId, TeamMember lockedBy, LocalDateTime lockedUntil) {
        Optional<WikiPage> pageOpt = wikiPageRepository.findById(pageId);
        if (!pageOpt.isPresent()) {
            throw new RuntimeException("Wiki page not found with id: " + pageId);
        }
        
        WikiPage page = pageOpt.get();
        page.setIsLocked(true);
        page.setLockedBy(lockedBy);
        page.setLockedAt(LocalDateTime.now());
        page.setLockedUntil(lockedUntil);
        return wikiPageRepository.save(page);
    }

    @Override
    public WikiPage unlockPage(Long pageId) {
        Optional<WikiPage> pageOpt = wikiPageRepository.findById(pageId);
        if (!pageOpt.isPresent()) {
            throw new RuntimeException("Wiki page not found with id: " + pageId);
        }
        
        WikiPage page = pageOpt.get();
        page.setIsLocked(false);
        page.setLockedBy(null);
        page.setLockedAt(null);
        page.setLockedUntil(null);
        return wikiPageRepository.save(page);
    }

    @Override
    public boolean isPageLocked(Long pageId) {
        Optional<WikiPage> pageOpt = wikiPageRepository.findById(pageId);
        if (!pageOpt.isPresent()) {
            return false;
        }
        
        WikiPage page = pageOpt.get();
        if (!page.getIsLocked()) {
            return false;
        }
        
        // Check if lock has expired
        if (page.getLockedUntil() != null && page.getLockedUntil().isBefore(LocalDateTime.now())) {
            unlockPage(pageId);
            return false;
        }
        
        return true;
    }

    @Override
    public WikiPage forceUnlockPage(Long pageId, TeamMember unlockedBy) {
        WikiPage page = unlockPage(pageId);
        page.setForceUnlockedBy(unlockedBy);
        page.setForceUnlockedAt(LocalDateTime.now());
        return wikiPageRepository.save(page);
    }

    // =========================================================================
    // FEATURED AND SPECIAL PAGES
    // =========================================================================

    @Override
    public List<WikiPage> findFeaturedPages(Integer teamNumber, Integer season) {
        return wikiPageRepository.findFeaturedPages(teamNumber, season);
    }

    @Override
    public WikiPage setPageFeatured(Long pageId, boolean featured) {
        Optional<WikiPage> pageOpt = wikiPageRepository.findById(pageId);
        if (!pageOpt.isPresent()) {
            throw new RuntimeException("Wiki page not found with id: " + pageId);
        }
        
        WikiPage page = pageOpt.get();
        page.setIsFeatured(featured);
        if (featured) {
            page.setFeaturedAt(LocalDateTime.now());
        }
        return wikiPageRepository.save(page);
    }

    @Override
    public List<WikiPage> findHighRatedPages(Integer teamNumber, Integer season, Double minRating, Integer minRatingCount) {
        return wikiPageRepository.findHighRatedPages(teamNumber, season, minRating, minRatingCount);
    }

    @Override
    public List<WikiPage> findPopularPages(Integer teamNumber, Integer season, Long minViews, LocalDateTime recentCutoff) {
        return wikiPageRepository.findPopularPages(teamNumber, season, minViews, recentCutoff);
    }

    @Override
    public List<WikiPage> findOrphanedPages(Integer teamNumber, Integer season) {
        return wikiPageRepository.findOrphanedPages(teamNumber, season);
    }

    @Override
    public WikiPage ratePage(Long pageId, TeamMember rater, Integer rating) {
        Optional<WikiPage> pageOpt = wikiPageRepository.findById(pageId);
        if (!pageOpt.isPresent()) {
            throw new RuntimeException("Wiki page not found with id: " + pageId);
        }
        
        WikiPage page = pageOpt.get();
        
        // Update rating statistics
        Double currentAverage = page.getAverageRating();
        Integer currentCount = page.getRatingCount();
        
        if (currentAverage == null || currentCount == null) {
            page.setAverageRating(rating.doubleValue());
            page.setRatingCount(1);
        } else {
            double newAverage = ((currentAverage * currentCount) + rating) / (currentCount + 1);
            page.setAverageRating(newAverage);
            page.setRatingCount(currentCount + 1);
        }
        
        return wikiPageRepository.save(page);
    }

    // =========================================================================
    // CONTENT MANAGEMENT
    // =========================================================================

    @Override
    public WikiPage updateContent(Long pageId, String content, TeamMember updatedBy) {
        return createNewVersion(pageId, content, updatedBy, "Content updated");
    }

    @Override
    public WikiPage addAttachment(Long pageId, String attachmentUrl, String description) {
        Optional<WikiPage> pageOpt = wikiPageRepository.findById(pageId);
        if (!pageOpt.isPresent()) {
            throw new RuntimeException("Wiki page not found with id: " + pageId);
        }
        
        WikiPage page = pageOpt.get();
        page.getAttachmentUrls().add(attachmentUrl);
        page.setHasAttachments(true);
        return wikiPageRepository.save(page);
    }

    @Override
    public WikiPage removeAttachment(Long pageId, String attachmentUrl) {
        Optional<WikiPage> pageOpt = wikiPageRepository.findById(pageId);
        if (!pageOpt.isPresent()) {
            throw new RuntimeException("Wiki page not found with id: " + pageId);
        }
        
        WikiPage page = pageOpt.get();
        page.getAttachmentUrls().remove(attachmentUrl);
        page.setHasAttachments(!page.getAttachmentUrls().isEmpty());
        return wikiPageRepository.save(page);
    }

    @Override
    public WikiPage updateMetadata(Long pageId, String title, String summary, List<String> tags, List<String> categories) {
        Optional<WikiPage> pageOpt = wikiPageRepository.findById(pageId);
        if (!pageOpt.isPresent()) {
            throw new RuntimeException("Wiki page not found with id: " + pageId);
        }
        
        WikiPage page = pageOpt.get();
        page.setTitle(title);
        page.setSummary(summary);
        page.setTags(tags);
        page.setCategories(categories);
        page.setUpdatedAt(LocalDateTime.now());
        
        return wikiPageRepository.save(page);
    }

    @Override
    public WikiPage processContentFeatures(Long pageId) {
        Optional<WikiPage> pageOpt = wikiPageRepository.findById(pageId);
        if (!pageOpt.isPresent()) {
            return null;
        }
        
        WikiPage page = pageOpt.get();
        String content = page.getContent();
        
        if (content == null) {
            return page;
        }
        
        // Analyze content features
        page.setHasCodeBlocks(content.contains("```") || content.contains("<code>"));
        page.setHasImages(content.contains("![") || content.contains("<img"));
        page.setHasVideos(content.contains("youtube.com") || content.contains("vimeo.com") || content.contains("<video"));
        
        // Calculate word count
        String textContent = content.replaceAll("<[^>]*>", "").replaceAll("```[\\s\\S]*?```", "");
        page.setWordCount(textContent.split("\\s+").length);
        
        // Calculate reading time (average 200 words per minute)
        page.setReadingTime((int) Math.ceil(page.getWordCount() / 200.0));
        
        return wikiPageRepository.save(page);
    }

    @Override
    public List<String> validatePageContent(String content) {
        List<String> issues = new ArrayList<>();
        
        if (content == null || content.trim().isEmpty()) {
            issues.add("Content cannot be empty");
        }
        
        if (content != null && content.length() > 1000000) { // 1MB limit
            issues.add("Content exceeds maximum length limit");
        }
        
        // Check for potentially malicious content
        if (content != null && (content.contains("<script") || content.contains("javascript:"))) {
            issues.add("Content contains potentially unsafe elements");
        }
        
        return issues;
    }

    // =========================================================================
    // TEMPLATE AND STRUCTURE
    // =========================================================================

    @Override
    public WikiPage createPageFromTemplate(WikiPage template, Integer teamNumber, Integer season, String title) {
        WikiPage newPage = new WikiPage();
        copyPageProperties(template, newPage);
        
        newPage.setId(null);
        newPage.setTeamNumber(teamNumber);
        newPage.setSeason(season);
        newPage.setTitle(title);
        newPage.setSlug(generateSlug(title, teamNumber, season));
        newPage.setCreatedAt(LocalDateTime.now());
        newPage.setUpdatedAt(LocalDateTime.now());
        newPage.setVersion(1);
        newPage.setIsCurrentVersion(true);
        
        return createPage(newPage);
    }

    @Override
    public WikiPage saveAsTemplate(Long pageId, String templateName) {
        Optional<WikiPage> pageOpt = wikiPageRepository.findById(pageId);
        if (!pageOpt.isPresent()) {
            throw new RuntimeException("Wiki page not found with id: " + pageId);
        }
        
        WikiPage page = pageOpt.get();
        WikiPage template = new WikiPage();
        copyPageProperties(page, template);
        
        template.setId(null);
        template.setTitle(templateName);
        template.setSlug(generateSlug(templateName, page.getTeamNumber(), null));
        template.setPageType(WikiPage.PageType.TEMPLATE);
        template.setIsTemplate(true);
        template.setSeason(null); // Templates are season-independent
        
        return wikiPageRepository.save(template);
    }

    @Override
    public List<WikiPage> findTemplates(Integer teamNumber) {
        return wikiPageRepository.findByTeamNumberAndSeasonAndPageTypeAndIsActiveTrue(
                teamNumber, null, WikiPage.PageType.TEMPLATE);
    }

    @Override
    public List<WikiPage> applyTemplateStructure(WikiPage template, Integer teamNumber, Integer season) {
        List<WikiPage> createdPages = new ArrayList<>();
        
        // Create root page from template
        WikiPage rootPage = createPageFromTemplate(template, teamNumber, season, template.getTitle());
        createdPages.add(rootPage);
        
        // Apply child structure recursively
        List<WikiPage> childTemplates = findChildPages(template);
        for (WikiPage childTemplate : childTemplates) {
            List<WikiPage> childPages = applyTemplateStructureRecursive(childTemplate, teamNumber, season, rootPage);
            createdPages.addAll(childPages);
        }
        
        return createdPages;
    }

    // =========================================================================
    // STATISTICS AND ANALYTICS
    // =========================================================================

    @Override
    public Map<WikiPage.PageType, Long> countByPageType(Integer teamNumber, Integer season) {
        List<Object[]> results = wikiPageRepository.countByPageType(teamNumber, season);
        Map<WikiPage.PageType, Long> counts = new HashMap<>();
        
        for (Object[] result : results) {
            WikiPage.PageType type = (WikiPage.PageType) result[0];
            Long count = (Long) result[1];
            counts.put(type, count);
        }
        
        return counts;
    }

    @Override
    public Map<WikiPage.PageStatus, Long> countByStatus(Integer teamNumber, Integer season) {
        List<Object[]> results = wikiPageRepository.countByStatus(teamNumber, season);
        Map<WikiPage.PageStatus, Long> counts = new HashMap<>();
        
        for (Object[] result : results) {
            WikiPage.PageStatus status = (WikiPage.PageStatus) result[0];
            Long count = (Long) result[1];
            counts.put(status, count);
        }
        
        return counts;
    }

    @Override
    public Long calculateTotalViewCount(Integer teamNumber, Integer season) {
        return wikiPageRepository.findTotalViewCount(teamNumber, season).orElse(0L);
    }

    @Override
    public Integer calculateTotalWordCount(Integer teamNumber, Integer season) {
        return wikiPageRepository.findTotalWordCount(teamNumber, season).orElse(0);
    }

    @Override
    public Map<String, Long> findMostCommonTags(Integer teamNumber, Integer season) {
        List<Object[]> results = wikiPageRepository.findMostCommonTags(teamNumber, season);
        Map<String, Long> tags = new LinkedHashMap<>();
        
        for (Object[] result : results) {
            String tag = (String) result[0];
            Long count = (Long) result[1];
            tags.put(tag, count);
        }
        
        return tags;
    }

    @Override
    public Map<String, Long> findMostCommonCategories(Integer teamNumber, Integer season) {
        List<Object[]> results = wikiPageRepository.findMostCommonCategories(teamNumber, season);
        Map<String, Long> categories = new LinkedHashMap<>();
        
        for (Object[] result : results) {
            String category = (String) result[0];
            Long count = (Long) result[1];
            categories.put(category, count);
        }
        
        return categories;
    }

    @Override
    public Map<String, Double> calculateAverageMetrics(Integer teamNumber, Integer season) {
        List<Object[]> results = wikiPageRepository.findAverageMetrics(teamNumber, season);
        Map<String, Double> metrics = new HashMap<>();
        
        if (!results.isEmpty()) {
            Object[] result = results.get(0);
            metrics.put("averageViewCount", (Double) result[0]);
            metrics.put("averageEditCount", (Double) result[1]);
            metrics.put("averageWordCount", (Double) result[2]);
            metrics.put("averageRating", (Double) result[3]);
        }
        
        return metrics;
    }

    @Override
    public Map<String, Object> generateAnalyticsReport(Integer teamNumber, Integer season) {
        Map<String, Object> report = new HashMap<>();
        
        report.put("teamNumber", teamNumber);
        report.put("season", season);
        report.put("generatedAt", LocalDateTime.now());
        
        // Basic counts
        report.put("totalPages", countActivePages(teamNumber, season));
        report.put("pagesByType", countByPageType(teamNumber, season));
        report.put("pagesByStatus", countByStatus(teamNumber, season));
        
        // Content metrics
        report.put("totalViewCount", calculateTotalViewCount(teamNumber, season));
        report.put("totalWordCount", calculateTotalWordCount(teamNumber, season));
        report.put("averageMetrics", calculateAverageMetrics(teamNumber, season));
        
        // Popular content
        report.put("mostCommonTags", findMostCommonTags(teamNumber, season));
        report.put("mostCommonCategories", findMostCommonCategories(teamNumber, season));
        report.put("featuredPages", findFeaturedPages(teamNumber, season));
        report.put("popularPages", findPopularPages(teamNumber, season, 10L, LocalDateTime.now().minusDays(30)));
        
        // Maintenance items
        report.put("emptyPages", findEmptyPages(teamNumber, season).size());
        report.put("orphanedPages", findOrphanedPages(teamNumber, season).size());
        report.put("pendingApproval", findPendingApproval(teamNumber, season).size());
        
        return report;
    }

    // =========================================================================
    // MAINTENANCE AND CLEANUP
    // =========================================================================

    @Override
    public List<WikiPage> findEmptyPages(Integer teamNumber, Integer season) {
        return wikiPageRepository.findEmptyPages(teamNumber, season);
    }

    @Override
    public List<WikiPage> findStalePages(Integer teamNumber, Integer season, LocalDateTime staleDate, Long minViews) {
        return wikiPageRepository.findStalePages(teamNumber, season, staleDate, minViews);
    }

    @Override
    public List<String> findBrokenLinks(Integer teamNumber, Integer season) {
        return wikiPageRepository.findInternalLinks(teamNumber, season);
    }

    @Override
    public Map<String, Long> findDuplicateSlugs(Integer teamNumber, Integer season) {
        List<Object[]> results = wikiPageRepository.findDuplicateSlugs(teamNumber, season);
        Map<String, Long> duplicates = new HashMap<>();
        
        for (Object[] result : results) {
            String slug = (String) result[0];
            Long count = (Long) result[1];
            duplicates.put(slug, count);
        }
        
        return duplicates;
    }

    @Override
    public void updateSearchRankings(Integer teamNumber, Integer season) {
        List<WikiPage> pages = findCurrentVersions(teamNumber, season);
        
        for (WikiPage page : pages) {
            int rank = calculateSearchRank(page);
            page.setSearchRank(rank);
            wikiPageRepository.save(page);
        }
    }

    @Override
    public void rebuildPagePaths(Integer teamNumber, Integer season) {
        List<WikiPage> pages = findCurrentVersions(teamNumber, season);
        
        for (WikiPage page : pages) {
            String newPath = calculatePagePath(page);
            page.setPagePath(newPath);
            wikiPageRepository.save(page);
        }
    }

    @Override
    public void cleanupOrphanedVersions(Integer teamNumber, Integer season) {
        // Implementation would identify and clean up version history entries
        // that are no longer referenced by current versions
    }

    // =========================================================================
    // ARCHIVE AND HISTORY
    // =========================================================================

    @Override
    public List<WikiPage> findArchivedPages(Integer teamNumber, Integer season) {
        return wikiPageRepository.findArchivedPages(teamNumber, season);
    }

    @Override
    public List<WikiPage> findArchivedInDateRange(Integer teamNumber, Integer season, 
                                                 LocalDateTime startDate, LocalDateTime endDate) {
        return wikiPageRepository.findArchivedInDateRange(teamNumber, season, startDate, endDate);
    }

    @Override
    public List<WikiPage> findByArchiveReason(Integer teamNumber, Integer season, String reason) {
        return wikiPageRepository.findByArchiveReason(teamNumber, season, reason);
    }

    @Override
    public WikiPage restoreArchivedPage(Long pageId) {
        Optional<WikiPage> pageOpt = wikiPageRepository.findById(pageId);
        if (!pageOpt.isPresent()) {
            throw new RuntimeException("Wiki page not found with id: " + pageId);
        }
        
        WikiPage page = pageOpt.get();
        page.setIsArchived(false);
        page.setIsActive(true);
        page.setArchiveReason(null);
        page.setArchivedAt(null);
        page.setRestoredAt(LocalDateTime.now());
        
        return wikiPageRepository.save(page);
    }

    @Override
    public void permanentlyDeletePage(Long pageId) {
        wikiPageRepository.deleteById(pageId);
    }

    // =========================================================================
    // CROSS-SEASON ANALYSIS
    // =========================================================================

    @Override
    public List<WikiPage> findMultiSeasonPages(Integer teamNumber, List<Integer> seasons) {
        return wikiPageRepository.findMultiSeasonPages(teamNumber, seasons);
    }

    @Override
    public List<WikiPage> findByTitleAcrossSeasons(Integer teamNumber, String title) {
        return wikiPageRepository.findByTitleAcrossSeasons(teamNumber, title);
    }

    @Override
    public Map<String, Long> findCarryoverPages(Integer teamNumber) {
        List<Object[]> results = wikiPageRepository.findCarryoverPages(teamNumber);
        Map<String, Long> carryover = new LinkedHashMap<>();
        
        for (Object[] result : results) {
            String title = (String) result[0];
            Long seasonCount = (Long) result[1];
            carryover.put(title, seasonCount);
        }
        
        return carryover;
    }

    @Override
    public List<WikiPage> copyStructureToNewSeason(Integer teamNumber, Integer fromSeason, Integer toSeason) {
        List<WikiPage> sourcePages = findCurrentVersions(teamNumber, fromSeason);
        List<WikiPage> copiedPages = new ArrayList<>();
        
        for (WikiPage sourcePage : sourcePages) {
            WikiPage newPage = new WikiPage();
            copyPageProperties(sourcePage, newPage);
            
            newPage.setId(null);
            newPage.setSeason(toSeason);
            newPage.setCreatedAt(LocalDateTime.now());
            newPage.setUpdatedAt(LocalDateTime.now());
            newPage.setVersion(1);
            newPage.setIsCurrentVersion(true);
            
            copiedPages.add(wikiPageRepository.save(newPage));
        }
        
        return copiedPages;
    }

    // =========================================================================
    // BULK OPERATIONS
    // =========================================================================

    @Override
    public Long countActivePages(Integer teamNumber, Integer season) {
        return wikiPageRepository.countActivePages(teamNumber, season);
    }

    @Override
    public List<WikiPage> findAllCurrentPages(Integer teamNumber, Integer season) {
        return wikiPageRepository.findAllCurrentPages(teamNumber, season);
    }

    @Override
    public List<WikiPage> createBulkPages(List<WikiPage> pages) {
        List<WikiPage> createdPages = new ArrayList<>();
        for (WikiPage page : pages) {
            createdPages.add(createPage(page));
        }
        return createdPages;
    }

    @Override
    public List<WikiPage> updateBulkPages(Map<Long, WikiPage> pageUpdates) {
        List<WikiPage> updatedPages = new ArrayList<>();
        for (Map.Entry<Long, WikiPage> entry : pageUpdates.entrySet()) {
            updatedPages.add(updatePage(entry.getKey(), entry.getValue()));
        }
        return updatedPages;
    }

    @Override
    public void bulkArchivePages(List<Long> pageIds, String reason) {
        for (Long pageId : pageIds) {
            archivePage(pageId, reason);
        }
    }

    @Override
    public List<Map<String, Object>> exportPages(Integer teamNumber, Integer season) {
        List<WikiPage> pages = findAllCurrentPages(teamNumber, season);
        List<Map<String, Object>> export = new ArrayList<>();
        
        for (WikiPage page : pages) {
            Map<String, Object> pageData = new HashMap<>();
            pageData.put("id", page.getId());
            pageData.put("title", page.getTitle());
            pageData.put("slug", page.getSlug());
            pageData.put("content", page.getContent());
            pageData.put("pageType", page.getPageType());
            pageData.put("status", page.getStatus());
            pageData.put("createdAt", page.getCreatedAt());
            pageData.put("updatedAt", page.getUpdatedAt());
            pageData.put("tags", page.getTags());
            pageData.put("categories", page.getCategories());
            export.add(pageData);
        }
        
        return export;
    }

    // =========================================================================
    // INTEGRATION AND EXTERNAL SYSTEMS
    // =========================================================================

    @Override
    public WikiPage importPageFromExternalFormat(Map<String, Object> data, String format) {
        WikiPage page = new WikiPage();
        
        // Parse based on format
        if ("json".equals(format)) {
            page.setTitle((String) data.get("title"));
            page.setContent((String) data.get("content"));
            page.setTeamNumber((Integer) data.get("teamNumber"));
            page.setSeason((Integer) data.get("season"));
            
            @SuppressWarnings("unchecked")
            List<String> tags = (List<String>) data.get("tags");
            page.setTags(tags);
            
            @SuppressWarnings("unchecked")
            List<String> categories = (List<String>) data.get("categories");
            page.setCategories(categories);
        }
        
        return createPage(page);
    }

    @Override
    public Map<String, Object> exportPageToExternalFormat(Long pageId, String format) {
        Optional<WikiPage> pageOpt = wikiPageRepository.findById(pageId);
        if (!pageOpt.isPresent()) {
            throw new RuntimeException("Wiki page not found with id: " + pageId);
        }
        
        WikiPage page = pageOpt.get();
        Map<String, Object> export = new HashMap<>();
        
        if ("json".equals(format)) {
            export.put("id", page.getId());
            export.put("title", page.getTitle());
            export.put("slug", page.getSlug());
            export.put("content", page.getContent());
            export.put("pageType", page.getPageType());
            export.put("status", page.getStatus());
            export.put("teamNumber", page.getTeamNumber());
            export.put("season", page.getSeason());
            export.put("tags", page.getTags());
            export.put("categories", page.getCategories());
            export.put("createdAt", page.getCreatedAt());
            export.put("updatedAt", page.getUpdatedAt());
        }
        
        return export;
    }

    @Override
    public WikiPage syncWithExternalSystem(Long pageId, String externalSystem) {
        // Implementation would sync with external documentation systems
        return findById(pageId).orElse(null);
    }

    @Override
    public List<Map<String, Object>> generateSitemap(Integer teamNumber, Integer season) {
        return wikiPageRepository.findForSitemap(teamNumber, season).stream()
                .map(result -> {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("slug", result[0]);
                    entry.put("lastModified", result[1]);
                    return entry;
                })
                .collect(Collectors.toList());
    }

    // =========================================================================
    // NOTIFICATION AND EVENTS
    // =========================================================================

    @Override
    public void notifyPageSubscribers(Long pageId, String changeType) {
        // Implementation would send notifications to subscribers
        List<TeamMember> subscribers = findPageSubscribers(pageId);
        // Send notifications via email, WebSocket, etc.
    }

    @Override
    public void subscribeToPage(Long pageId, Long userId) {
        Optional<WikiPage> pageOpt = wikiPageRepository.findById(pageId);
        if (pageOpt.isPresent()) {
            WikiPage page = pageOpt.get();
            page.getSubscribers().add(userId);
            wikiPageRepository.save(page);
        }
    }

    @Override
    public void unsubscribeFromPage(Long pageId, Long userId) {
        Optional<WikiPage> pageOpt = wikiPageRepository.findById(pageId);
        if (pageOpt.isPresent()) {
            WikiPage page = pageOpt.get();
            page.getSubscribers().remove(userId);
            wikiPageRepository.save(page);
        }
    }

    @Override
    public List<TeamMember> findPageSubscribers(Long pageId) {
        // Implementation would return list of subscribed team members
        return new ArrayList<>();
    }

    // =========================================================================
    // VALIDATION AND BUSINESS RULES
    // =========================================================================

    @Override
    public List<String> validatePage(WikiPage wikiPage) {
        List<String> errors = new ArrayList<>();
        
        if (wikiPage.getTitle() == null || wikiPage.getTitle().trim().isEmpty()) {
            errors.add("Title is required");
        }
        
        if (wikiPage.getTeamNumber() == null) {
            errors.add("Team number is required");
        }
        
        if (wikiPage.getSeason() == null) {
            errors.add("Season is required");
        }
        
        if (wikiPage.getPageType() == null) {
            errors.add("Page type is required");
        }
        
        // Validate slug uniqueness
        if (wikiPage.getSlug() != null && !validateSlugUniqueness(wikiPage.getSlug(), 
                wikiPage.getTeamNumber(), wikiPage.getSeason(), wikiPage.getId())) {
            errors.add("Slug must be unique within team and season");
        }
        
        // Validate content
        if (wikiPage.getContent() != null) {
            errors.addAll(validatePageContent(wikiPage.getContent()));
        }
        
        return errors;
    }

    @Override
    public boolean validatePageHierarchy(WikiPage page) {
        // Check for circular references
        WikiPage current = page.getParentPage();
        Set<Long> visited = new HashSet<>();
        
        while (current != null) {
            if (current.getId().equals(page.getId()) || visited.contains(current.getId())) {
                return false; // Circular reference detected
            }
            visited.add(current.getId());
            current = current.getParentPage();
        }
        
        return true;
    }

    @Override
    public boolean validateSlugUniqueness(String slug, Integer teamNumber, Integer season, Long excludePageId) {
        Optional<WikiPage> existing = wikiPageRepository.findByTeamNumberAndSeasonAndSlugAndIsActiveTrue(
                teamNumber, season, slug);
        
        if (!existing.isPresent()) {
            return true;
        }
        
        // Allow if it's the same page being updated
        return excludePageId != null && existing.get().getId().equals(excludePageId);
    }

    @Override
    public boolean validateUserPermissions(Long pageId, Long userId, String operation) {
        switch (operation.toLowerCase()) {
            case "view":
                return canUserViewPage(pageId, userId);
            case "edit":
                return canUserEditPage(pageId, userId);
            default:
                return false;
        }
    }

    @Override
    public Map<String, Object> validateContentQuality(String content) {
        Map<String, Object> quality = new HashMap<>();
        
        if (content == null) {
            quality.put("score", 0);
            quality.put("issues", Arrays.asList("No content"));
            return quality;
        }
        
        int score = 100;
        List<String> issues = new ArrayList<>();
        
        // Check minimum length
        if (content.length() < 100) {
            score -= 20;
            issues.add("Content is too short");
        }
        
        // Check for headings
        if (!content.contains("#") && !content.contains("<h")) {
            score -= 10;
            issues.add("No headings found");
        }
        
        // Check for links
        if (!content.contains("[") && !content.contains("<a")) {
            score -= 5;
            issues.add("No links found");
        }
        
        quality.put("score", Math.max(0, score));
        quality.put("issues", issues);
        quality.put("wordCount", content.split("\\s+").length);
        
        return quality;
    }

    // =========================================================================
    // PRIVATE HELPER METHODS
    // =========================================================================

    private String generateSlug(String title, Integer teamNumber, Integer season) {
        String baseSlug = title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        
        String slug = baseSlug;
        int counter = 1;
        
        // Ensure uniqueness
        while (!validateSlugUniqueness(slug, teamNumber, season, null)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }
        
        return slug;
    }

    private void calculateHierarchyInfo(WikiPage page) {
        if (page.getParentPage() != null) {
            page.setPageLevel(page.getParentPage().getPageLevel() + 1);
        } else {
            page.setPageLevel(0);
        }
        
        page.setPagePath(calculatePagePath(page));
    }

    private void copyPageProperties(WikiPage source, WikiPage target) {
        target.setTeamNumber(source.getTeamNumber());
        target.setSeason(source.getSeason());
        target.setTitle(source.getTitle());
        target.setContent(source.getContent());
        target.setContentMarkdown(source.getContentMarkdown());
        target.setSummary(source.getSummary());
        target.setPageType(source.getPageType());
        target.setVisibility(source.getVisibility());
        target.setTags(new ArrayList<>(source.getTags()));
        target.setCategories(new ArrayList<>(source.getCategories()));
        target.setCreatedBy(source.getCreatedBy());
        target.setViewPermission(source.getViewPermission());
        target.setEditPermission(source.getEditPermission());
        target.setRequireApproval(source.getRequireApproval());
    }

    private WikiPage.ChangeType determineChangeType(String oldContent, String newContent) {
        if (oldContent == null || oldContent.isEmpty()) {
            return WikiPage.ChangeType.MAJOR;
        }
        
        if (newContent == null || newContent.isEmpty()) {
            return WikiPage.ChangeType.MAJOR;
        }
        
        // Simple heuristic: if more than 50% changed, it's major
        int oldLength = oldContent.length();
        int newLength = newContent.length();
        int maxLength = Math.max(oldLength, newLength);
        
        if (maxLength == 0) {
            return WikiPage.ChangeType.MINOR;
        }
        
        // Calculate similarity (very basic)
        double similarity = calculateContentSimilarity(oldContent, newContent);
        
        if (similarity < 0.5) {
            return WikiPage.ChangeType.MAJOR;
        } else if (similarity < 0.8) {
            return WikiPage.ChangeType.MINOR;
        } else {
            return WikiPage.ChangeType.EDITORIAL;
        }
    }

    private double calculateContentSimilarity(String content1, String content2) {
        // Very basic similarity calculation
        Set<String> words1 = new HashSet<>(Arrays.asList(content1.toLowerCase().split("\\s+")));
        Set<String> words2 = new HashSet<>(Arrays.asList(content2.toLowerCase().split("\\s+")));
        
        Set<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2);
        
        Set<String> union = new HashSet<>(words1);
        union.addAll(words2);
        
        return union.isEmpty() ? 1.0 : (double) intersection.size() / union.size();
    }

    private List<String> calculateContentDifferences(Long pageId, Integer version1, Integer version2) {
        // Implementation would return detailed differences between versions
        return Arrays.asList("Content differences would be calculated here");
    }

    private int calculateSearchRank(WikiPage page) {
        int rank = 0;
        
        // Base rank on view count
        rank += Math.min(page.getViewCount().intValue() / 10, 50);
        
        // Boost for recent activity
        if (page.getUpdatedAt().isAfter(LocalDateTime.now().minusDays(7))) {
            rank += 20;
        }
        
        // Boost for good ratings
        if (page.getAverageRating() != null && page.getAverageRating() > 4.0) {
            rank += 15;
        }
        
        // Boost for featured pages
        if (page.getIsFeatured()) {
            rank += 30;
        }
        
        // Penalty for old content
        if (page.getUpdatedAt().isBefore(LocalDateTime.now().minusMonths(6))) {
            rank -= 10;
        }
        
        return Math.max(0, Math.min(100, rank));
    }

    private List<WikiPage> applyTemplateStructureRecursive(WikiPage template, Integer teamNumber, 
                                                          Integer season, WikiPage parent) {
        List<WikiPage> createdPages = new ArrayList<>();
        
        WikiPage newPage = createPageFromTemplate(template, teamNumber, season, template.getTitle());
        newPage.setParentPage(parent);
        calculateHierarchyInfo(newPage);
        newPage = wikiPageRepository.save(newPage);
        createdPages.add(newPage);
        
        // Process children recursively
        List<WikiPage> childTemplates = findChildPages(template);
        for (WikiPage childTemplate : childTemplates) {
            List<WikiPage> childPages = applyTemplateStructureRecursive(childTemplate, teamNumber, season, newPage);
            createdPages.addAll(childPages);
        }
        
        return createdPages;
    }
}