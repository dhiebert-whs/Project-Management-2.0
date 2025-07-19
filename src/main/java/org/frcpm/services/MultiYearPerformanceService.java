// src/main/java/org/frcpm/services/MultiYearPerformanceService.java

package org.frcpm.services;

import org.frcpm.models.MultiYearPerformance;
import org.frcpm.models.TeamMember;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for managing multi-year performance tracking.
 * 
 * Provides comprehensive multi-year performance analysis, trend tracking,
 * competitive benchmarking, and improvement recommendations for FRC teams.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-BusinessIntel
 * @since Phase 4B.3 Multi-Year Performance Tracking
 */
public interface MultiYearPerformanceService extends Service<MultiYearPerformance, Long> {
    
    // =========================================================================
    // PERFORMANCE RECORD MANAGEMENT
    // =========================================================================
    
    /**
     * Creates a new performance record.
     * 
     * @param performance the performance record to create
     * @return the created performance record
     * @throws IllegalArgumentException if performance data is invalid
     */
    MultiYearPerformance createPerformanceRecord(MultiYearPerformance performance);
    
    /**
     * Creates a performance record with basic information.
     * 
     * @param teamNumber the team number
     * @param season the season year
     * @param category the performance category
     * @param metricType the metric type
     * @param metricValue the metric value
     * @param createdBy the team member creating the record
     * @return the created performance record
     */
    MultiYearPerformance createPerformanceRecord(Integer teamNumber, Integer season,
                                                MultiYearPerformance.PerformanceCategory category,
                                                MultiYearPerformance.MetricType metricType,
                                                Double metricValue, TeamMember createdBy);
    
    /**
     * Updates an existing performance record.
     * 
     * @param performanceId the performance record ID
     * @param performance the updated performance data
     * @return the updated performance record
     */
    MultiYearPerformance updatePerformanceRecord(Long performanceId, MultiYearPerformance performance);
    
    /**
     * Verifies a performance record for accuracy.
     * 
     * @param performanceId the performance record ID
     * @param verifiedBy the team member verifying the record
     * @return the verified performance record
     */
    MultiYearPerformance verifyPerformanceRecord(Long performanceId, TeamMember verifiedBy);
    
    /**
     * Deactivates a performance record.
     * 
     * @param performanceId the performance record ID
     * @return the deactivated performance record
     */
    MultiYearPerformance deactivatePerformanceRecord(Long performanceId);
    
    // =========================================================================
    // PERFORMANCE DISCOVERY AND RETRIEVAL
    // =========================================================================
    
    /**
     * Finds all active performance records.
     * 
     * @return list of active performance records
     */
    List<MultiYearPerformance> findActivePerformanceRecords();
    
    /**
     * Finds performance records by team number.
     * 
     * @param teamNumber the team number
     * @return list of performance records for the team
     */
    List<MultiYearPerformance> findByTeamNumber(Integer teamNumber);
    
    /**
     * Finds performance records by season.
     * 
     * @param season the season year
     * @return list of performance records for the season
     */
    List<MultiYearPerformance> findBySeason(Integer season);
    
    /**
     * Finds performance records by category.
     * 
     * @param category the performance category
     * @return list of performance records for the category
     */
    List<MultiYearPerformance> findByCategory(MultiYearPerformance.PerformanceCategory category);
    
    /**
     * Finds performance records by metric type.
     * 
     * @param metricType the metric type
     * @return list of performance records for the metric type
     */
    List<MultiYearPerformance> findByMetricType(MultiYearPerformance.MetricType metricType);
    
    /**
     * Gets specific performance record.
     * 
     * @param teamNumber the team number
     * @param season the season year
     * @param category the performance category
     * @param metricType the metric type
     * @return the performance record, if found
     */
    Optional<MultiYearPerformance> getPerformanceRecord(Integer teamNumber, Integer season,
                                                       MultiYearPerformance.PerformanceCategory category,
                                                       MultiYearPerformance.MetricType metricType);
    
    // =========================================================================
    // TEAM PERFORMANCE ANALYSIS
    // =========================================================================
    
    /**
     * Gets comprehensive team performance history.
     * 
     * @param teamNumber the team number
     * @return team performance history across all seasons
     */
    List<MultiYearPerformance> getTeamPerformanceHistory(Integer teamNumber);
    
    /**
     * Gets team performance for specific category across seasons.
     * 
     * @param teamNumber the team number
     * @param category the performance category
     * @return team category performance history
     */
    List<MultiYearPerformance> getTeamCategoryHistory(Integer teamNumber, 
                                                     MultiYearPerformance.PerformanceCategory category);
    
