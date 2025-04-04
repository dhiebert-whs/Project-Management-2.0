package org.frcpm.viewmodels;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
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
 * ViewModel for subsystem management in the FRC Project Management System.
 * Following MVVM pattern, this class handles all business logic for the
 * SubsystemManagementController.
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
    private final Command addSubsystemCommand;
    private final Command editSubsystemCommand;
    private final Command deleteSubsystemCommand;

    /**
     * Creates a new SubsystemManagementViewModel with default services.
     */
    public SubsystemManagementViewModel() {
        this(
                ServiceFactory.getSubsystemService(),
                ServiceFactory.getTaskService());
    }

    /**
     * Creates a new SubsystemManagementViewModel with the specified services.
     * This constructor is mainly used for testing.
     * 
     * @param subsystemService the subsystem service
     * @param taskService      the task service
     */
    public SubsystemManagementViewModel(SubsystemService subsystemService, TaskService taskService) {
        this.subsystemService = subsystemService;
        this.taskService = taskService;

        // Initialize error message property
        clearErrorMessage();

        // Initialize commands
        loadSubsystemsCommand = new Command(this::loadSubsystems);
        addSubsystemCommand = new Command(this::addSubsystem);
        editSubsystemCommand = new Command(this::editSubsystem, this::canEditSubsystem);
        deleteSubsystemCommand = new Command(this::deleteSubsystem, this::canDeleteSubsystem);

        // Set up selection listener
        selectedSubsystem.addListener((observable, oldValue, newValue) -> {
            // Update commands that depend on selection
            setDirty(true);
        });

        // Load subsystems initially
        loadSubsystems();
    }

    /**
     * Loads all subsystems from the service.
     */
    private void loadSubsystems() {
        try {
            List<Subsystem> subsystemList = subsystemService.findAll();
            subsystems.clear();
            subsystems.addAll(subsystemList);

            // Clear error message
            clearErrorMessage();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading subsystems", e);
            setErrorMessage("Failed to load subsystems: " + e.getMessage());
        }
    }

    /**
     * Adds a new subsystem - placeholder for dialog handling in controller.
     */
    private void addSubsystem() {
        // This is a placeholder method that will be handled by the controller
        // The controller will show the dialog and then refresh the list
        LOGGER.info("Add subsystem command executed");
    }

    /**
     * Edits the selected subsystem - placeholder for dialog handling in controller.
     */
    private void editSubsystem() {
        // This is a placeholder method that will be handled by the controller
        // The controller will show the dialog and then refresh the list
        LOGGER.info("Edit subsystem command executed for: " + selectedSubsystem.get().getName());
    }

    /**
     * Deletes the selected subsystem after performing validations.
     */
    private void deleteSubsystem() {
        Subsystem subsystem = selectedSubsystem.get();
        if (subsystem == null) {
            return;
        }

        try {
            // Check if subsystem has tasks
            List<Task> tasks = taskService.findBySubsystem(subsystem);
            if (!tasks.isEmpty()) {
                setErrorMessage("Cannot delete a subsystem that has tasks. Reassign or delete the tasks first.");
                return;
            }

            // Delete subsystem
            boolean success = subsystemService.deleteById(subsystem.getId());
            if (success) {
                subsystems.remove(subsystem);
                selectedSubsystem.set(null);
                clearErrorMessage();
            } else {
                setErrorMessage("Failed to delete subsystem.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting subsystem", e);
            setErrorMessage("Failed to delete subsystem: " + e.getMessage());
        }
    }

    /**
     * Checks if the edit subsystem command can be executed.
     * 
     * @return true if a subsystem is selected, false otherwise
     */
    private boolean canEditSubsystem() {
        return selectedSubsystem.get() != null;
    }

    /**
     * Checks if the delete subsystem command can be executed.
     * 
     * @return true if a subsystem is selected, false otherwise
     */
    private boolean canDeleteSubsystem() {
        return selectedSubsystem.get() != null;
    }

    /**
     * Gets the completion percentage for a subsystem based on its tasks.
     * 
     * @param subsystem the subsystem
     * @return the completion percentage (0-100)
     */
    public double getCompletionPercentage(Subsystem subsystem) {
        try {
            List<Task> tasks = taskService.findBySubsystem(subsystem);
            if (tasks.isEmpty()) {
                return 0.0;
            }

            long completed = tasks.stream().filter(Task::isCompleted).count();
            return (double) completed / tasks.size() * 100.0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error calculating completion percentage", e);
            return 0.0;
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
            return tasks.size();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error counting tasks", e);
            return 0;
        }
    }

    // Property accessors

    public ObservableList<Subsystem> getSubsystems() {
        return subsystems;
    }

    public ReadOnlyObjectProperty<Subsystem> selectedSubsystemProperty() {
        return selectedSubsystem;
    }

    public Subsystem getSelectedSubsystem() {
        return selectedSubsystem.get();
    }

    public void setSelectedSubsystem(Subsystem subsystem) {
        selectedSubsystem.set(subsystem);
    }

    // Command accessors

    public Command getLoadSubsystemsCommand() {
        return loadSubsystemsCommand;
    }

    public Command getAddSubsystemCommand() {
        return addSubsystemCommand;
    }

    public Command getEditSubsystemCommand() {
        return editSubsystemCommand;
    }

    public Command getDeleteSubsystemCommand() {
        return deleteSubsystemCommand;
    }
}