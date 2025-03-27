package org.frcpm.binding;

import java.util.function.Supplier;

/**
 * Represents a command that can be executed from the UI.
 * Used to implement the command pattern in the MVVM architecture.
 */
public class Command {
    
    private final Runnable action;
    private final Supplier<Boolean> canExecute;
    
    /**
     * Creates a new command with the specified action.
     * The command can always be executed.
     * 
     * @param action the action to execute
     */
    public Command(Runnable action) {
        this(action, () -> true);
    }
    
    /**
     * Creates a new command with the specified action and condition.
     * The command can only be executed if the condition returns true.
     * 
     * @param action the action to execute
     * @param canExecute the condition that determines if the command can be executed
     */
    public Command(Runnable action, Supplier<Boolean> canExecute) {
        this.action = action;
        this.canExecute = canExecute;
    }
    
    /**
     * Executes the command if it can be executed.
     */
    public void execute() {
        if (canExecute()) {
            action.run();
        }
    }
    
    /**
     * Checks if the command can be executed.
     * 
     * @return true if the command can be executed, false otherwise
     */
    public boolean canExecute() {
        return canExecute.get();
    }
}