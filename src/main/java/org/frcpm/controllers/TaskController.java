// src/main/java/org/frcpm/controllers/TaskController.java
package org.frcpm.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.frcpm.binding.ViewModelBinding;
import org.frcpm.models.Component;
import org.frcpm.models.Project;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.models.TeamMember;
import org.frcpm.services.DialogService;
import org.frcpm.services.ServiceFactory;
import org.frcpm.viewmodels.TaskViewModel;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the Task view.
 * Follows the MVVM pattern by delegating business logic to the TaskViewModel.
 */
public class TaskController {

    private static final Logger LOGGER = Logger.getLogger(TaskController.class.getName());

    // FXML controls
    @FXML
    private Label taskTitleLabel;
    @FXML
    private Label projectLabel;
    @FXML
    private Label subsystemLabel;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private ComboBox<Task.Priority> priorityComboBox;
    @FXML
    private Slider progressSlider;
    @FXML
    private Label progressLabel;
    @FXML
    private CheckBox completedCheckBox;
    @FXML
    private TextField estimatedHoursField;
    @FXML
    private TextField actualHoursField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    // Table views and buttons
    @FXML
    private TableView<TeamMember> assignedMembersTable;
    @FXML
    private TableColumn<TeamMember, String> memberNameColumn;
    @FXML
    private TableColumn<TeamMember, String> memberSubteamColumn;
    @FXML
    private Button addMemberButton;
    @FXML
    private Button removeMemberButton;

    @FXML
    private TableView<Component> requiredComponentsTable;
    @FXML
    private TableColumn<Component, String> componentNameColumn;
    @FXML
    private TableColumn<Component, String> componentPartNumberColumn;
    @FXML
    private TableColumn<Component, Boolean> componentDeliveredColumn;
    @FXML
    private Button addComponentButton;
    @FXML
    private Button removeComponentButton;

    @FXML
    private TableView<Task> dependenciesTable;
    @FXML
    private TableColumn<Task, String> dependencyTitleColumn;
    @FXML
    private TableColumn<Task, Integer> dependencyProgressColumn;
    @FXML
    private Button addDependencyButton;
    @FXML
    private Button removeDependencyButton;

    // ViewModel and services
    private TaskViewModel viewModel = new TaskViewModel();
    private DialogService dialogService = ServiceFactory.getDialogService();
    
    /**
     * Initializes the controller.
     * This method is automatically called after the FXML has been loaded.
     */
    @FXML
    private void initialize() {
        LOGGER.info("Initializing TaskController");

        // Set up priority combo box if not null
        if (priorityComboBox != null) {
            priorityComboBox.getItems().addAll(Task.Priority.values());
        }

        // Set up table columns
        setupTableColumns();

        // Set up bindings
        setupBindings();
    }

    /**
     * Sets up the table columns.
     */
    private void setupTableColumns() {
        // Check for null UI components - important for tests
        if (memberNameColumn == null || memberSubteamColumn == null || 
            componentNameColumn == null || componentPartNumberColumn == null || 
            componentDeliveredColumn == null || dependencyTitleColumn == null || 
            dependencyProgressColumn == null) {
            LOGGER.warning("Table columns not initialized - likely in test environment");
            return;
        }

        memberNameColumn
                .setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(() -> 
                        cellData.getValue().getFullName()));
        memberSubteamColumn.setCellValueFactory(
                cellData -> javafx.beans.binding.Bindings.createStringBinding(() -> cellData.getValue().getSubteam() != null
                        ? cellData.getValue().getSubteam().getName()
                        : ""));

        componentNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        componentPartNumberColumn.setCellValueFactory(new PropertyValueFactory<>("partNumber"));
        componentDeliveredColumn.setCellValueFactory(new PropertyValueFactory<>("delivered"));
        componentDeliveredColumn.setCellFactory(column -> new CheckBoxTableCell<>());

        dependencyTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        dependencyProgressColumn.setCellValueFactory(new PropertyValueFactory<>("progress"));
        dependencyProgressColumn.setCellFactory(column -> new ProgressBarTableCell<>());
    }

    /**
     * Binds the view model to the UI controls.
     */
    private void setupBindings() {
        // Check for null UI components for testability
        if (taskTitleLabel == null || projectLabel == null || subsystemLabel == null || 
            startDatePicker == null || endDatePicker == null || priorityComboBox == null || 
            progressSlider == null || progressLabel == null || completedCheckBox == null || 
            estimatedHoursField == null || actualHoursField == null || descriptionArea == null || 
            saveButton == null || cancelButton == null || assignedMembersTable == null || 
            requiredComponentsTable == null || dependenciesTable == null || addMemberButton == null || 
            removeMemberButton == null || addComponentButton == null || removeComponentButton == null || 
            addDependencyButton == null || removeDependencyButton == null) {
            LOGGER.warning("UI components not initialized - likely in test environment");
            return;
        }

        // Bind text properties
        taskTitleLabel.textProperty().bind(viewModel.titleProperty());
        projectLabel.textProperty().bind(
                javafx.beans.binding.Bindings.createStringBinding(
                        () -> viewModel.getProject() != null ? viewModel.getProject().getName() : "",
                        viewModel.projectProperty()));
        subsystemLabel.textProperty().bind(
                javafx.beans.binding.Bindings.createStringBinding(
                        () -> viewModel.getSubsystem() != null ? viewModel.getSubsystem().getName() : "",
                        viewModel.subsystemProperty()));

        // Bind numeric fields with proper converters
        javafx.util.StringConverter<Number> numberConverter = new javafx.util.converter.NumberStringConverter();
        javafx.beans.binding.Bindings.bindBidirectional(estimatedHoursField.textProperty(), viewModel.estimatedHoursProperty(), numberConverter);
        javafx.beans.binding.Bindings.bindBidirectional(actualHoursField.textProperty(), viewModel.actualHoursProperty(), numberConverter);

        // Bind text area
        ViewModelBinding.bindTextArea(descriptionArea, viewModel.descriptionProperty());

        // Bind date pickers
        ViewModelBinding.bindDatePicker(startDatePicker, viewModel.startDateProperty());
        ViewModelBinding.bindDatePicker(endDatePicker, viewModel.endDateProperty());

        // Bind combo box
        ViewModelBinding.bindComboBox(priorityComboBox, viewModel.priorityProperty());

        // Bind progress slider and completion
        progressSlider.valueProperty().bindBidirectional(viewModel.progressProperty());
        progressLabel.textProperty().bind(javafx.beans.binding.Bindings.format("%d%%", viewModel.progressProperty()));
        completedCheckBox.selectedProperty().bindBidirectional(viewModel.completedProperty());

        // Bind tables to view model collections
        assignedMembersTable.setItems(viewModel.getAssignedMembers());
        requiredComponentsTable.setItems(viewModel.getRequiredComponents());
        dependenciesTable.setItems(viewModel.getPreDependencies());

        // Bind buttons to commands
        ViewModelBinding.bindCommandButton(saveButton, viewModel.getSaveCommand());
        ViewModelBinding.bindCommandButton(cancelButton, viewModel.getCancelCommand());
        ViewModelBinding.bindCommandButton(addMemberButton, viewModel.getAddMemberCommand());
        ViewModelBinding.bindCommandButton(removeMemberButton, viewModel.getRemoveMemberCommand());
        ViewModelBinding.bindCommandButton(addComponentButton, viewModel.getAddComponentCommand());
        ViewModelBinding.bindCommandButton(removeComponentButton, viewModel.getRemoveComponentCommand());
        ViewModelBinding.bindCommandButton(addDependencyButton, viewModel.getAddDependencyCommand());
        ViewModelBinding.bindCommandButton(removeDependencyButton, viewModel.getRemoveDependencyCommand());

        // Setup selection changes
        assignedMembersTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> viewModel.setSelectedMember(newVal));

        requiredComponentsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> viewModel.setSelectedComponent(newVal));

        dependenciesTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> viewModel.setSelectedDependency(newVal));

        // Set up error message binding
        viewModel.errorMessageProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                showErrorAlert("Validation Error", newVal);
                viewModel.errorMessageProperty().set("");
            }
        });

        // Set up cancel button override to close dialog
        cancelButton.setOnAction(event -> closeDialog());
    }

    /**
     * Initializes the controller for a new task.
     * 
     * @param task the new task to create
     */
    public void initNewTask(Task task) {
        viewModel.initNewTask(task.getProject(), task.getSubsystem());
        viewModel.titleProperty().set(task.getTitle());
    }

    /**
     * Initializes the controller for editing an existing task.
     * 
     * @param task the task to edit
     */
    public void initExistingTask(Task task) {
        viewModel.initExistingTask(task);
    }

    /**
     * Shows an error alert with the given message.
     * Protected for testability.
     * 
     * @param title the title of the alert
     * @param message the error message
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
            LOGGER.log(Level.INFO, "Alert would show: {0} - {1}", new Object[] { title, message });
        }
    }

    /**
     * Shows an info alert with the given message.
     * Protected for testability.
     * 
     * @param title the title of the alert
     * @param message the info message
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
            LOGGER.log(Level.INFO, "Alert would show: {0} - {1}", new Object[] { title, message });
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
     * Closes the dialog.
     * Protected for testability.
     */
    protected void closeDialog() {
        try {
            if (saveButton != null && saveButton.getScene() != null) {
                Stage stage = (Stage) saveButton.getScene().getWindow();
                stage.close();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error closing dialog", e);
        }
    }

    /**
     * Compatibility method for legacy code calling setTask
     * 
     * @param task the task to edit
     */
    public void setTask(Task task) {
        initExistingTask(task);
    }

    /**
     * Compatibility method for legacy code calling setNewTask
     * 
     * @param project   the project
     * @param subsystem the subsystem
     */
    public void setNewTask(Project project, Subsystem subsystem) {
        Task newTask = new Task("New Task", project, subsystem);
        initNewTask(newTask);
    }

    /**
     * Public method to access initialize for testing.
     */
    public void testInitialize() {
        initialize();
    }

    /**
     * Gets the task from the ViewModel.
     * 
     * @return the task
     */
    public Task getTask() {
        return viewModel.getTask();
    }

    /**
     * Gets the ViewModel.
     * 
     * @return the ViewModel
     */
    public TaskViewModel getViewModel() {
        return viewModel;
    }

    /**
     * Sets the ViewModel (for testing).
     * 
     * @param viewModel the ViewModel
     */
    public void setViewModel(TaskViewModel viewModel) {
        this.viewModel = viewModel;
    }

    /**
     * Sets the DialogService for this controller.
     * This method is primarily used for testing to inject mock services.
     * 
     * @param dialogService the dialog service to use
     */
    public void setDialogService(DialogService dialogService) {
        this.dialogService = dialogService;
    }

    /**
     * CheckBox cell for Boolean properties in a TableView.
     */
    private static class CheckBoxTableCell<S, T> extends TableCell<S, T> {
        private final CheckBox checkBox = new CheckBox();

        public CheckBoxTableCell() {
            setGraphic(checkBox);
            checkBox.setDisable(true);
        }

        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                checkBox.setSelected((Boolean) item);
                setGraphic(checkBox);
            }
        }
    }

    /**
     * ProgressBar cell for numeric progress properties in a TableView.
     */
    private static class ProgressBarTableCell<S, T> extends TableCell<S, T> {
        private final ProgressBar progressBar = new ProgressBar();

        public ProgressBarTableCell() {
            progressBar.setMaxWidth(Double.MAX_VALUE);
        }

        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
            } else {
                int progress = (Integer) item;
                progressBar.setProgress(progress / 100.0);
                setText(progress + "%");
                setGraphic(progressBar);
            }
        }
    }
}