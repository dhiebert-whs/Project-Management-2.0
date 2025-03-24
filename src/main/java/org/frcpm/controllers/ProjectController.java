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
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.layout.HBox;
import org.frcpm.models.*;
import org.frcpm.services.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the project view.
 */
public class ProjectController {
    
    private static final Logger LOGGER = Logger.getLogger(ProjectController.class.getName());
    
    @FXML
    private Label projectNameLabel;
    
    @FXML
    private Label startDateLabel;
    
    @FXML
    private Label goalDateLabel;
    
    @FXML
    private Label deadlineLabel;
    
    @FXML
    private TextArea descriptionArea;
    
    @FXML
    private ProgressBar completionProgressBar;
    
    @FXML
    private Label completionLabel;
    
    @FXML
    private Label totalTasksLabel;
    
    @FXML
    private Label completedTasksLabel;
    
    @FXML
    private Label daysRemainingLabel;
    
    @FXML
    private TableView<Task> tasksTable;
    
    @FXML
    private TableColumn<Task, String> taskTitleColumn;
    
    @FXML
    private TableColumn<Task, String> taskSubsystemColumn;
    
    @FXML
    private TableColumn<Task, Integer> taskProgressColumn;
    
    @FXML
    private TableColumn<Task, LocalDate> taskDueDateColumn;
    
    @FXML
    private TableView<Milestone> milestonesTable;
    
    @FXML
    private TableColumn<Milestone, String> milestoneNameColumn;
    
    @FXML
    private TableColumn<Milestone, LocalDate> milestoneDateColumn;
    
    @FXML
    private TableView<Meeting> meetingsTable;
    
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
        
        // Format date columns
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        
        taskDueDateColumn.setCellFactory(column -> new TableCell<Task, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(dateFormatter));
                }
            }
        });
        
        // Apply the same formatter to other date columns
        milestoneDateColumn.setCellFactory(taskDueDateColumn.getCellFactory());
        meetingDateColumn.setCellFactory(taskDueDateColumn.getCellFactory());
        
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
    private void handleAddTask(ActionEvent event) {
        // This will be implemented in Phase 3
        showNotImplementedAlert("Add Task");
    }
    
    /**
     * Handles editing a task.
     * 
     * @param task the task to edit
     */
    private void handleEditTask(Task task) {
        // This will be implemented in Phase 3
        showNotImplementedAlert("Edit Task");
    }
    
    /**
     * Handles adding a new milestone.
     * 
     * @param event the action event
     */
    private void handleAddMilestone(ActionEvent event) {
        // This will be implemented in Phase 3
        showNotImplementedAlert("Add Milestone");
    }
    
    /**
     * Handles editing a milestone.
     * 
     * @param milestone the milestone to edit
     */
    private void handleEditMilestone(Milestone milestone) {
        // This will be implemented in Phase 3
        showNotImplementedAlert("Edit Milestone");
    }
    
    /**
     * Handles scheduling a new meeting.
     * 
     * @param event the action event
     */
    private void handleScheduleMeeting(ActionEvent event) {
        // This will be implemented in Phase 3
        showNotImplementedAlert("Schedule Meeting");
    }
    
    /**
     * Handles editing a meeting.
     * 
     * @param meeting the meeting to edit
     */
    private void handleEditMeeting(Meeting meeting) {
        // This will be implemented in Phase 3
        showNotImplementedAlert("Edit Meeting");
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
}