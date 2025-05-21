// src/main/java/org/frcpm/services/ReportGenerationService.java
package org.frcpm.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service interface for report generation operations.
 * This service provides methods to generate various reports
 * for project management and analysis.
 */
public interface ReportGenerationService {
    
    /**
     * Generate a project summary report.
     * 
     * @param projectId The ID of the project
     * @return Map containing report data
     */
    Map<String, Object> generateProjectSummaryReport(Long projectId);
    
    /**
     * Generate a team performance report.
     * 
     * @param projectId The ID of the project
     * @param startDate Start date for the report period
     * @param endDate End date for the report period
     * @return Map containing report data
     */
    Map<String, Object> generateTeamPerformanceReport(Long projectId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Generate a milestone status report.
     * 
     * @param projectId The ID of the project
     * @return Map containing report data
     */
    Map<String, Object> generateMilestoneStatusReport(Long projectId);
    
    /**
     * Generate an attendance report.
     * 
     * @param projectId The ID of the project
     * @param startDate Start date for the report period
     * @param endDate End date for the report period
     * @return Map containing report data
     */
    Map<String, Object> generateAttendanceReport(Long projectId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Generate a subsystem progress report.
     * 
     * @param projectId The ID of the project
     * @return Map containing report data
     */
    Map<String, Object> generateSubsystemProgressReport(Long projectId);
    
    /**
     * Generate an individual team member report.
     * 
     * @param teamMemberId The ID of the team member
     * @param startDate Start date for the report period
     * @param endDate End date for the report period
     * @return Map containing report data
     */
    Map<String, Object> generateTeamMemberReport(Long teamMemberId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Generate a project timeline report.
     * 
     * @param projectId The ID of the project
     * @return Map containing report data
     */
    Map<String, Object> generateProjectTimelineReport(Long projectId);
    
    /**
     * Export a report to PDF format.
     * 
     * @param reportData Report data to export
     * @param reportType Type of report
     * @return Byte array containing the PDF data
     */
    byte[] exportReportToPdf(Map<String, Object> reportData, String reportType);
    
    /**
     * Export a report to CSV format.
     * 
     * @param reportData Report data to export
     * @param reportType Type of report
     * @return String containing the CSV data
     */
    String exportReportToCsv(Map<String, Object> reportData, String reportType);
    
    /**
     * Generate a custom report using specified metrics.
     * 
     * @param projectId The ID of the project
     * @param metrics List of metrics to include in the report
     * @param startDate Start date for the report period
     * @param endDate End date for the report period
     * @return Map containing report data
     */
    Map<String, Object> generateCustomReport(Long projectId, List<String> metrics, LocalDate startDate, LocalDate endDate);
}