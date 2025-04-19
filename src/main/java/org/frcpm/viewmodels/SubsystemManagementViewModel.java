package org.frcpm.viewmodels;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.frcpm.binding.Command;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.services.ServiceFactory;
import org.frcpm.services.SubsystemService;
import org.frcpm.services.TaskService;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ViewModel for managing subsystems in a list view.
 * This is a higher-level ViewModel that manages a list of subsystems
 * and provides commands for common operations.
 */
public class SubsystemManagementViewModel extends BaseViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(SubsystemManagementViewModel.class.getName());
    
    // Services
    private final SubsystemService subsystemService;
    private final TaskService taskService;
    
    // Observable properties
    private final ObservableList<Subsystem> subsystems = FXCollections.observableArrayList();
    private final ObjectProperty<Subsystem> selectedSubsystem = new SimpleObjectProperty<>();
    
    // Commands
    private final Command loadSubsystemsCommand;
    private final Command newSubsystemCommand;
    private final Command editSubsystemCommand;
    private final Command deleteSubsystemCommand;
    
    /**
     * Creates a new SubsystemManagementViewModel with default services.
     */
    public SubsystemManagementViewModel() {
        this(
            ServiceFactory.getSubsystemService(),
            ServiceFactory.getTaskService()
        );
    }
    
    /**
     * Creates a new SubsystemManagementViewModel with the specified services.
     * This constructor is mainly used for testing.
     * 
     * @param subsystemService the subsystem service
     * @param taskService the task service
     */
    public SubsystemManagementViewModel(SubsystemService subsystemService, TaskService taskService) {
        this.subsystemService = subsystemService;
        this.taskService = taskService;
        
        // Create commands
        loadSubsystemsCommand = new Command(this::loadSubsystems);
        newSubsystemCommand = new Command(this::newSubsystem);
        editSubsystemCommand = new Command(this::editSubsystem, this::canEditSubsystem);
        deleteSubsystemCommand = new Command(this::deleteSubsystem, this::canDeleteSubsystem);
        
        // Load subsystems
        loadSubsystems();
    }
    
    /**
     * Loads the list of subsystems.
     */
    public void loadSubsystems() {
        try {
            List<Subsystem> subsystemList = subsystemService.findAll();
            if (subsystemList != null) {
                subsystems.clear();
                subsystems.addAll(subsystemList);
            } else {
                LOGGER.warning("Subsystem service returned null list");
                subsystems.clear();
            }
            clearErrorMessage();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading subsystems", e);
            setErrorMessage("Failed to load subsystems: " + e.getMessage());
        }
    }
    
    /**
     * Creates a new subsystem.
     * This is a placeholder for the dialog handler in the controller.
     */
    private void newSubsystem() {
        // Placeholder - dialog will be handled by controller
        LOGGER.info("New subsystem command executed");
    }
    
    /**
     * Edits the selected subsystem.
     * This is a placeholder for the dialog handler in the controller.
     */
    private void editSubsystem() {
        // Placeholder - dialog will be handled by controller
        LOGGER.info("Edit subsystem command executed");
    }
    
    /**
     * Deletes the selected subsystem.
     */
    private void deleteSubsystem() {
        try {
            Subsystem subsystem = selectedSubsystem.get();
            if (subsystem != null) {
                // Check if subsystem has tasks
                List<Task> subsystemTasks = taskService.findBySubsystem(subsystem);
                if (subsystemTasks != null && !subsystemTasks.isEmpty()) {
                    setErrorMessage("Cannot delete a subsystem that has tasks. Reassign or delete tasks first.");
                    return;
                }
                
                boolean deleted = subsystemService.deleteById(subsystem.getId());
                if (deleted) {
                    // Remove from subsystems list
                    subsystems.remove(subsystem);
                    
                    // Clear selection
                    selectedSubsystem.set(null);
                    clearErrorMessage();
                } else {
                    setErrorMessage("Failed to delete subsystem: Operation unsuccessful");
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting subsystem", e);
            setErrorMessage("Failed to delete subsystem: " + e.getMessage());
        }
    }
    
    /**
     * Gets the number of tasks for a subsystem.
     * 
     * @param subsystem the subsystem
     * @return the number of tasks
     */
    public int getTaskCount(Subsystem subsystem) {
        try {
            List<Task> tasks = taskService.findBySubsystem(subsystem);
            return tasks != null ? tasks.size() : 0;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error getting task count", e);
            return 0;
        }
    }
    
    /**
     * Gets the completion percentage for a subsystem.
     * 
     * @param subsystem the subsystem
     * @return the completion percentage (0-100)
     */
    public double getCompletionPercentage(Subsystem subsystem) {
        try {
            List<Task> tasks = taskService.findBySubsystem(subsystem);
            if (tasks == null || tasks.isEmpty()) {
                return 0.0;
            }
            
            int total = tasks.size();
            int completed = (int) tasks.stream().filter(Task::isCompleted).count();
            return total > 0 ? ((double)completed * 100.0 / total) : 0.0;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error getting completion percentage", e);
            return 0.0;
        }
    }
    
    /**
     * Checks if a subsystem can be edited.
     * 
     * @return true if a subsystem is selected, false otherwise
     */
    private boolean canEditSubsystem() {
        return selectedSubsystem.get() != null;
    }
    
    /**
     * Checks if a subsystem can be deleted.
     * 
     * @return true if a subsystem is selected, false otherwise
     */
    private boolean canDeleteSubsystem() {
        return selectedSubsystem.get() != null;
    }
    
    /**
     * Gets the subsystems list.
     * 
     * @return the subsystems list
     */
    public ObservableList<Subsystem> getSubsystems() {
        return subsystems;
    }
    
    /**
     * Gets the selected subsystem.
     * 
     * @return the selected subsystem
     */
    public Subsystem getSelectedSubsystem() {
        return selectedSubsystem.get();
    }
    
    /**
     * Sets the selected subsystem.
     * 
     * @param subsystem the subsystem to select
     */
    public void setSelectedSubsystem(Subsystem subsystem) {
        selectedSubsystem.set(subsystem);
    }
    
    /**
     * Gets the selected subsystem property.
     * 
     * @return the selected subsystem property
     */
    public ObjectProperty<Subsystem> selectedSubsystemProperty() {
        return selectedSubsystem;
    }
    
    /**
     * Gets the load subsystems command.
     * 
     * @return the load subsystems command
     */
    public Command getLoadSubsystemsCommand() {
        return loadSubsystemsCommand;
    }
    
    /**
     * Gets the new subsystem command.
     * 
     * @return the new subsystem command
     */
    public Command getNewSubsystemCommand() {
        return newSubsystemCommand;
    }
    
    /**
     * Gets the add subsystem command.
     * This is an alias for getNewSubsystemCommand() for backward compatibility.
     * 
     * @return the add subsystem command
     */
    public Command getAddSubsystemCommand() {
        return newSubsystemCommand;
    }
    
    /**
     * Gets the edit subsystem command.
     * 
     * @return the edit subsystem command
     */
    public Command getEditSubsystemCommand() {
        return editSubsystemCommand;
    }
    
    /**
     * Gets the delete subsystem command.
     * 
     * @return the delete subsystem command
     */
    public Command getDeleteSubsystemCommand() {
        return deleteSubsystemCommand;
    }
    
    /**
     * Cleans up resources when the ViewModel is no longer needed.
     */
    @Override
    public void cleanupResources() {
        super.cleanupResources();
        // Add any additional cleanup if needed
    }
}