package org.frcpm.presenters;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.frcpm.binding.ViewModelBinding;
import org.frcpm.models.Project;
import org.frcpm.models.Subsystem;
import org.frcpm.models.TeamMember;
import org.frcpm.services.DialogService;
import org.frcpm.services.ProjectService;
import org.frcpm.services.SubsystemService;
import org.frcpm.services.TeamMemberService;
import org.frcpm.viewmodels.MetricsViewModel;

import javax.inject.Inject;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Presenter for the metrics view using AfterburnerFX pattern.
 * Shows project metrics and statistics.
 */
public class MetricsPresenter implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(MetricsPresenter.class.getName());

    // FXML UI components - Charts
    @FXML
    private BarChart<String, Number> subsystemProgressChart;

    @FXML
    private PieChart taskDistributionChart;

    @FXML
    private LineChart<Number, Number> velocityChart;

    @FXML
    private StackedBarChart<String, Number> memberContributionChart;

    // FXML UI components - Controls
    @FXML
    private ComboBox<String> metricTypeComboBox;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private ComboBox<Subsystem> subsystemFilterComboBox;

    @FXML
    private Button generateReportButton;

    @FXML
    private Button exportDataButton;

    @FXML
    private Button refreshButton;

    @FXML
    private VBox chartsContainer;

    @FXML
    private Label noDataLabel;

    // Injected services
    @Inject
    private ProjectService projectService;
    
    @Inject
    private SubsystemService subsystemService;
    
    @Inject
    private TeamMemberService teamMemberService;
    
    @Inject
    private DialogService dialogService;

    // ViewModel and resources
    private MetricsViewModel viewModel;
    private ResourceBundle resources;
    private Project currentProject;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initializing MetricsPresenter with resource bundle");
        
        this.resources = resources;
        
        // Create view model with injected services
        viewModel = new MetricsViewModel(projectService, subsystemService, teamMemberService);

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
        metricTypeComboBox.getItems().clear();
        metricTypeComboBox.getItems().addAll(
            "Task Completion",
            "Team Velocity",
            "Member Contributions",
            "Subsystem Progress"
        );
        metricTypeComboBox.setValue("Task Completion");
        
        // Setup default date range
        LocalDate now = LocalDate.now();
        startDatePicker.setValue(now.minusMonths(1));
        endDatePicker.setValue(now.plusMonths(1));
    }
    
    /**
     * Sets up bindings between UI controls and ViewModel properties.
     */
    private void setupBindings() {
        // Check for null UI components for testability
        if (metricTypeComboBox == null || startDatePicker == null || endDatePicker == null || 
            subsystemFilterComboBox == null || generateReportButton == null || 
            exportDataButton == null || refreshButton == null || 
            subsystemProgressChart == null || taskDistributionChart == null || 
            velocityChart == null || memberContributionChart == null) {
            LOGGER.warning("UI components not initialized - likely in test environment");
            return;
        }

        try {
            // Bind date pickers
            ViewModelBinding.bindDatePicker(startDatePicker, viewModel.startDateProperty());
            ViewModelBinding.bindDatePicker(endDatePicker, viewModel.endDateProperty());
            
            // Bind subsystem filter combo box
            subsystemFilterComboBox.setItems(viewModel.getAvailableSubsystems());
            subsystemFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                viewModel.setSelectedSubsystem(newVal);
                refreshData();
            });
            
            // Bind metric type combo box
            metricTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                viewModel.setSelectedMetricType(newVal);
                updateChartsVisibility();
                refreshData();
            });
            
            // Bind buttons
            ViewModelBinding.bindCommandButton(generateReportButton, viewModel.getGenerateReportCommand());
            ViewModelBinding.bindCommandButton(exportDataButton, viewModel.getExportDataCommand());
            ViewModelBinding.bindCommandButton(refreshButton, viewModel.getRefreshCommand());
            
            // Set manual button actions
            refreshButton.setOnAction(event -> refreshData());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up bindings", e);
            showErrorAlert("Setup Error", "Failed to initialize bindings: " + e.getMessage());
        }
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
                showErrorAlert("Error", newVal);
                viewModel.clearErrorMessage();
            }
        });
    }
    
    /**
     * Updates the visibility of charts based on selected metric type.
     */
    private void updateChartsVisibility() {
        if (subsystemProgressChart == null || taskDistributionChart == null || 
            velocityChart == null || memberContributionChart == null || 
            metricTypeComboBox == null) {
            return;
        }
        
        String metricType = metricTypeComboBox.getValue();
        subsystemProgressChart.setVisible("Subsystem Progress".equals(metricType));
        taskDistributionChart.setVisible("Task Completion".equals(metricType));
        velocityChart.setVisible("Team Velocity".equals(metricType));
        memberContributionChart.setVisible("Member Contributions".equals(metricType));
    }
    
    /**
     * Updates chart data based on the current project and selected options.
     */
    private void updateCharts() {
        if (viewModel.hasData()) {
            if (chartsContainer != null && noDataLabel != null) {
                chartsContainer.setVisible(true);
                noDataLabel.setVisible(false);
            }
            
            // Update charts based on the selected metric type
            String metricType = metricTypeComboBox.getValue();
            
            if ("Subsystem Progress".equals(metricType)) {
                updateSubsystemProgressChart();
            } else if ("Task Completion".equals(metricType)) {
                updateTaskDistributionChart();
            } else if ("Team Velocity".equals(metricType)) {
                updateVelocityChart();
            } else if ("Member Contributions".equals(metricType)) {
                updateMemberContributionChart();
            }
        } else {
            if (chartsContainer != null && noDataLabel != null) {
                chartsContainer.setVisible(false);
                noDataLabel.setVisible(true);
            }
        }
    }
    
    /**
     * Updates the subsystem progress chart.
     */
    private void updateSubsystemProgressChart() {
        if (subsystemProgressChart == null) {
            return;
        }
        
        // Clear existing data
        subsystemProgressChart.getData().clear();
        
        // Get data from view model
        XYChart.Series<String, Number> progressSeries = viewModel.getSubsystemProgressData();
        subsystemProgressChart.getData().add(progressSeries);
        
        // Apply styling
        progressSeries.getNode().setStyle("-fx-bar-fill: #4285f4;");
    }
    
    /**
     * Updates the task distribution chart.
     */
    private void updateTaskDistributionChart() {
        if (taskDistributionChart == null) {
            return;
        }
        
        // Get data from view model
        taskDistributionChart.setData(viewModel.getTaskDistributionData());
    }
    
    /**
     * Updates the team velocity chart.
     */
    private void updateVelocityChart() {
        if (velocityChart == null) {
            return;
        }
        
        // Clear existing data
        velocityChart.getData().clear();
        
        // Get data from view model
        XYChart.Series<Number, Number> velocitySeries = viewModel.getVelocityData();
        velocityChart.getData().add(velocitySeries);
        
        // Apply styling
        velocitySeries.getNode().setStyle("-fx-stroke: #34a853; -fx-stroke-width: 2px;");
    }
    
    /**
     * Updates the member contribution chart.
     */
    private void updateMemberContributionChart() {
        if (memberContributionChart == null) {
            return;
        }
        
        // Clear existing data
        memberContributionChart.getData().clear();
        
        // Get data from view model
        memberContributionChart.getData().addAll(viewModel.getMemberContributionData());
    }
    
    /**
     * Sets the current project and loads metrics data.
     * 
     * @param project the current project
     */
    public void setProject(Project project) {
        if (project == null) {
            LOGGER.warning("Cannot set null project");
            return;
        }
        
        this.currentProject = project;
        viewModel.setProject(project);
        
        // Load subsystems for filter
        if (subsystemFilterComboBox != null) {
            subsystemFilterComboBox.getItems().clear();
            subsystemFilterComboBox.getItems().add(null); // For "All Subsystems"
            viewModel.loadSubsystems();
        }
        
        refreshData();
    }
    
    /**
     * Refreshes metrics data based on current selections.
     */
    @FXML
    private void refreshData() {
        if (viewModel.loadMetricsData()) {
            updateChartsVisibility();
            updateCharts();
        }
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
     * Shows an information alert dialog.
     * 
     * @param title the title
     * @param message the message
     */
    private void showInfoAlert(String title, String message) {
        try {
            dialogService.showInfoAlert(title, message);
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
    public MetricsViewModel getViewModel() {
        return viewModel;
    }
}