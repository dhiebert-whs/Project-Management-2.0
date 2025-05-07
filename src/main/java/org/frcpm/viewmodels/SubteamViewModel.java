// src/main/java/org/frcpm/viewmodels/SubteamViewModel.java

package org.frcpm.viewmodels;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.frcpm.binding.Command;
import org.frcpm.di.ServiceProvider;
import org.frcpm.models.Subteam;
import org.frcpm.models.TeamMember;
import org.frcpm.services.SubteamService;
import org.frcpm.services.TeamMemberService;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ViewModel for the Subteam view.
 * Handles business logic for subteam creation and editing.
 */
public class SubteamViewModel extends BaseViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(SubteamViewModel.class.getName());
    
    // Services
    private final SubteamService subteamService;
    private final TeamMemberService teamMemberService;
    
    // Model reference
    private Subteam subteam;
    
    // Observable properties
    private final StringProperty name = new SimpleStringProperty("");
    private final StringProperty colorCode = new SimpleStringProperty("#007BFF");
    private final StringProperty specialties = new SimpleStringProperty("");
    private final BooleanProperty isNewSubteam = new SimpleBooleanProperty(true);
    private final BooleanProperty valid = new SimpleBooleanProperty(false);
    
    // Collections
    private final ObservableList<TeamMember> teamMembers = FXCollections.observableArrayList();
    
    // Selected items
    private TeamMember selectedTeamMember;
    
    // Commands
    private final Command saveCommand;
    private final Command cancelCommand;
    
    /**
     * Creates a new SubteamViewModel with default services.
     */
    public SubteamViewModel() {
        this(ServiceProvider.getService(SubteamService.class), 
             ServiceProvider.getService(TeamMemberService.class));
    }
    
    /**
     * Creates a new SubteamViewModel with specified services.
     * This constructor is primarily used for testing to inject mock services.
     * 
     * @param subteamService the subteam service
     * @param teamMemberService the team member service
     */
    public SubteamViewModel(SubteamService subteamService, TeamMemberService teamMemberService) {
        this.subteamService = subteamService;
        this.teamMemberService = teamMemberService;
        
        // Initialize commands using BaseViewModel utility methods
        saveCommand = createValidAndDirtyCommand(this::save, this::isValid);
        cancelCommand = new Command(this::cancel);
        
        // Set up property listeners
        setupPropertyListeners();
        
        // Initial validation
        validate();
    }
    
    /**
     * Sets up property change listeners.
     */
    private void setupPropertyListeners() {
        // Create standard dirty flag handler with validation
        Runnable validationHandler = createDirtyFlagHandler(this::validate);
        
        // Set up validation listeners with tracking
        name.addListener((obs, oldVal, newVal) -> validationHandler.run());
        trackPropertyListener(validationHandler);
        
        colorCode.addListener((obs, oldVal, newVal) -> validationHandler.run());
        trackPropertyListener(validationHandler);
        
        // Track other property change listeners
        Runnable dirtyHandler = createDirtyFlagHandler(null);
        
        specialties.addListener((obs, oldVal, newVal) -> dirtyHandler.run());
        trackPropertyListener(dirtyHandler);
    }
    
    /**
     * Initializes the ViewModel for a new subteam.
     */
    public void initNewSubteam() {
        // Create a new subteam
        subteam = new Subteam();
        isNewSubteam.set(true);
        
        // Reset properties
        clearProperties();
        colorCode.set("#007BFF"); // Default blue color
        
        // No initial team members
        teamMembers.clear();
        
        // Not dirty initially
        setDirty(false);
        
        // Clear any error messages
        clearErrorMessage();
        
        // Validate
        validate();
    }
    
    /**
     * Initializes the ViewModel for editing an existing subteam.
     * 
     * @param subteam the subteam to edit
     */
    public void initExistingSubteam(Subteam subteam) {
        if (subteam == null) {
            throw new IllegalArgumentException("Subteam cannot be null");
        }
        
        this.subteam = subteam;
        isNewSubteam.set(false);
        
        // Set properties from subteam
        name.set(subteam.getName());
        colorCode.set(subteam.getColorCode());
        specialties.set(subteam.getSpecialties());
        
        // Load team members assigned to this subteam
        loadTeamMembers();
        
        // Not dirty initially
        setDirty(false);
        
        // Clear any error messages
        clearErrorMessage();
        
        // Validate
        validate();
    }
    
    /**
     * Loads team members assigned to this subteam.
     */
    public void loadTeamMembers() {
        try {
            teamMembers.clear();
            if (subteam != null && subteam.getId() != null) {
                List<TeamMember> members = teamMemberService.findBySubteam(subteam);
                teamMembers.addAll(members);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading team members for subteam", e);
            setErrorMessage("Failed to load team members for subteam");
        }
    }
    
    /**
     * Clears all properties.
     */
    private void clearProperties() {
        name.set("");
        colorCode.set("#007BFF"); // Default blue color
        specialties.set("");
    }
    
    /**
     * Updates the subteam from the properties.
     */
    private void updateSubteamFromProperties() {
        subteam.setName(name.get());
        subteam.setColorCode(colorCode.get());
        subteam.setSpecialties(specialties.get());
    }
    
    /**
     * Validates the subteam data.
     */
    public void validate() {
        List<String> errors = new ArrayList<>();
        
        // Name is required
        if (name.get() == null || name.get().trim().isEmpty()) {
            errors.add("Subteam name is required");
        }
        
        // Color code must be a valid hex color
        if (colorCode.get() == null || !colorCode.get().matches("^#[0-9A-Fa-f]{6}$")) {
            errors.add("Color code must be a valid hex color code (e.g., #007BFF)");
        }
        
        // Update valid state and error message
        valid.set(errors.isEmpty());
        
        if (!errors.isEmpty()) {
            setErrorMessage(String.join("\n", errors));
        } else {
            clearErrorMessage();
        }
    }
    
    // Command actions
    
    /**
     * Saves the subteam.
     */
    private void save() {
        if (!valid.get() || !isDirty()) {
            return;
        }
        
        try {
            // Update subteam from properties
            updateSubteamFromProperties();
            
            // Save subteam
            if (isNewSubteam.get()) {
                subteam = subteamService.createSubteam(
                    subteam.getName(),
                    subteam.getColorCode(),
                    subteam.getSpecialties()
                );
            } else {
                // For existing subteams, update properties individually
                // This ensures only the changed properties are updated
                if (subteam.getId() != null) {
                    subteamService.updateColorCode(subteam.getId(), subteam.getColorCode());
                    subteamService.updateSpecialties(subteam.getId(), subteam.getSpecialties());
                }
                
                // Reload the subteam to get the updated state
                Subteam refreshed = subteamService.findById(subteam.getId());
                if (refreshed != null) {
                    subteam = refreshed;
                }
            }
            
            // Not dirty after save
            setDirty(false);
            
            // Clear error message after successful save
            clearErrorMessage();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving subteam", e);
            setErrorMessage("Failed to save subteam: " + e.getMessage());
        }
    }
    
    /**
     * Cancels the current operation.
     */
    private void cancel() {
        // This will be handled by the controller to close the dialog
    }
    
    // Utility methods
    
    /**
     * Checks if the subteam is valid.
     * 
     * @return true if the subteam is valid, false otherwise
     */
    public boolean isValid() {
        return valid.get();
    }
    
    // Getters and setters
    
    /**
     * Gets the name property.
     * 
     * @return the name property
     */
    public StringProperty nameProperty() {
        return name;
    }
    
    /**
     * Gets the color code property.
     * 
     * @return the color code property
     */
    public StringProperty colorCodeProperty() {
        return colorCode;
    }
    
    /**
     * Gets the specialties property.
     * 
     * @return the specialties property
     */
    public StringProperty specialtiesProperty() {
        return specialties;
    }
    
    /**
     * Gets the isNewSubteam property.
     * 
     * @return the isNewSubteam property
     */
    public BooleanProperty isNewSubteamProperty() {
        return isNewSubteam;
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
     * Gets whether this is a new subteam.
     * 
     * @return true if this is a new subteam, false otherwise
     */
    public boolean isNewSubteam() {
        return isNewSubteam.get();
    }
    
    /**
     * Gets the team members list.
     * 
     * @return the team members list
     */
    public ObservableList<TeamMember> getTeamMembers() {
        return teamMembers;
    }
    
    /**
     * Sets the selected team member.
     * 
     * @param teamMember the selected team member
     */
    public void setSelectedTeamMember(TeamMember teamMember) {
        this.selectedTeamMember = teamMember;
    }
    
    /**
     * Gets the selected team member.
     * 
     * @return the selected team member
     */
    public TeamMember getSelectedTeamMember() {
        return selectedTeamMember;
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
    
    /**
     * Gets the subteam.
     * 
     * @return the subteam
     */
    public Subteam getSubteam() {
        return subteam;
    }
    
    /**
     * Cleans up resources when the ViewModel is no longer needed.
     */
    @Override
    public void cleanupResources() {
        super.cleanupResources();
        // Clear all references and collections to prevent memory leaks
        teamMembers.clear();
        selectedTeamMember = null;
    }
}