// src/main/java/org/frcpm/models/MultiYearPerformance.java

package org.frcpm.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Multi-Year Performance model for FRC teams.
 * 
 * Tracks and analyzes team performance across multiple seasons, providing
 * insights into trends, improvement areas, and long-term development patterns
 * for FRC teams.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-BusinessIntel
 * @since Phase 4B.3 Multi-Year Performance Tracking
 */
@Entity
@Table(name = "multi_year_performance")
public class MultiYearPerformance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Integer teamNumber;
    
    @Column(length = 100)
    private String teamName;
    
    @Column(nullable = false)
    private Integer season; // FRC season year (e.g., 2024)
    
    @Column(length = 50)
    private String gameName; // Game name for the season
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PerformanceCategory category;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MetricType metricType;
    
    // Performance Metrics
    @Column(nullable = false)
    private Double metricValue = 0.0;
    
    @Column(nullable = false)
    private Double weightedScore = 0.0; // Normalized score for comparison
    
    @Column(nullable = false)
    private Integer rank = 0; // Ranking within category and season
    
    @Column(nullable = false)
    private Integer totalTeams = 0; // Total teams in comparison group
    
    @Column(nullable = false)
    private Double percentileRank = 0.0; // Percentile ranking (0-100)
    
    // Competition Performance
    @Column(nullable = false)
    private Integer eventsAttended = 0;
    
    @Column(nullable = false)
    private Integer eventsWon = 0;
    
    @Column(nullable = false)
    private Integer eventsFinalist = 0;
    
    @Column(nullable = false)
    private Integer eventsSemifinals = 0;
    
    @Column(nullable = false)
    private Double averageRankingPoints = 0.0;
    
    @Column(nullable = false)
    private Integer totalAwards = 0;
    
    @Column(nullable = false)
    private Integer majorAwards = 0; // Chairman's, Engineering Inspiration, etc.
    
    @Column(nullable = false)
    private Integer districtPoints = 0;
    
    @Column(nullable = false)
    private Boolean qualifiedForChampionship = false;
    
    // Team Development Metrics
    @Column(nullable = false)
    private Integer teamSize = 0;
    
    @Column(nullable = false)
    private Integer rookieMembers = 0;
    
    @Column(nullable = false)
    private Integer veteranMembers = 0;
    
    @Column(nullable = false)
    private Integer mentorCount = 0;
    
    @Column(nullable = false)
    private Double teamRetentionRate = 0.0; // Percentage of returning members
    
    @Column(nullable = false)
    private Integer projectsCompleted = 0;
    
    @Column(nullable = false)
    private Double averageProjectScore = 0.0;
    
    // Technical Performance
    @Column(nullable = false)
    private Double robotReliabilityScore = 0.0; // Based on match performance
    
    @Column(nullable = false)
    private Double innovationScore = 0.0; // Innovation in design/approach
    
    @Column(nullable = false)
    private Double qualityScore = 0.0; // Build quality and craftsmanship
    
    @Column(nullable = false)
    private Double strategyScore = 0.0; // Strategic game play effectiveness
    
    @Column(nullable = false)
    private Integer codeCommits = 0; // Software development activity
    
    @Column(nullable = false)
    private Integer designIterations = 0; // Design process maturity
    
    // Financial and Resource Metrics
    @Column(nullable = false)
    private Double budgetUtilization = 0.0; // Percentage of budget used effectively
    
    @Column(nullable = false)
    private Double costPerPoint = 0.0; // Cost efficiency metric
    
    @Column(nullable = false)
    private Integer sponsorshipCount = 0;
    
    @Column(nullable = false)
    private Double totalFunding = 0.0;
    
    @Column(nullable = false)
    private Double fundraisingEfficiency = 0.0; // Success rate of fundraising
    
    // Outreach and Community Impact
    @Column(nullable = false)
    private Integer outreachEvents = 0;
    
    @Column(nullable = false)
    private Integer studentsImpacted = 0; // Through outreach
    
    @Column(nullable = false)
    private Integer communityPartnerships = 0;
    
    @Column(nullable = false)
    private Double sustainabilityScore = 0.0; // Long-term program sustainability
    
    @Column(nullable = false)
    private Integer volunteersEngaged = 0;
    
    // Trend Analysis Data
    @Column(nullable = false)
    private Double yearOverYearChange = 0.0; // Percentage change from previous year
    
    @Column(nullable = false)
    private Double threeYearTrend = 0.0; // 3-year average trend
    
    @Column(nullable = false)
    private Double fiveYearTrend = 0.0; // 5-year average trend
    
    @Column(nullable = false)
    private Boolean isImproving = false; // Positive trend indicator
    
    @Column(nullable = false)
    private Boolean isAtRisk = false; // Declining performance indicator
    
    // Comparative Analysis
    @Column(nullable = false)
    private Double peerAverageMetric = 0.0; // Average of similar teams
    
    @Column(nullable = false)
    private Double regionalAverageMetric = 0.0; // Regional average
    
    @Column(nullable = false)
    private Double nationalAverageMetric = 0.0; // National average
    
    @Column(nullable = false)
    private Double competitiveAdvantage = 0.0; // Advantage over peers
    
    // Analysis Metadata
    @Column(length = 2000)
    private String performanceSummary;
    
    @Column(length = 2000)
    private String strengthsIdentified;
    
    @Column(length = 2000)
    private String improvementAreas;
    
    @Column(length = 2000)
    private String trendAnalysis;
    
    @Column(length = 1000)
    private String recommendedActions;
    
    // Data Quality and Sources
    @Column(nullable = false)
    private Double dataCompleteness = 0.0; // Percentage of data available
    
    @Column(nullable = false)
    private Double dataAccuracy = 0.0; // Estimated accuracy of data
    
    @Column(length = 1000)
    private String dataSources; // Sources of performance data
    
    @Column
    private LocalDate dataCollectionDate;
    
    @Column
    private LocalDate lastVerificationDate;
    
    // Administrative Fields
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private TeamMember createdBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by_id")
    private TeamMember verifiedBy;
    
    @Column
    private LocalDateTime verifiedAt;
    
    /**
     * Categories of performance metrics
     */
    public enum PerformanceCategory {
        COMPETITION("Competition Performance", "Performance in FRC competitions"),
        TECHNICAL("Technical Excellence", "Robot design and technical capabilities"),
        TEAM_DEVELOPMENT("Team Development", "Team growth and member development"),
        OUTREACH("Outreach & Impact", "Community outreach and FIRST impact"),
        SUSTAINABILITY("Program Sustainability", "Long-term program viability"),
        INNOVATION("Innovation & Creativity", "Innovation in approach and design"),
        COLLABORATION("Collaboration & Spirit", "Teamwork and gracious professionalism"),
        LEADERSHIP("Leadership Development", "Student and mentor leadership"),
        FINANCIAL("Financial Management", "Budget and resource management"),
        SAFETY("Safety Excellence", "Safety culture and practices"),
        OVERALL("Overall Performance", "Comprehensive performance assessment");
        
        private final String displayName;
        private final String description;
        
        PerformanceCategory(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    /**
     * Types of performance metrics
     */
    public enum MetricType {
        WIN_RATE("Win Rate", "Percentage of matches won"),
        RANKING_POINTS("Ranking Points", "Average ranking points per match"),
        AWARD_COUNT("Award Count", "Total awards received"),
        DISTRICT_POINTS("District Points", "Total district championship points"),
        TEAM_SIZE("Team Size", "Number of active team members"),
        RETENTION_RATE("Retention Rate", "Percentage of returning members"),
        BUDGET_EFFICIENCY("Budget Efficiency", "Effective use of financial resources"),
        OUTREACH_IMPACT("Outreach Impact", "Number of students/community impacted"),
        INNOVATION_INDEX("Innovation Index", "Innovation and creativity score"),
        RELIABILITY_SCORE("Reliability Score", "Robot performance consistency"),
        QUALITY_INDEX("Quality Index", "Build quality and craftsmanship"),
        SUSTAINABILITY_INDEX("Sustainability Index", "Program long-term viability"),
        LEADERSHIP_DEVELOPMENT("Leadership Development", "Student leadership growth"),
        COMMUNITY_ENGAGEMENT("Community Engagement", "Community partnership level"),
        SAFETY_RECORD("Safety Record", "Safety incident rate and culture"),
        MENTOR_ENGAGEMENT("Mentor Engagement", "Mentor participation and effectiveness"),
        FUNDRAISING_SUCCESS("Fundraising Success", "Fundraising effectiveness"),
        PROJECT_COMPLETION("Project Completion", "Rate of successful project completion"),
        TECHNICAL_ADVANCEMENT("Technical Advancement", "Technical skill development"),
        COLLABORATIVE_SPIRIT("Collaborative Spirit", "Gracious professionalism score");
        
        private final String displayName;
        private final String description;
        
        MetricType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    // Constructors
    public MultiYearPerformance() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public MultiYearPerformance(Integer teamNumber, Integer season, 
                              PerformanceCategory category, MetricType metricType) {
        this();
        this.teamNumber = teamNumber;
        this.season = season;
        this.category = category;
        this.metricType = metricType;
        this.dataCollectionDate = LocalDate.now();
    }
    
    // Business Methods
    
    /**
     * Calculates performance grade based on percentile rank.
     */
    public String getPerformanceGrade() {
        if (percentileRank >= 95) return "A+";
        if (percentileRank >= 90) return "A";
        if (percentileRank >= 85) return "A-";
        if (percentileRank >= 80) return "B+";
        if (percentileRank >= 75) return "B";
        if (percentileRank >= 70) return "B-";
        if (percentileRank >= 65) return "C+";
        if (percentileRank >= 60) return "C";
        if (percentileRank >= 55) return "C-";
        if (percentileRank >= 50) return "D+";
        if (percentileRank >= 45) return "D";
        return "F";
    }
    
    /**
     * Determines if performance is above average.
     */
    public boolean isAboveAverage() {
        return percentileRank > 50.0;
    }
    
    /**
     * Determines if performance is in top tier.
     */
    public boolean isTopTier() {
        return percentileRank >= 80.0;
    }
    
    /**
     * Determines if performance is elite level.
     */
    public boolean isEliteLevel() {
        return percentileRank >= 95.0;
    }
    
    /**
     * Calculates performance improvement from previous year.
     */
    public String getImprovementStatus() {
        if (yearOverYearChange > 10.0) return "SIGNIFICANT_IMPROVEMENT";
        if (yearOverYearChange > 5.0) return "MODERATE_IMPROVEMENT";
        if (yearOverYearChange > 0.0) return "SLIGHT_IMPROVEMENT";
        if (yearOverYearChange > -5.0) return "STABLE";
        if (yearOverYearChange > -10.0) return "SLIGHT_DECLINE";
        return "SIGNIFICANT_DECLINE";
    }
    
    /**
     * Gets trend direction over multiple years.
     */
    public String getLongTermTrend() {
        if (fiveYearTrend > 5.0) return "STRONG_UPWARD";
        if (fiveYearTrend > 2.0) return "UPWARD";
        if (fiveYearTrend > -2.0) return "STABLE";
        if (fiveYearTrend > -5.0) return "DOWNWARD";
        return "STRONG_DOWNWARD";
    }
    
    /**
     * Calculates competitive position relative to peers.
     */
    public String getCompetitivePosition() {
        if (competitiveAdvantage > 20.0) return "MARKET_LEADER";
        if (competitiveAdvantage > 10.0) return "STRONG_PERFORMER";
        if (competitiveAdvantage > 0.0) return "ABOVE_AVERAGE";
        if (competitiveAdvantage > -10.0) return "AVERAGE";
        if (competitiveAdvantage > -20.0) return "BELOW_AVERAGE";
        return "STRUGGLING";
    }
    
    /**
     * Determines data reliability level.
     */
    public String getDataReliability() {
        double reliability = (dataCompleteness + dataAccuracy) / 2.0;
        if (reliability >= 95.0) return "VERY_HIGH";
        if (reliability >= 85.0) return "HIGH";
        if (reliability >= 75.0) return "MEDIUM";
        if (reliability >= 60.0) return "LOW";
        return "VERY_LOW";
    }
    
    /**
     * Calculates days since last data update.
     */
    public int getDaysSinceUpdate() {
        if (dataCollectionDate == null) return -1;
        return (int) java.time.temporal.ChronoUnit.DAYS.between(dataCollectionDate, LocalDate.now());
    }
    
    /**
     * Determines if data needs refresh.
     */
    public boolean needsDataRefresh() {
        return getDaysSinceUpdate() > 30; // More than 30 days old
    }
    
    /**
     * Calculates performance consistency score.
     */
    public double getConsistencyScore() {
        // Lower variability in year-over-year changes indicates higher consistency
        if (threeYearTrend == 0.0) return 100.0;
        return Math.max(0.0, 100.0 - Math.abs(threeYearTrend - yearOverYearChange));
    }
    
    /**
     * Gets risk assessment for team performance.
     */
    public String getRiskAssessment() {
        if (isAtRisk && yearOverYearChange < -10.0) return "HIGH_RISK";
        if (isAtRisk || yearOverYearChange < -5.0) return "MEDIUM_RISK";
        if (yearOverYearChange < 0.0) return "LOW_RISK";
        return "NO_RISK";
    }
    
    /**
     * Calculates benchmark comparison score.
     */
    public Map<String, Double> getBenchmarkComparison() {
        Map<String, Double> comparison = new HashMap<>();
        
        if (peerAverageMetric > 0) {
            comparison.put("vsPeers", (metricValue / peerAverageMetric - 1.0) * 100.0);
        }
        if (regionalAverageMetric > 0) {
            comparison.put("vsRegional", (metricValue / regionalAverageMetric - 1.0) * 100.0);
        }
        if (nationalAverageMetric > 0) {
            comparison.put("vsNational", (metricValue / nationalAverageMetric - 1.0) * 100.0);
        }
        
        return comparison;
    }
    
    /**
     * Generates performance insights summary.
     */
    public String generateInsightsSummary() {
        StringBuilder insights = new StringBuilder();
        
        insights.append("Performance Grade: ").append(getPerformanceGrade());
        insights.append(" (").append(String.format("%.1f", percentileRank)).append("th percentile)");
        
        insights.append("\nTrend: ").append(getImprovementStatus());
        if (yearOverYearChange != 0.0) {
            insights.append(" (").append(String.format("%.1f%%", yearOverYearChange)).append(" change)");
        }
        
        insights.append("\nCompetitive Position: ").append(getCompetitivePosition());
        
        if (isTopTier()) {
            insights.append("\n⭐ Top-tier performance");
        }
        if (isImproving) {
            insights.append("\n📈 Positive trajectory");
        }
        if (isAtRisk) {
            insights.append("\n⚠️ Performance at risk");
        }
        
        return insights.toString();
    }
    
    /**
     * Calculates weighted performance score for overall ranking.
     */
    public double calculateWeightedPerformanceScore() {
        double baseScore = percentileRank;
        
        // Apply category weights
        double categoryWeight = switch (category) {
            case COMPETITION -> 1.2;      // Competition performance weighted higher
            case TECHNICAL -> 1.1;        // Technical excellence important
            case TEAM_DEVELOPMENT -> 1.0; // Standard weight
            case OUTREACH -> 0.9;         // Important but lower weight
            case SUSTAINABILITY -> 1.0;   // Standard weight
            default -> 1.0;               // Standard weight for others
        };
        
        // Apply trend multiplier
        double trendMultiplier = 1.0;
        if (isImproving && yearOverYearChange > 5.0) {
            trendMultiplier = 1.1; // Bonus for strong improvement
        } else if (isAtRisk && yearOverYearChange < -10.0) {
            trendMultiplier = 0.9; // Penalty for significant decline
        }
        
        return baseScore * categoryWeight * trendMultiplier;
    }
    
    /**
     * Updates trend analysis based on historical data.
     */
    public void updateTrendAnalysis(List<Double> historicalValues) {
        if (historicalValues.isEmpty()) return;
        
        // Calculate year-over-year change
        if (historicalValues.size() >= 2) {
            double previousValue = historicalValues.get(historicalValues.size() - 2);
            if (previousValue > 0) {
                this.yearOverYearChange = ((metricValue / previousValue) - 1.0) * 100.0;
            }
        }
        
        // Calculate multi-year trends
        if (historicalValues.size() >= 3) {
            double sum = historicalValues.stream().mapToDouble(Double::doubleValue).sum();
            double average = sum / historicalValues.size();
            this.threeYearTrend = ((metricValue / average) - 1.0) * 100.0;
        }
        
        if (historicalValues.size() >= 5) {
            double fiveYearSum = historicalValues.subList(historicalValues.size() - 5, historicalValues.size())
                    .stream().mapToDouble(Double::doubleValue).sum();
            double fiveYearAverage = fiveYearSum / 5.0;
            this.fiveYearTrend = ((metricValue / fiveYearAverage) - 1.0) * 100.0;
        }
        
        // Update improvement and risk flags
        this.isImproving = yearOverYearChange > 0 && threeYearTrend > 0;
        this.isAtRisk = yearOverYearChange < -10.0 || threeYearTrend < -15.0;
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
    
    public String getGameName() { return gameName; }
    public void setGameName(String gameName) { this.gameName = gameName; }
    
    public PerformanceCategory getCategory() { return category; }
    public void setCategory(PerformanceCategory category) { this.category = category; }
    
    public MetricType getMetricType() { return metricType; }
    public void setMetricType(MetricType metricType) { this.metricType = metricType; }
    
    public Double getMetricValue() { return metricValue; }
    public void setMetricValue(Double metricValue) { this.metricValue = metricValue; }
    
    public Double getWeightedScore() { return weightedScore; }
    public void setWeightedScore(Double weightedScore) { this.weightedScore = weightedScore; }
    
    public Integer getRank() { return rank; }
    public void setRank(Integer rank) { this.rank = rank; }
    
    public Integer getTotalTeams() { return totalTeams; }
    public void setTotalTeams(Integer totalTeams) { this.totalTeams = totalTeams; }
    
    public Double getPercentileRank() { return percentileRank; }
    public void setPercentileRank(Double percentileRank) { this.percentileRank = percentileRank; }
    
    public Integer getEventsAttended() { return eventsAttended; }
    public void setEventsAttended(Integer eventsAttended) { this.eventsAttended = eventsAttended; }
    
    public Integer getEventsWon() { return eventsWon; }
    public void setEventsWon(Integer eventsWon) { this.eventsWon = eventsWon; }
    
    public Integer getEventsFinalist() { return eventsFinalist; }
    public void setEventsFinalist(Integer eventsFinalist) { this.eventsFinalist = eventsFinalist; }
    
    public Integer getEventsSemifinals() { return eventsSemifinals; }
    public void setEventsSemifinals(Integer eventsSemifinals) { this.eventsSemifinals = eventsSemifinals; }
    
    public Double getAverageRankingPoints() { return averageRankingPoints; }
    public void setAverageRankingPoints(Double averageRankingPoints) { this.averageRankingPoints = averageRankingPoints; }
    
    public Integer getTotalAwards() { return totalAwards; }
    public void setTotalAwards(Integer totalAwards) { this.totalAwards = totalAwards; }
    
    public Integer getMajorAwards() { return majorAwards; }
    public void setMajorAwards(Integer majorAwards) { this.majorAwards = majorAwards; }
    
    public Integer getDistrictPoints() { return districtPoints; }
    public void setDistrictPoints(Integer districtPoints) { this.districtPoints = districtPoints; }
    
    public Boolean getQualifiedForChampionship() { return qualifiedForChampionship; }
    public void setQualifiedForChampionship(Boolean qualifiedForChampionship) { this.qualifiedForChampionship = qualifiedForChampionship; }
    
    public Integer getTeamSize() { return teamSize; }
    public void setTeamSize(Integer teamSize) { this.teamSize = teamSize; }
    
    public Integer getRookieMembers() { return rookieMembers; }
    public void setRookieMembers(Integer rookieMembers) { this.rookieMembers = rookieMembers; }
    
    public Integer getVeteranMembers() { return veteranMembers; }
    public void setVeteranMembers(Integer veteranMembers) { this.veteranMembers = veteranMembers; }
    
    public Integer getMentorCount() { return mentorCount; }
    public void setMentorCount(Integer mentorCount) { this.mentorCount = mentorCount; }
    
    public Double getTeamRetentionRate() { return teamRetentionRate; }
    public void setTeamRetentionRate(Double teamRetentionRate) { this.teamRetentionRate = teamRetentionRate; }
    
    public Integer getProjectsCompleted() { return projectsCompleted; }
    public void setProjectsCompleted(Integer projectsCompleted) { this.projectsCompleted = projectsCompleted; }
    
    public Double getAverageProjectScore() { return averageProjectScore; }
    public void setAverageProjectScore(Double averageProjectScore) { this.averageProjectScore = averageProjectScore; }
    
    public Double getRobotReliabilityScore() { return robotReliabilityScore; }
    public void setRobotReliabilityScore(Double robotReliabilityScore) { this.robotReliabilityScore = robotReliabilityScore; }
    
    public Double getInnovationScore() { return innovationScore; }
    public void setInnovationScore(Double innovationScore) { this.innovationScore = innovationScore; }
    
    public Double getQualityScore() { return qualityScore; }
    public void setQualityScore(Double qualityScore) { this.qualityScore = qualityScore; }
    
    public Double getStrategyScore() { return strategyScore; }
    public void setStrategyScore(Double strategyScore) { this.strategyScore = strategyScore; }
    
    public Integer getCodeCommits() { return codeCommits; }
    public void setCodeCommits(Integer codeCommits) { this.codeCommits = codeCommits; }
    
    public Integer getDesignIterations() { return designIterations; }
    public void setDesignIterations(Integer designIterations) { this.designIterations = designIterations; }
    
    public Double getBudgetUtilization() { return budgetUtilization; }
    public void setBudgetUtilization(Double budgetUtilization) { this.budgetUtilization = budgetUtilization; }
    
    public Double getCostPerPoint() { return costPerPoint; }
    public void setCostPerPoint(Double costPerPoint) { this.costPerPoint = costPerPoint; }
    
    public Integer getSponsorshipCount() { return sponsorshipCount; }
    public void setSponsorshipCount(Integer sponsorshipCount) { this.sponsorshipCount = sponsorshipCount; }
    
    public Double getTotalFunding() { return totalFunding; }
    public void setTotalFunding(Double totalFunding) { this.totalFunding = totalFunding; }
    
    public Double getFundraisingEfficiency() { return fundraisingEfficiency; }
    public void setFundraisingEfficiency(Double fundraisingEfficiency) { this.fundraisingEfficiency = fundraisingEfficiency; }
    
    public Integer getOutreachEvents() { return outreachEvents; }
    public void setOutreachEvents(Integer outreachEvents) { this.outreachEvents = outreachEvents; }
    
    public Integer getStudentsImpacted() { return studentsImpacted; }
    public void setStudentsImpacted(Integer studentsImpacted) { this.studentsImpacted = studentsImpacted; }
    
    public Integer getCommunityPartnerships() { return communityPartnerships; }
    public void setCommunityPartnerships(Integer communityPartnerships) { this.communityPartnerships = communityPartnerships; }
    
    public Double getSustainabilityScore() { return sustainabilityScore; }
    public void setSustainabilityScore(Double sustainabilityScore) { this.sustainabilityScore = sustainabilityScore; }
    
    public Integer getVolunteersEngaged() { return volunteersEngaged; }
    public void setVolunteersEngaged(Integer volunteersEngaged) { this.volunteersEngaged = volunteersEngaged; }
    
    public Double getYearOverYearChange() { return yearOverYearChange; }
    public void setYearOverYearChange(Double yearOverYearChange) { this.yearOverYearChange = yearOverYearChange; }
    
    public Double getThreeYearTrend() { return threeYearTrend; }
    public void setThreeYearTrend(Double threeYearTrend) { this.threeYearTrend = threeYearTrend; }
    
    public Double getFiveYearTrend() { return fiveYearTrend; }
    public void setFiveYearTrend(Double fiveYearTrend) { this.fiveYearTrend = fiveYearTrend; }
    
    public Boolean getIsImproving() { return isImproving; }
    public void setIsImproving(Boolean isImproving) { this.isImproving = isImproving; }
    
    public Boolean getIsAtRisk() { return isAtRisk; }
    public void setIsAtRisk(Boolean isAtRisk) { this.isAtRisk = isAtRisk; }
    
    public Double getPeerAverageMetric() { return peerAverageMetric; }
    public void setPeerAverageMetric(Double peerAverageMetric) { this.peerAverageMetric = peerAverageMetric; }
    
    public Double getRegionalAverageMetric() { return regionalAverageMetric; }
    public void setRegionalAverageMetric(Double regionalAverageMetric) { this.regionalAverageMetric = regionalAverageMetric; }
    
    public Double getNationalAverageMetric() { return nationalAverageMetric; }
    public void setNationalAverageMetric(Double nationalAverageMetric) { this.nationalAverageMetric = nationalAverageMetric; }
    
    public Double getCompetitiveAdvantage() { return competitiveAdvantage; }
    public void setCompetitiveAdvantage(Double competitiveAdvantage) { this.competitiveAdvantage = competitiveAdvantage; }
    
    public String getPerformanceSummary() { return performanceSummary; }
    public void setPerformanceSummary(String performanceSummary) { this.performanceSummary = performanceSummary; }
    
    public String getStrengthsIdentified() { return strengthsIdentified; }
    public void setStrengthsIdentified(String strengthsIdentified) { this.strengthsIdentified = strengthsIdentified; }
    
    public String getImprovementAreas() { return improvementAreas; }
    public void setImprovementAreas(String improvementAreas) { this.improvementAreas = improvementAreas; }
    
    public String getTrendAnalysis() { return trendAnalysis; }
    public void setTrendAnalysis(String trendAnalysis) { this.trendAnalysis = trendAnalysis; }
    
    public String getRecommendedActions() { return recommendedActions; }
    public void setRecommendedActions(String recommendedActions) { this.recommendedActions = recommendedActions; }
    
    public Double getDataCompleteness() { return dataCompleteness; }
    public void setDataCompleteness(Double dataCompleteness) { this.dataCompleteness = dataCompleteness; }
    
    public Double getDataAccuracy() { return dataAccuracy; }
    public void setDataAccuracy(Double dataAccuracy) { this.dataAccuracy = dataAccuracy; }
    
    public String getDataSources() { return dataSources; }
    public void setDataSources(String dataSources) { this.dataSources = dataSources; }
    
    public LocalDate getDataCollectionDate() { return dataCollectionDate; }
    public void setDataCollectionDate(LocalDate dataCollectionDate) { this.dataCollectionDate = dataCollectionDate; }
    
    public LocalDate getLastVerificationDate() { return lastVerificationDate; }
    public void setLastVerificationDate(LocalDate lastVerificationDate) { this.lastVerificationDate = lastVerificationDate; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public TeamMember getCreatedBy() { return createdBy; }
    public void setCreatedBy(TeamMember createdBy) { this.createdBy = createdBy; }
    
    public TeamMember getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(TeamMember verifiedBy) { this.verifiedBy = verifiedBy; }
    
    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }
    
    @Override
    public String toString() {
        return String.format("MultiYearPerformance{id=%d, team=%d, season=%d, category=%s, metric=%s, value=%.2f, percentile=%.1f}", 
                           id, teamNumber, season, category, metricType, metricValue, percentileRank);
    }
}