    /**
     * Analyzes team's best performing seasons.
     * 
     * @param teamNumber the team number
     * @return map of seasons to performance metrics
     */
    Map<Integer, Map<String, Object>> analyzeTeamBestSeasons(Integer teamNumber);
    
    /**
     * Gets team performance trends analysis.
     * 
     * @param teamNumber the team number
     * @return performance trends by category
     */
    Map<MultiYearPerformance.PerformanceCategory, Map<String, Object>> getTeamPerformanceTrends(Integer teamNumber);
    
    /**
     * Calculates team's overall performance score.
     * 
     * @param teamNumber the team number
     * @param season the season year
     * @return overall weighted performance score
     */
    double calculateTeamOverallScore(Integer teamNumber, Integer season);
    
    /**
     * Generates team performance report.
     * 
     * @param teamNumber the team number
     * @param seasons number of seasons to include
     * @return comprehensive performance report
     */
    Map<String, Object> generateTeamPerformanceReport(Integer teamNumber, int seasons);
    
    // =========================================================================
    // TREND ANALYSIS AND PROJECTIONS
    // =========================================================================
    
    /**
     * Calculates performance trends across multiple seasons.
     * 
     * @param startSeason the start season
     * @param endSeason the end season
     * @return multi-season trend analysis
     */
    Map<String, Object> analyzeMultiSeasonTrends(Integer startSeason, Integer endSeason);
    
    /**
     * Analyzes improvement patterns by category.
     * 
     * @return improvement pattern analysis
     */
    Map<MultiYearPerformance.PerformanceCategory, Map<String, Object>> analyzeImprovementPatterns();
    
    /**
     * Finds consistent top performers across seasons.
     * 
     * @param percentileThreshold minimum percentile threshold
     * @param minSeasons minimum number of seasons
     * @return list of consistent top performers
     */
    List<Map<String, Object>> findConsistentTopPerformers(double percentileThreshold, int minSeasons);
    
    /**
     * Projects team performance for upcoming season.
     * 
     * @param teamNumber the team number
     * @param targetSeason the target season
     * @return performance projections
     */
    Map<String, Object> projectTeamPerformance(Integer teamNumber, Integer targetSeason);
    
    /**
     * Identifies performance outliers for investigation.
     * 
     * @param changeThreshold threshold for significant change
     * @return list of performance outliers
     */
    List<MultiYearPerformance> findPerformanceOutliers(double changeThreshold);
    
    // =========================================================================
    // COMPETITIVE ANALYSIS AND BENCHMARKING
    // =========================================================================
    
    /**
     * Gets season performance rankings by category.
     * 
     * @param season the season year
     * @param category the performance category
     * @return ranked list of team performances
     */
    List<MultiYearPerformance> getSeasonRankings(Integer season, MultiYearPerformance.PerformanceCategory category);
    
    /**
     * Gets top performers for specific metric.
     * 
     * @param season the season year
     * @param metricType the metric type
     * @param limit number of top performers to return
     * @return list of top performers
     */
    List<MultiYearPerformance> getTopPerformers(Integer season, MultiYearPerformance.MetricType metricType, int limit);
    
    /**
     * Analyzes competitive landscape for a season.
     * 
     * @param season the season year
     * @return competitive landscape analysis
     */
    Map<String, Object> analyzeCompetitiveLandscape(Integer season);
    
    /**
     * Finds market leaders by category.
     * 
     * @param season the season year
     * @param category the performance category
     * @param advantageThreshold competitive advantage threshold
     * @return list of market leaders
     */
    List<MultiYearPerformance> getMarketLeaders(Integer season, MultiYearPerformance.PerformanceCategory category,
                                               double advantageThreshold);
    
    /**
     * Calculates benchmark standards for metrics.
     * 
     * @param season the season year
     * @return benchmark standards by metric type
     */
    Map<MultiYearPerformance.MetricType, Map<String, Object>> calculateBenchmarkStandards(Integer season);
    
    /**
     * Analyzes peer group performance.
     * 
     * @param minTeamSize minimum team size
     * @param maxTeamSize maximum team size
     * @return peer group performance analysis
     */
    Map<String, Object> analyzePeerGroupPerformance(int minTeamSize, int maxTeamSize);
    
    // =========================================================================
    // IMPROVEMENT AND RISK ANALYSIS
    // =========================================================================
    
    /**
     * Finds most improved teams in a season.
     * 
     * @param season the season year
     * @return list of most improved teams
     */
    List<MultiYearPerformance> getMostImprovedTeams(Integer season);
    
    /**
     * Identifies at-risk teams requiring attention.
     * 
     * @param season the season year
     * @param declineThreshold threshold for performance decline
     * @return list of at-risk teams
     */
    List<MultiYearPerformance> getAtRiskTeams(Integer season, double declineThreshold);
    
