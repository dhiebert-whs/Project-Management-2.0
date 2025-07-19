// src/main/java/org/frcpm/services/ResourceDemandForecastService.java

package org.frcpm.services;

import org.frcpm.models.ResourceDemandForecast;
import org.frcpm.models.Project;
import org.frcpm.models.TeamMember;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for managing resource demand forecasting.
 * 
 * Provides comprehensive resource demand forecasting capabilities, including
 * AI-powered predictions, scenario analysis, optimization recommendations,
 * and accuracy tracking for FRC teams.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-BusinessIntel
 * @since Phase 4B.2 Resource Demand Forecasting
 */
public interface ResourceDemandForecastService extends Service<ResourceDemandForecast, Long> {
    
    // =========================================================================
    // FORECAST CREATION AND MANAGEMENT
    // =========================================================================
    
    /**
     * Creates a new resource demand forecast.
     * 
     * @param forecast the forecast to create
     * @return the created forecast
     * @throws IllegalArgumentException if forecast is invalid
     */
    ResourceDemandForecast createForecast(ResourceDemandForecast forecast);
    
    /**
     * Creates an automated forecast for a project using AI models.
     * 
     * @param projectId the project ID
     * @param resourceCategory the resource category to forecast
     * @param forecastHorizon the forecast time horizon
     * @param createdBy the team member creating the forecast
     * @return the generated automated forecast
     */
    ResourceDemandForecast createAutomatedForecast(Long projectId, 
                                                 ResourceDemandForecast.ResourceCategory resourceCategory,
                                                 ResourceDemandForecast.ForecastHorizon forecastHorizon,
                                                 TeamMember createdBy);
    
    /**
     * Creates a manual forecast with user-provided data.
     * 
     * @param projectId the project ID
     * @param resourceCategory the resource category
     * @param forecastHorizon the forecast horizon
     * @param totalRequired total resources required
     * @param estimatedCost estimated total cost
     * @param createdBy the team member creating the forecast
     * @return the created manual forecast
     */
    ResourceDemandForecast createManualForecast(Long projectId,
                                              ResourceDemandForecast.ResourceCategory resourceCategory,
                                              ResourceDemandForecast.ForecastHorizon forecastHorizon,
                                              int totalRequired, double estimatedCost,
                                              TeamMember createdBy);
    
    /**
     * Updates an existing forecast.
     * 
     * @param forecastId the forecast ID
     * @param forecast the updated forecast data
     * @return the updated forecast
     */
    ResourceDemandForecast updateForecast(Long forecastId, ResourceDemandForecast forecast);
    
    /**
     * Activates a forecast.
     * 
     * @param forecastId the forecast ID
     * @return the activated forecast
     */
    ResourceDemandForecast activateForecast(Long forecastId);
    
    /**
     * Deactivates a forecast.
     * 
     * @param forecastId the forecast ID
     * @return the deactivated forecast
     */
    ResourceDemandForecast deactivateForecast(Long forecastId);
    
    /**
     * Approves a forecast for use in planning.
     * 
     * @param forecastId the forecast ID
     * @param approvedBy the team member approving the forecast
     * @return the approved forecast
     */
    ResourceDemandForecast approveForecast(Long forecastId, TeamMember approvedBy);
    
    // =========================================================================
    // FORECAST DISCOVERY AND RETRIEVAL
    // =========================================================================
    
    /**
     * Finds all active forecasts.
     * 
     * @return list of active forecasts
     */
    List<ResourceDemandForecast> findActiveForecasts();
    
    /**
     * Finds forecasts by project.
     * 
     * @param projectId the project ID
     * @return list of forecasts for the project
     */
    List<ResourceDemandForecast> findByProject(Long projectId);
    
    /**
     * Finds forecasts by resource category.
     * 
     * @param resourceCategory the resource category
     * @return list of forecasts for the category
     */
    List<ResourceDemandForecast> findByResourceCategory(ResourceDemandForecast.ResourceCategory resourceCategory);
    
    /**
     * Finds the most recent forecast for a project and resource category.
     * 
     * @param projectId the project ID
     * @param resourceCategory the resource category
     * @return the most recent forecast, if any
     */
    Optional<ResourceDemandForecast> getMostRecentForecast(Long projectId, 
                                                          ResourceDemandForecast.ResourceCategory resourceCategory);
    
    /**
     * Finds forecasts by forecast type.
     * 
     * @param forecastType the forecast type
     * @return list of forecasts of the specified type
     */
    List<ResourceDemandForecast> findByForecastType(ResourceDemandForecast.ForecastType forecastType);
    
