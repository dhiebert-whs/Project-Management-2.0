// src/main/java/org/frcpm/controllers/ProjectListController.java
package org.frcpm.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.frcpm.binding.ViewModelBinding;
import org.frcpm.models.Project;
import org.frcpm.services.DialogService;
import org.frcpm.services.ServiceFactory;
import org.frcpm.viewmodels.ProjectListViewModel;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProjectListController {
    private static final Logger LOGGER = Logger.getLogger(ProjectListController.class.getName());
    
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
    
    private ProjectListViewModel viewModel = new ProjectListViewModel();
    private DialogService dialogService = ServiceFactory.getDialogService();
    
    @FXML
    private void initialize() {
        LOGGER.info("Initializing ProjectListController");
        
        // Set up bindings
        projectListView.setItems(viewModel.getProjects());
        
        // Bind buttons to commands
        ViewModelBinding.bindCommandButton(newProjectButton, viewModel.getNewProjectCommand());
        ViewModelBinding.bindCommandButton(openProjectButton, viewModel.getOpenProjectCommand());
        ViewModelBinding.bindCommandButton(importProjectButton, viewModel.getImportProjectCommand());
        ViewModelBinding.bindCommandButton(deleteProjectButton, viewModel.getDeleteProjectCommand());
        
        // Enable the open project button
        openProjectButton.setDisable(false);
        
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
        
        // Refresh the project list
        refreshProjectList();
    }
    
    /**
     * Refreshes the project list.
     */
    public void refreshProjectList() {
        viewModel.getLoadProjectsCommand().execute();
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
            showErrorAlert("No Selection", "Please select a project to open");
        }
    }
    
    /**
     * Creates a new project.
     */
    @FXML
    public void handleNewProject() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/NewProjectDialog.fxml"));
            Parent dialogView = loader.load();
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Create New Project");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(projectListView.getScene().getWindow());
            dialogStage.setScene(new Scene(dialogView));
            
            NewProjectController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            
            dialogStage.showAndWait();
            
            // After dialog closes, check if a project was created
            Project createdProject = controller.getCreatedProject();
            if (createdProject != null) {
                refreshProjectList();
                // Select and open the new project
                viewModel.setSelectedProject(createdProject);
                openProject(createdProject);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading new project dialog", e);
            showErrorAlert("Error", "Failed to open new project dialog: " + e.getMessage());
        }
    }
    
    /**
     * Handles the delete project action.
     */
    @FXML
    public void handleDeleteProject() {
        Project selectedProject = projectListView.getSelectionModel().getSelectedItem();
        
        if (selectedProject == null) {
            showErrorAlert("No Selection", "Please select a project to delete");
            return;
        }
        
        boolean confirmed = showConfirmationAlert(
            "Delete Project",
            "Are you sure you want to delete project '" + selectedProject.getName() + "'?");
            
        if (confirmed) {
            viewModel.setSelectedProject(selectedProject);
            viewModel.getDeleteProjectCommand().execute();
            refreshProjectList();
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
                showErrorAlert("Invalid Project", "The project is missing required date fields");
                return;
            }
            
            // Validate competition date/deadline
            if (project.getHardDeadline() == null) {
                showErrorAlert("Invalid Project", "Competition date is required");
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ProjectView.fxml"));
            Parent projectView = loader.load();
            
            Stage stage = (Stage) projectListView.getScene().getWindow();
            stage.setTitle("FRC Project Management - " + project.getName());
            
            // Get the controller and set the project
            ProjectController controller = loader.getController();
            controller.setProject(project);
            
            // Show the project view
            stage.setScene(new Scene(projectView));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error opening project", e);
            showErrorAlert("Error", "Failed to open project: " + e.getMessage());
        }
    }
    
    /**
     * Shows an error alert dialog.
     * 
     * @param title the title
     * @param message the message
     */
    private void showErrorAlert(String title, String message) {
        try {
            dialogService.showErrorAlert(title, message);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error showing alert", e);
        }
    }
    
    /**
     * Shows a confirmation alert dialog.
     * 
     * @param title the title
     * @param message the message
     * @return true if confirmed, false otherwise
     */
    private boolean showConfirmationAlert(String title, String message) {
        try {
            return dialogService.showConfirmationAlert(title, message);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error showing confirmation dialog", e);
            return false;
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
     * Sets the dialog service (for testing).
     * 
     * @param dialogService the dialog service
     */
    public void setDialogService(DialogService dialogService) {
        this.dialogService = dialogService;
    }
}