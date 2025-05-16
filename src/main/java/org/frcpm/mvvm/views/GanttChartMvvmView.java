// src/main/java/org/frcpm/mvvm/views/GanttChartMvvmView.java
package org.frcpm.mvvm.views;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.frcpm.models.Project;
import org.frcpm.mvvm.CommandAdapter;
import org.frcpm.mvvm.viewmodels.GanttChartMvvmViewModel;
import org.frcpm.mvvm.viewmodels.GanttChartMvvmViewModel.ViewMode;
import org.frcpm.mvvm.viewmodels.GanttChartMvvmViewModel.FilterOption;

/**
 * View for the Gantt chart visualization using MVVMFx.
 * This view displays Gantt charts using the Chart-FX library.
 */
public class GanttChartMvvmView implements FxmlView<GanttChartMvvmViewModel>, Initializable {
    
    private static final Logger LOGGER = Logger.getLogger(GanttChartMvvmView.class.getName());
    
    @FXML private BorderPane chartContainer;
    @FXML private Button refreshButton;
    @FXML private ComboBox<ViewMode> viewModeComboBox;
    @FXML private ComboBox<FilterOption> filterComboBox;
    @FXML private Button zoomInButton;
    @FXML private Button zoomOutButton;
    @FXML private Button exportButton;
    @FXML private Button todayButton;
    @FXML private ToggleButton milestonesToggle;
    @FXML private ToggleButton dependenciesToggle;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label errorLabel;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    
    @InjectViewModel
    private GanttChartMvvmViewModel viewModel;
    
    private ResourceBundle resources;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initializing GanttChartMvvmView");
        this.resources = resources;
        
        // Set up combo boxes
        setupComboBoxes();
        
        // Set up date pickers
        setupDatePickers();
        
        // Set up bindings
        setupBindings();
        
        // Set up event handlers
        setupEventHandlers();
        
