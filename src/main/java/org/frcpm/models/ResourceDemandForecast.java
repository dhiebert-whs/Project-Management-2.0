// src/main/java/org/frcpm/models/ResourceDemandForecast.java

package org.frcpm.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Resource Demand Forecast model for FRC teams.
 * 
 * Provides AI-powered resource demand forecasting and optimization to help FRC teams
 * plan resource allocation, identify potential shortages, and optimize ordering schedules
 * throughout the build season.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-BusinessIntel
 * @since Phase 4B.2 Resource Demand Forecasting
 */
@Entity
@Table(name = "resource_demand_forecasts")
public class ResourceDemandForecast {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    
    @Column(nullable = false)
    private LocalDate forecastDate;
    
    @Column(nullable = false)
    private LocalDate forecastPeriodStart;
    
    @Column(nullable = false)
    private LocalDate forecastPeriodEnd;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ForecastType forecastType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResourceCategory resourceCategory;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ForecastHorizon forecastHorizon;
    
    // Forecast Metrics
    @Column(nullable = false)
    private Double confidenceLevel = 0.0; // 0.0 to 1.0
    
    @Column(nullable = false)
    private Double accuracyScore = 0.0; // Historical accuracy of this forecast type
    
    @Column(nullable = false)
    private Integer totalResourcesRequired = 0;
    
    @Column(nullable = false)
    private Integer currentResourcesAvailable = 0;
    
    @Column(nullable = false)
    private Integer projectedShortfall = 0;
    
    @Column(nullable = false)
    private Double estimatedTotalCost = 0.0;
    
    @Column(nullable = false)
    private Double budgetImpact = 0.0; // Percentage of budget required
    
    // Timeline Predictions
    @Column
    private LocalDate peakDemandDate; // When demand will be highest
    
    @Column
    private Integer peakDemandQuantity; // Peak quantity needed
    
    @Column
    private LocalDate earliestOrderDate; // When to start ordering
    
    @Column
    private LocalDate latestOrderDate; // Latest viable order date
    
    @Column
    private Integer leadTimeDays = 7; // Expected delivery lead time
    
    // Resource Categories Breakdown
    @Column(nullable = false)
    private Integer materialsQuantity = 0;
    
    @Column(nullable = false)
    private Integer toolsQuantity = 0;
    
    @Column(nullable = false)
    private Integer componentsQuantity = 0;
    
    @Column(nullable = false)
    private Integer laborHours = 0;
    
    @Column(nullable = false)
    private Integer facilityHours = 0;
    
    @Column(nullable = false)
    private Integer equipmentHours = 0;
    
    // Cost Breakdown
    @Column(nullable = false)
    private Double materialsCost = 0.0;
    
    @Column(nullable = false)
    private Double toolsCost = 0.0;
    
    @Column(nullable = false)
    private Double componentsCost = 0.0;
    
    @Column(nullable = false)
    private Double laborCost = 0.0; // If using contractors
    
    @Column(nullable = false)
    private Double facilityCost = 0.0; // Facility rental, utilities
    
    @Column(nullable = false)
    private Double equipmentCost = 0.0; // Equipment rental/purchase
    
    // Risk and Uncertainty Analysis
    @Column(nullable = false)
    private Double demandVariability = 0.0; // Standard deviation of demand
    
    @Column(nullable = false)
    private Double supplyRisk = 0.0; // Risk of supply chain disruption
    
    @Column(nullable = false)
    private Double priceVolatility = 0.0; // Expected price fluctuation
    
    @Column(nullable = false)
    private Double seasonalFactor = 1.0; // Seasonal demand multiplier
    
    // Forecast Scenarios
    @Column(nullable = false)
    private Integer optimisticScenario = 0; // Best case resource need
    
    @Column(nullable = false)
    private Integer pessimisticScenario = 0; // Worst case resource need
    
    @Column(nullable = false)
    private Integer mostLikelyScenario = 0; // Most probable resource need
    
    // External Factors
    @Column(length = 1000)
    private String externalFactors; // Competition schedule, supplier issues, etc.
    
    @Column(length = 2000)
    private String forecastAssumptions; // Key assumptions in the forecast
    
    @Column(length = 2000)
    private String mitigationStrategies; // How to handle shortfalls
    
    @Column(length = 1000)
    private String recommendedActions; // Immediate actions needed
    
    // Forecast Metadata
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
    @JoinColumn(name = "approved_by_id")
    private TeamMember approvedBy;
    
    @Column
    private LocalDateTime approvedAt;
    
    // Analytics Data
    @Column(length = 5000)
    private String analyticsData; // JSON data for ML model inputs
    
