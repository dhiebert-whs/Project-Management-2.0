// src/test/java/org/frcpm/testfx/BaseFxTest.java
package org.frcpm.testfx;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.frcpm.di.ServiceLocator;
import org.frcpm.mvvm.MvvmConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.robot.Motion;
import org.testfx.util.WaitForAsyncUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base class for TestFX tests providing common functionality.
 * This class handles JavaFX toolkit initialization and test environment setup.
 */
@ExtendWith(ApplicationExtension.class)
public abstract class BaseFxTest extends FxRobot {
    
    private static final Logger LOGGER = Logger.getLogger(BaseFxTest.class.getName());
    
    /** Default timeout for TestFX operations in milliseconds */
    protected static final int DEFAULT_TIMEOUT = 5000;
    
    /** Default width for test stages */
    protected static final int DEFAULT_STAGE_WIDTH = 800;
    
    /** Default height for test stages */
    protected static final int DEFAULT_STAGE_HEIGHT = 600;
    
    /** Primary stage for tests */
    protected Stage primaryStage;
    
    /** List of all stages created during the test */
    private final List<Stage> testStages = new ArrayList<>();
    
    /** Properties loaded from testfx.properties */
    private Properties testFxProperties;
    
    /** Flag indicating if the test is running in headless mode */
    private boolean headless = false;
    
    /**
     * Default constructor.
     */
    public BaseFxTest() {
        loadTestProperties();
    }
    
    /**
     * Loads test properties from testfx.properties.
     */
    private void loadTestProperties() {
        try {
            testFxProperties = new Properties();
            testFxProperties.load(getClass().getClassLoader()
                    .getResourceAsStream("testfx.properties"));
            
            // Check if running in headless mode
            headless = Boolean.parseBoolean(testFxProperties.getProperty("testfx.headless", "false"));
            
            // Configure system properties from testfx.properties
            for (String key : testFxProperties.stringPropertyNames()) {
                if (System.getProperty(key) == null) {
                    System.setProperty(key, testFxProperties.getProperty(key));
                }
            }
            
            LOGGER.info("TestFX properties loaded, headless mode: " + headless);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not load testfx.properties", e);
        }
    }
    
    /**
     * Sets up the JavaFX toolkit and primary stage.
     * This method is called by TestFX before each test.
     * 
     * @param primaryStage the primary stage
     */
    @Start
    public void start(Stage primaryStage) {
        LOGGER.info("Starting TestFX with primary stage");
        this.primaryStage = primaryStage;
        
        // Configure the primary stage
        primaryStage.setTitle("TestFX Stage");
        primaryStage.setWidth(DEFAULT_STAGE_WIDTH);
        primaryStage.setHeight(DEFAULT_STAGE_HEIGHT);
        
        // Add to list of stages for tracking
        testStages.add(primaryStage);
        
        // Initialize MVVMFx configuration
        initMvvmConfig();
        
        // Setup test environment - override in subclasses
        setupTestEnvironment(primaryStage);
    }
    
    /**
     * Initializes the MVVMFx configuration.
     * This ensures the ServiceLocator is properly initialized for tests.
     */
    protected void initMvvmConfig() {
        // Clear existing configuration first
        ServiceLocator.clear();
        
        // Initialize MVVMFx configuration
        if (!MvvmConfig.isInitialized()) {
            MvvmConfig.initialize();
            LOGGER.info("MVVMFx configuration initialized for tests");
        }
    }
    
    /**
     * Sets up the test environment.
     * Override this method in subclasses to configure specific test setups.
     * 
     * @param stage the primary stage
     */
    protected abstract void setupTestEnvironment(Stage stage);
    
    /**
     * Creates and shows a test scene with the provided root node.
     * 
     * @param root the root node
     * @return the created scene
     */
    protected Scene createTestScene(Node root) {
        Scene scene = new Scene(root instanceof Pane ? (Pane) root : new Pane(root), 
                                DEFAULT_STAGE_WIDTH, DEFAULT_STAGE_HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.toFront();
        
        // Wait for the scene to be shown
        WaitForAsyncUtils.waitForFxEvents();
        
        return scene;
    }
    
    /**
     * Creates and shows a test scene in a new stage.
     * 
     * @param root the root node
     * @param title the stage title
     * @return the created stage
     */
    protected Stage createTestStage(Node root, String title) {
        Stage stage = new Stage();
        stage.setTitle(title);
        Scene scene = new Scene(root instanceof Pane ? (Pane) root : new Pane(root), 
                               DEFAULT_STAGE_WIDTH, DEFAULT_STAGE_HEIGHT);
        stage.setScene(scene);
        stage.show();
        stage.toFront();
        
        // Add to list of stages for tracking
        testStages.add(stage);
        
        // Wait for the stage to be shown
        WaitForAsyncUtils.waitForFxEvents();
        
        return stage;
    }
    
    /**
     * Gets a node from the scene with the specified ID.
     * 
     * @param <T> the node type
     * @param query the query string (CSS selector or lookup ID)
     * @return the node
     */
    @SuppressWarnings("unchecked")
    protected <T extends Node> T find(String query) {
        Node node = lookup(query).query();
        return (T) node;
    }
    
    /**
     * Gets a common resource bundle for the test.
     * 
     * @return the resource bundle
     */
    protected ResourceBundle getTestResourceBundle() {
        try {
            return ResourceBundle.getBundle("org.frcpm.mvvm.views.common");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not load common resource bundle", e);
            return null;
        }
    }
    
    /**
     * Gets a specific resource bundle for the test.
     * 
     * @param baseName the base name of the resource bundle
     * @return the resource bundle
     */
    protected ResourceBundle getResourceBundle(String baseName) {
        try {
            return ResourceBundle.getBundle(baseName);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not load resource bundle: " + baseName, e);
            return null;
        }
    }
    
    /**
     * Waits for the JavaFX application thread to process all events.
     */
    protected void waitForFxEvents() {
        WaitForAsyncUtils.waitForFxEvents();
    }
    
    /**
     * Runs code on the JavaFX application thread and waits for completion.
     * 
     * @param runnable the code to run
     */
    protected void runOnFxThreadAndWait(Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            final CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                try {
                    runnable.run();
                } finally {
                    latch.countDown();
                }
            });
            
