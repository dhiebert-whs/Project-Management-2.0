// src/main/java/org/frcpm/charts/ChartStyler.java
package org.frcpm.charts;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for styling Chart-FX chart components.
 * This class provides methods to apply CSS styles and custom styling to charts.
 */
public class ChartStyler {
    
    // Define CSS class mappings for chart elements
    private static final Map<String, String> TASK_STATUS_CLASSES = new HashMap<>();
    private static final Map<String, String> PRIORITY_CLASSES = new HashMap<>();
    
    static {
        // Initialize task status CSS classes
        TASK_STATUS_CLASSES.put("not-started", "status-not-started");
        TASK_STATUS_CLASSES.put("in-progress", "status-in-progress");
        TASK_STATUS_CLASSES.put("completed", "status-completed");
        
        // Initialize priority CSS classes
        PRIORITY_CLASSES.put("low", "priority-low");
        PRIORITY_CLASSES.put("medium", "priority-medium");
        PRIORITY_CLASSES.put("high", "priority-high");
        PRIORITY_CLASSES.put("critical", "priority-critical");
    }
    
    /**
     * Applies CSS styles to a chart pane based on the loaded stylesheet.
     *
     * @param chartPane the chart pane to style
     */
    public static void applyChartStyles(Pane chartPane) {
        if (chartPane == null) {
            return;
        }
        
        // Add base style class to chart pane
        if (!chartPane.getStyleClass().contains("gantt-chart-container")) {
            chartPane.getStyleClass().add("gantt-chart-container");
        }
        
        // Apply styles to chart components recursively
        applyStylesToChildren(chartPane);
    }
    
    /**
     * Recursively applies styles to all children of a node.
     *
     * @param parent the parent node
     */
    private static void applyStylesToChildren(Parent parent) {
        for (Node node : parent.getChildrenUnmodifiable()) {
            // Apply specific styles based on node type
            if (node instanceof Shape) {
                applyShapeStyles((Shape) node);
            } else if (node instanceof Label) {
                applyLabelStyles((Label) node);
            }
            
            // Recurse through children
            if (node instanceof Parent) {
                applyStylesToChildren((Parent) node);
            }
        }
    }
    
    /**
     * Applies styles to shape elements (task bars, milestone markers, etc.).
     *
     * @param shape the shape to style
     */
    private static void applyShapeStyles(Shape shape) {
        // Check user data for styling hints
        if (shape.getUserData() instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> userData = (Map<String, Object>) shape.getUserData();
            
            // Apply task bar styles
            if (userData.containsKey("type") && "task".equals(userData.get("type"))) {
                shape.getStyleClass().add("task-bar");
                
                // Apply status class
                if (userData.containsKey("status")) {
                    String status = (String) userData.get("status");
                    String statusClass = TASK_STATUS_CLASSES.get(status);
                    if (statusClass != null) {
                        shape.getStyleClass().add(statusClass);
                    }
                }
                
                // Apply priority class
                if (userData.containsKey("priority")) {
                    String priority = (String) userData.get("priority");
                    String priorityClass = PRIORITY_CLASSES.get(priority);
                    if (priorityClass != null) {
                        shape.getStyleClass().add(priorityClass);
                    }
                }
            }
            // Apply milestone styles
            else if (userData.containsKey("type") && "milestone".equals(userData.get("type"))) {
                shape.getStyleClass().add("milestone-marker");
                
                // Apply status class
                if (userData.containsKey("status")) {
                    String status = (String) userData.get("status");
                    String statusClass = TASK_STATUS_CLASSES.get(status);
                    if (statusClass != null) {
                        shape.getStyleClass().add(statusClass);
                    }
                }
            }
            // Apply dependency line styles
            else if (userData.containsKey("type") && "dependency".equals(userData.get("type"))) {
                shape.getStyleClass().add("dependency-line");
            }
            // Apply today line styles
            else if (userData.containsKey("type") && "today".equals(userData.get("type"))) {
                shape.getStyleClass().add("today-line");
            }
        }
    }
    
