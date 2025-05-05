package org.frcpm.testfx;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.condition.EnabledIf;
import org.testfx.api.FxRobot;

import java.awt.GraphicsEnvironment;
import java.util.logging.Logger;

/**
 * Configuration class for headless TestFX testing in CI/CD environments.
 * This class sets up the system properties needed to run JavaFX tests in headless mode.
 */
public class TestFXHeadlessConfig {
    
    private static final Logger LOGGER = Logger.getLogger(TestFXHeadlessConfig.class.getName());
    
    /**
     * Set up the required system properties for headless mode before any tests run.
     */
    @BeforeAll
    public static void setupHeadless() {
        if (shouldRunHeadless()) {
            LOGGER.info("Configuring headless mode for TestFX...");
            
            // Basic TestFX headless settings
            System.setProperty("testfx.robot", "glass");
            System.setProperty("testfx.headless", "true");
            System.setProperty("prism.order", "sw");
            System.setProperty("prism.text", "t2k");
            System.setProperty("java.awt.headless", "true");
            
            // Monocle settings for OpenJFX
            System.setProperty("glass.platform", "Monocle");
            System.setProperty("monocle.platform", "Headless");
            
            // Disable animations to improve test stability
            System.setProperty("javafx.animation.fullspeed", "true");
            
            // Timeout and error reporting settings
            System.setProperty("testfx.setup.timeout", "5000");
            System.setProperty("testfx.verbose", "true");
            
            LOGGER.info("Headless mode configuration complete");
        } else {
            LOGGER.info("Running in non-headless mode");
        }
    }
    
    /**
     * Determine if tests should run in headless mode.
     * This will return true if:
     * 1. The "testfx.headless" property is explicitly set to "true"
     * 2. OR we're in a headless environment (CI/CD)
     * 
     * @return true if tests should run in headless mode
     */
    public static boolean shouldRunHeadless() {
        // Check if headless mode is explicitly requested
        boolean explicitHeadless = Boolean.getBoolean("testfx.headless");
        
        // Check if we're in a headless environment
        boolean isHeadlessEnvironment = GraphicsEnvironment.isHeadless() || 
                System.getenv("CI") != null || 
                System.getenv("CONTINUOUS_INTEGRATION") != null;
        
        // Return true if either condition is true
        return explicitHeadless || isHeadlessEnvironment;
    }
    
    /**
     * Annotation helper to enable tests only in headless mode.
     * Usage: @EnabledIf("org.frcpm.testfx.TestFXHeadlessConfig#isHeadless")
     * 
     * @return true if running in headless mode
     */
    public static boolean isHeadless() {
        return Boolean.getBoolean("testfx.headless");
    }
    
    /**
     * Annotation helper to enable tests only in non-headless mode.
     * Usage: @EnabledIf("org.frcpm.testfx.TestFXHeadlessConfig#isNonHeadless")
     * 
     * @return true if running in non-headless mode
     */
    public static boolean isNonHeadless() {
        return !isHeadless();
    }
    
    /**
     * Configure robot speed for tests. Useful to slow down tests for debugging.
     * 
     * @param robot the TestFX robot to configure
     * @param millis the delay in milliseconds between robot actions
     */
    public static void configureRobotDelay(FxRobot robot, int millis) {
        // Set sleep time between robot actions - useful for debugging
        if (!isHeadless() && millis > 0) {
            robot.sleep(1); // Initial sleep to ensure robot is ready
        }
    }
}