            try {
                if (!latch.await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS)) {
                    LOGGER.warning("Timeout while waiting for JavaFX operation to complete");
                }
            } catch (InterruptedException e) {
                LOGGER.log(Level.WARNING, "Interrupted while waiting for JavaFX operation", e);
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Cleans up after each test.
     * This method is called by JUnit after each test.
     */
    @AfterEach
    public void cleanUp() throws TimeoutException {
        // Close all stages created during the test
        FxToolkit.cleanupStages();
        
        // Clear MVVMFx configuration
        if (MvvmConfig.isInitialized()) {
            MvvmConfig.shutdown();
        }
        
        // Clear the service locator
        ServiceLocator.clear();
        
        LOGGER.info("TestFX cleanup completed");
    }
    
    /**
     * Waits for a condition to be true with a default timeout.
     * 
     * @param condition the condition to wait for
     */
    protected void waitUntil(Runnable condition) {
        waitUntil(condition, DEFAULT_TIMEOUT);
    }
    
    /**
     * Waits for a condition to be true with a specified timeout.
     * 
     * @param condition the condition to wait for
     * @param timeoutInMillis the timeout in milliseconds
     */
    protected void waitUntil(Runnable condition, int timeoutInMillis) {
        long endTime = System.currentTimeMillis() + timeoutInMillis;
        boolean conditionMet = false;
        
        while (!conditionMet && System.currentTimeMillis() < endTime) {
            try {
                condition.run();
                conditionMet = true;
            } catch (Throwable t) {
                // Wait a bit before trying again
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        if (!conditionMet) {
            throw new RuntimeException("Condition not met within timeout: " + timeoutInMillis + " ms");
        }
    }
    
    /**
     * Takes a screenshot of the primary stage.
     * 
     * @param name the screenshot name
     */
    protected void takeScreenshot(String name) {
        try {
            // Create screenshots directory if it doesn't exist
            java.io.File screenshotsDir = new java.io.File("target/screenshots");
            if (!screenshotsDir.exists()) {
                screenshotsDir.mkdirs();
            }
            
            // Create a file to save the screenshot
            java.io.File file = new java.io.File("target/screenshots/" + name + ".png");
            
            // Get the scene from the primary stage
            Scene scene = primaryStage.getScene();
            if (scene == null) {
                LOGGER.warning("Cannot take screenshot: no scene in primary stage");
                return;
            }
            
            // Take the screenshot
            javafx.scene.image.WritableImage image = new javafx.scene.image.WritableImage(
                (int) scene.getWidth(), 
                (int) scene.getHeight()
            );
            scene.snapshot(image);
            
            // Save the image to a file using the Platform class
            runOnFxThreadAndWait(() -> {
                try {
                    // Use Java's standard ImageIO to write the image
                    // This requires converting from JavaFX image to AWT image
                    java.awt.image.BufferedImage bufferedImage = 
                        new java.awt.image.BufferedImage(
                            (int) image.getWidth(), 
                            (int) image.getHeight(), 
                            java.awt.image.BufferedImage.TYPE_INT_ARGB
                        );
                    
                    // Copy pixel data
                    for (int x = 0; x < image.getWidth(); x++) {
                        for (int y = 0; y < image.getHeight(); y++) {
                            javafx.scene.paint.Color color = image.getPixelReader().getColor(x, y);
                            int argb = (
                                (int) (color.getOpacity() * 255) << 24 |
                                (int) (color.getRed() * 255) << 16 |
                                (int) (color.getGreen() * 255) << 8 |
                                (int) (color.getBlue() * 255)
                            );
                            bufferedImage.setRGB(x, y, argb);
                        }
                    }
                    
                    javax.imageio.ImageIO.write(bufferedImage, "png", file);
                    LOGGER.info("Screenshot saved: " + file.getAbsolutePath());
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Failed to save screenshot", e);
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to take screenshot: " + name, e);
        }
    }
    
    /**
     * Closes and releases all windows created during the test.
     */
    protected void closeAllWindows() {
        // Get all windows
        ObservableList<Window> windows = Window.getWindows();
        
        // Close each window on the JavaFX thread
        runOnFxThreadAndWait(() -> {
            for (Window window : windows) {
                if (window instanceof Stage) {
                    ((Stage) window).close();
                }
            }
        });
        
        // Clear test stages list
        testStages.clear();
    }
}