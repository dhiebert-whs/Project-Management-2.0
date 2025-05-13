// src/main/java/org/frcpm/mvvm/views/MetricsMvvmView.java
package org.frcpm.mvvm.views;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.frcpm.models.Project;
import org.frcpm.models.Subsystem;
import org.frcpm.mvvm.CommandAdapter;
import org.frcpm.mvvm.viewmodels.MetricsMvvmViewModel;
import org.frcpm.mvvm.viewmodels.MetricsMvvmViewModel.MetricType;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * View for the metrics visualization using MVVMFx.
 */
public class MetricsMvvmView implements FxmlView<MetricsMvvmViewModel>, Initializable {
    
    private static final Logger LOGGER = Logger.getLogger(MetricsMvvmView.class.getName());
    
    // FXML UI components - Charts
    @FXML private BarChart<String, Number> subsystemProgressChart;
    @FXML private PieChart taskDistributionChart;
    @FXML private LineChart<Number, Number> velocityChart;
    @FXML private StackedBarChart<String, Number> memberContributionChart;
    
    // FXML UI components - Controls
    @FXML private ComboBox<MetricType> metricTypeComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<Subsystem> subsystemFilterComboBox;
    @FXML private Label projectNameLabel;
    @FXML private Label noDataLabel;
    @FXML private VBox chartsContainer;
    @FXML private Button generateReportButton;
    @FXML private Button exportDataButton;
    @FXML private Button refreshButton;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label errorLabel;
    
    @InjectViewModel
    private MetricsMvvmViewModel viewModel;
    
    private ResourceBundle resources;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initializing MetricsMvvmView with resource bundle");
        this.resources = resources;
        
        // Setup controls
        setupControls();
        
        // Setup bindings
        setupBindings();
        
        // Setup error handling
        setupErrorHandling();
        
        // Hide charts initially until data is loaded
        if (chartsContainer != null && noDataLabel != null) {
            chartsContainer.setVisible(false);
            noDataLabel.setVisible(true);
        }
    }

    /**
     * Sets up the control components.
     */
    private void setupControls() {
        // Check for null UI components for testability
        if (metricTypeComboBox == null || startDatePicker == null || endDatePicker == null || 
            subsystemFilterComboBox == null) {
            LOGGER.warning("Control components not initialized - likely in test environment");
            return;
        }

        // Setup metric type combo box
        metricTypeComboBox.setItems(viewModel.getMetricTypes());
        metricTypeComboBox.setValue(viewModel.getSelectedMetricType());
        
        // Setup subsystem filter combo box
        subsystemFilterComboBox.setItems(viewModel.getAvailableSubsystems());
        
        // Setup default date range
        startDatePicker.setValue(viewModel.getStartDate());
        endDatePicker.setValue(viewModel.getEndDate());
    }
    
    /**
     * Sets up bindings between UI controls and ViewModel properties.
     */
    private void setupBindings() {
        // Check for null UI components for testability
        if (metricTypeComboBox == null || startDatePicker == null || endDatePicker == null || 
            subsystemFilterComboBox == null || generateReportButton == null || 
            exportDataButton == null || refreshButton == null || projectNameLabel == null) {
            LOGGER.warning("UI components not initialized - likely in test environment");
            return;
        }

        try {
            // Bind project name label
            projectNameLabel.textProperty().bind(viewModel.projectNameProperty());
            
            // Bind date pickers
            startDatePicker.valueProperty().bindBidirectional(viewModel.startDateProperty());
            endDatePicker.valueProperty().bindBidirectional(viewModel.endDateProperty());
            
            // Bind metric type combo box
            metricTypeComboBox.valueProperty().bindBidirectional(viewModel.selectedMetricTypeProperty());
            
            // Listen for metric type changes to update chart visibility
            metricTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                updateChartsVisibility(newVal);
            });
            
            // Bind subsystem filter combo box
            subsystemFilterComboBox.valueProperty().bindBidirectional(viewModel.selectedSubsystemProperty());
            
            // Bind loading indicator
            loadingIndicator.visibleProperty().bind(viewModel.loadingProperty());
            
            // Bind buttons to commands
            CommandAdapter.bindCommandButton(generateReportButton, viewModel.getGenerateReportCommand());
            CommandAdapter.bindCommandButton(exportDataButton, viewModel.getExportDataCommand());
            CommandAdapter.bindCommandButton(refreshButton, viewModel.getRefreshCommand());
            
            // Bind has data property to show/hide charts
            viewModel.hasDataProperty().addListener((obs, oldVal, newVal) -> {
                if (chartsContainer != null && noDataLabel != null) {
                    chartsContainer.setVisible(newVal);
                    noDataLabel.setVisible(!newVal);
                }
            });
            
            // Setup charts
            setupCharts();
            
            // Initialize chart visibility based on selected metric type
            updateChartsVisibility(viewModel.getSelectedMetricType());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up bindings", e);
            showErrorAlert(resources.getString("error.title"), 
                          resources.getString("error.setup.bindings") + " " + e.getMessage());
        }
    }
    
