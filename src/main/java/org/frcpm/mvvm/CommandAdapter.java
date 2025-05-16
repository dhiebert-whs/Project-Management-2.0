// src/main/java/org/frcpm/mvvm/CommandAdapter.java

package org.frcpm.mvvm;

import de.saxsys.mvvmfx.utils.commands.Command;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.Button;

import java.util.function.Supplier;

/**
 * Adapter class that bridges between the application's existing Command system and MVVMFx's Command system.
 * Allows existing commands to be used with MVVMFx and MVVMFx commands to be used with the existing UI binding.
 */
public class CommandAdapter {
    
    /**
     * Converts an existing org.frcpm.binding.Command to a MVVMFx Command.
     * 
     * @param existingCommand the existing command
     * @return a MVVMFx command that delegates to the existing command
     */
    public static Command adapt(org.frcpm.binding.Command existingCommand) {
        return new ExistingCommandAdapter(existingCommand);
    }
    
    /**
     * Converts a MVVMFx Command to an existing org.frcpm.binding.Command.
     * 
     * @param mvvmfxCommand the MVVMFx command
     * @return an existing command that delegates to the MVVMFx command
     */
    public static org.frcpm.binding.Command adaptBack(Command mvvmfxCommand) {
        return new org.frcpm.binding.Command(
            () -> mvvmfxCommand.execute(),
            () -> mvvmfxCommand.isExecutable()
        );
    }
    
    /**
     * Binds a Button to a MVVMFx Command using the existing ViewModelBinding.
     * 
     * @param button the button to bind
     * @param mvvmfxCommand the MVVMFx command
     */
    public static void bindCommandButton(Button button, Command mvvmfxCommand) {
        org.frcpm.binding.ViewModelBinding.bindCommandButton(button, adaptBack(mvvmfxCommand));
    }
    
    /**
     * Adapter implementation that wraps an existing command to provide the MVVMFx Command interface.
     */
    private static class ExistingCommandAdapter implements Command {
        private final org.frcpm.binding.Command command;
        private final BooleanProperty runningProperty = new SimpleBooleanProperty(false);
        private final BooleanProperty executableProperty = new SimpleBooleanProperty();
        private final ReadOnlyBooleanWrapper notRunningProperty = new ReadOnlyBooleanWrapper();
        private final ReadOnlyBooleanWrapper notExecutableProperty = new ReadOnlyBooleanWrapper();
        private final SimpleDoubleProperty progressProperty = new SimpleDoubleProperty(0.0);
        
        public ExistingCommandAdapter(org.frcpm.binding.Command command) {
            this.command = command;
            this.executableProperty.set(command.canExecute());
            
            // Set up bindings for not-properties
            this.runningProperty.addListener((obs, oldVal, newVal) -> {
                notRunningProperty.set(!newVal);
            });
            
            this.executableProperty.addListener((obs, oldVal, newVal) -> {
                notExecutableProperty.set(!newVal);
            });
            
            // Initialize not-properties
            this.notRunningProperty.set(!this.runningProperty.get());
            this.notExecutableProperty.set(!this.executableProperty.get());
        }
        
        @Override
        public void execute() {
            if (isExecutable()) {
                runningProperty.set(true);
                try {
                    command.execute();
                } finally {
                    runningProperty.set(false);
                    // Update executable state after execution
                    executableProperty.set(command.canExecute());
                }
            }
        }
        
        @Override
        public ReadOnlyBooleanProperty executableProperty() {
            // Ensure the property is updated with the current state
            executableProperty.set(command.canExecute());
            return executableProperty;
        }
        
        @Override
        public boolean isExecutable() {
            return command.canExecute() && !isRunning();
        }
        
        @Override
        public ReadOnlyBooleanProperty runningProperty() {
            return runningProperty;
        }
        
        @Override
        public boolean isRunning() {
            return runningProperty.get();
        }

        @Override
        public boolean isNotExecutable() {
            return !isExecutable();
        }

        @Override
        public ReadOnlyBooleanProperty notExecutableProperty() {
            return notExecutableProperty.getReadOnlyProperty();
        }

        @Override
        public boolean isNotRunning() {
            return !isRunning();
        }

        @Override
        public ReadOnlyBooleanProperty notRunningProperty() {
            return notRunningProperty.getReadOnlyProperty();
        }

        @Override
        public double getProgress() {
            return progressProperty.get();
        }

        @Override
        public ReadOnlyDoubleProperty progressProperty() {
            return progressProperty;
        }
    }
}