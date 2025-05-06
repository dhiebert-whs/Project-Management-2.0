package org.frcpm.services;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.frcpm.charts.TaskChartItem;
import org.frcpm.models.GanttChartData;

import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

/**
 * Service interface for visualization-related operations.
 * This service provides methods to generate visualization data for
 * various charts and dashboard components.
 */
public interface VisualizationService {

    /**
     * Get project completion percentage data.
     * 
     * @param projectId The ID of the project
     * @return Map containing completion data by category
     */
    Map<String, Object> getProjectCompletionData(Long projectId);
    
    /**
     * Get task status summary data.
     * 
     * @param projectId The ID of the project
     * @return Map containing task counts by status
     */
    Map<String, Integer> getTaskStatusSummary(Long projectId);
    
    /**
     * Get upcoming deadlines data.
     * 
     * @param projectId The ID of the project
     * @param daysAhead Number of days to look ahead
     * @return List of upcoming deadline data
     */
    List<Map<String, Object>> getUpcomingDeadlines(Long projectId, int daysAhead);
    
    /**
     * Get subsystem progress data.
     * 
     * @param projectId The ID of the project
     * @return Map of subsystem names to completion percentages
     */
    Map<String, Double> getSubsystemProgress(Long projectId);
    
    /**
     * Get at-risk tasks data.
     * 
     * @param projectId The ID of the project
     * @return List of at-risk task data
     */
    List<Map<String, Object>> getAtRiskTasks(Long projectId);
    
    /**
     * Generate SVG export data for visualization.
     * 
     * @param chartData The chart data to export
     * @param chartType The type of chart being exported
     * @return String containing SVG data
     */
    String generateSvgExport(Map<String, Object> chartData, String chartType);
    
    /**
     * Generate PDF report for visualization.
     * 
     * @param projectId The ID of the project
     * @param reportType The type of report to generate
     * @return Byte array containing PDF data
     */
    byte[] generatePdfReport(Long projectId, String reportType);

    /**
     * Creates a chart for visualizing task and milestone data on a specific date.
     *
     * @param tasks the list of tasks active on the specified date
     * @param milestones the list of milestones on the specified date
     * @param date the date to visualize
     * @return a pane containing the chart
     */
    public static Pane createDailyChart(List<TaskChartItem> tasks, List<TaskChartItem> milestones, LocalDate date) {
        VBox dailyChart = new VBox(10);
        dailyChart.setPadding(new Insets(20));
        dailyChart.getStyleClass().add("daily-chart");
        
        // Add date header
        Label dateHeader = new Label(date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        dateHeader.getStyleClass().add("date-header");
        dateHeader.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        dailyChart.getChildren().add(dateHeader);
        
        // Add tasks section
        if (tasks != null && !tasks.isEmpty()) {
            Label tasksHeader = new Label("Tasks");
            tasksHeader.getStyleClass().add("section-header");
            tasksHeader.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            dailyChart.getChildren().add(tasksHeader);
            
            for (TaskChartItem task : tasks) {
                HBox taskItem = createDailyTaskItem(task);
                dailyChart.getChildren().add(taskItem);
            }
        }
        
        // Add milestones section
        if (milestones != null && !milestones.isEmpty()) {
            Label milestonesHeader = new Label("Milestones");
            milestonesHeader.getStyleClass().add("section-header");
            milestonesHeader.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            dailyChart.getChildren().add(milestonesHeader);
            
            for (TaskChartItem milestone : milestones) {
                HBox milestoneItem = createDailyMilestoneItem(milestone);
                dailyChart.getChildren().add(milestoneItem);
            }
        }
        
        // Add empty message if no tasks or milestones
        if ((tasks == null || tasks.isEmpty()) && (milestones == null || milestones.isEmpty())) {
            Label emptyMessage = new Label("No tasks or milestones scheduled for this date.");
            emptyMessage.setStyle("-fx-font-style: italic;");
            dailyChart.getChildren().add(emptyMessage);
        }
        
        return dailyChart;
    }

    /**
     * Creates an item for displaying a task in a daily view.
     *
     * @param task the task data
     * @return an HBox containing the task information
     */
    private static HBox createDailyTaskItem(TaskChartItem task) {
        HBox taskItem = new HBox(10);
        taskItem.setPadding(new Insets(5));
        taskItem.getStyleClass().add("daily-task-item");
        
        // Add color indicator
        Rectangle colorIndicator = new Rectangle(10, 20);
        if (task.getColor() != null && !task.getColor().isEmpty()) {
            colorIndicator.setFill(Color.web(task.getColor()));
        } else {
            colorIndicator.setFill(Color.BLUE);
        }
        
        // Add task title
        Label titleLabel = new Label(task.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold;");
        
        // Add progress indicator
        Label progressLabel = new Label(task.getProgress() + "%");
        
        // Add subsystem if available
        Label subsystemLabel = null;
        if (task.getSubsystem() != null && !task.getSubsystem().isEmpty()) {
            subsystemLabel = new Label(task.getSubsystem());
            subsystemLabel.setStyle("-fx-font-style: italic;");
        }
        
        // Add assignee if available
        Label assigneeLabel = null;
        if (task.getAssignee() != null && !task.getAssignee().isEmpty()) {
            assigneeLabel = new Label(task.getAssignee());
        }
        
        // Add components to task item
        taskItem.getChildren().add(colorIndicator);
        taskItem.getChildren().add(titleLabel);
        taskItem.getChildren().add(progressLabel);
        
        if (subsystemLabel != null) {
            taskItem.getChildren().add(subsystemLabel);
        }
        
        if (assigneeLabel != null) {
            taskItem.getChildren().add(assigneeLabel);
        }
        
        return taskItem;
    }

    /**
     * Creates an item for displaying a milestone in a daily view.
     *
     * @param milestone the milestone data
     * @return an HBox containing the milestone information
     */
    private static HBox createDailyMilestoneItem(TaskChartItem milestone) {
        HBox milestoneItem = new HBox(10);
        milestoneItem.setPadding(new Insets(5));
        milestoneItem.getStyleClass().add("daily-milestone-item");
        
        // Add color indicator
        Polygon diamond = new Polygon();
        diamond.getPoints().addAll(5.0, 0.0, 10.0, 5.0, 5.0, 10.0, 0.0, 5.0);
        
        if (milestone.getColor() != null && !milestone.getColor().isEmpty()) {
            diamond.setFill(Color.web(milestone.getColor()));
        } else {
            diamond.setFill(Color.PURPLE);
        }
        
        // Add milestone title
        Label titleLabel = new Label(milestone.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold;");
        
        // Add status
        Label statusLabel = new Label(milestone.getStatus());
        
        // Add components to milestone item
        milestoneItem.getChildren().add(diamond);
        milestoneItem.getChildren().add(titleLabel);
        milestoneItem.getChildren().add(statusLabel);
        
        return milestoneItem;
    }

    /**
     * Helper method to convert tasks from the GanttChartData format to TaskChartItem format.
     *
     * @param chartDataList the list of GanttChartData objects
     * @return a list of TaskChartItem objects
     */
    public static List<TaskChartItem> convertGanttChartDataToTaskChartItems(List<GanttChartData> chartDataList) {
        if (chartDataList == null) {
            return new ArrayList<>();
        }
        
        return chartDataList.stream()
            .map(TaskChartItem::fromGanttChartData)
            .collect(Collectors.toList());
    }
}