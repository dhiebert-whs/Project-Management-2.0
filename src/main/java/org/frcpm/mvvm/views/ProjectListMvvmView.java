// src/main/java/org/frcpm/mvvm/views/ProjectListMvvmView.java

package org.frcpm.mvvm.views;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
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
 * This is the first implementation using the MVVMFx framework.
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
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initializing ProjectListMvvmView");
        this.resources = resources;
        
        // Set up project list view
        projectListView.setItems(viewModel.getProjects());
        
        // Set cell factory for better display
        projectListView.setCellFactory(lv -> new javafx.scene.control.ListCell<Project>() {
            @Override
            protected void updateItem(Project item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
        
        // Bind selected project
        projectListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            viewModel.setSelectedProject(newVal);
        });
        
        // Bind command buttons using CommandAdapter to convert MVVMFx commands to the app's command system
        CommandAdapter.bindCommandButton(newProjectButton, viewModel.getNewProjectCommand());
        CommandAdapter.bindCommandButton(openProjectButton, viewModel.getOpenProjectCommand());
        CommandAdapter.bindCommandButton(importProjectButton, viewModel.getImportProjectCommand());
        CommandAdapter.bindCommandButton(deleteProjectButton, viewModel.getDeleteProjectCommand());
        
        // Bind error message
        errorLabel.textProperty().bind(viewModel.errorMessageProperty());
        errorLabel.visibleProperty().bind(viewModel.errorMessageProperty().isNotEmpty());
        
        // Bind loading indicator
        loadingIndicator.visibleProperty().bind(viewModel.loadingProperty());
        
        // Initial data load
        viewModel.getLoadProjectsCommand().execute();
    }
    
    /**
     * Handle new project button click.
     */
    @FXML
    private void onNewProjectAction() {
        viewModel.getNewProjectCommand().execute();
    }
    
    /**
     * Handle open project button click.
     */
    @FXML
    private void onOpenProjectAction() {
        viewModel.getOpenProjectCommand().execute();
    }
    
    /**
     * Handle import project button click.
     */
    @FXML
    private void onImportProjectAction() {
        viewModel.getImportProjectCommand().execute();
    }
    
    /**
     * Handle delete project button click.
     */
    @FXML
    private void onDeleteProjectAction() {
        if (viewModel.getSelectedProject() == null) {
            // Show alert about no selection
            String message = resources.getString("info.no.selection.project");
            // This would typically show an alert dialog
            LOGGER.warning(message);
            return;
        }
        
        // Confirm deletion
        String confirmMessage = resources.getString("project.delete.confirm") + 
            " '" + viewModel.getSelectedProject().getName() + "'?";
        
        // This would typically show a confirmation dialog
        LOGGER.info(confirmMessage);
        
        // Execute delete command (this would normally happen after confirmation)
        viewModel.getDeleteProjectCommand().execute();
    }
}