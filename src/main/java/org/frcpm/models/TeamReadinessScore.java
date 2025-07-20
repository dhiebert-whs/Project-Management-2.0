// src/main/java/org/frcpm/models/TeamReadinessScore.java

package org.frcpm.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Team Readiness Score model for FRC teams.
 * 
 * Provides comprehensive readiness assessment for FRC teams including
 * competition readiness, build season preparation, technical capabilities,
 * and overall team maturity scoring.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-BusinessIntel
 * @since Phase 4B.4 Team Readiness Scoring System
 */
@Entity
@Table(name = "team_readiness_scores")
public class TeamReadinessScore {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Integer teamNumber;
    
    @Column(length = 100)
    private String teamName;
    
    @Column(nullable = false)
    private Integer season;
    
    @Column(nullable = false)
    private LocalDate assessmentDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReadinessType readinessType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssessmentPhase assessmentPhase;
    
    // Overall Readiness Metrics
    @Column(nullable = false)
    private Double overallReadinessScore = 0.0; // 0-100 scale
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReadinessLevel readinessLevel;
    
    @Column(nullable = false)
    private Double confidenceLevel = 0.0; // 0-1 scale
    
    // Category Scores (0-100 scale)
    @Column(nullable = false)
    private Double technicalReadiness = 0.0;
    
    @Column(nullable = false)
    private Double teamReadiness = 0.0;
    
    @Column(nullable = false)
    private Double resourceReadiness = 0.0;
    
    @Column(nullable = false)
    private Double processReadiness = 0.0;
    
    @Column(nullable = false)
    private Double strategicReadiness = 0.0;
    
    @Column(nullable = false)
    private Double competitionReadiness = 0.0;
    
    // Technical Readiness Details
    @Column(nullable = false)
    private Double robotDesignScore = 0.0;
    
    @Column(nullable = false)
    private Double softwareReadiness = 0.0;
    
    @Column(nullable = false)
    private Double manufacturingCapability = 0.0;
    
    @Column(nullable = false)
    private Double testingMaturity = 0.0;
    
    @Column(nullable = false)
    private Double innovationLevel = 0.0;
    
    // Team Readiness Details
    @Column(nullable = false)
    private Double leadershipStrength = 0.0;
    
    @Column(nullable = false)
    private Double skillsDiversity = 0.0;
    
    @Column(nullable = false)
    private Double teamCohesion = 0.0;
    
    @Column(nullable = false)
    private Double mentorshipQuality = 0.0;
    
    @Column(nullable = false)
    private Double communicationEffectiveness = 0.0;
    
    // Resource Readiness Details
    @Column(nullable = false)
    private Double budgetPreparedness = 0.0;
    
    @Column(nullable = false)
    private Double facilityAdequacy = 0.0;
    
    @Column(nullable = false)
    private Double toolsAndEquipment = 0.0;
    
    @Column(nullable = false)
    private Double materialsAvailability = 0.0;
    
    @Column(nullable = false)
    private Double timeManagement = 0.0;
    
    // Process Readiness Details
    @Column(nullable = false)
    private Double projectManagement = 0.0;
    
    @Column(nullable = false)
    private Double qualityControl = 0.0;
    
    @Column(nullable = false)
    private Double documentationMaturity = 0.0;
    
    @Column(nullable = false)
    private Double safetyCompliance = 0.0;
    
    @Column(nullable = false)
    private Double continuousImprovement = 0.0;
    
    // Strategic Readiness Details
    @Column(nullable = false)
    private Double gameStrategyDepth = 0.0;
    
    @Column(nullable = false)
    private Double competitiveAnalysis = 0.0;
    
    @Column(nullable = false)
    private Double adaptabilityScore = 0.0;
    
    @Column(nullable = false)
    private Double sustainabilityPlanning = 0.0;
    
    @Column(nullable = false)
    private Double stakeholderEngagement = 0.0;
    
    // Competition Readiness Details
    @Column(nullable = false)
    private Double robotReliability = 0.0;
    
    @Column(nullable = false)
    private Double driverProficiency = 0.0;
    
