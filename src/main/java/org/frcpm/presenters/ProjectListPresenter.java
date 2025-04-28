package org.frcpm.presenters;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import org.frcpm.di.ServiceProvider;
import org.frcpm.di.ViewLoader;
import org.frcpm.models.Project;
import org.frcpm.services.ProjectService;
import org.frcpm.viewmodels.ProjectListViewModel;
import org.frcpm.views.ProjectView;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Presenter for the project list view.
 * This follows the AfterburnerFX presenter convention.
 */
public class ProjectListPresenter implements Initializable {
    
    private static final Logger LOGGER = Logger.getLogger(ProjectListPresenter.class.getName());
    
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
    
    @Inject
    private ProjectService projectService;
    
    private ProjectListViewModel viewModel;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initializing ProjectListPresenter");
        
        // Create view model
        viewModel = new ProjectListViewModel(projectService);
        
        // Set up UI bindings
        setupBindings();
        
        // Load projects
        loadProjects();
    }
    
    /**
     * Sets up UI bindings.
     */
    private void setupBindings() {
        // Bind the list view to the projects in the view model
        projectListView.setItems(viewModel.getProjects());
        
        // Bind button actions
        newProjectButton.setOnAction(e -> handleNewProject());
        openProjectButton.setOnAction(e -> handleOpenProject());
        importProjectButton.setOnAction(e -> handleImportProject());
        deleteProjectButton.setOnAction(e -> handleDeleteProject());
        
        // Set up selection binding
        projectListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> viewModel.setSelectedProject(newValue));
        
        // Disable buttons that need a selection if no project is selected
        openProjectButton.disableProperty().bind(
                projectListView.getSelectionModel().selectedItemProperty().isNull());
        deleteProjectButton.disableProperty().bind(
                projectListView.getSelectionModel().selectedItemProperty().isNull());
    }
    
    /**
     * Loads projects.
     */
    private void loadProjects() {
        try {
            viewModel.loadProjects();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading projects", e);
            showError("Error Loading Projects", "Failed to load projects: " + e.getMessage());
        }
    }
    
    /**
     * Handles creating a new project.
     */
    private void handleNewProject() {
        try {
            ViewLoader.showDialog(ProjectView.class, "New Project", 
                    projectListView.getScene().getWindow());
            loadProjects(); // Reload after dialog closes
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating new project", e);
            showError("Error Creating Project", "Failed to create new project: " + e.getMessage());
        }
    }
    
    /**
     * Handles opening a project.
     */
    private void handleOpenProject() {
        Project selectedProject = projectListView.getSelectionModel().getSelectedItem();
        if (selectedProject != null) {
            try {
                ViewLoader.showView(ProjectView.class, "Project: " + selectedProject.getName(), 
                        projectListView.getScene().getWindow());
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error opening project", e);
                showError("Error Opening Project", "Failed to open project: " + e.getMessage());
            }
        }
    }
    
    /**
     * Handles importing a project.
     */
    private void handleImportProject() {
        // Not implemented yet
        showInfo("Import Project", "This feature is not implemented yet.");
    }
    
    /**
     * Handles deleting a project.
     */
    private void handleDeleteProject() {
        Project selectedProject = projectListView.getSelectionModel().getSelectedItem();
        if (selectedProject != null) {
            try {
                if (showConfirmation("Delete Project", 
                        "Are you sure you want to delete the project '" + 
                                selectedProject.getName() + "'?")) {
                    viewModel.deleteProject(selectedProject);
                    loadProjects(); // Reload after deletion
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error deleting project", e);
                showError("Error Deleting Project", "Failed to delete project: " + e.getMessage());
            }
        }
    }
    
    /**
     * Shows an error dialog.
     * 
     * @param title the title of the dialog
     * @param message the error message
     */
    private void showError(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Shows an information dialog.
     * 
     * @param title the title of the dialog
     * @param message the information message
     */
    private void showInfo(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Shows a confirmation dialog.
     * 
     * @param title the title of the dialog
     * @param message the confirmation message
     * @return true if the user confirmed, false otherwise
     */
    private boolean showConfirmation(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(title);
        alert.setContentText(message);
        
        java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK;
    }
}