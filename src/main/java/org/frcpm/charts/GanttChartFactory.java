// src/main/java/org/frcpm/charts/GanttChartFactory.java
package org.frcpm.charts;

import de.gsi.chart.XYChart;
import de.gsi.chart.axes.spi.CategoryAxis;
import de.gsi.chart.axes.spi.DefaultNumericAxis;
import de.gsi.chart.plugins.DataPointTooltip;
import de.gsi.chart.plugins.Zoomer;
import de.gsi.chart.renderer.ErrorStyle;
import de.gsi.chart.renderer.LineStyle;
import de.gsi.chart.renderer.spi.ErrorDataSetRenderer;
import de.gsi.dataset.spi.DefaultDataSet;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Factory for creating Chart-FX Gantt charts.
 * This class provides methods to create and customize Gantt charts
 * using the Chart-FX library.
 */
public class GanttChartFactory {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy");
    private static final double DEFAULT_TASK_HEIGHT = 25.0;
    private static final double MILESTONE_DIAMOND_SIZE = 16.0;
    private static final double DAY_WIDTH = 30.0;
    private static final double WEEK_WIDTH = 50.0;
    private static final double MONTH_WIDTH = 100.0;

    /**
     * Creates a Gantt chart with the specified tasks and milestones.
     *
     * @param tasks            the list of tasks
     * @param milestones       the list of milestones
     * @param startDate        the start date of the chart
     * @param endDate          the end date of the chart
     * @param viewMode         the view mode (DAY, WEEK, MONTH)
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

        // Validate parameters
        if (startDate == null)
            startDate = LocalDate.now().minusMonths(1);
        if (endDate == null)
            endDate = LocalDate.now().plusMonths(2);
        if (tasks == null)
            tasks = new ArrayList<>();
        if (milestones == null)
            milestones = new ArrayList<>();

        // Create main container
        BorderPane chartPane = new BorderPane();
        chartPane.getStyleClass().add("gantt-chart-container");

        // Create timeline axis based on view mode
        Node timelineAxis = createTimelineAxis(startDate, endDate, viewMode);
        chartPane.setTop(timelineAxis);

        // Create chart content with tasks and milestones
        GridPane contentPane = createChartContent(tasks, milestones, startDate, endDate, viewMode);
        chartPane.setCenter(contentPane);

        // Add dependencies if requested
        if (showDependencies) {
            addDependencyLines(contentPane, tasks, startDate, endDate, viewMode);
        }

        // Add today line
        addTodayLine(contentPane, startDate, endDate, viewMode);

        // Apply CSS styling
        applyStyles(chartPane);

        return chartPane;
    }

    /**
     * Creates a daily chart with tasks and milestones for a specific date.
     *
     * @param tasks      the list of tasks active on the specified date
     * @param milestones the list of milestones on the specified date
     * @param date       the date to visualize
     * @return a pane containing the daily chart
     */
    public static Pane createDailyChart(List<TaskChartItem> tasks, List<TaskChartItem> milestones, LocalDate date) {
        VBox dailyChart = new VBox(10);
        dailyChart.setPadding(new Insets(20));
        dailyChart.getStyleClass().add("daily-chart");

        // Create date header
        Label dateHeader = new Label(date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        dateHeader.getStyleClass().add("date-header");
        dailyChart.getChildren().add(dateHeader);

        // Filter tasks and milestones for the specific date
        List<TaskChartItem> filteredTasks = tasks.stream()
                .filter(t -> t.isOnDate(date))
                .collect(Collectors.toList());

        List<TaskChartItem> filteredMilestones = milestones.stream()
                .filter(m -> m.getStartDate() != null && m.getStartDate().equals(date))
                .collect(Collectors.toList());

        // Add tasks section
        if (!filteredTasks.isEmpty()) {
            Label tasksHeader = new Label("Tasks");
            tasksHeader.getStyleClass().add("section-header");
            dailyChart.getChildren().add(tasksHeader);

            for (TaskChartItem task : filteredTasks) {
                HBox taskItem = createDailyTaskItem(task);
                dailyChart.getChildren().add(taskItem);
            }
        }

        // Add milestones section
        if (!filteredMilestones.isEmpty()) {
            Label milestonesHeader = new Label("Milestones");
            milestonesHeader.getStyleClass().add("section-header");
            dailyChart.getChildren().add(milestonesHeader);

            for (TaskChartItem milestone : filteredMilestones) {
                HBox milestoneItem = createDailyMilestoneItem(milestone);
                dailyChart.getChildren().add(milestoneItem);
            }
        }

        // Add empty message if no tasks or milestones
        if (filteredTasks.isEmpty() && filteredMilestones.isEmpty()) {
            Label emptyMessage = new Label("No tasks or milestones scheduled for this date.");
            emptyMessage.getStyleClass().add("empty-message");
            dailyChart.getChildren().add(emptyMessage);
        }

        return dailyChart;
    }

    /**
     * Creates a timeline axis based on the specified date range and view mode.
     *
     * @param startDate the start date
     * @param endDate   the end date
     * @param viewMode  the view mode (DAY, WEEK, MONTH)
     * @return a node containing the timeline axis
     */
    private static Node createTimelineAxis(LocalDate startDate, LocalDate endDate, String viewMode) {
        // Create an HBox for the timeline
        HBox timelineAxis = new HBox();
        timelineAxis.getStyleClass().add("timeline-axis");
        timelineAxis.setAlignment(Pos.BOTTOM_LEFT);

        // Determine unit width based on view mode
        double unitWidth = getUnitWidth(viewMode);

        if ("DAY".equalsIgnoreCase(viewMode)) {
            // Create day-based timeline
            LocalDate current = startDate;
            while (!current.isAfter(endDate)) {
                Label dayLabel = new Label(current.format(DATE_FORMATTER));
                dayLabel.setPrefWidth(unitWidth);
                dayLabel.setAlignment(Pos.CENTER);

                // Style weekends differently
                if (current.getDayOfWeek() == DayOfWeek.SATURDAY || current.getDayOfWeek() == DayOfWeek.SUNDAY) {
                    dayLabel.getStyleClass().add("weekend-label");
                }

                timelineAxis.getChildren().add(dayLabel);
                current = current.plusDays(1);
            }
        } else if ("WEEK".equalsIgnoreCase(viewMode)) {
            // Create week-based timeline
            LocalDate current = startDate;
            while (!current.isAfter(endDate)) {
                Label weekLabel = new Label("Week " + current.get(java.time.temporal.WeekFields.ISO.weekOfYear()) +
                        "\n" + current.format(DATE_FORMATTER));
                weekLabel.setPrefWidth(unitWidth);
                weekLabel.setAlignment(Pos.CENTER);

                timelineAxis.getChildren().add(weekLabel);
                current = current.plusWeeks(1);
            }
        } else { // MONTH view
            // Create month-based timeline
            LocalDate current = startDate.withDayOfMonth(1);
            while (!current.isAfter(endDate)) {
                Label monthLabel = new Label(current.format(MONTH_FORMATTER));

                // Calculate width based on days in month
                int daysInMonth = current.lengthOfMonth();
                monthLabel.setPrefWidth(unitWidth);
                monthLabel.setAlignment(Pos.CENTER);

                timelineAxis.getChildren().add(monthLabel);
                current = current.plusMonths(1);
            }
        }

        return timelineAxis;
    }

    /**
     * Creates the main chart content with tasks and milestones.
     *
     * @param tasks      the list of tasks
     * @param milestones the list of milestones
     * @param startDate  the start date
     * @param endDate    the end date
     * @param viewMode   the view mode
     * @return a GridPane containing the chart content
     */
    private static GridPane createChartContent(
            List<TaskChartItem> tasks,
            List<TaskChartItem> milestones,
            LocalDate startDate,
            LocalDate endDate,
            String viewMode) {

        GridPane contentPane = new GridPane();
        contentPane.setGridLinesVisible(false);
        contentPane.setHgap(0);
        contentPane.setVgap(2);

        // Create task rows
        int rowIndex = 0;
        for (TaskChartItem task : tasks) {
            // Create task label in column 0
            Label taskLabel = new Label(task.getTitle());
            taskLabel.setPrefWidth(150);
            taskLabel.setPadding(new Insets(5));
            taskLabel.setUserData(task.getId()); // Store ID for dependency lines
            contentPane.add(taskLabel, 0, rowIndex);

            // Create task bar in column 1
            Node taskBar = createTaskBar(task, startDate, endDate, viewMode);
            contentPane.add(taskBar, 1, rowIndex);

            rowIndex++;
        }

        // Add milestones after tasks
        for (TaskChartItem milestone : milestones) {
            // Create milestone label in column 0
            Label milestoneLabel = new Label(milestone.getTitle());
            milestoneLabel.setPrefWidth(150);
            milestoneLabel.setPadding(new Insets(5));
            milestoneLabel.getStyleClass().add("milestone-label");
            milestoneLabel.setUserData(milestone.getId()); // Store ID for dependency lines
            contentPane.add(milestoneLabel, 0, rowIndex);

            // Create milestone marker in column 1
            Node milestoneMarker = createMilestoneMarker(milestone, startDate, endDate, viewMode);
            contentPane.add(milestoneMarker, 1, rowIndex);

            rowIndex++;
        }

        // Configure column constraints
        ColumnConstraints labelColumn = new ColumnConstraints();
        labelColumn.setPrefWidth(150);
        labelColumn.setMinWidth(100);

        ColumnConstraints chartColumn = new ColumnConstraints();
        chartColumn.setHgrow(Priority.ALWAYS);
        chartColumn.setFillWidth(true);

        contentPane.getColumnConstraints().addAll(labelColumn, chartColumn);

        return contentPane;
    }

    /**
     * Creates a task bar for the Gantt chart.
     *
     * @param task       the task
     * @param chartStart the chart start date
     * @param chartEnd   the chart end date
     * @param viewMode   the view mode
     * @return a Node representing the task bar
     */
    private static Node createTaskBar(TaskChartItem task, LocalDate chartStart, LocalDate chartEnd, String viewMode) {
        // Create container for task bar
        StackPane container = new StackPane();
        container.setPrefHeight(DEFAULT_TASK_HEIGHT);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPadding(new Insets(0, 10, 0, 10));

        // Calculate position and width
        LocalDate taskStart = task.getStartDate();
        LocalDate taskEnd = task.getEndDate();

        // Ensure dates are within chart bounds
        if (taskStart == null || taskEnd == null ||
                taskStart.isAfter(chartEnd) || taskEnd.isBefore(chartStart)) {
            return container;
        }

        // Adjust dates to chart bounds if necessary
        if (taskStart.isBefore(chartStart)) {
            taskStart = chartStart;
        }
        if (taskEnd.isAfter(chartEnd)) {
            taskEnd = chartEnd;
        }

        double unitWidth = getUnitWidth(viewMode);
        double totalWidth = calculateTotalWidth(chartStart, chartEnd, viewMode);
        container.setPrefWidth(totalWidth);

        // Calculate bar position and width
        double startPos = calculatePosition(chartStart, taskStart, viewMode);
        double endPos = calculatePosition(chartStart, taskEnd, viewMode);
        double barWidth = endPos - startPos + unitWidth * 0.9; // Adjust width to avoid overlapping

        // Create task bar rectangle
        Rectangle taskBar = new Rectangle();
        taskBar.setWidth(barWidth);
        taskBar.setHeight(DEFAULT_TASK_HEIGHT * 0.8);
        taskBar.setArcWidth(5);
        taskBar.setArcHeight(5);

        // Set position
        StackPane.setAlignment(taskBar, Pos.CENTER_LEFT);
        StackPane.setMargin(taskBar, new Insets(0, 0, 0, startPos));

        // Set color based on task properties
        String color = task.getColor();
        if (color != null && !color.isEmpty()) {
            taskBar.setFill(Color.web(color));
            taskBar.setStroke(Color.web(adjustBorderColor(color)));
        } else {
            taskBar.setFill(Color.CORNFLOWERBLUE);
            taskBar.setStroke(Color.DARKBLUE);
        }

        // Add style class based on status
        if (task.isCompleted()) {
            taskBar.getStyleClass().add("task-bar");
            taskBar.getStyleClass().add("completed");
        } else {
            taskBar.getStyleClass().add("task-bar");
            taskBar.getStyleClass().add("in-progress");
        }

        // Set user data for dependency lines
        taskBar.setUserData(task.getId());

        // Add progress indicator
        if (task.getProgress() > 0 && task.getProgress() < 100) {
            Rectangle progressBar = new Rectangle();
            progressBar.setWidth((barWidth * task.getProgress()) / 100.0);
            progressBar.setHeight(DEFAULT_TASK_HEIGHT * 0.8);
            progressBar.setArcWidth(5);
            progressBar.setArcHeight(5);
            progressBar.setFill(Color.web(adjustProgressColor(color)));
            progressBar.setOpacity(0.7);

            // Position progress bar at same position as task bar
            StackPane.setAlignment(progressBar, Pos.CENTER_LEFT);
            StackPane.setMargin(progressBar, new Insets(0, 0, 0, startPos));

            container.getChildren().addAll(taskBar, progressBar);
        } else {
            container.getChildren().add(taskBar);
        }

        // Add tooltip with task details
        Tooltip tooltip = new Tooltip(
                "Task: " + task.getTitle() + "\n" +
                        "Start: " + task.getStartDate() + "\n" +
                        "End: " + task.getEndDate() + "\n" +
                        "Progress: " + task.getProgress() + "%");
        if (task.getAssignee() != null && !task.getAssignee().isEmpty()) {
            tooltip.setText(tooltip.getText() + "\nAssignee: " + task.getAssignee());
        }
        if (task.getSubsystem() != null && !task.getSubsystem().isEmpty()) {
            tooltip.setText(tooltip.getText() + "\nSubsystem: " + task.getSubsystem());
        }
        Tooltip.install(taskBar, tooltip);

        // Add task title if bar is wide enough
        if (barWidth > 50) {
            Label titleLabel = new Label(task.getTitle());
            titleLabel.setTextFill(Color.WHITE);
            titleLabel.setStyle("-fx-font-size: 10px;");
            StackPane.setAlignment(titleLabel, Pos.CENTER_LEFT);
            StackPane.setMargin(titleLabel, new Insets(0, 0, 0, startPos + 5));
            container.getChildren().add(titleLabel);
        }

        return container;
    }

    /**
     * Creates a milestone marker for the Gantt chart.
     *
     * @param milestone  the milestone
     * @param chartStart the chart start date
     * @param chartEnd   the chart end date
     * @param viewMode   the view mode
     * @return a Node representing the milestone marker
     */
    private static Node createMilestoneMarker(
            TaskChartItem milestone,
            LocalDate chartStart,
            LocalDate chartEnd,
            String viewMode) {

        // Create container for milestone marker
        StackPane container = new StackPane();
        container.setPrefHeight(DEFAULT_TASK_HEIGHT);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPadding(new Insets(0, 10, 0, 10));

        // Calculate position
        LocalDate milestoneDate = milestone.getStartDate();

        // Ensure date is within chart bounds
        if (milestoneDate == null || milestoneDate.isBefore(chartStart) || milestoneDate.isAfter(chartEnd)) {
            return container;
        }

        double unitWidth = getUnitWidth(viewMode);
        double totalWidth = calculateTotalWidth(chartStart, chartEnd, viewMode);
        container.setPrefWidth(totalWidth);

        // Calculate marker position
        double position = calculatePosition(chartStart, milestoneDate, viewMode);

        // Create milestone diamond marker
        Polygon diamond = new Polygon();
        diamond.getPoints().addAll(
                0.0, MILESTONE_DIAMOND_SIZE / 2,
                MILESTONE_DIAMOND_SIZE / 2, 0.0,
                MILESTONE_DIAMOND_SIZE, MILESTONE_DIAMOND_SIZE / 2,
                MILESTONE_DIAMOND_SIZE / 2, MILESTONE_DIAMOND_SIZE);

        // Set position
        StackPane.setAlignment(diamond, Pos.CENTER_LEFT);
        StackPane.setMargin(diamond, new Insets(0, 0, 0, position));

        // Set color based on milestone properties
        String color = milestone.getColor();
        if (color != null && !color.isEmpty()) {
            diamond.setFill(Color.web(color));
            diamond.setStroke(Color.web(adjustBorderColor(color)));
        } else {
            diamond.setFill(Color.PURPLE);
            //diamond.setStrokeColor(Color.DARKPURPLE);
            diamond.setStroke(Color.MEDIUMPURPLE);
        }

        // Add style class based on status
        if (milestone.isCompleted()) {
            diamond.getStyleClass().add("milestone-marker");
            diamond.getStyleClass().add("completed");
        } else {
            diamond.getStyleClass().add("milestone-marker");
            diamond.getStyleClass().add("in-progress");
        }

        // Set user data for dependency lines
        diamond.setUserData(milestone.getId());

        container.getChildren().add(diamond);

        // Add tooltip with milestone details
        Tooltip tooltip = new Tooltip(
                "Milestone: " + milestone.getTitle() + "\n" +
                        "Date: " + milestone.getStartDate());
        Tooltip.install(diamond, tooltip);

        return container;
    }

    /**
     * Adds dependency lines between tasks.
     *
     * @param chartPane the chart pane
     * @param tasks     the list of tasks
     * @param startDate the chart start date
     * @param endDate   the chart end date
     * @param viewMode  the view mode
     */
    private static void addDependencyLines(
            GridPane chartPane,
            List<TaskChartItem> tasks,
            LocalDate startDate,
            LocalDate endDate,
            String viewMode) {

        // Create a map of task IDs to their nodes in the chart
        Map<String, Node> taskNodesMap = new HashMap<>();

        // Collect all task and milestone nodes
        for (Node node : chartPane.getChildren()) {
            if (node.getUserData() instanceof String) {
                String id = (String) node.getUserData();
                taskNodesMap.put(id, node);
            }
        }

        // Create dependency lines
        for (TaskChartItem task : tasks) {
            String taskId = task.getId();
            List<String> dependencies = task.getDependencies();

            if (dependencies != null && !dependencies.isEmpty()) {
                for (String dependencyId : dependencies) {
                    Node targetNode = taskNodesMap.get(taskId);
                    Node sourceNode = taskNodesMap.get(dependencyId);

                    if (targetNode != null && sourceNode != null) {
                        // Calculate line positions
                        double sourceX = sourceNode.getBoundsInParent().getMaxX();
                        double sourceY = sourceNode.getBoundsInParent().getCenterY();
                        double targetX = targetNode.getBoundsInParent().getMinX();
                        double targetY = targetNode.getBoundsInParent().getCenterY();

                        // Create line
                        Line line = new Line(sourceX, sourceY, targetX, targetY);
                        line.getStyleClass().add("dependency-line");

                        // Add line to chart
                        chartPane.add(line, 1, 0, 1, chartPane.getRowCount());
                    }
                }
            }
        }
    }

    /**
     * Adds a line representing the current date to the chart.
     *
     * @param chartPane the chart pane
     * @param startDate the chart start date
     * @param endDate   the chart end date
     * @param viewMode  the view mode
     */
    private static void addTodayLine(
            GridPane chartPane,
            LocalDate startDate,
            LocalDate endDate,
            String viewMode) {

        LocalDate today = LocalDate.now();

        // Check if today is within chart range
        if (today.isBefore(startDate) || today.isAfter(endDate)) {
            return;
        }

        // Calculate line position
        double position = calculatePosition(startDate, today, viewMode);

        // Create line
        Line todayLine = new Line(position, 0, position, chartPane.getHeight());
        todayLine.getStyleClass().add("today-line");
        todayLine.setStrokeWidth(2);
        todayLine.setStroke(Color.RED);


        // Add line to chart
        chartPane.add(todayLine, 1, 0, 1, chartPane.getRowCount());

        // Add tooltip to the line
        Tooltip tooltip = new Tooltip("Today: " + today.format(DateTimeFormatter.ofPattern("MMM d, yyyy")));
        Tooltip.install(todayLine, tooltip);
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
            colorIndicator.setFill(Color.CORNFLOWERBLUE);
        }
        colorIndicator.setArcWidth(3);
        colorIndicator.setArcHeight(3);

        // Add task title
        Label titleLabel = new Label(task.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold;");

        // Add progress indicator
        ProgressBar progressBar = new ProgressBar(task.getProgress() / 100.0);
        progressBar.setPrefWidth(100);
        progressBar.setMaxHeight(10);

        Label progressLabel = new Label(task.getProgress() + "%");
        progressLabel.setStyle("-fx-font-size: 11px;");

        // Add all base components to task item
        taskItem.getChildren().addAll(colorIndicator, titleLabel, progressBar, progressLabel);

        // Add subsystem if available
        if (task.getSubsystem() != null && !task.getSubsystem().isEmpty()) {
            Label subsystemLabel = new Label(task.getSubsystem());
            subsystemLabel.setStyle("-fx-font-style: italic; -fx-font-size: 11px;");
            taskItem.getChildren().add(subsystemLabel);
        }

        // Add assignee if available
        if (task.getAssignee() != null && !task.getAssignee().isEmpty()) {
            Label assigneeLabel = new Label(task.getAssignee());
            assigneeLabel.setStyle("-fx-font-size: 11px;");
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

        // Add diamond icon
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
        Circle statusIndicator = new Circle(5);
        if (milestone.isCompleted()) {
            statusIndicator.setFill(Color.GREEN);
            statusIndicator.getStyleClass().add("status-completed");
        } else {
            statusIndicator.setFill(Color.GRAY);
            statusIndicator.getStyleClass().add("status-pending");
        }

        Label statusLabel = new Label(milestone.isCompleted() ? "Completed" : "Pending");
        statusLabel.setStyle("-fx-font-size: 11px;");

        // Add components to milestone item
        milestoneItem.getChildren().addAll(diamond, titleLabel, statusIndicator, statusLabel);

        return milestoneItem;
    }

    /**
     * Applies CSS styling to the chart components.
     *
     * @param chartPane the chart pane
     */
    private static void applyStyles(BorderPane chartPane) {
        // Apply CSS classes from gantt-chart.css
        chartPane.getStyleClass().add("gantt-chart-container");

        Node timelineAxis = chartPane.getTop();
        if (timelineAxis != null) {
            timelineAxis.getStyleClass().add("timeline-axis");
        }
    }

    /**
     * Calculates the position of a date on the chart.
     *
     * @param chartStart the chart start date
     * @param date       the date to position
     * @param viewMode   the view mode
     * @return the x-coordinate for the date
     */
    private static double calculatePosition(LocalDate chartStart, LocalDate date, String viewMode) {
        if (chartStart == null || date == null) {
            return 0;
        }

        double unitWidth = getUnitWidth(viewMode);

        if ("DAY".equalsIgnoreCase(viewMode)) {
            long days = ChronoUnit.DAYS.between(chartStart, date);
            return days * unitWidth;
        } else if ("WEEK".equalsIgnoreCase(viewMode)) {
            // Calculate weeks between dates
            long days = ChronoUnit.DAYS.between(chartStart, date);
            double weeks = days / 7.0;
            return weeks * unitWidth;
        } else { // MONTH view
            // Calculate months and days between dates
            long months = ChronoUnit.MONTHS.between(chartStart, date);

            // Add partial month
            LocalDate monthStart = chartStart.plusMonths(months);
            long daysInMonth = monthStart.lengthOfMonth();
            long daysIntoMonth = ChronoUnit.DAYS.between(monthStart, date);

            double fraction = daysIntoMonth / (double) daysInMonth;
            return months * unitWidth + (fraction * unitWidth);
        }
    }

    /**
     * Calculates the total width of the chart.
     *
     * @param startDate the chart start date
     * @param endDate   the chart end date
     * @param viewMode  the view mode
     * @return the total width
     */
    private static double calculateTotalWidth(LocalDate startDate, LocalDate endDate, String viewMode) {
        if (startDate == null || endDate == null) {
            return 1000; // Default width
        }

        double unitWidth = getUnitWidth(viewMode);

        if ("DAY".equalsIgnoreCase(viewMode)) {
            long days = ChronoUnit.DAYS.between(startDate, endDate) + 1; // Include end date
            return days * unitWidth;
        } else if ("WEEK".equalsIgnoreCase(viewMode)) {
            // Calculate weeks between dates (round up to include partial weeks)
            long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
            long weeks = (days + 6) / 7; // Round up to include partial weeks
            return weeks * unitWidth;
        } else { // MONTH view
            // Calculate months between dates (include partial months)
            long months = ChronoUnit.MONTHS.between(startDate, endDate);

            // Check if endDate is not at the beginning of a month
            if (endDate.getDayOfMonth() > 1) {
                months++; // Include partial month
            }

            return (months + 1) * unitWidth; // Add 1 to include end month
        }
    }

    /**
     * Gets the unit width based on view mode.
     *
     * @param viewMode the view mode
     * @return the unit width
     */
    private static double getUnitWidth(String viewMode) {
        if ("DAY".equalsIgnoreCase(viewMode)) {
            return DAY_WIDTH;
        } else if ("WEEK".equalsIgnoreCase(viewMode)) {
            return WEEK_WIDTH;
        } else { // MONTH view
            return MONTH_WIDTH;
        }
    }

    /**
     * Adjusts a color for use as a border color (typically darker).
     *
     * @param color the fill color
     * @return the border color
     */
    private static String adjustBorderColor(String color) {
        if (color == null || color.isEmpty()) {
            return "#000000";
        }

        // Simple implementation - for sophisticated color manipulation,
        // a dedicated color utility would be better
        try {
            Color c = Color.web(color);
            Color darker = c.darker();
            return String.format("#%02X%02X%02X",
                    (int) (darker.getRed() * 255),
                    (int) (darker.getGreen() * 255),
                    (int) (darker.getBlue() * 255));
        } catch (Exception e) {
            return color; // Return original on error
        }
    }

    /**
     * Adjusts a color for use as a progress indicator (typically lighter).
     *
     * @param color the task color
     * @return the progress color
     */
    private static String adjustProgressColor(String color) {
        if (color == null || color.isEmpty()) {
            return "#66A5FF"; // Default light blue
        }

        try {
            Color c = Color.web(color);
            Color lighter = c.brighter();
            return String.format("#%02X%02X%02X",
                    (int) (lighter.getRed() * 255),
                    (int) (lighter.getGreen() * 255),
                    (int) (lighter.getBlue() * 255));
        } catch (Exception e) {
            return "#66A5FF"; // Return default on error
        }
    }

    /**
     * Creates a Chart-FX XYChart for custom visualizations.
     * This is useful for more complex chart types that need Chart-FX's advanced
     * features.
     *
     * @param tasks     the list of tasks
     * @param startDate the chart start date
     * @param endDate   the chart end date
     * @return an XYChart
     */
    public static XYChart createChartFxGanttChart(List<TaskChartItem> tasks, LocalDate startDate, LocalDate endDate) {
        // Create x-axis (timeline)
        DefaultNumericAxis xAxis = new DefaultNumericAxis("Timeline", "days");
        xAxis.setAutoRanging(false);
        xAxis.setMin(0);
        xAxis.setMax(ChronoUnit.DAYS.between(startDate, endDate) + 1);

        // Create y-axis (tasks)
        CategoryAxis yAxis = new CategoryAxis("Tasks");
        List<String> categories = tasks.stream()
                .map(TaskChartItem::getTitle)
                .collect(Collectors.toList());
        yAxis.setCategories(categories);

        // Create chart
        XYChart chart = new XYChart(xAxis, yAxis);
        chart.getPlugins().add(new Zoomer());
        chart.getPlugins().add(new DataPointTooltip());

        // Create renderer
        ErrorDataSetRenderer renderer = new ErrorDataSetRenderer();
        renderer.setDrawBars(true);
        renderer.setErrorType(ErrorStyle.NONE);
        renderer.setPolyLineStyle(LineStyle.NORMAL);
        chart.getRenderers().add(renderer);

        // Create datasets for each task
        int taskIndex = 0;
        for (TaskChartItem task : tasks) {
            if (task.getStartDate() == null || task.getEndDate() == null) {
                continue;
            }

            long startDays = ChronoUnit.DAYS.between(startDate, task.getStartDate());
            long endDays = ChronoUnit.DAYS.between(startDate, task.getEndDate());

            DefaultDataSet dataSet = new DefaultDataSet(task.getTitle());
            dataSet.add(startDays, taskIndex);
            dataSet.add(endDays + 1, taskIndex); // +1 to include end date

            renderer.getDatasets().add(dataSet);
            taskIndex++;
        }

        return chart;
    }
}