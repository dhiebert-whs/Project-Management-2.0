// src/main/java/org/frcpm/viewmodels/TaskAsyncViewModel.java

package org.frcpm.viewmodels;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import org.frcpm.binding.Command;
import org.frcpm.models.Component;
import org.frcpm.models.Project;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.models.TeamMember;
import org.frcpm.services.AsyncServiceFactory;
import org.frcpm.services.ComponentService;
import org.frcpm.services.TaskService;
import org.frcpm.services.TeamMemberService;
import org.frcpm.services.impl.TaskServiceAsyncImpl;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enhanced ViewModel for Task management with asynchronous operations.
 */
public class TaskAsyncViewModel extends TaskViewModel {

    private static final Logger LOGGER = Logger.getLogger(TaskAsyncViewModel.class.getName());
    
    private final TaskServiceAsyncImpl taskServiceAsync;
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    
    private Command asyncSaveCommand;
    
    /**
     * Creates a new TaskAsyncViewModel with default services.
     */
    public TaskAsyncViewModel(TaskService taskService, TeamMemberService teamMemberService, 
                             ComponentService componentService) {
        super(taskService, teamMemberService, componentService);
        
        // Get the async service implementation
        this.taskServiceAsync = AsyncServiceFactory.getTaskService();
        
        // Initialize async commands
        asyncSaveCommand = new Command(this::saveAsync, this::isValid);
    }
    
    /**
     * Saves the task asynchronously.
     */
    public void saveAsync() {
        if (!isValid()) {
            return;
        }
        
        loading.set(true);
        
        try {
            if (isNewTask()) {
                // Create new task
                taskServiceAsync.createTaskAsync(
                    getTitle(),
                    getProject(),
                    getSubsystem(),
                    getEstimatedHours(),
                    getPriority(),
                    getStartDate(),
                    getEndDate(),
                    // Success handler
                    savedTask -> {
                        Platform.runLater(() -> {
                            // Update description
                            savedTask.setDescription(getDescription());
                            
                            // Handle assigned members
                            if (!getAssignedMembers().isEmpty()) {
                                Set<TeamMember> members = new HashSet<>(getAssignedMembers());
                                handleAssignMembersAsync(savedTask, members);
                            } else {
                                // If no members to assign, continue with dependencies
                                handleDependenciesAsync(savedTask);
                            }
                        });
                    },
                    // Error handler
                    error -> {
                        Platform.runLater(() -> {
                            LOGGER.log(Level.SEVERE, "Error creating task asynchronously", error);
                            loading.set(false);
                            setErrorMessage("Failed to create task: " + error.getMessage());
                        });
                    }
                );
            } else {
                // Update existing task
                Task existingTask = getTask();
                existingTask.setTitle(getTitle());
                existingTask.setDescription(getDescription());
                existingTask.setEstimatedDuration(Duration.ofMinutes((long) (getEstimatedHours() * 60)));
                
                if (getActualHours() > 0) {
                    existingTask.setActualDuration(Duration.ofMinutes((long) (getActualHours() * 60)));
                }
                
                existingTask.setPriority(getPriority());
                existingTask.setStartDate(getStartDate());
                existingTask.setEndDate(getEndDate());
                
                // Update progress and completion status
                taskServiceAsync.updateTaskProgressAsync(
                    existingTask.getId(),
                    getProgress(),
                    isCompleted(),
                    // Success handler
                    updatedTask -> {
                        Platform.runLater(() -> {
                            // Handle assigned members
                            if (!getAssignedMembers().isEmpty()) {
                                Set<TeamMember> members = new HashSet<>(getAssignedMembers());
                                handleAssignMembersAsync(updatedTask, members);
                            } else {
                                // If no members to assign, continue with dependencies
                                handleDependenciesAsync(updatedTask);
                            }
                        });
                    },
                    // Error handler
                    error -> {
                        Platform.runLater(() -> {
                            LOGGER.log(Level.SEVERE, "Error updating task progress asynchronously", error);
                            loading.set(false);
                            setErrorMessage("Failed to update task progress: " + error.getMessage());
                        });
                    }
                );
            }
        } catch (Exception e) {
            loading.set(false);
            LOGGER.log(Level.SEVERE, "Error in saveAsync method", e);
            setErrorMessage("Failed to save task: " + e.getMessage());
        }
    }
    
    /**
     * Handles assigning members to a task asynchronously.
     * 
     * @param task the task to update
     * @param members the members to assign
     */
    private void handleAssignMembersAsync(Task task, Set<TeamMember> members) {
        taskServiceAsync.assignMembersAsync(
            task.getId(),
            members,
            // Success handler
            updatedTask -> {
                // Continue with dependencies
                Platform.runLater(() -> {
                    handleDependenciesAsync(updatedTask);
                });
            },
            // Error handler
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error assigning members asynchronously", error);
                    loading.set(false);
                    setErrorMessage("Failed to assign members: " + error.getMessage());
                });
            }
        );
    }
    
    /**
     * Handles adding dependencies to a task asynchronously.
     * 
     * @param task the task to update
     */
    private void handleDependenciesAsync(Task task) {
        // Update task reference
        setTask(task);
        
        // Handle components
        handleComponentsAsync(task);
        
        // If we had to handle dependencies asynchronously, we would do it here
        // But for now, we'll just continue to the next step
    }
    
    /**
     * Handles adding components to a task asynchronously.
     * 
     * @param task the task to update
     */
    private void handleComponentsAsync(Task task) {
        // Update components in the background if needed
        
        // For now, just finalize the task
        finalizeTaskAsync(task);
    }
    
    /**
     * Finalizes the task save operation.
     * 
     * @param task the saved task
     */
    private void finalizeTaskAsync(Task task) {
        Platform.runLater(() -> {
            // Update task property with saved task
            setTask(task);
            
            // Clear dirty flag
            setDirty(false);
            loading.set(false);
            
            LOGGER.info("Saved task asynchronously: " + task.getTitle());
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
}