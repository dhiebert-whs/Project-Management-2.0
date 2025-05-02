// src/test/java/org/frcpm/viewmodels/BaseViewModelEnhancedTest.java
package org.frcpm.viewmodels;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.atomic.AtomicBoolean;

import org.frcpm.binding.Command;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BaseViewModelEnhancedTest {
    
    private TestViewModel viewModel;
    
    @Mock
    private Runnable mockListener;
    
    @BeforeEach
    public void setUp() {
        viewModel = new TestViewModel();
    }
    
    @Test
    public void testPropertyListenerTracking() {
        // Act
        viewModel.testTrackPropertyListener(mockListener);
        viewModel.cleanupResources();
        
        // Assert - test that the listener was tracked and cleaned up
        // We can't directly verify this, but we can test that the proper methods were called
        verify(mockListener, never()).run(); // Should never call the listener
    }
    
    @Test
    public void testDirtyFlagHandler() {
        // Arrange
        AtomicBoolean callbackCalled = new AtomicBoolean(false);
        Runnable callback = () -> callbackCalled.set(true);
        
        // Act
        Runnable handler = viewModel.testCreateDirtyFlagHandler(callback);
        assertFalse(viewModel.isDirty(), "ViewModel should not be dirty initially");
        
        handler.run();
        
        // Assert
        assertTrue(viewModel.isDirty(), "ViewModel should be marked as dirty");
        assertTrue(callbackCalled.get(), "Callback should have been called");
    }
    
    @Test
    public void testValidOnlyCommand() {
        // Arrange
        AtomicBoolean actionCalled = new AtomicBoolean(false);
        AtomicBoolean isValid = new AtomicBoolean(true);
        
        Runnable action = () -> actionCalled.set(true);
        
        // Act
        Command command = viewModel.testCreateValidOnlyCommand(
            action, () -> isValid.get());
        
        // Assert - when valid
        assertTrue(command.canExecute(), "Command should be executable when valid");
        command.execute();
        assertTrue(actionCalled.get(), "Action should be called when command is valid");
        
        // Reset and test when invalid
        actionCalled.set(false);
        isValid.set(false);
        
        assertFalse(command.canExecute(), "Command should not be executable when invalid");
        command.execute(); // Should not execute the action
        assertFalse(actionCalled.get(), "Action should not be called when command is invalid");
    }
    
    @Test
    public void testValidAndDirtyCommand() {
        // Arrange
        AtomicBoolean actionCalled = new AtomicBoolean(false);
        AtomicBoolean isValid = new AtomicBoolean(true);
        
        Runnable action = () -> actionCalled.set(true);
        
        // Act
        Command command = viewModel.testCreateValidAndDirtyCommand(
            action, () -> isValid.get());
        
        // Assert - when valid but not dirty
        viewModel.setTestDirty(false);
        assertFalse(command.canExecute(), "Command should not be executable when not dirty");
        command.execute(); // Should not execute
        assertFalse(actionCalled.get(), "Action should not be called when not dirty");
        
        // When valid and dirty
        viewModel.setTestDirty(true);
        assertTrue(command.canExecute(), "Command should be executable when valid and dirty");
        command.execute();
        assertTrue(actionCalled.get(), "Action should be called when valid and dirty");
        
        // When invalid but dirty
        actionCalled.set(false);
        isValid.set(false);
        assertFalse(command.canExecute(), "Command should not be executable when invalid");
        command.execute(); // Should not execute
        assertFalse(actionCalled.get(), "Action should not be called when invalid");
    }
    
    // Test helper class that extends BaseViewModel for testing
    private static class TestViewModel extends BaseViewModel {
        
        // Expose protected methods for testing
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
        
        public void setTestDirty(boolean dirty) {
            setDirty(dirty);
        }
    }
}