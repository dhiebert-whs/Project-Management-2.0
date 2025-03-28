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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.frcpm.models.*;
import org.frcpm.services.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the project view.
 */
public class ProjectController {
    
    private static final Logger LOGGER = Logger.getLogger(ProjectController.class.getName());
    
    @FXML Label projectNameLabel;
    
    @FXML Label startDateLabel;
    
    @FXML Label goalDateLabel;
    
    @FXML Label deadlineLabel;
    
    @FXML TextArea descriptionArea;
    
    @FXML
    private ProgressBar completionProgressBar;
    
    @FXML Label completionLabel;
    
    @FXML Label totalTasksLabel;
    
    @FXML Label completedTasksLabel;
    
    @FXML Label daysRemainingLabel;
    
    @FXML TableView<Task> tasksTable;
    
    @FXML
    private TableColumn<Task, String> taskTitleColumn;
    
    @FXML
    private TableColumn<Task, String> taskSubsystemColumn;
    
    @FXML
    private TableColumn<Task, Integer> taskProgressColumn;
    
    @FXML
    private TableColumn<Task, LocalDate> taskDueDateColumn;
    
    @FXML TableView<Milestone> milestonesTable;
    
    @FXML
    private TableColumn<Milestone, String> milestoneNameColumn;
    
    @FXML
    private TableColumn<Milestone, LocalDate> milestoneDateColumn;
    
    @FXML TableView<Meeting> meetingsTable;
    
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
    
    private Project project;
    private final ProjectService projectService = ServiceFactory.getProjectService();
    private final TaskService taskService = ServiceFactory.getTaskService();
    private final MilestoneService milestoneService = ServiceFactory.getMilestoneService();
    private final MeetingService meetingService = ServiceFactory.getMeetingService();
    
    private ObservableList<Task> taskList = FXCollections.observableArrayList();
    private ObservableList<Milestone> milestoneList = FXCollections.observableArrayList();
    private ObservableList<Meeting> meetingList = FXCollections.observableArrayList();
    
    /**
     * Helper method to create a date cell factory that works with any entity type.
     * This allows us to reuse the same formatting logic across different table columns.
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
        
        // Set up task table columns
        taskTitleColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTitle()));
        
        taskSubsystemColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getSubsystem() != null ? 
                cellData.getValue().getSubsystem().getName() : ""));
        
        taskProgressColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleIntegerProperty(
                cellData.getValue().getProgress()).asObject());
        
        taskDueDateColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(
                cellData.getValue().getEndDate()));
        
        // Set up milestone table columns
        milestoneNameColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        
        milestoneDateColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(
                cellData.getValue().getDate()));
        
        // Set up meeting table columns
        meetingDateColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(
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
        
        // Set up button actions
        addTaskButton.setOnAction(this::handleAddTask);
        addMilestoneButton.setOnAction(this::handleAddMilestone);
        scheduleMeetingButton.setOnAction(this::handleScheduleMeeting);
    }
    
    /**
     * Sets the project and loads its data.
     * 
     * @param project the project
     */
    public void setProject(Project project) {
        this.project = project;
        loadProjectData();
    }
    
    /**
     * Loads the project data.
     */
    private void loadProjectData() {
        if (project == null) {
            return;
        }
        
        // Set project details
        projectNameLabel.setText(project.getName());
        startDateLabel.setText(project.getStartDate().toString());
        goalDateLabel.setText(project.getGoalEndDate().toString());
        deadlineLabel.setText(project.getHardDeadline().toString());
        descriptionArea.setText(project.getDescription());
        
        // Load project summary
        Map<String, Object> summary = projectService.getProjectSummary(project.getId());
        
        // Update progress indicators
        double completionPercentage = (double) summary.get("completionPercentage");
        completionProgressBar.setProgress(completionPercentage / 100.0);
        completionLabel.setText(String.format("%.1f%%", completionPercentage));
        
        // Update task counts
        totalTasksLabel.setText(summary.get("totalTasks").toString());
        completedTasksLabel.setText(summary.get("completedTasks").toString());
        
        // Update days remaining
        long daysUntilGoal = (long) summary.get("daysUntilGoal");
        daysRemainingLabel.setText(daysUntilGoal + " days until goal");
        
        // Load tasks
        loadTasks();
        
        // Load milestones
        loadMilestones();
        
        // Load meetings
        loadMeetings();
    }
    
    /**
     * Loads the tasks for the project.
     */
    private void loadTasks() {
        try {
            List<Task> tasks = taskService.findByProject(project);
            taskList.setAll(tasks);
            tasksTable.setItems(taskList);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading tasks", e);
            showErrorAlert("Error Loading Tasks", "Failed to load tasks for the project.");
        }
    }
    
    /**
     * Loads the milestones for the project.
     */
    private void loadMilestones() {
        try {
            List<Milestone> milestones = milestoneService.findByProject(project);
            milestoneList.setAll(milestones);
            milestonesTable.setItems(milestoneList);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading milestones", e);
            showErrorAlert("Error Loading Milestones", "Failed to load milestones for the project.");
        }
    }
    
