// src/main/java/org/frcpm/repositories/spring/MultiYearPerformanceRepository.java

package org.frcpm.repositories.spring;

import org.frcpm.models.MultiYearPerformance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for MultiYearPerformance entities.
 * 
 * Provides database access methods for multi-year performance tracking,
 * trend analysis, comparative benchmarking, and performance analytics.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-BusinessIntel
 * @since Phase 4B.3 Multi-Year Performance Tracking
 */
@Repository
public interface MultiYearPerformanceRepository extends JpaRepository<MultiYearPerformance, Long> {
    
    // =========================================================================
    // BASIC QUERIES
    // =========================================================================
    
    /**
     * Finds all active performance records.
     */
    List<MultiYearPerformance> findByIsActiveTrueOrderBySeasonDescTeamNumberAsc();
    
    /**
     * Finds performance records by team number.
     */
    List<MultiYearPerformance> findByTeamNumberAndIsActiveTrueOrderBySeasonDesc(Integer teamNumber);
    
    /**
     * Finds performance records by season.
     */
    List<MultiYearPerformance> findBySeasonAndIsActiveTrueOrderByTeamNumberAsc(Integer season);
    
    /**
     * Finds performance records by category.
     */
    List<MultiYearPerformance> findByCategoryAndIsActiveTrueOrderBySeasonDescTeamNumberAsc(MultiYearPerformance.PerformanceCategory category);
    
    /**
     * Finds performance records by metric type.
     */
    List<MultiYearPerformance> findByMetricTypeAndIsActiveTrueOrderBySeasonDescTeamNumberAsc(MultiYearPerformance.MetricType metricType);
    
    /**
     * Finds performance record for specific team, season, category, and metric.
     */
    Optional<MultiYearPerformance> findByTeamNumberAndSeasonAndCategoryAndMetricTypeAndIsActiveTrue(
            Integer teamNumber, Integer season, MultiYearPerformance.PerformanceCategory category, 
            MultiYearPerformance.MetricType metricType);
    
    // =========================================================================
    // TEAM PERFORMANCE ANALYSIS
    // =========================================================================
    
    /**
     * Gets team performance history across all seasons.
     */
    @Query("SELECT p FROM MultiYearPerformance p " +
           "WHERE p.teamNumber = :teamNumber " +
           "AND p.isActive = true " +
           "ORDER BY p.season DESC, p.category ASC")
    List<MultiYearPerformance> getTeamPerformanceHistory(@Param("teamNumber") Integer teamNumber);
    
    /**
     * Gets team performance for specific category across seasons.
     */
    @Query("SELECT p FROM MultiYearPerformance p " +
           "WHERE p.teamNumber = :teamNumber " +
           "AND p.category = :category " +
           "AND p.isActive = true " +
           "ORDER BY p.season DESC")
    List<MultiYearPerformance> getTeamCategoryHistory(@Param("teamNumber") Integer teamNumber, 
                                                     @Param("category") MultiYearPerformance.PerformanceCategory category);
    
    /**
     * Gets team's best performing seasons.
     */
    @Query("SELECT p.season, AVG(p.percentileRank) as avgPercentile, COUNT(p) as metricCount " +
           "FROM MultiYearPerformance p " +
           "WHERE p.teamNumber = :teamNumber " +
           "AND p.isActive = true " +
           "GROUP BY p.season " +
           "ORDER BY AVG(p.percentileRank) DESC")
    List<Object[]> getTeamBestSeasons(@Param("teamNumber") Integer teamNumber);
    
    /**
     * Gets team's performance trends.
     */
    @Query("SELECT p.category, AVG(p.yearOverYearChange) as avgChange, AVG(p.threeYearTrend) as avgTrend " +
           "FROM MultiYearPerformance p " +
           "WHERE p.teamNumber = :teamNumber " +
           "AND p.isActive = true " +
           "GROUP BY p.category " +
           "ORDER BY AVG(p.yearOverYearChange) DESC")
    List<Object[]> getTeamPerformanceTrends(@Param("teamNumber") Integer teamNumber);
    
