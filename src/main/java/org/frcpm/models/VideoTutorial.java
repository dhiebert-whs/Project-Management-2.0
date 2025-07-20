// src/main/java/org/frcpm/models/VideoTutorial.java

package org.frcpm.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Video Tutorial model for FRC team video learning library.
 * 
 * Provides comprehensive video tutorial management for FRC teams including
 * categorized learning content, skill progression tracking, interactive
 * features, and integration with team knowledge management systems.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-KnowledgeManagement
 * @since Phase 4D.2 Video Tutorial Library Integration
 */
@Entity
@Table(name = "video_tutorials")
public class VideoTutorial {
    
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
    private String description;
    
    @Column(length = 1000)
    private String summary;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TutorialCategory category;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SkillLevel skillLevel;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TutorialStatus status;
    
    // Video Content Information
    @Column(nullable = false, length = 500)
    private String videoUrl;
    
    @Column(length = 500)
    private String thumbnailUrl;
    
    @Column(length = 500)
    private String previewUrl; // Short preview clip
    
    @Enumerated(EnumType.STRING)
    private VideoSource videoSource;
    
    @Column(length = 200)
    private String externalVideoId; // YouTube ID, Vimeo ID, etc.
    
    @Column
    private Duration duration; // Video length
    
    @Enumerated(EnumType.STRING)
    private VideoQuality videoQuality;
    
    @Column
    private Boolean hasSubtitles = false;
    
    @Column
    private Boolean hasTranscript = false;
    
    @Column(columnDefinition = "LONGTEXT")
    private String transcript;
    
