// src/main/java/org/frcpm/repositories/spring/ResourceDemandForecastRepository.java

package org.frcpm.repositories.spring;

import org.frcpm.models.ResourceDemandForecast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for ResourceDemandForecast entities.
 * 
 * Provides database access methods for resource demand forecasting,
 * including analytics, trend analysis, and optimization queries.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-BusinessIntel
 * @since Phase 4B.2 Resource Demand Forecasting
 */
@Repository
public interface ResourceDemandForecastRepository extends JpaRepository<ResourceDemandForecast, Long> {
    
    // =========================================================================
    // BASIC QUERIES
    // =========================================================================
    
    /**
     * Finds all active forecasts.
     */
    List<ResourceDemandForecast> findByIsActiveTrueOrderByForecastDateDesc();
    
    /**
     * Finds forecasts by project.
     */
    List<ResourceDemandForecast> findByProjectIdAndIsActiveTrueOrderByForecastDateDesc(Long projectId);
    
    /**
     * Finds forecasts by forecast type.
     */
    List<ResourceDemandForecast> findByForecastTypeAndIsActiveTrueOrderByForecastDateDesc(ResourceDemandForecast.ForecastType forecastType);
    
    /**
     * Finds forecasts by resource category.
     */
    List<ResourceDemandForecast> findByResourceCategoryAndIsActiveTrueOrderByForecastDateDesc(ResourceDemandForecast.ResourceCategory resourceCategory);
    
    /**
     * Finds forecasts by forecast horizon.
     */
    List<ResourceDemandForecast> findByForecastHorizonAndIsActiveTrueOrderByForecastDateDesc(ResourceDemandForecast.ForecastHorizon forecastHorizon);
    
    /**
     * Finds the most recent forecast for a project and resource category.
     */
    Optional<ResourceDemandForecast> findFirstByProjectIdAndResourceCategoryAndIsActiveTrueOrderByForecastDateDesc(Long projectId, ResourceDemandForecast.ResourceCategory resourceCategory);
    
    // =========================================================================
    // CRITICAL FORECASTS AND ALERTS
    // =========================================================================
    
    /**
     * Finds forecasts with critical resource shortages.
     */
    @Query("SELECT f FROM ResourceDemandForecast f " +
           "WHERE f.isActive = true " +
           "AND f.projectedShortfall > 0 " +
           "AND (f.projectedShortfall * 1.0 / f.totalResourcesRequired) > 0.20 " +
           "ORDER BY (f.projectedShortfall * 1.0 / f.totalResourcesRequired) DESC")
    List<ResourceDemandForecast> findCriticalShortageForecasts();
    
    /**
     * Finds forecasts requiring immediate action.
     */
    @Query("SELECT f FROM ResourceDemandForecast f " +
           "WHERE f.isActive = true " +
           "AND (f.latestOrderDate <= :currentDate " +
           "     OR f.projectedShortfall > f.totalResourcesRequired * 0.20) " +
           "ORDER BY f.latestOrderDate ASC")
    List<ResourceDemandForecast> findForecastsRequiringAction(@Param("currentDate") LocalDate currentDate);
    
