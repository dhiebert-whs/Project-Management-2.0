// src/main/java/org/frcpm/viewmodels/SubsystemViewModel.java
package org.frcpm.viewmodels;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.frcpm.binding.Command;
import org.frcpm.models.Subteam;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.services.ServiceFactory;
import org.frcpm.services.SubsystemService;
import org.frcpm.services.SubteamService;
import org.frcpm.services.TaskService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ViewModel for Subsystem management in the FRC Project Management System.
 */
public class SubsystemViewModel extends BaseViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(SubsystemViewModel.class.getName());
    
    // Services
    private final SubsystemService subsystemService;
    private final SubteamService subteamService;
    private final TaskService taskService;
    
    // Observable properties
    private final StringProperty subsystemName = new SimpleStringProperty("");
    private final StringProperty subsystemDescription = new SimpleStringProperty("");
    private final ObjectProperty<Subsystem.Status> status = new SimpleObjectProperty<>(Subsystem.Status.NOT_STARTED);
    private final ObjectProperty<Subteam> responsibleSubteam = new SimpleObjectProperty<>();
    private final ObjectProperty<Subsystem> selectedSubsystem = new SimpleObjectProperty<>();
    private final ObservableList<Subsystem> subsystems = FXCollections.observableArrayList();
    private final ObservableList<Task> tasks = FXCollections.observableArrayList();
    private final ObservableList<Subteam> availableSubteams = FXCollections.observableArrayList();
    private final BooleanProperty isNewSubsystem = new SimpleBooleanProperty(true);
    private final BooleanProperty valid = new SimpleBooleanProperty(false);
    private final ObjectProperty<Task> selectedTask = new SimpleObjectProperty<>();
    
    // Subsystem summary properties
    private final IntegerProperty totalTasks = new SimpleIntegerProperty(0);
    private final IntegerProperty completedTasks = new SimpleIntegerProperty(0);
    private final DoubleProperty completionPercentage = new SimpleDoubleProperty(0);
    
    // Commands
    private final Command saveCommand;
    private final Command viewTaskCommand;
    private final Command addTaskCommand;
    
    /**
     * Creates a new SubsystemViewModel with default services.
     */
    public SubsystemViewModel() {
        this(
            ServiceFactory.getSubsystemService(),
            ServiceFactory.getSubteamService(),
            ServiceFactory.getTaskService()
        );
    }
    
    /**
     * Creates a new SubsystemViewModel with the specified services.
     * This constructor is mainly used for testing.
     * 
     * @param subsystemService the subsystem service
     * @param subteamService the subteam service
     * @param taskService the task service
     */
    public SubsystemViewModel(SubsystemService subsystemService, SubteamService subteamService, TaskService taskService) {
        this.subsystemService = subsystemService;
        this.subteamService = subteamService;
        this.taskService = taskService;
        
        // Create commands using the BaseViewModel helper methods
        saveCommand = createValidAndDirtyCommand(this::save, this::isValid);
        viewTaskCommand = createValidOnlyCommand(this::viewTask, this::canViewTask);
        addTaskCommand = createValidOnlyCommand(this::addTask, this::canAddTask);
        
        // Set up validation listeners
        Runnable validationHandler = createDirtyFlagHandler(this::validate);
        subsystemName.addListener((observable, oldValue, newValue) -> validationHandler.run());
        trackPropertyListener(validationHandler);
        
        // Set up dirty flag listeners
        Runnable dirtyHandler = createDirtyFlagHandler(null);
        subsystemDescription.addListener((observable, oldValue, newValue) -> dirtyHandler.run());
        status.addListener((observable, oldValue, newValue) -> dirtyHandler.run());
        responsibleSubteam.addListener((observable, oldValue, newValue) -> dirtyHandler.run());
        trackPropertyListener(dirtyHandler);
        
        // Set up selection listener
        selectedSubsystem.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateFormFromSubsystem(newValue);
                loadTasks();
            } else {
                clearForm();
            }
        });
        
        // Initial state should be clear - don't validate yet
        clearErrorMessage();
        
        // Load subteams - performed in public methods for testing
        loadSubteams();
    }
    
    /**
     * Validates the form data.
     */
    public void validate() {
        List<String> errors = new ArrayList<>();
        
        // Check required fields
        if (subsystemName.get() == null || subsystemName.get().trim().isEmpty()) {
            errors.add("Subsystem name is required");
        }
        
        // Update valid state and error message
        valid.set(errors.isEmpty());
        if (!errors.isEmpty()) {
            setErrorMessage(String.join("\n", errors));
        } else {
            clearErrorMessage();
        }
    }
    
    /**
     * Loads the list of subteams.
     */
    public void loadSubteams() {
        try {
            List<Subteam> subteamList = subteamService.findAll();
            if (subteamList != null) {
                availableSubteams.clear();
                availableSubteams.addAll(subteamList);
            } else {
                LOGGER.warning("Subteam service returned null list");
                availableSubteams.clear();
            }
            clearErrorMessage();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading subteams", e);
            setErrorMessage("Failed to load subteams: " + e.getMessage());
        }
    }
    
    /**
     * Loads tasks for the selected subsystem.
     */
    public void loadTasks() {
        try {
            Subsystem subsystem = selectedSubsystem.get();
            if (subsystem != null) {
                List<Task> taskList = taskService.findBySubsystem(subsystem);
                if (taskList != null) {
                    tasks.clear();
                    tasks.addAll(taskList);
                    
                    // Update summary data
                    int total = taskList.size();
                    int completed = (int) taskList.stream().filter(Task::isCompleted).count();
                    double percentage = total > 0 ? ((double)completed * 100.0 / total) : 0.0;
                    
                    totalTasks.set(total);
                    completedTasks.set(completed);
                    completionPercentage.set(percentage);
                } else {
                    LOGGER.warning("Task service returned null list");
                    tasks.clear();
                    totalTasks.set(0);
                    completedTasks.set(0);
                    completionPercentage.set(0);
                }
                clearErrorMessage();
            } else {
                tasks.clear();
                totalTasks.set(0);
                completedTasks.set(0);
                completionPercentage.set(0);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading tasks", e);
            setErrorMessage("Failed to load tasks: " + e.getMessage());
        }
    }
    
    /**
     * Sets up the form for creating a new subsystem.
     */
    public void initNewSubsystem() {
        selectedSubsystem.set(null);
        isNewSubsystem.set(true);
        
        // Set default values
        subsystemName.set("");
        subsystemDescription.set("");
        status.set(Subsystem.Status.NOT_STARTED);
        responsibleSubteam.set(null);
        
        // Clear task data
        tasks.clear();
        totalTasks.set(0);
        completedTasks.set(0);
        completionPercentage.set(0);
        
        // Clear dirty flag and error message
        setDirty(false);
        clearErrorMessage();
    }
    
    /**
     * Sets up the form for editing an existing subsystem.
     * 
     * @param subsystem the subsystem to edit
     */
    public void initExistingSubsystem(Subsystem subsystem) {
        if (subsystem == null) {
            throw new IllegalArgumentException("Subsystem cannot be null");
        }
        
        selectedSubsystem.set(subsystem);
        isNewSubsystem.set(false);
        
        // Update form from subsystem
        updateFormFromSubsystem(subsystem);
        
        // Load tasks
        loadTasks();
        
        // Clear dirty flag and error messages
        setDirty(false);
        clearErrorMessage();
    }
    
    /**
     * Updates the form fields from a subsystem.
     * 
     * @param subsystem the subsystem to get values from
     */
    private void updateFormFromSubsystem(Subsystem subsystem) {
        subsystemName.set(subsystem.getName());
        subsystemDescription.set(subsystem.getDescription() != null ? subsystem.getDescription() : "");
        status.set(subsystem.getStatus());
        responsibleSubteam.set(subsystem.getResponsibleSubteam());
    }
    
    /**
     * Clears the form fields.
     */
    private void clearForm() {
        subsystemName.set("");
        subsystemDescription.set("");
        status.set(Subsystem.Status.NOT_STARTED);
        responsibleSubteam.set(null);
        tasks.clear();
        
        // Reset summary properties
        totalTasks.set(0);
        completedTasks.set(0);
        completionPercentage.set(0);
        
        // Clear error message
        clearErrorMessage();
    }
    
    /**
     * Saves the subsystem.
     * Called when the save command is executed.
     */
    private void save() {
        if (!valid.get()) {
            return;
        }
        
        try {
            Subsystem subsystem;
            
            if (isNewSubsystem.get()) {
                // Create new subsystem with full data
                subsystem = new Subsystem();
                subsystem.setName(subsystemName.get());
                subsystem.setDescription(subsystemDescription.get());
                subsystem.setStatus(status.get());
                subsystem.setResponsibleSubteam(responsibleSubteam.get());
                
                // Save to database
                subsystem = subsystemService.save(subsystem);
                
                // Update selection
                selectedSubsystem.set(subsystem);
            } else {
                // Get existing subsystem
                subsystem = selectedSubsystem.get();
                if (subsystem == null) {
                    setErrorMessage("No subsystem selected for update");
                    return;
                }
                
                // Update properties
                subsystem.setName(subsystemName.get());
                subsystem.setDescription(subsystemDescription.get());
                subsystem.setStatus(status.get());
                subsystem.setResponsibleSubteam(responsibleSubteam.get());
                
                // Save to database
                subsystem = subsystemService.save(subsystem);
                
                // Update selection
                selectedSubsystem.set(subsystem);
            }
            
            // Clear dirty flag and error message
            setDirty(false);
            clearErrorMessage();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving subsystem", e);
            setErrorMessage("Failed to save subsystem: " + e.getMessage());
        }
    }
    
    /**
     * Views the selected task.
     * This is a placeholder for the dialog handler in the controller.
     */
    private void viewTask() {
        // Placeholder - dialog will be handled by presenter
        LOGGER.info("View task command executed");
    }

    /**
     * Adds a new task.
     * This is a placeholder for the dialog handler in the controller.
     */
    private void addTask() {
        // Placeholder - dialog will be handled by presenter
        LOGGER.info("Add task command executed");
    }
    
    /**
     * Checks if the form data is valid.
     * 
     * @return true if the form data is valid, false otherwise
     */
    private boolean isValid() {
        return valid.get();
    }
    
    /**
     * Checks if the view task command can be executed.
     */
    private boolean canViewTask() {
        return selectedTask.get() != null;
    }

    /**
     * Checks if the add task command can be executed.
     */
    private boolean canAddTask() {
        return selectedSubsystem.get() != null && !isNewSubsystem.get();
    }
    
    // Property accessors
    
    public StringProperty subsystemNameProperty() {
        return subsystemName;
    }
    
    public StringProperty subsystemDescriptionProperty() {
        return subsystemDescription;
    }
    
    public ObjectProperty<Subsystem.Status> statusProperty() {
        return status;
    }
    
    public ObjectProperty<Subteam> responsibleSubteamProperty() {
        return responsibleSubteam;
    }
    
    public ObjectProperty<Subsystem> selectedSubsystemProperty() {
        return selectedSubsystem;
    }
    
    public ObservableList<Task> getTasks() {
        return tasks;
    }
    
    public ObservableList<Subteam> getAvailableSubteams() {
        return availableSubteams;
    }
    
    public IntegerProperty totalTasksProperty() {
        return totalTasks;
    }
    
    public IntegerProperty completedTasksProperty() {
        return completedTasks;
    }
    
    public DoubleProperty completionPercentageProperty() {
        return completionPercentage;
    }
    
    public Command getSaveCommand() {
        return saveCommand;
    }
    
    public Command getViewTaskCommand() {
        return viewTaskCommand;
    }
    
    public Command getAddTaskCommand() {
        return addTaskCommand;
    }
    
    // Getters and setters
    
    public String getSubsystemName() {
        return subsystemName.get();
    }
    
    public void setSubsystemName(String name) {
        subsystemName.set(name);
        setDirty(true);
    }
    
    public String getSubsystemDescription() {
        return subsystemDescription.get();
    }
    
    public void setSubsystemDescription(String description) {
        subsystemDescription.set(description);
        setDirty(true);
    }
    
    public Subsystem.Status getStatus() {
        return status.get();
    }
    
    public void setStatus(Subsystem.Status value) {
        status.set(value);
        setDirty(true);
    }
    
    public Subteam getResponsibleSubteam() {
        return responsibleSubteam.get();
    }
    
    public void setResponsibleSubteam(Subteam subteam) {
        responsibleSubteam.set(subteam);
        setDirty(true);
    }
    
    public Subsystem getSelectedSubsystem() {
        return selectedSubsystem.get();
    }
    
    public void setSelectedSubsystem(Subsystem subsystem) {
        selectedSubsystem.set(subsystem);
    }
    
    public Task getSelectedTask() {
        return selectedTask.get();
    }

    public void setSelectedTask(Task task) {
        selectedTask.set(task);
    }

    public ObjectProperty<Task> selectedTaskProperty() {
        return selectedTask;
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