    /**
     * Finds teams with consistent top performance.
     */
    @Query("SELECT p.teamNumber, p.teamName, COUNT(p) as seasons, AVG(p.percentileRank) as avgPercentile " +
           "FROM MultiYearPerformance p " +
           "WHERE p.isActive = true " +
           "AND p.percentileRank >= :percentileThreshold " +
           "GROUP BY p.teamNumber, p.teamName " +
           "HAVING COUNT(p) >= :minSeasons " +
           "ORDER BY AVG(p.percentileRank) DESC")
    List<Object[]> findConsistentTopPerformers(@Param("percentileThreshold") Double percentileThreshold,
                                              @Param("minSeasons") Integer minSeasons);
    
    // =========================================================================
    // SEASON AND COMPARATIVE ANALYSIS
    // =========================================================================
    
    /**
     * Gets season performance rankings by category.
     */
    @Query("SELECT p FROM MultiYearPerformance p " +
           "WHERE p.season = :season " +
           "AND p.category = :category " +
           "AND p.isActive = true " +
           "ORDER BY p.percentileRank DESC")
    List<MultiYearPerformance> getSeasonRankings(@Param("season") Integer season,
                                                @Param("category") MultiYearPerformance.PerformanceCategory category);
    
    /**
     * Gets top performers for a season and metric.
     */
    @Query("SELECT p FROM MultiYearPerformance p " +
           "WHERE p.season = :season " +
           "AND p.metricType = :metricType " +
           "AND p.isActive = true " +
           "ORDER BY p.metricValue DESC")
    List<MultiYearPerformance> getTopPerformers(@Param("season") Integer season,
                                               @Param("metricType") MultiYearPerformance.MetricType metricType);
    
    /**
     * Calculates season averages by category.
     */
    @Query("SELECT p.category, AVG(p.metricValue), AVG(p.percentileRank), COUNT(p) " +
           "FROM MultiYearPerformance p " +
           "WHERE p.season = :season " +
           "AND p.isActive = true " +
           "GROUP BY p.category " +
           "ORDER BY AVG(p.percentileRank) DESC")
    List<Object[]> getSeasonAverages(@Param("season") Integer season);
    
    /**
     * Finds most improved teams in a season.
     */
    @Query("SELECT p FROM MultiYearPerformance p " +
           "WHERE p.season = :season " +
           "AND p.isActive = true " +
           "AND p.yearOverYearChange > 0 " +
           "ORDER BY p.yearOverYearChange DESC")
    List<MultiYearPerformance> getMostImprovedTeams(@Param("season") Integer season);
    
    /**
     * Finds at-risk teams requiring attention.
     */
    @Query("SELECT p FROM MultiYearPerformance p " +
           "WHERE p.season = :season " +
           "AND p.isActive = true " +
           "AND (p.isAtRisk = true OR p.yearOverYearChange < :declineThreshold) " +
           "ORDER BY p.yearOverYearChange ASC")
    List<MultiYearPerformance> getAtRiskTeams(@Param("season") Integer season,
                                             @Param("declineThreshold") Double declineThreshold);
    
    // =========================================================================
    // TREND ANALYSIS AND STATISTICS
    // =========================================================================
    
    /**
     * Gets performance trends across multiple seasons.
     */
    @Query("SELECT p.season, p.category, AVG(p.metricValue), AVG(p.percentileRank) " +
           "FROM MultiYearPerformance p " +
           "WHERE p.isActive = true " +
           "AND p.season BETWEEN :startSeason AND :endSeason " +
           "GROUP BY p.season, p.category " +
           "ORDER BY p.season ASC, p.category ASC")
    List<Object[]> getMultiSeasonTrends(@Param("startSeason") Integer startSeason,
                                       @Param("endSeason") Integer endSeason);
    
    /**
     * Analyzes improvement patterns by category.
     */
    @Query("SELECT p.category, " +
           "       AVG(CASE WHEN p.isImproving = true THEN 1.0 ELSE 0.0 END) as improvementRate, " +
           "       AVG(p.yearOverYearChange) as avgChange, " +
           "       COUNT(p) as totalRecords " +
           "FROM MultiYearPerformance p " +
           "WHERE p.isActive = true " +
           "GROUP BY p.category " +
           "ORDER BY AVG(p.yearOverYearChange) DESC")
    List<Object[]> getImprovementPatterns();
    