    /**
     * Finds forecasts by forecast horizon.
     * 
     * @param forecastHorizon the forecast horizon
     * @return list of forecasts with the specified horizon
     */
    List<ResourceDemandForecast> findByForecastHorizon(ResourceDemandForecast.ForecastHorizon forecastHorizon);
    
    // =========================================================================
    // CRITICAL ANALYSIS AND ALERTS
    // =========================================================================
    
    /**
     * Finds forecasts with critical resource shortages.
     * 
     * @return list of forecasts with critical shortages
     */
    List<ResourceDemandForecast> findCriticalShortageForecasts();
    
    /**
     * Finds forecasts requiring immediate action.
     * 
     * @return list of forecasts needing immediate attention
     */
    List<ResourceDemandForecast> findForecastsRequiringAction();
    
    /**
     * Finds forecasts approaching peak demand period.
     * 
     * @param daysAhead number of days to look ahead
     * @return list of forecasts with approaching peak demand
     */
    List<ResourceDemandForecast> findApproachingPeakDemand(int daysAhead);
    
    /**
     * Finds forecasts with low confidence levels.
     * 
     * @param confidenceThreshold minimum confidence threshold
     * @return list of low-confidence forecasts
     */
    List<ResourceDemandForecast> findLowConfidenceForecasts(double confidenceThreshold);
    
    /**
     * Checks if a project has any critical resource issues.
     * 
     * @param projectId the project ID
     * @return true if critical issues exist
     */
    boolean hasProjectCriticalIssues(Long projectId);
    
    /**
     * Gets critical alerts for a project.
     * 
     * @param projectId the project ID
     * @return list of critical alert messages
     */
    List<String> getProjectCriticalAlerts(Long projectId);
    
    // =========================================================================
    // BUDGET AND COST ANALYSIS
    // =========================================================================
    
    /**
     * Finds forecasts exceeding budget thresholds.
     * 
     * @param budgetLimit the budget limit
     * @return list of forecasts exceeding the budget
     */
    List<ResourceDemandForecast> findBudgetExceedingForecasts(double budgetLimit);
    
    /**
     * Calculates total estimated cost for a project.
     * 
     * @param projectId the project ID
     * @return total estimated cost across all forecasts
     */
    double calculateTotalProjectCost(Long projectId);
    
    /**
     * Analyzes most expensive resource categories for a project.
     * 
     * @param projectId the project ID
     * @return map of resource categories to total costs
     */
    Map<ResourceDemandForecast.ResourceCategory, Double> analyzeMostExpensiveCategories(Long projectId);
    
    /**
     * Gets budget impact analysis by resource category.
     * 
     * @return map of categories to budget impact metrics
     */
    Map<ResourceDemandForecast.ResourceCategory, Map<String, Object>> getBudgetImpactAnalysis();
    
    /**
     * Optimizes budget allocation across forecasts.
     * 
     * @param projectId the project ID
     * @param totalBudget the total available budget
     * @return optimized allocation recommendations
     */
    Map<String, Object> optimizeBudgetAllocation(Long projectId, double totalBudget);
    
    // =========================================================================
    // TEMPORAL AND SEASONAL ANALYSIS
    // =========================================================================
    
    /**
     * Finds forecasts within a specific date range.
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @return list of forecasts in the date range
     */
    List<ResourceDemandForecast> findForecastsInDateRange(LocalDate startDate, LocalDate endDate);
    
    /**
     * Finds build season forecasts (6-week planning horizon).
     * 
     * @return list of build season forecasts
     */
    List<ResourceDemandForecast> findBuildSeasonForecasts();
    
    /**
     * Analyzes seasonal demand patterns.
     * 
     * @return seasonal pattern analysis data
     */
    Map<String, Object> analyzeSeasonalDemandPatterns();
    
    /**
     * Finds forecasts that need updates based on age.
     * 
     * @return list of outdated forecasts
     */
    List<ResourceDemandForecast> findOutdatedForecasts();
    
    /**
     * Updates all outdated forecasts for a project.
     * 
     * @param projectId the project ID
     * @return number of forecasts updated
     */
    int updateOutdatedForecasts(Long projectId);
    
    // =========================================================================
    // ACCURACY AND PERFORMANCE ANALYSIS
    // =========================================================================
    
