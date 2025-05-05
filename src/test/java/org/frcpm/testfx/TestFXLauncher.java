package org.frcpm.testfx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Launcher class for TestFX tests. This class can be used to manually run TestFX tests
 * outside of the JUnit framework for debugging purposes.
 */
public class TestFXLauncher extends Application {
    
    private static final Logger LOGGER = Logger.getLogger(TestFXLauncher.class.getName());
    private static Properties testfxProperties = new Properties();
    
    static {
        // Load TestFX properties
        try (InputStream is = TestFXLauncher.class.getClassLoader().getResourceAsStream("testfx.properties")) {
            if (is != null) {
                testfxProperties.load(is);
                LOGGER.info("Loaded TestFX properties file");
                
                // Apply properties to system
                for (String name : testfxProperties.stringPropertyNames()) {
                    System.setProperty(name, testfxProperties.getProperty(name));
                }
                
                LOGGER.info("Applied TestFX properties to system");
            } else {
                LOGGER.warning("Could not find testfx.properties file");
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to load TestFX properties", e);
        }
    }
    
    /**
     * JavaFX start method.
     * 
     * @param primaryStage the primary stage
     */
    @Override
    public void start(Stage primaryStage) {
        LOGGER.info("Starting TestFXLauncher...");
        
        StackPane root = new StackPane();
        Scene scene = new Scene(root, 800, 600);
        
        primaryStage.setScene(scene);
        primaryStage.setTitle("TestFX Test Launcher");
        primaryStage.show();
        
        LOGGER.info("TestFXLauncher started");
    }
    
    /**
     * Launch a TestFX test for debugging purposes.
     * 
     * @param testClass the test class to launch
     * @param args command-line arguments
     */
    public static void launchTest(Class<? extends BaseFxTest> testClass, String... args) {
        try {
            LOGGER.info("Launching test class: " + testClass.getName());
            
            // Initialize toolkit
            FxToolkit.registerPrimaryStage();
            
            // Launch application
            launch(args);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to launch test", e);
        }
    }
    
    /**
     * Utility method to run a TestFX test class.
     * 
     * @param testClass the test class to run
     */
    public static void runTest(Class<? extends BaseFxTest> testClass) {
        launchTest(testClass);
    }
    
    /**
     * Helper method to initialize the TestFX environment.
     * This can be called from a test class to ensure the environment is properly set up.
     */
    public static void setupTestFXEnvironment() {
        try {
            // Load properties
            try (InputStream is = TestFXLauncher.class.getClassLoader().getResourceAsStream("testfx.properties")) {
                if (is != null) {
                    Properties props = new Properties();
                    props.load(is);
                    
                    // Apply properties to system
                    for (String name : props.stringPropertyNames()) {
                        System.setProperty(name, props.getProperty(name));
                    }
                }
            }
            
            // Initialize JavaFX toolkit
            if (!Platform.isFxApplicationThread()) {
                TestFXTestRunner.initializeToolkit();
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to setup TestFX environment", e);
        }
    }
    
    /**
     * Helper method to shut down the TestFX environment.
     * This can be called from a test class to clean up after testing.
     */
    public static void tearDownTestFXEnvironment() {
        try {
            // Shut down JavaFX application
            FxToolkit.cleanupStages();
            
            // Give it some time to shut down
            Thread.sleep(100);
            
        } catch (TimeoutException | InterruptedException e) {
            LOGGER.log(Level.WARNING, "Error during TestFX environment teardown", e);
            
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Helper method to set up a test node and return a TestFX application test for it.
     * 
     * @param node the node to test
     * @return a TestFX application test
     */
    public static ApplicationTest setupTestNode(Node node) {
        return new ApplicationTest() {
            @Override
            public void start(Stage stage) {
                Scene scene = new Scene(new StackPane(node), 800, 600);
                stage.setScene(scene);
                stage.show();
            }
        };
    }
    
    /**
     * Execute a TestFX test manually.
     * 
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        LOGGER.info("TestFXLauncher main method started");
        
        // Initialize TestFX environment
        setupTestFXEnvironment();
        
        // Create a standalone test launcher
        launch(args);
        
        // Tear down the environment
        tearDownTestFXEnvironment();
    }
}