    @Column(length = 2000)
    private String modelVersion; // Version of the forecasting model used
    
    @Column
    private LocalDateTime lastModelUpdate;
    
    // Historical tracking for accuracy improvement
    @Column
    private Integer actualResourcesUsed; // Actual resources used (post-forecast)
    
    @Column
    private Double actualCost; // Actual cost incurred
    
    @Column
    private Double forecastAccuracy; // Accuracy of this specific forecast
    
    /**
     * Types of resource forecasts
     */
    public enum ForecastType {
        AUTOMATED("Automated Forecast", "AI-generated demand forecast"),
        MANUAL("Manual Forecast", "Human-created forecast"),
        HYBRID("Hybrid Forecast", "AI-assisted human forecast"),
        HISTORICAL("Historical Projection", "Based on historical patterns"),
        TEMPLATE_BASED("Template-Based", "Based on project templates"),
        EXPERT_JUDGMENT("Expert Judgment", "Based on expert estimates"),
        REGRESSION_MODEL("Regression Model", "Statistical regression forecast"),
        MACHINE_LEARNING("Machine Learning", "ML-powered prediction"),
        SIMULATION("Monte Carlo Simulation", "Probabilistic simulation"),
        COMPARATIVE("Comparative Analysis", "Based on similar projects");
        
        private final String displayName;
        private final String description;
        
        ForecastType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    /**
     * Categories of resources to forecast
     */
    public enum ResourceCategory {
        ALL_RESOURCES("All Resources", "Comprehensive resource forecast"),
        MATERIALS("Materials", "Raw materials and consumables"),
        TOOLS("Tools", "Tools and equipment needed"),
        COMPONENTS("Components", "Electronic and mechanical components"),
        LABOR("Labor", "Human resources and expertise"),
        FACILITY("Facility", "Workshop space and utilities"),
        EQUIPMENT("Equipment", "Specialized equipment and machinery"),
        BUDGET("Budget", "Financial resource requirements"),
        TIME("Time", "Time and schedule resources"),
        SUPPLIES("Supplies", "General supplies and consumables"),
        SOFTWARE("Software", "Software licenses and tools");
        
        private final String displayName;
        private final String description;
        
