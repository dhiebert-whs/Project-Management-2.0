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
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initializing GanttChartPresenter with resource bundle");
        
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
     */
    private void initializeWebView() {
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
                        }
                    });
                }
            }
        });
    }
    
    /**
     * Sets up the combo boxes.
     */
    private void setupComboBoxes() {
        // Set up view mode options
        viewModeComboBox.getItems().setAll(GanttChartViewModel.ViewMode.values());
        viewModeComboBox.setValue(GanttChartViewModel.ViewMode.WEEK);
        
        // Set up filter options
        filterComboBox.getItems().setAll(GanttChartViewModel.FilterOption.values());
        filterComboBox.setValue(GanttChartViewModel.FilterOption.ALL_TASKS);
    }
    
    /**
     * Sets up data bindings between UI and ViewModel.
     */
    private void setupBindings() {
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
     */
    @FXML
    void handleRefresh() {
        viewModel.getRefreshCommand().execute();
    }
    
    /**
     * Handles the zoom in button click.
     */
    @FXML
    void handleZoomIn() {
        viewModel.getZoomInCommand().execute();
    }
    
    /**
     * Handles the zoom out button click.
     */
    @FXML
    void handleZoomOut() {
        viewModel.getZoomOutCommand().execute();
    }
    
    /**
     * Handles the today button click.
     */
    @FXML
    void handleToday() {
        viewModel.getTodayCommand().execute();
    }
    
    /**
     * Handles the export button click.
     */
    @FXML
    void handleExport() {
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
     * 
     * @return the ViewModel
     */
    public GanttChartViewModel getViewModel() {
        return viewModel;
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
        setupBindings();
        setupErrorListener();
    }

    /**
     * For testing only - sets the bridge initialization flag
     */
    void setBridgeInitializedForTesting(boolean value) {
        this.bridgeInitialized = value;
    }
}