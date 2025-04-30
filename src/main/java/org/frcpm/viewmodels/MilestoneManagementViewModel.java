// src/main/java/org/frcpm/viewmodels/MilestoneManagementViewModel.java
package org.frcpm.viewmodels;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import org.frcpm.binding.Command;
import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.services.MilestoneService;
import org.frcpm.services.ServiceFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ViewModel for the Milestone Management view.
 * Handles the business logic for listing, filtering, and managing milestones.
 */
public class MilestoneManagementViewModel extends BaseViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(MilestoneManagementViewModel.class.getName());
    
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
    
    // Services
    private final MilestoneService milestoneService;
    
    // Data
    private final ObservableList<Milestone> allMilestones = FXCollections.observableArrayList();
    private final FilteredList<Milestone> filteredMilestones = new FilteredList<>(allMilestones);
    
    // Selected milestone
    private Milestone selectedMilestone;
    
    // Current project
    private final ObjectProperty<Project> project = new SimpleObjectProperty<>();
    
    // Current filter
    private MilestoneFilter currentFilter = MilestoneFilter.ALL;
    
    // Commands
    private final Command addMilestoneCommand;
    private final Command editMilestoneCommand;
    private final Command deleteMilestoneCommand;
    private final Command refreshCommand;
    
    /**
     * Creates a new MilestoneManagementViewModel with default services.
     */
    public MilestoneManagementViewModel() {
        this(ServiceFactory.getMilestoneService());
    }
    
    /**
     * Creates a new MilestoneManagementViewModel with specified services.
     * This constructor is primarily used for testing to inject mock services.
     * 
     * @param milestoneService the milestone service
     */
    public MilestoneManagementViewModel(MilestoneService milestoneService) {
        this.milestoneService = milestoneService;
        
        // Initialize commands
        addMilestoneCommand = new Command(this::addMilestone);
        editMilestoneCommand = new Command(this::editMilestone, this::canEditOrDeleteMilestone);
        deleteMilestoneCommand = new Command(this::deleteMilestone, this::canEditOrDeleteMilestone);
        refreshCommand = new Command(this::loadMilestones);
        
        // Set up filtered list predicate
        updateFilterPredicate();
        
        // Initialize project property listener
        project.addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadMilestones();
            } else {
                allMilestones.clear();
            }
        });
    }
    
    /**
     * Sets the current project.
     * 
     * @param project the project
     */
    public void setProject(Project project) {
        this.project.set(project);
    }
    
    /**
     * Gets the current project.
     * 
     * @return the current project
     */
    public Project getProject() {
        return project.get();
    }
    
    /**
     * Gets the project property.
     * 
     * @return the project property
     */
    public ObjectProperty<Project> projectProperty() {
        return project;
    }
    
    /**
     * Loads all milestones for the current project from the service.
     */
    public void loadMilestones() {
        if (project.get() == null) {
            LOGGER.warning("Cannot load milestones - project is null");
            setErrorMessage("No project set");
            return;
        }
        
        try {
            List<Milestone> milestones = milestoneService.findByProject(project.get());
            allMilestones.setAll(milestones);
            applyFilter(); // Apply filter after loading
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading milestones", e);
            setErrorMessage("Failed to load milestones: " + e.getMessage());
        }
    }
    
    /**
     * Sets the filter and updates the filtered list.
     * 
     * @param filter the filter to apply
     */
    public void setFilter(MilestoneFilter filter) {
        this.currentFilter = filter;
        updateFilterPredicate();
    }
    
    /**
     * Updates the filter predicate based on the current filter.
     */
    private void updateFilterPredicate() {
        filteredMilestones.setPredicate(milestone -> {
            switch (currentFilter) {
                case ALL:
                    return true;
                case UPCOMING:
                    return !milestone.isPassed();
                case PASSED:
                    return milestone.isPassed();
                default:
                    return true;
            }
        });
    }
    
    /**
     * Applies the current filter to the milestone list.
     * This method is called explicitly from the presenter.
     */
    public void applyFilter() {
        updateFilterPredicate();
    }
    
    /**
     * Command action to add a new milestone.
     * This will be handled by the controller to show the dialog.
     */
    private void addMilestone() {
        // This is intentionally empty because the controller will handle dialog creation
        LOGGER.info("Add milestone action triggered");
    }
    
    /**
     * Command action to edit the selected milestone.
     * This will be handled by the controller to show the dialog.
     */
    private void editMilestone() {
        // This is intentionally empty because the controller will handle dialog creation
        LOGGER.info("Edit milestone action triggered");
    }
    
    /**
     * Command action to delete the selected milestone.
     */
    private void deleteMilestone() {
        if (selectedMilestone == null) {
            return;
        }
        
        try {
            // Delete the milestone
            milestoneService.deleteById(selectedMilestone.getId());
            
            // Remove it from the list
            allMilestones.remove(selectedMilestone);
            selectedMilestone = null;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting milestone", e);
            setErrorMessage("Failed to delete milestone: " + e.getMessage());
        }
    }
    
    /**
     * Deletes a milestone by its object instance.
     * 
     * @param milestone the milestone to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteMilestone(Milestone milestone) {
        if (milestone == null) {
            return false;
        }
        
        try {
            // Delete the milestone
            boolean success = milestoneService.deleteById(milestone.getId());
            
            if (success) {
                // Remove it from the list
                allMilestones.remove(milestone);
                if (selectedMilestone != null && selectedMilestone.getId().equals(milestone.getId())) {
                    selectedMilestone = null;
                }
            }
            
            return success;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting milestone", e);
            setErrorMessage("Failed to delete milestone: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Command condition to check if a milestone is selected for edit/delete.
     * 
     * @return true if a milestone is selected, false otherwise
     */
    private boolean canEditOrDeleteMilestone() {
        return selectedMilestone != null;
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
     * Gets the milestones list.
     * 
     * @return the filtered milestones list
     */
    public ObservableList<Milestone> getMilestones() {
        return filteredMilestones;
    }
    
    /**
     * Sets the selected milestone.
     * 
     * @param milestone the selected milestone
     */
    public void setSelectedMilestone(Milestone milestone) {
        this.selectedMilestone = milestone;
    }
    
    /**
     * Gets the selected milestone.
     * 
     * @return the selected milestone
     */
    public Milestone getSelectedMilestone() {
        return selectedMilestone;
    }
    
    /**
     * Gets the add milestone command.
     * 
     * @return the add milestone command
     */
    public Command getAddMilestoneCommand() {
        return addMilestoneCommand;
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
     * Gets the refresh command.
     * 
     * @return the refresh command
     */
    public Command getRefreshCommand() {
        return refreshCommand;
    }
    
    /**
     * Gets the current filter.
     * 
     * @return the current filter
     */
    public MilestoneFilter getCurrentFilter() {
        return currentFilter;
    }
    
    /**
     * Clears the error message.
     * This overrides the protected method in BaseViewModel to make it public.
     */
    @Override
    public void clearErrorMessage() {
        super.clearErrorMessage();
    }
    
    /**
     * Cleans up resources when the ViewModel is no longer needed.
     */
    @Override
    public void cleanupResources() {
        super.cleanupResources();
        allMilestones.clear();
    }
}