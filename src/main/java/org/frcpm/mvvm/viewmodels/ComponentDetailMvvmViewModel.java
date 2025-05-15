// src/main/java/org/frcpm/mvvm/viewmodels/ComponentDetailMvvmViewModel.java
package org.frcpm.mvvm.viewmodels;

import de.saxsys.mvvmfx.utils.commands.Command;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.frcpm.models.Component;
import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.mvvm.BaseMvvmViewModel;
import org.frcpm.mvvm.async.MvvmAsyncHelper;
import org.frcpm.services.ComponentService;
import org.frcpm.services.TaskService;
import org.frcpm.services.impl.ComponentServiceAsyncImpl;
import org.frcpm.services.impl.TaskServiceAsyncImpl;

/**
 * ViewModel for the ComponentDetail view using MVVMFx.
 */
public class ComponentDetailMvvmViewModel extends BaseMvvmViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(ComponentDetailMvvmViewModel.class.getName());
    
    // Service dependencies
    private final ComponentService componentService;
    private final ComponentServiceAsyncImpl componentServiceAsync;
    private final TaskService taskService;
    private final TaskServiceAsyncImpl taskServiceAsync;
    
    // Observable properties for component fields
    private final StringProperty name = new SimpleStringProperty("");
    private final StringProperty partNumber = new SimpleStringProperty("");
    private final StringProperty description = new SimpleStringProperty("");
    private final ObjectProperty<LocalDate> expectedDelivery = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> actualDelivery = new SimpleObjectProperty<>();
    private final BooleanProperty delivered = new SimpleBooleanProperty(false);
    private final BooleanProperty isNewComponent = new SimpleBooleanProperty(true);
    private final BooleanProperty valid = new SimpleBooleanProperty(false);
    private final ObjectProperty<Component> component = new SimpleObjectProperty<>();
    private final ObjectProperty<Project> currentProject = new SimpleObjectProperty<>();
    private final ObjectProperty<Task> selectedTask = new SimpleObjectProperty<>();
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    
    // Observable collections
    private final ObservableList<Task> requiredForTasks = FXCollections.observableArrayList();
    
    // Commands
    private Command saveCommand;
    private Command cancelCommand;
    private Command addTaskCommand;
    private Command removeTaskCommand;
    
    /**
     * Creates a new ComponentDetailMvvmViewModel.
     * 
     * @param componentService the component service
     * @param taskService the task service
     */
    public ComponentDetailMvvmViewModel(ComponentService componentService, TaskService taskService) {
        this.componentService = componentService;
        this.componentServiceAsync = (ComponentServiceAsyncImpl) componentService;
        this.taskService = taskService;
        this.taskServiceAsync = (TaskServiceAsyncImpl) taskService;
        
        initializeCommands();
        setupValidation();
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
        
        // Remove task command
        removeTaskCommand = createValidOnlyCommand(
            this::removeTaskAsync,
            this::canRemoveTask
        );
    }
    
    /**
     * Sets up validation for this view model.
     */
    private void setupValidation() {
        // Validation listener for all required fields
        Runnable validateAndMarkDirty = createDirtyFlagHandler(this::validate);
        
        // Add listeners to properties that affect validation
        name.addListener((obs, oldVal, newVal) -> validateAndMarkDirty.run());
        
        // Add listeners to properties that don't affect validation but mark as dirty
        partNumber.addListener((obs, oldVal, newVal) -> setDirty(true));
        description.addListener((obs, oldVal, newVal) -> setDirty(true));
        expectedDelivery.addListener((obs, oldVal, newVal) -> setDirty(true));
        actualDelivery.addListener((obs, oldVal, newVal) -> setDirty(true));
        
        // Set up delivered property listener
        delivered.addListener((obs, oldVal, newVal) -> {
            setDirty(true);
            if (Boolean.TRUE.equals(newVal) && actualDelivery.get() == null) {
                actualDelivery.set(LocalDate.now());
            }
            validate();
        });
        
        // Initial validation
        validate();
    }
    
    /**
     * Validates the component data.
     * Sets the valid property and error message accordingly.
     */
    private void validate() {
        List<String> errors = new ArrayList<>();
        
        // Check required fields
        if (name.get() == null || name.get().trim().isEmpty()) {
            errors.add("Component name is required");
        }
        
        // If delivered is true, actual delivery date should be set
        if (Boolean.TRUE.equals(delivered.get()) && actualDelivery.get() == null) {
            errors.add("Actual delivery date is required for delivered components");
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
     * Initializes the view model for a new component.
     */
    public void initNewComponent() {
        // Create a new component
        Component newComponent = new Component();
        this.component.set(newComponent);
        this.isNewComponent.set(true);
        
        // Reset properties
        name.set("");
        partNumber.set("");
        description.set("");
        expectedDelivery.set(null);
        actualDelivery.set(null);
        delivered.set(false);
        
        // Clear tasks
        requiredForTasks.clear();
        
        // Clear dirty flag
        setDirty(false);
        
        // Clear error message
        clearErrorMessage();
        
        // Validate
        validate();
    }
    
    /**
     * Initializes the view model for editing an existing component.
     * 
     * @param component the component to edit
     */
    public void initExistingComponent(Component component) {
        if (component == null) {
            throw new IllegalArgumentException("Component cannot be null");
        }
        
        this.component.set(component);
        this.isNewComponent.set(false);
        
        // Set properties from component
        name.set(component.getName());
        partNumber.set(component.getPartNumber());
        description.set(component.getDescription());
        expectedDelivery.set(component.getExpectedDelivery());
        actualDelivery.set(component.getActualDelivery());
        delivered.set(component.isDelivered());
        
        // Load tasks
        loadTasksAsync();
        
        // Clear dirty flag
        setDirty(false);
        
        // Clear error message
        clearErrorMessage();
        
        // Validate
        validate();
    }
    
    /**
     * Loads tasks that require this component.
     */
    private void loadTasksAsync() {
        Component comp = component.get();
        if (comp == null || comp.getId() == null || loading.get()) {
            return;
        }
        
        loading.set(true);
        
        // For now, we'll refresh the component to get the latest tasks
        componentServiceAsync.findByIdAsync(
            comp.getId(),
            // Success handler
            refreshedComponent -> {
                Platform.runLater(() -> {
                    if (refreshedComponent != null && refreshedComponent.getRequiredForTasks() != null) {
                        requiredForTasks.clear();
                        requiredForTasks.addAll(refreshedComponent.getRequiredForTasks());
                        LOGGER.info("Loaded " + requiredForTasks.size() + " tasks for component asynchronously");
                    }
                    loading.set(false);
                });
            },
            // Error handler
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error loading tasks for component asynchronously", error);
                    setErrorMessage("Failed to load tasks: " + error.getMessage());
                    loading.set(false);
                });
            }
        );
    }
    
    /**
     * Saves the component.
     */
    private void save() {
        if (!valid.get() || !isDirty()) {
            return;
        }
        
        loading.set(true);
        
        try {
            // Get the current component
            Component componentToSave = component.get();
            if (componentToSave == null) {
                componentToSave = new Component();
            }
            
            // Update component from properties
            componentToSave.setName(name.get());
            componentToSave.setPartNumber(partNumber.get());
            componentToSave.setDescription(description.get());
            componentToSave.setExpectedDelivery(expectedDelivery.get());
            componentToSave.setActualDelivery(actualDelivery.get());
            componentToSave.setDelivered(delivered.get());
            
            // Save the component asynchronously
            componentServiceAsync.saveAsync(
                componentToSave,
                // Success handler
                savedComponent -> {
                    Platform.runLater(() -> {
                        component.set(savedComponent);
                        setDirty(false);
                        isNewComponent.set(false);
                        loading.set(false);
                        LOGGER.info("Component saved successfully: " + savedComponent.getName());
                    });
                },
                // Error handler
                error -> {
                    Platform.runLater(() -> {
                        LOGGER.log(Level.SEVERE, "Error saving component", error);
                        setErrorMessage("Failed to save component: " + error.getMessage());
                        loading.set(false);
                    });
                }
            );
        } catch (Exception e) {
            loading.set(false);
            LOGGER.log(Level.SEVERE, "Error in save method", e);
            setErrorMessage("Failed to save component: " + e.getMessage());
        }
    }
    
    /**
     * Checks if a task can be added to the component.
     * 
     * @return true if a task can be added, false otherwise
     */
    private boolean canAddTask() {
        // Can only add tasks to saved components
        return component.get() != null && component.get().getId() != null && !loading.get();
    }
    
    /**
     * Checks if a task can be removed from the component.
     * 
     * @return true if a task can be removed, false otherwise
     */
    private boolean canRemoveTask() {
        return selectedTask.get() != null && !loading.get();
    }
    
    /**
     * Adds a task to this component's required tasks.
     * 
     * @param task the task to add
     * @return true if successful, false otherwise
     */
    public boolean addTask(Task task) {
        if (task == null) {
            LOGGER.warning("Cannot add null task");
            return false;
        }
        
        if (component.get() == null || component.get().getId() == null) {
            setErrorMessage("Component must be saved before adding tasks");
            return false;
        }
        
        // Check if the task is already in the list
        for (Task existingTask : requiredForTasks) {
            if (existingTask.getId().equals(task.getId())) {
                setErrorMessage("Task is already associated with this component");
                return false;
            }
        }
        
        loading.set(true);
        
        // Create a set of component IDs to associate
        Set<Long> componentIds = new HashSet<>();
        componentIds.add(component.get().getId());
        
        try {
            // Use the async service to associate the component with the task
            taskServiceAsync.associateComponentsWithTaskAsync(
                task.getId(),
                componentIds,
                updatedTask -> {
                    Platform.runLater(() -> {
                        // Reload tasks to reflect the changes
                        loadTasksAsync();
                        
                        // Mark as dirty
                        setDirty(true);
                        
                        LOGGER.info("Task added to component successfully");
                    });
                },
                error -> {
                    Platform.runLater(() -> {
                        LOGGER.log(Level.SEVERE, "Error adding task to component", error);
                        setErrorMessage("Failed to add task: " + error.getMessage());
                        loading.set(false);
                    });
                }
            );
            
            return true;
        } catch (Exception e) {
            loading.set(false);
            LOGGER.log(Level.SEVERE, "Error adding task to component", e);
            setErrorMessage("Failed to add task: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Removes the selected task from this component's required tasks.
     */
    private void removeTaskAsync() {
        Task task = selectedTask.get();
        if (task == null || task.getId() == null) {
            return;
        }
        
        if (component.get() == null || component.get().getId() == null) {
            return;
        }
        
        loading.set(true);
        
        // Create a set of component IDs without this component
        try {
            // Use the async service to update the task's required components
            Set<Long> emptyComponentIds = new HashSet<>(); // Empty to remove all associations
            
            taskServiceAsync.associateComponentsWithTaskAsync(
                task.getId(),
                emptyComponentIds,
                updatedTask -> {
                    Platform.runLater(() -> {
                        // Reload tasks to reflect the changes
                        loadTasksAsync();
                        
                        // Mark as dirty
                        setDirty(true);
                        
                        LOGGER.info("Task removed from component successfully");
                    });
                },
                error -> {
                    Platform.runLater(() -> {
                        LOGGER.log(Level.SEVERE, "Error removing task from component", error);
                        setErrorMessage("Failed to remove task: " + error.getMessage());
                        loading.set(false);
                    });
                }
            );
        } catch (Exception e) {
            loading.set(false);
            LOGGER.log(Level.SEVERE, "Error removing task from component", e);
            setErrorMessage("Failed to remove task: " + e.getMessage());
        }
    }
    
    /**
     * Gets the value of the valid property.
     * 
     * @return true if the component is valid, false otherwise
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
     * Gets the component.
     * 
     * @return the component
     */
    public Component getComponent() {
        return component.get();
    }
    
    /**
     * Gets the component property.
     * 
     * @return the component property
     */
    public ObjectProperty<Component> componentProperty() {
        return component;
    }
    
    /**
     * Gets whether this is a new component.
     * 
     * @return true if this is a new component, false if editing an existing component
     */
    public boolean isNewComponent() {
        return isNewComponent.get();
    }
    
    /**
     * Gets the new component property.
     * 
     * @return the new component property
     */
    public BooleanProperty isNewComponentProperty() {
        return isNewComponent;
    }
    
    /**
     * Gets the required for tasks list.
     * 
     * @return the required for tasks list
     */
    public ObservableList<Task> getRequiredForTasks() {
        return requiredForTasks;
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
     * Gets the remove task command.
     * 
     * @return the remove task command
     */
    public Command getRemoveTaskCommand() {
        return removeTaskCommand;
    }
    
    // Property getters and setters
    
    public StringProperty nameProperty() {
        return name;
    }
    
    public String getName() {
        return name.get();
    }
    
    public void setName(String value) {
        name.set(value);
    }
    
    public StringProperty partNumberProperty() {
        return partNumber;
    }
    
    public String getPartNumber() {
        return partNumber.get();
    }
    

    public void setPartNumber(String value) {
        partNumber.set(value);
    }
    
    public StringProperty descriptionProperty() {
        return description;
    }
    
    public String getDescription() {
        return description.get();
    }
    
    public void setDescription(String value) {
        description.set(value);
    }
    
    public ObjectProperty<LocalDate> expectedDeliveryProperty() {
        return expectedDelivery;
    }
    
    public LocalDate getExpectedDelivery() {
        return expectedDelivery.get();
    }
    
    public void setExpectedDelivery(LocalDate value) {
        expectedDelivery.set(value);
    }
    
    public ObjectProperty<LocalDate> actualDeliveryProperty() {
        return actualDelivery;
    }
    
    public LocalDate getActualDelivery() {
        return actualDelivery.get();
    }
    
    public void setActualDelivery(LocalDate value) {
        actualDelivery.set(value);
    }
    
    public BooleanProperty deliveredProperty() {
        return delivered;
    }
    
    public boolean isDelivered() {
        return delivered.get();
    }
    
    public void setDelivered(boolean value) {
        delivered.set(value);
    }
    
    public ObjectProperty<Project> currentProjectProperty() {
        return currentProject;
    }
    
    public Project getCurrentProject() {
        return currentProject.get();
    }
    
    public void setCurrentProject(Project value) {
        currentProject.set(value);
    }
    
    /**
     * Cleans up resources when the ViewModel is no longer needed.
     */
    @Override
    public void dispose() {
        super.dispose();
        requiredForTasks.clear();
    }
}