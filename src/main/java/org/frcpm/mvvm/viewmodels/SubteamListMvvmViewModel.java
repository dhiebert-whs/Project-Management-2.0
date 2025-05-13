// src/main/java/org/frcpm/mvvm/viewmodels/SubteamListMvvmViewModel.java
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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.frcpm.models.Subteam;
import org.frcpm.models.Project;
import org.frcpm.mvvm.BaseMvvmViewModel;
import org.frcpm.mvvm.async.MvvmAsyncHelper;
import org.frcpm.services.SubteamService;
import org.frcpm.services.impl.SubteamServiceAsyncImpl;

/**
 * ViewModel for the SubteamList view using MVVMFx.
 */
public class SubteamListMvvmViewModel extends BaseMvvmViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(SubteamListMvvmViewModel.class.getName());
    
    // Service dependencies
    private final SubteamService subteamService;
    private final SubteamServiceAsyncImpl subteamServiceAsync;
    
    // Observable collections and properties
    private final ObservableList<Subteam> subteams = FXCollections.observableArrayList();
    private final ObjectProperty<Subteam> selectedSubteam = new SimpleObjectProperty<>();
    private final ObjectProperty<Project> currentProject = new SimpleObjectProperty<>();
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    
    // Commands
    private Command loadSubteamsCommand;
    private Command newSubteamCommand;
    private Command editSubteamCommand;
    private Command deleteSubteamCommand;
    private Command refreshSubteamsCommand;
    
    /**
     * Creates a new SubteamListMvvmViewModel.
     * 
     * @param subteamService the subteam service
     */
    @Inject
    public SubteamListMvvmViewModel(SubteamService subteamService) {
        this.subteamService = subteamService;
        
        // Cast to the async implementation
        this.subteamServiceAsync = (SubteamServiceAsyncImpl) subteamService;
        
        initializeCommands();
    }
    
    /**
     * Initializes the commands for this view model.
     */
    private void initializeCommands() {
        // Load subteams command
        loadSubteamsCommand = MvvmAsyncHelper.createSimpleAsyncCommand(this::loadSubteamsAsync);
        
        // New subteam command
        newSubteamCommand = createValidOnlyCommand(
            () -> {
                // This will be handled by the view/controller
                LOGGER.info("New subteam command executed");
            },
            () -> true // Always enabled
        );
        
        // Edit subteam command
        editSubteamCommand = createValidOnlyCommand(
            () -> {
                // This will be handled by the view/controller
                LOGGER.info("Edit subteam command executed for: " + 
                    (selectedSubteam.get() != null ? selectedSubteam.get().getName() : "null"));
            },
            () -> selectedSubteam.get() != null
        );
        
        // Delete subteam command
        deleteSubteamCommand = createValidOnlyCommand(
            this::deleteSubteamAsync,
            () -> selectedSubteam.get() != null
        );
        
        // Refresh subteams command
        refreshSubteamsCommand = MvvmAsyncHelper.createSimpleAsyncCommand(this::loadSubteamsAsync);
    }
    
    /**
     * Initializes the view model with a project.
     * 
     * @param project the project
     */
    public void initWithProject(Project project) {
        currentProject.set(project);
        loadSubteamsAsync();
    }
    
    /**
     * Loads subteams asynchronously.
     */
    private void loadSubteamsAsync() {
        loading.set(true);
        
        // Find all subteams asynchronously
        subteamServiceAsync.findAllAsync(
            // Success callback
            result -> {
                Platform.runLater(() -> {
                    subteams.clear();
                    subteams.addAll(result);
                    clearErrorMessage();
                    loading.set(false);
                    LOGGER.info("Loaded " + result.size() + " subteams asynchronously");
                });
            },
            // Error callback
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error loading subteams asynchronously", error);
                    setErrorMessage("Failed to load subteams: " + error.getMessage());
                    loading.set(false);
                });
            }
        );
    }
    
    /**
     * Deletes a subteam asynchronously.
     */
    private void deleteSubteamAsync() {
        Subteam subteam = selectedSubteam.get();
        if (subteam == null || subteam.getId() == null) {
            return;
        }
        
        // Check if the subteam has members
        if (subteam.getMembers() != null && !subteam.getMembers().isEmpty()) {
            int memberCount = subteam.getMembers().size();
            setErrorMessage("Cannot delete subteam because it has " + memberCount + 
                    " member(s). Please reassign these members first.");
            return;
        }
        
        // Check if the subteam has subsystems
        if (subteam.getSubsystems() != null && !subteam.getSubsystems().isEmpty()) {
            int subsystemCount = subteam.getSubsystems().size();
            setErrorMessage("Cannot delete subteam because it is responsible for " + subsystemCount + 
                    " subsystem(s). Please reassign these subsystems first.");
            return;
        }
        
        loading.set(true);
        
        // Using the async service method
        subteamServiceAsync.deleteByIdAsync(
            subteam.getId(),
            // Success callback
            result -> {
                Platform.runLater(() -> {
                    if (result) {
                        subteams.remove(subteam);
                        selectedSubteam.set(null);
                        clearErrorMessage();
                        LOGGER.info("Deleted subteam: " + subteam.getName() + " asynchronously");
                    } else {
                        LOGGER.warning("Failed to delete subteam: " + subteam.getName() + " asynchronously");
                        setErrorMessage("Failed to delete subteam: Database operation unsuccessful");
                    }
                    loading.set(false);
                });
            },
            // Error callback
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error deleting subteam asynchronously", error);
                    setErrorMessage("Failed to delete subteam: " + error.getMessage());
                    loading.set(false);
                });
            }
        );
    }
    
    /**
     * Gets the subteams list.
     * 
     * @return the subteams list
     */
    public ObservableList<Subteam> getSubteams() {
        return subteams;
    }
    
    /**
     * Gets the selected subteam property.
     * 
     * @return the selected subteam property
     */
    public ObjectProperty<Subteam> selectedSubteamProperty() {
        return selectedSubteam;
    }
    
    /**
     * Gets the selected subteam.
     * 
     * @return the selected subteam
     */
    public Subteam getSelectedSubteam() {
        return selectedSubteam.get();
    }
    
    /**
     * Sets the selected subteam.
     * 
     * @param subteam the selected subteam
     */
    public void setSelectedSubteam(Subteam subteam) {
        selectedSubteam.set(subteam);
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
     * Gets the load subteams command.
     * 
     * @return the load subteams command
     */
    public Command getLoadSubteamsCommand() {
        return loadSubteamsCommand;
    }
    
    /**
     * Gets the new subteam command.
     * 
     * @return the new subteam command
     */
    public Command getNewSubteamCommand() {
        return newSubteamCommand;
    }
    
    /**
     * Gets the edit subteam command.
     * 
     * @return the edit subteam command
     */
    public Command getEditSubteamCommand() {
        return editSubteamCommand;
    }
    
    /**
     * Gets the delete subteam command.
     * 
     * @return the delete subteam command
     */
    public Command getDeleteSubteamCommand() {
        return deleteSubteamCommand;
    }
    
    /**
     * Gets the refresh subteams command.
     * 
     * @return the refresh subteams command
     */
    public Command getRefreshSubteamsCommand() {
        return refreshSubteamsCommand;
    }
    
    @Override
    public void dispose() {
        super.dispose();
        subteams.clear();
    }
}