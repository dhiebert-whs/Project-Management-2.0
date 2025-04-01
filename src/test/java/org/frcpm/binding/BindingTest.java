// src/test/java/org/frcpm/binding/BindingTest.java
package org.frcpm.binding;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.framework.junit5.Start;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class BindingTest extends ApplicationTest {

    private Button button;
    private TextField textField;
    private BooleanProperty commandExecuted;
    private StringProperty textProperty;
    private BooleanProperty canExecuteProperty;
    private Command testCommand;

    @Start
    public void start(Stage stage) {
        // Create UI components
        button = new Button("Execute");
        textField = new TextField();

        // Create properties and command
        commandExecuted = new SimpleBooleanProperty(false);
        textProperty = new SimpleStringProperty("");
        canExecuteProperty = new SimpleBooleanProperty(true);

        testCommand = new Command(
                () -> commandExecuted.set(true),
                canExecuteProperty::get);

        // Set up bindings
        button.setOnAction(e -> testCommand.execute());
        button.disableProperty().bind(canExecuteProperty.not());
        textField.textProperty().bindBidirectional(textProperty);

        // Create scene
        VBox root = new VBox(10, button, textField);
        Scene scene = new Scene(root, 200, 100);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void testCommandExecution() throws Exception {
        // Given: Command can execute
        runOnFXThreadAndWait(() -> canExecuteProperty.set(true));

        // When: Button is clicked
        runOnFXThreadAndWait(() -> button.fire());

        // Then: Command should execute
        assertTrue(commandExecuted.get());
    }

    @Test
    public void testCommandCondition() throws Exception {
        // Given: Command cannot execute
        runOnFXThreadAndWait(() -> {
            canExecuteProperty.set(false);
            commandExecuted.set(false);
        });

        // Then: Button should be disabled
        assertTrue(button.isDisabled());

        // And: Command should not have executed
        assertFalse(commandExecuted.get());
    }

    @Test
    public void testPropertyBinding() throws Exception {
        // Given: Text property is set programmatically
        runOnFXThreadAndWait(() -> textProperty.set("Test Text"));

        // Then: TextField should reflect the value
        assertEquals("Test Text", textField.getText());

        // When: TextField is updated programmatically (simulating user input)
        runOnFXThreadAndWait(() -> textField.setText("Test Text Modified"));

        // Then: Property should reflect the new value
        assertEquals("Test Text Modified", textProperty.get());
    }

    @Test
    public void testCommandWithChangingConditions() throws Exception {
        // Given: Command initially cannot execute
        runOnFXThreadAndWait(() -> {
            canExecuteProperty.set(false);
            commandExecuted.set(false);
        });

        // Then: Button should be disabled
        assertTrue(button.isDisabled());

        // When: Condition changes to allow execution
        runOnFXThreadAndWait(() -> canExecuteProperty.set(true));

        // Then: Button should become enabled
        assertFalse(button.isDisabled());

        // When: Button is clicked
        runOnFXThreadAndWait(() -> button.fire());

        // Then: Command should execute
        assertTrue(commandExecuted.get());
    }

    /**
     * Utility method to run code on the JavaFX application thread and wait for
     * completion.
     */
    private void runOnFXThreadAndWait(Runnable action) throws Exception {
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                latch.countDown();
            }
        });

        if (!latch.await(5, TimeUnit.SECONDS)) {
            throw new AssertionError("Timeout waiting for JavaFX action to complete");
        }
    }
}