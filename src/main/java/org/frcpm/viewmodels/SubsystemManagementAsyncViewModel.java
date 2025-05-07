// src/main/java/org/frcpm/viewmodels/SubsystemManagementAsyncViewModel.java

package org.frcpm.viewmodels;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;

import org.frcpm.binding.Command;
import org.frcpm.models.Project;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.services.AsyncServiceFactory;
import org.frcpm.services.ProjectService;
import org.frcpm.services.SubsystemService;
import org.frcpm.services.TaskService;
import org.frcpm.services.impl.SubsystemServiceAsyncImpl;
import org.frcpm.services.impl.TaskServiceAsyncImpl;

import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enhanced ViewModel for subsystem management list view with asynchronous operations.
 */
public class SubsystemManagementAsyncViewModel extends SubsystemManagementViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(SubsystemManagementAsyncViewModel.class.getName());
    
    private final SubsystemServiceAsyncImpl subsystemServiceAsync;
    private final TaskServiceAsyncImpl taskServiceAsync;
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    
    private Command asyncLoadSubsystemsCommand;
    private Command asyncDeleteSubsystemCommand;
    
    /**
     * Creates a new SubsystemManagementAsyncViewModel with the specified services.
     * 
     * @param subsystemService the subsystem service
     * @param taskService the task service
     * @param projectService the project service
     */
    public SubsystemManagementAsyncViewModel(SubsystemService subsystemService, TaskService taskService, ProjectService projectService) {
        super(subsystemService, taskService, projectService);
        
        // Get the async service implementations
        this.subsystemServiceAsync = AsyncServiceFactory.getSubsystemService();
        this.taskServiceAsync = AsyncServiceFactory.getTaskService();
        
        // Initialize async commands
        asyncLoadSubsystemsCommand = new Command(this::loadSubsystemsAsync);
        asyncDeleteSubsystemCommand = new Command(
            this::deleteSubsystemAsync,
            () -> getSelectedSubsystem() != null
        );
    }
    
    /**
     * Loads subsystems asynchronously.
     */
    public void loadSubsystemsAsync() {
        loading.set(true);
        
        subsystemServiceAsync.findAllAsync(
            // Success handler
            result -> {
                Platform.runLater(() -> {
                    ObservableList<Subsystem> subsystems = getSubsystems();
                    subsystems.clear();
                    subsystems.addAll(result);
                    loading.set(false);
                    LOGGER.info("Loaded " + result.size() + " subsystems asynchronously");
                });
            },
            // Error handler
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error loading subsystems asynchronously", error);
                    loading.set(false);
                    setErrorMessage("Failed to load subsystems: " + error.getMessage());
                });
            }
        );
    }
    
    /**
     * Deletes the selected subsystem asynchronously.
     */
    public void deleteSubsystemAsync() {
        Subsystem subsystem = getSelectedSubsystem();
        if (subsystem == null) {
            return;
        }
        
        // First check if the subsystem has tasks
        loading.set(true);
        
        // Since findBySubsystemAsync doesn't exist, use findAllAsync and filter
        taskServiceAsync.findAllAsync(
            // Success handler for task check
            tasks -> {
                // Filter for tasks in this subsystem
                List<Task> subsystemTasks = tasks.stream()
                    .filter(task -> task.getSubsystem() != null && 
                            task.getSubsystem().getId().equals(subsystem.getId()))
                    .toList();
                    
                if (subsystemTasks != null && !subsystemTasks.isEmpty()) {
                    Platform.runLater(() -> {
                        loading.set(false);
                        setErrorMessage("Cannot delete a subsystem that has tasks. Reassign or delete tasks first.");
                    });
                } else {
                    // No tasks, proceed with deletion
                    Platform.runLater(() -> {
                        performSubsystemDeletionAsync(subsystem);
                    });
                }
            },
            // Error handler for task check
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error checking subsystem tasks asynchronously", error);
                    loading.set(false);
                    setErrorMessage("Failed to check subsystem tasks: " + error.getMessage());
                });
            }
        );
    }
    
    /**
     * Performs the actual subsystem deletion after task check.
     * 
     * @param subsystem the subsystem to delete
     */
    private void performSubsystemDeletionAsync(Subsystem subsystem) {
        loading.set(true);
        
        subsystemServiceAsync.deleteByIdAsync(
            subsystem.getId(),
            // Success handler
            result -> {
                Platform.runLater(() -> {
                    if (result) {
                        // Remove from subsystems list
                        getSubsystems().remove(subsystem);
                        
                        // Clear selection if this was the selected subsystem
                        if (getSelectedSubsystem() != null && 
                            getSelectedSubsystem().getId().equals(subsystem.getId())) {
                            setSelectedSubsystem(null);
                        }
                        
                        clearErrorMessage();
                        LOGGER.info("Deleted subsystem: " + subsystem.getName() + " asynchronously");
                    } else {
                        setErrorMessage("Failed to delete subsystem: Database operation unsuccessful");
                    }
                    loading.set(false);
                });
            },
            // Error handler
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error deleting subsystem asynchronously", error);
                    loading.set(false);
                    setErrorMessage("Failed to delete subsystem: " + error.getMessage());
                });
            }
        );
    }
    
    /**
     * Deletes a subsystem by its object instance asynchronously.
     * 
     * @param subsystem the subsystem to delete
     */
    public void deleteSubsystemAsync(Subsystem subsystem) {
        if (subsystem == null) {
            return;
        }
        
        // First check if the subsystem has tasks
        loading.set(true);
        
        // Since findBySubsystemAsync doesn't exist, use findAllAsync and filter
        taskServiceAsync.findAllAsync(
            // Success handler for task check
            tasks -> {
                // Filter for tasks in this subsystem
                List<Task> subsystemTasks = tasks.stream()
                    .filter(task -> task.getSubsystem() != null && 
                            task.getSubsystem().getId().equals(subsystem.getId()))
                    .toList();
                    
                if (subsystemTasks != null && !subsystemTasks.isEmpty()) {
                    Platform.runLater(() -> {
                        loading.set(false);
                        setErrorMessage("Cannot delete a subsystem that has tasks. Reassign or delete tasks first.");
                    });
                } else {
                    // No tasks, proceed with deletion
                    Platform.runLater(() -> {
                        performSubsystemDeletionAsync(subsystem);
                    });
                }
            },
            // Error handler for task check
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error checking subsystem tasks asynchronously", error);
                    loading.set(false);
                    setErrorMessage("Failed to check subsystem tasks: " + error.getMessage());
                });
            }
        );
    }
    
    /**
     * Gets the task count for a subsystem asynchronously.
     * 
     * @param subsystem the subsystem
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     */
    public void getTaskCountAsync(Subsystem subsystem, Consumer<Integer> onSuccess, 
                                 Consumer<Throwable> onFailure) {
        if (subsystem == null) {
            onSuccess.accept(0);
            return;
        }
        
        taskServiceAsync.findAllAsync(
            // Success handler
            tasks -> {
                // Filter for tasks in this subsystem
                List<Task> subsystemTasks = tasks.stream()
                    .filter(task -> task.getSubsystem() != null && 
                            task.getSubsystem().getId().equals(subsystem.getId()))
                    .toList();
                    
                int count = subsystemTasks.size();
                Platform.runLater(() -> {
                    onSuccess.accept(count);
                });
            },
            // Error handler
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.WARNING, "Error getting task count asynchronously", error);
                    onFailure.accept(error);
                });
            }
        );
    }
    
    /**
     * Gets the completion percentage for a subsystem asynchronously.
     * 
     * @param subsystem the subsystem
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     */
    public void getCompletionPercentageAsync(Subsystem subsystem, Consumer<Double> onSuccess, 
                                            Consumer<Throwable> onFailure) {
        if (subsystem == null) {
            onSuccess.accept(0.0);
            return;
        }
        
        taskServiceAsync.findAllAsync(
            // Success handler
            tasks -> {
                // Filter for tasks in this subsystem
                List<Task> subsystemTasks = tasks.stream()
                    .filter(task -> task.getSubsystem() != null && 
                            task.getSubsystem().getId().equals(subsystem.getId()))
                    .toList();
                    
                double percentage = 0.0;
                if (!subsystemTasks.isEmpty()) {
                    int total = subsystemTasks.size();
                    int completed = (int) subsystemTasks.stream().filter(Task::isCompleted).count();
                    percentage = total > 0 ? ((double) completed * 100.0 / total) : 0.0;
                }
                
                final double finalPercentage = percentage;
                Platform.runLater(() -> {
                    onSuccess.accept(finalPercentage);
                });
            },
            // Error handler
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.WARNING, "Error getting completion percentage asynchronously", error);
                    onFailure.accept(error);
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
     * Gets the async load subsystems command.
     * 
     * @return the async load subsystems command
     */
    public Command getAsyncLoadSubsystemsCommand() {
        return asyncLoadSubsystemsCommand;
    }
    
    /**
     * Gets the async delete subsystem command.
     * 
     * @return the async delete subsystem command
     */
    public Command getAsyncDeleteSubsystemCommand() {
        return asyncDeleteSubsystemCommand;
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