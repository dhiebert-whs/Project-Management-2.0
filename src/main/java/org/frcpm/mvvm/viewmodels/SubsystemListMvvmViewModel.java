// src/main/java/org/frcpm/mvvm/viewmodels/SubsystemListMvvmViewModel.java
package org.frcpm.mvvm.viewmodels;

import de.saxsys.mvvmfx.utils.commands.Command;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.ProgressBar;


import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.frcpm.models.Subsystem;
import org.frcpm.models.Project;
import org.frcpm.mvvm.BaseMvvmViewModel;
import org.frcpm.mvvm.async.MvvmAsyncHelper;
import org.frcpm.services.SubsystemService;
import org.frcpm.services.impl.SubsystemServiceAsyncImpl;

/**
 * ViewModel for the SubsystemList view using MVVMFx.
 */
public class SubsystemListMvvmViewModel extends BaseMvvmViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(SubsystemListMvvmViewModel.class.getName());
    
    /**
     * Enum for filtering subsystems.
     */
    public enum SubsystemFilter {
        ALL("All Subsystems"),
        COMPLETED("Completed"),
        IN_PROGRESS("In Progress"),
        NOT_STARTED("Not Started"),
        TESTING("Testing"),
        ISSUES("Issues");
        
        private final String displayName;
        
        SubsystemFilter(String displayName) {
            this.displayName = displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    // Service dependencies
    private final SubsystemService subsystemService;
    private final SubsystemServiceAsyncImpl subsystemServiceAsync;
    
    // Observable collections and properties
    private final ObservableList<Subsystem> allSubsystems = FXCollections.observableArrayList();
    private final FilteredList<Subsystem> filteredSubsystems = new FilteredList<>(allSubsystems);
    private final ObjectProperty<Subsystem> selectedSubsystem = new SimpleObjectProperty<>();
    private final ObjectProperty<Project> currentProject = new SimpleObjectProperty<>();
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    
    // Current filter
    private SubsystemFilter currentFilter = SubsystemFilter.ALL;
    
    // Commands
    private Command loadSubsystemsCommand;
    private Command newSubsystemCommand;
    private Command editSubsystemCommand;
    private Command deleteSubsystemCommand;
    private Command refreshSubsystemsCommand;
    
    /**
     * Creates a new SubsystemListMvvmViewModel.
     * 
     * @param subsystemService the subsystem service
     */
   
    public SubsystemListMvvmViewModel(SubsystemService subsystemService) {
        this.subsystemService = subsystemService;
        
        // Cast to the async implementation
        this.subsystemServiceAsync = (SubsystemServiceAsyncImpl) subsystemService;
        
        initializeCommands();
        updateFilterPredicate();
    }
    
    /**
     * Initializes the commands for this view model.
     */
    private void initializeCommands() {
        // Load subsystems command
        loadSubsystemsCommand = MvvmAsyncHelper.createSimpleAsyncCommand(this::loadSubsystemsAsync);
        
        // New subsystem command
        newSubsystemCommand = createValidOnlyCommand(
            () -> {
                // This will be handled by the view/controller
                LOGGER.info("New subsystem command executed");
            },
            () -> true // Always enabled
        );
        
        // Edit subsystem command
        editSubsystemCommand = createValidOnlyCommand(
            () -> {
                // This will be handled by the view/controller
                LOGGER.info("Edit subsystem command executed for: " + 
                    (selectedSubsystem.get() != null ? selectedSubsystem.get().getName() : "null"));
            },
            () -> selectedSubsystem.get() != null
        );
        
        // Delete subsystem command
        deleteSubsystemCommand = createValidOnlyCommand(
            this::deleteSubsystemAsync,
            () -> selectedSubsystem.get() != null
        );
        
        // Refresh subsystems command
        refreshSubsystemsCommand = MvvmAsyncHelper.createSimpleAsyncCommand(this::loadSubsystemsAsync);
    }
    
    /**
     * Loads subsystems asynchronously.
     */
    private void loadSubsystemsAsync() {
        loading.set(true);
        
        // Find all subsystems asynchronously
        subsystemServiceAsync.findAllAsync(
            // Success callback
            result -> {
                Platform.runLater(() -> {
                    allSubsystems.clear();
                    allSubsystems.addAll(result);
                    clearErrorMessage();
                    loading.set(false);
                    LOGGER.info("Loaded " + result.size() + " subsystems asynchronously");
                });
            },
            // Error callback
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error loading subsystems asynchronously", error);
                    setErrorMessage("Failed to load subsystems: " + error.getMessage());
                    loading.set(false);
                });
            }
        );
    }
    
    /**
     * Sets the filter and updates the filtered list.
     * 
     * @param filter the filter to apply
     */
    public void setFilter(SubsystemFilter filter) {
        if (filter == null) {
            LOGGER.warning("Attempting to set null filter, using ALL instead");
            filter = SubsystemFilter.ALL;
        }
        
        this.currentFilter = filter;
        updateFilterPredicate();
    }
    
    /**
     * Updates the filter predicate based on the current filter.
     */
    private void updateFilterPredicate() {
        filteredSubsystems.setPredicate(buildFilterPredicate());
    }
    
    /**
     * Builds a predicate based on the current filter.
     * 
     * @return the predicate
     */
    private Predicate<Subsystem> buildFilterPredicate() {
        return subsystem -> {
            if (subsystem == null) {
                return false;
            }
            
            switch (currentFilter) {
                case ALL:
                    return true;
                case COMPLETED:
                    return subsystem.getStatus() == Subsystem.Status.COMPLETED;
                case IN_PROGRESS:
                    return subsystem.getStatus() == Subsystem.Status.IN_PROGRESS;
                case NOT_STARTED:
                    return subsystem.getStatus() == Subsystem.Status.NOT_STARTED;
                case TESTING:
                    return subsystem.getStatus() == Subsystem.Status.TESTING;
                case ISSUES:
                    return subsystem.getStatus() == Subsystem.Status.ISSUES;
                default:
                    return true;
            }
        };
    }
    
    /**
     * Deletes a subsystem asynchronously.
     */
    private void deleteSubsystemAsync() {
        Subsystem subsystem = selectedSubsystem.get();
        if (subsystem == null || subsystem.getId() == null) {
            return;
        }
        
        // Check if the subsystem is used by any tasks
        if (subsystem.getTasks() != null && !subsystem.getTasks().isEmpty()) {
            int taskCount = subsystem.getTasks().size();
            setErrorMessage("Cannot delete subsystem because it has " + taskCount + 
                    " task(s). Please remove these dependencies first.");
            return;
        }
        
        loading.set(true);
        
        // Using the async service method
        subsystemServiceAsync.deleteByIdAsync(
            subsystem.getId(),
            // Success callback
            result -> {
                Platform.runLater(() -> {
                    if (result) {
                        allSubsystems.remove(subsystem);
                        selectedSubsystem.set(null);
                        clearErrorMessage();
                        LOGGER.info("Deleted subsystem: " + subsystem.getName() + " asynchronously");
                    } else {
                        LOGGER.warning("Failed to delete subsystem: " + subsystem.getName() + " asynchronously");
                        setErrorMessage("Failed to delete subsystem: Database operation unsuccessful");
                    }
                    loading.set(false);
                });
            },
            // Error callback
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error deleting subsystem asynchronously", error);
                    setErrorMessage("Failed to delete subsystem: " + error.getMessage());
                    loading.set(false);
                });
            }
        );
    }
    
    /**
     * Gets the task count for a subsystem.
     * 
     * @param subsystem the subsystem
     * @return the task count
     */
    public int getTaskCount(Subsystem subsystem) {
        if (subsystem == null || subsystem.getTasks() == null) {
            return 0;
        }
        return subsystem.getTasks().size();
    }
    
    /**
     * Gets the completion percentage for a subsystem.
     * 
     * @param subsystem the subsystem
     * @return the completion percentage
     */
    public double getCompletionPercentage(Subsystem subsystem) {
        if (subsystem == null || subsystem.getTasks() == null || subsystem.getTasks().isEmpty()) {
            return 0.0;
        }
        
        int taskCount = subsystem.getTasks().size();
        int completedCount = 0;
        int totalProgress = 0;
        
        for (org.frcpm.models.Task task : subsystem.getTasks()) {
            if (task.isCompleted()) {
                completedCount++;
            }
            totalProgress += task.getProgress();
        }
        
        return taskCount > 0 ? (double) totalProgress / taskCount : 0.0;
    }
    
    // Getters and setters
    
    /**
     * Gets the subsystems list.
     * 
     * @return the subsystems list
     */
    public ObservableList<Subsystem> getSubsystems() {
        return filteredSubsystems;
    }
    
    /**
     * Gets the selected subsystem property.
     * 
     * @return the selected subsystem property
     */
    public ObjectProperty<Subsystem> selectedSubsystemProperty() {
        return selectedSubsystem;
    }
    
    /**
     * Gets the selected subsystem.
     * 
     * @return the selected subsystem
     */
    public Subsystem getSelectedSubsystem() {
        return selectedSubsystem.get();
    }
    
    /**
     * Sets the selected subsystem.
     * 
     * @param subsystem the selected subsystem
     */
    public void setSelectedSubsystem(Subsystem subsystem) {
        selectedSubsystem.set(subsystem);
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
     * Gets the current filter.
     * 
     * @return the current filter
     */
    public SubsystemFilter getCurrentFilter() {
        return currentFilter;
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
     * Gets the load subsystems command.
     * 
     * @return the load subsystems command
     */
    public Command getLoadSubsystemsCommand() {
        return loadSubsystemsCommand;
    }
    
    /**
     * Gets the new subsystem command.
     * 
     * @return the new subsystem command
     */
    public Command getNewSubsystemCommand() {
        return newSubsystemCommand;
    }
    
    /**
     * Gets the edit subsystem command.
     * 
     * @return the edit subsystem command
     */
    public Command getEditSubsystemCommand() {
        return editSubsystemCommand;
    }
    
    /**
     * Gets the delete subsystem command.
     * 
     * @return the delete subsystem command
     */
    public Command getDeleteSubsystemCommand() {
        return deleteSubsystemCommand;
    }
    
    /**
     * Gets the refresh subsystems command.
     * 
     * @return the refresh subsystems command
     */
    public Command getRefreshSubsystemsCommand() {
        return refreshSubsystemsCommand;
    }
    
    @Override
    public void dispose() {
        super.dispose();
        allSubsystems.clear();
    }
}