// src/main/java/org/frcpm/services/impl/TestableReportGenerationServiceAsyncImpl.java

package org.frcpm.services.impl;

import org.frcpm.repositories.specific.*;
import org.frcpm.services.GanttDataService;
import org.frcpm.services.MetricsCalculationService;
import org.frcpm.services.ReportGenerationService;
import org.frcpm.services.VisualizationService;
import org.frcpm.di.ServiceLocator;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A testable async implementation of ReportGenerationService that accepts injected repositories
 * for better unit testing. This implementation provides async methods that can be stubbed
 * in tests without requiring spy patterns.
 */
public class TestableReportGenerationServiceAsyncImpl extends TestableReportGenerationServiceImpl {
    
    private static final Logger LOGGER = Logger.getLogger(TestableReportGenerationServiceAsyncImpl.class.getName());
    
    /**
     * Constructor for MVVMFx injection.
     * Uses ServiceLocator for default initialization.
     */
    public TestableReportGenerationServiceAsyncImpl() {
        super();
    }
    
    /**
     * Constructor for manual/test injection.
     * 
     * @param projectRepository the project repository
     * @param taskRepository the task repository
     * @param teamMemberRepository the team member repository
     * @param milestoneRepository the milestone repository
     * @param attendanceRepository the attendance repository
     * @param meetingRepository the meeting repository
     * @param subsystemRepository the subsystem repository
     * @param metricsService the metrics calculation service
     * @param ganttDataService the Gantt data service
     * @param visualizationService the visualization service
     */
    public TestableReportGenerationServiceAsyncImpl(
            ProjectRepository projectRepository,
            TaskRepository taskRepository,
            TeamMemberRepository teamMemberRepository,
            MilestoneRepository milestoneRepository,
            AttendanceRepository attendanceRepository,
            MeetingRepository meetingRepository,
            SubsystemRepository subsystemRepository,
            MetricsCalculationService metricsService,
            GanttDataService ganttDataService,
            VisualizationService visualizationService) {
        super(projectRepository, taskRepository, teamMemberRepository, milestoneRepository,
              attendanceRepository, meetingRepository, subsystemRepository, metricsService,
              ganttDataService, visualizationService);
    }
    
    // ASYNC METHODS - These can be stubbed directly in tests without spies
    
    /**
     * Generates a project summary report asynchronously.
     * 
     * @param projectId the project ID
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the project summary report
     */
    public CompletableFuture<Map<String, Object>> generateProjectSummaryReportAsync(Long projectId, Consumer<Map<String, Object>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        
        try {
            Map<String, Object> result = generateProjectSummaryReport(projectId);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * Generates a team performance report asynchronously.
     * 
     * @param projectId the project ID
     * @param startDate the start date
     * @param endDate the end date
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the team performance report
     */
    public CompletableFuture<Map<String, Object>> generateTeamPerformanceReportAsync(Long projectId, LocalDate startDate, LocalDate endDate, Consumer<Map<String, Object>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        
        try {
            Map<String, Object> result = generateTeamPerformanceReport(projectId, startDate, endDate);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * Generates a milestone status report asynchronously.
     * 
     * @param projectId the project ID
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the milestone status report
     */
    public CompletableFuture<Map<String, Object>> generateMilestoneStatusReportAsync(Long projectId, Consumer<Map<String, Object>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        
        try {
            Map<String, Object> result = generateMilestoneStatusReport(projectId);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * Generates an attendance report asynchronously.
     * 
     * @param projectId the project ID
     * @param startDate the start date
     * @param endDate the end date
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the attendance report
     */
    public CompletableFuture<Map<String, Object>> generateAttendanceReportAsync(Long projectId, LocalDate startDate, LocalDate endDate, Consumer<Map<String, Object>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        
        try {
            Map<String, Object> result = generateAttendanceReport(projectId, startDate, endDate);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * Generates a subsystem progress report asynchronously.
     * 
     * @param projectId the project ID
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the subsystem progress report
     */
    public CompletableFuture<Map<String, Object>> generateSubsystemProgressReportAsync(Long projectId, Consumer<Map<String, Object>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        
        try {
            Map<String, Object> result = generateSubsystemProgressReport(projectId);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * Generates an individual team member report asynchronously.
     * 
     * @param teamMemberId the team member ID
     * @param startDate the start date
     * @param endDate the end date
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the team member report
     */
    public CompletableFuture<Map<String, Object>> generateTeamMemberReportAsync(Long teamMemberId, LocalDate startDate, LocalDate endDate, Consumer<Map<String, Object>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        
        try {
            Map<String, Object> result = generateTeamMemberReport(teamMemberId, startDate, endDate);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * Generates a project timeline report asynchronously.
     * 
     * @param projectId the project ID
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the project timeline report
     */
    public CompletableFuture<Map<String, Object>> generateProjectTimelineReportAsync(Long projectId, Consumer<Map<String, Object>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        
        try {
            Map<String, Object> result = generateProjectTimelineReport(projectId);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * Exports a report to PDF format asynchronously.
     * 
     * @param reportData the report data
     * @param reportType the report type
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the PDF data
     */
    public CompletableFuture<byte[]> exportReportToPdfAsync(Map<String, Object> reportData, String reportType, Consumer<byte[]> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<byte[]> future = new CompletableFuture<>();
        
        try {
            byte[] result = exportReportToPdf(reportData, reportType);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * Exports a report to CSV format asynchronously.
     * 
     * @param reportData the report data
     * @param reportType the report type
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the CSV data
     */
    public CompletableFuture<String> exportReportToCsvAsync(Map<String, Object> reportData, String reportType, Consumer<String> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<String> future = new CompletableFuture<>();
        
        try {
            String result = exportReportToCsv(reportData, reportType);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * Generates a custom report asynchronously.
     * 
     * @param projectId the project ID
     * @param metrics the list of metrics
     * @param startDate the start date
     * @param endDate the end date
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the custom report
     */
    public CompletableFuture<Map<String, Object>> generateCustomReportAsync(Long projectId, List<String> metrics, LocalDate startDate, LocalDate endDate, Consumer<Map<String, Object>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        
        try {
            Map<String, Object> result = generateCustomReport(projectId, metrics, startDate, endDate);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        
        return future;
    }
}