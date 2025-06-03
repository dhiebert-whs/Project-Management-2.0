// src/main/java/org/frcpm/mvvm/views/ProjectListMvvmView.java

package org.frcpm.mvvm.views;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import org.frcpm.models.Project;
import org.frcpm.mvvm.CommandAdapter;
import org.frcpm.mvvm.viewmodels.ProjectListMvvmViewModel;

/**
 * View for the project list using MVVMFx.
 * FIXED: Uses deferred binding pattern to avoid ViewModel null access during initialize().
 */
public class ProjectListMvvmView implements FxmlView<ProjectListMvvmViewModel>, Initializable {
    
    private static final Logger LOGGER = Logger.getLogger(ProjectListMvvmView.class.getName());
    
    @FXML
    private BorderPane mainPane;
    
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
    
    @FXML
    private Label errorLabel;
    
    @FXML
    private ProgressIndicator loadingIndicator;
    
    @InjectViewModel
    private ProjectListMvvmViewModel viewModel;
    
    private ResourceBundle resources;
    private boolean bindingComplete = false;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initializing ProjectListMvvmView");
        this.resources = resources;
        
        // DO NOT ACCESS viewModel HERE - it's still null!
        // Instead, set up basic UI without ViewModel binding
        setupBasicUI();
        
        // Schedule binding for later when ViewModel is injected
        Platform.runLater(this::bindControlsWhenReady);
    }
    
    /**
     * Sets up basic UI components that don't require ViewModel.
     */
    private void setupBasicUI() {
        // Set cell factory for better display
        projectListView.setCellFactory(lv -> new javafx.scene.control.ListCell<Project>() {
            @Override
            protected void updateItem(Project item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
        
        // Hide error and loading indicators initially
        if (errorLabel != null) {
            errorLabel.setVisible(false);
        }
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(false);
        }
    }
    
    /**
     * Binds controls to ViewModel when it's ready.
     * Uses deferred binding pattern to handle MVVMFx injection timing.
     */
    private void bindControlsWhenReady() {
        if (viewModel != null && !bindingComplete) {
            LOGGER.info("ViewModel is ready, binding controls");
            bindControls();
            bindingComplete = true;
        } else if (viewModel == null) {
            // ViewModel still not ready, try again later
            Platform.runLater(this::bindControlsWhenReady);
        }
    }
    
    /**
     * Binds all controls to the ViewModel.
     * This is called after ViewModel injection is complete.
     */
    private void bindControls() {
        try {
            // Bind project list view
            if (viewModel.getProjects() != null) {
                projectListView.setItems(viewModel.getProjects());
            }
            
            // Bind selected project
            projectListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (viewModel != null) {
                    viewModel.setSelectedProject(newVal);
                }
            });
            
            // Bind command buttons using CommandAdapter to convert MVVMFx commands
            if (newProjectButton != null && viewModel.getNewProjectCommand() != null) {
                CommandAdapter.bindCommandButton(newProjectButton, viewModel.getNewProjectCommand());
            }
            if (openProjectButton != null && viewModel.getOpenProjectCommand() != null) {
                CommandAdapter.bindCommandButton(openProjectButton, viewModel.getOpenProjectCommand());
            }
            if (importProjectButton != null && viewModel.getImportProjectCommand() != null) {
                CommandAdapter.bindCommandButton(importProjectButton, viewModel.getImportProjectCommand());
            }
            if (deleteProjectButton != null && viewModel.getDeleteProjectCommand() != null) {
                CommandAdapter.bindCommandButton(deleteProjectButton, viewModel.getDeleteProjectCommand());
            }
            
            // Bind error message with null safety
            if (errorLabel != null && viewModel.errorMessageProperty() != null) {
                errorLabel.textProperty().bind(viewModel.errorMessageProperty());
                errorLabel.visibleProperty().bind(viewModel.errorMessageProperty().isNotEmpty());
            }
            
            // Bind loading indicator with null safety
            if (loadingIndicator != null && viewModel.loadingProperty() != null) {
                loadingIndicator.visibleProperty().bind(viewModel.loadingProperty());
            }
            
            // Initial data load after binding is complete
            if (viewModel.getLoadProjectsCommand() != null) {
                viewModel.getLoadProjectsCommand().execute();
            }
            
            LOGGER.info("Control binding completed successfully");
            
        } catch (Exception e) {
            LOGGER.severe("Error binding controls: " + e.getMessage());
            e.printStackTrace();
            // Graceful degradation - UI still works even if binding fails
        }
    }
    
    /**
     * Handle new project button click.
     */
    @FXML
    private void onNewProjectAction() {
        if (viewModel != null && viewModel.getNewProjectCommand() != null) {
            viewModel.getNewProjectCommand().execute();
        }
    }
    
    /**
     * Handle open project button click.
     */
    @FXML
    private void onOpenProjectAction() {
        if (viewModel != null && viewModel.getOpenProjectCommand() != null) {
            viewModel.getOpenProjectCommand().execute();
        }
    }
    
    /**
     * Handle import project button click.
     */
    @FXML
    private void onImportProjectAction() {
        if (viewModel != null && viewModel.getImportProjectCommand() != null) {
            viewModel.getImportProjectCommand().execute();
        }
    }
    
    /**
     * Handle delete project button click.
     */
    @FXML
    private void onDeleteProjectAction() {
        if (viewModel == null || viewModel.getSelectedProject() == null) {
            // Show alert about no selection
            String message = resources != null ? 
                resources.getString("info.no.selection.project") : 
                "No project selected";
            LOGGER.warning(message);
            return;
        }
        
        // Confirm deletion
        String confirmMessage = (resources != null ? 
            resources.getString("project.delete.confirm") : 
            "Delete project") + " '" + viewModel.getSelectedProject().getName() + "'?";
        
        // This would typically show a confirmation dialog
        LOGGER.info(confirmMessage);
        
        // Execute delete command
        if (viewModel.getDeleteProjectCommand() != null) {
            viewModel.getDeleteProjectCommand().execute();
        }
    }
}