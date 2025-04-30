// src/main/java/org/frcpm/viewmodels/MetricsViewModel.java
package org.frcpm.viewmodels;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import org.frcpm.binding.Command;
import org.frcpm.models.*;
import org.frcpm.services.ProjectService;
import org.frcpm.services.SubsystemService;
import org.frcpm.services.TeamMemberService;
import org.frcpm.services.TaskService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * ViewModel for metrics visualization in the FRC Project Management System.
 * Follows the MVVM pattern for standardization.
 */
public class MetricsViewModel extends BaseViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(MetricsViewModel.class.getName());
    
    // Services
    private final ProjectService projectService;
    private final SubsystemService subsystemService;
    private final TeamMemberService teamMemberService;
    
    // Observable properties
    private final ObjectProperty<Project> project = new SimpleObjectProperty<>();
    private final StringProperty selectedMetricType = new SimpleStringProperty("Task Completion");
    private final ObjectProperty<LocalDate> startDate = new SimpleObjectProperty<>(LocalDate.now().minusMonths(1));
    private final ObjectProperty<LocalDate> endDate = new SimpleObjectProperty<>(LocalDate.now().plusMonths(1));
    private final ObjectProperty<Subsystem> selectedSubsystem = new SimpleObjectProperty<>();
    private final BooleanProperty hasData = new SimpleBooleanProperty(false);
    
    // Collections
    private final ObservableList<Subsystem> availableSubsystems = FXCollections.observableArrayList();
    
    // Chart data
    private XYChart.Series<String, Number> subsystemProgressData = new XYChart.Series<>();
    private ObservableList<PieChart.Data> taskDistributionData = FXCollections.observableArrayList();
    private XYChart.Series<Number, Number> velocityData = new XYChart.Series<>();
    private List<XYChart.Series<String, Number>> memberContributionData = new ArrayList<>();
    
    // Commands
    private final Command generateReportCommand;
    private final Command exportDataCommand;
    private final Command refreshCommand;
    
    /**
     * Creates a new MetricsViewModel with default services.
     */
    public MetricsViewModel() {
        this(null, null, null);
    }
    
    /**
     * Creates a new MetricsViewModel with the specified services.
     *
     * @param projectService    the project service
     * @param subsystemService  the subsystem service
     * @param teamMemberService the team member service
     */
    public MetricsViewModel(ProjectService projectService, SubsystemService subsystemService, TeamMemberService teamMemberService) {
        this.projectService = projectService;
        this.subsystemService = subsystemService;
        this.teamMemberService = teamMemberService;
        
        // Initialize commands
        generateReportCommand = createValidOnlyCommand(this::generatePDFReport, this::hasData);
        exportDataCommand = createValidOnlyCommand(this::exportDataToCSV, this::hasData);
        refreshCommand = new Command(this::loadMetricsData);
        
        // Set up property change listeners
        Runnable dataChangeHandler = createDirtyFlagHandler(null);
        project.addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadSubsystems();
            } else {
                availableSubsystems.clear();
            }
            dataChangeHandler.run();
        });
        
        startDate.addListener((obs, oldVal, newVal) -> dataChangeHandler.run());
        endDate.addListener((obs, oldVal, newVal) -> dataChangeHandler.run());
        selectedMetricType.addListener((obs, oldVal, newVal) -> dataChangeHandler.run());
        selectedSubsystem.addListener((obs, oldVal, newVal) -> dataChangeHandler.run());
        
        // Track listeners for cleanup
        trackPropertyListener(dataChangeHandler);
        
        // Set default series labels
        subsystemProgressData.setName("Progress");
        velocityData.setName("Team Velocity");
    }
    
    /**
     * Loads the available subsystems for the selected project.
     */
    public void loadSubsystems() {
        try {
            if (project.get() == null || subsystemService == null) {
                availableSubsystems.clear();
                return;
            }
            
            List<Subsystem> subsystems = subsystemService.findAll();
            availableSubsystems.clear();
            if (subsystems != null) {
                availableSubsystems.addAll(subsystems);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading subsystems", e);
            setErrorMessage("Failed to load subsystems: " + e.getMessage());
        }
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
        
        try {
            String metricType = selectedMetricType.get();
            
            if ("Task Completion".equals(metricType)) {
                loadTaskCompletionData();
            } else if ("Team Velocity".equals(metricType)) {
                loadTeamVelocityData();
            } else if ("Member Contributions".equals(metricType)) {
                loadMemberContributionsData();
            } else if ("Subsystem Progress".equals(metricType)) {
                loadSubsystemProgressData();
            }
            
            hasData.set(true);
            setDirty(false);
            clearErrorMessage();
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading metrics data", e);
            setErrorMessage("Failed to load metrics data: " + e.getMessage());
            hasData.set(false);
            return false;
        }
    }
    
    /**
     * Loads task completion data for pie chart visualization.
     */
    private void loadTaskCompletionData() {
        if (projectService == null) {
            return;
        }
        
        try {
            // Get project summary data
            Map<String, Object> summary = projectService.getProjectSummary(project.get().getId());
            
            // Create pie chart data
            taskDistributionData.clear();
            
            int completedTasks = (Integer) summary.get("completedTasks");
            int totalTasks = (Integer) summary.get("totalTasks");
            int inProgressTasks = totalTasks - completedTasks;
            
            if (totalTasks > 0) {
                taskDistributionData.add(new PieChart.Data("Completed", completedTasks));
                taskDistributionData.add(new PieChart.Data("In Progress", inProgressTasks));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading task completion data", e);
            setErrorMessage("Failed to load task completion data: " + e.getMessage());
        }
    }
    
    /**
     * Loads team velocity data for line chart visualization.
     */
    private void loadTeamVelocityData() {
        // In a real implementation, this would calculate velocity over time
        // For this implementation, we'll generate some sample data
        
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
            velocityData.getData().add(new XYChart.Data<>(weekCount, velocity));
            
            date = date.plusWeeks(1);
            weekCount++;
        }
    }
    
    /**
     * Loads member contribution data for stacked bar chart visualization.
     */
    private void loadMemberContributionsData() {
        if (teamMemberService == null) {
            return;
        }
        
        try {
            // Clear existing data
            memberContributionData.clear();
            
            // Get team members
            List<TeamMember> members = teamMemberService.findAll();
            
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
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading member contributions data", e);
            setErrorMessage("Failed to load member contributions data: " + e.getMessage());
        }
    }
    
    /**
     * Loads subsystem progress data for bar chart visualization.
     */
    private void loadSubsystemProgressData() {
        if (subsystemService == null) {
            return;
        }
        
        try {
            // Clear existing data
            subsystemProgressData.getData().clear();
            
            // Get subsystems
            List<Subsystem> subsystems;
            
            if (selectedSubsystem.get() != null) {
                subsystems = Collections.singletonList(selectedSubsystem.get());
            } else {
                subsystems = subsystemService.findAll();
            }
            
            if (subsystems == null || subsystems.isEmpty()) {
                return;
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
                
                subsystemProgressData.getData().add(new XYChart.Data<>(subsystem.getName(), progress));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading subsystem progress data", e);
            setErrorMessage("Failed to load subsystem progress data: " + e.getMessage());
        }
    }
    
    /**
     * Generates a PDF report of the current metrics.
     * For the MVP, this is just a placeholder.
     */
    public void generatePDFReport() {
        LOGGER.info("Generate PDF report action triggered");
        // This would be implemented in a future version
    }
    
    /**
     * Exports the current metrics data to CSV.
     * For the MVP, this is just a placeholder.
     */
    public void exportDataToCSV() {
        LOGGER.info("Export data to CSV action triggered");
        // This would be implemented in a future version
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
    public List<XYChart.Series<String, Number>> getMemberContributionData() {
        return memberContributionData;
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
     * Sets the selected metric type.
     *
     * @param metricType the metric type
     */
    public void setSelectedMetricType(String metricType) {
        this.selectedMetricType.set(metricType);
    }
    
    /**
     * Gets the selected metric type.
     *
     * @return the selected metric type
     */
    public String getSelectedMetricType() {
        return selectedMetricType.get();
    }
    
    /**
     * Gets the selected metric type property.
     *
     * @return the selected metric type property
     */
    public StringProperty selectedMetricTypeProperty() {
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
     * Gets the available subsystems.
     *
     * @return the available subsystems
     */
    public ObservableList<Subsystem> getAvailableSubsystems() {
        return availableSubsystems;
    }
    
    /**
     * Cleans up resources when the ViewModel is no longer needed.
     */
    @Override
    public void cleanupResources() {
        super.cleanupResources();
        
        // Clear chart data
        subsystemProgressData.getData().clear();
        taskDistributionData.clear();
        velocityData.getData().clear();
        memberContributionData.clear();
        
        // Clear collections
        availableSubsystems.clear();
    }
    
    /**
     * Clears the error message.
     * Made public to satisfy the project standard.
     */
    @Override
    public void clearErrorMessage() {
        super.clearErrorMessage();
    }
}