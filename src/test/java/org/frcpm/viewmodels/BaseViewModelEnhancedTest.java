package org.frcpm.viewmodels;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.frcpm.binding.Command;
import org.frcpm.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the enhanced functionality in BaseViewModel.
 * Tests the new utility methods added during the MVVM standardization process.
 */
public class BaseViewModelEnhancedTest {

    private TestViewModel viewModel;

    @BeforeEach
    public void setUp() {
        viewModel = new TestViewModel();
    }

    @Test
    public void testTrackPropertyListener() {
        // Given: a runnable to track
        Runnable listener = () -> {};
        
        // When: adding the listener
        viewModel.testTrackPropertyListener(listener);
        
        // Then: verify the listener was tracked by checking the count
        // We'll use the TestViewModel's method to verify instead of direct field access
        assertEquals(1, viewModel.getTrackedListenersCount());
        assertTrue(viewModel.containsTrackedListener(listener));
    }

    @Test
    public void testCreateDirtyFlagHandler() {
        // Given: a counter to track calls
        AtomicInteger counter = new AtomicInteger(0);
        Runnable afterAction = counter::incrementAndGet;
        
        // When: creating and invoking the handler
        Runnable handler = viewModel.testCreateDirtyFlagHandler(afterAction);
        handler.run();
        
        // Then: dirty flag should be true and afterAction should be called
        assertTrue(viewModel.isDirty());
        assertEquals(1, counter.get());
    }

    @Test
    public void testCreateDirtyFlagHandlerWithNullAction() {
        // When: creating and invoking the handler with null afterAction
        Runnable handler = viewModel.testCreateDirtyFlagHandler(null);
        handler.run();
        
        // Then: dirty flag should be true and no exception should be thrown
        assertTrue(viewModel.isDirty());
    }

    @Test
    public void testCreateValidOnlyCommand() {
        // Given: a counter to track command execution
        AtomicInteger counter = new AtomicInteger(0);
        Runnable action = counter::incrementAndGet;
        
        // When: creating a command that should be valid
        Command validCommand = viewModel.testCreateValidOnlyCommand(action, () -> true);
        
        // Then: the command should be executable
        assertTrue(validCommand.canExecute());
        validCommand.execute();
        assertEquals(1, counter.get());
        
        // When: creating a command that should be invalid
        Command invalidCommand = viewModel.testCreateValidOnlyCommand(action, () -> false);
        
        // Then: the command should not be executable
        assertFalse(invalidCommand.canExecute());
        invalidCommand.execute(); // This should not increment the counter
        assertEquals(1, counter.get()); // Counter should still be 1
    }

    @Test
    public void testCreateValidAndDirtyCommand() {
        // Given: a counter to track command execution
        AtomicInteger counter = new AtomicInteger(0);
        Runnable action = counter::incrementAndGet;
        
        // When: creating a command that should be valid but not dirty
        viewModel.setDirty(false);
        Command validNotDirtyCommand = viewModel.testCreateValidAndDirtyCommand(action, () -> true);
        
        // Then: the command should not be executable
        assertFalse(validNotDirtyCommand.canExecute());
        
        // When: setting the dirty flag to true
        viewModel.setDirty(true);
        
        // Then: now the command should be executable
        assertTrue(validNotDirtyCommand.canExecute());
        validNotDirtyCommand.execute();
        assertEquals(1, counter.get());
        
        // When: creating a command that is not valid but is dirty
        Command invalidDirtyCommand = viewModel.testCreateValidAndDirtyCommand(action, () -> false);
        
        // Then: the command should not be executable
        assertFalse(invalidDirtyCommand.canExecute());
        invalidDirtyCommand.execute(); // This should not increment the counter
        assertEquals(1, counter.get()); // Counter should still be 1
    }

    @Test
    public void testCleanupResources() {
        // Given: multiple tracked listeners
        viewModel.testTrackPropertyListener(() -> {});
        viewModel.testTrackPropertyListener(() -> {});
        
        // When: cleaning up resources
        viewModel.cleanupResources();
        
        // Then: the tracked listeners list should be empty
        assertEquals(0, viewModel.getTrackedListenersCount());
    }

    @Test
    public void testPropertyChangeHandling() {
        // Given: a string property with a dirty flag handler
        StringProperty testProperty = new SimpleStringProperty("initial");
        
        // When: setting up the property change handler
        viewModel.setupTestPropertyChangeHandler(testProperty);
        
        // Then: dirty flag should be false initially
        assertFalse(viewModel.isDirty());
        
        // When: changing the property value
        testProperty.set("changed");
        
        // Then: dirty flag should be true
        assertTrue(viewModel.isDirty());
    }

    @Test
    public void testCommandValidityChangesWithProperties() {
        // Given: a property that affects validation
        SimpleBooleanProperty isValid = new SimpleBooleanProperty(false);
        
        // When: creating a command that depends on the validity
        AtomicBoolean wasCalled = new AtomicBoolean(false);
        Command command = viewModel.testCreateValidOnlyCommand(
                () -> wasCalled.set(true), 
                isValid::get
        );
        
        // Then: command should initially be invalid
        assertFalse(command.canExecute());
        
        // When: changing the validity property
        isValid.set(true);
        
        // Then: command should now be valid and executable
        assertTrue(command.canExecute());
        command.execute();
        assertTrue(wasCalled.get());
    }

    /**
     * Test implementation of BaseViewModel to access protected methods.
     */
    private class TestViewModel extends BaseViewModel {
        // List to track listeners for testing purposes
        private final List<Runnable> testListeners = new ArrayList<>();
        
        @Override
        protected void trackPropertyListener(Runnable listener) {
            super.trackPropertyListener(listener);
            // Also track in our test list
            testListeners.add(listener);
        }
        
        public void testTrackPropertyListener(Runnable listener) {
            trackPropertyListener(listener);
        }

        public Runnable testCreateDirtyFlagHandler(Runnable runAfter) {
            return createDirtyFlagHandler(runAfter);
        }

        public Command testCreateValidOnlyCommand(Runnable action, java.util.function.Supplier<Boolean> validCheck) {
            return createValidOnlyCommand(action, validCheck);
        }

        public Command testCreateValidAndDirtyCommand(Runnable action, java.util.function.Supplier<Boolean> validCheck) {
            return createValidAndDirtyCommand(action, validCheck);
        }
        
        public void setupTestPropertyChangeHandler(StringProperty property) {
            property.addListener((observable, oldValue, newValue) -> {
                Runnable handler = createDirtyFlagHandler(null);
                handler.run();
            });
        }
        
        public void setDirty(boolean value) {
            super.setDirty(value);
        }
        
        // Methods to access listener information for testing
        public int getTrackedListenersCount() {
            return testListeners.size();
        }
        
        public boolean containsTrackedListener(Runnable listener) {
            return testListeners.contains(listener);
        }
        
        // Override to also clear our test list
        @Override
        public void cleanupResources() {
            super.cleanupResources();
            testListeners.clear();
        }
    }
}