    /**
     * Updates forecast accuracy based on actual usage.
     * 
     * @param forecastId the forecast ID
     * @param actualResourcesUsed actual resources used
     * @param actualCost actual cost incurred
     * @return the updated forecast with accuracy data
     */
    ResourceDemandForecast updateForecastAccuracy(Long forecastId, int actualResourcesUsed, double actualCost);
    
    /**
     * Analyzes accuracy by forecast type.
     * 
     * @return map of forecast types to accuracy metrics
     */
    Map<ResourceDemandForecast.ForecastType, Map<String, Object>> analyzeAccuracyByType();
    
    /**
     * Gets accuracy trends over time.
     * 
     * @return time-series accuracy data
     */
    Map<String, Object> getAccuracyTrends();
    
    /**
     * Finds most accurate forecasters.
     * 
     * @return list of team members with highest accuracy
     */
    List<Map<String, Object>> getMostAccurateForecasters();
    
    /**
     * Calculates model performance metrics.
     * 
     * @return performance metrics for forecasting models
     */
    Map<String, Object> calculateModelPerformanceMetrics();
    
    // =========================================================================
    // SCENARIO AND RISK ANALYSIS
    // =========================================================================
    
    /**
     * Performs scenario analysis for a project.
     * 
     * @param projectId the project ID
     * @return scenario analysis results
     */
    Map<String, Object> performScenarioAnalysis(Long projectId);
    
    /**
     * Finds forecasts with high demand variability.
     * 
     * @param variabilityThreshold variability threshold
     * @return list of high-variability forecasts
     */
    List<ResourceDemandForecast> findHighVariabilityForecasts(double variabilityThreshold);
    
    /**
     * Finds forecasts with high supply risk.
     * 
     * @param riskThreshold risk threshold
     * @return list of high-risk forecasts
     */
    List<ResourceDemandForecast> findHighSupplyRiskForecasts(double riskThreshold);
    
    /**
     * Analyzes risk factors for a project.
     * 
     * @param projectId the project ID
     * @return risk analysis results
     */
    Map<String, Object> analyzeProjectRiskFactors(Long projectId);
    
    /**
     * Performs Monte Carlo simulation for demand uncertainty.
     * 
     * @param forecastId the forecast ID
     * @param iterations number of simulation iterations
     * @return simulation results
     */
    Map<String, Object> performMonteCarloCSimulation(Long forecastId, int iterations);
    
    // =========================================================================
    // OPTIMIZATION AND RECOMMENDATIONS
    // =========================================================================
    
    /**
     * Finds optimization opportunities for resource planning.
     * 
     * @param costThreshold cost threshold for optimization
     * @param efficiencyThreshold efficiency threshold
     * @return list of optimization opportunities
     */
    List<ResourceDemandForecast> findOptimizationOpportunities(double costThreshold, double efficiencyThreshold);
    
    /**
     * Analyzes potential consolidation opportunities.
     * 
     * @return consolidation analysis results
     */
    Map<String, Object> analyzeConsolidationOpportunities();
    
    /**
     * Optimizes lead times for resource ordering.
     * 
     * @return lead time optimization recommendations
     */
    Map<String, Object> optimizeLeadTimes();
    
    /**
     * Generates procurement recommendations for a project.
     * 
     * @param projectId the project ID
     * @return procurement recommendations
     */
    List<Map<String, Object>> generateProcurementRecommendations(Long projectId);
    
    /**
     * Optimizes resource allocation across projects.
     * 
     * @param projectIds list of project IDs
     * @return resource allocation optimization results
     */
    Map<String, Object> optimizeResourceAllocation(List<Long> projectIds);
    
    // =========================================================================
    // SEARCH AND FILTERING
    // =========================================================================
    
    /**
     * Searches forecasts by project name or description.
     * 
     * @param searchTerm the search term
     * @return list of matching forecasts
     */
    List<ResourceDemandForecast> searchForecastsByProject(String searchTerm);
    
    /**
     * Finds forecasts created by a specific team member.
     * 
     * @param createdById the team member ID
     * @return list of forecasts created by the team member
     */
    List<ResourceDemandForecast> findByCreatedBy(Long createdById);
    
    /**
     * Advanced search with multiple criteria.
     * 
     * @param criteria search criteria map
     * @return list of matching forecasts
     */
    List<ResourceDemandForecast> advancedSearch(Map<String, Object> criteria);
    
