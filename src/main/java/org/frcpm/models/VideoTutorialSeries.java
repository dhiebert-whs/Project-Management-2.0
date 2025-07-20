// src/main/java/org/frcpm/models/VideoTutorialSeries.java

package org.frcpm.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Video Tutorial Series model for organizing related tutorials.
 * 
 * Provides structured learning paths and course organization for FRC teams
 * with progress tracking, completion requirements, and educational sequences.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-KnowledgeManagement
 * @since Phase 4D.2 Video Tutorial Library Integration
 */
@Entity
@Table(name = "video_tutorial_series")
public class VideoTutorialSeries {
    
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
    private String slug;
    
    @Column(columnDefinition = "LONGTEXT")
    private String description;
    
    @Column(length = 1000)
    private String summary;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeriesType seriesType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeriesCategory category;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SkillLevel targetSkillLevel;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeriesStatus status;
    
    // Content Organization
    @OneToMany(mappedBy = "series", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("seriesOrder ASC")
    private List<VideoTutorial> tutorials = new ArrayList<>();
    
    @Column(nullable = false)
    private Integer tutorialCount = 0;
    
    @Column
    private Duration totalDuration;
    
    @Column
    private Integer estimatedCompletionHours;
    
    // Learning Structure
    @ElementCollection
    @CollectionTable(name = "series_learning_objectives")
    private List<String> learningObjectives = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "series_prerequisites")
    private List<String> prerequisites = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "series_skills_covered")
    private List<String> skillsCovered = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "series_outcomes")
    private List<String> expectedOutcomes = new ArrayList<>();
    
    @Column
    private Boolean requiresSequentialCompletion = false;
    
    @Column
    private Integer minimumCompletionPercentage = 80;
    
    // Content Metadata
    @ElementCollection
    @CollectionTable(name = "series_tags")
    private List<String> tags = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "series_topics")
    private List<String> topics = new ArrayList<>();
    
    @Column(length = 500)
    private String thumbnailUrl;
    
    @Column(length = 500)
    private String bannerUrl;
    
    // Access and Visibility
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VisibilityLevel visibility;
    
    @Enumerated(EnumType.STRING)
    private AccessLevel viewPermission;
    
    @ElementCollection
    @CollectionTable(name = "series_authorized_viewers")
    private List<Long> authorizedViewers = new ArrayList<>();
    
    @Column
    private Boolean isFeatured = false;
    
    @Column
    private LocalDateTime featuredAt;
    
    // Analytics and Engagement
    @Column(nullable = false)
    private Long viewCount = 0L;
    
    @Column(nullable = false)
    private Long enrollmentCount = 0L;
    
    @Column(nullable = false)
    private Long completionCount = 0L;
    
    @Column
    private Double averageRating = 0.0;
    
    @Column(nullable = false)
    private Integer ratingCount = 0;
    
    @Column
    private Double completionRate = 0.0;
    
    @Column
    private Double engagementScore = 0.0;
    
    @Column
    private LocalDateTime lastViewed;
    
    @Column
    private Long lastViewedBy;
    
    // Production Information
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private TeamMember createdBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id")
    private TeamMember primaryInstructor;
    
    @ElementCollection
    @CollectionTable(name = "series_instructors")
    private List<Long> instructors = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "series_contributors")
    private List<Long> contributors = new ArrayList<>();
    
    @Column
    private Boolean isOriginalContent = true;
    
    @Column(length = 500)
    private String sourceAttribution;
    
    @Column(length = 500)
    private String license;
    
    // Quality and Moderation
    @Column
    private Boolean isQualityChecked = false;
    
    @Column
    private Integer qualityScore;
    
    @Column
    private Boolean isModerated = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderated_by")
    private TeamMember moderatedBy;
    
    @Column
    private LocalDateTime moderatedAt;
    
    // Versioning
    @Column
    private Integer version = 1;
    
    @Column
    private Boolean isCurrentVersion = true;
    
    @Column
    private LocalDateTime lastUpdated;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private TeamMember updatedBy;
    
    @Column(length = 1000)
    private String updateNotes;
    
    // Search and Discovery
    @Column
    private Boolean isSearchable = true;
    
    @Column
    private Integer searchRank = 0;
    
    @ElementCollection
    @CollectionTable(name = "series_search_keywords")
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
    
    public enum SeriesType {
        COURSE("Course"),
        WORKSHOP("Workshop"),
        TRAINING_PROGRAM("Training Program"),
        SKILL_TRACK("Skill Track"),
        PROJECT_GUIDE("Project Guide"),
        QUICK_START("Quick Start"),
        MASTERCLASS("Masterclass"),
        CERTIFICATION("Certification");
        
        private final String displayName;
        
        SeriesType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum SeriesCategory {
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
        BUSINESS_SKILLS("Business Skills"),
        GENERAL_SKILLS("General Skills");
        
        private final String displayName;
        
        SeriesCategory(String displayName) {
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
        MIXED_LEVELS("Mixed Levels");
        
        private final String displayName;
        
        SkillLevel(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum SeriesStatus {
        DRAFT("Draft"),
        IN_PROGRESS("In Progress"),
        PUBLISHED("Published"),
        COMPLETED("Completed"),
        ARCHIVED("Archived"),
        UNDER_REVIEW("Under Review");
        
        private final String displayName;
        
        SeriesStatus(String displayName) {
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
    
    public VideoTutorialSeries() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public VideoTutorialSeries(Integer teamNumber, Integer season, String title) {
        this();
        this.teamNumber = teamNumber;
        this.season = season;
        this.title = title;
        this.status = SeriesStatus.DRAFT;
        this.visibility = VisibilityLevel.TEAM_ONLY;
    }
    
    // =========================================================================
    // BUSINESS METHODS
    // =========================================================================
    
    /**
     * Adds a tutorial to this series.
     */
    public void addTutorial(VideoTutorial tutorial) {
        if (!this.tutorials.contains(tutorial)) {
            this.tutorials.add(tutorial);
            tutorial.setSeries(this);
            tutorial.setSeriesOrder(this.tutorials.size());
            updateSeriesMetrics();
        }
    }
    
    /**
     * Removes a tutorial from this series.
     */
    public void removeTutorial(VideoTutorial tutorial) {
        if (this.tutorials.remove(tutorial)) {
            tutorial.setSeries(null);
            tutorial.setSeriesOrder(null);
            reorderTutorials();
            updateSeriesMetrics();
        }
    }
    
    /**
     * Reorders tutorials in the series.
     */
    public void reorderTutorials() {
        for (int i = 0; i < this.tutorials.size(); i++) {
            this.tutorials.get(i).setSeriesOrder(i + 1);
        }
    }
    
    /**
     * Updates series metrics based on tutorials.
     */
    public void updateSeriesMetrics() {
        this.tutorialCount = this.tutorials.size();
        
        // Calculate total duration
        Duration total = Duration.ZERO;
        for (VideoTutorial tutorial : this.tutorials) {
            if (tutorial.getDuration() != null) {
                total = total.plus(tutorial.getDuration());
            }
        }
        this.totalDuration = total;
        
        // Estimate completion hours (includes practice time)
        this.estimatedCompletionHours = (int) Math.ceil(total.toHours() * 1.5);
        
        // Update engagement metrics
        updateEngagementScore();
        updateCompletionRate();
    }
    
    /**
     * Records a view of this series.
     */
    public void recordView(Long userId) {
        this.viewCount++;
        this.lastViewed = LocalDateTime.now();
        this.lastViewedBy = userId;
        updateEngagementScore();
    }
    
    /**
     * Records an enrollment in this series.
     */
    public void recordEnrollment(Long userId) {
        this.enrollmentCount++;
        updateEngagementScore();
    }
    
    /**
     * Records a completion of this series.
     */
    public void recordCompletion(Long userId) {
        this.completionCount++;
        updateCompletionRate();
        updateEngagementScore();
    }
    
    /**
     * Adds a rating to this series.
     */
    public void addRating(Integer rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        
        double totalRating = this.averageRating * this.ratingCount;
        this.ratingCount++;
        this.averageRating = (totalRating + rating) / this.ratingCount;
        updateEngagementScore();
    }
    
    /**
     * Updates completion rate based on enrollments and completions.
     */
    public void updateCompletionRate() {
        if (this.enrollmentCount > 0) {
            this.completionRate = (double) this.completionCount / this.enrollmentCount * 100.0;
        }
    }
    
    /**
     * Calculates and updates engagement score.
     */
    public void updateEngagementScore() {
        double score = 0.0;
        
        // Base score from view count (max 25 points)
        score += Math.min(25.0, this.viewCount / 20.0);
        
        // Enrollment contribution (max 20 points)
        score += Math.min(20.0, this.enrollmentCount / 10.0);
        
        // Completion rate contribution (max 25 points)
        score += (this.completionRate / 100.0) * 25.0;
        
        // Rating contribution (max 20 points)
        if (this.ratingCount > 0) {
            score += (this.averageRating / 5.0) * 20.0;
        }
        
        // Recent activity boost (max 5 points)
        if (this.lastViewed != null && this.lastViewed.isAfter(LocalDateTime.now().minusDays(7))) {
            score += 5.0;
        }
        
        // Quality bonus (max 5 points)
        if (this.qualityScore != null) {
            score += (this.qualityScore / 100.0) * 5.0;
        }
        
        this.engagementScore = Math.min(100.0, score);
    }
    
    /**
     * Gets formatted total duration string.
     */
    public String getFormattedTotalDuration() {
        if (this.totalDuration == null) {
            return "Unknown";
        }
        
        long hours = this.totalDuration.toHours();
        long minutes = this.totalDuration.toMinutesPart();
        
        if (hours > 0) {
            return String.format("%d hours %d minutes", hours, minutes);
        } else {
            return String.format("%d minutes", minutes);
        }
    }
    
    /**
     * Calculates series progress for a user.
     */
    public double calculateProgress(Long userId) {
        if (this.tutorials.isEmpty()) {
            return 0.0;
        }
        
        int completedTutorials = 0;
        for (VideoTutorial tutorial : this.tutorials) {
            // This would need to check user completion status
            // Implementation would query user progress data
        }
        
        return (double) completedTutorials / this.tutorials.size() * 100.0;
    }
    
    /**
     * Checks if user is authorized to view this series.
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
     * Gets the next tutorial in sequence for a user.
     */
    public VideoTutorial getNextTutorialForUser(Long userId) {
        if (!this.requiresSequentialCompletion) {
            return null; // User can choose any tutorial
        }
        
        // Find first incomplete tutorial
        for (VideoTutorial tutorial : this.tutorials) {
            // This would need to check user completion status
            // Implementation would query user progress data
            // For now, return first tutorial as placeholder
            return tutorial;
        }
        
        return null; // All completed
    }
    
    /**
     * Checks if series meets completion requirements for certification.
     */
    public boolean meetsCompletionRequirements(Long userId) {
        double progress = calculateProgress(userId);
        return progress >= this.minimumCompletionPercentage;
    }
    
    /**
     * Gets difficulty level based on content.
     */
    public String getDifficultyDescription() {
        if (this.targetSkillLevel == null) {
            return "Not Specified";
        }
        
        return this.targetSkillLevel.getDisplayName();
    }
    
    /**
     * Archives this series with reason.
     */
    public void archive(String reason) {
        this.isArchived = true;
        this.isActive = false;
        this.archivedAt = LocalDateTime.now();
        this.archiveReason = reason;
        this.status = SeriesStatus.ARCHIVED;
    }
    
    /**
     * Restores archived series.
     */
    public void restore() {
        this.isArchived = false;
        this.isActive = true;
        this.archivedAt = null;
        this.archiveReason = null;
        this.status = SeriesStatus.PUBLISHED;
    }
    
    /**
     * Updates series timestamp.
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
    
    public SeriesType getSeriesType() { return seriesType; }
    public void setSeriesType(SeriesType seriesType) { this.seriesType = seriesType; }
    
    public SeriesCategory getCategory() { return category; }
    public void setCategory(SeriesCategory category) { this.category = category; }
    
    public SkillLevel getTargetSkillLevel() { return targetSkillLevel; }
    public void setTargetSkillLevel(SkillLevel targetSkillLevel) { this.targetSkillLevel = targetSkillLevel; }
    
    public SeriesStatus getStatus() { return status; }
    public void setStatus(SeriesStatus status) { this.status = status; }
    
    public List<VideoTutorial> getTutorials() { return tutorials; }
    public void setTutorials(List<VideoTutorial> tutorials) { this.tutorials = tutorials; }
    
    public Integer getTutorialCount() { return tutorialCount; }
    public void setTutorialCount(Integer tutorialCount) { this.tutorialCount = tutorialCount; }
    
    public Duration getTotalDuration() { return totalDuration; }
    public void setTotalDuration(Duration totalDuration) { this.totalDuration = totalDuration; }
    
    public Integer getEstimatedCompletionHours() { return estimatedCompletionHours; }
    public void setEstimatedCompletionHours(Integer estimatedCompletionHours) { this.estimatedCompletionHours = estimatedCompletionHours; }
    
    public List<String> getLearningObjectives() { return learningObjectives; }
    public void setLearningObjectives(List<String> learningObjectives) { this.learningObjectives = learningObjectives; }
    
    public List<String> getPrerequisites() { return prerequisites; }
    public void setPrerequisites(List<String> prerequisites) { this.prerequisites = prerequisites; }
    
    public List<String> getSkillsCovered() { return skillsCovered; }
    public void setSkillsCovered(List<String> skillsCovered) { this.skillsCovered = skillsCovered; }
    
    public List<String> getExpectedOutcomes() { return expectedOutcomes; }
    public void setExpectedOutcomes(List<String> expectedOutcomes) { this.expectedOutcomes = expectedOutcomes; }
    
    public Boolean getRequiresSequentialCompletion() { return requiresSequentialCompletion; }
    public void setRequiresSequentialCompletion(Boolean requiresSequentialCompletion) { this.requiresSequentialCompletion = requiresSequentialCompletion; }
    
    public Integer getMinimumCompletionPercentage() { return minimumCompletionPercentage; }
    public void setMinimumCompletionPercentage(Integer minimumCompletionPercentage) { this.minimumCompletionPercentage = minimumCompletionPercentage; }
    
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    
    public List<String> getTopics() { return topics; }
    public void setTopics(List<String> topics) { this.topics = topics; }
    
    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    
    public String getBannerUrl() { return bannerUrl; }
    public void setBannerUrl(String bannerUrl) { this.bannerUrl = bannerUrl; }
    
    public VisibilityLevel getVisibility() { return visibility; }
    public void setVisibility(VisibilityLevel visibility) { this.visibility = visibility; }
    
    public AccessLevel getViewPermission() { return viewPermission; }
    public void setViewPermission(AccessLevel viewPermission) { this.viewPermission = viewPermission; }
    
    public List<Long> getAuthorizedViewers() { return authorizedViewers; }
    public void setAuthorizedViewers(List<Long> authorizedViewers) { this.authorizedViewers = authorizedViewers; }
    
    public Boolean getIsFeatured() { return isFeatured; }
    public void setIsFeatured(Boolean isFeatured) { this.isFeatured = isFeatured; }
    
    public LocalDateTime getFeaturedAt() { return featuredAt; }
    public void setFeaturedAt(LocalDateTime featuredAt) { this.featuredAt = featuredAt; }
    
    public Long getViewCount() { return viewCount; }
    public void setViewCount(Long viewCount) { this.viewCount = viewCount; }
    
    public Long getEnrollmentCount() { return enrollmentCount; }
    public void setEnrollmentCount(Long enrollmentCount) { this.enrollmentCount = enrollmentCount; }
    
    public Long getCompletionCount() { return completionCount; }
    public void setCompletionCount(Long completionCount) { this.completionCount = completionCount; }
    
    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
    
    public Integer getRatingCount() { return ratingCount; }
    public void setRatingCount(Integer ratingCount) { this.ratingCount = ratingCount; }
    
    public Double getCompletionRate() { return completionRate; }
    public void setCompletionRate(Double completionRate) { this.completionRate = completionRate; }
    
    public Double getEngagementScore() { return engagementScore; }
    public void setEngagementScore(Double engagementScore) { this.engagementScore = engagementScore; }
    
    public LocalDateTime getLastViewed() { return lastViewed; }
    public void setLastViewed(LocalDateTime lastViewed) { this.lastViewed = lastViewed; }
    
    public Long getLastViewedBy() { return lastViewedBy; }
    public void setLastViewedBy(Long lastViewedBy) { this.lastViewedBy = lastViewedBy; }
    
    public TeamMember getCreatedBy() { return createdBy; }
    public void setCreatedBy(TeamMember createdBy) { this.createdBy = createdBy; }
    
    public TeamMember getPrimaryInstructor() { return primaryInstructor; }
    public void setPrimaryInstructor(TeamMember primaryInstructor) { this.primaryInstructor = primaryInstructor; }
    
    public List<Long> getInstructors() { return instructors; }
    public void setInstructors(List<Long> instructors) { this.instructors = instructors; }
    
    public List<Long> getContributors() { return contributors; }
    public void setContributors(List<Long> contributors) { this.contributors = contributors; }
    
    public Boolean getIsOriginalContent() { return isOriginalContent; }
    public void setIsOriginalContent(Boolean isOriginalContent) { this.isOriginalContent = isOriginalContent; }
    
    public String getSourceAttribution() { return sourceAttribution; }
    public void setSourceAttribution(String sourceAttribution) { this.sourceAttribution = sourceAttribution; }
    
    public String getLicense() { return license; }
    public void setLicense(String license) { this.license = license; }
    
    public Boolean getIsQualityChecked() { return isQualityChecked; }
    public void setIsQualityChecked(Boolean isQualityChecked) { this.isQualityChecked = isQualityChecked; }
    
    public Integer getQualityScore() { return qualityScore; }
    public void setQualityScore(Integer qualityScore) { this.qualityScore = qualityScore; }
    
    public Boolean getIsModerated() { return isModerated; }
    public void setIsModerated(Boolean isModerated) { this.isModerated = isModerated; }
    
    public TeamMember getModeratedBy() { return moderatedBy; }
    public void setModeratedBy(TeamMember moderatedBy) { this.moderatedBy = moderatedBy; }
    
    public LocalDateTime getModeratedAt() { return moderatedAt; }
    public void setModeratedAt(LocalDateTime moderatedAt) { this.moderatedAt = moderatedAt; }
    
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    
    public Boolean getIsCurrentVersion() { return isCurrentVersion; }
    public void setIsCurrentVersion(Boolean isCurrentVersion) { this.isCurrentVersion = isCurrentVersion; }
    
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    
    public TeamMember getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(TeamMember updatedBy) { this.updatedBy = updatedBy; }
    
    public String getUpdateNotes() { return updateNotes; }
    public void setUpdateNotes(String updateNotes) { this.updateNotes = updateNotes; }
    
    public Boolean getIsSearchable() { return isSearchable; }
    public void setIsSearchable(Boolean isSearchable) { this.isSearchable = isSearchable; }
    
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