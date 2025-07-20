// src/main/java/org/frcpm/models/SkillDevelopmentRecord.java

package org.frcpm.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Skill Development Record model for FRC team skills tracking.
 * 
 * Provides comprehensive skills development tracking including individual
 * competency assessment, learning paths, certification tracking, and
 * team-wide skills analytics for effective FRC team development.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-KnowledgeManagement
 * @since Phase 4D.5 Skills Development Tracking
 */
@Entity
@Table(name = "skill_development_records")
public class SkillDevelopmentRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Integer teamNumber;
    
    @Column(nullable = false)
    private Integer season;
    
    // Team Member and Skill Information
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_member_id", nullable = false)
    private TeamMember teamMember;
    
    @Column(nullable = false, length = 100)
    private String skillName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SkillCategory category;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SkillType skillType;
    
    @Column(length = 1000)
    private String skillDescription;
    
    // Competency Assessment
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompetencyLevel currentLevel;
    
    @Enumerated(EnumType.STRING)
    private CompetencyLevel targetLevel;
    
    @Enumerated(EnumType.STRING)
    private CompetencyLevel previousLevel;
    
    @Column
    private Double competencyScore = 0.0; // 0-100 score
    
    @Column
    private Double progressPercentage = 0.0; // Progress toward target level
    
    @Column
    private Integer assessmentCount = 0;
    
    @Column
    private LocalDate lastAssessmentDate;
    
    @Column
    private LocalDate nextAssessmentDate;
    
    // Learning and Development
    @Enumerated(EnumType.STRING)
    private LearningStatus learningStatus;
    
    @Column
    private LocalDate learningStartDate;
    
    @Column
    private LocalDate learningCompletionDate;
    
    @Column
    private Integer estimatedLearningHours;
    
    @Column
    private Integer actualLearningHours = 0;
    
    @Column
    private Double learningEfficiency = 0.0; // Actual vs estimated hours
    
    @ElementCollection
    @CollectionTable(name = "skill_learning_resources")
    private List<String> learningResources = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "skill_learning_methods")
    private List<String> learningMethods = new ArrayList<>();
    
    // Mentorship and Instruction
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id")
    private TeamMember mentor;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id")
    private TeamMember instructor;
    
    @Column
    private Integer mentorshipHours = 0;
    
    @Column
    private Integer instructionHours = 0;
    
    @Column
    private Double mentorRating; // 1-5 rating of mentor effectiveness
    
    @Column
    private Double instructionQuality; // 1-5 rating of instruction quality
    
    // Practice and Application
    @Column
    private Integer practiceHours = 0;
    
    @Column
    private Integer applicationCount = 0; // Times skill was applied
    
    @Column
    private LocalDate lastPracticeDate;
    
    @Column
    private LocalDate lastApplicationDate;
    
    @Column
    private Double practiceEffectiveness = 0.0; // Improvement per practice hour
    
    @ElementCollection
    @CollectionTable(name = "skill_practice_activities")
    private List<String> practiceActivities = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "skill_application_contexts")
    private List<String> applicationContexts = new ArrayList<>();
    
    // Assessment and Evaluation
    @Column
    private Double selfAssessmentScore = 0.0; // 1-5 self-rating
    
    @Column
    private Double peerAssessmentScore = 0.0; // 1-5 peer rating
    
    @Column
    private Double mentorAssessmentScore = 0.0; // 1-5 mentor rating
    
    @Column
    private Double overallAssessmentScore = 0.0; // Composite score
    
    @ElementCollection
    @CollectionTable(name = "skill_assessment_evidence")
    private List<String> assessmentEvidence = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "skill_assessment_feedback")
    private List<String> assessmentFeedback = new ArrayList<>();
    
    // Certification and Recognition
    @Column
    private Boolean isCertified = false;
    
    @Column(length = 200)
    private String certificationType;
    
    @Column
    private LocalDate certificationDate;
    
    @Column
    private LocalDate certificationExpiry;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certified_by_id")
    private TeamMember certifiedBy;
    
    @Column
    private Boolean isRecognized = false; // Team recognition for skill
    
    @Column(length = 200)
    private String recognitionType;
    
    @Column
    private LocalDate recognitionDate;
    
    // Prerequisites and Dependencies
    @ElementCollection
    @CollectionTable(name = "skill_prerequisites")
    private List<String> prerequisites = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "skill_enables")
    private List<String> enables = new ArrayList<>(); // Skills this enables
    
    @Column
    private Boolean prerequisitesMet = false;
    
    @Column
    private Integer dependentSkillsCount = 0; // Skills that depend on this one
    
    // Goals and Milestones
    @ElementCollection
    @CollectionTable(name = "skill_development_goals")
    private List<String> developmentGoals = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "skill_milestones")
    private List<String> milestones = new ArrayList<>();
    
    @Column
    private Integer completedMilestones = 0;
    
    @Column
    private Integer totalMilestones = 0;
    
    @Column
    private LocalDate targetCompletionDate;
    
    @Column
    private LocalDate actualCompletionDate;
    
    // Performance and Impact
    @Column
    private Double performanceImpact = 0.0; // Impact on team performance
    
    @Column
    private Double skillUtilization = 0.0; // How often skill is used
    
    @Column
    private Double skillImportance = 0.0; // Importance to team success
    
    @Column
    private Integer projectsApplied = 0; // Projects where skill was applied
    
    @Column
    private Integer tasksCompleted = 0; // Tasks completed using this skill
    
    @ElementCollection
    @CollectionTable(name = "skill_performance_metrics")
    private List<String> performanceMetrics = new ArrayList<>();
    
    // Learning Path and Progression
    @Column(length = 200)
    private String learningPath;
    
    @Column
    private Integer pathOrder; // Order within learning path
    
    @Column
    private Boolean isPathCompleted = false;
    
    @Column
    private Double pathProgress = 0.0; // Progress through learning path
    
    @Column(length = 200)
    private String nextSkillInPath;
    
    @Column(length = 200)
    private String previousSkillInPath;
    
    // Difficulty and Challenge
    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficulty;
    
    @Column
    private Double learningCurve = 0.0; // How steep the learning curve is
    
    @Column
    private Integer strugglingDays = 0; // Days spent struggling with skill
    
    @Column
    private Integer breakthroughMoments = 0; // Major progress moments
    
    @ElementCollection
    @CollectionTable(name = "skill_challenges")
    private List<String> challenges = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "skill_breakthroughs")
    private List<String> breakthroughs = new ArrayList<>();
    
    // Team Context and Collaboration
    @Column
    private Boolean isTeamCritical = false; // Critical for team success
    
    @Column
    private Boolean isLeadershipSkill = false; // Leadership-related skill
    
    @Column
    private Boolean isSpecialistSkill = false; // Specialist/advanced skill
    
    @Column
    private Integer teamMembersWithSkill = 0; // Others with same skill
    
    @Column
    private Integer teachingOpportunities = 0; // Times taught others
    
    @ElementCollection
    @CollectionTable(name = "skill_team_roles")
    private List<String> teamRoles = new ArrayList<>(); // Roles requiring this skill
    
    // Competition and Events
    @Column
    private Boolean isCompetitionRelevant = false;
    
    @Column
    private Integer competitionsUsed = 0; // Competitions where skill was used
    
    @Column
    private Double competitionPerformance = 0.0; // Performance in competition
    
    @ElementCollection
    @CollectionTable(name = "skill_competition_contexts")
    private List<String> competitionContexts = new ArrayList<>();
    
    @Column
    private Boolean readyForCompetition = false;
    
    // Documentation and Evidence
    @ElementCollection
    @CollectionTable(name = "skill_documentation")
    private List<String> documentation = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "skill_portfolio_items")
    private List<String> portfolioItems = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "skill_photo_evidence")
    private List<String> photoEvidence = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "skill_video_evidence")
    private List<String> videoEvidence = new ArrayList<>();
    
    // Analytics and Trends
    @Column
    private Double improvementRate = 0.0; // Rate of skill improvement
    
    @Column
    private Double consistencyScore = 0.0; // Consistency of performance
    
    @Column
    private Double retentionRate = 0.0; // Skill retention over time
    
    @Column
    private Integer plateauPeriods = 0; // Times progress plateaued
    
    @Column
    private Integer regressionPeriods = 0; // Times skill regressed
    
    @Column
    private LocalDate lastImprovementDate;
    
    // Feedback and Reviews
    @Column
    private Integer feedbackCount = 0;
    
    @Column
    private Double averageFeedbackRating = 0.0;
    
    @Column
    private LocalDate lastFeedbackDate;
    
    @ElementCollection
    @CollectionTable(name = "skill_improvement_suggestions")
    private List<String> improvementSuggestions = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "skill_strength_areas")
    private List<String> strengthAreas = new ArrayList<>();
    
    // Risk and Challenges
    @Column
    private Double riskScore = 0.0; // Risk of not achieving target level
    
    @Column
    private Boolean requiresIntervention = false;
    
    @Column(length = 500)
    private String interventionPlan;
    
    @Column
    private Boolean isAtRisk = false; // At risk of not meeting goals
    
    @ElementCollection
    @CollectionTable(name = "skill_risk_factors")
    private List<String> riskFactors = new ArrayList<>();
    
    // Audit and Tracking
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private TeamMember createdBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_id")
    private TeamMember updatedBy;
    
    @Column
    private Boolean isActive = true;
    
    @Column
    private Boolean isArchived = false;
    
    @Column
    private LocalDateTime archivedAt;
    
    @Column(length = 500)
    private String archiveReason;
    
    // Enums
    public enum SkillCategory {
        TECHNICAL("Technical"),
        PROGRAMMING("Programming"),
        DESIGN("Design & CAD"),
        FABRICATION("Fabrication"),
        ELECTRONICS("Electronics"),
        MECHANICAL("Mechanical"),
        SAFETY("Safety"),
        PROJECT_MANAGEMENT("Project Management"),
        LEADERSHIP("Leadership"),
        COMMUNICATION("Communication"),
        PROBLEM_SOLVING("Problem Solving"),
        TEAMWORK("Teamwork"),
        BUSINESS("Business & Marketing"),
        COMPETITION("Competition Strategy"),
        MENTORING("Mentoring & Teaching");
        
        private final String displayName;
        
        SkillCategory(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum SkillType {
        CORE("Core Skill"),
        SPECIALIZED("Specialized"),
        LEADERSHIP("Leadership"),
        SOFT_SKILL("Soft Skill"),
        TECHNICAL("Technical"),
        SAFETY("Safety"),
        COMPETITION("Competition");
        
        private final String displayName;
        
        SkillType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum CompetencyLevel {
        NOVICE("Novice"),
        BEGINNER("Beginner"),
        DEVELOPING("Developing"),
        COMPETENT("Competent"),
        PROFICIENT("Proficient"),
        ADVANCED("Advanced"),
        EXPERT("Expert"),
        MASTER("Master");
        
        private final String displayName;
        
        CompetencyLevel(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public int getNumericValue() {
            return ordinal() + 1;
        }
    }
    
    public enum LearningStatus {
        NOT_STARTED("Not Started"),
        PLANNING("Planning"),
        LEARNING("Learning"),
        PRACTICING("Practicing"),
        APPLYING("Applying"),
        MASTERING("Mastering"),
        COMPLETED("Completed"),
        ON_HOLD("On Hold"),
        NEEDS_HELP("Needs Help");
        
        private final String displayName;
        
        LearningStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum DifficultyLevel {
        VERY_EASY("Very Easy"),
        EASY("Easy"),
        MODERATE("Moderate"),
        CHALLENGING("Challenging"),
        DIFFICULT("Difficult"),
        VERY_DIFFICULT("Very Difficult"),
        EXPERT_LEVEL("Expert Level");
        
        private final String displayName;
        
        DifficultyLevel(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public int getNumericValue() {
            return ordinal() + 1;
        }
    }
    
    // Constructors
    public SkillDevelopmentRecord() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public SkillDevelopmentRecord(Integer teamNumber, Integer season, TeamMember teamMember, 
                                 String skillName, SkillCategory category, SkillType skillType) {
        this();
        this.teamNumber = teamNumber;
        this.season = season;
        this.teamMember = teamMember;
        this.skillName = skillName;
        this.category = category;
        this.skillType = skillType;
        this.currentLevel = CompetencyLevel.NOVICE;
        this.learningStatus = LearningStatus.NOT_STARTED;
    }
    
    // Business Methods
    
    /**
     * Updates competency level and recalculates related metrics.
     */
    public void updateCompetencyLevel(CompetencyLevel newLevel, TeamMember assessor) {
        this.previousLevel = this.currentLevel;
        this.currentLevel = newLevel;
        this.competencyScore = newLevel.getNumericValue() * 12.5; // Convert to 0-100 scale
        this.lastAssessmentDate = LocalDate.now();
        this.assessmentCount++;
        
        // Calculate progress toward target
        if (this.targetLevel != null) {
            double progress = (double) newLevel.getNumericValue() / targetLevel.getNumericValue() * 100.0;
            this.progressPercentage = Math.min(100.0, progress);
        }
        
        // Update improvement metrics
        updateImprovementMetrics();
        
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Records practice session for the skill.
     */
    public void recordPracticeSession(Integer hours, String activity) {
        this.practiceHours += hours;
        this.lastPracticeDate = LocalDate.now();
        
        if (activity != null && !this.practiceActivities.contains(activity)) {
            this.practiceActivities.add(activity);
        }
        
        // Update practice effectiveness
        calculatePracticeEffectiveness();
        
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Records application of the skill in a real context.
     */
    public void recordSkillApplication(String context) {
        this.applicationCount++;
        this.lastApplicationDate = LocalDate.now();
        
        if (context != null && !this.applicationContexts.contains(context)) {
            this.applicationContexts.add(context);
        }
        
        // Update utilization score
        updateUtilizationScore();
        
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Adds learning time and updates efficiency metrics.
     */
    public void addLearningTime(Integer hours, String method) {
        this.actualLearningHours += hours;
        
        if (method != null && !this.learningMethods.contains(method)) {
            this.learningMethods.add(method);
        }
        
        // Calculate learning efficiency
        if (this.estimatedLearningHours != null && this.estimatedLearningHours > 0) {
            this.learningEfficiency = (double) this.estimatedLearningHours / this.actualLearningHours;
        }
        
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Sets target level and calculates initial progress.
     */
    public void setTargetLevel(CompetencyLevel target, LocalDate targetDate) {
        this.targetLevel = target;
        this.targetCompletionDate = targetDate;
        
        // Recalculate progress percentage
        if (this.currentLevel != null) {
            double progress = (double) this.currentLevel.getNumericValue() / target.getNumericValue() * 100.0;
            this.progressPercentage = Math.min(100.0, progress);
        }
        
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Completes a milestone and updates progress.
     */
    public void completeMilestone(String milestone) {
        if (!this.milestones.contains(milestone)) {
            this.milestones.add(milestone);
            this.totalMilestones = this.milestones.size();
        }
        
        this.completedMilestones++;
        
        // Update path progress if in a learning path
        if (this.totalMilestones > 0) {
            this.pathProgress = (double) this.completedMilestones / this.totalMilestones * 100.0;
            
            if (this.pathProgress >= 100.0) {
                this.isPathCompleted = true;
            }
        }
        
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Certifies the skill for the team member.
     */
    public void certifySkill(String certificationType, TeamMember certifier, LocalDate expiryDate) {
        this.isCertified = true;
        this.certificationType = certificationType;
        this.certificationDate = LocalDate.now();
        this.certificationExpiry = expiryDate;
        this.certifiedBy = certifier;
        
        // Update competency if not already at proficient level
        if (this.currentLevel.getNumericValue() < CompetencyLevel.PROFICIENT.getNumericValue()) {
            updateCompetencyLevel(CompetencyLevel.PROFICIENT, certifier);
        }
        
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Records assessment feedback and updates scores.
     */
    public void recordAssessment(Double selfRating, Double peerRating, Double mentorRating, String feedback) {
        if (selfRating != null) {
            this.selfAssessmentScore = selfRating;
        }
        if (peerRating != null) {
            this.peerAssessmentScore = peerRating;
        }
        if (mentorRating != null) {
            this.mentorAssessmentScore = mentorRating;
        }
        
        // Calculate overall assessment score
        calculateOverallAssessment();
        
        if (feedback != null && !feedback.trim().isEmpty()) {
            this.assessmentFeedback.add(feedback);
            this.feedbackCount++;
        }
        
        this.lastFeedbackDate = LocalDate.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Calculates risk score based on various factors.
     */
    public void calculateRiskScore() {
        double risk = 0.0;
        
        // Time-based risk
        if (this.targetCompletionDate != null) {
            long daysRemaining = LocalDate.now().until(this.targetCompletionDate).getDays();
            if (daysRemaining < 7) {
                risk += 30.0;
            } else if (daysRemaining < 30) {
                risk += 15.0;
            }
        }
        
        // Progress-based risk
        if (this.progressPercentage < 25.0) {
            risk += 25.0;
        } else if (this.progressPercentage < 50.0) {
            risk += 15.0;
        }
        
        // Practice-based risk
        if (this.lastPracticeDate == null || 
            LocalDate.now().until(this.lastPracticeDate).getDays() > 14) {
            risk += 20.0;
        }
        
        // Assessment-based risk
        if (this.overallAssessmentScore < 3.0) {
            risk += 25.0;
        }
        
        this.riskScore = Math.min(100.0, risk);
        this.isAtRisk = this.riskScore >= 50.0;
        this.requiresIntervention = this.riskScore >= 70.0;
        
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Updates improvement rate based on competency changes.
     */
    private void updateImprovementMetrics() {
        if (this.previousLevel != null && this.currentLevel != null) {
            int levelImprovement = this.currentLevel.getNumericValue() - this.previousLevel.getNumericValue();
            
            if (levelImprovement > 0) {
                this.lastImprovementDate = LocalDate.now();
                
                // Calculate improvement rate (levels per day)
                if (this.learningStartDate != null) {
                    long daysSinceStart = this.learningStartDate.until(LocalDate.now()).getDays();
                    if (daysSinceStart > 0) {
                        this.improvementRate = (double) this.currentLevel.getNumericValue() / daysSinceStart;
                    }
                }
            } else if (levelImprovement < 0) {
                this.regressionPeriods++;
            }
        }
    }
    
    /**
     * Calculates practice effectiveness.
     */
    private void calculatePracticeEffectiveness() {
        if (this.practiceHours > 0 && this.currentLevel != null) {
            this.practiceEffectiveness = this.currentLevel.getNumericValue() / (double) this.practiceHours;
        }
    }
    
    /**
     * Updates skill utilization score.
     */
    private void updateUtilizationScore() {
        // Calculate utilization based on application frequency
        if (this.learningStartDate != null) {
            long daysSinceStart = this.learningStartDate.until(LocalDate.now()).getDays();
            if (daysSinceStart > 0) {
                this.skillUtilization = (double) this.applicationCount / daysSinceStart * 100.0;
            }
        }
    }
    
    /**
     * Calculates overall assessment score from individual assessments.
     */
    private void calculateOverallAssessment() {
        double total = 0.0;
        int count = 0;
        
        if (this.selfAssessmentScore > 0) {
            total += this.selfAssessmentScore * 0.3; // 30% weight
            count++;
        }
        if (this.peerAssessmentScore > 0) {
            total += this.peerAssessmentScore * 0.3; // 30% weight
            count++;
        }
        if (this.mentorAssessmentScore > 0) {
            total += this.mentorAssessmentScore * 0.4; // 40% weight
            count++;
        }
        
        if (count > 0) {
            this.overallAssessmentScore = total;
        }
        
        // Calculate average feedback rating
        if (this.feedbackCount > 0) {
            this.averageFeedbackRating = this.overallAssessmentScore;
        }
    }
    
    /**
     * Checks if the skill is ready for competition use.
     */
    public boolean isReadyForCompetition() {
        return this.readyForCompetition && 
               this.currentLevel.getNumericValue() >= CompetencyLevel.COMPETENT.getNumericValue() &&
               this.applicationCount >= 3 &&
               this.overallAssessmentScore >= 3.5;
    }
    
    /**
     * Gets display text for current progress.
     */
    public String getProgressDisplayText() {
        if (this.targetLevel != null) {
            return String.format("%s → %s (%.1f%%)", 
                this.currentLevel.getDisplayName(),
                this.targetLevel.getDisplayName(),
                this.progressPercentage);
        }
        return this.currentLevel.getDisplayName();
    }
    
    /**
     * Gets CSS class for competency level styling.
     */
    public String getCompetencyLevelCssClass() {
        switch (this.currentLevel) {
            case EXPERT:
            case MASTER:
                return "competency-expert";
            case ADVANCED:
            case PROFICIENT:
                return "competency-advanced";
            case COMPETENT:
                return "competency-competent";
            case DEVELOPING:
                return "competency-developing";
            default:
                return "competency-novice";
        }
    }
    
    /**
     * Determines if skill needs immediate attention.
     */
    public boolean needsAttention() {
        return this.isAtRisk || 
               this.requiresIntervention ||
               (this.lastPracticeDate != null && 
                LocalDate.now().until(this.lastPracticeDate).getDays() > 21) ||
               this.overallAssessmentScore < 2.5;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Integer getTeamNumber() { return teamNumber; }
    public void setTeamNumber(Integer teamNumber) { this.teamNumber = teamNumber; }
    
    public Integer getSeason() { return season; }
    public void setSeason(Integer season) { this.season = season; }
    
    public TeamMember getTeamMember() { return teamMember; }
    public void setTeamMember(TeamMember teamMember) { this.teamMember = teamMember; }
    
    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }
    
    public SkillCategory getCategory() { return category; }
    public void setCategory(SkillCategory category) { this.category = category; }
    
    public SkillType getSkillType() { return skillType; }
    public void setSkillType(SkillType skillType) { this.skillType = skillType; }
    
    public String getSkillDescription() { return skillDescription; }
    public void setSkillDescription(String skillDescription) { this.skillDescription = skillDescription; }
    
    public CompetencyLevel getCurrentLevel() { return currentLevel; }
    public void setCurrentLevel(CompetencyLevel currentLevel) { this.currentLevel = currentLevel; }
    
    public CompetencyLevel getTargetLevel() { return targetLevel; }
    public void setTargetLevel(CompetencyLevel targetLevel) { this.targetLevel = targetLevel; }
    
    public CompetencyLevel getPreviousLevel() { return previousLevel; }
    public void setPreviousLevel(CompetencyLevel previousLevel) { this.previousLevel = previousLevel; }
    
    public Double getCompetencyScore() { return competencyScore; }
    public void setCompetencyScore(Double competencyScore) { this.competencyScore = competencyScore; }
    
    public Double getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(Double progressPercentage) { this.progressPercentage = progressPercentage; }
    
    public Integer getAssessmentCount() { return assessmentCount; }
    public void setAssessmentCount(Integer assessmentCount) { this.assessmentCount = assessmentCount; }
    
    public LocalDate getLastAssessmentDate() { return lastAssessmentDate; }
    public void setLastAssessmentDate(LocalDate lastAssessmentDate) { this.lastAssessmentDate = lastAssessmentDate; }
    
    public LocalDate getNextAssessmentDate() { return nextAssessmentDate; }
    public void setNextAssessmentDate(LocalDate nextAssessmentDate) { this.nextAssessmentDate = nextAssessmentDate; }
    
    public LearningStatus getLearningStatus() { return learningStatus; }
    public void setLearningStatus(LearningStatus learningStatus) { this.learningStatus = learningStatus; }
    
    public LocalDate getLearningStartDate() { return learningStartDate; }
    public void setLearningStartDate(LocalDate learningStartDate) { this.learningStartDate = learningStartDate; }
    
    public LocalDate getLearningCompletionDate() { return learningCompletionDate; }
    public void setLearningCompletionDate(LocalDate learningCompletionDate) { this.learningCompletionDate = learningCompletionDate; }
    
    public Integer getEstimatedLearningHours() { return estimatedLearningHours; }
    public void setEstimatedLearningHours(Integer estimatedLearningHours) { this.estimatedLearningHours = estimatedLearningHours; }
    
    public Integer getActualLearningHours() { return actualLearningHours; }
    public void setActualLearningHours(Integer actualLearningHours) { this.actualLearningHours = actualLearningHours; }
    
    public Double getLearningEfficiency() { return learningEfficiency; }
    public void setLearningEfficiency(Double learningEfficiency) { this.learningEfficiency = learningEfficiency; }
    
    public List<String> getLearningResources() { return learningResources; }
    public void setLearningResources(List<String> learningResources) { this.learningResources = learningResources; }
    
    public List<String> getLearningMethods() { return learningMethods; }
    public void setLearningMethods(List<String> learningMethods) { this.learningMethods = learningMethods; }
    
    public TeamMember getMentor() { return mentor; }
    public void setMentor(TeamMember mentor) { this.mentor = mentor; }
    
    public TeamMember getInstructor() { return instructor; }
    public void setInstructor(TeamMember instructor) { this.instructor = instructor; }
    
    public Integer getMentorshipHours() { return mentorshipHours; }
    public void setMentorshipHours(Integer mentorshipHours) { this.mentorshipHours = mentorshipHours; }
    
    public Integer getInstructionHours() { return instructionHours; }
    public void setInstructionHours(Integer instructionHours) { this.instructionHours = instructionHours; }
    
    public Double getMentorRating() { return mentorRating; }
    public void setMentorRating(Double mentorRating) { this.mentorRating = mentorRating; }
    
    public Double getInstructionQuality() { return instructionQuality; }
    public void setInstructionQuality(Double instructionQuality) { this.instructionQuality = instructionQuality; }
    
    public Integer getPracticeHours() { return practiceHours; }
    public void setPracticeHours(Integer practiceHours) { this.practiceHours = practiceHours; }
    
    public Integer getApplicationCount() { return applicationCount; }
    public void setApplicationCount(Integer applicationCount) { this.applicationCount = applicationCount; }
    
    public LocalDate getLastPracticeDate() { return lastPracticeDate; }
    public void setLastPracticeDate(LocalDate lastPracticeDate) { this.lastPracticeDate = lastPracticeDate; }
    
    public LocalDate getLastApplicationDate() { return lastApplicationDate; }
    public void setLastApplicationDate(LocalDate lastApplicationDate) { this.lastApplicationDate = lastApplicationDate; }
    
    public Double getPracticeEffectiveness() { return practiceEffectiveness; }
    public void setPracticeEffectiveness(Double practiceEffectiveness) { this.practiceEffectiveness = practiceEffectiveness; }
    
    public List<String> getPracticeActivities() { return practiceActivities; }
    public void setPracticeActivities(List<String> practiceActivities) { this.practiceActivities = practiceActivities; }
    
    public List<String> getApplicationContexts() { return applicationContexts; }
    public void setApplicationContexts(List<String> applicationContexts) { this.applicationContexts = applicationContexts; }
    
    public Double getSelfAssessmentScore() { return selfAssessmentScore; }
    public void setSelfAssessmentScore(Double selfAssessmentScore) { this.selfAssessmentScore = selfAssessmentScore; }
    
    public Double getPeerAssessmentScore() { return peerAssessmentScore; }
    public void setPeerAssessmentScore(Double peerAssessmentScore) { this.peerAssessmentScore = peerAssessmentScore; }
    
    public Double getMentorAssessmentScore() { return mentorAssessmentScore; }
    public void setMentorAssessmentScore(Double mentorAssessmentScore) { this.mentorAssessmentScore = mentorAssessmentScore; }
    
    public Double getOverallAssessmentScore() { return overallAssessmentScore; }
    public void setOverallAssessmentScore(Double overallAssessmentScore) { this.overallAssessmentScore = overallAssessmentScore; }
    
    public List<String> getAssessmentEvidence() { return assessmentEvidence; }
    public void setAssessmentEvidence(List<String> assessmentEvidence) { this.assessmentEvidence = assessmentEvidence; }
    
    public List<String> getAssessmentFeedback() { return assessmentFeedback; }
    public void setAssessmentFeedback(List<String> assessmentFeedback) { this.assessmentFeedback = assessmentFeedback; }
    
    public Boolean getIsCertified() { return isCertified; }
    public void setIsCertified(Boolean isCertified) { this.isCertified = isCertified; }
    
    public String getCertificationType() { return certificationType; }
    public void setCertificationType(String certificationType) { this.certificationType = certificationType; }
    
    public LocalDate getCertificationDate() { return certificationDate; }
    public void setCertificationDate(LocalDate certificationDate) { this.certificationDate = certificationDate; }
    
    public LocalDate getCertificationExpiry() { return certificationExpiry; }
    public void setCertificationExpiry(LocalDate certificationExpiry) { this.certificationExpiry = certificationExpiry; }
    
    public TeamMember getCertifiedBy() { return certifiedBy; }
    public void setCertifiedBy(TeamMember certifiedBy) { this.certifiedBy = certifiedBy; }
    
    public Boolean getIsRecognized() { return isRecognized; }
    public void setIsRecognized(Boolean isRecognized) { this.isRecognized = isRecognized; }
    
    public String getRecognitionType() { return recognitionType; }
    public void setRecognitionType(String recognitionType) { this.recognitionType = recognitionType; }
    
    public LocalDate getRecognitionDate() { return recognitionDate; }
    public void setRecognitionDate(LocalDate recognitionDate) { this.recognitionDate = recognitionDate; }
    
    public List<String> getPrerequisites() { return prerequisites; }
    public void setPrerequisites(List<String> prerequisites) { this.prerequisites = prerequisites; }
    
    public List<String> getEnables() { return enables; }
    public void setEnables(List<String> enables) { this.enables = enables; }
    
    public Boolean getPrerequisitesMet() { return prerequisitesMet; }
    public void setPrerequisitesMet(Boolean prerequisitesMet) { this.prerequisitesMet = prerequisitesMet; }
    
    public Integer getDependentSkillsCount() { return dependentSkillsCount; }
    public void setDependentSkillsCount(Integer dependentSkillsCount) { this.dependentSkillsCount = dependentSkillsCount; }
    
    public List<String> getDevelopmentGoals() { return developmentGoals; }
    public void setDevelopmentGoals(List<String> developmentGoals) { this.developmentGoals = developmentGoals; }
    
    public List<String> getMilestones() { return milestones; }
    public void setMilestones(List<String> milestones) { this.milestones = milestones; }
    
    public Integer getCompletedMilestones() { return completedMilestones; }
    public void setCompletedMilestones(Integer completedMilestones) { this.completedMilestones = completedMilestones; }
    
    public Integer getTotalMilestones() { return totalMilestones; }
    public void setTotalMilestones(Integer totalMilestones) { this.totalMilestones = totalMilestones; }
    
    public LocalDate getTargetCompletionDate() { return targetCompletionDate; }
    public void setTargetCompletionDate(LocalDate targetCompletionDate) { this.targetCompletionDate = targetCompletionDate; }
    
    public LocalDate getActualCompletionDate() { return actualCompletionDate; }
    public void setActualCompletionDate(LocalDate actualCompletionDate) { this.actualCompletionDate = actualCompletionDate; }
    
    public Double getPerformanceImpact() { return performanceImpact; }
    public void setPerformanceImpact(Double performanceImpact) { this.performanceImpact = performanceImpact; }
    
    public Double getSkillUtilization() { return skillUtilization; }
    public void setSkillUtilization(Double skillUtilization) { this.skillUtilization = skillUtilization; }
    
    public Double getSkillImportance() { return skillImportance; }
    public void setSkillImportance(Double skillImportance) { this.skillImportance = skillImportance; }
    
    public Integer getProjectsApplied() { return projectsApplied; }
    public void setProjectsApplied(Integer projectsApplied) { this.projectsApplied = projectsApplied; }
    
    public Integer getTasksCompleted() { return tasksCompleted; }
    public void setTasksCompleted(Integer tasksCompleted) { this.tasksCompleted = tasksCompleted; }
    
    public List<String> getPerformanceMetrics() { return performanceMetrics; }
    public void setPerformanceMetrics(List<String> performanceMetrics) { this.performanceMetrics = performanceMetrics; }
    
    public String getLearningPath() { return learningPath; }
    public void setLearningPath(String learningPath) { this.learningPath = learningPath; }
    
    public Integer getPathOrder() { return pathOrder; }
    public void setPathOrder(Integer pathOrder) { this.pathOrder = pathOrder; }
    
    public Boolean getIsPathCompleted() { return isPathCompleted; }
    public void setIsPathCompleted(Boolean isPathCompleted) { this.isPathCompleted = isPathCompleted; }
    
    public Double getPathProgress() { return pathProgress; }
    public void setPathProgress(Double pathProgress) { this.pathProgress = pathProgress; }
    
    public String getNextSkillInPath() { return nextSkillInPath; }
    public void setNextSkillInPath(String nextSkillInPath) { this.nextSkillInPath = nextSkillInPath; }
    
    public String getPreviousSkillInPath() { return previousSkillInPath; }
    public void setPreviousSkillInPath(String previousSkillInPath) { this.previousSkillInPath = previousSkillInPath; }
    
    public DifficultyLevel getDifficulty() { return difficulty; }
    public void setDifficulty(DifficultyLevel difficulty) { this.difficulty = difficulty; }
    
    public Double getLearningCurve() { return learningCurve; }
    public void setLearningCurve(Double learningCurve) { this.learningCurve = learningCurve; }
    
    public Integer getStrugglingDays() { return strugglingDays; }
    public void setStrugglingDays(Integer strugglingDays) { this.strugglingDays = strugglingDays; }
    
    public Integer getBreakthroughMoments() { return breakthroughMoments; }
    public void setBreakthroughMoments(Integer breakthroughMoments) { this.breakthroughMoments = breakthroughMoments; }
    
    public List<String> getChallenges() { return challenges; }
    public void setChallenges(List<String> challenges) { this.challenges = challenges; }
    
    public List<String> getBreakthroughs() { return breakthroughs; }
    public void setBreakthroughs(List<String> breakthroughs) { this.breakthroughs = breakthroughs; }
    
    public Boolean getIsTeamCritical() { return isTeamCritical; }
    public void setIsTeamCritical(Boolean isTeamCritical) { this.isTeamCritical = isTeamCritical; }
    
    public Boolean getIsLeadershipSkill() { return isLeadershipSkill; }
    public void setIsLeadershipSkill(Boolean isLeadershipSkill) { this.isLeadershipSkill = isLeadershipSkill; }
    
    public Boolean getIsSpecialistSkill() { return isSpecialistSkill; }
    public void setIsSpecialistSkill(Boolean isSpecialistSkill) { this.isSpecialistSkill = isSpecialistSkill; }
    
    public Integer getTeamMembersWithSkill() { return teamMembersWithSkill; }
    public void setTeamMembersWithSkill(Integer teamMembersWithSkill) { this.teamMembersWithSkill = teamMembersWithSkill; }
    
    public Integer getTeachingOpportunities() { return teachingOpportunities; }
    public void setTeachingOpportunities(Integer teachingOpportunities) { this.teachingOpportunities = teachingOpportunities; }
    
    public List<String> getTeamRoles() { return teamRoles; }
    public void setTeamRoles(List<String> teamRoles) { this.teamRoles = teamRoles; }
    
    public Boolean getIsCompetitionRelevant() { return isCompetitionRelevant; }
    public void setIsCompetitionRelevant(Boolean isCompetitionRelevant) { this.isCompetitionRelevant = isCompetitionRelevant; }
    
    public Integer getCompetitionsUsed() { return competitionsUsed; }
    public void setCompetitionsUsed(Integer competitionsUsed) { this.competitionsUsed = competitionsUsed; }
    
    public Double getCompetitionPerformance() { return competitionPerformance; }
    public void setCompetitionPerformance(Double competitionPerformance) { this.competitionPerformance = competitionPerformance; }
    
    public List<String> getCompetitionContexts() { return competitionContexts; }
    public void setCompetitionContexts(List<String> competitionContexts) { this.competitionContexts = competitionContexts; }
    
    public Boolean getReadyForCompetition() { return readyForCompetition; }
    public void setReadyForCompetition(Boolean readyForCompetition) { this.readyForCompetition = readyForCompetition; }
    
    public List<String> getDocumentation() { return documentation; }
    public void setDocumentation(List<String> documentation) { this.documentation = documentation; }
    
    public List<String> getPortfolioItems() { return portfolioItems; }
    public void setPortfolioItems(List<String> portfolioItems) { this.portfolioItems = portfolioItems; }
    
    public List<String> getPhotoEvidence() { return photoEvidence; }
    public void setPhotoEvidence(List<String> photoEvidence) { this.photoEvidence = photoEvidence; }
    
    public List<String> getVideoEvidence() { return videoEvidence; }
    public void setVideoEvidence(List<String> videoEvidence) { this.videoEvidence = videoEvidence; }
    
    public Double getImprovementRate() { return improvementRate; }
    public void setImprovementRate(Double improvementRate) { this.improvementRate = improvementRate; }
    
    public Double getConsistencyScore() { return consistencyScore; }
    public void setConsistencyScore(Double consistencyScore) { this.consistencyScore = consistencyScore; }
    
    public Double getRetentionRate() { return retentionRate; }
    public void setRetentionRate(Double retentionRate) { this.retentionRate = retentionRate; }
    
    public Integer getPlateauPeriods() { return plateauPeriods; }
    public void setPlateauPeriods(Integer plateauPeriods) { this.plateauPeriods = plateauPeriods; }
    
    public Integer getRegressionPeriods() { return regressionPeriods; }
    public void setRegressionPeriods(Integer regressionPeriods) { this.regressionPeriods = regressionPeriods; }
    
    public LocalDate getLastImprovementDate() { return lastImprovementDate; }
    public void setLastImprovementDate(LocalDate lastImprovementDate) { this.lastImprovementDate = lastImprovementDate; }
    
    public Integer getFeedbackCount() { return feedbackCount; }
    public void setFeedbackCount(Integer feedbackCount) { this.feedbackCount = feedbackCount; }
    
    public Double getAverageFeedbackRating() { return averageFeedbackRating; }
    public void setAverageFeedbackRating(Double averageFeedbackRating) { this.averageFeedbackRating = averageFeedbackRating; }
    
    public LocalDate getLastFeedbackDate() { return lastFeedbackDate; }
    public void setLastFeedbackDate(LocalDate lastFeedbackDate) { this.lastFeedbackDate = lastFeedbackDate; }
    
    public List<String> getImprovementSuggestions() { return improvementSuggestions; }
    public void setImprovementSuggestions(List<String> improvementSuggestions) { this.improvementSuggestions = improvementSuggestions; }
    
    public List<String> getStrengthAreas() { return strengthAreas; }
    public void setStrengthAreas(List<String> strengthAreas) { this.strengthAreas = strengthAreas; }
    
    public Double getRiskScore() { return riskScore; }
    public void setRiskScore(Double riskScore) { this.riskScore = riskScore; }
    
    public Boolean getRequiresIntervention() { return requiresIntervention; }
    public void setRequiresIntervention(Boolean requiresIntervention) { this.requiresIntervention = requiresIntervention; }
    
    public String getInterventionPlan() { return interventionPlan; }
    public void setInterventionPlan(String interventionPlan) { this.interventionPlan = interventionPlan; }
    
    public Boolean getIsAtRisk() { return isAtRisk; }
    public void setIsAtRisk(Boolean isAtRisk) { this.isAtRisk = isAtRisk; }
    
    public List<String> getRiskFactors() { return riskFactors; }
    public void setRiskFactors(List<String> riskFactors) { this.riskFactors = riskFactors; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public TeamMember getCreatedBy() { return createdBy; }
    public void setCreatedBy(TeamMember createdBy) { this.createdBy = createdBy; }
    
    public TeamMember getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(TeamMember updatedBy) { this.updatedBy = updatedBy; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public Boolean getIsArchived() { return isArchived; }
    public void setIsArchived(Boolean isArchived) { this.isArchived = isArchived; }
    
    public LocalDateTime getArchivedAt() { return archivedAt; }
    public void setArchivedAt(LocalDateTime archivedAt) { this.archivedAt = archivedAt; }
    
    public String getArchiveReason() { return archiveReason; }
    public void setArchiveReason(String archiveReason) { this.archiveReason = archiveReason; }
}