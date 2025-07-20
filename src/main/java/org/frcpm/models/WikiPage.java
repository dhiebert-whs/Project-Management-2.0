// src/main/java/org/frcpm/models/WikiPage.java

package org.frcpm.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Wiki Page model for FRC team knowledge management.
 * 
 * Provides comprehensive wiki functionality for FRC teams including
 * hierarchical documentation, version control, collaborative editing,
 * and knowledge sharing for team coordination and learning.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-KnowledgeManagement
 * @since Phase 4D.1 Team Wiki and Documentation System
 */
@Entity
@Table(name = "wiki_pages")
public class WikiPage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Integer teamNumber;
    
    @Column(nullable = false)
    private Integer season;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(length = 300)
    private String slug; // URL-friendly identifier
    
    @Column(columnDefinition = "LONGTEXT")
    private String content;
    
    @Column(columnDefinition = "LONGTEXT")
    private String contentMarkdown;
    
    @Column(length = 500)
    private String summary;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PageType pageType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PageStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VisibilityLevel visibility;
    
    // Hierarchical Structure
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_page_id")
    private WikiPage parentPage;
    
    @OneToMany(mappedBy = "parentPage", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WikiPage> childPages = new ArrayList<>();
    
    @Column(nullable = false)
    private Integer pageLevel = 0; // Depth in hierarchy
    
    @Column(length = 1000)
    private String pagePath; // Full path from root
    
    @Column(nullable = false)
    private Integer sortOrder = 0;
    
    // Version Control
    @Column(nullable = false)
    private Integer version = 1;
    
    @Column(nullable = false)
    private Boolean isCurrentVersion = true;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_version_id")
    private WikiPage currentVersion;
    
    @OneToMany(mappedBy = "currentVersion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WikiPage> versionHistory = new ArrayList<>();
    
    @Column(length = 1000)
    private String changeLog;
    
    @Enumerated(EnumType.STRING)
    private ChangeType changeType;
    
    // Content Metadata
    @ElementCollection
    @CollectionTable(name = "wiki_page_tags", joinColumns = @JoinColumn(name = "page_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "wiki_page_categories", joinColumns = @JoinColumn(name = "page_id"))
    @Column(name = "category")
    private List<String> categories = new ArrayList<>();
    
    @Column(length = 100)
    private String language = "en";
    
    @Column(nullable = false)
    private Integer wordCount = 0;
    
    @Column(nullable = false)
    private Integer readingTimeMinutes = 0;
    
    // Access Control and Permissions
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccessLevel editPermission;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccessLevel viewPermission;
    
    @ElementCollection
    @CollectionTable(name = "wiki_page_editors", joinColumns = @JoinColumn(name = "page_id"))
    @Column(name = "team_member_id")
    private List<Long> authorizedEditors = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "wiki_page_viewers", joinColumns = @JoinColumn(name = "page_id"))
    @Column(name = "team_member_id")
    private List<Long> authorizedViewers = new ArrayList<>();
    
    // Collaboration Features
    @Column(nullable = false)
    private Boolean allowComments = true;
    
    @Column(nullable = false)
    private Boolean allowSuggestions = true;
    
    @Column(nullable = false)
    private Boolean trackChanges = true;
    
    @Column(nullable = false)
    private Boolean requireApproval = false;
    
    @Column(nullable = false)
    private Boolean isLocked = false;
    
    @Column
    private LocalDateTime lockedUntil;
    
    // Statistics and Analytics
    @Column(nullable = false)
    private Long viewCount = 0L;
    
    @Column(nullable = false)
    private Long editCount = 0L;
    
    @Column(nullable = false)
    private Integer commentCount = 0;
    
    @Column(nullable = false)
    private Integer attachmentCount = 0;
    
    @Column
    private LocalDateTime lastViewed;
    
    @Column
    private LocalDateTime lastEdited;
    
    @Column(nullable = false)
    private Double averageRating = 0.0;
    
    @Column(nullable = false)
    private Integer ratingCount = 0;
    
    // SEO and Search
    @Column(length = 300)
    private String metaDescription;
    
    @Column(length = 200)
    private String metaKeywords;
    
    @Column(nullable = false)
    private Boolean isSearchable = true;
    
    @Column(nullable = false)
    private Boolean isFeatured = false;
    
    @Column(nullable = false)
    private Integer searchRank = 0;
    
    // Content Features
    @Column(nullable = false)
    private Boolean hasTableOfContents = false;
    
    @Column(nullable = false)
    private Boolean hasCodeBlocks = false;
    
    @Column(nullable = false)
    private Boolean hasImages = false;
    
    @Column(nullable = false)
    private Boolean hasVideos = false;
    
    @Column(nullable = false)
    private Boolean hasAttachments = false;
    
    @Column(nullable = false)
    private Boolean hasLinks = false;
    
    // Template and Layout
    @Column(length = 100)
    private String templateName;
    
    @Column(length = 100)
    private String layoutStyle;
    
    @Column(columnDefinition = "LONGTEXT")
    private String customCss;
    
    @Column(columnDefinition = "LONGTEXT")
    private String customJavaScript;
    
    // Integration and External Links
    @ElementCollection
    @CollectionTable(name = "wiki_page_external_links", joinColumns = @JoinColumn(name = "page_id"))
    @Embedded
    private List<ExternalLink> externalLinks = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "wiki_page_attachments", joinColumns = @JoinColumn(name = "page_id"))
    @Embedded
    private List<PageAttachment> attachments = new ArrayList<>();
    
    // Administrative Fields
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @Column(nullable = false)
    private Boolean isArchived = false;
    
    @Column
    private LocalDateTime archivedAt;
    
    @Column(length = 500)
    private String archiveReason;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private TeamMember createdBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_updated_by_id")
    private TeamMember lastUpdatedBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    private TeamMember approvedBy;
    
    @Column
    private LocalDateTime approvedAt;
    
    /**
     * Types of wiki pages
     */
    public enum PageType {
        ARTICLE("Article", "General knowledge article"),
        TUTORIAL("Tutorial", "Step-by-step tutorial"),
        DOCUMENTATION("Documentation", "Technical documentation"),
        FAQ("FAQ", "Frequently asked questions"),
        PROCEDURE("Procedure", "Standard operating procedure"),
        REFERENCE("Reference", "Reference material"),
        TEMPLATE("Template", "Reusable template"),
        MEETING_NOTES("Meeting Notes", "Meeting minutes and notes"),
        PROJECT_OVERVIEW("Project Overview", "Project summary and overview"),
        TROUBLESHOOTING("Troubleshooting", "Problem-solving guide"),
        BEST_PRACTICES("Best Practices", "Recommended practices"),
        LESSON_LEARNED("Lesson Learned", "Experience and learning summary"),
        DESIGN_SPEC("Design Specification", "Design requirements and specifications"),
        USER_GUIDE("User Guide", "User instruction manual"),
        GLOSSARY("Glossary", "Terms and definitions"),
        INDEX("Index", "Navigation and index page");
        
        private final String displayName;
        private final String description;
        
        PageType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    /**
     * Page publication status
     */
    public enum PageStatus {
        DRAFT("Draft", "Page is being written"),
        REVIEW("Under Review", "Page is being reviewed"),
        APPROVED("Approved", "Page is approved and published"),
        PUBLISHED("Published", "Page is live and visible"),
        ARCHIVED("Archived", "Page is archived"),
        DEPRECATED("Deprecated", "Page is outdated"),
        PRIVATE("Private", "Page is private");
        
        private final String displayName;
        private final String description;
        
        PageStatus(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    /**
     * Page visibility levels
     */
    public enum VisibilityLevel {
        PUBLIC("Public", "Visible to everyone"),
        TEAM_ONLY("Team Only", "Visible to team members only"),
        MENTORS_ONLY("Mentors Only", "Visible to mentors only"),
        ADMINS_ONLY("Admins Only", "Visible to administrators only"),
        PRIVATE("Private", "Visible to specific users only"),
        RESTRICTED("Restricted", "Visible with special permissions");
        
        private final String displayName;
        private final String description;
        
        VisibilityLevel(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    /**
     * Types of changes made to pages
     */
    public enum ChangeType {
        CREATED("Created", "Page was created"),
        CONTENT_UPDATED("Content Updated", "Page content was modified"),
        STRUCTURE_CHANGED("Structure Changed", "Page structure was reorganized"),
        METADATA_UPDATED("Metadata Updated", "Page metadata was updated"),
        PERMISSIONS_CHANGED("Permissions Changed", "Access permissions were modified"),
        MOVED("Moved", "Page was moved to different location"),
        RENAMED("Renamed", "Page was renamed"),
        MERGED("Merged", "Page was merged with another"),
        SPLIT("Split", "Page was split into multiple pages"),
        ARCHIVED("Archived", "Page was archived"),
        RESTORED("Restored", "Page was restored from archive"),
        TEMPLATE_APPLIED("Template Applied", "Template was applied to page");
        
        private final String displayName;
        private final String description;
        
        ChangeType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    /**
     * Access control levels
     */
    public enum AccessLevel {
        ANYONE("Anyone", "No restrictions"),
        TEAM_MEMBERS("Team Members", "Team members only"),
        MENTORS("Mentors", "Mentors and above"),
        ADMINS("Administrators", "Administrators only"),
        SPECIFIC_USERS("Specific Users", "Only specified users"),
        ROLE_BASED("Role Based", "Based on specific roles");
        
        private final String displayName;
        private final String description;
        
        AccessLevel(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    /**
     * External link information
     */
    @Embeddable
    public static class ExternalLink {
        @Column(name = "link_url", length = 500)
        private String url;
        
        @Column(name = "link_title", length = 200)
        private String title;
        
        @Column(name = "link_description", length = 500)
        private String description;
        
        @Column(name = "link_type", length = 50)
        private String linkType; // reference, related, external, etc.
        
        public ExternalLink() {}
        
        public ExternalLink(String url, String title, String description, String linkType) {
            this.url = url;
            this.title = title;
            this.description = description;
            this.linkType = linkType;
        }
        
        // Getters and Setters
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getLinkType() { return linkType; }
        public void setLinkType(String linkType) { this.linkType = linkType; }
    }
    
    /**
     * Page attachment information
     */
    @Embeddable
    public static class PageAttachment {
        @Column(name = "attachment_url", length = 500)
        private String url;
        
        @Column(name = "attachment_filename", length = 200)
        private String filename;
        
        @Column(name = "attachment_type", length = 50)
        private String fileType;
        
        @Column(name = "attachment_size")
        private Long fileSize;
        
        @Column(name = "attachment_description", length = 500)
        private String description;
        
        @Column(name = "upload_date")
        private LocalDateTime uploadDate;
        
        public PageAttachment() {}
        
        public PageAttachment(String url, String filename, String fileType, Long fileSize, String description) {
            this.url = url;
            this.filename = filename;
            this.fileType = fileType;
            this.fileSize = fileSize;
            this.description = description;
            this.uploadDate = LocalDateTime.now();
        }
        
        // Getters and Setters
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        
        public String getFilename() { return filename; }
        public void setFilename(String filename) { this.filename = filename; }
        
        public String getFileType() { return fileType; }
        public void setFileType(String fileType) { this.fileType = fileType; }
        
        public Long getFileSize() { return fileSize; }
        public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public LocalDateTime getUploadDate() { return uploadDate; }
        public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }
    }
    
    // Constructors
    public WikiPage() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public WikiPage(Integer teamNumber, Integer season, String title, PageType pageType) {
        this();
        this.teamNumber = teamNumber;
        this.season = season;
        this.title = title;
        this.pageType = pageType;
        this.status = PageStatus.DRAFT;
        this.visibility = VisibilityLevel.TEAM_ONLY;
        this.editPermission = AccessLevel.TEAM_MEMBERS;
        this.viewPermission = AccessLevel.TEAM_MEMBERS;
        this.slug = generateSlug(title);
    }
    
    // Business Methods
    
    /**
     * Generates URL-friendly slug from title.
     */
    public String generateSlug(String title) {
        if (title == null) return null;
        
        return title.toLowerCase()
                   .replaceAll("[^a-z0-9\\s-]", "")
                   .replaceAll("\\s+", "-")
                   .replaceAll("-+", "-")
                   .replaceAll("^-|-$", "");
    }
    
    /**
     * Checks if page is editable by user.
     */
    public boolean isEditableBy(TeamMember user) {
        if (isLocked && (lockedUntil == null || LocalDateTime.now().isBefore(lockedUntil))) {
            return false;
        }
        
        switch (editPermission) {
            case ANYONE:
                return true;
            case TEAM_MEMBERS:
                return user != null;
            case SPECIFIC_USERS:
                return user != null && authorizedEditors.contains(user.getId());
            default:
                return false;
        }
    }
    
    /**
     * Checks if page is viewable by user.
     */
    public boolean isViewableBy(TeamMember user) {
        switch (viewPermission) {
            case ANYONE:
                return true;
            case TEAM_MEMBERS:
                return user != null;
            case SPECIFIC_USERS:
                return user != null && authorizedViewers.contains(user.getId());
            default:
                return false;
        }
    }
    
    /**
     * Adds view to page statistics.
     */
    public void addView() {
        this.viewCount++;
        this.lastViewed = LocalDateTime.now();
    }
    
    /**
     * Records an edit to page statistics.
     */
    public void recordEdit(TeamMember editor) {
        this.editCount++;
        this.lastEdited = LocalDateTime.now();
        this.lastUpdatedBy = editor;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Calculates reading time based on word count.
     */
    public void calculateReadingTime() {
        // Average reading speed: 200 words per minute
        this.readingTimeMinutes = Math.max(1, (int) Math.ceil(wordCount / 200.0));
    }
    
    /**
     * Updates word count from content.
     */
    public void updateWordCount() {
        if (content != null) {
            String textContent = content.replaceAll("<[^>]+>", " "); // Remove HTML tags
            String[] words = textContent.trim().split("\\s+");
            this.wordCount = words.length;
            calculateReadingTime();
        }
    }
    
    /**
     * Analyzes content features.
     */
    public void analyzeContentFeatures() {
        if (content != null) {
            this.hasCodeBlocks = content.contains("<code>") || content.contains("```");
            this.hasImages = content.contains("<img") || content.contains("![");
            this.hasVideos = content.contains("<video") || content.contains("youtube") || content.contains("vimeo");
            this.hasLinks = content.contains("<a ") || content.contains("http");
            this.hasTableOfContents = content.contains("[TOC]") || content.contains("table-of-contents");
        }
        
        this.hasAttachments = !attachments.isEmpty();
        this.attachmentCount = attachments.size();
    }
    
    /**
     * Creates new version of the page.
     */
    public WikiPage createNewVersion(String changeLog, ChangeType changeType) {
        WikiPage newVersion = new WikiPage();
        
        // Copy all fields
        newVersion.setTeamNumber(this.teamNumber);
        newVersion.setSeason(this.season);
        newVersion.setTitle(this.title);
        newVersion.setSlug(this.slug);
        newVersion.setContent(this.content);
        newVersion.setContentMarkdown(this.contentMarkdown);
        newVersion.setSummary(this.summary);
        newVersion.setPageType(this.pageType);
        newVersion.setStatus(this.status);
        newVersion.setVisibility(this.visibility);
        newVersion.setParentPage(this.parentPage);
        newVersion.setPageLevel(this.pageLevel);
        newVersion.setPagePath(this.pagePath);
        newVersion.setSortOrder(this.sortOrder);
        
        // Version control
        newVersion.setVersion(this.version + 1);
        newVersion.setIsCurrentVersion(true);
        newVersion.setCurrentVersion(this);
        newVersion.setChangeLog(changeLog);
        newVersion.setChangeType(changeType);
        
        // Mark current version as not current
        this.isCurrentVersion = false;
        
        return newVersion;
    }
    
    /**
     * Updates page path based on hierarchy.
     */
    public void updatePagePath() {
        StringBuilder path = new StringBuilder();
        
        WikiPage current = this;
        List<String> pathParts = new ArrayList<>();
        
        while (current != null) {
            if (current.getSlug() != null) {
                pathParts.add(0, current.getSlug());
            }
            current = current.getParentPage();
        }
        
        this.pagePath = "/" + String.join("/", pathParts);
    }
    
    /**
     * Adds tag to page.
     */
    public void addTag(String tag) {
        if (tag != null && !tag.trim().isEmpty() && !tags.contains(tag.toLowerCase())) {
            tags.add(tag.toLowerCase());
        }
    }
    
    /**
     * Removes tag from page.
     */
    public void removeTag(String tag) {
        tags.remove(tag.toLowerCase());
    }
    
    /**
     * Adds category to page.
     */
    public void addCategory(String category) {
        if (category != null && !category.trim().isEmpty() && !categories.contains(category)) {
            categories.add(category);
        }
    }
    
    /**
     * Removes category from page.
     */
    public void removeCategory(String category) {
        categories.remove(category);
    }
    
    /**
     * Adds external link to page.
     */
    public void addExternalLink(String url, String title, String description, String linkType) {
        ExternalLink link = new ExternalLink(url, title, description, linkType);
        externalLinks.add(link);
    }
    
    /**
     * Adds attachment to page.
     */
    public void addAttachment(String url, String filename, String fileType, Long fileSize, String description) {
        PageAttachment attachment = new PageAttachment(url, filename, fileType, fileSize, description);
        attachments.add(attachment);
        this.hasAttachments = true;
        this.attachmentCount = attachments.size();
    }
    
    /**
     * Archives the page.
     */
    public void archive(String reason) {
        this.isArchived = true;
        this.archivedAt = LocalDateTime.now();
        this.archiveReason = reason;
        this.status = PageStatus.ARCHIVED;
        this.isActive = false;
    }
    
    /**
     * Restores page from archive.
     */
    public void restore() {
        this.isArchived = false;
        this.archivedAt = null;
        this.archiveReason = null;
        this.status = PageStatus.PUBLISHED;
        this.isActive = true;
    }
    
    /**
     * Locks page for editing.
     */
    public void lock(LocalDateTime until) {
        this.isLocked = true;
        this.lockedUntil = until;
    }
    
    /**
     * Unlocks page for editing.
     */
    public void unlock() {
        this.isLocked = false;
        this.lockedUntil = null;
    }
    
    /**
     * Checks if page is currently locked.
     */
    public boolean isCurrentlyLocked() {
        return isLocked && (lockedUntil == null || LocalDateTime.now().isBefore(lockedUntil));
    }
    
    /**
     * Adds rating to page.
     */
    public void addRating(double rating) {
        double totalRating = averageRating * ratingCount + rating;
        ratingCount++;
        averageRating = totalRating / ratingCount;
    }
    
    /**
     * Gets display title with hierarchy.
     */
    public String getDisplayTitle() {
        if (parentPage == null) {
            return title;
        }
        return parentPage.getTitle() + " > " + title;
    }
    
    /**
     * Checks if page has child pages.
     */
    public boolean hasChildren() {
        return !childPages.isEmpty();
    }
    
    /**
     * Checks if page is root level.
     */
    public boolean isRootLevel() {
        return parentPage == null;
    }
    
    /**
     * Gets page breadcrumb path.
     */
    public List<WikiPage> getBreadcrumbPath() {
        List<WikiPage> breadcrumbs = new ArrayList<>();
        
        WikiPage current = this;
        while (current != null) {
            breadcrumbs.add(0, current);
            current = current.getParentPage();
        }
        
        return breadcrumbs;
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        updateWordCount();
        analyzeContentFeatures();
        updatePagePath();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Integer getTeamNumber() { return teamNumber; }
    public void setTeamNumber(Integer teamNumber) { this.teamNumber = teamNumber; }
    
    public Integer getSeason() { return season; }
    public void setSeason(Integer season) { this.season = season; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getContentMarkdown() { return contentMarkdown; }
    public void setContentMarkdown(String contentMarkdown) { this.contentMarkdown = contentMarkdown; }
    
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    
    public PageType getPageType() { return pageType; }
    public void setPageType(PageType pageType) { this.pageType = pageType; }
    
    public PageStatus getStatus() { return status; }
    public void setStatus(PageStatus status) { this.status = status; }
    
    public VisibilityLevel getVisibility() { return visibility; }
    public void setVisibility(VisibilityLevel visibility) { this.visibility = visibility; }
    
    public WikiPage getParentPage() { return parentPage; }
    public void setParentPage(WikiPage parentPage) { this.parentPage = parentPage; }
    
    public List<WikiPage> getChildPages() { return childPages; }
    public void setChildPages(List<WikiPage> childPages) { this.childPages = childPages; }
    
    public Integer getPageLevel() { return pageLevel; }
    public void setPageLevel(Integer pageLevel) { this.pageLevel = pageLevel; }
    
    public String getPagePath() { return pagePath; }
    public void setPagePath(String pagePath) { this.pagePath = pagePath; }
    
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    
    public Boolean getIsCurrentVersion() { return isCurrentVersion; }
    public void setIsCurrentVersion(Boolean isCurrentVersion) { this.isCurrentVersion = isCurrentVersion; }
    
    public WikiPage getCurrentVersion() { return currentVersion; }
    public void setCurrentVersion(WikiPage currentVersion) { this.currentVersion = currentVersion; }
    
    public List<WikiPage> getVersionHistory() { return versionHistory; }
    public void setVersionHistory(List<WikiPage> versionHistory) { this.versionHistory = versionHistory; }
    
    public String getChangeLog() { return changeLog; }
    public void setChangeLog(String changeLog) { this.changeLog = changeLog; }
    
    public ChangeType getChangeType() { return changeType; }
    public void setChangeType(ChangeType changeType) { this.changeType = changeType; }
    
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    
    public List<String> getCategories() { return categories; }
    public void setCategories(List<String> categories) { this.categories = categories; }
    
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    
    public Integer getWordCount() { return wordCount; }
    public void setWordCount(Integer wordCount) { this.wordCount = wordCount; }
    
    public Integer getReadingTimeMinutes() { return readingTimeMinutes; }
    public void setReadingTimeMinutes(Integer readingTimeMinutes) { this.readingTimeMinutes = readingTimeMinutes; }
    
    public AccessLevel getEditPermission() { return editPermission; }
    public void setEditPermission(AccessLevel editPermission) { this.editPermission = editPermission; }
    
    public AccessLevel getViewPermission() { return viewPermission; }
    public void setViewPermission(AccessLevel viewPermission) { this.viewPermission = viewPermission; }
    
    public List<Long> getAuthorizedEditors() { return authorizedEditors; }
    public void setAuthorizedEditors(List<Long> authorizedEditors) { this.authorizedEditors = authorizedEditors; }
    
    public List<Long> getAuthorizedViewers() { return authorizedViewers; }
    public void setAuthorizedViewers(List<Long> authorizedViewers) { this.authorizedViewers = authorizedViewers; }
    
    public Boolean getAllowComments() { return allowComments; }
    public void setAllowComments(Boolean allowComments) { this.allowComments = allowComments; }
    
    public Boolean getAllowSuggestions() { return allowSuggestions; }
    public void setAllowSuggestions(Boolean allowSuggestions) { this.allowSuggestions = allowSuggestions; }
    
    public Boolean getTrackChanges() { return trackChanges; }
    public void setTrackChanges(Boolean trackChanges) { this.trackChanges = trackChanges; }
    
    public Boolean getRequireApproval() { return requireApproval; }
    public void setRequireApproval(Boolean requireApproval) { this.requireApproval = requireApproval; }
    
    public Boolean getIsLocked() { return isLocked; }
    public void setIsLocked(Boolean isLocked) { this.isLocked = isLocked; }
    
    public LocalDateTime getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(LocalDateTime lockedUntil) { this.lockedUntil = lockedUntil; }
    
    public Long getViewCount() { return viewCount; }
    public void setViewCount(Long viewCount) { this.viewCount = viewCount; }
    
    public Long getEditCount() { return editCount; }
    public void setEditCount(Long editCount) { this.editCount = editCount; }
    
    public Integer getCommentCount() { return commentCount; }
    public void setCommentCount(Integer commentCount) { this.commentCount = commentCount; }
    
    public Integer getAttachmentCount() { return attachmentCount; }
    public void setAttachmentCount(Integer attachmentCount) { this.attachmentCount = attachmentCount; }
    
    public LocalDateTime getLastViewed() { return lastViewed; }
    public void setLastViewed(LocalDateTime lastViewed) { this.lastViewed = lastViewed; }
    
    public LocalDateTime getLastEdited() { return lastEdited; }
    public void setLastEdited(LocalDateTime lastEdited) { this.lastEdited = lastEdited; }
    
    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
    
    public Integer getRatingCount() { return ratingCount; }
    public void setRatingCount(Integer ratingCount) { this.ratingCount = ratingCount; }
    
    public String getMetaDescription() { return metaDescription; }
    public void setMetaDescription(String metaDescription) { this.metaDescription = metaDescription; }
    
    public String getMetaKeywords() { return metaKeywords; }
    public void setMetaKeywords(String metaKeywords) { this.metaKeywords = metaKeywords; }
    
    public Boolean getIsSearchable() { return isSearchable; }
    public void setIsSearchable(Boolean isSearchable) { this.isSearchable = isSearchable; }
    
    public Boolean getIsFeatured() { return isFeatured; }
    public void setIsFeatured(Boolean isFeatured) { this.isFeatured = isFeatured; }
    
    public Integer getSearchRank() { return searchRank; }
    public void setSearchRank(Integer searchRank) { this.searchRank = searchRank; }
    
    public Boolean getHasTableOfContents() { return hasTableOfContents; }
    public void setHasTableOfContents(Boolean hasTableOfContents) { this.hasTableOfContents = hasTableOfContents; }
    
    public Boolean getHasCodeBlocks() { return hasCodeBlocks; }
    public void setHasCodeBlocks(Boolean hasCodeBlocks) { this.hasCodeBlocks = hasCodeBlocks; }
    
    public Boolean getHasImages() { return hasImages; }
    public void setHasImages(Boolean hasImages) { this.hasImages = hasImages; }
    
    public Boolean getHasVideos() { return hasVideos; }
    public void setHasVideos(Boolean hasVideos) { this.hasVideos = hasVideos; }
    
    public Boolean getHasAttachments() { return hasAttachments; }
    public void setHasAttachments(Boolean hasAttachments) { this.hasAttachments = hasAttachments; }
    
    public Boolean getHasLinks() { return hasLinks; }
    public void setHasLinks(Boolean hasLinks) { this.hasLinks = hasLinks; }
    
    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }
    
    public String getLayoutStyle() { return layoutStyle; }
    public void setLayoutStyle(String layoutStyle) { this.layoutStyle = layoutStyle; }
    
    public String getCustomCss() { return customCss; }
    public void setCustomCss(String customCss) { this.customCss = customCss; }
    
    public String getCustomJavaScript() { return customJavaScript; }
    public void setCustomJavaScript(String customJavaScript) { this.customJavaScript = customJavaScript; }
    
    public List<ExternalLink> getExternalLinks() { return externalLinks; }
    public void setExternalLinks(List<ExternalLink> externalLinks) { this.externalLinks = externalLinks; }
    
    public List<PageAttachment> getAttachments() { return attachments; }
    public void setAttachments(List<PageAttachment> attachments) { this.attachments = attachments; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public Boolean getIsArchived() { return isArchived; }
    public void setIsArchived(Boolean isArchived) { this.isArchived = isArchived; }
    
    public LocalDateTime getArchivedAt() { return archivedAt; }
    public void setArchivedAt(LocalDateTime archivedAt) { this.archivedAt = archivedAt; }
    
    public String getArchiveReason() { return archiveReason; }
    public void setArchiveReason(String archiveReason) { this.archiveReason = archiveReason; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public TeamMember getCreatedBy() { return createdBy; }
    public void setCreatedBy(TeamMember createdBy) { this.createdBy = createdBy; }
    
    public TeamMember getLastUpdatedBy() { return lastUpdatedBy; }
    public void setLastUpdatedBy(TeamMember lastUpdatedBy) { this.lastUpdatedBy = lastUpdatedBy; }
    
    public TeamMember getApprovedBy() { return approvedBy; }
    public void setApprovedBy(TeamMember approvedBy) { this.approvedBy = approvedBy; }
    
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    
    @Override
    public String toString() {
        return String.format("WikiPage{id=%d, title='%s', type=%s, status=%s, version=%d}", 
                           id, title, pageType, status, version);
    }
}