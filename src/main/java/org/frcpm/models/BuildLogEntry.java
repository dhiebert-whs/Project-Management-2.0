// src/main/java/org/frcpm/models/BuildLogEntry.java

package org.frcpm.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Build Log Entry model for FRC team visual build documentation.
 * 
 * Provides comprehensive build progress tracking with photo documentation,
 * timeline visualization, milestone tracking, and collaborative build
 * process documentation for FRC teams during build season.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-KnowledgeManagement
 * @since Phase 4D.4 Visual Build Log with Photo Timeline
 */
@Entity
@Table(name = "build_log_entries")
public class BuildLogEntry {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Integer teamNumber;
    
    @Column(nullable = false)
    private Integer season;
    
    // Core Entry Information
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(length = 2000)
    private String description;
    
    @Column(length = 1000)
    private String workAccomplished;
    
    @Column(length = 1000)
    private String lessonsLearned;
    
    @Column(length = 1000)
    private String nextSteps;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntryType entryType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BuildPhase buildPhase;
    
    @Enumerated(EnumType.STRING)
    private Priority priority;
    
    // Timeline and Dating
    @Column(nullable = false)
    private LocalDateTime entryDateTime;
    
    @Column
    private LocalDateTime workStartTime;
    
    @Column
    private LocalDateTime workEndTime;
    
    @Column
    private Integer workDurationMinutes;
    
    @Column
    private Integer buildSeasonDay; // Day number in build season (1-42)
    
    @Column
    private Integer buildWeek; // Week number in build season (1-6)
    
    // Project and Task Association
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project relatedProject;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task relatedTask;
    
    @Column(length = 100)
    private String subsystem; // Robot subsystem (drivetrain, arm, intake, etc.)
    
    @Column(length = 100)
    private String component; // Specific component being worked on
    
    // Team Member Information
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_id", nullable = false)
    private TeamMember createdBy;
    
    @ElementCollection
    @CollectionTable(name = "build_log_participants")
    private List<Long> participantIds = new ArrayList<>(); // IDs of participating team members
    
    @ElementCollection
    @CollectionTable(name = "build_log_participant_names")
    private List<String> participantNames = new ArrayList<>(); // Names for display
    
