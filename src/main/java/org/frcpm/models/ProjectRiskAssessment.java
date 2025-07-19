// src/main/java/org/frcpm/models/ProjectRiskAssessment.java

package org.frcpm.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Project Risk Assessment model for FRC teams.
 * 
 * Provides AI-powered risk analysis and predictive analytics for FRC projects,
 * helping teams identify potential issues before they impact competition readiness.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-BusinessIntel
 * @since Phase 4B.1 Predictive Analytics for Project Risks
 */
@Entity
@Table(name = "project_risk_assessments")
public class ProjectRiskAssessment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    
    @Column(nullable = false)
    private LocalDateTime assessmentDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssessmentType assessmentType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskLevel overallRiskLevel;
    
    @Column(nullable = false)
    private Double riskScore; // 0.0 to 100.0
    
    @Column(nullable = false)
    private Double confidenceLevel; // 0.0 to 1.0
    
    // Risk Categories
    @Column(nullable = false)
    private Double scheduleRisk = 0.0;
    
    @Column(nullable = false)
    private Double resourceRisk = 0.0;
    
    @Column(nullable = false)
    private Double technicalRisk = 0.0;
    
    @Column(nullable = false)
    private Double teamRisk = 0.0;
    
    @Column(nullable = false)
    private Double competitionRisk = 0.0;
    
    @Column(nullable = false)
    private Double qualityRisk = 0.0;
    
    // Predictive Analytics
    @Column
    private Double completionProbability; // Probability of completing on time
    
    @Column
    private Integer predictedDelayDays; // Predicted delay in days
    
    @Column
    private Double competitionReadinessProbability; // Probability of being competition-ready
    
    @Column
    private Double budgetOverrunRisk; // Risk of budget overrun
    
    // Assessment Summary
    @Column(length = 2000)
    private String executiveSummary;
    
    @Column(length = 2000)
    private String keyRisks;
    
    @Column(length = 2000)
    private String mitigationRecommendations;
    
    @Column(length = 1000)
    private String criticalActions;
    
    // Assessment Metadata
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessed_by_id")
    private TeamMember assessedBy;
    
    // Simplified collections for now - can be enhanced later
    @ElementCollection
    @CollectionTable(name = "risk_factors", joinColumns = @JoinColumn(name = "assessment_id"))
    @Column(name = "risk_factor", length = 500)
    private List<String> riskFactors = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "mitigation_strategies", joinColumns = @JoinColumn(name = "assessment_id"))
    @Column(name = "strategy", length = 500)
    private List<String> mitigationStrategies = new ArrayList<>();
    
    // Analytics Data
    @Column(length = 5000)
    private String analyticsData; // JSON data for ML model inputs
    
    @Column(length = 2000)
    private String modelVersion; // Version of the predictive model used
    
    @Column
    private LocalDateTime lastModelUpdate;
    
    /**
     * Types of risk assessments
     */
    public enum AssessmentType {
        INITIAL("Initial Assessment", "First assessment at project start"),
        WEEKLY("Weekly Review", "Regular weekly risk review"),
        MILESTONE("Milestone Assessment", "Assessment at major milestones"),
        TRIGGERED("Triggered Assessment", "Assessment triggered by events"),
        PRE_COMPETITION("Pre-Competition", "Final assessment before competition"),
        POST_MORTEM("Post-Mortem", "Analysis after project completion"),
        AUTOMATED("Automated Assessment", "AI-generated risk assessment"),
        MANUAL("Manual Assessment", "Human-conducted risk assessment");
        
        private final String displayName;
        private final String description;
        
        AssessmentType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    /**
     * Overall risk levels
     */
    public enum RiskLevel {
        LOW("Low Risk", "Project on track, minimal concerns", "#28a745"),
        MEDIUM_LOW("Medium-Low Risk", "Some concerns but manageable", "#6c757d"),
        MEDIUM("Medium Risk", "Significant risks requiring attention", "#ffc107"),
        MEDIUM_HIGH("Medium-High Risk", "High risk of issues, action needed", "#fd7e14"),
        HIGH("High Risk", "Critical risks, immediate action required", "#dc3545"),
        CRITICAL("Critical Risk", "Project in jeopardy, emergency response needed", "#6f42c1");
        
        private final String displayName;
        private final String description;
        private final String colorCode;
        
        RiskLevel(String displayName, String description, String colorCode) {
            this.displayName = displayName;
            this.description = description;
            this.colorCode = colorCode;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public String getColorCode() { return colorCode; }
    }
    
    // Constructors
    public ProjectRiskAssessment() {
        this.assessmentDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public ProjectRiskAssessment(Project project, AssessmentType assessmentType, TeamMember assessedBy) {
        this();
        this.project = project;
        this.assessmentType = assessmentType;
        this.assessedBy = assessedBy;
    }
    
    // Business Methods
    
    /**
     * Calculates overall risk score from individual risk categories.
     */
    public void calculateOverallRiskScore() {
        // Weighted average of risk categories
        double totalScore = (scheduleRisk * 0.25) +
                           (resourceRisk * 0.20) +
                           (technicalRisk * 0.20) +
                           (teamRisk * 0.15) +
                           (competitionRisk * 0.10) +
                           (qualityRisk * 0.10);
        
        this.riskScore = Math.min(100.0, Math.max(0.0, totalScore));
        this.overallRiskLevel = determineRiskLevel(this.riskScore);
    }
    
    /**
     * Determines risk level based on risk score.
     */
    private RiskLevel determineRiskLevel(double score) {
        if (score >= 90) return RiskLevel.CRITICAL;
        if (score >= 75) return RiskLevel.HIGH;
        if (score >= 60) return RiskLevel.MEDIUM_HIGH;
        if (score >= 40) return RiskLevel.MEDIUM;
        if (score >= 20) return RiskLevel.MEDIUM_LOW;
        return RiskLevel.LOW;
    }
    
    /**
     * Checks if this assessment indicates the project is at risk.
     */
    public boolean isProjectAtRisk() {
        return overallRiskLevel.ordinal() >= RiskLevel.MEDIUM_HIGH.ordinal();
    }
    
    /**
     * Checks if immediate action is required.
     */
    public boolean requiresImmediateAction() {
        return overallRiskLevel == RiskLevel.HIGH || overallRiskLevel == RiskLevel.CRITICAL;
    }
    
    /**
     * Gets the highest individual risk category.
     */
    public String getHighestRiskCategory() {
        double maxRisk = Math.max(scheduleRisk, 
                         Math.max(resourceRisk,
                         Math.max(technicalRisk,
                         Math.max(teamRisk,
                         Math.max(competitionRisk, qualityRisk)))));
        
        if (maxRisk == scheduleRisk) return "Schedule";
        if (maxRisk == resourceRisk) return "Resource";
        if (maxRisk == technicalRisk) return "Technical";
        if (maxRisk == teamRisk) return "Team";
        if (maxRisk == competitionRisk) return "Competition";
        if (maxRisk == qualityRisk) return "Quality";
        return "Unknown";
    }
    
    /**
     * Calculates days until project deadline.
     */
    public int getDaysUntilDeadline() {
        if (project != null && project.getHardDeadline() != null) {
            return (int) java.time.temporal.ChronoUnit.DAYS.between(
                LocalDateTime.now().toLocalDate(), 
                project.getHardDeadline()
            );
        }
        return -1;
    }
    
    /**
     * Determines if project is on critical timeline.
     */
    public boolean isOnCriticalTimeline() {
        int daysLeft = getDaysUntilDeadline();
        return daysLeft >= 0 && daysLeft <= 14; // Within 2 weeks of deadline
    }
    
    /**
     * Gets risk trend compared to previous assessment.
     */
    public String getRiskTrend() {
        // This would be calculated by comparing with previous assessments
        // For now, return placeholder
        if (riskScore > 60) return "INCREASING";
        if (riskScore < 30) return "DECREASING";
        return "STABLE";
    }
    
    /**
     * Validates the assessment data for completeness.
     */
    public boolean isAssessmentComplete() {
        return project != null &&
               assessmentType != null &&
               overallRiskLevel != null &&
               riskScore != null &&
               scheduleRisk != null &&
               resourceRisk != null &&
               technicalRisk != null &&
               teamRisk != null &&
               competitionRisk != null &&
               qualityRisk != null;
    }
    
    /**
     * Generates a risk assessment summary.
     */
    public String generateRiskSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Risk Level: ").append(overallRiskLevel.getDisplayName());
        summary.append(" (Score: ").append(String.format("%.1f", riskScore)).append("/100)");
        
        if (isProjectAtRisk()) {
            summary.append(" - ATTENTION REQUIRED");
        }
        
        summary.append("\nHighest Risk Area: ").append(getHighestRiskCategory());
        
        if (predictedDelayDays != null && predictedDelayDays > 0) {
            summary.append("\nPredicted Delay: ").append(predictedDelayDays).append(" days");
        }
        
        if (completionProbability != null) {
            summary.append("\nCompletion Probability: ").append(String.format("%.0f%%", completionProbability * 100));
        }
        
        return summary.toString();
    }
    
    /**
     * Adds a risk factor to this assessment.
     */
    public void addRiskFactor(String riskFactor) {
        this.riskFactors.add(riskFactor);
    }
    
    /**
     * Adds a mitigation strategy to this assessment.
     */
    public void addMitigationStrategy(String strategy) {
        this.mitigationStrategies.add(strategy);
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    
    public LocalDateTime getAssessmentDate() { return assessmentDate; }
    public void setAssessmentDate(LocalDateTime assessmentDate) { this.assessmentDate = assessmentDate; }
    
    public AssessmentType getAssessmentType() { return assessmentType; }
    public void setAssessmentType(AssessmentType assessmentType) { this.assessmentType = assessmentType; }
    
    public RiskLevel getOverallRiskLevel() { return overallRiskLevel; }
    public void setOverallRiskLevel(RiskLevel overallRiskLevel) { this.overallRiskLevel = overallRiskLevel; }
    
    public Double getRiskScore() { return riskScore; }
    public void setRiskScore(Double riskScore) { this.riskScore = riskScore; }
    
    public Double getConfidenceLevel() { return confidenceLevel; }
    public void setConfidenceLevel(Double confidenceLevel) { this.confidenceLevel = confidenceLevel; }
    
    public Double getScheduleRisk() { return scheduleRisk; }
    public void setScheduleRisk(Double scheduleRisk) { this.scheduleRisk = scheduleRisk; }
    
    public Double getResourceRisk() { return resourceRisk; }
    public void setResourceRisk(Double resourceRisk) { this.resourceRisk = resourceRisk; }
    
    public Double getTechnicalRisk() { return technicalRisk; }
    public void setTechnicalRisk(Double technicalRisk) { this.technicalRisk = technicalRisk; }
    
    public Double getTeamRisk() { return teamRisk; }
    public void setTeamRisk(Double teamRisk) { this.teamRisk = teamRisk; }
    
    public Double getCompetitionRisk() { return competitionRisk; }
    public void setCompetitionRisk(Double competitionRisk) { this.competitionRisk = competitionRisk; }
    
    public Double getQualityRisk() { return qualityRisk; }
    public void setQualityRisk(Double qualityRisk) { this.qualityRisk = qualityRisk; }
    
    public Double getCompletionProbability() { return completionProbability; }
    public void setCompletionProbability(Double completionProbability) { this.completionProbability = completionProbability; }
    
    public Integer getPredictedDelayDays() { return predictedDelayDays; }
    public void setPredictedDelayDays(Integer predictedDelayDays) { this.predictedDelayDays = predictedDelayDays; }
    
    public Double getCompetitionReadinessProbability() { return competitionReadinessProbability; }
    public void setCompetitionReadinessProbability(Double competitionReadinessProbability) { this.competitionReadinessProbability = competitionReadinessProbability; }
    
    public Double getBudgetOverrunRisk() { return budgetOverrunRisk; }
    public void setBudgetOverrunRisk(Double budgetOverrunRisk) { this.budgetOverrunRisk = budgetOverrunRisk; }
    
    public String getExecutiveSummary() { return executiveSummary; }
    public void setExecutiveSummary(String executiveSummary) { this.executiveSummary = executiveSummary; }
    
    public String getKeyRisks() { return keyRisks; }
    public void setKeyRisks(String keyRisks) { this.keyRisks = keyRisks; }
    
    public String getMitigationRecommendations() { return mitigationRecommendations; }
    public void setMitigationRecommendations(String mitigationRecommendations) { this.mitigationRecommendations = mitigationRecommendations; }
    
    public String getCriticalActions() { return criticalActions; }
    public void setCriticalActions(String criticalActions) { this.criticalActions = criticalActions; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public TeamMember getAssessedBy() { return assessedBy; }
    public void setAssessedBy(TeamMember assessedBy) { this.assessedBy = assessedBy; }
    
    public List<String> getRiskFactors() { return riskFactors; }
    public void setRiskFactors(List<String> riskFactors) { this.riskFactors = riskFactors; }
    
    public List<String> getMitigationStrategies() { return mitigationStrategies; }
    public void setMitigationStrategies(List<String> mitigationStrategies) { this.mitigationStrategies = mitigationStrategies; }
    
    public String getAnalyticsData() { return analyticsData; }
    public void setAnalyticsData(String analyticsData) { this.analyticsData = analyticsData; }
    
    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }
    
    public LocalDateTime getLastModelUpdate() { return lastModelUpdate; }
    public void setLastModelUpdate(LocalDateTime lastModelUpdate) { this.lastModelUpdate = lastModelUpdate; }
    
    @Override
    public String toString() {
        return String.format("ProjectRiskAssessment{id=%d, project='%s', riskLevel=%s, score=%.1f, date=%s}", 
                           id, project != null ? project.getName() : "Unknown", 
                           overallRiskLevel, riskScore, assessmentDate);
    }
}