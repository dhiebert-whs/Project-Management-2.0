package org.frcpm.presenters;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import org.frcpm.di.ServiceProvider;
import org.frcpm.di.ViewLoader;
import org.frcpm.models.Project;
import org.frcpm.services.ProjectService;
import org.frcpm.viewmodels.MainViewModel;
import org.frcpm.views.ProjectView;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Presenter (Controller) for the main view of the application.
 * This follows the AfterburnerFX presenter convention.
 */
public class MainPresenter implements Initializable {
    
    private static final Logger LOGGER = Logger.getLogger(MainPresenter.class.getName());
    
    @FXML
    private BorderPane mainBorderPane;
    
    @FXML
    private MenuItem fileNewProjectMenuItem;
    
    @FXML
    private MenuItem fileOpenProjectMenuItem;
    
    @FXML
    private MenuItem fileExitMenuItem;
    
    @FXML
    private ListView<Project> projectListView;
    
    @FXML
    private Button newProjectButton;
    
    @FXML
    private Button openProjectButton;
    
    @Inject
    private ProjectService projectService;
    
    @Inject
    private MainViewModel viewModel;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initializing MainPresenter");
        
        // Verify injection - create fallback if needed
        if (viewModel == null) {
            LOGGER.severe("MainViewModel not injected - creating manually as fallback");
            viewModel = new MainViewModel();
        }
        
        // Set up UI bindings
        setupBindings();
        
        // Set up error handling
        setupErrorHandling();
        
        // Load projects
        loadProjects();
    }
    
    /**
     * Sets up UI bindings.
     */
    private void setupBindings() {
        // Set up project list
        projectListView.setItems(viewModel.getProjectList());
        
        // Set up actions
        fileNewProjectMenuItem.setOnAction(e -> newProject());
        fileOpenProjectMenuItem.setOnAction(e -> openProject());
        fileExitMenuItem.setOnAction(e -> exit());
        
        newProjectButton.setOnAction(e -> newProject());
        openProjectButton.setOnAction(e -> openProject());
        
        // Set up selection change listener
        projectListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> viewModel.setSelectedProject(newValue));
        
        // Disable open button if no project is selected
        openProjectButton.disableProperty().bind(
                projectListView.getSelectionModel().selectedItemProperty().isNull());
    }
    
    /**
     * Sets up error message handling.
     */
    private void setupErrorHandling() {
        if (viewModel == null) {
            LOGGER.warning("ViewModel not initialized - likely in test environment");
            return;
        }
        
        // Show an alert when error message changes
        viewModel.errorMessageProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                showError("Error", newVal);
                viewModel.clearErrorMessage();
            }
        });
    }
    
    /**
     * Loads projects using the ViewModel's command.
     */
    private void loadProjects() {
        try {
            viewModel.getLoadProjectsCommand().execute();
        } catch (Exception e) {
            LOGGER.severe("Error loading projects: " + e.getMessage());
            showError("Error loading projects", e.getMessage());
        }
    }
    
    /**
     * Creates a new project.
     */
    private void newProject() {
        ViewLoader.showDialog(ProjectView.class, "New Project", mainBorderPane.getScene().getWindow());
        loadProjects(); // Reload after dialog closes
    }
    
    /**
     * Opens the selected project.
     */
    private void openProject() {
        Project selectedProject = projectListView.getSelectionModel().getSelectedItem();
        if (selectedProject != null) {
            ViewLoader.showView(ProjectView.class, "Project: " + selectedProject.getName(), 
                    mainBorderPane.getScene().getWindow());
        }
    }
    
    /**
     * Shows an error dialog.
     * 
     * @param title the title of the dialog
     * @param message the error message
     */
    private void showError(String title, String message) {
        try {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(title);
            alert.setContentText(message);
            alert.showAndWait();
        } catch (Exception e) {
            // This can happen in tests when not on FX thread
            LOGGER.log(Level.INFO, "Alert would show: {0} - {1}", new Object[] { title, message });
        }
    }
    
    /**
     * Exits the application.
     */
    private void exit() {
        mainBorderPane.getScene().getWindow().hide();
    }
    
    /**
     * For testing - gets the view model.
     * 
     * @return the view model
     */
    public MainViewModel getViewModel() {
        return viewModel;
    }
    
    /**
     * For testing - sets the view model.
     * 
     * @param viewModel the view model to set
     */
    public void setViewModel(MainViewModel viewModel) {
        this.viewModel = viewModel;
        
        // Reset bindings if UI is available
        if (projectListView != null) {
            setupBindings();
            setupErrorHandling();
        }
    }
    
}