/**
     * Sets up the charts.
     */
    private void setupCharts() {
        if (subsystemProgressChart == null || taskDistributionChart == null || 
            velocityChart == null || memberContributionChart == null) {
            LOGGER.warning("Chart components not initialized - likely in test environment");
            return;
        }

        try {
            // Set up subsystem progress chart
            subsystemProgressChart.setTitle(resources.getString("metrics.chart.subsystemProgress"));
            subsystemProgressChart.setAnimated(false);
            subsystemProgressChart.getData().clear();
            subsystemProgressChart.getData().add(viewModel.getSubsystemProgressData());
            
            // Set up task distribution chart
            taskDistributionChart.setTitle(resources.getString("metrics.chart.taskDistribution"));
            taskDistributionChart.setData(viewModel.getTaskDistributionData());
            taskDistributionChart.setLabelsVisible(true);
            taskDistributionChart.setAnimated(false);
            
            // Set up velocity chart
            velocityChart.setTitle(resources.getString("metrics.chart.teamVelocity"));
            velocityChart.setAnimated(false);
            velocityChart.getData().clear();
            velocityChart.getData().add(viewModel.getVelocityData());
            
            // Set up member contribution chart
            memberContributionChart.setTitle(resources.getString("metrics.chart.memberContributions"));
            memberContributionChart.setAnimated(false);
            memberContributionChart.getData().clear();
            memberContributionChart.getData().addAll(viewModel.getMemberContributionData());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up charts", e);
            showErrorAlert(resources.getString("error.title"), 
                          "Failed to set up charts: " + e.getMessage());
        }
    }
    
    /**
     * Updates the visibility of charts based on selected metric type.
     * 
     * @param metricType the selected metric type
     */
    private void updateChartsVisibility(MetricType metricType) {
        if (subsystemProgressChart == null || taskDistributionChart == null || 
            velocityChart == null || memberContributionChart == null) {
            return;
        }
        
        subsystemProgressChart.setVisible(metricType == MetricType.SUBSYSTEM_PROGRESS);
        taskDistributionChart.setVisible(metricType == MetricType.TASK_COMPLETION);
        velocityChart.setVisible(metricType == MetricType.TEAM_VELOCITY);
        memberContributionChart.setVisible(metricType == MetricType.MEMBER_CONTRIBUTIONS);
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
     * Sets the current project for the metrics view.
     * 
     * @param project the project
     */
    public void setProject(Project project) {
        try {
            viewModel.setProject(project);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting project for metrics view", e);
            showErrorAlert(resources.getString("error.title"), 
                          "Failed to set project: " + e.getMessage());
        }
    }
    
    /**
     * Refreshes the metrics data.
     */
    @FXML
    private void refreshData() {
        viewModel.getRefreshCommand().execute();
    }
    
    /**
     * Handles exporting data to CSV.
     */
    @FXML
    private void handleExportData() {
        viewModel.exportDataToCSV();
    }
    
    /**
     * Handles generating a PDF report.
     */
    @FXML
    private void handleGenerateReport() {
        viewModel.generatePDFReport();
    }
    
    /**
     * Updates the metric type based on combo box selection.
     */
    @FXML
    private void onMetricTypeChanged() {
        refreshData();
    }
    
    /**
     * Updates the subsystem filter based on combo box selection.
     */
    @FXML
    private void onSubsystemFilterChanged() {
        refreshData();
    }

    /**
     * Shows an error alert dialog.
     * 
     * @param title the title
     * @param message the message
     */
    private void showErrorAlert(String title, String message) {
        try {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        } catch (Exception e) {
            // This can happen in tests when not on FX thread
            LOGGER.log(Level.INFO, "Alert would show: {0} - {1}", new Object[] { title, message });
        }
    }

    /**
     * Shows an information alert dialog.
     * 
     * @param title the title
     * @param message the message
     */
    private void showInfoAlert(String title, String message) {
        try {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        } catch (Exception e) {
            // This can happen in tests when not on FX thread
            LOGGER.log(Level.INFO, "Alert would show: {0} - {1}", new Object[] { title, message });
        }
    }
    
    /**
     * Gets the ViewModel.
     * This method is useful for testing.
     * 
     * @return the ViewModel
     */
    public MetricsMvvmViewModel getViewModel() {
        return viewModel;
    }
}