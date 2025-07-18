// src/main/java/org/frcpm/services/ReportingService.java
// Phase 3B: Advanced Reporting & Analytics Service Interface

package org.frcpm.services;

import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.models.TeamMember;
import org.frcpm.models.Meeting;
import org.frcpm.models.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service interface for advanced reporting and analytics.
 * 
 * Provides comprehensive reporting capabilities for FRC project management,
 * including project analytics, team performance metrics, and data visualization.
 * 
 * Features:
 * - Project progress and completion analytics
 * - Team performance and productivity metrics
 * - Task and dependency analytics
 * - Meeting effectiveness analysis
 * - Resource utilization reporting
 * - Custom report generation
 * - Data export capabilities
 * 
 * @author FRC Project Management Team
 * @version 3.0.0-3B
 * @since Phase 3B - Advanced Reporting & Analytics
 */
public interface ReportingService {
    
    // =========================================================================
    // PROJECT ANALYTICS
    // =========================================================================
    
    /**
     * Get comprehensive project analytics.
     * 
     * @param projectId the project ID
     * @return project analytics data
     */
    Map<String, Object> getProjectAnalytics(Long projectId);
    
    /**
     * Get project progress analytics over time.
     * 
     * @param projectId the project ID
     * @param startDate start date for analysis
     * @param endDate end date for analysis
     * @return progress analytics data
     */
    Map<String, Object> getProjectProgressAnalytics(Long projectId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Get project completion forecast.
     * 
     * @param projectId the project ID
     * @return completion forecast data
     */
    Map<String, Object> getProjectCompletionForecast(Long projectId);
    
    /**
     * Get project risk analysis.
     * 
     * @param projectId the project ID
     * @return risk analysis data
     */
    Map<String, Object> getProjectRiskAnalysis(Long projectId);
    
    /**
     * Get project milestone analysis.
     * 
     * @param projectId the project ID
     * @return milestone analysis data
     */
    Map<String, Object> getProjectMilestoneAnalysis(Long projectId);
    
    // =========================================================================
    // TEAM PERFORMANCE ANALYTICS
    // =========================================================================
    
    /**
     * Get team performance metrics.
     * 
     * @param projectId the project ID
     * @return team performance data
     */
    Map<String, Object> getTeamPerformanceMetrics(Long projectId);
    
    /**
     * Get individual team member performance.
     * 
     * @param teamMemberId the team member ID
     * @param startDate start date for analysis
     * @param endDate end date for analysis
     * @return individual performance data
     */
    Map<String, Object> getIndividualPerformanceMetrics(Long teamMemberId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Get team productivity analytics.
     * 
     * @param projectId the project ID
     * @param periodDays period in days for analysis
     * @return productivity analytics data
     */
    Map<String, Object> getTeamProductivityAnalytics(Long projectId, int periodDays);
    
    /**
     * Get team collaboration metrics.
     * 
     * @param projectId the project ID
     * @return collaboration metrics data
     */
    Map<String, Object> getTeamCollaborationMetrics(Long projectId);
    
    /**
     * Get team workload distribution.
     * 
     * @param projectId the project ID
     * @return workload distribution data
     */
    Map<String, Object> getTeamWorkloadDistribution(Long projectId);
    
    // =========================================================================
    // TASK ANALYTICS
    // =========================================================================
    
    /**
     * Get task completion analytics.
     * 
     * @param projectId the project ID
     * @return task completion data
     */
    Map<String, Object> getTaskCompletionAnalytics(Long projectId);
    
    /**
     * Get task priority analytics.
     * 
     * @param projectId the project ID
     * @return task priority data
     */
    Map<String, Object> getTaskPriorityAnalytics(Long projectId);
    
    /**
     * Get task dependency analytics.
     * 
     * @param projectId the project ID
     * @return dependency analytics data
     */
    Map<String, Object> getTaskDependencyAnalytics(Long projectId);
    
    /**
     * Get task cycle time analytics.
     * 
     * @param projectId the project ID
     * @param periodDays period in days for analysis
     * @return cycle time data
     */
    Map<String, Object> getTaskCycleTimeAnalytics(Long projectId, int periodDays);
    
    /**
     * Get task bottleneck analysis.
     * 
     * @param projectId the project ID
     * @return bottleneck analysis data
     */
    Map<String, Object> getTaskBottleneckAnalysis(Long projectId);
    
    // =========================================================================
    // MEETING ANALYTICS
    // =========================================================================
    
    /**
     * Get meeting effectiveness analytics.
     * 
     * @param projectId the project ID
     * @return meeting effectiveness data
     */
    Map<String, Object> getMeetingEffectivenessAnalytics(Long projectId);
    
    /**
     * Get meeting attendance analytics.
     * 
     * @param projectId the project ID
     * @param periodDays period in days for analysis
     * @return attendance analytics data
     */
    Map<String, Object> getMeetingAttendanceAnalytics(Long projectId, int periodDays);
    
    /**
     * Get meeting frequency analytics.
     * 
     * @param projectId the project ID
     * @return frequency analytics data
     */
    Map<String, Object> getMeetingFrequencyAnalytics(Long projectId);
    
    /**
     * Get meeting outcome analytics.
     * 
     * @param projectId the project ID
     * @return outcome analytics data
     */
    Map<String, Object> getMeetingOutcomeAnalytics(Long projectId);
    
    // =========================================================================
    // RESOURCE ANALYTICS
    // =========================================================================
    
    /**
     * Get resource utilization analytics.
     * 
     * @param projectId the project ID
     * @return resource utilization data
     */
    Map<String, Object> getResourceUtilizationAnalytics(Long projectId);
    
    /**
     * Get resource allocation analytics.
     * 
     * @param projectId the project ID
     * @return allocation analytics data
     */
    Map<String, Object> getResourceAllocationAnalytics(Long projectId);
    
    /**
     * Get resource capacity planning.
     * 
     * @param projectId the project ID
     * @param forecastDays days to forecast
     * @return capacity planning data
     */
    Map<String, Object> getResourceCapacityPlanning(Long projectId, int forecastDays);
    
    // =========================================================================
    // CUSTOM REPORTS
    // =========================================================================
    
    /**
     * Generate custom report.
     * 
     * @param reportType the report type
     * @param parameters report parameters
     * @return custom report data
     */
    Map<String, Object> generateCustomReport(String reportType, Map<String, Object> parameters);
    
    /**
     * Get available report templates.
     * 
     * @return list of report templates
     */
    List<Map<String, Object>> getAvailableReportTemplates();
    
    /**
     * Save custom report template.
     * 
     * @param template the report template
     * @return saved template ID
     */
    Long saveReportTemplate(Map<String, Object> template);
    
    /**
     * Get saved report templates for user.
     * 
     * @param userId the user ID
     * @return list of saved templates
     */
    List<Map<String, Object>> getSavedReportTemplates(Long userId);
    
    // =========================================================================
    // DATA EXPORT
    // =========================================================================
    
    /**
     * Export project data to CSV.
     * 
     * @param projectId the project ID
     * @param includeDetails whether to include detailed data
     * @return CSV export data
     */
    byte[] exportProjectDataToCsv(Long projectId, boolean includeDetails);
    
    /**
     * Export project data to Excel.
     * 
     * @param projectId the project ID
     * @param includeDetails whether to include detailed data
     * @return Excel export data
     */
    byte[] exportProjectDataToExcel(Long projectId, boolean includeDetails);
    
    /**
     * Export project data to PDF.
     * 
     * @param projectId the project ID
     * @param reportType the report type
     * @return PDF export data
     */
    byte[] exportProjectDataToPdf(Long projectId, String reportType);
    
    /**
     * Export analytics data to JSON.
     * 
     * @param projectId the project ID
     * @param analyticsType the analytics type
     * @return JSON export data
     */
    String exportAnalyticsToJson(Long projectId, String analyticsType);
    
    // =========================================================================
    // DASHBOARD DATA
    // =========================================================================
    
    /**
     * Get executive dashboard data.
     * 
     * @param projectId the project ID
     * @return executive dashboard data
     */
    Map<String, Object> getExecutiveDashboardData(Long projectId);
    
    /**
     * Get team dashboard data.
     * 
     * @param projectId the project ID
     * @return team dashboard data
     */
    Map<String, Object> getTeamDashboardData(Long projectId);
    
    /**
     * Get project manager dashboard data.
     * 
     * @param projectId the project ID
     * @return project manager dashboard data
     */
    Map<String, Object> getProjectManagerDashboardData(Long projectId);
    
    /**
     * Get student dashboard data.
     * 
     * @param projectId the project ID
     * @param studentId the student ID
     * @return student dashboard data
     */
    Map<String, Object> getStudentDashboardData(Long projectId, Long studentId);
    
    // =========================================================================
    // COMPARATIVE ANALYTICS
    // =========================================================================
    
    /**
     * Compare project performance with historical data.
     * 
     * @param projectId the project ID
     * @param compareWithProjects list of project IDs to compare with
     * @return comparative analytics data
     */
    Map<String, Object> getComparativeProjectAnalytics(Long projectId, List<Long> compareWithProjects);
    
    /**
     * Get team performance comparison.
     * 
     * @param projectId the project ID
     * @param compareWithProjects list of project IDs to compare with
     * @return team performance comparison data
     */
    Map<String, Object> getTeamPerformanceComparison(Long projectId, List<Long> compareWithProjects);
    
    /**
     * Get benchmark analytics.
     * 
     * @param projectId the project ID
     * @return benchmark analytics data
     */
    Map<String, Object> getBenchmarkAnalytics(Long projectId);
    
    // =========================================================================
    // TREND ANALYSIS
    // =========================================================================
    
    /**
     * Get project velocity trends.
     * 
     * @param projectId the project ID
     * @param periodDays period in days for analysis
     * @return velocity trend data
     */
    Map<String, Object> getProjectVelocityTrends(Long projectId, int periodDays);
    
    /**
     * Get team performance trends.
     * 
     * @param projectId the project ID
     * @param periodDays period in days for analysis
     * @return performance trend data
     */
    Map<String, Object> getTeamPerformanceTrends(Long projectId, int periodDays);
    
    /**
     * Get quality trends.
     * 
     * @param projectId the project ID
     * @param periodDays period in days for analysis
     * @return quality trend data
     */
    Map<String, Object> getQualityTrends(Long projectId, int periodDays);
    
    // =========================================================================
    // PREDICTIVE ANALYTICS
    // =========================================================================
    
    /**
     * Get project completion prediction.
     * 
     * @param projectId the project ID
     * @return completion prediction data
     */
    Map<String, Object> getProjectCompletionPrediction(Long projectId);
    
    /**
     * Get resource demand prediction.
     * 
     * @param projectId the project ID
     * @param forecastDays days to forecast
     * @return resource demand prediction data
     */
    Map<String, Object> getResourceDemandPrediction(Long projectId, int forecastDays);
    
    /**
     * Get risk prediction analytics.
     * 
     * @param projectId the project ID
     * @return risk prediction data
     */
    Map<String, Object> getRiskPredictionAnalytics(Long projectId);
    
    // =========================================================================
    // ASYNC OPERATIONS
    // =========================================================================
    
    /**
     * Generate comprehensive analytics report asynchronously.
     * 
     * @param projectId the project ID
     * @param reportType the report type
     * @return future result
     */
    CompletableFuture<Map<String, Object>> generateAnalyticsReportAsync(Long projectId, String reportType);
    
    /**
     * Generate data export asynchronously.
     * 
     * @param projectId the project ID
     * @param exportType the export type
     * @param includeDetails whether to include detailed data
     * @return future result
     */
    CompletableFuture<byte[]> generateDataExportAsync(Long projectId, String exportType, boolean includeDetails);
    
    /**
     * Calculate complex analytics asynchronously.
     * 
     * @param projectId the project ID
     * @param analyticsType the analytics type
     * @param parameters analysis parameters
     * @return future result
     */
    CompletableFuture<Map<String, Object>> calculateComplexAnalyticsAsync(Long projectId, String analyticsType, Map<String, Object> parameters);
    
    // =========================================================================
    // REPORT SCHEDULING
    // =========================================================================
    
    /**
     * Schedule automatic report generation.
     * 
     * @param projectId the project ID
     * @param reportType the report type
     * @param schedule the schedule configuration
     * @return scheduled report ID
     */
    Long scheduleAutomaticReport(Long projectId, String reportType, Map<String, Object> schedule);
    
    /**
     * Get scheduled reports for project.
     * 
     * @param projectId the project ID
     * @return list of scheduled reports
     */
    List<Map<String, Object>> getScheduledReports(Long projectId);
    
    /**
     * Cancel scheduled report.
     * 
     * @param scheduledReportId the scheduled report ID
     * @return true if cancelled successfully
     */
    boolean cancelScheduledReport(Long scheduledReportId);
    
    // =========================================================================
    // VISUALIZATION DATA
    // =========================================================================
    
    /**
     * Get chart data for project analytics.
     * 
     * @param projectId the project ID
     * @param chartType the chart type
     * @param parameters chart parameters
     * @return chart data
     */
    Map<String, Object> getChartData(Long projectId, String chartType, Map<String, Object> parameters);
    
    /**
     * Get available chart types.
     * 
     * @return list of available chart types
     */
    List<String> getAvailableChartTypes();
    
    /**
     * Get chart configuration options.
     * 
     * @param chartType the chart type
     * @return configuration options
     */
    Map<String, Object> getChartConfigurationOptions(String chartType);
}