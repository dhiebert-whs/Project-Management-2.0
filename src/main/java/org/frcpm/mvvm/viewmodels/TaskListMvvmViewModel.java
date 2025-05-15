// src/main/java/org/frcpm/mvvm/viewmodels/TaskListMvvmViewModel.java

package org.frcpm.mvvm.viewmodels;

import de.saxsys.mvvmfx.utils.commands.Command;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.mvvm.BaseMvvmViewModel;
import org.frcpm.mvvm.async.MvvmAsyncHelper;
import org.frcpm.services.TaskService;
import org.frcpm.services.impl.TaskServiceAsyncImpl;

/**
 * ViewModel for the TaskList view using MVVMFx.
 */
public class TaskListMvvmViewModel extends BaseMvvmViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(TaskListMvvmViewModel.class.getName());
    
    // Service dependencies
    private final TaskService taskService;
    private final TaskServiceAsyncImpl taskServiceAsync;
    
    // Observable collections and properties
    private final ObservableList<Task> tasks = FXCollections.observableArrayList();
    private final ObjectProperty<Task> selectedTask = new SimpleObjectProperty<>();
    private final ObjectProperty<Project> currentProject = new SimpleObjectProperty<>();
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    
    // Commands
    private Command loadTasksCommand;
    private Command newTaskCommand;
    private Command editTaskCommand;
    private Command deleteTaskCommand;
    private Command refreshTasksCommand;
    
    /**
     * Creates a new TaskListMvvmViewModel.
     * 
     * @param taskService the task service
     */
   
    public TaskListMvvmViewModel(TaskService taskService) {
        this.taskService = taskService;
        
        // Get the async service implementation - we need to cast since we're using the specific implementation
        this.taskServiceAsync = (TaskServiceAsyncImpl) taskService;
        
        initializeCommands();
    }
    
    /**
     * Initializes the commands for this view model.
     */
    private void initializeCommands() {
        // Load tasks command
        loadTasksCommand = MvvmAsyncHelper.createSimpleAsyncCommand(this::loadTasksAsync);
        
        // New task command
        newTaskCommand = createValidOnlyCommand(
            () -> {
                // This will be handled by the view/controller
                LOGGER.info("New task command executed");
            },
            () -> currentProject.get() != null
        );
        
        // Edit task command
        editTaskCommand = createValidOnlyCommand(
            () -> {
                // This will be handled by the view/controller
                LOGGER.info("Edit task command executed for: " + 
                    (selectedTask.get() != null ? selectedTask.get().getTitle() : "null"));
            },
            () -> selectedTask.get() != null
        );
        
        // Delete task command
        deleteTaskCommand = createValidOnlyCommand(
            this::deleteTaskAsync,
            () -> selectedTask.get() != null
        );
        
        // Refresh tasks command
        refreshTasksCommand = MvvmAsyncHelper.createSimpleAsyncCommand(this::loadTasksAsync);
    }
    
    /**
     * Initializes the view model with a project.
     * 
     * @param project the project
     */
    public void initWithProject(Project project) {
        currentProject.set(project);
        loadTasksAsync();
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
     * Gets the current project property.
     * 
     * @return the current project property
     */
    public ObjectProperty<Project> currentProjectProperty() {
        return currentProject;
    }
    
    /**
     * Gets the current project.
     * 
     * @return the current project
     */
    public Project getCurrentProject() {
        return currentProject.get();
    }
    
    /**
     * Sets the current project.
     * 
     * @param project the project
     */
    public void setCurrentProject(Project project) {
        currentProject.set(project);
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
     * Checks if the view model is loading data.
     * 
     * @return true if loading, false otherwise
     */
    public boolean isLoading() {
        return loading.get();
    }
    
    /**
     * Gets the load tasks command.
     * 
     * @return the load tasks command
     */
    public Command getLoadTasksCommand() {
        return loadTasksCommand;
    }
    
    /**
     * Gets the new task command.
     * 
     * @return the new task command
     */
    public Command getNewTaskCommand() {
        return newTaskCommand;
    }
    
    /**
     * Gets the edit task command.
     * 
     * @return the edit task command
     */
    public Command getEditTaskCommand() {
        return editTaskCommand;
    }
    
    /**
     * Gets the delete task command.
     * 
     * @return the delete task command
     */
    public Command getDeleteTaskCommand() {
        return deleteTaskCommand;
    }
    
    /**
     * Gets the refresh tasks command.
     * 
     * @return the refresh tasks command
     */
    public Command getRefreshTasksCommand() {
        return refreshTasksCommand;
    }
    
    /**
     * Loads tasks asynchronously.
     */
    private void loadTasksAsync() {
        if (currentProject.get() == null) {
            return;
        }
        
        loading.set(true);
        
        // Use the specific async method from TaskServiceAsyncImpl
        taskServiceAsync.findByProjectAsync(
            currentProject.get(),
            // Success callback
            result -> {
                Platform.runLater(() -> {
                    tasks.clear();
                    tasks.addAll(result);
                    clearErrorMessage();
                    loading.set(false);
                    LOGGER.info("Loaded " + result.size() + " tasks asynchronously");
                });
            },
            // Error callback
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error loading tasks asynchronously", error);
                    setErrorMessage("Failed to load tasks: " + error.getMessage());
                    loading.set(false);
                });
            }
        );
    }
    
    /**
     * Deletes a task asynchronously.
     */
    private void deleteTaskAsync() {
        Task task = selectedTask.get();
        if (task == null || task.getId() == null) {
            return;
        }
        
        loading.set(true);
        
        // Using the generic deleteByIdAsync from AbstractAsyncService
        taskServiceAsync.deleteByIdAsync(
            task.getId(),
            // Success callback
            result -> {
                Platform.runLater(() -> {
                    if (result) {
                        tasks.remove(task);
                        selectedTask.set(null);
                        clearErrorMessage();
                        LOGGER.info("Deleted task: " + task.getTitle() + " asynchronously");
                    } else {
                        LOGGER.warning("Failed to delete task: " + task.getTitle() + " asynchronously");
                        setErrorMessage("Failed to delete task: Database operation unsuccessful");
                    }
                    loading.set(false);
                });
            },
            // Error callback
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error deleting task asynchronously", error);
                    setErrorMessage("Failed to delete task: " + error.getMessage());
                    loading.set(false);
                });
            }
        );
    }
}