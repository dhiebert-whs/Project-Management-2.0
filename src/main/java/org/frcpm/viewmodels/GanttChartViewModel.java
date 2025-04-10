// src/main/java/org/frcpm/viewmodels/GanttChartViewModel.java
package org.frcpm.viewmodels;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.frcpm.binding.Command;
import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.services.GanttDataService;
import org.frcpm.services.ServiceFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ViewModel for the Gantt chart visualization.
 * Follows the MVVM pattern for standardization.
 */
public class GanttChartViewModel extends BaseViewModel {
    private static final Logger LOGGER = Logger.getLogger(GanttChartViewModel.class.getName());

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

    // Commands
    private final Command refreshCommand;
    private final Command zoomInCommand;
    private final Command zoomOutCommand;
    private final Command exportCommand;
    private final Command todayCommand;
    private final Command toggleMilestonesCommand;
    private final Command toggleDependenciesCommand;

    /**
     * Creates a new GanttChartViewModel with default services.
     */
    public GanttChartViewModel() {
        this(ServiceFactory.getGanttDataService());
    }

    /**
     * Creates a new GanttChartViewModel with the specified service.
     * This constructor is mainly used for testing.
     * 
     * @param ganttDataService the Gantt data service
     */
    public GanttChartViewModel(GanttDataService ganttDataService) {
        this.ganttDataService = ganttDataService;

        // Initialize commands
        refreshCommand = new Command(this::loadGanttData, this::canLoadData);
        zoomInCommand = new Command(this::zoomIn);
        zoomOutCommand = new Command(this::zoomOut);
        exportCommand = new Command(this::exportChart, this::canExport);
        todayCommand = new Command(this::goToToday);
        toggleMilestonesCommand = new Command(this::toggleMilestones);
        toggleDependenciesCommand = new Command(this::toggleDependencies);

        // Set up property listeners
        viewMode.addListener((obs, oldVal, newVal) -> {
            if (canLoadData()) {
                loadGanttData();
            }
        });

        filterOption.addListener((obs, oldVal, newVal) -> {
            if (canLoadData()) {
                applyFilter();
            }
        });

        project.addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                startDate.set(newVal.getStartDate());
                LocalDate projectEnd = newVal.getHardDeadline();
                endDate.set(projectEnd);
                loadGanttData();
            } else {
                dataLoaded.set(false);
                chartData.set(null);
            }
        });
    }

    /**
     * Loads Gantt chart data for the current project and date range.
     */
    private void loadGanttData() {
        Project currentProject = project.get();
        if (currentProject == null) {
            statusMessage.set("No project selected");
            dataLoaded.set(false);
            return;
        }

        try {
            // Get data from service
            Map<String, Object> data = ganttDataService.formatTasksForGantt(
                    currentProject.getId(), startDate.get(), endDate.get());

            // Apply any active filters
            data = applyFilterToData(data);

            // Update chart data property
            chartData.set(data);
            dataLoaded.set(true);
            statusMessage.set("Chart data loaded successfully");

            // Load critical path
            if (FilterOption.CRITICAL_PATH.equals(filterOption.get())) {
                loadCriticalPath();
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading Gantt data", e);
            statusMessage.set("Error loading chart data: " + e.getMessage());
            dataLoaded.set(false);
        }
    }

    /**
     * Applies the current filter to the Gantt data.
     */
    private void applyFilter() {
        if (chartData.get() == null) {
            return;
        }

        try {
            Map<String, Object> filteredData = applyFilterToData(chartData.get());
            chartData.set(filteredData);
            statusMessage.set("Filter applied: " + filterOption.get());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error applying filter", e);
            statusMessage.set("Error applying filter: " + e.getMessage());
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
            Map<String, Object> filterCriteria = Map.of("filterType", filter.toString());

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
            criticalPathTasks.setAll(criticalPath);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading critical path", e);
            statusMessage.set("Error loading critical path: " + e.getMessage());
        }
    }

    /**
     * Zooms in by adjusting the date range.
     */
    private void zoomIn() {
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
        if (canLoadData()) {
            loadGanttData();
        }
    }

    /**
     * Zooms out by adjusting the date range.
     */
    private void zoomOut() {
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
        if (canLoadData()) {
            loadGanttData();
        }
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
        LocalDate today = LocalDate.now();

        // Keep the same range but center on today
        long currentRangeDays = startDate.get().until(endDate.get()).getDays();

        startDate.set(today.minusDays(currentRangeDays / 2));
        endDate.set(today.plusDays(currentRangeDays / 2));

        // Reload data with new range
        if (canLoadData()) {
            loadGanttData();
        }
    }

    /**
     * Toggles the display of milestones.
     */
    private void toggleMilestones() {
        showMilestones.set(!showMilestones.get());

        // Notify about the change
        statusMessage.set(showMilestones.get() ? "Showing milestones" : "Hiding milestones");
    }

    /**
     * Toggles the display of task dependencies.
     */
    private void toggleDependencies() {
        showDependencies.set(!showDependencies.get());

        // Notify about the change
        statusMessage.set(showDependencies.get() ? "Showing dependencies" : "Hiding dependencies");
    }

    /**
     * Checks if data can be loaded.
     * 
     * @return true if a project is selected, false otherwise
     */
    private boolean canLoadData() {
        return project.get() != null;
    }

    /**
     * Checks if the chart can be exported.
     * 
     * @return true if data is loaded, false otherwise
     */
    private boolean canExport() {
        return dataLoaded.get();
    }

    /**
     * Clears the error message.
     * Public wrapper for BaseViewModel's protected clearErrorMessage method.
     */
    @Override
    public void clearErrorMessage() {
        super.clearErrorMessage();
    }

    // Getters for commands

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

    public Command getToggleMilestonesCommand() {
        return toggleMilestonesCommand;
    }

    public Command getToggleDependenciesCommand() {
        return toggleDependenciesCommand;
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

    public BooleanProperty showMilestonesProperty() {
        return showMilestones;
    }

    public boolean isShowDependencies() {
        return showDependencies.get();
    }

    public BooleanProperty showDependenciesProperty() {
        return showDependencies;
    }
}