        // Set up error handling
        setupErrorHandling();
    }
    
    /**
     * Sets up combo boxes with appropriate values.
     */
    private void setupComboBoxes() {
        try {
            // Check for null UI components for testability
            if (viewModeComboBox == null || filterComboBox == null) {
                LOGGER.warning("Combo boxes not initialized - likely in test environment");
                return;
            }
            
            // Set up view mode options
            viewModeComboBox.getItems().setAll(ViewMode.values());
            viewModeComboBox.setValue(ViewMode.WEEK);
            
            // Set up filter options
            filterComboBox.getItems().setAll(FilterOption.values());
            filterComboBox.setValue(FilterOption.ALL_TASKS);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up combo boxes", e);
            showErrorAlert(resources.getString("error.title"), 
                          "Failed to set up filter options: " + e.getMessage());
        }
    }
    
    /**
     * Sets up date pickers with appropriate values and bindings.
     */
    private void setupDatePickers() {
        try {
            // Check for null UI components for testability
            if (startDatePicker == null || endDatePicker == null) {
                LOGGER.warning("Date pickers not initialized - likely in test environment");
                return;
            }
            
            // Set initial values
            startDatePicker.setValue(LocalDate.now().minusDays(30));
            endDatePicker.setValue(LocalDate.now().plusDays(60));
            
            // Bind to view model
            startDatePicker.valueProperty().bindBidirectional(viewModel.startDateProperty());
            endDatePicker.valueProperty().bindBidirectional(viewModel.endDateProperty());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up date pickers", e);
            showErrorAlert(resources.getString("error.title"), 
                          "Failed to set up date pickers: " + e.getMessage());
        }
    }
    
    /**
     * Sets up bindings between UI controls and ViewModel properties.
     */
    private void setupBindings() {
        try {
            // Check for null UI components for testability
            if (chartContainer == null || refreshButton == null || viewModeComboBox == null || 
                filterComboBox == null || zoomInButton == null || zoomOutButton == null || 
                exportButton == null || todayButton == null || milestonesToggle == null || 
                dependenciesToggle == null || statusLabel == null || loadingIndicator == null) {
                LOGGER.warning("UI components not initialized - likely in test environment");
                return;
            }
            
            // Bind combo boxes
            viewModeComboBox.valueProperty().bindBidirectional(viewModel.viewModeProperty());
            filterComboBox.valueProperty().bindBidirectional(viewModel.filterOptionProperty());
            
            // Bind status label
            statusLabel.textProperty().bind(viewModel.statusMessageProperty());
            
            // Bind toggle buttons
            milestonesToggle.selectedProperty().bindBidirectional(viewModel.showMilestonesProperty());
            dependenciesToggle.selectedProperty().bindBidirectional(viewModel.showDependenciesProperty());
            
            // Bind loading indicator
            loadingIndicator.visibleProperty().bind(viewModel.loadingProperty());
            
            // Bind command buttons using CommandAdapter
            CommandAdapter.bindCommandButton(refreshButton, viewModel.getRefreshCommand());
            CommandAdapter.bindCommandButton(zoomInButton, viewModel.getZoomInCommand());
            CommandAdapter.bindCommandButton(zoomOutButton, viewModel.getZoomOutCommand());
            CommandAdapter.bindCommandButton(exportButton, viewModel.getExportCommand());
            CommandAdapter.bindCommandButton(todayButton, viewModel.getTodayCommand());
            
            // Set up toggle button handlers
            milestonesToggle.setOnAction(e -> viewModel.toggleMilestones());
            dependenciesToggle.setOnAction(e -> viewModel.toggleDependencies());
            
            // Bind chart pane to container
            viewModel.chartPaneProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    chartContainer.setCenter(newValue);
                } else {
                    chartContainer.setCenter(null);
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up bindings", e);
            showErrorAlert(resources.getString("error.title"), 
                          "Failed to set up UI bindings: " + e.getMessage());
        }
    }
    
    /**
     * Sets up event handlers for UI components.
     */
    private void setupEventHandlers() {
        // Set up any additional event handlers not covered by bindings
        try {
            // Example: Handle date picker changes
            if (startDatePicker != null) {
                startDatePicker.setOnAction(e -> {
                    // Ensure end date is after start date
                    if (startDatePicker.getValue() != null &&
                        endDatePicker.getValue() != null &&
                        endDatePicker.getValue().isBefore(startDatePicker.getValue())) {
                        endDatePicker.setValue(startDatePicker.getValue().plusMonths(2));
                    }
                });
            }
            
            if (endDatePicker != null) {
                endDatePicker.setOnAction(e -> {
                    // Ensure start date is before end date
                    if (startDatePicker.getValue() != null &&
                        endDatePicker.getValue() != null &&
                        startDatePicker.getValue().isAfter(endDatePicker.getValue())) {
                        startDatePicker.setValue(endDatePicker.getValue().minusMonths(2));
                    }
                });
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up event handlers", e);
        }
    }
    
    /**
     * Sets up error message handling.
     */
    private void setupErrorHandling() {
        if (errorLabel == null) {
            LOGGER.warning("Error label not initialized - likely in test environment");
            return;
        }
        
        // Bind error label to view model error message
        errorLabel.textProperty().bind(viewModel.errorMessageProperty());
        errorLabel.visibleProperty().bind(viewModel.errorMessageProperty().isNotEmpty());
    }
    
    /**
     * Sets the project to display.
     * 
     * @param project the project
     */
    public void setProject(Project project) {
        viewModel.setProject(project);
        
        // Update window title to include project name
        if (project != null) {
            var scene = chartContainer.getScene();
            if (scene != null && scene.getWindow() != null) {
                if (scene.getWindow() instanceof Stage) {
                    Stage stage = (Stage) scene.getWindow();
                    stage.setTitle("Gantt Chart - " + project.getName());
                }
            }
        }
    }
    
    /**
     * Shows an error alert dialog.
     * 
     * @param title the title
     * @param message the message
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Gets the view model.
     * 
     * @return the view model
     */
    public GanttChartMvvmViewModel getViewModel() {
        return viewModel;
    }
    
    /**
     * Helper method to get a reference to the chart container.
     * This is mainly for testing purposes.
     * 
     * @return the chart container
     */
    public BorderPane getChartContainer() {
        return chartContainer;
    }
}