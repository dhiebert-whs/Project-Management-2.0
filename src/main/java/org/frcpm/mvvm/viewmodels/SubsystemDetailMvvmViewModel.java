// src/main/java/org/frcpm/mvvm/viewmodels/SubsystemDetailMvvmViewModel.java
package org.frcpm.mvvm.viewmodels;

import de.saxsys.mvvmfx.utils.commands.Command;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.models.Subteam;
import org.frcpm.mvvm.BaseMvvmViewModel;
import org.frcpm.mvvm.async.MvvmAsyncHelper;
import org.frcpm.services.SubsystemService;
import org.frcpm.services.SubteamService;
import org.frcpm.services.TaskService;
import org.frcpm.services.impl.SubsystemServiceAsyncImpl;
import org.frcpm.services.impl.SubteamServiceAsyncImpl;
import org.frcpm.services.impl.TaskServiceAsyncImpl;

/**
 * ViewModel for the SubsystemDetail view using MVVMFx.
 */
public class SubsystemDetailMvvmViewModel extends BaseMvvmViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(SubsystemDetailMvvmViewModel.class.getName());
    
    // Service dependencies
    private final SubsystemService subsystemService;
    private final SubsystemServiceAsyncImpl subsystemServiceAsync;
    private final SubteamService subteamService;
    private final SubteamServiceAsyncImpl subteamServiceAsync;
    private final TaskService taskService;
    private final TaskServiceAsyncImpl taskServiceAsync;
    
    // Observable properties for subsystem fields
    private final StringProperty subsystemName = new SimpleStringProperty("");
    private final StringProperty subsystemDescription = new SimpleStringProperty("");
    private final ObjectProperty<Subsystem.Status> status = new SimpleObjectProperty<>(Subsystem.Status.NOT_STARTED);
    private final ObjectProperty<Subteam> responsibleSubteam = new SimpleObjectProperty<>();
    private final BooleanProperty isNewSubsystem = new SimpleBooleanProperty(true);
    private final BooleanProperty valid = new SimpleBooleanProperty(false);
    private final ObjectProperty<Subsystem> subsystem = new SimpleObjectProperty<>();
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    
    // Properties for tasks
    private final ObservableList<Task> tasks = FXCollections.observableArrayList();
    private final ObjectProperty<Task> selectedTask = new SimpleObjectProperty<>();
    private final IntegerProperty totalTasks = new SimpleIntegerProperty(0);
    private final IntegerProperty completedTasks = new SimpleIntegerProperty(0);
    private final DoubleProperty completionPercentage = new SimpleDoubleProperty(0.0);
    
    // Lists for UI
    private final ObservableList<Subsystem.Status> statusOptions = FXCollections.observableArrayList(Subsystem.Status.values());
    private final ObservableList<Subteam> availableSubteams = FXCollections.observableArrayList();
    
    // Commands
    private Command saveCommand;
    private Command cancelCommand;
    private Command addTaskCommand;
    private Command viewTaskCommand;
    
    /**
     * Creates a new SubsystemDetailMvvmViewModel.
     * 
     * @param subsystemService the subsystem service
     * @param subteamService the subteam service
     * @param taskService the task service
     */
    @Inject
    public SubsystemDetailMvvmViewModel(
            SubsystemService subsystemService, 
            SubteamService subteamService,
            TaskService taskService) {
        this.subsystemService = subsystemService;
        this.subsystemServiceAsync = (SubsystemServiceAsyncImpl) subsystemService;
        this.subteamService = subteamService;
        this.subteamServiceAsync = (SubteamServiceAsyncImpl) subteamService;
        this.taskService = taskService;
        this.taskServiceAsync = (TaskServiceAsyncImpl) taskService;
        
        initializeCommands();
        setupValidation();
        loadSubteams();
    }
    
    /**
     * Initializes the commands for this view model.
     */
    private void initializeCommands() {
        // Save command
        saveCommand = createValidAndDirtyCommand(this::save, this::isValid);
        
        // Cancel command - implemented by the view to close dialog
        cancelCommand = MvvmAsyncHelper.createSimpleAsyncCommand(() -> {
            LOGGER.info("Cancel command executed");
        });
        
        // Add task command
        addTaskCommand = createValidOnlyCommand(
            () -> {
                // This will be handled by the view to show dialog
                LOGGER.info("Add task command executed");
            },
            this::canAddTask
        );
        
        // View task command
        viewTaskCommand = createValidOnlyCommand(
            () -> {
                // This will be handled by the view to show dialog
                LOGGER.info("View task command executed");
            },
            () -> selectedTask.get() != null
        );
    }
    
    /**
     * Sets up validation for this view model.
     */
    private void setupValidation() {
        // Validation listener for all required fields
        Runnable validateAndMarkDirty = createDirtyFlagHandler(this::validate);
        
        // Add listeners to properties that affect validation
        subsystemName.addListener((obs, oldVal, newVal) -> validateAndMarkDirty.run());
        status.addListener((obs, oldVal, newVal) -> validateAndMarkDirty.run());
        
        // Add listeners to properties that don't affect validation but mark as dirty
        subsystemDescription.addListener((obs, oldVal, newVal) -> setDirty(true));
        responsibleSubteam.addListener((obs, oldVal, newVal) -> setDirty(true));
        
        // Initial validation
        validate();
    }
    
    /**
     * Validates the subsystem data.
     * Sets the valid property and error message accordingly.
     */
    private void validate() {
        List<String> errors = new ArrayList<>();
        
        // Check required fields
        if (subsystemName.get() == null || subsystemName.get().trim().isEmpty()) {
            errors.add("Subsystem name is required");
        }
        
        if (status.get() == null) {
            errors.add("Status is required");
        }
        
        // Update valid property and error message
        valid.set(errors.isEmpty());
        if (!errors.isEmpty()) {
            setErrorMessage(String.join("\n", errors));
        } else {
            clearErrorMessage();
        }
    }
    
    /**
     * Loads available subteams.
     */
    private void loadSubteams() {
        loading.set(true);
        
        subteamServiceAsync.findAllAsync(
            // Success callback
            result -> {
                Platform.runLater(() -> {
                    availableSubteams.clear();
                    availableSubteams.addAll(result);
                    loading.set(false);
                    LOGGER.info("Loaded " + result.size() + " subteams asynchronously");
                });
            },
            // Error callback
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error loading subteams asynchronously", error);
                    loading.set(false);
                });
            }
        );
    }
    
    /**
     * Loads tasks for the current subsystem.
     */
    private void loadTasks() {
        if (subsystem.get() == null || subsystem.get().getId() == null) {
            tasks.clear();
            totalTasks.set(0);
            completedTasks.set(0);
            completionPercentage.set(0.0);
            return;
        }
        
        loading.set(true);
        
        // For now, we'll refresh the subsystem to get the latest tasks
        subsystemServiceAsync.findByIdAsync(
            subsystem.get().getId(),
            // Success callback
            refreshedSubsystem -> {
                Platform.runLater(() -> {
                    if (refreshedSubsystem != null) {
                        subsystem.set(refreshedSubsystem);
                        
                        // Update tasks list
                        tasks.clear();
                        if (refreshedSubsystem.getTasks() != null) {
                            tasks.addAll(refreshedSubsystem.getTasks());
                        }
                        
                        // Update statistics
                        updateTaskStatistics();
                    }
                    loading.set(false);
                });
            },
            // Error callback
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error loading tasks for subsystem asynchronously", error);
                    setErrorMessage("Failed to load tasks: " + error.getMessage());
                    loading.set(false);
                });
            }
        );
    }
    
    /**
     * Updates task statistics.
     */
    private void updateTaskStatistics() {
        int total = tasks.size();
        int completed = 0;
        int totalProgress = 0;
        
        for (Task task : tasks) {
            if (task.isCompleted()) {
                completed++;
            }
            totalProgress += task.getProgress();
        }
        
        double completion = total > 0 ? (double) totalProgress / total : 0.0;
        
        totalTasks.set(total);
        completedTasks.set(completed);
        completionPercentage.set(completion);
    }
    
    /**
     * Initializes the view model for a new subsystem.
     */
    public void initNewSubsystem() {
        // Create a new subsystem
        Subsystem newSubsystem = new Subsystem();
        subsystem.set(newSubsystem);
        isNewSubsystem.set(true);
        
        // Reset properties
        subsystemName.set("");
        subsystemDescription.set("");
        status.set(Subsystem.Status.NOT_STARTED);
        responsibleSubteam.set(null);
        
        // Clear tasks
        tasks.clear();
        totalTasks.set(0);
        completedTasks.set(0);
        completionPercentage.set(0.0);
        
        // Clear dirty flag
        setDirty(false);
        
        // Clear error message
        clearErrorMessage();
        
        // Validate
        validate();
    }
    
    /**
     * Initializes the view model for editing an existing subsystem.
     * 
     * @param subsystem the subsystem to edit
     */
    public void initExistingSubsystem(Subsystem subsystem) {
        if (subsystem == null) {
            throw new IllegalArgumentException("Subsystem cannot be null");
        }
        
        this.subsystem.set(subsystem);
        isNewSubsystem.set(false);
        
        // Set properties from subsystem
        subsystemName.set(subsystem.getName());
        subsystemDescription.set(subsystem.getDescription());
        status.set(subsystem.getStatus());
        responsibleSubteam.set(subsystem.getResponsibleSubteam());
        
        // Load tasks
        loadTasks();
        
        // Clear dirty flag
        setDirty(false);
        
        // Clear error message
        clearErrorMessage();
        
        // Validate
        validate();
    }
    
    /**
     * Saves the subsystem.
     */
    private void save() {
        if (!valid.get() || !isDirty()) {
            return;
        }
        
        loading.set(true);
        
        try {
            // Get the current subsystem
            Subsystem subsystemToSave = subsystem.get();
            if (subsystemToSave == null) {
                subsystemToSave = new Subsystem();
            }
            
            // Update subsystem from properties
            subsystemToSave.setName(subsystemName.get());
            subsystemToSave.setDescription(subsystemDescription.get());
            subsystemToSave.setStatus(status.get());
            subsystemToSave.setResponsibleSubteam(responsibleSubteam.get());
            
            // Save the subsystem asynchronously
            subsystemServiceAsync.saveAsync(
                subsystemToSave,
                // Success callback
                savedSubsystem -> {
                    Platform.runLater(() -> {
                        subsystem.set(savedSubsystem);
                        setDirty(false);
                        isNewSubsystem.set(false);
                        loading.set(false);
                        LOGGER.info("Subsystem saved successfully: " + savedSubsystem.getName());
                    });
                },
                // Error callback
                error -> {
                    Platform.runLater(() -> {
                        LOGGER.log(Level.SEVERE, "Error saving subsystem", error);
                        setErrorMessage("Failed to save subsystem: " + error.getMessage());
                        loading.set(false);
                    });
                }
            );
        } catch (Exception e) {
            loading.set(false);
            LOGGER.log(Level.SEVERE, "Error in save method", e);
            setErrorMessage("Failed to save subsystem: " + e.getMessage());
        }
    }
    
    /**
     * Checks if a task can be added to the subsystem.
     * 
     * @return true if a task can be added, false otherwise
     */
    private boolean canAddTask() {
        // Can only add tasks to saved subsystems
        return subsystem.get() != null && subsystem.get().getId() != null && !loading.get();
    }
    
    /**
     * Gets the value of the valid property.
     * 
     * @return true if the subsystem is valid, false otherwise
     */
    public boolean isValid() {
        return valid.get();
    }
    
    /**
     * Gets the valid property.
     * 
     * @return the valid property
     */
    public BooleanProperty validProperty() {
        return valid;
    }
    
    /**
     * Gets the loading property.
     * 
     * @return the loading property
     */
    public BooleanProperty loadingProperty() {
        return loading;
    }
    
    /**
     * Gets whether the view model is loading data.
     * 
     * @return true if loading, false otherwise
     */
    public boolean isLoading() {
        return loading.get();
    }
    
    /**
     * Gets the subsystem.
     * 
     * @return the subsystem
     */
    public Subsystem getSelectedSubsystem() {
        return subsystem.get();
    }
    
    /**
     * Gets the subsystem property.
     * 
     * @return the subsystem property
     */
    public ObjectProperty<Subsystem> subsystemProperty() {
        return subsystem;
    }
    
    /**
     * Gets whether this is a new subsystem.
     * 
     * @return true if this is a new subsystem, false if editing an existing subsystem
     */
    public boolean isNewSubsystem() {
        return isNewSubsystem.get();
    }
    
    /**
     * Gets the new subsystem property.
     * 
     * @return the new subsystem property
     */
    public BooleanProperty isNewSubsystemProperty() {
        return isNewSubsystem;
    }
    
    /**
     * Gets the tasks list.
     * 
     * @return the tasks list
     */
    public ObservableList<Task> getTasks() {
        return tasks;
    }
    
    /**
     * Gets the selected task property.
     * 
     * @return the selected task property
     */
    public ObjectProperty<Task> selectedTaskProperty() {
        return selectedTask;
    }
    
    /**
     * Gets the selected task.
     * 
     * @return the selected task
     */
    public Task getSelectedTask() {
        return selectedTask.get();
    }
    