    /**
     * Generates improvement recommendations for a team.
     * 
     * @param teamNumber the team number
     * @return improvement recommendations
     */
    List<Map<String, Object>> generateImprovementRecommendations(Integer teamNumber);
    
    /**
     * Analyzes team strengths and weaknesses.
     * 
     * @param teamNumber the team number
     * @param season the season year
     * @return strengths and weaknesses analysis
     */
    Map<String, Object> analyzeTeamStrengthsWeaknesses(Integer teamNumber, Integer season);
    
    /**
     * Identifies performance risk factors.
     * 
     * @param teamNumber the team number
     * @return risk factor analysis
     */
    Map<String, Object> identifyPerformanceRiskFactors(Integer teamNumber);
    
    // =========================================================================
    // SEASONAL AND STATISTICAL ANALYSIS
    // =========================================================================
    
    /**
     * Calculates season averages by category.
     * 
     * @param season the season year
     * @return season averages by category
     */
    Map<MultiYearPerformance.PerformanceCategory, Map<String, Object>> getSeasonAverages(Integer season);
    
    /**
     * Gets performance distribution statistics.
     * 
     * @param season the season year
     * @return performance distribution by category and metric
     */
    Map<String, Object> getPerformanceDistribution(Integer season);
    
    /**
     * Calculates growth rates by category.
     * 
     * @return growth rate analysis by category
     */
    Map<MultiYearPerformance.PerformanceCategory, Map<String, Object>> calculateGrowthRates();
    
    /**
     * Analyzes seasonal performance patterns.
     * 
     * @return seasonal pattern analysis
     */
    Map<String, Object> analyzeSeasonalPatterns();
    
    /**
     * Gets regional performance comparisons.
     * 
     * @param category the performance category
     * @return regional comparison analysis
     */
    Map<String, Object> getRegionalComparisons(MultiYearPerformance.PerformanceCategory category);
    
    /**
     * Determines excellence standards and best practices.
     * 
     * @return excellence standards by metric type
     */
    Map<MultiYearPerformance.MetricType, Map<String, Object>> getExcellenceStandards();
    
    // =========================================================================
    // DATA QUALITY AND VALIDATION
    // =========================================================================
    
    /**
     * Validates performance data quality.
     * 
     * @param performanceId the performance record ID
     * @return validation results
     */
    Map<String, Object> validateDataQuality(Long performanceId);
    
    /**
     * Finds records with low data quality.
     * 
     * @param completenessThreshold data completeness threshold
     * @param accuracyThreshold data accuracy threshold
     * @return list of low-quality records
     */
    List<MultiYearPerformance> findLowQualityData(double completenessThreshold, double accuracyThreshold);
    
    /**
     * Gets overall data quality statistics.
     * 
     * @return data quality statistics
     */
    Map<String, Object> getDataQualityStatistics();
    
    /**
     * Finds records needing verification.
     * 
     * @param verificationCutoff cutoff date for verification
     * @return list of records needing verification
     */
    List<MultiYearPerformance> findRecordsNeedingVerification(LocalDate verificationCutoff);
    
    /**
     * Updates data quality metrics for a record.
     * 
     * @param performanceId the performance record ID
     * @param completeness data completeness score
     * @param accuracy data accuracy score
     * @return updated performance record
     */
    MultiYearPerformance updateDataQuality(Long performanceId, double completeness, double accuracy);
    
    // =========================================================================
    // SEARCH AND FILTERING
    // =========================================================================
    
    /**
     * Searches performance records by team name.
     * 
     * @param searchTerm the search term
     * @return list of matching performance records
     */
    List<MultiYearPerformance> searchByTeamName(String searchTerm);
    
    /**
     * Advanced search with multiple criteria.
     * 
     * @param criteria search criteria map
     * @return list of matching performance records
     */
    List<MultiYearPerformance> advancedSearch(Map<String, Object> criteria);
    
    /**
     * Filters performance records by custom criteria.
     * 
     * @param teamNumber optional team number filter
     * @param season optional season filter
     * @param category optional category filter
     * @param metricType optional metric type filter
     * @param minPercentile optional minimum percentile filter
     * @param maxPercentile optional maximum percentile filter
     * @return list of filtered performance records
     */
    List<MultiYearPerformance> filterPerformanceRecords(Integer teamNumber, Integer season,
                                                        MultiYearPerformance.PerformanceCategory category,
                                                        MultiYearPerformance.MetricType metricType,
                                                        Double minPercentile, Double maxPercentile);
    
