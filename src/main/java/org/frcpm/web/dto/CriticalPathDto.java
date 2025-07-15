// src/main/java/org/frcpm/web/dto/CriticalPathDto.java
// Phase 2E-D: Critical Path Analysis Data Transfer Object

package org.frcpm.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object for critical path analysis results.
 * 
 * ✅ PHASE 2E-D: New DTO for critical path analysis and visualization
 * 
 * This DTO carries critical path analysis results for REST API operations,
 * including critical path tasks, dependencies, task floats, and project
 * timeline information for visualization and optimization.
 * 
 * @author FRC Project Management Team - Phase 2E-D
 * @version 2.0.0-2E-D
 * @since Phase 2E-D - Advanced Task Management
 */
public class CriticalPathDto {
    
    @NotNull
    private Long projectId;
    
    private String projectName;
    
    private double totalDuration;
    
    private List<Map<String, Object>> criticalTasks = new ArrayList<>();
    
    private List<TaskDependencyDto> criticalDependencies = new ArrayList<>();
    
    private Map<String, Double> taskFloats = new HashMap<>();
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime calculatedAt;
    
    private String durationUnit = "hours";
    
    // Analysis metrics
    private int totalTasks;
    private int totalDependencies;
    private int criticalTaskCount;
    private int criticalDependencyCount;
    private double criticalPathPercentage;
    private double averageFloat;
    
    // Risk assessment
    private String riskLevel;
    private List<String> riskFactors = new ArrayList<>();
    private List<String> recommendations = new ArrayList<>();
    
    // Schedule information
    private String earliestStart;
    private String latestFinish;
    private String plannedStart;
    private String plannedFinish;
    
    // Default constructor
    public CriticalPathDto() {
        this.calculatedAt = LocalDateTime.now();
    }
    
    // Constructor with project ID
    public CriticalPathDto(Long projectId) {
        this();
        this.projectId = projectId;
    }
    
    // =========================================================================
    // GETTERS AND SETTERS
    // =========================================================================
    
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    
    public double getTotalDuration() { return totalDuration; }
    public void setTotalDuration(double totalDuration) { this.totalDuration = totalDuration; }
    
    public List<Map<String, Object>> getCriticalTasks() { return criticalTasks; }
    public void setCriticalTasks(List<Map<String, Object>> criticalTasks) { this.criticalTasks = criticalTasks; }
    
    public List<TaskDependencyDto> getCriticalDependencies() { return criticalDependencies; }
    public void setCriticalDependencies(List<TaskDependencyDto> criticalDependencies) { this.criticalDependencies = criticalDependencies; }
    
    public Map<String, Double> getTaskFloats() { return taskFloats; }
    public void setTaskFloats(Map<String, Double> taskFloats) { this.taskFloats = taskFloats; }
    
    public LocalDateTime getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(LocalDateTime calculatedAt) { this.calculatedAt = calculatedAt; }
    
    public String getDurationUnit() { return durationUnit; }
    public void setDurationUnit(String durationUnit) { this.durationUnit = durationUnit; }
    
