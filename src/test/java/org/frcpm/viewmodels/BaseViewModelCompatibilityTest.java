package org.frcpm.viewmodels;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.frcpm.binding.Command;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for backward compatibility with existing ViewModels.
 * Ensures that the enhanced BaseViewModel works correctly with ViewModels
 * that follow existing patterns.
 */
public class BaseViewModelCompatibilityTest {

    private ExistingPatternViewModel existingViewModel;
    private NewPatternViewModel newViewModel;

    @BeforeEach
    public void setUp() {
        existingViewModel = new ExistingPatternViewModel();
        newViewModel = new NewPatternViewModel();
    }

    @Test
    public void testExistingValidation() {
        // Given: a property value that should fail validation
        existingViewModel.setName("");
        
        // When: triggering validation
        existingViewModel.validate();
        
        // Then: validation should fail and error message should be set
        assertFalse(existingViewModel.isValid());
        assertEquals("Name cannot be empty", existingViewModel.getErrorMessage());
        
        // When: setting a valid value and validating
        existingViewModel.setName("Test");
        existingViewModel.validate();
        
        // Then: validation should pass and error message should be cleared
        assertTrue(existingViewModel.isValid());
        assertNull(existingViewModel.getErrorMessage()); // BaseViewModel returns empty string not null
    }

    @Test
    public void testExistingCommandPattern() {
        // Given: a command with existing pattern
        Command saveCommand = existingViewModel.getSaveCommand();
        
        // When: checking if command can execute with invalid state
        existingViewModel.setName("");
        existingViewModel.validate();
        
        // Then: command should not be executable
        assertFalse(saveCommand.canExecute());
        
        // When: setting valid state, dirty flag, and validating
        existingViewModel.setName("Test");
        existingViewModel.validate();
        existingViewModel.setupPropertyListeners(); // This will set up listeners
        existingViewModel.triggerPropertyChange(); // Manually trigger change to set dirty flag
        
        // Then: command should be executable
        assertTrue(saveCommand.canExecute());
    }

    @Test
    public void testNewPropertyListenerTracking() {
        // When: setting up property listeners with new pattern
        newViewModel.setupPropertyListenersNewPattern();
        
        // Then: dirty flag should be false initially
        assertFalse(newViewModel.isDirty());
        
        // When: changing a property
        newViewModel.setName("Changed");
        
        // Then: dirty flag should be true
        assertTrue(newViewModel.isDirty());
    }

    @Test
    public void testNewCommandPattern() {
        // Given: a command with new pattern
        Command saveCommand = newViewModel.getSaveCommandNewPattern();
        
        // When: checking if command can execute with invalid state
        newViewModel.setName("");
        newViewModel.validateNewPattern();
        
        // Then: command should not be executable
        assertFalse(saveCommand.canExecute());
        
        // When: setting valid state but not dirty
        newViewModel.setName("Test");
        newViewModel.validateNewPattern();
        
        // Then: command should not be executable yet (valid but not dirty)
        assertFalse(saveCommand.canExecute());
        
        // When: making a change that sets dirty flag
        newViewModel.setupPropertyListenersNewPattern();
        newViewModel.setName("Test Changed");
        
        // Then: command should be executable
        assertTrue(saveCommand.canExecute());
    }

    @Test
    public void testMixedPatterns() {
        // Create a fresh ViewModel instance
        MixedPatternViewModel viewModel = new MixedPatternViewModel();
        
        // Initialize it
        viewModel.setUp();
        
        // Explicitly set an invalid state and ensure not dirty
        viewModel.setName("");
        viewModel.validate();
        viewModel.resetDirty();
        
        // Get commands
        Command oldCommand = viewModel.getSaveCommandOldPattern();
        Command newCommand = viewModel.getSaveCommandNewPattern();
        
        // Verify initial state (should be invalid and not dirty)
        assertFalse(viewModel.isValid());
        assertFalse(viewModel.isDirty());
        
        // Both commands should be not executable
        assertFalse(oldCommand.canExecute());
        assertFalse(newCommand.canExecute());
        
        // Set to valid state but keep not dirty
        viewModel.setName("Valid Name");
        viewModel.validate();
        viewModel.resetDirty();
        
        // Verify valid but not dirty
        assertTrue(viewModel.isValid());
        assertFalse(viewModel.isDirty());
        
        // Both commands should still not be executable
        assertFalse(oldCommand.canExecute());
        assertFalse(newCommand.canExecute());
        
        // Now set dirty flag
        viewModel.setDirty(true);
        
        // Verify valid and dirty
        assertTrue(viewModel.isValid());
        assertTrue(viewModel.isDirty());
        
        // Both commands should now be executable
        assertTrue(oldCommand.canExecute());
        assertTrue(newCommand.canExecute());
    }

