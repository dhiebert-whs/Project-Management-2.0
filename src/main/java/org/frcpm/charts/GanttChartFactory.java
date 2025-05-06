// src/main/java/org/frcpm/charts/GanttChartFactory.java
package org.frcpm.charts;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.Chart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.StringConverter;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.frcpm.models.GanttChartData;

/**
 * Factory for creating Gantt chart visualizations with native JavaFX components.
 * This class creates horizontal bar charts for tasks and specialized markers for milestones.
 */
public class GanttChartFactory {

    // Constants
    private static final int DEFAULT_BAR_HEIGHT = 25;
    private static final int MILESTONE_MARKER_SIZE = 15;
    private static final int TASK_SPACING = 10;
    private static final int VERTICAL_PADDING = 20;
    
    // Date formatters for different view modes
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("MM/dd");
    private static final DateTimeFormatter WEEK_FORMATTER = DateTimeFormatter.ofPattern("MM/dd");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMM yyyy");

    /**
     * Creates a Gantt chart with the specified tasks and milestones.
     *
     * @param tasks the list of tasks to display
     * @param milestones the list of milestones to display
     * @param startDate the start date of the chart
     * @param endDate the end date of the chart
     * @param viewMode the view mode (DAY, WEEK, MONTH)
     * @param showDependencies whether to show dependencies
     * @return a pane containing the Gantt chart
     */
    public static Pane createGanttChart(
            List<TaskChartItem> tasks,
            List<TaskChartItem> milestones,
            LocalDate startDate,
            LocalDate endDate,
            String viewMode,
            boolean showDependencies) {
        
        if (tasks == null) {
            tasks = new ArrayList<>();
        }
        
        if (milestones == null) {
            milestones = new ArrayList<>();
        }
        
        // Ensure valid dates
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(7);
        }
        
        if (endDate == null) {
            endDate = LocalDate.now().plusDays(30);
        }
        
        // Create the main container
        BorderPane chartContainer = new BorderPane();
        chartContainer.getStyleClass().add("gantt-chart-container");
        
        // Create timeline axis
        HBox timelineAxis = createTimelineAxis(startDate, endDate, viewMode);
        chartContainer.setTop(timelineAxis);
        
        // Create task list
        VBox taskList = new VBox(TASK_SPACING);
        taskList.setPadding(new Insets(VERTICAL_PADDING, 10, VERTICAL_PADDING, 10));
        taskList.setAlignment(Pos.TOP_LEFT);
        
        // Create task chart area
        Pane chartArea = new Pane();
        chartArea.setPadding(new Insets(VERTICAL_PADDING, 10, VERTICAL_PADDING, 10));
        
        // Add today line
        Line todayLine = createTodayLine(chartArea.getHeight(), startDate, endDate);
        chartArea.getChildren().add(todayLine);
        
        // Create map to store task nodes for dependency drawing
        Map<String, Rectangle> taskNodeMap = new HashMap<>();
        
        // Calculate date range for positioning
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
        double dayWidth = timelineAxis.getWidth() / totalDays;
        
        // Add tasks
        int yPosition = VERTICAL_PADDING;
        for (TaskChartItem task : tasks) {
            // Create task label
            Label taskLabel = new Label(task.getTitle());
            taskLabel.setPrefHeight(DEFAULT_BAR_HEIGHT);
            taskLabel.setAlignment(Pos.CENTER_LEFT);
            taskList.getChildren().add(taskLabel);
            
            // Create task bar
            Rectangle taskBar = createTaskBar(task, startDate, endDate, dayWidth, yPosition);
            chartArea.getChildren().add(taskBar);
            
            // Store task node for dependency drawing
            taskNodeMap.put(task.getId(), taskBar);
            
            // Add progress indicator
            if (task.getProgress() > 0) {
                Rectangle progressBar = createProgressBar(task, taskBar);
                chartArea.getChildren().add(progressBar);
            }
            
            // Style the task bar
            ChartStyler.styleTaskBar(taskBar, task);
            
            yPosition += DEFAULT_BAR_HEIGHT + TASK_SPACING;
        }
        
