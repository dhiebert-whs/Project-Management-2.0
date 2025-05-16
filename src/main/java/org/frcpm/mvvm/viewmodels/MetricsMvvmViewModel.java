// src/main/java/org/frcpm/mvvm/viewmodels/MetricsMvvmViewModel.java
package org.frcpm.mvvm.viewmodels;

import de.saxsys.mvvmfx.utils.commands.Command;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;

import java.lang.module.ModuleDescriptor.Exports;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.frcpm.async.TaskExecutor;
import org.frcpm.models.*;
import org.frcpm.mvvm.BaseMvvmViewModel;
import org.frcpm.mvvm.async.MvvmAsyncHelper;
import org.frcpm.services.ProjectService;
import org.frcpm.services.SubsystemService;
import org.frcpm.services.TeamMemberService;
import org.frcpm.services.impl.ProjectServiceAsyncImpl;
import org.frcpm.services.impl.SubsystemServiceAsyncImpl;
import org.frcpm.services.impl.TeamMemberServiceAsyncImpl;

/**
 * ViewModel for metrics visualization in the FRC Project Management System using MVVMFx.
 */
public class MetricsMvvmViewModel extends BaseMvvmViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(MetricsMvvmViewModel.class.getName());
    
    /**
     * Enum for metric types.
     */
    public enum MetricType {
        TASK_COMPLETION("Task Completion"),
        TEAM_VELOCITY("Team Velocity"),
        MEMBER_CONTRIBUTIONS("Member Contributions"),
        SUBSYSTEM_PROGRESS("Subsystem Progress");
        
        private final String displayName;
        
        MetricType(String displayName) {
            this.displayName = displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    // Services
    private final ProjectService projectService;
    private final ProjectServiceAsyncImpl projectServiceAsync;
    private final SubsystemService subsystemService;
    private final SubsystemServiceAsyncImpl subsystemServiceAsync;
    private final TeamMemberService teamMemberService;
    private final TeamMemberServiceAsyncImpl teamMemberServiceAsync;
    
    // Observable properties
    private final ObjectProperty<Project> project = new SimpleObjectProperty<>();
    private final StringProperty projectName = new SimpleStringProperty("");
    private final ObjectProperty<MetricType> selectedMetricType = new SimpleObjectProperty<>(MetricType.TASK_COMPLETION);
    private final ObjectProperty<LocalDate> startDate = new SimpleObjectProperty<>(LocalDate.now().minusMonths(1));
    private final ObjectProperty<LocalDate> endDate = new SimpleObjectProperty<>(LocalDate.now().plusMonths(1));
    private final ObjectProperty<Subsystem> selectedSubsystem = new SimpleObjectProperty<>();
    private final BooleanProperty hasData = new SimpleBooleanProperty(false);
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    
    // Collections
    private final ObservableList<Subsystem> availableSubsystems = FXCollections.observableArrayList();
    private final ObservableList<MetricType> metricTypes = FXCollections.observableArrayList(MetricType.values());
    
    // Chart data
    private final ObservableList<PieChart.Data> taskDistributionData = FXCollections.observableArrayList();
    private final XYChart.Series<String, Number> subsystemProgressData = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> velocityData = new XYChart.Series<>();
    private final ObservableList<XYChart.Series<String, Number>> memberContributionData = FXCollections.observableArrayList();
    
    // Commands
    private Command generateReportCommand;
    private Command exportDataCommand;
    private Command refreshCommand;
    
    /**
     * Creates a new MetricsMvvmViewModel with the specified services.
     *
     * @param projectService    the project service
     * @param subsystemService  the subsystem service
     * @param teamMemberService the team member service
     */
    
    public MetricsMvvmViewModel(ProjectService projectService, SubsystemService subsystemService, TeamMemberService teamMemberService) {
        this.projectService = projectService;
        this.projectServiceAsync = (ProjectServiceAsyncImpl) projectService;
        this.subsystemService = subsystemService;
        this.subsystemServiceAsync = (SubsystemServiceAsyncImpl) subsystemService;
        this.teamMemberService = teamMemberService;
        this.teamMemberServiceAsync = (TeamMemberServiceAsyncImpl) teamMemberService;
        
        // Initialize commands
        initializeCommands();
        
        // Set up property change listeners
        setupPropertyListeners();
        
        // Set default series labels
        subsystemProgressData.setName("Progress");
        velocityData.setName("Team Velocity");
    }
    
    /**
     * Initializes the commands for this view model.
     */
    private void initializeCommands() {
        // Generate report command
        generateReportCommand = createValidOnlyCommand(
            this::generatePDFReport,
            this::hasData
        );
        
        // Export data command
        exportDataCommand = createValidOnlyCommand(
            this::exportDataToCSV,
            this::hasData
        );
        
        // Refresh command - using MvvmAsyncHelper to create an async command
        refreshCommand = MvvmAsyncHelper.createAsyncCommand(
            // Return a CompletableFuture
            () -> TaskExecutor.executeAsync(
                "RefreshMetrics",
                () -> {
                    boolean result = loadMetricsData();
                    return result;
                },
                null,
                null
            ),
            // Success handler
            result -> {
                // Update has data flag
                hasData.set(Boolean.TRUE.equals(result));
            },
            // Error handler
            error -> {
                LOGGER.log(Level.SEVERE, "Error refreshing metrics", error);
                setErrorMessage("Failed to refresh metrics: " + error.getMessage());
                loading.set(false);
            },
            // Loading property
            loading
        );
    }
    
    /**
     * Sets up listeners for property changes.
     */
    private void setupPropertyListeners() {
        // Create validation handler
        Runnable dataChangeHandler = createDirtyFlagHandler(null);
        
        // Listen for project changes to refresh dashboard
        project.addListener((obs, oldProject, newProject) -> {
            if (newProject != null) {
                projectName.set(newProject.getName());
                loadSubsystems();
                refreshCommand.execute();
                dataChangeHandler.run();
            } else {
                projectName.set("");
                availableSubsystems.clear();
            }
        });
        
        // Listen for date and filter changes
        startDate.addListener((obs, oldVal, newVal) -> dataChangeHandler.run());
        endDate.addListener((obs, oldVal, newVal) -> dataChangeHandler.run());
        selectedMetricType.addListener((obs, oldVal, newVal) -> dataChangeHandler.run());
        selectedSubsystem.addListener((obs, oldVal, newVal) -> dataChangeHandler.run());
        
        // Track the listener
        trackPropertyListener(dataChangeHandler);
    }
    
    /**
     * Loads the available subsystems for the selected project.
     */
    public void loadSubsystems() {
        if (project.get() == null || subsystemServiceAsync == null) {
            availableSubsystems.clear();
            return;
        }
        
        loading.set(true);
        
        subsystemServiceAsync.findAllAsync(
            // Success handler
            subsystems -> {
                Platform.runLater(() -> {
                    availableSubsystems.clear();
                    if (subsystems != null) {
                        availableSubsystems.addAll(subsystems);
                    }
                    loading.set(false);
                });
            },
            // Error handler
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error loading subsystems", error);
                    setErrorMessage("Failed to load subsystems: " + error.getMessage());
                    loading.set(false);
                });
            }
        );
    }
    
    /**
     * Loads metrics data based on the current selections.
     * 
     * @return true if data was loaded successfully, false otherwise
     */
    public boolean loadMetricsData() {
        if (project.get() == null) {
            setErrorMessage("No project selected");
            hasData.set(false);
            return false;
        }
        
        loading.set(true);
        
        try {
            MetricType metricType = selectedMetricType.get();
            
            if (metricType == MetricType.TASK_COMPLETION) {
                loadTaskCompletionData();
            } else if (metricType == MetricType.TEAM_VELOCITY) {
                loadTeamVelocityData();
            } else if (metricType == MetricType.MEMBER_CONTRIBUTIONS) {
                loadMemberContributionsData();
            } else if (metricType == MetricType.SUBSYSTEM_PROGRESS) {
                loadSubsystemProgressData();
            }
            
            hasData.set(true);
            setDirty(false);
            clearErrorMessage();
            loading.set(false);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading metrics data", e);
            setErrorMessage("Failed to load metrics data: " + e.getMessage());
            hasData.set(false);
            loading.set(false);
            return false;
        }
    }
    
    /**
     * Loads task completion data for pie chart visualization.
     */
    private void loadTaskCompletionData() {
        projectServiceAsync.getProjectSummaryAsync(
            project.get().getId(),
            // Success handler
            summary -> {
                Platform.runLater(() -> {
                    // Create pie chart data
                    taskDistributionData.clear();
                    
                    int completedTasks = (Integer) summary.get("completedTasks");
                    int totalTasks = (Integer) summary.get("totalTasks");
                    int inProgressTasks = totalTasks - completedTasks;
                    
                    if (totalTasks > 0) {
                        taskDistributionData.add(new PieChart.Data("Completed", completedTasks));
                        taskDistributionData.add(new PieChart.Data("In Progress", inProgressTasks));
                    }
                    
                    LOGGER.info("Loaded task completion data");
                });
            },
            // Error handler
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error loading task completion data", error);
                    setErrorMessage("Failed to load task completion data: " + error.getMessage());
                });
            }
        );
    }
    
    /**
     * Loads team velocity data for line chart visualization.
     */
    private void loadTeamVelocityData() {
        // In a real implementation, this would calculate velocity over time
        // For this implementation, we'll generate some sample data asynchronously
        TaskExecutor.executeAsync(
            "Generate Velocity Data",
            () -> {
                // Prepare velocity data
                velocityData.getData().clear();
                
                // Generate velocity data points
                LocalDate date = startDate.get();
                LocalDate end = endDate.get();
                
                // Use synthetic data for demonstration
                Random random = new Random(42); // Fixed seed for consistency
                
                int weekCount = 0;
                while (!date.isAfter(end)) {
                    // Add data point for each week
                    double velocity = 5 + weekCount * 0.5 + random.nextDouble() * 3;
                    final int weekIndex = weekCount;
                    final double weekVelocity = velocity;
                    
                    Platform.runLater(() -> {
                        velocityData.getData().add(new XYChart.Data<>(weekIndex, weekVelocity));
                    });
                    
                    date = date.plusWeeks(1);
                    weekCount++;
                }
                
                return null;
            },
            // Success handler
            result -> {
                LOGGER.info("Generated velocity data");
            },
            // Error handler
            error -> {
                LOGGER.log(Level.WARNING, "Error generating velocity data", error);
                setErrorMessage("Failed to generate velocity data: " + error.getMessage());
            }
        );
    }
    
    /**
     * Loads member contribution data for stacked bar chart visualization.
     */
    private void loadMemberContributionsData() {
        teamMemberServiceAsync.findAllAsync(
            // Success handler
            members -> {
                Platform.runLater(() -> {
                    // Clear existing data
                    memberContributionData.clear();
                    
                    if (members == null || members.isEmpty()) {
                        return;
                    }
                    
                    // Create series for different work types
                    XYChart.Series<String, Number> completedSeries = new XYChart.Series<>();
                    completedSeries.setName("Completed Tasks");
                    
                    XYChart.Series<String, Number> inProgressSeries = new XYChart.Series<>();
                    inProgressSeries.setName("In-Progress Tasks");
                    
                    // Add data for each team member
                    for (TeamMember member : members) {
                        // In a real implementation, this would use actual task data
                        // For this demo, we'll use random data
                        Random random = new Random(member.getId().intValue()); // Use member ID as seed for consistency
                        
                        int completed = random.nextInt(10);
                        int inProgress = random.nextInt(5);
                        
                        completedSeries.getData().add(new XYChart.Data<>(member.getFullName(), completed));
                        inProgressSeries.getData().add(new XYChart.Data<>(member.getFullName(), inProgress));
                    }
                    
                    memberContributionData.add(completedSeries);
                    memberContributionData.add(inProgressSeries);
                    
                    LOGGER.info("Loaded member contributions data");
                });
            },
            // Error handler
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error loading member contributions data", error);
                    setErrorMessage("Failed to load member contributions data: " + error.getMessage());
                });
            }
        );
    }
    
    /**
     * Loads subsystem progress data for bar chart visualization.
     */
    private void loadSubsystemProgressData() {
        TaskExecutor.executeAsync(
            "Load Subsystem Progress",
            () -> {
                // Clear existing data
                Platform.runLater(() -> subsystemProgressData.getData().clear());
                
                // Get subsystems
                List<Subsystem> subsystems = new ArrayList<>();
                
                if (selectedSubsystem.get() != null) {
                    subsystems.add(selectedSubsystem.get());
                } else {
                    subsystems.addAll(availableSubsystems);
                }
                
                if (subsystems.isEmpty()) {
                    return null;
                }
                
                // Add data for each subsystem
                for (Subsystem subsystem : subsystems) {
                    // In a real implementation, this would calculate actual progress
                    // For this demo, we'll use a calculation based on status
                    double progress = 0;
                    
                    switch (subsystem.getStatus()) {
                        case NOT_STARTED:
                            progress = 0;
                            break;
                        case IN_PROGRESS:
                            progress = 50;
                            break;
                        case TESTING:
                            progress = 75;
                            break;
                        case COMPLETED:
                            progress = 100;
                            break;
                        case ISSUES:
                            progress = 30;
                            break;
                    }
                    
                    final String subsystemName = subsystem.getName();
                    final double subsystemProgress = progress;
                    
                    Platform.runLater(() -> {
                        subsystemProgressData.getData().add(new XYChart.Data<>(subsystemName, subsystemProgress));
                    });
                }
                
                return null;
            },
            // Success handler
            result -> {
                LOGGER.info("Loaded subsystem progress data");
            },
            // Error handler
            error -> {
                LOGGER.log(Level.SEVERE, "Error loading subsystem progress data", error);
                setErrorMessage("Failed to load subsystem progress data: " + error.getMessage());
            }
        );
    }
    
    /**
     * Generates a PDF report of the current metrics.
     */
    public void generatePDFReport() {
        LOGGER.info("Generate PDF report action triggered");
        
        if (project.get() == null) {
            setErrorMessage("No project selected for report generation");
            return;
        }
        
        loading.set(true);
        
        TaskExecutor.executeAsync(
            "Generate PDF Report",
            () -> {
                try {
                    // In a real implementation, we would use a PDF library like iText or Apache PDFBox
                    // For now, we'll simulate report generation with a delay
                    Thread.sleep(1000);
                    
                    // Simulate a generated file path
                    String filePath = System.getProperty("user.home") + 
                                    "/FRCMetricsReport_" + project.get().getId() + ".pdf";
                    
                    return filePath;
                } catch (Exception e) {
                    throw new RuntimeException("Error generating PDF report: " + e.getMessage(), e);
                }
            },
            // Success handler
            filePath -> {
                Platform.runLater(() -> {
                    loading.set(false);
                    clearErrorMessage();
                    
                    // Show a success message with the file path
                    showSuccessMessage("Report generated successfully and saved to: " + filePath);
                });
            },
            // Error handler
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error generating PDF report", error);
                    setErrorMessage("Failed to generate PDF report: " + error.getMessage());
                    loading.set(false);
                });
            }
        );
    }

    /**
     * Shows a success message that can be accessed by the view.
     * 
     * @param message the success message
     */
    private void showSuccessMessage(String message) {
        // This would be implemented with a success message property
        // For now, just log the message
        LOGGER.info(message);
    }
    
    /**
    * Exports the current metrics data to CSV.
    */
    public void exportDataToCSV() {
        LOGGER.info("Export data to CSV action triggered");
        
        if (project.get() == null) {
            setErrorMessage("No project selected for CSV export");
            return;
        }
        
        MetricType metricType = selectedMetricType.get();
        if (metricType == null) {
            setErrorMessage("No metric type selected for CSV export");
            return;
        }
        
        loading.set(true);
        
        TaskExecutor.executeAsync(
            "Export Data to CSV",
            () -> {
                try {
                    // Create CSV content based on the selected metric type
                    StringBuilder csvContent = new StringBuilder();
                    
                    // Add header
                    csvContent.append("Project: ").append(project.get().getName()).append("\n");
                    csvContent.append("Metric Type: ").append(metricType.toString()).append("\n");
                    csvContent.append("Date Range: ").append(startDate.get()).append(" to ").append(endDate.get()).append("\n\n");
                    
                    // Add data based on metric type
                    switch (metricType) {
                        case TASK_COMPLETION:
                            csvContent.append("Status,Count\n");
                            for (PieChart.Data data : taskDistributionData) {
                                csvContent.append(data.getName()).append(",").append(data.getPieValue()).append("\n");
                            }
                            break;
                            
                        case TEAM_VELOCITY:
                            csvContent.append("Week,Velocity\n");
                            for (XYChart.Data<Number, Number> data : velocityData.getData()) {
                                csvContent.append(data.getXValue()).append(",").append(data.getYValue()).append("\n");
                            }
                            break;
                            
                        case MEMBER_CONTRIBUTIONS:
                            csvContent.append("Member");
                            for (XYChart.Series<String, Number> series : memberContributionData) {
                                csvContent.append(",").append(series.getName());
                            }
                            csvContent.append("\n");
                            
                            // This is a simplification - in a real implementation we would need to align data properly
                            if (!memberContributionData.isEmpty() && !memberContributionData.get(0).getData().isEmpty()) {
                                for (XYChart.Data<String, Number> data : memberContributionData.get(0).getData()) {
                                    csvContent.append(data.getXValue());
                                    for (XYChart.Series<String, Number> series : memberContributionData) {
                                        for (XYChart.Data<String, Number> seriesData : series.getData()) {
                                            if (seriesData.getXValue().equals(data.getXValue())) {
                                                csvContent.append(",").append(seriesData.getYValue());
                                                break;
                                            }
                                        }
                                    }
                                    csvContent.append("\n");
                                }
                            }
                            break;
                            
                        case SUBSYSTEM_PROGRESS:
                            csvContent.append("Subsystem,Progress\n");
                            for (XYChart.Data<String, Number> data : subsystemProgressData.getData()) {
                                csvContent.append(data.getXValue()).append(",").append(data.getYValue()).append("\n");
                            }
                            break;
                    }
                    
                    // In a real implementation, we would save this to a file
                    // For now, just simulate the file save
                    String filePath = System.getProperty("user.home") + "/FRCMetrics_" + 
                                    metricType.toString().replace(" ", "") + "_" + 
                                    project.get().getId() + ".csv";
                    
                    // Simulate writing to file
                    Thread.sleep(500);
                    
                    return filePath;
                } catch (Exception e) {
                    throw new RuntimeException("Error exporting to CSV: " + e.getMessage(), e);
                }
            },
            // Success handler
            filePath -> {
                Platform.runLater(() -> {
                    loading.set(false);
                    clearErrorMessage();
                    
                    // Show a success message with the file path
                    showSuccessMessage("Data exported successfully and saved to: " + filePath);
                });
            },
            // Error handler
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error exporting to CSV", error);
                    setErrorMessage("Failed to export to CSV: " + error.getMessage());
                    loading.set(false);
                });
            }
        );
    }
    
    /**
     * Checks if data is available for visualization.
     *
     * @return true if data is available, false otherwise
     */
    public boolean hasData() {
        return hasData.get();
    }
    
    /**
     * Gets the property for data availability.
     *
     * @return the hasData property
     */
    public BooleanProperty hasDataProperty() {
        return hasData;
    }
    
    /**
     * Gets the loading property.
     *
     * @return the loading property
     */
    public BooleanProperty loadingProperty() {
        return loading;
    }
    
    /**
     * Gets whether the view model is loading data.
     *
     * @return true if loading, false otherwise
     */
    public boolean isLoading() {
        return loading.get();
    }
    
    /**
     * Gets the subsystem progress data.
     *
     * @return the subsystem progress data series
     */
    public XYChart.Series<String, Number> getSubsystemProgressData() {
        return subsystemProgressData;
    }
    
    /**
     * Gets the task distribution data.
     *
     * @return the task distribution data for pie chart
     */
    public ObservableList<PieChart.Data> getTaskDistributionData() {
        return taskDistributionData;
    }
    
    /**
     * Gets the velocity data.
     *
     * @return the velocity data series
     */
    public XYChart.Series<Number, Number> getVelocityData() {
        return velocityData;
    }
    
    /**
     * Gets the member contribution data.
     *
     * @return the member contribution data series list
     */
    public ObservableList<XYChart.Series<String, Number>> getMemberContributionData() {
        return memberContributionData;
    }
    
    /**
     * Gets the available subsystems.
     *
     * @return the available subsystems
     */
    public ObservableList<Subsystem> getAvailableSubsystems() {
        return availableSubsystems;
    }
    
    /**
     * Gets the metric types.
     *
     * @return the metric types
     */
    public ObservableList<MetricType> getMetricTypes() {
        return metricTypes;
    }
    
    /**
     * Gets the generate report command.
     *
     * @return the generate report command
     */
    public Command getGenerateReportCommand() {
        return generateReportCommand;
    }
    
    /**
     * Gets the export data command.
     *
     * @return the export data command
     */
    public Command getExportDataCommand() {
        return exportDataCommand;
    }
    
    /**
     * Gets the refresh command.
     *
     * @return the refresh command
     */
    public Command getRefreshCommand() {
        return refreshCommand;
    }
    
    /**
     * Sets the current project.
     *
     * @param project the project
     */
    public void setProject(Project project) {
        this.project.set(project);
    }
    
    /**
     * Gets the current project.
     *
     * @return the project
     */
    public Project getProject() {
        return project.get();
    }
    
    /**
     * Gets the project property.
     *
     * @return the project property
     */
    public ObjectProperty<Project> projectProperty() {
        return project;
    }
    
    /**
     * Gets the project name property.
     *
     * @return the project name property
     */
    public StringProperty projectNameProperty() {
        return projectName;
    }
    
    /**
     * Gets the project name.
     *
     * @return the project name
     */
    public String getProjectName() {
        return projectName.get();
    }
    
    /**
     * Sets the selected metric type.
     *
     * @param metricType the metric type
     */
    public void setSelectedMetricType(MetricType metricType) {
        this.selectedMetricType.set(metricType);
    }
    
    /**
     * Gets the selected metric type.
     *
     * @return the selected metric type
     */
    public MetricType getSelectedMetricType() {
        return selectedMetricType.get();
    }
    
    /**
     * Gets the selected metric type property.
     *
     * @return the selected metric type property
     */
    public ObjectProperty<MetricType> selectedMetricTypeProperty() {
        return selectedMetricType;
    }
    
    /**
     * Sets the start date.
     *
     * @param startDate the start date
     */
    public void setStartDate(LocalDate startDate) {
        this.startDate.set(startDate);
    }
    
    /**
     * Gets the start date.
     *
     * @return the start date
     */
    public LocalDate getStartDate() {
        return startDate.get();
    }
    
    /**
     * Gets the start date property.
     *
     * @return the start date property
     */
    public ObjectProperty<LocalDate> startDateProperty() {
        return startDate;
    }
    
    /**
     * Sets the end date.
     *
     * @param endDate the end date
     */
    public void setEndDate(LocalDate endDate) {
        this.endDate.set(endDate);
    }
    
    /**
     * Gets the end date.
     *
     * @return the end date
     */
    public LocalDate getEndDate() {
        return endDate.get();
    }
    
    /**
     * Gets the end date property.
     *
     * @return the end date property
     */
    public ObjectProperty<LocalDate> endDateProperty() {
        return endDate;
    }
    
    /**
     * Sets the selected subsystem.
     *
     * @param subsystem the subsystem
     */
    public void setSelectedSubsystem(Subsystem subsystem) {
        this.selectedSubsystem.set(subsystem);
    }
    
    /**
     * Gets the selected subsystem.
     *
     * @return the selected subsystem
     */
    public Subsystem getSelectedSubsystem() {
        return selectedSubsystem.get();
    }
    
    /**
     * Gets the selected subsystem property.
     *
     * @return the selected subsystem property
     */
    public ObjectProperty<Subsystem> selectedSubsystemProperty() {
        return selectedSubsystem;
    }
    
    /**
     * Cleans up resources when the ViewModel is no longer needed.
     */
    @Override
    public void dispose() {
        super.dispose();
        
        // Clear chart data
        subsystemProgressData.getData().clear();
        taskDistributionData.clear();
        velocityData.getData().clear();
        memberContributionData.clear();
        
        // Clear collections
        availableSubsystems.clear();
    }
}