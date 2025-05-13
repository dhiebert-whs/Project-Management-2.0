// src/main/java/org/frcpm/mvvm/views/DailyMvvmView.java

package org.frcpm.mvvm.views;

import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.mvvm.CommandAdapter;
import org.frcpm.mvvm.viewmodels.AttendanceMvvmViewModel;
import org.frcpm.mvvm.viewmodels.DailyMvvmViewModel;
import org.frcpm.mvvm.viewmodels.MeetingDetailMvvmViewModel;
import org.frcpm.mvvm.viewmodels.TaskDetailMvvmViewModel;

/**
 * View for the daily task board using MVVMFx.
 */
public class DailyMvvmView implements FxmlView<DailyMvvmViewModel>, Initializable {
    
    private static final Logger LOGGER = Logger.getLogger(DailyMvvmView.class.getName());
    
    @FXML private DatePicker datePicker;
    @FXML private Label dateLabel;
    
    @FXML private TableView<Task> tasksTable;
    @FXML private TableColumn<Task, String> taskTitleColumn;
    @FXML private TableColumn<Task, String> taskSubsystemColumn;
    @FXML private TableColumn<Task, Integer> taskProgressColumn;
    
    @FXML private TableView<Meeting> meetingsTable;
    @FXML private TableColumn<Meeting, String> meetingTitleColumn;
    @FXML private TableColumn<Meeting, String> meetingTimeColumn;
    
    @FXML private Button addTaskButton;
    @FXML private Button editTaskButton;
    @FXML private Button addMeetingButton;
    @FXML private Button editMeetingButton;
    @FXML private Button takeAttendanceButton;
    @FXML private Button refreshButton;
    
    @FXML private Label errorLabel;
    @FXML private ProgressIndicator loadingIndicator;
    
    @InjectViewModel
    private DailyMvvmViewModel viewModel;
    
    private ResourceBundle resources;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initializing DailyMvvmView");
        this.resources = resources;
        
        // Set up table columns
        setupTasksTable();
        setupMeetingsTable();
        
        // Set up bindings
        setupBindings();
        
        // Set up loading indicator
        loadingIndicator.visibleProperty().bind(viewModel.loadingProperty());
        