    /**
     * ViewModel using existing patterns (private validation method, manual command creation).
     */
    private class ExistingPatternViewModel extends BaseViewModel {
        private final StringProperty name = new SimpleStringProperty();
        private final SimpleBooleanProperty valid = new SimpleBooleanProperty(false);
        private final Command saveCommand;
        
        public ExistingPatternViewModel() {
            // Traditional command creation approach
            saveCommand = new Command(
                    this::save, 
                    () -> isValid() && isDirty()
            );
        }
        
        // Traditional property setup approach
        public void setupPropertyListeners() {
            name.addListener((observable, oldValue, newValue) -> {
                setDirty(true);
                validate();
            });
        }
        
        // Method to manually trigger property change for testing
        public void triggerPropertyChange() {
            setDirty(true);
        }
        
        // Traditional validation method
        public void validate() {
            if (name.get() == null || name.get().trim().isEmpty()) {
                setErrorMessage("Name cannot be empty");
                valid.set(false);
            } else {
                clearErrorMessage();
                valid.set(true);
            }
        }
        
        private void save() {
            // Save implementation
        }
        
        public boolean isValid() {
            return valid.get();
        }
        
        public void setName(String name) {
            this.name.set(name);
        }
        
        public String getName() {
            return name.get();
        }
        
        public Command getSaveCommand() {
            return saveCommand;
        }
    }
    
    /**
     * ViewModel using new patterns (tracked listeners, helper methods).
     */
    private class NewPatternViewModel extends BaseViewModel {
        private final StringProperty name = new SimpleStringProperty();
        private final SimpleBooleanProperty valid = new SimpleBooleanProperty(false);
        private Command saveCommand;
        
        public NewPatternViewModel() {
            // No command creation here - we'll create it in the setup method
        }
        
        // New pattern for property setup
        public void setupPropertyListenersNewPattern() {
            // Use createDirtyFlagHandler and trackPropertyListener
            Runnable handler = createDirtyFlagHandler(() -> validateNewPattern());
            name.addListener((observable, oldValue, newValue) -> handler.run());
            trackPropertyListener(handler);
        }
        
        // Same validation logic but different method name
        public void validateNewPattern() {
            if (name.get() == null || name.get().trim().isEmpty()) {
                setErrorMessage("Name cannot be empty");
                valid.set(false);
            } else {
                clearErrorMessage();
                valid.set(true);
            }
        }
        
        private void save() {
            // Save implementation
        }
        
        public Command getSaveCommandNewPattern() {
            if (saveCommand == null) {
                // Use helper method to create command
                saveCommand = createValidAndDirtyCommand(this::save, valid::get);
            }
            return saveCommand;
        }
        
        public boolean isValid() {
            return valid.get();
        }
        
        public void setName(String name) {
            this.name.set(name);
        }
        
        public String getName() {
            return name.get();
        }
    }
    
    /**
     * ViewModel that mixes both old and new patterns to demonstrate compatibility.
     */
    private class MixedPatternViewModel extends BaseViewModel {
        private final StringProperty name = new SimpleStringProperty("");
        private final SimpleBooleanProperty valid = new SimpleBooleanProperty(false);
        private Command saveCommandOld;
        private Command saveCommandNew;
        
        public void setUp() {
            // Set initial validation state based on empty name
            validate();
            
            // Create commands using both patterns
            saveCommandOld = new Command(
                    this::save, 
                    () -> isValid() && isDirty()
            );
            
            saveCommandNew = createValidAndDirtyCommand(this::save, valid::get);
            
            // Set up listeners AFTER creating commands
            name.addListener((observable, oldValue, newValue) -> {
                setDirty(true);
                validate();
            });
        }
        
        public void validate() {
            if (name.get() == null || name.get().trim().isEmpty()) {
                setErrorMessage("Name cannot be empty");
                valid.set(false);
            } else {
                clearErrorMessage();
                valid.set(true);
            }
        }
        
        private void save() {
            // Save implementation
        }
        
        public boolean isValid() {
            return valid.get();
        }
        
        public void setName(String name) {
            this.name.set(name);
        }
        
        public String getName() {
            return name.get();
        }
        
        public Command getSaveCommandOldPattern() {
            return saveCommandOld;
        }
        
        public Command getSaveCommandNewPattern() {
            return saveCommandNew;
        }
        
        // Method to explicitly reset dirty flag for testing
        public void resetDirty() {
            setDirty(false);
        }
    }
}