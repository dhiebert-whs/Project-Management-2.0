package org.frcpm.presenters;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.frcpm.binding.ViewModelBinding;
import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.services.AttendanceService;
import org.frcpm.services.DialogService;
import org.frcpm.services.MeetingService;
import org.frcpm.services.TaskService;
import org.frcpm.viewmodels.DailyViewModel;
import org.frcpm.di.ViewLoader;
import org.frcpm.views.AttendanceView;
import org.frcpm.views.MeetingView;
import org.frcpm.views.TaskView;

import javax.inject.Inject;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Presenter for the daily view using AfterburnerFX pattern.
 * Shows tasks and meetings scheduled for the current day.
 */
public class DailyPresenter implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(DailyPresenter.class.getName());

    // FXML UI components
    @FXML
    private DatePicker datePicker;

    @FXML
    private Label dateLabel;

    @FXML
    private TableView<Task> tasksTable;

    @FXML
    private TableColumn<Task, String> taskTitleColumn;

    @FXML
    private TableColumn<Task, String> taskSubsystemColumn;

    @FXML
    private TableColumn<Task, Integer> taskProgressColumn;

    @FXML
    private TableView<Meeting> meetingsTable;

    @FXML
    private TableColumn<Meeting, String> meetingTitleColumn;

    @FXML
    private TableColumn<Meeting, String> meetingTimeColumn;

    @FXML
    private Button addTaskButton;

    @FXML
    private Button editTaskButton;

    @FXML
    private Button addMeetingButton;

    @FXML
    private Button editMeetingButton;

    @FXML
    private Button takeAttendanceButton;

    @FXML
    private Button refreshButton;

    // Injected services
    @Inject
    private TaskService taskService;
    
    @Inject
    private MeetingService meetingService;
    
    @Inject
    private AttendanceService attendanceService;
    
    @Inject
    private DialogService dialogService;

    // ViewModel - now injected
    @Inject
    private DailyViewModel viewModel;
    
    private ResourceBundle resources;
    private Project currentProject;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initializing DailyViewPresenter with resource bundle");
        
        this.resources = resources;
        
        // Check if we're in a testing environment and create fallbacks
        if (viewModel == null) {
            LOGGER.severe("ViewModel not injected - creating manually as fallback");
            viewModel = new DailyViewModel(taskService, meetingService);
        }

        // Setup table columns
        setupTasksTable();
        setupMeetingsTable();
        
        // Setup bindings
        setupBindings();
        
        // Setup error handling
        setupErrorHandling();
        
        // Set today's date by default
        if (datePicker != null) {
            datePicker.setValue(LocalDate.now());
        }
    }

    /**
     * Sets up the tasks table columns.
     */
    private void setupTasksTable() {
        // Check for null UI components for testability
        if (tasksTable == null || taskTitleColumn == null || 
            taskSubsystemColumn == null || taskProgressColumn == null) {
            LOGGER.warning("Tasks table components not initialized - likely in test environment");
            return;
        }

        // Setup task title column
        taskTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        
        // Setup subsystem column with subsystem name
        taskSubsystemColumn.setCellValueFactory(cellData -> {
            Task task = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                    task.getSubsystem() != null ? task.getSubsystem().getName() : "");
        });
        
        // Setup progress column with progress bar
        taskProgressColumn.setCellValueFactory(new PropertyValueFactory<>("progress"));
        taskProgressColumn.setCellFactory(column -> new TableCell<Task, Integer>() {
            private final ProgressBar progressBar = new ProgressBar();
            
            @Override
            protected void updateItem(Integer progress, boolean empty) {
                super.updateItem(progress, empty);
                if (empty || progress == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    progressBar.setProgress(progress / 100.0);
                    progressBar.setPrefWidth(80);
                    setText(progress + "%");
                    setGraphic(progressBar);
                }
            }
        });
        
        // Setup row double-click handler for editing
        tasksTable.setRowFactory(tv -> {
            TableRow<Task> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleEditTask(row.getItem());
                }
            });
            return row;
        });
    }
    
    /**
     * Sets up the meetings table columns.
     */
    private void setupMeetingsTable() {
        // Check for null UI components for testability
        if (meetingsTable == null || meetingTitleColumn == null || meetingTimeColumn == null) {
            LOGGER.warning("Meetings table components not initialized - likely in test environment");
            return;
        }

        // Setup meeting title column
        meetingTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        
        // Setup meeting time column
        meetingTimeColumn.setCellValueFactory(cellData -> {
            Meeting meeting = cellData.getValue();
            String startTime = meeting.getStartTime() != null ? meeting.getStartTime().toString() : "";
            String endTime = meeting.getEndTime() != null ? meeting.getEndTime().toString() : "";
            return new javafx.beans.property.SimpleStringProperty(startTime + " - " + endTime);
        });
        
        // Setup row double-click handler for editing
        meetingsTable.setRowFactory(tv -> {
            TableRow<Meeting> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleEditMeeting(row.getItem());
                }
            });
            return row;
        });
    }
    
    /**
     * Sets up bindings between UI controls and ViewModel properties.
     */
    private void setupBindings() {
        // Check for null UI components for testability
        if (datePicker == null || dateLabel == null || tasksTable == null || 
            meetingsTable == null || addTaskButton == null || editTaskButton == null || 
            addMeetingButton == null || editMeetingButton == null || 
            takeAttendanceButton == null || refreshButton == null) {
            LOGGER.warning("UI components not initialized - likely in test environment");
            return;
        }

        try {
            // Bind date picker
            ViewModelBinding.bindDatePicker(datePicker, viewModel.selectedDateProperty());
            
            // Format date for the label
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
            dateLabel.textProperty().bind(
                javafx.beans.binding.Bindings.createStringBinding(
                    () -> viewModel.getSelectedDate() != null ? 
                          viewModel.getSelectedDate().format(formatter) : "",
                    viewModel.selectedDateProperty()
                )
            );
            
            // Bind tables to ViewModel lists
            tasksTable.setItems(viewModel.getTasks());
            meetingsTable.setItems(viewModel.getMeetings());
            
            // Bind buttons to commands
            ViewModelBinding.bindCommandButton(addTaskButton, viewModel.getAddTaskCommand());
            ViewModelBinding.bindCommandButton(editTaskButton, viewModel.getEditTaskCommand());
            ViewModelBinding.bindCommandButton(addMeetingButton, viewModel.getAddMeetingCommand());
            ViewModelBinding.bindCommandButton(editMeetingButton, viewModel.getEditMeetingCommand());
            ViewModelBinding.bindCommandButton(takeAttendanceButton, viewModel.getTakeAttendanceCommand());
            ViewModelBinding.bindCommandButton(refreshButton, viewModel.getRefreshCommand());
            
            // Bind selection changes
            tasksTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldVal, newVal) -> viewModel.setSelectedTask(newVal));
                    
            meetingsTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldVal, newVal) -> viewModel.setSelectedMeeting(newVal));
            
            // Add listener to date picker to reload data when date changes
            datePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    viewModel.loadDataForDate(newVal);
                }
            });
            
            // Manually set actions for buttons that need access to the view
            addTaskButton.setOnAction(event -> handleAddTask());
            editTaskButton.setOnAction(event -> {
                Task selectedTask = tasksTable.getSelectionModel().getSelectedItem();
                if (selectedTask != null) {
                    handleEditTask(selectedTask);
                } else {
                    showInfoAlert("No Selection", "Please select a task to edit");
                }
            });
            
            addMeetingButton.setOnAction(event -> handleAddMeeting());
            editMeetingButton.setOnAction(event -> {
                Meeting selectedMeeting = meetingsTable.getSelectionModel().getSelectedItem();
                if (selectedMeeting != null) {
                    handleEditMeeting(selectedMeeting);
                } else {
                    showInfoAlert("No Selection", "Please select a meeting to edit");
                }
            });
            
            takeAttendanceButton.setOnAction(event -> {
                Meeting selectedMeeting = meetingsTable.getSelectionModel().getSelectedItem();
                if (selectedMeeting != null) {
                    handleTakeAttendance(selectedMeeting);
                } else {
                    showInfoAlert("No Selection", "Please select a meeting to take attendance");
                }
            });
            
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
     * Sets the current project.
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
        refreshData();
    }
    
    /**
     * Refreshes data for the selected date.
     */
    private void refreshData() {
        if (datePicker != null && datePicker.getValue() != null) {
            viewModel.loadDataForDate(datePicker.getValue());
        } else {
            viewModel.loadDataForDate(LocalDate.now());
        }
    }
    
    /**
     * Handles adding a new task.
     */
    private void handleAddTask() {
        if (currentProject == null) {
            showErrorAlert("Error", "No project selected");
            return;
        }
        
        try {
            // Use ViewLoader to show dialog
            TaskPresenter presenter = ViewLoader.showDialog(TaskView.class, 
                    resources.getString("task.new.title"), 
                    datePicker.getScene().getWindow());
            
            if (presenter != null) {
                // Set start/end date to the currently selected date
                Task newTask = new Task();
                newTask.setStartDate(viewModel.getSelectedDate());
                newTask.setEndDate(viewModel.getSelectedDate());
                newTask.setProject(currentProject);
                
                presenter.initNewTask(newTask);
                
                // Refresh data after dialog closes
                refreshData();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error adding task", e);
            showErrorAlert("Error", "Failed to open task dialog: " + e.getMessage());
        }
    }
    
    /**
     * Handles editing an existing task.
     * 
     * @param task the task to edit
     */
    private void handleEditTask(Task task) {
        if (task == null) {
            return;
        }
        
        try {
            // Use ViewLoader to show dialog
            TaskPresenter presenter = ViewLoader.showDialog(TaskView.class, 
                    resources.getString("task.edit.title"), 
                    datePicker.getScene().getWindow());
            
            if (presenter != null) {
                presenter.initExistingTask(task);
                
                // Refresh data after dialog closes
                refreshData();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error editing task", e);
            showErrorAlert("Error", "Failed to open task dialog: " + e.getMessage());
        }
    }
    
    /**
     * Handles adding a new meeting.
     */
    private void handleAddMeeting() {
        if (currentProject == null) {
            showErrorAlert("Error", "No project selected");
            return;
        }
        
        try {
            // Use ViewLoader to show dialog
            MeetingPresenter presenter = ViewLoader.showDialog(MeetingView.class, 
                    resources.getString("meeting.new.title"), 
                    datePicker.getScene().getWindow());
            
            if (presenter != null) {
                // Create new meeting with current date
                Meeting meeting = new Meeting();
                meeting.setDate(viewModel.getSelectedDate());
                meeting.setProject(currentProject);
                
                presenter.initNewMeeting(currentProject);
                
                // Refresh data after dialog closes
                refreshData();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error adding meeting", e);
            showErrorAlert("Error", "Failed to open meeting dialog: " + e.getMessage());
        }
    }
    
    /**
     * Handles editing an existing meeting.
     * 
     * @param meeting the meeting to edit
     */
    private void handleEditMeeting(Meeting meeting) {
        if (meeting == null) {
            return;
        }
        
        try {
            // Use ViewLoader to show dialog
            MeetingPresenter presenter = ViewLoader.showDialog(MeetingView.class, 
                    resources.getString("meeting.edit.title"), 
                    datePicker.getScene().getWindow());
            
            if (presenter != null) {
                presenter.initExistingMeeting(meeting);
                
                // Refresh data after dialog closes
                refreshData();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error editing meeting", e);
            showErrorAlert("Error", "Failed to open meeting dialog: " + e.getMessage());
        }
    }
    
    /**
     * Handles taking attendance for a meeting.
     * 
     * @param meeting the meeting
     */
    private void handleTakeAttendance(Meeting meeting) {
        if (meeting == null) {
            return;
        }
        
        try {
            // Use ViewLoader to show dialog
            AttendancePresenter presenter = ViewLoader.showDialog(AttendanceView.class, 
                    resources.getString("attendance.title"), 
                    datePicker.getScene().getWindow());
            
            if (presenter != null) {
                presenter.initWithMeeting(meeting);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error taking attendance", e);
            showErrorAlert("Error", "Failed to open attendance dialog: " + e.getMessage());
        }
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
    public DailyViewModel getViewModel() {
        return viewModel;
    }
}