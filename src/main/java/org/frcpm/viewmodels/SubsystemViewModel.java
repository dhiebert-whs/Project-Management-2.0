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
    private final Command createNewCommand;
    private final Command deleteCommand;
    private final Command loadSubsystemsCommand;
    private final Command loadTasksCommand;
    private final Command loadSubteamsCommand;
    private final Command updateStatusCommand;
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
        
        // Create commands
        saveCommand = createValidOnlyCommand(this::save, this::isValid);
        createNewCommand = new Command(this::createNew);
        deleteCommand = new Command(this::delete, this::canDelete);
        loadSubsystemsCommand = new Command(this::loadSubsystems);
        loadTasksCommand = new Command(this::loadTasks, this::canLoadTasks);
        loadSubteamsCommand = new Command(this::loadSubteams);
        updateStatusCommand = new Command(this::updateStatus, this::canUpdateStatus);
        viewTaskCommand = new Command(this::viewTask, this::canViewTask);
        addTaskCommand = new Command(this::addTask, this::canAddTask);
        
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
        
        // Load subsystems and subteams - performed in public methods for testing
        loadSubsystems();
        loadSubteams();
    }
    
  // Make validate method public for direct testing
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
            Long responsibleSubteamId = null;
            
            // Get responsible subteam ID if set
            if (responsibleSubteam.get() != null) {
                responsibleSubteamId = responsibleSubteam.get().getId();
            }
            
            if (isNewSubsystem.get()) {
                // Create new subsystem
                subsystem = subsystemService.createSubsystem(
                    subsystemName.get(),
                    subsystemDescription.get(),
                    status.get(),
                    responsibleSubteamId
                );
                
                if (subsystem != null) {
                    // Add to subsystems list
                    subsystems.add(subsystem);
                    
                    // Update selected subsystem
                    selectedSubsystem.set(subsystem);
                } else {
                    setErrorMessage("Failed to create subsystem: Service returned null");
                    return;
                }
            } else {
                // Update existing subsystem
                subsystem = selectedSubsystem.get();
                if (subsystem == null) {
                    setErrorMessage("No subsystem selected for update");
                    return;
                }
                
                // Update status
                subsystem = subsystemService.updateStatus(
                    subsystem.getId(),
                    status.get()
                );
                
                if (subsystem == null) {
                    setErrorMessage("Failed to update subsystem status: Service returned null");
                    return;
                }
                
                // Assign responsible subteam
                subsystem = subsystemService.assignResponsibleSubteam(
                    subsystem.getId(),
                    responsibleSubteamId
                );
                
                if (subsystem == null) {
                    setErrorMessage("Failed to assign subteam: Service returned null");
                    return;
                }
                
                // Update description
                subsystem.setDescription(subsystemDescription.get());
                subsystem = subsystemService.save(subsystem);
                
                if (subsystem == null) {
                    setErrorMessage("Failed to save subsystem: Service returned null");
                    return;
                }
                
                // Update in subsystems list
                int index = -1;
                for (int i = 0; i < subsystems.size(); i++) {
                    if (subsystems.get(i).getId().equals(subsystem.getId())) {
                        index = i;
                        break;
                    }
                }
                
                if (index >= 0) {
                    subsystems.set(index, subsystem);
                }
                
                // Update selected subsystem
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
     * Creates a new subsystem.
     * Called when the create new command is executed.
     */
    private void createNew() {
        initNewSubsystem();
    }
    
    /**
     * Deletes the selected subsystem.
     * Called when the delete command is executed.
     */
    private void delete() {
        try {
            Subsystem subsystem = selectedSubsystem.get();
            if (subsystem != null) {
                // Check if subsystem has tasks
                List<Task> subsystemTasks = taskService.findBySubsystem(subsystem);
                if (subsystemTasks != null && !subsystemTasks.isEmpty()) {
                    setErrorMessage("Cannot delete subsystem that has tasks. Reassign tasks first.");
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
     * Updates the status of the selected subsystem.
     * Called when the update status command is executed.
     */
    private void updateStatus() {
        try {
            Subsystem subsystem = selectedSubsystem.get();
            if (subsystem != null) {
                // Update status
                subsystem = subsystemService.updateStatus(
                    subsystem.getId(),
                    status.get()
                );
                
                if (subsystem != null) {
                    // Update in subsystems list
                    int index = -1;
                    for (int i = 0; i < subsystems.size(); i++) {
                        if (subsystems.get(i).getId().equals(subsystem.getId())) {
                            index = i;
                            break;
                        }
                    }
                    
                    if (index >= 0) {
                        subsystems.set(index, subsystem);
                    }
                    
                    // Update selected subsystem
                    selectedSubsystem.set(subsystem);
                    
                    // Clear dirty flag and error message
                    setDirty(false);
                    clearErrorMessage();
                } else {
                    setErrorMessage("Failed to update subsystem status: Service returned null");
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating subsystem status", e);
            setErrorMessage("Failed to update subsystem status: " + e.getMessage());
        }
    }
    
    /**
     * Finds a subsystem by name.
     * 
     * @param name the name to search for
     * @return the subsystem if found, or null if not found
     */
    public Subsystem findByName(String name) {
        try {
            Optional<Subsystem> subsystem = subsystemService.findByName(name);
            clearErrorMessage();
            return subsystem.orElse(null);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding subsystem by name", e);
            setErrorMessage("Failed to find subsystem: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Checks if the delete command can be executed.
     * 
     * @return true if a subsystem is selected, false otherwise
     */
    private boolean canDelete() {
        return selectedSubsystem.get() != null;
    }
    
    /**
     * Checks if the load tasks command can be executed.
     * 
     * @return true if a subsystem is selected, false otherwise
     */
    private boolean canLoadTasks() {
        return selectedSubsystem.get() != null;
    }
    
    /**
     * Checks if the update status command can be executed.
     * 
     * @return true if a subsystem is selected, false otherwise
     */
    private boolean canUpdateStatus() {
        return selectedSubsystem.get() != null;
    }
    
    /**
     * Override to set an empty name to validate properly for testing
     */
    public void setSubsystemName(String name) {
        subsystemName.set(name);
        // When explicitly setting name, mark as dirty to ensure validation works
        setDirty(true);
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
    
    public ObservableList<Subsystem> getSubsystems() {
        return subsystems;
    }
    
    public ObservableList<Task> getTasks() {
        return tasks;
    }
    
    public ObservableList<Subteam> getAvailableSubteams() {
        return availableSubteams;
    }
    
    public BooleanProperty isNewSubsystemProperty() {
        return isNewSubsystem;
    }
    
    public BooleanProperty validProperty() {
        return valid;
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
    
    public Command getCreateNewCommand() {
        return createNewCommand;
    }
    
    public Command getDeleteCommand() {
        return deleteCommand;
    }
    
    public Command getLoadSubsystemsCommand() {
        return loadSubsystemsCommand;
    }
    
    public Command getLoadTasksCommand() {
        return loadTasksCommand;
    }
    
    public Command getLoadSubteamsCommand() {
        return loadSubteamsCommand;
    }
    
    public Command getUpdateStatusCommand() {
        return updateStatusCommand;
    }
    
    // Getters and setters
    
    public String getSubsystemName() {
        return subsystemName.get();
    }
    
    public String getSubsystemDescription() {
        return subsystemDescription.get();
    }
    
    public void setSubsystemDescription(String description) {
        subsystemDescription.set(description);
    }
    
    public Subsystem.Status getStatus() {
        return status.get();
    }
    
    public void setStatus(Subsystem.Status value) {
        status.set(value);
    }
    
    public Subteam getResponsibleSubteam() {
        return responsibleSubteam.get();
    }
    
    public void setResponsibleSubteam(Subteam subteam) {
        responsibleSubteam.set(subteam);
    }
    
    public Subsystem getSelectedSubsystem() {
        return selectedSubsystem.get();
    }
    
    public void setSelectedSubsystem(Subsystem subsystem) {
        selectedSubsystem.set(subsystem);
    }
    
    public boolean isNewSubsystem() {
        return isNewSubsystem.get();
    }
    
    public void setIsNewSubsystem(boolean isNew) {
        isNewSubsystem.set(isNew);
    }
    
    public boolean isValid() {
        return valid.get();
    }
    
    public int getTotalTasks() {
        return totalTasks.get();
    }
    
    public int getCompletedTasks() {
        return completedTasks.get();
    }
    
    public double getCompletionPercentage() {
        return completionPercentage.get();
    }

    /**
     * Gets or sets the selected task.
     */
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
     * Gets the view task command.
     * The view task command is executed when the user wants to view/edit a task.
     */
    public Command getViewTaskCommand() {
        return viewTaskCommand;
    }

    /**
     * Gets the add task command.
     * The add task command is executed when the user wants to add a new task.
     */
    public Command getAddTaskCommand() {
        return addTaskCommand;
    }

    /**
     * Views the selected task.
     * This is a placeholder for the dialog handler in the controller.
     */
    private void viewTask() {
        // Placeholder - dialog will be handled by controller
        LOGGER.info("View task command executed");
    }

    /**
     * Adds a new task.
     * This is a placeholder for the dialog handler in the controller.
     */
    private void addTask() {
        // Placeholder - dialog will be handled by controller
        LOGGER.info("Add task command executed");
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
        return selectedSubsystem.get() != null;
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