    /**
     * Loads the meetings for the project.
     */
    private void loadMeetings() {
        try {
            List<Meeting> meetings = meetingService.findByProject(project);
            meetingList.setAll(meetings);
            meetingsTable.setItems(meetingList);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading meetings", e);
            showErrorAlert("Error Loading Meetings", "Failed to load meetings for the project.");
        }
    }
    
   /**
 * Handles adding a new task.
 * 
 * @param event the action event
 */
public void handleAddTask(ActionEvent event) {
    try {
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
        dialogStage.initOwner(((Node) event.getSource()).getScene().getWindow());
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
     * Handles adding a new milestone.
     * 
     * @param event the action event
     */
    @FXML
    public void handleAddMilestone(ActionEvent event) {
        try {
            // Load the milestone dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MilestoneView.fxml"));
            Parent dialogView = loader.load();
            
            // Create the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("New Milestone");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(((Node) event.getSource()).getScene().getWindow());
            dialogStage.setScene(new Scene(dialogView));
            
            // Get the controller
            MilestoneController controller = loader.getController();
            controller.setNewMilestone(project);
            
            // Show the dialog and wait for result
            dialogStage.showAndWait();
            
            // Reload milestones to show the new one
            loadMilestones();
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading milestone dialog", e);
            showErrorAlert("Error Creating Milestone", "Failed to open the milestone creation dialog.");
        }
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
            
            // Reload milestones to show the updated one
            loadMilestones();
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading milestone dialog", e);
            showErrorAlert("Error Editing Milestone", "Failed to open the milestone editing dialog.");
        }
    }
    
    /**
     * Handles scheduling a new meeting.
     * 
     * @param event the action event
     */
    @FXML
    public void handleScheduleMeeting(ActionEvent event) {
        try {
            // Load the meeting dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MeetingView.fxml"));
            Parent dialogView = loader.load();
            
            // Create the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Schedule Meeting");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(((Node) event.getSource()).getScene().getWindow());
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
     * @param title the title
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
     * Shows a "Not Implemented" alert dialog.
     * 
     * @param feature the feature that is not implemented
     */
    private void showNotImplementedAlert(String feature) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Not Implemented");
        alert.setHeaderText(feature);
        alert.setContentText("This feature is not yet implemented in the current version.");
        alert.showAndWait();
    }
    
    /**
     * Shows an error alert dialog.
     * 
     * @param title the title
     * @param message the message
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    //Getter for project
    public Project getProject() {
        return project;
    }

    /**
     * Gets the project name label.
     * 
     * @return the project name label
     */
    public Label getProjectNameLabel() {
        return projectNameLabel;
    }

    /**
     * Gets the start date label.
     * 
     * @return the start date label
     */
    public Label getStartDateLabel() {
        return startDateLabel;
    }

    /**
     * Gets the goal date label.
     * 
     * @return the goal date label
     */
    public Label getGoalDateLabel() {
        return goalDateLabel;
    }

    /**
     * Gets the deadline label.
     * 
     * @return the deadline label
     */
    public Label getDeadlineLabel() {
        return deadlineLabel;
    }

    /**
     * Gets the description area.
     * 
     * @return the description area
     */
    public TextArea getDescriptionArea() {
        return descriptionArea;
    }

    /**
     * Gets the completion progress bar.
     * 
     * @return the completion progress bar
     */
    public ProgressBar getCompletionProgressBar() {
        return completionProgressBar;
    }

    /**
     * Gets the completion label.
     * 
     * @return the completion label
     */
    public Label getCompletionLabel() {
        return completionLabel;
    }

    /**
     * Gets the total tasks label.
     * 
     * @return the total tasks label
     */
    public Label getTotalTasksLabel() {
        return totalTasksLabel;
    }

    /**
     * Gets the completed tasks label.
     * 
     * @return the completed tasks label
     */
    public Label getCompletedTasksLabel() {
        return completedTasksLabel;
    }

    /**
     * Gets the days remaining label.
     * 
     * @return the days remaining label
     */
    public Label getDaysRemainingLabel() {
        return daysRemainingLabel;
    }

    /**
     * Gets the tasks table.
     * 
     * @return the tasks table
     */
    public TableView<Task> getTasksTable() {
        return tasksTable;
    }

    /**
     * Gets the task title column.
     * 
     * @return the task title column
     */
    public TableColumn<Task, String> getTaskTitleColumn() {
        return taskTitleColumn;
    }

    /**
     * Gets the task subsystem column.
     * 
     * @return the task subsystem column
     */
    public TableColumn<Task, String> getTaskSubsystemColumn() {
        return taskSubsystemColumn;
    }

    /**
     * Gets the task progress column.
     * 
     * @return the task progress column
     */
    public TableColumn<Task, Integer> getTaskProgressColumn() {
        return taskProgressColumn;
    }

    /**
     * Gets the task due date column.
     * 
     * @return the task due date column
     */
    public TableColumn<Task, LocalDate> getTaskDueDateColumn() {
        return taskDueDateColumn;
    }

    /**
     * Gets the milestones table.
     * 
     * @return the milestones table
     */
    public TableView<Milestone> getMilestonesTable() {
        return milestonesTable;
    }

    /**
     * Gets the milestone name column.
     * 
     * @return the milestone name column
     */
    public TableColumn<Milestone, String> getMilestoneNameColumn() {
        return milestoneNameColumn;
    }

    /**
     * Gets the milestone date column.
     * 
     * @return the milestone date column
     */
    public TableColumn<Milestone, LocalDate> getMilestoneDateColumn() {
        return milestoneDateColumn;
    }

    /**
     * Gets the meetings table.
     * 
     * @return the meetings table
     */
    public TableView<Meeting> getMeetingsTable() {
        return meetingsTable;
    }

    /**
     * Gets the meeting date column.
     * 
     * @return the meeting date column
     */
    public TableColumn<Meeting, LocalDate> getMeetingDateColumn() {
        return meetingDateColumn;
    }

    /**
     * Gets the meeting time column.
     * 
     * @return the meeting time column
     */
    public TableColumn<Meeting, String> getMeetingTimeColumn() {
        return meetingTimeColumn;
    }

    /**
     * Gets the add task button.
     * 
     * @return the add task button
     */
    public Button getAddTaskButton() {
        return addTaskButton;
    }

    /**
     * Gets the add milestone button.
     * 
     * @return the add milestone button
     */
    public Button getAddMilestoneButton() {
        return addMilestoneButton;
    }

    /**
     * Gets the schedule meeting button.
     * 
     * @return the schedule meeting button
     */
    public Button getScheduleMeetingButton() {
        return scheduleMeetingButton;
    }

    /**
     * Gets the project service.
     * 
     * @return the project service
     */
    public ProjectService getProjectService() {
        return projectService;
    }

    /**
     * Gets the task service.
     * 
     * @return the task service
     */
    public TaskService getTaskService() {
        return taskService;
    }

    /**
     * Gets the milestone service.
     * 
     * @return the milestone service
     */
    public MilestoneService getMilestoneService() {
        return milestoneService;
    }

    /**
     * Gets the meeting service.
     * 
     * @return the meeting service
     */
    public MeetingService getMeetingService() {
        return meetingService;
    }

    /**
     * Gets the task list.
     * 
     * @return the task list
     */
    public ObservableList<Task> getTaskList() {
        return taskList;
    }

    /**
     * Gets the milestone list.
     * 
     * @return the milestone list
     */
    public ObservableList<Milestone> getMilestoneList() {
        return milestoneList;
    }

    /**
     * Gets the meeting list.
     * 
     * @return the meeting list
     */
    public ObservableList<Meeting> getMeetingList() {
        return meetingList;
    }

    /**
     * Public method to access initialize for testing.
     */
    public void testInitialize() {
        initialize();
    }

    /**
     * Public method to access loadProjectData for testing.
     */
    public void testLoadProjectData() {
        loadProjectData();
    }

    /**
     * Public method to access loadTasks for testing.
     */
    public void testLoadTasks() {
        loadTasks();
    }

    /**
     * Public method to access loadMilestones for testing.
     */
    public void testLoadMilestones() {
        loadMilestones();
    }

    /**
     * Public method to access loadMeetings for testing.
     */
    public void testLoadMeetings() {
        loadMeetings();
    }

    /**
     * Public method to access createDateCellFactory for testing.
     * 
     * @param <T> the type parameter
     * @return the date cell factory
     */
    public <T> Callback<TableColumn<T, LocalDate>, TableCell<T, LocalDate>> testCreateDateCellFactory() {
        return createDateCellFactory();
    }

    /**
     * Public method to access showErrorAlert for testing.
     * 
     * @param title the title
     * @param message the message
     */
    public void testShowErrorAlert(String title, String message) {
        showErrorAlert(title, message);
    }

    /**
     * Public method to access showInfoAlert for testing.
     * 
     * @param title the title
     * @param message the message
     */
    public void testShowInfoAlert(String title, String message) {
        showInfoAlert(title, message);
    }

    /**
     * Public method to access showNotImplementedAlert for testing.
     * 
     * @param feature the feature
     */
    public void testShowNotImplementedAlert(String feature) {
        showNotImplementedAlert(feature);
    }

    /**
     * Public method to access handleEditTask for testing.
     * 
     * @param task the task to edit
     */
    public void testHandleEditTask(Task task) {
        handleEditTask(task);
    }

    /**
     * Public method to access handleEditMilestone for testing.
     * 
     * @param milestone the milestone to edit
     */
    public void testHandleEditMilestone(Milestone milestone) {
        handleEditMilestone(milestone);
    }

    /**
     * Public method to access handleEditMeeting for testing.
     * 
     * @param meeting the meeting to edit
     */
    public void testHandleEditMeeting(Meeting meeting) {
        handleEditMeeting(meeting);
    }

    
}