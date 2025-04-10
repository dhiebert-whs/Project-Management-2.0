// src/main/java/org/frcpm/controllers/GanttChartController.java
package org.frcpm.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.frcpm.binding.ViewModelBinding;
import org.frcpm.models.Project;
import org.frcpm.services.DialogService;
import org.frcpm.services.ServiceFactory;
import org.frcpm.services.WebViewBridgeService;
import org.frcpm.viewmodels.GanttChartViewModel;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the Gantt chart view.
 * Follows the standardized MVVM pattern by delegating all business logic to the
 * GanttChartViewModel.
 */
public class GanttChartController {
    private static final Logger LOGGER = Logger.getLogger(GanttChartController.class.getName());
    
    // FXML UI components
    @FXML private WebView webView;
    @FXML private Button refreshButton;
    @FXML private ComboBox<GanttChartViewModel.ViewMode> viewModeComboBox;
    @FXML private ComboBox<GanttChartViewModel.FilterOption> filterComboBox;
    @FXML private Button zoomInButton;
    @FXML private Button zoomOutButton;
    @FXML private Button exportButton;
    @FXML private Button todayButton;
    @FXML private ToggleButton milestonesToggle;
    @FXML private ToggleButton dependenciesToggle;
    @FXML private Label statusLabel;
    
    // ViewModel and services
    private GanttChartViewModel viewModel = new GanttChartViewModel();
    private WebViewBridgeService bridgeService = ServiceFactory.getWebViewBridgeService();
    private DialogService dialogService = ServiceFactory.getDialogService();
    
    // Bridge interface initialization flag
    private boolean bridgeInitialized = false;
    
    /**
     * Initializes the controller.
     * This method is automatically called after the FXML has been loaded.
     */
    @FXML
    private void initialize() {
        LOGGER.info("Initializing GanttChartController");
        
        // Always include comprehensive null checks for UI components
        if (webView == null || refreshButton == null || viewModeComboBox == null || 
            filterComboBox == null || zoomInButton == null || zoomOutButton == null || 
            exportButton == null || todayButton == null || milestonesToggle == null ||
            dependenciesToggle == null || statusLabel == null) {
            
            LOGGER.warning("UI components not initialized - likely in test environment");
            return;
        }
        
        // Initialize WebView
        initializeWebView();
        
        // Set up comboboxes
        setupComboBoxes();
        
        // Set up bindings
        setupBindings();
        
        // Set up error message listener
        setupErrorListener();
    }
    