    /**
     * Finds teams by performance criteria.
     * 
     * @param percentileThreshold percentile threshold
     * @param seasonStart starting season
     * @return list of teams meeting criteria
     */
    List<Map<String, Object>> findTeamsByPerformanceCriteria(double percentileThreshold, int seasonStart);
    
    // =========================================================================
    // BULK OPERATIONS AND DATA MANAGEMENT
    // =========================================================================
    
    /**
     * Creates multiple performance records from data import.
     * 
     * @param performances the list of performance records to create
     * @return list of created performance records
     */
    List<MultiYearPerformance> createBulkPerformanceRecords(List<MultiYearPerformance> performances);
    
    /**
     * Updates multiple performance records.
     * 
     * @param performanceUpdates map of performance IDs to updated data
     * @return list of updated performance records
     */
    List<MultiYearPerformance> updateBulkPerformanceRecords(Map<Long, MultiYearPerformance> performanceUpdates);
    
    /**
     * Calculates and updates percentile rankings for a season.
     * 
     * @param season the season year
     * @param category the performance category
     * @return number of records updated
     */
    int updatePercentileRankings(Integer season, MultiYearPerformance.PerformanceCategory category);
    
    /**
     * Recalculates trend data for all teams.
     * 
     * @param season the season year
     * @return number of records updated
     */
    int recalculateTrendData(Integer season);
    
    /**
     * Imports performance data from external sources.
     * 
     * @param sourceType the external data source type
     * @param season the season year
     * @return import results
     */
    Map<String, Object> importPerformanceData(String sourceType, Integer season);
    
    // =========================================================================
    // ANALYTICS AND REPORTING
    // =========================================================================
    
    /**
     * Gets comprehensive performance analytics dashboard.
     * 
     * @return analytics dashboard data
     */
    Map<String, Object> getPerformanceAnalyticsDashboard();
    
    /**
     * Generates executive performance summary.
     * 
     * @param season the season year
     * @return executive summary report
     */
    Map<String, Object> generateExecutivePerformanceSummary(Integer season);
    
    /**
     * Creates detailed trend analysis report.
     * 
     * @param startSeason the start season
     * @param endSeason the end season
     * @return trend analysis report
     */
    Map<String, Object> createTrendAnalysisReport(Integer startSeason, Integer endSeason);
    
    /**
     * Generates competitive intelligence report.
     * 
     * @param season the season year
     * @return competitive intelligence report
     */
    Map<String, Object> generateCompetitiveIntelligenceReport(Integer season);
    
    /**
     * Creates team development recommendations.
     * 
     * @param teamNumber the team number
     * @return development recommendations
     */
    Map<String, Object> createTeamDevelopmentRecommendations(Integer teamNumber);
    
    // =========================================================================
    // STATISTICAL ANALYSIS AND METRICS
    // =========================================================================
    
    /**
     * Calculates metric correlations for insights.
     * 
     * @return metric correlation analysis
     */
    Map<String, Object> calculateMetricCorrelations();
    
    /**
     * Performs statistical significance testing.
     * 
     * @param teamNumber the team number
     * @param metricType the metric type
     * @param seasons number of seasons to analyze
     * @return statistical significance results
     */
    Map<String, Object> performStatisticalSignificanceTest(Integer teamNumber, 
                                                           MultiYearPerformance.MetricType metricType, 
                                                           int seasons);
    
    /**
     * Calculates confidence intervals for projections.
     * 
     * @param teamNumber the team number
     * @param metricType the metric type
     * @return confidence interval analysis
     */
    Map<String, Object> calculateConfidenceIntervals(Integer teamNumber, MultiYearPerformance.MetricType metricType);
    
    /**
     * Analyzes performance volatility and consistency.
     * 
     * @param teamNumber the team number
     * @return volatility and consistency analysis
     */
    Map<String, Object> analyzePerformanceVolatility(Integer teamNumber);
    
    // =========================================================================
    // SYSTEM ADMINISTRATION
    // =========================================================================
    
    /**
     * Gets unique teams in the system.
     * 
     * @return list of unique teams
     */
    List<Map<String, Object>> getUniqueTeams();
    
    /**
     * Gets unique seasons in the system.
     * 
     * @return list of unique seasons
     */
    List<Integer> getUniqueSeasons();
    
    /**
     * Gets database statistics.
     * 
     * @return database statistics
     */
    Map<String, Object> getDatabaseStatistics();
    
    /**
     * Optimizes database performance.
     * 
     * @return optimization results
     */
    Map<String, Object> optimizeDatabasePerformance();
    
    /**
     * Archives old performance data.
     * 
     * @param cutoffSeason seasons before this will be archived
     * @return archival results
     */
    Map<String, Object> archiveOldPerformanceData(Integer cutoffSeason);
}