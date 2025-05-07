// src/main/java/org/frcpm/viewmodels/SubsystemAsyncViewModel.java
package org.frcpm.viewmodels;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;

import org.frcpm.binding.Command;
import org.frcpm.models.Subteam;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.services.AsyncServiceFactory;
import org.frcpm.services.SubsystemService;
import org.frcpm.services.SubteamService;
import org.frcpm.services.TaskService;
import org.frcpm.services.impl.SubsystemServiceAsyncImpl;
import org.frcpm.services.impl.TaskServiceAsyncImpl;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enhanced ViewModel for subsystem management with asynchronous operations.
 */
public class SubsystemAsyncViewModel extends SubsystemViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(SubsystemAsyncViewModel.class.getName());
    
    private final SubsystemServiceAsyncImpl subsystemServiceAsync;
    private final TaskServiceAsyncImpl taskServiceAsync;
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    
    private Command asyncSaveCommand;
    private Command asyncLoadSubteamsCommand;
    private Command asyncLoadTasksCommand;
    
    /**
     * Creates a new SubsystemAsyncViewModel with default services.
     */
    public SubsystemAsyncViewModel() {
        super();
        this.subsystemServiceAsync = AsyncServiceFactory.getSubsystemService();
        this.taskServiceAsync = AsyncServiceFactory.getTaskService();
        initAsyncCommands();
    }
    
    /**
     * Creates a new SubsystemAsyncViewModel with the specified services.
     * 
     * @param subsystemService the subsystem service
     * @param subteamService the subteam service
     * @param taskService the task service
     */
    public SubsystemAsyncViewModel(SubsystemService subsystemService, SubteamService subteamService, TaskService taskService) {
        super(subsystemService, subteamService, taskService);
        
        // Get the async service implementations
        this.subsystemServiceAsync = AsyncServiceFactory.getSubsystemService();
        this.taskServiceAsync = AsyncServiceFactory.getTaskService();
        
        initAsyncCommands();
    }
    
    /**
     * Initialize async commands
     */
    private void initAsyncCommands() {
        // Initialize async commands with proper lambdas
        asyncSaveCommand = new Command(
            this::saveAsync, 
            () -> {
                try {
                    // Access the valid property via reflection since it's private
                    java.lang.reflect.Field validField = SubsystemViewModel.class.getDeclaredField("valid");
                    validField.setAccessible(true);
                    BooleanProperty validProperty = (BooleanProperty) validField.get(this);
                    
                    return validProperty.get() && this.isDirty();
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error accessing valid field", e);
                    return false;
                }
            }
        );
        
        asyncLoadSubteamsCommand = new Command(this::loadSubteamsAsync);
        asyncLoadTasksCommand = new Command(
            this::loadTasksAsync,
            () -> getSelectedSubsystem() != null
        );
    }
    
    /**
     * Loads subteams asynchronously.
     */
    public void loadSubteamsAsync() {
        loading.set(true);
        
        // Load subteams
        try {
            // In a real implementation, this would use a SubteamServiceAsync
            // Since we don't have that, we'll just use a synchronous call in a try-catch
            super.loadSubteams();
            Platform.runLater(() -> {
                loading.set(false);
                LOGGER.info("Loaded subteams asynchronously");
            });
        } catch (Exception e) {
            Platform.runLater(() -> {
                LOGGER.log(Level.SEVERE, "Error loading subteams asynchronously", e);
                loading.set(false);
                setErrorMessage("Failed to load subteams: " + e.getMessage());
            });
        }
    }
    
    /**
     * Loads tasks for the selected subsystem asynchronously.
     */
    public void loadTasksAsync() {
        Subsystem subsystem = getSelectedSubsystem();
        if (subsystem == null) {
            return;
        }
        
        loading.set(true);
        
        // Use findAll since we don't have a specific findBySubsystem method
        taskServiceAsync.findAllAsync(
            // Success handler
            result -> {
                Platform.runLater(() -> {
                    // Filter tasks for this subsystem
                    List<Task> subsystemTasks = result.stream()
                        .filter(task -> task.getSubsystem() != null && 
                                task.getSubsystem().getId().equals(subsystem.getId()))
                        .toList();
                        
                    ObservableList<Task> tasks = getTasks();
                    tasks.clear();
                    tasks.addAll(subsystemTasks);
                    
                    // Update summary data
                    int total = subsystemTasks.size();
                    int completed = (int) subsystemTasks.stream().filter(Task::isCompleted).count();
                    double percentage = total > 0 ? ((double)completed * 100.0 / total) : 0.0;
                    
                    totalTasksProperty().set(total);
                    completedTasksProperty().set(completed);
                    completionPercentageProperty().set(percentage);
                    
                    loading.set(false);
                    LOGGER.info("Loaded " + subsystemTasks.size() + " tasks asynchronously for subsystem: " + subsystem.getName());
                });
            },
            // Error handler
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error loading tasks asynchronously", error);
                    loading.set(false);
                    setErrorMessage("Failed to load tasks: " + error.getMessage());
                });
            }
        );
    }
    
    /**
     * Override parent's loadTasks method to use async version
     */
    @Override
    public void loadTasks() {
        loadTasksAsync();
    }
    
    /**
     * Saves the subsystem asynchronously.
     */
    public void saveAsync() {
        try {
            // Revalidate since we're calling directly
            validate();
            
            // Check if valid by getting the value from the field
            boolean isValid = false;
            try {
                java.lang.reflect.Field validField = SubsystemViewModel.class.getDeclaredField("valid");
                validField.setAccessible(true);
                BooleanProperty validProperty = (BooleanProperty) validField.get(this);
                isValid = validProperty.get();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error accessing valid field", e);
            }
            
            if (!isValid) {
                return;
            }
            
            loading.set(true);
            
            // Get if this is a new subsystem
            boolean isNewSubsystem = false;
            try {
                java.lang.reflect.Field isNewSubsystemField = SubsystemViewModel.class.getDeclaredField("isNewSubsystem");
                isNewSubsystemField.setAccessible(true);
                BooleanProperty isNewSubsystemProperty = (BooleanProperty) isNewSubsystemField.get(this);
                isNewSubsystem = isNewSubsystemProperty.get();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error accessing isNewSubsystem field", e);
            }
            
            if (isNewSubsystem) {
                // Create new subsystem
                subsystemServiceAsync.createSubsystemAsync(
                    getSubsystemName(),
                    getSubsystemDescription(),
                    getStatus(),
                    getResponsibleSubteam() != null ? getResponsibleSubteam().getId() : null,
                    // Success handler
                    createdSubsystem -> {
                        Platform.runLater(() -> {
                            // Update the selection with the created subsystem
                            setSelectedSubsystem(createdSubsystem);
                            setDirty(false);
                            loading.set(false);
                            LOGGER.info("Created subsystem asynchronously: " + createdSubsystem.getName());
                        });
                    },
                    // Error handler
                    error -> {
                        Platform.runLater(() -> {
                            LOGGER.log(Level.SEVERE, "Error creating subsystem asynchronously", error);
                            loading.set(false);
                            setErrorMessage("Failed to create subsystem: " + error.getMessage());
                        });
                    }
                );
            } else {
                // Update existing subsystem
                Subsystem subsystem = getSelectedSubsystem();
                if (subsystem == null) {
                    LOGGER.warning("No subsystem selected for update");
                    loading.set(false);
                    setErrorMessage("No subsystem selected for update");
                    return;
                }
                
                // Update properties
                subsystem.setName(getSubsystemName());
                subsystem.setDescription(getSubsystemDescription());
                subsystem.setStatus(getStatus());
                subsystem.setResponsibleSubteam(getResponsibleSubteam());
                
                // Save to database asynchronously
                subsystemServiceAsync.saveAsync(
                    subsystem,
                    // Success handler
                    updatedSubsystem -> {
                        Platform.runLater(() -> {
                            // Update selection
                            setSelectedSubsystem(updatedSubsystem);
                            setDirty(false);
                            loading.set(false);
                            LOGGER.info("Updated subsystem asynchronously: " + updatedSubsystem.getName());
                        });
                    },
                    // Error handler
                    error -> {
                        Platform.runLater(() -> {
                            LOGGER.log(Level.SEVERE, "Error updating subsystem asynchronously", error);
                            loading.set(false);
                            setErrorMessage("Failed to update subsystem: " + error.getMessage());
                        });
                    }
                );
            }
        } catch (Exception e) {
            loading.set(false);
            LOGGER.log(Level.SEVERE, "Error in saveAsync method", e);
            setErrorMessage("Failed to save subsystem: " + e.getMessage());
        }
    }
    
    /**
     * Updates a subsystem's status asynchronously.
     * 
     * @param subsystemId the subsystem ID
     * @param status the new status
     */
    public void updateStatusAsync(Long subsystemId, Subsystem.Status status) {
        if (subsystemId == null) {
            setErrorMessage("Subsystem ID cannot be null");
            return;
        }
        
        if (status == null) {
            setErrorMessage("Status cannot be null");
            return;
        }
        
        loading.set(true);
        
        subsystemServiceAsync.updateStatusAsync(
            subsystemId,
            status,
            // Success handler
            updatedSubsystem -> {
                Platform.runLater(() -> {
                    if (updatedSubsystem != null) {
                        // If this is the currently selected subsystem, update the selection
                        Subsystem currentSelection = getSelectedSubsystem();
                        if (currentSelection != null && currentSelection.getId().equals(updatedSubsystem.getId())) {
                            setSelectedSubsystem(updatedSubsystem);
                        }
                    }
                    loading.set(false);
                    LOGGER.info("Updated status for subsystem ID " + subsystemId + " to " + status + " asynchronously");
                });
            },
            // Error handler
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error updating subsystem status asynchronously", error);
                    loading.set(false);
                    setErrorMessage("Failed to update subsystem status: " + error.getMessage());
                });
            }
        );
    }
    
    /**
     * Assigns a subteam to a subsystem asynchronously.
     * 
     * @param subsystemId the subsystem ID
     * @param subteamId the subteam ID
     */
    public void assignResponsibleSubteamAsync(Long subsystemId, Long subteamId) {
        if (subsystemId == null) {
            setErrorMessage("Subsystem ID cannot be null");
            return;
        }
        
        loading.set(true);
        
        subsystemServiceAsync.assignResponsibleSubteamAsync(
            subsystemId,
            subteamId,
            // Success handler
            updatedSubsystem -> {
                Platform.runLater(() -> {
                    if (updatedSubsystem != null) {
                        // If this is the currently selected subsystem, update the selection
                        Subsystem currentSelection = getSelectedSubsystem();
                        if (currentSelection != null && currentSelection.getId().equals(updatedSubsystem.getId())) {
                            setSelectedSubsystem(updatedSubsystem);
                        }
                    }
                    loading.set(false);
                    LOGGER.info("Assigned subteam ID " + subteamId + " to subsystem ID " + subsystemId + " asynchronously");
                });
            },
            // Error handler
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error assigning subteam to subsystem asynchronously", error);
                    loading.set(false);
                    setErrorMessage("Failed to assign subteam to subsystem: " + error.getMessage());
                });
            }
        );
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
     * Gets whether the view model is currently loading data.
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
     * Gets the async load subteams command.
     * 
     * @return the async load subteams command
     */
    public Command getAsyncLoadSubteamsCommand() {
        return asyncLoadSubteamsCommand;
    }
    
    /**
     * Gets the async load tasks command.
     * 
     * @return the async load tasks command
     */
    public Command getAsyncLoadTasksCommand() {
        return asyncLoadTasksCommand;
    }
    
    @Override
    public void clearErrorMessage() {
        Platform.runLater(() -> {
            super.clearErrorMessage();
        });
    }
    
    @Override
    public void cleanupResources() {
        super.cleanupResources();
    }
}