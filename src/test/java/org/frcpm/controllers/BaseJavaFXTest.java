package org.frcpm.controllers;

import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

/**
 * Base class for JavaFX tests that handles toolkit initialization.
 */
@ExtendWith(ApplicationExtension.class)
public abstract class BaseJavaFXTest {

    /**
     * Sets up the test environment for headless operation.
     * This is run once before all tests in the class.
     */
    @BeforeAll
    public static void setupHeadless() {
        System.setProperty("java.awt.headless", "true");
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.text", "t2k");
        System.setProperty("glass.platform", "Monocle");
        System.setProperty("monocle.platform", "Headless");
    }

    /**
     * This method will be overridden by subclasses to set up the 
     * JavaFX environment before each test.
     * 
     * @param stage the primary stage for this test
     */
    @Start
    public void start(Stage stage) {
        // This method is required by TestFX
        // Individual test classes can override this method to configure the stage
    }
}
