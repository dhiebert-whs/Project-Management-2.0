package org.frcpm.controllers;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.frcpm.binding.ViewModelBinding;
import org.frcpm.models.Subteam;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.viewmodels.SubsystemViewModel;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for subsystem management using MVVM pattern.
 */
public class SubsystemController {

    private static final Logger LOGGER = Logger.getLogger(SubsystemController.class.getName());

    // Main form fields
    @FXML
    private TextField nameField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private ComboBox<Subsystem.Status> statusComboBox;

    @FXML
    private ComboBox<Subteam> responsibleSubteamComboBox;

    // Task table
    @FXML
    private TableView<Task> tasksTable;

    @FXML
    private TableColumn<Task, String> taskTitleColumn;

    @FXML
    private TableColumn<Task, Integer> taskProgressColumn;

    @FXML
    private TableColumn<Task, LocalDate> taskDueDateColumn;

    // Summary fields
    @FXML
    private Label totalTasksLabel;

    @FXML
    private Label completedTasksLabel;

    @FXML
    private Label completionPercentageLabel;

    @FXML
    private ProgressBar completionProgressBar;

    // Buttons
    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Button addTaskButton;

    @FXML
    private Button viewTaskButton;

    // ViewModel
    private final SubsystemViewModel viewModel = new SubsystemViewModel();

    /**
     * Initializes the controller.
     * This method is automatically called after the FXML has been loaded.
     */
    @FXML
    private void initialize() {
        LOGGER.info("Initializing SubsystemController");

        // Initialize status combo box
        statusComboBox.setItems(FXCollections.observableArrayList(Arrays.asList(Subsystem.Status.values())));

        // Initialize responsible subteam combo box
        responsibleSubteamComboBox.setItems(viewModel.getAvailableSubteams());
        
        // Set up tasks table columns
        setupTasksTable();

        // Set up bindings
        setupBindings();
    }

    /**
     * Sets up the tasks table.
     */
    private void setupTasksTable() {
        taskTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        
        taskProgressColumn.setCellValueFactory(new PropertyValueFactory<>("progress"));
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
                    setGraphic(pb);
                    setText(progress + "%");
                }
            }
        });
        
        taskDueDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        taskDueDateColumn.setCellFactory(column -> new TableCell<Task, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.toString());
                }
            }
        });
        
        // Set up double-click handler
        tasksTable.setRowFactory(tv -> {
            TableRow<Task> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleViewTask(row.getItem());
                }
            });
            return row;
        });
    }

    /**
     * Sets up the bindings between UI controls and ViewModel properties.
     */
    private void setupBindings() {
        // Bind text fields
        ViewModelBinding.bindTextField(nameField, viewModel.subsystemNameProperty());
        ViewModelBinding.bindTextArea(descriptionArea, viewModel.subsystemDescriptionProperty());
        
        // Bind combo boxes
        ViewModelBinding.bindComboBox(statusComboBox, viewModel.statusProperty());
        ViewModelBinding.bindComboBox(responsibleSubteamComboBox, viewModel.responsibleSubteamProperty());
        
        // Bind table items
        tasksTable.setItems(viewModel.getTasks());
        
        // Bind summary fields
        totalTasksLabel.textProperty().bind(Bindings.convert(viewModel.totalTasksProperty()));
        completedTasksLabel.textProperty().bind(Bindings.convert(viewModel.completedTasksProperty()));
        completionPercentageLabel.textProperty().bind(
                Bindings.format("%.1f%%", viewModel.completionPercentageProperty()));
        completionProgressBar.progressProperty().bind(
                viewModel.completionPercentageProperty().divide(100.0));
        
        // Bind buttons
        ViewModelBinding.bindCommandButton(saveButton, viewModel.getSaveCommand());
        
        // Handle cancel button
        cancelButton.setOnAction(event -> closeDialog());
        
        // Handle add task button
        addTaskButton.setOnAction(event -> handleAddTask());
        
        // Handle view task button
        viewTaskButton.setOnAction(event -> {
            Task selectedTask = tasksTable.getSelectionModel().getSelectedItem();
            if (selectedTask != null) {
                handleViewTask(selectedTask);
            }
        });
        
        // Disable view task button when no task is selected
        viewTaskButton.disableProperty().bind(
                tasksTable.getSelectionModel().selectedItemProperty().isNull());
    }

    /**
     * Sets up the controller for creating a new subsystem.
     */
    public void initNewSubsystem() {
        viewModel.initNewSubsystem();
    }

    /**
     * Sets up the controller for editing an existing subsystem.
     * 
     * @param subsystem the subsystem to edit
     */
    public void initExistingSubsystem(Subsystem subsystem) {
        viewModel.initExistingSubsystem(subsystem);
    }

    /**
     * Handles adding a new task to the subsystem.
     */
    private void handleAddTask() {
        Subsystem subsystem = viewModel.getSelectedSubsystem();
        if (subsystem != null) {
            try {
                MainController mainController = MainController.getInstance();
                if (mainController != null) {
                    mainController.showTaskDialog(null, subsystem);
                    
                    // Reload tasks
                    viewModel.getLoadTasksCommand().execute();
                } else {
                    LOGGER.warning("MainController instance is null");
                    showErrorAlert("Error", "Cannot access main controller");
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error showing task dialog", e);
                showErrorAlert("Error", "Failed to open task dialog: " + e.getMessage());
            }
        } else {
            showErrorAlert("Error", "No subsystem selected");
        }
    }

    /**
     * Handles viewing/editing a task.
     * 
     * @param task the task to view/edit
     */
    private void handleViewTask(Task task) {
        try {
            MainController mainController = MainController.getInstance();
            if (mainController != null) {
                mainController.showTaskDialog(task, null);
                
                // Reload tasks
                viewModel.getLoadTasksCommand().execute();
            } else {
                LOGGER.warning("MainController instance is null");
                showErrorAlert("Error", "Cannot access main controller");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error showing task dialog", e);
            showErrorAlert("Error", "Failed to open task dialog: " + e.getMessage());
        }
    }

    /**
     * Closes the dialog.
     */
    private void closeDialog() {
        try {
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error closing dialog", e);
        }
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
     * Gets the subsystem from the ViewModel.
     * 
     * @return the subsystem
     */
    public Subsystem getSubsystem() {
        return viewModel.getSelectedSubsystem();
    }

    /**
     * Gets the ViewModel.
     * 
     * @return the ViewModel
     */
    public SubsystemViewModel getViewModel() {
        return viewModel;
    }

    /**
     * Public method to access initialize for testing.
     */
    public void testInitialize() {
        initialize();
    }
}