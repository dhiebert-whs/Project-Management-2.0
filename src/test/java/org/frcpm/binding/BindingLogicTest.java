// src/test/java/org/frcpm/binding/BindingLogicTest.java
package org.frcpm.binding;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the basic binding logic without UI interaction.
 * This test doesn't require the JavaFX toolkit to be initialized.
 */
public class BindingLogicTest {

    private BooleanProperty commandExecuted;
    private BooleanProperty canExecuteProperty;
    private StringProperty textProperty;
    private Command testCommand;

    @BeforeEach
    public void setUp() {
        commandExecuted = new SimpleBooleanProperty(false);
        canExecuteProperty = new SimpleBooleanProperty(true);
        textProperty = new SimpleStringProperty("");

        testCommand = new Command(
                () -> commandExecuted.set(true),
                canExecuteProperty::get);
    }

    @Test
    public void testCommandExecution() {
        // Given: Command can execute
        canExecuteProperty.set(true);

        // When: Command is executed
        testCommand.execute();

        // Then: Command action should have run
        assertTrue(commandExecuted.get());
    }

    @Test
    public void testCommandBlocked() {
        // Given: Command cannot execute
        canExecuteProperty.set(false);
        commandExecuted.set(false);

        // When: Command is executed
        testCommand.execute();

        // Then: Command action should not have run
        assertFalse(commandExecuted.get());
    }

    @Test
    public void testCanExecuteMethod() {
        // Given: Command with condition

        // When: Condition is true
        canExecuteProperty.set(true);

        // Then: canExecute() should return true
        assertTrue(testCommand.canExecute());

        // When: Condition is false
        canExecuteProperty.set(false);

        // Then: canExecute() should return false
        assertFalse(testCommand.canExecute());
    }

    @Test
    public void testCommandWithoutCondition() {
        // Given: Command without condition (always executable)
        Command unconditionalCommand = new Command(() -> commandExecuted.set(true));

        // Then: canExecute() should return true
        assertTrue(unconditionalCommand.canExecute());

        // When: Command is executed
        unconditionalCommand.execute();

        // Then: Command action should have run
        assertTrue(commandExecuted.get());
    }
}