    // Photo and Media Management
    @ElementCollection
    @CollectionTable(name = "build_log_photos")
    private List<String> photoUrls = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "build_log_photo_captions")
    private List<String> photoCaptions = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "build_log_video_urls")
    private List<String> videoUrls = new ArrayList<>();
    
    @Column(length = 500)
    private String primaryPhotoUrl; // Main photo for timeline display
    
    @Column(length = 200)
    private String primaryPhotoCaption;
    
    @Column
    private Integer photoCount = 0;
    
    @Column
    private Integer videoCount = 0;
    
    // Technical Documentation
    @ElementCollection
    @CollectionTable(name = "build_log_materials")
    private List<String> materialsUsed = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "build_log_tools")
    private List<String> toolsUsed = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "build_log_techniques")
    private List<String> techniquesUsed = new ArrayList<>();
    
    @Column(length = 1000)
    private String technicalNotes;
    
    @Column(length = 500)
    private String safetyNotes;
    
    // Progress and Metrics
    @Column
    private Double progressPercentage = 0.0; // Overall progress on related item
    
    @Column
    private Double qualityRating; // 1-5 quality assessment
    
    @Column
    private Double difficultyRating; // 1-5 difficulty assessment
    
    @Enumerated(EnumType.STRING)
    private CompletionStatus completionStatus;
    
    @Column
    private Boolean isMilestone = false;
    
    @Column(length = 200)
    private String milestoneDescription;
    
    // Problem and Solution Tracking
    @Column(length = 1000)
    private String problemsEncountered;
    
    @Column(length = 1000)
    private String solutionsImplemented;
    
    @Column
    private Integer problemCount = 0;
    
    @Column
    private Boolean requiresFollowUp = false;
    
    @Column(length = 500)
    private String followUpActions;
    
    // Collaboration and Review
    @Column
    private Boolean isReviewed = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id")
    private TeamMember reviewedBy;
    
    @Column
    private LocalDateTime reviewedAt;
    
    @Column(length = 1000)
    private String reviewComments;
    
    @Column
    private Integer helpfulVotes = 0;
    
    @Column
    private Boolean isFeatured = false;
    
    // Timeline and Visibility
    @Column
    private Boolean isPublic = false; // Visible to other teams
    
    @Column
    private Boolean isHighlight = false; // Featured in timeline highlights
    
    @Enumerated(EnumType.STRING)
    private VisibilityLevel visibility;
    
    @ElementCollection
    @CollectionTable(name = "build_log_tags")
    private List<String> tags = new ArrayList<>();
    
    @Column
    private Integer timelineOrder; // Custom ordering for timeline display
    
    // Competition and Event Relation
    @Column(length = 100)
    private String targetEvent; // Competition this work is targeting
    
    @Column
    private LocalDateTime targetDate; // Deadline for this work
    
    @Column
    private Boolean isCompetitionReady = false;
    
    // Weather and Environment (for outdoor work)
    @Column(length = 100)
    private String workLocation; // Workshop, home, competition venue, etc.
    
    @Column(length = 100)
    private String weatherConditions;
    
    @Column
    private Double temperatureFahrenheit;
    
    // Measurement and Specifications
    @Column
    private Double measurementValue; // Key measurement for this entry
    
    @Column(length = 50)
    private String measurementUnit; // inches, pounds, volts, etc.
    
    @Column(length = 200)
    private String measurementDescription;
    
    @ElementCollection
    @CollectionTable(name = "build_log_specifications")
    private List<String> specifications = new ArrayList<>();
    
    // Integration and Dependencies
    @Column
    private Boolean blocksFutureWork = false;
    
    @ElementCollection
    @CollectionTable(name = "build_log_dependencies")
    private List<String> dependencies = new ArrayList<>(); // What this entry depends on
    
    @ElementCollection
    @CollectionTable(name = "build_log_enables")
    private List<String> enables = new ArrayList<>(); // What this entry enables
    
    // Analytics and Engagement
    @Column
    private Integer viewCount = 0;
    
    @Column
    private Integer commentCount = 0;
    
    @Column
    private Integer shareCount = 0;
    
    @Column
    private Double engagementScore = 0.0;
    
    @Column
    private LocalDateTime lastViewed;
    
    // Content Quality and Moderation
    @Column
    private Integer contentQualityScore = 0; // 0-100 score
    
    @Column
    private Boolean isModerated = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderated_by_id")
    private TeamMember moderatedBy;
    
    @Column
    private LocalDateTime moderatedAt;
    
    @Column
    private Boolean isFlagged = false;
    
    @Column(length = 500)
    private String flagReason;
    
    // Search and Discovery
    @Column
    private Boolean isSearchable = true;
    
    @Column
    private Integer searchRank = 0;
    
    @ElementCollection
    @CollectionTable(name = "build_log_keywords")
    private List<String> searchKeywords = new ArrayList<>();
    
    // Archive and History
    @Column
    private Boolean isArchived = false;
    
    @Column
    private LocalDateTime archivedAt;
    
    @Column(length = 500)
    private String archiveReason;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "archived_by_id")
    private TeamMember archivedBy;
    
    // Audit Fields
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_id")
    private TeamMember updatedBy;
    
    @Column
    private Boolean isActive = true;
    
    // Enums
    public enum EntryType {
        WORK_SESSION("Work Session"),
        MILESTONE("Milestone"),
        PROBLEM_SOLUTION("Problem & Solution"),
        TESTING("Testing & Validation"),
        DESIGN_CHANGE("Design Change"),
        ASSEMBLY("Assembly Work"),
        FABRICATION("Fabrication"),
        PROGRAMMING("Programming"),
        WIRING("Wiring & Electronics"),
        INSPECTION("Quality Inspection"),
        DOCUMENTATION("Documentation"),
        MEETING("Team Meeting"),
        TRAINING("Skills Training"),
        COMPETITION("Competition Event"),
        REFLECTION("Reflection & Analysis");
        
        private final String displayName;
        
        EntryType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum BuildPhase {
        PRE_SEASON("Pre-Season"),
        KICKOFF("Kickoff"),
        DESIGN("Design Phase"),
        PROTOTYPING("Prototyping"),
        FABRICATION("Fabrication"),
        ASSEMBLY("Assembly"),
        PROGRAMMING("Programming"),
        TESTING("Testing & Iteration"),
        INTEGRATION("System Integration"),
        COMPETITION_PREP("Competition Prep"),
        COMPETITION("Competition"),
        POST_SEASON("Post-Season");
        
        private final String displayName;
        
        BuildPhase(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum Priority {
        CRITICAL("Critical"),
        HIGH("High"),
        MEDIUM("Medium"),
        LOW("Low"),
        INFORMATIONAL("Informational");
        
        private final String displayName;
        
        Priority(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum CompletionStatus {
        NOT_STARTED("Not Started"),
        IN_PROGRESS("In Progress"),
        COMPLETED("Completed"),
        ON_HOLD("On Hold"),
        BLOCKED("Blocked"),
        TESTING("Testing"),
        FAILED("Failed"),
        CANCELLED("Cancelled");
        
        private final String displayName;
        
        CompletionStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum VisibilityLevel {
        PRIVATE("Private"),
        TEAM_ONLY("Team Only"),
        MENTORS_ONLY("Mentors Only"),
        PUBLIC("Public"),
        COMPETITION_TEAMS("Competition Teams"),
        FRC_COMMUNITY("FRC Community");
        
        private final String displayName;
        
        VisibilityLevel(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Constructors
    public BuildLogEntry() {
        this.entryDateTime = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public BuildLogEntry(Integer teamNumber, Integer season, String title, EntryType entryType, 
                        BuildPhase buildPhase, TeamMember createdBy) {
        this();
        this.teamNumber = teamNumber;
        this.season = season;
        this.title = title;
        this.entryType = entryType;
        this.buildPhase = buildPhase;
        this.createdBy = createdBy;
    }
    
    // Business Methods
    
    /**
     * Adds a photo to the build log entry.
     */
    public void addPhoto(String photoUrl, String caption) {
        if (this.photoUrls == null) {
            this.photoUrls = new ArrayList<>();
        }
        if (this.photoCaptions == null) {
            this.photoCaptions = new ArrayList<>();
        }
        
        this.photoUrls.add(photoUrl);
        this.photoCaptions.add(caption != null ? caption : "");
        this.photoCount = this.photoUrls.size();
        
        // Set as primary photo if first one
        if (this.primaryPhotoUrl == null && photoUrl != null) {
            this.primaryPhotoUrl = photoUrl;
            this.primaryPhotoCaption = caption;
        }
        
        updateContentQuality();
    }
    
    /**
     * Removes a photo from the build log entry.
     */
    public void removePhoto(String photoUrl) {
        if (this.photoUrls != null) {
            int index = this.photoUrls.indexOf(photoUrl);
            if (index >= 0) {
                this.photoUrls.remove(index);
                if (this.photoCaptions != null && index < this.photoCaptions.size()) {
                    this.photoCaptions.remove(index);
                }
                this.photoCount = this.photoUrls.size();
                
                // Update primary photo if removed
                if (photoUrl.equals(this.primaryPhotoUrl)) {
                    this.primaryPhotoUrl = !this.photoUrls.isEmpty() ? this.photoUrls.get(0) : null;
                    this.primaryPhotoCaption = !this.photoCaptions.isEmpty() ? this.photoCaptions.get(0) : null;
                }
                
                updateContentQuality();
            }
        }
    }
    
    /**
     * Adds a participant to the build log entry.
     */
    public void addParticipant(Long participantId, String participantName) {
        if (this.participantIds == null) {
            this.participantIds = new ArrayList<>();
        }
        if (this.participantNames == null) {
            this.participantNames = new ArrayList<>();
        }
        
        if (!this.participantIds.contains(participantId)) {
            this.participantIds.add(participantId);
            this.participantNames.add(participantName);
        }
    }
    
    /**
     * Removes a participant from the build log entry.
     */
    public void removeParticipant(Long participantId) {
        if (this.participantIds != null) {
            int index = this.participantIds.indexOf(participantId);
            if (index >= 0) {
                this.participantIds.remove(index);
                if (this.participantNames != null && index < this.participantNames.size()) {
                    this.participantNames.remove(index);
                }
            }
        }
    }
    
    /**
     * Calculates work duration in minutes.
     */
    public Integer calculateWorkDuration() {
        if (this.workStartTime != null && this.workEndTime != null) {
            long minutes = java.time.Duration.between(this.workStartTime, this.workEndTime).toMinutes();
            this.workDurationMinutes = (int) minutes;
            return this.workDurationMinutes;
        }
        return this.workDurationMinutes;
    }
    
    /**
     * Sets work session times and calculates duration.
     */
    public void setWorkSession(LocalDateTime startTime, LocalDateTime endTime) {
        this.workStartTime = startTime;
        this.workEndTime = endTime;
        calculateWorkDuration();
    }
    
    /**
     * Determines if this entry represents a significant milestone.
     */
    public boolean isSignificantMilestone() {
        return this.isMilestone || 
               this.entryType == EntryType.MILESTONE ||
               this.completionStatus == CompletionStatus.COMPLETED && this.progressPercentage >= 90.0 ||
               this.isHighlight;
    }
    
    /**
     * Calculates build season day from entry date.
     */
    public void calculateBuildSeasonDay(LocalDateTime kickoffDate) {
        if (kickoffDate != null && this.entryDateTime != null) {
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(
                kickoffDate.toLocalDate(), 
                this.entryDateTime.toLocalDate()
            );
            this.buildSeasonDay = Math.max(1, (int) daysBetween + 1);
            this.buildWeek = Math.max(1, (this.buildSeasonDay - 1) / 7 + 1);
        }
    }
    
    /**
     * Updates content quality score based on various factors.
     */
    public void updateContentQuality() {
        int score = 0;
        
        // Basic content (40 points)
        if (this.title != null && !this.title.trim().isEmpty()) score += 10;
        if (this.description != null && this.description.length() > 50) score += 15;
        if (this.workAccomplished != null && !this.workAccomplished.trim().isEmpty()) score += 15;
        
        // Visual content (30 points)
        if (this.photoCount > 0) score += 15;
        if (this.photoCount >= 3) score += 10;
        if (this.videoCount > 0) score += 5;
        
        // Technical documentation (20 points)
        if (this.technicalNotes != null && !this.technicalNotes.trim().isEmpty()) score += 10;
        if (this.materialsUsed != null && !this.materialsUsed.isEmpty()) score += 5;
        if (this.toolsUsed != null && !this.toolsUsed.isEmpty()) score += 5;
        
        // Learning and reflection (10 points)
        if (this.lessonsLearned != null && !this.lessonsLearned.trim().isEmpty()) score += 5;
        if (this.nextSteps != null && !this.nextSteps.trim().isEmpty()) score += 5;
        
        this.contentQualityScore = Math.min(100, score);
    }
    
    /**
     * Calculates engagement score based on views, comments, and shares.
     */
    public void updateEngagementScore() {
        double score = 0.0;
        
        // View engagement (40%)
        if (this.viewCount > 0) {
            score += Math.min(40.0, this.viewCount * 2.0);
        }
        
        // Comment engagement (35%)
        if (this.commentCount > 0) {
            score += Math.min(35.0, this.commentCount * 7.0);
        }
        
        // Share engagement (25%)
        if (this.shareCount > 0) {
            score += Math.min(25.0, this.shareCount * 12.5);
        }
        
        this.engagementScore = Math.min(100.0, score);
    }
    
    /**
     * Checks if entry needs follow-up based on various factors.
     */
    public boolean needsFollowUp() {
        return this.requiresFollowUp ||
               (this.problemsEncountered != null && !this.problemsEncountered.trim().isEmpty() && 
                (this.solutionsImplemented == null || this.solutionsImplemented.trim().isEmpty())) ||
               this.completionStatus == CompletionStatus.BLOCKED ||
               this.completionStatus == CompletionStatus.ON_HOLD ||
               this.blocksFutureWork;
    }
    
    /**
     * Generates summary text for timeline display.
     */
    public String generateTimelineSummary() {
        StringBuilder summary = new StringBuilder();
        
        if (this.workAccomplished != null && !this.workAccomplished.trim().isEmpty()) {
            summary.append(this.workAccomplished);
        } else if (this.description != null && !this.description.trim().isEmpty()) {
            summary.append(this.description);
        }
        
        // Truncate to reasonable length for timeline
        String result = summary.toString();
        if (result.length() > 150) {
            result = result.substring(0, 147) + "...";
        }
        
        return result;
    }
    
    /**
     * Checks if entry is visible to specified user role.
     */
    public boolean isVisibleToRole(String userRole) {
        if (this.visibility == null) {
            return true; // Default to visible
        }
        
        switch (this.visibility) {
            case PRIVATE:
                return false; // Only visible to creator (would need additional check)
            case TEAM_ONLY:
                return userRole.equals("STUDENT") || userRole.equals("MENTOR") || userRole.equals("ADMIN");
            case MENTORS_ONLY:
                return userRole.equals("MENTOR") || userRole.equals("ADMIN");
            case PUBLIC:
            case COMPETITION_TEAMS:
            case FRC_COMMUNITY:
                return true;
            default:
                return true;
        }
    }
    
    /**
     * Gets display text for progress percentage.
     */
    public String getProgressDisplayText() {
        if (this.progressPercentage == null) {
            return "Not specified";
        }
        return String.format("%.1f%% complete", this.progressPercentage);
    }
    
    /**
     * Gets CSS class for completion status styling.
     */
    public String getCompletionStatusCssClass() {
        if (this.completionStatus == null) {
            return "status-unknown";
        }
        
        switch (this.completionStatus) {
            case COMPLETED:
                return "status-completed";
            case IN_PROGRESS:
                return "status-in-progress";
            case BLOCKED:
                return "status-blocked";
            case ON_HOLD:
                return "status-on-hold";
            case FAILED:
                return "status-failed";
            case CANCELLED:
                return "status-cancelled";
            default:
                return "status-default";
        }
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
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getWorkAccomplished() { return workAccomplished; }
    public void setWorkAccomplished(String workAccomplished) { this.workAccomplished = workAccomplished; }
    
    public String getLessonsLearned() { return lessonsLearned; }
    public void setLessonsLearned(String lessonsLearned) { this.lessonsLearned = lessonsLearned; }
    
    public String getNextSteps() { return nextSteps; }
    public void setNextSteps(String nextSteps) { this.nextSteps = nextSteps; }
    
    public EntryType getEntryType() { return entryType; }
    public void setEntryType(EntryType entryType) { this.entryType = entryType; }
    
    public BuildPhase getBuildPhase() { return buildPhase; }
    public void setBuildPhase(BuildPhase buildPhase) { this.buildPhase = buildPhase; }
    
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    
    public LocalDateTime getEntryDateTime() { return entryDateTime; }
    public void setEntryDateTime(LocalDateTime entryDateTime) { this.entryDateTime = entryDateTime; }
    
    public LocalDateTime getWorkStartTime() { return workStartTime; }
    public void setWorkStartTime(LocalDateTime workStartTime) { this.workStartTime = workStartTime; }
    
    public LocalDateTime getWorkEndTime() { return workEndTime; }
    public void setWorkEndTime(LocalDateTime workEndTime) { this.workEndTime = workEndTime; }
    
    public Integer getWorkDurationMinutes() { return workDurationMinutes; }
    public void setWorkDurationMinutes(Integer workDurationMinutes) { this.workDurationMinutes = workDurationMinutes; }
    
    public Integer getBuildSeasonDay() { return buildSeasonDay; }
    public void setBuildSeasonDay(Integer buildSeasonDay) { this.buildSeasonDay = buildSeasonDay; }
    
    public Integer getBuildWeek() { return buildWeek; }
    public void setBuildWeek(Integer buildWeek) { this.buildWeek = buildWeek; }
    
    public Project getRelatedProject() { return relatedProject; }
    public void setRelatedProject(Project relatedProject) { this.relatedProject = relatedProject; }
    
    public Task getRelatedTask() { return relatedTask; }
    public void setRelatedTask(Task relatedTask) { this.relatedTask = relatedTask; }
    
    public String getSubsystem() { return subsystem; }
    public void setSubsystem(String subsystem) { this.subsystem = subsystem; }
    
    public String getComponent() { return component; }
    public void setComponent(String component) { this.component = component; }
    
    public TeamMember getCreatedBy() { return createdBy; }
    public void setCreatedBy(TeamMember createdBy) { this.createdBy = createdBy; }
    
    public List<Long> getParticipantIds() { return participantIds; }
    public void setParticipantIds(List<Long> participantIds) { this.participantIds = participantIds; }
    
    public List<String> getParticipantNames() { return participantNames; }
    public void setParticipantNames(List<String> participantNames) { this.participantNames = participantNames; }
    
    public List<String> getPhotoUrls() { return photoUrls; }
    public void setPhotoUrls(List<String> photoUrls) { this.photoUrls = photoUrls; }
    
    public List<String> getPhotoCaptions() { return photoCaptions; }
    public void setPhotoCaptions(List<String> photoCaptions) { this.photoCaptions = photoCaptions; }
    
    public List<String> getVideoUrls() { return videoUrls; }
    public void setVideoUrls(List<String> videoUrls) { this.videoUrls = videoUrls; }
    
    public String getPrimaryPhotoUrl() { return primaryPhotoUrl; }
    public void setPrimaryPhotoUrl(String primaryPhotoUrl) { this.primaryPhotoUrl = primaryPhotoUrl; }
    
    public String getPrimaryPhotoCaption() { return primaryPhotoCaption; }
    public void setPrimaryPhotoCaption(String primaryPhotoCaption) { this.primaryPhotoCaption = primaryPhotoCaption; }
    
    public Integer getPhotoCount() { return photoCount; }
    public void setPhotoCount(Integer photoCount) { this.photoCount = photoCount; }
    
    public Integer getVideoCount() { return videoCount; }
    public void setVideoCount(Integer videoCount) { this.videoCount = videoCount; }
    
    public List<String> getMaterialsUsed() { return materialsUsed; }
    public void setMaterialsUsed(List<String> materialsUsed) { this.materialsUsed = materialsUsed; }
    
    public List<String> getToolsUsed() { return toolsUsed; }
    public void setToolsUsed(List<String> toolsUsed) { this.toolsUsed = toolsUsed; }
    
    public List<String> getTechniquesUsed() { return techniquesUsed; }
    public void setTechniquesUsed(List<String> techniquesUsed) { this.techniquesUsed = techniquesUsed; }
    
    public String getTechnicalNotes() { return technicalNotes; }
    public void setTechnicalNotes(String technicalNotes) { this.technicalNotes = technicalNotes; }
    
    public String getSafetyNotes() { return safetyNotes; }
    public void setSafetyNotes(String safetyNotes) { this.safetyNotes = safetyNotes; }
    
    public Double getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(Double progressPercentage) { this.progressPercentage = progressPercentage; }
    
    public Double getQualityRating() { return qualityRating; }
    public void setQualityRating(Double qualityRating) { this.qualityRating = qualityRating; }
    
    public Double getDifficultyRating() { return difficultyRating; }
    public void setDifficultyRating(Double difficultyRating) { this.difficultyRating = difficultyRating; }
    
    public CompletionStatus getCompletionStatus() { return completionStatus; }
    public void setCompletionStatus(CompletionStatus completionStatus) { this.completionStatus = completionStatus; }
    
    public Boolean getIsMilestone() { return isMilestone; }
    public void setIsMilestone(Boolean isMilestone) { this.isMilestone = isMilestone; }
    
    public String getMilestoneDescription() { return milestoneDescription; }
    public void setMilestoneDescription(String milestoneDescription) { this.milestoneDescription = milestoneDescription; }
    
    public String getProblemsEncountered() { return problemsEncountered; }
    public void setProblemsEncountered(String problemsEncountered) { this.problemsEncountered = problemsEncountered; }
    
    public String getSolutionsImplemented() { return solutionsImplemented; }
    public void setSolutionsImplemented(String solutionsImplemented) { this.solutionsImplemented = solutionsImplemented; }
    
    public Integer getProblemCount() { return problemCount; }
    public void setProblemCount(Integer problemCount) { this.problemCount = problemCount; }
    
    public Boolean getRequiresFollowUp() { return requiresFollowUp; }
    public void setRequiresFollowUp(Boolean requiresFollowUp) { this.requiresFollowUp = requiresFollowUp; }
    
    public String getFollowUpActions() { return followUpActions; }
    public void setFollowUpActions(String followUpActions) { this.followUpActions = followUpActions; }
    
    public Boolean getIsReviewed() { return isReviewed; }
    public void setIsReviewed(Boolean isReviewed) { this.isReviewed = isReviewed; }
    
    public TeamMember getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(TeamMember reviewedBy) { this.reviewedBy = reviewedBy; }
    
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
    
    public String getReviewComments() { return reviewComments; }
    public void setReviewComments(String reviewComments) { this.reviewComments = reviewComments; }
    
    public Integer getHelpfulVotes() { return helpfulVotes; }
    public void setHelpfulVotes(Integer helpfulVotes) { this.helpfulVotes = helpfulVotes; }
    
    public Boolean getIsFeatured() { return isFeatured; }
    public void setIsFeatured(Boolean isFeatured) { this.isFeatured = isFeatured; }
    
    public Boolean getIsPublic() { return isPublic; }
    public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }
    
    public Boolean getIsHighlight() { return isHighlight; }
    public void setIsHighlight(Boolean isHighlight) { this.isHighlight = isHighlight; }
    
    public VisibilityLevel getVisibility() { return visibility; }
    public void setVisibility(VisibilityLevel visibility) { this.visibility = visibility; }
    
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    
    public Integer getTimelineOrder() { return timelineOrder; }
    public void setTimelineOrder(Integer timelineOrder) { this.timelineOrder = timelineOrder; }
    
    public String getTargetEvent() { return targetEvent; }
    public void setTargetEvent(String targetEvent) { this.targetEvent = targetEvent; }
    
    public LocalDateTime getTargetDate() { return targetDate; }
    public void setTargetDate(LocalDateTime targetDate) { this.targetDate = targetDate; }
    
    public Boolean getIsCompetitionReady() { return isCompetitionReady; }
    public void setIsCompetitionReady(Boolean isCompetitionReady) { this.isCompetitionReady = isCompetitionReady; }
    
    public String getWorkLocation() { return workLocation; }
    public void setWorkLocation(String workLocation) { this.workLocation = workLocation; }
    
    public String getWeatherConditions() { return weatherConditions; }
    public void setWeatherConditions(String weatherConditions) { this.weatherConditions = weatherConditions; }
    
    public Double getTemperatureFahrenheit() { return temperatureFahrenheit; }
    public void setTemperatureFahrenheit(Double temperatureFahrenheit) { this.temperatureFahrenheit = temperatureFahrenheit; }
    
    public Double getMeasurementValue() { return measurementValue; }
    public void setMeasurementValue(Double measurementValue) { this.measurementValue = measurementValue; }
    
    public String getMeasurementUnit() { return measurementUnit; }
    public void setMeasurementUnit(String measurementUnit) { this.measurementUnit = measurementUnit; }
    
    public String getMeasurementDescription() { return measurementDescription; }
    public void setMeasurementDescription(String measurementDescription) { this.measurementDescription = measurementDescription; }
    
    public List<String> getSpecifications() { return specifications; }
    public void setSpecifications(List<String> specifications) { this.specifications = specifications; }
    
    public Boolean getBlocksFutureWork() { return blocksFutureWork; }
    public void setBlocksFutureWork(Boolean blocksFutureWork) { this.blocksFutureWork = blocksFutureWork; }
    
    public List<String> getDependencies() { return dependencies; }
    public void setDependencies(List<String> dependencies) { this.dependencies = dependencies; }
    
    public List<String> getEnables() { return enables; }
    public void setEnables(List<String> enables) { this.enables = enables; }
    
    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
    
    public Integer getCommentCount() { return commentCount; }
    public void setCommentCount(Integer commentCount) { this.commentCount = commentCount; }
    
    public Integer getShareCount() { return shareCount; }
    public void setShareCount(Integer shareCount) { this.shareCount = shareCount; }
    
    public Double getEngagementScore() { return engagementScore; }
    public void setEngagementScore(Double engagementScore) { this.engagementScore = engagementScore; }
    
    public LocalDateTime getLastViewed() { return lastViewed; }
    public void setLastViewed(LocalDateTime lastViewed) { this.lastViewed = lastViewed; }
    
    public Integer getContentQualityScore() { return contentQualityScore; }
    public void setContentQualityScore(Integer contentQualityScore) { this.contentQualityScore = contentQualityScore; }
    
    public Boolean getIsModerated() { return isModerated; }
    public void setIsModerated(Boolean isModerated) { this.isModerated = isModerated; }
    
    public TeamMember getModeratedBy() { return moderatedBy; }
    public void setModeratedBy(TeamMember moderatedBy) { this.moderatedBy = moderatedBy; }
    
    public LocalDateTime getModeratedAt() { return moderatedAt; }
    public void setModeratedAt(LocalDateTime moderatedAt) { this.moderatedAt = moderatedAt; }
    
    public Boolean getIsFlagged() { return isFlagged; }
    public void setIsFlagged(Boolean isFlagged) { this.isFlagged = isFlagged; }
    
    public String getFlagReason() { return flagReason; }
    public void setFlagReason(String flagReason) { this.flagReason = flagReason; }
    
    public Boolean getIsSearchable() { return isSearchable; }
    public void setIsSearchable(Boolean isSearchable) { this.isSearchable = isSearchable; }
    
    public Integer getSearchRank() { return searchRank; }
    public void setSearchRank(Integer searchRank) { this.searchRank = searchRank; }
    
    public List<String> getSearchKeywords() { return searchKeywords; }
    public void setSearchKeywords(List<String> searchKeywords) { this.searchKeywords = searchKeywords; }
    
    public Boolean getIsArchived() { return isArchived; }
    public void setIsArchived(Boolean isArchived) { this.isArchived = isArchived; }
    
    public LocalDateTime getArchivedAt() { return archivedAt; }
    public void setArchivedAt(LocalDateTime archivedAt) { this.archivedAt = archivedAt; }
    
    public String getArchiveReason() { return archiveReason; }
    public void setArchiveReason(String archiveReason) { this.archiveReason = archiveReason; }
    
    public TeamMember getArchivedBy() { return archivedBy; }
    public void setArchivedBy(TeamMember archivedBy) { this.archivedBy = archivedBy; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public TeamMember getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(TeamMember updatedBy) { this.updatedBy = updatedBy; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}