    /**
     * Applies styles to label elements.
     *
     * @param label the label to style
     */
    private static void applyLabelStyles(Label label) {
        // Check for label type in user data
        if (label.getUserData() instanceof String) {
            String type = (String) label.getUserData();
            
            if ("task-title".equals(type)) {
                label.getStyleClass().add("task-title");
            } else if ("milestone-title".equals(type)) {
                label.getStyleClass().add("milestone-title");
            } else if ("date-label".equals(type)) {
                label.getStyleClass().add("date-label");
            }
        }
    }
    
    /**
     * Applies a color to a shape based on task priority.
     *
     * @param shape the shape to color
     * @param priority the task priority
     */
    public static void applyPriorityColor(Shape shape, String priority) {
        if (shape == null) {
            return;
        }
        
        Color fillColor;
        
        switch (priority.toLowerCase()) {
            case "low":
                fillColor = Color.web("#28a745"); // Green
                break;
            case "medium":
                fillColor = Color.web("#ffc107"); // Yellow
                break;
            case "high":
                fillColor = Color.web("#fd7e14"); // Orange
                break;
            case "critical":
                fillColor = Color.web("#dc3545"); // Red
                break;
            default:
                fillColor = Color.web("#6c757d"); // Gray for unknown priority
                break;
        }
        
        // Apply fill color
        shape.setFill(fillColor);
        
        // Apply darker stroke color
        Color strokeColor = fillColor.darker();
        shape.setStroke(strokeColor);
    }
    
    /**
     * Applies a color to a shape based on task status.
     *
     * @param shape the shape to color
     * @param status the task status
     */
    public static void applyStatusColor(Shape shape, String status) {
        if (shape == null) {
            return;
        }
        
        Color fillColor;
        
        switch (status.toLowerCase()) {
            case "not-started":
                fillColor = Color.web("#ced4da"); // Gray
                break;
            case "in-progress":
                fillColor = Color.web("#17a2b8"); // Cyan
                break;
            case "completed":
                fillColor = Color.web("#28a745"); // Green
                break;
            default:
                fillColor = Color.web("#6c757d"); // Dark gray for unknown status
                break;
        }
        
        // Apply fill color
        shape.setFill(fillColor);
        
        // Apply darker stroke color
        Color strokeColor = fillColor.darker();
        shape.setStroke(strokeColor);
    }
    
    /**
     * Applies a progress indicator to a task bar.
     *
     * @param taskBar the task bar rectangle
     * @param progress the progress percentage (0-100)
     */
    public static void applyProgressIndicator(Rectangle taskBar, int progress) {
        if (taskBar == null || progress < 0 || progress > 100) {
            return;
        }
        
        // Create a new rectangle for the progress indicator
        Rectangle progressIndicator = new Rectangle();
        
        // Set the same height and y position as the task bar
        progressIndicator.setHeight(taskBar.getHeight());
        progressIndicator.setY(taskBar.getY());
        
        // Calculate width based on progress
        double progressWidth = taskBar.getWidth() * (progress / 100.0);
        progressIndicator.setWidth(progressWidth);
        progressIndicator.setX(taskBar.getX());
        
        // Set the style for the progress indicator
        progressIndicator.getStyleClass().add("progress-indicator");
        
        // Get the parent pane and add the progress indicator
        if (taskBar.getParent() instanceof Pane) {
            Pane parent = (Pane) taskBar.getParent();
            parent.getChildren().add(progressIndicator);
            
            // Ensure the progress indicator is behind the task bar
            progressIndicator.toBack();
            taskBar.toFront();
        }
    }
    
    /**
     * Creates a color based on hexadecimal string representation.
     *
     * @param hexColor the hexadecimal color string (e.g., "#ff0000")
     * @return the Color object, or a default color if the input is invalid
     */
    public static Color createColor(String hexColor) {
        try {
            return Color.web(hexColor);
        } catch (Exception e) {
            return Color.GRAY; // Default color on error
        }
    }
}