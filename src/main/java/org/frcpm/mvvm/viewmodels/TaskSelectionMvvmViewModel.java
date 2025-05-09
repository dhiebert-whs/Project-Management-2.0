// src/main/java/org/frcpm/mvvm/viewmodels/TaskSelectionMvvmViewModel.java
package org.frcpm.mvvm.viewmodels;

import de.saxsys.mvvmfx.utils.commands.Command;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.mvvm.BaseMvvmViewModel;
import org.frcpm.mvvm.async.MvvmAsyncHelper;
import org.frcpm.services.TaskService;
import org.frcpm.services.impl.TaskServiceAsyncImpl;

/**
 * ViewModel for task selection dialog using MVVMFx.
 */
public class TaskSelectionMvvmViewModel extends BaseMvvmViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(TaskSelectionMvvmViewModel.class.getName());
    
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
    private Command selectTaskCommand;
    private Command cancelCommand;
    
    /**
     * Creates a new TaskSelectionMvvmViewModel.
     * 
     * @param taskService the task service
     */
    @Inject
    public TaskSelectionMvvmViewModel(TaskService taskService) {
        this.taskService = taskService;
        
        // Cast to the async implementation
        this.taskServiceAsync = (TaskServiceAsyncImpl) taskService;
        
        initializeCommands();
    }
    
    /**
     * Initializes the commands for this view model.
     */
    private void initializeCommands() {
        // Load tasks command
        loadTasksCommand = MvvmAsyncHelper.createSimpleAsyncCommand(this::loadTasksAsync);
        
        // Select task command
        selectTaskCommand = createValidOnlyCommand(
            () -> {
                // This will be handled by the view/controller
                LOGGER.info("Task selected: " + 
                    (selectedTask.get() != null ? selectedTask.get().getTitle() : "null"));
            },
            () -> selectedTask.get() != null
        );
        
        // Cancel command
        cancelCommand = MvvmAsyncHelper.createSimpleAsyncCommand(() -> {
            LOGGER.info("Task selection canceled");
        });
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
     * Loads tasks asynchronously.
     */
    private void loadTasksAsync() {
        loading.set(true);
        
        if (currentProject.get() == null) {
            // Load all tasks if no project specified
            taskServiceAsync.findAllAsync(
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
        } else {
            // Load tasks for the current project
            taskServiceAsync.findByProjectAsync(
                currentProject.get(),
                // Success callback
                result -> {
                    Platform.runLater(() -> {
                        tasks.clear();
                        tasks.addAll(result);
                        clearErrorMessage();
                        loading.set(false);
                        LOGGER.info("Loaded " + result.size() + " tasks for project asynchronously");
                    });
                },
                // Error callback
                error -> {
                    Platform.runLater(() -> {
                        LOGGER.log(Level.SEVERE, "Error loading tasks for project asynchronously", error);
                        setErrorMessage("Failed to load tasks: " + error.getMessage());
                        loading.set(false);
                    });
                }
            );
        }
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
     * Gets the select task command.
     * 
     * @return the select task command
     */
    public Command getSelectTaskCommand() {
        return selectTaskCommand;
    }
    
    /**
     * Gets the cancel command.
     * 
     * @return the cancel command
     */
    public Command getCancelCommand() {
        return cancelCommand;
    }
    
    @Override
    public void dispose() {
        super.dispose();
        tasks.clear();
    }
}