    // Learning and Skill Information
    @ElementCollection
    @CollectionTable(name = "video_tutorial_learning_objectives")
    private List<String> learningObjectives = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "video_tutorial_prerequisites")
    private List<String> prerequisites = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "video_tutorial_skills_taught")
    private List<String> skillsTaught = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "video_tutorial_tools_required")
    private List<String> toolsRequired = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "video_tutorial_safety_notes")
    private List<String> safetyNotes = new ArrayList<>();
    
    @Column
    private Integer estimatedCompletionTime; // Minutes to complete practice
    
    @Column
    private Integer difficultyRating; // 1-10 scale
    
    // Content Organization
    @ElementCollection
    @CollectionTable(name = "video_tutorial_tags")
    private List<String> tags = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "video_tutorial_topics")
    private List<String> topics = new ArrayList<>();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "series_id")
    private VideoTutorialSeries series;
    
    @Column
    private Integer seriesOrder; // Order within series
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_tutorial_id")
    private VideoTutorial previousTutorial;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "next_tutorial_id")
    private VideoTutorial nextTutorial;
    
    // Interactive Features
    @ElementCollection
    @CollectionTable(name = "video_tutorial_chapters")
    private List<String> chapters = new ArrayList<>(); // JSON or structured data
    
    @ElementCollection
    @CollectionTable(name = "video_tutorial_timestamps")
    private List<String> keyTimestamps = new ArrayList<>(); // Important moments
    
    @ElementCollection
    @CollectionTable(name = "video_tutorial_quiz_questions")
    private List<String> quizQuestions = new ArrayList<>(); // JSON quiz data
    
    @Column
    private Boolean hasInteractiveElements = false;
    
    @Column
    private Boolean hasQuiz = false;
    
    @Column
    private Boolean hasDownloadableResources = false;
    
    @ElementCollection
    @CollectionTable(name = "video_tutorial_resources")
    private List<String> resourceUrls = new ArrayList<>();
    
    // Access Control and Visibility
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VisibilityLevel visibility;
    
    @Enumerated(EnumType.STRING)
    private AccessLevel viewPermission;
    
    @ElementCollection
    @CollectionTable(name = "video_tutorial_authorized_viewers")
    private List<Long> authorizedViewers = new ArrayList<>();
    
    @Column
    private Boolean requiresApproval = false;
    
    @Column
    private Boolean isRestricted = false; // Age restrictions, etc.
    
    @Column(length = 500)
    private String accessRestrictions;
    
    // Analytics and Engagement
    @Column(nullable = false)
    private Long viewCount = 0L;
    
    @Column(nullable = false)
    private Long completionCount = 0L;
    
    @Column
    private Double averageRating = 0.0;
    
    @Column(nullable = false)
    private Integer ratingCount = 0;
    
    @Column
    private Double engagementScore = 0.0; // Calculated metric
    
    @Column
    private Double completionRate = 0.0; // Percentage who complete
    
    @Column
    private LocalDateTime lastViewed;
    
    @Column
    private Long lastViewedBy;
    
    // Content Quality and Moderation
    @Column
    private Boolean isFlagged = false;
    
    @Column(length = 1000)
    private String flagReason;
    
    @Column
    private Boolean isModerated = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderated_by")
    private TeamMember moderatedBy;
    
    @Column
    private LocalDateTime moderatedAt;
    
    @Column
    private Boolean isQualityChecked = false;
    
    @Column
    private Integer qualityScore; // 1-100
    
    // Production Information
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private TeamMember createdBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id")
    private TeamMember instructor; // Primary instructor
    
    @ElementCollection
    @CollectionTable(name = "video_tutorial_contributors")
    private List<Long> contributors = new ArrayList<>(); // Additional contributors
    
    @Column
    private LocalDateTime recordedAt;
    
    @Column(length = 200)
    private String recordingLocation;
    
    @Column(length = 200)
    private String equipment; // Camera/recording equipment used
    
    @Column
    private Boolean isOriginalContent = true;
    
    @Column(length = 500)
    private String sourceAttribution; // If external content
    
    @Column(length = 500)
    private String license; // Creative Commons, etc.
    
    // Update and Versioning
    @Column
    private Integer version = 1;
    
    @Column
    private Boolean isCurrentVersion = true;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "superseded_by")
    private VideoTutorial supersededBy;
    
    @Column
    private LocalDateTime lastUpdated;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private TeamMember updatedBy;
    
    @Column(length = 1000)
    private String updateNotes;
    
    @Column
    private Boolean needsUpdate = false;
    
    @Column(length = 500)
    private String updateReason;
    
    // Integration and Relationships
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "video_tutorial_related_tasks",
        joinColumns = @JoinColumn(name = "tutorial_id"),
        inverseJoinColumns = @JoinColumn(name = "task_id")
    )
    private List<Task> relatedTasks = new ArrayList<>();
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "video_tutorial_related_projects",
        joinColumns = @JoinColumn(name = "tutorial_id"),
        inverseJoinColumns = @JoinColumn(name = "project_id")
    )
    private List<Project> relatedProjects = new ArrayList<>();
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "video_tutorial_wiki_pages",
        joinColumns = @JoinColumn(name = "tutorial_id"),
        inverseJoinColumns = @JoinColumn(name = "wiki_page_id")
    )
    private List<WikiPage> relatedWikiPages = new ArrayList<>();
    
    // Comments and Feedback
    @Column
    private Boolean allowComments = true;
    
    @Column
    private Boolean moderateComments = false;
    
    @Column(nullable = false)
    private Integer commentCount = 0;
    
    // Search and Discoverability
    @Column
    private Boolean isSearchable = true;
    
    @Column
    private Boolean isFeatured = false;
    
    @Column
    private LocalDateTime featuredAt;
    
    @Column
    private Integer searchRank = 0;
    
    @ElementCollection
    @CollectionTable(name = "video_tutorial_search_keywords")
    private List<String> searchKeywords = new ArrayList<>();
    
    // System Fields
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @Column
    private Boolean isArchived = false;
    
    @Column
    private LocalDateTime archivedAt;
    
    @Column(length = 500)
    private String archiveReason;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // =========================================================================
    // ENUMS
    // =========================================================================
    
    public enum TutorialCategory {
        MECHANICAL_DESIGN("Mechanical Design"),
        ELECTRICAL_SYSTEMS("Electrical Systems"),
        PROGRAMMING("Programming"),
        CONTROLS("Controls"),
        PNEUMATICS("Pneumatics"),
        FABRICATION("Fabrication"),
        CAD_DESIGN("CAD Design"),
        MACHINING("Machining"),
        ASSEMBLY("Assembly"),
        TROUBLESHOOTING("Troubleshooting"),
        SAFETY("Safety"),
        TOOLS_EQUIPMENT("Tools & Equipment"),
        PROJECT_MANAGEMENT("Project Management"),
        TEAM_ORGANIZATION("Team Organization"),
        COMPETITION_STRATEGY("Competition Strategy"),
        SCOUTING("Scouting"),
        DRIVE_TEAM("Drive Team"),
        BUSINESS_SKILLS("Business Skills"),
        PRESENTATION("Presentation"),
        GENERAL_SKILLS("General Skills");
        
        private final String displayName;
        
        TutorialCategory(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum SkillLevel {
        BEGINNER("Beginner"),
        INTERMEDIATE("Intermediate"),
        ADVANCED("Advanced"),
        EXPERT("Expert"),
        ALL_LEVELS("All Levels");
        
        private final String displayName;
        
        SkillLevel(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum TutorialStatus {
        DRAFT("Draft"),
        UNDER_REVIEW("Under Review"),
        PUBLISHED("Published"),
        ARCHIVED("Archived"),
        PENDING_UPDATE("Pending Update"),
        RESTRICTED("Restricted");
        
        private final String displayName;
        
        TutorialStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum VideoSource {
        INTERNAL("Internal Hosting"),
        YOUTUBE("YouTube"),
        VIMEO("Vimeo"),
        EXTERNAL_LINK("External Link"),
        FILE_UPLOAD("File Upload"),
        STREAM("Live Stream");
        
        private final String displayName;
        
        VideoSource(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum VideoQuality {
        SD_480P("480p (SD)"),
        HD_720P("720p (HD)"),
        FHD_1080P("1080p (Full HD)"),
        UHD_4K("4K (Ultra HD)"),
        VARIABLE("Variable Quality");
        
        private final String displayName;
        
        VideoQuality(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum VisibilityLevel {
        PUBLIC("Public"),
        TEAM_ONLY("Team Only"),
        MENTORS_ONLY("Mentors Only"),
        PRIVATE("Private"),
        RESTRICTED("Restricted");
        
        private final String displayName;
        
        VisibilityLevel(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum AccessLevel {
        ANYONE("Anyone"),
        TEAM_MEMBERS("Team Members"),
        STUDENTS("Students"),
        MENTORS("Mentors"),
        ADMINISTRATORS("Administrators"),
        SPECIFIC_USERS("Specific Users");
        
        private final String displayName;
        
        AccessLevel(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================
    
    public VideoTutorial() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public VideoTutorial(Integer teamNumber, Integer season, String title, String videoUrl) {
        this();
        this.teamNumber = teamNumber;
        this.season = season;
        this.title = title;
        this.videoUrl = videoUrl;
        this.status = TutorialStatus.DRAFT;
        this.visibility = VisibilityLevel.TEAM_ONLY;
    }
    
    // =========================================================================
    // BUSINESS METHODS
    // =========================================================================
    
    /**
     * Records a view of this tutorial.
     */
    public void recordView(Long userId) {
        this.viewCount++;
        this.lastViewed = LocalDateTime.now();
        this.lastViewedBy = userId;
        updateEngagementMetrics();
    }
    
    /**
     * Records a completion of this tutorial.
     */
    public void recordCompletion(Long userId) {
        this.completionCount++;
        updateCompletionRate();
        updateEngagementMetrics();
    }
    
    /**
     * Adds a rating to this tutorial.
     */
    public void addRating(Integer rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        
        double totalRating = this.averageRating * this.ratingCount;
        this.ratingCount++;
        this.averageRating = (totalRating + rating) / this.ratingCount;
        updateEngagementMetrics();
    }
    
    /**
     * Updates completion rate based on views and completions.
     */
    public void updateCompletionRate() {
        if (this.viewCount > 0) {
            this.completionRate = (double) this.completionCount / this.viewCount * 100.0;
        }
    }
    
    /**
     * Calculates and updates engagement score.
     */
    public void updateEngagementMetrics() {
        double score = 0.0;
        
        // Base score from view count (max 30 points)
        score += Math.min(30.0, this.viewCount / 10.0);
        
        // Completion rate contribution (max 25 points)
        score += (this.completionRate / 100.0) * 25.0;
        
        // Rating contribution (max 25 points)
        if (this.ratingCount > 0) {
            score += (this.averageRating / 5.0) * 25.0;
        }
        
        // Recent activity boost (max 10 points)
        if (this.lastViewed != null && this.lastViewed.isAfter(LocalDateTime.now().minusDays(7))) {
            score += 10.0;
        }
        
        // Quality bonus (max 10 points)
        if (this.qualityScore != null) {
            score += (this.qualityScore / 100.0) * 10.0;
        }
        
        this.engagementScore = Math.min(100.0, score);
    }
    
    /**
     * Checks if tutorial needs updating based on age and feedback.
     */
    public boolean needsContentUpdate() {
        if (this.needsUpdate) {
            return true;
        }
        
        // Auto-flag for update if very old
        if (this.lastUpdated.isBefore(LocalDateTime.now().minusYears(2))) {
            return true;
        }
        
        // Flag if low engagement despite age
        if (this.lastUpdated.isBefore(LocalDateTime.now().minusMonths(6)) && this.engagementScore < 30.0) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Gets tutorial difficulty as readable string.
     */
    public String getDifficultyDescription() {
        if (this.difficultyRating == null) {
            return "Not Rated";
        }
        
        if (this.difficultyRating <= 3) {
            return "Easy";
        } else if (this.difficultyRating <= 6) {
            return "Moderate";
        } else if (this.difficultyRating <= 8) {
            return "Challenging";
        } else {
            return "Expert Level";
        }
    }
    
    /**
     * Gets formatted duration string.
     */
    public String getFormattedDuration() {
        if (this.duration == null) {
            return "Unknown";
        }
        
        long hours = this.duration.toHours();
        long minutes = this.duration.toMinutesPart();
        long seconds = this.duration.toSecondsPart();
        
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%d:%02d", minutes, seconds);
        }
    }
    
    /**
     * Checks if user is authorized to view this tutorial.
     */
    public boolean isViewableBy(Long userId, String userRole) {
        if (!this.isActive || this.isArchived) {
            return false;
        }
        
        if (this.visibility == VisibilityLevel.PUBLIC) {
            return true;
        }
        
        if (this.authorizedViewers.contains(userId)) {
            return true;
        }
        
        if (this.viewPermission != null) {
            switch (this.viewPermission) {
                case ANYONE:
                    return true;
                case TEAM_MEMBERS:
                    return userRole != null;
                case MENTORS:
                    return "MENTOR".equals(userRole) || "ADMIN".equals(userRole);
                case ADMINISTRATORS:
                    return "ADMIN".equals(userRole);
                default:
                    return false;
            }
        }
        
        return false;
    }
    
    /**
     * Gets all related content (tasks, projects, wiki pages).
     */
    public int getRelatedContentCount() {
        return this.relatedTasks.size() + this.relatedProjects.size() + this.relatedWikiPages.size();
    }
    
    /**
     * Checks if tutorial has interactive elements.
     */
    public boolean hasInteractiveContent() {
        return this.hasInteractiveElements || this.hasQuiz || !this.quizQuestions.isEmpty() || !this.chapters.isEmpty();
    }
    
    /**
     * Archives this tutorial with reason.
     */
    public void archive(String reason) {
        this.isArchived = true;
        this.isActive = false;
        this.archivedAt = LocalDateTime.now();
        this.archiveReason = reason;
        this.status = TutorialStatus.ARCHIVED;
    }
    
    /**
     * Restores archived tutorial.
     */
    public void restore() {
        this.isArchived = false;
        this.isActive = true;
        this.archivedAt = null;
        this.archiveReason = null;
        this.status = TutorialStatus.PUBLISHED;
    }
    
    /**
     * Updates tutorial timestamp.
     */
    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // =========================================================================
    // GETTERS AND SETTERS
    // =========================================================================
    
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
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    
    public TutorialCategory getCategory() { return category; }
    public void setCategory(TutorialCategory category) { this.category = category; }
    
    public SkillLevel getSkillLevel() { return skillLevel; }
    public void setSkillLevel(SkillLevel skillLevel) { this.skillLevel = skillLevel; }
    
    public TutorialStatus getStatus() { return status; }
    public void setStatus(TutorialStatus status) { this.status = status; }
    
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    
    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    
    public String getPreviewUrl() { return previewUrl; }
    public void setPreviewUrl(String previewUrl) { this.previewUrl = previewUrl; }
    
    public VideoSource getVideoSource() { return videoSource; }
    public void setVideoSource(VideoSource videoSource) { this.videoSource = videoSource; }
    
    public String getExternalVideoId() { return externalVideoId; }
    public void setExternalVideoId(String externalVideoId) { this.externalVideoId = externalVideoId; }
    
    public Duration getDuration() { return duration; }
    public void setDuration(Duration duration) { this.duration = duration; }
    
    public VideoQuality getVideoQuality() { return videoQuality; }
    public void setVideoQuality(VideoQuality videoQuality) { this.videoQuality = videoQuality; }
    
    public Boolean getHasSubtitles() { return hasSubtitles; }
    public void setHasSubtitles(Boolean hasSubtitles) { this.hasSubtitles = hasSubtitles; }
    
    public Boolean getHasTranscript() { return hasTranscript; }
    public void setHasTranscript(Boolean hasTranscript) { this.hasTranscript = hasTranscript; }
    
    public String getTranscript() { return transcript; }
    public void setTranscript(String transcript) { this.transcript = transcript; }
    
    public List<String> getLearningObjectives() { return learningObjectives; }
    public void setLearningObjectives(List<String> learningObjectives) { this.learningObjectives = learningObjectives; }
    
    public List<String> getPrerequisites() { return prerequisites; }
    public void setPrerequisites(List<String> prerequisites) { this.prerequisites = prerequisites; }
    
    public List<String> getSkillsTaught() { return skillsTaught; }
    public void setSkillsTaught(List<String> skillsTaught) { this.skillsTaught = skillsTaught; }
    
    public List<String> getToolsRequired() { return toolsRequired; }
    public void setToolsRequired(List<String> toolsRequired) { this.toolsRequired = toolsRequired; }
    
    public List<String> getSafetyNotes() { return safetyNotes; }
    public void setSafetyNotes(List<String> safetyNotes) { this.safetyNotes = safetyNotes; }
    
    public Integer getEstimatedCompletionTime() { return estimatedCompletionTime; }
    public void setEstimatedCompletionTime(Integer estimatedCompletionTime) { this.estimatedCompletionTime = estimatedCompletionTime; }
    
    public Integer getDifficultyRating() { return difficultyRating; }
    public void setDifficultyRating(Integer difficultyRating) { this.difficultyRating = difficultyRating; }
    
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    
    public List<String> getTopics() { return topics; }
    public void setTopics(List<String> topics) { this.topics = topics; }
    
    public VideoTutorialSeries getSeries() { return series; }
    public void setSeries(VideoTutorialSeries series) { this.series = series; }
    
    public Integer getSeriesOrder() { return seriesOrder; }
    public void setSeriesOrder(Integer seriesOrder) { this.seriesOrder = seriesOrder; }
    
    public VideoTutorial getPreviousTutorial() { return previousTutorial; }
    public void setPreviousTutorial(VideoTutorial previousTutorial) { this.previousTutorial = previousTutorial; }
    
    public VideoTutorial getNextTutorial() { return nextTutorial; }
    public void setNextTutorial(VideoTutorial nextTutorial) { this.nextTutorial = nextTutorial; }
    
    public List<String> getChapters() { return chapters; }
    public void setChapters(List<String> chapters) { this.chapters = chapters; }
    
    public List<String> getKeyTimestamps() { return keyTimestamps; }
    public void setKeyTimestamps(List<String> keyTimestamps) { this.keyTimestamps = keyTimestamps; }
    
    public List<String> getQuizQuestions() { return quizQuestions; }
    public void setQuizQuestions(List<String> quizQuestions) { this.quizQuestions = quizQuestions; }
    
    public Boolean getHasInteractiveElements() { return hasInteractiveElements; }
    public void setHasInteractiveElements(Boolean hasInteractiveElements) { this.hasInteractiveElements = hasInteractiveElements; }
    
    public Boolean getHasQuiz() { return hasQuiz; }
    public void setHasQuiz(Boolean hasQuiz) { this.hasQuiz = hasQuiz; }
    
    public Boolean getHasDownloadableResources() { return hasDownloadableResources; }
    public void setHasDownloadableResources(Boolean hasDownloadableResources) { this.hasDownloadableResources = hasDownloadableResources; }
    
    public List<String> getResourceUrls() { return resourceUrls; }
    public void setResourceUrls(List<String> resourceUrls) { this.resourceUrls = resourceUrls; }
    
    public VisibilityLevel getVisibility() { return visibility; }
    public void setVisibility(VisibilityLevel visibility) { this.visibility = visibility; }
    
    public AccessLevel getViewPermission() { return viewPermission; }
    public void setViewPermission(AccessLevel viewPermission) { this.viewPermission = viewPermission; }
    
    public List<Long> getAuthorizedViewers() { return authorizedViewers; }
    public void setAuthorizedViewers(List<Long> authorizedViewers) { this.authorizedViewers = authorizedViewers; }
    
    public Boolean getRequiresApproval() { return requiresApproval; }
    public void setRequiresApproval(Boolean requiresApproval) { this.requiresApproval = requiresApproval; }
    
    public Boolean getIsRestricted() { return isRestricted; }
    public void setIsRestricted(Boolean isRestricted) { this.isRestricted = isRestricted; }
    
    public String getAccessRestrictions() { return accessRestrictions; }
    public void setAccessRestrictions(String accessRestrictions) { this.accessRestrictions = accessRestrictions; }
    
    public Long getViewCount() { return viewCount; }
    public void setViewCount(Long viewCount) { this.viewCount = viewCount; }
    
    public Long getCompletionCount() { return completionCount; }
    public void setCompletionCount(Long completionCount) { this.completionCount = completionCount; }
    
    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
    
    public Integer getRatingCount() { return ratingCount; }
    public void setRatingCount(Integer ratingCount) { this.ratingCount = ratingCount; }
    
    public Double getEngagementScore() { return engagementScore; }
    public void setEngagementScore(Double engagementScore) { this.engagementScore = engagementScore; }
    
    public Double getCompletionRate() { return completionRate; }
    public void setCompletionRate(Double completionRate) { this.completionRate = completionRate; }
    
    public LocalDateTime getLastViewed() { return lastViewed; }
    public void setLastViewed(LocalDateTime lastViewed) { this.lastViewed = lastViewed; }
    
    public Long getLastViewedBy() { return lastViewedBy; }
    public void setLastViewedBy(Long lastViewedBy) { this.lastViewedBy = lastViewedBy; }
    
    public Boolean getIsFlagged() { return isFlagged; }
    public void setIsFlagged(Boolean isFlagged) { this.isFlagged = isFlagged; }
    
    public String getFlagReason() { return flagReason; }
    public void setFlagReason(String flagReason) { this.flagReason = flagReason; }
    
    public Boolean getIsModerated() { return isModerated; }
    public void setIsModerated(Boolean isModerated) { this.isModerated = isModerated; }
    
    public TeamMember getModeratedBy() { return moderatedBy; }
    public void setModeratedBy(TeamMember moderatedBy) { this.moderatedBy = moderatedBy; }
    
    public LocalDateTime getModeratedAt() { return moderatedAt; }
    public void setModeratedAt(LocalDateTime moderatedAt) { this.moderatedAt = moderatedAt; }
    
    public Boolean getIsQualityChecked() { return isQualityChecked; }
    public void setIsQualityChecked(Boolean isQualityChecked) { this.isQualityChecked = isQualityChecked; }
    
    public Integer getQualityScore() { return qualityScore; }
    public void setQualityScore(Integer qualityScore) { this.qualityScore = qualityScore; }
    
    public TeamMember getCreatedBy() { return createdBy; }
    public void setCreatedBy(TeamMember createdBy) { this.createdBy = createdBy; }
    
    public TeamMember getInstructor() { return instructor; }
    public void setInstructor(TeamMember instructor) { this.instructor = instructor; }
    
    public List<Long> getContributors() { return contributors; }
    public void setContributors(List<Long> contributors) { this.contributors = contributors; }
    
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
    
    public String getRecordingLocation() { return recordingLocation; }
    public void setRecordingLocation(String recordingLocation) { this.recordingLocation = recordingLocation; }
    
    public String getEquipment() { return equipment; }
    public void setEquipment(String equipment) { this.equipment = equipment; }
    
    public Boolean getIsOriginalContent() { return isOriginalContent; }
    public void setIsOriginalContent(Boolean isOriginalContent) { this.isOriginalContent = isOriginalContent; }
    
    public String getSourceAttribution() { return sourceAttribution; }
    public void setSourceAttribution(String sourceAttribution) { this.sourceAttribution = sourceAttribution; }
    
    public String getLicense() { return license; }
    public void setLicense(String license) { this.license = license; }
    
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    
    public Boolean getIsCurrentVersion() { return isCurrentVersion; }
    public void setIsCurrentVersion(Boolean isCurrentVersion) { this.isCurrentVersion = isCurrentVersion; }
    
    public VideoTutorial getSupersededBy() { return supersededBy; }
    public void setSupersededBy(VideoTutorial supersededBy) { this.supersededBy = supersededBy; }
    
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    
    public TeamMember getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(TeamMember updatedBy) { this.updatedBy = updatedBy; }
    
    public String getUpdateNotes() { return updateNotes; }
    public void setUpdateNotes(String updateNotes) { this.updateNotes = updateNotes; }
    
    public Boolean getNeedsUpdate() { return needsUpdate; }
    public void setNeedsUpdate(Boolean needsUpdate) { this.needsUpdate = needsUpdate; }
    
    public String getUpdateReason() { return updateReason; }
    public void setUpdateReason(String updateReason) { this.updateReason = updateReason; }
    
    public List<Task> getRelatedTasks() { return relatedTasks; }
    public void setRelatedTasks(List<Task> relatedTasks) { this.relatedTasks = relatedTasks; }
    
    public List<Project> getRelatedProjects() { return relatedProjects; }
    public void setRelatedProjects(List<Project> relatedProjects) { this.relatedProjects = relatedProjects; }
    
    public List<WikiPage> getRelatedWikiPages() { return relatedWikiPages; }
    public void setRelatedWikiPages(List<WikiPage> relatedWikiPages) { this.relatedWikiPages = relatedWikiPages; }
    
    public Boolean getAllowComments() { return allowComments; }
    public void setAllowComments(Boolean allowComments) { this.allowComments = allowComments; }
    
    public Boolean getModerateComments() { return moderateComments; }
    public void setModerateComments(Boolean moderateComments) { this.moderateComments = moderateComments; }
    
    public Integer getCommentCount() { return commentCount; }
    public void setCommentCount(Integer commentCount) { this.commentCount = commentCount; }
    
    public Boolean getIsSearchable() { return isSearchable; }
    public void setIsSearchable(Boolean isSearchable) { this.isSearchable = isSearchable; }
    
    public Boolean getIsFeatured() { return isFeatured; }
    public void setIsFeatured(Boolean isFeatured) { this.isFeatured = isFeatured; }
    
    public LocalDateTime getFeaturedAt() { return featuredAt; }
    public void setFeaturedAt(LocalDateTime featuredAt) { this.featuredAt = featuredAt; }
    
    public Integer getSearchRank() { return searchRank; }
    public void setSearchRank(Integer searchRank) { this.searchRank = searchRank; }
    
    public List<String> getSearchKeywords() { return searchKeywords; }
    public void setSearchKeywords(List<String> searchKeywords) { this.searchKeywords = searchKeywords; }
    
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
}