        ResourceCategory(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    /**
     * Forecast time horizons
     */
    public enum ForecastHorizon {
        DAILY("Daily", "Daily resource planning", 1),
        WEEKLY("Weekly", "Weekly resource planning", 7),
        BI_WEEKLY("Bi-weekly", "Two-week resource planning", 14),
        MONTHLY("Monthly", "Monthly resource planning", 30),
        BUILD_SEASON("Build Season", "Full 6-week build season", 42),
        COMPETITION_SEASON("Competition Season", "Full competition season", 120),
        ANNUAL("Annual", "Full year planning", 365),
        MULTI_YEAR("Multi-year", "Long-term strategic planning", 730);
        
        private final String displayName;
        private final String description;
        private final int days;
        
        ForecastHorizon(String displayName, String description, int days) {
            this.displayName = displayName;
            this.description = description;
            this.days = days;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public int getDays() { return days; }
    }
    
    // Constructors
    public ResourceDemandForecast() {
        this.forecastDate = LocalDate.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public ResourceDemandForecast(Project project, ForecastType forecastType, 
                                ResourceCategory resourceCategory, ForecastHorizon forecastHorizon) {
        this();
        this.project = project;
        this.forecastType = forecastType;
        this.resourceCategory = resourceCategory;
        this.forecastHorizon = forecastHorizon;
        this.forecastPeriodStart = LocalDate.now();
        this.forecastPeriodEnd = forecastPeriodStart.plusDays(forecastHorizon.getDays());
    }
    
    // Business Methods
    
    /**
     * Calculates the total resource shortfall across all categories.
     */
    public int calculateTotalShortfall() {
        return Math.max(0, totalResourcesRequired - currentResourcesAvailable);
    }
    
    /**
     * Determines if there's a critical resource shortage.
     */
    public boolean hasCriticalShortage() {
        double shortagePercentage = (double) calculateTotalShortfall() / totalResourcesRequired;
        return shortagePercentage > 0.20; // More than 20% shortage
    }
    
    /**
     * Checks if immediate action is required.
     */
    public boolean requiresImmediateAction() {
        return hasCriticalShortage() || 
               (earliestOrderDate != null && LocalDate.now().isAfter(earliestOrderDate)) ||
               (latestOrderDate != null && LocalDate.now().plusDays(leadTimeDays).isAfter(latestOrderDate));
    }
    
    /**
     * Calculates days remaining until latest order date.
     */
    public int getDaysUntilLatestOrder() {
        if (latestOrderDate == null) return -1;
        return (int) java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), latestOrderDate);
    }
    
    /**
     * Determines the criticality level of this forecast.
     */
    public String getCriticalityLevel() {
        if (requiresImmediateAction()) return "CRITICAL";
        if (hasCriticalShortage()) return "HIGH";
        if (calculateTotalShortfall() > 0) return "MEDIUM";
        return "LOW";
    }
    
    /**
     * Calculates the resource utilization efficiency.
     */
    public double getResourceUtilizationEfficiency() {
        if (totalResourcesRequired == 0) return 1.0;
        return Math.min(1.0, (double) currentResourcesAvailable / totalResourcesRequired);
    }
    
    /**
     * Gets the most critical resource category.
     */
    public String getMostCriticalResourceCategory() {
        Map<String, Double> shortageRatios = new HashMap<>();
        
        if (materialsQuantity > 0) {
            shortageRatios.put("Materials", (double) materialsQuantity / totalResourcesRequired);
        }
        if (toolsQuantity > 0) {
            shortageRatios.put("Tools", (double) toolsQuantity / totalResourcesRequired);
        }
        if (componentsQuantity > 0) {
            shortageRatios.put("Components", (double) componentsQuantity / totalResourcesRequired);
        }
        
        return shortageRatios.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Unknown");
    }
    
    /**
     * Calculates cost per resource unit.
     */
    public double getCostPerResourceUnit() {
        if (totalResourcesRequired == 0) return 0.0;
        return estimatedTotalCost / totalResourcesRequired;
    }
    
    /**
     * Determines if forecast is within budget tolerance.
     */
    public boolean isWithinBudgetTolerance(double budgetLimit) {
        return estimatedTotalCost <= budgetLimit;
    }
    
    /**
     * Calculates forecast age in days.
     */
    public int getForecastAge() {
        return (int) java.time.temporal.ChronoUnit.DAYS.between(forecastDate, LocalDate.now());
    }
    
    /**
     * Determines if forecast needs to be updated.
     */
    public boolean needsUpdate() {
        int maxAge = switch (forecastHorizon) {
            case DAILY -> 1;
            case WEEKLY -> 3;
            case BI_WEEKLY -> 7;
            case MONTHLY -> 14;
            default -> 30;
        };
        return getForecastAge() > maxAge;
    }
    
    /**
     * Calculates confidence-adjusted demand.
     */
    public int getConfidenceAdjustedDemand() {
        // Adjust demand based on confidence level
        // Lower confidence = higher safety stock
        double safetyFactor = 1.0 + (1.0 - confidenceLevel) * 0.5;
        return (int) (totalResourcesRequired * safetyFactor);
    }
    
    /**
     * Gets forecast summary for reporting.
     */
    public String getForecastSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Forecast Type: ").append(forecastType.getDisplayName());
        summary.append("\nResource Category: ").append(resourceCategory.getDisplayName());
        summary.append("\nTotal Required: ").append(totalResourcesRequired);
        summary.append("\nAvailable: ").append(currentResourcesAvailable);
        summary.append("\nShortfall: ").append(calculateTotalShortfall());
        summary.append("\nConfidence: ").append(String.format("%.1f%%", confidenceLevel * 100));
        summary.append("\nEstimated Cost: $").append(String.format("%.2f", estimatedTotalCost));
        
        if (requiresImmediateAction()) {
            summary.append("\n⚠️ IMMEDIATE ACTION REQUIRED");
        }
        
        return summary.toString();
    }
    
    /**
     * Calculates scenario range for planning.
     */
    public Map<String, Integer> getScenarioRange() {
        Map<String, Integer> scenarios = new HashMap<>();
        scenarios.put("optimistic", optimisticScenario);
        scenarios.put("mostLikely", mostLikelyScenario);
        scenarios.put("pessimistic", pessimisticScenario);
        scenarios.put("range", pessimisticScenario - optimisticScenario);
        return scenarios;
    }
    
