// src/main/java/org/frcpm/presenters/GanttChartPresenter.java (updated)
package org.frcpm.presenters;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.frcpm.binding.ViewModelBinding;
import org.frcpm.models.Project;
import org.frcpm.services.DialogService;
import org.frcpm.services.GanttDataService;
import org.frcpm.services.VisualizationService;
import org.frcpm.services.impl.VisualizationServiceImpl;
import org.frcpm.viewmodels.GanttChartViewModel;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Presenter for the Gantt chart view using AfterburnerFX pattern.
 * Handles user interaction and delegates business logic to the ViewModel.
 * Updated to use Chart-FX instead of WebView for visualization.
 */
public class GanttChartPresenter implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(GanttChartPresenter.class.getName());
    
    // FXML UI components
    @FXML private BorderPane chartContainer; // Replaces the WebView
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
    private DialogService dialogService;
    
    // ViewModel and resources
    private GanttChartViewModel viewModel;
    private ResourceBundle resources;
    
    // Visualization service for chart creation
    private VisualizationService visualizationService;
    
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
        
        // Create visualization service
        visualizationService = new VisualizationServiceImpl();
        
        // Always include comprehensive null checks for UI components
        if (chartContainer == null || refreshButton == null || viewModeComboBox == null || 
            filterComboBox == null || zoomInButton == null || zoomOutButton == null || 
            exportButton == null || todayButton == null || milestonesToggle == null ||
            dependenciesToggle == null || statusLabel == null) {
            
            LOGGER.warning("UI components not initialized - likely in test environment");
            return;
        }
        
        // Set up comboboxes
        setupComboBoxes();
        
        // Set up bindings
        setupBindings();
        
        // Set up error message listener
        setupErrorListener();
        
        // Set status message
        viewModel.setStatusMessage(resources.getString("gantt.status.ready"));
        
        // Initialize view model listeners
        setupViewModelListeners();
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
     * Sets up listeners for the view model properties.
     */
    private void setupViewModelListeners() {
        // Listen for chart data changes
        viewModel.chartDataProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateChartView();
            }
        });
        
        // Listen for date range changes
        viewModel.startDateProperty().addListener((obs, oldVal, newVal) -> {
            if (viewModel.getChartData() != null) {
                updateChartView();
            }
        });
        
        viewModel.endDateProperty().addListener((obs, oldVal, newVal) -> {
            if (viewModel.getChartData() != null) {
                updateChartView();
            }
        });
        
        // Listen for view mode changes
        viewModel.viewModeProperty().addListener((obs, oldVal, newVal) -> {
            if (viewModel.getChartData() != null) {
                updateChartView();
            }
        });
        
        // Listen for visibility changes
        viewModel.showMilestonesProperty().addListener((obs, oldVal, newVal) -> {
            if (viewModel.getChartData() != null) {
                updateChartView();
            }
        });
        
        viewModel.showDependenciesProperty().addListener((obs, oldVal, newVal) -> {
            if (viewModel.getChartData() != null) {
                updateChartView();
            }
        });
    }
    
    /**
     * Updates the chart view with the current data from the view model.
     */
    private void updateChartView() {
        try {
            // Clear the existing chart
            chartContainer.setCenter(null);
            
            // Get the current data and settings from the view model
            Project project = viewModel.getProject();
            if (project == null) {
                return;
            }
            
            // Create a new chart using the visualization service
            Pane chartPane = visualizationService.createGanttChartPane(
                project.getId(),
                viewModel.getStartDate(),
                viewModel.getEndDate(),
                viewModel.getViewMode().toString(),
                viewModel.isShowMilestones(),
                viewModel.isShowDependencies()
            );
            
            // Add the chart to the container
            chartContainer.setCenter(chartPane);
            
            // Update status message
            viewModel.setStatusMessage(resources.getString("gantt.status.loaded"));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating chart view", e);
            viewModel.setErrorMessage("Failed to update chart view: " + e.getMessage());
        }
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
        
        // Refresh the data
        viewModel.getRefreshCommand().execute();
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
        
        // Clear the chart container
        if (chartContainer != null) {
            chartContainer.setCenter(null);
        }
    }
    
    /**
     * For testing only - avoids initializing UI components
     */
    void initializeForTesting() {
        // Setup bindings and error listener only
        viewModel = new GanttChartViewModel(ganttDataService);
        visualizationService = new VisualizationServiceImpl();
        setupBindings();
        setupErrorListener();
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