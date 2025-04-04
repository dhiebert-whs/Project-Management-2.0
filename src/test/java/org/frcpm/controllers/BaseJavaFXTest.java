package org.frcpm.controllers;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.AfterEach;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.TimeoutException;

/**
 * Base class for JavaFX test classes.
 * Extends ApplicationTest to provide JavaFX testing capabilities.
 */
public abstract class BaseJavaFXTest extends ApplicationTest {
    
    /**
     * Cleanup after each test method.
     */
    @AfterEach
    public void tearDown() throws TimeoutException {
        // Release all keys and buttons
        release(new KeyCode[]{});
        release(new MouseButton[]{});
        // Close all windows
        FxToolkit.cleanupStages();
        // Wait for any pending JavaFX events to be processed
        WaitForAsyncUtils.waitForFxEvents();
    }
    
    /**
     * Runs the specified action on the JavaFX application thread and waits for completion.
     * 
     * @param action the action to run
     */
    protected void runOnFxThread(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
            WaitForAsyncUtils.waitForFxEvents();
        }
    }
    
    /**
     * Gets a node from a scene with the specified ID.
     * 
     * @param <T> the node type
     * @param scene the scene
     * @param nodeId the node ID
     * @return the node
     */
    @SuppressWarnings("unchecked")
    protected <T extends Node> T getNodeById(Scene scene, String nodeId) {
        return (T) scene.lookup("#" + nodeId);
    }
    
    /**
     * Gets a node from a parent with the specified ID.
     * 
     * @param <T> the node type
     * @param parent the parent
     * @param nodeId the node ID
     * @return the node
     */
    @SuppressWarnings("unchecked")
    protected <T extends Node> T getNodeById(Parent parent, String nodeId) {
        return (T) parent.lookup("#" + nodeId);
    }
    
    /**
     * Gets the current window.
     * 
     * @return the window
     */
    protected Window getCurrentWindow() {
        return targetWindow();
    }
}