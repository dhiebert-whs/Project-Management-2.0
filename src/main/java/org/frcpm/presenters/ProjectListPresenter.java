package org.frcpm.presenters;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.frcpm.binding.ViewModelBinding;
import org.frcpm.di.DialogFactory;
import org.frcpm.di.ViewLoader;
import org.frcpm.models.Project;
import org.frcpm.services.DialogService;
import org.frcpm.services.ProjectService;
import org.frcpm.services.impl.ProjectServiceImpl;
import org.frcpm.utils.ErrorHandler;
import org.frcpm.viewmodels.ProjectListViewModel;
import org.frcpm.views.NewProjectDialogView;
import org.frcpm.views.ProjectView;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Presenter for the project list view using AfterburnerFX pattern.
 */
public class ProjectListPresenter implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(ProjectListPresenter.class.getName());

    // FXML UI components
    @FXML
    private ListView<Project> projectListView;
    
    @FXML
    private Button newProjectButton;
    
    @FXML
    private Button openProjectButton;
    
    @FXML
    private Button importProjectButton;
    
    @FXML
    private Button deleteProjectButton;
    
    // Injected services
    @Inject
    private ProjectService projectService;
    
    @Inject
    private DialogService dialogService;

    // ViewModel and resources
    private ProjectListViewModel viewModel;
    private ResourceBundle resources;

    /**
     * Default constructor required by AfterburnerFX.
     */
    public ProjectListPresenter() {
        LOGGER.info("ProjectListPresenter constructor called");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initializing ProjectListPresenter with resource bundle");
        
        this.resources = resources;
        
        try {
            // Create view model with injected service
            if (projectService == null) {
                LOGGER.warning("ProjectService was null in initialize - attempting to create fallback");
                // Fallback only if injection failed
                projectService = new ProjectServiceImpl();
            }
            
            // Create view model and initialize it
            viewModel = new ProjectListViewModel(projectService);

            // Set up bindings between UI and ViewModel
            setupBindings();
            
            // Load the project list
            refreshProjectList();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during initialization", e);
            ErrorHandler.showError("Initialization Error", 
                "Failed to initialize the project list: " + e.getMessage(), e);
        }
    }

    /**
     * Sets up the bindings between UI controls and ViewModel properties.
     */
    private void setupBindings() {
        // Check for null UI components for testability
        if (projectListView == null || newProjectButton == null || openProjectButton == null || 
            importProjectButton == null || deleteProjectButton == null) {
            LOGGER.warning("UI components not initialized - likely in test environment");
            return;
        }

        // Set up list view
        projectListView.setItems(viewModel.getProjects());
        projectListView.setCellFactory(lv -> new ListCell<Project>() {
            @Override
            protected void updateItem(Project project, boolean empty) {
                super.updateItem(project, empty);
                if (empty || project == null) {
                    setText(null);
                } else {
                    setText(project.getName());
                }
            }
        });
        
        // Setup selection handling
        projectListView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> viewModel.setSelectedProject(newValue));
        
        // Setup double-click handler for opening projects
        projectListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Project selectedProject = projectListView.getSelectionModel().getSelectedItem();
                if (selectedProject != null) {
                    openProject(selectedProject);
                }
            }
        });
        
        // Set up button handlers
        newProjectButton.setOnAction(event -> handleNewProject());
        openProjectButton.setOnAction(event -> handleOpenProject());
        importProjectButton.setOnAction(event -> {/* TODO: Implement import */});
        deleteProjectButton.setOnAction(event -> handleDeleteProject());
        
        // Enable the open project button
        openProjectButton.setDisable(false);
    }

    /**
     * Refreshes the project list.
     */
    public void refreshProjectList() {
        try {
            viewModel.loadProjects();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading projects", e);
            ErrorHandler.showError("Error Loading Projects", 
                "Failed to load the list of projects: " + e.getMessage(), e);
        }
    }
    
    /**
     * Opens the selected project.
     */
    @FXML
    public void handleOpenProject() {
        Project selectedProject = projectListView.getSelectionModel().getSelectedItem();
        if (selectedProject != null) {
            openProject(selectedProject);
        } else {
            ErrorHandler.showError("No Selection", "Please select a project to open");
        }
    }
    
    /**
     * Creates a new project.
     */
    @FXML
    public void handleNewProject() {
        try {
            // Use AfterburnerFX ViewLoader to create the dialog
            NewProjectDialogPresenter presenter = DialogFactory.showDialog(
                NewProjectDialogView.class, 
                resources != null ? resources.getString("project.new.title") : "New Project", 
                projectListView.getScene().getWindow(),
                resources,
                null);
            
            // After dialog closes, check if a project was created
            if (presenter != null) {
                Project createdProject = presenter.getCreatedProject();
                if (createdProject != null) {
                    refreshProjectList();
                    // Select and open the new project
                    viewModel.setSelectedProject(createdProject);
                    openProject(createdProject);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error showing new project dialog", e);
            ErrorHandler.showError(
                resources != null ? resources.getString("error.title") : "Error", 
                resources != null ? resources.getString("error.project.dialog.failed") : "Failed to open project dialog", e);
        }
    }
    
    /**
     * Handles the delete project action.
     */
    @FXML
    public void handleDeleteProject() {
        Project selectedProject = projectListView.getSelectionModel().getSelectedItem();
        
        if (selectedProject == null) {
            ErrorHandler.showError(
                resources != null ? resources.getString("info.title") : "Information", 
                resources != null ? resources.getString("info.no.selection.project") : "Please select a project first");
            return;
        }
        
        boolean confirmed = ErrorHandler.showConfirmation(
            resources != null ? resources.getString("project.delete.title") : "Delete Project",
            (resources != null ? resources.getString("project.delete.confirm") : "Are you sure you want to delete the project") 
                + " '" + selectedProject.getName() + "'?");
            
        if (confirmed) {
            try {
                viewModel.setSelectedProject(selectedProject);
                if (viewModel.deleteProject(selectedProject)) {
                    refreshProjectList();
                } else {
                    ErrorHandler.showError(
                        resources != null ? resources.getString("error.title") : "Error", 
                        resources != null ? resources.getString("error.project.delete.failed") : "Failed to delete project");
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error deleting project", e);
                ErrorHandler.showError(
                    resources != null ? resources.getString("error.title") : "Error", 
                    resources != null ? resources.getString("error.project.delete.failed") : "Failed to delete project", e);
            }
        }
    }
    
    /**
     * Opens a project.
     * 
     * @param project the project to open
     */
    private void openProject(Project project) {
        try {
            // Ensure the project has all required fields
            if (project.getStartDate() == null || project.getGoalEndDate() == null || 
                project.getHardDeadline() == null) {
                ErrorHandler.showError("Invalid Project", "The project is missing required date fields");
                return;
            }
            
            // Validate competition date/deadline
            if (project.getHardDeadline() == null) {
                ErrorHandler.showError("Invalid Project", "Competition date is required");
                return;
            }
            
            // Use AfterburnerFX ViewLoader to load the project view
            Parent root = ViewLoader.loadView(ProjectView.class);
            
            // Get the controller and set the project
            ProjectPresenter presenter = ViewLoader.loadController(ProjectView.class);
            presenter.setProject(project);
            
            // Show the project view
            Stage stage = (Stage) projectListView.getScene().getWindow();
            stage.setTitle("FRC Project Management - " + project.getName());
            stage.setScene(new Scene(root));
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening project", e);
            ErrorHandler.showError("Error", "Failed to open project", e);
        }
    }
    
    /**
     * Gets the ViewModel.
     * 
     * @return the ViewModel
     */
    public ProjectListViewModel getViewModel() {
        return viewModel;
    }
    
    /**
     * Sets the ViewModel (for testing).
     * 
     * @param viewModel the ViewModel
     */
    public void setViewModel(ProjectListViewModel viewModel) {
        this.viewModel = viewModel;
    }
    
    /**
     * Gets the project service.
     * 
     * @return the project service
     */
    public ProjectService getProjectService() {
        return projectService;
    }
    
    /**
     * Sets the project service (for testing).
     * 
     * @param projectService the project service
     */
    public void setProjectService(ProjectService projectService) {
        this.projectService = projectService;
    }
    
    /**
     * Gets the dialog service.
     * 
     * @return the dialog service
     */
    public DialogService getDialogService() {
        return dialogService;
    }
    
    /**
     * Sets the dialog service (for testing).
     * 
     * @param dialogService the dialog service
     */
    public void setDialogService(DialogService dialogService) {
        this.dialogService = dialogService;
    }
}