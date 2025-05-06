// src/main/java/org/frcpm/charts/ChartStyler.java
package org.frcpm.charts;

import javafx.scene.paint.Color;
import javafx.scene.chart.Chart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.scene.Node;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Utility class for styling Chart-FX charts.
 * This class provides methods for applying consistent styling to chart components.
 */
public class ChartStyler {
    
    // CSS class names
    private static final String TASK_BAR_CLASS = "task-bar";
    private static final String MILESTONE_MARKER_CLASS = "milestone-marker";
    private static final String COMPLETED_CLASS = "completed";
    private static final String IN_PROGRESS_CLASS = "in-progress";
    private static final String DEPENDENCY_LINE_CLASS = "dependency-line";
    
    /**
     * Applies default styling to a chart.
     * 
     * @param chart the chart to style
     */
    public static void applyDefaultStyling(Chart chart) {
        // Apply CSS class to chart
        chart.getStyleClass().add("gantt-chart");
        
        // Set default chart properties
        chart.setAnimated(false);
        chart.setLegendVisible(false);
    }
    
    /**
     * Styles a task bar node based on the task properties.
     * 
     * @param node the node to style
     * @param task the task data
     */
    public static void styleTaskBar(Node node, TaskChartItem task) {
        if (node == null || task == null) {
            return;
        }
        
        // Apply base CSS class
        node.getStyleClass().add(TASK_BAR_CLASS);
        
        // Apply status-specific CSS class
        if (task.isCompleted()) {
            node.getStyleClass().add(COMPLETED_CLASS);
        } else {
            node.getStyleClass().add(IN_PROGRESS_CLASS);
        }
        
        // Apply task-specific color if provided
        if (task.getColor() != null && !task.getColor().isEmpty()) {
            // If the node is a Rectangle, set its fill directly
            if (node instanceof Rectangle) {
                Rectangle rect = (Rectangle) node;
                rect.setFill(Color.web(task.getColor()));
                
                // Set slightly darker stroke
                Color baseColor = Color.web(task.getColor());
                Color strokeColor = baseColor.darker();
                rect.setStroke(strokeColor);
            }
        }
        
        // Create and apply tooltip
        createTooltip(node, task);
    }
    
    /**
     * Styles a milestone marker based on the milestone properties.
     * 
     * @param node the node to style
     * @param milestone the milestone data
     */
    public static void styleMilestoneMarker(Node node, TaskChartItem milestone) {
        if (node == null || milestone == null) {
            return;
        }
        
        // Apply base CSS class
        node.getStyleClass().add(MILESTONE_MARKER_CLASS);
        
        // Apply status-specific CSS class
        if (milestone.isCompleted()) {
            node.getStyleClass().add(COMPLETED_CLASS);
        } else {
            node.getStyleClass().add(IN_PROGRESS_CLASS);
        }
        
        // Apply milestone-specific color if provided
        if (milestone.getColor() != null && !milestone.getColor().isEmpty()) {
            // If the node is a shape, set its fill directly
            if (node instanceof javafx.scene.shape.Shape) {
                javafx.scene.shape.Shape shape = (javafx.scene.shape.Shape) node;
                shape.setFill(Color.web(milestone.getColor()));
                
                // Set slightly darker stroke
                Color baseColor = Color.web(milestone.getColor());
                Color strokeColor = baseColor.darker();
                shape.setStroke(strokeColor);
            }
        }
        
        // Create and apply tooltip
        createTooltip(node, milestone);
    }
    
    /**
     * Styles a dependency line between tasks.
     * 
     * @param node the node to style
     */
    public static void styleDependencyLine(Node node) {
        if (node == null) {
            return;
        }
        
        // Apply CSS class
        node.getStyleClass().add(DEPENDENCY_LINE_CLASS);
        
        // Additional styling if needed
        if (node instanceof javafx.scene.shape.Line) {
            javafx.scene.shape.Line line = (javafx.scene.shape.Line) node;
            line.setStroke(Color.GRAY);
            line.setStrokeWidth(1.5);
            line.getStrokeDashArray().addAll(5.0, 5.0);
        }
    }
    
    /**
     * Creates and applies a tooltip to a node with task/milestone information.
     * 
     * @param node the node to apply the tooltip to
     * @param item the task/milestone data
     */
    private static void createTooltip(Node node, TaskChartItem item) {
        StringBuilder tooltipText = new StringBuilder();
        
        // Add title
        tooltipText.append(item.getTitle()).append("\n");
        
        // Add dates
        tooltipText.append("Start: ").append(item.getStartDate()).append("\n");
        if (!item.isMilestone()) {
            tooltipText.append("End: ").append(item.getEndDate()).append("\n");
        }
        
        // Add progress for tasks
        if (!item.isMilestone()) {
            tooltipText.append("Progress: ").append(item.getProgress()).append("%\n");
        }
        
        // Add assignee if available
        if (item.getAssignee() != null && !item.getAssignee().isEmpty()) {
            tooltipText.append("Assignee: ").append(item.getAssignee()).append("\n");
        }
        
        // Add subsystem if available
        if (item.getSubsystem() != null && !item.getSubsystem().isEmpty()) {
            tooltipText.append("Subsystem: ").append(item.getSubsystem());
        }
        
        // Create tooltip
        Tooltip tooltip = new Tooltip(tooltipText.toString());
        tooltip.setShowDelay(Duration.millis(200));
        tooltip.setHideDelay(Duration.millis(200));
        Tooltip.install(node, tooltip);
    }
    
    /**
     * Converts a hex color string to a Color object.
     * 
     * @param colorStr the hex color string (e.g., "#FF0000" for red)
     * @return the Color object, or Color.GRAY if conversion fails
     */
    public static Color parseColor(String colorStr) {
        if (colorStr == null || colorStr.isEmpty()) {
            return Color.GRAY;
        }
        
        try {
            return Color.web(colorStr);
        } catch (IllegalArgumentException e) {
            // Return a default color if parsing fails
            return Color.GRAY;
        }
    }
    
    /**
     * Gets a darker version of the specified color for use in borders or outlines.
     * 
     * @param baseColor the base color
     * @return a darker version of the color
     */
    public static Color getDarkerColor(Color baseColor) {
        return baseColor.darker();
    }
    
    /**
     * Gets a lighter version of the specified color for use in highlights.
     * 
     * @param baseColor the base color
     * @return a lighter version of the color
     */
    public static Color getLighterColor(Color baseColor) {
        return baseColor.brighter();
    }
}