    /**
     * Finds forecasts approaching peak demand period.
     */
    @Query("SELECT f FROM ResourceDemandForecast f " +
           "WHERE f.isActive = true " +
           "AND f.peakDemandDate BETWEEN :startDate AND :endDate " +
           "ORDER BY f.peakDemandDate ASC")
    List<ResourceDemandForecast> findApproachingPeakDemand(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * Finds forecasts with low confidence levels.
     */
    @Query("SELECT f FROM ResourceDemandForecast f " +
           "WHERE f.isActive = true " +
           "AND f.confidenceLevel < :confidenceThreshold " +
           "ORDER BY f.confidenceLevel ASC")
    List<ResourceDemandForecast> findLowConfidenceForecasts(@Param("confidenceThreshold") Double confidenceThreshold);
    
    // =========================================================================
    // BUDGET AND COST ANALYSIS
    // =========================================================================
    
    /**
     * Finds forecasts exceeding budget thresholds.
     */
    @Query("SELECT f FROM ResourceDemandForecast f " +
           "WHERE f.isActive = true " +
           "AND f.estimatedTotalCost > :budgetLimit " +
           "ORDER BY f.estimatedTotalCost DESC")
    List<ResourceDemandForecast> findBudgetExceedingForecasts(@Param("budgetLimit") Double budgetLimit);
    
    /**
     * Calculates total estimated cost for a project.
     */
    @Query("SELECT SUM(f.estimatedTotalCost) FROM ResourceDemandForecast f " +
           "WHERE f.projectId = :projectId " +
           "AND f.isActive = true")
    Double calculateTotalProjectCost(@Param("projectId") Long projectId);
    
    /**
     * Finds most expensive forecast categories for a project.
     */
    @Query("SELECT f.resourceCategory, SUM(f.estimatedTotalCost) as totalCost FROM ResourceDemandForecast f " +
           "WHERE f.projectId = :projectId " +
           "AND f.isActive = true " +
           "GROUP BY f.resourceCategory " +
           "ORDER BY SUM(f.estimatedTotalCost) DESC")
    List<Object[]> findMostExpensiveCategories(@Param("projectId") Long projectId);
    
    /**
     * Calculates budget impact by resource category.
     */
    @Query("SELECT f.resourceCategory, AVG(f.budgetImpact), SUM(f.estimatedTotalCost) FROM ResourceDemandForecast f " +
           "WHERE f.isActive = true " +
           "GROUP BY f.resourceCategory " +
           "ORDER BY AVG(f.budgetImpact) DESC")
    List<Object[]> getBudgetImpactByCategory();
    
    // =========================================================================
    // SEASONAL AND TEMPORAL ANALYSIS
    // =========================================================================
    
    /**
     * Finds forecasts within a specific date range.
     */
    @Query("SELECT f FROM ResourceDemandForecast f " +
           "WHERE f.isActive = true " +
           "AND f.forecastPeriodStart <= :endDate " +
           "AND f.forecastPeriodEnd >= :startDate " +
           "ORDER BY f.forecastPeriodStart ASC")
    List<ResourceDemandForecast> findForecastsInDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * Finds build season forecasts (6-week period).
     */
    @Query("SELECT f FROM ResourceDemandForecast f " +
           "WHERE f.isActive = true " +
           "AND f.forecastHorizon = 'BUILD_SEASON' " +
           "ORDER BY f.forecastDate DESC")
    List<ResourceDemandForecast> findBuildSeasonForecasts();
    
    /**
     * Gets seasonal demand patterns.
     */
    @Query("SELECT MONTH(f.forecastDate), f.resourceCategory, AVG(f.totalResourcesRequired) FROM ResourceDemandForecast f " +
           "WHERE f.isActive = true " +
           "GROUP BY MONTH(f.forecastDate), f.resourceCategory " +
           "ORDER BY MONTH(f.forecastDate), f.resourceCategory")
    List<Object[]> getSeasonalDemandPatterns();
    
    /**
     * Finds forecasts that need updates based on age.
     */
    @Query("SELECT f FROM ResourceDemandForecast f " +
           "WHERE f.isActive = true " +
           "AND f.forecastDate < :cutoffDate " +
           "ORDER BY f.forecastDate ASC")
    List<ResourceDemandForecast> findOutdatedForecasts(@Param("cutoffDate") LocalDate cutoffDate);
    
    // =========================================================================
    // ACCURACY AND PERFORMANCE ANALYSIS
    // =========================================================================
    
    /**
     * Finds forecasts with accuracy data for model improvement.
     */
    @Query("SELECT f FROM ResourceDemandForecast f " +
           "WHERE f.isActive = true " +
           "AND f.actualResourcesUsed IS NOT NULL " +
           "AND f.forecastAccuracy IS NOT NULL " +
           "ORDER BY f.forecastAccuracy DESC")
    List<ResourceDemandForecast> findForecastsWithAccuracyData();
    
    /**
     * Gets average accuracy by forecast type.
     */
    @Query("SELECT f.forecastType, AVG(f.forecastAccuracy), COUNT(f) FROM ResourceDemandForecast f " +
           "WHERE f.forecastAccuracy IS NOT NULL " +
           "GROUP BY f.forecastType " +
           "ORDER BY AVG(f.forecastAccuracy) DESC")
    List<Object[]> getAccuracyByForecastType();
    
    /**
     * Gets accuracy trends over time.
     */
    @Query("SELECT YEAR(f.forecastDate), MONTH(f.forecastDate), AVG(f.forecastAccuracy) FROM ResourceDemandForecast f " +
           "WHERE f.forecastAccuracy IS NOT NULL " +
           "GROUP BY YEAR(f.forecastDate), MONTH(f.forecastDate) " +
           "ORDER BY YEAR(f.forecastDate), MONTH(f.forecastDate)")
    List<Object[]> getAccuracyTrends();
    
    /**
     * Finds most accurate forecasters.
     */
    @Query("SELECT f.createdBy.id, f.createdBy.firstName, f.createdBy.lastName, AVG(f.forecastAccuracy) FROM ResourceDemandForecast f " +
           "WHERE f.forecastAccuracy IS NOT NULL " +
           "AND f.createdBy IS NOT NULL " +
           "GROUP BY f.createdBy.id, f.createdBy.firstName, f.createdBy.lastName " +
           "ORDER BY AVG(f.forecastAccuracy) DESC")
    List<Object[]> getMostAccurateForecasters();
    
    // =========================================================================
    // SCENARIO AND RISK ANALYSIS
    // =========================================================================
    
    /**
     * Finds forecasts with high demand variability.
     */
    @Query("SELECT f FROM ResourceDemandForecast f " +
           "WHERE f.isActive = true " +
           "AND f.demandVariability > :variabilityThreshold " +
           "ORDER BY f.demandVariability DESC")
    List<ResourceDemandForecast> findHighVariabilityForecasts(@Param("variabilityThreshold") Double variabilityThreshold);
    
    /**
     * Finds forecasts with high supply risk.
     */
    @Query("SELECT f FROM ResourceDemandForecast f " +
           "WHERE f.isActive = true " +
           "AND f.supplyRisk > :riskThreshold " +
           "ORDER BY f.supplyRisk DESC")
    List<ResourceDemandForecast> findHighSupplyRiskForecasts(@Param("riskThreshold") Double riskThreshold);
    
    /**
     * Gets scenario analysis for a project.
     */
    @Query("SELECT f.resourceCategory, " +
           "       AVG(f.optimisticScenario), " +
           "       AVG(f.mostLikelyScenario), " +
           "       AVG(f.pessimisticScenario) " +
           "FROM ResourceDemandForecast f " +
           "WHERE f.projectId = :projectId " +
           "AND f.isActive = true " +
           "GROUP BY f.resourceCategory")
    List<Object[]> getProjectScenarioAnalysis(@Param("projectId") Long projectId);
    
    // =========================================================================
    // OPTIMIZATION AND RECOMMENDATIONS
    // =========================================================================
    
    /**
     * Finds optimization opportunities (high cost, low efficiency).
     */
    @Query("SELECT f FROM ResourceDemandForecast f " +
           "WHERE f.isActive = true " +
           "AND f.estimatedTotalCost > :costThreshold " +
           "AND (f.currentResourcesAvailable * 1.0 / f.totalResourcesRequired) < :efficiencyThreshold " +
           "ORDER BY f.estimatedTotalCost DESC")
    List<ResourceDemandForecast> findOptimizationOpportunities(@Param("costThreshold") Double costThreshold, 
                                                              @Param("efficiencyThreshold") Double efficiencyThreshold);
    
    /**
     * Finds potential consolidation opportunities.
     */
    @Query("SELECT f.resourceCategory, f.forecastPeriodStart, f.forecastPeriodEnd, COUNT(f), SUM(f.totalResourcesRequired) " +
           "FROM ResourceDemandForecast f " +
           "WHERE f.isActive = true " +
           "GROUP BY f.resourceCategory, f.forecastPeriodStart, f.forecastPeriodEnd " +
           "HAVING COUNT(f) > 1 " +
           "ORDER BY COUNT(f) DESC")
    List<Object[]> findConsolidationOpportunities();
    
    /**
     * Gets lead time optimization analysis.
     */
    @Query("SELECT f.resourceCategory, AVG(f.leadTimeDays), MIN(f.leadTimeDays), MAX(f.leadTimeDays) " +
           "FROM ResourceDemandForecast f " +
           "WHERE f.isActive = true " +
           "AND f.leadTimeDays > 0 " +
           "GROUP BY f.resourceCategory " +
           "ORDER BY AVG(f.leadTimeDays) DESC")
    List<Object[]> getLeadTimeAnalysis();
    
    // =========================================================================
    // SEARCH AND FILTERING
    // =========================================================================
    
    /**
     * Searches forecasts by project name or description.
     */
    @Query("SELECT f FROM ResourceDemandForecast f " +
           "WHERE f.isActive = true " +
           "AND (LOWER(f.project.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "     OR LOWER(f.project.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY f.forecastDate DESC")
    List<ResourceDemandForecast> searchForecastsByProject(@Param("searchTerm") String searchTerm);
    
    /**
     * Finds forecasts created by a specific team member.
     */
    @Query("SELECT f FROM ResourceDemandForecast f " +
           "WHERE f.createdBy.id = :createdById " +
           "AND f.isActive = true " +
           "ORDER BY f.createdAt DESC")
    List<ResourceDemandForecast> findByCreatedBy(@Param("createdById") Long createdById);
    
    /**
     * Advanced search with multiple criteria.
     */
    @Query("SELECT f FROM ResourceDemandForecast f " +
           "WHERE f.isActive = true " +
           "AND (:projectId IS NULL OR f.projectId = :projectId) " +
           "AND (:forecastType IS NULL OR f.forecastType = :forecastType) " +
           "AND (:resourceCategory IS NULL OR f.resourceCategory = :resourceCategory) " +
           "AND (:minCost IS NULL OR f.estimatedTotalCost >= :minCost) " +
           "AND (:maxCost IS NULL OR f.estimatedTotalCost <= :maxCost) " +
           "ORDER BY f.forecastDate DESC")
    List<ResourceDemandForecast> advancedSearch(@Param("projectId") Long projectId,
                                               @Param("forecastType") ResourceDemandForecast.ForecastType forecastType,
                                               @Param("resourceCategory") ResourceDemandForecast.ResourceCategory resourceCategory,
                                               @Param("minCost") Double minCost,
                                               @Param("maxCost") Double maxCost);
    
    // =========================================================================
    // STATISTICS AND ANALYTICS
    // =========================================================================
    
    /**
     * Gets forecast distribution by type.
     */
    @Query("SELECT f.forecastType, COUNT(f) FROM ResourceDemandForecast f " +
           "WHERE f.isActive = true " +
           "GROUP BY f.forecastType " +
           "ORDER BY COUNT(f) DESC")
    List<Object[]> getForecastTypeDistribution();
    
    /**
     * Gets resource category distribution.
     */
    @Query("SELECT f.resourceCategory, COUNT(f), AVG(f.estimatedTotalCost) FROM ResourceDemandForecast f " +
           "WHERE f.isActive = true " +
           "GROUP BY f.resourceCategory " +
           "ORDER BY COUNT(f) DESC")
    List<Object[]> getResourceCategoryDistribution();
    
    /**
     * Gets average forecast metrics by horizon.
     */
    @Query("SELECT f.forecastHorizon, " +
           "       AVG(f.totalResourcesRequired), " +
           "       AVG(f.estimatedTotalCost), " +
           "       AVG(f.confidenceLevel), " +
           "       COUNT(f) " +
           "FROM ResourceDemandForecast f " +
           "WHERE f.isActive = true " +
           "GROUP BY f.forecastHorizon")
    List<Object[]> getMetricsByHorizon();
    
    /**
     * Gets demand trend analysis.
     */
    @Query("SELECT YEAR(f.forecastDate), MONTH(f.forecastDate), " +
           "       SUM(f.totalResourcesRequired), " +
           "       SUM(f.estimatedTotalCost), " +
           "       AVG(f.confidenceLevel) " +
           "FROM ResourceDemandForecast f " +
           "WHERE f.isActive = true " +
           "GROUP BY YEAR(f.forecastDate), MONTH(f.forecastDate) " +
           "ORDER BY YEAR(f.forecastDate), MONTH(f.forecastDate)")
    List<Object[]> getDemandTrends();
    
    // =========================================================================
    // MAINTENANCE AND CLEANUP
    // =========================================================================
    
    /**
     * Finds inactive forecasts for cleanup.
     */
    List<ResourceDemandForecast> findByIsActiveFalseOrderByUpdatedAtAsc();
    
    /**
     * Counts active forecasts by project.
     */
    @Query("SELECT COUNT(f) FROM ResourceDemandForecast f " +
           "WHERE f.projectId = :projectId " +
           "AND f.isActive = true")
    long countActiveByProject(@Param("projectId") Long projectId);
    
    /**
     * Gets database statistics.
     */
    @Query("SELECT " +
           "       COUNT(f), " +
           "       COUNT(CASE WHEN f.isActive = true THEN 1 END), " +
           "       AVG(f.estimatedTotalCost), " +
           "       SUM(f.estimatedTotalCost) " +
           "FROM ResourceDemandForecast f")
    List<Object[]> getDatabaseStatistics();
}