    /**
     * Gets metric correlations for analysis.
     */
    @Query("SELECT p1.metricType as metric1, p2.metricType as metric2, " +
           "       COUNT(*) as pairCount " +
           "FROM MultiYearPerformance p1, MultiYearPerformance p2 " +
           "WHERE p1.teamNumber = p2.teamNumber " +
           "AND p1.season = p2.season " +
           "AND p1.metricType != p2.metricType " +
           "AND p1.isActive = true AND p2.isActive = true " +
           "GROUP BY p1.metricType, p2.metricType " +
           "ORDER BY COUNT(*) DESC")
    List<Object[]> getMetricCorrelations();
    
    /**
     * Finds performance outliers for investigation.
     */
    @Query("SELECT p FROM MultiYearPerformance p " +
           "WHERE p.isActive = true " +
           "AND (p.percentileRank >= 95.0 OR p.percentileRank <= 5.0 " +
           "     OR ABS(p.yearOverYearChange) > :changeThreshold) " +
           "ORDER BY ABS(p.yearOverYearChange) DESC")
    List<MultiYearPerformance> findPerformanceOutliers(@Param("changeThreshold") Double changeThreshold);
    
    // =========================================================================
    // COMPETITIVE ANALYSIS
    // =========================================================================
    
    /**
     * Gets competitive landscape analysis.
     */
    @Query("SELECT p.category, " +
           "       AVG(p.competitiveAdvantage) as avgAdvantage, " +
           "       MIN(p.competitiveAdvantage) as minAdvantage, " +
           "       MAX(p.competitiveAdvantage) as maxAdvantage, " +
           "       COUNT(p) as teamCount " +
           "FROM MultiYearPerformance p " +
           "WHERE p.season = :season " +
           "AND p.isActive = true " +
           "GROUP BY p.category " +
           "ORDER BY AVG(p.competitiveAdvantage) DESC")
    List<Object[]> getCompetitiveLandscape(@Param("season") Integer season);
    
    /**
     * Finds market leaders by category.
     */
    @Query("SELECT p FROM MultiYearPerformance p " +
           "WHERE p.season = :season " +
           "AND p.category = :category " +
           "AND p.isActive = true " +
           "AND p.competitiveAdvantage > :advantageThreshold " +
           "ORDER BY p.competitiveAdvantage DESC")
    List<MultiYearPerformance> getMarketLeaders(@Param("season") Integer season,
                                               @Param("category") MultiYearPerformance.PerformanceCategory category,
                                               @Param("advantageThreshold") Double advantageThreshold);
    
    /**
     * Analyzes peer group performance.
     */
    @Query("SELECT p.teamNumber, p.teamName, AVG(p.percentileRank) as avgPercentile " +
           "FROM MultiYearPerformance p " +
           "WHERE p.isActive = true " +
           "AND p.teamSize BETWEEN :minTeamSize AND :maxTeamSize " +
           "GROUP BY p.teamNumber, p.teamName " +
           "ORDER BY AVG(p.percentileRank) DESC")
    List<Object[]> analyzePeerGroupPerformance(@Param("minTeamSize") Integer minTeamSize,
                                              @Param("maxTeamSize") Integer maxTeamSize);
    
    // =========================================================================
    // BENCHMARKING AND STANDARDS
    // =========================================================================
    
    /**
     * Calculates benchmark standards by metric type.
     */
    @Query("SELECT p.metricType, " +
           "       AVG(p.metricValue) as avgValue, " +
           "       MIN(p.metricValue) as minValue, " +
           "       MAX(p.metricValue) as maxValue, " +
           "       AVG(p.percentileRank) as avgPercentile " +
           "FROM MultiYearPerformance p " +
           "WHERE p.season = :season " +
           "AND p.isActive = true " +
           "GROUP BY p.metricType " +
           "ORDER BY AVG(p.metricValue) DESC")
    List<Object[]> calculateBenchmarkStandards(@Param("season") Integer season);
    
