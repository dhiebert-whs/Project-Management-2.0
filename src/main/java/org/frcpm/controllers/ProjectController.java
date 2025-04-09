// src/main/java/org/frcpm/controllers/ProjectController.java

package org.frcpm.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import org.frcpm.binding.ViewModelBinding;
import org.frcpm.models.*;
import org.frcpm.services.DialogService;
import org.frcpm.services.ServiceFactory;
import org.frcpm.viewmodels.ProjectViewModel;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the project view using standardized MVVM pattern.
 */
public class ProjectController {

    private static final Logger LOGGER = Logger.getLogger(ProjectController.class.getName());

    // FXML UI components
    @FXML private Label projectNameLabel;
    @FXML private Label startDateLabel;
    @FXML private Label goalDateLabel;
    @FXML private Label deadlineLabel;
    @FXML private TextArea descriptionArea;
    @FXML private ProgressBar completionProgressBar;
    @FXML private Label completionLabel;
    @FXML private Label totalTasksLabel;
    @FXML private Label completedTasksLabel;
    @FXML private Label daysRemainingLabel;
    @FXML private TableView<Task> tasksTable;
    @FXML private TableColumn<Task, String> taskTitleColumn;
    @FXML private TableColumn<Task, String> taskSubsystemColumn;
    @FXML private TableColumn<Task, Integer> taskProgressColumn;
    @FXML private TableColumn<Task, LocalDate> taskDueDateColumn;
    @FXML private TableView<Milestone> milestonesTable;
    @FXML private TableColumn<Milestone, String> milestoneNameColumn;
    @FXML private TableColumn<Milestone, LocalDate> milestoneDateColumn;
    @FXML private TableView<Meeting> meetingsTable;
    @FXML private TableColumn<Meeting, LocalDate> meetingDateColumn;
    @FXML private TableColumn<Meeting, String> meetingTimeColumn;
    @FXML private Button addTaskButton;
    @FXML private Button addMilestoneButton;
    @FXML private Button scheduleMeetingButton;

    // ViewModel reference
    private ProjectViewModel viewModel = new ProjectViewModel();
    
    // Services
    private DialogService dialogService = ServiceFactory.getDialogService();

    /**
     * Initializes the controller.
     * This method is automatically called after the FXML file has been loaded.
     */
    @FXML
    private void initialize() {
        LOGGER.info("Initializing ProjectController");
        
        // Set up table columns
        setupTableColumns();
        
        // Set up bindings between UI controls and ViewModel properties
        setupBindings();
        
        // Set up row double-click handlers
        setupTableRowHandlers();
        
        // Set up error message display
        setupErrorHandling();
    }
    