    /**
     * Filters forecasts by custom criteria.
     * 
     * @param projectId optional project ID filter
     * @param forecastType optional forecast type filter
     * @param resourceCategory optional resource category filter
     * @param minCost optional minimum cost filter
     * @param maxCost optional maximum cost filter
     * @return list of filtered forecasts
     */
    List<ResourceDemandForecast> filterForecasts(Long projectId,
                                                ResourceDemandForecast.ForecastType forecastType,
                                                ResourceDemandForecast.ResourceCategory resourceCategory,
                                                Double minCost, Double maxCost);
    
    // =========================================================================
    // BULK OPERATIONS
    // =========================================================================
    
    /**
     * Creates multiple forecasts from templates or definitions.
     * 
     * @param forecasts the list of forecasts to create
     * @return list of created forecasts
     */
    List<ResourceDemandForecast> createBulkForecasts(List<ResourceDemandForecast> forecasts);
    
    /**
     * Updates multiple forecasts.
     * 
     * @param forecastUpdates map of forecast IDs to updated data
     * @return list of updated forecasts
     */
    List<ResourceDemandForecast> updateBulkForecasts(Map<Long, ResourceDemandForecast> forecastUpdates);
    
    /**
     * Generates forecasts for all resource categories of a project.
     * 
     * @param projectId the project ID
     * @param forecastHorizon the forecast horizon
     * @param createdBy the team member creating the forecasts
     * @return list of generated forecasts
     */
    List<ResourceDemandForecast> generateAllCategoryForecasts(Long projectId,
                                                             ResourceDemandForecast.ForecastHorizon forecastHorizon,
                                                             TeamMember createdBy);
    
    /**
     * Refreshes all active forecasts for a project.
     * 
     * @param projectId the project ID
     * @return number of forecasts refreshed
     */
    int refreshProjectForecasts(Long projectId);
    
    // =========================================================================
    // ANALYTICS AND REPORTING
    // =========================================================================
    
    /**
     * Gets forecast analytics and statistics.
     * 
     * @return comprehensive analytics data
     */
    Map<String, Object> getForecastAnalytics();
    
    /**
     * Gets forecast distribution by type.
     * 
     * @return distribution of forecasts by type
     */
    Map<ResourceDemandForecast.ForecastType, Long> getForecastTypeDistribution();
    
    /**
     * Gets resource category distribution and metrics.
     * 
     * @return distribution and metrics by category
     */
    Map<ResourceDemandForecast.ResourceCategory, Map<String, Object>> getResourceCategoryDistribution();
    
    /**
     * Analyzes forecast metrics by horizon.
     * 
     * @return metrics analysis by forecast horizon
     */
    Map<ResourceDemandForecast.ForecastHorizon, Map<String, Object>> analyzeMetricsByHorizon();
    
    /**
     * Gets demand trend analysis over time.
     * 
     * @return time-series demand trend data
     */
    Map<String, Object> getDemandTrendAnalysis();
    
    /**
     * Generates executive summary dashboard data.
     * 
     * @return executive dashboard metrics
     */
    Map<String, Object> generateExecutiveDashboard();
    
    /**
     * Generates forecast accuracy report.
     * 
     * @return comprehensive accuracy report
     */
    Map<String, Object> generateAccuracyReport();
    
    // =========================================================================
    // MACHINE LEARNING AND AI INTEGRATION
    // =========================================================================
    
    /**
     * Trains forecasting models with historical data.
     * 
     * @return training results and model performance
     */
    Map<String, Object> trainForecastingModels();
    
    /**
     * Updates model parameters based on recent accuracy data.
     * 
     * @return model update results
     */
    Map<String, Object> updateModelParameters();
    
    /**
     * Validates model performance against test data.
     * 
     * @return model validation results
     */
    Map<String, Object> validateModelPerformance();
    
    /**
     * Generates AI-powered insights and recommendations.
     * 
     * @param projectId the project ID
     * @return AI-generated insights
     */
    Map<String, Object> generateAIInsights(Long projectId);
    
    // =========================================================================
    // INTEGRATION AND EXPORT
    // =========================================================================
    
    /**
     * Exports forecast data for external analysis.
     * 
     * @param projectId optional project ID filter
     * @param format export format (CSV, JSON, XML)
     * @return exported data in the specified format
     */
    String exportForecastData(Long projectId, String format);
    
    /**
     * Imports forecast data from external sources.
     * 
     * @param data the data to import
     * @param format data format
     * @return number of forecasts imported
     */
    int importForecastData(String data, String format);
    
    /**
     * Integrates with external planning systems.
     * 
     * @param systemType the external system type
     * @param projectId the project ID
     * @return integration results
     */
    Map<String, Object> integrateWithExternalSystem(String systemType, Long projectId);
}