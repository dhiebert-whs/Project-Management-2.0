package org.frcpm.testfx;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Configuration class for headless TestFX testing in CI/CD environments.
 * This class sets up the system properties needed to run JavaFX tests in headless mode.
 */
public class TestFXHeadlessConfig {
    
    /**
     * Set up the required system properties for headless mode before any tests run.
     */
    @BeforeAll
    public static void setupHeadless() {
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.text", "t2k");
        System.setProperty("java.awt.headless", "true");
        
        // Disable animations to improve test stability
        System.setProperty("javafx.animation.fullspeed", "true");
        
        // Disable focus handling during headless tests
        System.setProperty("testfx.setup.timeout", "2500");
    }
    
    /**
     * Check if running in headless mode.
     * 
     * @return true if running in headless mode, false otherwise
     */
    public static boolean isHeadless() {
        return Boolean.getBoolean("testfx.headless");
    }
}