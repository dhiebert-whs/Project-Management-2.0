// src/main/java/org/frcpm/services/impl/TestableMetricsCalculationServiceAsyncImpl.java

package org.frcpm.services.impl;

import org.frcpm.repositories.specific.*;
import org.frcpm.services.MetricsCalculationService;
import org.frcpm.di.ServiceLocator;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A testable async implementation of MetricsCalculationService that accepts injected repositories
 * for better unit testing. This implementation provides async methods that can be stubbed
 * in tests without requiring spy patterns.
 */
public class TestableMetricsCalculationServiceAsyncImpl extends TestableMetricsCalculationServiceImpl {
    
    private static final Logger LOGGER = Logger.getLogger(TestableMetricsCalculationServiceAsyncImpl.class.getName());
    
    /**
     * Constructor for MVVMFx injection.
     * Uses ServiceLocator for default initialization.
     */
    public TestableMetricsCalculationServiceAsyncImpl() {
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
     */
    public TestableMetricsCalculationServiceAsyncImpl(
            ProjectRepository projectRepository,
            TaskRepository taskRepository,
            TeamMemberRepository teamMemberRepository,
            MilestoneRepository milestoneRepository,
            AttendanceRepository attendanceRepository,
            MeetingRepository meetingRepository,
            SubsystemRepository subsystemRepository) {
        super(projectRepository, taskRepository, teamMemberRepository, milestoneRepository,
              attendanceRepository, meetingRepository, subsystemRepository);
    }
    
    // ASYNC METHODS - These can be stubbed directly in tests without spies
    
    /**
     * Calculates project progress metrics asynchronously.
     * 
     * @param projectId the project ID
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the project progress metrics
     */
    public CompletableFuture<Map<String, Object>> calculateProjectProgressMetricsAsync(Long projectId, Consumer<Map<String, Object>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        
        try {
            Map<String, Object> result = calculateProjectProgressMetrics(projectId);
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
     * Calculates team performance metrics asynchronously.
     * 
     * @param projectId the project ID
     * @param startDate the start date
     * @param endDate the end date
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the team performance metrics
     */
    public CompletableFuture<Map<String, Object>> calculateTeamPerformanceMetricsAsync(Long projectId, LocalDate startDate, LocalDate endDate, Consumer<Map<String, Object>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        
        try {
            Map<String, Object> result = calculateTeamPerformanceMetrics(projectId, startDate, endDate);
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
     * Calculates task completion metrics asynchronously.
     * 
     * @param projectId the project ID
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the task completion metrics
     */
    public CompletableFuture<Map<String, Object>> calculateTaskCompletionMetricsAsync(Long projectId, Consumer<Map<String, Object>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        
        try {
            Map<String, Object> result = calculateTaskCompletionMetrics(projectId);
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
     * Calculates attendance metrics asynchronously.
     * 
     * @param projectId the project ID
     * @param startDate the start date
     * @param endDate the end date
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the attendance metrics
     */
    public CompletableFuture<Map<String, Object>> calculateAttendanceMetricsAsync(Long projectId, LocalDate startDate, LocalDate endDate, Consumer<Map<String, Object>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        
        try {
            Map<String, Object> result = calculateAttendanceMetrics(projectId, startDate, endDate);
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
     * Calculates timeline deviation metrics asynchronously.
     * 
     * @param projectId the project ID
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the timeline deviation metrics
     */
    public CompletableFuture<Map<String, Object>> calculateTimelineDeviationMetricsAsync(Long projectId, Consumer<Map<String, Object>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        
        try {
            Map<String, Object> result = calculateTimelineDeviationMetrics(projectId);
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
     * Calculates individual performance metrics asynchronously.
     * 
     * @param teamMemberId the team member ID
     * @param startDate the start date
     * @param endDate the end date
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the individual performance metrics
     */
    public CompletableFuture<Map<String, Object>> calculateIndividualPerformanceMetricsAsync(Long teamMemberId, LocalDate startDate, LocalDate endDate, Consumer<Map<String, Object>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        
        try {
            Map<String, Object> result = calculateIndividualPerformanceMetrics(teamMemberId, startDate, endDate);
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
     * Calculates subsystem performance metrics asynchronously.
     * 
     * @param projectId the project ID
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the subsystem performance metrics
     */
    public CompletableFuture<Map<String, Object>> calculateSubsystemPerformanceMetricsAsync(Long projectId, Consumer<Map<String, Object>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        
        try {
            Map<String, Object> result = calculateSubsystemPerformanceMetrics(projectId);
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
     * Generates a project health dashboard asynchronously.
     * 
     * @param projectId the project ID
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the project health dashboard
     */
    public CompletableFuture<Map<String, Object>> generateProjectHealthDashboardAsync(Long projectId, Consumer<Map<String, Object>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        
        try {
            Map<String, Object> result = generateProjectHealthDashboard(projectId);
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