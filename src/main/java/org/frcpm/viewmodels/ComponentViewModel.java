package org.frcpm.viewmodels;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.frcpm.binding.Command;
import org.frcpm.models.Component;
import org.frcpm.models.Task;
import org.frcpm.services.ComponentService;
import org.frcpm.services.ServiceFactory;
import org.frcpm.services.TaskService;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * ViewModel for the Component view.
 * Handles business logic for component creation and editing.
 */
public class ComponentViewModel extends BaseViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(ComponentViewModel.class.getName());
    
    // Services
    private final ComponentService componentService;
    private final TaskService taskService;
    
    // Model reference
    private Component component;
    
    // Observable properties
    private final StringProperty name = new SimpleStringProperty("");
    private final StringProperty partNumber = new SimpleStringProperty("");
    private final StringProperty description = new SimpleStringProperty("");
    private final ObjectProperty<LocalDate> expectedDelivery = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> actualDelivery = new SimpleObjectProperty<>();
    private final BooleanProperty delivered = new SimpleBooleanProperty(false);
    
    // Collections
    private final ObservableList<Task> requiredForTasks = FXCollections.observableArrayList();
    
    // Selected items
    private Task selectedTask;
    
    // Commands
    private final Command saveCommand;
    private final Command cancelCommand;
    private final Command addTaskCommand;
    private final Command removeTaskCommand;
    
    /**
     * Creates a new ComponentViewModel with default services.
     */
    public ComponentViewModel() {
        this(ServiceFactory.getComponentService(), ServiceFactory.getTaskService());
    }
    
    /**
     * Creates a new ComponentViewModel with specified services.
     * This constructor is primarily used for testing to inject mock services.
     * 
     * @param componentService the component service
     * @param taskService the task service
     */
    public ComponentViewModel(ComponentService componentService, TaskService taskService) {
        this.componentService = componentService;
        this.taskService = taskService;
        
        // Initialize commands
        saveCommand = new Command(this::save, this::canSave);
        cancelCommand = new Command(this::cancel);
        addTaskCommand = new Command(this::addTask, this::canAddTask);
        removeTaskCommand = new Command(this::removeTask, this::canRemoveTask);
        
        // Set up property listeners
        setupPropertyListeners();
    }
    
    /**
     * Sets up property change listeners.
     */
    private void setupPropertyListeners() {
        // When delivered is changed, update actual delivery date if needed
        delivered.addListener((obs, oldVal, newVal) -> {
            if (newVal && actualDelivery.get() == null) {
                actualDelivery.set(LocalDate.now());
            }
        });
        
        // Mark as dirty when any property changes
        name.addListener((obs, oldVal, newVal) -> setDirty(true));
        partNumber.addListener((obs, oldVal, newVal) -> setDirty(true));
        description.addListener((obs, oldVal, newVal) -> setDirty(true));
        expectedDelivery.addListener((obs, oldVal, newVal) -> setDirty(true));
        actualDelivery.addListener((obs, oldVal, newVal) -> setDirty(true));
        delivered.addListener((obs, oldVal, newVal) -> setDirty(true));
    }
    
    /**
     * Initializes the ViewModel for a new component.
     */
    public void initNewComponent() {
        // Create a new component
        component = new Component();
        
        // Reset properties
        clearProperties();
        
        // No initial tasks
        requiredForTasks.clear();
        
        // Not dirty initially
        setDirty(false);
    }
    
    /**
     * Initializes the ViewModel for editing an existing component.
     * 
     * @param component the component to edit
     */
    public void initExistingComponent(Component component) {
        if (component == null) {
            throw new IllegalArgumentException("Component cannot be null");
        }
        
        this.component = component;
        
        // Set properties from component
        name.set(component.getName());
        partNumber.set(component.getPartNumber());
        description.set(component.getDescription());
        expectedDelivery.set(component.getExpectedDelivery());
        actualDelivery.set(component.getActualDelivery());
        delivered.set(component.isDelivered());
        
        // Load tasks
        loadTasks();
        
        // Not dirty initially
        setDirty(false);
    }
    
    /**
     * Clears all properties.
     */
    private void clearProperties() {
        name.set("");
        partNumber.set("");
        description.set("");
        expectedDelivery.set(null);
        actualDelivery.set(null);
        delivered.set(false);
    }
    
    /**
     * Loads tasks that require this component.
     */
    private void loadTasks() {
        try {
            requiredForTasks.setAll(component.getRequiredForTasks());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading tasks for component", e);
            setErrorMessage("Failed to load tasks for component");
        }
    }
    
    /**
     * Updates the component from the properties.
     */
    private void updateComponentFromProperties() {
        component.setName(name.get());
        component.setPartNumber(partNumber.get());
        component.setDescription(description.get());
        component.setExpectedDelivery(expectedDelivery.get());
        component.setActualDelivery(actualDelivery.get());
        component.setDelivered(delivered.get());
    }
    
    // Command actions
    
    /**
     * Saves the component.
     */
    private void save() {
        if (!validate()) {
            return;
        }
        
        try {
            // Update component from properties
            updateComponentFromProperties();
            
            // Save component
            component = componentService.save(component);
            
            // Not dirty after save
            setDirty(false);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving component", e);
            setErrorMessage("Failed to save component: " + e.getMessage());
        }
    }
    
    /**
     * Validates the component data.
     * 
     * @return true if the data is valid, false otherwise
     */
    private boolean validate() {
        // Name is required
        if (name.get() == null || name.get().trim().isEmpty()) {
            setErrorMessage("Component name is required");
            return false;
        }
        
        // If delivered is true, actual delivery date should be set
        if (delivered.get() && actualDelivery.get() == null) {
            setErrorMessage("Actual delivery date is required for delivered components");
            return false;
        }
        
        return true;
    }
    
    /**
     * Cancels the current operation.
     */
    private void cancel() {
        // This will be handled by the controller to close the dialog
    }
    
    /**
     * Adds a task that requires this component.
     */
    private void addTask() {
        // This would typically show a dialog to select a task
        // For now, just log the action
        LOGGER.info("Add task action triggered");
    }
    
    /**
     * Removes a task from the required tasks.
     */
    private void removeTask() {
        if (selectedTask == null) {
            return;
        }
        
        try {
            // Remove task from the component's required tasks
            requiredForTasks.remove(selectedTask);
            
            // Update the relationship in the model
            component.getRequiredForTasks().remove(selectedTask);
            
            // Update the task to remove this component
            // Since we don't have a direct method in TaskService, we need to handle it here
            selectedTask.getRequiredComponents().remove(component);
            taskService.save(selectedTask);
            
            // Mark as dirty
            setDirty(true);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error removing task", e);
            setErrorMessage("Failed to remove task: " + e.getMessage());
        }
    }
    
    // Command condition methods
    
    /**
     * Checks if the component can be saved.
     * 
     * @return true if the component can be saved, false otherwise
     */
    private boolean canSave() {
        return isDirty();
    }
    
    /**
     * Checks if a task can be added.
     * 
     * @return true if a task can be added, false otherwise
     */
    private boolean canAddTask() {
        // Can only add tasks to existing components
        return component != null && component.getId() != null;
    }
    
    /**
     * Checks if a task can be removed.
     * 
     * @return true if a task can be removed, false otherwise
     */
    private boolean canRemoveTask() {
        return selectedTask != null;
    }
    
    // Getters and setters
    
    /**
     * Gets the name property.
     * 
     * @return the name property
     */
    public StringProperty nameProperty() {
        return name;
    }
    
    /**
     * Gets the part number property.
     * 
     * @return the part number property
     */
    public StringProperty partNumberProperty() {
        return partNumber;
    }
    
    /**
     * Gets the description property.
     * 
     * @return the description property
     */
    public StringProperty descriptionProperty() {
        return description;
    }
    
    /**
     * Gets the expected delivery property.
     * 
     * @return the expected delivery property
     */
    public ObjectProperty<LocalDate> expectedDeliveryProperty() {
        return expectedDelivery;
    }
    
    /**
     * Gets the actual delivery property.
     * 
     * @return the actual delivery property
     */
    public ObjectProperty<LocalDate> actualDeliveryProperty() {
        return actualDelivery;
    }
    
    /**
     * Gets the delivered property.
     * 
     * @return the delivered property
     */
    public BooleanProperty deliveredProperty() {
        return delivered;
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
     * Sets the selected task.
     * 
     * @param task the selected task
     */
    public void setSelectedTask(Task task) {
        this.selectedTask = task;
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
    
    /**
     * Gets the component.
     * 
     * @return the component
     */
    public Component getComponent() {
        return component;
    }
    
    /**
     * Gets the selected task.
     * This method is primarily used for testing.
     * 
     * @return the selected task
     */
    public Task getSelectedTask() {
        return selectedTask;
    }
}