    /**
     * Gets regional performance comparisons.
     */
    @Query("SELECT p.season, " +
           "       AVG(p.regionalAverageMetric) as regionalAvg, " +
           "       AVG(p.nationalAverageMetric) as nationalAvg, " +
           "       COUNT(p) as recordCount " +
           "FROM MultiYearPerformance p " +
           "WHERE p.isActive = true " +
           "AND p.category = :category " +
           "GROUP BY p.season " +
           "ORDER BY p.season DESC")
    List<Object[]> getRegionalComparisons(@Param("category") MultiYearPerformance.PerformanceCategory category);
    
    /**
     * Finds excellence standards and best practices.
     */
    @Query("SELECT p.metricType, " +
           "       MAX(p.metricValue) as excellenceStandard, " +
           "       AVG(CASE WHEN p.percentileRank >= 90 THEN p.metricValue END) as topTierAvg, " +
           "       COUNT(CASE WHEN p.percentileRank >= 90 THEN 1 END) as topTierCount " +
           "FROM MultiYearPerformance p " +
           "WHERE p.isActive = true " +
           "GROUP BY p.metricType " +
           "ORDER BY MAX(p.metricValue) DESC")
    List<Object[]> getExcellenceStandards();
    
    // =========================================================================
    // DATA QUALITY AND VALIDATION
    // =========================================================================
    
    /**
     * Finds records with low data quality.
     */
    @Query("SELECT p FROM MultiYearPerformance p " +
           "WHERE p.isActive = true " +
           "AND (p.dataCompleteness < :completenessThreshold " +
           "     OR p.dataAccuracy < :accuracyThreshold " +
           "     OR p.dataCollectionDate IS NULL) " +
           "ORDER BY p.dataCompleteness ASC, p.dataAccuracy ASC")
    List<MultiYearPerformance> findLowQualityData(@Param("completenessThreshold") Double completenessThreshold,
                                                 @Param("accuracyThreshold") Double accuracyThreshold);
    
    /**
     * Gets data quality statistics.
     */
    @Query("SELECT " +
           "       AVG(p.dataCompleteness) as avgCompleteness, " +
           "       AVG(p.dataAccuracy) as avgAccuracy, " +
           "       COUNT(CASE WHEN p.dataCompleteness >= 90 THEN 1 END) as highQualityCount, " +
           "       COUNT(p) as totalCount " +
           "FROM MultiYearPerformance p " +
           "WHERE p.isActive = true")
    List<Object[]> getDataQualityStatistics();
    
    /**
     * Finds records needing verification.
     */
    @Query("SELECT p FROM MultiYearPerformance p " +
           "WHERE p.isActive = true " +
           "AND (p.verifiedBy IS NULL " +
           "     OR p.lastVerificationDate < :verificationCutoff " +
           "     OR (p.percentileRank >= 95 AND p.verifiedBy IS NULL)) " +
           "ORDER BY p.percentileRank DESC")
    List<MultiYearPerformance> findRecordsNeedingVerification(@Param("verificationCutoff") LocalDate verificationCutoff);
    
    // =========================================================================
    // SEARCH AND FILTERING
    // =========================================================================
    
    // Note: searchByTeamName query removed - LIKE CONCAT validation issues in H2
    
    /**
     * Advanced search with multiple criteria.
     */
    @Query("SELECT p FROM MultiYearPerformance p " +
           "WHERE p.isActive = true " +
           "AND (:teamNumber IS NULL OR p.teamNumber = :teamNumber) " +
           "AND (:season IS NULL OR p.season = :season) " +
           "AND (:category IS NULL OR p.category = :category) " +
           "AND (:metricType IS NULL OR p.metricType = :metricType) " +
           "AND (:minPercentile IS NULL OR p.percentileRank >= :minPercentile) " +
           "AND (:maxPercentile IS NULL OR p.percentileRank <= :maxPercentile) " +
           "ORDER BY p.season DESC, p.percentileRank DESC")
    List<MultiYearPerformance> advancedSearch(@Param("teamNumber") Integer teamNumber,
                                             @Param("season") Integer season,
                                             @Param("category") MultiYearPerformance.PerformanceCategory category,
                                             @Param("metricType") MultiYearPerformance.MetricType metricType,
                                             @Param("minPercentile") Double minPercentile,
                                             @Param("maxPercentile") Double maxPercentile);
    
