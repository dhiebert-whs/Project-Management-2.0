// src/main/java/org/frcpm/viewmodels/ComponentAsyncViewModel.java

package org.frcpm.viewmodels;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import org.frcpm.binding.Command;
import org.frcpm.models.Component;
import org.frcpm.models.Task;
import org.frcpm.services.impl.ComponentServiceAsyncImpl;
import org.frcpm.services.impl.TaskServiceAsyncImpl;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Asynchronous ViewModel for component management.
 */
public class ComponentAsyncViewModel extends ComponentViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(ComponentAsyncViewModel.class.getName());
    
    // Services
    private final ComponentServiceAsyncImpl componentServiceAsync;
    private final TaskServiceAsyncImpl taskServiceAsync;
    
    // UI state
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    
    // Async commands
    private Command asyncSaveCommand;
    private Command asyncLoadTasksCommand;
    private Command asyncAddTaskCommand;
    private Command asyncRemoveTaskCommand;
    
    /**
     * Creates a new ComponentAsyncViewModel with the specified services.
     * 
     * @param componentServiceAsync the async component service
     * @param taskServiceAsync the async task service
     */
    public ComponentAsyncViewModel(ComponentServiceAsyncImpl componentServiceAsync, 
                               TaskServiceAsyncImpl taskServiceAsync) {
        super(null, null); // Initialize parent with null services
        
        this.componentServiceAsync = componentServiceAsync;
        this.taskServiceAsync = taskServiceAsync;
        
        // Initialize async commands
        initAsyncCommands();
    }
    
    /**
     * Initializes async commands.
     */
    private void initAsyncCommands() {
        asyncSaveCommand = new Command(
            this::saveAsync, 
            () -> {
                try {
                    // Access the valid property via reflection
                    Field validField = ComponentViewModel.class.getDeclaredField("valid");
                    validField.setAccessible(true);
                    BooleanProperty validProperty = (BooleanProperty) validField.get(this);
                    
                    return validProperty.get() && isDirty() && !loading.get();
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error accessing valid field", e);
                    return false;
                }
            }
        );
        
        asyncLoadTasksCommand = new Command(
            this::loadTasksAsync,
            () -> {
                try {
                    Component component = getComponent();
                    return component != null && component.getId() != null && !loading.get();
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error checking component ID", e);
                    return false;
                }
            }
        );
        
        asyncAddTaskCommand = new Command(
            this::addTaskAsync,
            () -> {
                try {
                    // Check if we have a component with an ID
                    Component component = getComponent();
                    return component != null && component.getId() != null && !loading.get();
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error checking component ID", e);
                    return false;
                }
            }
        );
        
        asyncRemoveTaskCommand = new Command(
            this::removeTaskAsync,
            () -> {
                try {
                    // Check if we have a component with an ID and a selected task
                    Component component = getComponent();
                    Task selectedTask = getSelectedTask();
                    return component != null && component.getId() != null && 
                           selectedTask != null && !loading.get();
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error checking component or task", e);
                    return false;
                }
            }
        );
    }
    
    /**
     * Asynchronously saves the component.
     */
    public void saveAsync() {
        try {
            // Get the valid property via reflection to check if we can save
            Field validField = ComponentViewModel.class.getDeclaredField("valid");
            validField.setAccessible(true);
            BooleanProperty validProperty = (BooleanProperty) validField.get(this);
            
            if (!validProperty.get() || !isDirty() || loading.get()) {
                return;
            }
            
            // Get the component and check if it's null
            Component component = getComponent();
            if (component == null) {
                setErrorMessage("No component to save");
                return;
            }
            
            // Update component from properties using reflection
            Field nameField = ComponentViewModel.class.getDeclaredField("name");
            nameField.setAccessible(true);
            Field partNumberField = ComponentViewModel.class.getDeclaredField("partNumber");
            partNumberField.setAccessible(true);
            Field descriptionField = ComponentViewModel.class.getDeclaredField("description");
            descriptionField.setAccessible(true);
            Field expectedDeliveryField = ComponentViewModel.class.getDeclaredField("expectedDelivery");
            expectedDeliveryField.setAccessible(true);
            Field actualDeliveryField = ComponentViewModel.class.getDeclaredField("actualDelivery");
            actualDeliveryField.setAccessible(true);
            Field deliveredField = ComponentViewModel.class.getDeclaredField("delivered");
            deliveredField.setAccessible(true);
            
            // Update component properties
            component.setName(nameField.get(this).toString());
            component.setPartNumber(partNumberField.get(this).toString());
            component.setDescription(descriptionField.get(this).toString());
            component.setExpectedDelivery((LocalDate) expectedDeliveryField.get(this));
            component.setActualDelivery((LocalDate) actualDeliveryField.get(this));
            component.setDelivered((Boolean) deliveredField.get(this));
            
            // Set loading state
            loading.set(true);
            
            // Check if this is a new component
            Field isNewComponentField = ComponentViewModel.class.getDeclaredField("isNewComponent");
            isNewComponentField.setAccessible(true);
            BooleanProperty isNewComponentProperty = (BooleanProperty) isNewComponentField.get(this);
            
            CompletableFuture<Component> future;
            if (isNewComponentProperty.get()) {
                // Create new component
                future = componentServiceAsync.createComponentAsync(
                    component.getName(),
                    component.getPartNumber(),
                    component.getDescription(),
                    component.getExpectedDelivery(),
                    this::handleSaveSuccess,
                    this::handleError
                );
            } else {
                // For existing components, just save
                future = componentServiceAsync.saveAsync(component,
                    this::handleSaveSuccess,
                    this::handleError
                );
            }
            
            // Handle completion
            future.whenComplete((result, error) -> {
                if (error != null) {
                    Platform.runLater(() -> {
                        loading.set(false);
                        setErrorMessage("Error saving component: " + error.getMessage());
                    });
                }
            });
            
        } catch (Exception e) {
            loading.set(false);
            LOGGER.log(Level.SEVERE, "Error saving component", e);
            setErrorMessage("Failed to save component: " + e.getMessage());
        }
    }
    
    /**
     * Asynchronously loads tasks that require this component.
     */
    public void loadTasksAsync() {
        try {
            Component component = getComponent();
            if (component == null || component.getId() == null || loading.get()) {
                return;
            }
            
            loading.set(true);
            
            // For now, we'll use a direct query to the database
            CompletableFuture.supplyAsync(() -> {
                try {
                    // Get the requiredForTasks field via reflection
                    Field requiredForTasksField = ComponentViewModel.class.getDeclaredField("requiredForTasks");
                    requiredForTasksField.setAccessible(true);
                    
                    // Fetch tasks that require this component
                    Component refreshedComponent = componentServiceAsync.findById(component.getId());
                    
                    if (refreshedComponent != null && refreshedComponent.getRequiredForTasks() != null) {
                        return List.copyOf(refreshedComponent.getRequiredForTasks());
                    } else {
                        return List.of();
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Error loading tasks", e);
                }
            }).thenAccept(tasks -> {
                Platform.runLater(() -> {
                    try {
                        // Get the requiredForTasks field via reflection
                        Field requiredForTasksField = ComponentViewModel.class.getDeclaredField("requiredForTasks");
                        requiredForTasksField.setAccessible(true);
                        
                        // Set the tasks in the ViewModel
                        requiredForTasksField.set(this, FXCollections.observableArrayList(tasks));
                        
                        loading.set(false);
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Error updating tasks in ViewModel", e);
                        setErrorMessage("Error updating tasks: " + e.getMessage());
                        loading.set(false);
                    }
                });
            }).exceptionally(error -> {
                Platform.runLater(() -> {
                    loading.set(false);
                    LOGGER.log(Level.SEVERE, "Error loading tasks", error);
                    setErrorMessage("Failed to load tasks: " + error.getMessage());
                });
                return null;
            });
            
        } catch (Exception e) {
            loading.set(false);
            LOGGER.log(Level.SEVERE, "Error loading tasks", e);
            setErrorMessage("Failed to load tasks: " + e.getMessage());
        }
    }
    
    /**
     * Asynchronously adds a task that requires this component.
     * This is a placeholder - the actual task selection would be done by the presenter.
     */
    public void addTaskAsync() {
        // This is a placeholder - the actual implementation would be provided by the presenter
        LOGGER.info("Add task action triggered asynchronously");
    }
    
    /**
     * Asynchronously adds a task to the component's required tasks.
     * This is called from the presenter after task selection.
     * 
     * @param task the task to add
     * @return a CompletableFuture that will be completed with a boolean indicating success
     */
    public CompletableFuture<Boolean> addTaskAsync(Task task) {
        CompletableFuture<Boolean> resultFuture = new CompletableFuture<>();
        
        try {
            if (task == null) {
                LOGGER.warning("Cannot add null task");
                resultFuture.complete(false);
                return resultFuture;
            }
            
            Component component = getComponent();
            if (component == null || component.getId() == null) {
                setErrorMessage("Component not found");
                resultFuture.complete(false);
                return resultFuture;
            }
            
            // Check if the task is already in the list
            Field requiredForTasksField = ComponentViewModel.class.getDeclaredField("requiredForTasks");
            requiredForTasksField.setAccessible(true);
            List<Task> requiredForTasks = (List<Task>) requiredForTasksField.get(this);
            
            if (requiredForTasks.contains(task)) {
                setErrorMessage("Task is already associated with this component");
                resultFuture.complete(false);
                return resultFuture;
            }
            
            loading.set(true);
            
            // Create a set of component IDs to associate
            java.util.Set<Long> componentIds = new java.util.HashSet<>();
            componentIds.add(component.getId());
            
            // Use the async service to associate the component with the task
            taskServiceAsync.associateComponentsWithTaskAsync(
                task.getId(),
                componentIds,
                updatedTask -> {
                    Platform.runLater(() -> {
                        try {
                            // Reload tasks to reflect the changes
                            loadTasksAsync();
                            
                            // Mark as dirty
                            setDirty(true);
                            
                            resultFuture.complete(true);
                        } catch (Exception e) {
                            LOGGER.log(Level.SEVERE, "Error updating task association", e);
                            setErrorMessage("Error updating task association: " + e.getMessage());
                            resultFuture.complete(false);
                        }
                    });
                },
                error -> {
                    Platform.runLater(() -> {
                        loading.set(false);
                        LOGGER.log(Level.SEVERE, "Error adding task", error);
                        setErrorMessage("Failed to add task: " + error.getMessage());
                        resultFuture.complete(false);
                    });
                }
            );
            
        } catch (Exception e) {
            loading.set(false);
            LOGGER.log(Level.SEVERE, "Error adding task", e);
            setErrorMessage("Failed to add task: " + e.getMessage());
            resultFuture.complete(false);
        }
        
        return resultFuture;
    }
    
    /**
     * Asynchronously removes a task from the component's required tasks.
     */
    public void removeTaskAsync() {
        try {
            Component component = getComponent();
            Task selectedTask = getSelectedTask();
            
            if (component == null || component.getId() == null || 
                selectedTask == null || selectedTask.getId() == null || 
                loading.get()) {
                return;
            }
            
            loading.set(true);
            
            // Create an empty set of component IDs to remove all associations
            java.util.Set<Long> emptySet = new java.util.HashSet<>();
            
            // Use the async service to disassociate the component from the task
            taskServiceAsync.associateComponentsWithTaskAsync(
                selectedTask.getId(),
                emptySet,
                updatedTask -> {
                    Platform.runLater(() -> {
                        try {
                            // Reload tasks to reflect the changes
                            loadTasksAsync();
                            
                            // Mark as dirty
                            setDirty(true);
                        } catch (Exception e) {
                            LOGGER.log(Level.SEVERE, "Error updating task association", e);
                            setErrorMessage("Error updating task association: " + e.getMessage());
                        } finally {
                            loading.set(false);
                        }
                    });
                },
                error -> {
                    Platform.runLater(() -> {
                        loading.set(false);
                        LOGGER.log(Level.SEVERE, "Error removing task", error);
                        setErrorMessage("Failed to remove task: " + error.getMessage());
                    });
                }
            );
            
        } catch (Exception e) {
            loading.set(false);
            LOGGER.log(Level.SEVERE, "Error removing task", e);
            setErrorMessage("Failed to remove task: " + e.getMessage());
        }
    }
    
    /**
     * Handles successful save operation.
     * 
     * @param savedComponent the saved component
     */
    private void handleSaveSuccess(Component savedComponent) {
        Platform.runLater(() -> {
            try {
                // Update the component reference with the saved one
                Field componentField = ComponentViewModel.class.getDeclaredField("component");
                componentField.setAccessible(true);
                componentField.set(this, savedComponent);
                
                // Not dirty after save
                setDirty(false);
                
                // Clear error message
                clearErrorMessage();
                
                // Update the isNewComponent property if this was a new component
                if (savedComponent.getId() != null) {
                    Field isNewComponentField = ComponentViewModel.class.getDeclaredField("isNewComponent");
                    isNewComponentField.setAccessible(true);
                    BooleanProperty isNewComponentProperty = (BooleanProperty) isNewComponentField.get(this);
                    isNewComponentProperty.set(false);
                }
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error updating ViewModel after save", e);
                setErrorMessage("Error updating ViewModel after save: " + e.getMessage());
            } finally {
                loading.set(false);
            }
        });
    }
    
    /**
     * Handles errors from async operations.
     * 
     * @param error the error that occurred
     */
    private void handleError(Throwable error) {
        Platform.runLater(() -> {
            loading.set(false);
            LOGGER.log(Level.SEVERE, "Error in async operation", error);
            setErrorMessage("Error: " + error.getMessage());
        });
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
     * Gets whether the ViewModel is currently loading data.
     * 
     * @return true if loading, false otherwise
     */
    public boolean isLoading() {
        return loading.get();
    }
    
    /**
     * Gets the async save command.
     * 
     * @return the async save command
     */
    public Command getAsyncSaveCommand() {
        return asyncSaveCommand;
    }
    
    /**
     * Gets the async load tasks command.
     * 
     * @return the async load tasks command
     */
    public Command getAsyncLoadTasksCommand() {
        return asyncLoadTasksCommand;
    }
    
    /**
     * Gets the async add task command.
     * 
     * @return the async add task command
     */
    public Command getAsyncAddTaskCommand() {
        return asyncAddTaskCommand;
    }
    
    /**
     * Gets the async remove task command.
     * 
     * @return the async remove task command
     */
    public Command getAsyncRemoveTaskCommand() {
        return asyncRemoveTaskCommand;
    }
    
    // Override parent methods to use async implementations
    
    @Override
    public void loadTasks() {
        loadTasksAsync();
    }
    
    @Override
    public boolean addTask(Task task) {
        CompletableFuture<Boolean> future = addTaskAsync(task);
        try {
            return future.join(); // This will wait for the future to complete
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error adding task", e);
            return false;
        }
    }
    
    @Override
    private void save() {
        saveAsync();
    }
    
    @Override
    private void removeTask() {
        removeTaskAsync();
    }
}