    /**
     * Sets up the WebView component.
     * Protected for testability.
     */
    protected void initializeWebView() {
        WebEngine engine = webView.getEngine();
        
        // Enable JavaScript
        engine.setJavaScriptEnabled(true);
        
        // Load the HTML file from resources
        String htmlUrl = getClass().getResource("/web/gantt-chart.html").toExternalForm();
        engine.load(htmlUrl);
        
        // Set up the bridge between Java and JavaScript
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                // Initialize bridge service
                bridgeService.initialize(engine, viewModel);
                bridgeInitialized = true;
                
                // Set status message
                viewModel.setStatusMessage("Chart loaded successfully");
                
                // If we already have a project set, load the data
                if (viewModel.getProject() != null) {
                    // Give the bridge a moment to initialize fully
                    CompletableFuture.runAsync(() -> {
                        try {
                            Thread.sleep(500);
                            javafx.application.Platform.runLater(() -> {
                                viewModel.getRefreshCommand().execute();
                            });
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    });
                }
            }
        });
    }
    
    /**
     * Sets up the combo boxes.
     * Protected for testability.
     */
    protected void setupComboBoxes() {
        // Set up view mode options
        viewModeComboBox.getItems().setAll(GanttChartViewModel.ViewMode.values());
        viewModeComboBox.setValue(GanttChartViewModel.ViewMode.WEEK);
        
        // Set up filter options
        filterComboBox.getItems().setAll(GanttChartViewModel.FilterOption.values());
        filterComboBox.setValue(GanttChartViewModel.FilterOption.ALL_TASKS);
    }
    
    /**
     * Sets up data bindings between UI and ViewModel.
     * Protected for testability.
     */
    protected void setupBindings() {
        // Bind combo boxes
        ViewModelBinding.bindComboBox(viewModeComboBox, viewModel.viewModeProperty());
        ViewModelBinding.bindComboBox(filterComboBox, viewModel.filterOptionProperty());
        
        // Bind status label
        ViewModelBinding.bindLabel(statusLabel, viewModel.statusMessageProperty());
        
        // Bind toggle buttons
        ViewModelBinding.bindToggleButton(milestonesToggle, viewModel.showMilestonesProperty());
        ViewModelBinding.bindToggleButton(dependenciesToggle, viewModel.showDependenciesProperty());
        
        // Bind commands to buttons
        ViewModelBinding.bindCommandButton(refreshButton, viewModel.getRefreshCommand());
        ViewModelBinding.bindCommandButton(zoomInButton, viewModel.getZoomInCommand());
        ViewModelBinding.bindCommandButton(zoomOutButton, viewModel.getZoomOutCommand());
        ViewModelBinding.bindCommandButton(exportButton, viewModel.getExportCommand());
        ViewModelBinding.bindCommandButton(todayButton, viewModel.getTodayCommand());
    }
    
    /**
     * Sets up error message listener.
     * Protected for testability.
     */
    protected void setupErrorListener() {
        viewModel.errorMessageProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                showErrorAlert("Error", newValue);
                viewModel.clearErrorMessage();
            }
        });
    }
    
    /**
     * Sets the project to display.
     * 
     * @param project the project
     */
    public void setProject(Project project) {
        if (project == null) {
            LOGGER.warning("Cannot set null project");
            return;
        }
        
        viewModel.setProject(project);
        
        // If bridge is already initialized, refresh the data
        if (bridgeInitialized) {
            viewModel.getRefreshCommand().execute();
        }
    }
    
    /**
     * Handles the refresh button click.
     * Protected for testability.
     */
    @FXML
    protected void handleRefresh() {
        viewModel.getRefreshCommand().execute();
    }
    
    /**
     * Handles the zoom in button click.
     * Protected for testability.
     */
    @FXML
    protected void handleZoomIn() {
        viewModel.getZoomInCommand().execute();
    }
    
    /**
     * Handles the zoom out button click.
     * Protected for testability.
     */
    @FXML
    protected void handleZoomOut() {
        viewModel.getZoomOutCommand().execute();
    }
    
    /**
     * Handles the export button click.
     * Protected for testability.
     */
    @FXML
    protected void handleExport() {
        viewModel.getExportCommand().execute();
    }
    
    /**
     * Shows an error alert dialog.
     * Protected for testability.
     * 
     * @param title the title
     * @param message the message
     */
    protected void showErrorAlert(String title, String message) {
        try {
            dialogService.showErrorAlert(title, message);
        } catch (Exception e) {
            // This can happen in tests when not on FX thread
            LOGGER.log(Level.INFO, "Alert would show: {0} - {1}", new Object[] { title, message });
        }
    }
    
    /**
     * Gets the ViewModel.
     * 
     * @return the ViewModel
     */
    public GanttChartViewModel getViewModel() {
        return viewModel;
    }
    
    /**
     * Sets the ViewModel (for testing).
     * 
     * @param viewModel the ViewModel
     */
    public void setViewModel(GanttChartViewModel viewModel) {
        this.viewModel = viewModel;
        setupBindings();
    }
    
    /**
     * Sets the bridge service (for testing).
     * 
     * @param bridgeService the bridge service
     */
    public void setBridgeService(WebViewBridgeService bridgeService) {
        this.bridgeService = bridgeService;
    }
    
    /**
     * Sets the dialog service (for testing).
     * 
     * @param dialogService the dialog service
     */
    public void setDialogService(DialogService dialogService) {
        this.dialogService = dialogService;
    }
    
    /**
     * Get the WebView for testing.
     * 
     * @return the WebView
     */
    protected WebView getWebView() {
        return webView;
    }
}