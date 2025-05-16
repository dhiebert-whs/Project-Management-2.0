// src/main/java/org/frcpm/mvvm/viewmodels/MilestoneDetailMvvmViewModel.java

package org.frcpm.mvvm.viewmodels;

import de.saxsys.mvvmfx.utils.commands.Command;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;


import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.mvvm.BaseMvvmViewModel;
import org.frcpm.mvvm.async.MvvmAsyncHelper;
import org.frcpm.services.MilestoneService;
import org.frcpm.services.impl.MilestoneServiceAsyncImpl;

/**
 * ViewModel for the MilestoneDetail view using MVVMFx.
 */
public class MilestoneDetailMvvmViewModel extends BaseMvvmViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(MilestoneDetailMvvmViewModel.class.getName());
    
    // Service dependencies
    private final MilestoneService milestoneService;
    private final MilestoneServiceAsyncImpl milestoneServiceAsync;
    
    // Observable properties for milestone fields
    private final StringProperty name = new SimpleStringProperty("");
    private final StringProperty description = new SimpleStringProperty("");
    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>(LocalDate.now());
    private final ObjectProperty<Project> project = new SimpleObjectProperty<>();
    private final ObjectProperty<Milestone> milestone = new SimpleObjectProperty<>();
    private final BooleanProperty isNewMilestone = new SimpleBooleanProperty(true);
    private final BooleanProperty valid = new SimpleBooleanProperty(false);
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    
    // Commands
    private Command saveCommand;
    private Command cancelCommand;
    
    /**
     * Creates a new MilestoneDetailMvvmViewModel.
     * 
     * @param milestoneService the milestone service
     */

    public MilestoneDetailMvvmViewModel(MilestoneService milestoneService) {
        this.milestoneService = milestoneService;
        
        // Get the async service implementation
        this.milestoneServiceAsync = (MilestoneServiceAsyncImpl) milestoneService;
        
        initializeCommands();
        setupValidation();
    }
    
    /**
     * Initializes the commands for this view model.
     */
    private void initializeCommands() {
        // Save command
        saveCommand = createValidAndDirtyCommand(this::save, this::isValid);
        
        // Cancel command - implemented by the view to close dialog
        cancelCommand = MvvmAsyncHelper.createSimpleAsyncCommand(() -> {
            LOGGER.info("Cancel command executed");
        });
    }
    
    /**
     * Sets up validation for this view model.
     */
    private void setupValidation() {
        // Validation listener for all required fields
        Runnable validateAndMarkDirty = createDirtyFlagHandler(this::validate);
        
        // Add listeners to properties that affect validation
        name.addListener((obs, oldVal, newVal) -> validateAndMarkDirty.run());
        date.addListener((obs, oldVal, newVal) -> validateAndMarkDirty.run());
        project.addListener((obs, oldVal, newVal) -> validateAndMarkDirty.run());
        
        // Add listeners to properties that don't affect validation but mark as dirty
        description.addListener((obs, oldVal, newVal) -> setDirty(true));
        
        // Initial validation
        validate();
    }
    
    /**
     * Validates the milestone data.
     * Sets the valid property and error message accordingly.
     */
    private void validate() {
        StringBuilder errorMessages = new StringBuilder();
        
        // Check required fields
        if (name.get() == null || name.get().trim().isEmpty()) {
            errorMessages.append("Milestone name cannot be empty\n");
        }
        
        if (date.get() == null) {
            errorMessages.append("Milestone date cannot be empty\n");
        }
        
        if (project.get() == null) {
            errorMessages.append("Project cannot be null\n");
        }
        
        // Validate date is within project timeline if project is set
        if (date.get() != null && project.get() != null) {
            if (date.get().isBefore(project.get().getStartDate())) {
                errorMessages.append("Milestone date cannot be before project start date\n");
            }
            
            if (date.get().isAfter(project.get().getHardDeadline())) {
                errorMessages.append("Milestone date cannot be after project hard deadline\n");
            }
        }
        
        // Update valid property and error message
        boolean isValid = errorMessages.length() == 0;
        valid.set(isValid);
        if (!isValid) {
            setErrorMessage(errorMessages.toString().trim());
        } else {
            clearErrorMessage();
        }
    }
    
    /**
     * Initializes the view model for a new milestone.
     * 
     * @param project the project for the milestone
     */
    public void initNewMilestone(Project project) {
        this.project.set(project);
        this.milestone.set(null);
        this.isNewMilestone.set(true);
        
        // Set default values
        name.set("");
        description.set("");
        date.set(LocalDate.now());
        
        // Clear dirty flag and validate
        setDirty(false);
        validate();
    }
    
    /**
     * Initializes the view model for editing an existing milestone.
     * 
     * @param milestone the milestone to edit
     */
    public void initExistingMilestone(Milestone milestone) {
        if (milestone == null) {
            throw new IllegalArgumentException("Milestone cannot be null");
        }
        
        this.milestone.set(milestone);
        this.project.set(milestone.getProject());
        this.isNewMilestone.set(false);
        
        // Set field values from milestone
        name.set(milestone.getName());
        description.set(milestone.getDescription() != null ? milestone.getDescription() : "");
        date.set(milestone.getDate());
        
        // Clear dirty flag and validate
        setDirty(false);
        validate();
    }
    
    /**
     * Saves the milestone.
     */
    private void save() {
        if (!valid.get()) {
            return;
        }
        
        loading.set(true);
        
        try {
            if (isNewMilestone.get()) {
                // Create new milestone asynchronously
                milestoneServiceAsync.createMilestoneAsync(
                    name.get(),
                    date.get(),
                    project.get().getId(),
                    description.get(),
                    // Success handler
                    createdMilestone -> {
                        Platform.runLater(() -> {
                            milestone.set(createdMilestone);
                            setDirty(false);
                            loading.set(false);
                            LOGGER.info("Created milestone: " + createdMilestone.getName() + " asynchronously");
                        });
                    },
                    // Error handler
                    error -> {
                        Platform.runLater(() -> {
                            LOGGER.log(Level.SEVERE, "Error creating milestone asynchronously", error);
                            loading.set(false);
                            setErrorMessage("Failed to create milestone: " + error.getMessage());
                        });
                    }
                );
            } else {
                // Update existing milestone asynchronously
                Milestone existingMilestone = milestone.get();
                
                // First update the date if needed
                if (!date.get().equals(existingMilestone.getDate())) {
                    milestoneServiceAsync.updateMilestoneDateAsync(
                        existingMilestone.getId(),
                        date.get(),
                        // Success handler
                        updatedMilestone -> {
                            if (updatedMilestone != null) {
                                // Then update the description if needed
                                updateDescriptionIfNeeded(updatedMilestone);
                            } else {
                                Platform.runLater(() -> {
                                    loading.set(false);
                                    setErrorMessage("Failed to update milestone date: Database operation unsuccessful");
                                });
                            }
                        },
                        // Error handler
                        error -> {
                            Platform.runLater(() -> {
                                LOGGER.log(Level.SEVERE, "Error updating milestone date asynchronously", error);
                                loading.set(false);
                                setErrorMessage("Failed to update milestone date: " + error.getMessage());
                            });
                        }
                    );
                } else {
                    // Just update the description if date hasn't changed
                    updateDescriptionIfNeeded(existingMilestone);
                }
            }
        } catch (Exception e) {
            loading.set(false);
            LOGGER.log(Level.SEVERE, "Error in save method", e);
            setErrorMessage("Failed to save milestone: " + e.getMessage());
        }
    }
    
    /**
     * Updates the description of a milestone if needed.
     * 
     * @param milestone the milestone to update
     */
    private void updateDescriptionIfNeeded(Milestone milestone) {
        // Need to handle null description
        String currentDesc = milestone.getDescription();
        String newDesc = description.get();
        
        boolean needsUpdate = (currentDesc == null && newDesc != null && !newDesc.isEmpty()) || 
                             (currentDesc != null && (newDesc == null || !currentDesc.equals(newDesc)));
        
        if (needsUpdate) {
            milestoneServiceAsync.updateDescriptionAsync(
                milestone.getId(),
                newDesc,
                // Success handler
                updatedMilestone -> {
                    Platform.runLater(() -> {
                        this.milestone.set(updatedMilestone);
                        setDirty(false);
                        loading.set(false);
                        LOGGER.info("Updated milestone: " + updatedMilestone.getName() + " asynchronously");
                    });
                },
                // Error handler
                error -> {
                    Platform.runLater(() -> {
                        LOGGER.log(Level.SEVERE, "Error updating milestone description asynchronously", error);
                        loading.set(false);
                        setErrorMessage("Failed to update milestone description: " + error.getMessage());
                    });
                }
            );
        } else {
            // No update needed
            Platform.runLater(() -> {
                this.milestone.set(milestone);
                setDirty(false);
                loading.set(false);
                LOGGER.info("No updates needed for milestone: " + milestone.getName());
            });
        }
    }
    
    /**
     * Gets the value of the valid property.
     * 
     * @return true if the milestone is valid, false otherwise
     */
    public boolean isValid() {
        return valid.get();
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
     * Gets the loading property.
     * 
     * @return the loading property
     */
    public BooleanProperty loadingProperty() {
        return loading;
    }
    
    /**
     * Gets whether the view model is loading data.
     * 
     * @return true if loading, false otherwise
     */
    public boolean isLoading() {
        return loading.get();
    }
    
    /**
     * Gets the milestone.
     * 
     * @return the milestone
     */
    public Milestone getMilestone() {
        return milestone.get();
    }
    
    /**
     * Gets the milestone property.
     * 
     * @return the milestone property
     */
    public ObjectProperty<Milestone> milestoneProperty() {
        return milestone;
    }
    
    /**
     * Gets whether this is a new milestone.
     * 
     * @return true if this is a new milestone, false if editing an existing milestone
     */
    public boolean isNewMilestone() {
        return isNewMilestone.get();
    }
    
    /**
     * Gets the new milestone property.
     * 
     * @return the new milestone property
     */
    public BooleanProperty isNewMilestoneProperty() {
        return isNewMilestone;
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
    
    // Property getters and setters
    
    public StringProperty nameProperty() {
        return name;
    }
    
    public String getName() {
        return name.get();
    }
    
    public void setName(String value) {
        name.set(value);
    }
    
    public StringProperty descriptionProperty() {
        return description;
    }
    
    public String getDescription() {
        return description.get();
    }
    
    public void setDescription(String value) {
        description.set(value);
    }
    
    public ObjectProperty<LocalDate> dateProperty() {
        return date;
    }
    
    public LocalDate getDate() {
        return date.get();
    }
    
    public void setDate(LocalDate value) {
        date.set(value);
    }
    
    public ObjectProperty<Project> projectProperty() {
        return project;
    }
    
    public Project getProject() {
        return project.get();
    }
    
    public void setProject(Project value) {
        project.set(value);
    }
    
    /**
     * Cleans up resources when the ViewModel is no longer needed.
     */
    @Override
    public void dispose() {
        super.dispose();
    }
}