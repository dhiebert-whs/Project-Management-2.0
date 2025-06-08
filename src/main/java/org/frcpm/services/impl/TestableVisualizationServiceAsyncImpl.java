// src/main/java/org/frcpm/services/impl/TestableVisualizationServiceAsyncImpl.java

package org.frcpm.services.impl;

import org.frcpm.repositories.specific.MilestoneRepository;
import org.frcpm.repositories.specific.ProjectRepository;
import org.frcpm.repositories.specific.SubsystemRepository;
import org.frcpm.repositories.specific.TaskRepository;
import org.frcpm.services.GanttDataService;

import org.frcpm.charts.TaskChartItem;
import org.frcpm.models.GanttChartData;

import javafx.scene.layout.Pane;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * A testable async implementation of VisualizationService that accepts injected repositories
 * for better unit testing. This implementation provides async methods that can be stubbed
 * in tests without requiring spy patterns.
 */
public class TestableVisualizationServiceAsyncImpl extends TestableVisualizationServiceImpl {
    
    private static final Logger LOGGER = Logger.getLogger(TestableVisualizationServiceAsyncImpl.class.getName());
    
    /**
     * Constructor for MVVMFx injection.
     * Uses ServiceLocator for default initialization.
     */
    public TestableVisualizationServiceAsyncImpl() {
        super();
    }
    
    /**
     * Constructor for manual/test injection.
     * 
     * @param projectRepository the project repository
     * @param taskRepository the task repository
     * @param milestoneRepository the milestone repository
     * @param subsystemRepository the subsystem repository
     * @param ganttDataService the Gantt data service
     */
    public TestableVisualizationServiceAsyncImpl(
            ProjectRepository projectRepository,
            TaskRepository taskRepository,
            MilestoneRepository milestoneRepository,
            SubsystemRepository subsystemRepository,
            GanttDataService ganttDataService) {
        super(projectRepository, taskRepository, milestoneRepository, subsystemRepository, ganttDataService);
    }
    
    // ASYNC METHODS - These can be stubbed directly in tests without spies
    
    /**
     * Creates a Gantt chart pane asynchronously.
     * 
     * @param projectId the project ID
     * @param startDate the start date
     * @param endDate the end date
     * @param viewMode the view mode
     * @param showMilestones whether to show milestones
     * @param showDependencies whether to show dependencies
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the Gantt chart pane
     */
    public CompletableFuture<Pane> createGanttChartPaneAsync(Long projectId, LocalDate startDate, LocalDate endDate, String viewMode, boolean showMilestones, boolean showDependencies, Consumer<Pane> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Pane> future = new CompletableFuture<>();
        
        try {
            Pane result = createGanttChartPane(projectId, startDate, endDate, viewMode, showMilestones, showDependencies);
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
     * Creates a daily chart pane asynchronously.
     * 
     * @param projectId the project ID
     * @param date the date
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the daily chart pane
     */
    public CompletableFuture<Pane> createDailyChartPaneAsync(Long projectId, LocalDate date, Consumer<Pane> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Pane> future = new CompletableFuture<>();
        
        try {
            Pane result = createDailyChartPane(projectId, date);
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
     * Gets project completion data asynchronously.
     * 
     * @param projectId the project ID
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the project completion data
     */
    public CompletableFuture<Map<String, Object>> getProjectCompletionDataAsync(Long projectId, Consumer<Map<String, Object>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        
        try {
            Map<String, Object> result = getProjectCompletionData(projectId);
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
     * Gets task status summary asynchronously.
     * 
     * @param projectId the project ID
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the task status summary
     */
    public CompletableFuture<Map<String, Integer>> getTaskStatusSummaryAsync(Long projectId, Consumer<Map<String, Integer>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Map<String, Integer>> future = new CompletableFuture<>();
        
        try {
            Map<String, Integer> result = getTaskStatusSummary(projectId);
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
     * Converts GanttChartData to TaskChartItems asynchronously.
     * 
     * @param chartDataList the chart data list
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the converted task chart items
     */
    public CompletableFuture<List<TaskChartItem>> convertGanttChartDataToTaskChartItemsAsync(List<GanttChartData> chartDataList, Consumer<List<TaskChartItem>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<List<TaskChartItem>> future = new CompletableFuture<>();
        
        try {
            List<TaskChartItem> result = convertGanttChartDataToTaskChartItems(chartDataList);
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
     * Gets upcoming deadlines asynchronously.
     * 
     * @param projectId the project ID
     * @param daysAhead the number of days ahead to look
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the upcoming deadlines
     */
    public CompletableFuture<List<Map<String, Object>>> getUpcomingDeadlinesAsync(Long projectId, int daysAhead, Consumer<List<Map<String, Object>>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<List<Map<String, Object>>> future = new CompletableFuture<>();
        
        try {
            List<Map<String, Object>> result = getUpcomingDeadlines(projectId, daysAhead);
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
     * Gets subsystem progress asynchronously.
     * 
     * @param projectId the project ID
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the subsystem progress
     */
    public CompletableFuture<Map<String, Double>> getSubsystemProgressAsync(Long projectId, Consumer<Map<String, Double>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Map<String, Double>> future = new CompletableFuture<>();
        
        try {
            Map<String, Double> result = getSubsystemProgress(projectId);
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
     * Gets at-risk tasks asynchronously.
     * 
     * @param projectId the project ID
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the at-risk tasks
     */
    public CompletableFuture<List<Map<String, Object>>> getAtRiskTasksAsync(Long projectId, Consumer<List<Map<String, Object>>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<List<Map<String, Object>>> future = new CompletableFuture<>();
        
        try {
            List<Map<String, Object>> result = getAtRiskTasks(projectId);
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
     * Generates SVG export asynchronously.
     * 
     * @param chartData the chart data
     * @param chartType the chart type
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the SVG export data
     */
    public CompletableFuture<String> generateSvgExportAsync(Map<String, Object> chartData, String chartType, Consumer<String> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<String> future = new CompletableFuture<>();
        
        try {
            String result = generateSvgExport(chartData, chartType);
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
     * Generates PDF report asynchronously.
     * 
     * @param projectId the project ID
     * @param reportType the report type
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the PDF report data
     */
    public CompletableFuture<byte[]> generatePdfReportAsync(Long projectId, String reportType, Consumer<byte[]> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<byte[]> future = new CompletableFuture<>();
        
        try {
            byte[] result = generatePdfReport(projectId, reportType);
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