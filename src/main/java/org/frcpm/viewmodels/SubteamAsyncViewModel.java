// src/main/java/org/frcpm/viewmodels/SubteamAsyncViewModel.java

package org.frcpm.viewmodels;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.frcpm.binding.Command;
import org.frcpm.models.Subteam;
import org.frcpm.models.TeamMember;
import org.frcpm.services.TeamMemberService;
import org.frcpm.services.impl.SubteamServiceAsyncImpl;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Asynchronous ViewModel for the Subteam view.
 * Extends the synchronous SubteamViewModel to provide non-blocking operations.
 */
public class SubteamAsyncViewModel extends SubteamViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(SubteamAsyncViewModel.class.getName());
    
    // Services
    private final SubteamServiceAsyncImpl subteamServiceAsync;
    private final TeamMemberService teamMemberService;
    
    // UI state
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    
    // Async commands
    private Command asyncSaveCommand;
    private Command asyncLoadTeamMembersCommand;
    
    /**
     * Creates a new SubteamAsyncViewModel with the specified services.
     * 
     * @param subteamServiceAsync the asynchronous subteam service
     * @param teamMemberService the team member service
     */
    public SubteamAsyncViewModel(SubteamServiceAsyncImpl subteamServiceAsync, 
                              TeamMemberService teamMemberService) {
        // Initialize with standard services for the parent class
        super(null, teamMemberService);
        
        this.subteamServiceAsync = subteamServiceAsync;
        this.teamMemberService = teamMemberService;
        
        // Initialize async commands
        initAsyncCommands();
    }
    
    /**
     * Initializes async commands.
     */
    private void initAsyncCommands() {
        asyncSaveCommand = new Command(
            this::saveAsync, 
            () -> {
                try {
                    // Access the valid property via reflection
                    Field validField = SubteamViewModel.class.getDeclaredField("valid");
                    validField.setAccessible(true);
                    BooleanProperty validProperty = (BooleanProperty) validField.get(this);
                    
                    return validProperty.get() && this.isDirty() && !loading.get();
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error accessing valid field", e);
                    return false;
                }
            }
        );
        
        asyncLoadTeamMembersCommand = new Command(
            this::loadTeamMembersAsync,
            () -> {
                try {
                    // Check if we have a subteam with an ID
                    Subteam subteam = getSubteam();
                    return subteam != null && subteam.getId() != null && !loading.get();
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error checking subteam ID", e);
                    return false;
                }
            }
        );
    }
    
    /**
     * Asynchronously saves the subteam.
     */
    public void saveAsync() {
        try {
            // Get the valid property via reflection to check if we can save
            Field validField = SubteamViewModel.class.getDeclaredField("valid");
            validField.setAccessible(true);
            BooleanProperty validProperty = (BooleanProperty) validField.get(this);
            
            if (!validProperty.get() || !isDirty() || loading.get()) {
                return;
            }
            
            // Get the subteam and check if it's null
            Subteam subteam = getSubteam();
            if (subteam == null) {
                setErrorMessage("No subteam to save");
                return;
            }
            
            // Update subteam from properties
            // We need to use reflection to call the private method
            Field nameField = SubteamViewModel.class.getDeclaredField("name");
            nameField.setAccessible(true);
            Field colorCodeField = SubteamViewModel.class.getDeclaredField("colorCode");
            colorCodeField.setAccessible(true);
            Field specialtiesField = SubteamViewModel.class.getDeclaredField("specialties");
            specialtiesField.setAccessible(true);
            
            subteam.setName(nameField.get(this).toString());
            subteam.setColorCode(colorCodeField.get(this).toString());
            subteam.setSpecialties(specialtiesField.get(this).toString());
            
            // Set loading state
            loading.set(true);
            
            // Check if this is a new subteam
            Field isNewSubteamField = SubteamViewModel.class.getDeclaredField("isNewSubteam");
            isNewSubteamField.setAccessible(true);
            BooleanProperty isNewSubteamProperty = (BooleanProperty) isNewSubteamField.get(this);
            
            CompletableFuture<Subteam> future;
            if (isNewSubteamProperty.get()) {
                // Create new subteam
                future = subteamServiceAsync.createSubteamAsync(
                    subteam.getName(),
                    subteam.getColorCode(),
                    subteam.getSpecialties(),
                    this::handleSaveSuccess,
                    this::handleError
                );
            } else {
                // For existing subteams, update properties individually
                if (subteam.getId() != null) {
                    // First update color code
                    future = subteamServiceAsync.updateColorCodeAsync(
                        subteam.getId(),
                        subteam.getColorCode(),
                        updatedSubteam -> {
                            // Then update specialties
                            subteamServiceAsync.updateSpecialtiesAsync(
                                subteam.getId(),
                                subteam.getSpecialties(),
                                this::handleSaveSuccess,
                                this::handleError
                            );
                        },
                        this::handleError
                    );
                } else {
                    // Shouldn't happen, but handle it anyway
                    future = CompletableFuture.completedFuture(null);
                    Platform.runLater(() -> {
                        loading.set(false);
                        setErrorMessage("Cannot save subteam without ID");
                    });
                }
            }
            
            // Handle completion
            future.whenComplete((result, error) -> {
                if (error != null) {
                    Platform.runLater(() -> {
                        loading.set(false);
                        setErrorMessage("Error saving subteam: " + error.getMessage());
                    });
                }
            });
            
        } catch (Exception e) {
            loading.set(false);
            LOGGER.log(Level.SEVERE, "Error saving subteam", e);
            setErrorMessage("Failed to save subteam: " + e.getMessage());
        }
    }
    
    /**
     * Handles successful save operation.
     * 
     * @param savedSubteam the saved subteam
     */
    private void handleSaveSuccess(Subteam savedSubteam) {
        Platform.runLater(() -> {
            try {
                // Update the subteam reference with the saved one
                Field subteamField = SubteamViewModel.class.getDeclaredField("subteam");
                subteamField.setAccessible(true);
                subteamField.set(this, savedSubteam);
                
                // Not dirty after save
                setDirty(false);
                
                // Clear error message
                clearErrorMessage();
                
                // Update the isNewSubteam property if this was a new subteam
                if (savedSubteam.getId() != null) {
                    Field isNewSubteamField = SubteamViewModel.class.getDeclaredField("isNewSubteam");
                    isNewSubteamField.setAccessible(true);
                    BooleanProperty isNewSubteamProperty = (BooleanProperty) isNewSubteamField.get(this);
                    isNewSubteamProperty.set(false);
                }
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error updating ViewModel after save", e);
                setErrorMessage("Error updating ViewModel after save: " + e.getMessage());
            } finally {
                loading.set(false);
            }
        });
    }
    
    /**
     * Handles errors from async operations.
     * 
     * @param error the error that occurred
     */
    private void handleError(Throwable error) {
        Platform.runLater(() -> {
            loading.set(false);
            LOGGER.log(Level.SEVERE, "Error in async operation", error);
            setErrorMessage("Error: " + error.getMessage());
        });
    }
    
    /**
     * Asynchronously loads team members assigned to this subteam.
     */
    public void loadTeamMembersAsync() {
        try {
            Subteam subteam = getSubteam();
            if (subteam == null || subteam.getId() == null || loading.get()) {
                return;
            }
            
            loading.set(true);
            
            // For now, use the synchronous service but in a background thread
            CompletableFuture.supplyAsync(() -> {
                try {
                    return teamMemberService.findBySubteam(subteam);
                } catch (Exception e) {
                    throw new RuntimeException("Error loading team members", e);
                }
            }).thenAccept(this::handleTeamMembersLoaded)
              .exceptionally(this::handleTeamMembersError);
            
        } catch (Exception e) {
            loading.set(false);
            LOGGER.log(Level.SEVERE, "Error loading team members", e);
            setErrorMessage("Failed to load team members: " + e.getMessage());
        }
    }
    
    /**
     * Handles successful team members loading.
     * 
     * @param teamMembers the loaded team members
     */
    private void handleTeamMembersLoaded(List<TeamMember> teamMembers) {
        Platform.runLater(() -> {
            try {
                // Clear the existing list and add the loaded members
                getTeamMembers().clear();
                getTeamMembers().addAll(teamMembers);
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error updating team members list", e);
                setErrorMessage("Error updating team members list: " + e.getMessage());
            } finally {
                loading.set(false);
            }
        });
    }
    
    /**
     * Handles errors from team members loading.
     * 
     * @param error the error that occurred
     * @return null (required by CompletableFuture.exceptionally)
     */
    private Void handleTeamMembersError(Throwable error) {
        Platform.runLater(() -> {
            loading.set(false);
            LOGGER.log(Level.SEVERE, "Error loading team members", error);
            setErrorMessage("Error loading team members: " + error.getMessage());
        });
        return null;
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
     * Gets whether the ViewModel is currently loading data.
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
     * Gets the async load team members command.
     * 
     * @return the async load team members command
     */
    public Command getAsyncLoadTeamMembersCommand() {
        return asyncLoadTeamMembersCommand;
    }
    
    @Override
    public void loadTeamMembers() {
        loadTeamMembersAsync();
    }
}