    /**
     * Finds teams by performance criteria.
     */
    @Query("SELECT DISTINCT p.teamNumber, p.teamName " +
           "FROM MultiYearPerformance p " +
           "WHERE p.isActive = true " +
           "AND p.percentileRank >= :percentileThreshold " +
           "AND p.season >= :seasonStart " +
           "ORDER BY p.teamNumber ASC")
    List<Object[]> findTeamsByPerformanceCriteria(@Param("percentileThreshold") Double percentileThreshold,
                                                 @Param("seasonStart") Integer seasonStart);
    
    // =========================================================================
    // ANALYTICS AND REPORTING
    // =========================================================================
    
    /**
     * Gets performance distribution statistics.
     */
    @Query("SELECT p.category, p.metricType, " +
           "       COUNT(p) as recordCount, " +
           "       AVG(p.metricValue) as avgValue, " +
           "       MIN(p.metricValue) as minValue, " +
           "       MAX(p.metricValue) as maxValue " +
           "FROM MultiYearPerformance p " +
           "WHERE p.season = :season " +
           "AND p.isActive = true " +
           "GROUP BY p.category, p.metricType " +
           "ORDER BY p.category, AVG(p.metricValue) DESC")
    List<Object[]> getPerformanceDistribution(@Param("season") Integer season);
    
    /**
     * Calculates growth rates by category.
     */
    @Query("SELECT p.category, " +
           "       AVG(p.yearOverYearChange) as avgGrowthRate, " +
           "       COUNT(CASE WHEN p.isImproving = true THEN 1 END) as improvingCount, " +
           "       COUNT(CASE WHEN p.isAtRisk = true THEN 1 END) as atRiskCount, " +
           "       COUNT(p) as totalCount " +
           "FROM MultiYearPerformance p " +
           "WHERE p.isActive = true " +
           "GROUP BY p.category " +
           "ORDER BY AVG(p.yearOverYearChange) DESC")
    List<Object[]> calculateGrowthRates();
    
    /**
     * Gets seasonal performance patterns.
     */
    @Query("SELECT p.season, " +
           "       COUNT(p) as totalRecords, " +
           "       AVG(p.percentileRank) as avgPercentile, " +
           "       COUNT(CASE WHEN p.percentileRank >= 80 THEN 1 END) as topPerformers, " +
           "       AVG(p.yearOverYearChange) as avgChange " +
           "FROM MultiYearPerformance p " +
           "WHERE p.isActive = true " +
           "GROUP BY p.season " +
           "ORDER BY p.season DESC")
    List<Object[]> getSeasonalPatterns();
    
    // =========================================================================
    // BULK OPERATIONS AND MAINTENANCE
    // =========================================================================
    
    /**
     * Counts records by team and season.
     */
    @Query("SELECT COUNT(p) FROM MultiYearPerformance p " +
           "WHERE p.teamNumber = :teamNumber " +
           "AND p.season = :season " +
           "AND p.isActive = true")
    long countByTeamAndSeason(@Param("teamNumber") Integer teamNumber, @Param("season") Integer season);
    
    /**
     * Gets unique teams in the system.
     */
    @Query("SELECT DISTINCT p.teamNumber, p.teamName " +
           "FROM MultiYearPerformance p " +
           "WHERE p.isActive = true " +
           "ORDER BY p.teamNumber ASC")
    List<Object[]> getUniqueTeams();
    
    /**
     * Gets unique seasons in the system.
     */
    @Query("SELECT DISTINCT p.season " +
           "FROM MultiYearPerformance p " +
           "WHERE p.isActive = true " +
           "ORDER BY p.season DESC")
    List<Integer> getUniqueSeasons();
    
    /**
     * Calculates database statistics.
     */
    @Query("SELECT " +
           "       COUNT(p) as totalRecords, " +
           "       COUNT(DISTINCT p.teamNumber) as uniqueTeams, " +
           "       COUNT(DISTINCT p.season) as uniqueSeasons, " +
           "       AVG(p.dataCompleteness) as avgDataCompleteness " +
           "FROM MultiYearPerformance p " +
           "WHERE p.isActive = true")
    List<Object[]> getDatabaseStatistics();
}