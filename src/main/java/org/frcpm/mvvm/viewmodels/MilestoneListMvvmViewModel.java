// src/main/java/org/frcpm/mvvm/viewmodels/MilestoneListMvvmViewModel.java

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


import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.mvvm.BaseMvvmViewModel;
import org.frcpm.mvvm.async.MvvmAsyncHelper;
import org.frcpm.services.MilestoneService;
import org.frcpm.services.impl.MilestoneServiceAsyncImpl;

/**
 * ViewModel for the MilestoneList view using MVVMFx.
 */
public class MilestoneListMvvmViewModel extends BaseMvvmViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(MilestoneListMvvmViewModel.class.getName());
    
    /**
     * Enum for filtering milestones.
     */
    public enum MilestoneFilter {
        ALL("All Milestones"),
        UPCOMING("Upcoming"),
        PASSED("Passed");
        
        private final String displayName;
        
        MilestoneFilter(String displayName) {
            this.displayName = displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    // Service dependencies
    private final MilestoneService milestoneService;
    private final MilestoneServiceAsyncImpl milestoneServiceAsync;
    
    // Observable collections and properties
    private final ObservableList<Milestone> allMilestones = FXCollections.observableArrayList();
    private final FilteredList<Milestone> filteredMilestones = new FilteredList<>(allMilestones);
    private final ObjectProperty<Milestone> selectedMilestone = new SimpleObjectProperty<>();
    private final ObjectProperty<Project> currentProject = new SimpleObjectProperty<>();
    private final ObjectProperty<MilestoneFilter> currentFilter = new SimpleObjectProperty<>(MilestoneFilter.ALL);
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    
    // Commands
    private Command loadMilestonesCommand;
    private Command newMilestoneCommand;
    private Command editMilestoneCommand;
    private Command deleteMilestoneCommand;
    private Command refreshMilestonesCommand;
    
    /**
     * Creates a new MilestoneListMvvmViewModel.
     * 
     * @param milestoneService the milestone service
     */
   
    public MilestoneListMvvmViewModel(MilestoneService milestoneService) {
        this.milestoneService = milestoneService;
        
        // Get the async service implementation
        this.milestoneServiceAsync = (MilestoneServiceAsyncImpl) milestoneService;
        
        initializeCommands();
        setupFilters();
    }
    
    /**
     * Initializes the commands for this view model.
     */
    private void initializeCommands() {
        // Load milestones command
        loadMilestonesCommand = MvvmAsyncHelper.createSimpleAsyncCommand(this::loadMilestonesAsync);
        
        // New milestone command
        newMilestoneCommand = createValidOnlyCommand(
            () -> {
                // This will be handled by the view/controller
                LOGGER.info("New milestone command executed");
            },
            () -> currentProject.get() != null
        );
        
        // Edit milestone command
        editMilestoneCommand = createValidOnlyCommand(
            () -> {
                // This will be handled by the view/controller
                LOGGER.info("Edit milestone command executed for: " + 
                    (selectedMilestone.get() != null ? selectedMilestone.get().getName() : "null"));
            },
            () -> selectedMilestone.get() != null
        );
        
        // Delete milestone command
        deleteMilestoneCommand = createValidOnlyCommand(
            this::deleteMilestoneAsync,
            () -> selectedMilestone.get() != null
        );
        
        // Refresh milestones command
        refreshMilestonesCommand = MvvmAsyncHelper.createSimpleAsyncCommand(this::loadMilestonesAsync);
    }
    
    /**
     * Sets up the filter predicates for milestone filtering.
     */
    private void setupFilters() {
        // Setup filter change listener
        currentFilter.addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                applyFilter();
            }
        });
        
        // Apply initial filter
        applyFilter();
    }
    
    /**
     * Applies the current filter to the milestone list.
     */
    public void applyFilter() {
        MilestoneFilter filter = currentFilter.get();
        LocalDate today = LocalDate.now();
        
        Predicate<Milestone> predicate;
        switch (filter) {
            case UPCOMING:
                predicate = milestone -> milestone.getDate().isEqual(today) || milestone.getDate().isAfter(today);
                break;
            case PASSED:
                predicate = milestone -> milestone.getDate().isBefore(today);
                break;
            case ALL:
            default:
                predicate = milestone -> true;
                break;
        }
        
        filteredMilestones.setPredicate(predicate);
    }
    
    /**
     * Initializes the view model with a project.
     * 
     * @param project the project
     */
    public void initWithProject(Project project) {
        currentProject.set(project);
        loadMilestonesAsync();
    }
    
    /**
     * Gets the filtered milestones list.
     * 
     * @return the filtered milestones list
     */
    public ObservableList<Milestone> getFilteredMilestones() {
        return filteredMilestones;
    }
    
    /**
     * Gets the all milestones list.
     * 
     * @return the all milestones list
     */
    public ObservableList<Milestone> getAllMilestones() {
        return allMilestones;
    }
    
    /**
     * Gets the selected milestone property.
     * 
     * @return the selected milestone property
     */
    public ObjectProperty<Milestone> selectedMilestoneProperty() {
        return selectedMilestone;
    }
    
    /**
     * Gets the selected milestone.
     * 
     * @return the selected milestone
     */
    public Milestone getSelectedMilestone() {
        return selectedMilestone.get();
    }
    
    /**
     * Sets the selected milestone.
     * 
     * @param milestone the selected milestone
     */
    public void setSelectedMilestone(Milestone milestone) {
        selectedMilestone.set(milestone);
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
     * Gets the current filter property.
     * 
     * @return the current filter property
     */
    public ObjectProperty<MilestoneFilter> currentFilterProperty() {
        return currentFilter;
    }
    
    /**
     * Gets the current filter.
     * 
     * @return the current filter
     */
    public MilestoneFilter getCurrentFilter() {
        return currentFilter.get();
    }
    
    /**
     * Sets the current filter.
     * 
     * @param filter the filter
     */
    public void setCurrentFilter(MilestoneFilter filter) {
        currentFilter.set(filter);
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
     * Gets the load milestones command.
     * 
     * @return the load milestones command
     */
    public Command getLoadMilestonesCommand() {
        return loadMilestonesCommand;
    }
    
    /**
     * Gets the new milestone command.
     * 
     * @return the new milestone command
     */
    public Command getNewMilestoneCommand() {
        return newMilestoneCommand;
    }
    
    /**
     * Gets the edit milestone command.
     * 
     * @return the edit milestone command
     */
    public Command getEditMilestoneCommand() {
        return editMilestoneCommand;
    }
    
    /**
     * Gets the delete milestone command.
     * 
     * @return the delete milestone command
     */
    public Command getDeleteMilestoneCommand() {
        return deleteMilestoneCommand;
    }
    
    /**
     * Gets the refresh milestones command.
     * 
     * @return the refresh milestones command
     */
    public Command getRefreshMilestonesCommand() {
        return refreshMilestonesCommand;
    }
    
    /**
     * Loads milestones asynchronously.
     */
    private void loadMilestonesAsync() {
        if (currentProject.get() == null) {
            return;
        }
        
        loading.set(true);
        
        // Use the specific async method from MilestoneServiceAsyncImpl
        milestoneServiceAsync.findByProjectAsync(
            currentProject.get(),
            // Success callback
            result -> {
                Platform.runLater(() -> {
                    allMilestones.clear();
                    allMilestones.addAll(result);
                    applyFilter();
                    clearErrorMessage();
                    loading.set(false);
                    LOGGER.info("Loaded " + result.size() + " milestones asynchronously");
                });
            },
            // Error callback
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error loading milestones asynchronously", error);
                    setErrorMessage("Failed to load milestones: " + error.getMessage());
                    loading.set(false);
                });
            }
        );
    }
    
    /**
     * Deletes a milestone asynchronously.
     */
    private void deleteMilestoneAsync() {
        Milestone milestone = selectedMilestone.get();
        if (milestone == null || milestone.getId() == null) {
            return;
        }
        
        loading.set(true);
        
        // Using the generic deleteByIdAsync from AbstractAsyncService
        milestoneServiceAsync.deleteByIdAsync(
            milestone.getId(),
            // Success callback
            result -> {
                Platform.runLater(() -> {
                    if (result) {
                        allMilestones.remove(milestone);
                        selectedMilestone.set(null);
                        clearErrorMessage();
                        LOGGER.info("Deleted milestone: " + milestone.getName() + " asynchronously");
                    } else {
                        LOGGER.warning("Failed to delete milestone: " + milestone.getName() + " asynchronously");
                        setErrorMessage("Failed to delete milestone: Database operation unsuccessful");
                    }
                    loading.set(false);
                });
            },
            // Error callback
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error deleting milestone asynchronously", error);
                    setErrorMessage("Failed to delete milestone: " + error.getMessage());
                    loading.set(false);
                });
            }
        );
    }
    
    /**
     * Cleans up resources when the ViewModel is no longer needed.
     */
    @Override
    public void dispose() {
        super.dispose();
        allMilestones.clear();
    }
}