/**
     * Sets the selected task.
     * 
     * @param task the selected task
     */
    public void setSelectedTask(Task task) {
        selectedTask.set(task);
    }
    
    /**
     * Gets the available subteams.
     * 
     * @return the available subteams
     */
    public ObservableList<Subteam> getAvailableSubteams() {
        return availableSubteams;
    }
    
    /**
     * Gets the status options.
     * 
     * @return the status options
     */
    public ObservableList<Subsystem.Status> getStatusOptions() {
        return statusOptions;
    }
    
    /**
     * Gets the total tasks property.
     * 
     * @return the total tasks property
     */
    public IntegerProperty totalTasksProperty() {
        return totalTasks;
    }
    
    /**
     * Gets the completed tasks property.
     * 
     * @return the completed tasks property
     */
    public IntegerProperty completedTasksProperty() {
        return completedTasks;
    }
    
    /**
     * Gets the completion percentage property.
     * 
     * @return the completion percentage property
     */
    public DoubleProperty completionPercentageProperty() {
        return completionPercentage;
    }
    
    /**
     * Gets the save command.
     * 
     * @return the save command
     */
    public Command getSaveCommand() {
        return saveCommand;
    }
    
    /**
     * Gets the cancel command.
     * 
     * @return the cancel command
     */
    public Command getCancelCommand() {
        return cancelCommand;
    }
    
    /**
     * Gets the add task command.
     * 
     * @return the add task command
     */
    public Command getAddTaskCommand() {
        return addTaskCommand;
    }
    
    /**
     * Gets the view task command.
     * 
     * @return the view task command
     */
    public Command getViewTaskCommand() {
        return viewTaskCommand;
    }
    
    // Property getters and setters
    
    public StringProperty subsystemNameProperty() {
        return subsystemName;
    }
    
    public String getSubsystemName() {
        return subsystemName.get();
    }
    
    public void setSubsystemName(String value) {
        subsystemName.set(value);
    }
    
    public StringProperty subsystemDescriptionProperty() {
        return subsystemDescription;
    }
    
    public String getSubsystemDescription() {
        return subsystemDescription.get();
    }
    
    public void setSubsystemDescription(String value) {
        subsystemDescription.set(value);
    }
    
    public ObjectProperty<Subsystem.Status> statusProperty() {
        return status;
    }
    
    public Subsystem.Status getStatus() {
        return status.get();
    }
    
    public void setStatus(Subsystem.Status value) {
        status.set(value);
    }
    
    public ObjectProperty<Subteam> responsibleSubteamProperty() {
        return responsibleSubteam;
    }
    
    public Subteam getResponsibleSubteam() {
        return responsibleSubteam.get();
    }
    
    public void setResponsibleSubteam(Subteam value) {
        responsibleSubteam.set(value);
    }
    
    /**
     * Cleans up resources when the ViewModel is no longer needed.
     */
    @Override
    public void dispose() {
        super.dispose();
        tasks.clear();
        availableSubteams.clear();
    }
}