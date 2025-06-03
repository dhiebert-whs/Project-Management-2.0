// src/main/java/org/frcpm/services/impl/TestableGanttDataServiceAsyncImpl.java

package org.frcpm.services.impl;

import org.frcpm.repositories.specific.MilestoneRepository;
import org.frcpm.repositories.specific.ProjectRepository;
import org.frcpm.repositories.specific.TaskRepository;
import org.frcpm.services.GanttChartTransformationService;
import org.frcpm.services.GanttDataService;
import org.frcpm.di.ServiceLocator;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A testable async implementation of GanttDataService that accepts injected repositories
 * for better unit testing. This implementation provides async methods that can be stubbed
 * in tests without requiring spy patterns.
 */
public class TestableGanttDataServiceAsyncImpl extends TestableGanttDataServiceImpl {
    
    private static final Logger LOGGER = Logger.getLogger(TestableGanttDataServiceAsyncImpl.class.getName());
    
    /**
     * Constructor for MVVMFx injection.
     * Uses ServiceLocator for default initialization.
     */
    public TestableGanttDataServiceAsyncImpl() {
        super();
    }
    
    /**
     * Constructor for manual/test injection.
     * 
     * @param projectRepository the project repository
     * @param taskRepository the task repository
     * @param milestoneRepository the milestone repository
     * @param transformationService the transformation service
     */
    public TestableGanttDataServiceAsyncImpl(
            ProjectRepository projectRepository,
            TaskRepository taskRepository,
            MilestoneRepository milestoneRepository,
            GanttChartTransformationService transformationService) {
        super(projectRepository, taskRepository, milestoneRepository, transformationService);
    }
    
    // ASYNC METHODS - These can be stubbed directly in tests without spies
    
    /**
     * Formats tasks for Gantt chart asynchronously.
     * 
     * @param projectId the project ID
     * @param startDate the start date
     * @param endDate the end date
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the formatted Gantt data
     */
    public CompletableFuture<Map<String, Object>> formatTasksForGanttAsync(Long projectId, LocalDate startDate, LocalDate endDate, Consumer<Map<String, Object>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        
        try {
            Map<String, Object> result = formatTasksForGantt(projectId, startDate, endDate);
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
     * Applies filters to Gantt data asynchronously.
     * 
     * @param ganttData the Gantt data to filter
     * @param filterCriteria the filter criteria
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the filtered Gantt data
     */
    public CompletableFuture<Map<String, Object>> applyFiltersToGanttDataAsync(Map<String, Object> ganttData, Map<String, Object> filterCriteria, Consumer<Map<String, Object>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        
        try {
            Map<String, Object> result = applyFiltersToGanttData(ganttData, filterCriteria);
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
     * Calculates critical path asynchronously.
     * 
     * @param projectId the project ID
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the critical path task IDs
     */
    public CompletableFuture<List<Long>> calculateCriticalPathAsync(Long projectId, Consumer<List<Long>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<List<Long>> future = new CompletableFuture<>();
        
        try {
            List<Long> result = calculateCriticalPath(projectId);
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
     * Gets Gantt data for a specific date asynchronously.
     * 
     * @param projectId the project ID
     * @param date the date
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the Gantt data for the date
     */
    public CompletableFuture<Map<String, Object>> getGanttDataForDateAsync(Long projectId, LocalDate date, Consumer<Map<String, Object>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        
        try {
            Map<String, Object> result = getGanttDataForDate(projectId, date);
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
     * Gets task dependencies asynchronously.
     * 
     * @param projectId the project ID
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the task dependencies map
     */
    public CompletableFuture<Map<Long, List<Long>>> getTaskDependenciesAsync(Long projectId, Consumer<Map<Long, List<Long>>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Map<Long, List<Long>>> future = new CompletableFuture<>();
        
        try {
            Map<Long, List<Long>> result = getTaskDependencies(projectId);
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
     * Identifies bottlenecks asynchronously.
     * 
     * @param projectId the project ID
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the bottleneck task IDs
     */
    public CompletableFuture<List<Long>> identifyBottlenecksAsync(Long projectId, Consumer<List<Long>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<List<Long>> future = new CompletableFuture<>();
        
        try {
            List<Long> result = identifyBottlenecks(projectId);
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
     * Gets the transformation service asynchronously.
     * 
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the transformation service
     */
    public CompletableFuture<GanttChartTransformationService> getTransformationServiceAsync(Consumer<GanttChartTransformationService> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<GanttChartTransformationService> future = new CompletableFuture<>();
        
        try {
            GanttChartTransformationService result = getTransformationService();
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