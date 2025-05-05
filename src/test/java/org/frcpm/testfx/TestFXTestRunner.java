package org.frcpm.testfx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A test runner for TestFX tests that ensures proper JavaFX toolkit initialization.
 * This class can be used to manually initialize the JavaFX toolkit in test environments.
 */
public class TestFXTestRunner extends Application {
    
    private static final Logger LOGGER = Logger.getLogger(TestFXTestRunner.class.getName());
    private static final CountDownLatch latch = new CountDownLatch(1);
    private static Stage primaryStage;
    
    /**
     * JavaFX start method called by the JavaFX runtime.
     * 
     * @param stage the primary stage
     */
    @Override
    public void start(Stage stage) {
        LOGGER.info("TestFXTestRunner start() method called");
        primaryStage = stage;
        latch.countDown();
    }
    
    /**
     * Launches the JavaFX application and waits for initialization to complete.
     * This method should be called in a @BeforeAll method of a test class.
     * 
     * @return the primary stage
     */
    public static Stage initializeToolkit() {
        LOGGER.info("Initializing JavaFX toolkit...");
        
        // Check if the toolkit is already initialized
        if (Platform.isFxApplicationThread()) {
            LOGGER.info("JavaFX toolkit already initialized on this thread");
            return primaryStage;
        }
        
        try {
            // Start the JavaFX application
            new Thread(() -> {
                try {
                    Application.launch(TestFXTestRunner.class);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Exception launching JavaFX application", e);
                }
            }).start();
            
            // Wait for the application to start
            if (!latch.await(10, TimeUnit.SECONDS)) {
                LOGGER.severe("Timeout waiting for JavaFX toolkit initialization");
                throw new RuntimeException("Timeout waiting for JavaFX toolkit initialization");
            }
            
            LOGGER.info("JavaFX toolkit initialized successfully");
            return primaryStage;
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Interrupted during JavaFX toolkit initialization", e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted during JavaFX toolkit initialization", e);
        }
    }
    
    /**
     * Executes a task on the JavaFX application thread and waits for it to complete.
     * 
     * @param runnable the task to execute
     * @param timeoutMillis the timeout in milliseconds
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
}