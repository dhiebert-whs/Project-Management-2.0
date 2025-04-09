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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ViewModel for the Component view.
 * Handles business logic for component creation and editing.
 * Follows the standardized MVVM pattern.
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
    private final BooleanProperty isNewComponent = new SimpleBooleanProperty(true);
    private final BooleanProperty valid = new SimpleBooleanProperty(false);
    
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
        
        // Initial validation
        validate();
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
        
        // Set up validation listeners
        name.addListener((obs, oldVal, newVal) -> {
            setDirty(true);
            validate();
        });
        
        partNumber.addListener((obs, oldVal, newVal) -> setDirty(true));
        description.addListener((obs, oldVal, newVal) -> setDirty(true));
        expectedDelivery.addListener((obs, oldVal, newVal) -> setDirty(true));
        actualDelivery.addListener((obs, oldVal, newVal) -> {
            setDirty(true);
            validate();
        });
        
        delivered.addListener((obs, oldVal, newVal) -> {
            setDirty(true);
            validate();
        });
    }
    
    /**
     * Initializes the ViewModel for a new component.
     */
    public void initNewComponent() {
        // Create a new component
        component = new Component();
        isNewComponent.set(true);
        
        // Reset properties
        clearProperties();
        
        // No initial tasks
        requiredForTasks.clear();
        
        // Not dirty initially
        setDirty(false);
        
        // Validate
        validate();
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
        isNewComponent.set(false);
        
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
        
        // Validate
        validate();
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
            requiredForTasks.clear();
            if (component != null && component.getRequiredForTasks() != null) {
                requiredForTasks.addAll(component.getRequiredForTasks());
            }
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
    
    /**
     * Validates the component data.
     */
    private void validate() {
        // Name is required
        boolean isValid = name.get() != null && !name.get().trim().isEmpty();
        
        // If delivered is true, actual delivery date should be set
        if (delivered.get() && actualDelivery.get() == null) {
            isValid = false;
            setErrorMessage("Actual delivery date is required for delivered components");
        } else if (!isValid) {
            setErrorMessage("Component name is required");
        } else {
            clearErrorMessage();
        }
        
        valid.set(isValid);
    }
    
    // Command actions
    
    /**
     * Saves the component.
     */
    private void save() {
        if (!valid.get()) {
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
        // Actual implementation would be provided by the controller
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
        return isDirty() && valid.get();
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
     * Gets the isNewComponent property.
     * 
     * @return the isNewComponent property
     */
    public BooleanProperty isNewComponentProperty() {
        return isNewComponent;
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
     * Gets whether this is a new component.
     * 
     * @return true if this is a new component, false otherwise
     */
    public boolean isNewComponent() {
        return isNewComponent.get();
    }
    
    /**
     * Gets whether the component is valid.
     * 
     * @return true if the component is valid, false otherwise
     */
    public boolean isValid() {
        return valid.get();
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
     * Gets the selected task.
     * 
     * @return the selected task
     */
    public Task getSelectedTask() {
        return selectedTask;
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
     * Clears the error message.
     * This overrides the protected method in BaseViewModel to make it public.
     */
    @Override
    public void clearErrorMessage() {
        super.clearErrorMessage();
    }
}