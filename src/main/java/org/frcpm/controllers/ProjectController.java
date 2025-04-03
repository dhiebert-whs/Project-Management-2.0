// src/main/java/org/frcpm/controllers/ProjectController.java

package org.frcpm.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.frcpm.binding.Command;
import org.frcpm.binding.ViewModelBinding;
import org.frcpm.models.*;
import org.frcpm.services.*;
import org.frcpm.viewmodels.ProjectViewModel;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the project view using MVVM pattern.
 */
public class ProjectController {

    private static final Logger LOGGER = Logger.getLogger(ProjectController.class.getName());

    @FXML
    Label projectNameLabel;

    @FXML
    Label startDateLabel;

    @FXML
    Label goalDateLabel;

    @FXML
    Label deadlineLabel;

    @FXML
    TextArea descriptionArea;

    @FXML
    private ProgressBar completionProgressBar;

    @FXML
    Label completionLabel;

    @FXML
    Label totalTasksLabel;

    @FXML
    Label completedTasksLabel;

    @FXML
    Label daysRemainingLabel;

    @FXML
    TableView<Task> tasksTable;

    @FXML
    private TableColumn<Task, String> taskTitleColumn;

    @FXML
    private TableColumn<Task, String> taskSubsystemColumn;

    @FXML
    private TableColumn<Task, Integer> taskProgressColumn;

    @FXML
    private TableColumn<Task, LocalDate> taskDueDateColumn;

    @FXML
    TableView<Milestone> milestonesTable;

    @FXML
    private TableColumn<Milestone, String> milestoneNameColumn;

    @FXML
    private TableColumn<Milestone, LocalDate> milestoneDateColumn;

    @FXML
    TableView<Meeting> meetingsTable;

    @FXML
    private TableColumn<Meeting, LocalDate> meetingDateColumn;

    @FXML
    private TableColumn<Meeting, String> meetingTimeColumn;

    @FXML
    private Button addTaskButton;

    @FXML
    private Button addMilestoneButton;

    @FXML
    private Button scheduleMeetingButton;

    // ViewModel
    private final ProjectViewModel viewModel = new ProjectViewModel();

    // Observable collections for tables
    private ObservableList<Task> taskList = FXCollections.observableArrayList();
    private ObservableList<Meeting> meetingList = FXCollections.observableArrayList();

    // Custom commands for UI actions
    private Command addTaskCommand;
    private Command addMilestoneCommand;
    private Command scheduleMeetingCommand;