    /**
     * Updates forecast accuracy based on actual usage.
     */
    public void updateAccuracy(int actualUsed, double actualCostIncurred) {
        this.actualResourcesUsed = actualUsed;
        this.actualCost = actualCostIncurred;
        
        if (totalResourcesRequired > 0) {
            // Calculate accuracy as inverse of percentage error
            double quantityError = Math.abs(actualUsed - totalResourcesRequired) / (double) totalResourcesRequired;
            double costError = Math.abs(actualCostIncurred - estimatedTotalCost) / estimatedTotalCost;
            
            // Combined accuracy score (lower error = higher accuracy)
            this.forecastAccuracy = 1.0 - Math.min(1.0, (quantityError + costError) / 2.0);
        }
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
    
    public LocalDate getForecastDate() { return forecastDate; }
    public void setForecastDate(LocalDate forecastDate) { this.forecastDate = forecastDate; }
    
    public LocalDate getForecastPeriodStart() { return forecastPeriodStart; }
    public void setForecastPeriodStart(LocalDate forecastPeriodStart) { this.forecastPeriodStart = forecastPeriodStart; }
    
    public LocalDate getForecastPeriodEnd() { return forecastPeriodEnd; }
    public void setForecastPeriodEnd(LocalDate forecastPeriodEnd) { this.forecastPeriodEnd = forecastPeriodEnd; }
    
    public ForecastType getForecastType() { return forecastType; }
    public void setForecastType(ForecastType forecastType) { this.forecastType = forecastType; }
    
    public ResourceCategory getResourceCategory() { return resourceCategory; }
    public void setResourceCategory(ResourceCategory resourceCategory) { this.resourceCategory = resourceCategory; }
    
    public ForecastHorizon getForecastHorizon() { return forecastHorizon; }
    public void setForecastHorizon(ForecastHorizon forecastHorizon) { this.forecastHorizon = forecastHorizon; }
    
    public Double getConfidenceLevel() { return confidenceLevel; }
    public void setConfidenceLevel(Double confidenceLevel) { this.confidenceLevel = confidenceLevel; }
    
    public Double getAccuracyScore() { return accuracyScore; }
    public void setAccuracyScore(Double accuracyScore) { this.accuracyScore = accuracyScore; }
    
    public Integer getTotalResourcesRequired() { return totalResourcesRequired; }
    public void setTotalResourcesRequired(Integer totalResourcesRequired) { this.totalResourcesRequired = totalResourcesRequired; }
    
    public Integer getCurrentResourcesAvailable() { return currentResourcesAvailable; }
    public void setCurrentResourcesAvailable(Integer currentResourcesAvailable) { this.currentResourcesAvailable = currentResourcesAvailable; }
    
    public Integer getProjectedShortfall() { return projectedShortfall; }
    public void setProjectedShortfall(Integer projectedShortfall) { this.projectedShortfall = projectedShortfall; }
    
    public Double getEstimatedTotalCost() { return estimatedTotalCost; }
    public void setEstimatedTotalCost(Double estimatedTotalCost) { this.estimatedTotalCost = estimatedTotalCost; }
    
    public Double getBudgetImpact() { return budgetImpact; }
    public void setBudgetImpact(Double budgetImpact) { this.budgetImpact = budgetImpact; }
    
    public LocalDate getPeakDemandDate() { return peakDemandDate; }
    public void setPeakDemandDate(LocalDate peakDemandDate) { this.peakDemandDate = peakDemandDate; }
    
    public Integer getPeakDemandQuantity() { return peakDemandQuantity; }
    public void setPeakDemandQuantity(Integer peakDemandQuantity) { this.peakDemandQuantity = peakDemandQuantity; }
    
    public LocalDate getEarliestOrderDate() { return earliestOrderDate; }
    public void setEarliestOrderDate(LocalDate earliestOrderDate) { this.earliestOrderDate = earliestOrderDate; }
    
    public LocalDate getLatestOrderDate() { return latestOrderDate; }
    public void setLatestOrderDate(LocalDate latestOrderDate) { this.latestOrderDate = latestOrderDate; }
    
    public Integer getLeadTimeDays() { return leadTimeDays; }
    public void setLeadTimeDays(Integer leadTimeDays) { this.leadTimeDays = leadTimeDays; }
    
    public Integer getMaterialsQuantity() { return materialsQuantity; }
    public void setMaterialsQuantity(Integer materialsQuantity) { this.materialsQuantity = materialsQuantity; }
    
    public Integer getToolsQuantity() { return toolsQuantity; }
    public void setToolsQuantity(Integer toolsQuantity) { this.toolsQuantity = toolsQuantity; }
    
    public Integer getComponentsQuantity() { return componentsQuantity; }
    public void setComponentsQuantity(Integer componentsQuantity) { this.componentsQuantity = componentsQuantity; }
    
    public Integer getLaborHours() { return laborHours; }
    public void setLaborHours(Integer laborHours) { this.laborHours = laborHours; }
    
    public Integer getFacilityHours() { return facilityHours; }
    public void setFacilityHours(Integer facilityHours) { this.facilityHours = facilityHours; }
    
    public Integer getEquipmentHours() { return equipmentHours; }
    public void setEquipmentHours(Integer equipmentHours) { this.equipmentHours = equipmentHours; }
    
    public Double getMaterialsCost() { return materialsCost; }
    public void setMaterialsCost(Double materialsCost) { this.materialsCost = materialsCost; }
    
    public Double getToolsCost() { return toolsCost; }
    public void setToolsCost(Double toolsCost) { this.toolsCost = toolsCost; }
    
    public Double getComponentsCost() { return componentsCost; }
    public void setComponentsCost(Double componentsCost) { this.componentsCost = componentsCost; }
    
    public Double getLaborCost() { return laborCost; }
    public void setLaborCost(Double laborCost) { this.laborCost = laborCost; }
    
    public Double getFacilityCost() { return facilityCost; }
    public void setFacilityCost(Double facilityCost) { this.facilityCost = facilityCost; }
    
    public Double getEquipmentCost() { return equipmentCost; }
    public void setEquipmentCost(Double equipmentCost) { this.equipmentCost = equipmentCost; }
    
    public Double getDemandVariability() { return demandVariability; }
    public void setDemandVariability(Double demandVariability) { this.demandVariability = demandVariability; }
    
    public Double getSupplyRisk() { return supplyRisk; }
    public void setSupplyRisk(Double supplyRisk) { this.supplyRisk = supplyRisk; }
    
    public Double getPriceVolatility() { return priceVolatility; }
    public void setPriceVolatility(Double priceVolatility) { this.priceVolatility = priceVolatility; }
    
    public Double getSeasonalFactor() { return seasonalFactor; }
    public void setSeasonalFactor(Double seasonalFactor) { this.seasonalFactor = seasonalFactor; }
    
    public Integer getOptimisticScenario() { return optimisticScenario; }
    public void setOptimisticScenario(Integer optimisticScenario) { this.optimisticScenario = optimisticScenario; }
    
    public Integer getPessimisticScenario() { return pessimisticScenario; }
    public void setPessimisticScenario(Integer pessimisticScenario) { this.pessimisticScenario = pessimisticScenario; }
    
    public Integer getMostLikelyScenario() { return mostLikelyScenario; }
    public void setMostLikelyScenario(Integer mostLikelyScenario) { this.mostLikelyScenario = mostLikelyScenario; }
    
    public String getExternalFactors() { return externalFactors; }
    public void setExternalFactors(String externalFactors) { this.externalFactors = externalFactors; }
    
    public String getForecastAssumptions() { return forecastAssumptions; }
    public void setForecastAssumptions(String forecastAssumptions) { this.forecastAssumptions = forecastAssumptions; }
    
    public String getMitigationStrategies() { return mitigationStrategies; }
    public void setMitigationStrategies(String mitigationStrategies) { this.mitigationStrategies = mitigationStrategies; }
    
    public String getRecommendedActions() { return recommendedActions; }
    public void setRecommendedActions(String recommendedActions) { this.recommendedActions = recommendedActions; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public TeamMember getCreatedBy() { return createdBy; }
    public void setCreatedBy(TeamMember createdBy) { this.createdBy = createdBy; }
    
    public TeamMember getApprovedBy() { return approvedBy; }
    public void setApprovedBy(TeamMember approvedBy) { this.approvedBy = approvedBy; }
    
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    
    public String getAnalyticsData() { return analyticsData; }
    public void setAnalyticsData(String analyticsData) { this.analyticsData = analyticsData; }
    
    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }
    
    public LocalDateTime getLastModelUpdate() { return lastModelUpdate; }
    public void setLastModelUpdate(LocalDateTime lastModelUpdate) { this.lastModelUpdate = lastModelUpdate; }
    
    public Integer getActualResourcesUsed() { return actualResourcesUsed; }
    public void setActualResourcesUsed(Integer actualResourcesUsed) { this.actualResourcesUsed = actualResourcesUsed; }
    
    public Double getActualCost() { return actualCost; }
    public void setActualCost(Double actualCost) { this.actualCost = actualCost; }
    
    public Double getForecastAccuracy() { return forecastAccuracy; }
    public void setForecastAccuracy(Double forecastAccuracy) { this.forecastAccuracy = forecastAccuracy; }
    
    @Override
    public String toString() {
        return String.format("ResourceDemandForecast{id=%d, project='%s', type=%s, category=%s, required=%d, available=%d, cost=%.2f}", 
                           id, project != null ? project.getName() : "Unknown", 
                           forecastType, resourceCategory, totalResourcesRequired, 
                           currentResourcesAvailable, estimatedTotalCost);
    }
}