        // Set up error label
        errorLabel.textProperty().bind(viewModel.errorMessageProperty());
        errorLabel.visibleProperty().bind(viewModel.errorMessageProperty().isNotEmpty());
    }
    
    /**
     * Sets up the tasks table columns.
     */
    private void setupTasksTable() {
        if (tasksTable == null || taskTitleColumn == null || 
            taskSubsystemColumn == null || taskProgressColumn == null) {
            LOGGER.warning("Tasks table components not initialized - likely in test environment");
            return;
        }

        try {
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
            taskProgressColumn.setCellFactory(column -> new javafx.scene.control.TableCell<Task, Integer>() {
                private final javafx.scene.control.ProgressBar progressBar = new javafx.scene.control.ProgressBar();
                
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
                javafx.scene.control.TableRow<Task> row = new javafx.scene.control.TableRow<>();
                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && !row.isEmpty()) {
                        handleEditTask(row.getItem());
                    }
                });
                return row;
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up tasks table", e);
            throw new RuntimeException("Failed to set up tasks table", e);
        }
    }
    
    /**
     * Sets up the meetings table columns.
     */
    private void setupMeetingsTable() {
        if (meetingsTable == null || meetingTitleColumn == null || meetingTimeColumn == null) {
            LOGGER.warning("Meetings table components not initialized - likely in test environment");
            return;
        }

        try {
            // Set up meeting title column - use appropriate property or create a description
            // Since Meeting doesn't have a title method, we'll create a descriptive string
            meetingTitleColumn.setCellValueFactory(cellData -> {
                Meeting meeting = cellData.getValue();
                String description = "Meeting on " + meeting.getDate();
                if (meeting.getNotes() != null && !meeting.getNotes().isEmpty()) {
                    description += ": " + meeting.getNotes();
                }
                return new javafx.beans.property.SimpleStringProperty(description);
            });
            
            // Set up meeting time column
            meetingTimeColumn.setCellValueFactory(cellData -> {
                Meeting meeting = cellData.getValue();
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                String startTime = meeting.getStartTime() != null ? meeting.getStartTime().format(timeFormatter) : "";
                String endTime = meeting.getEndTime() != null ? meeting.getEndTime().format(timeFormatter) : "";
                return new javafx.beans.property.SimpleStringProperty(startTime + " - " + endTime);
            });
            
            // Set up row double-click handler for editing
            meetingsTable.setRowFactory(tv -> {
                javafx.scene.control.TableRow<Meeting> row = new javafx.scene.control.TableRow<>();
                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && !row.isEmpty()) {
                        handleEditMeeting(row.getItem());
                    }
                });
                return row;
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up meetings table", e);
            throw new RuntimeException("Failed to set up meetings table", e);
        }
    }
    
    /**
     * Sets up bindings between UI controls and ViewModel properties.
     */
    private void setupBindings() {
        if (datePicker == null || dateLabel == null || tasksTable == null || 
            meetingsTable == null || addTaskButton == null || editTaskButton == null || 
            addMeetingButton == null || editMeetingButton == null || 
            takeAttendanceButton == null || refreshButton == null) {
            LOGGER.warning("UI components not initialized - likely in test environment");
            return;
        }

        try {
            // Bind date picker
            datePicker.valueProperty().bindBidirectional(viewModel.selectedDateProperty());
            
            // Bind formatted date label
            dateLabel.textProperty().bind(viewModel.formattedDateProperty());
            
            // Bind tables to ViewModel lists
            tasksTable.setItems(viewModel.getTasks());
            meetingsTable.setItems(viewModel.getMeetings());
            
            // Bind selection changes
            tasksTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldVal, newVal) -> viewModel.setSelectedTask(newVal));
                    
            meetingsTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldVal, newVal) -> viewModel.setSelectedMeeting(newVal));
            
            // Bind command buttons
            CommandAdapter.bindCommandButton(addTaskButton, viewModel.getAddTaskCommand());
            CommandAdapter.bindCommandButton(editTaskButton, viewModel.getEditTaskCommand());
            CommandAdapter.bindCommandButton(addMeetingButton, viewModel.getAddMeetingCommand());
            CommandAdapter.bindCommandButton(editMeetingButton, viewModel.getEditMeetingCommand());
            CommandAdapter.bindCommandButton(takeAttendanceButton, viewModel.getTakeAttendanceCommand());
            CommandAdapter.bindCommandButton(refreshButton, viewModel.getRefreshCommand());
            
            // Set action handlers for buttons that need to access the view
            addTaskButton.setOnAction(event -> handleAddTask());
            editTaskButton.setOnAction(event -> {
                Task selectedTask = tasksTable.getSelectionModel().getSelectedItem();
                if (selectedTask != null) {
                    handleEditTask(selectedTask);
                } else {
                    showInfoAlert(resources.getString("info.title"), 
                               resources.getString("info.no.selection.task"));
                }
            });
            
            addMeetingButton.setOnAction(event -> handleAddMeeting());
            editMeetingButton.setOnAction(event -> {
                Meeting selectedMeeting = meetingsTable.getSelectionModel().getSelectedItem();
                if (selectedMeeting != null) {
                    handleEditMeeting(selectedMeeting);
                } else {
                    showInfoAlert(resources.getString("info.title"), 
                               resources.getString("info.no.selection.meeting"));
                }
            });
            
            takeAttendanceButton.setOnAction(event -> {
                Meeting selectedMeeting = meetingsTable.getSelectionModel().getSelectedItem();
                if (selectedMeeting != null) {
                    handleTakeAttendance(selectedMeeting);
                } else {
                    showInfoAlert(resources.getString("info.title"), 
                               resources.getString("info.no.selection.meeting"));
                }
            });
            
            refreshButton.setOnAction(event -> viewModel.refreshData());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up bindings", e);
            throw new RuntimeException("Failed to set up bindings", e);
        }
    }
    
    /**
     * Sets the project for the daily task board.
     * 
     * @param project the project
     */
    public void setProject(Project project) {
        viewModel.setCurrentProject(project);
    }
    
    /**
     * Handles adding a new task.
     */
    private void handleAddTask() {
        Project project = viewModel.getCurrentProject();
        if (project == null) {
            showErrorAlert(resources.getString("error.title"), 
                        resources.getString("error.project.required"));
            return;
        }
        
        try {
            // Load the task detail view
            ViewTuple<TaskDetailMvvmView, TaskDetailMvvmViewModel> viewTuple = 
                FluentViewLoader.fxmlView(TaskDetailMvvmView.class)
                    .resourceBundle(resources)
                    .load();
            
            // Get the controller and view model
            TaskDetailMvvmView viewController = viewTuple.getCodeBehind();
            
            // Initialize with a new task
            // Note: In the real implementation, you would need to create a valid task with a subsystem
            // For this example, we'll assume that's handled in the TaskDetailMvvmView
            Task newTask = new Task();
            newTask.setProject(project);
            newTask.setStartDate(viewModel.getSelectedDate());
            newTask.setEndDate(viewModel.getSelectedDate());
            
            viewController.initNewTask(newTask);
            
            // Create a new stage for the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle(resources.getString("task.new.title"));
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(datePicker.getScene().getWindow());
            dialogStage.setScene(new Scene(viewTuple.getView()));
            
            // Show the dialog and wait for it to close
            dialogStage.showAndWait();
            
            // Refresh data after dialog closes
            viewModel.refreshData();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error adding task", e);
            showErrorAlert(resources.getString("error.title"), 
                        resources.getString("error.task.dialog.failed") + ": " + e.getMessage());
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
            // Load the task detail view
            ViewTuple<TaskDetailMvvmView, TaskDetailMvvmViewModel> viewTuple = 
                FluentViewLoader.fxmlView(TaskDetailMvvmView.class)
                    .resourceBundle(resources)
                    .load();
            
            // Get the controller and view model
            TaskDetailMvvmView viewController = viewTuple.getCodeBehind();
            
            // Initialize with the existing task
            viewController.initExistingTask(task);
            
            // Create a new stage for the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle(resources.getString("task.edit.title"));
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(datePicker.getScene().getWindow());
            dialogStage.setScene(new Scene(viewTuple.getView()));
            
            // Show the dialog and wait for it to close
            dialogStage.showAndWait();
            
            // Refresh data after dialog closes
            viewModel.refreshData();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error editing task", e);
            showErrorAlert(resources.getString("error.title"), 
                        resources.getString("error.task.dialog.failed") + ": " + e.getMessage());
        }
    }
    
    /**
     * Handles adding a new meeting.
     */
    private void handleAddMeeting() {
        Project project = viewModel.getCurrentProject();
        if (project == null) {
            showErrorAlert(resources.getString("error.title"), 
                        resources.getString("error.project.required"));
            return;
        }
        
        try {
            // Load the meeting detail view
            ViewTuple<MeetingDetailMvvmView, MeetingDetailMvvmViewModel> viewTuple = 
                FluentViewLoader.fxmlView(MeetingDetailMvvmView.class)
                    .resourceBundle(resources)
                    .load();
            
            // Get the controller and view model
            MeetingDetailMvvmView viewController = viewTuple.getCodeBehind();
            
            // Initialize with a new meeting
            Meeting meeting = new Meeting();
            meeting.setDate(viewModel.getSelectedDate());
            meeting.setProject(project);
            
            viewController.initNewMeeting(project);
            
            // Create a new stage for the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle(resources.getString("meeting.new.title"));
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(datePicker.getScene().getWindow());
            dialogStage.setScene(new Scene(viewTuple.getView()));
            
            // Show the dialog and wait for it to close
            dialogStage.showAndWait();
            
            // Refresh data after dialog closes
            viewModel.refreshData();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error adding meeting", e);
            showErrorAlert(resources.getString("error.title"), 
                        resources.getString("error.meeting.dialog.failed") + ": " + e.getMessage());
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
            // Load the meeting detail view
            ViewTuple<MeetingDetailMvvmView, MeetingDetailMvvmViewModel> viewTuple = 
                FluentViewLoader.fxmlView(MeetingDetailMvvmView.class)
                    .resourceBundle(resources)
                    .load();
            
            // Get the controller and view model
            MeetingDetailMvvmView viewController = viewTuple.getCodeBehind();
            
            // Initialize with the existing meeting
            viewController.initExistingMeeting(meeting);
            
            // Create a new stage for the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle(resources.getString("meeting.edit.title"));
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(datePicker.getScene().getWindow());
            dialogStage.setScene(new Scene(viewTuple.getView()));
            
            // Show the dialog and wait for it to close
            dialogStage.showAndWait();
            
            // Refresh data after dialog closes
            viewModel.refreshData();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error editing meeting", e);
            showErrorAlert(resources.getString("error.title"), 
                        resources.getString("error.meeting.dialog.failed") + ": " + e.getMessage());
        }
    }
    
    /**
     * Handles taking attendance for a meeting.
     * 
     * @param meeting the meeting to take attendance for
     */
    private void handleTakeAttendance(Meeting meeting) {
        if (meeting == null) {
            return;
        }
        
        try {
            // Load the attendance view
            ViewTuple<AttendanceMvvmView, AttendanceMvvmViewModel> viewTuple = 
                FluentViewLoader.fxmlView(AttendanceMvvmView.class)
                    .resourceBundle(resources)
                    .load();
            
            // Get the controller and view model
            AttendanceMvvmView viewController = viewTuple.getCodeBehind();
            
            // Initialize with the meeting
            viewController.initWithMeeting(meeting);
            
            // Create a new stage for the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle(resources.getString("attendance.title"));
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(datePicker.getScene().getWindow());
            dialogStage.setScene(new Scene(viewTuple.getView()));
            dialogStage.setWidth(800);
            dialogStage.setHeight(600);
            
            // Show the dialog
            dialogStage.showAndWait();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error taking attendance", e);
            showErrorAlert(resources.getString("error.title"), 
                        resources.getString("error.attendance.view.failed") + ": " + e.getMessage());
        }
    }
    
    /**
     * Shows an error alert dialog.
     * 
     * @param title the title
     * @param message the message
     */
    private void showErrorAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Shows an information alert dialog.
     * 
     * @param title the title
     * @param message the message
     */
    private void showInfoAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.INFORMATION);
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
    public DailyMvvmViewModel getViewModel() {
        return viewModel;
    }
}