        // Add milestones
        for (TaskChartItem milestone : milestones) {
            // Create milestone label
            Label milestoneLabel = new Label(milestone.getTitle());
            milestoneLabel.setPrefHeight(DEFAULT_BAR_HEIGHT);
            milestoneLabel.setAlignment(Pos.CENTER_LEFT);
            taskList.getChildren().add(milestoneLabel);
            
            // Create milestone marker
            Shape milestoneMarker = createMilestoneMarker(milestone, startDate, endDate, dayWidth, yPosition);
            chartArea.getChildren().add(milestoneMarker);
            
            // Style the milestone marker
            ChartStyler.styleMilestoneMarker(milestoneMarker, milestone);
            
            yPosition += DEFAULT_BAR_HEIGHT + TASK_SPACING;
        }
        
        // Draw dependencies if requested
        if (showDependencies) {
            drawDependencies(chartArea, tasks, taskNodeMap);
        }
        
        // Add components to container
        BorderPane contentPane = new BorderPane();
        contentPane.setLeft(taskList);
        contentPane.setCenter(chartArea);
        chartContainer.setCenter(contentPane);
        
        return chartContainer;
    }
    
    /**
     * Creates a timeline axis for the Gantt chart.
     *
     * @param startDate the start date of the chart
     * @param endDate the end date of the chart
     * @param viewMode the view mode (DAY, WEEK, MONTH)
     * @return an HBox containing the timeline axis
     */
    private static HBox createTimelineAxis(LocalDate startDate, LocalDate endDate, String viewMode) {
        HBox timelineAxis = new HBox();
        timelineAxis.getStyleClass().add("timeline-axis");
        timelineAxis.setPadding(new Insets(5, 0, 5, 0));
        
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
        DateTimeFormatter formatter;
        
        // Select formatter based on view mode
        switch (viewMode.toUpperCase()) {
            case "DAY":
                formatter = DAY_FORMATTER;
                break;
            case "MONTH":
                formatter = MONTH_FORMATTER;
                break;
            case "WEEK":
            default:
                formatter = WEEK_FORMATTER;
                break;
        }
        
        // Create timeline labels
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            Label dateLabel = new Label(formatter.format(currentDate));
            dateLabel.setPrefWidth(30);
            dateLabel.setAlignment(Pos.CENTER);
            
            // Highlight weekends
            if (currentDate.getDayOfWeek() == DayOfWeek.SATURDAY || 
                currentDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
                dateLabel.getStyleClass().add("weekend-label");
            }
            
            timelineAxis.getChildren().add(dateLabel);
            
            // Increment date based on view mode
            switch (viewMode.toUpperCase()) {
                case "DAY":
                    currentDate = currentDate.plusDays(1);
                    break;
                case "MONTH":
                    currentDate = currentDate.plusMonths(1);
                    break;
                case "WEEK":
                default:
                    currentDate = currentDate.plusWeeks(1);
                    break;
            }
        }
        
        return timelineAxis;
    }
    
    /**
     * Creates a vertical line indicating the current date.
     *
     * @param height the height of the chart area
     * @param startDate the start date of the chart
     * @param endDate the end date of the chart
     * @return a Line representing today's date
     */
    private static Line createTodayLine(double height, LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();
        
        // Calculate position
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
        long daysFromStart = ChronoUnit.DAYS.between(startDate, today);
        double position = daysFromStart * 100.0 / totalDays;
        
        // Create line
        Line todayLine = new Line(position, 0, position, height);
        todayLine.getStyleClass().add("today-line");
        todayLine.setStroke(Color.RED);
        todayLine.setStrokeWidth(2);
        todayLine.getStrokeDashArray().addAll(5.0, 5.0);
        
        return todayLine;
    }
    
    /**
     * Creates a rectangle representing a task bar.
     *
     * @param task the task data
     * @param startDate the start date of the chart
     * @param endDate the end date of the chart
     * @param dayWidth the width of one day in pixels
     * @param yPosition the vertical position of the task bar
     * @return a Rectangle representing the task
     */
    private static Rectangle createTaskBar(TaskChartItem task, LocalDate startDate, LocalDate endDate, 
                                        double dayWidth, int yPosition) {
        // Calculate task position
        LocalDate taskStart = task.getStartDate();
        LocalDate taskEnd = task.getEndDate();
        
        // Clip to chart range if necessary
        if (taskStart.isBefore(startDate)) {
            taskStart = startDate;
        }
        if (taskEnd.isAfter(endDate)) {
            taskEnd = endDate;
        }
        
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
        long taskStartDays = ChronoUnit.DAYS.between(startDate, taskStart);
        long taskDuration = ChronoUnit.DAYS.between(taskStart, taskEnd) + 1; // Include both start and end days
        
        double xPosition = taskStartDays * dayWidth;
        double width = taskDuration * dayWidth;
        
        // Create rectangle
        Rectangle taskBar = new Rectangle(xPosition, yPosition, width, DEFAULT_BAR_HEIGHT);
        
        // Set color from task
        if (task.getColor() != null && !task.getColor().isEmpty()) {
            taskBar.setFill(Color.web(task.getColor()));
            taskBar.setStroke(Color.web(task.getColor()).darker());
        } else {
            taskBar.setFill(Color.BLUE);
            taskBar.setStroke(Color.DARKBLUE);
        }
        
        taskBar.setStrokeWidth(1);
        taskBar.setArcWidth(5);
        taskBar.setArcHeight(5);
        
        return taskBar;
    }
    
    /**
     * Creates a rectangle representing the progress of a task.
     *
     * @param task the task data
     * @param taskBar the rectangle representing the full task
     * @return a Rectangle representing the task progress
     */
    private static Rectangle createProgressBar(TaskChartItem task, Rectangle taskBar) {
        double progressWidth = taskBar.getWidth() * task.getProgress() / 100.0;
        
        Rectangle progressBar = new Rectangle(
            taskBar.getX(), 
            taskBar.getY(), 
            progressWidth, 
            taskBar.getHeight()
        );
        
        // Set darker color for progress
        Color taskColor;
        if (task.getColor() != null && !task.getColor().isEmpty()) {
            taskColor = Color.web(task.getColor());
        } else {
            taskColor = Color.BLUE;
        }
        
        progressBar.setFill(taskColor.darker());
        progressBar.setArcWidth(5);
        progressBar.setArcHeight(5);
        
        return progressBar;
    }
    
    /**
     * Creates a shape representing a milestone marker.
     *
     * @param milestone the milestone data
     * @param startDate the start date of the chart
     * @param endDate the end date of the chart
     * @param dayWidth the width of one day in pixels
     * @param yPosition the vertical position of the milestone marker
     * @return a Shape representing the milestone
     */
    private static Shape createMilestoneMarker(TaskChartItem milestone, LocalDate startDate, LocalDate endDate,
                                            double dayWidth, int yPosition) {
        // Calculate milestone position
        LocalDate milestoneDate = milestone.getStartDate();
        
        // Clip to chart range if necessary
        if (milestoneDate.isBefore(startDate)) {
            milestoneDate = startDate;
        }
        if (milestoneDate.isAfter(endDate)) {
            milestoneDate = endDate;
        }
        
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
        long milestoneDays = ChronoUnit.DAYS.between(startDate, milestoneDate);
        
        double xPosition = milestoneDays * dayWidth;
        double centerY = yPosition + DEFAULT_BAR_HEIGHT / 2.0;
        
        // Create diamond shape
        Polygon diamond = new Polygon();
        diamond.getPoints().addAll(
            xPosition, centerY - MILESTONE_MARKER_SIZE / 2.0,
            xPosition + MILESTONE_MARKER_SIZE / 2.0, centerY,
            xPosition, centerY + MILESTONE_MARKER_SIZE / 2.0,
            xPosition - MILESTONE_MARKER_SIZE / 2.0, centerY
        );
        
        // Set color from milestone
        if (milestone.getColor() != null && !milestone.getColor().isEmpty()) {
            diamond.setFill(Color.web(milestone.getColor()));
            diamond.setStroke(Color.web(milestone.getColor()).darker());
        } else {
            diamond.setFill(Color.PURPLE);
            diamond.setStroke(Color.DARKVIOLET);  
        }
        
        diamond.setStrokeWidth(1);
        
        return diamond;
    }
    
    /**
     * Draws dependency lines between tasks.
     *
     * @param chartArea the pane containing the chart
     * @param tasks the list of tasks
     * @param taskNodeMap a map of task IDs to task node rectangles
     */
    private static void drawDependencies(Pane chartArea, List<TaskChartItem> tasks, Map<String, Rectangle> taskNodeMap) {
        for (TaskChartItem task : tasks) {
            Rectangle taskRect = taskNodeMap.get(task.getId());
            if (taskRect == null) {
                continue;
            }
            
            for (String dependencyId : task.getDependencies()) {
                Rectangle dependencyRect = taskNodeMap.get(dependencyId);
                if (dependencyRect == null) {
                    continue;
                }
                
                // Draw line from dependency end to task start
                double startX = dependencyRect.getX() + dependencyRect.getWidth();
                double startY = dependencyRect.getY() + dependencyRect.getHeight() / 2;
                double endX = taskRect.getX();
                double endY = taskRect.getY() + taskRect.getHeight() / 2;
                
                // Create dependency line
                Line line = new Line(startX, startY, endX, endY);
                
                // Add arrow at the end
                double arrowSize = 6;
                double angle = Math.atan2(endY - startY, endX - startX);
                
                Polygon arrow = new Polygon();
                arrow.getPoints().addAll(
                    endX, endY,
                    endX - arrowSize * Math.cos(angle - Math.PI / 6), endY - arrowSize * Math.sin(angle - Math.PI / 6),
                    endX - arrowSize * Math.cos(angle + Math.PI / 6), endY - arrowSize * Math.sin(angle + Math.PI / 6)
                );
                
                // Style the dependency line and arrow
                ChartStyler.styleDependencyLine(line);
                arrow.setFill(Color.GRAY);
                
                // Add to chart
                chartArea.getChildren().addAll(line, arrow);
            }
        }
    }
    
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
    
    /**
     * Filters tasks for a specific date range.
     *
     * @param tasks the list of tasks
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @return a list of tasks that overlap with the date range
     */
    public static List<TaskChartItem> filterTasksByDateRange(List<TaskChartItem> tasks, LocalDate startDate, LocalDate endDate) {
        if (tasks == null) {
            return new ArrayList<>();
        }
        
        return tasks.stream()
            .filter(task -> {
                // Task starts before range ends and ends after range starts
                return !task.getStartDate().isAfter(endDate) && !task.getEndDate().isBefore(startDate);
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Filters milestones for a specific date range.
     *
     * @param milestones the list of milestones
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @return a list of milestones that fall within the date range
     */
    public static List<TaskChartItem> filterMilestonesByDateRange(List<TaskChartItem> milestones, LocalDate startDate, LocalDate endDate) {
        if (milestones == null) {
            return new ArrayList<>();
        }
        
        return milestones.stream()
            .filter(milestone -> {
                // Milestone date is within range
                LocalDate milestoneDate = milestone.getStartDate();
                return !milestoneDate.isBefore(startDate) && !milestoneDate.isAfter(endDate);
            })
            .collect(Collectors.toList());
    }


    
}