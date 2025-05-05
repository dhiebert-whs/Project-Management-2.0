package org.frcpm.testfx;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base class for TestFX UI tests that sets up the JavaFX environment.
 * All UI tests should extend this class.
 */
@ExtendWith(ApplicationExtension.class)
public abstract class BaseFxTest extends FxRobot {
    
    private static final Logger LOGGER = Logger.getLogger(BaseFxTest.class.getName());
    
    protected Stage stage;
    protected boolean initialized = false;

    /**
     * Method called by TestFX to initialize the JavaFX stage.
     * @param stage the primary stage for this test
     */
    @Start
    public void start(Stage stage) {
        this.stage = stage;
        Scene scene = new Scene(new Pane(), 800, 600);
        stage.setScene(scene);
        
        // Set a reasonable position for the stage
        stage.setX(50);
        stage.setY(50);
        
        // Initialize the application components to test
        try {
            LOGGER.info("Initializing test components for stage...");
            
            // Ensure JavaFX toolkit is properly initialized
            ensureToolkitInitialized();
            
            // Initialize on JavaFX thread to prevent threading issues
            Platform.runLater(() -> {
                try {
                    initializeTestComponents(stage);
                    initialized = true;
                    LOGGER.info("Test components initialized successfully");
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error initializing test components on FX thread", e);
                }
            });
            
            // Wait for initialization to complete
            WaitForAsyncUtils.waitForFxEvents();
            
            // Make sure the stage is showing
            stage.show();
            
            // Wait again for any events triggered by showing
            WaitForAsyncUtils.waitForFxEvents();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during start() method", e);
            e.printStackTrace();
        }
    }
    
    /**
     * Ensures that the JavaFX toolkit is initialized.
     */
    private void ensureToolkitInitialized() {
        try {
            // This will ensure the toolkit is initialized
            if (!Platform.isFxApplicationThread()) {
                LOGGER.info("Initializing JavaFX toolkit...");
                
                // Use proper initialization for TestFX
                FxToolkit.registerPrimaryStage();
                FxToolkit.setupApplication(() -> new javafx.application.Application() {
                    @Override
                    public void start(Stage stage) {
                        // Do nothing, just initialize toolkit
                    }
                });
                
                LOGGER.info("JavaFX toolkit initialized successfully");
            }
        } catch (TimeoutException e) {
            LOGGER.log(Level.SEVERE, "Timeout initializing JavaFX toolkit", e);
        }
    }
    
    /**
     * Run a task on the JavaFX application thread and wait for completion.
     * @param runnable The task to run
     */
    protected void runFxThreadAndWait(Runnable runnable) {
        try {
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
                
                // Wait for the task to complete, with timeout
                if (!latch.await(10, TimeUnit.SECONDS)) {
                    LOGGER.warning("Timeout waiting for JavaFX task to complete");
                }
            }
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, "Interrupted while waiting for JavaFX task", e);
            Thread.currentThread().interrupt();
        }
    }
    
    @BeforeEach
    public void setUp() {
        // Common setup for all tests - can be overridden by subclasses
        LOGGER.info("Setting up test...");
        
        // Ensure components are initialized
        if (!initialized) {
            LOGGER.warning("Components weren't initialized properly in start(). Attempting to initialize now...");
            runFxThreadAndWait(() -> {
                initializeTestComponents(stage);
                initialized = true;
            });
        }
    }
    
    @AfterEach
    public void tearDown() {
        // Common teardown for all tests - can be overridden by subclasses
        LOGGER.info("Tearing down test...");
        
        // Ensure we remove any showing dialogs
        closeAllDialogs();
        
        // Clean up any resources
        runFxThreadAndWait(() -> {
            // Clear the scene to release any component references
            if (stage != null && stage.getScene() != null) {
                stage.getScene().setRoot(new Pane());
            }
        });
        
        // Wait for any pending events
        WaitForAsyncUtils.waitForFxEvents();
    }
    
    /**
     * Abstract method to be implemented by test classes to initialize
     * the specific components they need to test.
     * 
     * @param stage the primary stage for this test
     */
    protected abstract void initializeTestComponents(Stage stage);
    
    /**
     * Utility method to lookup a node by its ID.
     * 
     * @param <T> the type of the node
     * @param id the ID of the node to look up
     * @return the node with the given ID
     */
    @SuppressWarnings("unchecked")
    protected <T extends Node> T lookupById(String id) {
        return (T) lookup("#" + id).queryAs(Node.class);
    }
    
    /**
     * Utility method to close any open dialogs.
     */
    protected void closeAllDialogs() {
        runFxThreadAndWait(() -> {
            for (Window window : Window.getWindows()) {
                if (window.isShowing() && window.getScene() != null 
                        && window.getScene().getRoot() instanceof DialogPane) {
                    window.hide();
                }
            }
        });
    }
    
    /**
     * Wait for the specified node to be visible.
     * 
     * @param <T> the type of the node
     * @param query the TestFX query to locate the node
     * @return the node when visible
     */
    protected <T extends Node> T waitForNode(String query) {
        T node = lookup(query).query();
        WaitForAsyncUtils.waitForFxEvents();
        return node;
    }
}