    // Analysis metrics
    public int getTotalTasks() { return totalTasks; }
    public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }
    
    public int getTotalDependencies() { return totalDependencies; }
    public void setTotalDependencies(int totalDependencies) { this.totalDependencies = totalDependencies; }
    
    public int getCriticalTaskCount() { return criticalTaskCount; }
    public void setCriticalTaskCount(int criticalTaskCount) { this.criticalTaskCount = criticalTaskCount; }
    
    public int getCriticalDependencyCount() { return criticalDependencyCount; }
    public void setCriticalDependencyCount(int criticalDependencyCount) { this.criticalDependencyCount = criticalDependencyCount; }
    
    public double getCriticalPathPercentage() { return criticalPathPercentage; }
    public void setCriticalPathPercentage(double criticalPathPercentage) { this.criticalPathPercentage = criticalPathPercentage; }
    
    public double getAverageFloat() { return averageFloat; }
    public void setAverageFloat(double averageFloat) { this.averageFloat = averageFloat; }
    
    // Risk assessment
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    
    public List<String> getRiskFactors() { return riskFactors; }
    public void setRiskFactors(List<String> riskFactors) { this.riskFactors = riskFactors; }
    
    public List<String> getRecommendations() { return recommendations; }
    public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
    
    // Schedule information
    public String getEarliestStart() { return earliestStart; }
    public void setEarliestStart(String earliestStart) { this.earliestStart = earliestStart; }
    
    public String getLatestFinish() { return latestFinish; }
    public void setLatestFinish(String latestFinish) { this.latestFinish = latestFinish; }
    
    public String getPlannedStart() { return plannedStart; }
    public void setPlannedStart(String plannedStart) { this.plannedStart = plannedStart; }
    
    public String getPlannedFinish() { return plannedFinish; }
    public void setPlannedFinish(String plannedFinish) { this.plannedFinish = plannedFinish; }
    
    // =========================================================================
    // CONVENIENCE METHODS
    // =========================================================================
    
    /**
     * Add a critical task to the analysis.
     * 
     * @param taskId Task ID
     * @param taskTitle Task title
     * @param taskFloat Task float value
     * @param additionalData Additional task data
     */
    public void addCriticalTask(Long taskId, String taskTitle, Double taskFloat, Map<String, Object> additionalData) {
        Map<String, Object> task = new HashMap<>();
        task.put("id", taskId);
        task.put("title", taskTitle);
        task.put("float", taskFloat);
        task.put("isCritical", taskFloat != null && taskFloat == 0.0);
        
        if (additionalData != null) {
            task.putAll(additionalData);
        }
        
        criticalTasks.add(task);
        taskFloats.put(taskId.toString(), taskFloat);
    }
    
    /**
     * Add a critical dependency to the analysis.
     * 
     * @param dependency Critical dependency DTO
     */
    public void addCriticalDependency(TaskDependencyDto dependency) {
        dependency.setOnCriticalPath(true);
        criticalDependencies.add(dependency);
    }
    
    /**
     * Calculate metrics based on current data.
     */
    public void calculateMetrics() {
        criticalTaskCount = criticalTasks.size();
        criticalDependencyCount = criticalDependencies.size();
        
        if (totalTasks > 0) {
            criticalPathPercentage = (double) criticalTaskCount / totalTasks * 100;
        }
        
        if (!taskFloats.isEmpty()) {
            averageFloat = taskFloats.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        }
    }
    
    /**
     * Get total duration formatted for display.
     * 
     * @return formatted duration string
     */
    public String getFormattedDuration() {
        if (totalDuration == 0) {
            return "0 " + durationUnit;
        }
        
        if ("hours".equals(durationUnit)) {
            if (totalDuration >= 24) {
                double days = totalDuration / 24;
                return String.format("%.1f days (%.1f hours)", days, totalDuration);
            } else {
                return String.format("%.1f hours", totalDuration);
            }
        } else if ("days".equals(durationUnit)) {
            return String.format("%.1f days", totalDuration);
        }
        
        return String.format("%.1f %s", totalDuration, durationUnit);
    }
    
    /**
     * Get critical path percentage formatted for display.
     * 
     * @return formatted percentage string
     */
    public String getFormattedCriticalPathPercentage() {
        return String.format("%.1f%%", criticalPathPercentage);
    }
    
    /**
     * Get risk level CSS class for UI styling.
     * 
     * @return CSS class name
     */
    public String getRiskLevelClass() {
        if (riskLevel == null) {
            return "text-secondary";
        }
        
        switch (riskLevel.toUpperCase()) {
            case "LOW":
                return "text-success";
            case "MEDIUM":
                return "text-warning";
            case "HIGH":
                return "text-danger";
            case "CRITICAL":
                return "text-danger fw-bold";
            default:
                return "text-secondary";
        }
    }
    
    /**
     * Get risk level icon for UI display.
     * 
     * @return Font Awesome icon class
     */
    public String getRiskLevelIcon() {
        if (riskLevel == null) {
            return "fas fa-question-circle";
        }
        
        switch (riskLevel.toUpperCase()) {
            case "LOW":
                return "fas fa-check-circle";
            case "MEDIUM":
                return "fas fa-exclamation-triangle";
            case "HIGH":
                return "fas fa-exclamation-circle";
            case "CRITICAL":
                return "fas fa-times-circle";
            default:
                return "fas fa-question-circle";
        }
    }
    
    /**
     * Add a risk factor to the analysis.
     * 
     * @param riskFactor Risk factor description
     */
    public void addRiskFactor(String riskFactor) {
        if (riskFactor != null && !riskFactor.trim().isEmpty()) {
            riskFactors.add(riskFactor);
        }
    }
    
    /**
     * Add a recommendation to the analysis.
     * 
     * @param recommendation Recommendation description
     */
    public void addRecommendation(String recommendation) {
        if (recommendation != null && !recommendation.trim().isEmpty()) {
            recommendations.add(recommendation);
        }
    }
    
    /**
     * Check if the critical path analysis indicates high risk.
     * 
     * @return true if high risk, false otherwise
     */
    public boolean isHighRisk() {
        return "HIGH".equalsIgnoreCase(riskLevel) || "CRITICAL".equalsIgnoreCase(riskLevel);
    }
    
    /**
     * Get tasks with zero float (critical tasks).
     * 
     * @return list of critical task data
     */
    public List<Map<String, Object>> getZeroFloatTasks() {
        return criticalTasks.stream()
            .filter(task -> {
                Object floatValue = task.get("float");
                return floatValue instanceof Double && ((Double) floatValue) == 0.0;
            })
            .collect(ArrayList::new, (list, task) -> list.add(task), ArrayList::addAll);
    }
    
    /**
     * Get tasks with float greater than zero (non-critical tasks).
     * 
     * @return list of non-critical task data
     */
    public List<Map<String, Object>> getNonCriticalTasks() {
        return criticalTasks.stream()
            .filter(task -> {
                Object floatValue = task.get("float");
                return floatValue instanceof Double && ((Double) floatValue) > 0.0;
            })
            .collect(ArrayList::new, (list, task) -> list.add(task), ArrayList::addAll);
    }
    
    @Override
    public String toString() {
        return String.format("CriticalPathDto{projectId=%d, totalDuration=%.1f %s, criticalTasks=%d, criticalDependencies=%d, riskLevel='%s'}",
                            projectId, totalDuration, durationUnit, criticalTaskCount, criticalDependencyCount, riskLevel);
    }
}