    /**
     * Helper method to create a date cell factory that works with any entity type.
     * 
     * @param <T> the entity type for the table row
     * @return a callback that creates properly formatted date cells
     */
    private <T> Callback<TableColumn<T, LocalDate>, TableCell<T, LocalDate>> createDateCellFactory() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        return column -> new TableCell<T, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(dateFormatter));
                }
            }
        };
    }

    /**
     * Initializes the controller.
     */
    @FXML
    private void initialize() {
        LOGGER.info("Initializing ProjectController");

        // Initialize custom commands
        initializeCommands();

        // Set up task table columns
        taskTitleColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTitle()));

        taskSubsystemColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getSubsystem() != null ? cellData.getValue().getSubsystem().getName() : ""));

        taskProgressColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(
                cellData.getValue().getProgress()).asObject());

        taskDueDateColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(
                cellData.getValue().getEndDate()));

        // Set up milestone table columns
        milestoneNameColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));

        milestoneDateColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(
                cellData.getValue().getDate()));

        // Set up meeting table columns
        meetingDateColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(
                cellData.getValue().getDate()));

        meetingTimeColumn.setCellValueFactory(cellData -> {
            Meeting meeting = cellData.getValue();
            String timeStr = meeting.getStartTime() + " - " + meeting.getEndTime();
            return new javafx.beans.property.SimpleStringProperty(timeStr);
        });

        // Apply the date cell factory to all date columns
        taskDueDateColumn.setCellFactory(createDateCellFactory());
        milestoneDateColumn.setCellFactory(createDateCellFactory());
        meetingDateColumn.setCellFactory(createDateCellFactory());

        // Set up progress column renderer
        taskProgressColumn.setCellFactory(column -> new TableCell<Task, Integer>() {
            @Override
            protected void updateItem(Integer progress, boolean empty) {
                super.updateItem(progress, empty);
                if (empty || progress == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Create a progress bar for the cell
                    ProgressBar pb = new ProgressBar(progress / 100.0);
                    pb.setPrefWidth(80);

                    // Create a label for the percentage
                    Label label = new Label(progress + "%");
                    label.setPrefWidth(40);

                    // Combine in an HBox
                    HBox hbox = new HBox(5, pb, label);
                    setGraphic(hbox);
                    setText(null);
                }
            }
        });

        // Set up row double-click handlers
        tasksTable.setRowFactory(tv -> {
            TableRow<Task> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Task task = row.getItem();
                    handleEditTask(task);
                }
            });
            return row;
        });

        milestonesTable.setRowFactory(tv -> {
            TableRow<Milestone> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Milestone milestone = row.getItem();
                    handleEditMilestone(milestone);
                }
            });
            return row;
        });

        meetingsTable.setRowFactory(tv -> {
            TableRow<Meeting> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Meeting meeting = row.getItem();
                    handleEditMeeting(meeting);
                }
            });
            return row;
        });

        // Set up error message display
        viewModel.errorMessageProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                showErrorAlert("Error", newValue);
            }
        });

        // Set up bindings to ViewModel
        setupBindings();
    }

    /**
     * Initializes the custom commands for UI actions.
     */
    private void initializeCommands() {
        // Create commands for UI actions
        addTaskCommand = new Command(this::handleAddTaskAction);
        addMilestoneCommand = new Command(this::handleAddMilestoneAction);
        scheduleMeetingCommand = new Command(this::handleScheduleMeetingAction);
    }

    /**
     * Sets up bindings between UI controls and ViewModel properties.
     */
    private void setupBindings() {
        // Bind project details
        projectNameLabel.textProperty().bind(viewModel.projectNameProperty());
        descriptionArea.textProperty().bind(viewModel.projectDescriptionProperty());

        // Bind date labels
        startDateLabel.textProperty().bind(viewModel.startDateProperty().asString());
        goalDateLabel.textProperty().bind(viewModel.goalEndDateProperty().asString());
        deadlineLabel.textProperty().bind(viewModel.hardDeadlineProperty().asString());

        // Bind progress indicators
        completionProgressBar.progressProperty().bind(viewModel.completionPercentageProperty().divide(100.0));
        completionLabel.textProperty().bind(viewModel.completionPercentageProperty().asString("%.1f%%"));
        totalTasksLabel.textProperty().bind(viewModel.totalTasksProperty().asString());
        completedTasksLabel.textProperty().bind(viewModel.completedTasksProperty().asString());
        daysRemainingLabel.textProperty().bind(viewModel.daysUntilGoalProperty().asString("%d days until goal"));

        // Bind tables
        tasksTable.setItems(taskList);
        milestonesTable.setItems(viewModel.getMilestones());
        meetingsTable.setItems(meetingList);

        // Bind buttons to commands
        ViewModelBinding.bindCommandButton(addTaskButton, addTaskCommand);
        ViewModelBinding.bindCommandButton(addMilestoneButton, addMilestoneCommand);
        ViewModelBinding.bindCommandButton(scheduleMeetingButton, scheduleMeetingCommand);
    }

    /**
     * Sets the project and loads its data.
     * 
     * @param project the project
     */
    public void setProject(Project project) {
        viewModel.initExistingProject(project);
        loadProjectData();
    }

    /**
     * Loads the project data.
     */
    private void loadProjectData() {
        Project project = viewModel.getSelectedProject();
        if (project == null) {
            return;
        }

        // Load tasks and meetings
        loadTasks();
        loadMeetings();
    }

    /**
     * Loads the tasks for the project.
     */
    private void loadTasks() {
        try {
            Project project = viewModel.getSelectedProject();
            if (project == null) {
                return;
            }

            List<Task> tasks = ServiceFactory.getTaskService().findByProject(project);
            taskList.setAll(tasks);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading tasks", e);
            showErrorAlert("Error Loading Tasks", "Failed to load tasks for the project.");
        }
    }

    /**
     * Loads the meetings for the project.
     */
    private void loadMeetings() {
        try {
            Project project = viewModel.getSelectedProject();
            if (project == null) {
                return;
            }

            List<Meeting> meetings = ServiceFactory.getMeetingService().findByProject(project);
            meetingList.setAll(meetings);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading meetings", e);
            showErrorAlert("Error Loading Meetings", "Failed to load meetings for the project.");
        }
    }

    /**
     * Handles the add task action.
     * Bound to the addTaskCommand.
     */
    private void handleAddTaskAction() {
        try {
            Project project = viewModel.getSelectedProject();
            if (project == null) {
                showErrorAlert("No Project", "No project is selected.");
                return;
            }

            // First, select a subsystem
            List<Subsystem> subsystems = ServiceFactory.getSubsystemService().findAll();

            if (subsystems.isEmpty()) {
                showErrorAlert("No Subsystems", "Please create at least one subsystem before adding tasks.");
                return;
            }

            // Show subsystem selection dialog
            ChoiceDialog<Subsystem> subsystemDialog = new ChoiceDialog<>(subsystems.get(0), subsystems);
            subsystemDialog.setTitle("Select Subsystem");
            subsystemDialog.setHeaderText("Select a subsystem for the new task");
            subsystemDialog.setContentText("Subsystem:");

            Optional<Subsystem> subsystemResult = subsystemDialog.showAndWait();
            if (!subsystemResult.isPresent()) {
                return; // User canceled
            }

            Subsystem selectedSubsystem = subsystemResult.get();

            // Now load the task dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TaskView.fxml"));
            Parent dialogView = loader.load();

            // Create the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("New Task");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(addTaskButton.getScene().getWindow());
            dialogStage.setScene(new Scene(dialogView));

            // Get the controller
            TaskController controller = loader.getController();
            Task newTask = new Task("", project, selectedSubsystem);
            controller.initNewTask(newTask);

            // Show the dialog and wait for result
            dialogStage.showAndWait();

            // Reload tasks to show the new one
            loadTasks();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading task dialog", e);
            showErrorAlert("Error Creating Task", "Failed to open the task creation dialog.");
        }
    }

    /**
     * Handles adding a new task.
     * Used for event handlers.
     * 
     * @param event the action event
     */
    @FXML
    public void handleAddTask(ActionEvent event) {
        addTaskCommand.execute();
    }

    /**
     * Handles editing a task.
     * 
     * @param task the task to edit
     */
    public void handleEditTask(Task task) {
        if (task == null) {
            return;
        }

        try {
            // Load the task dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TaskView.fxml"));
            Parent dialogView = loader.load();

            // Create the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Task");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(tasksTable.getScene().getWindow());
            dialogStage.setScene(new Scene(dialogView));

            // Get the controller
            TaskController controller = loader.getController();
            controller.initExistingTask(task);

            // Show the dialog and wait for result
            dialogStage.showAndWait();

            // Reload tasks to show the updated one
            loadTasks();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading task dialog", e);
            showErrorAlert("Error Editing Task", "Failed to open the task editing dialog.");
        }
    }

    /**
     * Handles the add milestone action.
     * Bound to the addMilestoneCommand.
     */
    private void handleAddMilestoneAction() {
        try {
            Project project = viewModel.getSelectedProject();
            if (project == null) {
                showErrorAlert("No Project", "No project is selected.");
                return;
            }

            // Load the milestone dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MilestoneView.fxml"));
            Parent dialogView = loader.load();

            // Create the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("New Milestone");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(addMilestoneButton.getScene().getWindow());
            dialogStage.setScene(new Scene(dialogView));

            // Get the controller
            MilestoneController controller = loader.getController();
            controller.setNewMilestone(project);

            // Show the dialog and wait for result
            dialogStage.showAndWait();

            // Reload milestones using ViewModel command
            viewModel.getLoadMilestonesCommand().execute();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading milestone dialog", e);
            showErrorAlert("Error Creating Milestone", "Failed to open the milestone creation dialog.");
        }
    }

    /**
     * Handles adding a new milestone.
     * Used for event handlers.
     * 
     * @param event the action event
     */
    @FXML
    public void handleAddMilestone(ActionEvent event) {
        addMilestoneCommand.execute();
    }

    /**
     * Handles editing a milestone.
     * 
     * @param milestone the milestone to edit
     */
    public void handleEditMilestone(Milestone milestone) {
        if (milestone == null) {
            return;
        }

        try {
            // Load the milestone dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MilestoneView.fxml"));
            Parent dialogView = loader.load();

            // Create the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Milestone");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(milestonesTable.getScene().getWindow());
            dialogStage.setScene(new Scene(dialogView));

            // Get the controller
            MilestoneController controller = loader.getController();
            controller.setMilestone(milestone);

            // Show the dialog and wait for result
            dialogStage.showAndWait();

            // Reload milestones using ViewModel command
            viewModel.getLoadMilestonesCommand().execute();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading milestone dialog", e);
            showErrorAlert("Error Editing Milestone", "Failed to open the milestone editing dialog.");
        }
    }

    /**
     * Handles the schedule meeting action.
     * Bound to the scheduleMeetingCommand.
     */
    private void handleScheduleMeetingAction() {
        try {
            Project project = viewModel.getSelectedProject();
            if (project == null) {
                showErrorAlert("No Project", "No project is selected.");
                return;
            }

            // Load the meeting dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MeetingView.fxml"));
            Parent dialogView = loader.load();

            // Create the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Schedule Meeting");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(scheduleMeetingButton.getScene().getWindow());
            dialogStage.setScene(new Scene(dialogView));

            // Get the controller
            MeetingController controller = loader.getController();
            controller.setNewMeeting(project);

            // Show the dialog and wait for result
            dialogStage.showAndWait();

            // Reload meetings to show the new one
            loadMeetings();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading meeting dialog", e);
            showErrorAlert("Error Scheduling Meeting", "Failed to open the meeting scheduling dialog.");
        }
    }

    /**
     * Handles scheduling a new meeting.
     * Used for event handlers.
     * 
     * @param event the action event
     */
    @FXML
    public void handleScheduleMeeting(ActionEvent event) {
        scheduleMeetingCommand.execute();
    }

    /**
     * Handles editing a meeting.
     * 
     * @param meeting the meeting to edit
     */
    public void handleEditMeeting(Meeting meeting) {
        if (meeting == null) {
            return;
        }

        try {
            // Load the meeting dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MeetingView.fxml"));
            Parent dialogView = loader.load();

            // Create the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Meeting");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(meetingsTable.getScene().getWindow());
            dialogStage.setScene(new Scene(dialogView));

            // Get the controller
            MeetingController controller = loader.getController();
            controller.setMeeting(meeting);

            // Show the dialog and wait for result
            dialogStage.showAndWait();

            // Reload meetings to show the updated one
            loadMeetings();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading meeting dialog", e);
            showErrorAlert("Error Editing Meeting", "Failed to open the meeting editing dialog.");
        }
    }

    /**
     * Shows an information alert dialog.
     * 
     * @param title   the title
     * @param message the message
     */
    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows an error alert dialog.
     * 
     * @param title   the title
     * @param message the message
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Gets the ViewModel.
     * 
     * @return the ViewModel
     */
    public ProjectViewModel getViewModel() {
        return viewModel;
    }

    /**
     * Gets the project from the ViewModel.
     * 
     * @return the selected project
     */
    public Project getProject() {
        return viewModel.getSelectedProject();
    }

    // Getters for UI elements (for testing)

    public Label getProjectNameLabel() {
        return projectNameLabel;
    }

    public Label getStartDateLabel() {
        return startDateLabel;
    }

    public Label getGoalDateLabel() {
        return goalDateLabel;
    }

    public Label getDeadlineLabel() {
        return deadlineLabel;
    }

    public TextArea getDescriptionArea() {
        return descriptionArea;
    }

    public ProgressBar getCompletionProgressBar() {
        return completionProgressBar;
    }

    public Label getCompletionLabel() {
        return completionLabel;
    }

    public Label getTotalTasksLabel() {
        return totalTasksLabel;
    }

    public Label getCompletedTasksLabel() {
        return completedTasksLabel;
    }

    public Label getDaysRemainingLabel() {
        return daysRemainingLabel;
    }

    public TableView<Task> getTasksTable() {
        return tasksTable;
    }

    public TableView<Milestone> getMilestonesTable() {
        return milestonesTable;
    }

    public TableView<Meeting> getMeetingsTable() {
        return meetingsTable;
    }

    public Button getAddTaskButton() {
        return addTaskButton;
    }

    public Button getAddMilestoneButton() {
        return addMilestoneButton;
    }

    public Button getScheduleMeetingButton() {
        return scheduleMeetingButton;
    }

    public ObservableList<Task> getTaskList() {
        return taskList;
    }

    public ObservableList<Meeting> getMeetingList() {
        return meetingList;
    }

    /**
     * For testing purposes.
     */
    public void testInitialize() {
        initialize();
    }

    /**
     * For testing purposes.
     */
    public void testLoadProjectData() {
        loadProjectData();
    }

    /**
     * For testing purposes.
     */
    public void testLoadTasks() {
        loadTasks();
    }

    /**
     * For testing purposes.
     */
    public void testLoadMeetings() {
        loadMeetings();
    }
}