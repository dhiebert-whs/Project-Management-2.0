// src/main/java/org/frcpm/mvvm/viewmodels/GanttChartMvvmViewModel.java
package org.frcpm.mvvm.viewmodels;

import de.saxsys.mvvmfx.utils.commands.Command;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.Pane;



import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.frcpm.async.TaskExecutor;
import org.frcpm.models.*;
import org.frcpm.mvvm.BaseMvvmViewModel;
import org.frcpm.mvvm.async.MvvmAsyncHelper;
import org.frcpm.services.GanttDataService;
import org.frcpm.services.VisualizationService;
import org.frcpm.services.impl.VisualizationServiceImpl;

/**
 * ViewModel for the Gantt chart view using MVVMFx.
 */
public class GanttChartMvvmViewModel extends BaseMvvmViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(GanttChartMvvmViewModel.class.getName());
    
    // View mode options
    public enum ViewMode {
        DAY("Day"),
        WEEK("Week"),
        MONTH("Month");
        
        private final String displayName;
        
        ViewMode(String displayName) {
            this.displayName = displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    // Filter options
    public enum FilterOption {
        ALL_TASKS("All Tasks"),
        MY_TASKS("My Tasks"),
        CRITICAL_PATH("Critical Path"),
        BEHIND_SCHEDULE("Behind Schedule");
        
        private final String displayName;
        
        FilterOption(String displayName) {
            this.displayName = displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    // Services
    private final GanttDataService ganttDataService;
    private final VisualizationService visualizationService;
    
    // Observable properties
    private final ObjectProperty<Project> project = new SimpleObjectProperty<>();
    private final ObjectProperty<ViewMode> viewMode = new SimpleObjectProperty<>(ViewMode.WEEK);
    private final ObjectProperty<FilterOption> filterOption = new SimpleObjectProperty<>(FilterOption.ALL_TASKS);
    private final StringProperty statusMessage = new SimpleStringProperty("");
    private final ObjectProperty<Map<String, Object>> chartData = new SimpleObjectProperty<>();
    private final BooleanProperty dataLoaded = new SimpleBooleanProperty(false);
    private final ObjectProperty<Task> selectedTask = new SimpleObjectProperty<>();
    private final ObjectProperty<Milestone> selectedMilestone = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> startDate = new SimpleObjectProperty<>(LocalDate.now().minusDays(7));
    private final ObjectProperty<LocalDate> endDate = new SimpleObjectProperty<>(LocalDate.now().plusDays(30));
    private final ObservableList<Long> criticalPathTasks = FXCollections.observableArrayList();
    private final BooleanProperty showMilestones = new SimpleBooleanProperty(true);
    private final BooleanProperty showDependencies = new SimpleBooleanProperty(true);
    private final ObjectProperty<Subsystem> selectedSubsystem = new SimpleObjectProperty<>();
    private final ObjectProperty<Subteam> selectedSubteam = new SimpleObjectProperty<>();
    private final BooleanProperty showCompletedTasks = new SimpleBooleanProperty(true);
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    
    // Chart pane property for the view to bind to
    private final ObjectProperty<Pane> chartPane = new SimpleObjectProperty<>();
    
    // Commands
    private Command refreshCommand;
    private Command zoomInCommand;
    private Command zoomOutCommand;
    private Command exportCommand;
    private Command todayCommand;
    
    /**
     * Creates a new GanttChartMvvmViewModel with the specified services.
     * 
     * @param ganttDataService the Gantt data service
     */
  
    public GanttChartMvvmViewModel(GanttDataService ganttDataService) {
        this.ganttDataService = ganttDataService;
        this.visualizationService = new VisualizationServiceImpl();
        
        // Initialize commands
        initializeCommands();
        
        // Set up property listeners
        setupPropertyListeners();
    }
    
    /**
     * Initializes the commands.
     */
    private void initializeCommands() {
        // Refresh command
        refreshCommand = MvvmAsyncHelper.createAsyncCommand(
            // Return a CompletableFuture
            () -> TaskExecutor.executeAsync(
                "LoadGanttData",
                () -> {
                    loadGanttDataAsync();
                    return null;
                },
                null,
                null
            ),
            // Success handler
            result -> {
                // No additional handling needed, everything is done in loadGanttDataAsync
            },
            // Error handler
            error -> {
                LOGGER.log(Level.SEVERE, "Error loading Gantt data", error);
                statusMessage.set("Error loading chart data: " + error.getMessage());
                setErrorMessage("Error loading chart data: " + error.getMessage());
            },
            // Loading property
            loading
        );
        
        // Zoom in command
        zoomInCommand = MvvmAsyncHelper.createAsyncCommand(
            () -> TaskExecutor.executeAsync(
                "ZoomIn", 
                () -> { 
                    zoomIn(); 
                    return null; 
                }, 
                null, 
                null
            ),
            // Success handler
            result -> {
                // No additional handling needed
            },
            // Error handler
            error -> {
                LOGGER.log(Level.SEVERE, "Error zooming in", error);
                statusMessage.set("Error zooming in: " + error.getMessage());
                setErrorMessage("Error zooming in: " + error.getMessage());
            },
            // Loading property
            loading
        );
        
        // Zoom out command
        zoomOutCommand = MvvmAsyncHelper.createAsyncCommand(
            () -> TaskExecutor.executeAsync(
                "ZoomOut", 
                () -> { 
                    zoomOut(); 
                    return null; 
                }, 
                null, 
                null
            ),
            // Success handler
            result -> {
                // No additional handling needed
            },
            // Error handler
            error -> {
                LOGGER.log(Level.SEVERE, "Error zooming out", error);
                statusMessage.set("Error zooming out: " + error.getMessage());
                setErrorMessage("Error zooming out: " + error.getMessage());
            },
            // Loading property
            loading
        );
        
        // Export command
        exportCommand = MvvmAsyncHelper.createAsyncCommand(
            () -> TaskExecutor.executeAsync(
                "ExportChart", 
                () -> { 
                    exportChart(); 
                    return null; 
                }, 
                null, 
                null
            ),
            // Success handler
            result -> {
                // No additional handling needed
            },
            // Error handler
            error -> {
                LOGGER.log(Level.SEVERE, "Error exporting chart", error);
                statusMessage.set("Error exporting chart: " + error.getMessage());
                setErrorMessage("Error exporting chart: " + error.getMessage());
            },
            // Loading property
            loading
        );
        
        // Today command
        todayCommand = MvvmAsyncHelper.createAsyncCommand(
            () -> TaskExecutor.executeAsync(
                "GoToToday", 
                () -> { 
                    goToToday(); 
                    return null; 
                }, 
                null, 
                null
            ),
            // Success handler
            result -> {
                // No additional handling needed
            },
            // Error handler
            error -> {
                LOGGER.log(Level.SEVERE, "Error going to today", error);
                statusMessage.set("Error going to today: " + error.getMessage());
                setErrorMessage("Error going to today: " + error.getMessage());
            },
            // Loading property
            loading
        );
    }
    
    /**
     * Sets up listeners for property changes.
     */
    private void setupPropertyListeners() {
        // Apply dirty flag handler when properties change
        Runnable dirtyHandler = createDirtyFlagHandler(null);
        
        // Create a listener for viewMode changes
        viewMode.addListener((obs, oldVal, newVal) -> {
            if (project.get() != null && !loading.get()) {
                refreshCommand.execute();
            }
        });
        trackPropertyListener(dirtyHandler);
        
        // Create a listener for filterOption changes
        filterOption.addListener((obs, oldVal, newVal) -> {
            if (project.get() != null && dataLoaded.get() && !loading.get()) {
                applyFilter();
            }
        });
        trackPropertyListener(dirtyHandler);
        
        // Create a listener for project changes
        project.addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                startDate.set(newVal.getStartDate());
                LocalDate projectEnd = newVal.getHardDeadline();
                endDate.set(projectEnd);
                refreshCommand.execute();
            } else {
                dataLoaded.set(false);
                chartData.set(null);
                chartPane.set(null);
            }
        });
        trackPropertyListener(dirtyHandler);
        
        // Create a listener for show milestones changes
        showMilestones.addListener((obs, oldVal, newVal) -> {
            if (project.get() != null && dataLoaded.get() && !loading.get()) {
                updateChartView();
            }
        });
        trackPropertyListener(dirtyHandler);
        
        // Create a listener for show dependencies changes
        showDependencies.addListener((obs, oldVal, newVal) -> {
            if (project.get() != null && dataLoaded.get() && !loading.get()) {
                updateChartView();
            }
        });
        trackPropertyListener(dirtyHandler);
        
        // Create a listener for start date changes
        startDate.addListener((obs, oldVal, newVal) -> {
            setDirty(true);
        });
        trackPropertyListener(dirtyHandler);
        
        // Create a listener for end date changes
        endDate.addListener((obs, oldVal, newVal) -> {
            setDirty(true);
        });
        trackPropertyListener(dirtyHandler);
    }
    
    /**
     * Loads Gantt chart data for the current project and date range asynchronously.
     */
    private void loadGanttDataAsync() {
        Project currentProject = project.get();
        if (currentProject == null) {
            statusMessage.set("No project selected");
            dataLoaded.set(false);
            return;
        }
        
        // No need to set loading flag here as it's handled by MvvmAsyncHelper
        
        try {
            // Run this asynchronously
            Thread.ofVirtual().start(() -> {
                try {
                    // Get data from service
                    final Map<String, Object> rawData = ganttDataService.formatTasksForGantt(
                            currentProject.getId(), startDate.get(), endDate.get());
                    
                    // Apply any active filters - create a new final variable for the filtered data
                    final Map<String, Object> filteredData = applyFilterToData(rawData);
                    
                    // Update on JavaFX thread
                    Platform.runLater(() -> {
                        // Update chart data property with the filtered data
                        chartData.set(filteredData);
                        dataLoaded.set(true);
                        
                        // Create chart view
                        updateChartView();
                        
                        statusMessage.set("Chart data loaded successfully");
                        // No need to set loading to false here as it's handled by MvvmAsyncHelper
                    });
                    
                    // Load critical path if needed
                    if (FilterOption.CRITICAL_PATH.equals(filterOption.get())) {
                        loadCriticalPath();
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        LOGGER.log(Level.SEVERE, "Error loading Gantt data", e);
                        statusMessage.set("Error loading chart data: " + e.getMessage());
                        setErrorMessage("Error loading chart data: " + e.getMessage());
                        dataLoaded.set(false);
                        // No need to set loading to false here as it's handled by MvvmAsyncHelper
                    });
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading Gantt data", e);
            statusMessage.set("Error loading chart data: " + e.getMessage());
            setErrorMessage("Error loading chart data: " + e.getMessage());
            dataLoaded.set(false);
            // No need to set loading to false here as it's handled by MvvmAsyncHelper
        }
    }
    
    /**
     * Updates the chart view based on the current data.
     */
    private void updateChartView() {
        if (chartData.get() == null) {
            return;
        }
        
        try {
            // Create a new chart using the visualization service
            Pane newChartPane = visualizationService.createGanttChartPane(
                project.get().getId(),
                startDate.get(),
                endDate.get(),
                viewMode.get().toString(),
                showMilestones.get(),
                showDependencies.get()
            );
            
            // Update chart pane property for the view to update
            chartPane.set(newChartPane);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating chart view", e);
            setErrorMessage("Failed to update chart view: " + e.getMessage());
        }
    }
    
    /**
     * Applies the current filter and updates the chart.
     */
    private void applyFilter() {
        if (chartData.get() == null || loading.get()) {
            return;
        }
        
        try {
            loading.set(true);
            
            Map<String, Object> filteredData = applyFilterToData(chartData.get());
            chartData.set(filteredData);
            updateChartView();
            
            statusMessage.set("Filter applied: " + filterOption.get());
            loading.set(false);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error applying filter", e);
            statusMessage.set("Error applying filter: " + e.getMessage());
            setErrorMessage("Error applying filter: " + e.getMessage());
            loading.set(false);
        }
    }
    
    /**
     * Applies the current filter to the given data.
     * 
     * @param data the data to filter
     * @return the filtered data
     */
    private Map<String, Object> applyFilterToData(Map<String, Object> data) {
        if (data == null) {
            return null;
        }
        
        FilterOption filter = filterOption.get();
        if (filter == null || filter.equals(FilterOption.ALL_TASKS)) {
            return data;
        }
        
        try {
            // Create filter criteria based on selected option
            Map<String, Object> filterCriteria = new HashMap<>();
            filterCriteria.put("filterType", filter.toString());
            
            // Add subsystem filter if selected
            if (selectedSubsystem.get() != null) {
                filterCriteria.put("subsystem", selectedSubsystem.get().getName());
            }
            
            // Add subteam filter if selected
            if (selectedSubteam.get() != null) {
                filterCriteria.put("subteam", selectedSubteam.get().getName());
            }
            
            // Add completed tasks filter
            filterCriteria.put("showCompleted", showCompletedTasks.get());
            
            // Apply filter using service
            return ganttDataService.applyFiltersToGanttData(data, filterCriteria);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error applying filter to data", e);
            return data; // Return unfiltered data on error
        }
    }
    
    /**
     * Loads the critical path tasks for highlighting.
     */
    private void loadCriticalPath() {
        Project currentProject = project.get();
        if (currentProject == null) {
            return;
        }
        
        try {
            List<Long> criticalPath = ganttDataService.calculateCriticalPath(currentProject.getId());
            Platform.runLater(() -> {
                criticalPathTasks.setAll(criticalPath);
            });
        } catch (Exception e) {
            Platform.runLater(() -> {
                LOGGER.log(Level.SEVERE, "Error loading critical path", e);
                statusMessage.set("Error loading critical path: " + e.getMessage());
                setErrorMessage("Error loading critical path: " + e.getMessage());
            });
        }
    }
    
    /**
     * Zooms in by adjusting the date range.
     */
    private void zoomIn() {
        if (loading.get()) {
            return;
        }
        
        // Calculate the center date of the current range
        LocalDate center = startDate.get().plusDays(
                startDate.get().until(endDate.get()).getDays() / 2);
        
        // Reduce the range by 25%
        long currentRangeDays = startDate.get().until(endDate.get()).getDays();
        long newRangeDays = Math.max(7, (long) (currentRangeDays * 0.75));
        
        // Set new range centered on the same date
        startDate.set(center.minusDays(newRangeDays / 2));
        endDate.set(center.plusDays(newRangeDays / 2));
        
        // Reload data with new range
        refreshCommand.execute();
    }
    
    /**
     * Zooms out by adjusting the date range.
     */
    private void zoomOut() {
        if (loading.get()) {
            return;
        }
        
        // Calculate the center date of the current range
        LocalDate center = startDate.get().plusDays(
                startDate.get().until(endDate.get()).getDays() / 2);
        
        // Expand the range by 25%
        long currentRangeDays = startDate.get().until(endDate.get()).getDays();
        long newRangeDays = (long) (currentRangeDays * 1.25);
        
        // Set new range centered on the same date
        startDate.set(center.minusDays(newRangeDays / 2));
        endDate.set(center.plusDays(newRangeDays / 2));
        
        // Reload data with new range
        refreshCommand.execute();
    }
    
    /**
     * Exports the chart as an image or data file.
     * This is a placeholder for the actual export functionality.
     */
    private void exportChart() {
        statusMessage.set("Export functionality not yet implemented");
    }
    
    /**
     * Centers the chart on today's date.
     */
    private void goToToday() {
        if (loading.get()) {
            return;
        }
        
        LocalDate today = LocalDate.now();
        
        // Keep the same range but center on today
        long currentRangeDays = startDate.get().until(endDate.get()).getDays();
        
        startDate.set(today.minusDays(currentRangeDays / 2));
        endDate.set(today.plusDays(currentRangeDays / 2));
        
        // Reload data with new range
        refreshCommand.execute();
    }
    
    /**
     * Toggles the display of milestones.
     */
    public void toggleMilestones() {
        showMilestones.set(!showMilestones.get());
        
        // Notify about the change
        statusMessage.set(showMilestones.get() ? "Showing milestones" : "Hiding milestones");
        
        // Update chart if data is loaded
        if (dataLoaded.get() && !loading.get()) {
            updateChartView();
        }
    }
    
    /**
     * Toggles the display of task dependencies.
     */
    public void toggleDependencies() {
        showDependencies.set(!showDependencies.get());
        
        // Notify about the change
        statusMessage.set(showDependencies.get() ? "Showing dependencies" : "Hiding dependencies");
        
        // Update chart if data is loaded
        if (dataLoaded.get() && !loading.get()) {
            updateChartView();
        }
    }
    
    /**
     * Toggles the display of completed tasks.
     */
    public void toggleCompletedTasks() {
        showCompletedTasks.set(!showCompletedTasks.get());
        
        // Notify about the change
        statusMessage.set(showCompletedTasks.get() ? "Showing completed tasks" : "Hiding completed tasks");
        
        // Reload data with new filter
        if (dataLoaded.get() && !loading.get()) {
            refreshCommand.execute();
        }
    }
    
    /**
     * Cleans up resources used by this ViewModel.
     */
    @Override
    public void dispose() {
        super.dispose();
    }
    
    // Getters and setters for properties
    
    public Project getProject() {
        return project.get();
    }
    
    public void setProject(Project project) {
        this.project.set(project);
    }
    
    public ObjectProperty<Project> projectProperty() {
        return project;
    }
    
    public ViewMode getViewMode() {
        return viewMode.get();
    }
    
    public void setViewMode(ViewMode viewMode) {
        this.viewMode.set(viewMode);
    }
    
    public ObjectProperty<ViewMode> viewModeProperty() {
        return viewMode;
    }
    
    public FilterOption getFilterOption() {
        return filterOption.get();
    }
    
    public void setFilterOption(FilterOption filterOption) {
        this.filterOption.set(filterOption);
    }
    
    public ObjectProperty<FilterOption> filterOptionProperty() {
        return filterOption;
    }
    
    public String getStatusMessage() {
        return statusMessage.get();
    }
    
    public void setStatusMessage(String message) {
        this.statusMessage.set(message);
    }
    
    public StringProperty statusMessageProperty() {
        return statusMessage;
    }
    
    public Map<String, Object> getChartData() {
        return chartData.get();
    }
    
    public ObjectProperty<Map<String, Object>> chartDataProperty() {
        return chartData;
    }
    
    public boolean isDataLoaded() {
        return dataLoaded.get();
    }
    
    public BooleanProperty dataLoadedProperty() {
        return dataLoaded;
    }
    
    public Task getSelectedTask() {
        return selectedTask.get();
    }
    
    public void setSelectedTask(Task task) {
        this.selectedTask.set(task);
    }
    
    public ObjectProperty<Task> selectedTaskProperty() {
        return selectedTask;
    }
    
    public Milestone getSelectedMilestone() {
        return selectedMilestone.get();
    }
    
    public void setSelectedMilestone(Milestone milestone) {
        this.selectedMilestone.set(milestone);
    }
    
    public ObjectProperty<Milestone> selectedMilestoneProperty() {
        return selectedMilestone;
    }
    
    public LocalDate getStartDate() {
        return startDate.get();
    }
    
    public void setStartDate(LocalDate date) {
        this.startDate.set(date);
    }
    
    public ObjectProperty<LocalDate> startDateProperty() {
        return startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate.get();
    }
    
    public void setEndDate(LocalDate date) {
        this.endDate.set(date);
    }
    
    public ObjectProperty<LocalDate> endDateProperty() {
        return endDate;
    }
    
    public ObservableList<Long> getCriticalPathTasks() {
        return criticalPathTasks;
    }
    
    public boolean isShowMilestones() {
        return showMilestones.get();
    }
    
    public void setShowMilestones(boolean show) {
        this.showMilestones.set(show);
    }
    
    public BooleanProperty showMilestonesProperty() {
        return showMilestones;
    }
    
    public boolean isShowDependencies() {
        return showDependencies.get();
    }
    
    public void setShowDependencies(boolean show) {
        this.showDependencies.set(show);
    }
    
    public BooleanProperty showDependenciesProperty() {
        return showDependencies;
    }
    
    public Subsystem getSelectedSubsystem() {
        return selectedSubsystem.get();
    }
    
    public void setSelectedSubsystem(Subsystem subsystem) {
        this.selectedSubsystem.set(subsystem);
    }
    
    public ObjectProperty<Subsystem> selectedSubsystemProperty() {
        return selectedSubsystem;
    }
    
    public Subteam getSelectedSubteam() {
        return selectedSubteam.get();
    }
    
    public void setSelectedSubteam(Subteam subteam) {
        this.selectedSubteam.set(subteam);
    }
    
    public ObjectProperty<Subteam> selectedSubteamProperty() {
        return selectedSubteam;
    }
    
    public boolean isShowCompletedTasks() {
        return showCompletedTasks.get();
    }
    
    public void setShowCompletedTasks(boolean show) {
        this.showCompletedTasks.set(show);
    }
    
    public BooleanProperty showCompletedTasksProperty() {
        return showCompletedTasks;
    }
    
    public Pane getChartPane() {
        return chartPane.get();
    }
    
    public ObjectProperty<Pane> chartPaneProperty() {
        return chartPane;
    }
    
    public BooleanProperty loadingProperty() {
        return loading;
    }
    
    public boolean isLoading() {
        return loading.get();
    }
    
    // Command getters
    
    public Command getRefreshCommand() {
        return refreshCommand;
    }
    
    public Command getZoomInCommand() {
        return zoomInCommand;
    }
    
    public Command getZoomOutCommand() {
        return zoomOutCommand;
    }
    
    public Command getExportCommand() {
        return exportCommand;
    }
    
    public Command getTodayCommand() {
        return todayCommand;
    }
}