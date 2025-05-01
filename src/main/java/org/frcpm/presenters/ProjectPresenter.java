package org.frcpm.presenters;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.frcpm.binding.ViewModelBinding;
import org.frcpm.di.DialogFactory;
import org.frcpm.di.ServiceProvider;
import org.frcpm.di.ViewLoader;
import org.frcpm.models.*;
import org.frcpm.services.*;
import org.frcpm.viewmodels.ProjectViewModel;
import org.frcpm.views.MeetingView;
import org.frcpm.views.MilestoneView;
import org.frcpm.views.TaskView;

import javax.inject.Inject;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Presenter for the project detail view.
 * This follows the AfterburnerFX presenter convention.
 */
public class ProjectPresenter implements Initializable {
    
    private static final Logger LOGGER = Logger.getLogger(ProjectPresenter.class.getName());
    
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
    
        
    @Inject
    private ProjectService projectService;
    
    @Inject
    private TaskService taskService;
    
    @Inject
    private MilestoneService milestoneService;
    
    @Inject
    private MeetingService meetingService;
    
    @Inject
    private DialogService dialogService;
    
    @Inject
    private SubsystemService subsystemService;
    
    @Inject
    private ProjectViewModel viewModel;
    
    private Project project;

    // Resource bundle
    private ResourceBundle resources;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initializing ProjectPresenter");
        
        // No need to create view model manually - it's injected
        
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
        viewModel.errorMessageProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                dialogService.showErrorAlert("Error", newValue);
                viewModel.clearErrorMessage();
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
                        javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox(5, pb, label);
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
    private <T> javafx.util.Callback<TableColumn<T, LocalDate>, TableCell<T, LocalDate>> createDateCellFactory() {
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
        
        this.project = project;
        viewModel.initExistingProject(project);
    }
    
    /**
     * Initializes a new project for creation.
     */
    public void initNewProject() {
        viewModel.initNewProject();
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
            // Use ViewLoader to show dialog with localized title
            TaskPresenter presenter = ViewLoader.showDialog(
                    TaskView.class, 
                    resources.getString("task.edit.title"), 
                    tasksTable.getScene().getWindow());
            
            // Initialize task in presenter
            if (presenter != null) {
                presenter.initExistingTask(task);
                
                // Reload tasks
                viewModel.getLoadTasksCommand().execute();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error editing task", e);
            dialogService.showErrorAlert(
                resources.getString("error.title"), 
                resources.getString("error.task.edit.failed"));
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
            // Use ViewLoader to show dialog with localized title
            MilestonePresenter presenter = ViewLoader.showDialog(
                    MilestoneView.class, 
                    resources.getString("milestone.edit.title"), 
                    milestonesTable.getScene().getWindow());
            
            // Initialize milestone in presenter
            if (presenter != null) {
                presenter.initExistingMilestone(milestone);
                
                // Reload milestones
                viewModel.getLoadMilestonesCommand().execute();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error editing milestone", e);
            dialogService.showErrorAlert(
                resources.getString("error.title"), 
                resources.getString("error.milestone.edit.failed"));
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
            // Use ViewLoader to show dialog with localized title
            MeetingPresenter presenter = ViewLoader.showDialog(
                    MeetingView.class, 
                    resources.getString("meeting.edit.title"), 
                    meetingsTable.getScene().getWindow());
            
            // Initialize meeting in presenter
            if (presenter != null) {
                presenter.initExistingMeeting(meeting);
                
                // Reload meetings
                viewModel.getLoadMeetingsCommand().execute();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error editing meeting", e);
            dialogService.showErrorAlert(
                resources.getString("error.title"), 
                resources.getString("error.meeting.edit.failed"));
        }
    }
    
    /**
     * Gets the project.
     * 
     * @return the project
     */
    public Project getProject() {
        return project;
    }
}