// src/main/java/org/frcpm/models/MentorshipRelationship.java

package org.frcpm.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Mentorship Relationship model for FRC team mentorship tracking.
 * 
 * Provides comprehensive mentorship management for FRC teams including
 * mentor-student pairing, progress tracking, goal setting, and
 * relationship analytics for effective team development.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-KnowledgeManagement
 * @since Phase 4D.3 Mentorship Tracking System
 */
@Entity
@Table(name = "mentorship_relationships")
public class MentorshipRelationship {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Integer teamNumber;
    
    @Column(nullable = false)
    private Integer season;
    
    // Core Relationship
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mentor_id", nullable = false)
    private TeamMember mentor;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mentee_id", nullable = false)
    private TeamMember mentee;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RelationshipType relationshipType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MentorshipStatus status;
    
    @Enumerated(EnumType.STRING)
    private MentorshipFocus primaryFocus;
    
    // Relationship Details
    @Column(nullable = false)
    private LocalDate startDate;
    
    @Column
    private LocalDate endDate;
    
    @Column
    private LocalDate expectedEndDate;
    
    @Column(length = 1000)
    private String relationshipDescription;
    
    @Column(length = 500)
    private String mentorshipGoals;
    
    @ElementCollection
    @CollectionTable(name = "mentorship_focus_areas")
    private List<String> focusAreas = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "mentorship_skills_development")
    private List<String> skillsDevelopment = new ArrayList<>();
    
    @Column
    private Integer expectedDurationWeeks;
    
    @Column
    private Integer actualDurationWeeks;
    
    // Meeting and Interaction Tracking
    @Column(nullable = false)
    private Integer totalMeetings = 0;
    
    @Column(nullable = false)
    private Integer completedMeetings = 0;
    
    @Column(nullable = false)
    private Integer cancelledMeetings = 0;
    
    @Column
    private Integer plannedMeetingsPerWeek = 1;
    
    @Column
    private Integer averageMeetingDurationMinutes = 60;
    
    @Column
    private LocalDateTime lastMeetingDate;
    
    @Column
    private LocalDateTime nextScheduledMeeting;
    
    @Column
    private Double meetingAttendanceRate = 0.0;
    
    // Progress and Goals
    @ElementCollection
    @CollectionTable(name = "mentorship_goals")
    private List<String> goals = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "mentorship_milestones")
    private List<String> milestones = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "mentorship_completed_goals")
    private List<String> completedGoals = new ArrayList<>();
    
    @Column
    private Double overallProgressPercentage = 0.0;
    
    @Column
    private Double goalCompletionRate = 0.0;
    
    @Column
    private Integer currentMilestoneIndex = 0;
    
    @Column(length = 1000)
    private String progressNotes;
    
    @Column(length = 1000)
    private String challengesEncountered;
    
    @Column(length = 1000)
    private String successStories;
    
    // Assessment and Feedback
    @Column
    private Double mentorRating = 0.0; // Mentee's rating of mentor
    
    @Column
    private Double menteeRating = 0.0; // Mentor's rating of mentee
    
    @Column
    private Double relationshipSatisfaction = 0.0;
    
    @Column
    private Double learningEffectiveness = 0.0;
    
    @Column
    private Double communicationQuality = 0.0;
    
    @Column
    private Integer feedbackCount = 0;
    
    @Column
    private LocalDateTime lastFeedbackDate;
    
    @Column(length = 1000)
    private String mentorFeedback;
    
    @Column(length = 1000)
    private String menteeFeedback;
    
    @Column(length = 1000)
    private String improvementSuggestions;
    
    // Skills and Learning Analytics
    @ElementCollection
    @CollectionTable(name = "mentorship_skills_learned")
    private List<String> skillsLearned = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "mentorship_competencies_gained")
    private List<String> competenciesGained = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "mentorship_certifications_earned")
    private List<String> certificationsEarned = new ArrayList<>();
    
    @Column
    private Integer projectsWorkedOn = 0;
    
    @Column
    private Integer tasksCompleted = 0;
    
    @Column
    private Integer videoTutorialsWatched = 0;
    
    @Column
    private Integer documentsCreated = 0;
    
    @Column
    private Double skillGrowthRate = 0.0;
    
    @Column
    private Double knowledgeTransferEffectiveness = 0.0;
    
    // Communication and Engagement
    @Enumerated(EnumType.STRING)
    private CommunicationFrequency communicationFrequency;
    
    @ElementCollection
    @CollectionTable(name = "mentorship_communication_methods")
    private List<String> preferredCommunicationMethods = new ArrayList<>();
    
    @Column
    private Integer totalCommunications = 0;
    
    @Column
    private Double responseTimeHours = 24.0;
    
    @Column
    private Double engagementLevel = 0.0;
    
    @Column
    private Boolean isRemoteMentorship = false;
    
    @Column
    private Boolean requiresParentalConsent = false;
    
    @Column
    private LocalDateTime lastContactDate;
    
    // Resource and Support
    @ElementCollection
    @CollectionTable(name = "mentorship_resources_used")
    private List<String> resourcesUsed = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "mentorship_tools_used")
    private List<String> toolsUsed = new ArrayList<>();
    
    @Column
    private Boolean hasStructuredPlan = false;
    
    @Column
    private Boolean followsTeamGuidelines = true;
    
    @Column
    private Boolean needsAdditionalSupport = false;
    
    @Column(length = 500)
    private String supportNeeded;
    
    @Column(length = 500)
    private String resourcesNeeded;
    
    // Outcome and Impact Measurement
    @Column
    private Double menteeSkillImprovement = 0.0;
    
    @Column
    private Double menteeConfidenceIncrease = 0.0;
    
    @Column
    private Double teamContributionIncrease = 0.0;
    
    @Column
    private Double leadershipDevelopment = 0.0;
    
    @Column
    private Boolean achievedPrimaryGoals = false;
    
    @Column
    private Boolean exceededExpectations = false;
    
    @Column
    private Boolean recommendedForAdvancement = false;
    
    @Column
    private Boolean eligibleForMentorRole = false;
    
    @Column(length = 1000)
    private String impactAssessment;
    
    @Column(length = 1000)
    private String futureDevelopmentPlan;
    
    // Administrative and Compliance
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by")
    private TeamMember assignedBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_id")
    private TeamMember supervisor; // Lead mentor overseeing relationship
    
    @Column
    private Boolean requiresApproval = false;
    
    @Column
    private Boolean isApproved = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private TeamMember approvedBy;
    
    @Column
    private LocalDateTime approvedAt;
    
    @Column
    private Boolean isDocumented = false;
    
    @Column
    private Boolean complianceChecked = false;
    
    @Column
    private LocalDateTime lastComplianceCheck;
    
    // Analytics and Reporting
    @Column
    private Double successProbability = 0.0;
    
    @Column
    private Double riskScore = 0.0;
    
    @Column
    private Integer warningFlags = 0;
    
    @Column(length = 500)
    private String riskFactors;
    
    @Column(length = 500)
    private String successFactors;
    
    @Column
    private Boolean isHighPerforming = false;
    
    @Column
    private Boolean needsIntervention = false;
    
    @Column
    private LocalDateTime lastAnalysisDate;
    
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
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private TeamMember createdBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private TeamMember updatedBy;
    
    // =========================================================================
    // ENUMS
    // =========================================================================
    
    public enum RelationshipType {
        MENTOR_STUDENT("Mentor-Student"),
        PEER_MENTOR("Peer Mentor"),
        TECHNICAL_SPECIALIST("Technical Specialist"),
        LEADERSHIP_MENTOR("Leadership Mentor"),
        PROJECT_MENTOR("Project Mentor"),
        ONBOARDING_BUDDY("Onboarding Buddy"),
        SKILLS_COACH("Skills Coach"),
        CAREER_ADVISOR("Career Advisor");
        
        private final String displayName;
        
        RelationshipType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum MentorshipStatus {
        PROPOSED("Proposed"),
        PENDING_APPROVAL("Pending Approval"),
        ACTIVE("Active"),
        ON_HOLD("On Hold"),
        COMPLETED("Completed"),
        TERMINATED("Terminated"),
        PAUSED("Paused"),
        UNDER_REVIEW("Under Review");
        
        private final String displayName;
        
        MentorshipStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum MentorshipFocus {
        TECHNICAL_SKILLS("Technical Skills"),
        LEADERSHIP_DEVELOPMENT("Leadership Development"),
        PROJECT_MANAGEMENT("Project Management"),
        COMMUNICATION_SKILLS("Communication Skills"),
        PROBLEM_SOLVING("Problem Solving"),
        TEAMWORK("Teamwork"),
        PRESENTATION_SKILLS("Presentation Skills"),
        CAREER_DEVELOPMENT("Career Development"),
        COMPETITION_PREPARATION("Competition Preparation"),
        GENERAL_GUIDANCE("General Guidance");
        
        private final String displayName;
        
        MentorshipFocus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum CommunicationFrequency {
        DAILY("Daily"),
        TWICE_WEEKLY("Twice Weekly"),
        WEEKLY("Weekly"),
        BI_WEEKLY("Bi-Weekly"),
        MONTHLY("Monthly"),
        AS_NEEDED("As Needed"),
        PROJECT_BASED("Project Based");
        
        private final String displayName;
        
        CommunicationFrequency(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================
    
    public MentorshipRelationship() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.startDate = LocalDate.now();
    }
    
    public MentorshipRelationship(TeamMember mentor, TeamMember mentee, Integer teamNumber, Integer season) {
        this();
        this.mentor = mentor;
        this.mentee = mentee;
        this.teamNumber = teamNumber;
        this.season = season;
        this.status = MentorshipStatus.PROPOSED;
        this.relationshipType = RelationshipType.MENTOR_STUDENT;
    }
    
    // =========================================================================
    // BUSINESS METHODS
    // =========================================================================
    
    /**
     * Records a completed meeting.
     */
    public void recordMeeting(int durationMinutes, String notes) {
        this.totalMeetings++;
        this.completedMeetings++;
        this.lastMeetingDate = LocalDateTime.now();
        updateMeetingAttendanceRate();
        updateEngagementMetrics();
        touch();
    }
    
    /**
     * Records a cancelled meeting.
     */
    public void recordCancelledMeeting(String reason) {
        this.totalMeetings++;
        this.cancelledMeetings++;
        updateMeetingAttendanceRate();
        updateRiskScore();
        touch();
    }
    
    /**
     * Updates meeting attendance rate.
     */
    public void updateMeetingAttendanceRate() {
        if (this.totalMeetings > 0) {
            this.meetingAttendanceRate = (double) this.completedMeetings / this.totalMeetings * 100.0;
        }
    }
    
    /**
     * Adds a goal to the mentorship relationship.
     */
    public void addGoal(String goal) {
        if (!this.goals.contains(goal)) {
            this.goals.add(goal);
            updateGoalCompletionRate();
            touch();
        }
    }
    
    /**
     * Marks a goal as completed.
     */
    public void completeGoal(String goal) {
        if (this.goals.contains(goal) && !this.completedGoals.contains(goal)) {
            this.completedGoals.add(goal);
            updateGoalCompletionRate();
            updateOverallProgress();
            touch();
        }
    }
    
    /**
     * Updates goal completion rate.
     */
    public void updateGoalCompletionRate() {
        if (!this.goals.isEmpty()) {
            this.goalCompletionRate = (double) this.completedGoals.size() / this.goals.size() * 100.0;
        }
    }
    
    /**
     * Updates overall progress percentage.
     */
    public void updateOverallProgress() {
        double progress = 0.0;
        
        // Goal completion (40%)
        progress += this.goalCompletionRate * 0.4;
        
        // Meeting attendance (30%)
        progress += this.meetingAttendanceRate * 0.3;
        
        // Skills development (20%)
        if (!this.skillsDevelopment.isEmpty()) {
            double skillsProgress = (double) this.skillsLearned.size() / this.skillsDevelopment.size() * 100.0;
            progress += skillsProgress * 0.2;
        }
        
        // Milestone completion (10%)
        if (!this.milestones.isEmpty()) {
            double milestoneProgress = (double) this.currentMilestoneIndex / this.milestones.size() * 100.0;
            progress += milestoneProgress * 0.1;
        }
        
        this.overallProgressPercentage = Math.min(100.0, progress);
    }
    
    /**
     * Adds feedback to the relationship.
     */
    public void addFeedback(String feedback, boolean fromMentor, double rating) {
        if (fromMentor) {
            this.mentorFeedback = feedback;
            this.menteeRating = rating;
        } else {
            this.menteeFeedback = feedback;
            this.mentorRating = rating;
        }
        
        this.feedbackCount++;
        this.lastFeedbackDate = LocalDateTime.now();
        updateRelationshipSatisfaction();
        touch();
    }
    
    /**
     * Updates relationship satisfaction based on ratings.
     */
    public void updateRelationshipSatisfaction() {
        if (this.mentorRating > 0 && this.menteeRating > 0) {
            this.relationshipSatisfaction = (this.mentorRating + this.menteeRating) / 2.0;
        }
    }
    
    /**
     * Records skill learning.
     */
    public void recordSkillLearned(String skill) {
        if (!this.skillsLearned.contains(skill)) {
            this.skillsLearned.add(skill);
            updateSkillGrowthRate();
            updateOverallProgress();
            touch();
        }
    }
    
    /**
     * Updates skill growth rate.
     */
    public void updateSkillGrowthRate() {
        if (this.startDate != null) {
            long daysSinceStart = java.time.temporal.ChronoUnit.DAYS.between(this.startDate, LocalDate.now());
            if (daysSinceStart > 0) {
                this.skillGrowthRate = (double) this.skillsLearned.size() / daysSinceStart * 7.0; // Skills per week
            }
        }
    }
    
    /**
     * Updates engagement metrics.
     */
    public void updateEngagementMetrics() {
        double engagement = 0.0;
        
        // Meeting attendance (40%)
        engagement += this.meetingAttendanceRate * 0.4;
        
        // Communication frequency (30%)
        if (this.lastContactDate != null) {
            long daysSinceContact = java.time.temporal.ChronoUnit.DAYS.between(
                this.lastContactDate.toLocalDate(), LocalDate.now());
            double communicationScore = Math.max(0, 100 - daysSinceContact * 10);
            engagement += communicationScore * 0.3;
        }
        
        // Goal progress (20%)
        engagement += this.goalCompletionRate * 0.2;
        
        // Activity level (10%)
        double activityScore = Math.min(100, (this.tasksCompleted + this.projectsWorkedOn) * 10);
        engagement += activityScore * 0.1;
        
        this.engagementLevel = Math.min(100.0, engagement);
    }
    
    /**
     * Updates risk score based on various factors.
     */
    public void updateRiskScore() {
        double risk = 0.0;
        this.warningFlags = 0;
        List<String> risks = new ArrayList<>();
        
        // Low attendance risk
        if (this.meetingAttendanceRate < 70.0) {
            risk += 20.0;
            this.warningFlags++;
            risks.add("Low meeting attendance");
        }
        
        // Communication gaps
        if (this.lastContactDate != null) {
            long daysSinceContact = java.time.temporal.ChronoUnit.DAYS.between(
                this.lastContactDate.toLocalDate(), LocalDate.now());
            if (daysSinceContact > 7) {
                risk += 15.0;
                this.warningFlags++;
                risks.add("Communication gap");
            }
        }
        
        // Low progress
        if (this.overallProgressPercentage < 30.0 && this.startDate.isBefore(LocalDate.now().minusWeeks(4))) {
            risk += 25.0;
            this.warningFlags++;
            risks.add("Low progress");
        }
        
        // Low satisfaction
        if (this.relationshipSatisfaction < 3.0 && this.relationshipSatisfaction > 0) {
            risk += 20.0;
            this.warningFlags++;
            risks.add("Low satisfaction ratings");
        }
        
        // High cancellation rate
        if (this.totalMeetings > 0 && (double) this.cancelledMeetings / this.totalMeetings > 0.3) {
            risk += 15.0;
            this.warningFlags++;
            risks.add("High meeting cancellation rate");
        }
        
        this.riskScore = Math.min(100.0, risk);
        this.riskFactors = String.join(", ", risks);
        this.needsIntervention = this.riskScore > 60.0;
    }
    
    /**
     * Calculates success probability.
     */
    public void updateSuccessProbability() {
        double probability = 100.0;
        
        // Reduce based on risk factors
        probability -= this.riskScore;
        
        // Boost based on positive factors
        if (this.meetingAttendanceRate > 80.0) probability += 10.0;
        if (this.goalCompletionRate > 70.0) probability += 15.0;
        if (this.relationshipSatisfaction > 4.0) probability += 10.0;
        if (this.engagementLevel > 75.0) probability += 10.0;
        
        this.successProbability = Math.max(0.0, Math.min(100.0, probability));
        this.isHighPerforming = this.successProbability > 85.0;
    }
    
    /**
     * Determines if relationship has achieved primary goals.
     */
    public boolean hasAchievedPrimaryGoals() {
        return this.goalCompletionRate >= 80.0 && this.overallProgressPercentage >= 85.0;
    }
    
    /**
     * Checks if mentee is ready for advancement.
     */
    public boolean isReadyForAdvancement() {
        return hasAchievedPrimaryGoals() && 
               this.menteeSkillImprovement >= 70.0 && 
               this.relationshipSatisfaction >= 4.0;
    }
    
    /**
     * Checks if mentee is eligible to become a mentor.
     */
    public boolean isEligibleForMentorRole() {
        return isReadyForAdvancement() && 
               this.leadershipDevelopment >= 70.0 && 
               this.skillsLearned.size() >= 5;
    }
    
    /**
     * Archives the mentorship relationship.
     */
    public void archive(String reason) {
        this.isArchived = true;
        this.isActive = false;
        this.archivedAt = LocalDateTime.now();
        this.archiveReason = reason;
        this.status = MentorshipStatus.COMPLETED;
    }
    
    /**
     * Restores archived relationship.
     */
    public void restore() {
        this.isArchived = false;
        this.isActive = true;
        this.archivedAt = null;
        this.archiveReason = null;
        this.status = MentorshipStatus.ACTIVE;
    }
    
    /**
     * Updates the timestamp.
     */
    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Gets formatted duration string.
     */
    public String getFormattedDuration() {
        if (this.actualDurationWeeks != null) {
            return this.actualDurationWeeks + " weeks";
        } else if (this.startDate != null) {
            long weeks = java.time.temporal.ChronoUnit.WEEKS.between(this.startDate, LocalDate.now());
            return weeks + " weeks (ongoing)";
        }
        return "Unknown";
    }
    
    /**
     * Gets status summary.
     */
    public String getStatusSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(this.status.getDisplayName());
        
        if (this.needsIntervention) {
            summary.append(" - Needs Intervention");
        } else if (this.isHighPerforming) {
            summary.append(" - High Performing");
        }
        
        if (this.warningFlags > 0) {
            summary.append(" (").append(this.warningFlags).append(" warnings)");
        }
        
        return summary.toString();
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
    
    public TeamMember getMentor() { return mentor; }
    public void setMentor(TeamMember mentor) { this.mentor = mentor; }
    
    public TeamMember getMentee() { return mentee; }
    public void setMentee(TeamMember mentee) { this.mentee = mentee; }
    
    public RelationshipType getRelationshipType() { return relationshipType; }
    public void setRelationshipType(RelationshipType relationshipType) { this.relationshipType = relationshipType; }
    
    public MentorshipStatus getStatus() { return status; }
    public void setStatus(MentorshipStatus status) { this.status = status; }
    
    public MentorshipFocus getPrimaryFocus() { return primaryFocus; }
    public void setPrimaryFocus(MentorshipFocus primaryFocus) { this.primaryFocus = primaryFocus; }
    
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    
    public LocalDate getExpectedEndDate() { return expectedEndDate; }
    public void setExpectedEndDate(LocalDate expectedEndDate) { this.expectedEndDate = expectedEndDate; }
    
    public String getRelationshipDescription() { return relationshipDescription; }
    public void setRelationshipDescription(String relationshipDescription) { this.relationshipDescription = relationshipDescription; }
    
    public String getMentorshipGoals() { return mentorshipGoals; }
    public void setMentorshipGoals(String mentorshipGoals) { this.mentorshipGoals = mentorshipGoals; }
    
    public List<String> getFocusAreas() { return focusAreas; }
    public void setFocusAreas(List<String> focusAreas) { this.focusAreas = focusAreas; }
    
    public List<String> getSkillsDevelopment() { return skillsDevelopment; }
    public void setSkillsDevelopment(List<String> skillsDevelopment) { this.skillsDevelopment = skillsDevelopment; }
    
    public Integer getExpectedDurationWeeks() { return expectedDurationWeeks; }
    public void setExpectedDurationWeeks(Integer expectedDurationWeeks) { this.expectedDurationWeeks = expectedDurationWeeks; }
    
    public Integer getActualDurationWeeks() { return actualDurationWeeks; }
    public void setActualDurationWeeks(Integer actualDurationWeeks) { this.actualDurationWeeks = actualDurationWeeks; }
    
    public Integer getTotalMeetings() { return totalMeetings; }
    public void setTotalMeetings(Integer totalMeetings) { this.totalMeetings = totalMeetings; }
    
    public Integer getCompletedMeetings() { return completedMeetings; }
    public void setCompletedMeetings(Integer completedMeetings) { this.completedMeetings = completedMeetings; }
    
    public Integer getCancelledMeetings() { return cancelledMeetings; }
    public void setCancelledMeetings(Integer cancelledMeetings) { this.cancelledMeetings = cancelledMeetings; }
    
    public Integer getPlannedMeetingsPerWeek() { return plannedMeetingsPerWeek; }
    public void setPlannedMeetingsPerWeek(Integer plannedMeetingsPerWeek) { this.plannedMeetingsPerWeek = plannedMeetingsPerWeek; }
    
    public Integer getAverageMeetingDurationMinutes() { return averageMeetingDurationMinutes; }
    public void setAverageMeetingDurationMinutes(Integer averageMeetingDurationMinutes) { this.averageMeetingDurationMinutes = averageMeetingDurationMinutes; }
    
    public LocalDateTime getLastMeetingDate() { return lastMeetingDate; }
    public void setLastMeetingDate(LocalDateTime lastMeetingDate) { this.lastMeetingDate = lastMeetingDate; }
    
    public LocalDateTime getNextScheduledMeeting() { return nextScheduledMeeting; }
    public void setNextScheduledMeeting(LocalDateTime nextScheduledMeeting) { this.nextScheduledMeeting = nextScheduledMeeting; }
    
    public Double getMeetingAttendanceRate() { return meetingAttendanceRate; }
    public void setMeetingAttendanceRate(Double meetingAttendanceRate) { this.meetingAttendanceRate = meetingAttendanceRate; }
    
    public List<String> getGoals() { return goals; }
    public void setGoals(List<String> goals) { this.goals = goals; }
    
    public List<String> getMilestones() { return milestones; }
    public void setMilestones(List<String> milestones) { this.milestones = milestones; }
    
    public List<String> getCompletedGoals() { return completedGoals; }
    public void setCompletedGoals(List<String> completedGoals) { this.completedGoals = completedGoals; }
    
    public Double getOverallProgressPercentage() { return overallProgressPercentage; }
    public void setOverallProgressPercentage(Double overallProgressPercentage) { this.overallProgressPercentage = overallProgressPercentage; }
    
    public Double getGoalCompletionRate() { return goalCompletionRate; }
    public void setGoalCompletionRate(Double goalCompletionRate) { this.goalCompletionRate = goalCompletionRate; }
    
    public Integer getCurrentMilestoneIndex() { return currentMilestoneIndex; }
    public void setCurrentMilestoneIndex(Integer currentMilestoneIndex) { this.currentMilestoneIndex = currentMilestoneIndex; }
    
    public String getProgressNotes() { return progressNotes; }
    public void setProgressNotes(String progressNotes) { this.progressNotes = progressNotes; }
    
    public String getChallengesEncountered() { return challengesEncountered; }
    public void setChallengesEncountered(String challengesEncountered) { this.challengesEncountered = challengesEncountered; }
    
    public String getSuccessStories() { return successStories; }
    public void setSuccessStories(String successStories) { this.successStories = successStories; }
    
    public Double getMentorRating() { return mentorRating; }
    public void setMentorRating(Double mentorRating) { this.mentorRating = mentorRating; }
    
    public Double getMenteeRating() { return menteeRating; }
    public void setMenteeRating(Double menteeRating) { this.menteeRating = menteeRating; }
    
    public Double getRelationshipSatisfaction() { return relationshipSatisfaction; }
    public void setRelationshipSatisfaction(Double relationshipSatisfaction) { this.relationshipSatisfaction = relationshipSatisfaction; }
    
    public Double getLearningEffectiveness() { return learningEffectiveness; }
    public void setLearningEffectiveness(Double learningEffectiveness) { this.learningEffectiveness = learningEffectiveness; }
    
    public Double getCommunicationQuality() { return communicationQuality; }
    public void setCommunicationQuality(Double communicationQuality) { this.communicationQuality = communicationQuality; }
    
    public Integer getFeedbackCount() { return feedbackCount; }
    public void setFeedbackCount(Integer feedbackCount) { this.feedbackCount = feedbackCount; }
    
    public LocalDateTime getLastFeedbackDate() { return lastFeedbackDate; }
    public void setLastFeedbackDate(LocalDateTime lastFeedbackDate) { this.lastFeedbackDate = lastFeedbackDate; }
    
    public String getMentorFeedback() { return mentorFeedback; }
    public void setMentorFeedback(String mentorFeedback) { this.mentorFeedback = mentorFeedback; }
    
    public String getMenteeFeedback() { return menteeFeedback; }
    public void setMenteeFeedback(String menteeFeedback) { this.menteeFeedback = menteeFeedback; }
    
    public String getImprovementSuggestions() { return improvementSuggestions; }
    public void setImprovementSuggestions(String improvementSuggestions) { this.improvementSuggestions = improvementSuggestions; }
    
    public List<String> getSkillsLearned() { return skillsLearned; }
    public void setSkillsLearned(List<String> skillsLearned) { this.skillsLearned = skillsLearned; }
    
    public List<String> getCompetenciesGained() { return competenciesGained; }
    public void setCompetenciesGained(List<String> competenciesGained) { this.competenciesGained = competenciesGained; }
    
    public List<String> getCertificationsEarned() { return certificationsEarned; }
    public void setCertificationsEarned(List<String> certificationsEarned) { this.certificationsEarned = certificationsEarned; }
    
    public Integer getProjectsWorkedOn() { return projectsWorkedOn; }
    public void setProjectsWorkedOn(Integer projectsWorkedOn) { this.projectsWorkedOn = projectsWorkedOn; }
    
    public Integer getTasksCompleted() { return tasksCompleted; }
    public void setTasksCompleted(Integer tasksCompleted) { this.tasksCompleted = tasksCompleted; }
    
    public Integer getVideoTutorialsWatched() { return videoTutorialsWatched; }
    public void setVideoTutorialsWatched(Integer videoTutorialsWatched) { this.videoTutorialsWatched = videoTutorialsWatched; }
    
    public Integer getDocumentsCreated() { return documentsCreated; }
    public void setDocumentsCreated(Integer documentsCreated) { this.documentsCreated = documentsCreated; }
    
    public Double getSkillGrowthRate() { return skillGrowthRate; }
    public void setSkillGrowthRate(Double skillGrowthRate) { this.skillGrowthRate = skillGrowthRate; }
    
    public Double getKnowledgeTransferEffectiveness() { return knowledgeTransferEffectiveness; }
    public void setKnowledgeTransferEffectiveness(Double knowledgeTransferEffectiveness) { this.knowledgeTransferEffectiveness = knowledgeTransferEffectiveness; }
    
    public CommunicationFrequency getCommunicationFrequency() { return communicationFrequency; }
    public void setCommunicationFrequency(CommunicationFrequency communicationFrequency) { this.communicationFrequency = communicationFrequency; }
    
    public List<String> getPreferredCommunicationMethods() { return preferredCommunicationMethods; }
    public void setPreferredCommunicationMethods(List<String> preferredCommunicationMethods) { this.preferredCommunicationMethods = preferredCommunicationMethods; }
    
    public Integer getTotalCommunications() { return totalCommunications; }
    public void setTotalCommunications(Integer totalCommunications) { this.totalCommunications = totalCommunications; }
    
    public Double getResponseTimeHours() { return responseTimeHours; }
    public void setResponseTimeHours(Double responseTimeHours) { this.responseTimeHours = responseTimeHours; }
    
    public Double getEngagementLevel() { return engagementLevel; }
    public void setEngagementLevel(Double engagementLevel) { this.engagementLevel = engagementLevel; }
    
    public Boolean getIsRemoteMentorship() { return isRemoteMentorship; }
    public void setIsRemoteMentorship(Boolean isRemoteMentorship) { this.isRemoteMentorship = isRemoteMentorship; }
    
    public Boolean getRequiresParentalConsent() { return requiresParentalConsent; }
    public void setRequiresParentalConsent(Boolean requiresParentalConsent) { this.requiresParentalConsent = requiresParentalConsent; }
    
    public LocalDateTime getLastContactDate() { return lastContactDate; }
    public void setLastContactDate(LocalDateTime lastContactDate) { this.lastContactDate = lastContactDate; }
    
    public List<String> getResourcesUsed() { return resourcesUsed; }
    public void setResourcesUsed(List<String> resourcesUsed) { this.resourcesUsed = resourcesUsed; }
    
    public List<String> getToolsUsed() { return toolsUsed; }
    public void setToolsUsed(List<String> toolsUsed) { this.toolsUsed = toolsUsed; }
    
    public Boolean getHasStructuredPlan() { return hasStructuredPlan; }
    public void setHasStructuredPlan(Boolean hasStructuredPlan) { this.hasStructuredPlan = hasStructuredPlan; }
    
    public Boolean getFollowsTeamGuidelines() { return followsTeamGuidelines; }
    public void setFollowsTeamGuidelines(Boolean followsTeamGuidelines) { this.followsTeamGuidelines = followsTeamGuidelines; }
    
    public Boolean getNeedsAdditionalSupport() { return needsAdditionalSupport; }
    public void setNeedsAdditionalSupport(Boolean needsAdditionalSupport) { this.needsAdditionalSupport = needsAdditionalSupport; }
    
    public String getSupportNeeded() { return supportNeeded; }
    public void setSupportNeeded(String supportNeeded) { this.supportNeeded = supportNeeded; }
    
    public String getResourcesNeeded() { return resourcesNeeded; }
    public void setResourcesNeeded(String resourcesNeeded) { this.resourcesNeeded = resourcesNeeded; }
    
    public Double getMenteeSkillImprovement() { return menteeSkillImprovement; }
    public void setMenteeSkillImprovement(Double menteeSkillImprovement) { this.menteeSkillImprovement = menteeSkillImprovement; }
    
    public Double getMenteeConfidenceIncrease() { return menteeConfidenceIncrease; }
    public void setMenteeConfidenceIncrease(Double menteeConfidenceIncrease) { this.menteeConfidenceIncrease = menteeConfidenceIncrease; }
    
    public Double getTeamContributionIncrease() { return teamContributionIncrease; }
    public void setTeamContributionIncrease(Double teamContributionIncrease) { this.teamContributionIncrease = teamContributionIncrease; }
    
    public Double getLeadershipDevelopment() { return leadershipDevelopment; }
    public void setLeadershipDevelopment(Double leadershipDevelopment) { this.leadershipDevelopment = leadershipDevelopment; }
    
    public Boolean getAchievedPrimaryGoals() { return achievedPrimaryGoals; }
    public void setAchievedPrimaryGoals(Boolean achievedPrimaryGoals) { this.achievedPrimaryGoals = achievedPrimaryGoals; }
    
    public Boolean getExceededExpectations() { return exceededExpectations; }
    public void setExceededExpectations(Boolean exceededExpectations) { this.exceededExpectations = exceededExpectations; }
    
    public Boolean getRecommendedForAdvancement() { return recommendedForAdvancement; }
    public void setRecommendedForAdvancement(Boolean recommendedForAdvancement) { this.recommendedForAdvancement = recommendedForAdvancement; }
    
    public Boolean getEligibleForMentorRole() { return eligibleForMentorRole; }
    public void setEligibleForMentorRole(Boolean eligibleForMentorRole) { this.eligibleForMentorRole = eligibleForMentorRole; }
    
    public String getImpactAssessment() { return impactAssessment; }
    public void setImpactAssessment(String impactAssessment) { this.impactAssessment = impactAssessment; }
    
    public String getFutureDevelopmentPlan() { return futureDevelopmentPlan; }
    public void setFutureDevelopmentPlan(String futureDevelopmentPlan) { this.futureDevelopmentPlan = futureDevelopmentPlan; }
    
    public TeamMember getAssignedBy() { return assignedBy; }
    public void setAssignedBy(TeamMember assignedBy) { this.assignedBy = assignedBy; }
    
    public TeamMember getSupervisor() { return supervisor; }
    public void setSupervisor(TeamMember supervisor) { this.supervisor = supervisor; }
    
    public Boolean getRequiresApproval() { return requiresApproval; }
    public void setRequiresApproval(Boolean requiresApproval) { this.requiresApproval = requiresApproval; }
    
    public Boolean getIsApproved() { return isApproved; }
    public void setIsApproved(Boolean isApproved) { this.isApproved = isApproved; }
    
    public TeamMember getApprovedBy() { return approvedBy; }
    public void setApprovedBy(TeamMember approvedBy) { this.approvedBy = approvedBy; }
    
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    
    public Boolean getIsDocumented() { return isDocumented; }
    public void setIsDocumented(Boolean isDocumented) { this.isDocumented = isDocumented; }
    
    public Boolean getComplianceChecked() { return complianceChecked; }
    public void setComplianceChecked(Boolean complianceChecked) { this.complianceChecked = complianceChecked; }
    
    public LocalDateTime getLastComplianceCheck() { return lastComplianceCheck; }
    public void setLastComplianceCheck(LocalDateTime lastComplianceCheck) { this.lastComplianceCheck = lastComplianceCheck; }
    
    public Double getSuccessProbability() { return successProbability; }
    public void setSuccessProbability(Double successProbability) { this.successProbability = successProbability; }
    
    public Double getRiskScore() { return riskScore; }
    public void setRiskScore(Double riskScore) { this.riskScore = riskScore; }
    
    public Integer getWarningFlags() { return warningFlags; }
    public void setWarningFlags(Integer warningFlags) { this.warningFlags = warningFlags; }
    
    public String getRiskFactors() { return riskFactors; }
    public void setRiskFactors(String riskFactors) { this.riskFactors = riskFactors; }
    
    public String getSuccessFactors() { return successFactors; }
    public void setSuccessFactors(String successFactors) { this.successFactors = successFactors; }
    
    public Boolean getIsHighPerforming() { return isHighPerforming; }
    public void setIsHighPerforming(Boolean isHighPerforming) { this.isHighPerforming = isHighPerforming; }
    
    public Boolean getNeedsIntervention() { return needsIntervention; }
    public void setNeedsIntervention(Boolean needsIntervention) { this.needsIntervention = needsIntervention; }
    
    public LocalDateTime getLastAnalysisDate() { return lastAnalysisDate; }
    public void setLastAnalysisDate(LocalDateTime lastAnalysisDate) { this.lastAnalysisDate = lastAnalysisDate; }
    
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
    
    public TeamMember getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(TeamMember updatedBy) { this.updatedBy = updatedBy; }
}