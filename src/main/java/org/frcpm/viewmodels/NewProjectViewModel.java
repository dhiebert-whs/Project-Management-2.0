package org.frcpm.viewmodels;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.frcpm.binding.Command;
import org.frcpm.di.ServiceProvider;
import org.frcpm.models.Project;
import org.frcpm.services.ProjectService;


import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ViewModel for the new project dialog in the MVVM architecture.
 * Handles business logic for project creation.
 */
public class NewProjectViewModel extends BaseViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(NewProjectViewModel.class.getName());
    
    // Services
    private final ProjectService projectService;
    
    // Observable properties
    private final StringProperty projectName = new SimpleStringProperty("");
    private final ObjectProperty<LocalDate> startDate = new SimpleObjectProperty<>(LocalDate.now());
    private final ObjectProperty<LocalDate> goalEndDate = new SimpleObjectProperty<>(LocalDate.now().plusWeeks(6));
    private final ObjectProperty<LocalDate> hardDeadline = new SimpleObjectProperty<>(LocalDate.now().plusWeeks(8));
    private final StringProperty description = new SimpleStringProperty("");
    private final BooleanProperty inputValid = new SimpleBooleanProperty(false);
    
    // Result
    private Project createdProject;
    
    // Commands
    private final Command createProjectCommand;
    private final Command cancelCommand;
    
    /**
     * Creates a new NewProjectViewModel with dependency injection.
     * This constructor is used by AfterburnerFX.
     */
    public NewProjectViewModel() {
        this(ServiceProvider.getProjectService());
    }
    
    /**
     * Creates a new NewProjectViewModel with the specified service.
     * This constructor is mainly used for testing.
     * 
     * @param projectService the project service
     */
    public NewProjectViewModel(ProjectService projectService) {
        this.projectService = projectService;
        
        // Initialize with default values
        initializeDefaultDates();
        
        // Set up commands
        createProjectCommand = new Command(this::createProject, this::canCreateProject);
        cancelCommand = new Command(() -> {/* Will be handled by controller */});
        
        // Set up property listeners for validation
        setupValidation();
    }
    
    /**
     * Initializes default dates.
     */
    private void initializeDefaultDates() {
        LocalDate today = LocalDate.now();
        startDate.set(today);
        goalEndDate.set(today.plusWeeks(6));
        hardDeadline.set(today.plusWeeks(8));
    }
    
    /**
     * Sets up validation listeners.
     */
    private void setupValidation() {
        // Validate input when properties change
        projectName.addListener((observable, oldValue, newValue) -> validateInput());
        startDate.addListener((observable, oldValue, newValue) -> validateInput());
        goalEndDate.addListener((observable, oldValue, newValue) -> validateInput());
        hardDeadline.addListener((observable, oldValue, newValue) -> validateInput());
        
        // Initial validation
        validateInput();
    }
    
    /**
     * Creates a new project using the current property values.
     */
    public void createProject() {
        if (!validateInput()) {
            return;
        }
        
        try {
            clearErrorMessage();
            
            // Create the project
            Project project = projectService.createProject(
                projectName.get(),
                startDate.get(),
                goalEndDate.get(),
                hardDeadline.get()
            );
            
            // Set description if provided
            String descriptionText = description.get();
            if (descriptionText != null && !descriptionText.isEmpty()) {
                project = projectService.updateProject(
                    project.getId(),
                    projectName.get(),
                    startDate.get(),
                    goalEndDate.get(),
                    hardDeadline.get(),
                    descriptionText
                );
            }
            
            // Store the created project
            createdProject = project;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating project", e);
            setErrorMessage("Failed to create the project: " + e.getMessage());
        }
    }
    
    /**
     * Validates the input fields.
     * 
     * @return true if the input is valid, false otherwise
     */
    private boolean validateInput() {
        // Check if required fields are filled
        if (projectName.get() == null || projectName.get().trim().isEmpty()) {
            inputValid.set(false);
            return false;
        }
        
        if (startDate.get() == null || goalEndDate.get() == null || hardDeadline.get() == null) {
            inputValid.set(false);
            return false;
        }
        
        // Check date relationships
        LocalDate start = startDate.get();
        LocalDate goal = goalEndDate.get();
        LocalDate deadline = hardDeadline.get();
        
        if (goal.isBefore(start) || deadline.isBefore(start)) {
            inputValid.set(false);
            return false;
        }
        
        // All validations passed
        inputValid.set(true);
        return true;
    }
    
    /**
     * Condition method for the create project command.
     * 
     * @return true if a project can be created, false otherwise
     */
    private boolean canCreateProject() {
        return inputValid.get();
    }
    
    /**
     * Gets the created project.
     * 
     * @return the created project, or null if no project was created
     */
    public Project getCreatedProject() {
        return createdProject;
    }
    
    // Getters and setters for properties
    
    public StringProperty projectNameProperty() {
        return projectName;
    }
    
    public String getProjectName() {
        return projectName.get();
    }
    
    public void setProjectName(String name) {
        projectName.set(name);
    }
    
    public ObjectProperty<LocalDate> startDateProperty() {
        return startDate;
    }
    
    public LocalDate getStartDate() {
        return startDate.get();
    }
    
    public void setStartDate(LocalDate date) {
        startDate.set(date);
    }
    
    public ObjectProperty<LocalDate> goalEndDateProperty() {
        return goalEndDate;
    }
    
    public LocalDate getGoalEndDate() {
        return goalEndDate.get();
    }
    
    public void setGoalEndDate(LocalDate date) {
        goalEndDate.set(date);
    }
    
    public ObjectProperty<LocalDate> hardDeadlineProperty() {
        return hardDeadline;
    }
    
    public LocalDate getHardDeadline() {
        return hardDeadline.get();
    }
    
    public void setHardDeadline(LocalDate date) {
        hardDeadline.set(date);
    }
    
    public StringProperty descriptionProperty() {
        return description;
    }
    
    public String getDescription() {
        return description.get();
    }
    
    public void setDescription(String text) {
        description.set(text);
    }
    
    public BooleanProperty inputValidProperty() {
        return inputValid;
    }
    
    public boolean isInputValid() {
        return inputValid.get();
    }
    
    // Command getters
    
    public Command getCreateProjectCommand() {
        return createProjectCommand;
    }
    
    public Command getCancelCommand() {
        return cancelCommand;
    }
}