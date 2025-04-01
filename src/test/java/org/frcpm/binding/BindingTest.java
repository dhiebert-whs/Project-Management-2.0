// src/test/java/org/frcpm/binding/BindingTest.java
package org.frcpm.binding;

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
                () -> canExecuteProperty.get());

        // Set up bindings
        ViewModelBinding.bindCommandButton(button, testCommand);
        ViewModelBinding.bindTextField(textField, textProperty);

        // Create scene
        VBox root = new VBox(10, button, textField);
        Scene scene = new Scene(root, 200, 100);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void testCommandExecution() {
        // Given: Command can execute
        canExecuteProperty.set(true);

        // When: Button is clicked
        clickOn(button);

        // Then: Command should execute
        assertTrue(commandExecuted.get());
    }

    @Test
    public void testCommandCondition() {
        // Given: Command cannot execute
        canExecuteProperty.set(false);
        commandExecuted.set(false);

        // When: Try to click button (it should be disabled)
        // We need to check button state rather than clicking since disabled buttons
        // can't be clicked

        // Then: Button should be disabled
        assertTrue(button.isDisabled());

        // And: Command should not have executed
        assertFalse(commandExecuted.get());
    }

    @Test
    public void testPropertyBinding() {
        // Given: Text property is set programmatically
        textProperty.set("Test Text");

        // Then: TextField should reflect the value
        assertEquals("Test Text", textField.getText());

        // When: TextField is updated programmatically (simulating user input)
        textField.setText("Test Text Modified");

        // Then: Property should reflect the new value
        assertEquals("Test Text Modified", textProperty.get());
    }

    @Test
    public void testCommandWithChangingConditions() {
        // Given: Command initially cannot execute
        canExecuteProperty.set(false);
        commandExecuted.set(false);

        // Then: Button should be disabled
        assertTrue(button.isDisabled());

        // When: Condition changes to allow execution
        canExecuteProperty.set(true);

        // Then: Button should become enabled
        assertFalse(button.isDisabled());

        // When: Button is clicked
        clickOn(button);

        // Then: Command should execute
        assertTrue(commandExecuted.get());
    }
}