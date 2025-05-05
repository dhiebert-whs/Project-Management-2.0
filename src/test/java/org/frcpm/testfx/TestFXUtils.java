package org.frcpm.testfx;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Labeled;
import javafx.scene.input.KeyCode;
import javafx.stage.Window;
import org.testfx.api.FxRobot;
import org.testfx.util.WaitForAsyncUtils;

import java.io.File;
import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility methods for TestFX testing.
 */
public class TestFXUtils {
    
    private static final Logger LOGGER = Logger.getLogger(TestFXUtils.class.getName());
    
    private TestFXUtils() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Wait for the JavaFX thread to process all pending events with a timeout.
     * 
     * @param timeoutMillis the timeout in milliseconds
     */
    public static void waitForFxEvents(long timeoutMillis) {
        try {
            // Use the standard waitForFxEvents() and add a separate sleep for timeout
            WaitForAsyncUtils.waitForFxEvents();
            
            // Add a small sleep to ensure events are processed
            if (timeoutMillis > 0) {
                Thread.sleep(timeoutMillis);
                // Wait again after the sleep
                WaitForAsyncUtils.waitForFxEvents();
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Exception waiting for FX events", e);
        }
    }
    
    /**
     * Run a task on the JavaFX application thread and wait for completion.
     * 
     * @param runnable The task to run
     * @param timeoutMillis The timeout in milliseconds
     * @return true if the task completed within the timeout, false otherwise
     */
    public static boolean runOnFxThreadAndWait(Runnable runnable, long timeoutMillis) {
        try {
            if (Platform.isFxApplicationThread()) {
                runnable.run();
                return true;
            } else {
                final CountDownLatch latch = new CountDownLatch(1);
                Platform.runLater(() -> {
                    try {
                        runnable.run();
                    } finally {
                        latch.countDown();
                    }
                });
                
                // Wait for the task to complete, with timeout
                return latch.await(timeoutMillis, TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, "Interrupted while waiting for JavaFX task", e);
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    /**
     * Print the scene graph for debugging purposes.
     * 
     * @param root the root node of the scene graph
     */
    public static void printSceneGraph(Parent root) {
        printNode(root, 0);
    }
    
    /**
     * Recursively print a node and its children with indentation.
     * 
     * @param node the node to print
     * @param level the indentation level
     */
    private static void printNode(Node node, int level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append("  ");
        }
        
        sb.append(node.getClass().getSimpleName());
        sb.append(" [id=").append(node.getId()).append("]");
        
        if (node instanceof Labeled) {
            Labeled labeled = (Labeled) node;
            sb.append(" [text=").append(labeled.getText()).append("]");
        }
        
        LOGGER.info(sb.toString());
        
        if (node instanceof Parent) {
            Parent parent = (Parent) node;
            for (Node child : parent.getChildrenUnmodifiable()) {
                printNode(child, level + 1);
            }
        }
    }
    
    /**
     * Print information about all open windows.
     */
    public static void printOpenWindows() {
        LOGGER.info("Open windows:");
        for (Window window : Window.getWindows()) {
            LOGGER.info("Window: " + window.getClass().getSimpleName() + 
                      " [visible=" + window.isShowing() + "]");
            
            if (window.isShowing() && window.getScene() != null) {
                Scene scene = window.getScene();
                LOGGER.info("  Scene: " + scene);
                if (scene.getRoot() != null) {
                    LOGGER.info("  Root: " + scene.getRoot().getClass().getSimpleName());
                }
            }
        }
    }
    
    /**
     * Take a screenshot of the current scene for debugging.
     * 
     * @param robot the TestFX robot
     * @param name the filename for the screenshot
     */
    public static void takeScreenshot(FxRobot robot, String name) {
        try {
            File screenshot = new File("target/screenshots");
            if (!screenshot.exists()) {
                screenshot.mkdirs();
            }
            
            screenshot = new File(screenshot, name + ".png");
            LOGGER.info("Taking screenshot: " + screenshot.getAbsolutePath());
            
            try {
                // Get the active window
                Window window = null;
                for (Window w : Window.getWindows()) {
                    if (w.isShowing()) {
                        window = w;
                        break;
                    }
                }
                
                if (window == null) {
                    LOGGER.warning("No active window found for screenshot");
                    return;
                }
                
                // Use simple file logging instead of actual screenshots
                // since the JavaFX embed classes aren't available
                StringBuilder sb = new StringBuilder();
                sb.append("Screenshot details for: ").append(name).append("\n");
                sb.append("Window class: ").append(window.getClass().getName()).append("\n");
                sb.append("Scene dimensions: ").append(window.getScene().getWidth())
                  .append("x").append(window.getScene().getHeight()).append("\n");
                sb.append("Root node: ").append(window.getScene().getRoot().getClass().getName()).append("\n");
                
                // Log visible components
                if (window.getScene().getRoot() instanceof Parent) {
                    Parent root = (Parent) window.getScene().getRoot();
                    sb.append("Children count: ").append(root.getChildrenUnmodifiable().size()).append("\n");
                    
                    for (Node child : root.getChildrenUnmodifiable()) {
                        sb.append("  - ").append(child.getClass().getSimpleName());
                        if (child.getId() != null) {
                            sb.append(" [id=").append(child.getId()).append("]");
                        }
                        if (child instanceof Labeled) {
                            sb.append(" [text=\"").append(((Labeled)child).getText()).append("\"]");
                        }
                        sb.append("\n");
                    }
                }
                
                // Write log instead of image
                try (java.io.FileWriter writer = new java.io.FileWriter(screenshot + ".log")) {
                    writer.write(sb.toString());
                }
                
                LOGGER.info("Screenshot log saved to: " + screenshot + ".log");
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to save screenshot log", e);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to take screenshot", e);
        }
    }
    
    /**
     * Clear a text field and enter a new value.
     * 
     * @param robot the TestFX robot
     * @param query the query to find the text field
     * @param text the text to enter
     */
    public static void clearAndWrite(FxRobot robot, String query, String text) {
        robot.clickOn(query);
        robot.press(KeyCode.CONTROL).press(KeyCode.A).release(KeyCode.A).release(KeyCode.CONTROL);
        robot.write(text);
    }
    
    /**
     * Set a value in a date picker.
     * 
     * @param robot the TestFX robot
     * @param query the query to find the date picker
     * @param date the date to set
     */
    public static void setDatePickerValue(FxRobot robot, String query, LocalDate date) {
        WaitForAsyncUtils.waitForFxEvents();
        Platform.runLater(() -> {
            robot.lookup(query).queryAs(javafx.scene.control.DatePicker.class).setValue(date);
        });
        WaitForAsyncUtils.waitForFxEvents();
    }
    
    /**
     * Log node info for debugging.
     * 
     * @param node the node to log info for
     */
    public static void logNodeInfo(Node node) {
        if (node == null) {
            LOGGER.info("Node is null");
            return;
        }
        
        LOGGER.info("Node: " + node.getClass().getName());
        LOGGER.info("  ID: " + node.getId());
        LOGGER.info("  Visible: " + node.isVisible());
        LOGGER.info("  Managed: " + node.isManaged());
        LOGGER.info("  Bounds: " + node.getBoundsInParent());
        
        if (node instanceof Labeled) {
            LOGGER.info("  Text: " + ((Labeled) node).getText());
        }
    }
}