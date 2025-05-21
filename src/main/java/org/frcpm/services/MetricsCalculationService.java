// src/main/java/org/frcpm/services/MetricsCalculationService.java
package org.frcpm.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service interface for metrics calculation operations.
 * This service provides methods to calculate various performance metrics
 * for projects, team members, and tasks.
 */
public interface MetricsCalculationService {
    
    /**
     * Calculates project progress metrics.
     * 
     * @param projectId The ID of the project
     * @return Map containing various project progress metrics
     */
    Map<String, Object> calculateProjectProgressMetrics(Long projectId);
    
    /**
     * Calculates team performance metrics.
     * 
     * @param projectId The ID of the project
     * @param startDate Optional start date for filtering
     * @param endDate Optional end date for filtering
     * @return Map containing team performance metrics
     */
    Map<String, Object> calculateTeamPerformanceMetrics(Long projectId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Calculates task completion metrics.
     * 
     * @param projectId The ID of the project
     * @return Map containing task completion metrics
     */
    Map<String, Object> calculateTaskCompletionMetrics(Long projectId);
    
    /**
     * Calculates attendance metrics.
     * 
     * @param projectId The ID of the project
     * @param startDate Optional start date for filtering
     * @param endDate Optional end date for filtering
     * @return Map containing attendance metrics
     */
    Map<String, Object> calculateAttendanceMetrics(Long projectId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Calculates timeline deviation metrics.
     * 
     * @param projectId The ID of the project
     * @return Map containing timeline deviation metrics
     */
    Map<String, Object> calculateTimelineDeviationMetrics(Long projectId);
    
    /**
     * Calculates individual team member performance metrics.
     * 
     * @param teamMemberId The ID of the team member
     * @param startDate Optional start date for filtering
     * @param endDate Optional end date for filtering
     * @return Map containing individual performance metrics
     */
    Map<String, Object> calculateIndividualPerformanceMetrics(Long teamMemberId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Calculates subsystem performance metrics.
     * 
     * @param projectId The ID of the project
     * @return Map containing subsystem performance metrics
     */
    Map<String, Object> calculateSubsystemPerformanceMetrics(Long projectId);
    
    /**
     * Generates a project health dashboard.
     * 
     * @param projectId The ID of the project
     * @return Map containing project health indicators
     */
    Map<String, Object> generateProjectHealthDashboard(Long projectId);
}