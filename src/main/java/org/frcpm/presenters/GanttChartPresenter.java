package org.frcpm.presenters;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.frcpm.binding.ViewModelBinding;
import org.frcpm.models.Project;
import org.frcpm.services.DialogService;
import org.frcpm.services.GanttDataService;
import org.frcpm.services.WebViewBridgeService;
import org.frcpm.viewmodels.GanttChartViewModel;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Presenter for the Gantt chart view using AfterburnerFX pattern.
 * Handles user interaction and delegates business logic to the ViewModel.
 */
public class GanttChartPresenter implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(GanttChartPresenter.class.getName());
    
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
    
    // Injected services
    @Inject
    private GanttDataService ganttDataService;
    
    @Inject
    private WebViewBridgeService bridgeService;
    
    @Inject
    private DialogService dialogService;
    
    // ViewModel and resources
    private GanttChartViewModel viewModel;
    private ResourceBundle resources;
    
    // Bridge interface initialization flag
    private boolean bridgeInitialized = false;
    
    /**
     * Initializes the presenter.
     * This method is automatically called after the FXML has been loaded.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initializing GanttChartPresenter");
        
        this.resources = resources;
        
        // Create view model with injected services
        viewModel = new GanttChartViewModel(ganttDataService);
        
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
     * Initializes the WebView and sets up the bridge between Java and JavaScript.
     */
    private void initializeWebView() {
        try {
            WebEngine engine = webView.getEngine();
            
            // Enable JavaScript
            engine.setJavaScriptEnabled(true);
            
            // Load the HTML file from resources
            String htmlUrl = getClass().getResource("/web/gantt-chart.html").toExternalForm();
            engine.load(htmlUrl);
            
            // Set up the bridge between Java and JavaScript
            engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                    try {
                        // Initialize bridge service
                        bridgeService.initialize(engine, viewModel);
                        bridgeInitialized = true;
                        
                        // Set status message
                        viewModel.setStatusMessage(resources.getString("gantt.status.loaded"));
                        
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
                                    LOGGER.log(Level.WARNING, "Interrupted while waiting for bridge initialization", e);
                                }
                            });
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Error initializing WebView bridge", e);
                        viewModel.setErrorMessage("Failed to initialize chart: " + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up WebView", e);
            viewModel.setErrorMessage("Failed to set up chart view: " + e.getMessage());
        }
    }
    
    /**
     * Sets up the combo boxes with appropriate values.
     */
    private void setupComboBoxes() {
        try {
            // Set up view mode options
            viewModeComboBox.getItems().setAll(GanttChartViewModel.ViewMode.values());
            viewModeComboBox.setValue(GanttChartViewModel.ViewMode.WEEK);
            
            // Set up filter options
            filterComboBox.getItems().setAll(GanttChartViewModel.FilterOption.values());
            filterComboBox.setValue(GanttChartViewModel.FilterOption.ALL_TASKS);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up combo boxes", e);
            viewModel.setErrorMessage("Failed to set up filter options: " + e.getMessage());
        }
    }
    
    /**
     * Sets up data bindings between UI and ViewModel.
     */
    private void setupBindings() {
        try {
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
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up bindings", e);
            viewModel.setErrorMessage("Failed to set up UI bindings: " + e.getMessage());
        }
    }
    
    /**
     * Sets up error message listener.
     */
    private void setupErrorListener() {
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
     * This method is called when the refresh button is clicked.
     */
    @FXML
    public void handleRefresh() {
        viewModel.getRefreshCommand().execute();
    }
    
    /**
     * Handles the zoom in button click.
     * This method is called when the zoom in button is clicked.
     */
    @FXML
    public void handleZoomIn() {
        viewModel.getZoomInCommand().execute();
    }
    
    /**
     * Handles the zoom out button click.
     * This method is called when the zoom out button is clicked.
     */
    @FXML
    public void handleZoomOut() {
        viewModel.getZoomOutCommand().execute();
    }
    
    /**
     * Handles the today button click.
     * This method is called when the today button is clicked.
     */
    @FXML
    public void handleToday() {
        viewModel.getTodayCommand().execute();
    }
    
    /**
     * Handles the export button click.
     * This method is called when the export button is clicked.
     */
    @FXML
    public void handleExport() {
        viewModel.getExportCommand().execute();
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
            // This can happen in tests when not on FX thread
            LOGGER.log(Level.INFO, "Alert would show: {0} - {1}", new Object[] { title, message });
        }
    }
    
    /**
     * Gets the ViewModel.
     * Useful for testing and programmatic access.
     * 
     * @return the ViewModel
     */
    public GanttChartViewModel getViewModel() {
        return viewModel;
    }
    
    /**
     * Cleans up resources.
     * This method should be called when the presenter is no longer needed.
     */
    public void cleanup() {
        if (viewModel != null) {
            viewModel.cleanupResources();
        }
        
        // Clean up WebView
        if (webView != null) {
            WebEngine engine = webView.getEngine();
            if (engine != null) {
                engine.load(null);
            }
            webView = null;
        }
        
        bridgeInitialized = false;
    }
    
    /**
     * For testing only - avoids initializing WebView components
     */
    void initializeForTesting() {
        // Skip WebView initialization in tests
        if (this.webView == null) {
            // Already skipping initialization
            return;
        }
        
        // Setup bindings and error listener only
        viewModel = new GanttChartViewModel(ganttDataService);
        setupBindings();
        setupErrorListener();
    }
    
    /**
     * For testing only - sets the bridge initialization flag
     */
    void setBridgeInitializedForTesting(boolean value) {
        this.bridgeInitialized = value;
    }
    
    /**
     * For testing - sets the ViewModel
     * 
     * @param viewModel the ViewModel to set
     */
    void setViewModel(GanttChartViewModel viewModel) {
        this.viewModel = viewModel;
        setupBindings();
    }
}