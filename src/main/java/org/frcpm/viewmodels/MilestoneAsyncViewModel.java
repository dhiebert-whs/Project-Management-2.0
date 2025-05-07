// src/main/java/org/frcpm/viewmodels/MilestoneAsyncViewModel.java

package org.frcpm.viewmodels;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;

import org.frcpm.binding.Command;
import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.services.AsyncServiceFactory;
import org.frcpm.services.MilestoneService;
import org.frcpm.services.impl.MilestoneServiceAsyncImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enhanced ViewModel for milestone management with asynchronous operations.
 */
public class MilestoneAsyncViewModel extends MilestoneViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(MilestoneAsyncViewModel.class.getName());
    
    private final MilestoneServiceAsyncImpl milestoneServiceAsync;
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    
    private Command asyncSaveCommand;
    
    /**
     * Creates a new milestone async view model.
     * 
     * @param milestoneService the milestone service
     */
    public MilestoneAsyncViewModel(MilestoneService milestoneService) {
        super(milestoneService);
        
        // Get the async service implementation
        this.milestoneServiceAsync = AsyncServiceFactory.getMilestoneService();
        
        // Initialize async commands
        asyncSaveCommand = new Command(
            this::saveMilestoneAsync,
            () -> isValid() && isDirty()
        );
    }
    
    /**
     * Saves the current milestone asynchronously.
     */
    public void saveMilestoneAsync() {
        if (!isValid() || !isDirty()) {
            return;
        }
        
        loading.set(true);
        
        if (isNewMilestone()) {
            // Create new milestone
            milestoneServiceAsync.createMilestoneAsync(
                getName(),
                getDate(),
                getProject().getId(),
                getDescription(),
                // Success handler
                createdMilestone -> {
                    Platform.runLater(() -> {
                        setMilestone(createdMilestone);
                        setDirty(false);
                        loading.set(false);
                        LOGGER.info("Created milestone: " + createdMilestone.getName() + " asynchronously");
                        
                        // Close dialog if configured
                        if (getCloseDialogAction() != null) {
                            getCloseDialogAction().run();
                        }
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
            // Update existing milestone
            Milestone existingMilestone = getMilestone();
            
            // First update the date if needed
            if (!getDate().equals(existingMilestone.getDate())) {
                milestoneServiceAsync.updateMilestoneDateAsync(
                    existingMilestone.getId(),
                    getDate(),
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
    }
    
    /**
     * Updates the description of a milestone if needed.
     * 
     * @param milestone the milestone to update
     */
    private void updateDescriptionIfNeeded(Milestone milestone) {
        if (getDescription() == null && milestone.getDescription() == null) {
            // No change needed
            finalizeMilestoneUpdate(milestone);
            return;
        }
        
        if ((getDescription() != null && milestone.getDescription() == null) ||
            (getDescription() == null && milestone.getDescription() != null) ||
            (getDescription() != null && !getDescription().equals(milestone.getDescription()))) {
            
            milestoneServiceAsync.updateDescriptionAsync(
                milestone.getId(),
                getDescription(),
                // Success handler
                updatedMilestone -> {
                    if (updatedMilestone != null) {
                        finalizeMilestoneUpdate(updatedMilestone);
                    } else {
                        Platform.runLater(() -> {
                            loading.set(false);
                            setErrorMessage("Failed to update milestone description: Database operation unsuccessful");
                        });
                    }
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
            // No change needed
            finalizeMilestoneUpdate(milestone);
        }
    }
    
    /**
     * Finalizes the milestone update process.
     * 
     * @param updatedMilestone the updated milestone
     */
    private void finalizeMilestoneUpdate(Milestone updatedMilestone) {
        Platform.runLater(() -> {
            setMilestone(updatedMilestone);
            setDirty(false);
            loading.set(false);
            LOGGER.info("Updated milestone: " + updatedMilestone.getName() + " asynchronously");
            
            // Close dialog if configured
            if (getCloseDialogAction() != null) {
                getCloseDialogAction().run();
            }
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
    
    /**
     * Gets the closeDialogAction.
     * This is needed because MilestoneViewModel doesn't expose it.
     * 
     * @return the closeDialogAction
     */
    private Runnable getCloseDialogAction() {
        // For testing, this method may need to be made public
        // and the closeDialogAction field in MilestoneViewModel made protected
        return null; // Will need to be updated if MilestoneViewModel exposes it
    }
}