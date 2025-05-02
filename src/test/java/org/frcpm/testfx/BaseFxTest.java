package org.frcpm.testfx;

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
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

/**
 * Base class for TestFX UI tests that sets up the JavaFX environment.
 * All UI tests should extend this class.
 */
@ExtendWith(ApplicationExtension.class)
public abstract class BaseFxTest extends FxRobot {
    
    private static final Logger LOGGER = Logger.getLogger(BaseFxTest.class.getName());
    
    protected Stage stage;

    /**
     * Method called by TestFX to initialize the JavaFX stage.
     * @param stage the primary stage for this test
     */
    @Start
    public void start(Stage stage) {
        this.stage = stage;
        Scene scene = new Scene(new Pane(), 800, 600);
        stage.setScene(scene);
        stage.show();
        
        // Set a reasonable position for the stage
        stage.setX(50);
        stage.setY(50);
        
        // Initialize the application components to test
        try {
            initializeTestComponents(stage);
        } catch (Exception e) {
            LOGGER.severe("Error initializing test components: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @BeforeEach
    public void setUp() {
        // Common setup for all tests - can be overridden by subclasses
    }
    
    @AfterEach
    public void tearDown() {
        // Common teardown for all tests - can be overridden by subclasses
        // Ensure we remove any showing dialogs
        closeAllDialogs();
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
        for (Window window : Window.getWindows()) {
            if (window.isShowing() && window.getScene() != null 
                    && window.getScene().getRoot() instanceof DialogPane) {
                window.hide();
            }
        }
    }
    
    /**
     * Wait for the specified node to be visible.
     * 
     * @param <T> the type of the node
     * @param query the TestFX query to locate the node
     * @return the node when visible
     */
    protected <T extends Node> T waitForNode(String query) {
        return lookup(query).query();
    }
}