    @Column(nullable = false)
    private Double pitCrewEfficiency = 0.0;
    
    @Column(nullable = false)
    private Double scouttingCapability = 0.0;
    
    @Column(nullable = false)
    private Double allianceCollaboration = 0.0;
    
    // Risk Assessment
    @Column(nullable = false)
    private Double riskExposure = 0.0; // Higher = more risk
    
    @Column(nullable = false)
    private Double mitigationStrength = 0.0;
    
    @Column(nullable = false)
    private Double contingencyPreparedness = 0.0;
    
    // Performance Predictions
    @Column(nullable = false)
    private Double predictedCompetitionPerformance = 0.0;
    
    @Column(nullable = false)
    private Double improvementPotential = 0.0;
    
    @Column(nullable = false)
    private Integer daysToReadiness = 0; // Days until fully ready
    
    // Assessment Metadata
    @Column(length = 2000)
    private String strengthsAssessment;
    
    @Column(length = 2000)
    private String weaknessesAssessment;
    
    @Column(length = 2000)
    private String readinessRecommendations;
    
    @Column(length = 1000)
    private String criticalActions;
    
    @Column(length = 1000)
    private String nextAssessmentDate;
    
    // Administrative Fields
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessed_by_id")
    private TeamMember assessedBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    private TeamMember approvedBy;
    
    @Column
    private LocalDateTime approvedAt;
    
    /**
     * Types of readiness assessments
     */
    public enum ReadinessType {
        COMPETITION("Competition Readiness", "Assessment for upcoming competition"),
        BUILD_SEASON("Build Season Readiness", "Preparation for 6-week build season"),
        KICKOFF("Kickoff Readiness", "Preparation for season kickoff"),
        CHAMPIONSHIP("Championship Readiness", "Assessment for championship events"),
        OFF_SEASON("Off-Season Readiness", "Off-season preparation and development"),
        OVERALL("Overall Team Readiness", "Comprehensive team maturity assessment"),
        TECHNICAL("Technical Readiness", "Focus on technical capabilities"),
        ORGANIZATIONAL("Organizational Readiness", "Focus on team structure and processes"),
        STRATEGIC("Strategic Readiness", "Focus on planning and strategy"),
        EMERGENCY("Emergency Readiness", "Crisis response and adaptation capability");
        
        private final String displayName;
        private final String description;
        
        ReadinessType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    /**
     * Phases of team development and assessment
     */
    public enum AssessmentPhase {
        FORMATION("Formation Phase", "Team is forming and establishing basics"),
        DEVELOPMENT("Development Phase", "Team is developing capabilities"),
        MATURATION("Maturation Phase", "Team is maturing and optimizing"),
        PEAK_PERFORMANCE("Peak Performance", "Team is at peak operational capability"),
        SUSTAINING("Sustaining Phase", "Team is maintaining high performance"),
        TRANSITION("Transition Phase", "Team is in transition or rebuilding"),
        CRISIS("Crisis Phase", "Team is dealing with significant challenges"),
        INNOVATION("Innovation Phase", "Team is pushing boundaries and innovating");
        
        private final String displayName;
        private final String description;
        