    /**
     * Sets up error handling for the view model.
     */
    private void setupErrorHandling() {
        // Check for null UI components for testability
        if (viewModel == null) {
            LOGGER.warning("ViewModel not initialized - likely in test environment");
            return;
        }
        
        viewModel.errorMessageProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                showErrorAlert("Error", newValue);
                viewModel.errorMessageProperty().set("");
            }
        });
    }
    
    /**
     * Sets up bindings between UI controls and ViewModel properties.
     */
    private void setupBindings() {
        // Check for null UI components for testability
        if (projectNameLabel == null || startDateLabel == null || goalDateLabel == null || 
            deadlineLabel == null || descriptionArea == null || completionProgressBar == null || 
            completionLabel == null || totalTasksLabel == null || completedTasksLabel == null || 
            daysRemainingLabel == null || tasksTable == null || milestonesTable == null || 
            meetingsTable == null || addTaskButton == null || addMilestoneButton == null || 
            scheduleMeetingButton == null) {
            LOGGER.warning("UI components not initialized - likely in test environment");
            return;
        }

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
        
        // Bind tables to ViewModel collections
        tasksTable.setItems(viewModel.getTasks());
        milestonesTable.setItems(viewModel.getMilestones());
        meetingsTable.setItems(viewModel.getMeetings());
        
        // Bind buttons to commands
        ViewModelBinding.bindCommandButton(addTaskButton, viewModel.getAddTaskCommand());
        ViewModelBinding.bindCommandButton(addMilestoneButton, viewModel.getAddMilestoneCommand());
        ViewModelBinding.bindCommandButton(scheduleMeetingButton, viewModel.getScheduleMeetingCommand());
    }
    
    /**
     * Sets up the table columns.
     */
    private void setupTableColumns() {
        // Check for null UI components for testability
        if (taskTitleColumn == null || taskSubsystemColumn == null || taskProgressColumn == null || 
            taskDueDateColumn == null || milestoneNameColumn == null || milestoneDateColumn == null || 
            meetingDateColumn == null || meetingTimeColumn == null) {
            LOGGER.warning("Table columns not initialized - likely in test environment");
            return;
        }

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
        if (taskProgressColumn != null) {
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
        }
    }
    
    /**
     * Sets up the row double-click handlers for tables.
     */
    private void setupTableRowHandlers() {
        // Check for null UI components for testability
        if (tasksTable == null || milestonesTable == null || meetingsTable == null) {
            LOGGER.warning("Tables not initialized - likely in test environment");
            return;
        }

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
    }
    
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
     * Sets the project and loads its data.
     * 
     * @param project the project
     */
    public void setProject(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }
        
        viewModel.initExistingProject(project);
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
            FXMLLoader loader = createFXMLLoader("/fxml/TaskView.fxml");
            Parent dialogView = loader.load();
            
            // Create the dialog
            Stage dialogStage = createDialogStage("Edit Task", getWindowFromComponent(tasksTable), dialogView);
            
            // Get the controller
            TaskController controller = loader.getController();
            controller.initExistingTask(task);
            
            // Show the dialog and wait for result
            showAndWaitDialog(dialogStage);
            
            // Reload tasks
            viewModel.getLoadTasksCommand().execute();
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading task dialog", e);
            showErrorAlert("Error Editing Task", "Failed to open the task editing dialog.");
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
            FXMLLoader loader = createFXMLLoader("/fxml/MilestoneView.fxml");
            Parent dialogView = loader.load();
            
            // Create the dialog
            Stage dialogStage = createDialogStage("Edit Milestone", getWindowFromComponent(milestonesTable), dialogView);
            
            // Get the controller
            MilestoneController controller = loader.getController();
            controller.setMilestone(milestone);
            
            // Show the dialog and wait for result
            showAndWaitDialog(dialogStage);
            
            // Reload milestones using ViewModel command
            viewModel.getLoadMilestonesCommand().execute();
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading milestone dialog", e);
            showErrorAlert("Error Editing Milestone", "Failed to open the milestone editing dialog.");
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
            FXMLLoader loader = createFXMLLoader("/fxml/MeetingView.fxml");
            Parent dialogView = loader.load();
            
            // Create the dialog
            Stage dialogStage = createDialogStage("Edit Meeting", getWindowFromComponent(meetingsTable), dialogView);
            
            // Get the controller
            MeetingController controller = loader.getController();
            controller.setMeeting(meeting);
            
            // Show the dialog and wait for result
            showAndWaitDialog(dialogStage);
            
            // Reload meetings using ViewModel command
            viewModel.getLoadMeetingsCommand().execute();
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading meeting dialog", e);
            showErrorAlert("Error Editing Meeting", "Failed to open the meeting editing dialog.");
        }
    }
    
    /**
     * Gets the window from a UI component.
     * Protected for testability.
     * 
     * @param component the UI component
     * @return the window containing the component, or null if not in a scene
     */
    protected javafx.stage.Window getWindowFromComponent(Control component) {
        if (component == null || component.getScene() == null) {
            return null;
        }
        return component.getScene().getWindow();
    }
    
    /**
     * Creates an FXML loader.
     * Protected for testability.
     * 
     * @param fxmlPath the path to the FXML file
     * @return the FXMLLoader
     */
    protected FXMLLoader createFXMLLoader(String fxmlPath) {
        return new FXMLLoader(getClass().getResource(fxmlPath));
    }
    
    /**
     * Creates a dialog stage.
     * Protected for testability.
     * 
     * @param title the dialog title
     * @param owner the owner window
     * @param content the dialog content
     * @return the created Stage
     */
    protected Stage createDialogStage(String title, javafx.stage.Window owner, Parent content) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle(title);
        dialogStage.initModality(Modality.WINDOW_MODAL);
        if (owner != null) {
            dialogStage.initOwner(owner);
        }
        dialogStage.setScene(new Scene(content));
        return dialogStage;
    }
    
    /**
     * Shows a dialog and waits for it to be closed.
     * Protected for testability.
     * 
     * @param dialogStage the dialog stage to show
     */
    protected void showAndWaitDialog(Stage dialogStage) {
        try {
            if (dialogStage != null) {
                dialogStage.showAndWait();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error showing dialog", e);
        }
    }
    
    /**
     * Shows a choice dialog.
     * Protected for testability.
     * 
     * @param <T> the type of items in the dialog
     * @param title the dialog title
     * @param headerText the header text
     * @param contentText the content text
     * @param defaultChoice the default choice
     * @param choices the list of choices
     * @return an Optional containing the selected item or empty if cancelled
     */
    protected <T> Optional<T> showChoiceDialog(String title, String headerText, String contentText, T defaultChoice, List<T> choices) {
        try {
            ChoiceDialog<T> dialog = createChoiceDialog(defaultChoice, choices);
            dialog.setTitle(title);
            dialog.setHeaderText(headerText);
            dialog.setContentText(contentText);
            return dialog.showAndWait();
        } catch (Exception e) {
            LOGGER.log(Level.INFO, "Cannot show dialog in test environment", e);
            return Optional.empty();
        }
    }
    
    /**
     * Creates a choice dialog.
     * Protected for testability.
     * 
     * @param <T> the type of items in the dialog
     * @param defaultChoice the default choice
     * @param choices the list of choices
     * @return the created choice dialog
     */
    protected <T> ChoiceDialog<T> createChoiceDialog(T defaultChoice, List<T> choices) {
        return new ChoiceDialog<>(defaultChoice, choices);
    }
    
    /**
     * Shows an information alert dialog.
     * Protected for testability.
     * 
     * @param title the title
     * @param message the message
     */
    protected void showInfoAlert(String title, String message) {
        try {
            Alert alert = createAlert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText(title);
            alert.setContentText(message);
            alert.showAndWait();
        } catch (Exception e) {
            // This can happen in tests when not on FX thread
            LOGGER.log(Level.INFO, "Alert would show: Info - {0} - {1}", new Object[]{title, message});
        }
    }
    
    /**
     * Shows an error alert dialog.
     * Protected for testability.
     * 
     * @param title the title
     * @param message the message
     */
    protected void showErrorAlert(String title, String message) {
        try {
            Alert alert = createAlert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(title);
            alert.setContentText(message);
            alert.showAndWait();
        } catch (Exception e) {
            // This can happen in tests when not on FX thread
            LOGGER.log(Level.INFO, "Alert would show: Error - {0} - {1}", new Object[]{title, message});
        }
    }
    
    /**
     * Creates an alert dialog.
     * Protected for testability.
     * 
     * @param alertType the type of alert
     * @return the created alert
     */
    protected Alert createAlert(Alert.AlertType alertType) {
        return new Alert(alertType);
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
     * Sets the ViewModel (for testing).
     * 
     * @param viewModel the ViewModel
     */
    public void setViewModel(ProjectViewModel viewModel) {
        this.viewModel = viewModel;
        
        // Re-setup bindings and error handling after ViewModel change
        setupBindings();
        setupErrorHandling();
    }
    
    /**
     * Sets the dialog service (for testing).
     * 
     * @param dialogService the dialog service
     */
    public void setDialogService(DialogService dialogService) {
        this.dialogService = dialogService;
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
}