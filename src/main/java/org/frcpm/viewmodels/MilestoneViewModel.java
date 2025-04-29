package org.frcpm.viewmodels;

import javafx.beans.property.*;
import org.frcpm.binding.Command;
import org.frcpm.di.ServiceProvider;
import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.services.MilestoneService;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ViewModel for milestone management in the FRC Project Management System.
 * Enhanced version with improved command handling for full MVVM implementation.
 */
public class MilestoneViewModel extends BaseViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(MilestoneViewModel.class.getName());
    
    // Services
    private final MilestoneService milestoneService;
    
    // Observable properties
    private final StringProperty name = new SimpleStringProperty("");
    private final StringProperty description = new SimpleStringProperty("");
    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>(LocalDate.now());
    private final ObjectProperty<Project> project = new SimpleObjectProperty<>();
    private final ObjectProperty<Milestone> milestone = new SimpleObjectProperty<>();
    private final BooleanProperty isNewMilestone = new SimpleBooleanProperty(true);
    private final BooleanProperty valid = new SimpleBooleanProperty(false);
    
    // Commands
    private final Command saveCommand;
    private final Command cancelCommand;
    
    // Close dialog action
    private Runnable closeDialogAction;
    
    /**
     * Creates a new MilestoneViewModel with default services.
     */
    public MilestoneViewModel() {
        this(ServiceProvider.getService(MilestoneService.class));
    }
    
    /**
     * Creates a new MilestoneViewModel with the specified service.
     * This constructor is mainly used for testing.
     * 
     * @param milestoneService the milestone service
     */
    public MilestoneViewModel(MilestoneService milestoneService) {
        this.milestoneService = milestoneService;
        
        // Create commands using BaseViewModel's enhanced methods
        saveCommand = createValidAndDirtyCommand(this::save, this::isValid);
        cancelCommand = new Command(() -> {
            if (closeDialogAction != null) {
                closeDialogAction.run();
            }
        });
        
        // Set up validation and dirty flag listeners using BaseViewModel's enhanced methods
        setupPropertyListeners();
        
        // Initial validation
        validate();
    }
    
    /**
     * Sets up property change listeners with better resource management
     */
    private void setupPropertyListeners() {
        // Create a handler that validates and sets dirty flag
        Runnable validateAndSetDirty = createDirtyFlagHandler(this::validate);
        
        // Add listeners to properties that affect both validation and dirty state
        name.addListener((observable, oldValue, newValue) -> validateAndSetDirty.run());
        trackPropertyListener(validateAndSetDirty);
        
        date.addListener((observable, oldValue, newValue) -> validateAndSetDirty.run());
        trackPropertyListener(validateAndSetDirty);
        
        project.addListener((observable, oldValue, newValue) -> validateAndSetDirty.run());
        trackPropertyListener(validateAndSetDirty);
        
        // For description, we only need to track dirty state but not validation
        Runnable dirtyOnly = createDirtyFlagHandler(null);
        description.addListener((observable, oldValue, newValue) -> dirtyOnly.run());
        trackPropertyListener(dirtyOnly);
    }
    
    /**
     * Sets the action to be executed when the cancel command is executed.
     * 
     * @param closeDialogAction the action to close the dialog
     */
    public void setCloseDialogAction(Runnable closeDialogAction) {
        this.closeDialogAction = closeDialogAction;
    }
    
    /**
     * Sets up the ViewModel for creating a new milestone.
     * 
     * @param project the project for the milestone
     */
    public void initNewMilestone(Project project) {
        this.project.set(project);
        milestone.set(null);
        isNewMilestone.set(true);
        
        // Set default values
        name.set("");
        description.set("");
        date.set(LocalDate.now());
        
        // Clear dirty flag and validate
        setDirty(false);
        validate();
    }
    
    /**
     * Sets up the ViewModel for editing an existing milestone.
     * 
     * @param milestone the milestone to edit
     */
    public void initExistingMilestone(Milestone milestone) {
        if (milestone == null) {
            throw new IllegalArgumentException("Milestone cannot be null");
        }
        
        this.milestone.set(milestone);
        project.set(milestone.getProject());
        isNewMilestone.set(false);
        
        // Set field values from milestone
        name.set(milestone.getName());
        description.set(milestone.getDescription() != null ? milestone.getDescription() : "");
        date.set(milestone.getDate());
        
        // Clear dirty flag and validate
        setDirty(false);
        validate();
    }
    
    /**
     * Validates the milestone data.
     * Sets the valid property and error message.
     * Made public for testing purposes.
     */
    public void validate() {
        List<String> errors = new ArrayList<>();
        
        // Check required fields
        if (name.get() == null || name.get().trim().isEmpty()) {
            errors.add("Milestone name cannot be empty");
        }
        
        if (date.get() == null) {
            errors.add("Milestone date cannot be empty");
        }
        
        if (project.get() == null) {
            errors.add("Project cannot be null");
        }
        
        // Validate date is within project timeline if project is set
        if (date.get() != null && project.get() != null) {
            if (date.get().isBefore(project.get().getStartDate())) {
                errors.add("Milestone date cannot be before project start date");
            }
            
            if (date.get().isAfter(project.get().getHardDeadline())) {
                errors.add("Milestone date cannot be after project hard deadline");
            }
        }
        
        // Update valid property and error message
        valid.set(errors.isEmpty());
        if (!errors.isEmpty()) {
            setErrorMessage(String.join("\n", errors));
        } else {
            clearErrorMessage();
        }
    }
    
    /**
     * Saves the milestone.
     * Called when the save command is executed.
     */
    private void save() {
        if (!valid.get() || !isDirty()) {
            return;
        }
        
        try {
            Milestone savedMilestone;
            
            if (isNewMilestone.get()) {
                // Create new milestone
                savedMilestone = milestoneService.createMilestone(
                    name.get(),
                    date.get(),
                    project.get().getId(),
                    description.get()
                );
            } else {
                // Update existing milestone
                Milestone existingMilestone = milestone.get();
                
                // First update the name if needed
                if (!name.get().equals(existingMilestone.getName())) {
                    existingMilestone.setName(name.get());
                    existingMilestone = milestoneService.save(existingMilestone);
                }
                
                // Update date if needed
                if (!date.get().equals(existingMilestone.getDate())) {
                    savedMilestone = milestoneService.updateMilestoneDate(
                        existingMilestone.getId(),
                        date.get()
                    );
                } else {
                    savedMilestone = existingMilestone;
                }
                
                // Update description if needed
                if (description.get() != null && 
                    (savedMilestone.getDescription() == null || 
                     !description.get().equals(savedMilestone.getDescription()))) {
                    savedMilestone = milestoneService.updateDescription(
                        savedMilestone.getId(),
                        description.get()
                    );
                }
            }
            
            // Update milestone property with saved milestone
            milestone.set(savedMilestone);
            
            // Clear dirty flag
            setDirty(false);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving milestone", e);
            setErrorMessage("Failed to save milestone: " + e.getMessage());
        }
    }
    
    /**
     * Gets whether the input is valid.
     * 
     * @return true if the input is valid, false otherwise
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
     * Gets the new milestone flag.
     * 
     * @return true if this is a new milestone, false otherwise
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
    
    // Name property accessors
    public StringProperty nameProperty() {
        return name;
    }
    
    public String getName() {
        return name.get();
    }
    
    public void setName(String value) {
        name.set(value);
    }
    
    // Description property accessors
    public StringProperty descriptionProperty() {
        return description;
    }
    
    public String getDescription() {
        return description.get();
    }
    
    public void setDescription(String value) {
        description.set(value);
    }
    
    // Date property accessors
    public ObjectProperty<LocalDate> dateProperty() {
        return date;
    }
    
    public LocalDate getDate() {
        return date.get();
    }
    
    public void setDate(LocalDate value) {
        date.set(value);
    }
    
    // Project property accessors
    public ObjectProperty<Project> projectProperty() {
        return project;
    }
    
    public Project getProject() {
        return project.get();
    }
    
    public void setProject(Project value) {
        project.set(value);
    }
    
    // Additional setters
    public void setMilestone(Milestone value) {
        milestone.set(value);
    }
    
    public void setIsNewMilestone(boolean value) {
        isNewMilestone.set(value);
    }
    
    public void setValid(boolean value) {
        valid.set(value);
    }
    
    /**
     * Cleans up resources when the ViewModel is no longer needed
     */
    @Override
    public void cleanupResources() {
        super.cleanupResources();
        // Clear any references to avoid memory leaks
        closeDialogAction = null;
    }
}