        AssessmentPhase(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    /**
     * Overall readiness levels
     */
    public enum ReadinessLevel {
        CRITICAL("Critical", "Immediate intervention required", "#dc3545"),
        LOW("Low Readiness", "Significant preparation needed", "#fd7e14"),
        MODERATE("Moderate Readiness", "Some preparation needed", "#ffc107"),
        GOOD("Good Readiness", "Minor adjustments needed", "#28a745"),
        EXCELLENT("Excellent Readiness", "Well prepared for challenges", "#007bff"),
        EXCEPTIONAL("Exceptional Readiness", "Exceeds all readiness criteria", "#6f42c1");
        
        private final String displayName;
        private final String description;
        private final String colorCode;
        
        ReadinessLevel(String displayName, String description, String colorCode) {
            this.displayName = displayName;
            this.description = description;
            this.colorCode = colorCode;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public String getColorCode() { return colorCode; }
    }
    
    // Constructors
    public TeamReadinessScore() {
        this.assessmentDate = LocalDate.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public TeamReadinessScore(Integer teamNumber, Integer season, ReadinessType readinessType, 
                             AssessmentPhase assessmentPhase) {
        this();
        this.teamNumber = teamNumber;
        this.season = season;
        this.readinessType = readinessType;
        this.assessmentPhase = assessmentPhase;
    }
    
    // Business Methods
    
    /**
     * Calculates overall readiness score from category scores.
     */
    public void calculateOverallReadinessScore() {
        // Weighted average based on readiness type
        Map<String, Double> weights = getWeightsForReadinessType();
        
        double totalScore = 
            technicalReadiness * weights.get("technical") +
            teamReadiness * weights.get("team") +
            resourceReadiness * weights.get("resource") +
            processReadiness * weights.get("process") +
            strategicReadiness * weights.get("strategic") +
            competitionReadiness * weights.get("competition");
        
        this.overallReadinessScore = Math.min(100.0, Math.max(0.0, totalScore));
        this.readinessLevel = determineReadinessLevel(this.overallReadinessScore);
    }
    
    /**
     * Determines readiness level based on overall score.
     */
    private ReadinessLevel determineReadinessLevel(double score) {
        if (score >= 95) return ReadinessLevel.EXCEPTIONAL;
        if (score >= 85) return ReadinessLevel.EXCELLENT;
        if (score >= 70) return ReadinessLevel.GOOD;
        if (score >= 55) return ReadinessLevel.MODERATE;
        if (score >= 35) return ReadinessLevel.LOW;
        return ReadinessLevel.CRITICAL;
    }
    
    /**
     * Gets weights for different readiness types.
     */
    private Map<String, Double> getWeightsForReadinessType() {
        Map<String, Double> weights = new HashMap<>();
        
        switch (readinessType) {
            case COMPETITION:
                weights.put("technical", 0.25);
                weights.put("team", 0.20);
                weights.put("resource", 0.10);
                weights.put("process", 0.15);
                weights.put("strategic", 0.10);
                weights.put("competition", 0.20);
                break;
            case BUILD_SEASON:
                weights.put("technical", 0.30);
                weights.put("team", 0.25);
                weights.put("resource", 0.20);
                weights.put("process", 0.15);
                weights.put("strategic", 0.05);
                weights.put("competition", 0.05);
                break;
            case TECHNICAL:
                weights.put("technical", 0.50);
                weights.put("team", 0.15);
                weights.put("resource", 0.15);
                weights.put("process", 0.20);
                weights.put("strategic", 0.00);
                weights.put("competition", 0.00);
                break;
            default: // OVERALL
                weights.put("technical", 0.20);
                weights.put("team", 0.20);
                weights.put("resource", 0.15);
                weights.put("process", 0.15);
                weights.put("strategic", 0.15);
                weights.put("competition", 0.15);
        }
        
        return weights;
    }
    
    /**
     * Checks if team is ready for competition.
     */
    public boolean isCompetitionReady() {
        return overallReadinessScore >= 70.0 && 
               robotReliability >= 75.0 && 
               driverProficiency >= 70.0;
    }
    
    /**
     * Checks if immediate action is required.
     */
    public boolean requiresImmediateAction() {
        return readinessLevel == ReadinessLevel.CRITICAL || 
               (readinessLevel == ReadinessLevel.LOW && daysToReadiness < 14);
    }
    
    /**
     * Gets the weakest readiness area.
     */
    public String getWeakestArea() {
        Map<String, Double> areas = Map.of(
            "Technical", technicalReadiness,
            "Team", teamReadiness,
            "Resource", resourceReadiness,
            "Process", processReadiness,
            "Strategic", strategicReadiness,
            "Competition", competitionReadiness
        );
        
        return areas.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Unknown");
    }
    
    /**
     * Gets the strongest readiness area.
     */
    public String getStrongestArea() {
        Map<String, Double> areas = Map.of(
            "Technical", technicalReadiness,
            "Team", teamReadiness,
            "Resource", resourceReadiness,
            "Process", processReadiness,
            "Strategic", strategicReadiness,
            "Competition", competitionReadiness
        );
        
        return areas.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Unknown");
    }
    
    /**
     * Calculates readiness gap to target level.
     */
    public double getReadinessGap(double targetScore) {
        return Math.max(0.0, targetScore - overallReadinessScore);
    }
    
    /**
     * Determines if team is improving or declining.
     */
    public String getReadinessTrend() {
        // This would compare with previous assessments
        // For now, return based on current state
        if (overallReadinessScore >= 85.0) return "STRONG";
        if (overallReadinessScore >= 70.0) return "IMPROVING";
        if (overallReadinessScore >= 55.0) return "STABLE";
        return "DECLINING";
    }
    
    /**
     * Calculates risk-adjusted readiness score.
     */
    public double getRiskAdjustedScore() {
        double riskFactor = (100.0 - riskExposure) / 100.0;
        return overallReadinessScore * riskFactor;
    }
    
    /**
     * Gets readiness maturity level.
     */
    public String getMaturityLevel() {
        if (overallReadinessScore >= 90.0 && assessmentPhase == AssessmentPhase.PEAK_PERFORMANCE) {
            return "WORLD_CLASS";
        }
        if (overallReadinessScore >= 80.0) return "ADVANCED";
        if (overallReadinessScore >= 65.0) return "PROFICIENT";
        if (overallReadinessScore >= 45.0) return "DEVELOPING";
        return "BASIC";
    }
    
    /**
     * Checks if assessment is current.
     */
    public boolean isAssessmentCurrent() {
        LocalDate cutoff = LocalDate.now().minusWeeks(4); // 4 weeks old
        return assessmentDate.isAfter(cutoff);
    }
    
    /**
     * Calculates days since last assessment.
     */
    public int getDaysSinceAssessment() {
        return (int) java.time.temporal.ChronoUnit.DAYS.between(assessmentDate, LocalDate.now());
    }
    
    /**
     * Generates readiness summary report.
     */
    public String generateReadinessSummary() {
        StringBuilder summary = new StringBuilder();
        
        summary.append("Team ").append(teamNumber);
        if (teamName != null) {
            summary.append(" (").append(teamName).append(")");
        }
        
        summary.append("\nOverall Readiness: ").append(readinessLevel.getDisplayName());
        summary.append(" (").append(String.format("%.1f", overallReadinessScore)).append("/100)");
        
        summary.append("\nStrongest Area: ").append(getStrongestArea());
        summary.append("\nWeakest Area: ").append(getWeakestArea());
        
        if (requiresImmediateAction()) {
            summary.append("\n⚠️ IMMEDIATE ACTION REQUIRED");
        }
        
        if (isCompetitionReady()) {
            summary.append("\n✅ Competition Ready");
        } else {
            summary.append("\n❌ Not Competition Ready");
        }
        
        if (daysToReadiness > 0) {
            summary.append("\nDays to Full Readiness: ").append(daysToReadiness);
        }
        
        return summary.toString();
    }
    
    /**
     * Gets priority improvement areas.
     */
    public Map<String, String> getPriorityImprovementAreas() {
        Map<String, String> priorities = new HashMap<>();
        
        if (technicalReadiness < 60.0) {
            priorities.put("Technical", "Focus on robot design and manufacturing");
        }
        if (teamReadiness < 60.0) {
            priorities.put("Team", "Improve leadership and team cohesion");
        }
        if (resourceReadiness < 60.0) {
            priorities.put("Resource", "Secure adequate resources and facilities");
        }
        if (competitionReadiness < 70.0 && readinessType == ReadinessType.COMPETITION) {
            priorities.put("Competition", "Enhance robot reliability and driver skills");
        }
        
        return priorities;
    }
    
    /**
     * Calculates readiness score for specific category.
     */
    public void calculateCategoryScore(String category) {
        switch (category.toLowerCase()) {
            case "technical":
                this.technicalReadiness = (robotDesignScore + softwareReadiness + 
                                         manufacturingCapability + testingMaturity + innovationLevel) / 5.0;
                break;
            case "team":
                this.teamReadiness = (leadershipStrength + skillsDiversity + teamCohesion + 
                                    mentorshipQuality + communicationEffectiveness) / 5.0;
                break;
            case "resource":
                this.resourceReadiness = (budgetPreparedness + facilityAdequacy + toolsAndEquipment + 
                                        materialsAvailability + timeManagement) / 5.0;
                break;
            case "process":
                this.processReadiness = (projectManagement + qualityControl + documentationMaturity + 
                                       safetyCompliance + continuousImprovement) / 5.0;
                break;
            case "strategic":
                this.strategicReadiness = (gameStrategyDepth + competitiveAnalysis + adaptabilityScore + 
                                         sustainabilityPlanning + stakeholderEngagement) / 5.0;
                break;
            case "competition":
                this.competitionReadiness = (robotReliability + driverProficiency + pitCrewEfficiency + 
                                           scouttingCapability + allianceCollaboration) / 5.0;
                break;
        }
    }
    
    /**
     * Updates all category scores and overall score.
     */
    public void recalculateAllScores() {
        calculateCategoryScore("technical");
        calculateCategoryScore("team");
        calculateCategoryScore("resource");
        calculateCategoryScore("process");
        calculateCategoryScore("strategic");
        calculateCategoryScore("competition");
        calculateOverallReadinessScore();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Integer getTeamNumber() { return teamNumber; }
    public void setTeamNumber(Integer teamNumber) { this.teamNumber = teamNumber; }
    
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    
    public Integer getSeason() { return season; }
    public void setSeason(Integer season) { this.season = season; }
    
    public LocalDate getAssessmentDate() { return assessmentDate; }
    public void setAssessmentDate(LocalDate assessmentDate) { this.assessmentDate = assessmentDate; }
    
    public ReadinessType getReadinessType() { return readinessType; }
    public void setReadinessType(ReadinessType readinessType) { this.readinessType = readinessType; }
    
    public AssessmentPhase getAssessmentPhase() { return assessmentPhase; }
    public void setAssessmentPhase(AssessmentPhase assessmentPhase) { this.assessmentPhase = assessmentPhase; }
    
    public Double getOverallReadinessScore() { return overallReadinessScore; }
    public void setOverallReadinessScore(Double overallReadinessScore) { this.overallReadinessScore = overallReadinessScore; }
    
    public ReadinessLevel getReadinessLevel() { return readinessLevel; }
    public void setReadinessLevel(ReadinessLevel readinessLevel) { this.readinessLevel = readinessLevel; }
    
    public Double getConfidenceLevel() { return confidenceLevel; }
    public void setConfidenceLevel(Double confidenceLevel) { this.confidenceLevel = confidenceLevel; }
    
    public Double getTechnicalReadiness() { return technicalReadiness; }
    public void setTechnicalReadiness(Double technicalReadiness) { this.technicalReadiness = technicalReadiness; }
    
    public Double getTeamReadiness() { return teamReadiness; }
    public void setTeamReadiness(Double teamReadiness) { this.teamReadiness = teamReadiness; }
    
    public Double getResourceReadiness() { return resourceReadiness; }
    public void setResourceReadiness(Double resourceReadiness) { this.resourceReadiness = resourceReadiness; }
    
    public Double getProcessReadiness() { return processReadiness; }
    public void setProcessReadiness(Double processReadiness) { this.processReadiness = processReadiness; }
    
    public Double getStrategicReadiness() { return strategicReadiness; }
    public void setStrategicReadiness(Double strategicReadiness) { this.strategicReadiness = strategicReadiness; }
    
    public Double getCompetitionReadiness() { return competitionReadiness; }
    public void setCompetitionReadiness(Double competitionReadiness) { this.competitionReadiness = competitionReadiness; }
    
    public Double getRobotDesignScore() { return robotDesignScore; }
    public void setRobotDesignScore(Double robotDesignScore) { this.robotDesignScore = robotDesignScore; }
    
    public Double getSoftwareReadiness() { return softwareReadiness; }
    public void setSoftwareReadiness(Double softwareReadiness) { this.softwareReadiness = softwareReadiness; }
    
    public Double getManufacturingCapability() { return manufacturingCapability; }
    public void setManufacturingCapability(Double manufacturingCapability) { this.manufacturingCapability = manufacturingCapability; }
    
    public Double getTestingMaturity() { return testingMaturity; }
    public void setTestingMaturity(Double testingMaturity) { this.testingMaturity = testingMaturity; }
    
    public Double getInnovationLevel() { return innovationLevel; }
    public void setInnovationLevel(Double innovationLevel) { this.innovationLevel = innovationLevel; }
    
    public Double getLeadershipStrength() { return leadershipStrength; }
    public void setLeadershipStrength(Double leadershipStrength) { this.leadershipStrength = leadershipStrength; }
    
    public Double getSkillsDiversity() { return skillsDiversity; }
    public void setSkillsDiversity(Double skillsDiversity) { this.skillsDiversity = skillsDiversity; }
    
    public Double getTeamCohesion() { return teamCohesion; }
    public void setTeamCohesion(Double teamCohesion) { this.teamCohesion = teamCohesion; }
    
    public Double getMentorshipQuality() { return mentorshipQuality; }
    public void setMentorshipQuality(Double mentorshipQuality) { this.mentorshipQuality = mentorshipQuality; }
    
    public Double getCommunicationEffectiveness() { return communicationEffectiveness; }
    public void setCommunicationEffectiveness(Double communicationEffectiveness) { this.communicationEffectiveness = communicationEffectiveness; }
    
    public Double getBudgetPreparedness() { return budgetPreparedness; }
    public void setBudgetPreparedness(Double budgetPreparedness) { this.budgetPreparedness = budgetPreparedness; }
    
    public Double getFacilityAdequacy() { return facilityAdequacy; }
    public void setFacilityAdequacy(Double facilityAdequacy) { this.facilityAdequacy = facilityAdequacy; }
    
    public Double getToolsAndEquipment() { return toolsAndEquipment; }
    public void setToolsAndEquipment(Double toolsAndEquipment) { this.toolsAndEquipment = toolsAndEquipment; }
    
    public Double getMaterialsAvailability() { return materialsAvailability; }
    public void setMaterialsAvailability(Double materialsAvailability) { this.materialsAvailability = materialsAvailability; }
    
    public Double getTimeManagement() { return timeManagement; }
    public void setTimeManagement(Double timeManagement) { this.timeManagement = timeManagement; }
    
    public Double getProjectManagement() { return projectManagement; }
    public void setProjectManagement(Double projectManagement) { this.projectManagement = projectManagement; }
    
    public Double getQualityControl() { return qualityControl; }
    public void setQualityControl(Double qualityControl) { this.qualityControl = qualityControl; }
    
    public Double getDocumentationMaturity() { return documentationMaturity; }
    public void setDocumentationMaturity(Double documentationMaturity) { this.documentationMaturity = documentationMaturity; }
    
    public Double getSafetyCompliance() { return safetyCompliance; }
    public void setSafetyCompliance(Double safetyCompliance) { this.safetyCompliance = safetyCompliance; }
    
    public Double getContinuousImprovement() { return continuousImprovement; }
    public void setContinuousImprovement(Double continuousImprovement) { this.continuousImprovement = continuousImprovement; }
    
    public Double getGameStrategyDepth() { return gameStrategyDepth; }
    public void setGameStrategyDepth(Double gameStrategyDepth) { this.gameStrategyDepth = gameStrategyDepth; }
    
    public Double getCompetitiveAnalysis() { return competitiveAnalysis; }
    public void setCompetitiveAnalysis(Double competitiveAnalysis) { this.competitiveAnalysis = competitiveAnalysis; }
    
    public Double getAdaptabilityScore() { return adaptabilityScore; }
    public void setAdaptabilityScore(Double adaptabilityScore) { this.adaptabilityScore = adaptabilityScore; }
    
    public Double getSustainabilityPlanning() { return sustainabilityPlanning; }
    public void setSustainabilityPlanning(Double sustainabilityPlanning) { this.sustainabilityPlanning = sustainabilityPlanning; }
    
    public Double getStakeholderEngagement() { return stakeholderEngagement; }
    public void setStakeholderEngagement(Double stakeholderEngagement) { this.stakeholderEngagement = stakeholderEngagement; }
    
    public Double getRobotReliability() { return robotReliability; }
    public void setRobotReliability(Double robotReliability) { this.robotReliability = robotReliability; }
    
    public Double getDriverProficiency() { return driverProficiency; }
    public void setDriverProficiency(Double driverProficiency) { this.driverProficiency = driverProficiency; }
    
    public Double getPitCrewEfficiency() { return pitCrewEfficiency; }
    public void setPitCrewEfficiency(Double pitCrewEfficiency) { this.pitCrewEfficiency = pitCrewEfficiency; }
    
    public Double getScouttingCapability() { return scouttingCapability; }
    public void setScouttingCapability(Double scouttingCapability) { this.scouttingCapability = scouttingCapability; }
    
    public Double getAllianceCollaboration() { return allianceCollaboration; }
    public void setAllianceCollaboration(Double allianceCollaboration) { this.allianceCollaboration = allianceCollaboration; }
    
    public Double getRiskExposure() { return riskExposure; }
    public void setRiskExposure(Double riskExposure) { this.riskExposure = riskExposure; }
    
    public Double getMitigationStrength() { return mitigationStrength; }
    public void setMitigationStrength(Double mitigationStrength) { this.mitigationStrength = mitigationStrength; }
    
    public Double getContingencyPreparedness() { return contingencyPreparedness; }
    public void setContingencyPreparedness(Double contingencyPreparedness) { this.contingencyPreparedness = contingencyPreparedness; }
    
    public Double getPredictedCompetitionPerformance() { return predictedCompetitionPerformance; }
    public void setPredictedCompetitionPerformance(Double predictedCompetitionPerformance) { this.predictedCompetitionPerformance = predictedCompetitionPerformance; }
    
    public Double getImprovementPotential() { return improvementPotential; }
    public void setImprovementPotential(Double improvementPotential) { this.improvementPotential = improvementPotential; }
    
    public Integer getDaysToReadiness() { return daysToReadiness; }
    public void setDaysToReadiness(Integer daysToReadiness) { this.daysToReadiness = daysToReadiness; }
    
    public String getStrengthsAssessment() { return strengthsAssessment; }
    public void setStrengthsAssessment(String strengthsAssessment) { this.strengthsAssessment = strengthsAssessment; }
    
    public String getWeaknessesAssessment() { return weaknessesAssessment; }
    public void setWeaknessesAssessment(String weaknessesAssessment) { this.weaknessesAssessment = weaknessesAssessment; }
    
    public String getReadinessRecommendations() { return readinessRecommendations; }
    public void setReadinessRecommendations(String readinessRecommendations) { this.readinessRecommendations = readinessRecommendations; }
    
    public String getCriticalActions() { return criticalActions; }
    public void setCriticalActions(String criticalActions) { this.criticalActions = criticalActions; }
    
    public String getNextAssessmentDate() { return nextAssessmentDate; }
    public void setNextAssessmentDate(String nextAssessmentDate) { this.nextAssessmentDate = nextAssessmentDate; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public TeamMember getAssessedBy() { return assessedBy; }
    public void setAssessedBy(TeamMember assessedBy) { this.assessedBy = assessedBy; }
    
    public TeamMember getApprovedBy() { return approvedBy; }
    public void setApprovedBy(TeamMember approvedBy) { this.approvedBy = approvedBy; }
    
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    
    @Override
    public String toString() {
        return String.format("TeamReadinessScore{id=%d, team=%d, season=%d, type=%s, score=%.1f, level=%s}", 
                           id, teamNumber, season, readinessType, overallReadinessScore, readinessLevel);
    }
}